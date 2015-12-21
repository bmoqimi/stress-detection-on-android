package com.datenkrake;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import android.content.Context;
import android.util.Log;

/**
 * 
 * @author Thomas
 *
 */
public class CsvHandler {
	private final String TAG = getClass().getSimpleName();
	private File commFlowFile;
	private File heartbeatFile;
	private File appTimesFile;
	private File activitiesFile;
	private String commFlowsFilename = "/commFlows.csv";
	private String heartbeatFilename = "/heartbeats.csv";
	private String appTimesFilename = "/appTimes.csv";
	private String activitiesFilename = "/activities.csv";
	private static String path;
	
	/**
	 * Creates the initial CSV file. Is called when activity is created.
	 * @return 0, if everything worked and 1, if something went wrong
	 */
	public void createInitialCsvs(Context context) {
		
		path = context.getFilesDir().getPath().toString();
		commFlowFile = new File(path + commFlowsFilename);
		heartbeatFile = new File(path + heartbeatFilename);
		appTimesFile = new File(path + appTimesFilename);
		activitiesFile = new File(path + activitiesFilename);

		// only write header data if file doesn't exist yet
		// It is indeed possible that the file exists e.g. app/device was restarted
		if (!commFlowFile.exists()){
			writeCommFlowHeaderData(context);
		}
		else {
			Log.i(TAG, "CommFlow File already exists, nothing new created");
		}
		
		if (!heartbeatFile.exists()){
			writeHeartbeatHeaderData(context);
		}
		else {
			Log.i(TAG, "Heartbeat File already exists, nothing new created");
		}
		
		if (!appTimesFile.exists()){
			writeAppTimesHeaderData(context);
		}
		else {
			Log.i(TAG, "Apptimes File already exists, nothing new created");
		}
		if (!activitiesFile.exists()){
			writeActivitiesHeaderData(context);
		}
		else {
			Log.i(TAG, "Activities File already exists, nothing new created");
		}
	}

	/**
	 * Appends the appropriate data read from the SMS/Call/Mail
	 * @param medium Determines whether it's from an SMS/Call or Mail
	 * @param name Sender's name. If not available left empty
	 * @param number Sender's number. If not available left empty
	 * @param longitude Longitude value of the location
	 * @param latitude Latitude value of the location
	 * @param date Current date
	 * @param duration Duration of the call. If not available left empty
	 * @param area The learned area the user is in, e.g. work, home
	 * @return
	 */
	public int appendCommFlowData(Context context, String medium, String contactHash, String date, String duration, boolean important, String area, boolean atBody) {
		try {
			commFlowFile = new File(path + commFlowsFilename);
		    
		    // when the app data has been cleared manually AND the activity has not been opened again yet we have to create the headers again
		    if (!commFlowFile.exists()){
		    	writeCommFlowHeaderData(context);
		    }
		    FileWriter fw = new FileWriter(commFlowFile, true);
			BufferedWriter writer = new BufferedWriter(fw);
			
			String id = Util.getAndroidID(context);
			writer.append(id);
			writer.append(";");
			Log.i(TAG, id);
			
			writer.append(medium);
			writer.append(";");
			Log.i(TAG, "Medium: " + medium);
			

			if (contactHash != null) {
				writer.append(contactHash);
				Log.i(TAG, "Contact Hash: " + contactHash);
			}
			else {
				writer.append("No contact known");
				Log.i(TAG, "Contact Hash: empty");
			}
			writer.append(";");
			
			writer.append(date);
			writer.append(";");
			Log.i(TAG, "Date: "+  date);
			
			if (duration != null) {
				writer.append(duration);
				Log.i(TAG, "Duration: " + duration);
			}
			writer.append(";");
			
			if (important) {
				writer.append("Important");
				Log.i(TAG, "Important");
			}
			else {
				writer.append("Not important");
				Log.i(TAG, "Not important");
			}
			writer.append(";");
			
			writer.append(area);
			Log.i(TAG, "Area: "+area);
			writer.append(";");
			
			if (atBody) {
				writer.append("Near body");
				Log.i(TAG, "Body: Near body");
			}
			else {
				writer.append("Not near body");
				Log.i(TAG, "Body: Not near body");
			}
			
			writer.append("\n");

			writer.flush();
			writer.close();
			fw.close();
			
			Log.i(TAG, "Commflow data appended");
		}
		catch (IOException e) {
			Log.e(TAG, e.getMessage());
			return 1;
		}
		
		return 0;
	}
	
