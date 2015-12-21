package com.datenkrake;

import java.util.GregorianCalendar;
import java.util.List;

import android.app.ActivityManager;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

/**
 * Puts the current time that the screen was on into the database. Receives the Intent from the {@link com.datenkrake.BackgroundService}
 * @author Thomas
 *
 */
public class AlarmReceiver extends BroadcastReceiver {
    private String TAG = getClass().getSimpleName();
    private static boolean firstAppUpdate = true;
    private static String lastApp = "";
    private static int appDuration = 0;
    private static GregorianCalendar lastAppStart;
    private NotificationManager notificationManager;
    private DatabaseHandler db = SolinApplication.getDbHandler();
    
	@Override
	public void onReceive(Context context, Intent intent) {
        notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        Bundle extras = intent.getExtras();
        String type = extras.getString(Constants.ALARM_TYPE);
        Log.i(TAG, "Type of received alarm: " + type);
        if (type.equals(Constants.ALARM_TIMES)) {
            // calender object representing the current date + hour of day
            GregorianCalendar calendar = new GregorianCalendar();
            db.addScreenTime(calendar, ScreenReceiver.getCurrentScreenTime());
            ScreenReceiver.resetTime();
        }
        if (type.equals("stress")) {
        	String time = extras.getString("time");
        	StressManager sm = new StressManager(context);
        	sm.setupStressLevelNotification(time);
        }

        // save chosen stresslevel as integer
        // providing time when the question was asked
        if (type.equals("stressLevel")) {
        	StressManager sm = new StressManager(context);
            sm.saveStressLevel(extras.getInt("level"), extras.getString("time"), extras.getInt("day"), extras.getInt("hour"), extras.getInt("minute"));
            notificationManager.cancel(Constants.PERCEIVED_STRESS_NOTIFICATION_ID);
        }
        if (type.equals("activity")) {
            // get the info from the currently running task
    		ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            List<ActivityManager.RunningTaskInfo> taskInfo = am.getRunningTasks(1); 

            ComponentName componentInfo = taskInfo.get(0).topActivity;
            String applicationName = Util.getAppName(context, componentInfo.getPackageName());
        	Log.i(TAG, "CURRENT Activity: " + componentInfo.getClassName()+"   Package Name: " + componentInfo.getPackageName());
            Log.i(TAG, "App name: " + applicationName);
            updateAppTimes(context, applicationName);
        }
        if (type.equals("cron")) {
        	BackgroundService.reset();
        }
	}

	/**
	 * Sums up the duration an app is opened. If another app shows up, the last app is saved to the database and the new one summed up.
	 * @param context
	 * @param applicationName
	 */
	private void updateAppTimes(Context context, String applicationName) {
		CsvHandler csv = new CsvHandler();
		if (firstAppUpdate) {
			lastAppStart = new GregorianCalendar();
			lastApp = applicationName;
			firstAppUpdate = false;
		}
		// old app still opened
		if (lastApp.equals(applicationName)) {
			appDuration += 5;
		}
		// new app
		else {
			Log.i(TAG, "New App detected! Adding last app time to database.");
			db.addAppTime(lastApp, appDuration, lastAppStart.getTimeInMillis());
			csv.appendAppTimesData(context, lastApp, appDuration, Util.getFormattedDate(lastAppStart.getTimeInMillis()));
			lastAppStart = new GregorianCalendar();
			lastApp = applicationName;
			appDuration = 5;
			Log.i(TAG, "Startet new app counter for: " + applicationName + ". Duration now is: " + appDuration + ". AppStart time is: " + lastAppStart.getTimeInMillis());
		}
	}
}
