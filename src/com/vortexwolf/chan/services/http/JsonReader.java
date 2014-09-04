package com.vortexwolf.chan.services.http;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.client.params.CookiePolicy;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.ObjectMapper;

import android.content.res.Resources;

import com.vortexwolf.chan.R;
import com.vortexwolf.chan.boards.dvach.DvachUriBuilder;
import com.vortexwolf.chan.boards.dvach.models.DvachFoundPostsList;
import com.vortexwolf.chan.boards.dvach.models.DvachPostsList;
import com.vortexwolf.chan.boards.dvach.models.DvachThreadsList;
import com.vortexwolf.chan.common.Constants;
import com.vortexwolf.chan.common.library.ExtendedHttpClient;
import com.vortexwolf.chan.common.library.MyLog;
import com.vortexwolf.chan.common.utils.IoUtils;
import com.vortexwolf.chan.common.utils.StringUtils;
import com.vortexwolf.chan.exceptions.HtmlNotJsonException;
import com.vortexwolf.chan.exceptions.HttpRequestException;
import com.vortexwolf.chan.exceptions.JsonApiReaderException;
import com.vortexwolf.chan.interfaces.ICancelled;
import com.vortexwolf.chan.interfaces.IJsonApiReader;
import com.vortexwolf.chan.interfaces.IJsonProgressChangeListener;
import com.vortexwolf.chan.settings.ApplicationSettings;

public class JsonReader {
    static final String TAG = "JsonReader";
    private final Resources mResources;
    private final ObjectMapper mObjectMapper;
    private final HttpStreamReader mHttpStreamReader;

    public JsonReader(Resources resources, ObjectMapper mapper, HttpStreamReader httpStreamReader) {
        this.mResources = resources;
        this.mObjectMapper = mapper;
        this.mHttpStreamReader = httpStreamReader;
    }

    public <T> T readData(String url, Class<T> valueType, IJsonProgressChangeListener listener, ICancelled task) throws JsonApiReaderException, HtmlNotJsonException {
        return this.readData(url, valueType, listener, task, false, null);
    }
    
    public <T> T readData(String url, Class<T> valueType, IJsonProgressChangeListener listener, ICancelled task, boolean isPostRequest, HttpEntity entity) throws JsonApiReaderException, HtmlNotJsonException {
        T result = null;
        boolean parseSuccess = false;
        boolean wasCancelled = false;

        for (int i = 0; i < 2; i++) {
            try {
                result = this.tryReadAndParse(url, valueType, listener, task, isPostRequest, entity);
                parseSuccess = true;
                break;
            } catch (HttpRequestException e) {
                throw new JsonApiReaderException(e.getMessage());
            } catch (HtmlNotJsonException e) {
                throw e;
            } catch (CancelTaskException e) {
                wasCancelled = true;
                break;
            } catch (JsonParseException e) {
                // try to load once again
                continue;
            } catch (Exception e) {
                MyLog.e(TAG, e);
                break;
            }
        }

        if (task != null && task.isCancelled()) {
            wasCancelled = true;
        }

        if (!parseSuccess && !wasCancelled) {
            throw new JsonApiReaderException(this.mResources.getString(R.string.error_json_parse));
        }

        return result;
    }

    private <T> T tryReadAndParse(String url, Class<T> valueType, IJsonProgressChangeListener listener, ICancelled task, boolean isPostRequest, HttpEntity entity) throws HttpRequestException, CancelTaskException, IOException, HtmlNotJsonException {
        HttpStreamModel streamModel = null;
        try {
            if (isPostRequest) streamModel = this.mHttpStreamReader.fromUri(url, null, entity, listener, task);
            else streamModel = this.mHttpStreamReader.fromUri(url, null, listener, task);

            if (streamModel.notModifiedResult || streamModel.stream == null) {
                throw new CancelTaskException();
            }

            byte[] bytes = this.readBytes(streamModel.stream, listener);

            if (task != null && task.isCancelled()) {
                throw new CancelTaskException();
            }

            if (bytes.length > 20) {
                String firstLetters = new String(bytes, 0, 20, Constants.UTF8_CHARSET.name());
                // check if it is an html page instead of a json page
                if (firstLetters != null && firstLetters.toLowerCase().startsWith("<!doctype")) {
                    String html = new String(bytes, 0, bytes.length, Constants.UTF8_CHARSET.name());
                    throw new HtmlNotJsonException(html, this.mResources.getString(R.string.error_not_json));
                }
            }

            InputStream memoryStream = this.createStreamForParsing(bytes, listener, task);

            try {
                T result = this.mObjectMapper.readValue(memoryStream, valueType);
                return result;
            } catch (JsonParseException e) {
                if (task != null && task.isCancelled()) {
                    throw new CancelTaskException();
                }

                MyLog.e(TAG, e);
                MyLog.v(TAG, "Read json once again");

                this.mHttpStreamReader.removeIfModifiedForUri(url);

                if (listener != null) {
                    listener.indeterminateProgress();
                }

                throw e;
            }
        } finally {
            if (streamModel != null) {
                ExtendedHttpClient.releaseRequestResponse(streamModel.request, streamModel.response);
            }
        }
    }

    private byte[] readBytes(InputStream stream, IJsonProgressChangeListener listener) throws IOException {
        double scale = 1;
        long oldContentLength = listener.getContentLength();
        long newContentLength = oldContentLength + (long) (oldContentLength * scale);

        // increase the progress bar length, so that the reading stopped at 50%
        listener.setContentLength(newContentLength);

        byte[] bytes = IoUtils.convertStreamToBytes(stream);

        listener.setOffsetAndScale(oldContentLength, scale);

        return bytes;
    }

    private InputStream createStreamForParsing(byte[] bytes, IJsonProgressChangeListener listener, ICancelled task) throws IllegalStateException, IOException {
        InputStream memoryStream = IoUtils.modifyInputStream(new ByteArrayInputStream(bytes), listener.getContentLength(), listener, task);

        return memoryStream;
    }

    private static class CancelTaskException extends Exception {
        private static final long serialVersionUID = -4423318670475901875L;
    }
}
