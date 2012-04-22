package com.vortexwolf.dvach.api;

import java.io.IOException;
import java.io.InputStream;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.ObjectMapper;

import android.app.Activity;
import android.view.Window;

import com.vortexwolf.dvach.api.entities.BoardSettings;
import com.vortexwolf.dvach.api.entities.CaptchaEntity;
import com.vortexwolf.dvach.api.entities.PostsList;
import com.vortexwolf.dvach.api.entities.ThreadsList;
import com.vortexwolf.dvach.common.Constants;
import com.vortexwolf.dvach.common.Errors;
import com.vortexwolf.dvach.common.library.CancellableInputStream;
import com.vortexwolf.dvach.common.library.MyLog;
import com.vortexwolf.dvach.common.library.ProgressInputStream;
import com.vortexwolf.dvach.interfaces.ICancellable;
import com.vortexwolf.dvach.interfaces.IJsonApiReader;
import com.vortexwolf.dvach.interfaces.IProgressChangeListener;

public class JsonApiReader implements IJsonApiReader{

	static final String TAG = "JsonApiReader";
	private final DefaultHttpClient mHttpClient;
	private final Errors mErrors;
	private final ObjectMapper mObjectMapper;

	public JsonApiReader(DefaultHttpClient client, Errors errors, ObjectMapper mapper)
	{
		this.mHttpClient = client;
		this.mErrors = errors;
		this.mObjectMapper = mapper;
	}
	
	private static String formatApiUri(String boardName, String code){
		return "http://2ch.so/"+boardName+"/wakaba.pl?task=api&code="+code; //85.17.28.149
	}
	
	private static String formatThreadsUri(String boardName, int page){
		String pageName = page == 0 ? "wakaba" : String.valueOf(page);
		
		return "http://2ch.so/" + boardName + "/" + pageName + ".json";
	}
	
	private static String formatPostsUri(String boardName, String threadId){
		return "http://2ch.so/"+boardName+"/res/"+threadId+".json";
	}
	
	@Override
	public BoardSettings readBoardSettings(String boardName) throws JsonApiReaderException{
		String uri = formatApiUri(boardName, Constants.JSON_SETTINGS_TYPE);
		return this.readData(uri, BoardSettings.class);
	}
	
	@Override
	public CaptchaEntity readCaptcha(String boardName, ICancellable task) throws JsonApiReaderException{
		String uri = formatApiUri(boardName, Constants.JSON_CAPTCHA_TYPE);
		return this.readData(uri, CaptchaEntity.class, null, task);
	}
	
	@Override
	public ThreadsList readThreadsList(String boardName, int page, IProgressChangeListener listener, ICancellable task, Activity activity) throws JsonApiReaderException{
		String uri = formatThreadsUri(boardName, page);
		return this.readData(uri, ThreadsList.class, listener, task, activity);
	}

	@Override
	public PostsList readPostsList(String boardName, String threadNumber, String from, IProgressChangeListener listener, ICancellable task) throws JsonApiReaderException{
		String uri = formatPostsUri(boardName, threadNumber);
		return this.readData(uri, PostsList.class, listener, task);
	}
	
	public <T> T readData(String url, Class<T> valueType) throws JsonApiReaderException{
		return readData(url, valueType, null, null);
	}
	
	public <T> T readData(String url, Class<T> valueType, IProgressChangeListener listener, ICancellable task) throws JsonApiReaderException{
		return readData(url, valueType, listener, task, null);
	}
	
	public <T> T readData(String url, Class<T> valueType, IProgressChangeListener listener, ICancellable task, Activity activity) throws JsonApiReaderException{
		return readData(url, valueType, listener, task, activity, 0);
	}
	
