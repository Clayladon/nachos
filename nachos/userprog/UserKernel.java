package nachos.userprog;

import nachos.machine.*;
import nachos.threads.*;
import nachos.userprog.*;

import java.util.LinkedList;

/**
 * A kernel that can support multiple user processes.
 */
public class UserKernel extends ThreadedKernel {
    /**
     * Allocate a new user kernel.
     */
    public UserKernel() {
		super();
	
		globalFreePageList = new LinkedList<TranslationEntry>();
    }

    /**
     * Initialize this kernel. Creates a synchronized console and sets the
     * processor's exception handler. Also sets the globalFreePageList with
     * fresh TranslationEntries.
     */
    public void initialize(String[] args) {
		super.initialize(args);

		console = new SynchConsole(Machine.console());
	
		Machine.processor().setExceptionHandler(new Runnable() {
			public void run() { exceptionHandler(); }
		    });
	    
		//Get the number of physical pages available 
		int numPages = Machine.processor().getNumPhysPages();
		
		//Fills the globalFreePageList with new TranslationEntries
		for(int i=0; i<numPages; ++i)
			globalFreePageList.add(new TranslationEntry(0, i, false, false, false, false));
		
		//Initialize the pageLock
		pageLock = new Lock();
    }

    /**
     * Test the console device.
     */	
    public void selfTest() {
	super.selfTest();
	
	UserProcess testP = null;	
	if(Lib.test(vmTestChar)){
		testP = new UserProcess();
		testP.selfTest();
	}

	System.out.println("Testing the console device. Typed characters");
	System.out.println("will be echoed until q is typed.");

	char c;

	do {
	    c = (char) console.readByte(true);
	    console.writeByte(c);
	}
	while (c != 'q');

	System.out.println("");
	if(Lib.test(vmTestChar))
		testP.handleExit(0);
	
    }

    /**
     * Returns the current process.
     *
     * @return	the current process, or <tt>null</tt> if no process is current.
     */
    public static UserProcess currentProcess() {
	if (!(KThread.currentThread() instanceof UThread))
	    return null;
	
	return ((UThread) KThread.currentThread()).process;
    }

    /**
     * The exception handler. This handler is called by the processor whenever
     * a user instruction causes a processor exception.
     *
     * <p>
     * When the exception handler is invoked, interrupts are enabled, and the
     * processor's cause register contains an integer identifying the cause of
     * the exception (see the <tt>exceptionZZZ</tt> constants in the
     * <tt>Processor</tt> class). If the exception involves a bad virtual
     * address (e.g. page fault, TLB miss, read-only, bus error, or address
     * error), the processor's BadVAddr register identifies the virtual address
     * that caused the exception.
     */
    public void exceptionHandler() {
	Lib.assertTrue(KThread.currentThread() instanceof UThread);

	UserProcess process = ((UThread) KThread.currentThread()).process;
	int cause = Machine.processor().readRegister(Processor.regCause);
	process.handleException(cause);
    }

    /**
     * Start running user programs, by creating a process and running a shell
     * program in it. The name of the shell program it must run is returned by
     * <tt>Machine.getShellProgramName()</tt>.
     *
     * @see	nachos.machine.Machine#getShellProgramName
     */
    public void run() {
	super.run();

	UserProcess process = UserProcess.newUserProcess();

	String shellProgram = Machine.getShellProgramName();	
	Lib.assertTrue(process.execute(shellProgram, new String[] { }));

	KThread.currentThread().finish();
    }

    /**
     * Terminate this kernel. Never returns.
     */
    public void terminate() {
	super.terminate();
    }
    
    /**
     * This method is responsible for allocating free pages to user processes. A user process
     * makes a call to this method when it needs more memory. If there are enough free physical
     * pages to satisfy the request, a TranslationEntry array is returned. The requested pages
     * are then added to the user process' pageTable. If there aren't enough pages the method
     * throws an InsufficientFreePagesException.
     */
    public TranslationEntry[] getPages(int amount) throws InsufficientFreePagesException{
    	//Acquire the lock
    	pageLock.acquire();
    	
    	//If there are enough pages left to accomodate the request
    	if(!globalFreePageList.isEmpty() && globalFreePageList.size() >= amount){
    		//Create a new array of the requested pages
    		TranslationEntry[] requestedPages = new TranslationEntry[amount];
    		
    		//Cycle through the pages to remove them from the free pages and validate them
    		for(int i=0; i<requestedPages.length; ++i){
    			requestedPages[i] = globalFreePageList.remove();
    			requestedPages[i].valid = true;
    		}
    		
    		//Release the lock and return the requested pages
    		pageLock.release();
    		return requestedPages;
    	}
    	//If there are not enough pages left to accomodate the reques
    	else{
    		//Release the lock and throw an InsufficientFreePagesException
    		pageLock.release();
    		throw new InsufficientFreePagesException();
    	}
    }
    
    /**
     * This method releases a user process' pages. It takes the user process' pageTable
     * and adds all of its elements to the globalFreePageList. It also sets each element's
     * valid flag to false and fills the array with nulls. This ensures that the released
     * pages cannot be accessed without first being reallocated by the UserKernel.
     */
    public void releasePageTable(TranslationEntry[] pageTable){
    	pageLock.acquire();
    	
    	for(int i=0; i<pageTable.length; ++i){
    		pageTable[i].valid = false;
    		globalFreePageList.add(pageTable[i]);
    	}
    	
    	pageLock.release();
    }
    
    public class InsufficientFreePagesException extends Exception{}

    /** Globally accessible reference to the synchronized console. */
    public static SynchConsole console;

    // dummy variables to make javac smarter
    private static Coff dummy1 = null;
    
    //Custom variables
    public LinkedList<TranslationEntry> globalFreePageList;
    public Lock pageLock;

	//debug flag
	public static final char vmTestChar = 'j';
}
