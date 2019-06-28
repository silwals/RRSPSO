package com.miamioh.ridesharing.app.entity;

import java.util.concurrent.atomic.AtomicInteger;

public class Taxi {
	
	private String taxiId;
	private double longitude;
	private double latitude;
	private AtomicInteger noOfPassenger;
	
	public String getTaxiId() {
		return taxiId;
	}

	public void setTaxiId(String taxiId) {
		this.taxiId = taxiId;
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
	public AtomicInteger getNoOfPassenger() {
		return noOfPassenger;
	}

	public void setNoOfPassenger(AtomicInteger noOfPassenger) {
		this.noOfPassenger = noOfPassenger;
	}

	public Taxi(String taxiNumber, double longitude, double latitude) {
		super();
		this.longitude = longitude;
		this.latitude = latitude;
		this.noOfPassenger= new AtomicInteger(0);
		//this.tempScheduledEventList = new LinkedList<Event>();
		//this.finalScheduledEventList = new LinkedList<Event>();
		/*this.routeGraph = new Graph();
		Vertex startingVertex = new Vertex(this.getTaxiId(), this.getLatitude(), this.getLongitude(), VertexTypeEnum.TAXI);
		routeGraph.addSingleVertex(startingVertex);*/
		//taxiUtility.registerTaxi(this);
	}

	public Taxi() {
		
	}

	
	/*public void addEventSchedule(RideSharingRequest request) {
		boolean isValid = validateRequest(request);
		if(isValid) {
			//this.tempScheduledEventList.add(request.getPickUpEvent());
			//this.tempScheduledEventList.add(request.getDropOffEvent());
			Vertex pickUpPoint = new Vertex(request.getRequestID(), request.getPickUpEvent().getLatitude(), request.getPickUpEvent().getLongitude(), VertexTypeEnum.PICKUP);
			Vertex dropVertex = new Vertex(request.getRequestID(), request.getPickUpEvent().getLatitude(), request.getPickUpEvent().getLongitude(), VertexTypeEnum.DROP);
			routeGraph.addSingleVertex(pickUpPoint);
			routeGraph.addSingleVertex(dropVertex);
			scheduleTaxiEventsHelper.scheduleEvents(this, request);
		}
	}*/
	
	/*private boolean validateRequest(RideSharingRequest request) {
		if((noOfPassenger < maxCapacity) &&
				(request.getPickUpEvent().getEventTime().before(request.getDropOffEvent().getEventTime()))) {
			
		}
		return false;
		
	}*/
	
	/*public void deregisterTaxi() {
		taxiUtility.deregisterTaxi(this);
	}*/
}
