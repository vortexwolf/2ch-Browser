package ua.in.quireg.chan.ui.activities;

import android.content.Context;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import ua.in.quireg.chan.R;
import ua.in.quireg.chan.asynctasks.DownloadFileTask;
import ua.in.quireg.chan.common.Constants;
import ua.in.quireg.chan.common.Factory;
import ua.in.quireg.chan.common.library.ExtendedPagerAdapter;
import ua.in.quireg.chan.common.utils.AppearanceUtils;
import ua.in.quireg.chan.common.utils.ThreadPostUtils;
import ua.in.quireg.chan.common.utils.UriUtils;
import ua.in.quireg.chan.interfaces.IDownloadFileView;
import ua.in.quireg.chan.models.presentation.AttachmentInfo;
import ua.in.quireg.chan.models.presentation.GalleryItemViewBag;
import ua.in.quireg.chan.models.presentation.ThreadImageModel;
import ua.in.quireg.chan.services.BitmapManager;
import ua.in.quireg.chan.services.BrowserLauncher;
import ua.in.quireg.chan.services.CacheDirectoryManager;
import ua.in.quireg.chan.services.ThreadImagesService;
import ua.in.quireg.chan.settings.ApplicationSettings;
import ua.in.quireg.chan.ui.controls.SelectiveViewPager;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ImageGalleryActivity extends AppCompatActivity implements SelectiveViewPager.OnSingleClickListener {
    public static final String LOG_TAG = ImageGalleryActivity.class.getSimpleName();

    private ThreadImagesService mThreadImagesService;
    private CacheDirectoryManager mCacheDirectoryManager;
    private ApplicationSettings mApplicationSettings;

    private String mThreadUri;
    private ThreadImageModel mCurrentImageModel;
    private GalleryItemViewBag mCurrentImageViewBag;
    private boolean mImageLoaded;
    private File mImageLoadedFile;

    private DownloadFileTask mCurrentTask = null;

    private AppBarLayout appBarLayout;
    private Toolbar toolbar;
    private View bottomPanel;
    private Menu mMenu;
    private TextView mImageText;
    private ProgressBar mProgressBar;
    private int mBackgroundColor;

    private Uri fileToBeShared;

    //private boolean isPanelsVisible = true;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mThreadImagesService = Factory.getContainer().resolve(ThreadImagesService.class);
        mCacheDirectoryManager = Factory.getContainer().resolve(CacheDirectoryManager.class);
        mApplicationSettings = Factory.getContainer().resolve(ApplicationSettings.class);

        setTheme(mApplicationSettings.getTheme());
        setContentView(R.layout.image_gallery_view);
        //getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        bottomPanel = findViewById(R.id.image_gallery_bottom_bar);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        String imageUrl = getIntent().getData().toString();
        mThreadUri = getIntent().getStringExtra(Constants.EXTRA_THREAD_URL);

        SelectiveViewPager selectiveViewPager = (SelectiveViewPager) findViewById(R.id.view_pager);
        selectiveViewPager.setSingleClickListener(this);


        // get the current image
        ArrayList<ThreadImageModel> images = mThreadImagesService.getImagesList(mThreadUri);
        if (images.size() == 0) {
            // it happens if the activity was killed because of low memory and then reloaded with empty data
            ThreadImageModel singleImage = new ThreadImageModel();
            singleImage.url = imageUrl;
            images.add(singleImage);
        }
        ThreadImageModel currentImage = mThreadImagesService.getImageByUrl(images, imageUrl);
        int imagePosition = images.indexOf(currentImage);

        mImageText = (TextView) findViewById(R.id.image_gallery_text);
        mProgressBar = (ProgressBar) findViewById(R.id.page_progress_bar);
        mBackgroundColor = AppearanceUtils.getThemeColor(getTheme(), R.styleable.Theme_activityRootBackground);

        // view pager
        final ImageGalleryAdapter adapter = new ImageGalleryAdapter(this, images);
        selectiveViewPager.setAdapter(adapter);
        adapter.subscribeToPageChangeEvent(selectiveViewPager);
        selectiveViewPager.setCurrentItem(imagePosition);


//        ImageButton prevImageButton = (ImageButton) findViewById(R.id.image_gallery_prev);
//        prevImageButton.setOnClickListener(new OnClickListener() {
//            @Override
//            public void onClick(View arg0) {
//                //selectiveViewPager.movePrevious();
//            }
//        });
//
//        ImageButton nextImageButton = (ImageButton) findViewById(R.id.image_gallery_next);
//        nextImageButton.setOnClickListener(new OnClickListener() {
//            @Override
//            public void onClick(View arg0) {
//                //selectiveViewPager.moveNext();
//            }
//        });
    }

    @Override
    protected void onDestroy() {
        if (mCurrentTask != null) {
            mCurrentTask.cancel(true);
        }
        if (mCurrentImageViewBag != null) {
            mCurrentImageViewBag.clear();
        }
        if (fileToBeShared != null) {
            revokeFileReadPermission(fileToBeShared);
            fileToBeShared = null;
        }

        super.onDestroy();
    }

    @Override
    protected void onResume() {
        if (fileToBeShared != null) {
            revokeFileReadPermission(fileToBeShared);
            fileToBeShared = null;
        }
        super.onResume();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.clear();
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.browser, menu);

        mMenu = menu;
        updateOptionsMenu();

        return true;
    }
    @Override
    public void onSingleClick() {
//        updatePanelsVisibility();
    }

