package com.datenkrake;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.concurrent.TimeUnit;

import com.datenkrake.badge.BadgeHandler;
import com.datenkrake.badge.StressAnsweredBadge;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Bitmap.Config;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

/**
 * 
 * @author Thomas
 *
 */
public class StressManager {
	
	private final String TAG = getClass().getSimpleName();
	
	private static GregorianCalendar earlyStress;

	private static GregorianCalendar lateStress;

    private static PendingIntent early, late;
    // the initial times of the notification asking the user to set his stresslevel
    private final static int earlyStressHour = 10;
	private static final int earlyStressMinute = 0;
	
    private final static int lateStressHour = 19;
	private final static int lateStressMinute = 0;
	
    private static AlarmManager am;
    private static Intent earlyIntent;
    private static Intent lateIntent;
    
    private static boolean earlyStressAnswered = false;
    
    public int getEarlyStressHour() {
    	return earlyStress.get(Calendar.HOUR_OF_DAY);
    }
    
    public int getEarlyStressMinute() {
    	return earlyStress.get(Calendar.MINUTE);
    }
    
    public int getLateStressHour() {
    	return lateStress.get(Calendar.HOUR_OF_DAY);
    }
    
    public int getLateStressMinute() {
    	return lateStress.get(Calendar.MINUTE);
    }

	public static void setEarlyStressLevelAnswered(boolean earlyStressLevelAnswered) {
		StressManager.setEarlyStressAnswered(earlyStressLevelAnswered);
	}

	public static void setLateStressLevelAnswered(boolean lateStressLevelAnswered) {
		StressManager.setLateStressAnswered(lateStressLevelAnswered);
	}

	private static boolean lateStressAnswered = false;
    
    private Context context;
    
