# 客户端异常日志Connection reset by peer错误分析

在`zk`中，客户端大量报错`Connection reset by peer`，通常是单个ip链接到zk的客户端数量超出限制(在zoo.cfg配置文件中),zk服务端主动关闭连接，所以错误信息