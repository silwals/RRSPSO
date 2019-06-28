package com.miamioh.ridesharing.app.controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.Resource;
import javax.validation.constraints.NotBlank;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.miamioh.ridesharing.app.constants.AppConstants;
import com.miamioh.ridesharing.app.data.dao.TaxiResponseDao;
import com.miamioh.ridesharing.app.data.entity.TaxiOnWait;
import com.miamioh.ridesharing.app.data.entity.TaxiResponse;
import com.miamioh.ridesharing.app.data.entity.TempScheduledEventList;
import com.miamioh.ridesharing.app.data.repository.TaxiOnWaitRepository;
import com.miamioh.ridesharing.app.data.repository.TempScheduledEventListRepository;
import com.miamioh.ridesharing.app.entity.Event;
import com.miamioh.ridesharing.app.entity.Taxi;
import com.miamioh.ridesharing.app.request.RideSharingConfirmation;
import com.miamioh.ridesharing.app.request.RideSharingConfirmationAck;
import com.miamioh.ridesharing.app.utilities.helper.TaxiUtility;

import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
public class TaxiResponseController {
	
	@Autowired
	private TaxiUtility taxiUtility;
	
	@Autowired
	private TaxiResponseDao taxiResponseDao;
	
	@Autowired
	private TempScheduledEventListRepository tempScheduledEventListRepository;
	
	@Autowired
	private TaxiOnWaitRepository taxiOnWaitRepository;
	
	private static final Logger log = LoggerFactory.getLogger(TaxiResponseController.class);
	/*@Resource(name="redisTemplate")
	private SetOperations<String, Event> setOperations;*/
	
	@Resource(name="redisTemplate")
	private ZSetOperations<String, Event> zSetOperations;
	
	@GetMapping(value = "/RideSharing/TaxiResponse/{request_id}", produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public TaxiResponse getTaxiResponsesByRequestId(@NotBlank @PathVariable(value="request_id") String requestId){
		log.info("Inside Get Taxi Response Controller");
		Iterable<TaxiResponse> taxiResponses = taxiResponseDao.getTaxiResponses(taxiResponseDao.getResponseIds(requestId));
		
		List<TaxiResponse> taxiResponsesList = new ArrayList<>();
		taxiResponses.forEach(a -> taxiResponsesList.add(a));
		log.info("Total No. Of Responses: "+taxiResponsesList.size());
		
		/*if the response if 0 no need to process anything*/
		if(taxiResponsesList.size()!=0) {
		
		Collections.sort(taxiResponsesList, ((a,b)->{
			int result = Double.valueOf(a.getCost()).compareTo(Double.valueOf(b.getCost()));
			if(result==0) {
				result = Long.valueOf(a.getPickTimeInMinutes()).compareTo(Long.valueOf(b.getPickTimeInMinutes()));
			}
			if(result == 0) {
				result = Long.valueOf(a.getTimeToDestinationInMinutes()).compareTo(Long.valueOf(b.getTimeToDestinationInMinutes()));
			}
			if(result == 0) {
				result = Integer.valueOf(a.getAvailableSeats()).compareTo(Integer.valueOf(b.getAvailableSeats()));
			}
			return result;
		}));
		return taxiResponsesList.get(0);
		}
		return null; //Check here 
	}
	
	@GetMapping(value="/RideSharing/Taxis", produces=MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public List<Taxi> getAllTaxi(){
		List<Taxi> allTaxi = taxiUtility.getAllTaxi();
		log.info("Total number of Taxis: "+allTaxi.size());
		return allTaxi;
	}
	
	
	@PostMapping(value = "/RideSharing/RideConfirmation", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public RideSharingConfirmationAck confirmRide(@RequestBody RideSharingConfirmation rideSharingConfirmation) {
		// What is happening here?? Permanent Queue??
		// Once the permanent queue is set , we need to find the success rate of mapping request and taxi
		// if it maps more >1 requests to single taxi then more successful
		RideSharingConfirmationAck ack = new RideSharingConfirmationAck();
		ack.setResponseId(rideSharingConfirmation.getResponseId());
		Taxi taxi = taxiUtility.getTaxiInstance(rideSharingConfirmation.getTaxiId());
		int noOfPassenger = taxi.getNoOfPassenger().get();//add synchronized block
		Optional<TaxiOnWait> taxiOnWait = taxiOnWaitRepository.findById(rideSharingConfirmation.getTaxiId());
		int waitCount =0;
		/*if(taxiOnWait.isPresent()) {
			waitCount=taxiOnWait.get().getCount();
			
		}*/
		if(rideSharingConfirmation.isConfirmed() && noOfPassenger < AppConstants.TAXI_MAX_CAPACITY)  {
			
			 Optional<TempScheduledEventList> findById = tempScheduledEventListRepository.findById(rideSharingConfirmation.getResponseId());
			 findById.ifPresent(a -> {
				 //setOperations.add(rideSharingConfirmation.getTaxiId(), a.getPickUpEvent());// can be used sorted set to sort all events based on timestamp
				 // setOperations.add(rideSharingConfirmation.getTaxiId(), a.getDropEvent());
				 zSetOperations.add(rideSharingConfirmation.getTaxiId(), a.getPickUpEvent(), a.getPickUpEvent().getIndex());
				 zSetOperations.add(rideSharingConfirmation.getTaxiId(), a.getDropEvent(), a.getDropEvent().getIndex());
				 taxi.getNoOfPassenger().incrementAndGet();
				 //Since the remaining set of events will be invalidated
				 // tempScheduledEventListRepository.deleteAll();
			 });
			 
			 if(findById.isPresent()) {
				 ack.setAckStatus(true);
				 ack.setTaxi(taxiUtility.getTaxiInstance(rideSharingConfirmation.getTaxiId()));
				 ack.setMessage("Booking Confirmed"); 
			 }else {
				 // Incase the 
				 ack.setAckStatus(false);
				 ack.setMessage("Timed Out");
			 }
		}else {
			ack.setAckStatus(false);
			ack.setMessage("Taxi Max capacity reached");
		}
		return ack;
	}
}
