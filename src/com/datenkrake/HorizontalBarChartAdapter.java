package com.datenkrake;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * Custom Adapter-Class for giving a ListView a BarChart-Look.
 */
public class HorizontalBarChartAdapter extends ArrayAdapter<String>{
	Context context;
	String[] barLeftLabels;
	String[] barRightLabels;
	int[] barWidths;

	public HorizontalBarChartAdapter(Context context, int resource, String[] leftLabels, String[] rightLabels, int[] widths) {
		super(context, resource, leftLabels);
		this.context = context;
		this.barLeftLabels = leftLabels;
		this.barRightLabels = rightLabels;
		this.barWidths = widths;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent){
		RelativeLayout row = (RelativeLayout) convertView;
		if (row == null){
			LayoutInflater inflater = ((Activity) context).getLayoutInflater();
			row = (RelativeLayout) inflater.inflate(R.layout.horizontal_bar, parent, false);
		}
        TextView bar = (TextView) row.findViewById(R.id.horizontal_bar);
        TextView leftLabel = (TextView) row.findViewById(R.id.horizontal_bar_left_label);
        TextView rightLabel = (TextView) row.findViewById(R.id.horizontal_bar_right_label);
        bar.setWidth(barWidths[position]);
        leftLabel.setText(barLeftLabels[position]);
        rightLabel.setText(barRightLabels[position]);
        return row;
	}
	
}