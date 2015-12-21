package com.datenkrake;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;

/**
 * @deprecated Not useful anymore
 * This class provides a single method for retrieving the last known location of the user
 * @author Thomas
 *
 */

@Deprecated
public class LocationReceiver {
	
	/**
	 * Get the last known network(coarse) location of the user
	 * @return
	 */
	public static Location getLocation(Context c) {
		Location network_loc = null;
		
		// maybe listener listening for updates? actually only one location needed
		LocationManager lm = (LocationManager) c.getSystemService(Context.LOCATION_SERVICE);
		// check if location available
		if (lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)){
			network_loc = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
		}
		
		return network_loc;
	}
}
