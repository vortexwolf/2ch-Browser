package com.vortexwolf.dvach.asynctasks;

import com.vortexwolf.dvach.exceptions.JsonApiReaderException;
import com.vortexwolf.dvach.interfaces.IBoardSettingsStorage;
import com.vortexwolf.dvach.interfaces.IPostSendView;
import com.vortexwolf.dvach.interfaces.IPostSender;
import com.vortexwolf.dvach.models.domain.BoardSettings;
import com.vortexwolf.dvach.models.domain.PostEntity;
import com.vortexwolf.dvach.models.domain.PostFields;

import android.os.AsyncTask;

public class SendPostTask extends AsyncTask<Void, Long, Boolean> {

	private final IPostSender mPostSender;
	private final IPostSendView mView;
	private final IBoardSettingsStorage mBoardSettingsStorage;
	
	private final String mBoardName;
	private final String mThreadNumber;
	private final PostEntity mEntity;
	
	private String mRedirectedPage = null;
	private String mUserError;
	
	public SendPostTask(IPostSender postSender, IPostSendView view, IBoardSettingsStorage boardSettingsStorage,
			String boardName, String threadNumber, PostEntity entity) {
		this.mPostSender = postSender;
		this.mView = view;
		this.mBoardSettingsStorage = boardSettingsStorage;
		
		this.mBoardName = boardName;
		this.mThreadNumber = threadNumber;
		this.mEntity = entity;
	}

	@Override
	protected Boolean doInBackground(Void... args) {
		try{
			this.mRedirectedPage = this.mPostSender.sendPost(mBoardName, mThreadNumber, getPostFields(), mEntity);
			return true;
		}
		catch (Exception e){
			this.mUserError = e.getMessage();
			return false;
		}
	}
	
	@Override
	public void onPreExecute() {
		this.mView.showPostLoading();
	}

	@Override
	protected void onPostExecute(final Boolean result) {

		this.mView.hidePostLoading();
		if(result){
			this.mView.showSuccess(this.mRedirectedPage);
		}
		else{
			this.mView.showError(this.mUserError);
		}
	}
	
	private PostFields getPostFields(){
//		try {
//			BoardSettings bs = this.mBoardSettingsStorage.getSettings(this.mBoardName);
//			return bs.getPostFields();
//		} 
//		catch (JsonApiReaderException e) {
//			this.mUserError = e.getMessage();
		
		//// the settings api doesn't work any more
			return PostFields.getDefault();
//		}
	}
}
