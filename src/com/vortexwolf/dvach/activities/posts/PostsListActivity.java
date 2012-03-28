package com.vortexwolf.dvach.activities.posts;

import java.util.Timer;
import java.util.TimerTask;

import com.vortexwolf.dvach.R;
import com.vortexwolf.dvach.activities.addpost.AddPostActivity;
import com.vortexwolf.dvach.activities.browser.BrowserLauncher;
import com.vortexwolf.dvach.activities.files.SerializableFileModel;
import com.vortexwolf.dvach.activities.threads.ThumbnailOnClickListenerFactory;
import com.vortexwolf.dvach.api.entities.PostInfo;
import com.vortexwolf.dvach.api.entities.PostsList;
import com.vortexwolf.dvach.common.Constants;
import com.vortexwolf.dvach.common.MainApplication;
import com.vortexwolf.dvach.common.http.DownloadFileTask;
import com.vortexwolf.dvach.common.http.HttpBitmapReader;
import com.vortexwolf.dvach.common.library.BitmapManager;
import com.vortexwolf.dvach.common.library.MyLog;
import com.vortexwolf.dvach.common.library.TimerService;
import com.vortexwolf.dvach.common.library.Tracker;
import com.vortexwolf.dvach.common.utils.AppearanceUtils;
import com.vortexwolf.dvach.common.utils.StringUtils;
import com.vortexwolf.dvach.common.utils.UriUtils;
import com.vortexwolf.dvach.interfaces.IBitmapManager;
import com.vortexwolf.dvach.interfaces.IDownloadFileService;
import com.vortexwolf.dvach.interfaces.IJsonApiReader;
import com.vortexwolf.dvach.interfaces.IPostsListView;
import com.vortexwolf.dvach.interfaces.IThumbnailOnClickListenerFactory;
import com.vortexwolf.dvach.presentation.models.AttachmentInfo;
import com.vortexwolf.dvach.presentation.models.PostItemViewModel;
import com.vortexwolf.dvach.settings.ApplicationPreferencesActivity;
import com.vortexwolf.dvach.settings.ApplicationSettings;
import com.vortexwolf.dvach.settings.SettingsEntity;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AbsListView.OnScrollListener;

public class PostsListActivity extends ListActivity implements ListView.OnScrollListener {
    private static final String TAG = "PostsListActivity";
    
    private MainApplication mApplication;
    private ApplicationSettings mSettings;
	private IJsonApiReader mJsonReader;
	private Tracker mTracker;

	private PostsListAdapter mAdapter = null;
	private DownloadPostsTask mCurrentDownloadTask = null;
    private TimerService mAutoRefreshTimer = null;
    private final PostsReaderListener mPostsReaderListener = new PostsReaderListener();
	
	private SettingsEntity mCurrentSettings;
	
	private View mLoadingView = null;
	private View mErrorView = null;
	private View mPartialLoadingView = null;
	private enum ViewType { LIST, LOADING, ERROR};
	
	private String mBoardName;
	private String mThreadNumber;
	
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		
        requestWindowFeature(Window.FEATURE_PROGRESS);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        
        this.mApplication = (MainApplication) this.getApplication();
        this.mSettings = this.mApplication.getSettings();
        this.mJsonReader = this.mApplication.getJsonApiReader();
        this.mCurrentSettings = this.mSettings.getCurrentSettings();
        this.mTracker = this.mApplication.getTracker();
        
        this.resetUI();

    	//Хоть одним из двух способов я должен получить название борды и номер треда
        if(mBoardName == null || mThreadNumber == null){
           	//При переходе извне приложения (нажатие на html-ссылку)
        	Uri data = this.getIntent().getData();
        	if(data != null){
        		this.mBoardName = UriUtils.getBoardName(data);
        		this.mThreadNumber = UriUtils.getPageName(data);
        	}
        	else{
        		//При переходе изнутри этого приложения
		        Bundle extras = getIntent().getExtras();
		    	if (extras != null) {
		    		this.mBoardName = extras.getString(Constants.EXTRA_BOARD_NAME);
		    		this.mThreadNumber = extras.getString(Constants.EXTRA_THREAD_NUMBER);
		    	}
        	}
        }
        
