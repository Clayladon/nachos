package nachos.userprog;

import nachos.machine.*;
import nachos.threads.*;
import nachos.userprog.*;
import nachos.userprog.UserKernel.InsufficientFreePagesException;
import java.io.EOFException;
import java.util.HashMap;

/**
 * Encapsulates the state of a user process that is not contained in its
 * user thread (or threads). This includes its address translation state, a
 * file table, and information about the program being executed.
 *
 * <p>
 * This class is extended by other classes to support additional functionality
 * (such as additional syscalls).
 *
 * @see	nachos.vm.VMProcess
 * @see	nachos.network.NetProcess
 */
public class UserProcess {
    /**
     * Allocate a new process.
     */
    public UserProcess() {
		int numPhysPages = Machine.processor().getNumPhysPages();
		pageTable = new TranslationEntry[numPhysPages];
		for (int i=0; i<numPhysPages; i++)
		    pageTable[i] = new TranslationEntry(i,i, true,false,false,false);
    	
    	//Task 1
    	processID = numProcesses;
    	numProcesses++;
    	localFileArray = new OpenFile[16];
    	globalFileRefArray = new FileReference[16];
    	
		localFileArray[0] = UserKernel.console.openForReading();
		localFileArray[1] = UserKernel.console.openForWriting();
		globalFileRefArray[0] = new FileReference(localFileArray[0].getName());
		globalFileRefArray[1] = new FileReference(localFileArray[1].getName());
    
	 
    	//Task 2
    	memoryLock = new Lock();
	
    	
    	//Task 3
    	hasExited = false;
    	joinLock = new Lock();
    	waitingToJoin = new Condition(joinLock);
    	children = new HashMap<Integer, ChildProcess>();
    }
    
    /**
     * Allocate and return a new process of the correct class. The class name
     * is specified by the <tt>nachos.conf</tt> key
     * <tt>Kernel.processClassName</tt>.
     *
     * @return	a new process of the correct class.
     */
    public static UserProcess newUserProcess() {
	return (UserProcess)Lib.constructObject(Machine.getProcessClassName());
    }

    /**
     * Execute the specified program with the specified arguments. Attempts to
     * load the program, and then forks a thread to run it.
     *
     * @param	name	the name of the file containing the executable.
     * @param	args	the arguments to pass to the executable.
     * @return	<tt>true</tt> if the program was successfully executed.
     */
    public boolean execute(String name, String[] args) {
	if (!load(name, args))
	    return false;
	
	new UThread(this).setName(name).fork();

	return true;

    }

    /**
     * Save the state of this process in preparation for a context switch.
     * Called by <tt>UThread.saveState()</tt>.
     */
    public void saveState() {
    }

    /**
     * Restore the state of this process after a context switch. Called by
     * <tt>UThread.restoreState()</tt>.
     */
    public void restoreState() {
	Machine.processor().setPageTable(pageTable);
    }
    
    /**
     * This method is responsible for every memory access executed by a user process. First
     * the virtual addesses are translated into physical addresses which is then validated.
     * If the address is valid the method accesses the physical memory. If isRead is set
     * to true then a section of memory is copied into data via the arraycopy method.
     * If isRead is false and the page isn't readOnly, data is copied into a section of
     * physical memory again via arraycopy. The return value is the amount of bytes accessed.
     */
    public int accessMemory(int vaddr, byte[] data, int offset, int length, boolean isRead){
    	//Get the virtual page number and virtual offset
    	int vpn = vaddr / pageSize;
    	int vOffset = vaddr % pageSize;
    	
    	//Access the entry being accessed and set its used field to true
    	TranslationEntry entry = pageTable[vpn]; 
    	entry.used = true;
    	
    	//Calculate the physical address and memory available
    	int addr = entry.ppn * pageSize + vOffset;
    	byte[] memory = Machine.processor().getMemory();
    	
    	//If the physical address is out of bounds return 0
    	if(addr < 0 || addr > memory.length || !entry.valid)
    		return 0;
    		
    	//Set the amount of bytes accessed
    	int amount = Math.min(length, memory.length - addr);
    	//If the method is reading
    	if(isRead)
    		//Copy from memory into data
    		System.arraycopy(memory, addr, data, offset, amount);
    	//If the method is writing
    	else
    		//And the page is not readOnly
    		if(!entry.readOnly)
    			//Copy into memory from data
    			System.arraycopy(data, offset, memory, addr, amount);
    		else
    			//If if is read only return 0
    			return 0;
    			
    	//Finally return the amount of bytes accessed
    	return amount;
    }

