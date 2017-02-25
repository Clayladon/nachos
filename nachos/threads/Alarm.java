package nachos.threads;

import nachos.machine.*;
import java.util.PriorityQueue;
import nachos.threads.KThread;

/**
 * Uses the hardware timer to provide preemption, and to allow threads to sleep
 * until a certain time.
 */
public class Alarm {
    /**
     * Allocate a new Alarm. Set the machine's timer interrupt handler to this
     * alarm's callback.
     *
     * <p><b>Note</b>: Nachos will not function correctly with more than one
     * alarm.
     */
    public Alarm() {
	Machine.timer().setInterruptHandler(new Runnable() {
		public void run() { timerInterrupt(); }
	    });
    }

    /**
     * The timer interrupt handler. This is called by the machine's timer
     * periodically (approximately every 500 clock ticks). Causes the current
     * thread to yield, forcing a context switch if there is another thread
     * that should be run.
     */
    public void timerInterrupt() {
    
	boolean interruptStatus = Machine.interrupt().disable();
    	long currentTime = Machine.timer().getTime();
    	
	//All waiting thread's whose timeToWait has expired are readied
    	while(!waitQueue.isEmpty() && (waitQueue.peek().wakeTime <= currentTime)){
    		waitQueue.poll().waitingThread.ready();
    	}
    	
		Machine.interrupt().restore(interruptStatus);
		KThread.yield();
    }

    /**
     * Put the current thread to sleep for at least <i>x</i> ticks,
     * waking it up in the timer interrupt handler. The thread must be
     * woken up (placed in the scheduler ready set) during the first timer
     * interrupt where
     *
     * <p><blockquote>
     * (current time) >= (WaitUntil called time)+(x)
     * </blockquote>
     *
     * @param	x	the minimum number of clock ticks to wait.
     *
     * @see	nachos.machine.Timer#getTime()
     */
    public void waitUntil(long timeToWait) {
    
		boolean interruptStatus = Machine.interrupt().disable();
		
		long wakeTime = Machine.timer().getTime() + timeToWait;
		
		//Store the KThread & its wakeTime in a waitingThread object
		waitingThread waiter = new waitingThread(KThread.currentThread(), wakeTime);
		
		waitQueue.add(waiter);
		KThread.sleep();
		
		Machine.interrupt().restore(interruptStatus);
    }

    /* SelfTest(), this method tests to make sure that threads are put
     * to sleep and awoken in the proper order. The order being dictated
     * by each thread's timeToWait instead of the order in which the threads
     * were put to sleep.
    */
    public static void selfTest(){
		Lib.debug(AlarmTestChar, "Alarm.selfTest(): Starting self test.");

		Alarm clock = new Alarm();
		//Stack multiple waiters with the same timeToWait
		//The threads should finish in the order they were
		//called.
		KThread threadA = new KThread();
		KThread threadB = new KThread();
		KThread threadC = new KThread();
		Lib.debug(AlarmTestChar, "Alarm.selfTest(): Alarm (clock) and three threads (A,B,C) created.");
		
		threadA.setTarget(new Runnable(){
			public void run(){
				Lib.debug(AlarmTestChar, "Alarm.selfTest(): Thread A waiting.");
				clock.waitUntil(10000000);
				Lib.debug(AlarmTestChar, "Alarm.selfTest(): Thread A finished.");
			}
		});
		threadB.setTarget(new Runnable(){
			public void run(){
				Lib.debug(AlarmTestChar, "Alarm.selfTest(): Thread B waiting.");
				clock.waitUntil(10000000);	
				Lib.debug(AlarmTestChar, "Alarm.selfTest(): Thread B finished.");
			}
		});
		threadC.setTarget(new Runnable(){
			public void run(){
				Lib.debug(AlarmTestChar, "Alarm.selfTest(): Thread C waiting.");
				clock.waitUntil(10000000);
				Lib.debug(AlarmTestChar, "Alarm.selfTest(): Thread C finished.");
			}
		});

		Lib.debug(AlarmTestChar, "Alarm.selfTest(): Forking threads A, B, and C.");
		threadA.fork();
		threadB.fork();
		threadC.fork();
		threadC.join();
		
		Lib.debug(AlarmTestChar, "Alarm.selfTest(): Alarm test with same wait times finished.");
		//Stack waiters with differing timeToWait
		//The threads should finish in the order
		//dictated by their timeToWait
		KThread thread1 = new KThread();
		KThread thread2 = new KThread();
		KThread thread3 = new KThread();
		Lib.debug(AlarmTestChar, "Alarm.selfTest(): Three threads (1,2,3) created.");

		//Should finish last
		thread1.setTarget(new Runnable(){
			public void run(){
				Lib.debug(AlarmTestChar, "Alarm.selfTest(): Thread 1 waiting.");
				clock.waitUntil(20000000);
				Lib.debug(AlarmTestChar, "Alarm.selfTest(): Thread 1 finished.");
			}
		});

		//Should finish second.
		thread2.setTarget(new Runnable(){
			public void run(){
				Lib.debug(AlarmTestChar, "Alarm.selfTest(): Thread 2 waiting.");
				clock.waitUntil(10000000);
				Lib.debug(AlarmTestChar, "Alarm.selfTest(): Thread 2 finished.");
			}
		});

		//Should finish first.
		thread3.setTarget(new Runnable(){
			public void run(){
				Lib.debug(AlarmTestChar, "Alarm.selfTest(): Thread 3 waiting.");
				clock.waitUntil(1000000);
				Lib.debug(AlarmTestChar, "Alarm.selfTest(): Thread 3 finished.");
			}
		});

		Lib.debug(AlarmTestChar, "Alarm.selfTest(): Forking threads 1, 2, and 3.");
		thread1.fork();
		thread2.fork();
		thread3.fork();
		thread1.join();
		
		Lib.debug(AlarmTestChar, "Alarm.selfTest(): Alarm test with different wait times finished.");
		Lib.debug(AlarmTestChar, "Alarm.selfTest(): Finished selfTest(), passed.");
    }	
	
	
	//Priority queue allows threads to be stored according to their timeToWait
	private PriorityQueue<waitingThread> waitQueue = new PriorityQueue<waitingThread>();
	private static final char AlarmTestChar = 'a';
	
	
	
	/* This class is a container for threads and their wakeTimes.
	 * It also implements the Comparable allowing waitingThread objects
	 * to be stored according to their wakeTimes in a priority queue.
	 */
	private class waitingThread implements Comparable<waitingThread>{

		//Datafields
		public KThread waitingThread;
		public long wakeTime;
		
		//Constructor
		public waitingThread(KThread thread, long time){

			this.waitingThread = thread;
			this.wakeTime = time;

		}
		
		//Implementing the Comparable interface
		public int compareTo(waitingThread other){

			if(wakeTime > other.wakeTime)
				return 1;
			else if(wakeTime == other.wakeTime)
				return 0;
			else
				return -1;
		}
	}
}
