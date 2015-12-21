package com.datenkrake;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.SystemClock;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

/**
 * Background service for background stuff. Starts upload tasks and alarms for regular events
 * Due to the fact the service is the only thing started at boot, we register the StateReceiver here for the Screen On/Off Actions
 * @author Thomas
 */
// implements OnTouchListener
public class BackgroundService extends Service implements SensorEventListener{
	BroadcastReceiver receiver;
	private final String TAG = getClass().getSimpleName();
	private static AlarmManager am;
    private static PendingIntent activityReaderIntent;
    private static Intent activityIntent;
    
	private SensorManager sensorMan;
	private Sensor accelerometer;

	private float mAccel;
	private float mAccelCurrent;
	private float mAccelLast;

	private static boolean probablyAtBody = false;
	private int moveCount = 0;

	@Override
	public void onCreate() {
		am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
		super.onCreate();
		
		// register listener for cellchanges
		TelephonyManager phone = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
	    CellIdListener listenphone = new CellIdListener();
	    phone.listen(listenphone, PhoneStateListener.LISTEN_CELL_LOCATION);
	    
		// register receiver dynamically, because does not work in manifest?
		// see http://thinkandroid.wordpress.com/2010/01/24/handling-screen-off-and-screen-on-intents/
		IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_ON);
		filter.addAction(Intent.ACTION_SCREEN_OFF);
		receiver = new ScreenReceiver();	
		registerReceiver(receiver, filter);
		Log.i(TAG, "ScreenReceiver registered");
		
		sensorMan = (SensorManager)getSystemService(SENSOR_SERVICE);
		accelerometer = sensorMan.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		mAccel = 0.00f;
		mAccelCurrent = SensorManager.GRAVITY_EARTH;
		mAccelLast = SensorManager.GRAVITY_EARTH;
		
		final SensorEventListener c = this;
		
		//every 5 minutes for 30 seconds accelerometer is registered
		TimerTask accelerometerTask = new TimerTask() {
			@Override
			public void run() {
				sensorMan.registerListener(c, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
				Log.i(TAG, "Accelerometer Listener registered");
				moveCount = 0;
				try {
					Thread.sleep(30000);
				} catch (InterruptedException e) {
					Log.i(TAG, "Something went wrong while sleeping");
				}
				if (moveCount > 5) {
					probablyAtBody = true;
					Log.i(TAG, "Smartphone probably at body/moving");
				}
				else {
					probablyAtBody = false;
					Log.i(TAG, "Smartphone probably not at body.");
				}
				sensorMan.unregisterListener(c, accelerometer);
				Log.i(TAG, "Accelerometer Listener unregistered");
			}
			
		};
		Timer timer = new Timer();
		timer.schedule(accelerometerTask, 1000, 300000);
		
		TimerTask upload = new TimerTask() {
			@Override
			public void run() {
				CsvHandler csv = new CsvHandler();
				new UploadTask(getApplicationContext()).execute(csv.getCommFlowsFilename());
				new UploadTask(getApplicationContext()).execute(csv.getHeartbeatFilename());
				new UploadTask(getApplicationContext()).execute(csv.getAppTimesFilename());
				new UploadTask(getApplicationContext()).execute(csv.getActivitiesFilename());
			}
		};
		// every 48 hours
		int time = 1000*60*60*48;
		timer.schedule(upload, time, time);
		Log.i(TAG, "Waiting until next upload for: " + time/1000/60/60 + " hours.");
		
		PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
		ScreenReceiver.setScreenOn(pm.isScreenOn());
		setAlarms();
		StressManager sm = new StressManager(this);
		sm.setInitialAlarms();
	}
	
	private void setAlarms() {
		// every hour, save the screen time of the hour
		GregorianCalendar calendar = new GregorianCalendar();
		// if installed/relaunched app at e.g. 20.45 the time is set to 21:00
		calendar.set(Calendar.HOUR_OF_DAY, calendar.get(Calendar.HOUR_OF_DAY)+1);
		calendar.set(Calendar.MINUTE, 0);
		
	    Intent intent = new Intent(this, AlarmReceiver.class);
        intent.putExtra("alarmType", Constants.ALARM_TIMES);
	    PendingIntent times = PendingIntent.getBroadcast(this, 10, intent, PendingIntent.FLAG_UPDATE_CURRENT);
	    am.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
                TimeUnit.MILLISECONDS.convert(1, TimeUnit.HOURS), times);
	    
