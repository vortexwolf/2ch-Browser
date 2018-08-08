package ua.in.quireg.chan.ui.fragments;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;

import java.util.List;

import javax.inject.Inject;

import ua.in.quireg.chan.R;
import ua.in.quireg.chan.asynctasks.DownloadFileListTask;
import ua.in.quireg.chan.asynctasks.DownloadFileTask;
import ua.in.quireg.chan.asynctasks.DownloadPostsTask;
import ua.in.quireg.chan.boards.makaba.MakabaApiReader;
import ua.in.quireg.chan.common.Constants;
import ua.in.quireg.chan.common.Factory;
import ua.in.quireg.chan.common.MainApplication;
import ua.in.quireg.chan.common.Websites;
import ua.in.quireg.chan.common.utils.AppearanceUtils;
import ua.in.quireg.chan.common.utils.CompatibilityUtils;
import ua.in.quireg.chan.common.utils.StringUtils;
import ua.in.quireg.chan.db.FavoritesDataSource;
import ua.in.quireg.chan.db.HistoryDataSource;
import ua.in.quireg.chan.interfaces.IJsonApiReader;
import ua.in.quireg.chan.interfaces.IPostsListView;
import ua.in.quireg.chan.interfaces.IUrlBuilder;
import ua.in.quireg.chan.interfaces.IWebsite;
import ua.in.quireg.chan.models.domain.CaptchaEntity;
import ua.in.quireg.chan.models.domain.PostModel;
import ua.in.quireg.chan.models.presentation.AttachmentInfo;
import ua.in.quireg.chan.models.presentation.IPostListEntity;
import ua.in.quireg.chan.models.presentation.OpenTabModel;
import ua.in.quireg.chan.models.presentation.PostItemViewModel;
import ua.in.quireg.chan.models.presentation.StatusIndicatorEntity;
import ua.in.quireg.chan.mvp.routing.MainRouter;
import ua.in.quireg.chan.services.BrowserLauncher;
import ua.in.quireg.chan.services.ThreadImagesService;
import ua.in.quireg.chan.services.TimerService;
import ua.in.quireg.chan.services.presentation.ListViewScrollListener;
import ua.in.quireg.chan.services.presentation.OpenTabsManager;
import ua.in.quireg.chan.services.presentation.PagesSerializationService;
import ua.in.quireg.chan.services.presentation.PostItemViewBuilder;
import ua.in.quireg.chan.settings.ApplicationSettings;
import ua.in.quireg.chan.ui.activities.AddPostActivity;
import ua.in.quireg.chan.ui.adapters.PostsListAdapter;

import static android.app.Activity.RESULT_OK;

public class PostsListFragment extends BaseListFragment {

    @Inject MainRouter mRouter;
    @Inject ApplicationSettings mSettings;
    @Inject OpenTabsManager mOpenTabsManager;
    @Inject HistoryDataSource mHistoryDataSource;
    @Inject FavoritesDataSource mFavoritesDatasource;

    private IJsonApiReader mJsonReader;
    private final PagesSerializationService mSerializationService = Factory.resolve(PagesSerializationService.class);
    private final ThreadImagesService mThreadImagesService = Factory.resolve(ThreadImagesService.class);

    private IUrlBuilder mUrlBuilder;
    private PostsListAdapter mAdapter = null;
    private DownloadPostsTask mCurrentDownloadTask = null;
    private TimerService mAutoRefreshTimer = null;
    private final PostsReaderListener mPostsReaderListener = new PostsReaderListener();

//    private SettingsEntity mCurrentSettings;

    private OpenTabModel mTabModel;
    private IWebsite mWebsite;
    private String mBoardName;
    private String mThreadNumber;
    private String mPostNumber;
    private String pageSubject;
    private Menu mMenu;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        MainApplication.getAppComponent().inject(this);
        super.onCreate(savedInstanceState);

