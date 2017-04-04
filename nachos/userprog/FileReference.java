package nachos.userprog;

/**
 * This class represents a reference to a file. The purpose is to keep track of each
 * process' opened giles as well as maintain a global view of all opened files and their
 * references.
 */
public class FileReference{

	//The number of references associated to a file
	public int numReferences;

	//Whether or not a file is set to be unlinked
	public boolean markedForDeath;

	//The name of the file
	public String fileName;
	
	//Constructor
	public FileReference(String inFileName){
		//Since the FileReference is being created 1 reference must exist
		numReferences = 1;
		
		//The file is not marked for unlinking on creation
		markedForDeath = false;
		
		fileName = inFileName;
	}
}
