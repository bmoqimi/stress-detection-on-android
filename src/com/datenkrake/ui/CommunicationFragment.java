package com.datenkrake.ui;

import java.util.ArrayList;
import java.util.GregorianCalendar;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.datenkrake.Alerts;
import com.datenkrake.Constants;
import com.datenkrake.DatabaseHandler;
import com.datenkrake.R;
import com.datenkrake.SolinApplication;

/**
 * CommunicationStatisticsFragment.
 * A Fragment for showing communication statistics in a barchart.
 * 
 * @author svenja
 */
public class CommunicationFragment extends Fragment implements OnItemSelectedListener{
	private final String TAG = getClass().getSimpleName();
	private DatabaseHandler db;
	private Context context;
	
	private final String SHARED_PREFS_KEY_FIRSTRUN =  "firstrun_" + TAG;
	private final double REDUCE_BARCHART_HEIGHT_TO_PERCENTAGE = 0.9;	// remaining space is for Labels
	private final int[] media = {R.string.all, R.string.calls, R.string.mails, R.string.messages};
	private final int[] mediaIcons = {-1, R.drawable.call, R.drawable.email, R.drawable.chat};
	private final int[] splitBarColors = {R.color.light_red, R.color.light_green, R.color.light_blue};
	private final int[] groups = {R.string.empty, R.string.group_important, R.string.group_unassigned};
	private ArrayList<String> areas;
	private long[][] commFlowsPerArea;	// commFlowsPerArea[media_index][area_index]
	private long[][] commFlowsPerGroup;	// commFlowsPerArea[media_index][group_index]
	private View rootView;
	private LinearLayout barChart;
	private RelativeLayout[] bars;
	private int timeSpan = 1;
	private int checkedRadioButtonId = R.id.fragment_communication_radio_all;
	
	/**
	 * Returns a new instance of this fragment.
	 */
	public static CommunicationFragment newInstance() {
		CommunicationFragment fragment = new CommunicationFragment();
		Bundle args = new Bundle();
		fragment.setArguments(args);
		return fragment;
	}
	
	public CommunicationFragment() {
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
			Alerts.showSimpleOKDialog(context, context.getResources().getString(R.string.action_help), context.getResources().getString(R.string.help_dialog_communication));
		}

		db = SolinApplication.getDbHandler();
		areas = db.getAllAreas();
		commFlowsPerArea = new long[media.length][splitBarColors.length];
		commFlowsPerGroup = new long[media.length][splitBarColors.length];
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		rootView = inflater.inflate(R.layout.fragment_communication, container, false);
		barChart = (LinearLayout) rootView.findViewById(R.id.fragment_communication_barchart);
		Spinner timeSpanSpinner = (Spinner) rootView.findViewById(R.id.fragment_communication_spinner);

