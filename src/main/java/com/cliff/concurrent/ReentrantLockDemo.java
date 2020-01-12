package com.cliff.concurrent;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class ReentrantLockDemo {
    public static void main(String[] args) {
        ReentrantLockDemo demo = new ReentrantLockDemo();
//        demo.reactInterrupt();
        demo.useCondition();
    }

    private ReentrantLock lock1 = new ReentrantLock();
    private ReentrantLock lock2 = new ReentrantLock();

    /**
     * 说明，此程序为了制造死锁的状态制造了两把锁lock1,lock2
     * t1内，先申请lock1，执行耗时操作，在申请lock2(过程中不释放lock1)
     * t2内，先申请lock2，执行耗时操作，在申请lock1(不释放lock2)
     * 造成在t1申请lock2时一直在等待被t2持有的lock2,同理t2在等待lock1 这样就造成了死锁。
     * 但是，我们在t1申请lock2时，申请了可中断锁，所以将t1中断后，执行catch，t1执行undo操作，并释放lock1.
     * 此时t2获取到lock1，死锁解除。
     */
    public void reactInterrupt() {
        Runnable run1 = new Runnable() {
            public void run() {
                System.out.println("run1 prepare to get lock1");
                lock1.lock();
                System.out.println("run1 got lock1, and to do something need time");
                doSomething("run1");
                System.out.println("run1 wake up, and prepare to get lock2");
                try {
                    lock2.lockInterruptibly();
                    System.out.println("run1 got lock2");
                    lock2.unlock();
                    System.out.println("run1 release lock2");
                    lock1.unlock();
                    System.out.println("run1 release lock1");
                } catch (InterruptedException e) {
                    System.out.println("run1 lock2 interrupted unlock lock1");
                    undoSomething("run1");
                    lock1.unlock();
                }
            }
        };

        Runnable run2 = new Runnable() {
            public void run() {
                System.out.println("run2 prepare to get lock2");
                lock2.lock();
                System.out.println("run2 got lock2, and sleep");
                doSomething("run2");
                System.out.println("run2 wake up, and prepare to get lock1");
                lock1.lock();
                System.out.println("run2 got lock1");
                lock1.unlock();
                System.out.println("run2 release lock1");
                lock2.unlock();
                System.out.println("run2 release lock2");
            }
        };

        Thread t1 = new Thread(run1);
        Thread t2 = new Thread(run2);
        t1.start();
        t2.start();

        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        t1.interrupt();
    }

    /**
     * 此方法演示了Condition的wait和notify的使用，以及interrupt处理其实和synchronize没有太多区别
     */
    public void useCondition() {
        final Condition condition1 = lock1.newCondition();

        Runnable run = new Runnable() {
            public void run() {
                System.out.println("run begin run, prepare to get lock1");
                lock1.lock();
                System.out.println("run got  lock1 and begin wait");
                try {
                    condition1.await();
                    System.out.println("run get notify go on");
                } catch (InterruptedException e) {
                    System.out.println("run interruptedException");
                    e.printStackTrace();
                } finally {
                    System.out.println("run  release lock");
                    lock1.unlock();
                }

                System.out.println("run finish");
            }
        };
        Thread t1 = new Thread(run);
        t1.start();

        System.out.println("main  begin sleep -------------------");
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            System.out.println("main InterruptedException ");
            e.printStackTrace();
        }
        System.out.println("main wake up, prepare to get lock ----------");
        lock1.lock();
        System.out.println("main got lock");
//        condition1.signal();
        t1.interrupt();
        lock1.unlock();
        System.out.println("main release lock and finish");
    }

    private void doSomething(String label) {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            System.out.print(label + ":  interrupt:");
            e.printStackTrace();
        }
    }

    private void undoSomething(String label) {

    }



}
