package ua.in.quireg.chan.ui.fragments;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.FileProvider;
import android.support.v4.view.ViewPager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

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
import ua.in.quireg.chan.ui.views.SelectiveViewPager;

public class ImageGalleryFragment extends Fragment implements SelectiveViewPager.OnSingleClickListener {

    private ThreadImagesService mThreadImagesService;
    private CacheDirectoryManager mCacheDirectoryManager;
    private ApplicationSettings mApplicationSettings;

    private String mThreadUri;
    private ThreadImageModel mCurrentImageModel;
    private GalleryItemViewBag mCurrentImageViewBag;
    private boolean mImageLoaded;
    private File mImageLoadedFile;

    private DownloadFileTask mCurrentTask = null;

    private View bottomPanel;
    private Menu mMenu;
    private TextView mImageText;
    private ProgressBar mProgressBar;
    private int mBackgroundColor;
    private View mContainerView;

    private Uri fileToBeShared;

    private boolean isPanelsVisible = true;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        mThreadImagesService = Factory.getContainer().resolve(ThreadImagesService.class);
        mCacheDirectoryManager = Factory.getContainer().resolve(CacheDirectoryManager.class);
        mApplicationSettings = Factory.getContainer().resolve(ApplicationSettings.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (container != null) {
            mContainerView = container.getRootView().findViewById(R.id.base_activity_container);
        }
        return inflater.inflate(R.layout.image_gallery_view, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        bottomPanel = view.findViewById(R.id.image_gallery_bottom_bar);

        String imageUrl = "";

        if (getArguments() != null) {
            imageUrl = getArguments().getString(Constants.EXTRA_IMAGE_URI);
            mThreadUri = getArguments().getString(Constants.EXTRA_THREAD_URL);
        }

        SelectiveViewPager selectiveViewPager = view.findViewById(R.id.view_pager);
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

        mImageText = view.findViewById(R.id.image_gallery_text);
        mProgressBar = view.findViewById(R.id.page_progress_bar);
        mBackgroundColor = AppearanceUtils.getThemeDependentColor(view.getContext().getTheme(), R.styleable.Theme_activityRootBackground);

        // view pager
        final ImageGalleryAdapter adapter = new ImageGalleryAdapter(selectiveViewPager, images);
        selectiveViewPager.setAdapter(adapter);
        adapter.subscribeToPageChangeEvent(selectiveViewPager);
        selectiveViewPager.setCurrentItem(imagePosition);

    }

    @Override
    public void onStart() {
        super.onStart();
        expandContainerView();
    }

    @Override
    public void onStop() {
        super.onStop();
        restoreContainerView();
    }

    @Override
    public void onDestroyView() {
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
        super.onDestroyView();
    }

    @Override
    public void onResume() {
        if (fileToBeShared != null) {
            revokeFileReadPermission(fileToBeShared);
            fileToBeShared = null;
        }
        super.onResume();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.browser, menu);
        mMenu = menu;
        updateOptionsMenu();
    }

    @Override
    public void onSingleClick() {
        updatePanelsVisibility();
    }

