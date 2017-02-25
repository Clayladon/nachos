package nachos.threads;

import nachos.machine.*;

public class ReactWater{
	//Datafield declaration
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
    	//Initialize datafields
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
    	//Acquire the lock and increment the number of readied hydrogen atoms
    	lock.acquire();
    	numHydrogen++;
    	
    	//While there are not enough atoms to make water
    	while(numHydrogen < 2 || numOxygen < 1)
    		//If the hasMadeWater flag is set
    		if(hasMadeWater){
    			//set the flag to false, and realease lock, then immediately return
    			hasMadeWater = false;
    			lock.release();
    			return;
    		}
    		//Otherwire put hydrogen to sleep
    		else
    			hydrogen.sleep();
    	
    	//If there are enough atoms to make water, call the makeWater() method
    	makeWater();
    	//Wake the oxygen used to make water
    	oxygen.wake();
    	//And release the lock
    	lock.release();
    } // end of hReady()
 
    /** 
     *   When O element comes, if there already exist another two H
     *   elements, then call the method of Makewater(). Or let O element
     *   wait in line. 
     **/ 
    public void oReady() {
    	//Acquire the lock and increment the number of readied oxygen atoms
		lock.acquire();
		numOxygen++;
		
		//Wake a hydrogen atom to set off a check for the number of readied atoms
		hydrogen.wake();
		//Then put this oxygen atom to sleep to wait on a call to make water
		oxygen.sleep();
		
		//Once water has been made this oxygen atom will wake the second hydrogen
		//atom used in the transaction so it will finish
		hydrogen.wake();
		
		//release the lock
		lock.release();
    } // end of oReady()
    
    /** 
     *   Print out the message of "Water was made!".
     **/
    public void makeWater() {
    	//Reduce the number of hydrogen and oxygen atoms available
		numHydrogen = numHydrogen - 2;
		numOxygen--;
		//Set the hasMadeWater flag for use in the hydrogen while loop
		hasMadeWater = true;
		//Print the "Water was made!" message
		System.out.println("Water was made!");
    } // end of Makewater()
    
    public static void selfTest(){
    	Lib.debug(ReactWaterTestChar, "ReactWater.selfTest(): Starting self test.");
    	
    	manyHydrogenTest();
    	manyOxygenTest();
    	//hydrogenOxygenTest();
    	//oxygenHydrogenTest();
    	
    	Lib.debug(ReactWaterTestChar, "ReactWater.selfTest(): Finished selfTest(), passed.");
    }
    
    public static void manyHydrogenTest(){
    
    	Lib.debug(ReactWaterTestChar, "ReactWater.manyHydrogenTest(): Starting multiple hydrogen test.");
    	ReactWater manyHydrogenObj = new ReactWater();
    
		KThread h1 = new KThread();
    	h1.setName("hydrogen 1");
    	h1.setTarget(new Runnable() {
    		public void run(){
    			Lib.debug(ReactWaterTestChar, "ReactWater.manyHydrogenTest(): hydrogen ready.");
    			manyHydrogenObj.hReady();
    		}
    	});
    
		KThread h2 = new KThread();
    	h2.setName("hydrogen 2");
    	h2.setTarget(new Runnable() {
    		public void run(){
    			Lib.debug(ReactWaterTestChar, "ReactWater.manyHydrogenTest(): hydrogen ready.");
    			manyHydrogenObj.hReady();
    		}
    	});
    
		KThread h3 = new KThread();
    	h3.setName("hydrogen 3");
    	h3.setTarget(new Runnable() {
    		public void run(){
    			Lib.debug(ReactWaterTestChar, "ReactWater.manyHydrogenTest(): hydrogen ready.");
    			manyHydrogenObj.hReady();
    		}
    	});
    
		KThread h4 = new KThread();
    	h4.setName("hydrogen 4");
    	h4.setTarget(new Runnable() {
    		public void run(){
    			Lib.debug(ReactWaterTestChar, "ReactWater.manyHydrogenTest(): hydrogen ready.");
    			manyHydrogenObj.hReady();
    		}
    	});
    
		KThread o1 = new KThread();
    	o1.setName("oxygen 1");
    	o1.setTarget(new Runnable() {
    		public void run(){
    			Lib.debug(ReactWaterTestChar, "ReactWater.manyHydrogenTest(): oxygen ready.");
    			manyHydrogenObj.oReady();
    		}
    	});
    
		KThread o2 = new KThread();
    	o2.setName("oxygen 2");
    	o2.setTarget(new Runnable() {
    		public void run(){
    			Lib.debug(ReactWaterTestChar, "ReactWater.manyHydrogenTest(): oxygen ready.");
    			manyHydrogenObj.oReady();
    		}
    	});
    	
    	h1.fork();
    	h2.fork();
    	h3.fork();
    	h4.fork();
    	o1.fork();
    	o2.fork();
    	
    	h1.join();
    	h2.join();
    	h3.join();
    	//h4.join();
    	o1.join();
    	o2.join();
		
    	Lib.debug(ReactWaterTestChar, "ReactWater.manyHydrogenTest(): Finished multiple hydrogen test, passed.");
	}
	
	public static void manyOxygenTest(){
    
    	Lib.debug(ReactWaterTestChar, "ReactWater.manyOxygenTest(): Starting multiple oxygen test.");
    	ReactWater manyOxygenObj = new ReactWater();
    
		KThread o1 = new KThread();
    	o1.setName("oxygen 1");
    	o1.setTarget(new Runnable() {
    		public void run(){
    			Lib.debug(ReactWaterTestChar, "ReactWater.manyOxygenTest(): oxygen ready.");
    			manyOxygenObj.oReady();
    		}
    	});
    
		KThread o2 = new KThread();
    	o2.setName("oxygen 2");
    	o2.setTarget(new Runnable() {
    		public void run(){
    			Lib.debug(ReactWaterTestChar, "ReactWater.manyOxygenTest(): oxygen ready.");
    			manyOxygenObj.oReady();
    		}
    	});
    
		KThread h1 = new KThread();
    	h1.setName("hydrogen 1");
    	h1.setTarget(new Runnable() {
    		public void run(){
    			Lib.debug(ReactWaterTestChar, "ReactWater.manyOxygenTest(): hydrogen ready.");
    			manyOxygenObj.hReady();
    		}
    	});
    
		KThread h2 = new KThread();
    	h2.setName("hydrogen 2");
    	h2.setTarget(new Runnable() {
    		public void run(){
    			Lib.debug(ReactWaterTestChar, "ReactWater.manyOxygenTest(): hydrogen ready.");
    			manyOxygenObj.hReady();
    		}
    	});
    
		KThread h3 = new KThread();
    	h3.setName("hydrogen 3");
    	h3.setTarget(new Runnable() {
    		public void run(){
    			Lib.debug(ReactWaterTestChar, "ReactWater.manyOxygenTest(): hydrogen ready.");
    			manyOxygenObj.hReady();
    		}
    	});
    
		KThread h4 = new KThread();
    	h4.setName("hydrogen 4");
    	h4.setTarget(new Runnable() {
    		public void run(){
    			Lib.debug(ReactWaterTestChar, "ReactWater.manyOxygenTest(): hydrogen ready.");
    			manyOxygenObj.hReady();
    		}
    	});
    	
    	o1.fork();
    	o2.fork();
    	h1.fork();
    	h2.fork();
    	h3.fork();
    	h4.fork();
    	
    	o1.join();
    	o2.join();
    	h1.join();
    	h2.join();
    	h3.join();
    	h4.join();
		
    	Lib.debug(ReactWaterTestChar, "ReactWater.manyOxygenTest(): Finished multiple oxygen test, passed.");
	}
	
	public static void hydrogenOxygenTest(){
    	
    	Lib.debug(ReactWaterTestChar, "ReactWater.oxygenHydrogenTest(): Starting hydrogen then oxygen test.");
		ReactWater h_oTest = new ReactWater();
		
		KThread h1 = new KThread();
    	h1.setName("hydrogen 1");
    	h1.setTarget(new Runnable() {
    		public void run(){
    			Lib.debug(ReactWaterTestChar, "ReactWater.hydrogenOxygenTest(): hydrogen ready.");
    			h_oTest.hReady();
    		}
    	});
		
		KThread o1 = new KThread();
    	o1.setName("oxygen 1");
    	o1.setTarget(new Runnable() {
    		public void run(){
    			Lib.debug(ReactWaterTestChar, "ReactWater.hydrogenOxygenTest(): oxygen ready.");
    			h_oTest.oReady();
    		}
    	});
    	
    	h1.fork();
    	o1.fork();
    	h1.join();
		
    	Lib.debug(ReactWaterTestChar, "ReactWater.hydrogenOxygenTest(): Finished hydrogen then oxygen test, passed.");
	}
	
	public static void oxygenHydrogenTest(){
	
    	Lib.debug(ReactWaterTestChar, "ReactWater.hydrogenOxygenTest(): Starting oxygen then hydrogen test.");
		ReactWater o_hTest = new ReactWater();
		
		KThread o1 = new KThread();
    	o1.setName("oxygen 1");
    	o1.setTarget(new Runnable() {
    		public void run(){
    			Lib.debug(ReactWaterTestChar, "ReactWater.oxygenHydrogenTest(): oxygen ready.");
    			o_hTest.oReady();
    		}
    	});
		
		KThread h1 = new KThread();
    	h1.setName("hydrogen 1");
    	h1.setTarget(new Runnable() {
    		public void run(){
    			Lib.debug(ReactWaterTestChar, "ReactWater.oxygenHydrogenTest(): hydrogen ready.");
    			o_hTest.hReady();
    		}
    	});
    	
    	o1.fork();
    	h1.fork();
    	o1.join();
		
    	Lib.debug(ReactWaterTestChar, "ReactWater.oxygenHydrogenTest(): Finished oxygen then hydrogen test, passed.");
	}
	
    private static final char ReactWaterTestChar = 'r';
} // end of class ReactWater


