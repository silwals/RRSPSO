package com.miamioh.ridesharing.app.kafka.consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import com.miamioh.ridesharing.app.entity.Taxi;
import com.miamioh.ridesharing.app.request.RideSharingRequest;
import com.miamioh.ridesharing.app.utilities.helper.TaxiUtility;

import lombok.extern.slf4j.Slf4j;

@Component
public class RideSharingRequestConsumer {
	private static final Logger log = LoggerFactory.getLogger(RideSharingRequestConsumer.class);
	@Autowired
	private TaxiUtility taxiUtility;
	
	@KafkaListener(topics="${kafka.topic}", containerFactory="batchFactory")
	public void consumeRideSharingRequest(RideSharingRequest rideSharingRequest, Acknowledgment ack){
		log.info("Recieved Ride Sharing Request: "+rideSharingRequest);
		taxiUtility.shareRide(rideSharingRequest);
		ack.acknowledge();
	}
	
	@KafkaListener(topics="${kafka.taxi.topic}", containerFactory="registerTaxi")
	public void registerTaxi(Taxi taxi, Acknowledgment ack){
		log.info("Recieved Register Taxi Request: "+taxi);
		taxiUtility.registerTaxi(taxi);
		ack.acknowledge();
	}
	
}
