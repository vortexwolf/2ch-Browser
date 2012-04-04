package com.vortexwolf.dvach.settings;

import com.vortexwolf.dvach.R;

import android.content.Context;
import android.content.res.TypedArray;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.SeekBar.OnSeekBarChangeListener;

public class SeekBarPreference extends DialogPreference implements OnSeekBarChangeListener {
	// Namespaces to read attributes
    private static final String ANDROID_NS = "http://schemas.android.com/apk/res/android";

    // Attribute names
    private static final String ATTR_DEFAULT_VALUE = "defaultValue";

    // Default values for defaults
    private static final int DEFAULT_CURRENT_VALUE = 50;
    private static final int DEFAULT_MIN_VALUE = 0;
    private static final int DEFAULT_MAX_VALUE = 100;
    private static final int DEFAULT_STEP = 1;

    // Real defaults
    private final int mDefaultValue;
    private final int mMaxValue;
    private final int mMinValue;
    private final int mStep;
    private final String mValueFormat;
    
    // Current value
    private int mCurrentValue;
    
    // View elements
    private SeekBar mSeekBar;
    private TextView mValueText;

    public SeekBarPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
	
		// Read parameters from attributes
		mDefaultValue = attrs.getAttributeIntValue(ANDROID_NS, ATTR_DEFAULT_VALUE, DEFAULT_CURRENT_VALUE);
	
		TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.SeekBarPreference);
		mMinValue = ta.getInt(R.styleable.SeekBarPreference_minValue, DEFAULT_MIN_VALUE);
		mMaxValue = ta.getInt(R.styleable.SeekBarPreference_maxValue, DEFAULT_MAX_VALUE);
		mStep = ta.getInt(R.styleable.SeekBarPreference_step, DEFAULT_STEP);
		mValueFormat = ta.getString(R.styleable.SeekBarPreference_valueFormat);
		ta.recycle();
    }
    

    @Override
    protected View onCreateDialogView() {
		// Get current value from preferences
		mCurrentValue = getPersistedInt(mDefaultValue);
	
		// Inflate layout
		LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(R.layout.dialog_slider, null);
	
		// Setup minimum and maximum text labels
		((TextView) view.findViewById(R.id.min_value)).setText(Integer.toString(mMinValue));
		((TextView) view.findViewById(R.id.max_value)).setText(Integer.toString(mMaxValue));
	
		// Setup SeekBar
		mSeekBar = (SeekBar) view.findViewById(R.id.seek_bar);
		mSeekBar.setMax((mMaxValue - mMinValue) / mStep);
		mSeekBar.setProgress((mCurrentValue - mMinValue) / mStep);
		mSeekBar.setOnSeekBarChangeListener(this);
	
		// Setup text label for current value
		mValueText = (TextView) view.findViewById(R.id.current_value);
		this.updateCurrentValueText();
	
		return view;
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
		super.onDialogClosed(positiveResult);
	
		// Return if change was cancelled
		if (!positiveResult) {
		    return;
		}
		
		// Persist current value if needed
		if (shouldPersist()) {
		    persistInt(mCurrentValue);
		}
	
		// Notify activity about changes (to update preference summary line)
		notifyChanged();
    }

    @Override
    public CharSequence getSummary() {
		// Format summary string with current value
		String summary = super.getSummary().toString();
		int value = getPersistedInt(mDefaultValue);
		return String.format(summary, value);
    }
    
    @Override
	public void onProgressChanged(SeekBar seek, int value, boolean fromTouch) {
		// Update current value
		mCurrentValue = value * mStep + mMinValue;
		// Update label with current value
		this.updateCurrentValueText();
    }

    @Override
	public void onStartTrackingTouch(SeekBar seek) {
	// Not used
    }

    @Override
	public void onStopTrackingTouch(SeekBar seek) {
	// Not used
    }
    
    private void updateCurrentValueText(){
    	int currentValue = mCurrentValue;
    	String currentValueText = mValueFormat != null ? String.format(mValueFormat, currentValue) : Integer.toString(currentValue);
    	mValueText.setText(currentValueText);
    }
}
