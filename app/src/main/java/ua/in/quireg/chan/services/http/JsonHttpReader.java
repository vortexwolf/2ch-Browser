package ua.in.quireg.chan.services.http;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.http.HttpEntity;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.ObjectMapper;

import android.content.res.Resources;

import ua.in.quireg.chan.R;
import ua.in.quireg.chan.common.Constants;
import ua.in.quireg.chan.common.library.ExtendedHttpClient;
import ua.in.quireg.chan.common.library.MyLog;
import ua.in.quireg.chan.common.utils.IoUtils;
import ua.in.quireg.chan.exceptions.HtmlNotJsonException;
import ua.in.quireg.chan.exceptions.HttpRequestException;
import ua.in.quireg.chan.exceptions.JsonApiReaderException;
import ua.in.quireg.chan.interfaces.ICancelled;
import ua.in.quireg.chan.interfaces.IJsonProgressChangeListener;

public class JsonHttpReader {
    private static final String TAG = JsonHttpReader.class.getSimpleName();
    private final Resources mResources;
    private final ObjectMapper mObjectMapper;
    private final HttpStreamReader mHttpStreamReader;

    public JsonHttpReader(Resources resources, ObjectMapper mapper, HttpStreamReader httpStreamReader) {
        this.mResources = resources;
        this.mObjectMapper = mapper;
        this.mHttpStreamReader = httpStreamReader;
    }

    public JsonNode readData(String url, boolean checkModified, IJsonProgressChangeListener listener, ICancelled task) throws JsonApiReaderException, HtmlNotJsonException {
        return this.readData(url, checkModified, listener, task, false, null);
    }

    public JsonNode postData(String url, IJsonProgressChangeListener listener, ICancelled task, HttpEntity entity) throws JsonApiReaderException, HtmlNotJsonException {
        return this.readData(url, false, listener, task, true, entity);
    }

    public <T> T convertValue(JsonNode map, Class<T> valueType) {
        try {
            return this.mObjectMapper.convertValue(map, valueType);
        } catch (Exception e) {
            return null;
        }
    }

    private JsonNode readData(String url, boolean checkModified, IJsonProgressChangeListener listener, ICancelled task, boolean isPostRequest, HttpEntity entity) throws JsonApiReaderException, HtmlNotJsonException {
        JsonNode result = null;
        boolean returnSuccess = false;

        try {
            for (int i = 0; i < 2; i++) {
                try {
                    result = this.tryRead(url, checkModified, listener, task, isPostRequest, entity);
                    returnSuccess = true;
                    break;
                } catch (JsonParseException e) {
                    // try to load once again
                    if (listener != null) {
                        listener.indeterminateProgress();
                    }
                    checkModified = false;
                }
            }
        } catch (CancelTaskException e) {
            returnSuccess = true;
        } catch (HtmlNotJsonException e) {
            throw e;
        } catch (Exception e) {
            MyLog.e(TAG, e);
            throw new JsonApiReaderException(e.getMessage());
        }

        if (!returnSuccess) {
            throw new JsonApiReaderException(this.mResources.getString(R.string.error_json_parse));
        }

        return result;
    }

    private JsonNode tryRead(String url, boolean checkModified, IJsonProgressChangeListener listener, ICancelled task, boolean isPostRequest, HttpEntity entity) throws HttpRequestException, CancelTaskException, IOException, HtmlNotJsonException {
        HttpStreamModel streamModel = null;
        try {
            if (isPostRequest) {
                streamModel = this.mHttpStreamReader.fromUri(url, null, entity, listener, task);
            } else {
                streamModel = this.mHttpStreamReader.fromUri(url, checkModified, null, listener, task);
            }

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

            // Quick fix: replaced multiple ",,,," by single ",".
            // Should be fixed on the server side.
            String jsonStr = new String(bytes, 0, bytes.length, Constants.UTF8_CHARSET.name());
            jsonStr = jsonStr.replaceAll(",{2,}", ",");
            bytes = jsonStr.getBytes();

            InputStreamReader streamReader = this.createStreamReaderForParsing(bytes, listener, task);
            JsonNode result = this.mObjectMapper.readValue(streamReader, JsonNode.class);
            return result;
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

    private InputStreamReader createStreamReaderForParsing(byte[] bytes, IJsonProgressChangeListener listener, ICancelled task) throws IllegalStateException, IOException {
        InputStream memoryStream = IoUtils.modifyInputStream(new ByteArrayInputStream(bytes), listener.getContentLength(), listener, task);
        // Maybe I don't need these replacements anymore because if InputStreamReader. I need to check.
        //Map<byte[], byte[]> replacements = new HashMap<byte[],byte[]>();
        //replacements.put(new byte[] {(byte)'\\', (byte)'v'}, new byte[] {(byte)'\\', (byte)'n'});
        //memoryStream = new ReplaceFilterInputStream (memoryStream, replacements);

        InputStreamReader streamReader = new InputStreamReader(memoryStream, Constants.UTF8_CHARSET.name());

        return streamReader;
    }

    private static class CancelTaskException extends Exception {
        private static final long serialVersionUID = -4423318670475901875L;
    }
}
