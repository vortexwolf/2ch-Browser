package com.vortexwolf.dvach.settings;

import java.io.File;

import android.content.Context;
import android.os.AsyncTask;
import android.preference.Preference;
import android.util.AttributeSet;

import com.vortexwolf.dvach.R;
import com.vortexwolf.dvach.common.Factory;
import com.vortexwolf.dvach.common.utils.IoUtils;
import com.vortexwolf.dvach.interfaces.ICacheDirectoryManager;

public class CacheSizePreference extends Preference {
    private final File mExternalCacheDir;
    private final File mInternalCacheDir;

    private CalculateCacheSizeTask mCalculateCacheSizeTask;

    public CacheSizePreference(Context context, AttributeSet attrs) {
        super(context, attrs);

        ICacheDirectoryManager cacheManager = Factory.getContainer().resolve(ICacheDirectoryManager.class);
        this.mExternalCacheDir = cacheManager.getExternalCacheDir();
        this.mInternalCacheDir = cacheManager.getInternalCacheDir();

        this.updateSummary();
    }

    @Override
    protected void onPrepareForRemoval() {
        this.mCalculateCacheSizeTask.cancel(true);
        super.onPrepareForRemoval();
    }

    @Override
    protected void onClick() {
        this.setSummary(this.getContext().getString(R.string.loading));

        IoUtils.deleteDirectory(this.mExternalCacheDir);
        IoUtils.deleteDirectory(this.mInternalCacheDir);

        this.updateSummary();

        super.onClick();
    }

    private void updateSummary() {
        this.mCalculateCacheSizeTask = new CalculateCacheSizeTask();
        this.mCalculateCacheSizeTask.execute();
    }

    private class CalculateCacheSizeTask extends AsyncTask<Void, Long, Double> {

        @Override
        protected void onPreExecute() {
            CacheSizePreference.this.setSummary(CacheSizePreference.this.getContext().getString(R.string.loading));
        }

        @Override
        protected void onPostExecute(Double result) {
            String summary = result + " MB";
            CacheSizePreference.this.setSummary(summary);

            CacheSizePreference.this.setEnabled(result > 0);
        }

        @Override
        protected Double doInBackground(Void... arg0) {
            double cacheSize = IoUtils.getSizeInMegabytes(CacheSizePreference.this.mExternalCacheDir, CacheSizePreference.this.mInternalCacheDir);

            return cacheSize;
        }
    }
}
