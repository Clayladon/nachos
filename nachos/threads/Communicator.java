package nachos.threads;

import nachos.machine.*;

/**
 * A <i>communicator</i> allows threads to synchronously exchange 32-bit
 * messages. Multiple threads can be waiting to <i>speak</i>,
 * and multiple threads can be waiting to <i>listen</i>. But there should never
 * be a time when both a speaker and a listener are waiting, because the two
 * threads can be paired off at this point.
 */
public class Communicator {
    /**
     * Allocate a new communicator.
     */
    public Communicator() {
    	Lock lock = new Lock();
    	Condition2 speaker = new Condition2();
    	Condition2 listener = new Condition2();
    	int sharedMessage = 0;
    	boolean sharedMessageFree = true;
    	int numListenersWaiting = 0;
    }

    /**
     * Wait for a thread to listen through this communicator, and then transfer
     * <i>word</i> to the listener.
     *
     * <p>
     * Does not return until this thread is paired up with a listening thread.
     * Exactly one listener should receive <i>word</i>.
     *
     * @param	word	the integer to transfer.
     */
    public void speak(int word) {
    	lock.acquire();
    	while(numListenersWaiting == 0 || (!sharedMessageFree))
    		speaker.sleep();
    		
    	sharedMessageFree = false;
    	sharedMessage = word;
    	
    	listener.wake();
    	
    	lock.release();
    }

    /**
     * Wait for a thread to speak through this communicator, and then return
     * the <i>word</i> that thread passed to <tt>speak()</tt>.
     *
     * @return	the integer transferred.
     */    
    public int listen() {
    	lock.acquire();
    	numListenersWaiting++;
    
    	speaker.wake();
    	listener.sleep();
    
    	int word = sharedMessage;
    	sharedMessageFree = true;
    
    	numListenersWaiting--;
    	speaker.wake();
    	lock.release();
    
		return word;
    }
}
