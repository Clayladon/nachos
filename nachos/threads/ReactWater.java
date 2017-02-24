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
    
    public static void selfTest(){
    	Lib.debug(ReactWaterTestChar, "ReactWater.selfTest(): Starting self test.");
    	
    	manyHydrogenTest();
    	manyOxygenTest();
    	hydrogenOxygenTest();
    	oxygenHydrogenTest();
    	
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
    	h1.fork();
    
		KThread h2 = new KThread();
    	h2.setName("hydrogen 2");
    	h2.setTarget(new Runnable() {
    		public void run(){
    			Lib.debug(ReactWaterTestChar, "ReactWater.manyHydrogenTest(): hydrogen ready.");
    			manyHydrogenObj.hReady();
    		}
    	});
    	h2.fork();
    
		KThread h3 = new KThread();
    	h3.setName("hydrogen 3");
    	h3.setTarget(new Runnable() {
    		public void run(){
    			Lib.debug(ReactWaterTestChar, "ReactWater.manyHydrogenTest(): hydrogen ready.");
    			manyHydrogenObj.hReady();
    		}
    	});
    	h3.fork();
    
		KThread h4 = new KThread();
    	h4.setName("hydrogen 4");
    	h4.setTarget(new Runnable() {
    		public void run(){
    			Lib.debug(ReactWaterTestChar, "ReactWater.manyHydrogenTest(): hydrogen ready.");
    			manyHydrogenObj.hReady();
    		}
    	});
    	h4.fork();
    
		KThread o1 = new KThread();
    	o1.setName("oxygen 1");
    	o1.setTarget(new Runnable() {
    		public void run(){
    			Lib.debug(ReactWaterTestChar, "ReactWater.manyHydrogenTest(): oxygen ready.");
    			manyHydrogenObj.oReady();
    		}
    	});
    	o1.fork();
    
		KThread o2 = new KThread();
    	o2.setName("oxygen 2");
    	o2.setTarget(new Runnable() {
    		public void run(){
    			Lib.debug(ReactWaterTestChar, "ReactWater.manyHydrogenTest(): oxygen ready.");
    			manyHydrogenObj.oReady();
    		}
    	});
    	o2.fork();
		
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
    	o1.fork();
    
		KThread o2 = new KThread();
    	o2.setName("oxygen 2");
    	o2.setTarget(new Runnable() {
    		public void run(){
    			Lib.debug(ReactWaterTestChar, "ReactWater.manyOxygenTest(): oxygen ready.");
    			manyOxygenObj.oReady();
    		}
    	});
    	o2.fork();
    
		KThread h1 = new KThread();
    	h1.setName("hydrogen 1");
    	h1.setTarget(new Runnable() {
    		public void run(){
    			Lib.debug(ReactWaterTestChar, "ReactWater.manyOxygenTest(): hydrogen ready.");
    			manyOxygenObj.hReady();
    		}
    	});
    	h1.fork();
    
		KThread h2 = new KThread();
    	h2.setName("hydrogen 2");
    	h2.setTarget(new Runnable() {
    		public void run(){
    			Lib.debug(ReactWaterTestChar, "ReactWater.manyOxygenTest(): hydrogen ready.");
    			manyOxygenObj.hReady();
    		}
    	});
    	h2.fork();
    
		KThread h3 = new KThread();
    	h3.setName("hydrogen 3");
    	h3.setTarget(new Runnable() {
    		public void run(){
    			Lib.debug(ReactWaterTestChar, "ReactWater.manyOxygenTest(): hydrogen ready.");
    			manyOxygenObj.hReady();
    		}
    	});
    	h3.fork();
    
		KThread h4 = new KThread();
    	h4.setName("hydrogen 4");
    	h4.setTarget(new Runnable() {
    		public void run(){
    			Lib.debug(ReactWaterTestChar, "ReactWater.manyOxygenTest(): hydrogen ready.");
    			manyOxygenObj.hReady();
    		}
    	});
    	h4.fork();
		
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
    	h1.fork();
		
		KThread o1 = new KThread();
    	o1.setName("oxygen 1");
    	o1.setTarget(new Runnable() {
    		public void run(){
    			Lib.debug(ReactWaterTestChar, "ReactWater.hydrogenOxygenTest(): oxygen ready.");
    			h_oTest.oReady();
    		}
    	});
    	o1.fork();
		
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
    	o1.fork();
		
		KThread h1 = new KThread();
    	h1.setName("hydrogen 1");
    	h1.setTarget(new Runnable() {
    		public void run(){
    			Lib.debug(ReactWaterTestChar, "ReactWater.oxygenHydrogenTest(): hydrogen ready.");
    			o_hTest.hReady();
    		}
    	});
    	h1.fork();
		
    	Lib.debug(ReactWaterTestChar, "ReactWater.oxygenHydrogenTest(): Finished oxygen then hydrogen test, passed.");
	}
	
    private static final char ReactWaterTestChar = 'r';
} // end of class ReactWater


