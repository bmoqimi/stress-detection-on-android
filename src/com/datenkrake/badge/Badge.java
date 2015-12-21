package com.datenkrake.badge;

/**
 * 
 * @author Thomas
 *
 */
public abstract class Badge {

	protected final String name;
	protected int progress;
	protected int badgeID;
	protected String badgeLevel;
	
	public Badge (String name) {
		this.name = name;
	}
	
	public abstract void updateProgress (Object arg);
	
	/**
	 * Updates the id of the badge to be shown and the name of the badge level
	 */
	protected abstract void updateStatus();
	
	/**
	 * Returns the level of the badge i.e. nothing, bronze, silver, gold
	 * @return The level name that equals one from {@link Constants}
	 */
	public String getBadgeLevel() {
		return badgeLevel;
	}
	
	/**
	 * Returns the image ID of the badge
	 * @return The id of the image
	 */
	public int getBadgeId() {
		return badgeID;
	}
	
	/**
	 * Returns the name of the badge
	 * @return The name that equals one from {@link Constants}
	 */
	public String getName() {
		return name;
	}
}
