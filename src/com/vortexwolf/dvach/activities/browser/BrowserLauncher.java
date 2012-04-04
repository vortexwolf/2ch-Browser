package com.vortexwolf.dvach.activities.browser;

import com.vortexwolf.dvach.common.utils.UriUtils;
import com.vortexwolf.dvach.presentation.services.Tracker;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.Browser;

public class BrowserLauncher {

    private static void launchBrowser(Context context, String url, boolean useExternalBrowser) {
    	
    	Uri uri = Uri.parse(url);

    	// Some URLs should always be opened externally, if BrowserActivity doesn't support their content.
    	if (!useExternalBrowser && (UriUtils.isYoutubeUri(uri) || !UriUtils.isImageUri(uri))){
    		useExternalBrowser = true;
    	}
    		
    	if (useExternalBrowser) {
    		Intent browser = new Intent(Intent.ACTION_VIEW, uri);
    		browser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    		browser.putExtra(Browser.EXTRA_APPLICATION_ID, context.getPackageName());
			context.startActivity(browser);
    	} else {
	    	Intent browser = new Intent(context, BrowserActivity.class);
	    	browser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	    	browser.setData(uri);
			context.startActivity(browser);
    	}
	}
    
    public static void launchExternalBrowser(Context context, String url){
    	launchBrowser(context, url, true);
    }
    
    public static void launchExternalBrowser(Context context, String url, boolean bypassManifestFilter){
    	// Таким образом попробую узнать,пользуется ли кто-нибудь этой опцией
    	Tracker.getInstance().trackEvent(Tracker.CATEGORY_UI, Tracker.ACTION_EXTERNAL_BROWSER_OPTION, url);
    	
    	if(bypassManifestFilter){
    		url = url.replaceFirst("2ch\\.so/", "2ch.so//");
    	}

    	launchExternalBrowser(context, url);
    }
    
    public static void launchInternalBrowser(Context context, String url){
    	launchBrowser(context, url, false);
    }
}