    /**
     * Read a null-terminated string from this process's virtual memory. Read
     * at most <tt>maxLength + 1</tt> bytes from the specified address, search
     * for the null terminator, and convert it to a <tt>java.lang.String</tt>,
     * without including the null terminator. If no null terminator is found,
     * returns <tt>null</tt>.
     *
     * @param	vaddr	the starting virtual address of the null-terminated
     *			string.
     * @param	maxLength	the maximum number of characters in the string,
     *				not including the null terminator.
     * @return	the string read, or <tt>null</tt> if no null terminator was
     *		found.
     */
    public String readVirtualMemoryString(int vaddr, int maxLength) {
	Lib.assertTrue(maxLength >= 0);

	byte[] bytes = new byte[maxLength+1];

	int bytesRead = readVirtualMemory(vaddr, bytes);

	for (int length=0; length<bytesRead; length++) {
	    if (bytes[length] == 0)
		return new String(bytes, 0, length);
	}

	return null;
    }

    /**
     * Transfer data from this process's virtual memory to all of the specified
     * array. Same as <tt>readVirtualMemory(vaddr, data, 0, data.length)</tt>.
     *
     * @param	vaddr	the first byte of virtual memory to read.
     * @param	data	the array where the data will be stored.
     * @return	the number of bytes successfully transferred.
     */
    public int readVirtualMemory(int vaddr, byte[] data) {
	return readVirtualMemory(vaddr, data, 0, data.length);
    }

    /**
     * Transfer data from this process's virtual memory to the specified array.
     * This method handles address translation details. This method must
     * <i>not</i> destroy the current process if an error occurs, but instead
     * should return the number of bytes successfully copied (or zero if no
     * data could be copied).
     *
     * @param	vaddr	the first byte of virtual memory to read.
     * @param	data	the array where the data will be stored.
     * @param	offset	the first byte to write in the array.
     * @param	length	the number of bytes to transfer from virtual memory to
     *			the array.
     * @return	the number of bytes successfully transferred.
     */
    public int readVirtualMemory(int vaddr, byte[] data, int offset,
				 int length) {
		//Acquire the lock
		memoryLock.acquire();

		//Call the accessMemory method with isRead set to true
		int pages = ((length + vaddr%pageSize)/pageSize)+1;
		int firstLength = Math.min(length, pageSize - vaddr%pageSize);

		int amount = accessMemory(vaddr, data, offset, firstLength, true);
		if(pages > 1){
			for(int i = 1; i < pages; ++i){
				amount += accessMemory((vaddr/pageSize +i*pageSize), data, offset+amount, Math.min(length-amount, pageSize), true);
			}
		}
		
		//Release the lock
		memoryLock.release();

		//Return the amount of bytes read
		return amount;
    }

    /**
     * Transfer all data from the specified array to this process's virtual
     * memory.
     * Same as <tt>writeVirtualMemory(vaddr, data, 0, data.length)</tt>.
     *
     * @param	vaddr	the first byte of virtual memory to write.
     * @param	data	the array containing the data to transfer.
     * @return	the number of bytes successfully transferred.
     */
    public int writeVirtualMemory(int vaddr, byte[] data) {
	return writeVirtualMemory(vaddr, data, 0, data.length);
    }

    /**
     * Transfer data from the specified array to this process's virtual memory.
     * This method handles address translation details. This method must
     * <i>not</i> destroy the current process if an error occurs, but instead
     * should return the number of bytes successfully copied (or zero if no
     * data could be copied).
     *
     * @param	vaddr	the first byte of virtual memory to write.
     * @param	data	the array containing the data to transfer.
     * @param	offset	the first byte to transfer from the array.
     * @param	length	the number of bytes to transfer from the array to
     *			virtual memory.
     * @return	the number of bytes successfully transferred.
     */
    public int writeVirtualMemory(int vaddr, byte[] data, int offset,
				  int length) {
		//Acquire the lock
		memoryLock.acquire();

		int pages = ((length + vaddr%pageSize)/pageSize)+1;
		int firstLength = Math.min(length, pageSize - vaddr%pageSize);

		int amount = accessMemory(vaddr, data, offset, firstLength, false);
		if(pages > 1){
			for(int i = 1; i < pages; ++i){
				amount += accessMemory((vaddr/pageSize +i*pageSize), data, offset+amount, Math.min(length-amount, pageSize), false);
			}
		}


		//Release the lock
		memoryLock.release();

		//Return the amount of bytes written
		return amount;
    }

