package com.datenkrake;

public class Constants {

	private Constants() {
		
	}
	/*
	 * Badge
	 */
	public static final String BADGE_GOLD = "Gold! :)";
	public static final String BADGE_SILVER = "Silber!";
	public static final String BADGE_BRONZE = "Bronze";
	public static final String BADGE_NOTHING = "Nichts erreicht :(";
	public static final String BADGE_NAME_STRESSANSWER = "Antworten für gefühlten Stress";
	public static final String BADGE_NAME_CONTACTSSETUP = "Kontake richtig eingerichtet";
	public static final String BADGE_NAME_USAGETIME = "Nutzungsdauer der Datenkrake";
	
	/*
	 * Alarms
	 */
	public static final String ALARM_TIMES = "times";
	public static final String ALARM_TYPE = "alarmType";
	
	/*
	 * Notification
	 */
	public static final int PERCEIVED_STRESS_NOTIFICATION_ID = 0;
	public static final int ZEPHYR_NOTIFICATION_ID = 1;
	
	/*
	 * Intervals
	 */
	public static final long ACTIVITY_RECOGNITION_INTERVAL_MS = 30000;
	
	/*
	 * Request Codes
	 */
	public static final int REQUEST_CODE_ENABLE_BLUETOOTH = 1;
	
	/*
	 * Numbers
	 */
	public static final int MAX_CONTACT_REMINDERS = 5;
	public static final int NR_BADGE_GRID_COLUMNS = 4;
	
	/*
	 * Names
	 */
	public static final String NOTIFICATION_LISTENERS_SETTING_NAME = "enabled_notification_listeners";
	public static final String MAIN_SHARED_PREFS_NAME = "main_preferences";
	
	/*
	 * Plot
	 */
	public static final int MEASUREMENTS_PER_XLABEL_DISTANCE = 6;
}
