# 客户端异常日志Connection reset by peer错误分析
1月4日出现一台生产服务器上大量输出zk错误日志`Connection reset by peer`，一天有大约62G，查看命令发现大量`ESTABLISH off`的连接

在`zk`中，客户端大量报错`Connection reset by peer`，通常是单个ip连接到zk的客户端数量超出限制(在zoo.cfg配置文件中的`maxClientCnxns=`设置)，zk服务端主动关闭连接，然后客户端不断重试，导致日志爆炸增长。

排查过程如下：

首先配置本地zk文件中的`maxClientCnxns=1`，启动服务，使用两个`zkCli`尝试连接，第二个客户端不断输出`Connection reset by peer`，说明错误原因确实是因为单客户端对服务器连接数量超过配置，用`netstat -ano | grep 2181`查看状态发现有一个本机ip处于`ESTABLISH off`状态。

然后参考TCP连接状态详解，`ESTABLISH off`状态说明客户端连接未释放。由此想到线上问题应该是因为新写的zk锁在某些情况下没有close

然后设置`maxClientCnxns=10`并重启zk,使用最新代的zk锁代码模拟4个定时任务不断尝试获取/释放，并设置任务执行时间超时，10分钟后仍没有出现超出连接数，查看端口状态，所有连接的状态都是`TIME WAIT`，说明所有连接都已经正确断开，尝试了很多方案都无法重现

然后想到之前一次发布误上了第一次修改的zk代码，先在zk中创建节点`/DistributedLock/testLock`,使用这部分代码测试创建相同节点，很快就重现了错误，发现所有创建节点失败的client连接都处于`ESTABLISH off`状态，部分代码如下:
```java
public class SimpleLock
public synchronized boolean tryLock(String lockName) {
        lockName = LOCK_PATH + ZkClient.SP + lockName;
        if (client.exist(lockName)) return false;//此处没有关闭连接
        String lockPath = client.createEphemeralNode(lockName, null);
        boolean lockCreated = !Strings.isNullOrEmpty(lockPath);
        if (!lockCreated) client.close();
        return lockCreated;
    }
```
在判断节点已存在后直接`return`，没有显式`close()`，在定时任务执行完毕后，zk一直保持`ESTABLISH`状态。

#### 所以重现线上情景如下：

服务器集群到定时任务时间开始竞争锁，竞争锁可能有两种情况：

节点已存在，节点未存在

对于节点未存在的情况下所有服务器同时创建，失败者退出，断开连接`if (!lockCreated) client.close();`

但是当节点已存在，获取节点的服务器正在执行任务时，就会出现上面的情景。如果是按照该任务的本意，每天执行一次，需要连续100天才会出现超出连接数，但是定时参数一直有个错误`@Scheduled(cron = "* 10 1 * * ? ")`,左边第一个`*`表示每秒执行一次，所以这个配置导致从1点10分开始每秒执行获取锁，持续一分钟，所以每天会出现60个未释放连接数，第二天就会超出最大连接数。正确写法应为`@Scheduled(cron = "0 10 1 * * ? ")`

错误总结：

第一版代码有缺陷且测试不全面

本来这部分代码不应该出现在`dev`分支的，误上代码，在上线前没有检查一遍上线代码质量

定时任务一直有问题却没有发现