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
    	
    	//manyHydrogenTest();
    	//manyOxygenTest();
    	//hydrogenOxygenTest();
    	//oxygenHydrogenTest();
    }
    
    public static void manyHydrogenTest(){
    
    	ReactWater manyHydrogenObj = new ReactWater();
    
		KThread h1 = new KThread();
    	h1.setName("hydrogen 1");
    	h1.setTarget(new Runnable() {
    		public void run(){
    			manyHydrogenObj.hReady();
    		}
    	});
    	h1.fork();
    
		KThread h2 = new KThread();
    	h2.setName("hydrogen 2");
    	h2.setTarget(new Runnable() {
    		public void run(){
    			manyHydrogenObj.hReady();
    		}
    	});
    	h2.fork();
    
		KThread h3 = new KThread();
    	h3.setName("hydrogen 3");
    	h3.setTarget(new Runnable() {
    		public void run(){
    			manyHydrogenObj.hReady();
    		}
    	});
    	h3.fork();
    
		KThread h4 = new KThread();
    	h4.setName("hydrogen 4");
    	h4.setTarget(new Runnable() {
    		public void run(){
    			manyHydrogenObj.hReady();
    		}
    	});
    	h4.fork();
    
		KThread o1 = new KThread();
    	o1.setName("oxygen 1");
    	o1.setTarget(new Runnable() {
    		public void run(){
    			manyHydrogenObj.oReady();
    		}
    	});
    	o1.fork();
    
		KThread o2 = new KThread();
    	o2.setName("oxygen 2");
    	o2.setTarget(new Runnable() {
    		public void run(){
    			manyHydrogenObj.oReady();
    		}
    	});
    	o2.fork();
		
		System.out.println("Multiple hydrogen test succeeded!");
	}
	
	public static void manyOxygenTest(){
    
    	ReactWater manyOxygenObj = new ReactWater();
    
		KThread o1 = new KThread();
    	o1.setName("oxygen 1");
    	o1.setTarget(new Runnable() {
    		public void run(){
    			manyOxygenObj.oReady();
    		}
    	});
    	o1.fork();
    
		KThread o2 = new KThread();
    	o2.setName("oxygen 2");
    	o2.setTarget(new Runnable() {
    		public void run(){
    			manyOxygenObj.oReady();
    		}
    	});
    	o2.fork();
    
		KThread h1 = new KThread();
    	h1.setName("hydrogen 1");
    	h1.setTarget(new Runnable() {
    		public void run(){
    			manyOxygenObj.hReady();
    		}
    	});
    	h1.fork();
    
		KThread h2 = new KThread();
    	h2.setName("hydrogen 2");
    	h2.setTarget(new Runnable() {
    		public void run(){
    			manyOxygenObj.hReady();
    		}
    	});
    	h2.fork();
    
		KThread h3 = new KThread();
    	h3.setName("hydrogen 3");
    	h3.setTarget(new Runnable() {
    		public void run(){
    			manyOxygenObj.hReady();
    		}
    	});
    	h3.fork();
    
		KThread h4 = new KThread();
    	h4.setName("hydrogen 4");
    	h4.setTarget(new Runnable() {
    		public void run(){
    			manyOxygenObj.hReady();
    		}
    	});
    	h4.fork();
		
		System.out.println("Multiple oxygen test succeeded!");
	}
	
	public static void hydrogenOxygenTest(){
		ReactWater h_oTest = new ReactWater();
		
		KThread h1 = new KThread();
    	h1.setName("hydrogen 1");
    	h1.setTarget(new Runnable() {
    		public void run(){
    			h_oTest.hReady();
    		}
    	});
    	h1.fork();
		
		KThread o1 = new KThread();
    	o1.setName("oxygen 1");
    	o1.setTarget(new Runnable() {
    		public void run(){
    			h_oTest.oReady();
    		}
    	});
    	o1.fork();
		
		System.out.println("hydrogenOxygenTest() succeeded!");
	}
	
	public static void oxygenHydrogenTest(){
		ReactWater o_hTest = new ReactWater();
		
		KThread o1 = new KThread();
    	o1.setName("oxygen 1");
    	o1.setTarget(new Runnable() {
    		public void run(){
    			o_hTest.oReady();
    		}
    	});
    	o1.fork();
		
		KThread h1 = new KThread();
    	h1.setName("hydrogen 1");
    	h1.setTarget(new Runnable() {
    		public void run(){
    			o_hTest.hReady();
    		}
    	});
    	h1.fork();
		
		System.out.println("oxygenHydrogenTest() succeeded!");
	}
} // end of class ReactWater


