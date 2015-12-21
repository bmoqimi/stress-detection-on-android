package com.datenkrake.ui;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.Fragment;
import android.content.ContentResolver;
import android.content.Context;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import com.datenkrake.Alerts;
import com.datenkrake.Cell;
import com.datenkrake.Constants;
import com.datenkrake.DatabaseHandler;
import com.datenkrake.R;
import com.datenkrake.SolinApplication;
import com.datenkrake.Util;
import com.datenkrake.badge.Badge;
import com.datenkrake.badge.BadgeHandler;

/**
 * OverviewFragment.
 * A Fragment for showing an overview as Welcome-Screen of the app.
 * 
 * @author svenja
 */
public class OverviewFragment extends Fragment {
	private final String TAG = getClass().getSimpleName();
	private final String SHARED_PREFS_KEY_FIRSTRUN = "firstrun_" + TAG;
	private final String SHARED_PREFS_KEY_NR_REMINDED = "nr_reminded";
	private DatabaseHandler db;
	private Context context;

	private View rootView;
	private Switch zephyrSwitch;
	private TextView instantSpeed;
	private TextView status;
	private TextView heartRate;
	private TextView currentAreaName;
	View badgesContainer;
	private GridView badges;
	private ArrayList<Badge> myBadges;
	
	/**
	 * Returns a new instance of this fragment.
	 */
	public static OverviewFragment newInstance() {
		OverviewFragment fragment = new OverviewFragment();
		Bundle args = new Bundle();
		fragment.setArguments(args);
		return fragment;
	}

	public OverviewFragment() {
		// Required empty public constructor
	}
	
	@Override
	public void onAttach(Activity activity) {
	    super.onAttach(activity);
	    context = activity;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		db = SolinApplication.getDbHandler();
		myBadges = BadgeHandler.getInstance().getBadges();
		
		// First Run ???
		boolean firstRun = 
				context.getSharedPreferences(Constants.MAIN_SHARED_PREFS_NAME, Context.MODE_PRIVATE)
				.getBoolean(this.SHARED_PREFS_KEY_FIRSTRUN, true);
		if (firstRun) {
			context.getSharedPreferences(Constants.MAIN_SHARED_PREFS_NAME, Context.MODE_PRIVATE).edit()
					.putBoolean(this.SHARED_PREFS_KEY_FIRSTRUN, false).commit();
			Alerts.showWelcomeDialog(context);
		} else {
			
			// Important-Contacts-Group existing ???
			boolean importantContactsCreated = Util
					.updateIdOfImportantGroup(context);
			int nrReminded = context.getSharedPreferences(Constants.MAIN_SHARED_PREFS_NAME, Context.MODE_PRIVATE)
					.getInt(this.SHARED_PREFS_KEY_NR_REMINDED, 0);
			if (nrReminded < Constants.MAX_CONTACT_REMINDERS) {
				context.getSharedPreferences(Constants.MAIN_SHARED_PREFS_NAME, Context.MODE_PRIVATE).edit()
						.putInt(this.SHARED_PREFS_KEY_NR_REMINDED, nrReminded+1).commit();
			} else{
				importantContactsCreated = true;
			}
			
			// Notification-Listener-Permission enabled ???
			ContentResolver contentResolver = context
					.getContentResolver();
			String enabledNotificationListeners = Settings.Secure.getString(
					contentResolver, Constants.NOTIFICATION_LISTENERS_SETTING_NAME);
			boolean notificationAccessGranted = false;
			if (enabledNotificationListeners != null) {
				notificationAccessGranted = enabledNotificationListeners
						.contains(getActivity().getPackageName());
			}
			if (!importantContactsCreated || !notificationAccessGranted) {
				Alerts.showConfigurationDialog(getActivity(),
						importantContactsCreated, notificationAccessGranted);
			}
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		rootView = inflater.inflate(R.layout.fragment_overview, container, false);
		
		heartRate = (TextView) rootView.findViewById(R.id.fragment_overview_heart_rate);
		instantSpeed = (TextView) rootView.findViewById(R.id.fragment_overview_instant_speed);
		currentAreaName = (TextView) rootView.findViewById(R.id.fragment_overview_current_area_name);
		status = (TextView) rootView.findViewById(R.id.fragment_overview_zephyr_status);
		badges = (GridView) rootView.findViewById(R.id.fragment_overview_badges_grid);
		badges.setNumColumns(Constants.NR_BADGE_GRID_COLUMNS);
		badges.setOnItemClickListener(new OnItemClickListener(){

			@Override
			public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
				Badge badge = myBadges.get(position);
				String level = "Level: " + badge.getBadgeLevel();
				Alerts.showSimpleOKDialog(context, badge.getName(), level);
			}
			
		});
		zephyrSwitch = (Switch) rootView.findViewById(R.id.fragment_overview_zephyr_switch);
		zephyrSwitch.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				handleZephyr();
			}
			
		});
		
		return rootView;
	}
	
	@Override
	public void onResume() {
		super.onResume();
		updateValuesAndView();
	}
	

	@Override
	public void onStart(){
    	super.onStart();
		
		// Update current area (TODO: is always "Unbekannt" at start, because the background assigning last cell to an area isn't finished yet)
		ArrayList<Cell> recent = db.getLastKnownCells(1);
		if(!recent.isEmpty()) {
			currentAreaName.setText(recent.get(0).getArea());
		}
		
    	// Update Zephyr-UI-References in MainActivity
    	((MainActivity) context).setZephyrUIElements(status, zephyrSwitch, heartRate, instantSpeed);
	}

	@Override
	public void onStop(){
		
		// Clear Zephyr-UI-References in MainActivity
	    ((MainActivity) context).setZephyrUIElements(null, null, null, null);
	    
	    super.onStop();
	}

	/**
	 * Called when clicking the Switch to (dis-)connect Zephyr.
	 */
	private void handleZephyr(){
		((MainActivity) context).handleZephyr(zephyrSwitch.isChecked());
	}
	
	/**
	 * Call to update data and UI.
	 */
	private void updateValuesAndView() {
		BadgesAdapter adapter = new BadgesAdapter(context, R.layout.badge, myBadges);
		badges.setAdapter(adapter);
	}
	
	/**
	 * Custom Adapter-Class for displaying the badges in a GridView.
	 */
	public class BadgesAdapter extends ArrayAdapter<Badge>{
		Context context;
		List<Badge> myBadges;

		public BadgesAdapter(Context context, int resource, List<Badge> myBadges) {
			super(context, resource, myBadges);
			this.context = context;
			this.myBadges = myBadges;
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent){
			
			View gridElem = convertView;
			if (gridElem == null){
				LayoutInflater inflater = getActivity().getLayoutInflater();
				gridElem = inflater.inflate(R.layout.badge, parent, false);
			}
			
			ImageView badgeIcon = (ImageView) gridElem.findViewById(R.id.badge_icon);
			TextView badgeName = (TextView) gridElem.findViewById(R.id.badge_name);
			
			Badge badge = myBadges.get(position);
			badgeIcon.setImageDrawable(getResources().getDrawable(badge.getBadgeId()));
			badgeName.setText(badge.getName());
			
            return gridElem;
		}
		
	}
	
}