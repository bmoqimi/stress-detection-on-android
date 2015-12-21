package com.datenkrake;

import java.util.concurrent.TimeUnit;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * @author Thomas
 *
 */
public class ScreenReceiver extends BroadcastReceiver {
	
	private Util util = new Util();
	private final String TAG = getClass().getSimpleName();
	private static long time = 0;
	private static long startTime = System.nanoTime();
	
	private static boolean screenOn;

	@Override
	public void onReceive(Context context, Intent intent) {
		
		// Read screen status, e.g. ON or OFF
		if (Intent.ACTION_SCREEN_OFF.equals(intent.getAction())) {
			util.writeDisplayData(context, "ScreenOff");
			Log.i(TAG, "Screen was turned off");
			time = time + (System.nanoTime() - startTime);
			screenOn = false;
			BackgroundService.stopAppReader();
		}
		
		if (Intent.ACTION_SCREEN_ON.equals(intent.getAction())) {
			util.writeDisplayData(context, "ScreenOn");
			Log.i(TAG, "Screen was turned on");
			startTime = System.nanoTime();
			screenOn = true;
			BackgroundService.startAppReader();
		}
	}

	/**
	 * Returns the screentime that is not yet saved to the database
	 * @return Screentime in seconds
	 */
	public static long getCurrentScreenTime() {
		if (screenOn) {
			time = time + (System.nanoTime() - startTime);
			startTime = System.nanoTime();
		}
		Log.i("ScreenReceiver", "Screen Time until now: " + TimeUnit.SECONDS.convert(time, TimeUnit.NANOSECONDS) + " seconds.");
		return TimeUnit.SECONDS.convert(time, TimeUnit.NANOSECONDS);
	}

	public static void resetTime() {
		time = 0;
		startTime = System.nanoTime();
	}
	
	public static void setScreenOn(boolean screenOn) {
		ScreenReceiver.screenOn = screenOn;
	}
}
