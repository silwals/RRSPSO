package com.miamioh.ridesharing.app.request;

import com.miamioh.ridesharing.app.entity.Taxi;

public class RideSharingConfirmationAck {
	
	private String responseId;
	private boolean ackStatus;
	private Taxi taxi;
	private String message;
	public String getResponseId() {
		return responseId;
	}
	public void setResponseId(String responseId) {
		this.responseId = responseId;
	}
	public boolean isAckStatus() {
		return ackStatus;
	}
	public void setAckStatus(boolean ackStatus) {
		this.ackStatus = ackStatus;
	}
	public Taxi getTaxi() {
		return taxi;
	}
	public void setTaxi(Taxi taxi) {
		this.taxi = taxi;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}

}
