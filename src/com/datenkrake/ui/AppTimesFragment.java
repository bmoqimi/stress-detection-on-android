package com.datenkrake.ui;

import java.util.GregorianCalendar;
import java.util.LinkedHashMap;
import java.util.LinkedList;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;

import com.datenkrake.HorizontalBarChartAdapter;
import com.datenkrake.DatabaseHandler;
import com.datenkrake.R;
import com.datenkrake.SolinApplication;

/**
 * AppTimesFragment.
 * A Fragment for showing the time the user spents on using single apps in a barchart.
 * 
 * @author svenja
 */
public class AppTimesFragment extends Fragment implements OnItemSelectedListener {
	private final String TAG = getClass().getSimpleName();
	private DatabaseHandler db;
	private Context context;
	
	private LinkedList<String> leftLabels;
	private LinkedList<String> rightLabels;
	private LinkedList<Integer> values;
	private ListView barChart;
	private String hourAbbrv;
	private String minuteAbbrv;
	private String secondAbbrv;
	private int maxWidth;
	private int span;
	
	/**
	 * Returns a new instance of this fragment.
	 */
	public static AppTimesFragment newInstance() {
		AppTimesFragment fragment = new AppTimesFragment();
		Bundle args = new Bundle();
		fragment.setArguments(args);
		return fragment;
	}
	
	public AppTimesFragment() {
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
		leftLabels = new LinkedList<String>();
		rightLabels = new LinkedList<String>();
		values = new LinkedList<Integer>();
		span = 1;
	}
	
	@Override
	public void setUserVisibleHint(boolean isVisibleToUser) {
	    super.setUserVisibleHint(isVisibleToUser);

	    if (this.isVisible()) {
	        if (isVisibleToUser) {
	            updateValuesAndView(span);
	        }
	    }
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		View rootView = inflater.inflate(R.layout.fragment_app_times, container, false);
		barChart = (ListView) rootView.findViewById(R.id.fragment_app_times_barchart);
		Spinner spinner = (Spinner) rootView.findViewById(R.id.fragment_app_times_spinner);

		// fill spinner from resources
		ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_dropdown_item, context.getResources().getStringArray(R.array.statistic_timespans_entries));
		spinner.setAdapter(spinnerAdapter);
		spinner.setOnItemSelectedListener(this);
		
		// calculate maxWidth for bars
		int displayWidth = context.getResources().getDisplayMetrics().widthPixels;
		int activityPadding = (int) (getResources()
				.getDimensionPixelSize(R.dimen.activity_horizontal_margin));
		int boxPadding = (int) (getResources()
				.getDimensionPixelSize(R.dimen.box_horizontal_margin));
		maxWidth = displayWidth - 2 * activityPadding - 2 * boxPadding;
		
		hourAbbrv = getResources().getString(R.string.hour);
		minuteAbbrv = getResources().getString(R.string.minute);
		secondAbbrv = getResources().getString(R.string.second);
	    
	    return rootView;
	}
	
	@Override
	public void onResume() {
		super.onResume();
		updateValuesAndView(span);
	}

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position,
			long id) {
		int newSpan = context.getResources().getIntArray(R.array.statistic_timespans_values)[position];
		updateValuesAndView(newSpan);
	}

	@Override
	public void onNothingSelected(AdapterView<?> parent) {
		// nothing currently
	}

	/**
	 * Call to update data and UI.
	 * 
	 * @param newSpan	new time-span
	 */
	private void updateValuesAndView(int newSpan) {
		span = newSpan;

		GregorianCalendar now = new GregorianCalendar();
		LinkedHashMap<String, Integer> times = db.getAppTimes(now, newSpan);
		values.clear();
		leftLabels.clear();
		rightLabels.clear();
		for (String key : times.keySet()) {
			values.add(times.get(key));
			int h = times.get(key) / 3600;
			int rem = times.get(key) % 3600;
			int m = rem / 60;
			int s = rem % 60;
			String hStr = (h < 10 ? "0" : "") + h;
			String mStr = (m < 10 ? "0" : "") + m;
			String sStr = (s < 10 ? "0" : "") + s;
			leftLabels.add(key + ":");
			rightLabels.add(hStr + hourAbbrv + " " + mStr + minuteAbbrv + " " + sStr + secondAbbrv);
		}

		// find max value
		int maxValue = 1;
		for (int value : values) {
			if (value > maxValue) {
				maxValue = value;
			}
		}

		// calculate widths for the bars (and transform lists to arrays)
		int[] widths = new int[values.size()];
		String[] leftLabelsArray = new String[values.size()];
		String[] rightLabelsArray = new String[values.size()];
		for (int i = 0; i < values.size(); i++) {
			widths[i] = (int) values.get(i) * maxWidth / maxValue;
			leftLabelsArray[i] = (String) leftLabels.get(i);
			rightLabelsArray[i] = (String) rightLabels.get(i);
		}

		HorizontalBarChartAdapter adapter = new HorizontalBarChartAdapter(context,
				R.layout.horizontal_bar, leftLabelsArray, rightLabelsArray, widths);
		barChart.setAdapter(adapter);
	}

}
