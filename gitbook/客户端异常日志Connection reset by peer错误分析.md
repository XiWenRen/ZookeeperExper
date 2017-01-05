# 客户端异常日志Connection reset by peer错误分析
1月4日出现131服务器上大量输出zk错误日志`Connection reset by peer`，查看命令发现大量`ESTABLISH off`的连接，而且使用

在`zk`中，客户端大量报错`Connection reset by peer`，通常是单个ip连接到zk的客户端数量超出限制(在zoo.cfg配置文件中的`maxClientCnxns=`设置)，zk服务端主动关闭连接，然后客户端不断重试，导致日志爆炸增长。

排查过程如下：

首先配置本地zk文件中的`maxClientCnxns=1`，启动服务，使用两个zkCli尝试连接，第二个客户端不断输出`Connection reset by peer`，说明

当连接数量达到配置最大值时，使用zk四字命令`echo cons | nc ip 2181`不能返回信息。
在客户端机器上使用`netstat -ano | grep 2181`命令查看所有连接到zk服务器的连接状态。