    /**
     * Load the executable with the specified name into this process, and
     * prepare to pass it the specified arguments. Opens the executable, reads
     * its header information, and copies sections and arguments into this
     * process's virtual memory.
     *
     * @param	name	the name of the file containing the executable.
     * @param	args	the arguments to pass to the executable.
     * @return	<tt>true</tt> if the executable was successfully loaded.
     */
    private boolean load(String name, String[] args) {
	Lib.debug(dbgProcess, "UserProcess.load(\"" + name + "\")");
	
	OpenFile executable = ThreadedKernel.fileSystem.open(name, false);
	if (executable == null) {
	    Lib.debug(dbgProcess, "\topen failed");
	    return false;
	}

	try {
	    coff = new Coff(executable);
	}
	catch (EOFException e) {
	    executable.close();
	    Lib.debug(dbgProcess, "\tcoff load failed");
	    return false;
	}

	// make sure the sections are contiguous and start at page 0
	numPages = 0;
	for (int s=0; s<coff.getNumSections(); s++) {
	    CoffSection section = coff.getSection(s);
	    if (section.getFirstVPN() != numPages) {
		coff.close();
		Lib.debug(dbgProcess, "\tfragmented executable");
		return false;
	    }
	    numPages += section.getLength();
	}

	// make sure the argv array will fit in one page
	byte[][] argv = new byte[args.length][];
	int argsSize = 0;
	for (int i=0; i<args.length; i++) {
	    argv[i] = args[i].getBytes();
	    // 4 bytes for argv[] pointer; then string plus one for null byte
	    argsSize += 4 + argv[i].length + 1;
	}
	if (argsSize > pageSize) {
	    coff.close();
	    Lib.debug(dbgProcess, "\targuments too long");
	    return false;
	}

	// program counter initially points at the program entry point
	initialPC = coff.getEntryPoint();	

	// next comes the stack; stack pointer initially points to top of it
	numPages += stackPages;
	initialSP = numPages*pageSize;

	// and finally reserve 1 page for arguments
	numPages++;

	if (!loadSections())
	    return false;

	// store arguments in last page
	int entryOffset = (numPages-1)*pageSize;
	int stringOffset = entryOffset + args.length*4;

	this.argc = args.length;
	this.argv = entryOffset;
	
	for (int i=0; i<argv.length; i++) {
	    byte[] stringOffsetBytes = Lib.bytesFromInt(stringOffset);
	    Lib.assertTrue(writeVirtualMemory(entryOffset,stringOffsetBytes) == 4);
	    entryOffset += 4;
	    Lib.assertTrue(writeVirtualMemory(stringOffset, argv[i]) ==
		       argv[i].length);
	    stringOffset += argv[i].length;
	    Lib.assertTrue(writeVirtualMemory(stringOffset,new byte[] { 0 }) == 1);
	    stringOffset += 1;
	}

	return true;
    }

    /**
     * Allocates memory for this process, and loads the COFF sections into
     * memory. If this returns successfully, the process will definitely be
     * run (this is the last step in process initialization that can fail).
     *
     * @return	<tt>true</tt> if the sections were successfully loaded.
     */
	protected boolean loadSections() {
	
		//If there isn't enough memory to load a new process
		if(numPages > Machine.processor().getNumPhysPages()) {
		    //Close the file, print to screen, and return false
		    coff.close();
		    Lib.debug(dbgProcess, "\tinsufficient physical memory");
		    return false;
		}

		//Try to get pages for the pageTable
		try{
			//Call the UserKernel.getPages method to allocate free pages
			pageTable = ((UserKernel)Kernel.kernel).getPages(numPages);
		}
		//Catch the exception if there aren't enough pages to satisfy the request
		catch(InsufficientFreePagesException e){
			//Close the file and return false
			coff.close();
			return false;
		}
	
		//Populate the pageTable's vpn with numbers from 0 to length-1
		for(int i = 0; i < pageTable.length; i++)
			pageTable[i].vpn = i;

		//Iterate through each section
		for (int s=0; s<coff.getNumSections(); s++) {
			//Get the section
			CoffSection section = coff.getSection(s);
	    
			Lib.debug(dbgProcess, "\tinitializing " 
			+ section.getName() + 
			" section (" + section.getLength() + " pages)");
			
			//Load the pages
			for (int i=0; i<section.getLength(); i++) {
				int vpn = section.getFirstVPN()+i;
				section.loadPage(i, pageTable[vpn].ppn);
			}
		}

		//Return true since the operation was successful
		return true;
	}

