package com.vortexwolf.chan.services;

import java.nio.charset.Charset;
import java.util.HashMap;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.client.params.CookiePolicy;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;

import android.content.res.Resources;

import com.vortexwolf.chan.R;
import com.vortexwolf.chan.boards.dvach.DvachSendPostMapper;
import com.vortexwolf.chan.boards.dvach.DvachUriBuilder;
import com.vortexwolf.chan.common.Constants;
import com.vortexwolf.chan.common.library.ExtendedHttpClient;
import com.vortexwolf.chan.common.library.MyLog;
import com.vortexwolf.chan.common.utils.StringUtils;
import com.vortexwolf.chan.exceptions.HttpRequestException;
import com.vortexwolf.chan.exceptions.SendPostException;
import com.vortexwolf.chan.interfaces.IPostSender;
import com.vortexwolf.chan.models.domain.SendPostModel;
import com.vortexwolf.chan.services.http.HttpStringReader;
import com.vortexwolf.chan.settings.ApplicationSettings;

public class PostSender implements IPostSender {
    private static final String TAG = "PostSender";
    private final DefaultHttpClient mHttpClient;
    private final Resources mResources;
    private final HttpStringReader mHttpStringReader;
    private final PostResponseParser mResponseParser;
    private final DvachUriBuilder mDvachUriBuilder;
    private final ApplicationSettings mApplicationSettings;
    private final DvachSendPostMapper mDvachSendPostMapper;

    public PostSender(DefaultHttpClient client, Resources resources, DvachUriBuilder dvachUriBuilder, ApplicationSettings settings, HttpStringReader httpStringReader) {
        this.mHttpClient = client;
        this.mResources = resources;
        this.mResponseParser = new PostResponseParser();
        this.mHttpStringReader = httpStringReader;
        this.mDvachUriBuilder = dvachUriBuilder;
        this.mApplicationSettings = settings;
        this.mDvachSendPostMapper = new DvachSendPostMapper();
    }

    @Override
    public String sendPost(String boardName, SendPostModel entity) throws SendPostException {

        if (boardName == null || entity == null) {
            throw new SendPostException(this.mResources.getString(R.string.error_incorrect_argument));
        }

        String uri = this.mDvachUriBuilder.createBoardUri(boardName, "/wakaba.pl").toString();
        //String uri = "http://posttestserver.com/post.php?dir=vortexwolf";

        // 1 - 'ро' на кириллице, 2 - 'р' на кириллице, 3 - 'о' на кириллице, 4
        // - все латинскими буквами,
        String[] possibleTasks = new String[] { "роst", "рost", "pоst", "post", };
        HashMap<String, String> extraValues = new HashMap<String, String>();
        int statusCode = 502; // Возвращается при неправильном значении
                              // task=post, часто меняется, поэтому неизвестно
                              // какой будет на данный момент
        boolean had301 = false;
        HttpPost httpPost = null;
        HttpResponse response = null;
        try {
            for (int i = 0; i < possibleTasks.length && (statusCode == 502 || statusCode == 301); i++) {
                httpPost = new HttpPost(uri);
                extraValues.put("task", possibleTasks[i]);
                response = this.executeHttpPost(httpPost, entity, extraValues);
                // Проверяем код ответа
                statusCode = response.getStatusLine().getStatusCode();

                if (statusCode == 502) {
                    httpPost.abort();
                }

                // TODO: rewrite this error handling
                if (statusCode == 301 && !had301) {
                    uri = ExtendedHttpClient.getLocationHeader(response);
                    had301 = true;
                    i--;
                }

                MyLog.v(TAG, response.getStatusLine());
            }

            // Вернуть ссылку на тред после успешной отправки и редиректа
            if (statusCode == 302 || statusCode == 303) {
                return ExtendedHttpClient.getLocationHeader(response);
            }

            if (statusCode != 200) {
                throw new SendPostException(statusCode + " - " + response.getStatusLine().getReasonPhrase());
            }

            // Проверяю 200-response на наличие html-разметки с ошибкой
            String responseText = null;
            try {
                responseText = this.mHttpStringReader.fromResponse(response);
            } catch (HttpRequestException e) {
                throw new SendPostException(e.getMessage());
            }

            // Вызываю только для выброса exception
            this.mResponseParser.isPostSuccessful(responseText);

            return null;
        } finally {
            ExtendedHttpClient.releaseRequestResponse(httpPost, response);
        }
    }

    private HttpResponse executeHttpPost(HttpPost httpPost, SendPostModel postModel, HashMap<String, String> extraValues) throws SendPostException {
        // Редирект-коды я обработаю самостоятельно путем парсинга и возврата
        // заголовка Location
        HttpClientParams.setRedirecting(httpPost.getParams(), false);
        HttpResponse response = null;
        try {
            HttpEntity entity = this.mDvachSendPostMapper.mapModelToHttpEntity(postModel, extraValues);
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
            throw new SendPostException(this.mResources.getString(R.string.error_send_post));
        }

        return response;
    }
}
