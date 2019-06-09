package com.miamioh.ridesharing.app.request;

import java.util.Date;

import com.miamioh.ridesharing.app.entity.Event;

public class RideSharingRequest {
	
	private String requestID;
	private Event pickUpEvent;
	private Event dropOffEvent;
	private Date timestamp; // Time at which a request is sent
	private int seatsNeeded;
	public String getRequestID() {
		return requestID;
	}
	public void setRequestID(String requestID) {
		this.requestID = requestID;
	}
	public Event getPickUpEvent() {
		return pickUpEvent;
	}
	public void setPickUpEvent(Event pickUpEvent) {
		this.pickUpEvent = pickUpEvent;
	}
	public Event getDropOffEvent() {
		return dropOffEvent;
	}
	public void setDropOffEvent(Event dropOffEvent) {
		this.dropOffEvent = dropOffEvent;
	}
	public Date getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}
	public int getSeatsNeeded() {
		return seatsNeeded;
	}
	public void setSeatsNeeded(int seatsNeeded) {
		this.seatsNeeded = seatsNeeded;
	}
	
}
