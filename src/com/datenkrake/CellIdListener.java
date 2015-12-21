package com.datenkrake;

import java.util.GregorianCalendar;

import android.telephony.CellLocation;
import android.telephony.PhoneStateListener;
import android.telephony.gsm.GsmCellLocation;
import android.util.Log;

/**
 * 
 * @author Thomas
 * 
 */
public class CellIdListener extends PhoneStateListener {

	private final String TAG = getClass().getSimpleName();
	private static DatabaseHandler db;
	private static int areaId;
	private static boolean logging = false;
	private GregorianCalendar time;

	public CellIdListener() {
		db = SolinApplication.getDbHandler();
		// 1 for "Unbekannt" since the first row in sqlite is a 1 and "Unbekannt" is the first
		areaId = 1;
	}

	@Override
	public void onCellLocationChanged(CellLocation location) {
		GsmCellLocation gsmLoc = (GsmCellLocation) location;
		if (!(gsmLoc.getCid() == -1) && !(gsmLoc.getCid() == Integer.MAX_VALUE)
				&& !(gsmLoc.getPsc() == Integer.MAX_VALUE)
				&& !(gsmLoc.getLac() == Integer.MAX_VALUE)
				&& !(gsmLoc.getLac() == 0)) {
			super.onCellLocationChanged(location);
			Log.i(TAG, gsmLoc.getLac() + " " + gsmLoc.getCid() + " " + gsmLoc.getPsc());
			String locationString = location.toString();
			if (logging) {
				Log.i(TAG, "Cell ID changed to: " + locationString + "\nWritten AreaID: " + areaId + " and CellId: " + locationString);
				db.addAreaValue(locationString, areaId, true);
			} else {
				db.addAreaValue(locationString, areaId, false);
			}

			// add to location history in DB
			time = new GregorianCalendar();
			db.addRecentCell(locationString, time.getTimeInMillis());
		}
	}

	/**
	 * Set the id of the area to be learned. 1 = Unbekannt, 2 = Zu
	 * Hause/Freizeit, 3 = Arbeit/Uni
	 * 
	 * @param id
	 */
	public static void setAreaIdForLogging(int id) {
		if (id == 1) {
			logging = false;
		} else {
			logging = true;
		}
		areaId = id;
	}

	public static void addLastCellToArea(int areaId) {
		Log.i("CellIdListener", "Adding the last known cell to the area");
		Cell cell = db.getLastKnownCells(1).get(0);
		// update the cell object as well
		// RecentCellsFragment.recentCells.getFirst().setArea(db.getArea(areaId));
		// RecentCellsFragment.recentCells.getFirst().setAreaId(areaId);
		db.addAreaValue(cell.getLocationString(), areaId, true);
	}
}
