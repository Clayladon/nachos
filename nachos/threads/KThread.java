package nachos.threads;

import nachos.machine.*;
import java.util.ArrayList;

/**
 * A KThread is a thread that can be used to execute Nachos kernel code. Nachos
 * allows multiple threads to run concurrently.
 *
 * To create a new thread of execution, first declare a class that implements
 * the <tt>Runnable</tt> interface. That class then implements the <tt>run</tt>
 * method. An instance of the class can then be allocated, passed as an
 * argument when creating <tt>KThread</tt>, and forked. For example, a thread
 * that computes pi could be written as follows:
 *
 * <p><blockquote><pre>
 * class PiRun implements Runnable {
 *     public void run() {
 *         // compute pi
 *         ...
 *     }
 * }
 * </pre></blockquote>
 * <p>The following code would then create a thread and start it running:
 *
 * <p><blockquote><pre>
 * PiRun p = new PiRun();
 * new KThread(p).fork();
 * </pre></blockquote>
 */
public class KThread {
    /**
     * Get the current thread.
     *
     * @return	the current thread.
     */
    public static KThread currentThread() {
	Lib.assertTrue(currentThread != null);
	return currentThread;
    }
    
    /**
     * Allocate a new <tt>KThread</tt>. If this is the first <tt>KThread</tt>,
     * create an idle thread as well.
     */
    public KThread() {
	if (currentThread != null) {
	    tcb = new TCB();
	}	    
	else {
	    readyQueue = ThreadedKernel.scheduler.newThreadQueue(false);
	    readyQueue.acquire(this);	    

	    currentThread = this;
	    tcb = TCB.currentTCB();
	    name = "main";
	    restoreState();

	    createIdleThread();
	}
    }

    /**
     * Allocate a new KThread.
     *
     * @param	target	the object whose <tt>run</tt> method is called.
     */
    public KThread(Runnable target) {
	this();
	this.target = target;
    }

    /**
     * Set the target of this thread.
     *
     * @param	target	the object whose <tt>run</tt> method is called.
     * @return	this thread.
     */
    public KThread setTarget(Runnable target) {
	Lib.assertTrue(status == statusNew);
	
	this.target = target;
	return this;
    }

    /**
     * Set the name of this thread. This name is used for debugging purposes
     * only.
     *
     * @param	name	the name to give to this thread.
     * @return	this thread.
     */
    public KThread setName(String name) {
	this.name = name;
	return this;
    }

    /**
     * Get the name of this thread. This name is used for debugging purposes
     * only.
     *
     * @return	the name given to this thread.
     */     
    public String getName() {
	return name;
    }

    /**
     * Get the full name of this thread. This includes its name along with its
     * numerical ID. This name is used for debugging purposes only.
     *
     * @return	the full name given to this thread.
     */
    public String toString() {
	return (name + " (#" + id + ")");
    }

    /**
     * Deterministically and consistently compare this thread to another
     * thread.
     */
    public int compareTo(Object o) {
	KThread thread = (KThread) o;

	if (id < thread.id)
	    return -1;
	else if (id > thread.id)
	    return 1;
	else
	    return 0;
    }

    /**
     * Causes this thread to begin execution. The result is that two threads
     * are running concurrently: the current thread (which returns from the
     * call to the <tt>fork</tt> method) and the other thread (which executes
     * its target's <tt>run</tt> method).
     */
    public void fork() {
	Lib.assertTrue(status == statusNew);
	Lib.assertTrue(target != null);
	
	Lib.debug(dbgThread,
		  "Forking thread: " + toString() + " Runnable: " + target);

	boolean intStatus = Machine.interrupt().disable();

	tcb.start(new Runnable() {
		public void run() {
		    runThread();
		}
	    });

	ready();
	
	Machine.interrupt().restore(intStatus);
    }

    private void runThread() {
	begin();
	target.run();
	finish();
    }

    private void begin() {
	Lib.debug(dbgThread, "Beginning thread: " + toString());
	
	Lib.assertTrue(this == currentThread);

	restoreState();

	Machine.interrupt().enable();
    }

    /**
     * Finish the current thread and schedule it to be destroyed when it is
     * safe to do so. This method is automatically called when a thread's
     * <tt>run</tt> method returns, but it may also be called directly.
     *
     * The current thread cannot be immediately destroyed because its stack and
     * other execution state are still in use. Instead, this thread will be
     * destroyed automatically by the next thread to run, when it is safe to
     * delete this thread.
     */
    public static void finish() {
		Lib.debug(dbgThread, "Finishing thread: " + currentThread.toString());
	
		Machine.interrupt().disable();

		Machine.autoGrader().finishingCurrentThread();

		Lib.assertTrue(toBeDestroyed == null);
		toBeDestroyed = currentThread;


		currentThread.status = statusFinished;

		//Unjoining & Waking the joined threads
		currentThread.unjoin();
		sleep();
    }

