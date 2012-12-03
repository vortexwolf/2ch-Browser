package com.vortexwolf.dvach.asynctasks;

import org.apache.http.impl.client.DefaultHttpClient;

import com.vortexwolf.dvach.R;
import com.vortexwolf.dvach.common.utils.AppearanceUtils;
import com.vortexwolf.dvach.exceptions.SendPostException;
import com.vortexwolf.dvach.services.BrowserLauncher;
import com.vortexwolf.dvach.services.domain.TineyeSearch;

import android.content.Context;
import android.content.res.Resources;
import android.os.AsyncTask;

public class SearchImageTask extends AsyncTask<Void, Void, String> {

	private final String mImageUrl;
	private final Context mContext;
	private final TineyeSearch mSearch;
	
	public SearchImageTask(String imageUrl, Context context, DefaultHttpClient httpClient){
		mImageUrl = imageUrl;
		mContext = context;
		mSearch = new TineyeSearch(mContext.getResources(), httpClient);
	}
	
	@Override
	protected String doInBackground(Void... params) {
		try {
			return mSearch.search(mImageUrl);
		}
		catch(Exception e) {
			AppearanceUtils.showToastMessage(mContext, e.getMessage());
		}
		
		return null;
	}
	
	@Override
	protected void onPostExecute(final String result) {
		if(result != null){
			BrowserLauncher.launchExternalBrowser(mContext, result);
		}
	}

}
