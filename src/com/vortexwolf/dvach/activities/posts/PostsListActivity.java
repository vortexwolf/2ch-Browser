package com.vortexwolf.dvach.activities.posts;

import com.vortexwolf.dvach.R;
import com.vortexwolf.dvach.activities.addpost.AddPostActivity;
import com.vortexwolf.dvach.activities.browser.BrowserLauncher;
import com.vortexwolf.dvach.activities.tabs.TabsHistoryBookmarksActivity;
import com.vortexwolf.dvach.api.entities.PostInfo;
import com.vortexwolf.dvach.api.entities.PostsList;
import com.vortexwolf.dvach.common.BaseListActivity;
import com.vortexwolf.dvach.common.Constants;
import com.vortexwolf.dvach.common.MainApplication;
import com.vortexwolf.dvach.common.library.MyLog;
import com.vortexwolf.dvach.common.utils.AppearanceUtils;
import com.vortexwolf.dvach.common.utils.StringUtils;
import com.vortexwolf.dvach.common.utils.UriUtils;
import com.vortexwolf.dvach.interfaces.IJsonApiReader;
import com.vortexwolf.dvach.interfaces.IOpenTabsManager;
import com.vortexwolf.dvach.interfaces.IPostsListView;
import com.vortexwolf.dvach.interfaces.IPagesSerializationService;
import com.vortexwolf.dvach.presentation.models.AttachmentInfo;
import com.vortexwolf.dvach.presentation.models.OpenTabModel;
import com.vortexwolf.dvach.presentation.models.PostItemViewModel;
import com.vortexwolf.dvach.presentation.services.ListViewScrollListener;
import com.vortexwolf.dvach.presentation.services.TimerService;
import com.vortexwolf.dvach.presentation.services.Tracker;
import com.vortexwolf.dvach.settings.ApplicationPreferencesActivity;
import com.vortexwolf.dvach.settings.ApplicationSettings;
import com.vortexwolf.dvach.settings.SettingsEntity;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.ClipboardManager;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AbsListView.OnScrollListener;

public class PostsListActivity extends BaseListActivity {
    private static final String TAG = "PostsListActivity";
    
    private MainApplication mApplication;
    private ApplicationSettings mSettings;
	private IJsonApiReader mJsonReader;
	private Tracker mTracker;
	private IPagesSerializationService mSerializationService;

	private PostsListAdapter mAdapter = null;
	private DownloadPostsTask mCurrentDownloadTask = null;
    private TimerService mAutoRefreshTimer = null;
    private final PostsReaderListener mPostsReaderListener = new PostsReaderListener();
	
	private SettingsEntity mCurrentSettings;

	private OpenTabModel mTabModel;
	private String mBoardName;
	private String mThreadNumber;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.mApplication = (MainApplication) this.getApplication();

        // Парсим код доски и номер страницы
    	Uri data = this.getIntent().getData();
    	if(data != null){
    		this.mBoardName = UriUtils.getBoardName(data);
    		this.mThreadNumber = UriUtils.getThreadNumber(data);
    	}
        
        this.mSettings = this.mApplication.getSettings();
        this.mJsonReader = this.mApplication.getJsonApiReader();
        this.mCurrentSettings = this.mSettings.getCurrentSettings();
        this.mTracker = this.mApplication.getTracker();
        this.mSerializationService = this.mApplication.getSerializationService();
        IOpenTabsManager tabsManager = this.mApplication.getOpenTabsManager();
        
        // Заголовок страницы
        String pageSubject = this.getIntent().hasExtra(Constants.EXTRA_THREAD_SUBJECT) ? this.getIntent().getExtras().getString(Constants.EXTRA_THREAD_SUBJECT) : null;
        String pageTitle = pageSubject != null
        					? String.format(getString(R.string.data_thread_withsubject_title), mBoardName, pageSubject)
        					: String.format(getString(R.string.data_thread_title), mBoardName, mThreadNumber);
        
		this.setTitle(pageTitle);
		
		// Сохраняем во вкладках
		OpenTabModel tabModel = new OpenTabModel(pageSubject != null ? pageSubject : pageTitle, mBoardName, mThreadNumber);
		this.mTabModel = tabsManager.add(tabModel);
		
        this.resetUI();

        this.setAdapter();
        
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

		MyLog.d(TAG, "Destroyed");

