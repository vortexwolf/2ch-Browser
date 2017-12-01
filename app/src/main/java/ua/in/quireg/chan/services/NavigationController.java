package ua.in.quireg.chan.services;

import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import com.ncapdevi.fragnav.FragNavController;
import com.ncapdevi.fragnav.FragNavTransactionOptions;

import timber.log.Timber;
import ua.in.quireg.chan.R;
import ua.in.quireg.chan.common.Constants;
import ua.in.quireg.chan.mvp.presenters.MainActivityPresenter;
import ua.in.quireg.chan.ui.fragments.AppPreferenceFragment;
import ua.in.quireg.chan.ui.fragments.BoardsListFragment;
import ua.in.quireg.chan.ui.fragments.FavoritesFragment;
import ua.in.quireg.chan.ui.fragments.HistoryFragment;
import ua.in.quireg.chan.ui.fragments.ImageGalleryFragment;
import ua.in.quireg.chan.ui.fragments.OpenTabsFragment;
import ua.in.quireg.chan.ui.fragments.PostsListFragment;
import ua.in.quireg.chan.ui.fragments.ThreadsListFragment;
import ua.in.quireg.chan.ui.views.FragmentHistory;

/**
 * Created by Arcturus Mengsk on 12/1/2017, 9:26 AM.
 * 2ch-Browser
 */

public class NavigationController implements FragNavController.RootFragmentListener {

    private static final int TABS_AMOUNT = 5;

    private FragNavController mFragNavController;
    private FragmentHistory mFragmentHistory = new FragmentHistory();

    private MainActivityPresenter mActivityPresenter;

    private boolean isInitialized = false;

    private boolean mDoubleBackToExitPressedOnce = false;

    public void init(FragmentManager fragmentManager,
                     FragNavController.TransactionListener transactionListener, MainActivityPresenter presenter) {
        mActivityPresenter = presenter;

        Bundle savedInstanceState = new Bundle();

        if (mFragNavController != null) {
            //re-initialization
            mFragNavController.onSaveInstanceState(savedInstanceState);
        }

        FragNavTransactionOptions options = FragNavTransactionOptions.newBuilder()
                .transition(FragmentTransaction.TRANSIT_NONE)
                //.transition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                .build();

        mFragNavController = FragNavController.newBuilder(savedInstanceState, fragmentManager, R.id.container)
                .transactionListener(transactionListener)
                .rootFragmentListener(this, TABS_AMOUNT)
                .defaultTransactionOptions(options)
                .build();

        isInitialized = true;
    }

    @Override
    public Fragment getRootFragment(int index) {
        switch (index) {

            case FragNavController.TAB1:
                return new BoardsListFragment();
            case FragNavController.TAB2:
                return new OpenTabsFragment();
            case FragNavController.TAB3:
                return new FavoritesFragment();
            case FragNavController.TAB4:
                return new HistoryFragment();
            case FragNavController.TAB5:
                return new AppPreferenceFragment();

        }
        throw new IllegalStateException("Need to send an index that we know");
    }

    public void switchTab(int position) {
        if (!isInitialized) {
            Timber.e("Attempt to use navigation without init");
            return;
        }
        mFragNavController.switchTab(position);
        mFragmentHistory.push(position);

        if (mActivityPresenter != null) {
            mActivityPresenter.updateTabSelection(position);
        }
    }

    public boolean isRootFragment() {
        if (!isInitialized) {
            Timber.e("Attempt to use navigation without init");
            return false;
        }
        return mFragNavController.isRootFragment();
    }

    public void onBackPressed() {

        if (!mFragNavController.isRootFragment()) {
            //Non-root fragment on top, pop it
            mFragNavController.popFragment();
        } else {
            //root fragment is visible, let's check fragment commit history.
            if (mFragmentHistory.isEmpty()) {
                //No history, proceed with exit
                if (mDoubleBackToExitPressedOnce) {
                    //super.onBackPressed();
                    return;
                }
                waitForAnotherPressToExit();

            } else {
                if (mFragmentHistory.getStackSize() > 1) {
                    //History is there. let's go to previous root fragment that has been opened.
                    int position = mFragmentHistory.pop();
                    mFragNavController.switchTab(position);
                    if (mActivityPresenter != null) {
                        mActivityPresenter.updateTabSelection(position);
                    }

                } else {
                    //single fragment in stack, go to home fragment.
                    mFragmentHistory.pop();
                    if (mActivityPresenter != null) {
                        mActivityPresenter.updateTabSelection(0);
                    }
                    mFragNavController.switchTab(0);
                    mFragmentHistory.emptyStack();

                }
            }
        }
    }

    private void waitForAnotherPressToExit() {
        mDoubleBackToExitPressedOnce = true;

        mActivityPresenter.showToast("Press again to exit");

        new Handler().postDelayed(() -> mDoubleBackToExitPressedOnce = false, 2000);
    }

//    public void navigate(Uri uri, Context context, Integer flags, boolean preferDeserialized) {
//        IWebsite website = Websites.fromUri(uri);
//        IUrlParser urlParser = website.getUrlParser();
//
//        if (urlParser.isBoardUri(uri)) {
//            this.onBoardClick(context, website.name(), urlParser.getBoardName(uri), urlParser.getBoardPageNumber(uri), preferDeserialized);
//        } else if (urlParser.isThreadUri(uri)) {
//            this.navigateThread(context, null, website.name(), urlParser.getBoardName(uri), urlParser.getThreadNumber(uri), null, urlParser.getPostNumber(uri), preferDeserialized);
//        } else if (UriUtils.isImageUri(uri) || UriUtils.isWebmUri(uri)) {
//            //this.navigateActivity(context, BrowserActivity.class, uri, null, flags);
//        } else {
//            this.navigateBoardList(context, website.name(), false);
//        }
//    }

    public void pushFragment(Fragment fragment) {
        mFragNavController.pushFragment(fragment);
    }


    public void navigateBoard(String website, String board) {
        ThreadsListFragment threadsListFragment = new ThreadsListFragment();
        Bundle extras = new Bundle();
        extras.putString(Constants.EXTRA_WEBSITE, website);
        extras.putString(Constants.EXTRA_BOARD_NAME, board);
        extras.putBoolean(Constants.EXTRA_PREFER_DESERIALIZED, true);

        threadsListFragment.setArguments(extras);
        mFragNavController.pushFragment(threadsListFragment);

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
        mFragNavController.pushFragment(postsListFragment);

    }

    public void navigateGallery(Uri imageUri, String threadUrl) {
        ImageGalleryFragment imageGalleryFragment = new ImageGalleryFragment();
        Bundle extras = new Bundle();
        extras.putString(Constants.EXTRA_IMAGE_URI, imageUri.toString());
        extras.putString(Constants.EXTRA_THREAD_URL, threadUrl);

        imageGalleryFragment.setArguments(extras);
        mFragNavController.pushFragment(imageGalleryFragment);

    }

    public void navigateCatalog(String website, String board) {
        Bundle extras = new Bundle();
        extras.putString(Constants.EXTRA_WEBSITE, website);
        extras.putString(Constants.EXTRA_BOARD_NAME, board);
        extras.putBoolean(Constants.EXTRA_CATALOG, true);

    }
}
