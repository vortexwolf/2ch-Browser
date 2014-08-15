package com.vortexwolf.chan.asynctasks;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;

import com.vortexwolf.chan.R;
import com.vortexwolf.chan.common.Constants;
import com.vortexwolf.chan.common.Factory;
import com.vortexwolf.chan.common.library.ExtendedHttpClient;
import com.vortexwolf.chan.common.library.MyLog;
import com.vortexwolf.chan.common.utils.AppearanceUtils;
import com.vortexwolf.chan.common.utils.StringUtils;
import com.vortexwolf.chan.settings.ApplicationSettings;

public class CheckPasscodeTask extends AsyncTask<Void, Void, String> {
    private final Context mContext;
    private final ApplicationSettings mSettings;
    private final String mPasscode;
    private final DefaultHttpClient mHttpClient = Factory.resolve(DefaultHttpClient.class);

    private Cookie mUserCodeCookie = null;

    public CheckPasscodeTask(Context context, ApplicationSettings settings, String passcode) {
        this.mContext = context;
        this.mSettings = settings;
        this.mPasscode = passcode;
    }

    @Override
    protected String doInBackground(Void... params) {
        HttpPost post = null;
        HttpResponse response = null;
        try {
            Uri uri = Uri.parse("https://" + Constants.DEFAULT_DOMAIN + "/makaba/makaba.fcgi"); // only .hk domain
            post = new HttpPost(uri.toString());

            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
            nameValuePairs.add(new BasicNameValuePair("task", "auth"));
            nameValuePairs.add(new BasicNameValuePair("usercode", this.mPasscode));
            UrlEncodedFormEntity entity = new UrlEncodedFormEntity(nameValuePairs);
            post.setEntity(entity);

            HttpClientParams.setRedirecting(post.getParams(), false);

            response = this.mHttpClient.execute(post);

            String location = ExtendedHttpClient.getLocationHeader(response);

            List<Cookie> cookies = this.mHttpClient.getCookieStore().getCookies();
            for (Cookie c : cookies) {
                if (c.getName().equals(Constants.USERCODE_COOKIE)) {
                    this.mUserCodeCookie = c;
                    break;
                }
            }

            return location;
        } catch (Exception e) {
            MyLog.e("CheckPasscodeTask", e);
        } finally {
            ExtendedHttpClient.releaseRequestResponse(post, response);
        }

        return null;
    }

    @Override
    protected void onPostExecute(String result) {
        if (StringUtils.isEmpty(this.mPasscode)) {
            this.mSettings.clearPassCodeCookie();
            return;
        }
        
        if (this.mUserCodeCookie != null) {
            this.mSettings.savePassCodeCookie(this.mUserCodeCookie);
        }

        if (StringUtils.emptyIfNull(result).equals("/b/")) {
            AppearanceUtils.showToastMessage(this.mContext, this.mContext.getString(R.string.notification_passcode_correct));
        } else {
            AppearanceUtils.showToastMessage(this.mContext, this.mContext.getString(R.string.notification_passcode_incorrect));
        }
    }
}