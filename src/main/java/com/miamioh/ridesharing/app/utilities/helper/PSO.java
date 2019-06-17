package com.miamioh.ridesharing.app.utilities.helper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.miamioh.ridesharing.app.entity.Event;

public class PSO {
	private static final Logger log = LoggerFactory.getLogger(ScheduleTaxiEventsHelper.class);
	private static final int PARTICLE_COUNT = 100;
	private static final int V_MAX = 4; // Maximum velocity change allowed.
										// Range: 0 >= V_MAX < EVENT_COUNT

	private static final int MAX_EPOCHS = 100;
	private static List<Particle> particles = new ArrayList<Particle>();

	public static List<Event> events = new ArrayList<Event>();
	private static Map<Event, Event> dropToPickupVertexMap = new HashMap<>();

	private static int noOfNodes;

	public PSO(List<Event> eventsPassed) {
		log.info("Inside PSO constructor ");
		events = eventsPassed;
		noOfNodes = events.size();
	}

	// void initializeMap(Set<Event> events, Map<Event, Event> dropToPickupMap)
	void initializeMap() {
		Map<String, List<Event>> requestEventMap = new HashMap<>();
		for (Event event : events) {
			if (requestEventMap.get(event.getRequestId()) != null) {
				requestEventMap.get(event.getRequestId()).add(event);
			} else {
				List<Event> eventList = new ArrayList<>();
				eventList.add(event);
				requestEventMap.put(event.getRequestId(), eventList);
			}

		}
		for (String requestId : requestEventMap.keySet()) {
			List<Event> nodeList = requestEventMap.get(requestId);
			Event p1 = null;
			Event d1 = null;
			for (Event vert : nodeList) {
				if (vert.isPickup()) {
					p1 = vert;
				} else {
					d1 = vert;
					
				}
			}
			dropToPickupVertexMap.put(d1, p1);
		}
		return;
	}

	// static void PSOAlgorithm(Taxi taxi, Set<Event> events)
	 void PSOAlgorithm() {
		log.info("Inside PSOAlgorithm");
		Particle aParticle = null;
		int epoch = 0;
		boolean done = false;

		initialize();
		while (!done) {
			// Two conditions can end this loop:
			// if the maximum number of epochs allowed has been reached, or,
			// if the Target value has been found.
			if (epoch < MAX_EPOCHS) {

				for (int i = 0; i < PARTICLE_COUNT; i++) {
					aParticle = particles.get(i);
					System.out.print("Particle: ");
					for (int j = 0; j < noOfNodes; j++) {
						System.out.print(aParticle.data(j) + ", ");
					} // j

					getTotalDistance(i);
					System.out.print("Distance: " + aParticle.pBest() + "\n");

				} // i

				bubbleSort(); // sort particles by their pBest scores, best to worst.

				getVelocity();

				updateparticles();

				System.out.println("epoch number: " + epoch);

				epoch++;

			} else {
				done = true;
			}
		}
		return;
	}

	private static void initialize() {
		for (int i = 0; i < PARTICLE_COUNT; i++) {
			Particle newParticle = new Particle();
			for (int j = 0; j < noOfNodes; j++) {
				newParticle.data(j, j);
			} // j
			particles.add(newParticle);
			for (int j = 0; j < 10; j++) {
				randomlyArrange(particles.indexOf(newParticle));
			}
			getTotalDistance(particles.indexOf(newParticle));
		} // i
		return;
	}

	private static void randomlyArrange(final int index) {
		int cityA = new Random().nextInt(noOfNodes);
		System.out.println("cityA" + cityA);
		int cityB = 0;
		boolean done = false;
		while (!done) {
			cityB = new Random().nextInt(noOfNodes);
			if (cityB != cityA) {
				done = true;
			}
		}

		int temp = particles.get(index).data(cityA);
		particles.get(index).data(cityA, particles.get(index).data(cityB));
		particles.get(index).data(cityB, temp);
		// swap if drop exist before pickup
		int[] mData = particles.get(index).getmData();
		log.info("mData :"+mData.length +" events :"+events.size());
		for (int i = 0; i < mData.length; i++) {
			Event node = events.get(mData[i]);
			if (!node.isPickup()) {
				for (int j = i + 1; j < mData.length; j++) {
					Event pickUpNode = events.get(mData[j]);
					if (dropToPickupVertexMap.get(node) != null && dropToPickupVertexMap.get(node).equals(pickUpNode)) {
						int temp1 = mData[i];
						mData[i] = mData[j];
						mData[j] = temp1;
						break;
					}
				}
			}
		}

		return;
	}

