package com.vortexwolf.dvach.common.library;

/* package com.wilson.android.library;
 * by James A Wilson, stackoverflow
 * http://stackoverflow.com/questions/541966/android-how-do-i-do-a-lazy-load-of-images-in-listview
 */

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;

import com.vortexwolf.dvach.interfaces.IBitmapManager;
import com.vortexwolf.dvach.interfaces.IHttpBitmapReader;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.ImageView;

public class BitmapManager implements IBitmapManager {

	private static final String TAG = "BitmapManager";

	private final Map<String, Bitmap> mCache;
	private final ArrayList<String> mErrorBitmaps;
	private final IHttpBitmapReader mHttpBitmapReader;
	private final ExecutorService mExecutorService; 
	
	public BitmapManager(IHttpBitmapReader httpBitmapReader) {
		this(httpBitmapReader, Executors.newFixedThreadPool(3));
	}
	
	private BitmapManager(IHttpBitmapReader httpBitmapReader, ExecutorService executorService) {
		this.mCache = new HashMap<String, Bitmap>();
		this.mErrorBitmaps = new ArrayList<String>();
		this.mHttpBitmapReader = httpBitmapReader;
		this.mExecutorService= executorService;
	}

	/** Функция для получение кэшированных картинок, чтобы не качать лишний раз */
	private Bitmap tryGetFromCache(String urlString) {
		Bitmap bitmap = mCache.get(urlString);

		return bitmap;
	}
	
	@Override
	public boolean isCached(String urlString) {
		return mCache.containsKey(urlString);
	}

	/** Загружает изображение и ложит его в кэш */
	public Bitmap fetchBitmap(String urlString) {
		// Проверяем кэш, чтобы не загружать из интернета
		Bitmap cachedBitmap = tryGetFromCache(urlString);
		if (cachedBitmap != null){
			return cachedBitmap;
		}

		Bitmap bitmap = this.mHttpBitmapReader.fromUri(urlString);
		if (bitmap != null){
			mCache.put(urlString, bitmap);
		} else {
			this.mErrorBitmaps.add(urlString);
			MyLog.v(TAG, "Bitmap is null");
		}

		return bitmap;
	}

	@Override
	public void fetchBitmapOnThread(final String urlString,
			final ImageView imageView, final View indeterminateProgressBar,
			final Activity act, final int errorImageId) {

		imageView.setTag(urlString);
		if(this.mCache.containsKey(urlString)) {
			imageView.setImageBitmap(this.tryGetFromCache(urlString));
			return;
		}
		else if(this.mErrorBitmaps.contains(urlString)){
			imageView.setImageResource(errorImageId);
			return;
		}
		
		if (indeterminateProgressBar != null) {
			imageView.setVisibility(View.GONE);
			indeterminateProgressBar.setVisibility(View.VISIBLE);
		}

		final Handler handler = new Handler() {
			@Override
			public void handleMessage(final Message message) {

				if(imageView.getTag() != urlString){
					MyLog.v(TAG, "Attempted to rewrite the image with other url");
					return;
				}
				
				if (indeterminateProgressBar != null) {
					indeterminateProgressBar.setVisibility(View.GONE);
					imageView.setVisibility(View.VISIBLE);
				}

				Bitmap b = (Bitmap) message.obj;
				if(b != null){
					imageView.setImageBitmap(b);
				}
				else{
					imageView.setImageResource(errorImageId);
				}
			}
		};


		
		Thread thread = new Thread() {
			@Override
			public void run() {
				
				Bitmap bitmap = fetchBitmap(urlString);
				Message message = handler.obtainMessage(1, bitmap);
				handler.sendMessage(message);
			}
		};
		
		//thread.start();

		this.mExecutorService.submit(thread);
	}

	@Override
	public void clearCache() {
		
		for(Bitmap b : mCache.values()){
			b.recycle();
		}
		
		mCache.clear();
	}
}