		super.onDestroy();
	}
    
	@Override
	protected void onPause() {
		this.mTabModel.setPosition(AppearanceUtils.getCurrentListPosition(this.getListView()));
		
		super.onPause();
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
	
	@Override
	protected int getLayoutId() {
		return R.layout.posts_list_view;
	}
	
	@Override
	protected void resetUI()
    {
		super.resetUI();
		
		this.registerForContextMenu(this.getListView());
    }
    
	private void setAdapter() {
		if (mAdapter != null) return;

		mAdapter = new PostsListAdapter(this, mBoardName, mThreadNumber, this.mApplication.getBitmapManager(), mApplication.getSettings(), this.getTheme(), this.getListView());
		this.setListAdapter(mAdapter);

		// добавляем обработчик, чтобы не рисовать картинки во время прокрутки
        if(Integer.valueOf(Build.VERSION.SDK) > 7){
        	this.getListView().setOnScrollListener(new ListViewScrollListener(this.mAdapter));
        }
        
		// Пробуем десериализовать в любом случае
		PostInfo[] posts = this.mSerializationService.deserializePosts(this.mThreadNumber);
		if (posts != null) {
			this.mAdapter.setAdapterData(posts);

			// Устанавливаем позицию, если открываем как уже открытую вкладку
			AppearanceUtils.ListViewPosition savedPosition = this.mTabModel.getPosition();
			if (savedPosition != null) {
				this.getListView().setSelectionFromTop(savedPosition.position, savedPosition.top);
			}

			// Обновляем посты, если не был установлен ограничивающий extra
			if (!this.getIntent().hasExtra(Constants.EXTRA_PREFER_DESERIALIZED)) {
				this.refreshPosts();
			}
		} else {
			this.refreshPosts();
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
    	case R.id.tabs_menu_id:
    		Intent openTabsIntent = new Intent(getApplicationContext(), TabsHistoryBookmarksActivity.class);
    		openTabsIntent.putExtra(Constants.EXTRA_CURRENT_URL, this.mTabModel.getUri().toString());
    		startActivity(openTabsIntent);
    		break;
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
    	if(!StringUtils.isEmpty(item.getSpannedComment().toString())){
    		menu.add(Menu.NONE, Constants.CONTEXT_MENU_COPY_TEXT, 2, this.getString(R.string.cmenu_copy_post));
    	}
    	if(item.hasAttachment() && item.getAttachment(this.mBoardName).isFile()){
    		menu.add(Menu.NONE, Constants.CONTEXT_MENU_DOWNLOAD_FILE, 3, this.getString(R.string.cmenu_download_file));
    	}
	}
	
    @Override
    public boolean onContextItemSelected(MenuItem item) {
    	AdapterView.AdapterContextMenuInfo menuInfo = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
    	PostItemViewModel info = mAdapter.getItem(menuInfo.position);

        switch(item.getItemId()){
	        case Constants.CONTEXT_MENU_REPLY_POST:{
	        	mTracker.trackEvent(Tracker.CATEGORY_UI, Tracker.ACTION_CONTEXT_REPLY_POST);
	        	navigateToAddPostView(info.getNumber(), null);
				return true;
	        }
	        case Constants.CONTEXT_MENU_REPLY_POST_QUOTE:{
	        	mTracker.trackEvent(Tracker.CATEGORY_UI, Tracker.ACTION_CONTEXT_REPLY_POST_QUOTE);
	        	navigateToAddPostView(info.getNumber(), info.getSpannedComment().toString());
				return true;        	
	        }
	        case Constants.CONTEXT_MENU_COPY_TEXT:{
	        	mTracker.trackEvent(Tracker.CATEGORY_UI, Tracker.ACTION_CONTEXT_COPY_POST);
	        	ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE); 
	        	clipboard.setText(info.getSpannedComment().toString());
	        	
	        	AppearanceUtils.showToastMessage(this, this.getString(R.string.notification_post_copied));
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
		public void setData(PostsList postsList) {
			MyLog.d(TAG, "setData was called");
			if(postsList != null){
				PostInfo[] posts = postsList.getThread();
				mSerializationService.serializePosts(mThreadNumber, posts);
				mAdapter.setAdapterData(posts);
			}
			else {
				MyLog.e(TAG, "posts = null");
			}

		}

		@Override
		public void showError(String error) {
			PostsListActivity.this.switchToErrorView(error);
		}

	    @Override
	    public void showLoadingScreen() {
	    	PostsListActivity.this.switchToLoadingView();
	    }
	    
	    @Override
	    public void hideLoadingScreen() {
	    	PostsListActivity.this.switchToListView();
	    	mCurrentDownloadTask = null;
	    }

		@Override
		public void updateData(String from, PostsList list) {
			PostInfo[] posts = list.getThread();

			int addedCount = mAdapter.updateAdapterData(from, posts);
			if(addedCount != 0){
				// Нужно удостовериться, что элементы из posts не менялись после добавления в адаптер, чтобы сериализация прошла правильно
				mSerializationService.serializePosts(mThreadNumber, posts);
				AppearanceUtils.showToastMessage(PostsListActivity.this, PostsListActivity.this.getResources().getQuantityString(R.plurals.data_new_posts_quantity, addedCount, addedCount));
			}
		}

		@Override
		public void showUpdateError(String error) {
			AppearanceUtils.showToastMessage(PostsListActivity.this, error);
		}

		@Override
		public void showUpdateLoading() {
			mAdapter.setLoadingMore(true);
		}

		@Override
		public void hideUpdateLoading() {
			mAdapter.setLoadingMore(false);
			mCurrentDownloadTask = null;
		}
	}
}
