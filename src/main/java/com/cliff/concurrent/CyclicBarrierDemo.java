package com.cliff.concurrent;

import java.util.Random;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

public class CyclicBarrierDemo {

    public static void main(String[] args) {
        final int N = 10;
        Thread[] threads = new Thread[N];
        boolean flag = false;
        CyclicBarrier cyclicBarrier = new CyclicBarrier(N, new BarrierRun(flag, N));
        System.out.println("部队集合:");
        for (int i = 1; i <= N; i++) {
            System.out.println("士兵:" + i + "报道!");
            threads[i - 1] = new Thread(new Soldier(cyclicBarrier, "士兵" + i));
            threads[i - 1].start();
        }
    }

    public static class Soldier implements Runnable {
        private CyclicBarrier cyclicBarrier;
        private String soldierName;

        Soldier(CyclicBarrier cyclicBarrier, String soldierName) {
            this.cyclicBarrier = cyclicBarrier;
            this.soldierName = soldierName;
        }

        @Override
        public void run() {
            try {
                cyclicBarrier.await();
                doWork();
                cyclicBarrier.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (BrokenBarrierException e) {
                e.printStackTrace();
            }
        }

        private void doWork() {
            try {
                Thread.sleep(Math.abs(new Random().nextInt() % 10000));
                System.out.println("士兵:" + (cyclicBarrier.getNumberWaiting()+1) + "完成任务!");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public static class BarrierRun implements Runnable {
        boolean flag;
        int N;

        public BarrierRun(boolean flag, int N) {
            this.flag = flag;
            this.N = N;
        }

        @Override
        public void run() {
            if (flag) {
                System.out.println("司令【士兵" + N + "个，任务完成！】");
            } else {
                System.out.println("司令【士兵" + N + "个，集合完毕！】");
                flag = true;
            }

        }
    }
}
