package com.datenkrake;

/**
 * Class representing a Cell
 * @author Thomas
 *
 */
public class Cell {

	/**
	 * Cell Tower code
	 */
	private String locationString;
	
	/**
	 * Name of this area
	 */
	private String area;
	
	/**
	 * Date the cell was monitored
	 */
	private long date;
	
	public Cell (String locationString, String area, long date) {
		this.locationString = locationString;
		this.area = area;
		this.date = date;
	}
	public String getLocationString() {
		return locationString;
	}
	public void setLocationString(String locationString) {
		this.locationString = locationString;
	}
	public long getDate() {
		return date;
	}
	public void setDate(long date) {
		this.date = date;
	}
	public String getArea() {
		return area;
	}
	public void setArea(String area) {
		this.area = area;
	}
}
