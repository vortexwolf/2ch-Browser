package com.vortexwolf.dvach.settings;

import java.io.File;
import com.vortexwolf.dvach.R;
import com.vortexwolf.dvach.common.Factory;
import com.vortexwolf.dvach.common.MainApplication;
import com.vortexwolf.dvach.common.utils.IoUtils;
import com.vortexwolf.dvach.interfaces.ICacheDirectoryManager;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.preference.Preference;
import android.util.AttributeSet;

public class CacheSizePreference extends Preference {
    private final File mExternalCacheDir;
    private final File mInternalCacheDir;

    private CalculateCacheSizeTask mCalculateCacheSizeTask;

    public CacheSizePreference(Context context, AttributeSet attrs) {
        super(context, attrs);

        ICacheDirectoryManager cacheManager = Factory.getContainer().resolve(ICacheDirectoryManager.class);
        mExternalCacheDir = cacheManager.getExternalCacheDir();
        mInternalCacheDir = cacheManager.getInternalCacheDir();

        this.updateSummary();
    }

    @Override
    protected void onPrepareForRemoval() {
        mCalculateCacheSizeTask.cancel(true);
        super.onPrepareForRemoval();
    }

    @Override
    protected void onClick() {
        this.setSummary(getContext().getString(R.string.loading));

        IoUtils.deleteDirectory(mExternalCacheDir);
        IoUtils.deleteDirectory(mInternalCacheDir);

        this.updateSummary();

        super.onClick();
    }

    private void updateSummary() {
        mCalculateCacheSizeTask = new CalculateCacheSizeTask();
        mCalculateCacheSizeTask.execute();
    }

    private class CalculateCacheSizeTask extends AsyncTask<Void, Long, Double> {

        @Override
        protected void onPreExecute() {
            setSummary(getContext().getString(R.string.loading));
        }

        @Override
        protected void onPostExecute(Double result) {
            String summary = result + " MB";
            setSummary(summary);

            setEnabled(result > 0);
        }

        @Override
        protected Double doInBackground(Void... arg0) {
            double cacheSize = IoUtils.getSizeInMegabytes(mExternalCacheDir, mInternalCacheDir);

            return cacheSize;
        }
    }
}
