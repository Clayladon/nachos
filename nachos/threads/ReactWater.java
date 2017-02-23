package nachos.threads;

import nachos.machine.*;

public class ReactWater{
	Lock lock;
	Condition2 hydrogen;
	Condition2 oxygen;
	int numHydrogen;
	int numOxygen;
	boolean hasMadeWater;

    /** 
     *   Constructor of ReactWater
     **/
    public ReactWater() {
    	lock = new Lock();
		hydrogen = new Condition2(lock);
		oxygen = new Condition2(lock);
		numHydrogen = 0;
		numOxygen = 0;
		hasMadeWater = false;
    } // end of ReactWater()

    /** 
     *   When H element comes, if there already exist another H element 
     *   and an O element, then call the method of Makewater(). Or let 
     *   H element wait in line. 
     **/ 
    public void hReady() {
    	lock.acquire();
    	numHydrogen++;
    	
    	while(numHydrogen < 2 || numOxygen < 1)
    		if(hasMadeWater){
    			hasMadeWater = false;
    			lock.release();
    			return;
    		}
    		else
    			hydrogen.sleep();
    	
    	makeWater();
    	oxygen.wake();
    	lock.release();
    } // end of hReady()
 
    /** 
     *   When O element comes, if there already exist another two H
     *   elements, then call the method of Makewater(). Or let O element
     *   wait in line. 
     **/ 
    public void oReady() {
		lock.acquire();
		numOxygen++;
		
		hydrogen.wake();
		oxygen.sleep();
		
		hydrogen.wake();
		lock.release();
    } // end of oReady()
    
    /** 
     *   Print out the message of "Water was made!".
     **/
    public void makeWater() {
		numHydrogen = numHydrogen - 2;
		numOxygen--;
		hasMadeWater = true;
		System.out.println("Water was made!");
    } // end of Makewater()

} // end of class ReactWater


