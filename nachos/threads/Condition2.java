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
    	//Ensure that the current thread holds the lock
		Lib.assertTrue(conditionLock.isHeldByCurrentThread());
	
		//Store interrupts and release the lock
		boolean interruptStatus = Machine.interrupt().disable();
		conditionLock.release();

		//Add the current thread to the waitQueue and put it to sleep
		waitQueue.add(KThread.currentThread());
		KThread.sleep();
	
		//After waking up, reacquire the lock
		conditionLock.acquire();
	
		//Restore interrupts
		Machine.interrupt().restore(interruptStatus);
    }

    /**
     * Wake up at most one thread sleeping on this condition variable. The
     * current thread must hold the associated lock.
     */
    public void wake() {
    	//Ensure that the current thread holds the lock
		Lib.assertTrue(conditionLock.isHeldByCurrentThread());
	
		//Store interrupts
		boolean interruptStatus = Machine.interrupt().disable();
		
		//If the wait queue is not empty
		if(waitQueue.peek() != null)
			try{
				//Take the head off the queue and ready it
				((KThread)waitQueue.take()).ready();
			}
			catch(InterruptedException e){
				//This operation is atomic and this error will never happen
				System.out.println("Error waking thread.");
			}
		
		//Restore interrupts
		Machine.interrupt().restore(interruptStatus);
    }

    /**
     * Wake up all threads sleeping on this condition variable. The current
     * thread must hold the associated lock.
     */
    public void wakeAll() {
    	//Ensure that the current thread holds the lock
		Lib.assertTrue(conditionLock.isHeldByCurrentThread());
	
		//Store interrupts
		boolean interruptStatus = Machine.interrupt().disable();
		
		//While the wait queue is not empty
		while(waitQueue.peek() != null)
			try{
				//Take the head off the queue and ready it
				((KThread)waitQueue.take()).ready();
			}
			catch(InterruptedException e){
				//This operation is atomic and this error will never happen
				System.out.println("Error waking thread.");
			}
			
		//Restore interrupts
		Machine.interrupt().restore(interruptStatus);
    }

	/**
	 * Tests whether this module is working.
	 */
    public static void selfTest(){
		Lib.debug(Condition2TestChar, "Condition2.selfTest(): Starting self test.");
		
		//Create a lock and Condition2 object to test with
		Lock lock = new Lock();
		Condition2 tester = new Condition2(lock);
		
		Lib.debug(Condition2TestChar, "Condition2.selfTest(): Lock (lock), Condition2 (tester) created.");
	
		//Testing the wake & sleep methods
		KThread sleeper = new KThread();
		KThread waker = new KThread();
		
		Lib.debug(Condition2TestChar, "Condition2.selfTest(): Two threads created, sleeper and waker.");
		//Set sleeper's target to acquire the lock, fork a waker, sleep, and release the lock on waking up
		sleeper.setTarget(new Runnable(){
			public void run(){
				Lib.debug(Condition2TestChar, "Condition2.selfTest(): Sleeper starting.");
				lock.acquire();
				Lib.debug(Condition2TestChar, "Condition2.selfTest(): Sleeper acquired lock.");
				waker.fork();
				Lib.debug(Condition2TestChar, "Condition2.selfTest(): Sleeper about to sleep.");
				tester.sleep();
				tester.wake();
				Lib.debug(Condition2TestChar, "Condition2.selfTest(): Sleeper woke up, releasing lock.");
				lock.release();	
				}
			});
		//Set waker's target to acquire the lock, and print the state of the wait queue before and after
		//cycling the tester's wait queue.
		waker.setTarget(new Runnable(){
			public void run(){

				Lib.debug(Condition2TestChar, "Condition2.selfTest(): Waker starting.");
				lock.acquire();
				Lib.debug(Condition2TestChar, "Condition2.selfTest(): Waker acquired lock.");
			
				if(tester.waitQueue.isEmpty())
					Lib.debug(Condition2TestChar, "Condition2.selfTest(): Tester's waitQueue is empty.");
				else
					Lib.debug(Condition2TestChar, "Condition2.selfTest(): Tester's waitQueue is not empty.");
		
				tester.wake();

				tester.sleep();
		
				if(tester.waitQueue.isEmpty())
					Lib.debug(Condition2TestChar, "Condition2.selfTest(): Tester's waitQueue is empty.");
				else
					Lib.debug(Condition2TestChar, "Condition2.selfTest(): Tester's waitQueue is not empty");


				tester.wake();	//Testing if wake can handle an empty wait queue
				lock.release();
			}
		});
			sleeper.fork();
			sleeper.join();


		//Testing multiple sleeps & the wakeAll method

		Lib.debug(Condition2TestChar, "Condition2.selfTest(): Starting multiple wake test.");
		
		//Create three sleeper threads and a waker thread
		KThread sleep1 = new KThread();
		KThread sleep2 = new KThread();
		KThread sleep3 = new KThread();
		KThread wakeMulti = new KThread();
		Lib.debug(Condition2TestChar, "Condition2.selfTest(): Three threads (sleep1, sleep2, sleep3)"
										+" and thread (wakeMulti) created.");
	
		//Set sleeper targets to sleep when forked and fork the next sleeper
		sleep1.setTarget(new Runnable(){
			public void run(){
					lock.acquire();
					sleep2.fork();
					Lib.debug(Condition2TestChar, "Condition2.selfTest(): Sleep1 sleeping.");
					tester.sleep();
					Lib.debug(Condition2TestChar, "Condition2.selfTest(): Sleep1 woke up.");
					lock.release();	
				}
			});
		sleep2.setTarget(new Runnable(){
			public void run(){
					lock.acquire();
					sleep3.fork();
					Lib.debug(Condition2TestChar, "Condition2.selfTest(): Sleep2 sleeping.");
					tester.sleep();
					Lib.debug(Condition2TestChar, "Condition2.selfTest(): Sleep2 woke up.");
					lock.release();	
				}
			});
		//Sleep3 forks wakeMulti instead of another sleeper thread
		sleep3.setTarget(new Runnable(){
			public void run(){
					lock.acquire();
					wakeMulti.fork();
					Lib.debug(Condition2TestChar, "Condition2.selfTest(): Sleep3 sleeping.");
					tester.sleep();
					Lib.debug(Condition2TestChar, "Condition2.selfTest(): Sleep3 woke up.");
					tester.wake();
					lock.release();	
				}
			});
		//WakeMulti calls the wakeAll() method to wake all three sleepers
		wakeMulti.setTarget(new Runnable(){
			public void run(){
				lock.acquire();
				Lib.debug(Condition2TestChar, "Condition2.selfTest(): WakeMulti started.");
				tester.wakeAll();
				tester.sleep();
				Lib.debug(Condition2TestChar, "Condition2.selfTest(): WakeMulti finished.");
				lock.release();
			}
		});

		sleep1.fork();
		sleep1.join();
		wakeMulti.join();
		
		Lib.debug(Condition2TestChar, "Condiiton2.selfTest(): Finished selfTest(), passed.");
    }

	//Datafields
    private Lock conditionLock;
    private LinkedBlockingQueue<KThread> waitQueue;
    private static final char Condition2TestChar = 'c';
}
