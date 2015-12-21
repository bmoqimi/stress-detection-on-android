package com.datenkrake.badge;

import com.datenkrake.Constants;
import com.datenkrake.DatabaseHandler;
import com.datenkrake.R;
import com.datenkrake.SolinApplication;

import android.util.Log;

public class StressAnsweredBadge extends Badge {
	
	private String TAG = getClass().getSimpleName();
	private DatabaseHandler db = SolinApplication.getDbHandler();
	
	private final int bronze = 25;
	private final int silver = 50;
	private final int gold = 75;
	

	/**
	 * Only instantiate from BadgeHandler
	 * @param name
	 */
	public StressAnsweredBadge(String name) {
		super(name);
		progress = db.getBadgeProgress(name);
		updateStatus();
	}

	/**
	 * Updates the progress the user has made answering the stress notifications
	 * @param value Minutes that have passed from asked to answered
	 */
	@Override
	public void updateProgress(Object arg) {
		long value;
		if (arg instanceof Long) {
			value = (Long) arg;
		}
		else {
			Log.i(TAG, "Wrong parameter given! Integer value needed!");
			return;
		}
		Log.i(TAG, value + " minutes later answered.");
		if (progress < gold) {
			if (value < 5) {
				progress += 5;
				db.saveBadgeProgress(name, 5);
				Log.i(TAG, "Progress + 5. Now: " + progress);
			}
			else if (value >= 5 && value < 10) {
				progress += 3;
				db.saveBadgeProgress(name, 3);
				Log.i(TAG, "Progress + 3. Now: " + progress);
			}
			else if (value >= 10 && value < 20) {
				progress += 2;
				db.saveBadgeProgress(name, 2);
				Log.i(TAG, "Progress + 2. Now: " + progress);
			}
			else if (value >= 20 && value < 60) {
				progress += 1;
				db.saveBadgeProgress(name, 1);
				Log.i(TAG, "Progress + 1. Now: " + progress);
			}
			else {
				Log.i(TAG, "Progress stays. Now: " + progress);
			}
			updateStatus();
		}
		else {
			Log.i(TAG, value + "Nothing added to StressAnswered. Already goldlevel!");
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void updateStatus() {
		if (progress < bronze) {
			badgeID = R.drawable.notification_answered_none;
			badgeLevel = Constants.BADGE_NOTHING;
		}
		else if (progress >= bronze && progress < silver) {
			badgeID = R.drawable.notification_answered_bronze;
			badgeLevel = Constants.BADGE_BRONZE;
		}
		else if (progress >= silver && progress < gold) {
			badgeID = R.drawable.notification_answered_silver;
			badgeLevel = Constants.BADGE_SILVER;
		}
		else {
			badgeID = R.drawable.notification_answered_gold;
			badgeLevel = Constants.BADGE_GOLD;
		}
	}
}
