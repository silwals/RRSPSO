package com.miamioh.ridesharing.app.utilities.helper;

import java.util.ArrayList;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import com.miamioh.ridesharing.app.entity.Event;
import com.miamioh.ridesharing.app.entity.Taxi;

public class PSO {
	private static final int PARTICLE_COUNT = 10;
	private static final int V_MAX = 4; // Maximum velocity change allowed.
										// Range: 0 >= V_MAX < EVENT_COUNT

	private static final int MAX_EPOCHS = 100;

	private static ArrayList<Particle> particles = new ArrayList<Particle>();
	
	//private static ArrayList<eEvent> map = new ArrayList<eEvent>();
	private static ArrayList<Event> eventMap = new ArrayList<Event>();
	private static Map<Event, Event> dropToPickupMapCopy;
	private static int EVENT_COUNT;
	public PSO(int size) {
		System.out.println(" Inside PSO constructor");
	    EVENT_COUNT=size;		
	}

	void initializeMap(Set<Event> events, Map<Event, Event> dropToPickupMap)
	{
	System.out.println(" Inside PSO initilaize Map");
	dropToPickupMapCopy = dropToPickupMap;
			for(Event e:events){
				eventMap.add(e); // saving the events to use it later
			/*	eEvent location = new eEvent();
				location.x(e.getLatitude());
				location.y(e.getLongitude());
				//add if its pickup or drop
				location.setPickup(e.isPickup());
			
		        map.add(location);*/
			}
	    return;
	}
	 static void PSOAlgorithm(Taxi taxi, Set<Event> events)
	{
		System.out.println(" Inside PSOAlgorithm");
		Particle aParticle = null;
		int epoch = 0;
		boolean done = false;
		
		initialize();
		while(!done)
	    {
	        // Two conditions can end this loop:
	        //    if the maximum number of epochs allowed has been reached, or,
	        //    if the Target value has been found.
	        if(epoch < MAX_EPOCHS){

	            for(int i = 0; i < PARTICLE_COUNT; i++)
	            {
	                aParticle = particles.get(i);
	                System.out.print("Particle: ");
	                for(int j = 0; j < EVENT_COUNT; j++)
	                {
	                    System.out.print(aParticle.data(j) + ", ");
	                } // j

	                getTotalDistance(i);
	                System.out.print("Distance: " + aParticle.pBest() + "\n");
	            
	            } 
	            
	            bubbleSort(); // sort particles by their pBest scores, best to worst.
	            
	            getVelocity();

	            updateparticles();
	            
	            System.out.println("epoch number: " + epoch);

	            epoch++;

	        }else{
	            done = true;
	        }
		}
	    return;
	}
	
