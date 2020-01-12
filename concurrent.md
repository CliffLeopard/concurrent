# Java高并发

## 一、进入并行世界

### 1. 概念

* 同步/异步

* 并发/并行

  并发:多个任务之间交替进行. 并行:多个任务同时进行

* 临界区

  可以被多个线程同时使用的公共资源.

* 阻塞/非阻塞

  当一个线程占据了临界区资源.其他所有需要这个资源的线程都要挂起,这时位阻塞.没有一个线程可以妨碍其他线程则为非阻塞.

* 死锁/饥饿/活锁

  死锁:比如哲学家就餐问题.

  饥饿:在非公平锁的情况下,低优先级的线程长久得不到锁,则为饥饿状态.

  活锁:两个线程同时请求锁,右一直相互谦让,则造成活锁.

### 2. 并发级别

* 阻塞

  不论是synnchronized还是ReentrantLock,在一个线程获取锁时,其他线程都为阻塞状态.直到占有锁为止.

* 无饥饿

  公平锁可以实现无饥饿

* 无障碍

  乐观锁,依赖一种一致性标记,如果被修改了则回退. 有可能造成大量线程回退.

* 无锁

  一个线程可以在有限步骤内完成.可使用自旋. ReentrantLock等待队列中的第一个线程则一直在自旋.

* 无等待

  所以线程都可以在有限步骤内完成,则无锁.

### 3. JMM-java内存模型

* 原子性(Atomicity)

  一次读写操作不可中断,则为原子性.比如对int赋值,操作为原子性.32位系统下double的赋值为非原子性.

* 可见性(Visibility)

  由于编译器优化或者硬件优化,A线程在修改了数据D后没有回写到内存,而是写入了cache或者寄存器,那么B线程读取的数据D就是旧数据,新数据对B不可见.这样就会造成问题.

* 有序性(Ordering)

  指令重拍造成的问题. 当然指令重排在串行语义(即单线程运行时)会保证不会出现问题. 在执行汇编语言指令由于寄存器IO操作可能会造成CPU等待,为了提高效率,是使用流水线的方式来执行指令的,这就造成了指令重排.

  ```java
  public void write(){
    a =1;
    flag = true;
  }
  public int read(){
    if(flag){
      int i = a +1;
    }
    ...
    return i;
  }
  a = 0;
  write();
  read();   // 正常串行执行下,输出2,但是在高并发情况下 i = 1的情况是有可能发生的. 因为指令重排可能会在write()中,排为flag=true;a = 1;

  ```

* **Volatile关键字,就是用来解决可见性和有序性的问题的.**

## 二、Java并行程序基础

### 1. Thread

#### Thread创建

* extends Thread,重写run方法
* implements Runnable,传入Thread.  将一个实例化的runnable传递到多个Thread时,多个Thread共享一个runnable,则只需在runnable中实现线程锁即可.

#### Thread中断

* `thread.interrupt()`

  实例方法,中断线程. 即使调用了该函数,线程依然不会被中断,因为此方法的作用是标记当前线程为中断. 所以要在线程的代码逻辑中通过下面函数的判断进行处理.

* `thread.isInterrupted()`

  判断线程是否被打断

* `Thread.interrupted()` 

  类静态方法,判断是否中断,并清除当前中断状态.

* `Thread.sleep()`

  如果在执行sleep的过程中线程线程被interrupted,sleep函数会抛出InterruptedException异常,并且清除中断标记.因此可以配合thread.interrupt()进行线程中断.在线程thread中执行Thread.sleep()之后,线程会出让CPU时间片,但线程不会释放锁,线程会进入TIEMD_WAITING状态.

* `obj.wait()`/`ob j.notify()`/`obj.notifyAll()`

  这三个方法,都是配合synchronized 进行线程间通讯的.即obj必须是synchronized修饰的锁.当线程执行obj.wait()之后,线程会出让cpu时间片,并且让出锁,且不在参与锁的争夺,进入WAITING状态. 当其他线程中执行obj.notify(),系统从waiting状态中的线程中随机取出一个使之参与锁的竞争.当然obj.notifyAll()是使所有wait状态的线程参与锁的竞争.