    private void expandContainerView() {
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) mContainerView.getLayoutParams();
        layoutParams.removeRule(RelativeLayout.BELOW);
        layoutParams.removeRule(RelativeLayout.ABOVE);
        mContainerView.setBackgroundColor(Color.BLACK);
        mContainerView.setLayoutParams(layoutParams);
    }

    private void restoreContainerView() {
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) mContainerView.getLayoutParams();
        layoutParams.addRule(RelativeLayout.BELOW, R.id.toolbar_layout);
        layoutParams.addRule(RelativeLayout.ABOVE, R.id.bottom_tab_layout);
        mContainerView.setLayoutParams(layoutParams);
        mContainerView.setBackgroundColor(Color.TRANSPARENT);
    }

    private void updatePanelsVisibility() {
        isPanelsVisible = !isPanelsVisible;
    }

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
            boolean isVideoUrl = UriUtils.isVideoUri(Uri.parse(mCurrentImageModel.url));
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
                BrowserLauncher.launchExternalBrowser(getActivity(), mCurrentImageModel.url);
                break;
            case R.id.save_menu_id:
                new DownloadFileTask(getActivity(), Uri.parse(mCurrentImageModel.url)).execute();
                break;
            case R.id.refresh_menu_id:
                if (mCurrentImageViewBag != null) {
                    loadImage(mCurrentImageModel, mCurrentImageViewBag);
                }
                break;
            case R.id.play_video_menu_id:
                File cachedFile = mCacheDirectoryManager
                        .getCachedMediaFileForRead(Uri.parse(mCurrentImageModel.url));
                if (cachedFile.exists()) {
                    BrowserLauncher.playVideoExternal(cachedFile, getActivity());
                }
                break;
            case R.id.share_menu_id:
                if (mImageLoadedFile == null) {
                    break;
                }
                fileToBeShared = FileProvider.getUriForFile(getActivity(),
                        getActivity().getPackageName() + ".fileprovider", mImageLoadedFile);

                Intent shareImageIntent = new Intent(Intent.ACTION_SEND);
                if (UriUtils.isImageUri(fileToBeShared)) {
                    shareImageIntent.setType("image/jpeg");
                } else if (UriUtils.isWebmUri(fileToBeShared)) {
                    shareImageIntent.setType("video/webm");
                } else if (UriUtils.isMP4Uri(fileToBeShared)) {
                    shareImageIntent.setType("video/mp4");
                }
                shareImageIntent.putExtra(Intent.EXTRA_STREAM, fileToBeShared);
                shareImageIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                List<ResolveInfo> resInfoList = getActivity().getPackageManager()
                        .queryIntentActivities(shareImageIntent, PackageManager.MATCH_DEFAULT_ONLY);
                for (ResolveInfo resolveInfo : resInfoList) {
                    String packageName = resolveInfo.activityInfo.packageName;
                    getActivity().grantUriPermission(
                            packageName, fileToBeShared, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                }

                startActivity(Intent.createChooser(shareImageIntent, getString(R.string.share_via)));
                break;
            case R.id.share_link_menu_id:
                Intent shareLinkIntent = new Intent(Intent.ACTION_SEND);
                shareLinkIntent.setType("text/plain");
                shareLinkIntent.putExtra(Intent.EXTRA_SUBJECT, mCurrentImageModel.url);
                shareLinkIntent.putExtra(Intent.EXTRA_TEXT, mCurrentImageModel.url);
                startActivity(Intent.createChooser(shareLinkIntent, getString(R.string.share_via)));
                break;
            case R.id.menu_search_tineye_id:
                String tineyeSearchUrl = "http://www.tineye.com/search?url=" + mCurrentImageModel.url;
                BrowserLauncher.launchExternalBrowser(getActivity(), tineyeSearchUrl);
                break;
            case R.id.menu_search_google_id:
                String googleSearchUrl = "http://www.google.com/searchbyimage?image_url=" + mCurrentImageModel.url;
                BrowserLauncher.launchExternalBrowser(getActivity(), googleSearchUrl);
                break;
            case R.id.menu_image_operations_id:
                String imageOpsUrl = "http://imgops.com/" + mCurrentImageModel.url;
                BrowserLauncher.launchExternalBrowser(getActivity(), imageOpsUrl);
                break;
        }

        return true;
    }

    public void revokeFileReadPermission(Uri fileBeenShared) {
        getActivity().revokeUriPermission(fileBeenShared, Intent.FLAG_GRANT_READ_URI_PERMISSION);
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
        Uri uri = Uri.parse(model.url);

        if (UriUtils.isVideoUri(uri)
                && mApplicationSettings.getVideoPlayer() == Constants.VIDEO_PLAYER_EXTERNAL_2CLICK) {
            if (model.attachment != null) {
                setThumbnail(model.attachment, viewBag);
                mImageLoaded = true;
            } else {
                viewBag.switchToErrorView(getString(R.string.error_video_playing));
            }
            return;
        }
        File cachedFile = mCacheDirectoryManager.getCachedMediaFileForRead(uri);
        if (cachedFile.exists()) {
            // show from cache
            setProgressComp(Window.PROGRESS_END);
            setImage(cachedFile, viewBag);
        } else {
            // download image and cache it
            File writeCachedFile = mCacheDirectoryManager.getCachedMediaFileForWrite(uri);
            mCurrentTask = new DownloadFileTask(getActivity(), uri, writeCachedFile, new ImageDownloadView(viewBag), false);
            mCurrentTask.execute();
        }
        updateOptionsMenu();
    }

    private void setThumbnail(final AttachmentInfo attachment, GalleryItemViewBag viewBag) {
        //Set thumbnailSize to stretch across the screen.
        int thumbnailSize = FrameLayout.LayoutParams.MATCH_PARENT;
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(thumbnailSize, thumbnailSize, Gravity.CENTER);

        ImageView thumbnailView = new ImageView(getActivity());
        thumbnailView.setLayoutParams(layoutParams);
        thumbnailView.setBackgroundColor(mBackgroundColor);
        viewBag.layout.removeAllViews();
        viewBag.layout.addView(thumbnailView);
        thumbnailView.setOnClickListener(v -> ThreadPostUtils.openExternalAttachment(attachment, getActivity()));

        String thumbnailUrl = attachment.getThumbnailUrl();
        if (thumbnailUrl != null) {
            BitmapManager bitmapManager = Factory.resolve(BitmapManager.class);
            bitmapManager.fetchBitmapOnThread(Uri.parse(thumbnailUrl), thumbnailView, true,
                    null, R.drawable.doge);
        } else {
            if (attachment.isFile()) {
                thumbnailView.setImageResource(attachment.getDefaultThumbnail());
            } else {
                thumbnailView.setImageResource(R.drawable.doge);
            }
        }

        AppearanceUtils.showLongToast(getActivity(), getResources().getString(R.string.notification_video));
    }

    private void setImage(File file, GalleryItemViewBag viewBag) {
        if (UriUtils.isImageUri(Uri.fromFile(file))) {
            AppearanceUtils.setImage(file, getActivity(), viewBag.layout, mBackgroundColor);
        } else if (UriUtils.isVideoUri(Uri.fromFile(file))) {
            AppearanceUtils.setVideoFile(
                    file, getActivity(), viewBag, mBackgroundColor, getActivity().getTheme());
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

        private final ViewPager mViewPager;

        ImageGalleryAdapter(ViewPager viewPager, ArrayList<ThreadImageModel> images) {
            super(viewPager.getContext(), images);
            mViewPager = viewPager;
        }

        @Override
        protected View createView(int position) {
            View view = LayoutInflater.from(mViewPager.getContext())
                    .inflate(R.layout.image_gallery_item, mViewPager, false);
            GalleryItemViewBag vb = new GalleryItemViewBag();
            vb.layout = view.findViewById(R.id.image_layout);
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

            String infoString = (position + 1) + "/" + getCount() + " (" + imageModel.size + getResources().getString(R.string.data_file_size_measure) + ")";
            mImageText.setText(infoString);
        }
    }

    private class ImageDownloadView implements IDownloadFileView {

        private final GalleryItemViewBag mViewBag;
        private double mMaxValue = -1;

        ImageDownloadView(GalleryItemViewBag viewBag) {
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
        public void setOnCancelListener(DialogInterface.OnCancelListener listener) {
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
