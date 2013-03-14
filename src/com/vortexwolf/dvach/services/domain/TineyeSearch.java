package com.vortexwolf.dvach.services.domain;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import android.content.res.Resources;

import com.vortexwolf.dvach.R;
import com.vortexwolf.dvach.common.library.ExtendedHttpClient;
import com.vortexwolf.dvach.common.library.MyLog;
import com.vortexwolf.dvach.exceptions.SendPostException;

public class TineyeSearch {
    private static final String TAG = "TineyeSearch";
    private final DefaultHttpClient mHttpClient;
    private final Resources mResources;

    public TineyeSearch(Resources resources, DefaultHttpClient httpClient) {
        this.mHttpClient = httpClient;
        this.mResources = resources;
    }

    public String search(String imageUri) throws SendPostException {
        String searchUri = "http://www.tineye.com/search";

        HttpPost httpPost = new HttpPost(searchUri);
        HttpClientParams.setRedirecting(httpPost.getParams(), false);

        HttpResponse response = null;
        String location = null;
        try {
            List<BasicNameValuePair> nameValuePairs = new ArrayList<BasicNameValuePair>();
            nameValuePairs.add(new BasicNameValuePair("url", imageUri));

            httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
            response = this.mHttpClient.execute(httpPost);

            location = ExtendedHttpClient.getLocationHeader(response);
        } catch (Exception e) {
            MyLog.e(TAG, e);
            throw new SendPostException(this.mResources.getString(R.string.error_image_search));
        } finally {
            ExtendedHttpClient.releaseRequestResponse(httpPost, response);
        }

        return location;
    }
}
