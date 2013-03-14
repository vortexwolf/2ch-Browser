package com.vortexwolf.dvach.asynctasks;

import org.apache.http.impl.client.DefaultHttpClient;

import android.content.Context;
import android.os.AsyncTask;

import com.vortexwolf.dvach.common.utils.AppearanceUtils;
import com.vortexwolf.dvach.services.BrowserLauncher;
import com.vortexwolf.dvach.services.domain.TineyeSearch;

public class SearchImageTask extends AsyncTask<Void, Void, String> {

    private final String mImageUrl;
    private final Context mContext;
    private final TineyeSearch mSearch;

    public SearchImageTask(String imageUrl, Context context, DefaultHttpClient httpClient) {
        this.mImageUrl = imageUrl;
        this.mContext = context;
        this.mSearch = new TineyeSearch(this.mContext.getResources(), httpClient);
    }

    @Override
    protected String doInBackground(Void... params) {
        try {
            return this.mSearch.search(this.mImageUrl);
        } catch (Exception e) {
            AppearanceUtils.showToastMessage(this.mContext, e.getMessage());
        }

        return null;
    }

    @Override
    protected void onPostExecute(final String result) {
        if (result != null) {
            BrowserLauncher.launchExternalBrowser(this.mContext, result);
        }
    }

}
