package com.dom.lock;

import com.google.common.base.Strings;

import java.util.List;
import java.util.stream.Collectors;

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
 * 整个思路其实就是车站一群人等车,车来了,谁跑最快谁先买票,后面的人只要看前面的人一次执行买票程序即可
 */
public class DistributedLock extends AbstractLock {

    public static final String CHILD_PATH = "seqLock_";
    private String parentPathName;

    public AbstractLock connect(String parentPathName) {
        this.parentPathName = parentPathName;
        return connect(parentPathName);
    }


    /**
     *
     * @param path 这里使用统一的path前缀,忽略传入path
     */
    @Override
    public boolean tryLock(String path) {
        String curPath = client.createEphemeraSeqNode(CHILD_PATH, null);
        if(Strings.isNullOrEmpty(curPath)) return false;
        List<String> cList = client.getChildren(parentPathName);
        List<Integer> idList = cList.stream().map(child -> Integer.parseInt(child.split("_")[1])).collect(Collectors.toList());
        idList.sort((o1, o2) -> o1 - o2);
        if((CHILD_PATH + idList.get(0)).equals(curPath)) {
            return true;
        }
        //TODO 获取当前节点的前一个
        String preNode = cList.get(cList.indexOf(curPath) - 1);
        return false;
    }

}