		RadioButton radioTotal = (RadioButton) rootView.findViewById(R.id.fragment_communication_radio_all);
		radioTotal.setChecked(true);
		RadioGroup radioGroup = (RadioGroup) rootView.findViewById(R.id.fragment_communication_radio_group);
		radioGroup.setOnCheckedChangeListener(new OnCheckedChangeListener(){
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				updateView(checkedId);
			}
		});
		
		// fill spinner from resources
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_dropdown_item, context.getResources().getStringArray(R.array.statistic_timespans_entries));
		timeSpanSpinner.setAdapter(adapter);
		timeSpanSpinner.setOnItemSelectedListener(this);
		
		// create BarChart
		bars = new RelativeLayout[media.length];
		for (int i = 0; i < media.length; i++){
			bars[i] = (RelativeLayout) inflater.inflate(R.layout.vertical_bar, container, false);
			barChart.addView(bars[i]);
		}
		
		return rootView;
	}
	
	@Override
	public void onResume() {
		super.onResume();
		updateValues(timeSpan);
	}

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
		int newSpan = context.getResources().getIntArray(R.array.statistic_timespans_values)[position];
		updateValues(newSpan);
	}

	@Override
	public void onNothingSelected(AdapterView<?> parent) {
		// nothing currently
	}
	
	/**
	 * Call to u.pdate only UI.
	 * 
	 * @param checkedId	ID of checked RadioButon
	 */
	private void updateView(int checkedId){
		Log.d(TAG, "updateView called");
		checkedRadioButtonId = checkedId;
		int barChartHeight = (int) (barChart.getHeight() * REDUCE_BARCHART_HEIGHT_TO_PERCENTAGE);
		int barChartWidth = barChart.getWidth();
		
		TextView legendTopBar = (TextView) rootView.findViewById(R.id.fragment_communication_legend_top_bar);
		TextView legendMiddleBar = (TextView) rootView.findViewById(R.id.fragment_communication_legend_middle_bar);
		TextView legendBottomBar = (TextView) rootView.findViewById(R.id.fragment_communication_legend_bottom_bar);
		
		int[][] barHeightsPerGroup = new int[media.length][splitBarColors.length];
		int[][] barHeightsPerArea = new int[media.length][splitBarColors.length];
		
		// create the bars
		long highestValue = Math.max(commFlowsPerGroup[0][0], 1);
		for (int i = 0; i < bars.length; i++){

			TextView topBar = (TextView) bars[i].findViewById(R.id.vertical_bar_top);
			TextView middleBar = (TextView) bars[i].findViewById(R.id.vertical_bar_middle);
			TextView bottomBar = (TextView) bars[i].findViewById(R.id.vertical_bar_bottom);
			ImageView barLabelIcon = (ImageView) bars[i].findViewById(R.id.vertical_bar_label_icon);
			TextView barLabelText = (TextView) bars[i].findViewById(R.id.vertical_bar_label_text);
			TextView barOffset = (TextView) bars[i].findViewById(R.id.vertical_bar_offset);
			
			// set width
			int width = (int) barChartWidth / media.length;
			topBar.setWidth(width);
			middleBar.setWidth(width);
			bottomBar.setWidth(width);
			
			// set height
			for (int j = 0; j < splitBarColors.length; j++){
				barHeightsPerGroup[i][j] = (int) (commFlowsPerGroup[i][j] * barChartHeight / highestValue);
				barHeightsPerArea[i][j] = (int) (commFlowsPerArea[i][j] * barChartHeight / highestValue);
			}
			if(checkedId == R.id.fragment_communication_radio_per_area){
				barOffset.setHeight(barChartHeight - barHeightsPerGroup[i][0]);
				topBar.setHeight(barHeightsPerArea[i][0]);
				topBar.setText(""+commFlowsPerArea[i][0]);
				topBar.setBackgroundColor(getResources().getColor(splitBarColors[0]));
				middleBar.setHeight(barHeightsPerArea[i][1]);
				middleBar.setText(""+commFlowsPerArea[i][1]);
				middleBar.setBackgroundColor(getResources().getColor(splitBarColors[1]));
				bottomBar.setHeight(barHeightsPerArea[i][2]);
				bottomBar.setText(""+commFlowsPerArea[i][2]);
				bottomBar.setBackgroundColor(getResources().getColor(splitBarColors[2]));
				legendTopBar.setText(areas.get(0));
				legendTopBar.setTextColor(getResources().getColor(splitBarColors[0]));
				legendMiddleBar.setText(areas.get(1));
				legendMiddleBar.setTextColor(getResources().getColor(splitBarColors[1]));
				legendBottomBar.setText(areas.get(2));
				legendBottomBar.setTextColor(getResources().getColor(splitBarColors[2]));
			} else if(checkedId == R.id.fragment_communication_radio_per_group){
				barOffset.setHeight(barChartHeight - barHeightsPerGroup[i][0]);
				topBar.setHeight(0);
				middleBar.setHeight(barHeightsPerGroup[i][1]);
				middleBar.setText(""+commFlowsPerGroup[i][1]);
				middleBar.setBackgroundColor(getResources().getColor(splitBarColors[0]));
				bottomBar.setHeight(barHeightsPerGroup[i][2]);
				bottomBar.setText(""+commFlowsPerGroup[i][2]);
				bottomBar.setBackgroundColor(getResources().getColor(splitBarColors[1]));
				legendTopBar.setText(groups[0]);
				legendMiddleBar.setText(groups[1]);
				legendMiddleBar.setTextColor(getResources().getColor(splitBarColors[0]));
				legendBottomBar.setText(groups[2]);
				legendBottomBar.setTextColor(getResources().getColor(splitBarColors[1]));
			} else{
				barOffset.setHeight(barChartHeight - barHeightsPerGroup[i][0]);
				topBar.setHeight(0);
				middleBar.setHeight(0);
				bottomBar.setHeight(barHeightsPerGroup[i][0]);
				bottomBar.setBackgroundColor(getResources().getColor(R.color.vertical_bar));
				bottomBar.setText("");
				middleBar.setText("");
				topBar.setText("");
				legendTopBar.setText("");
				legendMiddleBar.setText("");
				legendBottomBar.setText("");
			}
			
			barLabelIcon.setMaxHeight((int) (barChartHeight * (2 - REDUCE_BARCHART_HEIGHT_TO_PERCENTAGE)) - barHeightsPerGroup[i][0]);
			barLabelText.setHeight((int) (barChartHeight * (2 - REDUCE_BARCHART_HEIGHT_TO_PERCENTAGE)) - barHeightsPerGroup[i][0]);
			if (mediaIcons[i]!=-1){
				barLabelIcon.setImageResource(mediaIcons[i]);
				barLabelText.setText(""+commFlowsPerGroup[i][0]);
			} else{
				barLabelText.setText(getResources().getString(media[i]) + ": " + commFlowsPerGroup[i][0]);
			}
		}
	}
	
	/**
	 * Call to update data and UI. 
	 * 
	 * @param newTimeSpan	the new time span for the database-query
	 */
	private void updateValues(int newTimeSpan){
		timeSpan = newTimeSpan;
		
		GregorianCalendar calendar = new GregorianCalendar();
		
		commFlowsPerGroup[0] = db.getCommFlows(calendar, "", timeSpan);
		commFlowsPerGroup[1] = db.getCalls(calendar, "", timeSpan);
		commFlowsPerGroup[2] = db.getMails(calendar, "", timeSpan);
		commFlowsPerGroup[3] = db.getMessages(calendar, "", timeSpan);
		
		int i = 0;
		for(String area : areas){
			commFlowsPerArea[0][i] = db.getCommFlows(calendar, area, timeSpan)[0];
			commFlowsPerArea[1][i] = db.getCalls(calendar, area, timeSpan)[0];
			commFlowsPerArea[2][i] = db.getMails(calendar, area, timeSpan)[0];
			commFlowsPerArea[3][i] = db.getMessages(calendar, area, timeSpan)[0];
			i++;
		}
		
		updateView(checkedRadioButtonId);
	}
	
}