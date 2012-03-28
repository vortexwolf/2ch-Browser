package com.vortexwolf.dvach.activities.addpost;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

import org.apache.http.impl.client.DefaultHttpClient;

import com.google.android.apps.analytics.GoogleAnalyticsTracker;
import com.vortexwolf.dvach.R;
import com.vortexwolf.dvach.activities.boards.PickBoardActivity;
import com.vortexwolf.dvach.activities.browser.BrowserLauncher;
import com.vortexwolf.dvach.activities.files.FilesListActivity;
import com.vortexwolf.dvach.activities.files.SerializableFileModel;
import com.vortexwolf.dvach.activities.threads.DownloadThreadsTask;
import com.vortexwolf.dvach.api.HtmlCaptchaChecker;
import com.vortexwolf.dvach.api.entities.CaptchaEntity;
import com.vortexwolf.dvach.common.*;
import com.vortexwolf.dvach.common.http.HttpBitmapReader;
import com.vortexwolf.dvach.common.http.HttpStringReader;
import com.vortexwolf.dvach.common.library.MyLog;
import com.vortexwolf.dvach.common.library.Tracker;
import com.vortexwolf.dvach.common.utils.AppearanceUtils;
import com.vortexwolf.dvach.common.utils.StringUtils;
import com.vortexwolf.dvach.common.utils.UriUtils;
import com.vortexwolf.dvach.interfaces.IBoardSettingsStorage;
import com.vortexwolf.dvach.interfaces.ICaptchaView;
import com.vortexwolf.dvach.interfaces.IDraftPostsStorage;
import com.vortexwolf.dvach.interfaces.IHtmlCaptchaChecker;
import com.vortexwolf.dvach.interfaces.IHttpBitmapReader;
import com.vortexwolf.dvach.interfaces.IHttpStringReader;
import com.vortexwolf.dvach.interfaces.IJsonApiReader;
import com.vortexwolf.dvach.interfaces.IPostSendView;
import com.vortexwolf.dvach.interfaces.IPostSender;
import com.vortexwolf.dvach.presentation.models.CaptchaViewType;
import com.vortexwolf.dvach.presentation.models.DraftPostModel;
import com.vortexwolf.dvach.presentation.models.ImageFileModel;
import com.vortexwolf.dvach.settings.ApplicationPreferencesActivity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.MediaStore.Images;
import android.text.Editable;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class AddPostActivity extends Activity implements IPostSendView, ICaptchaView {
	public static final String TAG = "AddPostActivity";
	
	private MainApplication mApplication;
	private IJsonApiReader mJsonReader;
	private IPostSender mPostSender;
	private IBoardSettingsStorage mBoardSettingsStorage;
	private IHtmlCaptchaChecker mHtmlCaptchaChecker;
	private IHttpBitmapReader mHttpBitmapReader;
	private IDraftPostsStorage mDraftPostsStorage;
	private Tracker mTracker;
	
	private ImageFileModel mAttachedFile;
	private CaptchaEntity mCaptcha;
	private String mBoardName;
	private String mThreadNumber;
	private CaptchaViewType mCurrentCaptchaView = null;
	private Bitmap mCaptchaBitmap;
	
	private SendPostTask mCurrentPostSendTask = null;
	private DownloadCaptchaTask mCurrentDownloadCaptchaTask = null;
		
	private View mCaptchaSkipView = null;
	private View mCaptchaLoadingView = null;
	private ImageView mCaptchaImageView = null;
	private EditText mCaptchaAnswerView = null;
	private CheckBox mSageCheckBox;
	private EditText mCommentView;
	private View mAttachmentView;
	private ProgressDialog mProgressDialog;
	private Button mSendButton;
	
	// Определяет, нужно ли сохранять пост (если не отправлен) или можно удалить (после успешной отправки)
	private boolean mFinishedSuccessfully = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		this.mApplication = (MainApplication)getApplication();
		this.mJsonReader = this.mApplication.getJsonApiReader();
		this.mPostSender = this.mApplication.getPostSender();
		this.mBoardSettingsStorage = this.mApplication.getBoardSettingsStorage();
		DefaultHttpClient httpClient = this.mApplication.getHttpClient();
		this.mHtmlCaptchaChecker = new HtmlCaptchaChecker(new HttpStringReader(httpClient));
		this.mHttpBitmapReader = new HttpBitmapReader(httpClient);
		this.mDraftPostsStorage = this.mApplication.getDraftPostsStorage();
		this.mTracker = this.mApplication.getTracker();

		this.resetUI();

		//Парсим название борды и номер треда
        Bundle extras = getIntent().getExtras();
		
    	if (extras != null) {
    		this.mBoardName = extras.getString(Constants.EXTRA_BOARD_NAME);
    		this.mThreadNumber = extras.getString(Constants.EXTRA_THREAD_NUMBER);
    	}

    	StringBuilder commentBuilder = new StringBuilder();
    	 
    	// Восстанавливаем состояние, если было сохранено
		DraftPostModel draft = this.mDraftPostsStorage.getDraft(this.mBoardName, this.mThreadNumber);
		if(draft != null){
    		if(!StringUtils.isEmpty(draft.getComment())){
    			commentBuilder.append(draft.getComment() + "\n");
    		}
    		
    		if(draft.getAttachedFile() != null){
    			this.setAttachment(draft.getAttachedFile());
    		}
    		
    		this.mSageCheckBox.setChecked(draft.isSage());

    		if(draft.getCaptchaType() == CaptchaViewType.SKIP){
    			this.skipCaptcha();
    		}
    		else if(draft.getCaptchaType() == CaptchaViewType.IMAGE){
    			this.showCaptcha(draft.getCaptcha(), draft.getCaptchaImage());
    		}
		}

		if (extras != null) {
    		String postNumber = extras.getString(Constants.EXTRA_POST_NUMBER);
    		String postComment = extras.getString(Constants.EXTRA_POST_COMMENT);
			if(postNumber != null){
				commentBuilder.append(">>"+postNumber+"\n");
			}
			
			if(postComment != null){
				postComment = postComment.replace("\n", "\n>");
				commentBuilder.append(">"+postComment+"\n");
			}
		}
		
		//Сохраняем коммент
		this.mCommentView.setText(commentBuilder.toString());
		//Ставим курсор в конце текстового поля
		this.mCommentView.setSelection(commentBuilder.length());
    	
		//Загружаем и показываем капчу
		if(this.mCurrentCaptchaView == null){
    		this.refreshCaptcha();
		}
		
		if(Constants.ADD_THREAD_PARENT.equals(this.mThreadNumber)){
			this.setTitle(String.format(getString(R.string.data_add_thread_title), mBoardName));
		}
		else{
			this.setTitle(String.format(getString(R.string.data_add_post_title), mBoardName, mThreadNumber));
		}
		
        this.mTracker.setBoardVar(mBoardName);
        this.mTracker.trackActivityView(TAG);
	}
	
	
	@Override
	protected void onPause() {
		MyLog.v(TAG, "save state");
		if(!this.mFinishedSuccessfully){
			DraftPostModel draft = new DraftPostModel(this.mCommentView.getText().toString(), this.mAttachedFile, this.mSageCheckBox.isChecked(),
					this.mCurrentCaptchaView, this.mCaptcha, this.mCaptchaBitmap);
			
			this.mDraftPostsStorage.saveDraft(this.mBoardName, this.mThreadNumber, draft);
		}
		else{
			this.mDraftPostsStorage.clearDraft(this.mBoardName, this.mThreadNumber);
		}
		
		super.onPause();
	}

	private void resetUI()
    {
    	this.setTheme(this.mApplication.getSettings().getTheme());
    	this.setContentView(R.layout.add_post_view);
   	
    	this.mCaptchaImageView = (ImageView) findViewById(R.id.addpost_captcha_image);
		this.mCaptchaLoadingView = findViewById(R.id.addpost_captcha_loading);
		this.mCaptchaSkipView = findViewById(R.id.addpost_captcha_skip_text);
		this.mCaptchaAnswerView = (EditText)this.findViewById(R.id.addpost_captcha_input);
		this.mCommentView = (EditText)findViewById(R.id.addpost_comment_input);
		this.mSageCheckBox = (CheckBox) this.findViewById(R.id.addpost_sage_checkbox);
		this.mAttachmentView = findViewById(R.id.addpost_attachment_view);
		this.mSendButton = (Button)this.findViewById(R.id.addpost_send_button);
		final Button removeAttachmentButton = (Button)this.findViewById(R.id.addpost_attachment_remove);
		final Button refreshCaptchaButton = (Button)this.findViewById(R.id.addpost_refresh_button);
		final LinearLayout textFormatView = (LinearLayout)this.findViewById(R.id.addpost_textformat_view);
		
		View.OnClickListener formatButtonListener = new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				switch(v.getId()){
					case R.id.addpost_textformat_b:
						formatSelectedText("b");
						break;
					case R.id.addpost_textformat_c:
						formatSelectedText("code");
						break;
					case R.id.addpost_textformat_i:
						formatSelectedText("i");
						break;
					case R.id.addpost_textformat_s:
						formatSelectedText("s");
						break;
					case R.id.addpost_textformat_spoiler:
						formatSelectedText("spoiler");
						break;
					case R.id.addpost_textformat_u:
						formatSelectedText("u");
						break;
				}
			}
		};

		for(int i = 0; i < textFormatView.getChildCount(); i++){
			ImageButton b = (ImageButton)textFormatView.getChildAt(i);
			if(b != null){
				b.setOnClickListener(formatButtonListener);
			}
		}

    	//Обрабатываем нажатие на кнопку "Отправить"
		mSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
			public void onClick(View v) {
            	AddPostActivity.this.onSend();
            }
        });
    	//Удаляем прикрепленный файл
    	removeAttachmentButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				AddPostActivity.this.removeAttachment();
			}
		});
    	//Обновляем нажатие кнопки Refresh для капчи
		refreshCaptchaButton.setOnClickListener(new View.OnClickListener() {
            @Override
			public void onClick(View v) {
            	AddPostActivity.this.refreshCaptcha();
            }
        });
    }
	
	private void onSend(){
		//Собираем все заполненные поля
		String captchaAnswer = this.mCaptchaAnswerView.getText().toString();
		if(this.mCurrentCaptchaView == CaptchaViewType.LOADING || (this.mCurrentCaptchaView == CaptchaViewType.IMAGE && StringUtils.isEmpty(captchaAnswer))){
			AppearanceUtils.showToastMessage(this, getString(R.string.warning_enter_captcha));
			return;
		}

		String comment = this.mCommentView.getText().toString();
		if(StringUtils.isEmpty(comment)){
			AppearanceUtils.showToastMessage(this, getString(R.string.warning_write_comment));
			return;
		}

		boolean isSage = this.mSageCheckBox.isChecked();
		
		File attachment = this.mAttachedFile != null ? this.mAttachedFile.file : null;
		if(this.mThreadNumber.equals(Constants.ADD_THREAD_PARENT) && attachment == null){
			AppearanceUtils.showToastMessage(this, getString(R.string.warning_attach_file_new_thread));
			return;
		}
		
		//Отправляем
		sendPost(captchaAnswer, comment, isSage, attachment);
	}

	private void sendPost(String captchaAnswer, String comment, boolean isSage, File attachment){

		if(this.mCurrentPostSendTask != null){
			this.mCurrentPostSendTask.cancel(true);
		}
		
		String captchaKey = mCaptcha != null ? mCaptcha.getKey() : null;
		PostEntity pe = new PostEntity(captchaKey, captchaAnswer, comment, isSage, attachment);
		this.mCurrentPostSendTask = new SendPostTask(this.mPostSender, this, this.mBoardSettingsStorage, mBoardName, mThreadNumber, pe);
		this.mCurrentPostSendTask.execute();
	}

	@Override
	public void showSuccess(String redirectedPage) {
		AppearanceUtils.showToastMessage(this, getString(R.string.notification_send_post_success));
		// return back to the list of posts
		String redirectedThreadNumber = null;
		if(redirectedPage != null){
			redirectedThreadNumber = UriUtils.getPageName(Uri.parse(redirectedPage));
		}

		this.mFinishedSuccessfully = true;

		// Отправляем статистику об отправленном сообщении
		if(Constants.ADD_THREAD_PARENT.equals(this.mThreadNumber)){
			this.mTracker.trackEvent(Tracker.CATEGORY_SEND, Tracker.ACTION_NEW_THREAD, this.mCommentView.getText().length());
		}
		else{
			String label = this.mSageCheckBox.isChecked() ? Constants.SAGE_EMAIL : "";
			this.mTracker.trackEvent(Tracker.CATEGORY_SEND, Tracker.ACTION_NEW_POST, label, this.mCommentView.getText().length());
		}
		
		if(this.mAttachedFile != null){
			this.mTracker.trackEvent(Tracker.CATEGORY_SEND, Tracker.ACTION_ATTACH_FILE, this.mAttachedFile.file.getParentFile().getAbsolutePath(), (int)this.mAttachedFile.file.length());
		}
		
		// Завершаем с успешным результатом
		Intent intent = new Intent();
		intent.putExtra(Constants.EXTRA_REDIRECTED_THREAD_NUMBER, redirectedThreadNumber);
		this.setResult(RESULT_OK, intent);
		finish();
	}

	@Override
	public void showError(String error) {
		AppearanceUtils.showToastMessage(this, error != null ? error : getString(R.string.error_send_post));
	}
	
	@Override
	public void showPostLoading() {
		mSendButton.setEnabled(false);
		mProgressDialog = new ProgressDialog(this);
		mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		mProgressDialog.setMessage(getString(R.string.loading));
		mProgressDialog.setCancelable(false);
		mProgressDialog.show();
	}

	@Override
	public void hidePostLoading() {
		mSendButton.setEnabled(true);
		mProgressDialog.dismiss();
		mCurrentPostSendTask = null;
	}

	@Override
	public void showCaptchaLoading() {
		switchToCaptchaView(CaptchaViewType.LOADING);
	}

	@Override
	public void skipCaptcha(){
		switchToCaptchaView(CaptchaViewType.SKIP);
	}
	
	@Override
	public void showCaptcha(CaptchaEntity captcha, Bitmap captchaImage) {
		this.mCaptchaImageView.setImageResource(android.R.color.transparent);
		if(this.mCaptchaBitmap != null){
			this.mCaptchaBitmap.recycle();
		}
		
		this.mCaptcha = captcha;
		this.mCaptchaBitmap = captchaImage;
		this.mCaptchaImageView.setImageBitmap(captchaImage);
		
		switchToCaptchaView(CaptchaViewType.IMAGE);
	}

	@Override
	public void showCaptchaError(String errorMessage) {
		this.mCaptchaImageView.setImageResource(android.R.color.transparent);
		AppearanceUtils.showToastMessage(this, errorMessage);
		
		switchToCaptchaView(CaptchaViewType.ERROR);
	}
	
	private void switchToCaptchaView(CaptchaViewType vt){
		this.mCurrentCaptchaView = vt;
		switch(vt){
			case LOADING:
				mCaptchaImageView.setVisibility(View.GONE);
				mCaptchaImageView.setImageResource(android.R.color.transparent);
				mCaptchaLoadingView.setVisibility(View.VISIBLE);
				mCaptchaSkipView.setVisibility(View.GONE);
				break;
			case ERROR:
			case IMAGE:
				this.mCurrentDownloadCaptchaTask = null;
				mCaptchaAnswerView.setVisibility(View.VISIBLE);
				mCaptchaImageView.setVisibility(View.VISIBLE);
				mCaptchaLoadingView.setVisibility(View.GONE);
				mCaptchaSkipView.setVisibility(View.GONE);
				break;
			case SKIP:
				this.mCurrentDownloadCaptchaTask = null;
				mCaptchaAnswerView.setVisibility(View.GONE);
				mCaptchaImageView.setVisibility(View.GONE);
				mCaptchaLoadingView.setVisibility(View.GONE);
				mCaptchaSkipView.setVisibility(View.VISIBLE);
				break;
		}
		
	}
	
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.addpost, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	switch (item.getItemId()) {
    		case R.id.menu_send_post_id:
    			this.onSend();
    			break;
    		case R.id.menu_attach_file_id:
    			Intent intent = new Intent(this, FilesListActivity.class);
    			intent.putExtra(FilesListActivity.EXTRA_CURRENT_FILE, this.mAttachedFile != null ? this.mAttachedFile.file.getAbsolutePath() : null);
    			startActivityForResult(intent, Constants.REQUEST_CODE_FILE_LIST_ACTIVITY);
    			break;
    		case R.id.menu_gallery_id:
    			Intent i = new Intent(Intent.ACTION_GET_CONTENT);
    			i.setType("image/*");
    			startActivityForResult(i, Constants.REQUEST_CODE_GALLERY); 
    			break;
    	}
    	
    	return true;
    }
    
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(resultCode == RESULT_OK) {
			switch(requestCode) {
				case Constants.REQUEST_CODE_FILE_LIST_ACTIVITY:
					SerializableFileModel fileModel = data.getParcelableExtra(getPackageName() + Constants.EXTRA_SELECTED_FILE);
					
					this.setAttachment(fileModel);
					
					break;
				case Constants.REQUEST_CODE_GALLERY:
					this.mTracker.trackEvent(Tracker.CATEGORY_UI, Tracker.ACTION_SELECT_IMAGE_FROM_GALLERY);
					
					Uri selectedImage = data.getData();
					
					String[] filePathColumn = { Images.Media.DATA };
		            Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
		            cursor.moveToFirst();

		            String filePath = cursor.getString(cursor.getColumnIndex(filePathColumn[0]));
		            ImageFileModel image = new ImageFileModel(filePath);
		            cursor.close();

		            this.setAttachment(image);
		            
					break;
			}
		}
	}
	
	private void setAttachment(ImageFileModel fileModel){
		this.mAttachedFile = fileModel;
		
		this.mAttachmentView.setVisibility(View.VISIBLE);
		TextView fileNameView = (TextView)findViewById(R.id.addpost_attachment_name);
		TextView fileSizeView = (TextView)findViewById(R.id.addpost_attachment_size);
		
		fileNameView.setText(fileModel.file.getName());
		
		String infoFormat = this.getResources().getString(R.string.data_add_post_attachment_info);
		fileSizeView.setText(String.format(infoFormat, fileModel.getFileSize(), fileModel.imageWidth, fileModel.imageHeight));
	}
	
	private void removeAttachment(){
		this.mAttachedFile = null;
		
		this.mAttachmentView.setVisibility(View.GONE);
	}
	
	private void refreshCaptcha(){
    	if(mCurrentDownloadCaptchaTask != null){
    		mCurrentDownloadCaptchaTask.cancel(true);
    	}
    	
    	mCurrentDownloadCaptchaTask = new DownloadCaptchaTask(this, mBoardName, mThreadNumber, mJsonReader, mHttpBitmapReader, mHtmlCaptchaChecker);
    	mCurrentDownloadCaptchaTask.execute();
	}
	
	private void formatSelectedText(String code) {
		Editable editable = mCommentView.getEditableText();
		String text = editable.toString();
				
		String startTag = "[" + code + "]";
		String endTag = "[/" + code + "]";
		
		int selectionStart = mCommentView.getSelectionStart();
		int selectionEnd = mCommentView.getSelectionEnd();
		String selectedText = text.substring(selectionStart, selectionEnd);
		
		String textBeforeSelection = text.substring(Math.max(0, selectionStart - startTag.length()), selectionStart);
		String textAfterSelection = text.substring(selectionEnd, Math.min(text.length(), selectionEnd + endTag.length()));
		
		// Удаляем теги форматирования если есть, добавляем если нет
		if(textBeforeSelection.equalsIgnoreCase(startTag) && textAfterSelection.equalsIgnoreCase(endTag)){
			editable.replace(selectionStart - startTag.length(), selectionEnd + endTag.length(), selectedText);
			mCommentView.setSelection(selectionStart - startTag.length(), selectionEnd - startTag.length());
		}
		else {
			editable.replace(selectionStart, selectionEnd, startTag + selectedText + endTag);
			mCommentView.setSelection(selectionStart + startTag.length(), selectionEnd + startTag.length());
		}
	}
}
