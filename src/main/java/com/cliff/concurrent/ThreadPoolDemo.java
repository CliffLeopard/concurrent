package com.cliff.concurrent;

import java.util.concurrent.*;

public class ThreadPoolDemo {
    public static void main(String[] args) {
        SynchronousQueue<Runnable> queue;
        Executor threadPoolExecutor = new ThreadPoolExecutor(
                10,
                10,
                2000,
                TimeUnit.SECONDS,
                new LinkedBlockingDeque<>(),
                new ThreadFactory() {
                    @Override
                    public Thread newThread(Runnable r) {
                        return null;
                    }
                },
                new RejectedExecutionHandler() {
                    @Override
                    public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {

                    }
                });

    }
}
