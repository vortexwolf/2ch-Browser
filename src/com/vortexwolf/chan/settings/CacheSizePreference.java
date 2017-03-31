package com.vortexwolf.chan.settings;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.preference.Preference;
import android.util.AttributeSet;
import android.util.Pair;

import com.vortexwolf.chan.R;
import com.vortexwolf.chan.common.Factory;
import com.vortexwolf.chan.common.utils.IoUtils;
import com.vortexwolf.chan.services.CacheDirectoryManager;

public class CacheSizePreference extends Preference {

    private CacheDirectoryManager cacheManager;

    private CalculateCacheSizeTask mCalculateCacheSizeTask;

    public CacheSizePreference(Context context, AttributeSet attrs) {
        super(context, attrs);

        cacheManager = Factory.getContainer().resolve(CacheDirectoryManager.class);

        this.updateSummary();
    }

    @Override
    protected void onPrepareForRemoval() {
        this.mCalculateCacheSizeTask.cancel(true);
        super.onPrepareForRemoval();
    }

    @Override
    protected void onClick() {
        new AlertDialog.Builder(getContext())
                .setTitle("Warning!")
                .setMessage("Do you wish to clear cache?")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        setSummary(getContext().getString(R.string.loading));
                        IoUtils.deleteDirectory(cacheManager.getCurrentCacheDirectory());
                        updateSummary();
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialogInterface) {

                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();

        super.onClick();

    }

    public void updateSummary() {
        this.mCalculateCacheSizeTask = new CalculateCacheSizeTask();
        this.mCalculateCacheSizeTask.execute();
    }

    private class CalculateCacheSizeTask extends AsyncTask<Void, Long, Pair<String, Double>[]> {

        @Override
        protected void onPreExecute() {
            CacheSizePreference.this.setSummary(CacheSizePreference.this.getContext().getString(R.string.loading));
        }

        @Override
        @SuppressWarnings("unchecked")
        protected Pair<String, Double>[] doInBackground(Void... arg0) {

            double totalCacheSize = IoUtils.getSizeInMegabytes(cacheManager.getCurrentCacheDirectory());
            double mediaCacheSize = IoUtils.getSizeInMegabytes(cacheManager.getMediaCacheDirectory());
            double pagesCacheSize = IoUtils.getSizeInMegabytes(cacheManager.getPagesCacheDirectory());
            double thumbCacheSize = IoUtils.getSizeInMegabytes(cacheManager.getThumbnailsCacheDirectory());

            return new Pair[]{
                    new Pair("Total", totalCacheSize),
                    new Pair("Media", mediaCacheSize),
                    new Pair("Pages", pagesCacheSize),
                    new Pair("Thumb", thumbCacheSize),
            };
        }

        @Override
        protected void onPostExecute(Pair<String, Double>[] result) {
            long mediaCacheUtilized = Math.round((result[1].second / cacheManager.getCacheSize()) * 100);
            long pagesCacheUtilized = Math.round((result[2].second / cacheManager.getCacheSize()) * 100);
            long thumbCacheUtilized = Math.round((result[3].second / cacheManager.getCacheSize()) * 100);


            String summary = result[0].first + ": " + result[0].second + " Mb \n" +
                    result[1].first + ": " + mediaCacheUtilized + "% " +
                    result[2].first + ": " + pagesCacheUtilized + "% " +
                    result[3].first + ": " + thumbCacheUtilized + "%";


            CacheSizePreference.this.setSummary(summary);

            CacheSizePreference.this.setEnabled(result[0].second > 0);
        }
    }
}
