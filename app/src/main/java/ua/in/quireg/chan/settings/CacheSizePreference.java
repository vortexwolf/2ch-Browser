package ua.in.quireg.chan.settings;

import android.app.AlertDialog;
import android.content.Context;
import android.support.v7.preference.Preference;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.TextView;

import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;
import ua.in.quireg.chan.R;
import ua.in.quireg.chan.common.Factory;
import ua.in.quireg.chan.common.utils.IoUtils;
import ua.in.quireg.chan.services.CacheDirectoryManager;

public class CacheSizePreference extends Preference {

    private static final String TOTAL = "Total";
    private static final String MEDIA = "Media";
    private static final String PAGES = "Pages";
    private static final String THUMB = "Thumb";

    private CacheDirectoryManager mCacheManager = Factory.getContainer().resolve(CacheDirectoryManager.class);

    public CacheSizePreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        updateSummary();
    }

    @Override
    protected void onClick() {

        AlertDialog alertDialog = new AlertDialog.Builder(getContext())
                .setTitle(getContext().getString(R.string.warning_popup_title))
                .setMessage(getContext().getString(R.string.clear_cache_popup_text))
                .setIcon(R.drawable.browser_logo)
                .setPositiveButton(android.R.string.yes, (dialog, which) -> {
                    setSummary(getContext().getString(R.string.loading));
                    IoUtils.deleteDirectory(mCacheManager.getCurrentCacheDirectory());
                    updateSummary();
                })
                .setNegativeButton(android.R.string.no, null)
                .show();
        TextView messageView = alertDialog.findViewById(android.R.id.message);
        messageView.setGravity(Gravity.CENTER);

        super.onClick();

    }

    private void updateSummary() {
        Timber.v("updateSummary()");

        setSummary(getContext().getString(R.string.loading));

        Single.create(e -> {

            double totalCacheUtilization = IoUtils.getSizeInMegabytes(mCacheManager.getCurrentCacheDirectory());
            double mediaCacheUtilization = IoUtils.getSizeInMegabytes(mCacheManager.getMediaCacheDirectory());
            double pagesCacheUtilization = IoUtils.getSizeInMegabytes(mCacheManager.getPagesCacheDirectory());
            double thumbCacheUtilization = IoUtils.getSizeInMegabytes(mCacheManager.getThumbnailsCacheDirectory());

            long mediaCacheUtilizedPercent, pagesCacheUtilizedPercent, thumbCacheUtilizedPercent, cacheSize;

            cacheSize = mCacheManager.getCacheSize();

            mediaCacheUtilizedPercent = Math.round((mediaCacheUtilization / cacheSize) * 100);
            pagesCacheUtilizedPercent = Math.round((pagesCacheUtilization / cacheSize) * 100);
            thumbCacheUtilizedPercent = Math.round((thumbCacheUtilization / cacheSize) * 100);

            String total = String.format("%s: %s Mb ", TOTAL, totalCacheUtilization);
            String media = String.format("%s: %s%% ", MEDIA, mediaCacheUtilizedPercent);
            String pages = String.format("%s: %s%% ", PAGES, pagesCacheUtilizedPercent);
            String thumb = String.format("%s: %s%% ", THUMB, thumbCacheUtilizedPercent);

            String summary = total.concat("\n").concat(media).concat(pages).concat(thumb);

            e.onSuccess(summary);

        })
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe((summary) -> {
            setSummary(String.valueOf(summary));
        });
    }
}