    /**
     * Release any resources allocated by <tt>loadSections()</tt>.
     */
    protected void unloadSections() {
    	((UserKernel)Kernel.kernel).releasePageTable(pageTable);
    }    

    /**
     * Initialize the processor's registers in preparation for running the
     * program loaded into this process. Set the PC register to point at the
     * start function, set the stack pointer register to point at the top of
     * the stack, set the A0 and A1 registers to argc and argv, respectively,
     * and initialize all other registers to 0.
     */
    public void initRegisters() {
	Processor processor = Machine.processor();

	// by default, everything's 0
	for (int i=0; i<processor.numUserRegisters; i++)
	    processor.writeRegister(i, 0);

	// initialize PC and SP according
	processor.writeRegister(Processor.regPC, initialPC);
	processor.writeRegister(Processor.regSP, initialSP);

	// initialize the first two argument registers to argc and argv
	processor.writeRegister(Processor.regA0, argc);
	processor.writeRegister(Processor.regA1, argv);
    }

	/**
     * This method ensures the validity of memory addresses. It does this
     * by making sure that addr is located within defined memory. If addr
     * is located on an invalid page handleExit is called with an exit code
     * of -1
     */
	public void addressChecker(int addr){
		int pageNum = Processor.pageFromAddress(addr);
		if(pageNum >= numPages || pageNum < 0)
			handleExit(-1);
	}

    /**
     * Handle the halt() system call. 
     */
    private int handleHalt() {
		if(processID == 0){
			Machine.halt();
			return 0;
		}
	
	return 1;
    }
    
    /**
     * This method is responsible for handling the creat system call. It
     * accomplishes this by calling the openFile method and passing true
     * as the isCreating boolean
     */
    private int handleCreate(int fileNamePtr){
    	return openFile(fileNamePtr, true);
    }
    
    /**
     * This method is responsible for handling the open system call. It
     * accomplishes this by calling the openFile method and passing false
     * as the isCreating boolean
     */
    private int handleOpen(int fileNamePtr){
    	return openFile(fileNamePtr, false);
    }
    
    /**
     * This method begins by ensuring that the address of the file name
     * is valid by running it through addressChecker(). Then the location
     * of the last null index and location of the file named fileName are
     * stored. If the file is found and not marked for death then its 
     * numReferences is incremented. An open space in the localFileArray
     * is found and the file is then opened with the isCreating flag at
     * that location in the process' localFileArray. If there are no errors
     * the method will return the index in the localFileArray where the file
     * was opened.
     */
    private int openFile(int fileNamePtr, boolean isCreating){
    	//Validate file name address and get the file name from that address
    	addressChecker(fileNamePtr);
    	String fileName = readVirtualMemoryString(fileNamePtr, 256);
    	//Set the indicies for the file and last null to -1
    	int globalFileIndex = -1;
    	int nullIndex = -1;
    	
    	//Iterate through the globalFileRefArray
    	for(int index = 2; index < globalFileRefArray.length; index++){
    		//If the space is empty, store that location
    		if(globalFileRefArray[index] == null){
    			nullIndex = index;
			}
			if(globalFileRefArray[index] != null){
				//If the space contains the file specified by fileName, store it
    			if(globalFileRefArray[index].fileName.equals(fileName)){
    				globalFileIndex = index;
				}
			}		
    	}
    	//If the file was found in the global array
    	if(globalFileIndex != -1){
    		//And is not marked for death
    		if(!globalFileRefArray[globalFileIndex].markedForDeath)
    			//Increment its references
    			globalFileRefArray[globalFileIndex].numReferences++;
    	
		}
       	else{
    		if(nullIndex == -1)
    			//If there is no space return -1
    			return -1;
    	}
    	//Set the index for a free space in the local file array to -1
    	int localFileIndex = -1;
    	//Find a free space in the local file array
    	for(int index = 0; index < localFileArray.length; index++)
    		if(localFileArray[index] == null)
    			localFileIndex = index;
    			
    	//If there is no free space in the local file array, return -1
    	if(localFileIndex == -1)
    		return -1;
    	
    	//Open the file in the localFileArray with the isCreating boolean
    	localFileArray[localFileIndex] = UserKernel.fileSystem.open(fileName, isCreating);
    	
		//If the file was opened successfully
		if(localFileArray[localFileIndex] != null){
			//And the file wasn't found
			if(globalFileIndex == -1)	
				//Create the file in the globalFileRefArray
    			globalFileRefArray[nullIndex] = new FileReference(fileName);
			//Finally return the index of the file in the local array
			return localFileIndex;
		}
		//If the file wasn't opened successfully return -1
		else
			return -1;
    }		
    
