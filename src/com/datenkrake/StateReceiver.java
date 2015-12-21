package com.datenkrake;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.TelephonyManager;
import android.util.Log;

/**
 * This receiver provides the start of the service at boot time and monitors incoming phone calls and SMS
 * @author Thomas
 *
 */
public class StateReceiver extends BroadcastReceiver {
	
	private Util util = new Util();
	private final String TAG = getClass().getSimpleName();
	Context gcontext;
	static boolean offhook = false;

	@Override
	public void onReceive(Context context, Intent intent) {

		gcontext = context;
		
		// starts the service at boot time
		if ("android.intent.action.BOOT_COMPLETED".equals(intent.getAction())){
			Intent serviceIntent = new Intent(context, BackgroundService.class);
			
			// Note that multiple calls to Context.startService() do not nest (though they do result in multiple corresponding calls to onStartCommand()),
			// so no matter how many times it is started a service will be stopped once Context.stopService() or stopSelf() is called;
			context.startService(serviceIntent);
			Log.i(TAG, "Service created after boot completion");
		}
		
		// when user's call state changes
		
		if ("android.intent.action.PHONE_STATE".equals(intent.getAction())){
			String state = intent.getStringExtra(TelephonyManager.EXTRA_STATE);
			// to make sure only calls that have been taken or self dialed are saved to the csv, and not ringing calls
			if (TelephonyManager.EXTRA_STATE_OFFHOOK.equals(state)) {
				Log.i(TAG, state);
				offhook = true;
			}
            if (TelephonyManager.EXTRA_STATE_IDLE.equals(state) && offhook) {
            	Log.i(TAG, "writing call data");
                util.writeCallData(gcontext);
                offhook = false;
            }
		}	
		
		// when user received a SMS
		if ("android.provider.Telephony.SMS_RECEIVED".equals(intent.getAction())){
			util.writeSmsData(context, intent);
		}
	}
	
	

}
