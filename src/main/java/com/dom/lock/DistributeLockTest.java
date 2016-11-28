package com.dom.lock;

/**
 * Date: 16/11/28
 * Author: dom
 * Usage:
 */
public class DistributeLockTest {

    public static void main(String[] args) {

        TaskThreadSimulation2 task1 = new TaskThreadSimulation2();
        TaskThreadSimulation2 task2 = new TaskThreadSimulation2();
        task1.start();
        task2.start();
    }
}

class TaskThreadSimulation2 extends Thread {

    @Override
    public void run() {
        AbstractLock lock = new DistributedLock().connect("getMoney");
        try {
            while(!lock.tryLock("")) {
                System.out.println(this.getName() + "get lock fail and retry");
            }
            doTask();
            lock.releaseLock();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void doTask() throws InterruptedException {
        System.out.println(this.getName() + ": doing something!");
        Thread.sleep(5000);
    }
}
