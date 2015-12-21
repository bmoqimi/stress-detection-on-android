package com.datenkrake;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

import com.datenkrake.badge.BadgeHandler;
import com.datenkrake.badge.ContactsSetupBadge;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CallLog;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.GroupMembership;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.PhoneLookup;
import android.provider.Settings.Secure;
import android.telephony.SmsMessage;
import android.util.Log;

/**
 * Utility class for writing communication flow data and retrieving certain
 * contacts data
 * 
 * @author Thomas
 * 
 */
public class Util {

	private CsvHandler csv = new CsvHandler();
	private final String TAG = getClass().getSimpleName();
	private static String importantGroupID = "";
	private DatabaseHandler db = SolinApplication.getDbHandler();
	private GregorianCalendar now;

	/**
	 * Method being called when a telephone call ends to write the call data
	 * 
	 * @param context
	 */

	public void writeCallData(Context context) {

		// sleep needed or otherwise last but one call is saved
		// should be no problem, because this statereceiver is VERY unlikely to
		// get a call in this very second and thread would be sleeping?
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			Log.e(TAG, e.getMessage());
		}

		// reading call details
		Uri calls = CallLog.Calls.CONTENT_URI;
		Cursor c = context.getContentResolver().query(calls, null, null, null,
				null);
		int numberIndex = c.getColumnIndex(CallLog.Calls.NUMBER);
		int dateIndex = c.getColumnIndex(CallLog.Calls.DATE);
		int durationIndex = c.getColumnIndex(CallLog.Calls.DURATION);
		c.moveToLast();
		String numberString = c.getString(numberIndex);
		long dateTime = c.getLong(dateIndex);
		String durationString = c.getString(durationIndex);
		c.close();

		Log.i(TAG, "Number of the caller: " + numberString);
		
		String[] contactData;
		boolean important = false;
		// unterdrückte nummer aussschließen
		if (!numberString.equals("")) {
			contactData = getContactNameAndIdByNumber(numberString,	context);
			important = isContactImportant(context, contactData[1]);
			numberString = normalizeNumber(numberString);
		}
		else {
			contactData = new String[] {"Private Number"};
		}

		String contactHash = getContactHash(contactData[0], numberString);

		String date = getFormattedDate(dateTime);
		Date d = new Date(Long.valueOf(dateTime));

