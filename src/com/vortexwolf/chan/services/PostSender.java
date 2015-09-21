package com.vortexwolf.chan.services;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.DefaultHttpClient;

import android.content.res.Resources;
import android.net.Uri;

import com.vortexwolf.chan.R;
import com.vortexwolf.chan.boards.makaba.MakabaSendPostMapper;
import com.vortexwolf.chan.common.Constants;
import com.vortexwolf.chan.common.Websites;
import com.vortexwolf.chan.common.library.ExtendedHttpClient;
import com.vortexwolf.chan.common.library.MyLog;
import com.vortexwolf.chan.common.utils.UriUtils;
import com.vortexwolf.chan.interfaces.IUrlBuilder;
import com.vortexwolf.chan.interfaces.IWebsite;
import com.vortexwolf.chan.models.domain.SendPostModel;
import com.vortexwolf.chan.models.domain.SendPostResult;
import com.vortexwolf.chan.services.http.HttpStringReader;
import com.vortexwolf.chan.settings.ApplicationSettings;

import java.util.List;

public class PostSender {
    private static final String TAG = "PostSender";
    private final DefaultHttpClient mHttpClient;
    private final Resources mResources;
    private final HttpStringReader mHttpStringReader;
    private final PostResponseParser mResponseParser;
    private final ApplicationSettings mApplicationSettings;
    private final MakabaSendPostMapper mMakabaSendPostMapper;

    public PostSender(DefaultHttpClient client, Resources resources, ApplicationSettings settings, HttpStringReader httpStringReader) {
        this.mHttpClient = client;
        this.mResources = resources;
        this.mResponseParser = new PostResponseParser();
        this.mHttpStringReader = httpStringReader;
        this.mApplicationSettings = settings;
        this.mMakabaSendPostMapper = new MakabaSendPostMapper();
    }

    public SendPostResult sendPost(IWebsite website, String boardName, SendPostModel entity) {
        SendPostResult result = new SendPostResult();

        if (boardName == null || entity == null) {
            result.error = this.mResources.getString(R.string.error_incorrect_argument);
            return result;
        }

        IUrlBuilder urlBuilder = website.getUrlBuilder();
        String referer = UriUtils.getBoardOrThreadUrl(urlBuilder, boardName, 0, entity.getParentThread());
        String uri = urlBuilder.getPostingUrlApi();
        //String uri = "http://posttestserver.com/post.php?dir=vortexwolf";

        HttpPost httpPost = null;
        HttpResponse response = null;
        try {
            httpPost = new HttpPost(uri);
            response = this.executeHttpPost(boardName, httpPost, entity, referer);
            if (response == null) {
                throw new Exception(this.mResources.getString(R.string.error_send_post));
            }

            List<Cookie> cookies = this.mHttpClient.getCookieStore().getCookies();
            for (Cookie c : cookies) {
                if (c.getName().equals(Constants.ADULT_ACCESS_COOKIE) &&
                    UriUtils.areCookieDomainsEqual(c.getDomain(), Uri.parse(uri).getHost())) {
                    this.mApplicationSettings.saveAdultAccessCookie(c);
                    break;
                }
            }

            int statusCode = response.getStatusLine().getStatusCode();

            if (statusCode == 403) {
                String html = this.mHttpStringReader.fromResponse(response);
                if (RecaptchaService.isCloudflareCaptchaPage(html)) {
                    result.isRecaptcha = true;
                    result.error = this.mResources.getString(R.string.notification_cloudflare_recaptcha);
                    return result;
                }
            }

            // Вернуть ссылку на тред после успешной отправки и редиректа
            if (statusCode == 302 || statusCode == 303) {
                result.isSuccess = true;
                result.location = ExtendedHttpClient.getLocationHeader(response);
                return result;
            }

            if (statusCode != 200) {
                throw new Exception(statusCode + " - " + response.getStatusLine().getReasonPhrase());
            }

            if (statusCode == 200) {
                // check json response for errors
                String responseText = this.mHttpStringReader.fromResponse(response);
                result = this.mResponseParser.isPostSuccessful(boardName, responseText);
            }
        } catch (Exception e) {
            result.error = e.getMessage();
        } finally {
            ExtendedHttpClient.releaseRequestResponse(httpPost, response);
        }

        return result;
    }

    private HttpResponse executeHttpPost(String boardName, HttpPost httpPost, SendPostModel postModel, String referer) {
        // Редирект-коды я обработаю самостоятельно путем парсинга и возврата
        // заголовка Location
        HttpClientParams.setRedirecting(httpPost.getParams(), false);
        HttpResponse response = null;
        try {
            httpPost.setHeader("Referer", referer);
            httpPost.setHeader("Content-Type", "multipart/form-data; boundary=" + Constants.MULTIPART_BOUNDARY);

            String usercode = this.mApplicationSettings.getPasscodeCookieValue();

            HttpEntity entity = this.mMakabaSendPostMapper.mapModelToHttpEntity(boardName, usercode, postModel);
            httpPost.setEntity(entity);

            // post and ignore recvfrom exceptions
            for (int i = 0; i < 3; i++) {
                try {
                    response = this.mHttpClient.execute(httpPost);

                    break;
                } catch (Exception e) {
                    if ("recvfrom failed: ECONNRESET (Connection reset by peer)".equals(e.getMessage())) {
                        // a stupid error, I have no idea how to solve it so I just try again
                        ExtendedHttpClient.releaseResponse(response);
                        continue;
                    } else {
                        throw e;
                    }
                }
            }
        } catch (Exception e) {
            MyLog.e(TAG, e);
            return null;
        }

        return response;
    }
}
