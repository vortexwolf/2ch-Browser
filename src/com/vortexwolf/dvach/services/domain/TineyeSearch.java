package com.vortexwolf.dvach.services.domain;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;

import android.content.res.Resources;
import android.content.res.Resources.NotFoundException;

import com.vortexwolf.dvach.R;
import com.vortexwolf.dvach.common.Constants;
import com.vortexwolf.dvach.common.library.ExtendedHttpClient;
import com.vortexwolf.dvach.common.library.MyLog;
import com.vortexwolf.dvach.common.utils.StringUtils;
import com.vortexwolf.dvach.exceptions.SendPostException;
import com.vortexwolf.dvach.models.domain.PostEntity;
import com.vortexwolf.dvach.models.domain.PostFields;

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
            throw new SendPostException(mResources.getString(R.string.error_image_search));
        } finally {
            ExtendedHttpClient.releaseRequestResponse(httpPost, response);
        }

        return location;
    }
}
