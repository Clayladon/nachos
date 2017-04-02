package nachos.userprog;

public class FileReference{
	public int numReferences;
	public boolean markedForDeath;
	public String fileName;
	
	public FileReference(String inFileName){
		numReferences = 1;
		markedForDeath = false;
		fileName = inFileName;
	}
}