* `suspend`/`resume`

  thread.suspend()函数会将线程挂起,但是线程不会释放任何资源,直到在其他线程执行thread.resume().而且在挂起状态Thread.State居然还是RUNNABLE,非常不利于调试.而且如果thread.resume()错误的在threrad.suspend()之前执行了,即thread.suspend()之后没有resume,那么线程将会永远被挂起而且难以检测到,所以一般不会使用这种方式中断程序. 更好的替代方式是使用LockSupport的park和unpark进行优雅的替代.

* `join()`/`join(long)`

  在thread1中执行thread2.join().thread1将进入WAITING状态直到thread2执行完毕才会被notify. **但是在此过程中thread1不会释放锁**. 而join(long)则表示会等待一段时间之后被notify. 当然,join会出让cpu和锁.

* `yield()`

  thread.yield(),  thread会释放cpu资源,释放锁,进入WAITING状态并重新参与锁的竞争. 谦让动作.

  

  总结: 只有wait(),yield()会释放锁,suspend(),sleep(),join()都不会释放锁. 当然现在操作系统的时间一般都会释放cpu时间片.

#### Thread 终止

`thread.stop`方法可以立即结束thread线程,但这时不安全的,因为线程中断时代码执行到的位置是不可预知的.所以是不推荐的方法. 建议使用interrupt,并在线程内部做判断进行处理.

### 2. Thread的状态

#### 线程的五种状态

* NEW: Thread创建,但是还没有执行start时
* RUNNABLE: thread执行start()方法之后,此时thread不一定就在运行,也可能在等待操作系统的其他资源.
* BLOCKED:thread正在等待锁. 比如执行了wait()操作
* WAITING: obj.wait(),obj.join(),LockSupport.lock()
* TIMED_WAITING:obj.wait(long),obj.join(long),LockSupport.parkNanos,LockSupport.parkUntil
* TERMINATED:完成操作

#### Daemon线程

在start之前设置setDaemon(true),为守卫线程,会随着主线程执行完而销毁.

#### 线程优先级

### 3. volatile和synchronized

volatile只保证实例的可见性和有序性,不能完全保证线程安全. 例如i++这种非原子操作.

synchronized三种使用形式:

* 修饰对象:为对象加锁
* 修饰方法:为方法实例加锁
* 修饰static静态方法.为类加锁

synchronized可以实现线程同步,保证线程安全.在synchronized的修饰下,多线程在临界区是串行执行的.



## 三、JDK并发包

### 1. JDK中的锁

#### 1.1 ReentrantLock

​		ReentrantLock是在JDK1.5 concurrent包中引入的可重入锁.他和synchronized一样,在其修饰下多线程在临界区是串行执行的. 开始的时候,ReentrantLock的并发效率要远高于synchronized,因为在多线程非同时访问临界区去资源的情况下,ReentLock并不需要调用nitive代码使操作系统切换线程,而是串行直接运行的. 所以大大提高了效率.而在jdk 1.6 之后,synchronized做了大量优化,二者的效率不相上下. 在极高并发的情况下synchronized的效率甚至要高于ReentrantLock.



**对比synchronized**

* ReentrantLock提供了丰富的接口,并发编程更容易控制
* ReentrantLock可实现公平锁和非公平锁,而synchronized只能是非公平锁
* ReentrantLock可以中断响应. 即thread1在已经启动且在等待回去锁时,可以被中断不再等待. 这在synchronized中时做不到的.
* ReentrantLock实例本身是锁,而synchronized关键字是修饰其他变量使其变成锁(见上文的三种情况).
* ReentrantLock和synchronized都是可重入锁,即同一个线程如果获取了锁,在释放之前可以再次获取锁.

**方法**

