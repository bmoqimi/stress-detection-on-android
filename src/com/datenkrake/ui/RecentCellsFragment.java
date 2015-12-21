package com.datenkrake.ui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.datenkrake.Cell;
import com.datenkrake.DatabaseHandler;
import com.datenkrake.R;
import com.datenkrake.SolinApplication;
import com.datenkrake.Util;

/**
 * RecentCellsFragment. 
 * A Fragment for showing the cells the user was currently located in and the area to which the cells belong (if known).
 * 
 * @author svenja
 */
public class RecentCellsFragment extends Fragment {
	private String TAG = getClass().getSimpleName();
	private DatabaseHandler db;
	private Context context;
	
	private final int UPDATE_INTERVAL_IN_MS = 10000;
	private ListView cellList;
	private List<String> cellInfos;
	private Thread updateViewThread;
	private Handler threadToUIHandler;
	private boolean fragmentVisible;

	/**
	 * Returns a new instance of this fragment.
	 */
	public static RecentCellsFragment newInstance() {
		RecentCellsFragment fragment = new RecentCellsFragment();
		Bundle args = new Bundle();
		fragment.setArguments(args);
		return fragment;
	}
	
	public RecentCellsFragment() {
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
		cellInfos = new ArrayList<String>();
		fragmentVisible = false;
		threadToUIHandler = new Handler(Looper.getMainLooper()){
			
			@Override
			public void handleMessage(Message msg) {
				updateValuesAndView();
			}
			
		};
	}
	
	@Override
	public void setUserVisibleHint(boolean isVisibleToUser) {
	    super.setUserVisibleHint(isVisibleToUser);

	    if (this.isVisible()) {
	        if (isVisibleToUser) {
	        	fragmentVisible = true;
	    		updateViewThread = new Thread(new Runnable() {
	    			@Override
	    			public void run() {
	    				while (fragmentVisible) {
	    					try {
	    						Message msg = threadToUIHandler.obtainMessage();
	    						threadToUIHandler.sendMessage(msg);
	    						Thread.sleep(UPDATE_INTERVAL_IN_MS);
	    					} catch (InterruptedException e) {
	    					}
	    				}
	    			}
	    		});
	    		updateViewThread.start();
	        } else{
	        	fragmentVisible = false;
	        }
	    }
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_recent_cells,
					container, false);
		cellList = (ListView) rootView.findViewById(R.id.fragment_recent_cells_list);
		registerForContextMenu(cellList);
		return rootView;
	}
	
	@Override
	public void onResume() {
		super.onResume();
		updateValuesAndView();
	}
	
	@Override
	public void onPause() {
		super.onPause();
		fragmentVisible = false;
	}

	@Override  
	public void onCreateContextMenu(ContextMenu menu, View v,ContextMenuInfo menuInfo) {  
		super.onCreateContextMenu(menu, v, menuInfo);
		if (v.getId() == R.id.fragment_recent_cells_list) {
			menu.setHeaderTitle(R.string.title_dialog_choose_action);  
		    menu.add(0, v.getId(), 0, R.string.delete_from_area);
		    menu.add(0, v.getId(), 0, R.string.add_to_area);
		}
	}
	
	@Override  
	public boolean onContextItemSelected(MenuItem item) {
		// get item which was selected
    	AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
    	String listEntry = (String)cellList.getAdapter().getItem(info.position);
    	Log.i(TAG, "Listentry chosen: " + listEntry);
    	final String location = listEntry.substring(listEntry.lastIndexOf("["), listEntry.lastIndexOf("]")+1);
    	Log.i(TAG, "Location string from listentry: " + location);
		if(item.getTitle().equals(getResources().getString(R.string.delete_from_area))) {
			deleteSelectedCellFromArea(location);
			Toast.makeText(context, "Zelle erfolgreich aus dem Gebiet gelöscht", Toast.LENGTH_SHORT).show();
	    } else if(item.getTitle().equals(getResources().getString(R.string.add_to_area))) {
	    	
	    	// get all available areas
	    	Object[] areas = db.getRealAreas().toArray();
			final String[] areaNames = Arrays.copyOf(areas, areas.length, String[].class);
			
			AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
			alertDialogBuilder.setTitle("Gebiet auswählen");
			alertDialogBuilder.setItems(areaNames, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					String area = areaNames[which];
					int areaId = db.getAreaId(area);
			    	addSelectedCellToArea(location, areaId);
					Toast.makeText(context, "Zelle erfolgreich dem Gebiet " + area + " hinzugefügt", Toast.LENGTH_SHORT).show();
				}
			});
			AlertDialog dialog = alertDialogBuilder.create();
			dialog.show();
		} else {
	    	return false;
	    }  
		return true;  
	}
	
	/**
	 * Adds a selected cell to a selected area.
	 * 
	 * @param locationString The locationString we add to the area
	 * @param areaId The areaId we want to add this cell to
	 */
	private void addSelectedCellToArea (String locationString, int areaId) {
		db.addAreaValue(locationString, areaId, true);
		updateValuesAndView();
	}
	
	/**
	 * Deletes a selected cell from a selected area.
	 * 
	 * @param locationString The locationString we add to the area
	 * @param areaId The areaId we want to add this cell to
	 */
	private void deleteSelectedCellFromArea (String locationString) {
		db.deleteAreaValue(locationString);
		updateValuesAndView();
	}
	
	/**
	 * Call to update data and UI.
	 */
	private void updateValuesAndView(){
		cellInfos.clear();
		ArrayList<Cell> cells = db.getLastKnownCells(40);
		for (int i = 0; i < cells.size(); i++) {
			cellInfos.add(Util.getFormattedDate(cells.get(i).getDate()) + " - Zelle: " + cells.get(i).getLocationString() 
					+ "\nGebiet: " + cells.get(i).getArea());		
		}
		if(cellList.getAdapter() == null){
			ArrayAdapter<String> cellAdapter = new ArrayAdapter<String>(context, R.layout.cell, cellInfos);
			cellList.setAdapter(cellAdapter);
		} else{
			((ArrayAdapter) cellList.getAdapter()).notifyDataSetChanged();
		}
	}
	
}