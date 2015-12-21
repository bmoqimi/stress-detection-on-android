package com.datenkrake;

import java.util.GregorianCalendar;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * 
 * @author Thomas
 *
 */
public class ActivityBroadcastReceiver extends BroadcastReceiver {
	
	private String TAG = getClass().getSimpleName();
	private CsvHandler csv = new CsvHandler();
	private GregorianCalendar time;
	private DatabaseHandler db = SolinApplication.getDbHandler();

	@Override
	public void onReceive(Context context, Intent intent) {
		String activity = intent.getStringExtra("Activity");
		int confidence = intent.getExtras().getInt("Confidence");
		String data =  "Activity: " + activity+ " " + "Confidence: " + confidence + "\n";
    	Log.i(TAG, data);
    	time = new GregorianCalendar();
		db.addActivity(activity, confidence, time.getTimeInMillis());
    	csv.appendActivitiesData(context, activity, confidence, Util.getFormattedDate(time.getTimeInMillis()));
	}

}
