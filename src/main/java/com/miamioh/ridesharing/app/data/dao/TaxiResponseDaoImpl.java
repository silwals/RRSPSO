package com.miamioh.ridesharing.app.data.dao;

import java.util.Collection;
import java.util.Set;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.stereotype.Repository;

import com.miamioh.ridesharing.app.data.entity.TaxiResponse;
import com.miamioh.ridesharing.app.data.repository.TaxiResponseRepository;

@Repository
public class TaxiResponseDaoImpl implements TaxiResponseDao{
	
	@Autowired
	private TaxiResponseRepository repository;
	
	@Resource(name="redisTemplate")
	private SetOperations<String, String> setOperations;
	
	@Override
	public void save(TaxiResponse response) {
		try {
		repository.save(response);
		setOperations.add(response.getRequestId(), response.getResponseId());
		}
		catch(Exception e) {
			System.out.println("Exception while saving:"+e );
		}
	}
	
	@Override
	public Set<String> getResponseIds(String requestID){
		return setOperations.members(requestID);
	}
	
	@Override
	public Iterable<TaxiResponse> getTaxiResponses(Collection<String> taxiResponseIds){
		return repository.findAllById(taxiResponseIds);
	}
	@Override
	public void delete(String responseId) {
		repository.deleteById(responseId);
	}
	
	@Override
	public void deleteAll(Collection<TaxiResponse> taxiResponseList) {
		repository.deleteAll(taxiResponseList);
	}

}
