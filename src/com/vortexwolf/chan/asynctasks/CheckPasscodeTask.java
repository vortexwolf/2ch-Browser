package com.vortexwolf.chan.asynctasks;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import android.net.Uri;
import android.os.AsyncTask;

import com.vortexwolf.chan.common.Constants;
import com.vortexwolf.chan.common.Factory;
import com.vortexwolf.chan.common.library.ExtendedHttpClient;
import com.vortexwolf.chan.common.library.MyLog;
import com.vortexwolf.chan.common.utils.StringUtils;
import com.vortexwolf.chan.common.utils.UriUtils;
import com.vortexwolf.chan.interfaces.ICheckPasscodeView;
import com.vortexwolf.chan.interfaces.IUrlBuilder;
import com.vortexwolf.chan.interfaces.IWebsite;
import com.vortexwolf.chan.settings.ApplicationSettings;

public class CheckPasscodeTask extends AsyncTask<Void, Void, String> {
    private final ICheckPasscodeView mCheckPasscodeView;
    private final String mPasscode;
    private final DefaultHttpClient mHttpClient = Factory.resolve(DefaultHttpClient.class);
    private final ApplicationSettings mApplicationSettings = Factory.resolve(ApplicationSettings.class);
    private final IWebsite mWebsite;

    private Cookie mUserCodeCookie = null;
    private String mErrorMessage = null;

    public CheckPasscodeTask(IWebsite website, ICheckPasscodeView view) {
        this.mWebsite = website;
        this.mCheckPasscodeView = view;
        this.mPasscode = this.mApplicationSettings.getPasscodeRaw();
    }

    public CheckPasscodeTask(IWebsite website, ICheckPasscodeView view, String passcode) {
        this.mWebsite = website;
        this.mCheckPasscodeView = view;
        this.mPasscode = passcode;
    }

    @Override
    protected String doInBackground(Void... params) {
        HttpPost post = null;
        HttpResponse response = null;
        try {
            String url = this.mWebsite.getUrlBuilder().getPasscodeCheckUrl();
            post = new HttpPost(url);

            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
            if (StringUtils.isEmpty(this.mPasscode)) {
                nameValuePairs.add(new BasicNameValuePair("task", "logout"));
            } else {
                nameValuePairs.add(new BasicNameValuePair("task", "auth"));
                nameValuePairs.add(new BasicNameValuePair("usercode", this.mPasscode));
            }
            UrlEncodedFormEntity entity = new UrlEncodedFormEntity(nameValuePairs);
            post.setEntity(entity);

            HttpClientParams.setRedirecting(post.getParams(), false);

            response = this.mHttpClient.execute(post);
            StatusLine status = response.getStatusLine();
            if (status.getStatusCode() != 302 && status.getStatusCode() != 301) {
                this.mErrorMessage = status.getStatusCode() + " - " + status.getReasonPhrase();
                return null;
            }

            String location = ExtendedHttpClient.getLocationHeader(response);

            List<Cookie> cookies = this.mHttpClient.getCookieStore().getCookies();
            for (Cookie c : cookies) {
                if (c.getName().equals(Constants.USERCODE_NOCAPTCHA_COOKIE) &&
                    UriUtils.areCookieDomainsEqual(c.getDomain(), Uri.parse(url).getHost())) {
                    this.mUserCodeCookie = c;
                    break;
                }
            }

            return location;
        } catch (Exception e) {
            MyLog.e("CheckPasscodeTask", e);
            this.mErrorMessage = e.getMessage();
        } finally {
            ExtendedHttpClient.releaseRequestResponse(post, response);
        }

        return null;
    }

    @Override
    protected void onPostExecute(String result) {
        if (StringUtils.isEmpty(this.mPasscode)) {
            this.mApplicationSettings.clearPassCodeCookie();
            this.mCheckPasscodeView.onPasscodeRemoved();
            return;
        }

        if (this.mUserCodeCookie != null) {
            this.mApplicationSettings.savePassCodeCookie(this.mUserCodeCookie);
        } else {
            this.mApplicationSettings.clearPassCodeCookie();
        }

        boolean isSuccess = StringUtils.emptyIfNull(result).equals("/b/");
        this.mCheckPasscodeView.onPasscodeChecked(isSuccess, this.mErrorMessage);
    }
}