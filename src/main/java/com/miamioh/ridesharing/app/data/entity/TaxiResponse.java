package com.miamioh.ridesharing.app.data.entity;

import java.io.Serializable;

import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

@RedisHash(value = "TaxiResponse")// removed TTL as it was getting deleted too fast
public class TaxiResponse implements Serializable {
	
	@Id
	private String responseId;
	private String requestId;
	private String taxiId;
	private int availableSeats;
	private int pickUpIndex;
	private int dropIndex;
	private Long timeToDestinationInMinutes;
	private double distanceInKms;
	private double cost;
	private Long pickTimeInMinutes;
	public String getResponseId() {
		return responseId;
	}
	public void setResponseId(String responseId) {
		this.responseId = responseId;
	}
	public String getRequestId() {
		return requestId;
	}
	public void setRequestId(String requestId) {
		this.requestId = requestId;
	}
	public String getTaxiId() {
		return taxiId;
	}
	public void setTaxiId(String taxiId) {
		this.taxiId = taxiId;
	}
	public int getAvailableSeats() {
		return availableSeats;
	}
	public void setAvailableSeats(int availableSeats) {
		this.availableSeats = availableSeats;
	}
	public int getPickUpIndex() {
		return pickUpIndex;
	}
	public void setPickUpIndex(int pickUpIndex) {
		this.pickUpIndex = pickUpIndex;
	}
	public int getDropIndex() {
		return dropIndex;
	}
	public void setDropIndex(int dropIndex) {
		this.dropIndex = dropIndex;
	}
	public Long getTimeToDestinationInMinutes() {
		return timeToDestinationInMinutes;
	}
	public void setTimeToDestinationInMinutes(Long timeToDestinationInMinutes) {
		this.timeToDestinationInMinutes = timeToDestinationInMinutes;
	}
	public double getDistanceInKms() {
		return distanceInKms;
	}
	public void setDistanceInKms(double distanceInKms) {
		this.distanceInKms = distanceInKms;
	}
	public double getCost() {
		return cost;
	}
	public void setCost(double cost) {
		this.cost = cost;
	}
	public Long getPickTimeInMinutes() {
		return pickTimeInMinutes;
	}
	public void setPickTimeInMinutes(Long pickTimeInMinutes) {
		this.pickTimeInMinutes = pickTimeInMinutes;
	}
	
	
}