    /**
     * This method is responsible for handling the read system call. It starts by
     * validating the address of the buffer; and validating the fileIndex. Then it
     * creates a byte array to store the bytes that will be read. It reads the bytes
     * from the file and stores the amount of bytes read into bytesRead; it then writes
     * the data into the buffer pointed to by bufferPtr. This method returns the amount
     * of bytes successfully read.
     */
    public int handleRead(int fileIndex, int bufferPtr, int size){
    	//Validate the address and file index
    	addressChecker(bufferPtr);
    	if(fileIndex < 0 || fileIndex > 15 || localFileArray[fileIndex] == null)
    		return -1;
    	
    	//Create the temporary data storage
    	byte[] storage = new byte[size];
    
    	//Read the data from the file and store
    	int bytesRead = localFileArray[fileIndex].read(storage, 0, size);
       	if(bytesRead == -1)
    		return -1;
    	//Write from storage into the buffer pointed to by bufferPtr
    	int bytesWritten = writeVirtualMemory(bufferPtr, storage, 0, bytesRead);
    	if(bytesWritten != bytesRead)
    		return -1;
    		
    	//Return the amount of bytes read
    	return bytesRead;
    }
    
    /**
     * This method is responsible for handling the write system call. It operates
     * very similarly to handleOpen; but reads from the buffer and writes to the file
     * as opposed to handleRead which does the opposite. This method returns the amount
     * of bytes successfully written to file.
     */
    public int handleWrite(int fileIndex, int bufferPtr, int size){
    	//Validate the address and file index
    	addressChecker(bufferPtr);
    	if(fileIndex < 0 || fileIndex > 15 || localFileArray[fileIndex] == null)
    		return -1;
    		
    	//Create the temporary data storage
    	byte[] storage = new byte[size];
    	
    	//Read the data from the buffer and store
    	int bytesRead = readVirtualMemory(bufferPtr, storage, 0, size);
    	//Write from storage into the file
    	int bytesWritten = localFileArray[fileIndex].write(storage, 0, bytesRead);
    	
    	//Return the amount of bytes 
    	return bytesWritten;
    }
    
    /**
     * This method is responsible for handling the close system call. It accomplishes this by
     * calling the closeFile method and passing a false as the isUnlinking boolean.
     */
    public int handleClose(int fileIndex){
    	return closeFile(fileIndex, false);
    }
    
    /**
     * This method is responsible for handling the unlink system call. It accomplishes this by
     * calling the closeFile method and passing a true as the isUnlinking boolean.
     */
    public int handleUnlink(int fileNamePtr){
    	return closeFile(fileNamePtr, true);
    }
    
