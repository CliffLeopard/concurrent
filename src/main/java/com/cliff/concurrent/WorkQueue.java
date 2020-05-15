package com.cliff.concurrent;

import java.util.PriorityQueue;
import java.util.concurrent.*;

public class WorkQueue {
    public static void main(String[] args) {
        BlockingQueue<String> blockingQueue;
        BlockingDeque<String> deque;
        ArrayBlockingQueue<String> arrayBlockingQueue;
        DelayQueue<DelayEntry> delayQueue;
        PriorityBlockingQueue<String> priorityBlockingQueue;


    }

    class DelayEntry implements Delayed{
        @Override
        public long getDelay(TimeUnit unit) {
            return 0;
        }

        @Override
        public int compareTo(Delayed o) {
            return 0;
        }
    }
}
