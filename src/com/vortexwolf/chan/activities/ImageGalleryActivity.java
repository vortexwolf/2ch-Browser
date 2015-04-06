package com.vortexwolf.chan.activities;

import java.io.File;
import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.vortexwolf.chan.R;
import com.vortexwolf.chan.asynctasks.DownloadFileTask;
import com.vortexwolf.chan.common.Constants;
import com.vortexwolf.chan.common.Factory;
import com.vortexwolf.chan.common.Websites;
import com.vortexwolf.chan.common.controls.ExtendedViewPager;
import com.vortexwolf.chan.common.library.ExtendedPagerAdapter;
import com.vortexwolf.chan.common.utils.AppearanceUtils;
import com.vortexwolf.chan.common.utils.ThreadPostUtils;
import com.vortexwolf.chan.common.utils.UriUtils;
import com.vortexwolf.chan.interfaces.IDownloadFileView;
import com.vortexwolf.chan.interfaces.IWebsite;
import com.vortexwolf.chan.models.presentation.AttachmentInfo;
import com.vortexwolf.chan.models.presentation.GalleryItemViewBag;
import com.vortexwolf.chan.models.presentation.ThreadImageModel;
import com.vortexwolf.chan.services.BitmapManager;
import com.vortexwolf.chan.services.BrowserLauncher;
import com.vortexwolf.chan.services.CacheDirectoryManager;
import com.vortexwolf.chan.services.MyTracker;
import com.vortexwolf.chan.services.ThreadImagesService;
import com.vortexwolf.chan.settings.ApplicationSettings;

public class ImageGalleryActivity extends Activity {
    public static final String TAG = "ImageGalleryActivity";

    private ThreadImagesService mThreadImagesService;
    private CacheDirectoryManager mCacheDirectoryManager;
    private ApplicationSettings mApplicationSettings;

    private String mThreadUri;
    private ThreadImageModel mCurrentImageModel;
    private GalleryItemViewBag mCurrentImageViewBag;
    private boolean mImageLoaded;
    private File mImageLoadedFile;

    private DownloadFileTask mCurrentTask = null;

    private Menu mMenu;
    private TextView mImageText;
    private int mBackgroundColor;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.requestWindowFeature(Window.FEATURE_PROGRESS);

        this.mThreadImagesService = Factory.getContainer().resolve(ThreadImagesService.class);
        this.mCacheDirectoryManager = Factory.getContainer().resolve(CacheDirectoryManager.class);
        this.mApplicationSettings = Factory.getContainer().resolve(ApplicationSettings.class);

        String imageUrl = this.getIntent().getData().toString();
        this.mThreadUri = this.getIntent().getStringExtra(Constants.EXTRA_THREAD_URL);

        // get the current image
        ArrayList<ThreadImageModel> images = this.mThreadImagesService.getImagesList(this.mThreadUri);
        if (images.size() == 0) {
            // it happens if the activity was killed because of low memory and then reloaded with empty data
            ThreadImageModel singleImage = new ThreadImageModel();
            singleImage.url = imageUrl;
            images.add(singleImage);
        }
        ThreadImageModel currentImage = this.mThreadImagesService.getImageByUrl(images, imageUrl);
        int imagePosition = images.indexOf(currentImage);

        this.setTheme(this.mApplicationSettings.getTheme());
        this.setContentView(R.layout.image_gallery_view);
        this.mImageText = (TextView) this.findViewById(R.id.image_gallery_text);
        this.mBackgroundColor = AppearanceUtils.getThemeColor(this.getTheme(), R.styleable.Theme_activityRootBackground);

        // view pager
        final ImageGalleryAdapter adapter = new ImageGalleryAdapter(this, images);
        final ExtendedViewPager viewPager = (ExtendedViewPager) this.findViewById(R.id.view_pager);
        viewPager.setAdapter(adapter);
        adapter.subscribeToPageChangeEvent(viewPager);
        viewPager.setCurrentItem(imagePosition);

