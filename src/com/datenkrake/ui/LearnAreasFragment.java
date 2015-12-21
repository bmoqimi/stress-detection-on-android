package com.datenkrake.ui;

import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.datenkrake.Alerts;
import com.datenkrake.CellIdListener;
import com.datenkrake.Constants;
import com.datenkrake.DatabaseHandler;
import com.datenkrake.R;
import com.datenkrake.SolinApplication;

/**
 * LearnAreasFragment. 
 * A Fragment for learning cells for an area and showing already learned cells per area.
 * 
 * TODO: sometimes crash (could not be reproduced), recheck whole class!!!
 * 
 * @author svenja
 */
public class LearnAreasFragment extends Fragment implements OnClickListener{
	private final String TAG = getClass().getSimpleName();
	private DatabaseHandler db;
	private Context context;
	
	private final String SHARED_PREFS_KEY_FIRSTRUN =  "firstrun_" + TAG;
	private final String SHARED_PREFS_NAME = "learn_preferences";
	private final String SHARED_PREFS_KEY_ENDTIME = "endtime";
	private final String SHARED_PREFS_KEY_AREA_ID = "area_id";
	private final int LEARN_TIMER_UPDATE_INTERVAL_IN_MS = 1000;
	private final int REFRESH_CELLLISTS_WHEN_LEARNING_INTERVAL_IN_S = 10;
	private ArrayList<String> areas;
	private ArrayList<String>[] cellLists;
	private ListView[] cellListViews;
	private TextView[] cellLabels;
	private SharedPreferences learnAreasSharedPrefs;
	private SharedPreferences.Editor learnAreasSharedPrefsEditor;
	private CountDownTimer mainTimer;
	private CountDownTimer temporaryTimer;
	private RelativeLayout learnedAreaLayout;
	private int learnedAreaID;

	/**
	 * Returns a new instance of this fragment.
	 */
	public static LearnAreasFragment newInstance() {
		LearnAreasFragment fragment = new LearnAreasFragment();
		Bundle args = new Bundle();
		fragment.setArguments(args);
		return fragment;
	}

	public LearnAreasFragment() {
		// Required empty public constructor
	}
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
	    context = activity;
		learnAreasSharedPrefs = activity.getSharedPreferences(this.SHARED_PREFS_NAME, Context.MODE_PRIVATE);
		learnAreasSharedPrefsEditor = learnAreasSharedPrefs.edit();
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		db = SolinApplication.getDbHandler();
		areas = db.getRealAreas();
		cellLists = new ArrayList[areas.size()];
		cellListViews = new ListView[areas.size()];
		cellLabels = new TextView[areas.size()];
		
