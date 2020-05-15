# Java Thread

## 并发队列(BlockingQueue)

- BlockingDeque: 双端队列接口
  - LinkdedBlockingQueue:BlockingDeque的链表实现

- ArrayBlockingQueue
- DelyQueue
- DelayedWorkQueue
- PriorityBlockingQueue
- SynchronousQueue
- TransferQueue
  - LinkedTransferQueue

## 拒绝策略

- AbortPolicy
- CallerRunsPolicy
- DiscardOldestPolicy
- DiscardPolicy

## 构造自己的线程池

 使用ThreadLocalPool创建自己的线程池,使用六个参数来进行配置.包括一下几种

- coreThreadumber
- notCoreThreadNUmber
- notCoreTime
- notCoreTimeUnit
- workQuene:
- threadFactory
- ExceptionInterceptor



## Fork/Join框架

## JDK中的并发容器

### ConcurrentHashMap

###  CopyOnWriteArrayList

### ConcurrentLinkedQueue

### BlockingQueue

### ConcurrentSkipListMap

