package com.datenkrake.ui;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.LinkedHashMap;
import java.util.Locale;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.datenkrake.Alerts;
import com.datenkrake.Constants;
import com.datenkrake.DatabaseHandler;
import com.datenkrake.R;
import com.datenkrake.SolinApplication;

/**
 * PerceivedStressFragment.
 * A Fragment for showing the stress of the user based on his personal sensation.
 * 
 * @author svenja
 */
public class PerceivedStressFragment extends Fragment implements OnItemClickListener{
	private final String TAG = getClass().getSimpleName();
	private DatabaseHandler db;
	private Context context;
	
	private final String SHARED_PREFS_KEY_FIRSTRUN =  "firstrun_" + TAG;
	private final int DAY_COUNT = 31;
	private final int NUM_COLUMNS = 7;
	private GridView calendar;
	private int[] calendarColors;
	private String[] calendarLabels;
	
	/**
	 * Returns a new instance of this fragment.
	 */
	public static PerceivedStressFragment newInstance() {
		PerceivedStressFragment fragment = new PerceivedStressFragment();
		Bundle args = new Bundle();
		fragment.setArguments(args);
		return fragment;
	}
	
	public PerceivedStressFragment() {
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
			Alerts.showSimpleOKDialog(context, context.getResources().getString(R.string.action_help), context.getResources().getString(R.string.help_dialog_stress_perceived));
		}

