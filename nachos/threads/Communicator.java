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
    	//manySpeakers();
    	manyListeners();
    	//speakerListenerTest();
    	//listenerSpeakerTest();
    	
    }
    public static void manySpeakers(){
    	
    	Communicator manySpeakersComm = new Communicator();
    	
    	KThread thread1 = new KThread();
    	thread1.setName("thread 1");
    	thread1.setTarget(new Runnable() {
    		public void run(){
    			manySpeakersComm.speak(1);
    		}
    	});
    	thread1.fork();
    	
    	KThread thread2 = new KThread();
    	thread2.setName("thread 2");
    	thread2.setTarget(new Runnable() {
    		public void run(){
    			manySpeakersComm.speak(2);
    		}
    	});
    	thread2.fork();
    	
    	KThread thread3 = new KThread();
    	thread3.setName("thread 3");
    	thread3.setTarget(new Runnable() {
    		public void run(){
    			manySpeakersComm.speak(3);
    		}
    	});
    	thread3.fork();
    	
    	KThread thread4 = new KThread();
    	thread4.setName("thread 4");
    	thread4.setTarget(new Runnable() {
    		public void run(){
    			manySpeakersComm.speak(4);
    		}
    	});
    	thread4.fork();
    	
    	KThread thread5 = new KThread();
    	thread5.setName("thread 5");
    	thread5.setTarget(new Runnable() {
    		public void run(){
    			manySpeakersComm.speak(5);
    		}
    	});
    	thread5.fork();
    	
    	KThread thread6 = new KThread();
    	thread6.setName("thread 6");
    	thread6.setTarget(new Runnable() {
    		public void run(){
    			manySpeakersComm.listen();
    		}
    	});
    	thread6.fork();
    	
    	KThread thread7 = new KThread();
    	thread7.setName("thread 7");
    	thread7.setTarget(new Runnable() {
    		public void run(){
    			manySpeakersComm.listen();
    		}
    	});
    	thread7.fork();
    	
    	KThread thread8 = new KThread();
    	thread8.setName("thread 8");
    	thread8.setTarget(new Runnable() {
    		public void run(){
    			manySpeakersComm.listen();
    		}
    	});
    	thread8.fork();
    	
    	KThread thread9 = new KThread();
    	thread9.setName("thread 9");
    	thread9.setTarget(new Runnable() {
    		public void run(){
    			manySpeakersComm.listen();
    		}
    	});
    	thread9.fork();
    	
    	KThread thread10 = new KThread();
    	thread10.setName("thread 10");
    	thread10.setTarget(new Runnable() {
    		public void run(){
    			manySpeakersComm.listen();
    		}
    	});
    	thread10.fork();
    	
    	System.out.println("Multiple speaker test succeeded!");
    	
    }
    public static void manyListeners(){
    	
    	Communicator manyListenersComm = new Communicator();
    	
    	KThread thread1 = new KThread();
    	thread1.setName("thread 1");
    	thread1.setTarget(new Runnable() {
    		public void run(){
    			manyListenersComm.listen();
    		}
    	});
    	thread1.fork();
    	
    	KThread thread2 = new KThread();
    	thread2.setName("thread 2");
    	thread2.setTarget(new Runnable() {
    		public void run(){
    			manyListenersComm.listen();
    		}
    	});
    	thread2.fork();
    	
    	KThread thread3 = new KThread();
    	thread3.setName("thread 3");
    	thread3.setTarget(new Runnable() {
    		public void run(){
    			manyListenersComm.listen();
    		}
    	});
    	thread3.fork();
    	
    	KThread thread4 = new KThread();
    	thread4.setName("thread 4");
    	thread4.setTarget(new Runnable() {
    		public void run(){
    			manyListenersComm.listen();
    		}
    	});
    	thread4.fork();
    	
    	KThread thread5 = new KThread();
    	thread5.setName("thread 5");
    	thread5.setTarget(new Runnable() {
    		public void run(){
    			manyListenersComm.listen();
    		}
    	});
    	thread5.fork();
    	
    	KThread thread6 = new KThread();
    	thread6.setName("thread 6");
    	thread6.setTarget(new Runnable() {
    		public void run(){
    			manyListenersComm.speak(6);
    		}
    	});
    	thread6.fork();
    	
    	KThread thread7 = new KThread();
    	thread7.setName("thread 7");
    	thread7.setTarget(new Runnable() {
    		public void run(){
    			manyListenersComm.speak(7);
    		}
    	});
    	thread7.fork();
    	
    	KThread thread8 = new KThread();
    	thread8.setName("thread 8");
    	thread8.setTarget(new Runnable() {
    		public void run(){
    			manyListenersComm.speak(8);
    		}
    	});
    	thread8.fork();
    	
    	KThread thread9 = new KThread();
    	thread9.setName("thread 9");
    	thread9.setTarget(new Runnable() {
    		public void run(){
    			manyListenersComm.speak(9);
    		}
    	});
    	thread9.fork();
    	
    	KThread thread10 = new KThread();
    	thread10.setName("thread 10");
    	thread10.setTarget(new Runnable() {
    		public void run(){
    			manyListenersComm.speak(10);
    		}
    	});
    	thread10.fork();
    	
    	System.out.println("Multiple listener test succeeded!");
    
    }
    
    public static void speakerListenerTest(){
    	Communicator tester = new Communicator();
    	
    	KThread thread1 = new KThread();
    	thread1.setName("thread 1");
    	thread1.setTarget(new Runnable() {
    		public void run(){
    			tester.speak(1);
    		}
    	});
    	thread1.fork();
    	
    	KThread thread2 = new KThread();
    	thread2.setName("thread 2");
    	thread2.setTarget(new Runnable() {
    		public void run(){
    			tester.listen();
    		}
    	});
    	thread2.fork();
    	
    	System.out.println("speakerListenerTest() succeeded!");
    }
    
    public static void listenerSpeakerTest(){
    	Communicator tester = new Communicator();
    	
    	KThread thread1 = new KThread();
    	thread1.setName("thread 1");
    	thread1.setTarget(new Runnable() {
    		public void run(){
    			tester.listen();
    		}
    	});
    	thread1.fork();
    	
    	
    	KThread thread2 = new KThread();
    	thread2.setName("thread 2");
    	thread2.setTarget(new Runnable() {
    		public void run(){
    			tester.speak(123);
    		}
    	});
    	thread2.fork();
    	
    	System.out.println("listenerSpeakerTest() succeeded!");
    }
}
