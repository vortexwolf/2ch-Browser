package com.vortexwolf.dvach.activities.addpost;

import java.nio.charset.Charset;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HTTP;

import com.vortexwolf.dvach.api.entities.PostFields;
import com.vortexwolf.dvach.common.Constants;
import com.vortexwolf.dvach.common.Errors;
import com.vortexwolf.dvach.common.library.GzipHttpClientFactory;
import com.vortexwolf.dvach.common.library.MyLog;
import com.vortexwolf.dvach.common.utils.StringUtils;
import com.vortexwolf.dvach.interfaces.IPostSender;
import com.vortexwolf.dvach.presentation.services.HttpStringReader;

public class PostSender implements IPostSender {
	private static final String TAG = "PostSender";
	private final DefaultHttpClient mHttpClient;
	private final Errors mErrors;
	private final HttpStringReader mHttpStringReader;
	private final PostResponseParser mResponseParser;
	
	public PostSender(DefaultHttpClient client, Errors errors){
		//this.mHttpClient = client;
		this.mHttpClient = new GzipHttpClientFactory().createHttpClient(); // похоже, что передаваемые клиент не работает, лучше создать новый
		this.mErrors = errors;
		this.mResponseParser = new PostResponseParser();
		this.mHttpStringReader = new HttpStringReader(this.mHttpClient);
	}
	
	@Override
	public String sendPost(String boardName, String threadNumber, PostFields fields, PostEntity entity) throws SendPostException {
		
		if(boardName == null || threadNumber == null || fields == null || entity == null){
			throw new SendPostException(this.mErrors.getIncorrectArgumentError());
		}

		String uri = "http://2ch.so/"+boardName+"/wakaba.pl";
		
		// 1 - 'ро' на кириллице, 2 - 'о' на кириллице, 3 - все латинскими буквами, 4 - 'р' на кириллице
		String[] possibleTasks = new String[] { "роst", "pоst", "post", "рost" };
		int statusCode = 502; // Возвращается при неправильном значении task=post, часто меняется, поэтому неизвестно какой будет на данный момент
		HttpResponse response = null;
			
		for(int i = 0; i < possibleTasks.length && statusCode == 502; i++){
			response = executeHttpPost(uri, threadNumber, possibleTasks[i], fields, entity);
			//Проверяем код ответа
			statusCode = response.getStatusLine().getStatusCode();
			
			MyLog.v(TAG, response.getStatusLine());
        }
		// Вернуть ссылку на тред после успешной отправки и редиректа
		if(statusCode == 302 || statusCode == 303){
			Header header = response.getFirstHeader("Location");
			if(header != null){
				return header.getValue();
			}
		}
		else if(statusCode != 200) {
	    	throw new SendPostException(statusCode + " - "+response.getStatusLine().getReasonPhrase());
	    }
	    
		//Проверяю 200-response на наличие html-разметки с ошибкой
	    String responseText = this.mHttpStringReader.fromResponse(response);
		// Вызываю только для выброса exception
	    this.mResponseParser.isPostSuccessful(responseText);
	    
	    return null;
	}
	
	private HttpResponse executeHttpPost(String uri, String threadNumber, String task, PostFields fields, PostEntity entity) throws SendPostException{
		HttpPost httpPost = new HttpPost(uri);
		//Редирект-коды я обработаю самостоятельно путем парсинга и возврата заголовка Location
		HttpClientParams.setRedirecting(httpPost.getParams(), false);
		//Настраиваем заголовки
		httpPost.setHeader(HTTP.USER_AGENT, Constants.USER_AGENT_STRING);
		httpPost.setHeader("Accept", "application/json,text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
	    httpPost.setHeader("Accept-Language", "ru,ru-ru;q=0.8,en-us;q=0.5,en;q=0.3");
	    httpPost.setHeader("Accept-Encoding", "gzip, deflate");
	    httpPost.setHeader("Accept-Charset", "ISO-8859-1,utf-8;q=0.7,*;q=0.7");

        HttpResponse response = null;
        try
        {
        	Charset utf = Constants.UTF8_CHARSET;
    	    //Заполняем параметры для отправки сообщения
        	MultipartEntity multipartEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);  
            multipartEntity.addPart("task", new StringBody(task, utf));
            multipartEntity.addPart("parent", new StringBody(threadNumber, utf));
            multipartEntity.addPart(fields.getCaptchaKey(), new StringBody(StringUtils.emptyIfNull(entity.getCaptchaKey()), utf));
            multipartEntity.addPart(fields.getCaptcha(), new StringBody(StringUtils.emptyIfNull(entity.getCaptchaAnswer()), utf));
            multipartEntity.addPart(fields.getComment(), new StringBody(StringUtils.emptyIfNull(entity.getComment()), utf));
            if(entity.isSage()){
            	multipartEntity.addPart(fields.getEmail(), new StringBody(Constants.SAGE_EMAIL, utf));
            }
            if(entity.getAttachment() != null){
            	multipartEntity.addPart(fields.getFile(), new FileBody(entity.getAttachment()));
            }
            if(entity.getSubject() != null){
            	multipartEntity.addPart(fields.getSubject(), new StringBody(entity.getSubject(), utf));
            }

	        httpPost.setEntity(multipartEntity);
	        response = this.mHttpClient.execute(httpPost);  
	    }	        
        catch (Exception e) {
        	MyLog.e(TAG, e);
        	throw new SendPostException(this.mErrors.getSendPostError());
        }
        
        return response;
	}
}