		db = SolinApplication.getDbHandler();
		calendarColors = new int[DAY_COUNT];
		calendarLabels = new String[DAY_COUNT];
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		
		View rootView = inflater.inflate(R.layout.fragment_perceived_stress, container, false);
		calendar = (GridView) rootView.findViewById(R.id.fragment_perceived_stress_calendar);
		calendar.setNumColumns(NUM_COLUMNS);	
		calendar.setOnItemClickListener(this);
		return rootView;
	}
	
	@Override
	public void onResume() {
		super.onResume();
		updateValuesAndView();
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
		GregorianCalendar date = new GregorianCalendar();
		date.add(Calendar.DATE, position + 1 - DAY_COUNT);
		int day = date.get(Calendar.DATE);
		String month = date.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.GERMANY);
		String title = getResources().getString(R.string.title_dialog_stresslevel_details) + " " + ((day > 9) ? day : ("0" + day)) + ". " + month;
		LinkedHashMap<Long,Integer> stresslevelDetails = db.getStressNotificationTimeAnswered(date);
		int i = 0;
		String[] times = new String[stresslevelDetails.size()];
		int[] colors = new int[stresslevelDetails.size()];
		for (long millis : stresslevelDetails.keySet()){
			date.setTimeInMillis(millis);
			int hour = date.get(Calendar.HOUR_OF_DAY);
			int minute = (date.get(Calendar.MINUTE));
			times[i] = ((hour > 9) ? hour : ("0" + hour)) + ":" + ((minute > 9) ? minute : ("0" + minute)) + " " + getResources().getString(R.string.time_suffix);
			colors[i] = resolveStresslevelColor(stresslevelDetails.get(millis));
			i++;
		}
		showStresslevelDetailsDialog(getActivity(), title, times, colors);
	}
	
	/**
	 * Shows the "StresslevelDetails"-Dialog.
	 */
	public void showStresslevelDetailsDialog(Context context, String title, String[] times, int[] colors) {
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
				context);
		
	    // This is a custom dialog! Now the layout is inflated and set...
	    LayoutInflater inflater = ((Activity)context).getLayoutInflater();
	    View view = inflater.inflate(R.layout.dialog_stresslevel_details, null);
	    ListView list = (ListView) view.findViewById(R.id.stresslevel_details_list);
	    TextView none = (TextView) view.findViewById(R.id.stresslevel_details_none);
	    if(times.length>0){
		    list.setAdapter(new StresslevelDetailsAdapter(context, R.layout.stresslevel_detail, times, colors));
		    none.setVisibility(View.GONE);
	    } else{
	    	none.setText(R.string.no_stresslevel_details);
		    list.setVisibility(View.GONE);
	    }
	    alertDialogBuilder.setView(view);
	    alertDialogBuilder.setTitle(title);
		alertDialogBuilder.setPositiveButton(R.string.ok,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						dialog.cancel();
					}
				});
		AlertDialog alertDialog = alertDialogBuilder.create();
		alertDialog.show();
    }
	
	/**
	 * Translates numerical stresslevel into color
	 * 
	 * @param stresslevel numerical stresslevel
	 * @return color for the given stresslevel
	 */
	private int resolveStresslevelColor(int stresslevel){
		switch (stresslevel) {
		case 1:
			return context.getResources().getColor(R.color.stress_1);
		case 2:
			return context.getResources().getColor(R.color.stress_2);
		case 3:
			return context.getResources().getColor(R.color.stress_3);
		case 4:
			return context.getResources().getColor(R.color.stress_4);
		case 5:
			return context.getResources().getColor(R.color.stress_5);
		default:
			return context.getResources().getColor(R.color.light_blue);
		}
	}
	
	/**
	 * Call to update data and UI.
	 */
	private void updateValuesAndView() {
		GregorianCalendar now = new GregorianCalendar();
		
		for (int i = DAY_COUNT - 1 ; i >= 0 ; i--) {
			calendarColors[i] = db.getNotificationStressLevelOfDay(now);
			int day = now.get(Calendar.DATE);
			int month = now.get(Calendar.MONTH)+1;
			calendarLabels[i] = ((day > 9) ? day : ("0" + day))
					+ "."
					+ ((month > 9) ? month : ("0" + month))
					+ ".";
			now.add(Calendar.DATE, -1);
		}

		// adapter returns custom views to make the GridView look like a calendar
		CalendarAdapter adapter = new CalendarAdapter(context, R.layout.calendar_elem, calendarLabels, calendarColors);
		calendar.setAdapter(adapter);
	}
	
	/**
	 * Custom Adapter-Class for creating the Calendar-Look.
	 */
	public class CalendarAdapter extends ArrayAdapter<String>{
		Context context;
		String[] calendarLabels;
		int[] calendarColors;

		public CalendarAdapter(Context context, int resource, String[] labels, int[] colors) {
			super(context, resource, labels);
			this.context = context;
			this.calendarLabels = labels;
			this.calendarColors = colors;
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent){
			RelativeLayout gridElem = (RelativeLayout) convertView;
			if (gridElem == null){
				LayoutInflater inflater = getActivity().getLayoutInflater();
				gridElem = (RelativeLayout) inflater.inflate(R.layout.calendar_elem, parent, false);
			}
			
			TextView color = (TextView) gridElem.findViewById(R.id.calendar_elem_color);
			TextView label = (TextView) gridElem.findViewById(R.id.calendar_elem_label);
			
			int gridHeight = calendar.getHeight();
			color.setHeight((int)gridHeight / (((int) DAY_COUNT / NUM_COLUMNS) + 1));
			color.setBackgroundColor(resolveStresslevelColor(calendarColors[position]));
			label.setText(calendarLabels[position]);
            return gridElem;
		}
	}
	
	/**
	 * Custom Adapter-Class for populating the stresslevels into the ListView of the dialog.
	 */
	public class StresslevelDetailsAdapter extends ArrayAdapter<String>{
		Context context;
		String[] times;
		int[] colors;

		public StresslevelDetailsAdapter(Context context, int resource, String[] times, int[] colors) {
			super(context, resource, times);
			this.context = context;
			this.times = times;
			this.colors = colors;
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent){
			RelativeLayout stresslevelDetail = (RelativeLayout) convertView;
			if (stresslevelDetail == null){
				LayoutInflater inflater = getActivity().getLayoutInflater();
				stresslevelDetail = (RelativeLayout) inflater.inflate(R.layout.stresslevel_detail, parent, false);
			}
			TextView color = (TextView) stresslevelDetail.findViewById(R.id.stresslevel_detail_color);
			TextView time = (TextView) stresslevelDetail.findViewById(R.id.stresslevel_detail_time);
			color.setBackgroundColor(colors[position]);
			time.setText(times[position]);
            return stresslevelDetail;
		}
	}
	
}