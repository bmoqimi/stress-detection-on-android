package com.datenkrake.ui;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.LinkedHashMap;
import java.util.Locale;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.BarChart;
import org.achartengine.chart.CubicLineChart;
import org.achartengine.chart.PointStyle;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.text.format.Time;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.datenkrake.Area;
import com.datenkrake.Constants;
import com.datenkrake.DatabaseHandler;
import com.datenkrake.R;
import com.datenkrake.SolinApplication;

/**
 * MeasuredStressFragment.
 * A Fragment for showing the users stress based on heartrate.
 * 
 * TODO: solve scrolling-problems
 * TODO: keep scale and position when changing date
 * TODO: show last 5 hours at start
 * TODO: define constants for intervals and stresslevel
 * 
 * @author svenja
 */
public class MeasuredStressFragment extends Fragment {
	private final String TAG = getClass().getSimpleName();
	private DatabaseHandler db;
	private Context context;
	
	private View rootView;
	private String todaysDate;
	private Button back, forward, toToday;
	private GregorianCalendar currentDay;
	private double[] measuredStress;
	private int[] perceivedStress;
	private int[][] areaHistory;
    private GraphicalView chart;
    private String[] xLabels;
    private int[] areaColors;
	
	/**
	 * Returns a new instance of this fragment.
	 */
	public static MeasuredStressFragment newInstance() {
		MeasuredStressFragment fragment = new MeasuredStressFragment();
		Bundle args = new Bundle();
		fragment.setArguments(args);
		return fragment;
	}

