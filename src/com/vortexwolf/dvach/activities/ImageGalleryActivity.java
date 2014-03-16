package com.vortexwolf.dvach.activities;

import java.io.File;
import java.util.ArrayList;

import uk.co.senab.photoview.PhotoView;

import com.vortexwolf.chan.R;
import com.vortexwolf.dvach.asynctasks.DownloadFileTask;
import com.vortexwolf.dvach.common.Constants;
import com.vortexwolf.dvach.common.Factory;
import com.vortexwolf.dvach.common.controls.EllipsizingTextView;
import com.vortexwolf.dvach.common.controls.HackyViewPager;
import com.vortexwolf.dvach.common.library.MyLog;
import com.vortexwolf.dvach.common.utils.AppearanceUtils;
import com.vortexwolf.dvach.common.utils.UriUtils;
import com.vortexwolf.dvach.interfaces.ICacheDirectoryManager;
import com.vortexwolf.dvach.interfaces.IDownloadFileView;
import com.vortexwolf.dvach.models.presentation.ImageFileModel;
import com.vortexwolf.dvach.models.presentation.ThreadImageModel;
import com.vortexwolf.dvach.services.BrowserLauncher;
import com.vortexwolf.dvach.services.ThreadImagesService;
import com.vortexwolf.dvach.services.MyTracker;
import com.vortexwolf.dvach.services.presentation.DvachUriBuilder;
import com.vortexwolf.dvach.settings.ApplicationSettings;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.DialogInterface.OnCancelListener;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ImageView.ScaleType;

public class ImageGalleryActivity extends Activity {
    public static final String TAG = "ImageGalleryActivity";
    
    private ThreadImagesService mThreadImagesService;
    private DvachUriBuilder mDvachUriBuilder;    
    private ICacheDirectoryManager mCacheDirectoryManager;
    private ApplicationSettings mApplicationSettings;
    
    private String mThreadUri;
    private int mImagesCount = 0;
    private ThreadImageModel mCurrentImageModel;
    private ImageItemViewBag mCurrentImageViewBag;
    private boolean mImageLoaded;
    private ImageFileModel mImageLoadedFile;
    
    private DownloadFileTask mCurrentTask = null;
    
    private Menu mMenu;
    private TextView mImageText;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.requestWindowFeature(Window.FEATURE_PROGRESS);
        
        this.mThreadImagesService = Factory.getContainer().resolve(ThreadImagesService.class);
        this.mDvachUriBuilder = Factory.getContainer().resolve(DvachUriBuilder.class);
        this.mCacheDirectoryManager = Factory.getContainer().resolve(ICacheDirectoryManager.class);
        this.mApplicationSettings = Factory.getContainer().resolve(ApplicationSettings.class);
        
        String imageUrl = this.getIntent().getData().toString();
        this.mThreadUri = this.getIntent().getExtras().getString(Constants.EXTRA_THREAD_URL);
        
        // get the current image
        ArrayList<ThreadImageModel> images = this.mThreadImagesService.getImagesList(this.mThreadUri);
        this.mImagesCount = images.size();
        ThreadImageModel currentImage = this.mThreadImagesService.getImageByUrl(images, imageUrl);
        int imagePosition = images.indexOf(currentImage);
        
        this.setTheme(this.mApplicationSettings.getTheme());
        this.setContentView(R.layout.image_gallery_view);
        this.mImageText = (TextView)this.findViewById(R.id.image_gallery_text);
        
