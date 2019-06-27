package com.miamioh.ridesharing.app.utilities.helper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.geo.Circle;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.GeoResult;
import org.springframework.data.geo.GeoResults;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.connection.RedisGeoCommands.DistanceUnit;
import org.springframework.data.redis.connection.RedisGeoCommands.GeoLocation;
import org.springframework.data.redis.core.GeoOperations;
import org.springframework.stereotype.Component;

import com.miamioh.ridesharing.app.constants.AppConstants;
import com.miamioh.ridesharing.app.data.entity.TaxiOnWait;
import com.miamioh.ridesharing.app.data.entity.TempScheduledEventList;
import com.miamioh.ridesharing.app.data.repository.TaxiOnWaitRepository;
import com.miamioh.ridesharing.app.data.repository.TempScheduledEventListRepository;
import com.miamioh.ridesharing.app.entity.Taxi;
import com.miamioh.ridesharing.app.request.RideSharingRequest;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class TaxiUtility {
	
	@Resource(name="redisTemplate")
	private GeoOperations<String, String> geoOperations;
	
	@Autowired
	private TempScheduledEventListRepository tempScheduledEventListRepository;
	
	@Autowired
	private TaxiOnWaitRepository taxiOnWaitRepository;
	
	@Autowired
	private ScheduleTaxiEventsHelper scheduleTaxiEventsHelper;
	
	private static final String GEO_SPATIAL_KEY = UUID.randomUUID().toString();
	
	private static final Map<String, Taxi> taxiHub = new ConcurrentHashMap<>();
	
	private static final Logger log = LoggerFactory.getLogger(TaxiUtility.class);
	
	public void registerTaxi(Taxi taxi) {
		log.info("Registering Taxi with taxiId: "+taxi.getTaxiId());
		if(taxi!=null && !taxiHub.containsKey(taxi.getTaxiId())) {
			this.geoOperations.add(GEO_SPATIAL_KEY, new Point(taxi.getLongitude(), taxi.getLatitude()), taxi.getTaxiId());
			taxiHub.putIfAbsent(taxi.getTaxiId(), taxi);
		}
	}
	
	public boolean deregisterTaxi(Taxi taxi) {
		log.info("DeRegistering Taxi with taxiId: "+taxi.getTaxiId());
			return taxiHub.remove(taxi.getTaxiId(), taxi);
	}
	
	public void shareRide(RideSharingRequest request) {
		Circle circle = new Circle(new Point(request.getPickUpEvent().getLongitude(), request.getPickUpEvent().getLatitude()), new Distance(AppConstants.FIND_TAXI_WITHIN_RADIUS_IN_KMS, DistanceUnit.KILOMETERS));
		GeoResults<GeoLocation<String>> radius = this.geoOperations.radius(GEO_SPATIAL_KEY, circle);
		List<GeoResult<GeoLocation<String>>> content = radius.getContent();
		List<Taxi> nearByTaxiList = new ArrayList<>();
		for(GeoResult< GeoLocation<String>> geoResult: content) {
			nearByTaxiList.add(taxiHub.get((geoResult.getContent().getName())));
		}
		log.info("RequestId: "+request.getRequestID()+" Total Number of near by Taxis fetched: "+nearByTaxiList.size());
		log.info("RequestId: "+request.getRequestID()+" List of near by Taxis fetched: "+nearByTaxiList);
		
	
		for(Taxi taxi: nearByTaxiList) {
			//taxi.addEventSchedule(request);
			/* before scheduling events, check if taxi has available space
			 * process only if taxi has capacity to add more requests
			 */
			 Optional<TaxiOnWait> findById = taxiOnWaitRepository.findById(taxi.getTaxiId());
			
			if(taxi.getNoOfPassenger().get()<= AppConstants.TAXI_MAX_CAPACITY && request.getSeatsNeeded()< (AppConstants.TAXI_MAX_CAPACITY -taxi.getNoOfPassenger().get())) {
				/*
				 * findById.ifPresent(a->a.setCount(a.getCount()+1)); if(!findById.isPresent())
				 * { TaxiOnWait taxiOnWait = new TaxiOnWait();
				 * taxiOnWait.setTaxiId(taxi.getTaxiId()); taxiOnWait.setCount(1);
				 * taxiOnWaitRepository.save(taxiOnWait);// what if it does not get saved , or
				 * it saves multiple times check ? log.info("Creating TaxiOnWait object for  :"
				 * +taxi.getTaxiId()); }
				 */
				
			CompletableFuture.runAsync(() -> scheduleTaxiEventsHelper.findPSO(taxi, request));
			}else {
				log.info("TaxiId: "+taxi.getTaxiId() +" does not have capacity.");	
			}
		}
	}
	
	public Taxi getTaxiInstance(String taxiId) {
		return taxiHub.get(taxiId);
	}
	
	public List<Taxi> getAllTaxi(){
		List<Taxi> taxis = new ArrayList<>(taxiHub.values());
		return taxis;
	}

}
