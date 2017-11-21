package ua.in.quireg.chan.services;

import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.ncapdevi.fragnav.FragNavController;
import ua.in.quireg.chan.common.Constants;
import ua.in.quireg.chan.views.fragments.ImageGalleryFragment;
import ua.in.quireg.chan.views.fragments.PostsListFragment;
import ua.in.quireg.chan.views.fragments.ThreadsListFragment;

public class NavigationService {

    private static NavigationService mNavigationService;

    private FragNavController mNavController;

    private NavigationService(FragNavController context) {
        mNavController = context;
    }

    public static void init(FragNavController context){
        mNavigationService = new NavigationService(context);
    }


    public static NavigationService getInstance(){
        if(mNavigationService != null){
            return mNavigationService;
        }else{
            throw new IllegalStateException("Must call init() first");
        }
    }

//    public void navigate(Uri uri, Context context, Integer flags, boolean preferDeserialized) {
//        IWebsite website = Websites.fromUri(uri);
//        IUrlParser urlParser = website.getUrlParser();
//
//        if (urlParser.isBoardUri(uri)) {
//            this.navigateBoard(context, website.name(), urlParser.getBoardName(uri), urlParser.getBoardPageNumber(uri), preferDeserialized);
//        } else if (urlParser.isThreadUri(uri)) {
//            this.navigateThread(context, null, website.name(), urlParser.getBoardName(uri), urlParser.getThreadNumber(uri), null, urlParser.getPostNumber(uri), preferDeserialized);
//        } else if (UriUtils.isImageUri(uri) || UriUtils.isWebmUri(uri)) {
//            //this.navigateActivity(context, BrowserActivity.class, uri, null, flags);
//        } else {
//            this.navigateBoardList(context, website.name(), false);
//        }
//    }

    public void pushFragment(Fragment fragment){
        mNavController.pushFragment(fragment);
    }


    public void navigateBoard(String website, String board) {
        ThreadsListFragment threadsListFragment = new ThreadsListFragment();
        Bundle extras = new Bundle();
        extras.putString(Constants.EXTRA_WEBSITE, website);
        extras.putString(Constants.EXTRA_BOARD_NAME, board);
        extras.putBoolean(Constants.EXTRA_PREFER_DESERIALIZED, true);

        threadsListFragment.setArguments(extras);
        mNavController.pushFragment(threadsListFragment);

    }

    public void navigateThread(String website, String board, String thread, String subject, String post, boolean preferDeserialized) {
        PostsListFragment postsListFragment = new PostsListFragment();
        Bundle extras = new Bundle();
        extras.putString(Constants.EXTRA_WEBSITE, website);
        extras.putString(Constants.EXTRA_BOARD_NAME, board);
        extras.putString(Constants.EXTRA_THREAD_NUMBER, thread);
        extras.putString(Constants.EXTRA_THREAD_SUBJECT, subject);
        extras.putString(Constants.EXTRA_POST_NUMBER, post);
        extras.putBoolean(Constants.EXTRA_PREFER_DESERIALIZED, preferDeserialized);

        postsListFragment.setArguments(extras);
        mNavController.pushFragment(postsListFragment);

    }

    public void navigateGallery(Uri imageUri, String threadUrl) {
        ImageGalleryFragment imageGalleryFragment = new ImageGalleryFragment();
        Bundle extras = new Bundle();
        extras.putString(Constants.EXTRA_IMAGE_URI, imageUri.toString());
        extras.putString(Constants.EXTRA_THREAD_URL, threadUrl);

        imageGalleryFragment.setArguments(extras);
        mNavController.pushFragment(imageGalleryFragment);

    }

    public void navigateCatalog(String website, String board) {
        Bundle extras = new Bundle();
        extras.putString(Constants.EXTRA_WEBSITE, website);
        extras.putString(Constants.EXTRA_BOARD_NAME, board);
        extras.putBoolean(Constants.EXTRA_CATALOG, true);

    }

}
