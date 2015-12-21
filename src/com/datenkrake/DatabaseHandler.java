package com.datenkrake;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.concurrent.TimeUnit;

import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;

/**
 * 
 * @author Thomas
 *
 */
public class DatabaseHandler extends SQLiteOpenHelper {
	
	private static DatabaseHandler instance;
	
	public static DatabaseHandler getInstance(Context context) {
		if (instance == null) {
			instance = new DatabaseHandler(context);
		}
		return instance;
	}
	
	private DatabaseHandler(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}
	
	private final String TAG = getClass().getSimpleName();
	
	private final long STRESS_CALCULATION_INTERVAL_MINUTES = 10;
	private final long STRESS_CALCULATION_INTERVAL_MILLIS = TimeUnit.MILLISECONDS.convert(STRESS_CALCULATION_INTERVAL_MINUTES, TimeUnit.MINUTES);
	
	private final static int DATABASE_VERSION = 3;
	private final static String DATABASE_NAME = "solinContextApp";
	
	private final String TABLE_AREAS = "areas";
	private final String KEY_AREAS_ID = "id";
	private final String KEY_AREAS_NAME = "name";
	
	private final String TABLE_AREA_VALUES = "areaValues";
	private final String KEY_VALUES_ID = "id";
	private final String KEY_VALUES_LOCATIONSTRING = "locationString";
	private final String KEY_VALUES_AREAID = "areaId";
	
    private final String TABLE_AREAHISTORY = "areahistory";
    private final String KEY_AREAHISTORY_ID = "id";
    private final String KEY_AREAHISTORY_LOCATIONSTRING = "locationString";
    private final String KEY_AREAHISTORY_START = "start";
	
	private final String TABLE_COMMFLOWS = "communicationFlows";
	private final String KEY_COMMFLOWS_ID = "id";
	private final String KEY_COMMFLOWS_MEDIUM = "medium";
	private final String KEY_COMMFLOWS_CONTACT = "contact";
	private final String KEY_COMMFLOWS_DATE = "date";
	private final String KEY_COMMFLOWS_DURATION = "duration";
	private final String KEY_COMMFLOWS_IMPORTANT = "important";
	private final String KEY_COMMFLOWS_AREA = "area";
	private final String KEY_COMMFLOWS_BODY = "body";
	
	private final String TABLE_SCREENTIME = "screenTime";
	private final String KEY_SCREENTIME_ID = "id";
	private final String KEY_SCREENTIME_DATE = "date";
	private final String KEY_SCREENTIME_TIME = "time";

    private final String TABLE_STRESSLEVELS = "stressLevels";
    private final String KEY_STRESSLEVELS_ID = "id";
    private final String KEY_STRESSLEVELS_STRESS = "stress";
    private final String KEY_STRESSLEVELS_NOTIFICATIONDATE = "notificationDate";
    private final String KEY_STRESSLEVELS_REALDATE = "realDate";
    private final String KEY_STRESSLEVELS_TIMEOFDAY = "timeOfDay";
    
    private final String TABLE_HEARTRATE = "heartrate";
    private final String KEY_HEARTRATE_ID = "id";
    private final String KEY_HEARTRATE_DATE = "date";
    private final String KEY_HEARTRATE_HEARTRATE = "heartrate";
    private final String KEY_HEARTRATE_SPEED = "speed";
    
    private final String TABLE_APPTIME = "apptime";
    private final String KEY_APPTIME_ID = "id";
    private final String KEY_APPTIME_NAME = "name";
    private final String KEY_APPTIME_TIME = "time";
    private final String KEY_APPTIME_START = "start";
    
    private final String TABLE_ACTIVITIES = "activities";
    private final String KEY_ACTIVITIES_ID = "id";
    private final String KEY_ACTIVITIES_ACTIVITY = "activity";
    private final String KEY_ACTIVITIES_CONFIDENCE = "confidence";
    private final String KEY_ACTIVITES_TIME = "time";
    
    private final String TABLE_BADGES = "badges";
    private final String KEY_BADGES_ID = "id";
    private final String KEY_BADGES_NAME = "name";
    private final String KEY_BADGES_PROGRESS = "progress";
    
    String CREATE_AREAS_TABLE = "CREATE TABLE " + TABLE_AREAS + "("
			+ KEY_AREAS_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
			+ KEY_AREAS_NAME + " TEXT UNIQUE"
			+ ")";
	
	
	String CREATE_AREA_VALUES_TABLE = "CREATE TABLE " + TABLE_AREA_VALUES + "("
			+ KEY_VALUES_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
			+ KEY_VALUES_LOCATIONSTRING	+ " TEXT,"
			+ KEY_VALUES_AREAID + " INTEGER,"
			+ "FOREIGN KEY(" + KEY_VALUES_AREAID + ")" + " REFERENCES " + TABLE_AREAS + "(" + KEY_AREAS_ID + "), "
			+ "UNIQUE(" + KEY_VALUES_LOCATIONSTRING + ")" // cellid and areaid pair only once
			+ ")";
	
	String CREATE_COMMFLOWS_TABLE = "CREATE TABLE " + TABLE_COMMFLOWS + "("
			+ KEY_COMMFLOWS_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
			+ KEY_COMMFLOWS_MEDIUM + " TEXT,"
			+ KEY_COMMFLOWS_CONTACT + " TEXT,"
			+ KEY_COMMFLOWS_DATE + " INTEGER,"
			+ KEY_COMMFLOWS_DURATION + " INTEGER,"
			+ KEY_COMMFLOWS_IMPORTANT + " BOOLEAN,"
			+ KEY_COMMFLOWS_AREA + " TEXT,"
			+ KEY_COMMFLOWS_BODY + " BOOLEAN"
			+ ")";

	String CREATE_SCREENTIME_TABLE = "CREATE TABLE " + TABLE_SCREENTIME + "("
			+ KEY_SCREENTIME_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
			+ KEY_SCREENTIME_DATE + " INTEGER UNIQUE,"
			+ KEY_SCREENTIME_TIME + " INTEGER"
			+ ")";

    String CREATE_STRESSLEVELS_TABLE = "CREATE TABLE " + TABLE_STRESSLEVELS + "("
            + KEY_STRESSLEVELS_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + KEY_STRESSLEVELS_NOTIFICATIONDATE + " INTEGER UNIQUE,"
            + KEY_STRESSLEVELS_REALDATE + " INTEGER UNIQUE,"
            + KEY_STRESSLEVELS_STRESS + " INTEGER,"
            + KEY_STRESSLEVELS_TIMEOFDAY + " TEXT"
            + ")";
    
    String CREATE_HEARTRATE_TABLE = "CREATE TABLE " + TABLE_HEARTRATE + "("
    		+ KEY_HEARTRATE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
    		+ KEY_HEARTRATE_DATE + " INTEGER UNIQUE,"
    		+ KEY_HEARTRATE_HEARTRATE + " INTEGER,"
    		+ KEY_HEARTRATE_SPEED + " INTEGER"
    		+ ")";
    
    String CREATE_APPTIME_TABLE = "CREATE TABLE " + TABLE_APPTIME + "("
    		+ KEY_APPTIME_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
    		+ KEY_APPTIME_NAME + " TEXT,"
    		+ KEY_APPTIME_TIME + " INTEGER,"
    		+ KEY_APPTIME_START + " INTEGER"
    		+ ")";
    
    String CREATE_AREAHISTORY_TABLE = "CREATE TABLE " + TABLE_AREAHISTORY + "("
    		+ KEY_AREAHISTORY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
    		+ KEY_AREAHISTORY_LOCATIONSTRING + " TEXT,"
    		+ KEY_AREAHISTORY_START + " INTEGER,"
    		+ "FOREIGN KEY(" + KEY_AREAHISTORY_LOCATIONSTRING + ")" + " REFERENCES " + TABLE_AREA_VALUES + "(" + KEY_VALUES_LOCATIONSTRING + ")"
    		+ ")";
    
