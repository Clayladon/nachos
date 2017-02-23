package nachos.threads;

import nachos.machine.*;
import java.util.concurrent.LinkedBlockingQueue;
import nachos.threads.KThread;

/**
 * An implementation of condition variables that disables interrupt()s for
 * synchronization.
 *
 * <p>
 * You must implement this.
 *
 * @see	nachos.threads.Condition
 */
public class Condition2 {
    /**
     * Allocate a new condition variable.
     *
     * @param	conditionLock	the lock associated with this condition
     *				variable. The current thread must hold this
     *				lock whenever it uses <tt>sleep()</tt>,
     *				<tt>wake()</tt>, or <tt>wakeAll()</tt>.
     */
    public Condition2(Lock conditionLock) {
	this.conditionLock = conditionLock;
	waitQueue = new LinkedBlockingQueue<KThread>();
    }

    /**
     * Atomically release the associated lock and go to sleep on this condition
     * variable until another thread wakes it using <tt>wake()</tt>. The
     * current thread must hold the associated lock. The thread will
     * automatically reacquire the lock before <tt>sleep()</tt> returns.
     */
    public void sleep() {
		Lib.assertTrue(conditionLock.isHeldByCurrentThread());
	
		boolean interruptStatus = Machine.interrupt().disable();
		conditionLock.release();

		waitQueue.add(KThread.currentThread());
		KThread.sleep();
	
		conditionLock.acquire();
	
		Machine.interrupt().restore(interruptStatus);
    }

    /**
     * Wake up at most one thread sleeping on this condition variable. The
     * current thread must hold the associated lock.
     */
    public void wake() {
		Lib.assertTrue(conditionLock.isHeldByCurrentThread());
	
		boolean interruptStatus = Machine.interrupt().disable();
		
		if(waitQueue.peek() != null)
			try{
				((KThread)waitQueue.take()).ready();
			}
			catch(InterruptedException e){
				System.out.println("Error waking thread.");
			}
		
		Machine.interrupt().restore(interruptStatus);
    }

    /**
     * Wake up all threads sleeping on this condition variable. The current
     * thread must hold the associated lock.
     */
    public void wakeAll() {
		Lib.assertTrue(conditionLock.isHeldByCurrentThread());
	
		boolean interruptStatus = Machine.interrupt().disable();
		
		while(waitQueue.peek() != null)
			try{
				((KThread)waitQueue.take()).ready();
			}
			catch(InterruptedException e){
				System.out.println("Error waking thread.");
			}
			
		Machine.interrupt().restore(interruptStatus);
    }

    public static void selfTest(){

	Lock lock = new Lock();
	Condition2 tester = new Condition2(lock);

	KThread sleeper = new KThread();
	KThread waker = new KThread();
	waker.setTarget(new Runnable(){
		public void run(){

			lock.acquire();
			
			if(tester.waitQueue.isEmpty())
				System.out.println("Empty waitQueue");
			else
				System.out.println("Not empty waitQueue");
		
			tester.wake();

			tester.sleep();
		
			if(tester.waitQueue.isEmpty())
				System.out.println("Empty waitQueue");
			else
				System.out.println("Not empty waitQueue");


	
			lock.release();
		}
	});
	sleeper.setTarget(new Runnable(){
		public void run(){
				System.out.println("Start");
				lock.acquire();
				System.out.println("Acquired lock, putting to sleep.");
				waker.fork();
				tester.sleep();
				System.out.println("Woke up. Releasing lock.");
				tester.wake();
				lock.release();	
			}
		});
		sleeper.fork();
		sleeper.join();
    }

    private Lock conditionLock;
    private LinkedBlockingQueue<KThread> waitQueue;
}