        // Парсим код доски и номер страницы
        Bundle extras = getArguments();
//        mWebsite = Websites.fromName(extras.getString(Constants.EXTRA_WEBSITE));
        mWebsite = Websites.getDefault();
        mBoardName = StringUtils.emptyIfNull(extras.getString(Constants.EXTRA_BOARD_NAME));
        mThreadNumber = StringUtils.emptyIfNull(extras.getString(Constants.EXTRA_THREAD_NUMBER));
        mPostNumber = StringUtils.emptyIfNull(extras.getString(Constants.EXTRA_POST_NUMBER));
        pageSubject = StringUtils.nullIfEmpty(extras.getString(Constants.EXTRA_THREAD_SUBJECT));

        mUrlBuilder = mWebsite.getUrlBuilder();
        mJsonReader = Factory.resolve(MakabaApiReader.class);

        //TODO fix isFavourite
//        OpenTabModel tabModel = new OpenTabModel(mWebsite, mBoardName, 0, mThreadNumber, pageSubject, false);
//        mTabModel = mOpenTabsManager.add(tabModel);

        setHasOptionsMenu(true);

//        final Runnable refreshTask = new Runnable() {
//            @Override
//            public void run() {
//                MyLog.v(TAG, "Attempted to refresh");
//                if (mCurrentDownloadTask == null) {
//                    refresh();
//                }
//            }
//        };
//
//        mAutoRefreshTimer = new TimerService(mSettings.isAutoRefresh(), mSettings.getAutoRefreshInterval(), refreshTask, getActivity());
//        mAutoRefreshTimer.start();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.posts_list_view, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setAdapter(savedInstanceState);
        registerForContextMenu(mListView);
        updateTitle(pageSubject);

    }

    @Override
    public void onDestroy() {
//        mAutoRefreshTimer.stop();

        String uri = mUrlBuilder.getThreadUrlHtml(mBoardName, mThreadNumber);
        mThreadImagesService.clearThreadImages(uri);

        super.onDestroy();
    }

    @Override
    public void onStop() {
//        mTabModel.setPosition(AppearanceUtils.getCurrentListPosition(mListView));
        if (mCurrentDownloadTask != null) {
            mCurrentDownloadTask.cancel(true);
        }
        super.onStop();
    }

    @Override
    public void onResume() {
        super.onResume();
        //TODO fix some kind of timer

//        // Проверяем изменение настроек
//        SettingsEntity newSettings = mSettings.getCurrentSettings();
//        if (mCurrentSettings.theme != newSettings.theme) {
//            finish();
//            Intent i = new Intent(getIntent());
//            i.putExtra(Constants.EXTRA_PREFER_DESERIALIZED, true);
//            startActivity(i);
//            return;
//        }
//
//        if (mCurrentSettings.isDisplayDate != newSettings.isDisplayDate || mCurrentSettings.isLoadThumbnails != newSettings.isLoadThumbnails || mCurrentSettings.isLocalDate != newSettings.isLocalDate) {
//            mAdapter.notifyDataSetChanged();
//        }
//
//        mAutoRefreshTimer.update(mSettings.isAutoRefresh(), mSettings.getAutoRefreshInterval());
//
//        mCurrentSettings = newSettings;
    }

    private void setAdapter(Bundle savedInstanceState) {
        if (mAdapter == null) {
            mAdapter = new PostsListAdapter(getActivity(), mWebsite, mBoardName, mThreadNumber, mListView);
        }
        mListView.setAdapter(mAdapter);

        mListView.setOnScrollListener(new ListViewScrollListener(mAdapter));

        boolean preferDeserialized = getArguments().getBoolean(Constants.EXTRA_PREFER_DESERIALIZED)
                || savedInstanceState != null && savedInstanceState.containsKey(Constants.EXTRA_PREFER_DESERIALIZED);

        LoadPostsTask task = new LoadPostsTask(preferDeserialized);
        task.execute();
    }

    private void setAdapterData(PostModel[] posts) {
        boolean isFirstTime = mAdapter.isEmpty();

        mAdapter.setAdapterData(posts);
        if (posts.length > 0) {
            PostItemViewModel firstModel = (PostItemViewModel) mAdapter.getItem(0);
            updateTitle(firstModel.getSubjectOrText());
        }

        if (isFirstTime) {
            if (mPostNumber != null) {
                mAdapter.scrollToPost(mPostNumber);
            } else {
                // Устанавливаем позицию, если открываем как уже открытую вкладку
                AppearanceUtils.ListViewPosition savedPosition = mTabModel.getPosition();
                if (savedPosition != null) {
                    mListView.setSelectionFromTop(savedPosition.position, savedPosition.top);
                }
            }
        }
    }

    private void updateTitle(String title) {
        if (!isAdded()) {
            return;
        }
        String pageTitle = title != null
                ? String.format(getString(R.string.data_thread_withsubject_title), mBoardName, title)
                : String.format(getString(R.string.data_thread_title), mBoardName, mThreadNumber);

        setTitle(pageTitle);

        String tabTitle = title != null ? title : pageTitle;
//        mTabModel.setTitle(tabTitle);
//        mHistoryDataSource.updateHistoryItem(mWebsite.name(), mBoardName, mThreadNumber, tabTitle);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.thread, menu);
        mMenu = menu;
        updateOptionsMenu();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                getActivity().onBackPressed();
                break;
            case R.id.refresh_menu_id:
                refreshPosts(true);
                break;
            case R.id.open_browser_menu_id:
                BrowserLauncher.launchExternalBrowser(getActivity(), mUrlBuilder.getThreadUrlHtml(mBoardName, mThreadNumber));
                break;
            case R.id.add_menu_id:
                navigateToAddNewPost();
                break;
            case R.id.download_all_files_menu_id:
                List<String> filePaths = mAdapter.getAllPostFiles();
                if (filePaths.size() > 0) {
                    DownloadFileListTask downloadAllTask = new DownloadFileListTask(getContext(), mWebsite, mThreadNumber, filePaths);
                    downloadAllTask.execute();
                }
                break;
            case R.id.share_menu_id:
                String shareUrl = mUrlBuilder.getThreadUrlHtml(mBoardName, mThreadNumber);

                Intent i = new Intent(Intent.ACTION_SEND);
                i.setType("text/plain");
                i.putExtra(Intent.EXTRA_SUBJECT, mTabModel.getTitle());
                i.putExtra(Intent.EXTRA_TEXT, shareUrl);
                startActivity(Intent.createChooser(i, getString(R.string.share_via)));
                break;
            case R.id.add_remove_favorites_menu_id:
                if (mFavoritesDatasource.hasFavorites(mWebsite.name(), mBoardName, mThreadNumber)) {
                    mFavoritesDatasource.removeFromFavorites(mWebsite.name(), mBoardName, mThreadNumber);
                } else {
                    mFavoritesDatasource.addToFavorites(mWebsite.name(), mBoardName, mThreadNumber, mTabModel.getTitle());
                }

                updateOptionsMenu();

                break;
        }

        return true;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
        IPostListEntity item = mAdapter.getItem(info.position);
        if (item instanceof StatusIndicatorEntity) {
            return;
        }

        PostItemViewModel model = (PostItemViewModel) item;
        populateContextMenu(menu, model, getResources());
    }

    public static void populateContextMenu(Menu menu, PostItemViewModel model, Resources res) {
        menu.add(Menu.NONE, Constants.CONTEXT_MENU_REPLY_POST, 0, res.getString(R.string.cmenu_reply_post));
        if (!StringUtils.isEmpty(model.getSpannedComment().toString())) {
            menu.add(Menu.NONE, Constants.CONTEXT_MENU_REPLY_POST_QUOTE, 1, res.getString(R.string.cmenu_reply_post_quote));
        }
        if (!StringUtils.isEmpty(model.getSpannedComment())) {
            menu.add(Menu.NONE, Constants.CONTEXT_MENU_COPY_TEXT, 2, res.getString(R.string.cmenu_copy_post));
        }
        if (model.hasAttachment() && model.getAttachment(0).isFile()) {
            menu.add(Menu.NONE, Constants.CONTEXT_MENU_DOWNLOAD_FILE, 3, res.getString(
                    model.getAttachmentsNumber() == 1 ? R.string.cmenu_download_file
                            : R.string.cmenu_download_files));
        }
        if (!StringUtils.isEmpty(model.getSpannedComment())) {
            menu.add(Menu.NONE, Constants.CONTEXT_MENU_SHARE, 4, res.getString(R.string.cmenu_share));
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo menuInfo = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        IPostListEntity adapterItem = mAdapter.getItem(menuInfo.position);
        if (adapterItem instanceof StatusIndicatorEntity) {
            return true;
        }

        PostItemViewModel model = (PostItemViewModel) adapterItem;
        View view = AppearanceUtils.getListItemAtPosition(mListView, menuInfo.position);

        return handleContextMenuItemClick(item, model, getActivity(), view);
    }

    public static boolean handleContextMenuItemClick(MenuItem item, PostItemViewModel model, Activity activity, View view) {
        switch (item.getItemId()) {
            case Constants.CONTEXT_MENU_REPLY_POST:
                navigateToAddReply(activity, model, null);
                break;
            case Constants.CONTEXT_MENU_REPLY_POST_QUOTE:
                navigateToAddReply(activity, model, model.getSpannedComment().toString());
                break;
            case Constants.CONTEXT_MENU_COPY_TEXT:
                PostItemViewBuilder.ViewBag vb = (PostItemViewBuilder.ViewBag) view.getTag();

                if (vb.commentView.isTextSelectable()) {
                    vb.commentView.startSelection();
                } else {
                    CompatibilityUtils.copyText(activity, "#" + model.getNumber(), model.getSpannedComment().toString());

                    AppearanceUtils.showLongToast(activity, activity.getString(R.string.notification_post_copied));
                }
                break;
            case Constants.CONTEXT_MENU_DOWNLOAD_FILE:
                for (int i = 0; i < model.getAttachmentsNumber(); ++i) {
                    AttachmentInfo attachment = model.getAttachment(i);
                    Uri fileUri = Uri.parse(attachment.getSourceUrl());
                    new DownloadFileTask(activity, fileUri).execute();
                }
                break;
            case Constants.CONTEXT_MENU_SHARE:
                Intent shareLinkIntent = new Intent(Intent.ACTION_SEND);
                shareLinkIntent.setType("text/plain");
                shareLinkIntent.putExtra(Intent.EXTRA_SUBJECT, model.getBoardName() + ", post #" + model.getNumber());
                shareLinkIntent.putExtra(Intent.EXTRA_TEXT, model.getSpannedComment().toString());
                activity.startActivity(Intent.createChooser(shareLinkIntent, activity.getString(R.string.share_via)));
                break;
        }

        return true;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putBoolean(Constants.EXTRA_PREFER_DESERIALIZED, true);

        super.onSaveInstanceState(outState);
    }

    private void updateOptionsMenu() {
        if (mMenu == null) {
            return;
        }

        MenuItem favoritesItem = mMenu.findItem(R.id.add_remove_favorites_menu_id);
        if (mFavoritesDatasource.hasFavorites(mWebsite.name(), mBoardName, mThreadNumber)) {
            favoritesItem.setTitle(R.string.menu_remove_favorites);
        } else {
            favoritesItem.setTitle(R.string.menu_add_favorites);
        }
    }

    private void navigateToAddNewPost() {
        Intent addPostIntent = new Intent(getContext(), AddPostActivity.class);
        addPostIntent.putExtra(Constants.EXTRA_WEBSITE, mWebsite.name());
        addPostIntent.putExtra(Constants.EXTRA_BOARD_NAME, mBoardName);
        addPostIntent.putExtra(Constants.EXTRA_THREAD_NUMBER, mThreadNumber);

        startActivityForResult(addPostIntent, Constants.REQUEST_CODE_ADD_POST_ACTIVITY);
    }

    private static void navigateToAddReply(Activity activity, PostItemViewModel model, String postComment) {
        Intent addPostIntent = new Intent(activity.getApplicationContext(), AddPostActivity.class);
        addPostIntent.putExtra(Constants.EXTRA_WEBSITE, model.getWebsite().name());
        addPostIntent.putExtra(Constants.EXTRA_BOARD_NAME, model.getBoardName());
        addPostIntent.putExtra(Constants.EXTRA_THREAD_NUMBER, model.getThreadNumber());
        addPostIntent.putExtra(Constants.EXTRA_POST_NUMBER, model.getNumber());
        if (postComment != null) {
            addPostIntent.putExtra(Constants.EXTRA_POST_COMMENT, postComment);
        }

        activity.startActivityForResult(addPostIntent, Constants.REQUEST_CODE_ADD_POST_ACTIVITY);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case Constants.REQUEST_CODE_ADD_POST_ACTIVITY:
                    refreshPosts(true);
                    break;
            }
        }
    }

    private void refreshPosts(boolean checkModified) {
        if (mCurrentDownloadTask != null) {
            mCurrentDownloadTask.cancel(true);
        }

        if (!mAdapter.isEmpty()) {
            // load new posts
            mCurrentDownloadTask = new DownloadPostsTask(mPostsReaderListener, mBoardName, mThreadNumber, true, mJsonReader, true);
            mCurrentDownloadTask.execute(mAdapter.getLastPostNumber());
        } else {
            mCurrentDownloadTask = new DownloadPostsTask(mPostsReaderListener, mBoardName, mThreadNumber, checkModified, mJsonReader, false);
            mCurrentDownloadTask.execute();
        }
    }

    @Override
    public void onRefresh() {
        refreshPosts(true);
    }

    private class LoadPostsTask extends AsyncTask<Void, Long, PostModel[]> {

        private boolean mPreferDeserialized;

        public LoadPostsTask(boolean preferDeserialized) {
            mPreferDeserialized = preferDeserialized;
        }

        @Override
        protected PostModel[] doInBackground(Void... arg0) {
            // Пробуем десериализовать в любом случае
            return mSerializationService.deserializePosts(mWebsite.name(), mBoardName, mThreadNumber);
        }

        @Override
        public void onPreExecute() {
            mPostsReaderListener.showLoadingScreen();
        }

        @Override
        public void onPostExecute(PostModel[] posts) {
            mPostsReaderListener.hideLoadingScreen();

            if (posts != null) {
                setAdapterData(posts);

                // Обновляем посты, если не был установлен ограничивающий extra
                if (!mPreferDeserialized) {
                    refreshPosts(true);
                }
            } else {
                refreshPosts(false);
            }
        }
    }

    private class PostsReaderListener implements IPostsListView {

        @Override
        public void setWindowProgress(int value) {
            getActivity().getWindow().setFeatureInt(Window.FEATURE_PROGRESS, value);
        }

        @Override
        public void setData(PostModel[] posts) {
            if (posts != null && posts.length > 0) {
                setAdapterData(posts);
                mSerializationService.serializePosts(mWebsite.name(), mBoardName, mThreadNumber, mAdapter.getOriginalPosts());
            } else {
                mAdapter.clear();
                showError(getString(R.string.error_list_empty));
            }
        }

        @Override
        public void showError(String error) {
            if (error != null && error.startsWith("503")) {
                error = "Error 503: it seems like Cloudflare check, open any board first.";
            }
            switchToErrorView(error);
        }

        @Override
        public void showCaptcha(CaptchaEntity captcha) {
            // TODO: replace by captcha view
            showError("Cloudflare captcha, open any board first.");
        }

        @Override
        public void showLoadingScreen() {
            switchToLoadingView();
        }

        @Override
        public void hideLoadingScreen() {
            switchToListView();
            mCurrentDownloadTask = null;
        }

        @Override
        public void updateData(int from, PostModel[] posts) {
            if (posts == null) {
                showToastIfVisible(getResources().getString(R.string.notification_no_new_posts));
                return;
            }

            int addedCount = mAdapter.updateAdapterData(from, posts);
            if (addedCount != 0) {
                mSerializationService.serializePosts(mWebsite.name(), mBoardName, mThreadNumber, mAdapter.getOriginalPosts());
                showToastIfVisible(getResources().getQuantityString(R.plurals.data_new_posts_quantity, addedCount, addedCount));
            } else {
                showToastIfVisible(getResources().getString(R.string.notification_no_new_posts));
            }
        }

        @Override
        public void showUpdateError(String error) {
            showToastIfVisible(error);
        }

        @Override
        public void showUpdateLoading() {
            mAdapter.setUpdating(true);
        }

        @Override
        public void hideUpdateLoading() {

            mAdapter.setUpdating(false);
            mCurrentDownloadTask = null;
            hideRefreshView();
        }
    }
}