	public void unjoin(){
		KThread threadToBeAwoken;	
		while((threadToBeAwoken = threadsToBeJoined.nextThread()) != null){
		
			if(threadToBeAwoken.status != statusReady)
				threadToBeAwoken.ready();
		}
	}
    /**
     * Relinquish the CPU if any other thread is ready to run. If so, put the
     * current thread on the ready queue, so that it will eventually be
     * rescheuled.
     *
     * <p>
     * Returns immediately if no other thread is ready to run. Otherwise
     * returns when the current thread is chosen to run again by
     * <tt>readyQueue.nextThread()</tt>.
     *
     * <p>
     * Interrupts are disabled, so that the current thread can atomically add
     * itself to the ready queue and switch to the next thread. On return,
     * restores interrupts to the previous state, in case <tt>yield()</tt> was
     * called with interrupts disabled.
     */
    public static void yield() {
	Lib.debug(dbgThread, "Yielding thread: " + currentThread.toString());
	
	Lib.assertTrue(currentThread.status == statusRunning);
	
	boolean intStatus = Machine.interrupt().disable();

	currentThread.ready();

	runNextThread();
	
	Machine.interrupt().restore(intStatus);
    }

    /**
     * Relinquish the CPU, because the current thread has either finished or it
     * is blocked. This thread must be the current thread.
     *
     * <p>
     * If the current thread is blocked (on a synchronization primitive, i.e.
     * a <tt>Semaphore</tt>, <tt>Lock</tt>, or <tt>Condition</tt>), eventually
     * some thread will wake this thread up, putting it back on the ready queue
     * so that it can be rescheduled. Otherwise, <tt>finish()</tt> should have
     * scheduled this thread to be destroyed by the next thread to run.
     */
    public static void sleep() {
	Lib.debug(dbgThread, "Sleeping thread: " + currentThread.toString());
	
	Lib.assertTrue(Machine.interrupt().disabled());

	if (currentThread.status != statusFinished)
	    currentThread.status = statusBlocked;

	runNextThread();
    }

    /**
     * Moves this thread to the ready state and adds this to the scheduler's
     * ready queue.
     */
    public void ready() {
	Lib.debug(dbgThread, "Ready thread: " + toString());
	
	Lib.assertTrue(Machine.interrupt().disabled());
	Lib.assertTrue(status != statusReady);
	
	status = statusReady;
	if (this != idleThread)
	    readyQueue.waitForAccess(this);
	
	Machine.autoGrader().readyThread(this);
    }

    /**
     * Waits for this thread to finish. If this thread is already finished,
     * return immediately. This method must only be called once; the second
     * call is not guaranteed to return. This thread must not be the current
     * thread.
     */
    public void join(){

    	//If the target has already finished; do not allow a join
		if(status == statusFinished)
			Lib.debug(KThreadTestChar, "Blocked, attempting to join a finished thread. Thread: " + toString());
			
		//If the target is joined to the current thread via cyclical dependencies, do not allow a join
		else if(currentThread.joinedIDs.contains(this.id)){
			Lib.debug(KThreadTestChar, "Blocked, cyclical joining. Thread: " + toString());
		}
		else{
			//Ensure that the method is called by another active thread
			Lib.assertTrue(this != currentThread);
			Lib.debug(dbgThread, "Joining to thread: " + toString());
			
			//Fill this.joinedIDs with the respective thread IDs to prevent cyclical joining.
			this.joinedIDs.add(currentThread.id);
			for(int i = 0; i < currentThread.joinedIDs.size(); i++){
				this.joinedIDs.add(currentThread.joinedIDs.get(i));
			}
			
			//Save the current interrupt status and disable system interrupts
			boolean interruptStatus = Machine.interrupt().disable();
			
			//If this thread is new it gets readied
			if(status == statusNew)
				ready();
				
			//The thread is then added to the ThreadQueue to wait for execution
			threadsToBeJoined.waitForAccess(currentThread);
			
			//And put to sleep until it's turn on the ThreadQueue
			sleep();

			//Restore interrupts
			Machine.interrupt().restore(interruptStatus);
		}
    }

    /**
     * Create the idle thread. Whenever there are no threads ready to be run,
     * and <tt>runNextThread()</tt> is called, it will run the idle thread. The
     * idle thread must never block, and it will only be allowed to run when
     * all other threads are blocked.
     *
     * <p>
     * Note that <tt>ready()</tt> never adds the idle thread to the ready set.
     */
    private static void createIdleThread() {
	Lib.assertTrue(idleThread == null);
	
	idleThread = new KThread(new Runnable() {
	    public void run() { while (true) yield(); }
	});
	idleThread.setName("idle");

	Machine.autoGrader().setIdleThread(idleThread);
	
	idleThread.fork();
    }
    
