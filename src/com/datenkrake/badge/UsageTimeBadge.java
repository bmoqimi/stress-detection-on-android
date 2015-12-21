package com.datenkrake.badge;

import java.util.GregorianCalendar;
import java.util.concurrent.TimeUnit;

import android.util.Log;

import com.datenkrake.Constants;
import com.datenkrake.DatabaseHandler;
import com.datenkrake.R;
import com.datenkrake.SolinApplication;

public class UsageTimeBadge extends Badge {

	private String TAG = getClass().getSimpleName();
	private DatabaseHandler db = SolinApplication.getDbHandler();
	
	// 12 weeks in hours
	private final int gold = 3000;
	// 4 weeks in hours
	private final int silver = 672;
	// 1 week in hours
	private final int bronze = 168;
	
	/**
	 * Only instantiate from BadgeHandler
	 * @param name
	 */
	public UsageTimeBadge(String name) {
		super(name);
		progress = db.getBadgeProgress(name);
		updateStatus();
	}

	/**
	 * This method calculates the current time the app was installed.
	 * @param value No parameter needed. Just call for recalculation
	 */
	@Override
	public void updateProgress(Object arg) {
		if (progress < gold) {
			GregorianCalendar now = new GregorianCalendar();
			long time = now.getTimeInMillis() - SolinApplication.getTimeInstalled();
			progress = (int) TimeUnit.HOURS.convert(time, TimeUnit.MILLISECONDS);
			db.saveBadgeProgress(name, progress);
			updateStatus();
			Log.i(TAG, "Installed for: " + progress + " hours");
		}
		else {
			Log.i(TAG, "UsageTime not updated. Already goldlevel!");
		}
		
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void updateStatus() {
		if (progress < bronze) {
			badgeID = R.drawable.usage_time_none;
			badgeLevel = Constants.BADGE_NOTHING;
		}
		else if (progress >= bronze && progress < silver) {
			badgeID = R.drawable.usage_time_bronze;
			badgeLevel = Constants.BADGE_BRONZE;
		}
		else if (progress >= silver && progress < gold) {
			badgeID = R.drawable.usage_time_silver;
			badgeLevel = Constants.BADGE_SILVER;
		}
		else {
			badgeID = R.drawable.usage_time_gold;
			badgeLevel = Constants.BADGE_GOLD;
		}
	}

	@Override
	public int getBadgeId() {
		updateProgress(null);
		return badgeID;
	}
}
