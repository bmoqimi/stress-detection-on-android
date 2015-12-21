package com.datenkrake.ui;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.datenkrake.Alerts;
import com.datenkrake.Constants;
import com.datenkrake.HorizontalBarChartAdapter;
import com.datenkrake.DatabaseHandler;
import com.datenkrake.R;
import com.datenkrake.ScreenReceiver;
import com.datenkrake.SolinApplication;

/**
 * PhoneTimeFragment.
 * A Fragment for showing the time per day which the user spents with his phone in a barchart.
 * 
 * @author svenja
 */
public class PhoneTimeFragment extends Fragment {
	private final String TAG = getClass().getSimpleName();
	private DatabaseHandler db;
	private Context context;
	
	private final String SHARED_PREFS_KEY_FIRSTRUN =  "firstrun_" + TAG;
	private final int LAST_X_DAYS = 30;
	private int[] values;
	private String[] leftLabels;
	private String[] rightLabels;
	private String hourAbbrv;
	private String minuteAbbrv;
	private String secondAbbrv;
	private int maxWidth;
	private View rootView;
	private ListView barChart;
	
	/**
	 * Returns a new instance of this fragment.
	 */
	public static PhoneTimeFragment newInstance() {
		PhoneTimeFragment fragment = new PhoneTimeFragment();
		Bundle args = new Bundle();
		fragment.setArguments(args);
		return fragment;
	}
	
	public PhoneTimeFragment() {
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
		
		// Is this the first run?
		boolean firstRun = context.getSharedPreferences(Constants.MAIN_SHARED_PREFS_NAME, Context.MODE_PRIVATE)
				.getBoolean(this.SHARED_PREFS_KEY_FIRSTRUN, true);
		if (firstRun) {
			context.getSharedPreferences(Constants.MAIN_SHARED_PREFS_NAME, Context.MODE_PRIVATE).edit()
			.putBoolean(SHARED_PREFS_KEY_FIRSTRUN, false).commit();
			Alerts.showSimpleOKDialog(context, context.getResources().getString(R.string.action_help), context.getResources().getString(R.string.help_dialog_times));
		}

		db = SolinApplication.getDbHandler();
		leftLabels = new String[LAST_X_DAYS];
		rightLabels = new String[LAST_X_DAYS];
		values = new int[LAST_X_DAYS];
		
		hourAbbrv = getResources().getString(R.string.hour);
		minuteAbbrv = getResources().getString(R.string.minute);
		secondAbbrv = getResources().getString(R.string.second);
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
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		
		rootView = inflater.inflate(R.layout.fragment_phone_time, container, false);
		barChart = (ListView) rootView.findViewById(R.id.fragment_phone_time_barchart);
		
		// calculate maxWidth for bars
		int displayWidth = context.getResources().getDisplayMetrics().widthPixels;
		int activityPadding = (int) (getResources().getDimensionPixelSize(R.dimen.activity_horizontal_margin));
		int boxPadding = (int) (getResources().getDimensionPixelSize(R.dimen.box_horizontal_margin));
		maxWidth = displayWidth - 2 * activityPadding - 2 * boxPadding;
		
		return rootView;
	}
	
	@Override
	public void onResume() {
		super.onResume();
		updateValuesAndView();
	}
	
	/**
	 * Call to update data and UI.
	 */
	private void updateValuesAndView() {
		TextView totalScreentime = (TextView) rootView.findViewById(R.id.fragment_phone_time_since_installation);
		GregorianCalendar now = new GregorianCalendar();
		
		// Total Screentime
		long time = db.getAllScreenTime() + ScreenReceiver.getCurrentScreenTime();
		int h = (int) time / 3600;
		int rem = (int) time % 3600;
		int m = (int) rem / 60;
		int s = (int) rem % 60;
		String hStr = (h < 10 ? "0" : "") + h;
		String mStr = (m < 10 ? "0" : "") + m;
		String sStr = (s < 10 ? "0" : "") + s;
		totalScreentime.setText(hStr + hourAbbrv + " " + mStr + minuteAbbrv + " " + sStr + secondAbbrv);
		
		// Todays Screentime
		time = db.getScreenTimeOfDay(now) + ScreenReceiver.getCurrentScreenTime();
		h = (int) time / 3600;
		rem = (int) time % 3600;
		m = (int) rem / 60;
		s = (int) rem % 60;
		hStr = (h < 10 ? "0" : "") + h;
		mStr = (m < 10 ? "0" : "") + m;
		sStr = (s < 10 ? "0" : "") + s;
		values[0] = (int) time;
		leftLabels[0] = getResources().getString(R.string.today) + ":";
		rightLabels[0] = hStr + hourAbbrv + " " + mStr + minuteAbbrv + " " + sStr + secondAbbrv;
		
		// Last X-1 Days' Screentime
		for (int i = 1; i < LAST_X_DAYS; i++) {
			now.add(Calendar.DATE, -1);
			time = db.getScreenTimeOfDay(now);
			h = (int) time / 3600;
			rem = (int) time % 3600;
			m = (int) rem / 60;
			s = (int) rem % 60;
			hStr = (h < 10 ? "0" : "") + h;
			mStr = (m < 10 ? "0" : "") + m;
			sStr = (s < 10 ? "0" : "") + s;
			values[i] = (int) time;
			leftLabels[i] = now.get(Calendar.DATE)
					+ ". "
					+ now.getDisplayName(Calendar.MONTH, Calendar.SHORT,
							Locale.GERMANY) + ":";
			rightLabels[i] = hStr + hourAbbrv + " " + mStr
					+ minuteAbbrv + " " + sStr + secondAbbrv;
		}
		
		// find max value
		int maxValue = 1;
		for (int value : values){
			if (value > maxValue){
				maxValue = value;
			}
		}
		
		// calculate widths for the bars
		int[] widths = new int[LAST_X_DAYS];
		for (int i = 0; i < values.length; i++){
			widths[i] = (int) values[i] * maxWidth / maxValue;
		}

		HorizontalBarChartAdapter adapter = new HorizontalBarChartAdapter(context, R.layout.horizontal_bar, leftLabels, rightLabels, widths);
		barChart.setAdapter(adapter);
	}
	
}