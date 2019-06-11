package com.miamioh.ridesharing.app.utilities.helper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.miamioh.ridesharing.app.constants.AppConstants;
import com.miamioh.ridesharing.app.constants.VertexTypeEnum;
import com.miamioh.ridesharing.app.data.dao.TaxiResponseDao;
import com.miamioh.ridesharing.app.data.entity.TaxiResponse;
import com.miamioh.ridesharing.app.data.entity.TempScheduledEventList;
import com.miamioh.ridesharing.app.data.repository.TempScheduledEventListRepository;
import com.miamioh.ridesharing.app.entity.Event;
import com.miamioh.ridesharing.app.entity.Taxi;
import com.miamioh.ridesharing.app.request.RideSharingRequest;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ScheduleTaxiEventsHelper {

	@Autowired
	private TaxiResponseDao taxiResponseDao;

	@Autowired
	private TempScheduledEventListRepository tempScheduledEventListRepository;
   
	@Autowired
	private RestTemplate restTemplate;
	
	/*
	 * @Resource(name="redisTemplate") private SetOperations<String, Event>
	 * setOperations;
	 */

	@Resource(name = "redisTemplate")
	private ZSetOperations<String, Event> zSetOperations;

	private static final Logger log = LoggerFactory.getLogger(ScheduleTaxiEventsHelper.class);
	
	public void scheduleEvents(Taxi taxi, RideSharingRequest request) {
		log.info("Inside Taxi Scheduler Utility RequestId: " + request.getRequestID());
		/*if (taxi.getNoOfPassenger().get() == AppConstants.TAXI_MAX_CAPACITY) {
			// taxi is full cannot add more requests
		} else */ //already handled
		if (taxi.getNoOfPassenger().get() == 0) {
			// taxi is empty and it can take in the new request
			Set<Event> events = zSetOperations.range(taxi.getTaxiId(), 0, -1);
			events.add(request.getPickUpEvent());
			events.add(request.getDropOffEvent());

		} else {
			// taxi already has schedule rides in it's event list
            Event pickup =request.getPickUpEvent();
            Event dropoff=request.getDropOffEvent();
            
            
           // taxi is empty and it can take in the new request
         			Set<Event> events = zSetOperations.range(taxi.getTaxiId(), 0, -1);
         			events.add(request.getPickUpEvent());
         			events.add(request.getDropOffEvent());
         	
         //Map pick and drop points 
         		/*	Map<String,List<Event>> dropToPickupMap = new HashMap<>();

         			for (Event e : events) {
         				if(e.isPickup())
         				dropToPickupMap.getOrDefault(e, null);
         				else {
         					dropToPickupMap.getOrDefault(e, null);
         				}
         				for (Vertex vert : vertexList) {
         					if (vert.getType().equals(VertexTypeEnum.PICKUP)) {
         						p1 = vert;
         					} else {
         						d1 = vert;
         					}
         				}
         				dropToPickupVertexMap.put(d1, p1);
         			}*/
         			
          // Before calling PSO initilaize the Map
         			PSO pso=new PSO(events.size());
         			pso.initializeMap(events);
          // PSO algorithm 
         		PSO.PSOAlgorithm(taxi,events);		
            
         	//create response
         		TaxiResponse response = createResponse(request, taxi);
         		log.info("Taxi Response Computed: " + response);
         	// Save the response
         		taxiResponseDao.save(response);
         	// Save the temporary schedule for the taxi 
    			saveEventsInTempScheduledEventList(request, response.getResponseId(), taxi.getTaxiId());
            

            //fetch time and distance from taxi to the pickup 
    		//&&&&&&&&&&&comment From here
           // double[] distanceAndTime = distanceAndTime(pickup.getLatitude(), dropoff.getLatitude(), pickup.getLongitude(),
           // 		dropoff.getLongitude());
        	
			/*
			 * TaxiResponse response = new TaxiResponse(); String responseId =
			 * UUID.randomUUID().toString(); response.setResponseId(responseId);
			 * response.setRequestId(request.getRequestID());
			 * response.setTaxiId(taxi.getTaxiId());
			 * response.setTaxiNumber(taxi.getTaxiNumber());
			 * response.setTaxiModel(taxi.getModel());
			 * response.setAvailableSeats(AppConstants.TAXI_MAX_CAPACITY -
			 * taxi.getNoOfPassenger().get()); response.setPickTimeInMinutes((new
			 * Double(distanceAndTime[1])).longValue()); log.info("Request ID: " +
			 * request.getRequestID() + " Taxi Id: " + taxi.getTaxiId() +
			 * " PickTimeInMinutes: " + response.getPickTimeInMinutes()); //Fetch the
			 * double[] distanceAndTime = distanceAndTime(pickup.getLatitude(),
			 * dropoff.getLatitude(), pickup.getLongitude(), dropoff.getLongitude());
			 * 
			 * // request.getPickUpEvent().setIndex(overallPickIndex); //
			 * response.setPickUpIndex(pickUpindex);
			 * response.setPickTimeInMinutes(calculateTime(totalWeightInMst));
			 * log.info("Request ID: " + request.getRequestID() + " Taxi Id: " +
			 * taxi.getTaxiId() + " PickTimeInMinutes: " + response.getPickTimeInMinutes());
			 * log.info("Request ID: " + request.getRequestID() + " Taxi Id: " +
			 * taxi.getTaxiId() + " distance: " + distance);
			 * 
			 * response.setDistanceInKms(distance / 1000.0);
			 * response.setCost(calculateCost(distance)); log.info("Request ID: " +
			 * request.getRequestID() + " Taxi Id: " + taxi.getTaxiId() + " totalCost: " +
			 * response.getCost());
			 * 
			 * int overallDropIndex = 0;
			 * response.setTimeToDestinationInMinutes(calculateTime(totalWeightToDestInMst))
			 * ; log.info("Taxi Response Computed: " + response);
			 * taxiResponseDao.save(response); saveEventsInTempScheduledEventList(request,
			 * responseId, taxi.getTaxiId());
			 */
		}

		}

	
	

	public void scheduleEvents_backUp(Taxi taxi, RideSharingRequest request) {
		log.info("Inside Taxi Scheduler Utility RequestId: " + request.getRequestID());
		Graph graph = new Graph();
		Vertex startVertex = new Vertex(taxi.getTaxiId(), taxi.getLatitude(), taxi.getLongitude(), VertexTypeEnum.TAXI);
		graph.addSingleVertex(startVertex);
		Vertex pickUpPoint = new Vertex(request.getRequestID(), request.getPickUpEvent().getLatitude(),
				request.getPickUpEvent().getLongitude(), VertexTypeEnum.PICKUP);
		Vertex dropVertex = new Vertex(request.getRequestID(), request.getPickUpEvent().getLatitude(),
				request.getPickUpEvent().getLongitude(), VertexTypeEnum.DROP);
		graph.addSingleVertex(pickUpPoint);
		graph.addSingleVertex(dropVertex);
		Set<Event> events = zSetOperations.range(taxi.getTaxiId(), 0, -1);
		Map<String, List<Vertex>> requestEventMap = new HashMap<>();
		for (Event event : events) {
			Vertex vertex = new Vertex(event.getRequestId(), event.getLatitude(), event.getLongitude(),
					event.isPickup() ? VertexTypeEnum.PICKUP : VertexTypeEnum.DROP);
			graph.addSingleVertex(vertex);
			if (requestEventMap.get(event.getRequestId()) != null) {
				requestEventMap.get(event.getRequestId()).add(vertex);
			} else {
				List<Vertex> eventList = new ArrayList<>();
				eventList.add(vertex);
				requestEventMap.put(event.getRequestId(), eventList);
			}

		}
		Map<Vertex, Vertex> dropToPickupVertexMap = new HashMap<>();

		for (String requestId : requestEventMap.keySet()) {
			List<Vertex> vertexList = requestEventMap.get(requestId);
			Vertex p1 = null;
			Vertex d1 = null;
			for (Vertex vert : vertexList) {
				if (vert.getType().equals(VertexTypeEnum.PICKUP)) {
					p1 = vert;
				} else {
					d1 = vert;
				}
			}
			dropToPickupVertexMap.put(d1, p1);
		}
		/*
		 * Vertex startVertex = new Vertex(id, latitude, longitude, type);
		 * startVertex.setLatitude(taxi.getLatitude());
		 * startVertex.setLongitude(taxi.getLongitude());
		 */
		PrimMST mst = new PrimMST();
		List<Edge> minSpanningTreeEdges = mst.primMST(graph, startVertex, dropToPickupVertexMap);// check that drop is
																									// always after pick
																									// up
		log.info("Request ID: " + request.getRequestID() + " Minimum Spanning tree: " + minSpanningTreeEdges);
		TaxiResponse response = new TaxiResponse();
		String responseId = UUID.randomUUID().toString();
		response.setResponseId(responseId);
		response.setRequestId(request.getRequestID());
		response.setTaxiId(taxi.getTaxiId());
		response.setTaxiNumber(taxi.getTaxiNumber());
		response.setTaxiModel(taxi.getModel());
		response.setAvailableSeats(AppConstants.TAXI_MAX_CAPACITY - taxi.getNoOfPassenger().get()); // increament no Of
																									// passenger in each
																									// taxi confirmation
		int overallPickIndex = 0;
		int pickUpindex = 0;
		double totalWeightInMst = 0.0;
		for (Edge edge : minSpanningTreeEdges) {
			if (edge.getEndVertex().getType().equals(VertexTypeEnum.PICKUP)) {
				pickUpindex++;
			}
			overallPickIndex++;
			totalWeightInMst = totalWeightInMst + edge.getWeight();
			if (edge.getEndVertex().equals(pickUpPoint)) {
				break;
			}
		}
		log.info("Request ID: " + request.getRequestID() + " Taxi Id: " + taxi.getTaxiId() + " pickUpIndex: "
				+ pickUpindex);
		log.info("Request ID: " + request.getRequestID() + " Taxi Id: " + taxi.getTaxiId() + " OverallPickUpIndex: "
				+ overallPickIndex);
		log.info("Request ID: " + request.getRequestID() + " Taxi Id: " + taxi.getTaxiId() + " totalWeightInMst: "
				+ totalWeightInMst);

		request.getPickUpEvent().setIndex(overallPickIndex);
		response.setPickUpIndex(pickUpindex);
		response.setPickTimeInMinutes(calculateTime(totalWeightInMst));
		log.info("Request ID: " + request.getRequestID() + " Taxi Id: " + taxi.getTaxiId() + " PickTimeInMinutes: "
				+ response.getPickTimeInMinutes());

		double distance = distance(pickUpPoint.getLatitude(), dropVertex.getLatitude(), pickUpPoint.getLongitude(),
				dropVertex.getLongitude(), 0.0, 0.0);
		log.info("Request ID: " + request.getRequestID() + " Taxi Id: " + taxi.getTaxiId() + " distance: " + distance);

		response.setDistanceInKms(distance / 1000.0);
		response.setCost(calculateCost(distance));
		log.info("Request ID: " + request.getRequestID() + " Taxi Id: " + taxi.getTaxiId() + " totalCost: "
				+ response.getCost());

		int overallDropIndex = 0;
		double totalWeightToDestInMst = 0.0;
		int dropOffindex = 0;
		for (Edge edge : minSpanningTreeEdges) {
			if (edge.getEndVertex().getType().equals(VertexTypeEnum.DROP)) {
				dropOffindex++;
			}
			overallDropIndex++;
			totalWeightToDestInMst = totalWeightToDestInMst + edge.getWeight();
			if (edge.getEndVertex().equals(dropVertex)) {
				break;
			}
		}
		log.info("Request ID: " + request.getRequestID() + " Taxi Id: " + taxi.getTaxiId() + " dropIndex: "
				+ dropOffindex);
		log.info("Request ID: " + request.getRequestID() + " Taxi Id: " + taxi.getTaxiId() + " OverallDropOffIndex: "
				+ overallDropIndex);
		log.info("Request ID: " + request.getRequestID() + " Taxi Id: " + taxi.getTaxiId() + " totalWeightToDestInMst: "
				+ totalWeightToDestInMst);

		request.getDropOffEvent().setIndex(overallDropIndex);
		response.setPickUpIndex(dropOffindex);
		response.setTimeToDestinationInMinutes(calculateTime(totalWeightToDestInMst));
		log.info("Taxi Response Computed: " + response);
		taxiResponseDao.save(response);
		saveEventsInTempScheduledEventList(request, responseId, taxi.getTaxiId());

	}

	private TaxiResponse createResponse(RideSharingRequest request, Taxi taxi) {
		log.info("Creating response for Request ID: " + request.getRequestID());
		TaxiResponse response = new TaxiResponse();
		String responseId = UUID.randomUUID().toString();
		response.setResponseId(responseId);
		response.setRequestId(request.getRequestID());
		response.setTaxiId(taxi.getTaxiId());
		//response.setTaxiNumber(taxi.getTaxiNumber());  Not required 
		//response.setTaxiModel(taxi.getModel());
		response.setAvailableSeats(AppConstants.TAXI_MAX_CAPACITY - taxi.getNoOfPassenger().get()); // increment no Of
																									// passenger in each																								// taxi confirmation
		return response;
	}
	private void saveEventsInTempScheduledEventList(RideSharingRequest request, String responseId, String taxiId) {
		TempScheduledEventList tempScheduledEventList = new TempScheduledEventList();
		tempScheduledEventList.setDropEvent(request.getDropOffEvent());
		tempScheduledEventList.setPickUpEvent(request.getPickUpEvent());
		tempScheduledEventList.setResponseId(responseId);
		tempScheduledEventList.setTaxiId(taxiId);
		tempScheduledEventListRepository.save(tempScheduledEventList);
	}

	private static long calculateTime(double totalWeightInMst) {
		double time = (totalWeightInMst / 1000.0) / AppConstants.AVG_SPEED_OF_TAXI_IN_KMPH;
		log.info("calculateTime: " + time);
		return Math.round(time) * 60;
	}

	private static double calculateCost(double distance) {
		return (distance * AppConstants.COST_PER_KMS);
	}

	/**
	 * Calculate distance between two points in latitude and longitude taking into
	 * account height difference. If you are not interested in height difference
	 * pass 0.0. Uses Haversine method as its base.
	 * 
	 * lat1, lon1 Start point lat2, lon2 End point el1 Start altitude in meters el2
	 * End altitude in meters
	 * 
	 * @returns Distance in Meters
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

	/**
     * Calculate distance between two points(given in latitude and longitude) 
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
}
