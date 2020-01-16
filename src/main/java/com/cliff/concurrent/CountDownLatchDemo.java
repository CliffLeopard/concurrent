package com.cliff.concurrent;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class CountDownLatchDemo {
    private int N = 100;

    public static void main(String[] args) {
        CountDownLatchDemo demo = new CountDownLatchDemo();
        try {
            demo.driver();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void doSomethingElse() throws InterruptedException {
        Thread.sleep(1000);
    }

    public void driver() throws InterruptedException {
        CountDownLatch startSignal = new CountDownLatch(1);
        CountDownLatch doneSignal = new CountDownLatch(N);
        for (int i = 0; i < N; ++i)
            new Thread(new Worker(startSignal, doneSignal)).start();
        doSomethingElse();
        startSignal.countDown();
        doSomethingElse();
        doneSignal.await();
    }

    class Worker implements Runnable {
        private final CountDownLatch startSignal;
        private final CountDownLatch doneSignal;

        Worker(CountDownLatch startSignal, CountDownLatch doneSignal) {
            this.startSignal = startSignal;
            this.doneSignal = doneSignal;
        }

        public void run() {
            try {
                startSignal.await();
                doWork();
                doneSignal.countDown();
            } catch (InterruptedException ex) {
            }
        }

        void doWork() {

        }
    }

    class Driver2 { // ...
        void main() throws InterruptedException {
            CountDownLatch doneSignal = new CountDownLatch(N);
            Executor e = Executors.newFixedThreadPool(100);

            for (int i = 0; i < N; ++i) // create and start threads
                e.execute(new WorkerRunnable(doneSignal, i));

            doneSignal.await();           // wait for all to finish
        }
    }

    class WorkerRunnable implements Runnable {
        private final CountDownLatch doneSignal;
        private final int i;

        WorkerRunnable(CountDownLatch doneSignal, int i) {
            this.doneSignal = doneSignal;
            this.i = i;
        }

        public void run() {
            try {
                doWork(i);
                doneSignal.countDown();
            } catch (InterruptedException ex) {
            } // return;
        }

        void doWork(int i) throws InterruptedException {

        }
    }
}
