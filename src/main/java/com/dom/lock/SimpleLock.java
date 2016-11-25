package com.dom.lock;

import com.dom.basic.ZkClient;
import com.dom.utils.Properties;

/**
 * Date: 16/11/24
 * Author: dom
 * Usage: 简单的分布式竞争锁,用于保证同一时间只有一个进程有权限执行任务,用于管理分布式环境下的定时任务
 * 流程如下:
 * 1.保证zk根部目录下有一个PERSISTENT的/lock节点,如果没有,就创建一个(非必须,这样是为了便于管理)
 * 2.所有执行任务的客户端在/lock节点下创建一个EPHEMERAL的节点,节点名字标识出当前任务即可,如果创建成功则标识获取到锁
 * 3.所有没有创建成功的客户端关闭连接
 * 4.获取到锁的客户端执行完任务,关闭连接,EPHEMERAL节点自动删除
 */
public class SimpleLock {

    public static final String LOCK_PATH = "lock";

    private ZkClient client;

    public SimpleLock connect() {
        client = new ZkClient();
        client.connect(Properties.CON_PATH_1);
        if(!client.exist(LOCK_PATH)) client.createPersistentNode(LOCK_PATH, "");
        return this;
    }

    /**
     * 尝试争取锁,如果没有争取到,说明此客户端无权限执行任务,直接关闭连接
     * 获取锁的客户端在执行完任务之后需要释放
     * @param lockName 唯一的任务标识
     * @return 是否获取锁
     */
    public boolean tryLock(String lockName) {
        lockName = LOCK_PATH + ZkClient.SP + lockName;
        if(client.exist(lockName)) return false;
        boolean b = client.createEphemeralNode(lockName, "");
        if(!b) client.close();
        return b;
    }

    /**
     * 当获取到锁的客户端执行完任务之后,关闭连接
     */
    public void releaseLock() {
        client.close();
    }

}
