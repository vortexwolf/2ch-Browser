package com.vortexwolf.dvach.activities.addpost;

import com.vortexwolf.dvach.api.JsonApiReaderException;
import com.vortexwolf.dvach.api.entities.BoardSettings;
import com.vortexwolf.dvach.api.entities.PostFields;
import com.vortexwolf.dvach.interfaces.IBoardSettingsStorage;
import com.vortexwolf.dvach.interfaces.IPostSendView;
import com.vortexwolf.dvach.interfaces.IPostSender;

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
		try {
			BoardSettings bs = this.mBoardSettingsStorage.getSettings(this.mBoardName);
			return bs.getPostFields();
		} 
		catch (JsonApiReaderException e) {
			this.mUserError = e.getMessage();
			return PostFields.getDefault();
		}
	}
}
