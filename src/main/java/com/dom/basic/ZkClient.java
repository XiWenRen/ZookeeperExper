package com.dom.basic;

import org.apache.zookeeper.*;
import org.apache.zookeeper.data.ACL;
import org.apache.zookeeper.data.Stat;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Date: 16/11/23
 * Author: dom
 * Usage: 自定义的zookeeper客户端类,提供一些基础方法
 */
public class ZkClient implements Watcher {
    //default session time out
    public static final int DSTO = 3600 * 1000;
    //zk的根目录/目录分隔符
    public static final String SP = "/";

    private ZooKeeper zooKeeper;

    public void connect(String connectPath, int sessionTimeOut, Watcher watcher) {
        try {
            zooKeeper = new ZooKeeper(connectPath, sessionTimeOut, watcher);
        } catch (IOException e) {
//            e.printStackTrace();
            System.out.println("连接zk服务器失败.cause:" + e.getMessage());
        }
    }

    public void connect(String connectPath) {
        connect(connectPath, DSTO, this);
    }

    public boolean createPersistentNode(String path, String nodeInfo) {
        return createNode(path, nodeInfo, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
    }

    public boolean createEphemeralNode(String path, String nodeInfo) {
        return createNode(path, nodeInfo, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
    }

    public boolean createPersistentSeqNode(String path, String nodeInfo) {
        return createNode(path, nodeInfo, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT_SEQUENTIAL);
    }

    public boolean createEphemeraSeqNode(String path, String nodeInfo) {
        return createNode(path, nodeInfo, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
    }

    public boolean createNode(String path, String nodeInfo, ArrayList<ACL> acl, CreateMode mode) {
        try {
            zooKeeper.create(dealPath(path), nodeInfo.getBytes(), acl, mode);
            return true;
        } catch (KeeperException | InterruptedException e) {
            System.out.println("创建节点" + path + "失败. cause:" + e.getMessage());
        }
        return false;
    }

    public void deleteNode(String path) {
        try {
            zooKeeper.delete(dealPath(path), -1);
        } catch (InterruptedException | KeeperException e) {
            System.out.println("删除节点" + path + "失败. cause:" + e.getMessage());
        }
    }

    public boolean exist(String path) {
        try {
            Stat stat = zooKeeper.exists(dealPath(path), false);
            return stat != null;
        } catch (KeeperException | InterruptedException e) {
            System.out.println("获取节点信息失败. cause:" + e.getMessage());
        }
        return false;
    }

    public void close() {
        try {
            zooKeeper.close();
        } catch (InterruptedException e) {
            System.out.println("关闭zk连接失败. cause" + e.getMessage());
        }
    }

    private String dealPath(String path) {
        if (!path.startsWith(SP)) path = SP + path;
        if(path.endsWith(SP)) path = path.substring(0, path.length() - 1);
        return path;
    }

    @Override
    public void process(WatchedEvent event) {
        //doNothing
    }
}
