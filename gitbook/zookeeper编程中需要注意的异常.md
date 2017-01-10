# zookeeper编程中需要注意的异常

线上日志差点被zk撑爆，心有余悸，还是要完善代码，在此记录zk开发中遇到的的坑，和要预防的问题，要写出一个健壮的应用。

### 首先是zookeeper api的一些特性

1. 创建客户端

   ```java
   ZooKeeper zooKeeper = new ZooKeeper(String connectString, int sessionTimeout, Watcher watcher)
   ```

   这是创建zk客户端的一种方式，其中的`sessionTimeOut`设置的是连接的session超时时间，当设置的时间到之后，客户端还会占用这个链接，此时如果操作节点，会报出`SessionTimeOutException`,所以zk客户端操作完成一定要显式关闭！

2. 节点操作

   ```java
   create()
   exists()
   getChildren()
   delete()
   close()
   ```

都会抛出异常，抛出异常时客户端会自动断开连接。

这两个特性可以通过终端的zkCli看到。

### 对于分布式锁的一些坑预防思路

1. session超时，在获取节点信息或者创建节点或者任何对节点操作时，出现`ConnectionLoss`异常\(通过设置默认`sessionTimeout=1`\)可以重现次问题，所有异常处理一定要到位，最好在最外层的try...finally中强制close

2. 当前客户端连接数超出zk设置的最大连接数，异常堆栈信息如下：

   ```java
   java.io.IOException: Connection reset by peer
    at sun.nio.ch.FileDispatcherImpl.read0(Native Method)
    at sun.nio.ch.SocketDispatcher.read(SocketDispatcher.java:39)
    at sun.nio.ch.IOUtil.readIntoNativeBuffer(IOUtil.java:223)
    at sun.nio.ch.IOUtil.read(IOUtil.java:192)
    at sun.nio.ch.SocketChannelImpl.read(SocketChannelImpl.java:380)
    at org.apache.zookeeper.ClientCnxnSocketNIO.doIO(ClientCnxnSocketNIO.java:68)
    at org.apache.zookeeper.ClientCnxnSocketNIO.doTransport(ClientCnxnSocketNIO.java:366)
    at org.apache.zookeeper.ClientCnxn$SendThread.run(ClientCnxn.java:1081)
   ```

   这个异常并没有被zk客户端抛出，此时如果不强制调用close方法，会不停尝试重连，日志输出到地老天荒，可以和坑\[1\]一起处理，因为即使出现这个异常，还是可执行节点操作，捕获节点操作异常后close客户端即可避免无限输出。

3. 创建节点后，任务中出现`RuntimeException`，一定要把任务代码块进行异常处理，在finally中关闭连接。

4. 当一个任务持有一个锁一直不释放，极有可能是任务执行异常，此时会阻塞后续任务执行，需要设置一个持有锁最长时间。暂时没有思路。