		// Is this the first run?
		boolean firstRun = context.getSharedPreferences(Constants.MAIN_SHARED_PREFS_NAME, Context.MODE_PRIVATE)
				.getBoolean(this.SHARED_PREFS_KEY_FIRSTRUN, true);
		if (firstRun) {
			context.getSharedPreferences(Constants.MAIN_SHARED_PREFS_NAME, Context.MODE_PRIVATE).edit()
			.putBoolean(SHARED_PREFS_KEY_FIRSTRUN, false).commit();
			Alerts.showSimpleOKDialog(context, context.getResources().getString(R.string.action_help), context.getResources().getString(R.string.help_dialog_areas));
		}
	}
	
	@Override
	public void setUserVisibleHint(boolean isVisibleToUser) {
	    super.setUserVisibleHint(isVisibleToUser);

	    if (this.isVisible()) {
	        if (isVisibleToUser) {
	            updateValuesAndView();
	        }
	    }
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		ScrollView rootView = (ScrollView) inflater.inflate(R.layout.fragment_learn_areas,
					container, false);
		LinearLayout linLayout = (LinearLayout) rootView.getChildAt(0);

		int i = 0;
		for (String area : areas){
			RelativeLayout areaView = (RelativeLayout) inflater.inflate(R.layout.area, linLayout, false);
			((TextView) areaView.findViewById(R.id.area_label)).setText(area);
			cellListViews[i] = (ListView) areaView.findViewById(R.id.area_cells);
			cellLists[i] = new ArrayList<String>();
			cellLabels[i] = (TextView) areaView.findViewById(R.id.known_cells_label);
			((Button) areaView.findViewById(R.id.area_learn)).setOnClickListener(this);
			linLayout.addView(areaView);
			i++;
		}
		return rootView;
	}
	
	@Override
	public void onStart() {
		super.onStart();

		// (re-)set learn-variables
		learnedAreaLayout = null;
		learnedAreaID = -1;
		long timeMillisLeft = 0l;

		// read/initialize SharedPreferences
		if (learnAreasSharedPrefs.contains(SHARED_PREFS_KEY_ENDTIME) && learnAreasSharedPrefs.contains(SHARED_PREFS_KEY_AREA_ID)) {
			long time = System.currentTimeMillis();
			timeMillisLeft = learnAreasSharedPrefs.getLong(SHARED_PREFS_KEY_ENDTIME, time) - time;
			learnedAreaID = learnAreasSharedPrefs.getInt(SHARED_PREFS_KEY_AREA_ID, -1);
			
			// check if an area is still be learned
			if (learnedAreaID != -1 && timeMillisLeft > 0) {
				// if yes, find Layout for ID and ...
				for (int i = 0; i < (((LinearLayout) ((ScrollView) this.getView()).getChildAt(0))).getChildCount(); i++) {
					String areaName = (String) ((TextView) ((RelativeLayout) (((LinearLayout) ((ScrollView) this.getView()).getChildAt(0))).getChildAt(i)).findViewById(R.id.area_label)).getText();
					if (db.getAreaId(areaName) == learnedAreaID) {
						learnedAreaLayout = (RelativeLayout) (((LinearLayout) ((ScrollView) this.getView()).getChildAt(0))).getChildAt(i);
						break;
					}
				}
				// ... disable buttons and show remaining time
				disableButtons();
				temporaryTimer = new CountDownTimer(timeMillisLeft, LEARN_TIMER_UPDATE_INTERVAL_IN_MS) {
					
					@Override
					public void onTick(long millisUntilFinished) {
						publishProgress(millisUntilFinished);
					}
					
					@Override
					public void onFinish() {
						if (learnedAreaLayout != null){
							TextView tv = (TextView) learnedAreaLayout.findViewById(R.id.area_time);
							tv.setText(R.string.empty);
						}
					}
				}.start();
				Log.i(TAG, "An area is still be learned, temporary timer started");
			}
		} else {
			// first started -> initialize settings
			learnAreasSharedPrefsEditor.putLong(SHARED_PREFS_KEY_ENDTIME, 0l);
			learnAreasSharedPrefsEditor.putInt(SHARED_PREFS_KEY_AREA_ID, -1);
			learnAreasSharedPrefsEditor.commit();
			Log.i(TAG, "SharedPrefs initialized");
		}
	}
	
	@Override
	public void onResume() {
		super.onResume();
		updateValuesAndView();
	}
	
	@Override
	public void onStop() {
		// reset learn-variables
		learnedAreaLayout = null;
		learnedAreaID = -1;
		// cancel temporary timer, if any
		if (temporaryTimer != null){
			temporaryTimer.cancel();
			temporaryTimer = null;
			Log.i(TAG, "temporary timer canceled and destroyed");
		}
		super.onStop();
	}
	
	@Override
	public void onClick(View v) {
		if(((String) ((Button) v).getText()).equals(getResources().getString(R.string.cancel_learning))){
			cancelMonitoring();
		} else if (((String) ((Button) v).getText()).equals(getResources().getString(R.string.learn_area))){
			RelativeLayout rel = (RelativeLayout) v.getParent();
			String areaName = (String) ((TextView) rel.findViewById(R.id.area_label)).getText();
			learnedAreaLayout = rel;
			learnedAreaID = db.getAreaId(areaName);
			askForDuration();
		}
	}
	
	/**
	 * Shows a Dialog for the user to choose the learning time.
	 */
	private void askForDuration() {
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
		alertDialogBuilder.setTitle(R.string.title_dialog_learn_duration);
		alertDialogBuilder.setItems(R.array.learn_duration_entries, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				startMonitoring(which);
			}
		});
		AlertDialog dialog = alertDialogBuilder.create();
		dialog.show();
	}
	
	/**
	 * Logs cells for the selected area and the selected time.
	 * 
	 * @param which	the index in the learning-times-array
	 */
	private void startMonitoring(int which) {
		int duration = getResources().getIntArray(R.array.learn_duration_values)[which];
		
		// disable all other Learn-Buttons while learning
		disableButtons();
		
		/* Update Listener and start it */
		CellIdListener.setAreaIdForLogging(learnedAreaID);
		Log.i(TAG, "Logging CellIds started");
		CellIdListener.addLastCellToArea(learnedAreaID);
		Toast.makeText(context, R.string.toast_started_learning, Toast.LENGTH_SHORT).show();

		// start timer
		long durationAsMillis = duration * 60 * 1000;
	    mainTimer = new CountDownTimer(durationAsMillis, LEARN_TIMER_UPDATE_INTERVAL_IN_MS) {
			
			@Override
			public void onTick(long millisUntilFinished) {
				publishProgress(millisUntilFinished);
			}
			
			@Override
			public void onFinish() {
				endMonitoring();
			}
			
		}.start();
		Log.i(TAG, "Main-Timer started, Logging for " + duration + " minutes");
		
		// Update SharedPreferences
		learnAreasSharedPrefsEditor.putInt(SHARED_PREFS_KEY_AREA_ID, learnedAreaID);
		learnAreasSharedPrefsEditor.putLong(SHARED_PREFS_KEY_ENDTIME, System.currentTimeMillis() + durationAsMillis);
		learnAreasSharedPrefsEditor.commit();
		Log.i(TAG, "SharedPrefs updated");
	}
	
	/**
	 * Stops Logging cells for the selected area. Called when learning-time finishes.
	 */
	private void endMonitoring() {
		CellIdListener.setAreaIdForLogging(1);
		Log.i(TAG, "Stopped Monitoring for time is over");
		Toast.makeText(context, R.string.toast_stopped_learning, Toast.LENGTH_SHORT).show();
		
		// reset UI and learn-variables
		if(learnedAreaLayout != null){
			TextView tv = (TextView) learnedAreaLayout.findViewById(R.id.area_time);
			tv.setText(R.string.empty);
			enableButtons();
			learnedAreaID = -1;
			learnedAreaLayout = null;
		}
		
		if (mainTimer != null){
			mainTimer = null;
			Log.i(TAG, "Main-Timer destroyed");
		}		
		// no need to update SharedPreferences because time is over
	}
	
	/**
	 * Stops Logging cells for the selected area. Called when user cancels monitoring.
	 */
	private void cancelMonitoring() {
		CellIdListener.setAreaIdForLogging(1);
		Log.i(TAG, "Monitoring canceled by user");
		Toast.makeText(context, R.string.toast_stopped_learning, Toast.LENGTH_SHORT).show();
		
		if (temporaryTimer != null){
			temporaryTimer.cancel();
			temporaryTimer = null;
			Log.i(TAG, "Temporary-Timer canceled and destroyed");
		}
		if (mainTimer != null){
			mainTimer.cancel();
			mainTimer = null;
			Log.i(TAG, "Main-Timer canceled and destroyed");
		}
		
		// reset UI and learn-variables
		TextView tv = (TextView) learnedAreaLayout.findViewById(R.id.area_time);
		tv.setText(R.string.empty);
		enableButtons();
		learnedAreaID = -1;
		learnedAreaLayout = null;
		
		// update SharedPreferences
		learnAreasSharedPrefsEditor.putLong(SHARED_PREFS_KEY_ENDTIME, System.currentTimeMillis());
		learnAreasSharedPrefsEditor.commit();
		Log.i(TAG, "SharedPrefs updated");
	}
	
	/**
	 * Shows remaining time of CountdownTimer.
	 * 
	 * @param millisUntilFinished	remaining time in milliseconds
	 */
	private void publishProgress(long millisUntilFinished){
		if(learnedAreaLayout != null){
			// update Timer
			TextView tv = (TextView) learnedAreaLayout.findViewById(R.id.area_time);
			int secondsUntilFinished = (int) millisUntilFinished/1000;
			String hours = "0" + (int) secondsUntilFinished/3600;
			String minutes = "" + (int) (secondsUntilFinished/60)%60;
			String seconds = "" + secondsUntilFinished%60;
			tv.setText(hours + ":" + ((minutes.length()<2) ? "0" + minutes : minutes) + ":" + ((seconds.length()<2) ? "0" + seconds : seconds));
			// update cells
			if((secondsUntilFinished % REFRESH_CELLLISTS_WHEN_LEARNING_INTERVAL_IN_S) == 0){
				updateValuesAndView();
			}
		}
	}
	
	/**
	 * Disables all Learn-Buttons except of this of the currently learned Area
	 */
	private void disableButtons() {
		for (int i = 0; i < ((LinearLayout) ((ScrollView) this.getView()).getChildAt(0)).getChildCount(); i++){
			String areaName = (String) ((TextView) ((RelativeLayout) (((LinearLayout) ((ScrollView) this.getView()).getChildAt(0))).getChildAt(i)).findViewById(R.id.area_label)).getText();
			if(db.getAreaId(areaName) == learnedAreaID){
				((Button)((RelativeLayout)(((LinearLayout) ((ScrollView) this.getView()).getChildAt(0))).getChildAt(i)).getChildAt(3)).setText(R.string.cancel_learning);
			} else{
				((Button)((RelativeLayout)(((LinearLayout) ((ScrollView) this.getView()).getChildAt(0))).getChildAt(i)).getChildAt(3)).setEnabled(false);
			}
		}
	}
	
	/**
	 * Enables all Learn-Buttons.
	 */
	private void enableButtons() {
		for (int i = 0; i < ((LinearLayout) ((ScrollView) this.getView()).getChildAt(0)).getChildCount(); i++){
			String areaName = (String)((TextView)((RelativeLayout) (((LinearLayout) ((ScrollView) this.getView()).getChildAt(0))).getChildAt(i)).findViewById(R.id.area_label)).getText();
			if(db.getAreaId(areaName) == learnedAreaID){
				((Button)((RelativeLayout)(((LinearLayout) ((ScrollView) this.getView()).getChildAt(0))).getChildAt(i)).getChildAt(3)).setText(R.string.learn_area);
			} else{
				((Button)((RelativeLayout)(((LinearLayout) ((ScrollView) this.getView()).getChildAt(0))).getChildAt(i)).getChildAt(3)).setEnabled(true);
			}
		}
	}
	
	/**
	 * Call to update data and UI.
	 */
	private void updateValuesAndView() {
		for (int i = 0; i < areas.size(); i++) {
			cellLists[i].clear();
			cellLists[i].addAll(db.getCellsForArea(
					db.getAreaId(areas.get(i))));
			cellLabels[i].setText(getResources().getString(R.string.known_cells) + " " + cellLists[i].size());
			if(cellListViews[i].getAdapter() == null){
				cellListViews[i].setAdapter(new ArrayAdapter<String>(getActivity(), R.layout.cell, cellLists[i]));
			} else{
				((ArrayAdapter) cellListViews[i].getAdapter()).notifyDataSetChanged();
			}
		}
	}
	
	/* CURRENTLY UNUSED 
	public void createNewArea(View view) {
		final DatabaseHandler db = SolinApplication.getDbHandler();
		AlertDialog.Builder alert = new AlertDialog.Builder(this);

		alert.setTitle("Neues Gebiet erstellen");
		alert.setMessage("Geben Sie hier den Namen für das Gebiet ein, wie zum Beispiel \"Arbeit\" oder \"Zu Hause\"");

		// Set an EditText view to get user input 
		final EditText input = new EditText(this);
		alert.setView(input);

		alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
		public void onClick(DialogInterface dialog, int whichButton) {
		  String value = input.getText().toString();
		  // add area to sqlite db
	      db.addArea(value);
		  Log.i(TAG, "User input: " + value);
		  
		  refreshActivity();
		  }
		});

		alert.setNegativeButton("Abbrechen", new DialogInterface.OnClickListener() {
		  public void onClick(DialogInterface dialog, int whichButton) {
		    // Canceled.
		  }
		});
		alert.show();
	}
	
	private void deleteArea(String area) {
			Log.i("...", area);
			int id = db.getAreaId(area);
			db.deleteArea(id);
	}*/
	
}