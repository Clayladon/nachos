package nachos.userprog;

public class ChildProcess{
	public int returnValue;
	public UserProcess process;
	
	public ChildProcess(UserProcess child){
		process = child;
		returnValue = -1;
	}
}