        final ImageGalleryAdapter adapter = new ImageGalleryAdapter(this, images);
        final ViewPager viewPager = (HackyViewPager) findViewById(R.id.view_pager);
        viewPager.setAdapter(adapter);
        viewPager.setCurrentItem(imagePosition);
        viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                adapter.onPageSelected(position);
            }
            
            @Override
            public void onPageScrolled(int arg0, float arg1, int arg2) {
            }
            
            @Override
            public void onPageScrollStateChanged(int arg0) {
            }
        });
        
        ImageButton prevImageButton = (ImageButton)this.findViewById(R.id.image_gallery_prev);
        prevImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                int currentItem = viewPager.getCurrentItem();
                if (currentItem > 0) {
                    viewPager.setCurrentItem(currentItem - 1, false);
                }
            }
        });
        
        ImageButton nextImageButton = (ImageButton)this.findViewById(R.id.image_gallery_next);
        nextImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                int currentItem = viewPager.getCurrentItem();
                if (currentItem < mImagesCount - 1) {
                    viewPager.setCurrentItem(currentItem + 1, false);
                }
            }
        });

        Factory.getContainer().resolve(MyTracker.class).setBoardVar(UriUtils.getBoardName(Uri.parse(imageUrl)));
        Factory.getContainer().resolve(MyTracker.class).trackActivityView(TAG);
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

        saveMenuItem.setVisible(this.mImageLoaded);
        shareMenuItem.setVisible(this.mImageLoaded);
        refreshMenuItem.setVisible(!this.mImageLoaded);
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
            case R.id.share_menu_id:
                Intent shareImageIntent = new Intent(Intent.ACTION_SEND);
                shareImageIntent.setType("image/jpeg");
                shareImageIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(this.mImageLoadedFile.file));
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
    
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }
    
    private void loadImage(ThreadImageModel model, ImageItemViewBag viewBag){
        if (this.mCurrentTask != null) {
            // only 1 image per time
            this.mCurrentTask.cancel(true);
        }
        
        setProgress(Window.PROGRESS_END);
        
        this.mImageLoaded = false;
        this.mImageLoadedFile = null;
        this.mCurrentImageViewBag = null;
        this.updateOptionsMenu();
        
        Uri uri = Uri.parse(model.url);
        
        File cachedFile = this.mCacheDirectoryManager.getCachedImageFileForRead(uri);
        if (cachedFile.exists()) {
            // show from cache
            this.setImage(cachedFile, viewBag);
        } else {
            // download image and cache it
            File writeCachedFile = this.mCacheDirectoryManager.getCachedImageFileForWrite(uri);
            this.mCurrentTask = new DownloadFileTask(this, uri, writeCachedFile, new ImageDownloadView(viewBag), false);
            this.mCurrentTask.execute();
        }
    }
    
    private void setImage(File file, ImageItemViewBag viewBag){
        ImageFileModel imgModel = new ImageFileModel(file);
        Bitmap bmp = imgModel.getBitmap(800); // max 800x800
        viewBag.image.setImageBitmap(bmp);
        
        this.mImageLoaded = true;
        this.mImageLoadedFile = imgModel;
        this.mCurrentImageViewBag = viewBag;
        this.updateOptionsMenu();
    }
    
    private class ImageGalleryAdapter extends PagerAdapter {
        private final LayoutInflater mInflater;
        private final ArrayList<ThreadImageModel> mImages;
        private final ImageItemViewBag[] mViewBags;
        private boolean mFirstTime = true;
        private ImageView mCurrentImageView;
        
        public ImageGalleryAdapter(Context context, ArrayList<ThreadImageModel> images){
            this.mInflater = LayoutInflater.from(context);
            this.mImages = images;
            this.mViewBags = new ImageItemViewBag[images.size() + 1]; // requires 1 extra item
        }
        
        @Override
        public int getCount() {
            return this.mImages.size();
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            MyLog.d("ImageGalleryAdapter", "instantiateItem " + position);
            View view = mInflater.inflate(R.layout.image_gallery_item, null);

            ImageItemViewBag vb = new ImageItemViewBag();
            vb.image = (PhotoView)view.findViewById(R.id.image);
            vb.loading = view.findViewById(R.id.loading);
            vb.error = view.findViewById(R.id.error);
            view.setTag(vb);
            this.mViewBags[position] = vb;
            
            //ThreadImageModel imageModel = this.mImages.get(position);
            //loadImage(imageModel, vb);
            
            container.addView(view);
            if (this.mFirstTime) {
                // call onPageSelected the first time
                this.onPageSelected(position);
                this.mFirstTime = false;
            }
            
            return view;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            MyLog.d("ImageGalleryAdapter", "destroyItem " + position);
            this.mViewBags[position] = null;
            container.removeView((View) object);
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        public void onPageSelected(int position) {
            MyLog.d("ImageGalleryAdapter", "onPageSelected " + position);
            if (this.mCurrentImageView != null && this.mCurrentImageView.getDrawable() instanceof BitmapDrawable) {
                Bitmap bitmap = ((BitmapDrawable)this.mCurrentImageView.getDrawable()).getBitmap();
                AppearanceUtils.clearImage(this.mCurrentImageView);
                if (bitmap != null) {
                    bitmap.recycle();
                }
            }
            
            ImageItemViewBag vb = this.mViewBags[position];
            if (vb == null) {
                // unknown bug
                return;
            }
            ThreadImageModel imageModel = this.mImages.get(position);
            mCurrentImageModel = imageModel;
            loadImage(imageModel, vb);
            
            this.mCurrentImageView = vb.image;
            mImageText.setText((position + 1) + "/" + this.getCount() + " (" + imageModel.size + getResources().getString(R.string.data_file_size_measure) + ")");
        }
    }
    
    private class ImageDownloadView implements IDownloadFileView {
        private final ImageItemViewBag mViewBag;
        private double mMaxValue = -1;

        public ImageDownloadView(ImageItemViewBag viewBag){
            mViewBag = viewBag;
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
            AppearanceUtils.clearImage(this.mViewBag.image);
            this.mViewBag.image.setVisibility(View.GONE);
            this.mViewBag.loading.setVisibility(View.VISIBLE);
            this.mViewBag.error.setVisibility(View.GONE);
        }

        @Override
        public void hideLoading() {
            ImageGalleryActivity.this.setProgress(Window.PROGRESS_END);
            this.mViewBag.image.setVisibility(View.VISIBLE);
            this.mViewBag.loading.setVisibility(View.GONE);
            this.mViewBag.error.setVisibility(View.GONE);
        }

        @Override
        public void setOnCancelListener(OnCancelListener listener) {
        }

        @Override
        public void showSuccess(File file) {
            setImage(file, this.mViewBag);
        }

        @Override
        public void showError(String error) {
            this.mViewBag.image.setVisibility(View.GONE);
            this.mViewBag.loading.setVisibility(View.GONE);
            this.mViewBag.error.setVisibility(View.VISIBLE);
            
            TextView errorTextView = (TextView) this.mViewBag.error.findViewById(R.id.error_text);
            errorTextView.setText(error != null ? error : ImageGalleryActivity.this.getString(R.string.error_unknown));
        }

        @Override
        public void showFileExists(File file) {
            setImage(file, this.mViewBag);
        }
    }
    
    private static class ImageItemViewBag {
        PhotoView image;
        View loading;
        View error;
    }
}