    /**
     * Determine the next thread to run, then dispatch the CPU to the thread
     * using <tt>run()</tt>.
     */
    private static void runNextThread() {
	KThread nextThread = readyQueue.nextThread();
	if (nextThread == null)
	    nextThread = idleThread;

	nextThread.run();
    }

    /**
     * Dispatch the CPU to this thread. Save the state of the current thread,
     * switch to the new thread by calling <tt>TCB.contextSwitch()</tt>, and
     * load the state of the new thread. The new thread becomes the current
     * thread.
     *
     * <p>
     * If the new thread and the old thread are the same, this method must
     * still call <tt>saveState()</tt>, <tt>contextSwitch()</tt>, and
     * <tt>restoreState()</tt>.
     *
     * <p>
     * The state of the previously running thread must already have been
     * changed from running to blocked or ready (depending on whether the
     * thread is sleeping or yielding).
     *
     * @param	finishing	<tt>true</tt> if the current thread is
     *				finished, and should be destroyed by the new
     *				thread.
     */
    private void run() {
	Lib.assertTrue(Machine.interrupt().disabled());

	Machine.yield();

	currentThread.saveState();

	Lib.debug(dbgThread, "Switching from: " + currentThread.toString()
		  + " to: " + toString());

	currentThread = this;

	tcb.contextSwitch();

	currentThread.restoreState();
    }

    /**
     * Prepare this thread to be run. Set <tt>status</tt> to
     * <tt>statusRunning</tt> and check <tt>toBeDestroyed</tt>.
     */
    protected void restoreState() {
	Lib.debug(dbgThread, "Running thread: " + currentThread.toString());
	
	Lib.assertTrue(Machine.interrupt().disabled());
	Lib.assertTrue(this == currentThread);
	Lib.assertTrue(tcb == TCB.currentTCB());

	Machine.autoGrader().runningThread(this);
	
	status = statusRunning;

	if (toBeDestroyed != null) {
	    toBeDestroyed.tcb.destroy();
	    toBeDestroyed.tcb = null;
	    toBeDestroyed = null;
	}
    }

    /**
     * Prepare this thread to give up the processor. Kernel threads do not
     * need to do anything here.
     */
    protected void saveState() {
	Lib.assertTrue(Machine.interrupt().disabled());
	Lib.assertTrue(this == currentThread);
    }

    private static class PingTest implements Runnable {
	PingTest(int which) {
	    this.which = which;
	}
	
	public void run() {
	    for (int i=0; i<5; i++) {
		System.out.println("*** thread " + which + " looped "
				   + i + " times");
		currentThread.yield();
	    }
	}

	private int which;
    }

    /**
     * Tests whether this module is working.
     */
    public static void selfTest() {
		Lib.debug(KThreadTestChar, "KThread.selfTest(): Starting self test.");
	
		//Default selfTest() lines for KThread
		new KThread(new PingTest(1)).setName("forked thread").fork();
		new PingTest(0).run();
		
		Lib.debug(KThreadTestChar, "KThread.selfTest(): Finished default tests, beginning custom tests.");
		
		//Custom selfTest() methods we wrote
		selfJoinTest();
		joinFinishedTest();
		cyclicalJoinTest();
		
		Lib.debug(KThreadTestChar, "KThread.selfTest(): Finished self test, passed.");
		
    }
    
    /**
     * Tests whether threads are able to join themselves.
     * Threads should not be able to join themselves, and any
     * self join should be blocked since the thread would never wake.
     */
    private static void selfJoinTest(){
		Lib.debug(KThreadTestChar, "KThread.selfJoinTest(): Starting self join test.");
    
    	//Create a thread
    	KThread thread = new KThread();
		thread.setName("Self");
    	Lib.debug(KThreadTestChar, "KThread.selfJoinTest(): Thread created.");

    	//Set the thread's target to run a join on itself
		thread.setTarget(new Runnable() {
		public void run(){
			String result = "Self join test failed.";
			
			try{
				Lib.debug(KThreadTestChar, "KThread.selfJoinTest(): Thread about to join itself.");
				thread.join();
			}
			catch (Error e){
				Lib.debug(KThreadTestChar, "Blocked, attempting to join self. Thread: " + thread.toString());
				result = "Self join test, passed.";
			}
				
			Lib.debug(KThreadTestChar, "KThread.selfJoinTest(): " + result);
		}
	});
		//Execute the thread
		thread.fork();
		thread.join();
    }
    
