package com.datenkrake;

import android.app.Notification;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

/**
 * This service captures all relevant notifications of the users communication like mail or whatsapp
 * @author Thomas
 *
 */
public class NotificationService extends NotificationListenerService {

	private final String TAG = getClass().getSimpleName();
	private Util util = new Util();

	@Override
	public void onNotificationPosted(StatusBarNotification sbn) {
		
		if (sbn.getPackageName().equals("com.google.android.gm") || sbn.getPackageName().equals("com.google.android.email")) {
			
			//Android API 19 needed
			String title = sbn.getNotification().extras.getString(Notification.EXTRA_TITLE);
			if (title.contains("neue Nachrichten")) {
				util.writeMailData(this, null);
			}
			else {
				util.writeMailData(this, title);
				Log.i(TAG, "Title of mail: " + title);
			}
		}
		
		if (sbn.getPackageName().equals("ch.threema.app")) {
			String ticker = sbn.getNotification().tickerText.toString();
			Log.i(TAG, "Threema: " + ticker);

			String sender = getSenderName(ticker);
			
			Log.i(TAG, "Threema sender: " + sender);
			util.writeMessengerData(this, sender, "Threema");
		}
		
		if (sbn.getPackageName().equals("com.google.android.talk")) {
			String ticker = sbn.getNotification().tickerText.toString();
			Log.i(TAG, "Hangout: " + ticker);

			String sender = getSenderName(ticker);
			
			Log.i(TAG, "Hangout sender: " + sender);
			util.writeMessengerData(this, sender, "Hangout");
		}
		
		if (sbn.getPackageName().equals("com.skype.raider")) {
			String ticker = sbn.getNotification().tickerText.toString();
			Log.i(TAG, "Skype: " + ticker);

			String lines[] = ticker.split("\\r?\\n");
			
			Log.i(TAG, "Skype sender: " + lines[0]);
			util.writeMessengerData(this, lines[0], "Skype");
		}
		
		if (sbn.getPackageName().equals("com.whatsapp")) {
			String ticker = sbn.getNotification().tickerText.toString();
			Log.i(TAG, "Whatsapp: " + ticker);
			String sender;
			// german
			if (ticker.startsWith("Nachricht von ")) {
				sender = ticker.substring(14, ticker.length());
			}
			// english
			else if (ticker.startsWith("Message from ")) {
				sender = ticker.substring(13, ticker.length());
			}
			else {
				sender = getSenderName(ticker);
			}

			Log.i(TAG, "Whatsapp sender: " + sender);
			util.writeMessengerData(this, sender, "Whatsapp");
		}
		// NOT TESTED
		if (sbn.getPackageName().equals("org.telegram.messenger")) {
			String ticker = sbn.getNotification().tickerText.toString();
			Log.i(TAG, "Telegram: " + ticker);

			String sender = getSenderName(ticker);
			
			Log.i(TAG, "Telegram sender: " + sender);
			util.writeMessengerData(this, sender, "Telegram");
		}
		
		if (sbn.getPackageName().equals("com.facebook.orca")) {
			String sender = sbn.getNotification().extras.getString(Notification.EXTRA_TITLE);
			
			Log.i(TAG, "Facebook sender: " + sender);
			util.writeMessengerData(this, sender, "Facebook");
		}
	}

	private String getSenderName(String ticker) {
		int indexDelimiter = ticker.indexOf(':');
		if (indexDelimiter == -1) {
			Log.i(TAG, "Sender can't be determined reliably -> putting general message");
			return "General Message";
		}
		String sender = ticker.substring(0, indexDelimiter);
		return sender;
	}

	@Override
	public void onNotificationRemoved(StatusBarNotification arg0) {
		
	}

}