        ImageButton prevImageButton = (ImageButton) this.findViewById(R.id.image_gallery_prev);
        prevImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                viewPager.movePrevious();
            }
        });

        ImageButton nextImageButton = (ImageButton) this.findViewById(R.id.image_gallery_next);
        nextImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                viewPager.moveNext();
            }
        });

        IWebsite website = Websites.fromUri(Uri.parse(imageUrl));
        if (website != null) {
            Factory.resolve(MyTracker.class).setBoardVar(website.getUrlParser().getBoardName(Uri.parse(imageUrl)));
        } else {
            Factory.resolve(MyTracker.class).setBoardVar("");
        }
        Factory.resolve(MyTracker.class).trackActivityView(TAG);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = this.getMenuInflater();
        inflater.inflate(R.menu.browser, menu);

        this.mMenu = menu;
        this.updateOptionsMenu();

        return true;
    }

    private void updateOptionsMenu() {
        if (this.mMenu == null) {
            return;
        }

        MenuItem saveMenuItem = this.mMenu.findItem(R.id.save_menu_id);
        MenuItem shareMenuItem = this.mMenu.findItem(R.id.share_menu_id);
        MenuItem refreshMenuItem = this.mMenu.findItem(R.id.refresh_menu_id);
        MenuItem playVideoMenuItem = this.mMenu.findItem(R.id.play_video_menu_id);
        MenuItem searchTineyeMenuItem = this.mMenu.findItem(R.id.menu_search_tineye_id);
        MenuItem searchGoogleMenuItem = this.mMenu.findItem(R.id.menu_search_google_id);
        MenuItem imageOpsMenuItem = this.mMenu.findItem(R.id.menu_image_operations_id);

        saveMenuItem.setVisible(this.mImageLoaded);
        shareMenuItem.setVisible(this.mImageLoaded);
        refreshMenuItem.setVisible(!this.mImageLoaded);
        if (this.mCurrentImageModel != null) {
            boolean isImageUrl = UriUtils.isImageUri(Uri.parse(this.mCurrentImageModel.url));
            boolean isVideoUrl = UriUtils.isWebmUri(Uri.parse(this.mCurrentImageModel.url));
            playVideoMenuItem.setVisible(this.mImageLoaded && isVideoUrl);
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
                BrowserLauncher.launchExternalBrowser(this, this.mCurrentImageModel.url.toString());
                break;
            case R.id.save_menu_id:
                new DownloadFileTask(this, Uri.parse(this.mCurrentImageModel.url)).execute();
                break;
            case R.id.refresh_menu_id:
                if (this.mCurrentImageViewBag != null) {
                    this.loadImage(this.mCurrentImageModel, this.mCurrentImageViewBag);
                }
                break;
            case R.id.play_video_menu_id:
                File cachedFile = this.mCacheDirectoryManager.getCachedImageFileForRead(Uri.parse(this.mCurrentImageModel.url));
                if (cachedFile.exists()) {
                    BrowserLauncher.playVideoExternal(cachedFile, this);
                }
                break;
            case R.id.share_menu_id:
                if (this.mImageLoadedFile == null) {
                    break;
                }
                Uri fileUri = Uri.fromFile(this.mImageLoadedFile);

                Intent shareImageIntent = new Intent(Intent.ACTION_SEND);
                if (UriUtils.isImageUri(fileUri)) {
                    shareImageIntent.setType("image/jpeg");
                } else if (UriUtils.isWebmUri(fileUri)) {
                    shareImageIntent.setType("video/webm");
                }
                shareImageIntent.putExtra(Intent.EXTRA_STREAM, fileUri);
                this.startActivity(Intent.createChooser(shareImageIntent, this.getString(R.string.share_via)));
                break;
            case R.id.share_link_menu_id:
                Intent shareLinkIntent = new Intent(Intent.ACTION_SEND);
                shareLinkIntent.setType("text/plain");
                shareLinkIntent.putExtra(Intent.EXTRA_SUBJECT, this.mCurrentImageModel.url.toString());
                shareLinkIntent.putExtra(Intent.EXTRA_TEXT, this.mCurrentImageModel.url.toString());
                this.startActivity(Intent.createChooser(shareLinkIntent, this.getString(R.string.share_via)));
                break;
            case R.id.menu_search_tineye_id:
                String tineyeSearchUrl = "http://www.tineye.com/search?url=" + this.mCurrentImageModel.url;
                BrowserLauncher.launchExternalBrowser(this.getApplicationContext(), tineyeSearchUrl);
                break;
            case R.id.menu_search_google_id:
                String googleSearchUrl = "http://www.google.com/searchbyimage?image_url=" + this.mCurrentImageModel.url;
                BrowserLauncher.launchExternalBrowser(this.getApplicationContext(), googleSearchUrl);
                break;
            case R.id.menu_image_operations_id:
                String imageOpsUrl = "http://imgops.com/" + this.mCurrentImageModel.url;
                BrowserLauncher.launchExternalBrowser(this.getApplicationContext(), imageOpsUrl);
                break;
        }

        return true;
    }

    private void loadImage(ThreadImageModel model, GalleryItemViewBag viewBag) {
        if (UriUtils.isWebmUri(Uri.parse(model.url)) && this.mApplicationSettings.isExternalVideoPlayer()) {
            if (model.attachment != null) {
                this.setThumbnail(model.attachment, viewBag);
            } else {
                viewBag.switchToErrorView(this.getString(R.string.error_video_playing));
            }
            return;
        }
        if (this.mCurrentTask != null) {
            // only 1 image per time
            this.mCurrentTask.cancel(true);
        }

        this.mImageLoaded = false;
        this.mImageLoadedFile = null;
        this.mCurrentImageModel = model;
        this.mCurrentImageViewBag = viewBag;
        this.updateOptionsMenu();

        Uri uri = Uri.parse(model.url);

        File cachedFile = this.mCacheDirectoryManager.getCachedImageFileForRead(uri);
        if (cachedFile.exists()) {
            // show from cache
            this.setProgress(Window.PROGRESS_END);
            this.setImage(cachedFile, viewBag);
        } else {
            // download image and cache it
            File writeCachedFile = this.mCacheDirectoryManager.getCachedImageFileForWrite(uri);
            this.mCurrentTask = new DownloadFileTask(this, uri, writeCachedFile, new ImageDownloadView(viewBag), false);
            this.mCurrentTask.execute();
        }
    }

    private void setThumbnail(final AttachmentInfo attachment, GalleryItemViewBag viewBag) {
        int thumbnailSize = (int) getResources().getDimension(R.dimen.thumbnail_size);
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(thumbnailSize, thumbnailSize, Gravity.CENTER);

        ImageView thumbnailView = new ImageView(this);
        thumbnailView.setLayoutParams(layoutParams);
        thumbnailView.setBackgroundColor(ImageGalleryActivity.this.mBackgroundColor);
        viewBag.layout.removeAllViews();
        viewBag.layout.addView(thumbnailView);
        thumbnailView.setOnClickListener(new OnClickListener(){
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

        AppearanceUtils.showToastMessage(this, getResources().getString(R.string.notification_video_thumbnail));
    }

    private void setImage(File file, GalleryItemViewBag viewBag) {
        if (UriUtils.isImageUri(Uri.fromFile(file))) {
            AppearanceUtils.setImage(file, this, viewBag.layout, ImageGalleryActivity.this.mBackgroundColor);
        } else if (UriUtils.isWebmUri(Uri.fromFile(file))) {
            AppearanceUtils.setVideoFile(file, this, viewBag);
        }

        this.mImageLoaded = true;
        this.mImageLoadedFile = file;
        this.updateOptionsMenu();
    }



    private class ImageGalleryAdapter extends ExtendedPagerAdapter<ThreadImageModel> {
        private final LayoutInflater mInflater;

        public ImageGalleryAdapter(Context context, ArrayList<ThreadImageModel> images) {
            super(context, images);
            this.mInflater = LayoutInflater.from(context);
        }

        @Override
        protected View createView(int position) {
            View view = this.mInflater.inflate(R.layout.image_gallery_item, null);

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
            ThreadImageModel imageModel = this.getItem(position);
            GalleryItemViewBag vb = (GalleryItemViewBag) view.getTag();
            ImageGalleryActivity.this.loadImage(imageModel, vb);

            ImageGalleryActivity.this.mImageText.setText((position + 1) + "/" + this.getCount() + " (" + imageModel.size + ImageGalleryActivity.this.getResources().getString(R.string.data_file_size_measure) + ")");
        }
    }

    private class ImageDownloadView implements IDownloadFileView {
        private final GalleryItemViewBag mViewBag;
        private double mMaxValue = -1;

        public ImageDownloadView(GalleryItemViewBag viewBag) {
            this.mViewBag = viewBag;
        }

        @Override
        public void setCurrentProgress(int value) {
            if (this.mMaxValue > 0) {
                double percent = value / this.mMaxValue;
                ImageGalleryActivity.this.setProgress((int) (percent * Window.PROGRESS_END)); // from 0 to 10000
            } else {
                ImageGalleryActivity.this.setProgress(Window.PROGRESS_INDETERMINATE_ON);
            }
        }

        @Override
        public void setMaxProgress(int value) {
            this.mMaxValue = value;
        }

        @Override
        public void showLoading(String message) {
            this.mViewBag.layout.setVisibility(View.GONE);
            this.mViewBag.loading.setVisibility(View.VISIBLE);
            this.mViewBag.error.setVisibility(View.GONE);
        }

        @Override
        public void hideLoading() {
            ImageGalleryActivity.this.setProgress(Window.PROGRESS_END);
            this.mViewBag.layout.setVisibility(View.VISIBLE);
            this.mViewBag.loading.setVisibility(View.GONE);
            this.mViewBag.error.setVisibility(View.GONE);
        }

        @Override
        public void setOnCancelListener(OnCancelListener listener) {
        }

        @Override
        public void showSuccess(File file) {
            ImageGalleryActivity.this.setImage(file, this.mViewBag);
        }

        @Override
        public void showError(String error) {
            this.mViewBag.switchToErrorView(
                error != null
                ? error
                : ImageGalleryActivity.this.getString(R.string.error_unknown));
        }

        @Override
        public void showFileExists(File file) {
            ImageGalleryActivity.this.setImage(file, this.mViewBag);
        }
    }
}
