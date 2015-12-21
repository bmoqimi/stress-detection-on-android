package com.datenkrake.ui;

import java.util.ArrayList;

import android.app.Activity;
import android.app.Fragment;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.datenkrake.Alerts;
import com.datenkrake.Constants;
import com.datenkrake.R;
import com.datenkrake.Util;

/**
 * ContactsFragment.
 * A Fragment for showing the users important contacts.
 * 
 * @author svenja
 */
public class ContactsFragment extends Fragment {
	private final String TAG = getClass().getSimpleName();
	private Context context;
	
	private ArrayList<String> importantContacts;
	private final String SHARED_PREFS_KEY_FIRSTRUN = "firstrun_" + TAG;
	private View rootView;
	
	/**
	 * Returns a new instance of this fragment.
	 */
	public static ContactsFragment newInstance() {
		ContactsFragment fragment = new ContactsFragment();
		Bundle args = new Bundle();
		fragment.setArguments(args);
		return fragment;
	}

	public ContactsFragment() {
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
		
		importantContacts = new ArrayList<String>();
		
		// Is this the first run?
		boolean firstRun = context.getSharedPreferences(Constants.MAIN_SHARED_PREFS_NAME, Context.MODE_PRIVATE)
				.getBoolean(this.SHARED_PREFS_KEY_FIRSTRUN, true);
		if (firstRun) {
			context.getSharedPreferences(Constants.MAIN_SHARED_PREFS_NAME, Context.MODE_PRIVATE).edit()
			.putBoolean(SHARED_PREFS_KEY_FIRSTRUN, false).commit();
			Alerts.showSimpleOKDialog(context, context.getResources().getString(R.string.action_help), context.getResources().getString(R.string.help_dialog_contacts));
		}
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		rootView =  inflater.inflate(R.layout.fragment_contacts,
				container, false);
		return rootView;
	}
	
	@Override
	public void onResume(){
		super.onResume();
		
		boolean importantGroupExists = Util.updateIdOfImportantGroup(context);
		TextView textView = (TextView) rootView.findViewById(R.id.fragment_contacts_no_important_contacts);
		TextView textViewHint = (TextView) rootView.findViewById(R.id.fragment_contacts_no_important_contacts_hint);
		ListView listView = (ListView) rootView.findViewById(R.id.fragment_contacts_important_contacts_list);
		
		if (importantGroupExists){
			importantContacts.clear();
			for (String contact : Util.getAllImportantContacts(context)){
				importantContacts.add(contact);
			}
			if(importantContacts.size() > 0){
				textView.setVisibility(View.GONE);
				textViewHint.setVisibility(View.GONE);
				listView.setVisibility(View.VISIBLE);
				if(listView.getAdapter() == null){
					ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), R.layout.contact, importantContacts);
					listView.setAdapter(adapter);
				} else{
					((ArrayAdapter) listView.getAdapter()).notifyDataSetChanged();
				}
			} else{
				listView.setVisibility(View.GONE);
				textView.setVisibility(View.VISIBLE);
				textViewHint.setText(R.string.no_important_contacts_hint);
				textViewHint.setVisibility(View.VISIBLE);
			}
		} else{
			listView.setVisibility(View.GONE);
			textView.setVisibility(View.VISIBLE);
			textViewHint.setText(R.string.no_important_group_hint);
			textViewHint.setVisibility(View.VISIBLE);
		}
	}

	/**
	 * Starts Android Contacts for manipulating Groups.
	 */
	public static void showGroupChooser(Context context) {
		Intent i = new Intent();
	    i.setComponent(new ComponentName("com.android.contacts", "com.android.contacts.DialtactsContactsEntryActivity"));
	    i.setAction("android.intent.action.MAIN");
	    i.addCategory("android.intent.category.LAUNCHER");
	    i.addCategory("android.intent.category.DEFAULT");
	    context.startActivity(i);
	}
	
}