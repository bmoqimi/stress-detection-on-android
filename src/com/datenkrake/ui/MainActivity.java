package com.datenkrake.ui;

import java.lang.ref.WeakReference;
import java.util.Set;

import zephyr.android.HxMBT.BTClient;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.datenkrake.ActivityBroadcastReceiver;
import com.datenkrake.ActivityRecognitionService;
import com.datenkrake.Alerts;
import com.datenkrake.BackgroundService;
import com.datenkrake.Constants;
import com.datenkrake.NewConnectedListener;
import com.datenkrake.R;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.ActivityRecognitionClient;

/**
 * MainActivity.
 * Container-Activity for all Content-Fragments. Handles connections with Zephyr and GooglePlayServices.
 * 
 * TODO: still problem when User gets out of reach or takes the sensor off (UI is ok but see Logcat-Output!!)
 * TODO: Better User-Feedback (status field currently unused, Progressbar??)
 * 
 * @author Thomas
 */
public class MainActivity extends Activity implements GooglePlayServicesClient.ConnectionCallbacks, GooglePlayServicesClient.OnConnectionFailedListener {
	private String TAG = getClass().getSimpleName();
	
	// Google Activity Recognition
	private ActivityRecognitionClient arClient;
	private BroadcastReceiver activityReceiver;
	private PendingIntent pIntent;
	
	// NavigationDrawer
    private String[] drawerItems;
    private String[] drawerHelpDialogs;
    private DrawerLayout drawerLayout;
    private ListView drawerList;
    private ActionBarDrawerToggle drawerToggle;
    private CharSequence drawerTitle; 		// displayed in ActionBar when drawer is open
    private CharSequence drawerItemTitle; 	// displayed in ActionBar when drawer is closed
	private String currentDrawerItemHelpDialog;
    
    // Zephyr
	private BTConnectedReceiver btConnectionStateReceiver;
	//private BTBondReceiver btBondStateReceiver;
	//private BTBroadcastReceiver btBroadcastReceiver;
    private boolean disconnectOnPurpose = false;
    private boolean zephyrConnected = false;
	private BluetoothAdapter adapter = null;
	private BTClient _bt;
	private NewConnectedListener _NConnListener;
	private ThreadToUIHandler Newhandler = new ThreadToUIHandler(this);
    TextView zephyrStatus = null;
    Switch zephyrSwitch = null;
    TextView zephyrHeartRate = null;
    TextView zephyrInstantSpeed = null;

