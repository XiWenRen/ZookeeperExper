package com.dom.basic;

import com.google.common.base.Strings;
import org.apache.zookeeper.*;
import org.apache.zookeeper.data.ACL;
import org.apache.zookeeper.data.Stat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Date: 16/11/23
 * Author: dom
 * Usage: 自定义的zookeeper客户端类,提供一些基础方法
 * 只是用作简单使用,所有zk的异常都被默认处理为输出信息
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

    /**
     * 创建一个永久节点
     */
    public String createPersistentNode(String path, String nodeInfo) {
        return createNode(path, nodeInfo, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
    }

    /**
     * 创建一个临时节点
     */
    public String createEphemeralNode(String path, String nodeInfo) {
        return createNode(path, nodeInfo, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
    }

    /**
     * 创建一个永久自增节点
     */
    public String createPersistentSeqNode(String path, String nodeInfo) {
        return createNode(path, nodeInfo, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT_SEQUENTIAL);
    }

    /**
     * 创建一个临时自增节点
     */
    public String createEphemeraSeqNode(String path, String nodeInfo) {
        return createNode(path, nodeInfo, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
    }

    /**
     * @param path     节点路径
     * @param nodeInfo 节点携带的数据
     * @param acl      权限
     * @param mode     节点形态
     * @return 创建的节点名称
     */
    public String createNode(String path, String nodeInfo, ArrayList<ACL> acl, CreateMode mode) {
        try {
            byte[] bytes = Strings.isNullOrEmpty(nodeInfo) ? new byte[0] : nodeInfo.getBytes();
            return zooKeeper.create(dealPath(path), bytes, acl, mode);
        } catch (KeeperException | InterruptedException e) {
            System.out.println("创建节点" + path + "失败. cause:" + e.getMessage());
        }
        return null;
    }

    public List<String> getChildren(String path) {
        try {
            return zooKeeper.getChildren(dealPath(path), false);
        } catch (KeeperException | InterruptedException e) {
            System.out.println("获取" + path + "路径下子节点失败. cause:" + e.getMessage());
        }
        return Collections.emptyList();
    }

    /**
     * 删除节点
     *
     * @param path 指定的节点路径,此方法一定会删除节点,而忽略节点的版本
     */
    public void deleteNode(String path) {
        try {
            zooKeeper.delete(dealPath(path), -1);
        } catch (InterruptedException | KeeperException e) {
            System.out.println("删除节点" + path + "失败. cause:" + e.getMessage());
        }
    }

    /**
     * 判断节点是否存在
     */
    public boolean exist(String path) {
        Stat stat = getStat(path);
        return stat != null;
    }

    public Stat getStat(String path) {
        return getStat(path, null);
    }

    public Stat getStat(String path, Watcher watcher) {
        try {
            return zooKeeper.exists(dealPath(path), watcher);
        } catch (KeeperException | InterruptedException e) {
            System.out.println("获取节点信息失败. cause:" + e.getMessage());
        }
        return null;
    }

    public void close() {
        try {
            zooKeeper.close();
        } catch (InterruptedException e) {
            System.out.println("关闭zk连接失败. cause" + e.getMessage());
        }
    }

    /**
     * 预处理要操作的节点路径,保证给zk的是标准的路径格式,
     */
    private String dealPath(String path) {
        if (!path.startsWith(SP)) path = SP + path;
        if (path.endsWith(SP)) path = path.substring(0, path.length() - 1);
        return path;
    }

    @Override
    public void process(WatchedEvent event) {
        //doNothing
        System.out.println("process");
    }
}
