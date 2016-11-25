package com.dom.lock;

import com.dom.basic.ZkClient;
import com.dom.utils.Properties;

/**
 * Date: 16/11/25
 * Author: dom
 * Usage: 基于zookeeper实现的不同用途的分布式锁的抽象,定义一些共用方法
 */
public abstract class AbstractLock {

    protected ZkClient client;

    public AbstractLock() {
    }

    public AbstractLock connect(String parentLockPath) {
        client = new ZkClient();
        client.connect(Properties.CON_PATH_1);
        if (!client.exist(parentLockPath)) client.createPersistentNode(parentLockPath, "");
        return this;
    }

    public abstract boolean tryLock(String path);

    /**
     * 当获取到锁的客户端执行完任务之后,关闭连接
     */
    public void releaseLock() {
        client.close();
    }
}
