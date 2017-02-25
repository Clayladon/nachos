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
    //Datafield declarations
    Lock lock;
    Condition2 speaker;
    Condition2 listener;
    int sharedMessage;
    boolean sharedMessageFree;
    int numListenersWaiting;
    
    /**
     * Allocate a new communicator.
     */
    public Communicator() {
    	//Initialize datafields
    	lock = new Lock();
    	speaker = new Condition2(lock);
    	listener = new Condition2(lock);
    	sharedMessage = 0;
    	sharedMessageFree = true;
    	numListenersWaiting = 0;
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
    	//Acquire the lock
    	lock.acquire();
    	
    	//While there are no listeners or the shared message is busy
    	while(numListenersWaiting == 0 || (!sharedMessageFree))
    		//Put this thread on the speaker's waitQueue
    		speaker.sleep();
    		
    	//Once there is a listener and the shared message is free claim the
    	//shared message flag and set the sharedMessage to word
    	sharedMessageFree = false;
    	sharedMessage = word;
    	
    	//Wake a listener
    	listener.wake();
    	
    	//Release the lock
    	lock.release();
    }

    /**
     * Wait for a thread to speak through this communicator, and then return
     * the <i>word</i> that thread passed to <tt>speak()</tt>.
     *
     * @return	the integer transferred.
     */    
    public int listen() {
    	//Acquire the lock and increment the number of listeners waiting
    	lock.acquire();
    	numListenersWaiting++;
    
    	//Wake a speaker
    	speaker.wake();
    	//Sleep until a speaker wakes the listener again
    	listener.sleep();
    
    	//Store the word that is in sharedMessage
    	int word = sharedMessage;
    	//Release the shared message flag to other speakers
    	sharedMessageFree = true;
    
    	//Decrement the number of listeners waiting
    	numListenersWaiting--;
    	//Wake a speaker to prompt a check for more listeners
    	speaker.wake();
    	//Release the lock
    	lock.release();
    
    	//Finally, return word
		return word;
    }
    
    /**
     * Tests whether this module is working.
     */
    public static void selfTest(){
    	Lib.debug(CommunicatorTestChar, "Communicator.selfTest(): Starting self test.");
    	
    	//Each test is broken into it's own method that is called in turn
    	manySpeakers();
    	manyListeners();
    	speakerListenerTest();
    	listenerSpeakerTest();
    	
    	Lib.debug(CommunicatorTestChar, "Communicator.selfTest(): Finished selfTest(), passed.");
    }
    
    /**
     * Tests whether the Communicator can handle a surplus of 
     * speakers before recieving any listeners. This should be 
     * the case as speakers are simply put to sleep until a listener
     * is ready to listen.
     */
    public static void manySpeakers(){
    	
    	Lib.debug(CommunicatorTestChar, "Communicator.manySpeakers(): Starting multiple speakers test.");
    	//Create a Communicator object
    	Communicator manySpeakersComm = new Communicator();
    	
    	//Create 5 speakers that each will speak a word (1-5) when they are forked
    	KThread thread1 = new KThread();
    	Lib.debug(CommunicatorTestChar, "Communicator.manySpeakers(): thread (thread1) created.");
    	thread1.setName("thread 1");
    	thread1.setTarget(new Runnable() {
    		public void run(){
    			Lib.debug(CommunicatorTestChar, "Communicator.manySpeakers(): thread1 speaking word 1.");
    			manySpeakersComm.speak(1);
    		}
    	});
    	
    	KThread thread2 = new KThread();
    	Lib.debug(CommunicatorTestChar, "Communicator.manySpeakers(): thread (thread2) created.");
    	thread2.setName("thread 2");
    	thread2.setTarget(new Runnable() {
    		public void run(){
    			Lib.debug(CommunicatorTestChar, "Communicator.manySpeakers(): thread2 speaking word 2.");
    			manySpeakersComm.speak(2);
    		}
    	});
    	
    	KThread thread3 = new KThread();
    	Lib.debug(CommunicatorTestChar, "Communicator.manySpeakers(): thread (thread3) created.");
    	thread3.setName("thread 3");
    	thread3.setTarget(new Runnable() {
    		public void run(){
    			Lib.debug(CommunicatorTestChar, "Communicator.manySpeakers(): thread3 speaking word 3.");
    			manySpeakersComm.speak(3);
    		}
    	});
    	
    	KThread thread4 = new KThread();
    	Lib.debug(CommunicatorTestChar, "Communicator.manySpeakers(): thread (thread4) created.");
    	thread4.setName("thread 4");
    	thread4.setTarget(new Runnable() {
    		public void run(){
    			Lib.debug(CommunicatorTestChar, "Communicator.manySpeakers(): thread4 speaking word 4.");
    			manySpeakersComm.speak(4);
    		}
    	});
    	
    	KThread thread5 = new KThread();
    	Lib.debug(CommunicatorTestChar, "Communicator.manySpeakers(): thread (thread5) created.");
    	thread5.setName("thread 5");
    	thread5.setTarget(new Runnable() {
    		public void run(){
    			Lib.debug(CommunicatorTestChar, "Communicator.manySpeakers(): thread5 speaking word 5.");
    			manySpeakersComm.speak(5);
    		}
    	});
    	
    	//Create 5 listeners that will listen to a word when forked
    	KThread thread6 = new KThread();
    	Lib.debug(CommunicatorTestChar, "Communicator.manySpeakers(): thread (thread6) created.");
    	thread6.setName("thread 6");
    	thread6.setTarget(new Runnable() {
    		public void run(){
    			Lib.debug(CommunicatorTestChar, "Communicator.manySpeakers(): thread6 listening to word " 
    												+ manySpeakersComm.listen() + ".");
    		}
    	});
    	
    	KThread thread7 = new KThread();
    	Lib.debug(CommunicatorTestChar, "Communicator.manySpeakers(): thread (thread7) created.");
    	thread7.setName("thread 7");
    	thread7.setTarget(new Runnable() {
    		public void run(){
    			Lib.debug(CommunicatorTestChar, "Communicator.manySpeakers(): thread7 listening to word " 
    												+ manySpeakersComm.listen() + ".");
    		}
    	});
    	
    	KThread thread8 = new KThread();
    	Lib.debug(CommunicatorTestChar, "Communicator.manySpeakers(): thread (thread8) created.");
    	thread8.setName("thread 8");
    	thread8.setTarget(new Runnable() {
    		public void run(){
    			Lib.debug(CommunicatorTestChar, "Communicator.manySpeakers(): thread8 listening to word " 
    												+ manySpeakersComm.listen() + ".");
    		}
    	});
    	
    	KThread thread9 = new KThread();
    	Lib.debug(CommunicatorTestChar, "Communicator.manySpeakers(): thread (thread9) created.");
    	thread9.setName("thread 9");
    	thread9.setTarget(new Runnable() {
    		public void run(){
    			Lib.debug(CommunicatorTestChar, "Communicator.manySpeakers(): thread9 listening to word " 
    												+ manySpeakersComm.listen() + ".");
    		}
    	});
    	
    	KThread thread10 = new KThread();
    	Lib.debug(CommunicatorTestChar, "Communicator.manySpeakers(): thread (thread10) created.");
    	thread10.setName("thread 10");
    	thread10.setTarget(new Runnable() {
    		public void run(){
    			Lib.debug(CommunicatorTestChar, "Communicator.manySpeakers(): thread10 listening to word " 
    												+ manySpeakersComm.listen() + ".");
    		}
    	});
		//fork all 10 threads
		thread1.fork();
		thread2.fork();
		thread3.fork();
		thread4.fork();
		thread5.fork();
		thread6.fork();
		thread7.fork();
		thread8.fork();
		thread9.fork();
		thread10.fork();
		//Join all 10 threads
		thread1.join();
		thread2.join();
		thread3.join();
		thread4.join();
		thread5.join();
		thread6.join();
		thread7.join();
		thread8.join();
		thread9.join();
		thread10.join();

    	Lib.debug(CommunicatorTestChar, "Communicator.manySpeakers(): Finished multiple speaker test, passed.");
    	
    }
    
    /**
     * Tests whether the Communicator can handle a surplus of 
     * listeners before recieving any speakers. This should be 
     * the case as listeners are simply put to sleep until a speaker
     * is ready to pass along a word.
     */
    public static void manyListeners(){
    	
    	Lib.debug(CommunicatorTestChar, "Communicator.manyListeners(): Starting multiple listeners test.");
    	//Create a Communicator object
    	Communicator manyListenersComm = new Communicator();
    	
    	//Create 5 listeners that will listen to a word when a word becomes available after they are forked
    	KThread thread1 = new KThread();
   // 	Lib.debug(CommunicatorTestChar, "Communicator.manyListeners(): thread (thread1) created.");
    	thread1.setName("thread 1");
    	thread1.setTarget(new Runnable() {
    		public void run(){
    			Lib.debug(CommunicatorTestChar, "Communicator.manyListeners(): thread1 about to listen.");
    			Lib.debug(CommunicatorTestChar, "Communicator.manyListeners(): thread1 listened to word " 
    												+ manyListenersComm.listen() + ".");
    		}
    	});
    	
    	KThread thread2 = new KThread();
    	Lib.debug(CommunicatorTestChar, "Communicator.manyListeners(): thread (thread1) created.");
    	thread2.setName("thread 1");
    	thread2.setTarget(new Runnable() {
    		public void run(){
    			Lib.debug(CommunicatorTestChar, "Communicator.manyListeners(): thread1 about to listen.");
    			Lib.debug(CommunicatorTestChar, "Communicator.manyListeners(): thread1 listened to word " 
    												+ manyListenersComm.listen() + ".");
    		}
    	});
    	
    	KThread thread3 = new KThread();
    	thread3.setName("thread 2");
    	Lib.debug(CommunicatorTestChar, "Communicator.manyListeners(): thread (thread2) created.");
    	thread3.setTarget(new Runnable() {
    		public void run(){
    			Lib.debug(CommunicatorTestChar, "Communicator.manyListeners(): thread2 about to listen.");
    			Lib.debug(CommunicatorTestChar, "Communicator.manyListeners(): thread2 listened to word " 
    												+ manyListenersComm.listen() + ".");
    		}
    	});
    	
    	KThread thread4 = new KThread();
    	Lib.debug(CommunicatorTestChar, "Communicator.manyListeners(): thread (thread3) created.");
    	thread4.setName("thread 3");
    	thread4.setTarget(new Runnable() {
    		public void run(){
    			Lib.debug(CommunicatorTestChar, "Communicator.manyListeners(): thread3 about to listen.");
    			Lib.debug(CommunicatorTestChar, "Communicator.manyListeners(): thread3 listened to word " 
    												+ manyListenersComm.listen() + ".");
    		}
    	});
    	
    	KThread thread5 = new KThread();
    	Lib.debug(CommunicatorTestChar, "Communicator.manyListeners(): thread (thread4) created.");
    	thread5.setName("thread 4");
    	thread5.setTarget(new Runnable() {
    		public void run(){
    			Lib.debug(CommunicatorTestChar, "Communicator.manyListeners(): thread4 about to listen.");
    			Lib.debug(CommunicatorTestChar, "Communicator.manyListeners(): thread4 listened to word " 
    												+ manyListenersComm.listen() + ".");
    		}
    	});
    	
    	//Create 5 speakers that will speak a word (6-10) when they are forked
    	KThread thread6 = new KThread();
    	Lib.debug(CommunicatorTestChar, "Communicator.manyListeners(): thread (thread5) created.");
    	thread6.setName("thread 5");
    	thread6.setTarget(new Runnable() {
    		public void run(){
    			Lib.debug(CommunicatorTestChar, "Communicator.manyListeners(): thread5 speaking word 5.");
    			manyListenersComm.speak(5);
    		}
    	});
    	
    	KThread thread7 = new KThread();
    	Lib.debug(CommunicatorTestChar, "Communicator.manyListeners(): thread (thread6) created.");
    	thread7.setName("thread 6");
    	thread7.setTarget(new Runnable() {
    		public void run(){
    			Lib.debug(CommunicatorTestChar, "Communicator.manyListeners(): thread6 speaking word 6.");
    			manyListenersComm.speak(6);
    		}
    	});
    	
    	KThread thread8 = new KThread();
    	Lib.debug(CommunicatorTestChar, "Communicator.manyListeners(): thread (thread7) created.");
    	thread8.setName("thread 7");
    	thread8.setTarget(new Runnable() {
    		public void run(){
    			Lib.debug(CommunicatorTestChar, "Communicator.manyListeners(): thread7 speaking word 7.");
    			manyListenersComm.speak(7);
    		}
    	});
    	
    	KThread thread9 = new KThread();
    	Lib.debug(CommunicatorTestChar, "Communicator.manyListeners(): thread (thread8) created.");
    	thread9.setName("thread 8");
    	thread9.setTarget(new Runnable() {
    		public void run(){
    			Lib.debug(CommunicatorTestChar, "Communicator.manyListeners(): thread8 speaking word 8.");
    			manyListenersComm.speak(8);
    		}
    	});
    	
    	KThread thread10 = new KThread();
    	Lib.debug(CommunicatorTestChar, "Communicator.manyListeners(): thread (thread9) created.");
    	thread10.setName("thread 9");
    	thread10.setTarget(new Runnable() {
    		public void run(){
    		//	Lib.debug(CommunicatorTestChar, "Communicator.manyListeners(): thread9 speaking word 9.");
    			manyListenersComm.speak(9);
    		}
    	});
    	
		//Fork all 10 threads
		thread2.fork();
		thread3.fork();
		thread4.fork();
		thread5.fork();
		thread6.fork();
		thread7.fork();
		thread8.fork();
		thread9.fork();
	
		//Join all 10 threads
    		thread2.join();
		thread3.join();
		thread4.join();
		thread5.join();
		thread6.join();
		thread7.join();
		thread8.join();
		thread9.join();
    	Lib.debug(CommunicatorTestChar, "Communicator.manyListeners(): Finished multiple listener test, passed.");
    
    }
    
    /**
     * Tests whether a speaker will wait for a listener to complete the communicaion transaction
     */
    public static void speakerListenerTest(){
    
    	Lib.debug(CommunicatorTestChar, "Communicator.speakerListenerTest(): Starting speaker then listener test.");
    	//Create a Communicator
    	Communicator tester = new Communicator();
    	
    	//Create a speaker that will speak 321 when forked
    	KThread thread1 = new KThread();
    	Lib.debug(CommunicatorTestChar, "Communicator.speakerListenerTest(): thread (thread1) created.");
    	thread1.setName("thread 1");
    	thread1.setTarget(new Runnable() {
    		public void run(){
    			Lib.debug(CommunicatorTestChar, "Communicator.speakerListenerTest(): thread1 speaking word 321.");
    			tester.speak(321);
    		}
    	});
    	
    	//Create a listener that will listen to a word when forked
    	KThread thread2 = new KThread();
    	Lib.debug(CommunicatorTestChar, "Communicator.speakerListenerTest(): thread (thread2) created.");
    	thread2.setName("thread 2");
    	thread2.setTarget(new Runnable() {
    		public void run(){
    			Lib.debug(CommunicatorTestChar, "Communicator.speakerListenerTest(): thread2 listening "
    												+ "to word " + tester.listen() + ".");
    			tester.listen();
    		}
    	});
    	
    	//Fork both threads and join thread1
		thread1.fork();
		thread2.fork();
		thread1.join();
	
    	Lib.debug(CommunicatorTestChar, "Communicator.speakerListenerTest(): Finished speaker then listener test, passed.");
    }
    
    /**
     * Tests whether a listener will wait for a speaker to complete the communicaion transaction
     */
    public static void listenerSpeakerTest(){
    
    	Lib.debug(CommunicatorTestChar, "Communicator.listenerSpeakerTest(): Starting listener then speaker test.");
    	//Create a Communicator
    	Communicator tester = new Communicator();
    	
    	//Create a listener that will listen for a word once one is available after it has been forked
    	KThread thread1 = new KThread();
    	Lib.debug(CommunicatorTestChar, "Communicator.listenerSpeakerTest(): thread (thread1) created.");
    	thread1.setName("thread 1");
    	thread1.setTarget(new Runnable() {
    		public void run(){
    			Lib.debug(CommunicatorTestChar, "Communicator.listenerSpeakerTest(): thread1 about to listen.");
    			Lib.debug(CommunicatorTestChar, "Communicator.listenerSpeakerTest(): thread1 listened to word " 
    												+ tester.listen() + ".");
    		}
    	});
    	
    	//Create a speaker that will speak 123 when forked
    	KThread thread2 = new KThread();
    	Lib.debug(CommunicatorTestChar, "Communicator.listenerSpeakerTest(): thread (thread2) created.");
    	thread2.setName("thread 2");
    	thread2.setTarget(new Runnable() {
    		public void run(){
    			Lib.debug(CommunicatorTestChar, "Communicator.listenerSpeakerTest(): thread2 speaking word 123.");
    			tester.speak(123);
    		}
    	});
    
    	//Fork both threads and join thread1
		thread1.fork();
		thread2.fork();
		thread1.join();
	
    	Lib.debug(CommunicatorTestChar, "Communicator.listenerSpeakerTest(): Finished listener then speaker test, passed.");
    }
    
    //Datafields
    private static final char CommunicatorTestChar = 'C';
}