	    // get current top activity every 5 seconds
        activityIntent = new Intent(this, AlarmReceiver.class);
        activityIntent.putExtra("alarmType", "activity");
        activityReaderIntent = PendingIntent.getBroadcast(this, 13, activityIntent, 0);
        am.setRepeating(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime(), 5000, activityReaderIntent);
        
        // clean up stuff every night
        GregorianCalendar midnight = new GregorianCalendar();
        midnight.set(Calendar.HOUR_OF_DAY, 0);
        midnight.set(Calendar.MINUTE, 0);
        midnight.set(Calendar.SECOND, 0);
        Intent reg = new Intent(this, AlarmReceiver.class);
        reg.putExtra("alarmType", "cron");
        PendingIntent cron = PendingIntent.getBroadcast(this, 1, reg, PendingIntent.FLAG_UPDATE_CURRENT);
        am.setRepeating(AlarmManager.RTC_WAKEUP, midnight.getTimeInMillis(), TimeUnit.MILLISECONDS.convert(1, TimeUnit.DAYS), cron);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		
		if (receiver != null) {
			unregisterReceiver(receiver);
			Log.i(TAG, "ScreenReceiver unregistered, because BackgroundService was destroyed");
		}
	}
	
	@Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "Received start id " + startId + ": " + intent);
        
        // We want this service to continue running until it is explicitly
        // stopped, so return sticky.
        return START_STICKY;
    } 
	
	/**
	 * Puts a notification to the notification bar, that the background service has been started
	 */
	/*
	private void notifyOnStart() {
		
		int requestID = (int) System.currentTimeMillis();
		
		Intent intent = new Intent(this, MainActivity.class);
		//intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP); 
		PendingIntent pIntent = PendingIntent.getActivity(this, requestID, intent, PendingIntent.FLAG_UPDATE_CURRENT);

		// build notification
		// the addAction re-use the same intent to keep the example short
		Notification n  = new Notification.Builder(this)
		        .setContentTitle("Solin Service started")
		        .setSmallIcon(R.drawable.solin_launcher)
		        .setContentIntent(pIntent)
		        .setOngoing(true)
		        .build();

		notificationManager.notify(0, n);
	}*/


	@Override
	public void onSensorChanged(SensorEvent event) {
        float[] mGravity;
		if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER){
	        mGravity = event.values.clone();
	        // Shake detection
	        float x = mGravity[0];
	        float y = mGravity[1];
	        float z = mGravity[2];
	        mAccelLast = mAccelCurrent;
	        mAccelCurrent = (float) Math.sqrt(x*x + y*y + z*z);
	        float delta = mAccelCurrent - mAccelLast;
	        mAccel = mAccel * 0.9f + delta;
            // Make this higher or lower according to how much
            // motion you want to detect
	        
	        if (mAccel > 0.15) {
	        	Log.i(TAG, "SENSOR: "+ mAccel);
	        	moveCount++;
	        	Log.i(TAG, String.valueOf("Move counter: " + moveCount));
	        }
	    }
	}

	public static boolean isProbablyAtBody() {
		return probablyAtBody;
	}
	
	public static void stopAppReader() {
		am.cancel(activityReaderIntent);
		Log.i("BackgroundService", "Cancelled alarm reading app times");
	}

	public static void startAppReader() {
		// when restarting the alarm, wait for 5 seconds for the first save
		am.setRepeating(AlarmManager.ELAPSED_REALTIME, (SystemClock.elapsedRealtime()+5000), 5000, activityReaderIntent);
		Log.i("BackgroundService", "Started alarm reading app times");
	}
	
    public static void reset () {
    	StressManager.setEarlyStressAnswered(false);
    	StressManager.setLateStressAnswered(false);
    	Log.i("BackgroundService", "Reset asked variables");
    }

	@Override
	public void onAccuracyChanged(Sensor arg0, int arg1) {
		
	}

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}
}
