package com.datenkrake.badge;

import com.datenkrake.Constants;
import com.datenkrake.DatabaseHandler;
import com.datenkrake.R;
import com.datenkrake.SolinApplication;

import android.util.Log;

public class ContactsSetupBadge extends Badge {

	private String TAG = getClass().getSimpleName();
	private DatabaseHandler db = SolinApplication.getDbHandler();
	
	private final int gold = 100;
	
	/**
	 * Only instantiate from BadgeHandler
	 * @param name
	 */
	public ContactsSetupBadge(String name) {
		super(name);
		progress = db.getBadgeProgress(name);
		updateStatus();
	}

	/**
	 * When this method is called, it means that the important group is configured. Nothing else can be made.
	 * @param No parameter needed. Just call for updating to successfully configured group
	 */
	@Override
	public void updateProgress(Object arg) {
		Log.i(TAG, "Contacts configured!");
		progress = 100;
		db.saveBadgeProgress(name, 100);
		Log.i(TAG, "Progress set to 100. Now: " + progress);
		updateStatus();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void updateStatus() {
		if (progress == gold) {
			badgeID = R.drawable.contacts_configured_gold;
			badgeLevel = Constants.BADGE_GOLD;
		}
		else {
			badgeID = R.drawable.contacts_configured_none;
			badgeLevel = Constants.BADGE_NOTHING;
		}
	}

}
