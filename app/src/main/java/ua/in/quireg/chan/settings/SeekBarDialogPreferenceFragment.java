package ua.in.quireg.chan.settings;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceDialogFragmentCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

import java.lang.ref.WeakReference;

import ua.in.quireg.chan.R;

/**
 * Created by Arcturus Mengsk on 12/1/2017, 1:50 AM.
 * 2ch-Browser
 */

public class SeekBarDialogPreferenceFragment extends PreferenceDialogFragmentCompat
        implements SeekBar.OnSeekBarChangeListener {

    private int mCurrentTempValue;
    private TextView mValueTextView;
    private WeakReference<SeekBarDialogPreference> mSeekBarDialogPreference;

    public static SeekBarDialogPreferenceFragment newInstance(Preference preference) {
        SeekBarDialogPreferenceFragment fragment = new SeekBarDialogPreferenceFragment();
        Bundle bundle = new Bundle(1);
        bundle.putString(ARG_KEY, preference.getKey());
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSeekBarDialogPreference = new WeakReference<>((SeekBarDialogPreference) getPreference());
        mCurrentTempValue = mSeekBarDialogPreference.get().getValue();
    }

    @Override
    protected View onCreateDialogView(Context context) {
        // Inflate layout
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.dialog_slider, null);

        int minValue = mSeekBarDialogPreference.get().getMinValue();
        int maxValue = mSeekBarDialogPreference.get().getMaxValue();
        int step = mSeekBarDialogPreference.get().getStep();

        // Setup minimum and maximum text labels
        ((TextView) view.findViewById(R.id.min_value)).setText(String.valueOf(minValue));
        ((TextView) view.findViewById(R.id.max_value)).setText(String.valueOf(maxValue));

        // Setup SeekBar
        SeekBar mSeekBar = view.findViewById(R.id.seek_bar);

        mSeekBar.setMax((maxValue - minValue) / step);
        mSeekBar.setProgress((mCurrentTempValue - minValue) / step);
        mSeekBar.setOnSeekBarChangeListener(this);

        // Setup text label for current value
        mValueTextView = view.findViewById(R.id.current_value);

        updateValueTextView();

        return view;
    }

    @Override
    public void onDialogClosed(boolean positiveResult) {
        if (positiveResult) {
            mSeekBarDialogPreference.get().setValue(mCurrentTempValue);
        }
    }

    @Override
    public void onProgressChanged(SeekBar seek, int value, boolean fromTouch) {
        int step = mSeekBarDialogPreference.get().getStep();
        int minValue = mSeekBarDialogPreference.get().getMinValue();

        mCurrentTempValue = value * step + minValue;
        // Update label with current value
        updateValueTextView();
    }

    @Override
    public void onStartTrackingTouch(SeekBar seek) {
        // Not used
    }

    @Override
    public void onStopTrackingTouch(SeekBar seek) {
        // Not used
    }

    private void updateValueTextView() {
        String text = String.format(
                mSeekBarDialogPreference.get().getValueFormat(), mCurrentTempValue);
        mValueTextView.setText(text);
    }
}
