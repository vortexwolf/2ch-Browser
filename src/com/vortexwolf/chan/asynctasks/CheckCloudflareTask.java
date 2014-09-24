package com.vortexwolf.chan.asynctasks;

import java.net.URLEncoder;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.DefaultHttpClient;

import com.vortexwolf.chan.boards.dvach.DvachUriBuilder;
import com.vortexwolf.chan.common.Constants;
import com.vortexwolf.chan.common.Factory;
import com.vortexwolf.chan.common.library.MyLog;
import com.vortexwolf.chan.common.utils.UriUtils;
import com.vortexwolf.chan.exceptions.HttpRequestException;
import com.vortexwolf.chan.interfaces.ICaptchaView;
import com.vortexwolf.chan.interfaces.ICheckCaptchaView;
import com.vortexwolf.chan.models.domain.CaptchaEntity;
import com.vortexwolf.chan.services.http.HttpStreamReader;
import com.vortexwolf.chan.settings.ApplicationSettings;

import android.net.Uri;
import android.os.AsyncTask;

public class CheckCloudflareTask extends AsyncTask<Void, Void, Boolean> {
    public static final String TAG = "CheckCloudflareTask";
    public static final String CHECK_URL_PATH = "cdn-cgi/l/chk_captcha";

    private static final DvachUriBuilder mDvachUriBuilder = Factory.resolve(DvachUriBuilder.class);
    private static final ApplicationSettings mApplicationSettings = Factory.resolve(ApplicationSettings.class);
    private static final HttpStreamReader mHttpStreamReader = Factory.resolve(HttpStreamReader.class);
    private static final DefaultHttpClient mHttpClient = Factory.resolve(DefaultHttpClient.class);

    private final CaptchaEntity mCaptcha;
    private final String mCaptchaAnswer;
    private final ICheckCaptchaView mView;

    private String mErrorMessage = null;

    public CheckCloudflareTask(CaptchaEntity captcha, String answer, ICheckCaptchaView view) {
        this.mCaptcha = captcha;
        this.mCaptchaAnswer = answer;
        this.mView = view;
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        try {
            mHttpClient.getCookieStore().clear();
            
            String uriPath = CHECK_URL_PATH + "?recaptcha_challenge_field=" + this.mCaptcha.getKey() + "&recaptcha_response_field=" + URLEncoder.encode(this.mCaptchaAnswer, Constants.UTF8_CHARSET.name());
            Uri checkUri = mDvachUriBuilder.createUri(uriPath);

            HttpGet request = mHttpStreamReader.createRequest(checkUri.toString(), null);
            HttpClientParams.setRedirecting(request.getParams(), false);

            HttpResponse response = mHttpStreamReader.getResponse(request);

            List<Cookie> cookies = mHttpClient.getCookieStore().getCookies();
            for (Cookie cookie : cookies) {
                if (Constants.CF_CLEARANCE_COOKIE.equals(cookie.getName()) && 
                    UriUtils.areCookieDomainsEqual(cookie.getDomain(), mApplicationSettings.getDomainUri().getHost())) {
                    mApplicationSettings.saveCloudflareClearanceCookie(cookie);
                    return true;
                }
            }
        } catch (Exception e) {
            MyLog.e(TAG, e.getMessage());
            this.mErrorMessage = e.getMessage();
        }

        return false;
    }

    @Override
    protected void onPostExecute(Boolean result) {
        if (result) {
            this.mView.showSuccess();
        } else {
            this.mView.showError(this.mErrorMessage);
        }
    }
}
