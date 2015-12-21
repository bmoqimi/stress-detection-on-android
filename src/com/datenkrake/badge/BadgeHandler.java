package com.datenkrake.badge;

import java.util.ArrayList;

import com.datenkrake.Constants;

public class BadgeHandler {

	private static BadgeHandler instance;
	
	private static StressAnsweredBadge stress;
	private static ContactsSetupBadge contacts;
	private static UsageTimeBadge usageTime;
	
	/**
	 * Singleton
	 * @return The instance to work with
	 */
	public static BadgeHandler getInstance() {
		if (instance == null) {
			instance = new BadgeHandler();
		}
		return instance;
	}
	
	/**
	 * Private constructor for singleton
	 */
	private BadgeHandler() {
		if (stress == null) {
			stress = new StressAnsweredBadge(Constants.BADGE_NAME_STRESSANSWER);
		}
		if (contacts == null) {
			contacts = new ContactsSetupBadge(Constants.BADGE_NAME_CONTACTSSETUP);
		}
		if (usageTime == null) {
			usageTime = new UsageTimeBadge(Constants.BADGE_NAME_USAGETIME);
		}
	}
	
	/**
	 * Returns all badges that exist:
	 * 0 - {@link StressAnsweredBadge};
	 * 1 - {@link ContactsSetupBadge};
	 * 2 - {@link UsageTimeBadge};
	 * @return An ArrayList containing all the badge objects in the system
	 */
	public ArrayList<Badge> getBadges() {
		ArrayList<Badge> result = new ArrayList<Badge>();
		result.add(stress);
		result.add(contacts);
		result.add(usageTime);
		return result;
	}
}
