package com.dom.lock;

/**
 * Date: 16/11/24
 * Author: dom
 * Usage:
 */
public class SimpleLockTest {

    public static void main(String[] args) {
        TaskThreadSimulation s1 = new TaskThreadSimulation("t1");
        TaskThreadSimulation s2 = new TaskThreadSimulation("t2");
        TaskThreadSimulation s3 = new TaskThreadSimulation("t3");
        s1.start();
        s2.start();
        s3.start();
    }
}

class TaskThreadSimulation extends Thread {

    private String threadName;

    public TaskThreadSimulation(String threadName) {
        super();
        this.threadName = threadName;
    }

    @Override
    public void run() {
        SimpleLock lock = new SimpleLock().connect();
        if (lock.tryLock("taskLock")) {
            System.out.println(threadName + "has got the lock and task begin...");
            try {
                System.out.println("I'm working...");
                Thread.sleep(30 * 1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("task end");
            lock.releaseLock();
        }
    }
}
