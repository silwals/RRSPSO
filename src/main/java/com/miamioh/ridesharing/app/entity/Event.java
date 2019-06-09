package com.miamioh.ridesharing.app.entity;

import java.util.Date;

public class Event {
	
	private String requestId;
	private double longitude;
	private double latitude;
	private Date eventTime;
	private long tolerance;
	private boolean isPickup;
	private int index;
	private Date timeOfOccurance;
	public String getRequestId() {
		return requestId;
	}
	public void setRequestId(String requestId) {
		this.requestId = requestId;
	}
	public double getLongitude() {
		return longitude;
	}
	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}
	public double getLatitude() {
		return latitude;
	}
	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}
	public Date getEventTime() {
		return eventTime;
	}
	public void setEventTime(Date eventTime) {
		this.eventTime = eventTime;
	}
	public long getTolerance() {
		return tolerance;
	}
	public void setTolerance(long tolerance) {
		this.tolerance = tolerance;
	}
	public boolean isPickup() {
		return isPickup;
	}
	public void setPickup(boolean isPickup) {
		this.isPickup = isPickup;
	}
	public int getIndex() {
		return index;
	}
	public void setIndex(int index) {
		this.index = index;
	}
	public Date getTimeOfOccurance() {
		return timeOfOccurance;
	}
	public void setTimeOfOccurance(Date timeOfOccurance) {
		this.timeOfOccurance = timeOfOccurance;
	}
	
}
