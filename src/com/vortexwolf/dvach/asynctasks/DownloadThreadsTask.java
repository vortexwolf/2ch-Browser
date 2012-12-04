package com.vortexwolf.dvach.asynctasks;

import com.vortexwolf.dvach.common.Constants;
import com.vortexwolf.dvach.exceptions.JsonApiReaderException;
import com.vortexwolf.dvach.interfaces.ICancelled;
import com.vortexwolf.dvach.interfaces.IJsonApiReader;
import com.vortexwolf.dvach.interfaces.IListView;
import com.vortexwolf.dvach.interfaces.IProgressChangeListener;
import com.vortexwolf.dvach.models.domain.ThreadsList;

import android.app.Activity;
import android.os.AsyncTask;
import android.view.Window;

public class DownloadThreadsTask extends AsyncTask<Void, Long, Boolean> implements IProgressChangeListener, ICancelled {

	static final String TAG = "DownloadThreadsTask";
	
	private final Activity mActivity;
	private final IJsonApiReader mJsonReader;
	private final IListView<ThreadsList> mView;
	private final String mBoard;
	private final int mPageNumber;
	
	private ThreadsList mThreadsList = null;
	private String mUserError = null;
	// Progress bar
	private long mContentLength = 0;
	
	public DownloadThreadsTask(Activity activity, IListView<ThreadsList> view, String board, int pageNumber, IJsonApiReader jsonReader) {
		this.mActivity = activity;
		this.mJsonReader = jsonReader;
		this.mView = view;
		this.mBoard = board != null ? board : Constants.DEFAULT_BOARD;
		this.mPageNumber = pageNumber;
	}
	
	@Override
	protected Boolean doInBackground(Void... params) {
		//Читаем по ссылке json-объект со списком тредов
		try{
			this.mThreadsList = this.mJsonReader.readThreadsList(this.mBoard, this.mPageNumber, this, this);
		}
		catch(Exception e){
			this.mUserError = e.getMessage();
		}

    	return this.mThreadsList != null;
	}	
	
	@Override
	public void onPreExecute() {
		// Отображаем экран загрузки и запускаем прогресс бар
		this.mView.showLoadingScreen();
		
		if (mContentLength == -1){
			this.mView.setWindowProgress(Window.PROGRESS_INDETERMINATE_ON);
		}
		else {
			this.mView.setWindowProgress(0);
		}
	}
	
	@Override
	public void onPostExecute(Boolean success) {
		// Прячем все индикаторы загрузки
		this.onFinished();
		
		// Обновляем список или отображаем ошибку
		if(success && this.mThreadsList != null) {
			this.mView.setData(this.mThreadsList);
		}
		else if(!success) {
			this.mView.showError(this.mUserError);
		}
	}

	private void onFinished() {
		this.mView.hideLoadingScreen();
		
		if (mContentLength == -1) {
			this.mView.setWindowProgress(Window.PROGRESS_INDETERMINATE_OFF);
		}
		//Hide progress anyway
		this.mView.setWindowProgress(10000);
	}

	@Override
	public void onProgressUpdate(Long... progress) {
		// 0-9999 is ok, 10000 means it's finished
		if(mContentLength > 0){
			int relativeProgress = progress[0].intValue() * 9999 / (int) mContentLength;
			this.mView.setWindowProgress(relativeProgress);
		}
	}

	@Override
	public void progressChanged(long newValue) {
		if(this.isCancelled()) return;
		
		this.publishProgress(newValue);
	}
	
	@Override
	public void indeterminateProgress() {
		this.mView.setWindowProgress(Window.PROGRESS_INDETERMINATE_ON);
	}

	@Override
	public void setContentLength(long value) {
		this.mContentLength = value;
	}
}
