package com.miamioh.ridesharing.app.data.entity;

import java.util.ArrayList;

public class MetaInfo {
	private String timestamp;
	private String mapVersion;
	private String moduleVersion;
	private String interfaceVersion;
	private ArrayList<String> availableMapVersion;
	public String getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}
	public String getMapVersion() {
		return mapVersion;
	}
	public void setMapVersion(String mapVersion) {
		this.mapVersion = mapVersion;
	}
	public String getModuleVersion() {
		return moduleVersion;
	}
	public void setModuleVersion(String moduleVersion) {
		this.moduleVersion = moduleVersion;
	}
	public String getInterfaceVersion() {
		return interfaceVersion;
	}
	public void setInterfaceVersion(String interfaceVersion) {
		this.interfaceVersion = interfaceVersion;
	}
	public ArrayList<String> getAvailableMapVersion() {
		return availableMapVersion;
	}
	public void setAvailableMapVersion(ArrayList<String> availableMapVersion) {
		this.availableMapVersion = availableMapVersion;
	}
}
