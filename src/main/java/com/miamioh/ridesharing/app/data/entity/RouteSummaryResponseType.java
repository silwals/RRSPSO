package com.miamioh.ridesharing.app.data.entity;

public class RouteSummaryResponseType {
	private double distance;
	private double trafficTime;
	private double baseTime;
	private String[] flags;
	private String text;
	private double travelTime;
	private String _type;
	public double getDistance() {
		return distance;
	}
	public void setDistance(double distance) {
		this.distance = distance;
	}
	public double getTrafficTime() {
		return trafficTime;
	}
	public void setTrafficTime(double trafficTime) {
		this.trafficTime = trafficTime;
	}
	public double getBaseTime() {
		return baseTime;
	}
	public void setBaseTime(double baseTime) {
		this.baseTime = baseTime;
	}
	public String[] getFlags() {
		return flags;
	}
	public void setFlags(String[] flags) {
		this.flags = flags;
	}
	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
	}
	public double getTravelTime() {
		return travelTime;
	}
	public void setTravelTime(double travelTime) {
		this.travelTime = travelTime;
	}
	public String get_type() {
		return _type;
	}
	public void set_type(String _type) {
		this._type = _type;
	}
	
	

}
