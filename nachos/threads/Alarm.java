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
    	
    	while(!waitQueue.isEmpty() && (waitQueue.peek().wakeTime <= currentTime)){
    		waitQueue.poll().waitingThread.ready();
    	}
    	
		Machine.interrupt().restore(interruptStatus);
		KThread.currentThread().yield();
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
		waitingThread waiter = new waitingThread(KThread.currentThread(), wakeTime);
		
		waitQueue.add(waiter);
		KThread.currentThread().sleep();
		
		Machine.interrupt().restore(interruptStatus);
    }
	
	
	
	//Datafields
	private PriorityQueue<waitingThread> waitQueue = new PriorityQueue<waitingThread>();
	
	
	
	//Custom Classes
	private class waitingThread implements Comparable<waitingThread>{

		public KThread waitingThread;
		public long wakeTime;
		

		public waitingThread(KThread thread, long time){

			this.waitingThread = thread;
			this.wakeTime = time;

		}

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
