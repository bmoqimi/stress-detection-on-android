package com.datenkrake;

import android.app.Application;
import android.content.pm.PackageManager.NameNotFoundException;
import android.util.Log;

import org.acra.*;
import org.acra.annotation.*;

import com.datenkrake.badge.BadgeHandler;

/**
 * 
 * @author Thomas
 *
 */

@ReportsCrashes(formKey = "", // will not be used
formUri = "http://130.83.245.98:8015/MAB-LAB-1.3.3-Helen/report/report.php",
reportType = org.acra.sender.HttpSender.Type.JSON,
mode = ReportingInteractionMode.TOAST,
forceCloseDialogAfterToast = false,
resToastText = R.string.crash_toast_text)
public class SolinApplication extends Application {

	private static DatabaseHandler db_handler;
	private final String TAG = getClass().getSimpleName();
	private static long installed;
	
	@Override
    public void onCreate() {
        super.onCreate();

        // The following line triggers the initialization of ACRA
        ACRA.init(this);
        db_handler = DatabaseHandler.getInstance(this);
        // initializes the badges
        BadgeHandler.getInstance();
        CsvHandler csv = new CsvHandler();
        csv.createInitialCsvs(this);
		db_handler.addArea("Unbekannt");
		db_handler.addArea("Zu Hause/Freizeit");
		db_handler.addArea("Arbeit/Uni");
		Util.updateIdOfImportantGroup(this);
		installed = timeInstalled();
    }

	/**
	 * @return the db_handler
	 */
	public static DatabaseHandler getDbHandler() {
		return db_handler;
	}
	
	public static long getTimeInstalled() {
		return installed;
	}
	
	public long timeInstalled() {
		long installed = 0;
		try {
			installed = getPackageManager().getPackageInfo("com.datenkrake", 0).firstInstallTime;
		} catch (NameNotFoundException e) {
			Log.i(TAG, e.getMessage());
		}
		return installed;
	}

}