    /**
     * This method takes advantage of the similarities between closing and unlinking files.
     * The method starts by validating the file index and setting a variable to store the 
     * file name. If the method is simply closing then the name of the file is stored and
     * the file is closed and removed from the local file array. If the method is unlinking
     * the file name is stored and the file is found in the local array and closed and removed.
     * Once this process is done the file is found in the global array and marked for unlinking
     * if the isUnlinking boolean is set. The number of references to the file is decremented
     * The file is then checked for removal from the global array if it is set for unlinking
     * and the number of references to the file is 0. Finally the method returns 0 if everything
     * was successful.
     */
    public int closeFile(int fileIndex, boolean isUnlinking){	
    	//Validate the file index
    	if((fileIndex < 0 || fileIndex > 15 || localFileArray[fileIndex] == null) && !isUnlinking)
    		return -1;
    	
    	//Set a string to store the file name
		String fileName = null;
	
		//If the method was called from handleUnlink	
		if(!isUnlinking){
			//Store the file name, close the file, and remove it from the local file array
    		fileName = localFileArray[fileIndex].getName();
    		localFileArray[fileIndex].close();
    		localFileArray[fileIndex] = null;
		}
		//If the method was called from handleClose
		else{
			//Store the file name, close all instances of the file
    		fileName = readVirtualMemoryString(fileIndex, 256);
			for(int i = 0; i < localFileArray.length; ++i){
				if(localFileArray[i] != null){
					if(localFileArray[i].getName().equals(fileName)){
						localFileArray[i].close();
						//And remove them from the local file array
						localFileArray[i] = null;
					}
				}
			}
		}
    	
    	int globalFileIndex = -1;
    	//Iterate through the global file array and store the location of the file
    	for(int index = 0; index < globalFileRefArray.length; index++){
			if(globalFileRefArray[index] != null){
    			if(globalFileRefArray[index].fileName.equals(fileName))
    				globalFileIndex = index;
			}
		}
    	if(globalFileIndex == -1)
    		return -1;
    	
    	//If the method was called from handleUnlink
    	if(isUnlinking)
    		//Set the file to be unlinked when possible
    		globalFileRefArray[globalFileIndex].markedForDeath = true;
    	
    	//Decrement the number of references to the file
    	globalFileRefArray[globalFileIndex].numReferences--;
    	
    	//If the references are 0 and the file is marked to be unlinked
    	if(globalFileRefArray[globalFileIndex].numReferences == 0){
    		if(globalFileRefArray[globalFileIndex].markedForDeath){
    			if(!UserKernel.fileSystem.remove(fileName))
    				return -1; 
    			//Remove it from the global file array
    			globalFileRefArray[globalFileIndex] = null;
    		}
    	}
    	
    	//Return 0 to indicate a successful operation
    	return 0;
    }
    
    /**
     * This method is responsible for handling the exec system call. It accomplishes 
     * this by first validating the file name and argument counter. Next it looks at
     * the argvPtrs, at it gives them their appropriate sizes. Once this is done it 
     * goes through and makes newly sized arguments into Strings that can be read and
     * executed. Lastly the method creates a new child process by calling UserProcess.
     * The child knows who its parent is since it is assigned to the current process,
     * it then places createChild into the HashMap. After this is completed the method
     * returns the processID of the child
     */
    public int handleExec(int fileNamePtr, int argc, int argvPtr){
    	//Validate the file name and argument count
    	String fileName = readVirtualMemoryString(fileNamePtr, 256);
    	if(fileName == null || !fileName.endsWith(".coff") || argc < 0)
    		return -1;
    	
    	//Create an array to store the arguments
		int[] argPtrs = new int[argc];

		//Iterate through the arguments and store them in argPtrs
    	for(int i=0; i<argc; i++){
    		byte[] size = new byte[4];
    		readVirtualMemory(argvPtr + i * 4, size, 0, 4);
    		argPtrs[i] = Lib.bytesToInt(size, 0);
    	}
    	
    	//Convert the arguments into Strings that can be processed
    	String[] args = new String[argc];
    	
    	for(int i=0; i<argc; i++){
    		byte[] argument = new byte[256];
    		readVirtualMemory(argPtrs[i], argument, 0, 256);
    		args[i] = new String(argument);
    	}
    	
    	//Create a new process and give it its parent
    	UserProcess Child = new UserProcess();
    	Child.parent = this;
    	
    	//Store the child in this process' children array
    	children.put(Child.processID, new ChildProcess(Child));
    	
    	//Execute the child with the appropriate file name and arguments
    	Child.execute(fileName, args);
    	
    	//Return the child's processID
    	return Child.processID;
    }
    
    /**
     * First the method will grab the child from the current list of children; if the
     * child is not found it will retrun -1. Then it will call the joinProcess method.
     * After this it will remove the child from children since that process has returned
     * and it will write the appropriate data into virtual memory. This method returns 1
     * if the join is a success.
     */
    public int handleJoin(int processID, int statusPtr){
		//Create a childProcess object from an existing child UserProcess
    	ChildProcess child = children.get(processID);
    	if(child == null)
    		return -1;
    	
    	//If the process is not null join it via joinProcess
    	if(child.process != null)
    		child.process.joinProcess();
    	
    	//Remove the child in question from the list of active children
    	children.remove(processID);
    	
    	//Write the child's return value into the status pointer
    	writeVirtualMemory(statusPtr, Lib.bytesFromInt(child.returnValue));
    
    	//Return 1 on a successful operation	
    	return 1;
    }
    
