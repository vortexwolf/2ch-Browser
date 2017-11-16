package ua.in.quireg.chan.settings;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.support.v7.preference.Preference;
import android.util.AttributeSet;
import android.util.Log;

import java.lang.ref.WeakReference;
import java.util.HashMap;

import ua.in.quireg.chan.R;
import ua.in.quireg.chan.common.Factory;
import ua.in.quireg.chan.common.utils.IoUtils;
import ua.in.quireg.chan.services.CacheDirectoryManager;

public class CacheSizePreference extends Preference {

    private static final String LOG_TAG = CacheSizePreference.class.getSimpleName();

    private CalculateCacheSizeTask mCalculateCacheSizeTask;
    private CacheDirectoryManager mCacheManager = Factory.getContainer().resolve(CacheDirectoryManager.class);

    public CacheSizePreference(Context context, AttributeSet attrs) {
        super(context, attrs);

        updateSummary();
    }

    @Override
    protected void onPrepareForRemoval() {
        mCalculateCacheSizeTask.cancel(true);
        super.onPrepareForRemoval();
    }

    @Override
    protected void onClick() {
        new AlertDialog.Builder(getContext())
                .setTitle("Warning!")
                .setMessage("Do you wish to clear cache?")
                .setPositiveButton(android.R.string.yes, (dialog, which) -> {
                    setSummary(getContext().getString(R.string.loading));
                    IoUtils.deleteDirectory(mCacheManager.getCurrentCacheDirectory());
                    updateSummary();
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();

        super.onClick();

    }

    private void updateSummary() {
        if (mCalculateCacheSizeTask != null && !mCalculateCacheSizeTask.isCancelled()) {
            mCalculateCacheSizeTask.cancel(true);
        }
        mCalculateCacheSizeTask = new CalculateCacheSizeTask(getContext());
        mCalculateCacheSizeTask.execute();
    }

    @SuppressLint("StaticFieldLeak")
    private class CalculateCacheSizeTask extends AsyncTask<Void, Long, HashMap<String, Double>> {

        private WeakReference<Context> mContext;

        private static final String TOTAL = "Total";
        private static final String MEDIA = "Media";
        private static final String PAGES = "Pages";
        private static final String THUMB = "Thumb";

        CalculateCacheSizeTask(Context context) {
            mContext = new WeakReference<>(context);
        }

        @Override
        protected void onPreExecute() {
            CacheSizePreference.this.setSummary(mContext.get().getString(R.string.loading));
            Log.d(LOG_TAG, "Updating cache size counter");
        }

        @Override
        protected HashMap<String, Double> doInBackground(Void... arg0) {

            double totalCacheSize = IoUtils.getSizeInMegabytes(mCacheManager.getCurrentCacheDirectory());
            double mediaCacheSize = IoUtils.getSizeInMegabytes(mCacheManager.getMediaCacheDirectory());
            double pagesCacheSize = IoUtils.getSizeInMegabytes(mCacheManager.getPagesCacheDirectory());
            double thumbCacheSize = IoUtils.getSizeInMegabytes(mCacheManager.getThumbnailsCacheDirectory());

            HashMap<String, Double> result = new HashMap<>();

            result.put(TOTAL, totalCacheSize);
            result.put(MEDIA, mediaCacheSize);
            result.put(PAGES, pagesCacheSize);
            result.put(THUMB, thumbCacheSize);

            return result;

        }

        @Override
        protected void onPostExecute(HashMap<String, Double> result) {

            long mediaCacheUtilized, pagesCacheUtilized, thumbCacheUtilized;

            mediaCacheUtilized = Math.round((result.get(MEDIA) / mCacheManager.getCacheSize()) * 100);
            pagesCacheUtilized = Math.round((result.get(PAGES) / mCacheManager.getCacheSize()) * 100);
            thumbCacheUtilized = Math.round((result.get(THUMB) / mCacheManager.getCacheSize()) * 100);

            String summary = TOTAL + ": " + result.get(TOTAL) + " Mb \n" +
                    MEDIA + ": " + mediaCacheUtilized + "% " +
                    PAGES + ": " + pagesCacheUtilized + "% " +
                    THUMB + ": " + thumbCacheUtilized + "%";

            CacheSizePreference.this.setSummary(summary);
            CacheSizePreference.this.setEnabled(result.get(TOTAL) > 0);

            Log.d(LOG_TAG, "Cache size counter update completed");
        }
    }
}