		csv.appendCommFlowData(context, "Call", contactHash, date,
				durationString, important, getCurrentArea(),
				BackgroundService.isProbablyAtBody());
		db.addCommFlow("Call", contactHash, d,
				durationString, important, getCurrentArea(),
				BackgroundService.isProbablyAtBody());
	}

	/**
	 * Method being called when a SMS is received to write the data
	 * 
	 * @param context
	 * @param intent
	 */
	public void writeSmsData(Context context, Intent intent) {

		Bundle bundle = intent.getExtras();
		try {
			String number = null;
			SmsMessage currentMessage = null;
			String phoneNumber;
			if (bundle != null) {
				final Object[] pdusObj = (Object[]) bundle.get("pdus");

				currentMessage = SmsMessage.createFromPdu((byte[]) pdusObj[0]);
				phoneNumber = currentMessage.getDisplayOriginatingAddress();
				number = phoneNumber;
			}

			String[] contactData = getContactNameAndIdByNumber(number, context);
			boolean important = isContactImportant(context, contactData[1]);

			number = normalizeNumber(number);

			String dateTime = getFormattedDate(-1);
			String contactHash = getContactHash(contactData[0], number);

			Date d = new Date(System.currentTimeMillis());
			
			csv.appendCommFlowData(context, "SMS", contactHash, dateTime, null,
					important, getCurrentArea(),
					BackgroundService.isProbablyAtBody());
			
			db.addCommFlow("SMS", contactHash, d,
					null, important, getCurrentArea(),
					BackgroundService.isProbablyAtBody());
		} catch (Exception e) {
			Log.e(TAG, "Exception smsReceiver");
		}
	}

	/**
	 * Method being called when a mail is received to write the data
	 * 
	 * @param context
	 * @param name Name of the sender of the mail
	 */
	public void writeMailData(Context context, String name) {

		if (name == null) {
			name = "";
		}

		boolean important = false;

		String number = getMobileNumberbyContactDisplayName(context, name);

		if (!number.equals("")) {
			String[] contactData = getContactNameAndIdByNumber(number, context);
			important = isContactImportant(context, contactData[1]);
		}
		number = normalizeNumber(number);

		String dateTime = getFormattedDate(-1);
		String contactHash = getContactHash(name, number);

		Date d = new Date(System.currentTimeMillis());
		
		csv.appendCommFlowData(context, "Mail", contactHash, dateTime, null,
				important, getCurrentArea(),
				BackgroundService.isProbablyAtBody());
		db.addCommFlow("Mail", contactHash, d, null,
				important, getCurrentArea(),
				BackgroundService.isProbablyAtBody());
	}


	/**
	 * Method being called when a Whatsapp/Threema/Hangout message is received to write the data
	 * @param context
	 * @param name
	 * @param messenger
	 */
	public void writeMessengerData(Context context, String name, String messenger) {
		boolean important = false;

		String dateTime = getFormattedDate(-1);

		String number = getMobileNumberbyContactDisplayName(context, name);
		if (!number.equals("")) {
			String[] contactData = getContactNameAndIdByNumber(number, context);
			important = isContactImportant(context, contactData[1]);
		}
		
		number = normalizeNumber(number);
		String contactHash = getContactHash(name, number);
		
		Date d = new Date(System.currentTimeMillis());
		
		csv.appendCommFlowData(context, messenger, contactHash, dateTime, null,
				important, getCurrentArea(),
				BackgroundService.isProbablyAtBody());
		db.addCommFlow(messenger, contactHash, d,
				null, important, getCurrentArea(),
				BackgroundService.isProbablyAtBody());
	}

	/**
	 * Write display state
	 * @param context
	 * @param screenState
	 */
	public void writeDisplayData(Context context, String screenState) {

		String dateTime = getFormattedDate(-1);

		Date d = new Date(System.currentTimeMillis());
		
		// screen status change is logged as "unimportant"
		csv.appendCommFlowData(context, screenState, null, dateTime, null,
				false, getCurrentArea(), BackgroundService.isProbablyAtBody());
		db.addCommFlow(screenState, null, d, null,
				false, getCurrentArea(), BackgroundService.isProbablyAtBody());
	}

	public void writeHeartbeatData(Context context, int heartbeat,
			double instantSpeed) {
		csv.appendHeartbeatData(context, getFormattedDate(-1),
				String.valueOf(heartbeat), String.valueOf(instantSpeed));
		now = new GregorianCalendar();
		db.addHeartrateData(now, heartbeat, instantSpeed);
	}
	
	/**
	 * Gives you a formatted Date as String
	 * @param date A milliseconds value to instantiate the Date object with. Put -1 for current date.
	 * @return
	 */
	public static String getFormattedDate(long date) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",
				Locale.GERMANY);
		// date from e.g. CallLog given
		if (date >= 0) {
			Date d = new Date(date);
			return sdf.format(d);
		}
		// get current calendar date
		else {
			return sdf.format(Calendar.getInstance().getTime());
		}
	}

	/**
	 * Retrieves the mobile number corresponding to a contacts name.
	 * @param context
	 * @param name The name of the contact
	 * @return The number of the contact or empty string, if no contact found
	 */
	public String getMobileNumberbyContactDisplayName(Context context, String name) {
		//
		// Find contact based on name.
		//
		
		ContentResolver cr = context.getContentResolver();
		String mobileNumber = null;
		String number = null;
		Cursor cursor = cr.query(ContactsContract.Contacts.CONTENT_URI, null,
				"DISPLAY_NAME = '" + name + "'", null, null);
		while (cursor.moveToNext()) {
			String contactId = cursor.getString(cursor
					.getColumnIndex(ContactsContract.Contacts._ID));
			//
			// Get all phone numbers.
			//
			Cursor phones = cr.query(Phone.CONTENT_URI, null, Phone.CONTACT_ID
					+ " = " + contactId, null, null);
			while (phones.moveToNext()) {
				number = phones.getString(phones.getColumnIndex(Phone.NUMBER));
				int type = phones.getInt(phones.getColumnIndex(Phone.TYPE));
				switch (type) {
				case Phone.TYPE_HOME:
					Log.i(TAG, "Home number found: " + number);
					break;
				case Phone.TYPE_MOBILE:
					Log.i(TAG, "Mobile number found: " + number);
					mobileNumber = number;
					break;
				case Phone.TYPE_WORK:
					Log.i(TAG, "Work number found: " + number);
					break;
				default:
					Log.i(TAG, "Default (not known type) Number found: "
							+ number);
					break;
				}
			}

			// if no number was saved as mobile number by the user take another
			// one found regardless which type it is
			if (mobileNumber == null && number != null) {
				Log.i(TAG, "No mobile number found, using another one found: "
						+ number);
				mobileNumber = number;
			}
			phones.close();
		}

		if (mobileNumber == null) {
			Log.i(TAG, "No contact number found");
			mobileNumber = "";
		}
		cursor.close();

		return mobileNumber;
	}

	public static boolean updateIdOfImportantGroup(Context context) {
		String[] Group_Projection = new String[] { ContactsContract.Groups._ID,
				ContactsContract.Groups.TITLE };
		Cursor groupCursor = context.getContentResolver().query(
				ContactsContract.Groups.CONTENT_URI, Group_Projection, null,
				null, null);

		String name = "";
		String id = "";
		boolean importantGroupExists = false;

		// read all groups for ID of "Wichtig"
		while (groupCursor.moveToNext()) {
			id = groupCursor.getString(0);
			name = groupCursor.getString(1);
			if (name.contains("Wichtig")) {
				importantGroupID = id;
				importantGroupExists = true;
			}
		}

		groupCursor.close();

		if (importantGroupExists) {
			Log.i("Util", "ID of the important group has been updated to " + id);
			ContactsSetupBadge c = (ContactsSetupBadge) BadgeHandler.getInstance().getBadges().get(1);
			c.updateProgress(null);
		} else {
			Log.i("Util", "No important group found");
		}

		return importantGroupExists;
	}

	public static String[] getAllImportantContacts(Context context) {
		// read all contacts with the above group id
		// this Contacts._ID is DIFFERENT from the actual contact id
		String[] ContactProjection = new String[] { Contacts.DISPLAY_NAME,
				Contacts._ID };
		Cursor contactCursor = context.getContentResolver().query(
				Data.CONTENT_URI, ContactProjection,
				GroupMembership.GROUP_ROW_ID + " = ?",
				new String[] { importantGroupID }, Phone.DISPLAY_NAME + " ASC");

		String[] contacts = new String[contactCursor.getCount()];
		int i = 0;
		while (contactCursor.moveToNext()) {
			contacts[i] = contactCursor.getString(0) + "\n";
			i++;
		}

		contactCursor.close();
		return contacts;
	}

	public void refreshCsvs(Context context) {
		csv.createInitialCsvs(context);
	}

	public static String getAndroidID(Context context) {
		return Secure
				.getString(context.getContentResolver(), Secure.ANDROID_ID);
	}

	public boolean deleteFile(String filename) {
		File file = new File("data/data/com.datenkrake/files" + filename);
		if (file.delete()) {
			System.out.println(filename + " is deleted!");
			return true;
		} else {
			System.out.println("Delete operation of " + filename
					+ " is failed.");
			return false;
		}
	}

	public static String bytesToHex(byte[] bytes) {
		char[] hexArray = "0123456789abcdef".toCharArray();
		char[] hexChars = new char[bytes.length * 2];
		for (int j = 0; j < bytes.length; j++) {
			int v = bytes[j] & 0xFF;
			hexChars[j * 2] = hexArray[v >>> 4];
			hexChars[j * 2 + 1] = hexArray[v & 0x0F];
		}
		return new String(hexChars);
	}
	
	public static String getAppName (Context context, String packageName) {
		PackageManager pm = context.getPackageManager();
		ApplicationInfo ai;
        try {
            ai = pm.getApplicationInfo(packageName, 0);
        } catch (NameNotFoundException e) {
            ai = null;
        }
		String applicationName = (String) (ai != null ? pm.getApplicationLabel(ai) : "(unknown)");
		
		return applicationName;
	}

	private String getCurrentArea() {
		return db.getLastKnownCells(1).get(0).getArea();
	}

	private boolean isContactImportant(Context context, String id) {

		Cursor dataCursor = context
				.getContentResolver()
				.query(ContactsContract.Data.CONTENT_URI,
						new String[] { ContactsContract.Data.CONTACT_ID,
								ContactsContract.Data.DATA1 },
						ContactsContract.Data.MIMETYPE + "=? AND "
								+ ContactsContract.Data.CONTACT_ID + "=" + id,
						new String[] { ContactsContract.CommonDataKinds.GroupMembership.CONTENT_ITEM_TYPE },
						null);
		while (dataCursor.moveToNext()) {
			if (dataCursor.getString(1).equals(importantGroupID)) {
				Log.i(TAG,
						"Contact with ID: " + dataCursor.getString(0)
								+ " is in imporant group (ID): "
								+ dataCursor.getString(1));
				dataCursor.close();
				return true;
			}

		}
		dataCursor.close();
		return false;
	}

	/**
	 * Retrieves the name and its contact id of a given phone number
	 * @param number The number to be searched for
	 * @param context
	 * @return The name and ID of the contact
	 */
	private String[] getContactNameAndIdByNumber(String number, Context context) {
		Uri uri = Uri.withAppendedPath(
				ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
				Uri.encode(number));
		String[] data = new String[2];

		ContentResolver contentResolver = context.getContentResolver();
		Cursor contactLookup = contentResolver.query(uri, new String[] {
				ContactsContract.PhoneLookup._ID,
				ContactsContract.PhoneLookup.DISPLAY_NAME }, null, null, null);

		try {
			if (contactLookup != null && contactLookup.getCount() > 0) {
				contactLookup.moveToNext();
				data[0] = contactLookup.getString(contactLookup
						.getColumnIndex(ContactsContract.Data.DISPLAY_NAME));
				data[1] = contactLookup.getString(contactLookup
						.getColumnIndex(PhoneLookup._ID));
				Log.i(TAG, "Name: " + data[0] + " with ID: " + data[1]
						+ " found");
			}
		} finally {
			if (contactLookup != null) {
				contactLookup.close();
			}
		}

		return data;
	}

	/**
	 * This method transforms all "+49" to "0" and removes unnecessary blank
	 * spaces
	 * 
	 * @param number
	 * @return
	 */
	private String normalizeNumber(String number) {
		String normalizedNumber = null;

		normalizedNumber = number.replace("+49", "0");
		normalizedNumber = normalizedNumber.replace(" ", "");

		return normalizedNumber;
	}

	private String getContactHash(String name, String number) {
		String result = "";
		String input = name + number;
		try {
			MessageDigest md = MessageDigest.getInstance("SHA-512");
			md.update(input.getBytes("UTF-8"));
			byte byteData[] = md.digest();
			result = bytesToHex(byteData);
			Log.i(TAG, "SHA-512 of " + name + " + " + number + " is: " + result);
		} catch (NoSuchAlgorithmException e) {
			Log.e(TAG, e.getMessage());
		} catch (UnsupportedEncodingException e) {
			Log.e(TAG, e.getMessage());
		}
		return result;
	}
}