	private static void initialize()
	{
		System.out.println(" Inside initialize");
		for(int i = 0; i < PARTICLE_COUNT; i++)
	    {
	        Particle newParticle = new Particle();
	        for(int j = 0; j < EVENT_COUNT; j++)
	        {
	            newParticle.data(j, j);
	        } // j
	        particles.add(newParticle);
	        for(int j = 0; j < 10; j++)
	        {
	        	randomlyArrange(particles.indexOf(newParticle));
	        }
	        getTotalDistance(particles.indexOf(newParticle));
	    } 
	    return;
	}
	private static void randomlyArrange(final int index)
	{
		int cityA = new Random().nextInt(EVENT_COUNT);
		System.out.println("cityA" +cityA);
		int cityB = 0;
		boolean done = false;
		while(!done)
		{
			cityB = new Random().nextInt(EVENT_COUNT);
			if(cityB != cityA){
				done = true;
			}
		}
		
		
		int temp = particles.get(index).data(cityA);
		particles.get(index).data(cityA, particles.get(index).data(cityB));
		particles.get(index).data(cityB, temp);
		
		// Make sure the drop event does not occur before the pick event	
		Particle particle=particles.get(index);
		int[] tempMdata=particle.getMData();
		
		for(int i=0;i<tempMdata.length;i++) {
			Event e1=eventMap.get(tempMdata[i]);
			if(e1.isPickup()) {
			       for(int j=i+1;j<tempMdata.length;j++) {
                    
			    	   Event e2=eventMap.get(tempMdata[j]);
			    	   if(!e2.isPickup() && ) {
			    		   
			    	   }
			       }
			}
		}
		
	/*	for(Integer ind:particle.getMData()) {
			Event event=eventMap.get(ind);
			if(event.isPickup()) {
				dropToPickupMapCopy
			}
		}*/
		
		
		
		
		
		return;
	}
	//Fitness Function
	private static void getTotalDistance(final int index)
	{
		Particle thisParticle = null;
	    thisParticle = particles.get(index);
	    thisParticle.pBest(0.0);
	    
	    for(int i = 0; i < EVENT_COUNT; i++)
	    {
	        if(i == EVENT_COUNT - 1){
	        	thisParticle.pBest(thisParticle.pBest() + getDistance(thisParticle.data(EVENT_COUNT - 1), thisParticle.data(0))); // Complete trip.
	        }else{
	        	thisParticle.pBest(thisParticle.pBest() + getDistance(thisParticle.data(i), thisParticle.data(i + 1)));
	        }
	    }
	    return;
	}
	//Fetch this distance from HEREAPI
	private static double getDistance(final int firstCity, final int secondCity)
	{
		Event cityA = null;
		Event cityB = null;
		double a2 = 0;
		double b2 = 0;
	    cityA = eventMap.get(firstCity);
	    cityB = eventMap.get(secondCity);
	   // a2 = Math.pow(Math.abs(cityA.x() - cityB.x()), 2);
	   // b2 = Math.pow(Math.abs(cityA.y() - cityB.y()), 2);
        a2= 10;
        b2= 20;
	    return Math.sqrt(a2 + b2);
	}
	private static void bubbleSort()
	{
		boolean done = false;
		while(!done)
		{
			int changes = 0;
			int listSize = particles.size();
			for(int i = 0; i < listSize - 1; i++)
			{
				if(particles.get(i).compareTo(particles.get(i + 1)) == 1){
					Particle temp = particles.get(i);
					particles.set(i, particles.get(i + 1));
					particles.set(i + 1, temp);
					changes++;
				}
			}
			if(changes == 0){
				done = true;
			}
		}
		return;
	}
	private static void getVelocity()
	{
		double worstResults = 0;
		double vValue = 0.0;
		
		// after sorting, worst will be last in list.
	    worstResults = particles.get(PARTICLE_COUNT - 1).pBest();

	    for(int i = 0; i < PARTICLE_COUNT; i++)
	    {
	        vValue = (V_MAX * particles.get(i).pBest()) / worstResults;

	        if(vValue > V_MAX){
	        	particles.get(i).velocity(V_MAX);
	        }else if(vValue < 0.0){
	        	particles.get(i).velocity(0.0);
	        }else{
	        	particles.get(i).velocity(vValue);
	        }
	    }
	    return;
	}
	private static void updateparticles()
	{
		// Best is at index 0, so start from the second best.
	    for(int i = 1; i < PARTICLE_COUNT; i++)
	    {
    		// The higher the velocity score, the more changes it will need.
	    	int changes = (int)Math.floor(Math.abs(particles.get(i).velocity()));
    		System.out.println("Changes in City Positions(Swapping) " + i + ": " + changes);
        	for(int j = 0; j < changes; j++){
        		if(new Random().nextBoolean()){
        			randomlyArrange(i);
        		}
        		// Push it closer to it's best neighbor.
        		copyFromParticle(i - 1, i);
        	} // j
	        
	        // Update pBest value.
	        getTotalDistance(i);
	    } // i
	    
	    return;
	}
	private static void copyFromParticle(final int source, final int destination)
	{
		// push destination's data points closer to source's data points.
		Particle best = particles.get(source);
		int targetA = new Random().nextInt(EVENT_COUNT); // source's city to target.
		int targetB = 0;
		int indexA = 0;
		int indexB = 0;
		int tempIndex = 0;
		
		// targetB will be source's neighbor immediately succeeding targetA (circular).
		int i = 0;
		for(; i < EVENT_COUNT; i++)
		{
			if(best.data(i) == targetA){
				if(i == EVENT_COUNT - 1){
					targetB = best.data(0); // if end of array, take from beginning.
				}else{
					targetB = best.data(i + 1);
				}
				break;
			}
		}
		
		// Move targetB next to targetA by switching values.
		for(int j = 0; j < EVENT_COUNT; j++)
		{
			if(particles.get(destination).data(j) == targetA){
				indexA = j;
			}
			if(particles.get(destination).data(j) == targetB){
				indexB = j;
			}
		}
		// get temp index succeeding indexA.
		if(indexA == EVENT_COUNT - 1){
			tempIndex = 0;
		}else{
			tempIndex = indexA + 1;
		}
		
		// Switch indexB value with tempIndex value.
		int temp = particles.get(destination).data(tempIndex);
		particles.get(destination).data(tempIndex, particles.get(destination).data(indexB));
		particles.get(destination).data(indexB, temp);
		
		return;
	}
	
	
	private static class Particle implements Comparable<Particle>
    {
	    private int[] mData = new int[EVENT_COUNT]; // array keeps track of the index of the events
	    private double mpBest = 0;
	    private double mVelocity = 0.0;
	
	    public Particle()
	    {
	        this.mpBest = 0;
	        this.mVelocity = 0.0;
	    }
	    
	    public int compareTo(Particle that)
	    {
	    	if(this.pBest() < that.pBest()){
	    		return -1;
	    	}else if(this.pBest() > that.pBest()){
	    		return 1;
	    	}else{
	    		return 0;
	    	}
	    }
	
	    public int data(final int index)
	    {
	    	return this.mData[index];
	    }	    
	    public void data(final int index, int value)
	    {
	        this.mData[index] = value;
	        return;
	    }
	    public int[] getMData() {
	    	return mData;
	    }

	    public double pBest()
	    {
	    	return this.mpBest;
	    }

	    public void pBest(final double value)
	    {
	    	this.mpBest = value;
	    	return;
	    }
	
	    public double velocity()
	    {
	    	return this.mVelocity;
	    }
	    
	    public void velocity(final double velocityScore)
	    {
	       this.mVelocity = velocityScore;
	       return;
	    }
    } // Particle
	
/*	private static class eEvent extends Event
	{
		private double mX = 0;
		private double mY = 0;
		private boolean isPickup;
		
		public boolean isPickup() {
			return isPickup;
		}

		public void setPickup(boolean isPickup) {
			this.isPickup = isPickup;
		}

		public double x()
		{
		    return mX;
		}
		
		public void x(final double xCoordinate)
		{
		    mX = xCoordinate;
		    return;
		}
	
		public double y()
		{
		    return mY;
		}
		
		public void y(final double yCoordinate)
		{
		    mY = yCoordinate;
		    return;
		}
	} // eEvent
*/
}