        if(mAdapter == null)
        {    	
        	mAdapter = new PostsListAdapter(this, mBoardName, mThreadNumber, this.mApplication.getBitmapManager(), mApplication.getSettings());
        	setListAdapter(mAdapter);
        	
    		this.refreshPosts();
        }
        
        String titleString = this.getIntent().hasExtra(Constants.EXTRA_THREAD_SUBJECT) 
        					? String.format(getString(R.string.data_thread_withsubject_title), mBoardName, this.getIntent().getExtras().getString(Constants.EXTRA_THREAD_SUBJECT))
        					: String.format(getString(R.string.data_thread_title), mBoardName, mThreadNumber);
        
		this.setTitle(titleString);
		

		final Runnable refreshTask = new Runnable() {
			@Override
			public void run() {
				MyLog.v(TAG, "Attempted to refresh");
				if(PostsListActivity.this.mCurrentDownloadTask == null) {
					PostsListActivity.this.refreshPosts();
				}
			}
		};
		
		this.mAutoRefreshTimer = new TimerService(this.mSettings.isAutoRefresh(), this.mSettings.getAutoRefreshInterval(), refreshTask, this);
		this.mAutoRefreshTimer.start();
		
        this.mTracker.setBoardVar(mBoardName);
        this.mTracker.trackActivityView(TAG);
    }
    
    @Override
	protected void onDestroy() {
		this.mAutoRefreshTimer.stop();
		
		if(this.mCurrentDownloadTask != null){
			this.mCurrentDownloadTask.cancel(true);
		}

		MyLog.d(TAG, "Destroyed");
		
	   super.onDestroy();
	}

	@Override
	protected void onResume() {
		super.onResume();
		
		// Проверяем изменение настроек
		SettingsEntity newSettings = this.mSettings.getCurrentSettings();
		if(this.mCurrentSettings.theme != newSettings.theme){
			this.resetUI();
			return;
		}

		if(this.mCurrentSettings.isDisplayDate != newSettings.isDisplayDate || this.mCurrentSettings.isLoadThumbnails != newSettings.isLoadThumbnails){
			this.mAdapter.notifyDataSetChanged();
		}

		this.mAutoRefreshTimer.update(this.mSettings.isAutoRefresh(), this.mSettings.getAutoRefreshInterval());
		
		this.mCurrentSettings = newSettings;
	}
	
	private void resetUI()
    {
		// Возвращаем прежнее положение scroll view после перезагрузки темы
		AppearanceUtils.ListViewPosition position = AppearanceUtils.getCurrentListPosition(this.getListView());
		
		this.setTheme(this.mSettings.getTheme());
		this.setContentView(R.layout.posts_list_view);
		this.registerForContextMenu(this.getListView());

		this.mLoadingView = this.findViewById(R.id.loading);
		this.mErrorView = this.findViewById(R.id.error);
		this.mPartialLoadingView = this.findViewById(R.id.addItemsLoading);
        
		this.getListView().setSelectionFromTop(position.position, position.top);
        if(Integer.valueOf(Build.VERSION.SDK) > 7){
        	this.getListView().setOnScrollListener(this);
        }
    }
    
	
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.thread, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	switch (item.getItemId()) {
    	case R.id.refresh_menu_id:
    		this.refreshPosts();
    		break;
    	case R.id.open_browser_menu_id:
    		BrowserLauncher.launchExternalBrowser(this, UriUtils.create2chThreadURL(this.mBoardName, this.mThreadNumber), true);
    		break;
    	case R.id.preferences_menu_id:
    		//Start new activity
    		Intent preferencesIntent = new Intent(getApplicationContext(), ApplicationPreferencesActivity.class);
    		startActivity(preferencesIntent);
    		break;
    	case R.id.add_menu_id:
    		this.navigateToAddPostView(null, null);
    		break;
    	}
    	
    	return true;
    }
	
	@Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo){
    	super.onCreateContextMenu(menu, v, menuInfo);
    	
    	AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
    	PostItemViewModel item = mAdapter.getItem(info.position);
    	
    	menu.add(Menu.NONE, Constants.CONTEXT_MENU_REPLY_POST, 0, this.getString(R.string.cmenu_reply_post));
    	if(!StringUtils.isEmpty(item.getSpannedComment().toString())){
    		menu.add(Menu.NONE, Constants.CONTEXT_MENU_REPLY_POST_QUOTE, 1, this.getString(R.string.cmenu_reply_post_quote));
    	}
    	if(item.hasAttachment() && item.getAttachment(this.mBoardName).isFile()){
    		menu.add(Menu.NONE, Constants.CONTEXT_MENU_DOWNLOAD_FILE, 2, this.getString(R.string.cmenu_download_file));
    	}
	}
	
    @Override
    public boolean onContextItemSelected(MenuItem item) {
    	AdapterView.AdapterContextMenuInfo menuInfo = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
    	PostItemViewModel info = mAdapter.getItem(menuInfo.position);
        int itemId = item.getItemId();
        
        switch(item.getItemId()){
	        case Constants.CONTEXT_MENU_REPLY_POST:{
	        	navigateToAddPostView(info.getNumber(), null);
				return true;
	        }
	        case Constants.CONTEXT_MENU_REPLY_POST_QUOTE:{
	        	navigateToAddPostView(info.getNumber(), info.getSpannedComment().toString());
				return true;        	
	        }
	        case Constants.CONTEXT_MENU_DOWNLOAD_FILE:{
	        	AttachmentInfo attachment = info.getAttachment(this.mBoardName);
	        	this.mApplication.getDownloadFileService().downloadFile(this, attachment.getSourceUrl(this.mSettings));
	        	
	    	    this.mTracker.trackEvent(Tracker.CATEGORY_UI, Tracker.ACTION_DOWNLOAD_FILE, Tracker.LABEL_DOWNLOAD_FILE_FROM_CONTEXT_MENU);
	    	    
	        	return true;
	        }
        }
        
        return false;
    }
       
    private void navigateToAddPostView(String postNumber, String postComment){
    	Intent addPostIntent = new Intent(getApplicationContext(), AddPostActivity.class);
		addPostIntent.putExtra(Constants.EXTRA_BOARD_NAME, this.mBoardName);
		addPostIntent.putExtra(Constants.EXTRA_THREAD_NUMBER, this.mThreadNumber);
		
		if(postNumber != null){
			addPostIntent.putExtra(Constants.EXTRA_POST_NUMBER, postNumber);
		}
		if(postComment != null){
			addPostIntent.putExtra(Constants.EXTRA_POST_COMMENT, postComment);
		}
		
		startActivityForResult(addPostIntent, Constants.REQUEST_CODE_ADD_POST_ACTIVITY);
    }
    
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(resultCode == RESULT_OK) {
			switch(requestCode) {
				case Constants.REQUEST_CODE_ADD_POST_ACTIVITY:
					this.refreshPosts();
					break;
			}
		}
	}
	
	private void switchToView(ViewType vt){
		switch(vt){
			case LIST:
				this.getListView().setVisibility(View.VISIBLE);
				mLoadingView.setVisibility(View.GONE);
				mErrorView.setVisibility(View.GONE);
				break;
			case LOADING:
				this.getListView().setVisibility(View.GONE);
				mLoadingView.setVisibility(View.VISIBLE);
				mErrorView.setVisibility(View.GONE);
				break;
			case ERROR:
				this.getListView().setVisibility(View.GONE);
				mLoadingView.setVisibility(View.GONE);
				mErrorView.setVisibility(View.VISIBLE);
				break;
		}
	}
	
	private void refreshPosts(){
		//На всякий случай отменю, чтобы не было проблем с обновлениями
		//Возможно, лучше бы не запускать совсем
		if(mCurrentDownloadTask != null){
			this.mCurrentDownloadTask.cancel(true);
		}
		//Если адаптер пустой, то значит была ошибка при загрузке, в таком случае запускаю загрузку заново
		if(!mAdapter.isEmpty()){
			//Здесь запускаю с индикатором того, что происходит обновление, а не загрузка заново
			mCurrentDownloadTask = new DownloadPostsTask(mPostsReaderListener, mBoardName, mThreadNumber, mJsonReader, true);
			mCurrentDownloadTask.execute(mAdapter.getLastPostNumber());
		}
		else
		{
			mCurrentDownloadTask = new DownloadPostsTask(mPostsReaderListener, mBoardName, mThreadNumber, mJsonReader, false);
    		mCurrentDownloadTask.execute();
		}
	}
	
	

	@Override
	public void onScroll(AbsListView arg0, int arg1, int arg2, int arg3) {	
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
        switch (scrollState) {
	        case OnScrollListener.SCROLL_STATE_IDLE:
	            this.mAdapter.setBusy(false, view);
	            break;
	        case OnScrollListener.SCROLL_STATE_TOUCH_SCROLL:
	        	this.mAdapter.setBusy(true, view);
	            break;
	        case OnScrollListener.SCROLL_STATE_FLING:
	        	this.mAdapter.setBusy(true, view);
	            break;
		}
	}
	
	private class PostsReaderListener implements IPostsListView {

		@Override
		public Context getApplicationContext() {
			return PostsListActivity.this.getApplicationContext();
		}

		@Override
		public void setWindowProgress(int value) {
			PostsListActivity.this.getWindow().setFeatureInt(Window.FEATURE_PROGRESS, value);
		}

		@Override
		public void setData(PostsList posts) {
			if(posts != null){
				mAdapter.setAdapterData(posts.getThread());
			}
			else {
				MyLog.e(TAG, "posts = null");
			}

		}

		@Override
		public void showError(String error) {
			PostsListActivity.this.switchToView(ViewType.ERROR);
			
			TextView errorTextView = (TextView)mErrorView.findViewById(R.id.error_text);
			if(errorTextView != null){
				errorTextView.setText(error != null ? error : mApplication.getErrors().getUnknownError());
			}
		}

	    @Override
	    public void showLoadingScreen() {
	    	PostsListActivity.this.switchToView(ViewType.LOADING);
	    }
	    
	    @Override
	    public void hideLoadingScreen() {
	    	PostsListActivity.this.switchToView(ViewType.LIST);
	    	mCurrentDownloadTask = null;
	    }

		@Override
		public void updateData(String from, PostsList list) {
			PostInfo[] posts = list.getThread();

			int addedCount = mAdapter.updateAdapterData(from, posts);
			if(addedCount != 0){
				AppearanceUtils.showToastMessage(PostsListActivity.this, PostsListActivity.this.getResources().getQuantityString(R.plurals.data_new_posts_quantity, addedCount, addedCount));
			}
			else {
				AppearanceUtils.showToastMessage(PostsListActivity.this, PostsListActivity.this.getString(R.string.notification_no_new_posts));
			}
		}

		@Override
		public void showUpdateError(String error) {
			AppearanceUtils.showToastMessage(PostsListActivity.this, error);
		}

		@Override
		public void showUpdateLoading() {
			mPartialLoadingView.setVisibility(View.VISIBLE);
		}

		@Override
		public void hideUpdateLoading() {
			mPartialLoadingView.setVisibility(View.GONE);
			mCurrentDownloadTask = null;
		}
	}
}