    String CREATE_ACTIVITIES_TABLE = "CREATE TABLE " + TABLE_ACTIVITIES + "("
    		+ KEY_ACTIVITIES_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
    		+ KEY_ACTIVITIES_ACTIVITY + " TEXT,"
    		+ KEY_ACTIVITIES_CONFIDENCE + " INTEGER,"
    		+ KEY_ACTIVITES_TIME + " INTEGER"
    		+ ")";
    
    String CREATE_BADGES_TABLE = "CREATE TABLE " + TABLE_BADGES + "("
    		+ KEY_BADGES_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
    		+ KEY_BADGES_NAME + " TEXT UNIQUE,"
    		+ KEY_BADGES_PROGRESS + " INTEGER"
    		+ ")";
    
	@Override
	public void onCreate(SQLiteDatabase db) {
		Log.i(TAG, CREATE_COMMFLOWS_TABLE);
		Log.i(TAG, CREATE_AREAS_TABLE);
		Log.i(TAG, CREATE_AREA_VALUES_TABLE);
		Log.i(TAG, CREATE_SCREENTIME_TABLE);
		Log.i(TAG, CREATE_STRESSLEVELS_TABLE);
		Log.i(TAG, CREATE_HEARTRATE_TABLE);
		Log.i(TAG, CREATE_APPTIME_TABLE);
		Log.i(TAG, CREATE_AREAHISTORY_TABLE);
		Log.i(TAG, CREATE_ACTIVITIES_TABLE);
		Log.i(TAG, CREATE_BADGES_TABLE);
		db.execSQL(CREATE_AREAS_TABLE);
		db.execSQL(CREATE_AREA_VALUES_TABLE);
		db.execSQL(CREATE_COMMFLOWS_TABLE);
		db.execSQL(CREATE_SCREENTIME_TABLE);
		db.execSQL(CREATE_STRESSLEVELS_TABLE);
		db.execSQL(CREATE_HEARTRATE_TABLE);
		db.execSQL(CREATE_APPTIME_TABLE);
		db.execSQL(CREATE_AREAHISTORY_TABLE);
		db.execSQL(CREATE_ACTIVITIES_TABLE);
		db.execSQL(CREATE_BADGES_TABLE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		switch (newVersion) {
		case 2:
			Log.i(TAG, "UPGRADING DATABASE FROM: " + oldVersion + " TO : " + newVersion);
			Log.i(TAG, CREATE_ACTIVITIES_TABLE);
			db.execSQL(CREATE_ACTIVITIES_TABLE);
			break;
			
		case 3:
			Log.i(TAG, "UPGRADING DATABASE FROM: " + oldVersion + " TO : " + newVersion);
			switch (oldVersion) {
			case 1:
				Log.i(TAG, CREATE_ACTIVITIES_TABLE);
				db.execSQL(CREATE_ACTIVITIES_TABLE);
				
				Log.i(TAG, CREATE_BADGES_TABLE);
				db.execSQL(CREATE_BADGES_TABLE);
				break;
			case 2:
				Log.i(TAG, CREATE_BADGES_TABLE);
				db.execSQL(CREATE_BADGES_TABLE);
				break;
			}
		}
	}
	
	/**
	 * Adds a new date recorded by a heartrate sensor to the database
	 * @param calendar The current date/time
	 * @param heartrate The heartrate
	 * @param speed The speed the user is probably moving at
	 */
	public void addHeartrateData(GregorianCalendar calendar, int heartrate, double speed) {
		SQLiteDatabase db = this.getWritableDatabase();
		String insert = "INSERT OR IGNORE INTO " + TABLE_HEARTRATE + " (" + KEY_HEARTRATE_DATE + "," + KEY_HEARTRATE_HEARTRATE + "," + KEY_HEARTRATE_SPEED + ") VALUES ('" + calendar.getTimeInMillis() + "', '" + heartrate + "', '" + speed + "')";
		db.execSQL(insert);
		Log.i(TAG, insert);
	}

	/**
	 * Gives you the heartrate and speed data of a certain point in time
	 * @param calendar The point in time you are looking for
	 * @return The heartrate and speed separated by a colon. E.g. 70;5
	 */
	public String getHeartrateData (GregorianCalendar calendar) {
		SQLiteDatabase db = this.getReadableDatabase();
		String result = "";
		Cursor cursor = db.query(TABLE_HEARTRATE, new String[] {KEY_HEARTRATE_HEARTRATE, KEY_HEARTRATE_SPEED}, KEY_HEARTRATE_DATE + "=?",
				new String[] {String.valueOf(calendar.getTimeInMillis())}, null, null, null, null);
		if(cursor.moveToFirst()) {
			int heartrate, speed;
			heartrate = cursor.getInt(0);
			speed = cursor.getInt(1);
			result = String.valueOf(heartrate) + ";" + String.valueOf(speed);
			Log.i(TAG, "Retrieved Heartrate+Speed: " + result);
		}
		else {
			Log.i(TAG, "No heartrate data found for date: " + Util.getFormattedDate(calendar.getTimeInMillis()));
		}
		cursor.close();
		
		return result;
	}
    /**
     * Adds a new area to the database
     * @param name The name of the area
     */
	public void addArea(String name) {
		SQLiteDatabase db = this.getWritableDatabase();
		String insert = "INSERT OR IGNORE INTO " + TABLE_AREAS + " (" + KEY_AREAS_NAME + ") VALUES ('" + name + "')";
		db.execSQL(insert);
		Log.i(TAG, insert);
	}

    /**
     * Gives you the name of an area for the given areaID
     * @param id The ID to look for
     * @return The name of the area. "FAIL" on no result
     */
	public String getArea(int id) {
		SQLiteDatabase db = this.getReadableDatabase();
		String result = "FAIL";
		Cursor cursor = db.query(TABLE_AREAS, new String[] {KEY_AREAS_ID, KEY_AREAS_NAME}, KEY_AREAS_ID + "=?",
				new String[] {String.valueOf(id) }, null, null, null, null);
		if(cursor.moveToFirst()) {
			result = cursor.getString(1);
			Log.i(TAG, "Retrieved Area with ID " + id + " named " + result + " from database");
		}
		else {
			Log.i(TAG, "No Area found to given id: " + id);
		}
		cursor.close();
		
		return result;
	}

    /**
     * Returns all areas that are present in the database
     * @return The list of areas
     */
	public ArrayList<String> getAllAreas() {
		SQLiteDatabase db = this.getReadableDatabase();
		ArrayList<String> result = new ArrayList<String>();
		
		Cursor cursor = db.query(TABLE_AREAS, new String[] {KEY_AREAS_ID, KEY_AREAS_NAME}, null,
				null, null, null, null, null);
		while(cursor.moveToNext()) {
			int id = cursor.getInt(0);
			String name = cursor.getString(1);
			result.add(name);
			Log.i(TAG, "Retrieved Area with ID " + id + " named " + name + " from database");
		}
		cursor.close();
		
		return result;
	}
	
	/**
     * Returns all real areas we care for i.e. without "Unbekannt" or so
     * @return The list of areas
     */
	public ArrayList<String> getRealAreas() {
		SQLiteDatabase db = this.getReadableDatabase();
		ArrayList<String> result = new ArrayList<String>();
		Cursor cursor = db.query(TABLE_AREAS, new String[] {KEY_AREAS_ID, KEY_AREAS_NAME}, KEY_AREAS_ID + "!=1",
				null, null, null, null, null);
		while(cursor.moveToNext()) {
			int id = cursor.getInt(0);
			String name = cursor.getString(1);
			result.add(name);
			Log.i(TAG, "Retrieved Area with ID " + id + " named " + name + " from database");
		}
		cursor.close();
		
		return result;
	}

    /**
     * Adds a new cell to an area. If this cell has been in another area, it is just changed to the new areaId
     * @param cellId The String of the cell
     * @param id The ID of the area
     * @param replace Must be set if a location should be forced to be assigned to an areaId, so that it may replace an already existing assignment
     */
	public void addAreaValue(String cellId, int areaId, boolean replace) {
		SQLiteDatabase db = this.getWritableDatabase();
		String insert = "INSERT OR IGNORE INTO " + TABLE_AREA_VALUES + " (" + KEY_VALUES_LOCATIONSTRING + "," + KEY_VALUES_AREAID
				+ ") VALUES ('" + cellId + "', " + areaId +")";
		Log.i(TAG, insert);
		db.execSQL(insert);

		if (replace) {
			String update = "UPDATE " + TABLE_AREA_VALUES + " SET " + KEY_VALUES_AREAID + "=" + areaId
					+ " WHERE " + KEY_VALUES_LOCATIONSTRING + "='" + cellId + "'";
			Log.i(TAG, update);
			db.execSQL(update);
		}
	}
	
	/**
	 * Deletes the assignment area <-> cell from the database. The id of the area "Unbekannt" is automatically set (1).
     * @param locationString The cell to be removed. Can only be attached to one area.
     */
	public void deleteAreaValue (String locationString) {
		SQLiteDatabase db = this.getWritableDatabase();
		String update = "UPDATE " + TABLE_AREA_VALUES + " SET " + KEY_VALUES_AREAID + "=1"
				+ " WHERE " + KEY_VALUES_LOCATIONSTRING + "='" + locationString + "'";
		Log.i(TAG, update);
		db.execSQL(update);
		Log.i(TAG, "Cell " + locationString + " deleted from its area. Set to \"Unbekannt\"");
	}

    /**
     * Deletes a certain area from the database
     * @param id The ID of the area
     */
	public void deleteArea (int id) {
		if (id == 0) {
			Log.i(TAG, "Couldn't delete Area, ID was 0");
			return;
		}
		SQLiteDatabase db = this.getWritableDatabase();
		db.delete(TABLE_AREAS, KEY_AREAS_ID + "=" + id, null);
		Log.i(TAG, "Area Row with id " + id + " deleted");
	}

    /**
     * Gives you the ID of an area string
     * @param area The name of the area
     * @return The ID of this area
     */
	public int getAreaId(String area) {
		SQLiteDatabase db = this.getReadableDatabase();
		int result = 0;
		Cursor cursor = db.query(TABLE_AREAS, new String[] {KEY_AREAS_ID, KEY_AREAS_NAME}, KEY_AREAS_NAME + "=?",
				new String[] {area}, null, null, null, null);
		if(cursor.moveToFirst()) {
			result = cursor.getInt(0);
			Log.i(TAG, "Retrieved Area with ID " + result + " named " + area + " from database");
		}
		cursor.close();
		
		return result;
	}

    /**
     * Gives you the area a given cell(locationString of it) belongs to
     * @param locationString The location to look for
     * @return The areas the cell belongs to. "FAIL" on no result. Should not happen
     */
	public String getAreaNameByLocation(String locationString) {
		SQLiteDatabase db = this.getReadableDatabase();
		String result = "FAIL";
		Cursor cursor = db.query(TABLE_AREA_VALUES, new String[] {KEY_VALUES_LOCATIONSTRING, KEY_VALUES_AREAID}, KEY_VALUES_LOCATIONSTRING + "=?",
				new String[] {locationString}, null, null, null, null);
		if(cursor.moveToFirst()) {
			result = getArea(cursor.getInt(1));
			Log.i(TAG, "CellId " + locationString + "belongs to " + result);
		}
		cursor.close();
		
		return result;
	}
	
	/**
	 * Returns an ArrayList of Cells belonging to the area with the given area-id
	 * @param areaID	the id of the area whose cells shall be returned
	 * @return			the cells belonging to the area with the given area-id
	 */
	public ArrayList<String> getCellsForArea(int areaID) {
		SQLiteDatabase db = this.getReadableDatabase();
		ArrayList<String> result = new ArrayList<String>();
		Cursor cursor = db.query(TABLE_AREA_VALUES, new String[] {KEY_VALUES_LOCATIONSTRING, KEY_VALUES_AREAID}, KEY_VALUES_AREAID + "=?",
				new String[] {String.valueOf(areaID)}, null, null, null, null);
		while (cursor.moveToNext()) {
			result.add(cursor.getString(0));
		}
		String debug = "";
		for (String cell : result){
			debug = debug + cell + " ";
		}
		Log.i(TAG, "Area " + areaID + " has cells: " + debug);
		cursor.close();
		return result;
	}
	
	/**
	 * Adds a single communication flow to the database
	 * @param medium The communication medium
	 * @param contactHash Hash value of the contact
	 * @param d Date of the communication flow
	 * @param duration Duration of a call
	 * @param important Whether the communication partner is important or not
	 * @param area Area this communication happened
	 * @param atBody Whether the phone was at the body
	 */
	public void addCommFlow (String medium, String contactHash, Date d, String duration, boolean important, String area, boolean atBody) {
		SQLiteDatabase db = this.getWritableDatabase();
		String contact, dur;
		long date = d.getTime();
		if (contactHash == null) {
			contact = "";
		}
		else {
			contact = contactHash;
		}
		
		if (duration == null) {
			dur = "";
		}
		else {
			dur = duration;
		}
		String insert = "INSERT INTO " + TABLE_COMMFLOWS + " (" + KEY_COMMFLOWS_MEDIUM + "," + KEY_COMMFLOWS_CONTACT + "," + KEY_COMMFLOWS_DATE
				+ "," + KEY_COMMFLOWS_DURATION + "," + KEY_COMMFLOWS_IMPORTANT + "," + KEY_COMMFLOWS_AREA + "," + KEY_COMMFLOWS_BODY
				+ ") VALUES ('" + medium + "', '" + contact + "', '" + date + "', '" + dur + "', '" + Boolean.toString(important) + "', '" + area + "', '" + Boolean.toString(atBody) +"')";
		Log.i(TAG, insert);
		db.execSQL(insert);
	}
	
	/**
	 * Method to get all communication flows.
	 * @param calendar Date of the day requested
	 * @param area Specific area? if not, put ""
	 * @param span Timespan in days or 0 for all time
	 * @return long[3] containing: Number of all commflows;important ones;unimportant ones
	 */
	public long[] getCommFlows (GregorianCalendar calendar, String area, int span) {

		Log.i(TAG, "Database CommFlow request:");
		Log.i(TAG, "Date: " + Util.getFormattedDate(calendar.getTimeInMillis()));
		Log.i(TAG, "Area: " + area);
		Log.i(TAG, "Span: " + span + " day(s)");
		SQLiteDatabase db = this.getReadableDatabase();
		long date = calendar.getTimeInMillis();
		long fromDate = 0;
		long important, all;
		if (span == 0) {
			if (area.equals("")){
				important = DatabaseUtils.queryNumEntries(db, TABLE_COMMFLOWS,
		                "important=? and date<? and medium!=? and medium!=?", new String[] {"true", Long.toString(date), "ScreenOff", "ScreenOn"});
				all = DatabaseUtils.queryNumEntries(db, TABLE_COMMFLOWS,
						"date<? and medium!=? and medium!=?", new String[] {Long.toString(date), "ScreenOff", "ScreenOn"});
			}
			else {
				important = DatabaseUtils.queryNumEntries(db, TABLE_COMMFLOWS,
		                "important=? and date<? and area=? and medium!=? and medium!=?", new String[] {"true", Long.toString(date), area, "ScreenOff", "ScreenOn"});
				all = DatabaseUtils.queryNumEntries(db, TABLE_COMMFLOWS,
						"date<? and area=? and medium!=? and medium!=?", new String[] {Long.toString(date), area, "ScreenOff", "ScreenOn"});
			}
		}
		else {
			calendar.add(Calendar.DATE, span * -1);
			fromDate = calendar.getTimeInMillis();
			if (area.equals("")){
				important = DatabaseUtils.queryNumEntries(db, TABLE_COMMFLOWS,
		                "important=? and date<? and date>? and medium!=? and medium!=?", new String[] {"true", Long.toString(date), Long.toString(fromDate), "ScreenOff", "ScreenOn"});
				all = DatabaseUtils.queryNumEntries(db, TABLE_COMMFLOWS,
						"date<? and date>? and medium!=? and medium!=?", new String[] {Long.toString(date), Long.toString(fromDate), "ScreenOff", "ScreenOn"});
			}
			else {
				important = DatabaseUtils.queryNumEntries(db, TABLE_COMMFLOWS,
		                "important=? and date<? and date>? and area=? and medium!=? and medium!=?", new String[] {"true", Long.toString(date), Long.toString(fromDate), area, "ScreenOff", "ScreenOn"});
				all = DatabaseUtils.queryNumEntries(db, TABLE_COMMFLOWS,
						"date<? and date>? and area=? and medium!=? and medium!=?", new String[] {Long.toString(date), Long.toString(fromDate), area, "ScreenOff", "ScreenOn"});
			}
			calendar.add(Calendar.DATE, span);
		}
		
		long unimportant = all - important;
		
		Log.i(TAG, "Found " + all + " communication flows. " + important + " are important and " + unimportant + " are unimportant");
		Log.i(TAG, "From: " + Util.getFormattedDate(fromDate));
		Log.i(TAG, "To: " + Util.getFormattedDate(date));
		long[] result = new long[3];
		result[0] = all;
		result[1] = important;
		result[2] = unimportant;
		return result;
	}
	
	/**
	 * WhatsApp, Threema, Hangout, Telegram, SMS
     * @param calendar Date of the day requested
     * @param area Specific area? if not, put ""
     * @param span Timespan in days or 0 for all time
     * @return long[3] containing: Number of all commflows;important ones;unimportant ones
	 */
	public long[] getMessages (GregorianCalendar calendar, String area, int span) {
		Log.i(TAG, "Database messages request:");
		Log.i(TAG, "Date: " + Util.getFormattedDate(calendar.getTimeInMillis()));
		Log.i(TAG, "Area: " + area);
		Log.i(TAG, "Span: " + span + " day(s)");
		SQLiteDatabase db = this.getReadableDatabase();
		long date = calendar.getTimeInMillis();
		long fromDate = 0;
		long important, all;
		if (span == 0) {
			if (area.equals("")){
				important = DatabaseUtils.queryNumEntries(db, TABLE_COMMFLOWS,
		                "important=? and date<? and (medium=? or medium=? or medium=? or medium=? or medium=? or medium=? or medium=?)", new String[] {"true", Long.toString(date), "Whatsapp", "Threema", "Hangout", "Telegram", "SMS", "Skype", "Facebook"});
				all = DatabaseUtils.queryNumEntries(db, TABLE_COMMFLOWS,
						"date<? and (medium=? or medium=? or medium=? or medium=? or medium=? or medium=? or medium=?)", new String[] {Long.toString(date), "Whatsapp", "Threema", "Hangout", "Telegram", "SMS", "Skype", "Facebook"});
			}
			else {
				important = DatabaseUtils.queryNumEntries(db, TABLE_COMMFLOWS,
		                "important=? and date<? and area=? and (medium=? or medium=? or medium=? or medium=? or medium=? or medium=? or medium=?)", new String[] {"true", Long.toString(date), area, "Whatsapp", "Threema", "Hangout", "Telegram", "SMS", "Skype", "Facebook"});
				all = DatabaseUtils.queryNumEntries(db, TABLE_COMMFLOWS,
						"date<? and area=? and (medium=? or medium=? or medium=? or medium=? or medium=? or medium=? or medium=?)", new String[] {Long.toString(date), area, "Whatsapp", "Threema", "Hangout", "Telegram", "SMS", "Skype", "Facebook"});
			}
		}
		else {
			calendar.add(Calendar.DATE, span * -1);
			fromDate = calendar.getTimeInMillis();
			if (area.equals("")){
				important = DatabaseUtils.queryNumEntries(db, TABLE_COMMFLOWS,
		                "important=? and date<? and date>? and (medium=? or medium=? or medium=? or medium=? or medium=? or medium=? or medium=?)", new String[] {"true", Long.toString(date), Long.toString(fromDate), "Whatsapp", "Threema", "Hangout", "Telegram", "SMS", "Skype", "Facebook"});
				all = DatabaseUtils.queryNumEntries(db, TABLE_COMMFLOWS,
						"date<? and date>? and (medium=? or medium=? or medium=? or medium=? or medium=? or medium=? or medium=?)", new String[] {Long.toString(date), Long.toString(fromDate), "Whatsapp", "Threema", "Hangout", "Telegram", "SMS", "Skype", "Facebook"});
			}
			else {
				important = DatabaseUtils.queryNumEntries(db, TABLE_COMMFLOWS,
		                "important=? and date<? and date>? and area=? and (medium=? or medium=? or medium=? or medium=? or medium=? or medium=? or medium=?)", new String[] {"true", Long.toString(date), Long.toString(fromDate), area, "Whatsapp", "Threema", "Hangout", "Telegram", "SMS", "Skype", "Facebook"});
				all = DatabaseUtils.queryNumEntries(db, TABLE_COMMFLOWS,
						"date<? and date>? and area=? and (medium=? or medium=? or medium=? or medium=? or medium=? or medium=? or medium=?)", new String[] {Long.toString(date), Long.toString(fromDate), area, "Whatsapp", "Threema", "Hangout", "Telegram", "SMS", "Skype", "Facebook"});
			}
			calendar.add(Calendar.DATE, span);
		}
		
		long unimportant = all - important;
		
		Log.i(TAG, "Found " + all + " Messenger communication flows. " + important + " are important and " + unimportant + " are unimportant");
		Log.i(TAG, "From: " + Util.getFormattedDate(fromDate));
		Log.i(TAG, "To: " + Util.getFormattedDate(date));
		long[] result = new long[3];
		result[0] = all;
		result[1] = important;
		result[2] = unimportant;
		return result;
	}
	
	/**
	 * Screen On/Off events
     * @param calendar Date of the day requested
     * @param area Specific area? if not, put ""
     * @param span Timespan in days or 0 for all time
     * @return long[3] containing: Number of all commflows;important ones;unimportant ones
	 */
	public String getScreen (GregorianCalendar calendar, String area, int span) {
		Log.i(TAG, "Database screenOn/Off request:");
		Log.i(TAG, "Date: " + Util.getFormattedDate(calendar.getTimeInMillis()));
		Log.i(TAG, "Area: " + area);
		Log.i(TAG, "Span: " + span + " day(s)");
		SQLiteDatabase db = this.getReadableDatabase();
		long date = calendar.getTimeInMillis();
		long fromDate = 0;
		long all;
		if (span == 0) {
			if (area.equals("")){
				all = DatabaseUtils.queryNumEntries(db, TABLE_COMMFLOWS,
						"date<? and (medium=? or medium=?)", new String[] {Long.toString(date), "ScreenOff", "ScreenOn"});
			}
			else {
				all = DatabaseUtils.queryNumEntries(db, TABLE_COMMFLOWS,
						"date<? and area=? and (medium=? or medium=?)", new String[] {Long.toString(date), area, "ScreenOff", "ScreenOn"});
			}
		}
		else {
			calendar.add(Calendar.DATE, span * -1);
			fromDate = calendar.getTimeInMillis();
			if (area.equals("")){
				all = DatabaseUtils.queryNumEntries(db, TABLE_COMMFLOWS,
						"date<? and date>? and (medium=? or medium=?)", new String[] {Long.toString(date), Long.toString(fromDate), "ScreenOff", "ScreenOn"});
			}
			else {
				all = DatabaseUtils.queryNumEntries(db, TABLE_COMMFLOWS,
						"date<? and date>? and area=? and (medium=? or medium=?)", new String[] {Long.toString(date), Long.toString(fromDate), area, "ScreenOff", "ScreenOn"});
			}
			calendar.add(Calendar.DATE, span);
		}
		
		String result;
		
		Log.i(TAG, "Found " + all + " Screen On/Off events");
		Log.i(TAG, "From: " + Util.getFormattedDate(fromDate));
		Log.i(TAG, "To: " + Util.getFormattedDate(date));
		result = String.valueOf(all);
		return result;
	}
	
	/**
	 * Returns the number of calls
     * @param calendar Date of the day requested
     * @param area Specific area? if not, put ""
     * @param span Timespan in days or 0 for all time
     * @return long[3] containing: Number of all commflows;important ones;unimportant ones
	 */
	public long[] getCalls (GregorianCalendar calendar, String area, int span) {
		Log.i(TAG, "Database calls request:");
		Log.i(TAG, "Date: " + Util.getFormattedDate(calendar.getTimeInMillis()));
		Log.i(TAG, "Area: " + area);
		Log.i(TAG, "Span: " + span + " day(s)");
		SQLiteDatabase db = this.getReadableDatabase();
		long date = calendar.getTimeInMillis();
		long fromDate = 0;
		long important, all;
		if (span == 0) {
			if (area.equals("")){
				important = DatabaseUtils.queryNumEntries(db, TABLE_COMMFLOWS,
		                "important=? and date<? and medium=?", new String[] {"true", Long.toString(date), "Call"});
				all = DatabaseUtils.queryNumEntries(db, TABLE_COMMFLOWS,
						"date<? and medium=?", new String[] {Long.toString(date), "Call"});
			}
			else {
				important = DatabaseUtils.queryNumEntries(db, TABLE_COMMFLOWS,
		                "important=? and date<? and area=? and medium=?", new String[] {"true", Long.toString(date), area, "Call"});
				all = DatabaseUtils.queryNumEntries(db, TABLE_COMMFLOWS,
						"date<? and area=? and medium=?", new String[] {Long.toString(date), area, "Call"});
			}
		}
		else {
			calendar.add(Calendar.DATE, span * -1);
			fromDate = calendar.getTimeInMillis();
			if (area.equals("")){
				important = DatabaseUtils.queryNumEntries(db, TABLE_COMMFLOWS,
		                "important=? and date<? and date>? and medium=?", new String[] {"true", Long.toString(date), Long.toString(fromDate), "Call"});
				all = DatabaseUtils.queryNumEntries(db, TABLE_COMMFLOWS,
						"date<? and date>? and medium=?", new String[] {Long.toString(date), Long.toString(fromDate), "Call"});
			}
			else {
				important = DatabaseUtils.queryNumEntries(db, TABLE_COMMFLOWS,
		                "important=? and date<? and date>? and area=? and medium=?", new String[] {"true", Long.toString(date), Long.toString(fromDate), area, "Call"});
				all = DatabaseUtils.queryNumEntries(db, TABLE_COMMFLOWS,
						"date<? and date>? and area=? and medium=?", new String[] {Long.toString(date), Long.toString(fromDate), area, "Call"});
			}
			calendar.add(Calendar.DATE, span);
		}
		
		long unimportant = all - important;
		
		Log.i(TAG, "Found " + all + " Calls. " + important + " are important and " + unimportant + " are unimportant");
		Log.i(TAG, "From: " + Util.getFormattedDate(fromDate));
		Log.i(TAG, "To: " + Util.getFormattedDate(date));
		long[] result = new long[3];
		result[0] = all;
		result[1] = important;
		result[2] = unimportant;
		return result;
	}
	
	/**
	 * Gives the number of mails
     * @param calendar Date of the day requested
     * @param area Specific area? if not, put ""
     * @param span Timespan in days or 0 for all time
     * @return long[3] containing: Number of all commflows;important ones;unimportant ones
	 */
	public long[] getMails (GregorianCalendar calendar, String area, int span) {
		Log.i(TAG, "Database mails request:");
		Log.i(TAG, "Date: " + Util.getFormattedDate(calendar.getTimeInMillis()));
		Log.i(TAG, "Area: " + area);
		Log.i(TAG, "Span: " + span + " day(s)");
		SQLiteDatabase db = this.getReadableDatabase();
		long date = calendar.getTimeInMillis();
		long fromDate = 0;
		long important, all;
		if (span == 0) {
			if (area.equals("")){
				important = DatabaseUtils.queryNumEntries(db, TABLE_COMMFLOWS,
		                "important=? and date<? and medium=?", new String[] {"true", Long.toString(date), "Mail"});
				all = DatabaseUtils.queryNumEntries(db, TABLE_COMMFLOWS,
						"date<? and medium=?", new String[] {Long.toString(date), "Mail"});
			}
			else {
				important = DatabaseUtils.queryNumEntries(db, TABLE_COMMFLOWS,
		                "important=? and date<? and area=? and medium=?", new String[] {"true", Long.toString(date), area, "Mail"});
				all = DatabaseUtils.queryNumEntries(db, TABLE_COMMFLOWS,
						"date<? and area=? and medium=?", new String[] {Long.toString(date), area, "Mail"});
			}
		}
		else {
			calendar.add(Calendar.DATE, span * -1);
			fromDate = calendar.getTimeInMillis();
			if (area.equals("")){
				important = DatabaseUtils.queryNumEntries(db, TABLE_COMMFLOWS,
		                "important=? and date<? and date>? and medium=?", new String[] {"true", Long.toString(date), Long.toString(fromDate), "Mail"});
				all = DatabaseUtils.queryNumEntries(db, TABLE_COMMFLOWS,
						"date<? and date>? and medium=?", new String[] {Long.toString(date), Long.toString(fromDate), "Mail"});
			}
			else {
				important = DatabaseUtils.queryNumEntries(db, TABLE_COMMFLOWS,
		                "important=? and date<? and date>? and area=? and medium=?", new String[] {"true", Long.toString(date), Long.toString(fromDate), area, "Mail"});
				all = DatabaseUtils.queryNumEntries(db, TABLE_COMMFLOWS,
						"date<? and date>? and area=? and medium=?", new String[] {Long.toString(date), Long.toString(fromDate), area, "Mail"});
			}
			calendar.add(Calendar.DATE, span);
		}
		
		long unimportant = all - important;
		
		Log.i(TAG, "Found " + all + " Mails. " + important + " are important and " + unimportant + " are unimportant");
		Log.i(TAG, "From: " + Util.getFormattedDate(fromDate));
		Log.i(TAG, "To: " + Util.getFormattedDate(date));
		long[] result = new long[3];
		result[0] = all;
		result[1] = important;
		result[2] = unimportant;
		return result;
	}
	
	/**
	 * Adds screentime for a given datetime to the database
	 * @param calendar The datetime you are assigning the screentime. Minute, second and milliseconds will automatically be set to zero, since screentime is saved per hour.
	 * @param time The actual screentime in milliseconds
	 */
	public void addScreenTime (GregorianCalendar calendar, long time) {
		SQLiteDatabase db = this.getWritableDatabase();
		
		calendar.set(Calendar.MILLISECOND, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MINUTE, 0);
		long date = calendar.getTimeInMillis();
		String insert = "INSERT OR REPLACE INTO " + TABLE_SCREENTIME + " (" + KEY_SCREENTIME_DATE + "," + KEY_SCREENTIME_TIME + ")" +
				" VALUES ('" + date + "', '" + time + "')";
		Log.i(TAG, insert);
		db.execSQL(insert);
	}
	
	/**
     * Gives you the screentime for a given day's hour
	 * @param calendar The date of the requested day+hour
	 * E.g. GregorianCalendar calendar = new GregorianCalendar(2014, 8, 7, 23, 0);
	 * Minute, second and milliseconds will automatically be set to zero, since screentime is saved per hour.
	 * @return The screentime in seconds
	 */
	public int getScreenTimeOfHour (GregorianCalendar calendar) {
		SQLiteDatabase db = this.getReadableDatabase();
		int result = 0;
		Cursor cursor;
		calendar.set(Calendar.MILLISECOND, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MINUTE, 0);
		long date = calendar.getTimeInMillis();
		cursor = db.query(TABLE_SCREENTIME, new String[] {KEY_SCREENTIME_DATE, KEY_SCREENTIME_TIME}, KEY_SCREENTIME_DATE + "=?",
				new String[] {String.valueOf(date) }, null, null, null, null);
		if(cursor.moveToFirst()) {
			result = cursor.getInt(1);
			Log.i(TAG, "Retrieved Screentime of Date " + Util.getFormattedDate(calendar.getTimeInMillis()) + ": " + result);
		}
		else {
			result = 0;
			Log.i(TAG, "No results found for date: " + Util.getFormattedDate(calendar.getTimeInMillis()));
		}
		cursor.close();
		
		return result;
	}
	
	/**
	 * Gives you all screentime since installation
	 * @return The screentime in seconds
	 */
	public int getAllScreenTime() {
		SQLiteDatabase db = this.getReadableDatabase();
		int result = 0;
		Cursor cursor;
		cursor = db.rawQuery("SELECT SUM(" + KEY_SCREENTIME_TIME + ") FROM " + TABLE_SCREENTIME, null);
		if(cursor.moveToFirst()) {
			result = cursor.getInt(0);
			Log.i(TAG, "Retrieved complete Screentime: " + result);
		}
		cursor.close();
		return result;
	}
	
	/**
	 * Gives you the screentime for a given day
	 * @param calendar The date of the requested day. Provide a GregorianCalendar day.
	 * Hour, minute, seconds and milliseconds will be automatically set to zero. Remember month january has to be 0...december 11.
	 * @return The screentime in seconds
	 */
	public int getScreenTimeOfDay (GregorianCalendar day) {
		SQLiteDatabase db = this.getReadableDatabase();
		int result = 0;
		Cursor cursor;
		day.set(Calendar.MILLISECOND, 0);
		day.set(Calendar.SECOND, 0);
		day.set(Calendar.MINUTE, 0);
		day.set(Calendar.HOUR_OF_DAY, 0);

        long date = day.getTimeInMillis();
		cursor = db.rawQuery("SELECT SUM(" + KEY_SCREENTIME_TIME + ") FROM " + TABLE_SCREENTIME + " WHERE " + KEY_SCREENTIME_DATE
				+ ">? AND " + KEY_SCREENTIME_DATE + "<?", new String[] {String.valueOf(date), String.valueOf(date + TimeUnit.MILLISECONDS.convert(1, TimeUnit.DAYS))});
		if(cursor.moveToFirst()) {
			result = cursor.getInt(0);
			Log.i(TAG, "Retrieved day Screentime of " + Util.getFormattedDate(day.getTimeInMillis()) + ": " + result);
		}
		cursor.close();
		
		return result;
	}


	/**
     * Saves the chosen stresslevel to the database. Time needs to be provided because if we take current time, it may be much later than the chosen time(e.g. 9,19)
     * @param stresslevel The stresslevel encoded as integer, currently 1-5(1=good, 5=not good)
	 * @param hour The hour the notification was shown/user was asked
	 * @param minute The minute the notification was shown/user was asked
	 * @param day The day the notification was shown/user was asked
	 * @param now The real date the stresslevel was chosen by the user
	 * @param time The time of day i.e. early or late
	 */
    public void saveNotificationStressLevel(int stresslevel, int hour, int minute, int day, GregorianCalendar now, String time) {
        GregorianCalendar calendar = new GregorianCalendar();
        calendar.set(Calendar.DAY_OF_YEAR, day);
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        SQLiteDatabase db = this.getWritableDatabase();

        long date = calendar.getTimeInMillis();
        String insert = "INSERT OR REPLACE INTO " + TABLE_STRESSLEVELS + " (" + KEY_STRESSLEVELS_NOTIFICATIONDATE + "," + KEY_STRESSLEVELS_STRESS + "," + KEY_STRESSLEVELS_REALDATE + "," + KEY_STRESSLEVELS_TIMEOFDAY + ")" +
                " VALUES ('" + date + "', '" + stresslevel + "', '" + now.getTimeInMillis() + "', '" + time + "')";
        Log.i(TAG, insert);
        db.execSQL(insert);
        Log.i(TAG, "Stress level asked Day-Hour-Minute: " +  Util.getFormattedDate(calendar.getTimeInMillis()));
        Log.i(TAG, "Stress level answered Day-Hour-Minute: " + Util.getFormattedDate(now.getTimeInMillis()));
    }

    /**
     * Retrieves the average stresslevel of a given day. The date where the user was asked is used and not when he actually answered.
     * @param day The requested day. Provide a GregorianCalendar day
     * Hour, minute, seconds and milliseconds will be automatically set to zero. Remember month january has to be 0...december 11.
     * @return The average of the stresslevel of a day (rounded down). 1 is great, 5 is bad
     */
    public int getNotificationStressLevelOfDay(GregorianCalendar day) {
        int result = 0;

        day.set(Calendar.HOUR_OF_DAY, 0);
		day.set(Calendar.MINUTE, 0);
		day.set(Calendar.SECOND, 0);
		day.set(Calendar.MILLISECOND, 0);

        long date = day.getTimeInMillis();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor;
        // all values of a day from 0 o'clock to the next day 0 o'clock, represented by the addition of 1 day as millis
        cursor = db.rawQuery("SELECT AVG(" + KEY_STRESSLEVELS_STRESS + ") FROM " + TABLE_STRESSLEVELS + " WHERE " + KEY_STRESSLEVELS_NOTIFICATIONDATE
                + " > ? AND " + KEY_STRESSLEVELS_NOTIFICATIONDATE + " < ?", new String[] {String.valueOf(date), String.valueOf(date + TimeUnit.MILLISECONDS.convert(1, TimeUnit.DAYS))});
        if(cursor.moveToFirst()) {
            result = (int)Math.floor(cursor.getDouble(0));
            Log.i(TAG, "Retrieved average stresslevel of day " + Util.getFormattedDate(day.getTimeInMillis()) + ": " + result);
        }
        cursor.close();
        
        return result;
    }
    
    /**
     * This method gives you the calculated stress values based on measured heart rate
     * @param day The day you want to know the values of
     * @return Returns a linked hashmap assigning stress values to 10 minute intervals from 00:00 to 24:00 of a day.
     * Key is a time as String e.g. 22-40 22-50 23-0 etc and the values are the stress values
     * The first is from 00:00 to 00:10, second from 00:10 to 00:20 etc. The list's length is always 24*6. 24 hours a day times 6 10-minute intervals.
     * Values should be bigger than 0. If you get 0.0 as a value it means no actual stress value for this time. Handle it how you need it.
     */
    public LinkedHashMap<String, Double> getHeartrateStressLevelOfDay (GregorianCalendar day) {
    	int numberQueries = 6*24;
    	LinkedHashMap<String, Double> result = new LinkedHashMap<String, Double>();
    	day.set(Calendar.HOUR_OF_DAY, 0);
		day.set(Calendar.MINUTE, 0);
		day.set(Calendar.SECOND, 0);
		day.set(Calendar.MILLISECOND, 0);
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor;
        int maxHeartrate;
        cursor = db.query(TABLE_HEARTRATE, new String[] {"MAX(" + KEY_HEARTRATE_HEARTRATE + ")"}, KEY_HEARTRATE_DATE + ">=? AND " + KEY_HEARTRATE_DATE + "<?",
				new String[] {String.valueOf(day.getTimeInMillis()), String.valueOf(day.getTimeInMillis() + TimeUnit.MILLISECONDS.convert(1, TimeUnit.DAYS))}, null, null, null);
        if (cursor.moveToFirst()) {
        	maxHeartrate = cursor.getInt(0);
        }
        else {
        	maxHeartrate = Integer.MAX_VALUE;
        }
        if (maxHeartrate != 0) {
        	Log.i(TAG, "Max heartrate found: " + maxHeartrate);
            for (int i = 0; i < numberQueries; i++) {
            	long date = day.getTimeInMillis();
            	cursor = db.rawQuery("SELECT AVG(" + KEY_HEARTRATE_HEARTRATE + ") FROM " + TABLE_HEARTRATE + " WHERE " + KEY_HEARTRATE_DATE
                        + " >= ? AND " + KEY_HEARTRATE_DATE + " < ?", new String[] {String.valueOf(date), String.valueOf(date + STRESS_CALCULATION_INTERVAL_MILLIS)});
                if(cursor.moveToFirst()) {
                    double stress = calculateStress(cursor.getDouble(0), maxHeartrate, date);
                    result.put(day.get(Calendar.HOUR_OF_DAY) + "-" + day.get(Calendar.MINUTE), stress);
                    Log.i(TAG, "Calculated average stresslevel of 10 minutes: " + Util.getFormattedDate(day.getTimeInMillis()) + ": " + stress);
                }
                cursor.close();
                day.add(Calendar.MINUTE, 10);
            }
        }
        else {
        	Log.i(TAG, "No max heartrate found => no values of this day, adding 0 for whole day");
        	for (int i = 0; i < numberQueries; i++) {
        		result.put(day.get(Calendar.HOUR_OF_DAY) + "-" + day.get(Calendar.MINUTE), 0.0);
        		day.add(Calendar.MINUTE, 10);
        	}
        }
        
    	return result;
    }
    
    private double calculateStress (double averageHeartrate, int maxHeartrate, long date) {
    	HashMap<String, Integer> activities = getActivities(date);
    	/*for (String activity: activities.keySet()) {
    		
    	}*/
    	double result;
    	Log.i(TAG, "Calculating stress...");
    	Log.i(TAG, "Average heartrate: " + averageHeartrate + " and max heartrate: " + maxHeartrate);
    	result = 5 * averageHeartrate / maxHeartrate;
    	Log.i(TAG, "Stress result: " + result);
    	return result;
    }
    
    /**
     * Gives you the times the user was asked about his stress level for a given day
     * @param day The requested day. Provide a GregorianCalendar day
     * Hour, minute, seconds and milliseconds will be automatically set to zero. Remember month january has to be 0...december 11.
     * @return A HashMap containing at max 2 values for the keys "early" and "late". The fields may not be present yet so check for it (containsKey())
     */
    public HashMap<String, Long> getStressNotificationTimeAsked(GregorianCalendar day) {
    	HashMap<String, Long> result = new HashMap<String, Long>();

        day.set(Calendar.HOUR_OF_DAY, 0);
		day.set(Calendar.MINUTE, 0);
		day.set(Calendar.SECOND, 0);
		day.set(Calendar.MILLISECOND, 0);
		
		// TODO: needed+transform to same format as ...answered or delete
		long date = day.getTimeInMillis();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor;
        cursor = db.query(TABLE_STRESSLEVELS, new String[] {KEY_STRESSLEVELS_NOTIFICATIONDATE, KEY_STRESSLEVELS_TIMEOFDAY}, KEY_STRESSLEVELS_NOTIFICATIONDATE + " > ? AND " + KEY_STRESSLEVELS_NOTIFICATIONDATE + " < ?",
        		new String[] {String.valueOf(date), String.valueOf(date + TimeUnit.MILLISECONDS.convert(1, TimeUnit.DAYS))}, null, null, null);
        while(cursor.moveToNext()) {
        	result.put(cursor.getString(1), cursor.getLong(0));
        }
        Log.i(TAG, "Notification stress dates of day " + Util.getFormattedDate(day.getTimeInMillis()) + ": ");
        if (result.containsKey("early")) {
        	Log.i(TAG, "Early: " + result.get("early"));
        }
        else {
        	Log.i(TAG, "Early: No early stresslevel found");
        }
        if (result.containsKey("late")) {
        	Log.i(TAG, "Late: " + result.get("late"));
        }
        else {
        	Log.i(TAG, "Late: No late stresslevel found");
        }
        cursor.close();
		
		return result;
    }
    
    /**
     * Gives you the times the user answered about his stress level for a given day
     * @param day The requested day. Provide a GregorianCalendar day
     * Hour, minute, seconds and milliseconds will be automatically set to zero. Remember month january has to be 0...december 11.
     * @return A LinkedHashMap containing the notification stress values assigned to their keys (time answered as millis)
     */
    public LinkedHashMap<Long, Integer> getStressNotificationTimeAnswered(GregorianCalendar day) {
    	LinkedHashMap<Long, Integer> result = new LinkedHashMap<Long, Integer>();

        day.set(Calendar.HOUR_OF_DAY, 0);
		day.set(Calendar.MINUTE, 0);
		day.set(Calendar.SECOND, 0);
		day.set(Calendar.MILLISECOND, 0);
		
		long date = day.getTimeInMillis();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor;
        cursor = db.query(TABLE_STRESSLEVELS, new String[] {KEY_STRESSLEVELS_REALDATE, KEY_STRESSLEVELS_STRESS}, KEY_STRESSLEVELS_REALDATE + " > ? AND " + KEY_STRESSLEVELS_REALDATE + " < ?",
        		new String[] {String.valueOf(date), String.valueOf(date + TimeUnit.MILLISECONDS.convert(1, TimeUnit.DAYS))}, null, null, null);
        while(cursor.moveToNext()) {
        	result.put(cursor.getLong(0), cursor.getInt(1));
        }
        Log.i(TAG, "Real stress dates of day " + Util.getFormattedDate(day.getTimeInMillis()) + ": ");
        for(long timeAnswered : result.keySet()){
        	Log.i(TAG, "Found stresslevel "+ result.get(timeAnswered) + " answered at " + timeAnswered);
        }
        cursor.close();
		return result;
    }
    
    /**
     * This method adds the given amount of seconds to the sum of duration the app has been shown
     * @param app The name of the app
     * @param time The duration we add
     * @param start The starttime the app was opened
     */
    public void addAppTime (String app, int time, long start) {
    	SQLiteDatabase db = this.getWritableDatabase();

        String insert = "INSERT INTO " + TABLE_APPTIME + " (" + KEY_APPTIME_NAME + "," + KEY_APPTIME_TIME + "," + KEY_APPTIME_START + ")" +
                " VALUES (?, '" + time + "', '" + start + "')";
        SQLiteStatement i = db.compileStatement(insert);
        i.bindString(1, app);
        i.execute();
        Log.i(TAG, insert + "\n? = " + app);
    }
    
    /**
     * Retrieves all Apps and their corresponding times that they have been open. Provide a day as time "endpoint" and a span backwards.
     * @param day A specific day that you are requesting as end point
     * @param span Timespan in days
     * @return A LinkedHashMap with Appname and its assigned time value (sorted descending)
     */
    public LinkedHashMap<String, Integer> getAppTimes (GregorianCalendar day, int span) {
    	
		LinkedHashMap<String, Integer> result = new LinkedHashMap<String, Integer>();
		Log.i(TAG, "Span: " + span + " day(s)");
		long toDate = day.getTimeInMillis();
		long fromDate = 0;
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor;
		if (span == 0){
			String query = "SELECT " + KEY_APPTIME_NAME + ", SUM(" + KEY_APPTIME_TIME + ") AS sum FROM " + TABLE_APPTIME + " GROUP BY " + KEY_APPTIME_NAME + " ORDER BY sum DESC";
			Log.i(TAG, query);
			cursor = db.rawQuery(query, null);
		}
		else {
			fromDate = day.getTimeInMillis() + TimeUnit.MILLISECONDS.convert(-1 * span, TimeUnit.DAYS);
			String query = "SELECT " + KEY_APPTIME_NAME + ", SUM(" + KEY_APPTIME_TIME + ") AS sum FROM " + TABLE_APPTIME + " WHERE " + KEY_APPTIME_START + "<" + toDate + " AND " + KEY_APPTIME_START + ">" + fromDate + " GROUP BY " + KEY_APPTIME_NAME + " ORDER BY sum DESC";
			Log.i(TAG, query);
			cursor = db.rawQuery(query, null);
		}
		Log.i(TAG, "Apptimes from " + Util.getFormattedDate(fromDate) + " to " + Util.getFormattedDate(day.getTimeInMillis()));
		while(cursor.moveToNext()) {
			result.put(cursor.getString(0), cursor.getInt(1));
			Log.i(TAG, "Apptime found. App: " + cursor.getString(0) + " Time: " + cursor.getInt(1) + " seconds");
		}
		cursor.close();
		
		return result;
    }
    
    /**
     * 
     * @param area The area to add
     * @param start The starttime the user is present in this area
     */
    public void addRecentCell (String locationString, long start) {
    	SQLiteDatabase db = this.getWritableDatabase();

        String insert = "INSERT INTO " + TABLE_AREAHISTORY + " (" + KEY_AREAHISTORY_LOCATIONSTRING + "," + KEY_AREAHISTORY_START + ")" +
                " VALUES ('" + locationString + "', " + start + ")";
        Log.i(TAG, insert);
		db.execSQL(insert);
    }
    
    /**
     * Gives you a list of areas and the date at which the user started being present in it for the given day.
     * E.g. Zu Hause - 0:00, Unbekannt - 8:34, Arbeit - 8:55, Unbekannt - 18:07, Zu Hause - 18:41
     * @param day The requested day. Provide a GregorianCalendar day
     * Hour, minute, seconds and milliseconds will be automatically set to zero. Remember month january has to be 0...december 11.
     * @return A list of Areas sorted by time
     */
    public ArrayList<Area> getAreaHistoryOfDay (GregorianCalendar day) {
    	Log.i(TAG, "Area History results:");
		ArrayList<Area> result = new ArrayList<Area>();
		HashMap<String, String> cache = new HashMap<String, String>();
		day.set(Calendar.HOUR_OF_DAY, 0);
		day.set(Calendar.MINUTE, 0);
		day.set(Calendar.SECOND, 0);
		day.set(Calendar.MILLISECOND, 0);

		String area = "";
        long date = day.getTimeInMillis();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor;
        // get the last change of area
        // maybe the evening before changed to "Zu Hause" and this is where the user is at the start of the asked day
        cursor = db.query(TABLE_AREAHISTORY, new String[] {KEY_AREAHISTORY_LOCATIONSTRING, KEY_AREAHISTORY_START}, KEY_AREAHISTORY_START + "<?",
				new String[] {String.valueOf(date)}, null, null, KEY_APPTIME_START + " DESC", "1");
        if (cursor.moveToFirst()) {
        	area = getAreaNameByLocation(cursor.getString(0));
        	// set time of the first area of the day to 0:00
        	result.add(new Area(area, day.getTimeInMillis()));
        	Log.i(TAG, "Last known area before given date: " + area + " at date: " + Util.getFormattedDate(cursor.getLong(1)));
        	Log.i(TAG, "Date in the object set to: " + Util.getFormattedDate(day.getTimeInMillis()));
        }
        else {
        	Log.i(TAG, "No last known area before given date found.");
        }
        db = this.getReadableDatabase();
        cursor = db.query(TABLE_AREAHISTORY, new String[] {KEY_AREAHISTORY_LOCATIONSTRING, KEY_AREAHISTORY_START}, KEY_AREAHISTORY_START + ">=? AND " + KEY_AREAHISTORY_START + "<?",
				new String[] {String.valueOf(date), String.valueOf(date + TimeUnit.MILLISECONDS.convert(1, TimeUnit.DAYS))}, null, null, null, null);
        
        while (cursor.moveToNext()) {
        	/*
        	 * caching of assignments area - locationstring in memory
        	 */
        	if (cache.containsKey(cursor.getString(0))) {
        		area = cache.get(cursor.getString(0));
        	}
        	else {
        		area = getAreaNameByLocation(cursor.getString(0));
        		cache.put(cursor.getString(0), area);
        	}
        	if (!result.isEmpty()) {
        		// last area is the same, might happen since many cells may be assigned the same area
        		if (!area.equals(result.get(result.size()-1).getName())){
        			result.add(new Area(area, cursor.getLong(1)));
        		}
        		else {
        			Log.i(TAG, "Last area in history is the same, don't need to add this one.");
        		}
        	}
        	else {
        		result.add(new Area(area, cursor.getLong(1)));
        	}
        	Log.i(TAG, "Found area: " + cursor.getString(0) + " at date: " + Util.getFormattedDate(cursor.getLong(1)));
        }
		return result;
    }
    
	public ArrayList<Cell> getLastKnownCells(int count) {
		ArrayList<Cell> result = new ArrayList<Cell>();
		String area = "";
		SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor;
        cursor = db.query(TABLE_AREAHISTORY, new String[] {KEY_AREAHISTORY_LOCATIONSTRING, KEY_AREAHISTORY_START}, null,
				null, null, null, KEY_APPTIME_START + " DESC", String.valueOf(count));
        while (cursor.moveToNext()) {
        	area = getAreaNameByLocation(cursor.getString(0));
        	result.add(new Cell(cursor.getString(0), area, cursor.getLong(1)));
        	Log.i(TAG, "LastKnownCells result: " + area + " seen at " + Util.getFormattedDate(cursor.getLong(1)));
        }
        return result;
	}

	/**
	 * Adds the most probable activity to the database.
	 * @param activity The activity
	 * @param confidence The probability for this activity
	 * @param time The time of the activity
	 */
	public void addActivity(String activity, int confidence, long time) {
		SQLiteDatabase db = this.getWritableDatabase();

        String insert = "INSERT INTO " + TABLE_ACTIVITIES + " (" + KEY_ACTIVITIES_ACTIVITY + "," + KEY_ACTIVITIES_CONFIDENCE + "," + KEY_ACTIVITES_TIME + ")" +
                " VALUES ('" + activity + "', " + confidence + ", " + time + ")";
        Log.i(TAG, insert);
		db.execSQL(insert);
	}
	
	private HashMap<String, Integer> getActivities (long time) {
		HashMap<String, Integer> result = new HashMap<String, Integer>();
		SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor;
        String activity;
        int confidence;
        cursor = db.query(TABLE_ACTIVITIES, new String[] {KEY_ACTIVITIES_ACTIVITY, KEY_ACTIVITIES_CONFIDENCE, KEY_ACTIVITES_TIME}, KEY_ACTIVITES_TIME + ">=? AND " + KEY_ACTIVITES_TIME + "<?",
				new String[] {String.valueOf(time), String.valueOf(time + STRESS_CALCULATION_INTERVAL_MILLIS)}, null, null, null, null);
        while (cursor.moveToNext()) {
        	activity = cursor.getString(0);
        	confidence = cursor.getInt(1);
        	if (!result.containsKey(activity)) {
        		result.put(activity, confidence);
        	}
        	else {
        		result.put(activity, result.get(activity)+confidence);
        	}
        	Log.i(TAG, STRESS_CALCULATION_INTERVAL_MINUTES + " minute interval activity: " + activity + " Confidence: " + confidence);
        }
        for (String act : result.keySet()) {
        	Log.i(TAG, act + ": " + result.get(act));
        }
		return result;
	}
	
	public void saveBadgeProgress (String name, int progress) {
		SQLiteDatabase db = this.getWritableDatabase();

		String insert;
		// badges with no addition but fixed values
		if (name.equals(Constants.BADGE_NAME_CONTACTSSETUP) || name.equals(Constants.BADGE_NAME_USAGETIME)) {
			insert = "INSERT OR REPLACE INTO " + TABLE_BADGES + " (" + KEY_BADGES_NAME + "," + KEY_BADGES_PROGRESS + ")" +
            " VALUES ('" + name+ "', " + progress + ")";
		}
		else {
			String selectProgress = "(SELECT " + KEY_BADGES_PROGRESS + " FROM " + TABLE_BADGES + " WHERE " + KEY_BADGES_NAME + "='" + name +"')";
	        insert = "INSERT OR REPLACE INTO " + TABLE_BADGES + " (" + KEY_BADGES_NAME + "," + KEY_BADGES_PROGRESS + ")" +
	                " VALUES ('" + name+ "', IFNULL(" + selectProgress + ",0)+" + progress + ")";
		}
        Log.i(TAG, insert);
		db.execSQL(insert);
	}
	
	public int getBadgeProgress (String name) {
		int result = 0;
		SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor;
        cursor = db.query(TABLE_BADGES, new String[] {KEY_BADGES_NAME, KEY_BADGES_PROGRESS}, KEY_BADGES_NAME + "=?",
				new String[] {name}, null, null, null);
        if (cursor.moveToFirst()) {
        	result = cursor.getInt(1);
        	Log.i(TAG, "Badge progress for " + name + " is: " + result);
        }
        else {
        	Log.i(TAG, "Badge progress for " + name + " not found!");
        }
        return result;
	}
}
