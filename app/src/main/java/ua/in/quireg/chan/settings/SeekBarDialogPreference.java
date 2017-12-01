package ua.in.quireg.chan.settings;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v7.preference.DialogPreference;
import android.util.AttributeSet;

import ua.in.quireg.chan.R;

public class SeekBarDialogPreference extends DialogPreference {

    // Default values for defaults
    private static final int DEFAULT_VALUE = 50;
    private static final int DEFAULT_MIN_VALUE = 0;
    private static final int DEFAULT_MAX_VALUE = 100;
    private static final int DEFAULT_STEP = 1;

    // Real defaults
    private final int mDefaultValue;
    private final int mMaxValue;
    private final int mMinValue;
    private final int mStep;
    private final String mValueFormat;

    public SeekBarDialogPreference(Context context, AttributeSet attrs) {
        super(context, attrs);

        // Read parameters from attributes
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.SeekBarDialogPreference);

        mDefaultValue = typedArray.getInt(R.styleable.SeekBarDialogPreference_defValue, DEFAULT_VALUE);
        mMinValue = typedArray.getInt(R.styleable.SeekBarDialogPreference_minValue, DEFAULT_MIN_VALUE);
        mMaxValue = typedArray.getInt(R.styleable.SeekBarDialogPreference_maxValue, DEFAULT_MAX_VALUE);
        mStep = typedArray.getInt(R.styleable.SeekBarDialogPreference_step, DEFAULT_STEP);
        mValueFormat = typedArray.getString(R.styleable.SeekBarDialogPreference_valueFormat);

        typedArray.recycle();

    }

    @Override
    public CharSequence getSummary() {
        // Format summary string with current value
        String summary = super.getSummary().toString();
        return String.format(summary, getValue());
    }

    public int getValue() {
        return getPersistedInt(mDefaultValue);
    }

    public void setValue(int newValue) {
        // Persist new value
        persistInt(newValue);

        // Notify activity about changes (to update preference summary line)
        notifyChanged();
    }

    public int getMaxValue() {
        return mMaxValue;
    }

    public int getMinValue() {
        return mMinValue;
    }

    public int getStep() {
        return mStep;
    }

    public String getValueFormat() {
        return mValueFormat;
    }
}
