package com.cliff.concurrent;

import java.util.concurrent.locks.LockSupport;
import java.util.concurrent.locks.ReentrantLock;

public class LockSupportDemo {
    public static void main(String[] args) {
        LockSupportDemo demo = new LockSupportDemo();
        demo.showDemo();
    }

    private final String obj = "Hahhah";

    private ReentrantLock lock1 = new ReentrantLock();
    private ReentrantLock lock2 = new ReentrantLock();

    public void showDemo() {
        Runnable run1 = new Runnable() {
            public void run() {
                print("run1 prepare to get lock1");
                lock1.lock();
                print("run1 got lock1, and to do something need time");
                LockSupport.park(obj);

                if (Thread.interrupted()) {
                    print("interrupted");
                } else {
                    print("not interrupted");
                }

                print(Thread.currentThread().isInterrupted() + "");
                print("run1 wake up, and prepare to get lock2");
                try {
                    lock2.lockInterruptibly();
                    System.out.println("run1 got lock2");
                    lock2.unlock();
                    System.out.println("run1 release lock2");
                    lock1.unlock();
                    System.out.println("run1 release lock1");
                } catch (InterruptedException e) {
                    System.out.println("run1 lock2 interrupted unlock lock1");
                    lock1.unlock();
                }
            }
        };

        Runnable run2 = new Runnable() {
            public void run() {
                print("run2 prepare to get lock2");
                lock2.lock();
                print("run2 got lock2, and sleep");
                LockSupport.park("run2");
                print("run2 wake up, and prepare to get lock1");
                lock1.lock();
                print("run2 got lock1");
                lock1.unlock();
                print("run2 release lock1");
                lock2.unlock();
                print("run2 release lock2");
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
//        LockSupport.unpark(t1);
//        t1.interrupt();
        print(LockSupport.getBlocker(t1).toString());
    }

    private void print(String str) {
        System.out.println(str);
    }
}