	// Fitness Function
	private static void getTotalDistance(final int index) {
		Particle thisParticle = null;
		thisParticle = particles.get(index);
		thisParticle.pBest(0.0);

		for (int i = 0; i < noOfNodes - 1; i++) {
			// commenting below as cab doesnt have to go to start node in order to complete
			// the trip.
			// if(i == noOfNodes - 1){
			// thisParticle.pBest(thisParticle.pBest() +
			// getDistance(thisParticle.data(noOfNodes - 1), thisParticle.data(0))); //
			// Complete trip.
			// }else{
			thisParticle.pBest(thisParticle.pBest() + getDistance(thisParticle.data(i), thisParticle.data(i + 1)));
			// }
		}
		return;
	}

	private static double getDistance(final int nodeAIndex, final int nodeBIndex) {
		Event nodeA = events.get(nodeAIndex);
		Event nodeB = events.get(nodeBIndex);
		double distance = distance(nodeA.getLatitude(), nodeB.getLatitude(), nodeA.getLongitude(), nodeB.getLongitude(),
				0.0, 0.0);

		return distance;
	}
	
	
	/**
     * Calculate distance and time between two points(given in latitude and longitude) 
     *
     * @returns Distance in Meters
     */
    public static double[] distanceAndTime(double lat1, double lat2, double lon1,
			double lon2) {
		log.info("Inside distance caluclator using HEREAPI ");
		double[] distanceAndTime = new double[2];
		String app_id = "4UCBn5UnDkcgKgY3gDNY";
		String app_code = "AuD0dBhxA6RTFPytkdYvhQ";
		String uri = "https://route.api.here.com/routing/7.2/calculateroute.json" + "?app_id=" + app_id + "&app_code="
				+ app_code + "&waypoint0=geo!" + lat1 + "," + lon1 + // 41.91,-87.63" +
				"&waypoint1=geo!" + lat2 + "," + lon2 + // 41.61,-87.62" +
				"&mode=fastest;car;traffic:disabled";

		RestTemplate restTemplate = new RestTemplate();
		try {
			String result = restTemplate.getForObject(uri, String.class);
			log.info("Output from Server .... \n" + result);
			String newVal = "{\"" + result.substring(result.lastIndexOf("summary"));
			System.out.println(newVal);
			int index = newVal.lastIndexOf("language") - 3;
			System.out.println(index);
			newVal = newVal.substring(0, index);
			System.out.println(newVal);
			final ObjectMapper mapper = new ObjectMapper();

			Map<String, Object> map = new HashMap<String, Object>();
			map = mapper.readValue(newVal, new TypeReference<HashMap<String, Object>>() {
			});
			System.out.println(map.keySet());
			System.out.println("keys" + map.keySet());
			System.out.println("values" + map.values().toString());
			//distanceAndTime[0]=;
			//distanceAndTime[1]=;
		} catch (Exception e) {
			e.printStackTrace();
		}
		
        return distanceAndTime; 

    }
	

	/*
	 * Calculates Haversine Distance ** @returns response in metres
	 * 
	 */
	
	public static double distance(double lat1, double lat2, double lon1, double lon2, double el1, double el2) {

		final int R = 6371; // Radius of the earth

		double latDistance = Math.toRadians(lat2 - lat1);
		double lonDistance = Math.toRadians(lon2 - lon1);
		double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2) + Math.cos(Math.toRadians(lat1))
				* Math.cos(Math.toRadians(lat2)) * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
		double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
		double distance = R * c * 1000; // convert to meters

		double height = el1 - el2;