    @Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);
		
        // Background Service
		Intent serviceIntent = new Intent(this, BackgroundService.class);
		startService(serviceIntent);
		Log.i(TAG, "Service started from MainActivity");
		
		// Google Play Services
		int response = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
		if (response == ConnectionResult.SUCCESS) {
			arClient = new ActivityRecognitionClient(this, this, this);
			arClient.connect();
			Log.i(TAG, "Google play services is installed!");
		}
		else {
			Toast.makeText(this, "Bitte Google Play Services installieren!", Toast.LENGTH_SHORT).show();
			Log.i(TAG, "Google play services is NOT installed!");
		}
		activityReceiver = new ActivityBroadcastReceiver();
		IntentFilter filter = new IntentFilter();
		filter.addAction("com.datenkrake.ACTIVITY_RECOGNITION_DATA");
		registerReceiver(activityReceiver, filter);
	    
	    // NavigationDrawer
        drawerItems = getResources().getStringArray(R.array.navigation_drawer_items);
        drawerHelpDialogs = getResources().getStringArray(R.array.help_dialogs);
        drawerLayout = (DrawerLayout) findViewById(R.id.activity_main_drawer_layout);
        drawerList = (ListView) findViewById(R.id.activity_main_navigation_drawer);
        drawerList.setAdapter(new ArrayAdapter<String>(this,
                R.layout.drawer_item, drawerItems));
        drawerList.setOnItemClickListener(new DrawerItemClickListener());
		
        // Interaction of Drawer with ActionBar
		drawerItemTitle = drawerTitle = getTitle();
        drawerToggle = new ActionBarDrawerToggle(this, drawerLayout,
                R.drawable.ic_drawer, R.string.drawer_open, R.string.drawer_close) {
        	
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                getActionBar().setTitle(drawerItemTitle);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                getActionBar().setTitle(drawerTitle);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };
        drawerLayout.setDrawerListener(drawerToggle);	// Set the drawer-toggle as the drawer-listener
        selectItem(0);									// show OverviewFragment as first Content-Fragment
        
        // Zephyr
		//btBondStateReceiver = new BTBondReceiver();
		//btBroadcastReceiver = new BTBroadcastReceiver();
		btConnectionStateReceiver = new BTConnectedReceiver();
		//IntentFilter filter1 = new IntentFilter("android.bluetooth.device.action.PAIRING_REQUEST");
		//IntentFilter filter2 = new IntentFilter("android.bluetooth.device.action.BOND_STATE_CHANGED");
		IntentFilter filter3 = new IntentFilter();
		filter3.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
		filter3.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
		filter3.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
		//getApplicationContext().registerReceiver(btBroadcastReceiver, filter1);
		//getApplicationContext().registerReceiver(btBondStateReceiver, filter2);
		getApplicationContext().registerReceiver(btConnectionStateReceiver, filter3);
		adapter = BluetoothAdapter.getDefaultAdapter();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// add menu items to ActionBar
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	/* Called whenever we call invalidateOptionsMenu() */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        boolean drawerOpen = drawerLayout.isDrawerOpen(drawerList);
        if(drawerOpen){
        	// hide all content-related menu-items
            menu.findItem(R.id.action_help).setVisible(false);
            menu.findItem(R.id.action_manage_contacts).setVisible(false);
        } else {
        	// hide menu-items not related to current content
        	if (!currentDrawerItemHelpDialog.equals(getResources().getString(R.string.help_dialog_contacts))){
                menu.findItem(R.id.action_manage_contacts).setVisible(false);
        	}
        }
        return super.onPrepareOptionsMenu(menu);
    }

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// drawer toggle clicked?
		if (drawerToggle.onOptionsItemSelected(item)) {
			return true;
		}
		// other action bar items clicked
		switch (item.getItemId()) {
		case R.id.action_settings:
			Intent intent = new Intent(this, SettingsActivity.class);
			startActivity(intent);
			return true;
		case R.id.action_manage_contacts:
			ContactsFragment.showGroupChooser(this);
			return true;
		case R.id.action_help:
			Alerts.showSimpleOKDialog(this, getResources().getString(R.string.action_help), currentDrawerItemHelpDialog);
			return true;
		case R.id.action_about:
			Alerts.showAboutDialog(this);
			return true;
		case R.id.action_feedback:
			Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
			emailIntent.setType("plain/text");
			emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL,
					new String[] { getResources().getString(
							R.string.mailing_list) });
			emailIntent.putExtra(
							android.content.Intent.EXTRA_SUBJECT,
							(getResources().getString(
									R.string.mailing_list_subject)
									+ " " + getResources().getString(
									R.string.app_name)));
			startActivity(Intent.createChooser(emailIntent, getResources()
					.getString(R.string.send_mail)));
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	/* Called by the system when the device configuration changes while your activity is running */
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		drawerToggle.onConfigurationChanged(newConfig);
	}
	
    /* Called when activity start-up is complete (after onStart and onRestoreInstanceState) */
	@Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        drawerToggle.syncState();
    }
    
    @Override
    protected void onDestroy() {
    	if (zephyrConnected){
    		disconnectZephyr();
    	}
    	if (arClient!=null) {
    		arClient.removeActivityUpdates(pIntent);
    		arClient.disconnect();
    	}
    	unregisterReceiver(activityReceiver);
    	//unregisterReceiver(btBondReceiver);
    	//unregisterReceiver(btBroadcastReceiver);
    	//unregisterReceiver(btConnectionStateReceiver);
    	super.onDestroy();
    }
	
	@Override
	public void onBackPressed() {
	    if (!currentDrawerItemHelpDialog.equals(getResources().getString(R.string.help_dialog_main))){
	    	selectItem(0);
	    } else{
	    	super.onBackPressed();
	    }
	}

	/* Change the title associated with this activity */
	@Override
	public void setTitle(CharSequence title) {
	    this.drawerItemTitle = title;
	    getActionBar().setTitle(title);
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		if (requestCode == Constants.REQUEST_CODE_ENABLE_BLUETOOTH) {
			if (resultCode == Activity.RESULT_OK) {
				connectZephyr();
			} else if (resultCode == Activity.RESULT_CANCELED) {
				Toast.makeText(getApplicationContext(), "Keine Verbindung möglich!", Toast.LENGTH_SHORT).show();
				setZephyrDisconnected();
			}
		}
	}
	
	/**
	 * Swaps fragments in the main content view
	 * 
	 * @param position the selected item's index in the drawerList
	 */
	private void selectItem(int position) {
	    // Create a new TabbedFragment and specify the content to show based on position
	    Fragment fragment = TabbedFragment.newInstance(this, position);
	    Bundle args = new Bundle();
	    args.putInt(TabbedFragment.ARG_ITEM_NUMBER, position);
	    fragment.setArguments(args);

	    // Insert the fragment by replacing any existing fragment
	    FragmentManager fragmentManager = getFragmentManager();
	    fragmentManager.beginTransaction()
	                   .replace(R.id.activity_main_content_frame, fragment)
	                   .commit();

	    // Highlight the selected item, update title and menu, and close the drawer
	    drawerList.setItemChecked(position, true);
	    setTitle(drawerItems[position]);
	    currentDrawerItemHelpDialog = drawerHelpDialogs[position];
	    drawerLayout.closeDrawer(drawerList);
	}
	
	/**
	 * Sends a Notification if the that the Connection to Zephyr was unintentionally interrupted. 
	 */
	private void notifyOnDisconnect() {
		NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
		int requestID = (int) System.currentTimeMillis();
		
		Intent intent = new Intent(getApplicationContext(), MainActivity.class);
		//intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP); 
		PendingIntent pIntent = PendingIntent.getActivity(getApplicationContext(), requestID, intent, PendingIntent.FLAG_UPDATE_CURRENT);

		// create LargeIcon (must be a Bitmap with special dimensions!)
        Drawable smallIcon = getResources().getDrawable(R.drawable.ic_notification);
        int iconHeight = smallIcon.getIntrinsicHeight();
        int iconWidth = smallIcon.getIntrinsicWidth();
        int bitmapHeight = (int) getResources().getDimension(android.R.dimen.notification_large_icon_height);
        int bitmapWidth = (int) getResources().getDimension(android.R.dimen.notification_large_icon_width);
		int horizontalOffset = (bitmapWidth - iconWidth) / 2;
		int verticalOffset = (bitmapHeight  -iconHeight) / 2;
        Bitmap largeIcon = Bitmap.createBitmap(bitmapWidth, bitmapHeight, Config.ARGB_8888);
		Canvas canvas = new Canvas(largeIcon);
		smallIcon.setBounds(horizontalOffset, verticalOffset, bitmapWidth - horizontalOffset, bitmapHeight - verticalOffset);
		smallIcon.draw(canvas);
		
		// build notification
		Notification n  = new Notification.Builder(getApplicationContext())
		        .setContentTitle("Datenkrake - Zephyr Sensor")
		        .setTicker("Bluetooth-Verbindung zum Zephyr Sensor unerwartet abgebrochen!")
		        .setContentText("Bluetooth-Verbindung abgebrochen!")
		        .setSmallIcon(R.drawable.ic_notification)
		        .setLargeIcon(largeIcon)
		        .setContentIntent(pIntent)
		        .setAutoCancel(true)
		        .build();

		n.defaults |= Notification.DEFAULT_VIBRATE;
		notificationManager.notify(Constants.ZEPHYR_NOTIFICATION_ID, n);
	}
	
	/**
	 * Listener for CklickEvents on NavigationDrawer
	 * 
	 * @author svenja
	 */
	private class DrawerItemClickListener implements ListView.OnItemClickListener {
	    @Override
	    public void onItemClick(AdapterView parent, View view, int position, long id) {
	        selectItem(position);
	    }
	}
	
	/*==================================== Activity Recognition Start ================================*/

	@Override
	public void onConnectionFailed(ConnectionResult arg0) {
		Toast.makeText(this, "Verbindung zu Google Play Services fehlgeschlagen", Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onConnected(Bundle arg0) {
		Intent intent = new Intent(this, ActivityRecognitionService.class);
		pIntent = PendingIntent.getService(this, 0, intent,PendingIntent.FLAG_UPDATE_CURRENT);
		arClient.requestActivityUpdates(Constants.ACTIVITY_RECOGNITION_INTERVAL_MS, pIntent);
		Log.i(TAG, "Connected to google play services");
	}

	@Override
	public void onDisconnected() {
	}
	
	/*==================================== Activity Recognition End ==================================*/
	
	/*==================================== Zephyr Start ==============================================*/
	
	/**
	 * Updates View-References of UI Elements which are updated by the Zephyr-Handler.
	 * 
	 * @param zStatus	the new Reference for status
	 * @param zSwitch	the new Reference for the switch
	 * @param zHR		the new Reference for heart rate
	 * @param zIS		the new Reference for instant speed
	 */
    void setZephyrUIElements(TextView zStatus, Switch zSwitch, TextView zHR, TextView zIS){
    	this.zephyrStatus = zStatus;
    	this.zephyrSwitch = zSwitch;
    	this.zephyrInstantSpeed = zIS;
    	this.zephyrHeartRate = zHR;
    	if (zStatus != null){
    		if (zephyrConnected){
    			setZephyrConnected();
    		} else{
    			setZephyrDisconnected();
    		}
    	}
    }
	
	/**
	 * Checks if Bluetooth is enabled and offers possibility to enable it
	 * 
	 * @return true if Bluetooth is enabled, false otherwise
	 */
	private boolean isBluetoothEnabled() {
		if (!adapter.isEnabled()) {
    		zephyrSwitch.setEnabled(false);
			Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(enableBtIntent, Constants.REQUEST_CODE_ENABLE_BLUETOOTH);
			return false;
		} else {
			return true;
		}
	}
	
	/**
	 * Opens System Bluetooth Settings (for pairing with Zephyr)
	 */
	private void openBTSettings() {
		Intent intentBluetooth = new Intent();
		intentBluetooth.setAction(android.provider.Settings.ACTION_BLUETOOTH_SETTINGS);
		startActivity(intentBluetooth);
		Toast.makeText(getApplicationContext(), "Bitte mit HXM... pairen!", Toast.LENGTH_SHORT).show();
	}
	
	/**
	 * Does all UI Changes for Zephyr is connected.
	 */
	private void setZephyrConnected() {
		if (zephyrStatus != null){
    		zephyrSwitch.setEnabled(true);
    		zephyrSwitch.setChecked(true);
    		zephyrStatus.setText(R.string.empty);
		}
	}
	
	/**
	 * Does all UI Changes for Zephyr is disconnected.
	 */
	private void setZephyrDisconnected() {
		if (zephyrStatus != null){
    		zephyrSwitch.setEnabled(true);
    		zephyrSwitch.setChecked(false);
    		zephyrStatus.setText(R.string.empty);
			zephyrHeartRate.setText(R.string.zero);
			zephyrInstantSpeed.setText(R.string.zero);
		}
	}
	
	/**
	 * Connects Zephyr.
	 */
	private void connectZephyr() {
    	Log.i(TAG, "Trying to connect to Zephyr");
		
		if (!isBluetoothEnabled()){
			setZephyrDisconnected();
	    	Log.i(TAG, "Bluetooth disabled, connection failed");
			
		} else{
			String mac = "";
			Set<BluetoothDevice> pairedDevices = adapter.getBondedDevices();
			for (BluetoothDevice btDevice : pairedDevices) {
				if (btDevice.getName().startsWith("HXM")) {
					mac = btDevice.getAddress();
					_bt = new BTClient(adapter, mac);
					_NConnListener = new NewConnectedListener(Newhandler,
							Newhandler, getApplicationContext());
					_bt.addConnectedEventListener(_NConnListener);
					
					if (_bt.IsConnected()) {
				    	Log.i(TAG, "connection to Zephyr successful");
						_bt.start();	// sends Broadcast ACL_DISCONNECTED
						Toast.makeText(getApplicationContext(), "Verbindung hergestellt!", Toast.LENGTH_SHORT).show();
						
					} else {
				    	Log.i(TAG, "connection failed due to unknown reason");
				    	setZephyrDisconnected();
						Toast.makeText(getApplicationContext(), "Verbindung fehlgeschlagen!", Toast.LENGTH_SHORT).show();
						disconnectOnPurpose = true;			// do not send a notification here
					}
					break;
	    		}
			} if (mac.isEmpty()){
		    	Log.i(TAG, "connection failed due to no paired Zephyr -> Pairing");
		    	setZephyrDisconnected();
		    	disconnectOnPurpose = true;		// do not send a notification here
				openBTSettings();
			}
		}
	}
	
	/**
	 * Disconnects Zephyr.
	 */
	private void disconnectZephyr() {
		Log.e(TAG, "Trying to disconnect Zephyr");
		try {
			_bt.removeConnectedEventListener(_NConnListener);	// this disconnects listener from acting on received messages
			_bt.Close();										// closes the communication with the device, sends Broadcast ACL_DISCONNECTED
			Log.e(TAG, "disconnecting was successful");
		} catch (NullPointerException e) {
			Log.e(TAG, "Failed disconnecting: ");
			e.printStackTrace();
		}
		Toast.makeText(getApplicationContext(), "Verbindung getrennt!", Toast.LENGTH_SHORT).show();
	}
	
	/**
	 * Connect/Disconnect Zephyr.
	 * 
	 * @param connect	true if Zephyr shall be connected, false if it shall be disconnected
	 */
	void handleZephyr(boolean connect) {
		if (connect){
			disconnectOnPurpose = false;
			connectZephyr();
		} else {
			disconnectOnPurpose = true;
			disconnectZephyr();
		}
	}
	
	/**
	 * 
	 * @author Thomas
	 */
	private class BTConnectedReceiver extends BroadcastReceiver {
		@Override
		public void onReceive (Context context, Intent intent) {
			String action = intent.getAction();
			Log.i(TAG, "New Bluetooth status: " + action);
			if (action.equals(BluetoothDevice.ACTION_ACL_DISCONNECTED)) {
				zephyrConnected = false;
				setZephyrDisconnected();
				if (!disconnectOnPurpose){
					notifyOnDisconnect();
				}
			}
			if (action.equals(BluetoothDevice.ACTION_ACL_CONNECTED)) {
				zephyrConnected = true;
				setZephyrConnected();
			}
			if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
	            final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
	            switch (state) {
	            case BluetoothAdapter.STATE_TURNING_OFF:
	                if (zephyrConnected){
	                	disconnectZephyr();
	                }
	                break;
	            }
			}
		}
	}
	
	/**
	 * Handler for updating UI with information from Zephyr
	 * 
	 * @author svenja
	 */
	static class ThreadToUIHandler extends Handler {
		WeakReference<MainActivity> weakReference;
		
		ThreadToUIHandler(Activity fragment){
			weakReference = new WeakReference<MainActivity>((MainActivity) fragment);
		}
		
		@Override
		public void handleMessage(Message message) {
			MainActivity activity = weakReference.get();
			final int HEART_RATE = 0x100;
			final int INSTANT_SPEED = 0x101;
			
			switch (message.what) {
			case HEART_RATE:
				String HeartRatetext = message.getData().getString("HeartRate");
				if (activity.zephyrHeartRate != null){
					activity.zephyrHeartRate.setText(HeartRatetext);
				}
				break;
			case INSTANT_SPEED:
				String InstantSpeedtext = message.getData().getString("InstantSpeed");
				if (activity.zephyrInstantSpeed != null){
					activity.zephyrInstantSpeed.setText(InstantSpeedtext);
				}
				break;
			}
		}
	}
	
	/*
	 * CURRENTLY UNUSED
	 * 
	
	
	private class BTBondReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			Bundle b = intent.getExtras();
			BluetoothDevice device = adapter.getRemoteDevice(b.get("android.bluetooth.device.extra.DEVICE").toString());
			Log.d("Bond state", "BOND_STATED = " + device.getBondState());
		}
	}

	private class BTBroadcastReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			Log.d("BTIntent", intent.getAction());
			Bundle b = intent.getExtras();
			Log.d("BTIntent", b.get("android.bluetooth.device.extra.DEVICE").toString());
			Log.d("BTIntent", b.get("android.bluetooth.device.extra.PAIRING_VARIANT").toString());
			try {
				BluetoothDevice device = adapter.getRemoteDevice(b.get("android.bluetooth.device.extra.DEVICE").toString());
				Method m = BluetoothDevice.class.getMethod("convertPinToBytes",	new Class[] {String.class});
				byte[] pin = (byte[]) m.invoke(device, "1234");
				m = device.getClass().getMethod("setPin", new Class[] {pin.getClass()});
				Object result = m.invoke(device, pin);
				Log.d("BTTest", result.toString());
			} catch (SecurityException e) {
				Log.e(TAG, e.getMessage());
			} catch (NoSuchMethodException e) {
				Log.e(TAG, e.getMessage());
			} catch (IllegalArgumentException e) {
				Log.e(TAG, e.getMessage());
			} catch (IllegalAccessException e) {
				Log.e(TAG, e.getMessage());
			} catch (InvocationTargetException e) {
				Log.e(TAG, e.getMessage());
			}
		}
	}
	
	*/
	
	/*==================================== Zephyr End ==============================================*/
	
}