	public int appendHeartbeatData (Context context, String date, String heartbeat, String instantSpeed) {
		try {
			heartbeatFile = new File(path + heartbeatFilename);
		    
		    // when the app data has been cleared manually AND the activity has not been opened again yet we have to create the headers again
		    if (!heartbeatFile.exists()){
		    	writeHeartbeatHeaderData(context);
		    }
		    FileWriter fw = new FileWriter(heartbeatFile, true);
			BufferedWriter writer = new BufferedWriter(fw);
			
			String id = Util.getAndroidID(context);
			writer.append(id);
			writer.append(";");
			Log.i(TAG, "AndroidID: " + id);
			
			writer.append(date);
			writer.append(";");
			Log.i(TAG, "Date: " + date);
			
			writer.append(heartbeat);
			writer.append(";");
			Log.i(TAG, "Heartbeat: " + heartbeat);
			
			writer.append(instantSpeed);
			Log.i(TAG, "Instant speed: " + instantSpeed);
			
			writer.append("\n");
	
			writer.flush();
			writer.close();
			fw.close();
			
			Log.i(TAG, "Heartbeat data appended");
		}
		catch (IOException e) {
			Log.e(TAG, e.getMessage());
			return 1;
		}
		
		return 0;
	}
	
	public int appendAppTimesData(Context context, String app, int time, String date) {
		try {
			appTimesFile = new File(path + appTimesFilename);
		    
		    // when the app data has been cleared manually AND the activity has not been opened again yet we have to create the headers again
		    if (!appTimesFile.exists()){
		    	writeAppTimesHeaderData(context);
		    }
		    FileWriter fw = new FileWriter(appTimesFile, true);
			BufferedWriter writer = new BufferedWriter(fw);
			
			String id = Util.getAndroidID(context);
			writer.append(id);
			writer.append(";");
			Log.i(TAG, "AndroidID: " + id);
			
			writer.append(app);
			writer.append(";");
			Log.i(TAG, "App: " + app);

			writer.append(String.valueOf(time));
			writer.append(";");
			Log.i(TAG, "Time: "+ time);
			
			writer.append(date);
			Log.i(TAG, "Date: " + date);
			
			writer.append("\n");

			writer.flush();
			writer.close();
			fw.close();
			
			Log.i(TAG, "Apptimes data appended");
		}
		catch (IOException e) {
			Log.e(TAG, e.getMessage());
			return 1;
		}
		
		return 0;
	}
	
	public int appendActivitiesData(Context context, String activity, int confidence, String date) {
		try {
			activitiesFile = new File(path + activitiesFilename);
		    
		    // when the app data has been cleared manually AND the activity has not been opened again yet we have to create the headers again
		    if (!activitiesFile.exists()){
		    	writeActivitiesHeaderData(context);
		    }
		    FileWriter fw = new FileWriter(activitiesFile, true);
			BufferedWriter writer = new BufferedWriter(fw);
			
			String id = Util.getAndroidID(context);
			writer.append(id);
			writer.append(";");
			Log.i(TAG, "AndroidID: " + id);
			
			writer.append(activity);
			writer.append(";");
			Log.i(TAG, "Activity: " + activity);
			
			writer.append(String.valueOf(confidence));
			writer.append(";");
			Log.i(TAG, "Confidence: " + confidence);

			writer.append(date);
			Log.i(TAG, "Date: "+ date);
			
			writer.append("\n");

			writer.flush();
			writer.close();
			fw.close();
			
			Log.i(TAG, "Activities data appended");
		}
		catch (IOException e) {
			Log.e(TAG, e.getMessage());
			return 1;
		}
		
		return 0;
	}

