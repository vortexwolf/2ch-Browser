package ua.in.quireg.chan.asynctasks;

import java.net.URLEncoder;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.DefaultHttpClient;

import android.net.Uri;
import android.os.AsyncTask;

import ua.in.quireg.chan.common.Constants;
import ua.in.quireg.chan.common.Factory;
import ua.in.quireg.chan.common.library.ExtendedHttpClient;
import ua.in.quireg.chan.common.library.MyLog;
import ua.in.quireg.chan.common.utils.UriUtils;
import ua.in.quireg.chan.interfaces.ICheckCaptchaView;
import ua.in.quireg.chan.interfaces.IUrlBuilder;
import ua.in.quireg.chan.interfaces.IWebsite;
import ua.in.quireg.chan.models.domain.CaptchaEntity;
import ua.in.quireg.chan.services.http.HttpStreamReader;
import ua.in.quireg.chan.settings.ApplicationSettings;

public class CheckCloudflareTask extends AsyncTask<Void, Void, Boolean> {
    public static final String TAG = "CheckCloudflareTask";
    public static final String CHECK_URL_PATH = "cdn-cgi/l/chk_captcha";

    private final ApplicationSettings mApplicationSettings = Factory.resolve(ApplicationSettings.class);
    private final HttpStreamReader mHttpStreamReader = Factory.resolve(HttpStreamReader.class);
    private final ExtendedHttpClient mHttpClient = (ExtendedHttpClient)Factory.resolve(DefaultHttpClient.class);
    private IUrlBuilder mUrlBuilder;

    private final CaptchaEntity mCaptcha;
    private final String mCaptchaAnswer;
    private final ICheckCaptchaView mView;

    private String mErrorMessage = null;

    public CheckCloudflareTask(IWebsite website, CaptchaEntity captcha, String answer, ICheckCaptchaView view) {
        this.mCaptcha = captcha;
        this.mCaptchaAnswer = answer;
        this.mView = view;
        this.mUrlBuilder = website.getUrlBuilder();
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        try {
            this.mHttpClient.removeCookie(Constants.CF_CLEARANCE_COOKIE);

            String url = this.mUrlBuilder.getCloudflareCheckUrl(this.mCaptcha.getKey(), URLEncoder.encode(this.mCaptchaAnswer, Constants.UTF8_CHARSET.name()));
            Uri checkUri = UriUtils.changeHttpsToHttp(Uri.parse(url)); // because https returns error 400

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
    protected void onPreExecute() {
        this.mView.beforeCheck();
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