* lock/unlock:加锁,释放锁
* lockInterrupted():加可响应中断的锁,抛出InterruptedException异常
* tryLock():现在可申请到锁则获取锁并返回true,否则返回false
* tryLock(time,timeUnit):在规定的时间内... 同上
* newCondition():创建Condition,作用和与synchronized配合的wait/notify相似,下节讲解
* getHoldCount():实际值为AQS里的state,因为是可重入锁,得到当前锁被持有线程,持有此锁的次数
* isHeldByCurrentThread/isLocked/isFair/getOwner:简单
* hasQueuedThreads()/hasQueuedThread(thread):队列中是否有(/特定)的线程
* getQueueLength/getQueuedThreads: 队列中线程的长度/线程们
* hasWaiters(condition)/getWaitQueueLength(condition)/getWaitingThreads(condition) : 特定Condition中waiter的状态.

**Condition**

Condition本身是一个接口,ReentrantLock使用的Condition实例实际是AQS中ConditionObject实例.ReentrantLock通过Condition实现多线程在临界区的通讯.相当于与synchronized配合的wait/notify.

但是其中断机制会更加方便使用.

 常用方法如下:

* await(): 在当前线程中释放lock,并抛出Interrupted异常.释放时间片,参与下一轮lock竞争.
* awaitUninterruptibly():不响应中断的await,即使执行了thread.interrupte()也不受影响.直到等来lock权限.
* awaitNanos(long nanosTimeout):等待几毫秒之后重新竞争lock.抛异常
* await(long time, TimeUnit unit):
* awaitUntil(Date deadline):最长等待到的时间
* signal():condition的wait队列中取出一个,让他参与lock竞争
* signalAll():取出所有...



**比较**

这里初学者可能会糊涂,lockInterrupted():和Condtion中的await():响应中断有什么区别吗?当然有区别,当thread.start之后,thread处于RUNNABLE或者WAIT状态,这时它还没有开始执行. 而wait方法要在thread获得lock并执行之后才能起效果.所以如果thread在第一次获取锁之前,锁已经被其他线程获取,他可能会等很长时间,而这时你执行了thread.interrupted,wait是不能响应到的.这也是synchronized不能在这时响应中断的原因. 而lockInterrupted()作用的时间,就是在第一次等待锁,或者释放了锁再次等待锁时.



**使用方式**

* 模拟死锁,以及解决方法来演示 ReentrantLock的使用方式

* 演示Condition的wiat/notify 以及中断处理

  ```Java
  public class ReentrantLockDemo {
      public static void main(String[] args) {
          ReentrantLockDemo demo = new ReentrantLockDemo();
          demo.reactInterrupt();
      }
  
      private ReentrantLock lock1 = new ReentrantLock();
      private ReentrantLock lock2 = new ReentrantLock();
  
      /**
       * 说明，此方法为了制造死锁的状态制造了两把锁lock1,lock2
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
  ```

#### 1.2 LockSupport

LockSupport是一个可以用来替代Thread.suspend(),Thread.resume. 在上文中我们分析了suspend和resume存在的问题.

**方法**

* park(): 
* park(obj)

* unpark(thread)
* parkNanos(obl,long)
* parkUntil(obl,long)
* getBlocker(thread)
* parkNanos(long)
* parkUntil(long)
* nextSecondarySeed():int 

#### 1.3 Semaphore

#### 1.4 ReadWriteLock

#### 1.5 CountDownLatch

#### 1.6 CyclicBarrier

### 2. ThreadPool 线程池

#### 并发队列

* SynchronousQueue
* ArrayBlockingQueue
* LinkdedBlockingQueue
* PriorityBlockingQueue

#### 拒绝策略

* AbortPolicy
* CallerRunsPolicy
* DiscardOldestPolicy
* DiscardPolicy

#### Fork/Join框架

### 3. JDK中的并发容器

#### 3.1 ConcurrentHashMap

#### 3.2 CopyOnWriteArrayList

#### 3.3 ConcurrentLinkedQueue

#### 3.4 BlockingQueue

#### 3.5 ConcurrentSkipListMap