    public StressManager(Context c) {
    	context = c;
    	earlyIntent = new Intent(context, AlarmReceiver.class);
		earlyIntent.putExtra("alarmType", "stress");
		earlyIntent.putExtra("time", "early");
		
		lateIntent = new Intent(context, AlarmReceiver.class);
		lateIntent.putExtra("alarmType", "stress");
		lateIntent.putExtra("time", "late");
		am = (AlarmManager) c.getSystemService(Context.ALARM_SERVICE);
    }
	/**
	 * Sets the alarms at the start of the app
	 */
	public void setInitialAlarms() {
		earlyStress = new GregorianCalendar();
        lateStress = new GregorianCalendar();
        // 10:00
        earlyStress.set(Calendar.HOUR_OF_DAY, earlyStressHour);
        earlyStress.set(Calendar.MINUTE, earlyStressMinute);
        earlyStress.set(Calendar.SECOND, 0);
        // 19:00
        lateStress.set(Calendar.HOUR_OF_DAY, lateStressHour);
        lateStress.set(Calendar.MINUTE, lateStressMinute);
        lateStress.set(Calendar.SECOND, 0);
        
		GregorianCalendar now = new GregorianCalendar();
    	
        // if app installed after "late" o'clock, first notification the next day
        if (now.getTimeInMillis() > lateStress.getTimeInMillis()) {
        	earlyStress.set(Calendar.DAY_OF_YEAR, earlyStress.get(Calendar.DAY_OF_YEAR)+1);
        	lateStress.set(Calendar.DAY_OF_YEAR, lateStress.get(Calendar.DAY_OF_YEAR)+1);
        }
        // between the two notification times, only early notification postponed
        else if (now.getTimeInMillis() > earlyStress.getTimeInMillis() && now.getTimeInMillis() < lateStress.getTimeInMillis()) {
    		earlyStress.set(Calendar.DAY_OF_YEAR, earlyStress.get(Calendar.DAY_OF_YEAR)+1);
        }
        
    	early = PendingIntent.getBroadcast(context, 11, earlyIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        am.setRepeating(AlarmManager.RTC_WAKEUP, earlyStress.getTimeInMillis(), TimeUnit.MILLISECONDS.convert(1, TimeUnit.DAYS), early);

        // alarm every evening
        // check for stress
        
        late = PendingIntent.getBroadcast(context, 12, lateIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        am.setRepeating(AlarmManager.RTC_WAKEUP, lateStress.getTimeInMillis(), TimeUnit.MILLISECONDS.convert(1, TimeUnit.DAYS), late);
        
        Log.i("StressManager", "Set early stress notification time to: " + earlyStress.get(Calendar.DAY_OF_MONTH) + "-" + (earlyStress.get(Calendar.MONTH)+1) + "-" + earlyStress.get(Calendar.YEAR) + "   " + earlyStress.get(Calendar.HOUR_OF_DAY) + ":" + earlyStress.get(Calendar.MINUTE));
        Log.i("StressManager", "Set late stress notification time to: " + lateStress.get(Calendar.DAY_OF_MONTH) + "-" + (lateStress.get(Calendar.MONTH)+1) + "-" + lateStress.get(Calendar.YEAR) + "   " + lateStress.get(Calendar.HOUR_OF_DAY) + ":" + lateStress.get(Calendar.MINUTE));
        
	}

	/**
     * Sets the times for the stresslevel notifications. Early has to be earlier than late.
     * If this fails to happen an error code is returned and the default values are set.
     * @param c Context
     * @param earlyHour
     * @param earlyMinute
     * @param lateHour
     * @param lateMinute
     * @return 1 on success and -1 on failure
     */
    public static int setStressTimes (Context c, int earlyHour, int earlyMinute, int lateHour, int lateMinute) {
    	
    	GregorianCalendar newEarlyTimer = new GregorianCalendar();
    	newEarlyTimer.set(Calendar.HOUR_OF_DAY, earlyHour);
    	newEarlyTimer.set(Calendar.MINUTE, earlyMinute);
    	
    	GregorianCalendar newLateTime = new GregorianCalendar();
    	newLateTime.set(Calendar.HOUR_OF_DAY, lateHour);
    	newLateTime.set(Calendar.MINUTE, lateMinute);
    	
        // right order of timers
    	if (newEarlyTimer.getTimeInMillis() < newLateTime.getTimeInMillis()) {
    		setEarlyStressTime(c, earlyHour, earlyMinute);
    		setLateStressTime(c, lateHour, lateMinute);
    		return 1;
    	}
    	// set default values and return fail code
    	else {
    		setEarlyStressTime(c, 11, 0);
    		setLateStressTime(c, 19, 0);
    		return -1;
    	}
    }
    
    /**
     * Use this method to change the time the user wants the early notification about his stresslevel.
     * If it lies in the past of the day and no notification was shown for this time yet, it is directly shown. Otherwise ignored and shown the next day.
     * @param c Context
     * @param earlyHour The new hour of his notification
     * @param earlyMinute The new minute of his notification
     */
    private static void setEarlyStressTime(Context c, int earlyHour, int earlyMinute) {
        earlyStress.set(Calendar.HOUR_OF_DAY, earlyHour);
        earlyStress.set(Calendar.MINUTE, earlyMinute);
        earlyStress.set(Calendar.SECOND, 0);
        early = PendingIntent.getBroadcast(c, 11, earlyIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        am.setRepeating(AlarmManager.RTC_WAKEUP, earlyStress.getTimeInMillis(), TimeUnit.MILLISECONDS.convert(1, TimeUnit.DAYS), early);
        Log.i("StressManager", "Set early stresslevel notification time to: " + Util.getFormattedDate(earlyStress.getTimeInMillis()));
    }

    /**
     * Use this method to change the time the user wants the late notification about his stresslevel.
     * If it lies in the past of the day and no notification was shown for this time yet, it is directly shown. Otherwise ignored and shown the next day.
     * @param c Context
     * @param lateHour The new hour of his notification
     * @param lateMinute The new minute of his notification
     */
    private static void setLateStressTime(Context c, int lateHour, int lateMinute) {
        lateStress.set(Calendar.HOUR_OF_DAY, lateHour);
        lateStress.set(Calendar.MINUTE, lateMinute);
        lateStress.set(Calendar.SECOND, 0);
        late = PendingIntent.getBroadcast(c, 12, lateIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        am.setRepeating(AlarmManager.RTC_WAKEUP, lateStress.getTimeInMillis(), TimeUnit.MILLISECONDS.convert(1, TimeUnit.DAYS), late);
        Log.i("StressManager", "Set late stresslevel notification time to: " + Util.getFormattedDate(lateStress.getTimeInMillis()));
    }
    
    private void popupNotification(Context context, String time) {
    	NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		GregorianCalendar t = new GregorianCalendar();
		t.set(Calendar.HOUR_OF_DAY, lateStressHour);
		t.set(Calendar.MINUTE, lateStressMinute);
        t.set(Calendar.SECOND, 0);
		GregorianCalendar now = new GregorianCalendar();
		if (time.equals("early") && now.getTimeInMillis() > t.getTimeInMillis()) {
			return;
		}
    	notificationManager.cancel(Constants.PERCEIVED_STRESS_NOTIFICATION_ID);
    	Intent in1 = new Intent(context, AlarmReceiver.class);
        in1.putExtra("alarmType", "stressLevel");
        in1.putExtra("level", 1);
        in1.putExtra("time", time);
        in1.putExtra("day", now.get(Calendar.DAY_OF_YEAR));
        in1.putExtra("hour", now.get(Calendar.HOUR_OF_DAY));
        in1.putExtra("minute", now.get(Calendar.MINUTE));
        PendingIntent pi1 = PendingIntent.getBroadcast(context, 21, in1, PendingIntent.FLAG_UPDATE_CURRENT);
        
        Intent in2 = new Intent(context, AlarmReceiver.class);
        in2.putExtra("alarmType", "stressLevel");
        in2.putExtra("level", 2);
        in2.putExtra("time", time);
        in2.putExtra("day", now.get(Calendar.DAY_OF_YEAR));
        in2.putExtra("hour", now.get(Calendar.HOUR_OF_DAY));
        in2.putExtra("minute", now.get(Calendar.MINUTE));
        PendingIntent pi2 = PendingIntent.getBroadcast(context, 22, in2, PendingIntent.FLAG_UPDATE_CURRENT);
        
        Intent in3 = new Intent(context, AlarmReceiver.class);
        in3.putExtra("alarmType", "stressLevel");
        in3.putExtra("level", 3);
        in3.putExtra("time", time);
        in3.putExtra("day", now.get(Calendar.DAY_OF_YEAR));
        in3.putExtra("hour", now.get(Calendar.HOUR_OF_DAY));
        in3.putExtra("minute", now.get(Calendar.MINUTE));
        PendingIntent pi3 = PendingIntent.getBroadcast(context, 23, in3, PendingIntent.FLAG_UPDATE_CURRENT);
        
        Intent in4 = new Intent(context, AlarmReceiver.class);
        in4.putExtra("alarmType", "stressLevel");
        in4.putExtra("level", 4);
        in4.putExtra("time", time);
        in4.putExtra("day", now.get(Calendar.DAY_OF_YEAR));
        in4.putExtra("hour", now.get(Calendar.HOUR_OF_DAY));
        in4.putExtra("minute", now.get(Calendar.MINUTE));
        PendingIntent pi4 = PendingIntent.getBroadcast(context, 24, in4, PendingIntent.FLAG_UPDATE_CURRENT);
        
        Intent in5 = new Intent(context, AlarmReceiver.class);
        in5.putExtra("alarmType", "stressLevel");
        in5.putExtra("level", 5);
        in5.putExtra("time", time);
        in5.putExtra("day", now.get(Calendar.DAY_OF_YEAR));
        in5.putExtra("hour", now.get(Calendar.HOUR_OF_DAY));
        in5.putExtra("minute", now.get(Calendar.MINUTE));
        PendingIntent pi5 = PendingIntent.getBroadcast(context, 25, in5, PendingIntent.FLAG_UPDATE_CURRENT);

        // create LargeIcon (must be a Bitmap with special dimensions!)
        Drawable smallIcon = context.getResources().getDrawable(R.drawable.ic_notification);
        int iconHeight = smallIcon.getIntrinsicHeight();
        int iconWidth = smallIcon.getIntrinsicWidth();
        int bitmapHeight = (int) context.getResources().getDimension(android.R.dimen.notification_large_icon_height);
        int bitmapWidth = (int) context.getResources().getDimension(android.R.dimen.notification_large_icon_width);
		int horizontalOffset = (bitmapWidth-iconWidth)/2;
		int verticalOffset = (bitmapHeight-iconHeight)/2;
        Bitmap largeIcon = Bitmap.createBitmap(bitmapWidth, bitmapHeight, Config.ARGB_8888);
		Canvas canvas = new Canvas(largeIcon);
		smallIcon.setBounds(horizontalOffset, verticalOffset, bitmapWidth-horizontalOffset, bitmapHeight-verticalOffset);
		smallIcon.draw(canvas);
        
    	Notification notification = new Notification.Builder(context)
        .setSmallIcon(R.drawable.ic_notification)
        .setOngoing(true) /** notification will appear as ongoing notification*/
        .build();
	    /** set a custom layout to the notification in notification drawer  */
	    RemoteViews notificationView = new RemoteViews(context.getPackageName(), R.layout.stress_notification);
	    notification.contentView = notificationView;
	    notification.defaults |= Notification.DEFAULT_VIBRATE;
	    notificationView.setImageViewBitmap(R.id.stress_notification_icon, largeIcon);
	    notificationView.setOnClickPendingIntent(R.id.stress_notification_button_1, pi1);
	    notificationView.setOnClickPendingIntent(R.id.stress_notification_button_2, pi2);
	    notificationView.setOnClickPendingIntent(R.id.stress_notification_button_3, pi3);
	    notificationView.setOnClickPendingIntent(R.id.stress_notification_button_4, pi4);
	    notificationView.setOnClickPendingIntent(R.id.stress_notification_button_5, pi5);
	    notificationManager.notify(Constants.PERCEIVED_STRESS_NOTIFICATION_ID, notification);
	}

	public void saveStressLevel(int stress, String time, int day, int hour, int minute) {
        if (time.equals("early")) {
            setEarlyStressLevelAnswered(true);
            Log.i(TAG, "Answered early notification. Flipping variable for no further notifications at this time for the day.");
        }
        if (time.equals("late")) {
            setLateStressLevelAnswered(true);
            Log.i(TAG, "Answered late notification. Flipping variable for no further notifications at this time for the day.");
        }
        GregorianCalendar now = new GregorianCalendar();
        
        GregorianCalendar asked = new GregorianCalendar();
        asked.set(Calendar.DAY_OF_YEAR, day);
        asked.set(Calendar.HOUR_OF_DAY, hour);
        asked.set(Calendar.MINUTE, minute);
        asked.set(Calendar.SECOND, 0);
        long difference = now.getTimeInMillis() - asked.getTimeInMillis();
        StressAnsweredBadge stressBadge = (StressAnsweredBadge) BadgeHandler.getInstance().getBadges().get(0);
        stressBadge.updateProgress(TimeUnit.MINUTES.convert(difference, TimeUnit.MILLISECONDS));
        
        SolinApplication.getDbHandler().saveNotificationStressLevel(stress, hour, minute, day, now, time);
        Log.i(TAG, "StressLevel: " + stress + " saved for time: " + time);
        Toast.makeText(context, "Stresslevel wurde gespeichert!", Toast.LENGTH_LONG).show();
        Intent it = new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
        context.sendBroadcast(it);
    }
	
	public void setupStressLevelNotification(String time) {
    	if (time.equals("early") && !earlyStressAnswered) {
    		popupNotification(context, time);
    	}
    	else if (time.equals("late") && !lateStressAnswered) {
    		popupNotification(context, time);
    	}
    	else {
    		Log.i(TAG, "Discarded notification request, because this type of notification already happened today.");
    		Log.i(TAG, "Time: " + time);
    	}
	}

	public static void setEarlyStressAnswered(boolean earlyStressAnswered) {
		StressManager.earlyStressAnswered = earlyStressAnswered;
	}

	public static void setLateStressAnswered(boolean lateStressAnswered) {
		StressManager.lateStressAnswered = lateStressAnswered;
	}
	
}