//    private void updatePanelsVisibility(){
//
//        float toolbar_statusbar_height = getResources().getDimension(R.dimen.status_bar_height) / getResources().getDisplayMetrics().density;
//
//
//        if(isPanelsVisible){
//
//
//            toolbar.animate()
//                    .translationY(toolbar_statusbar_height)
//                    .setInterpolator(new AccelerateDecelerateInterpolator())
//                    .setDuration(500)
//                    .start();
//
//        }else{
//
//            toolbar.animate()
//                    .translationY(150)
//                    .setInterpolator(new AccelerateDecelerateInterpolator())
//                    .setDuration(500)
//                    .start();
//            bottomPanel.animate()
//                    .translationY(-150)
//                    .setInterpolator(new AccelerateDecelerateInterpolator())
//                    .setDuration(500)
//                    .start();
//            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
//
//        }
//
//        isPanelsVisible = !isPanelsVisible;
//
//    }

    private void updateOptionsMenu() {
        if (mMenu == null) {
            return;
        }

        MenuItem saveMenuItem = mMenu.findItem(R.id.save_menu_id);
        MenuItem shareMenuItem = mMenu.findItem(R.id.share_menu_id);
        MenuItem refreshMenuItem = mMenu.findItem(R.id.refresh_menu_id);
        MenuItem playVideoMenuItem = mMenu.findItem(R.id.play_video_menu_id);
        MenuItem searchTineyeMenuItem = mMenu.findItem(R.id.menu_search_tineye_id);
        MenuItem searchGoogleMenuItem = mMenu.findItem(R.id.menu_search_google_id);
        MenuItem imageOpsMenuItem = mMenu.findItem(R.id.menu_image_operations_id);

        saveMenuItem.setVisible(mImageLoaded);
        shareMenuItem.setVisible(mImageLoaded);
        refreshMenuItem.setVisible(!mImageLoaded);
        if (mCurrentImageModel != null) {
            boolean isImageUrl = UriUtils.isImageUri(Uri.parse(mCurrentImageModel.url));
            boolean isVideoUrl = UriUtils.isWebmUri(Uri.parse(mCurrentImageModel.url));
            playVideoMenuItem.setVisible(mImageLoaded && isVideoUrl &&
                    !(mApplicationSettings.getVideoPlayer() == Constants.VIDEO_PLAYER_EXTERNAL_2CLICK));
            searchTineyeMenuItem.setVisible(isImageUrl);
            searchGoogleMenuItem.setVisible(isImageUrl);
            imageOpsMenuItem.setVisible(isImageUrl);
        } else {
            playVideoMenuItem.setVisible(false);
            searchTineyeMenuItem.setVisible(false);
            searchGoogleMenuItem.setVisible(false);
            imageOpsMenuItem.setVisible(false);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.open_browser_menu_id:
                BrowserLauncher.launchExternalBrowser(this, mCurrentImageModel.url.toString());
                break;
            case R.id.save_menu_id:
                new DownloadFileTask(this, Uri.parse(mCurrentImageModel.url)).execute();
                break;
            case R.id.refresh_menu_id:
                if (mCurrentImageViewBag != null) {
                    loadImage(mCurrentImageModel, mCurrentImageViewBag);
                }
                break;
            case R.id.play_video_menu_id:
                File cachedFile = mCacheDirectoryManager.getCachedMediaFileForRead(Uri.parse(mCurrentImageModel.url));
                if (cachedFile.exists()) {
                    BrowserLauncher.playVideoExternal(cachedFile, this);
                }
                break;
            case R.id.share_menu_id:
                if (mImageLoadedFile == null) {
                    break;
                }
                fileToBeShared = FileProvider.getUriForFile(getApplicationContext(), getApplicationContext().getPackageName() + ".fileprovider", mImageLoadedFile);

                Intent shareImageIntent = new Intent(Intent.ACTION_SEND);
                if (UriUtils.isImageUri(fileToBeShared)) {
                    shareImageIntent.setType("image/jpeg");
                } else if (UriUtils.isWebmUri(fileToBeShared)) {
                    shareImageIntent.setType("video/webm");
                }
                shareImageIntent.putExtra(Intent.EXTRA_STREAM, fileToBeShared);
                shareImageIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                List<ResolveInfo> resInfoList = getApplicationContext().getPackageManager().queryIntentActivities(shareImageIntent, PackageManager.MATCH_DEFAULT_ONLY);
                for (ResolveInfo resolveInfo : resInfoList) {
                    String packageName = resolveInfo.activityInfo.packageName;
                    getApplicationContext().grantUriPermission(packageName, fileToBeShared, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                }

                startActivity(Intent.createChooser(shareImageIntent, getString(R.string.share_via)));
                break;
            case R.id.share_link_menu_id:
                Intent shareLinkIntent = new Intent(Intent.ACTION_SEND);
                shareLinkIntent.setType("text/plain");
                shareLinkIntent.putExtra(Intent.EXTRA_SUBJECT, mCurrentImageModel.url.toString());
                shareLinkIntent.putExtra(Intent.EXTRA_TEXT, mCurrentImageModel.url.toString());
                startActivity(Intent.createChooser(shareLinkIntent, getString(R.string.share_via)));
                break;
            case R.id.menu_search_tineye_id:
                String tineyeSearchUrl = "http://www.tineye.com/search?url=" + mCurrentImageModel.url;
                BrowserLauncher.launchExternalBrowser(getApplicationContext(), tineyeSearchUrl);
                break;
            case R.id.menu_search_google_id:
                String googleSearchUrl = "http://www.google.com/searchbyimage?image_url=" + mCurrentImageModel.url;
                BrowserLauncher.launchExternalBrowser(getApplicationContext(), googleSearchUrl);
                break;
            case R.id.menu_image_operations_id:
                String imageOpsUrl = "http://imgops.com/" + mCurrentImageModel.url;
                BrowserLauncher.launchExternalBrowser(getApplicationContext(), imageOpsUrl);
                break;
        }

        return true;
    }

    public void revokeFileReadPermission(Uri fileBeenShared) {
        getApplicationContext().revokeUriPermission(fileBeenShared, Intent.FLAG_GRANT_READ_URI_PERMISSION);
    }



    private void loadImage(ThreadImageModel model, GalleryItemViewBag viewBag) {
        mImageLoaded = false;
        mImageLoadedFile = null;
        mCurrentImageModel = model;
        mCurrentImageViewBag = viewBag;

        if (mCurrentTask != null) {
            // only 1 image per time
            mCurrentTask.cancel(true);
        }

        if (UriUtils.isWebmUri(Uri.parse(model.url)) && mApplicationSettings.getVideoPlayer() == Constants.VIDEO_PLAYER_EXTERNAL_2CLICK) {
            if (model.attachment != null) {
                setThumbnail(model.attachment, viewBag);
                mImageLoaded = true;
            } else {
                viewBag.switchToErrorView(getString(R.string.error_video_playing));
            }
            return;
        }

        Uri uri = Uri.parse(model.url);

        File cachedFile = mCacheDirectoryManager.getCachedMediaFileForRead(uri);
        if (cachedFile.exists()) {
            // show from cache
            setProgressComp(Window.PROGRESS_END);
            setImage(cachedFile, viewBag);
        } else {
            // download image and cache it
            File writeCachedFile = mCacheDirectoryManager.getCachedMediaFileForWrite(uri);
            mCurrentTask = new DownloadFileTask(this, uri, writeCachedFile, new ImageDownloadView(viewBag), false);
            mCurrentTask.execute();
        }
        updateOptionsMenu();
    }

    private void setThumbnail(final AttachmentInfo attachment, GalleryItemViewBag viewBag) {
        //Set thumbnailSize to stretch across the screen.
        int thumbnailSize = FrameLayout.LayoutParams.MATCH_PARENT;
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(thumbnailSize, thumbnailSize, Gravity.CENTER);

        ImageView thumbnailView = new ImageView(this);
        thumbnailView.setLayoutParams(layoutParams);
        thumbnailView.setBackgroundColor(mBackgroundColor);
        viewBag.layout.removeAllViews();
        viewBag.layout.addView(thumbnailView);
        thumbnailView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                ThreadPostUtils.openExternalAttachment(attachment, ImageGalleryActivity.this);
            }
        });

        String thumbnailUrl = attachment.getThumbnailUrl();
        if (thumbnailUrl != null) {
            BitmapManager bitmapManager = Factory.resolve(BitmapManager.class);
            bitmapManager.fetchBitmapOnThread(Uri.parse(thumbnailUrl), thumbnailView, true, null, R.drawable.error_image);
        } else {
            if (attachment.isFile()) {
                thumbnailView.setImageResource(attachment.getDefaultThumbnail());
            } else {
                thumbnailView.setImageResource(R.drawable.error_image);
            }
        }

        AppearanceUtils.showToastMessage(this, getResources().getString(R.string.notification_video));
    }

    private void setImage(File file, GalleryItemViewBag viewBag) {
        if (UriUtils.isImageUri(Uri.fromFile(file))) {
            AppearanceUtils.setImage(file, this, viewBag.layout, mBackgroundColor);
        } else if (UriUtils.isWebmUri(Uri.fromFile(file))) {
            AppearanceUtils.setVideoFile(file, this, viewBag, mBackgroundColor, getTheme());
        }

        mImageLoaded = true;
        mImageLoadedFile = file;
        updateOptionsMenu();
    }

    private void setProgressComp(int progress) {
//        if (Constants.SDK_VERSION < 21) {
//            setProgress(progress);
//        } else if (progress == Window.PROGRESS_INDETERMINATE_ON) {
//            mProgressBar.setVisibility(View.VISIBLE);
//            mProgressBar.setIndeterminate(true);
//        } else if (progress > 0 && progress < Window.PROGRESS_END) {
//            mProgressBar.setVisibility(View.VISIBLE);
//            mProgressBar.setIndeterminate(false);
//            mProgressBar.setProgress(progress / 100);
//        } else {
//            mProgressBar.setVisibility(View.GONE);
//        }
    }


    private class ImageGalleryAdapter extends ExtendedPagerAdapter<ThreadImageModel> {
        private final LayoutInflater mInflater;

        public ImageGalleryAdapter(Context context, ArrayList<ThreadImageModel> images) {
            super(context, images);
            mInflater = LayoutInflater.from(context);
        }

        @Override
        protected View createView(int position) {
            View view = mInflater.inflate(R.layout.image_gallery_item, null);

            GalleryItemViewBag vb = new GalleryItemViewBag();
            vb.layout = (FrameLayout) view.findViewById(R.id.image_layout);
            vb.loading = view.findViewById(R.id.loading);
            vb.error = view.findViewById(R.id.error);
            view.setTag(vb);

            return view;
        }

        @Override
        public void onViewUnselected(int position, View view) {
            GalleryItemViewBag vb = (GalleryItemViewBag) view.getTag();
            vb.clear();
        }

        @Override
        public void onViewSelected(int position, View view) {
            ThreadImageModel imageModel = getItem(position);
            GalleryItemViewBag vb = (GalleryItemViewBag) view.getTag();
            loadImage(imageModel, vb);

            mImageText.setText((position + 1) + "/" + getCount() + " (" + imageModel.size + getResources().getString(R.string.data_file_size_measure) + ")");
        }
    }

    private class ImageDownloadView implements IDownloadFileView {
        private final GalleryItemViewBag mViewBag;
        private double mMaxValue = -1;

        public ImageDownloadView(GalleryItemViewBag viewBag) {
            mViewBag = viewBag;
        }

        @Override
        public void setCurrentProgress(int value) {
            if (mMaxValue > 0) {
                double percent = value / mMaxValue;
                setProgressComp((int) (percent * Window.PROGRESS_END)); // from 0 to 10000
            } else {
                setProgressComp(Window.PROGRESS_INDETERMINATE_ON);
            }
        }

        @Override
        public void setMaxProgress(int value) {
            mMaxValue = value;
        }

        @Override
        public void showLoading(String message) {
            mViewBag.layout.setVisibility(View.GONE);
            mViewBag.loading.setVisibility(View.VISIBLE);
            mViewBag.error.setVisibility(View.GONE);
        }

        @Override
        public void hideLoading() {
            setProgressComp(Window.PROGRESS_END);
            mViewBag.layout.setVisibility(View.VISIBLE);
            mViewBag.loading.setVisibility(View.GONE);
            mViewBag.error.setVisibility(View.GONE);
        }

        @Override
        public void setOnCancelListener(OnCancelListener listener) {
        }

        @Override
        public void showSuccess(File file) {
            setImage(file, mViewBag);
        }

        @Override
        public void showError(String error) {
            mViewBag.switchToErrorView(
                    error != null
                            ? error
                            : getString(R.string.error_unknown));
        }

        @Override
        public void showFileExists(File file) {
            setImage(file, mViewBag);
        }
    }
}
