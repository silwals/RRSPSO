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
	
	@Resource(name = "redisTemplate")
	private ZSetOperations<String, Event> zSetOperations;

	private static final Logger log = LoggerFactory.getLogger(ScheduleTaxiEventsHelper.class);
	/*
	 * Makes a call to the PSO utility class 
	 */
	public void findPSO(Taxi taxi, RideSharingRequest request) {
		log.info("Inside PSO Taxi Scheduler Utility RequestId: "+request.getRequestID());
		Set<Event> events = zSetOperations.range(taxi.getTaxiId(), 0, -1);
		TaxiResponse response =null;
		//Handle the edge case
		if (taxi.getNoOfPassenger().get() == 0 || events.size()==0) {
			log.info("No of passengers : "+taxi.getNoOfPassenger().get()+ " PSO is not called");
			// taxi is empty and it can take in the new request
			List<Event> singleRequest=new ArrayList<>();
			singleRequest.add(0, request.getPickUpEvent());
			singleRequest.add(1,request.getDropOffEvent());
			response = createResponseObject(request, taxi);
			Event taxiNode = new Event();
			taxiNode.setLatitude(taxi.getLatitude());
			taxiNode.setLongitude(taxi.getLongitude());
			double totalWeightInMst=getDistance(taxiNode, singleRequest.get(0));
				
			request.getPickUpEvent().setIndex(0);
			response.setPickUpIndex(0);
			response.setPickTimeInMinutes(calculateTime(totalWeightInMst));
			log.info("Request ID: "+request.getRequestID()+" Taxi Id: "+taxi.getTaxiId()+" PickTimeInMinutes: "+response.getPickTimeInMinutes());
			
			double distance = distance(request.getPickUpEvent().getLatitude(), request.getDropOffEvent().getLatitude(), request.getPickUpEvent().getLongitude(), request.getDropOffEvent().getLongitude(), 0.0, 0.0);
			log.info("Request ID: "+request.getRequestID()+" Taxi Id: "+taxi.getTaxiId()+" distance: "+distance);
			
			response.setDistanceInKms(distance/1000.0);
			response.setCost(calculateCost(distance));
			log.info("Request ID: "+request.getRequestID()+" Taxi Id: "+taxi.getTaxiId()+" totalCost: "+response.getCost());
			
			request.getDropOffEvent().setIndex(1);
			response.setDropIndex(1);
			response.setTimeToDestinationInMinutes(calculateTime(distance));			
			
		}else {
			log.info("No of passengers : "+taxi.getNoOfPassenger().get()+ "PSO is called");
			/*
			 * If taxi is not empty and has scheduled events in it's list
			 */
			events.add(request.getPickUpEvent());
			events.add(request.getDropOffEvent());
			PSO psoImpl = new PSO(new ArrayList<>(events));
			List<Event> psoEvents = psoImpl.start();
			
			log.info("Request ID: "+request.getRequestID()+" PSO Shortest Path: "+psoEvents);
	 		
	 		response = createResponseObject(request, taxi);
	 		log.info("Taxi Response Object Computed:"+response);
	 		log.info("Response needs the time to pick the request from the taxi");
	 		int overallPickIndex = 0;
			int pickUpindex = 0;
			double totalWeightInMst = 0.0;
			Event taxiNode = new Event();
			taxiNode.setLatitude(taxi.getLatitude());
			taxiNode.setLongitude(taxi.getLongitude());
			Event currentNode = taxiNode;
			for(Event event: psoEvents) {
				if(event.isPickup()) {
					pickUpindex++;
				}
				overallPickIndex++;
				totalWeightInMst = totalWeightInMst+ getDistance(currentNode, event);
				currentNode = event;
				if(event.equals(request.getPickUpEvent())) {
					break;
				}
			}
			log.info("Request ID: "+request.getRequestID()+" Taxi Id: "+taxi.getTaxiId()+" pickUpIndex: "+pickUpindex);
			log.info("Request ID: "+request.getRequestID()+" Taxi Id: "+taxi.getTaxiId()+" OverallPickUpIndex: "+overallPickIndex);
			log.info("Request ID: "+request.getRequestID()+" Taxi Id: "+taxi.getTaxiId()+" totalWeightInMst: "+totalWeightInMst);
			
			request.getPickUpEvent().setIndex(overallPickIndex);
			response.setPickUpIndex(pickUpindex);
			response.setPickTimeInMinutes(calculateTime(totalWeightInMst));
			log.info("Request ID: "+request.getRequestID()+" Taxi Id: "+taxi.getTaxiId()+" PickTimeInMinutes: "+response.getPickTimeInMinutes());
			
			double distance = distance(request.getPickUpEvent().getLatitude(), request.getDropOffEvent().getLatitude(), request.getPickUpEvent().getLongitude(), request.getDropOffEvent().getLongitude(), 0.0, 0.0);
			log.info("Request ID: "+request.getRequestID()+" Taxi Id: "+taxi.getTaxiId()+" distance: "+distance);
			
			response.setDistanceInKms(distance/1000.0);
			response.setCost(calculateCost(distance));
			log.info("Request ID: "+request.getRequestID()+" Taxi Id: "+taxi.getTaxiId()+" totalCost: "+response.getCost());
			
			int overallDropIndex = 0;
			double totalWeightToDestInMst = 0.0;
			int dropOffindex = 0;
			currentNode = taxiNode;
			for(Event event: psoEvents) {
				if(!event.isPickup()) {
					dropOffindex++;
				}
				overallDropIndex++;
				totalWeightToDestInMst = totalWeightToDestInMst + getDistance(currentNode, event);
				currentNode = event;
				if(event.equals(request.getDropOffEvent())) {
					break;
				}
			}
			log.info("Request ID: "+request.getRequestID()+" Taxi Id: "+taxi.getTaxiId()+" dropIndex: "+dropOffindex);
			log.info("Request ID: "+request.getRequestID()+" Taxi Id: "+taxi.getTaxiId()+" OverallDropOffIndex: "+overallDropIndex);
			log.info("Request ID: "+request.getRequestID()+" Taxi Id: "+taxi.getTaxiId()+" totalWeightToDestInMst: "+totalWeightToDestInMst);
			
			request.getDropOffEvent().setIndex(overallDropIndex);
			response.setPickUpIndex(dropOffindex);
			response.setTimeToDestinationInMinutes(calculateTime(totalWeightToDestInMst));
		    }
			log.info("Taxi Response Computed: "+response);
			taxiResponseDao.save(response);
			saveEventsInTempScheduledEventList(request, response.getResponseId(), taxi.getTaxiId());
			return ;

		}
		
	private TaxiResponse createResponseObject(RideSharingRequest request, Taxi taxi) {
		log.info("Creating response for Request ID: " + request.getRequestID());
		TaxiResponse response = new TaxiResponse();
		String responseId = UUID.randomUUID().toString();
		response.setResponseId(responseId);
		response.setRequestId(request.getRequestID());
		response.setTaxiId(taxi.getTaxiId());
		response.setAvailableSeats(AppConstants.TAXI_MAX_CAPACITY-taxi.getNoOfPassenger().get()); // increment no Of passenger in each taxi confirmation
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
	private double getDistance(Event nodeA, Event nodeB) {
		double distance = distance(nodeA.getLatitude(), nodeB.getLatitude(), nodeA.getLongitude(), nodeB.getLongitude(), 0.0, 0.0);

		return distance;
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