	private void writeCommFlowHeaderData(Context context) {
		try {
			commFlowFile = new File(path + commFlowsFilename);
			FileWriter fw = new FileWriter(commFlowFile, true);
			BufferedWriter writer = new BufferedWriter(fw);

			Log.i(TAG, commFlowsFilename + " File created");
			
			//header
			writer.append("AndroidID");
			writer.append(";");
			writer.append("Medium");
			writer.append(";");
			writer.append("ContactHash");
			writer.append(";");
			writer.append("Date");
			writer.append(";");
			writer.append("Duration");
			writer.append(";");
			writer.append("Important");
			writer.append(";");
			writer.append("Area");
			writer.append(";");
			writer.append("Body");
			writer.append("\n");

			writer.flush();
			writer.close();
			fw.close();
			
			Log.i(TAG, "Initial CommFlow CSV headers written");
		}
		catch (IOException e) {
			Log.e(TAG, e.getMessage());
		}
	}
	
	private void writeHeartbeatHeaderData(Context context) {
		try {
			heartbeatFile = new File(path + heartbeatFilename);
			FileWriter fw = new FileWriter(heartbeatFile, true);
			BufferedWriter writer = new BufferedWriter(fw);

			Log.i(TAG, heartbeatFilename + " File created");
			
			//header
			writer.append("AndroidID");
			writer.append(";");
			writer.append("Date");
			writer.append(";");
			writer.append("Heartbeat");
			writer.append(";");
			writer.append("Instant Speed");
			writer.append("\n");

			writer.flush();
			writer.close();
			fw.close();
			
			Log.i(TAG, "Initial Heartbeat CSV headers written");
		}
		catch (IOException e) {
			Log.e(TAG, e.getMessage());
		}
	}
	

	private void writeAppTimesHeaderData(Context context) {
		try {
			appTimesFile = new File(path + appTimesFilename);
			FileWriter fw = new FileWriter(appTimesFile, true);
			BufferedWriter writer = new BufferedWriter(fw);

			Log.i(TAG, appTimesFilename + " File created");
			
			//header
			writer.append("AndroidID");
			writer.append(";");
			writer.append("App");
			writer.append(";");
			writer.append("Time");
			writer.append(";");
			writer.append("Date");
			writer.append("\n");

			writer.flush();
			writer.close();
			fw.close();
			
			Log.i(TAG, "Initial Apptimes CSV headers written");
		}
		catch (IOException e) {
			Log.e(TAG, e.getMessage());
		}
	}
	
	private void writeActivitiesHeaderData(Context context) {
		try {
			activitiesFile = new File(path + activitiesFilename);
			FileWriter fw = new FileWriter(activitiesFile, true);
			BufferedWriter writer = new BufferedWriter(fw);

			Log.i(TAG, activitiesFilename + " File created");
			
			//header
			writer.append("AndroidID");
			writer.append(";");
			writer.append("Activity");
			writer.append(";");
			writer.append("Confidence");
			writer.append(";");
			writer.append("Date");
			writer.append("\n");

			writer.flush();
			writer.close();
			fw.close();
			
			Log.i(TAG, "Initial Activities CSV headers written");
		}
		catch (IOException e) {
			Log.e(TAG, e.getMessage());
		}
	}
	
	public String getCommFlowsFilename() {
		return commFlowsFilename;
	}

	public String getHeartbeatFilename() {
		return heartbeatFilename;
	}

	public String getAppTimesFilename() {
		return appTimesFilename;
	}
	public String getActivitiesFilename() {
		return activitiesFilename;
	}
}