    /**
     * This method acquires the joinLock then checks the exit boolean, initially set to
     * false, and while it's false it puts the current thread to sleep on the Condition
     * variable's waitQueue, then it releases the lock. This ensures that the processes
     * stay synchronized and then wake up when handleExit is called.
     */
    public void joinProcess(){
    	//Acquire the lock
    	joinLock.acquire();
		
		//While the hasExited variable has not been changed
    	while(!hasExited)
    		//Put the processes waiting to join to sleep
    		waitingToJoin.sleep();
    	//Release the lock
    	joinLock.release();
    }
    
    /**
     * This method is responsible for handling the exit syscall. It accomplishes this
     * by first acquiring the joinLock. If the process has a parent, it is notified that
     * the child is going to exit, so it will not attempt to join later on. It will then
     * go through each child in the HashMap, and disown it by making the parent null.
     * It will then set the HashMap to null as well, since it is no longer being used.
     * Next it loops through all the open files and closes them, releasing their references
     * by calling the handleClose method. It will also go through the waitingToJoin 
     * Condition, waking up any processes that were asleep. Once this is complete the
     * unloadSections is used to release the virtual memory, and release the lock.
     */
    public int handleExit(int status){
    	//Acquire the joinLock
    	joinLock.acquire();
    	
    	//If the process has a parent, notify it that this process is exiting
    	if(parent != null){
    		(parent.children.get(processID)).returnValue = status;
    		parent.children.get(processID).process = null;
    	}
    	
    	//Iterate through this process' children and notify them they have no parent
    	for(int i=0; i<children.size(); ++i)
    		if(children.get(i).process != null)
    			children.get(i).process.parent = null;
    			
    	//Set the children array to null
    	children = null;
    	
    	//Close all open files
    	for(int i=2; i<localFileArray.length; ++i)
    		handleClose(i);
    		
    	//Wake all sleeping processes and set hasExited to true
    	hasExited = true;
    	waitingToJoin.wakeAll();
    	
    	unloadSections();
    	
    	//Release the lock	
    	joinLock.release();
    	
    	//Halt the system if this is processID 0
    	handleHalt();
    	
    	//Finish the current thread and return 0 for a successful operation
    	KThread.finish();
    	return 0;
    }


    private static final int
        syscallHalt = 0,
	syscallExit = 1,
	syscallExec = 2,
	syscallJoin = 3,
	syscallCreate = 4,
	syscallOpen = 5,
	syscallRead = 6,
	syscallWrite = 7,
	syscallClose = 8,
	syscallUnlink = 9;

    /**
     * Handle a syscall exception. Called by <tt>handleException()</tt>. The
     * <i>syscall</i> argument identifies which syscall the user executed:
     *
     * <table>
     * <tr><td>syscall#</td><td>syscall prototype</td></tr>
     * <tr><td>0</td><td><tt>void halt();</tt></td></tr>
     * <tr><td>1</td><td><tt>void exit(int status);</tt></td></tr>
     * <tr><td>2</td><td><tt>int  exec(char *name, int argc, char **argv);
     * 								</tt></td></tr>
     * <tr><td>3</td><td><tt>int  join(int pid, int *status);</tt></td></tr>
     * <tr><td>4</td><td><tt>int  creat(char *name);</tt></td></tr>
     * <tr><td>5</td><td><tt>int  open(char *name);</tt></td></tr>
     * <tr><td>6</td><td><tt>int  read(int fd, char *buffer, int size);
     *								</tt></td></tr>
     * <tr><td>7</td><td><tt>int  write(int fd, char *buffer, int size);
     *								</tt></td></tr>
     * <tr><td>8</td><td><tt>int  close(int fd);</tt></td></tr>
     * <tr><td>9</td><td><tt>int  unlink(char *name);</tt></td></tr>
     * </table>
     * 
     * @param	syscall	the syscall number.
     * @param	a0	the first syscall argument.
     * @param	a1	the second syscall argument.
     * @param	a2	the third syscall argument.
     * @param	a3	the fourth syscall argument.
     * @return	the value to be returned to the user.
     */
    public int handleSyscall(int syscall, int a0, int a1, int a2, int a3) {
		switch (syscall) {
			case syscallHalt:
			    return handleHalt();
			case syscallCreate:
				return handleCreate(a0);
			case syscallOpen:
				return handleOpen(a0);
			case syscallRead:
				return handleRead(a0, a1, a2); 
			case syscallWrite:
				return handleWrite(a0, a1, a2);
			case syscallClose:
				return handleClose(a0);
			case syscallUnlink:
				return handleUnlink(a0);
			case syscallExec:
				return handleExec(a0, a1, a2);
			case syscallJoin:
				return handleJoin(a0, a1);
			case syscallExit:
				return handleExit(a0);

	default:
	    Lib.debug(dbgProcess, "Unknown syscall " + syscall);
	    Lib.assertNotReached("Unknown system call!");
	}
	return 0;
    }

