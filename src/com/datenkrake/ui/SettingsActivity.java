package com.datenkrake.ui;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.view.MenuItem;
import android.widget.Toast;

import com.datenkrake.CsvHandler;
import com.datenkrake.R;
import com.datenkrake.StressManager;
import com.datenkrake.UploadTask;

/**
 * 
 * @author Thomas
 *
 */
public class SettingsActivity extends PreferenceActivity implements OnSharedPreferenceChangeListener{
	
	public static final String KEY_PREF_STRESS_EARLY = "stress_early";
	public static final String KEY_PREF_STRESS_LATE = "stress_late";
	public static final String KEY_PREF_DATA_UPLOAD = "data_upload_button";
	SharedPreferences prefs;

	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getActionBar().setDisplayHomeAsUpEnabled(true);
        Context context = getApplicationContext(); 
        prefs = PreferenceManager.getDefaultSharedPreferences(context); 
        addPreferencesFromResource(R.xml.pref_global);
        
        Preference button = (Preference)findPreference(KEY_PREF_DATA_UPLOAD);
        button.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
			
			@Override
			public boolean onPreferenceClick(Preference preference) {
				CsvHandler csv = new CsvHandler();
				new UploadTask(getApplicationContext()).execute(csv.getCommFlowsFilename());
				new UploadTask(getApplicationContext()).execute(csv.getHeartbeatFilename());
				new UploadTask(getApplicationContext()).execute(csv.getAppTimesFilename());
				return true;
			}
		});
	}
	
	@Override
	protected void onResume() {
		super.onResume();
	        prefs.registerOnSharedPreferenceChangeListener(this);
	}

        @Override
	protected void onPause() {		
		super.onPause();
		prefs.unregisterOnSharedPreferenceChangeListener(this);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			return true;
        default:
            return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
		SharedPreferences.Editor prefEditor = prefs.edit();
		if (key.equals(KEY_PREF_STRESS_EARLY)
				|| key.equals(KEY_PREF_STRESS_LATE)) {
			String[] early = prefs.getString(KEY_PREF_STRESS_EARLY, getResources().getString(R.string.pref_stress_query_early_default))
					.split(":");
			String[] late = prefs.getString(KEY_PREF_STRESS_LATE, getResources().getString(R.string.pref_stress_query_late_default))
					.split(":");
			int success = StressManager.setStressTimes(this,
					Integer.parseInt(early[0]), Integer.parseInt(early[1]),
					Integer.parseInt(late[0]), Integer.parseInt(late[1]));
			if (success != 1) {
				Toast.makeText(this, R.string.stress_query_late_after_early, Toast.LENGTH_LONG).show();
				prefEditor.putString(KEY_PREF_STRESS_EARLY, getResources().getString(R.string.pref_stress_query_early_default));
				prefEditor.putString(KEY_PREF_STRESS_LATE, getResources().getString(R.string.pref_stress_query_late_default));
				prefEditor.commit();
				reload();
			}
		}
		return;
	}

	private void reload() {
		startActivity(getIntent());
		finish();
	}
}
