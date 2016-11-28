package com.dom.lock;

import com.dom.basic.ZkClient;
import com.google.common.base.Strings;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.data.Stat;

import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * Date: 16/11/25
 * Author: dom
 * Usage: 更通用的分布式锁,用于保护在分布式环境下,一些竞争激烈的资源在同一时间只能被某一个机器的一个任务使用
 * 步骤如下:
 * <p>
 * 1.创建一个zk节点'N',可以以资源名称标识
 * 2.每个竞争者在'N'下创建名为'L_'的类型为EPHEMERAL_SEQUENTIAL的节点
 * 3.随后每个竞争者调用getChildren("N")方法获取所有'N'节点下的子节点,通过zk自增的节点名称,找出其中最小的节点
 * 4.判断最小节点是否是自己,如果是,则表示获取到锁,开始执行任务,如果不是,观察比它小一位的节点
 * 4.5.如果上一步执行了观察,直到比它小一位的节点执行完任务并删除了自己,它收到通知,表示自己已经是最小的节点,获取锁,开始执行任务
 * 5.任务执行结束,删除自己
 * </p>
 * 整个思路其实就是车站一群人等车,车来了,谁跑最快谁先买票,后面的人只要看前面的人依次执行买票程序即可
 */
public class DistributedLock extends AbstractLock {

    public static final String CHILD_PATH = "seqLock_";
    private String parentPathName;
    private List<String> childList;
    private String curPath;
    private String curNode;
    private boolean hasLock;

    @Override
    public AbstractLock connect(String parentPathName) {
        this.parentPathName = parentPathName;
        return super.connect(parentPathName);
    }

    /**
     * @param path 这里使用统一的path前缀,忽略传入path
     */
    @Override
    public synchronized boolean tryLock(String path) {
        latch = new CountDownLatch(1);
        hasLock = getLock();
        if (hasLock) return true;
        waitForLock(curNode);
        return hasLock;
    }

    private boolean getLock() {
        if (Strings.isNullOrEmpty(curPath))
            curPath = client.createEphemeraSeqNode(parentPathName + ZkClient.SP + CHILD_PATH, null);
        if (Strings.isNullOrEmpty(curPath)) return false;
        childList = client.getChildren(parentPathName);
        curNode = curPath.split("/")[2];
        //如果当前任务的节点被删除了,重新生成一个节点
        if(!childList.contains(curNode)) {
            this.curPath = null;
            return getLock();
        }
        childList.sort((o1, o2) -> Integer.parseInt(o1.split("_")[1]) - Integer.parseInt(o2.split("_")[1]));
        if (childList.get(0).equals(curNode)) {
            System.out.println(Thread.currentThread().getName() + "has got the lock and task begin...");
            return true;
        }
        return false;
    }

    private void waitForLock(String curNode) {
        System.out.println(Thread.currentThread().getName() + "is waiting for lock...");
        String preNode = childList.get(childList.indexOf(curNode) - 1);
        //为前一个节点设置监听事件
        Stat stat = client.getStat(parentPathName + ZkClient.SP + preNode, event -> {
            latch.countDown();
            hasLock = getLock();//当监听的节点释放后,重新判断当前节点是否获取到锁
        });
        try {
            if (stat != null)
                latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