	private <T> T readData(String url, Class<T> valueType, IProgressChangeListener listener, ICancellable task, Activity activity, int recLevel) throws JsonApiReaderException {
//		//Создаем get-request
		HttpGet request = null;
		try {
			request = new HttpGet(url);
			request.setHeader("Accept", "application/json");
		} 
		catch (IllegalArgumentException e) {
			MyLog.e(TAG, e);
			finishRead(request, null, new JsonApiReaderException(this.mErrors.getIncorrectArgumentError(), e));
		}
		
		if(checkCancelled(task, request, null)) return null;
		
		//Получаем response
		HttpResponse response = null;

		try {
			response = mHttpClient.execute(request);
		} catch (Exception e) {
			MyLog.e(TAG, e);
			finishRead(request, response, new JsonApiReaderException(this.mErrors.getDownloadDataError()));
		}

		StatusLine status = response.getStatusLine();
		MyLog.v(TAG, status);
		if(status.getStatusCode() != 200) {
			finishRead(request, response, new JsonApiReaderException(status.getStatusCode() + " - " + status.getReasonPhrase()));
		}
		
		if(checkCancelled(task, request, response)) return null;

		InputStream json = null;
		try{
			json = getInputStream(response, listener, task);
		}
		catch (Exception e) {
			MyLog.e(TAG, e);
			finishRead(request, response, new JsonApiReaderException(this.mErrors.getDownloadDataError()));
		}

		//Парсим результат
		T result = null;
		try {
			result = this.mObjectMapper.readValue(json, valueType);
		} 
		catch (JsonParseException e){
			MyLog.e(TAG, e);
			if(recLevel < 2){
				MyLog.v(TAG, "Read json once again");
				if(activity != null){
					activity.getWindow().setFeatureInt(Window.FEATURE_PROGRESS, Window.PROGRESS_INDETERMINATE_ON);
				}
				return readData(url, valueType, null, task, null, recLevel + 1);
			}
			else {
				finishRead(request, response, new JsonApiReaderException(this.mErrors.getJsonParseError()));
			}
		}
		catch (Exception e) {
			// Если не удалось преобразовать, значит неверный json-объект
			MyLog.e(TAG, e);
			finishRead(request, response, new JsonApiReaderException(this.mErrors.getJsonParseError()));
		}

		finishRead(request, response);

		return result;
	}
	
	private boolean checkCancelled(ICancellable task, HttpGet request, HttpResponse response){
		if(task != null && task.isCancelled()){
			finishRead(request, response);
			MyLog.v(TAG, "task was cancelled");
			
	        return true;
		}

		return false;
	}
	
	/** Освобождает все ресурсы http-запроса и выбрасывает исключения */
	private void finishRead(HttpGet request, HttpResponse response, JsonApiReaderException exception) throws JsonApiReaderException{
		finishRead(request, response);
		
		if(exception != null){
			throw exception;
		}
	}
	
	/** Освобождает все ресурсы http-запроса */
	private void finishRead(HttpGet request, HttpResponse response){
		// Очищяем response перед request, чтобы не было SocketException
		if(response != null){
			HttpEntity entity = response.getEntity();
	        if (entity != null){
	        	try{ 
	        		entity.consumeContent();
	        	}
	        	catch (Exception e){ 
	        		MyLog.e(TAG, e); 
	        	}
	        }
		}
		if(request != null){
			request.abort();
		}
	}
		
	private InputStream getInputStream(HttpResponse response, IProgressChangeListener listener, ICancellable task) throws IllegalStateException, IOException{
		
		HttpEntity entity = response.getEntity();
		InputStream resultStream = entity.getContent();
		
		if (listener != null){
			long contentLength = -1;
			// Раньше и этот метод, и entity.getContentLength() возвращали -1. Сейчас вроде все норм.
			Header contentLengthHeader = response.getFirstHeader("Content-Length");
			if (contentLengthHeader != null){
				contentLength = Long.valueOf(contentLengthHeader.getValue());
			}
			else {
				contentLength = entity.getContentLength();
			}
			listener.setContentLength(contentLength);

			ProgressInputStream pin = new ProgressInputStream(resultStream, contentLength);
			pin.addProgressChangeListener(listener);
			resultStream = pin;
		}
		
		if(task != null) {
			resultStream = new CancellableInputStream(resultStream, task);
		}
		
		return resultStream;
	}
}