	/**
	 * Tests whether threads are able to joined threads that have already finished.
	 * Threads should never be able to join finished threads as they would be waiting
	 * for a thread to finish that has already finished.
	 */
	private static void joinFinishedTest(){
	
		Lib.debug(KThreadTestChar, "KThread.joinFinishedTest(): Starting test to join finished thread.");
		
		//Create two threads, one to finish and one to join afterwards
		KThread deadThread = new KThread();
		KThread joiner = new KThread();
		//Set names for debugging purposes
		joiner.setName("Joiner");
		deadThread.setName("Finished Thread");
		
		Lib.debug(KThreadTestChar, "KThread.joinFinishedTest(): Two threads created, deadThread and joiner.");

		//Set deadThread's run to finish immediately
		deadThread.setTarget(new Runnable() {
			public void run(){
				Lib.debug(KThreadTestChar, "KThread.joinFinishedTest(): deadThread finished running.");
			}
		});

		//Set joiner's thread to attempt to join deadThread
    	joiner.setTarget(new Runnable() {
			public void run(){
				Lib.debug(KThreadTestChar, "KThread.joinFinishedTest(): joiner about to join deadThread.");
				deadThread.join();
			}
		});

		//Execute both threads
		deadThread.fork();
		joiner.fork();
		joiner.join();
		
		Lib.debug(KThreadTestChar, "KThread.joinFinishedTest(): Finished thread join test, passed.");

	}
					
		
    /**
     * Tests whether threads can join a thread that is joined to them through a cyclical dependency.
     * Threads should never be allowed to join themselves via a cyclical dependency since they would
     * never wake up. Should be blocked in join().
     */
    private static void cyclicalJoinTest(){
    
		Lib.debug(KThreadTestChar, "KThread.cyclicalJoinTest(): Starting test to identify cyclical dependency.");
    
    	//Create three threads and name them for tracing purposes.
    	KThread thread1 = new KThread();
    	thread1.setName("cyclical test thread 1");
    	KThread thread2 = new KThread();
    	thread2.setName("cyclical test thread 2");
    	KThread thread3 = new KThread();
    	thread3.setName("cyclical test thread 3");
    	
    	
		Lib.debug(KThreadTestChar, "KThread.cyclicalJoinTest(): Three threads created.");
    	
    	//Set each thread's target to fork the thread it will join and set off a join to that thread.
    	thread1.setTarget(new Runnable() {
			public void run(){
				Lib.debug(KThreadTestChar, "KThread.cyclicalJoinTest(): thread1 about to join thread2.");
				thread2.fork();
				thread2.join();
			}
		});
		
		thread2.setTarget(new Runnable() {
			public void run(){
				Lib.debug(KThreadTestChar, "KThread.cyclicalJoinTest(): thread2 about to join thread3.");
				thread3.fork();
				thread3.join();
			}
		});
    	
    	//The final thread does not need to fork thread1 as it has already been forked.
    	thread3.setTarget(new Runnable() {
			public void run(){
				Lib.debug(KThreadTestChar, "KThread.cyclicalJoinTest(): thread3 about to join thread1.");
				thread1.join();
			}
		});
    	//Fork thread1 to set off the chain of forks and joins.
    	thread1.fork();
		thread1.join();
		Lib.debug(KThreadTestChar, "KThread.cyclicalJoinTest(): Cyclical join test, passed.");
    }
    
	
    private static final char dbgThread = 't';
    private static final char KThreadTestChar = 'k';

    /**
     * Additional state used by schedulers.
     *
     * @see	nachos.threads.PriorityScheduler.ThreadState
     */
    public Object schedulingState = null;

    private static final int statusNew = 0;
    private static final int statusReady = 1;
    private static final int statusRunning = 2;
    private static final int statusBlocked = 3;
    private static final int statusFinished = 4;

    /**
     * The status of this thread. A thread can either be new (not yet forked),
     * ready (on the ready queue but not running), running, or blocked (not
     * on the ready queue and not running).
     */
    private int status = statusNew;
    private String name = "(unnamed thread)";
    private Runnable target;
    private TCB tcb;

    /**
     * Unique identifer for this thread. Used to deterministically compare
     * threads.
     */
    private int id = numCreated++;
    /** Number of times the KThread constructor was called. */
    private static int numCreated = 0;

    private static ThreadQueue readyQueue = null;
    private static KThread currentThread = null;
    private static KThread toBeDestroyed = null;
    private static KThread idleThread = null;

	private ThreadQueue threadsToBeJoined = ThreadedKernel.scheduler.newThreadQueue(true);
	private ArrayList<Integer> joinedIDs = new ArrayList<Integer>();
}
