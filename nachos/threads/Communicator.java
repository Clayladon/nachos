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
    	System.out.println("Entered Communicator.selfTest()");
    	//manySpeakers();
    	//manyListeners();
    	speakerListenerTest();
    	//listenerSpeakerTest();
    	
    }
    public static void manySpeakers(){
    	
    	Communicator manySpeakersComm = new Communicator();
    	
    	manySpeakersComm.speak(0);
    	manySpeakersComm.speak(1);
    	manySpeakersComm.speak(2);
    	manySpeakersComm.speak(3);
    	manySpeakersComm.speak(4);
    	manySpeakersComm.speak(5);
    	manySpeakersComm.speak(6);
    	manySpeakersComm.speak(7);
    	manySpeakersComm.speak(8);
    	manySpeakersComm.speak(9);
    	
    	manySpeakersComm.listen();
    	manySpeakersComm.listen();
    	manySpeakersComm.listen();
    	manySpeakersComm.listen();
    	manySpeakersComm.listen();
    	manySpeakersComm.listen();
    	manySpeakersComm.listen();
    	manySpeakersComm.listen();
    	manySpeakersComm.listen();
    	manySpeakersComm.listen();
    	
    	System.out.println("Multiple speaker test succeeded!");
    	
    }
    public static void manyListeners(){
    	
    	Communicator manySpeakersComm = new Communicator();
    	
    	manySpeakersComm.listen();
    	manySpeakersComm.listen();
    	manySpeakersComm.listen();
    	manySpeakersComm.listen();
    	manySpeakersComm.listen();
    	manySpeakersComm.listen();
    	manySpeakersComm.listen();
    	manySpeakersComm.listen();
    	manySpeakersComm.listen();
    	manySpeakersComm.listen();
    	
    	manySpeakersComm.speak(0);
    	manySpeakersComm.speak(1);
    	manySpeakersComm.speak(2);
    	manySpeakersComm.speak(3);
    	manySpeakersComm.speak(4);
    	manySpeakersComm.speak(5);
    	manySpeakersComm.speak(6);
    	manySpeakersComm.speak(7);
    	manySpeakersComm.speak(8);
    	manySpeakersComm.speak(9);
    	
    	System.out.println("Multiple listener test succeeded!");
    
    }
    
    public static void speakerListenerTest(){
    	System.out.println("Entered Communicator.speakerListenerTest()");
    	Communicator tester = new Communicator();
    	
    	System.out.println("Created communicator object, speaking...");
    	
    	tester.speak(123);
    	System.out.println("Spoke word, listening...");
    	tester.listen();
    	
    	System.out.println("speakerListenerTest() succeeded!");
    }
    
    public static void listenerSpeakerTest(){
    	System.out.println("Entered Communicator.listenerSpeakerTest()");
    	Communicator tester = new Communicator();
    	
    	System.out.println("Created communicator object, listening...");
    	
    	tester.listen();
    	System.out.println("Listening for word, speaking...");
    	tester.speak(123);
    	
    	System.out.println("listenerSpeakerTest() succeeded!");
    }
}
