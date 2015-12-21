package com.datenkrake;

import android.content.Context;
import android.content.res.TypedArray;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TimePicker;

/**
 * A DialogPreference for picking a time. Stores time as a String: "hh:mm".
 * 
 * @author svenja
 */
public class TimePreference extends DialogPreference {
    private int lastHour = 0;
    private int lastMinute = 0;
    private TimePicker timePicker = null;
    
    public TimePreference(Context context, AttributeSet attributes) {
        super(context, attributes);
        setPositiveButtonText(R.string.ok);
        setNegativeButtonText(R.string.cancel);
    }

    @Override
    protected View onCreateDialogView() {
        timePicker = new TimePicker(getContext());
        return(timePicker);
    }

    @Override
    protected void onBindDialogView(View v) {
        super.onBindDialogView(v);
        timePicker.setCurrentHour(lastHour);
        timePicker.setCurrentMinute(lastMinute);
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);
        
        if (positiveResult) {
            lastHour = timePicker.getCurrentHour();
            lastMinute = timePicker.getCurrentMinute();
            String time = String.valueOf(lastHour) + ":" + String.valueOf(lastMinute);
            setSummary(getSummary());
            if (callChangeListener(time)) {
                persistString(time);
                notifyChanged();
            }
        }
    }

    @Override
    protected Object onGetDefaultValue(TypedArray tArray, int index) {
        return (tArray.getString(index));
    }

    @Override
    protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {
        String time;

        if (restoreValue) {
            if (defaultValue == null) {
                time = getPersistedString("12:00");
            }
            else {
                time = getPersistedString((String) defaultValue);
            }
        }
        else {
            if (defaultValue == null) {
                time = "12:00";
            }
            else {
                time = (String) defaultValue;
            }
        }

        lastHour = getHour(time);
        lastMinute = getMinute(time);
        setSummary(getSummary());
    }
        
    @Override
    public CharSequence getSummary() {
    	return  (lastHour >= 10 ? lastHour : ("0" + lastHour)) + ":" + (lastMinute >= 10 ? lastMinute : ("0" + lastMinute));
    }

    public static int getHour(String time) {
        return(Integer.parseInt(time.split(":")[0]));
    }

    public static int getMinute(String time) {
        return(Integer.parseInt(time.split(":")[1]));
    }
}