		distance = Math.pow(distance, 2) + Math.pow(height, 2);

		return Math.sqrt(distance);
	}

	private static void bubbleSort() {
		boolean done = false;
		while (!done) {
			int changes = 0;
			int listSize = particles.size();
			for (int i = 0; i < listSize - 1; i++) {
				if (particles.get(i).compareTo(particles.get(i + 1)) == 1) {
					Particle temp = particles.get(i);
					particles.set(i, particles.get(i + 1));
					particles.set(i + 1, temp);
					changes++;
				}
			}
			if (changes == 0) {
				done = true;
			}
		}
		return;
	}

	private static void getVelocity() {
		double worstResults = 0;
		double vValue = 0.0;

		// after sorting, worst will be last in list.
		worstResults = particles.get(PARTICLE_COUNT - 1).pBest();

		for (int i = 0; i < PARTICLE_COUNT; i++) {
			vValue = (V_MAX * particles.get(i).pBest()) / worstResults;

			if (vValue > V_MAX) {
				particles.get(i).velocity(V_MAX);
			} else if (vValue < 0.0) {
				particles.get(i).velocity(0.0);
			} else {
				particles.get(i).velocity(vValue);
			}
		}
		return;
	}

	private static void updateparticles() {
		// Best is at index 0, so start from the second best.
		for (int i = 1; i < PARTICLE_COUNT; i++) {
			// The higher the velocity score, the more changes it will need.
			int changes = (int) Math.floor(Math.abs(particles.get(i).velocity()));
			System.out.println("Changes in City Positions(Swapping) " + i + ": " + changes);
			for (int j = 0; j < changes; j++) {
				if (new Random().nextBoolean()) {
					randomlyArrange(i);
				}
				// Push it closer to it's best neighbor.
				// copyFromParticle(i - 1, i);
			} // j

			// Update pBest value.
			getTotalDistance(i);
		} // i

		return;
	}

	public Particle printBestSolution() {
		/*
		 * if(particles.get(0).pBest() <= TARGET){ // Print it.
		 * System.out.println("Target reached."); }else{
		 * System.out.println("Target not reached"); }
		 */
		System.out.print("Shortest Route: ");
		for (int j = 0; j < noOfNodes; j++) {
			int val=particles.get(0).data(j);			
			System.out.print( val+ ", ");
		} // j
		System.out.print("Distance: " + particles.get(0).pBest() + "\n");
		return particles.get(0);
	}
	
	public static class Particle implements Comparable<Particle> {
		private int mData[] = new int[noOfNodes];
		private double mpBest = 0;
		private double mVelocity = 0.0;

		public Particle() {
			this.mpBest = 0;
			this.mVelocity = 0.0;
		}

		public int compareTo(Particle that) {
			if (this.pBest() < that.pBest()) {
				return -1;
			} else if (this.pBest() > that.pBest()) {
				return 1;
			} else {
				return 0;
			}
		}

		public int[] getmData() {
			return mData;
		}

		public void setmData(int[] mData) {
			this.mData = mData;
		}

		public int data(final int index) {
			return this.mData[index];
		}

		public void data(final int index, final int value) {
			this.mData[index] = value;
			return;
		}

		public double pBest() {
			return this.mpBest;
		}

		public void pBest(final double value) {
			this.mpBest = value;
			return;
		}

		public double velocity() {
			return this.mVelocity;
		}

		public void velocity(final double velocityScore) {
			this.mVelocity = velocityScore;
			return;
		}
	} // Particle

	public static void main(String[] args) {
		List<Event> events = new ArrayList<>();
	/*	Event ev1 = new Event();
		ev1.setRequestId(UUID.randomUUID().toString());
		ev1.setRequestId("101"); // kroger
		ev1.setLatitude(39.507558);
		ev1.setLongitude(-84.751148);
		ev1.setPickup(true);

		Event ev2 = new Event();
		ev2.setRequestId(ev1.getRequestId());
		ev2.setLatitude(39.510794); //Engineering building
		ev2.setLongitude(-84.732891);
		ev2.setPickup(false);

		Event ev3 = new Event();
		ev3.setRequestId("102");
		ev3.setLatitude(39.506015); //Hampton Inn Oxford
		ev3.setLongitude(-84.744711);
		ev3.setPickup(true);

		Event ev4 = new Event();
		ev4.setRequestId(ev3.getRequestId());
		ev4.setLatitude(39.510649);  //Miami University (Microbiology)
		ev4.setLongitude(-84.730935);
		ev4.setPickup(false);

		Event ev5 = new Event();
		ev5.setRequestId("103");  // William Holmes McGuffey Museum
		ev5.setLatitude(39.507208);
		ev5.setLongitude(-84.735907);
		ev5.setPickup(true);

		Event ev6 = new Event();
		ev6.setRequestId(ev5.getRequestId());
		ev6.setLatitude(39.492166);  //level 27 
		ev6.setLongitude(-84.719996);
		ev6.setPickup(false);
		
		
		
		Event ev7 = new Event();
		ev7.setRequestId("104");// 303 S Patterson Ave
		ev7.setLatitude(39.506880);
		ev7.setLongitude(-84.730163);
		ev7.setPickup(true);

		Event ev8 = new Event();
		ev8.setRequestId(ev7.getRequestId());
		ev8.setLatitude(39.497070);  //Oxford cemetery
		ev8.setLongitude(-84.729224);
		ev8.setPickup(false);
*/
		Event ev1 = new Event();
		ev1.setRequestId(UUID.randomUUID().toString());
		ev1.setRequestId("101"); // Hilton Chicago
		ev1.setLatitude(41.873060);
		ev1.setLongitude(-87.624563);
		ev1.setPickup(true);

		Event ev2 = new Event();
		ev2.setRequestId(ev1.getRequestId());
		ev2.setLatitude(41.885609); //Chicago theatre
		ev2.setLongitude(-87.627661);
		ev2.setPickup(false);

		Event ev3 = new Event();
		ev3.setRequestId("102");
		ev3.setLatitude(41.874635); //Columbia College Chicago
		ev3.setLongitude(-87.624771);
		ev3.setPickup(true);

		Event ev4 = new Event();
		ev4.setRequestId(ev3.getRequestId());
		ev4.setLatitude(41.883510);  //Jay Pritzker Pavillion
		ev4.setLongitude(-87.622001);
		ev4.setPickup(false);

		Event ev5 = new Event();
		ev5.setRequestId("103");  // Chicago Board of Trade Building
		ev5.setLatitude(41.878313);
		ev5.setLongitude(-87.632259);
		ev5.setPickup(true);

		Event ev6 = new Event();
		ev6.setRequestId(ev5.getRequestId());
		ev6.setLatitude(41.887351);  //Swissotel Chicago
		ev6.setLongitude(-87.619572);
		ev6.setPickup(false);
		
		
		Event ev7 = new Event();
		ev7.setRequestId("104");// Chicago Cultural centre
		ev7.setLatitude(41.884052);
		ev7.setLongitude(-87.624945);
		ev7.setPickup(true);
		
		
		Event ev8 = new Event();
		ev8.setRequestId(ev7.getRequestId());
		ev8.setLatitude(41.894919);  //North western memorial Hospital
		ev8.setLongitude(-87.621338);
		ev8.setPickup(false);
		

		events.add(ev1);
		events.add(ev2);
		events.add(ev3);
		events.add(ev4);
	    events.add(ev5);
		events.add(ev6);
	    events.add(ev7);
		events.add(ev8);
		
		
		log.info("Size of events"+events.size());
		PSO psoImpl = new PSO(events);
		psoImpl.initializeMap();
		psoImpl.PSOAlgorithm();
		Particle printBestSolution = psoImpl.printBestSolution();
		List<Event> psoNodes = new ArrayList<>();
		log.info("Final Event Schedule after PSO");
		for (int i : printBestSolution.getmData()) {
			Event node = psoImpl.events.get(i);
			psoNodes.add(node);
			System.out.println(node.getRequestId() +":"+node.isPickup());
		}
		return;
	}

}
