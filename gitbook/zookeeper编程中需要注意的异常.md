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

1. 排它锁
1.