    /**
     * Handle a user exception. Called by
     * <tt>UserKernel.exceptionHandler()</tt>. The
     * <i>cause</i> argument identifies which exception occurred; see the
     * <tt>Processor.exceptionZZZ</tt> constants.
     *
     * @param	cause	the user exception that occurred.
     */
    public void handleException(int cause) {
		Processor processor = Machine.processor();

		switch (cause) {
			case Processor.exceptionSyscall:
	    		int result = handleSyscall(processor.readRegister(Processor.regV0),
				       processor.readRegister(Processor.regA0),
				       processor.readRegister(Processor.regA1),
				       processor.readRegister(Processor.regA2),
				       processor.readRegister(Processor.regA3)
				       );
	    		processor.writeRegister(Processor.regV0, result);
	    		processor.advancePC();
	    		break;				       
				       
			default:
	    		Lib.debug(dbgProcess, "Unexpected exception: " +
						Processor.exceptionNames[cause]);
	    		Lib.assertNotReached("Unexpected exception");
		}
    }

	public void selfTest(){

		byte[] data = {'S','U','C','C','E','S','S'};
		byte[] buffer = new byte[7];
		
		//Write to memory, then read the same section
		//What was read should be what was written
		int bytesWritten = writeVirtualMemory(0, data, 0, 7);
		int bytesRead = readVirtualMemory(0,buffer,0,7);

		String msg = new String(buffer);
		System.out.println("Read Write Test: " + msg);

		//Write more than a pages worth of bytes to memory
		byte[] overFlow = new byte[pageSize + 4];

		for(int i = 0; i < pageSize; ++i)
			overFlow[i] = (byte)(i%255);

		overFlow[pageSize] = 'G';
		overFlow[pageSize+1] = 'O';
		overFlow[pageSize+2] = 'O';
		overFlow[pageSize+3] = 'D';

		bytesWritten = writeVirtualMemory(0, overFlow,0, overFlow.length);

		System.out.println("Bytes Written: " + bytesWritten);
		System.out.println("Write OverFlow Test: GOOD");

		for(int i = 0; i < overFlow.length; ++i)
			overFlow[i] = 0;

		//Read more than a pages worth of bytes from memory
		bytesRead = readVirtualMemory(0,overFlow,0,overFlow.length);

		byte[] last4 = new byte[4];
		last4[0] = overFlow[pageSize];
		last4[1] = overFlow[pageSize+1];
		last4[2] = overFlow[pageSize+2];
		last4[3] = overFlow[pageSize+3];
		
		System.out.println("Bytes Read: " + bytesRead);
		System.out.println("Read OverFlow Test: " + new String(last4));

		for(int i = 0; i < last4.length; ++i)
			last4[i] = 0;

		//Read the first 4 bytes of vpn 1, should read GOOD		
		bytesRead = readVirtualMemory(pageSize, last4, 0, last4.length);
		System.out.println("OverFlow Test: " + new String(last4));
	}
	
    /** The program being run by this process. */
    protected Coff coff;

    /** This process's page table. */
    protected TranslationEntry[] pageTable;
    /** The number of contiguous pages occupied by the program. */
    protected int numPages;

    /** The number of pages in the program's stack. */
    protected final int stackPages = 8;
    
    private int initialPC, initialSP;
    private int argc, argv;
	
    private static final int pageSize = Processor.pageSize;
    private static final char dbgProcess = 'a';
    
    //Task 1 variables
    private static int numProcesses = 0;
    public int processID;
    
    OpenFile[] localFileArray;
    static FileReference[] globalFileRefArray;
    
    //Task 2 variables
    Lock memoryLock;

	//Task 3 variables
	UserProcess parent;
	boolean hasExited;
	Lock joinLock;
	Condition waitingToJoin;
	HashMap<Integer, ChildProcess> children;
}
