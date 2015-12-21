package com.datenkrake;

/**
 * This class represents an area with a start time a user started being in it
 * @author Thomas
 *
 */
public class Area {

	private String name;
	private long startTime;
	
	public Area (String name, long startTime) {
		this.name = name;
		this.startTime = startTime;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public long getStartTime() {
		return startTime;
	}
	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}
}
