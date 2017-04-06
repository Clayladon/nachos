package nachos.userprog;

/**
 * This class ensures that the child process can keep track of the children 
 * and their exit values. To this it identifies the child's status (returnValue) 
 * and the process it is running.
 */
public class ChildProcess{
	//Datafields
	public int returnValue;
	public UserProcess process;
	
	//Constructor
	public ChildProcess(UserProcess child){
		process = child;
		returnValue = -1;
	}
}