	public MeasuredStressFragment() {
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
		
	    xLabels = getResources().getStringArray(R.array.stress_plot_x_labels);
	    areaColors = getResources().getIntArray(R.array.area_colors);
	    measuredStress = new double[xLabels.length * Constants.MEASUREMENTS_PER_XLABEL_DISTANCE];
	    perceivedStress = new int[xLabels.length * Constants.MEASUREMENTS_PER_XLABEL_DISTANCE];
	    areaHistory = new int[db.getAllAreas().size()][];
	    for (String area : db.getAllAreas()){ // NOTE: buggy if user is later being able to add/delete areas (-> gaps in areaIDs)
	    	areaHistory[db.getAreaId(area)-1] = new int[xLabels.length * Constants.MEASUREMENTS_PER_XLABEL_DISTANCE];
	    }
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		rootView = inflater.inflate(R.layout.fragment_measured_stress, container, false);
		OnClickListener listener = new OnClickListener(){
			@Override
			public void onClick(View v) {
				changeDate(v);
			}
		};
		back = (Button) rootView.findViewById(R.id.fragment_measured_stress_controls_back);
		back.setOnClickListener(listener);
		back.setText("<");
		forward = (Button) rootView.findViewById(R.id.fragment_measured_stress_controls_forward);
		forward.setOnClickListener(listener);
		forward.setText(">");
		toToday = (Button) rootView.findViewById(R.id.fragment_measured_stress_controls_today);
		toToday.setOnClickListener(listener);
		toToday.setText("Heute");
		currentDay = new GregorianCalendar();
		todaysDate = currentDay.get(Calendar.DATE) + ". " + currentDay.getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.GERMANY);
		updateValuesAndView();
		return rootView;
	}
	
	private void changeDate(View v){
		switch(v.getId()){
		case(R.id.fragment_measured_stress_controls_back):
			currentDay.add(Calendar.DATE, -1);
			break;
		case(R.id.fragment_measured_stress_controls_forward):
			currentDay.add(Calendar.DATE, 1);
			break;
		case(R.id.fragment_measured_stress_controls_today):
			currentDay = new GregorianCalendar();
			break;
		}
		updateValuesAndView();
	}
	
	/**
	 * Call to update data and UI.
	 */
	private void updateValuesAndView() {
		// update date selector
		TextView currentDate = (TextView) rootView.findViewById(R.id.fragment_measured_stress_current_date);
		String newDate = currentDay.get(Calendar.DATE) + ". " + currentDay.getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.GERMANY);
		currentDate.setText(newDate);
		forward.setEnabled(!newDate.equals(todaysDate));
		toToday.setEnabled(!newDate.equals(todaysDate));
		
		// update data from database
        transformData();
		
		// draw chart
		updateChart();
	}
	
	/**
	 * Transforms data from database to the format in which it can be plotted
	 */
	private void transformData(){
		
		DateFormat formatter = DateFormat.getTimeInstance();
		int nextIndex = 0;
		int currentIndex = 0;
		GregorianCalendar day = new GregorianCalendar();
		day.set(currentDay.get(Calendar.YEAR), currentDay.get(Calendar.MONTH), currentDay.get(Calendar.DATE));
		
		// Area History Data
		ArrayList<Area> areaHistoryList = db.getAreaHistoryOfDay(day);
		for (int[] history : areaHistory){
			for (int i = 0; i < history.length; i++){
				history[i] = 0;
			}
		}
		int currentAreaId = 0;
		for (Area area : areaHistoryList){
			String time = formatter.format(new Date(area.getStartTime()));
			int nextStartTimeHour = Integer.parseInt(time.split(":")[0]);
			int nextStartTimeMinute = Integer.parseInt(time.split(":")[1]);
			nextIndex = nextStartTimeHour * Constants.MEASUREMENTS_PER_XLABEL_DISTANCE + nextStartTimeMinute / 10;
			while(currentIndex < nextIndex){
				areaHistory[currentAreaId][currentIndex] = 5;
				currentIndex++;
			}
			currentAreaId = db.getAreaId(area.getName())-1; // sqlite ids start at 1!
		}
		// fill up from last Area-Start-Time until now/23:59
		String currentDate = currentDay.get(Calendar.DATE) + ". " + currentDay.getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.GERMANY);
		if (currentDate.equals(todaysDate)){
			// until now
			Time now = new Time();
			now.setToNow();
			nextIndex = now.hour * Constants.MEASUREMENTS_PER_XLABEL_DISTANCE + now.minute / 10;
		} else{
			// until 23:59
			nextIndex = xLabels.length * Constants.MEASUREMENTS_PER_XLABEL_DISTANCE;
		}
		while(currentIndex < nextIndex){
			areaHistory[currentAreaId][currentIndex] = 5;
			currentIndex++;
		}
		
		// Measured Stress Data
		day.set(currentDay.get(Calendar.YEAR), currentDay.get(Calendar.MONTH), currentDay.get(Calendar.DATE));
		LinkedHashMap<String,Double> measuredStressMap = db.getHeartrateStressLevelOfDay(day);
		currentIndex = 0;
		for (double d : measuredStressMap.values()){
			measuredStress[currentIndex] = d;
			currentIndex++;
		}
		
		// Perceived Stress Data
		day.set(currentDay.get(Calendar.YEAR), currentDay.get(Calendar.MONTH), currentDay.get(Calendar.DATE));
		LinkedHashMap<Long,Integer> perceivedStressMap = db.getStressNotificationTimeAnswered(day);
		for (int i = 0; i < perceivedStress.length; i++){
			perceivedStress[i] = 0;
		}
		Log.d(TAG, "size " + perceivedStressMap.size());
		nextIndex = 0;
		for (long millis : perceivedStressMap.keySet()){
			String time = formatter.format(new Date(millis));
			int hour = Integer.parseInt(time.split(":")[0]);
			int minute = Integer.parseInt(time.split(":")[1]);
			nextIndex = (hour * Constants.MEASUREMENTS_PER_XLABEL_DISTANCE) + (minute / 10);
			perceivedStress[nextIndex] = perceivedStressMap.get(millis);
			Log.d(TAG, "setting index " + nextIndex + " to " + perceivedStressMap.get(millis));
		}
		
		// Debug
		String measuredStressStr = "";
		String perceivedStressStr = "";
		String area0Str = "";
		String area1Str = "";
		String area2Str = "";
		for (int i = 0; i < measuredStress.length; i++){
			measuredStressStr = measuredStressStr + ", " + measuredStress[i];
			perceivedStressStr = perceivedStressStr + ", " + perceivedStress[i];
			area0Str = area0Str + ", " + areaHistory[0][i];
			area1Str = area1Str + ", " + areaHistory[1][i];
			area2Str = area2Str + ", " + areaHistory[2][i];
		}
	    Log.d(TAG, "Data:\nMeasuredStress: " + measuredStressStr + "\nPerceivedStress: " + perceivedStressStr + "\nArea0: " + area0Str + "\nArea1: " + area1Str + "\nArea2: " + area2Str);
	}
	
	/**
	 * Draws/Updates the plot of the data.
	 */
	private void updateChart(){
    	
    	// Creating the XYSeries
        XYSeries measuredStressSeries = new XYSeries("Measured Stress");
        XYSeries perceivedStressSeries = new XYSeries("Perceived Stress");
        XYSeries[] areaSeries = new XYSeries[db.getAllAreas().size()];
    	int i = 0;
        for (String area : db.getAllAreas()){
        	areaSeries[i] = new XYSeries(area);
        	i++;
        }
        
        // Adding data to Series
        for(i = 0 ; i < (Constants.MEASUREMENTS_PER_XLABEL_DISTANCE * xLabels.length) ; i++){
            measuredStressSeries.add(i, measuredStress[i]);
            perceivedStressSeries.add(i, perceivedStress[i]);
            for (int j = 0; j < areaSeries.length; j++){
            	areaSeries[j].add(i, areaHistory[j][i]);
            }
        }
 
        // Creating a Dataset to hold Series
        XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();
        for (int j = 0; j < areaSeries.length; j++){
        	dataset.addSeries(areaSeries[j]);
        }
        dataset.addSeries(measuredStressSeries);
        dataset.addSeries(perceivedStressSeries);
 
        // Creating XYSeriesRenderers to customize Series
        XYSeriesRenderer[] areaRenderers = new XYSeriesRenderer[areaSeries.length];
        for (int j = 0; j < areaSeries.length; j++){
        	areaRenderers[j] = new XYSeriesRenderer();
            areaRenderers[j].setPointStyle(PointStyle.POINT);
            areaRenderers[j].setFillPoints(true);
            areaRenderers[j].setLineWidth(2);
            areaRenderers[j].setDisplayChartValues(false);
            areaRenderers[j].setColor(areaColors[j]);
        }
        
        XYSeriesRenderer measuredStressRenderer = new XYSeriesRenderer();
        measuredStressRenderer.setColor(Color.WHITE);
        measuredStressRenderer.setPointStyle(PointStyle.POINT);
        measuredStressRenderer.setFillPoints(true);
        measuredStressRenderer.setLineWidth(2);
        measuredStressRenderer.setDisplayChartValues(false);
 
        XYSeriesRenderer perceivedStressRenderer = new XYSeriesRenderer();
        perceivedStressRenderer.setColor(Color.WHITE);
        perceivedStressRenderer.setPointStyle(PointStyle.POINT);
        perceivedStressRenderer.setFillPoints(true);
        perceivedStressRenderer.setLineWidth(2);
        perceivedStressRenderer.setDisplayChartValues(false);
 
        // Creating a XYMultipleSeriesRenderer to customize the whole chart
        XYMultipleSeriesRenderer multiRenderer = new XYMultipleSeriesRenderer();
        // Axes and Titles
        multiRenderer.setXTitle("Time");
        multiRenderer.setYTitle("Stresslevel/Area ID");
        multiRenderer.setAxisTitleTextSize(30);
        multiRenderer.setAxesColor(getResources().getColor(android.R.color.white));
        multiRenderer.setXAxisMax(36);
        multiRenderer.setXAxisMin(0);
        multiRenderer.setYAxisMax(5);
        multiRenderer.setYAxisMin(0);
        multiRenderer.setXLabels(0);	// to make the x-axis not be labeled by 0,1,2,...
        // Labels and Legend
        multiRenderer.setLabelsTextSize(30);
        multiRenderer.setLegendTextSize(20);
        multiRenderer.setXLabelsAngle(45);
        multiRenderer.setXLabelsPadding(50);
        multiRenderer.setYLabelsPadding(20);
        // Zoom and Scroll
        multiRenderer.setZoomEnabled(true, false);
        multiRenderer.setPanEnabled(true, false);
        multiRenderer.setPanLimits(new double[]{0,144,0,5});
        multiRenderer.setZoomButtonsVisible(true);
        // Background and Margins
        multiRenderer.setBarSpacing(0);
        multiRenderer.setBackgroundColor(getResources().getColor(android.R.color.black));
        multiRenderer.setMargins(new int[]{40,80,80,40});;
        multiRenderer.setMarginsColor(getResources().getColor(android.R.color.black));
        
        for(i = 0 ; i < (Constants.MEASUREMENTS_PER_XLABEL_DISTANCE * xLabels.length) ; i++){
        	if (i % 6 == 0){
                multiRenderer.addXTextLabel(i, xLabels[i/Constants.MEASUREMENTS_PER_XLABEL_DISTANCE]);
        	}
        }
 
        // Adding incomeRenderer and expenseRenderer to multipleRenderer
        // Note: The order of adding dataseries to dataset and renderers to multipleRenderer
        // should be same
        for (XYSeriesRenderer areaRenderer : areaRenderers){
        	multiRenderer.addSeriesRenderer(areaRenderer);
        }
        multiRenderer.addSeriesRenderer(measuredStressRenderer);
        multiRenderer.addSeriesRenderer(perceivedStressRenderer);
 
        // Getting a reference to LinearLayout of the MainActivity Layout
        LinearLayout chartContainer = (LinearLayout) rootView.findViewById(R.id.chart_container);
 
        // Specifying chart types to be drawn in the graph
        // Number of data series and number of types should be same
        // Order of data series and chart type will be same
        String[] types = new String[] {BarChart.TYPE, BarChart.TYPE, BarChart.TYPE, CubicLineChart.TYPE, BarChart.TYPE};
 
        if (chart != null){
        	chartContainer.removeView(chart);
        }
        
        // Creating a combined chart with the chart types specified in types array
        chart = (GraphicalView) ChartFactory.getCombinedXYChartView(getActivity().getBaseContext(), dataset, multiRenderer, types);
     
        // Adding the Combined Chart to the LinearLayout
        chartContainer.addView(chart);
        
    }

}