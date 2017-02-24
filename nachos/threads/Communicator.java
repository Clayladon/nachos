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
    
    public static void selfTest(){
    	Lib.debug(CommunicatorTestChar, "Communicator.selfTest(): Starting self test.");
    	
    	manySpeakers();
    	manyListeners();
    	speakerListenerTest();
    	listenerSpeakerTest();
    	
    	Lib.debug(CommunicatorTestChar, "Communicator.selfTest(): Finished selfTest(), passed.");
    }
    public static void manySpeakers(){
    	
    	Lib.debug(CommunicatorTestChar, "Communicator.manySpeakers(): Starting multiple speakers test.");
    	Communicator manySpeakersComm = new Communicator();
    	
    	KThread thread1 = new KThread();
    	Lib.debug(CommunicatorTestChar, "Communicator.manySpeakers(): thread (thread1) created.");
    	thread1.setName("thread 1");
    	thread1.setTarget(new Runnable() {
    		public void run(){
    			Lib.debug(CommunicatorTestChar, "Communicator.manySpeakers(): thread1 speaking word 1.");
    			manySpeakersComm.speak(1);
    		}
    	});
    	thread1.fork();
    	
    	KThread thread2 = new KThread();
    	Lib.debug(CommunicatorTestChar, "Communicator.manySpeakers(): thread (thread2) created.");
    	thread2.setName("thread 2");
    	thread2.setTarget(new Runnable() {
    		public void run(){
    			Lib.debug(CommunicatorTestChar, "Communicator.manySpeakers(): thread2 speaking word 2.");
    			manySpeakersComm.speak(2);
    		}
    	});
    	thread2.fork();
    	
    	KThread thread3 = new KThread();
    	Lib.debug(CommunicatorTestChar, "Communicator.manySpeakers(): thread (thread3) created.");
    	thread3.setName("thread 3");
    	thread3.setTarget(new Runnable() {
    		public void run(){
    			Lib.debug(CommunicatorTestChar, "Communicator.manySpeakers(): thread3 speaking word 3.");
    			manySpeakersComm.speak(3);
    		}
    	});
    	thread3.fork();
    	
    	KThread thread4 = new KThread();
    	Lib.debug(CommunicatorTestChar, "Communicator.manySpeakers(): thread (thread4) created.");
    	thread4.setName("thread 4");
    	thread4.setTarget(new Runnable() {
    		public void run(){
    			Lib.debug(CommunicatorTestChar, "Communicator.manySpeakers(): thread4 speaking word 4.");
    			manySpeakersComm.speak(4);
    		}
    	});
    	thread4.fork();
    	
    	KThread thread5 = new KThread();
    	Lib.debug(CommunicatorTestChar, "Communicator.manySpeakers(): thread (thread5) created.");
    	thread5.setName("thread 5");
    	thread5.setTarget(new Runnable() {
    		public void run(){
    			Lib.debug(CommunicatorTestChar, "Communicator.manySpeakers(): thread5 speaking word 5.");
    			manySpeakersComm.speak(5);
    		}
    	});
    	thread5.fork();
    	
    	KThread thread6 = new KThread();
    	Lib.debug(CommunicatorTestChar, "Communicator.manySpeakers(): thread (thread6) created.");
    	thread6.setName("thread 6");
    	thread6.setTarget(new Runnable() {
    		public void run(){
    			Lib.debug(CommunicatorTestChar, "Communicator.manySpeakers(): thread6 listening to word " 
    												+ manySpeakersComm.listen() + ".");
    		}
    	});
    	thread6.fork();
    	
    	KThread thread7 = new KThread();
    	Lib.debug(CommunicatorTestChar, "Communicator.manySpeakers(): thread (thread7) created.");
    	thread7.setName("thread 7");
    	thread7.setTarget(new Runnable() {
    		public void run(){
    			Lib.debug(CommunicatorTestChar, "Communicator.manySpeakers(): thread7 listening to word " 
    												+ manySpeakersComm.listen() + ".");
    		}
    	});
    	thread7.fork();
    	
    	KThread thread8 = new KThread();
    	Lib.debug(CommunicatorTestChar, "Communicator.manySpeakers(): thread (thread8) created.");
    	thread8.setName("thread 8");
    	thread8.setTarget(new Runnable() {
    		public void run(){
    			Lib.debug(CommunicatorTestChar, "Communicator.manySpeakers(): thread8 listening to word " 
    												+ manySpeakersComm.listen() + ".");
    		}
    	});
    	thread8.fork();
    	
    	KThread thread9 = new KThread();
    	Lib.debug(CommunicatorTestChar, "Communicator.manySpeakers(): thread (thread9) created.");
    	thread9.setName("thread 9");
    	thread9.setTarget(new Runnable() {
    		public void run(){
    			Lib.debug(CommunicatorTestChar, "Communicator.manySpeakers(): thread9 listening to word " 
    												+ manySpeakersComm.listen() + ".");
    		}
    	});
    	thread9.fork();
    	
    	KThread thread10 = new KThread();
    	Lib.debug(CommunicatorTestChar, "Communicator.manySpeakers(): thread (thread10) created.");
    	thread10.setName("thread 10");
    	thread10.setTarget(new Runnable() {
    		public void run(){
    			Lib.debug(CommunicatorTestChar, "Communicator.manySpeakers(): thread10 listening to word " 
    												+ manySpeakersComm.listen() + ".");
    		}
    	});
    	thread10.fork();
    	
    	Lib.debug(CommunicatorTestChar, "Communicator.manySpeakers(): Finished multiple speaker test, passed.");
    	
    }
    public static void manyListeners(){
    	
    	Lib.debug(CommunicatorTestChar, "Communicator.manyListeners(): Starting multiple listeners test.");
    	Communicator manyListenersComm = new Communicator();
    	
    	KThread thread1 = new KThread();
    	Lib.debug(CommunicatorTestChar, "Communicator.manyListeners(): thread (thread1) created.");
    	thread1.setName("thread 1");
    	thread1.setTarget(new Runnable() {
    		public void run(){
    			Lib.debug(CommunicatorTestChar, "Communicator.manyListeners(): thread1 about to listen.");
    			Lib.debug(CommunicatorTestChar, "Communicator.manyListeners(): thread1 listened to word " 
    												+ manyListenersComm.listen() + ".");
    		}
    	});
    	thread1.fork();
    	
    	KThread thread2 = new KThread();
    	Lib.debug(CommunicatorTestChar, "Communicator.manyListeners(): thread (thread2) created.");
    	thread2.setName("thread 2");
    	thread2.setTarget(new Runnable() {
    		public void run(){
    			Lib.debug(CommunicatorTestChar, "Communicator.manyListeners(): thread2 about to listen.");
    			Lib.debug(CommunicatorTestChar, "Communicator.manyListeners(): thread2 listened to word " 
    												+ manyListenersComm.listen() + ".");
    		}
    	});
    	thread2.fork();
    	
    	KThread thread3 = new KThread();
    	thread3.setName("thread 3");
    	Lib.debug(CommunicatorTestChar, "Communicator.manyListeners(): thread (thread3) created.");
    	thread3.setTarget(new Runnable() {
    		public void run(){
    			Lib.debug(CommunicatorTestChar, "Communicator.manyListeners(): thread3 about to listen.");
    			Lib.debug(CommunicatorTestChar, "Communicator.manyListeners(): thread3 listened to word " 
    												+ manyListenersComm.listen() + ".");
    		}
    	});
    	thread3.fork();
    	
    	KThread thread4 = new KThread();
    	Lib.debug(CommunicatorTestChar, "Communicator.manyListeners(): thread (thread4) created.");
    	thread4.setName("thread 4");
    	thread4.setTarget(new Runnable() {
    		public void run(){
    			Lib.debug(CommunicatorTestChar, "Communicator.manyListeners(): thread4 about to listen.");
    			Lib.debug(CommunicatorTestChar, "Communicator.manyListeners(): thread4 listened to word " 
    												+ manyListenersComm.listen() + ".");
    		}
    	});
    	thread4.fork();
    	
    	KThread thread5 = new KThread();
    	Lib.debug(CommunicatorTestChar, "Communicator.manyListeners(): thread (thread5) created.");
    	thread5.setName("thread 5");
    	thread5.setTarget(new Runnable() {
    		public void run(){
    			Lib.debug(CommunicatorTestChar, "Communicator.manyListeners(): thread5 about to listen.");
    			Lib.debug(CommunicatorTestChar, "Communicator.manyListeners(): thread5 listened to word " 
    												+ manyListenersComm.listen() + ".");
    		}
    	});
    	thread5.fork();
    	
    	KThread thread6 = new KThread();
    	Lib.debug(CommunicatorTestChar, "Communicator.manyListeners(): thread (thread6) created.");
    	thread6.setName("thread 6");
    	thread6.setTarget(new Runnable() {
    		public void run(){
    			Lib.debug(CommunicatorTestChar, "Communicator.manyListeners(): thread6 speaking word 6.");
    			manyListenersComm.speak(6);
    		}
    	});
    	thread6.fork();
    	
    	KThread thread7 = new KThread();
    	Lib.debug(CommunicatorTestChar, "Communicator.manyListeners(): thread (thread7) created.");
    	thread7.setName("thread 7");
    	thread7.setTarget(new Runnable() {
    		public void run(){
    			Lib.debug(CommunicatorTestChar, "Communicator.manyListeners(): thread7 speaking word 7.");
    			manyListenersComm.speak(7);
    		}
    	});
    	thread7.fork();
    	
    	KThread thread8 = new KThread();
    	Lib.debug(CommunicatorTestChar, "Communicator.manyListeners(): thread (thread8) created.");
    	thread8.setName("thread 8");
    	thread8.setTarget(new Runnable() {
    		public void run(){
    			Lib.debug(CommunicatorTestChar, "Communicator.manyListeners(): thread8 speaking word 8.");
    			manyListenersComm.speak(8);
    		}
    	});
    	thread8.fork();
    	
    	KThread thread9 = new KThread();
    	Lib.debug(CommunicatorTestChar, "Communicator.manyListeners(): thread (thread9) created.");
    	thread9.setName("thread 9");
    	thread9.setTarget(new Runnable() {
    		public void run(){
    			Lib.debug(CommunicatorTestChar, "Communicator.manyListeners(): thread9 speaking word 9.");
    			manyListenersComm.speak(9);
    		}
    	});
    	thread9.fork();
    	
    	KThread thread10 = new KThread();
    	Lib.debug(CommunicatorTestChar, "Communicator.manyListeners(): thread (thread10) created.");
    	thread10.setName("thread 10");
    	thread10.setTarget(new Runnable() {
    		public void run(){
    			Lib.debug(CommunicatorTestChar, "Communicator.manyListeners(): thread10 speaking word 10.");
    			manyListenersComm.speak(10);
    		}
    	});
    	thread10.fork();
    	
    	Lib.debug(CommunicatorTestChar, "Communicator.manyListeners(): Finished multiple listener test, passed.");
    
    }
    
    public static void speakerListenerTest(){
    
    	Lib.debug(CommunicatorTestChar, "Communicator.speakerListenerTest(): Starting speaker then listener test.");
    	Communicator tester = new Communicator();
    	
    	KThread thread1 = new KThread();
    	Lib.debug(CommunicatorTestChar, "Communicator.speakerListenerTest(): thread (thread1) created.");
    	thread1.setName("thread 1");
    	thread1.setTarget(new Runnable() {
    		public void run(){
    			Lib.debug(CommunicatorTestChar, "Communicator.speakerListenerTest(): thread1 speaking word 321.");
    			tester.speak(321);
    		}
    	});
    	thread1.fork();
    	
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
    	thread2.fork();
    	
    	Lib.debug(CommunicatorTestChar, "Communicator.speakerListenerTest(): Finished speaker then listener test, passed.");
    }
    
    public static void listenerSpeakerTest(){
    
    	Lib.debug(CommunicatorTestChar, "Communicator.listenerSpeakerTest(): Starting listener then speaker test.");
    	Communicator tester = new Communicator();
    	
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
    	thread1.fork();
    	
    	
    	KThread thread2 = new KThread();
    	Lib.debug(CommunicatorTestChar, "Communicator.listenerSpeakerTest(): thread (thread2) created.");
    	thread2.setName("thread 2");
    	thread2.setTarget(new Runnable() {
    		public void run(){
    			Lib.debug(CommunicatorTestChar, "Communicator.listenerSpeakerTest(): thread2 speaking word 123.");
    			tester.speak(123);
    		}
    	});
    	thread2.fork();
    	
    	Lib.debug(CommunicatorTestChar, "Communicator.listenerSpeakerTest(): Finished listener then speaker test, passed.");
    }
    
    private static final char CommunicatorTestChar = 'C';
}
