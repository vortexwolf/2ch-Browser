package ua.in.quireg.chan.mvp.presenters;

import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;

import com.ncapdevi.fragnav.FragNavController;
import com.ncapdevi.fragnav.FragNavTransactionOptions;

import timber.log.Timber;
import ua.in.quireg.chan.R;
import ua.in.quireg.chan.common.Constants;
import ua.in.quireg.chan.common.MainApplication;
import ua.in.quireg.chan.mvp.views.MainActivityView;
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
 * Created by Arcturus Mengsk on 12/1/2017, 6:36 AM.
 * 2ch-Browser
 */

public class MainActivityPresenterImpl implements MainActivityPresenter, TabLayout.OnTabSelectedListener,
        FragNavController.TransactionListener, FragNavController.RootFragmentListener {

    private MainActivityView mMainActivityView;

    private FragNavController mFragNavController;
    private FragmentHistory mFragmentHistory = new FragmentHistory();

    private boolean isInitialized = false;

    private Bundle savedInstanceState = new Bundle();

    private static final int TABS_AMOUNT = 5;
    private boolean mDoubleBackToExitPressedOnce = false;


    public MainActivityPresenterImpl() {
        MainApplication.getAppComponent().inject(this);
    }


    @Override
    public void onTabTransaction(@Nullable Fragment fragment, int i) {
        if(mFragNavController == null) {
            return;
        }
        mMainActivityView.updateToolbar(mFragNavController.isRootFragment());
    }

    @Override
    public void onFragmentTransaction(Fragment fragment, FragNavController.TransactionType transactionType) {
        mMainActivityView.updateToolbar(mFragNavController.isRootFragment());
    }

    @Override
    public void onActivityAttached(AppCompatActivity a) {
        mMainActivityView = (MainActivityView) a;

        mMainActivityView.registerOnTabSelectedListener(this);
        initNavigation();
    }

    @Override
    public void onActivityDetached() {

    }

    private void initNavigation() {

        if (mFragNavController != null) {
            //re-initialization
            mFragNavController.onSaveInstanceState(savedInstanceState);
        }

        FragNavTransactionOptions options = FragNavTransactionOptions.newBuilder()
                .transition(FragmentTransaction.TRANSIT_NONE)
                //.transition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                .build();


        mFragNavController = FragNavController.newBuilder(savedInstanceState, mMainActivityView.getSupportFragmentManager(), R.id.container)
                .transactionListener(this)
                .rootFragmentListener(this, TABS_AMOUNT)
                .defaultTransactionOptions(options)
                .build();

        isInitialized = true;
    }

    @Override
    public void onBackPressed() {
        if (!isInitialized) {
            Timber.e("Attempt to use navigation without init");
            return;
        }

        if (!mFragNavController.isRootFragment()) {
            //Non-root fragment on top, pop it
            mFragNavController.popFragment();
        } else {
            //root fragment is visible, let's check fragment commit history.
            if (mFragmentHistory.isEmpty()) {
                //No history, proceed with exit
                if (mDoubleBackToExitPressedOnce) {
                    System.exit(0);
                    return;
                }
                waitForAnotherPressToExit();

            } else {
                if (mFragmentHistory.getStackSize() > 1) {
                    //History is there. let's go to previous root fragment that has been opened.
                    int position = mFragmentHistory.pop();
                    mFragNavController.switchTab(position);
                    if (mMainActivityView != null) {
                        mMainActivityView.updateTabSelection(position);
                    }

                } else {
                    //single fragment in stack, go to home fragment.
                    mFragmentHistory.pop();
                    if (mMainActivityView != null) {
                        mMainActivityView.updateTabSelection(0);
                    }
                    mFragNavController.switchTab(0);
                    mFragmentHistory.emptyStack();

                }
            }
        }
    }

    @Override
    public void onTabSelected(TabLayout.Tab tab) {
        mFragNavController.switchTab(tab.getPosition());
        mFragmentHistory.push(tab.getPosition());

    }

    @Override
    public void onTabUnselected(TabLayout.Tab tab) {
        //do nothing
    }

    @Override
    public void onTabReselected(TabLayout.Tab tab) {
        onTabSelected(tab);
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

    private void waitForAnotherPressToExit() {
        mDoubleBackToExitPressedOnce = true;

        mMainActivityView.showToast("Press again to exit");

        new Handler().postDelayed(() -> mDoubleBackToExitPressedOnce = false, 2000);
    }

    @Override
    public void pushFragment(Fragment fragment) {
        mFragNavController.pushFragment(fragment);
    }

    @Override
    public void navigateBoard(String website, String board) {
        ThreadsListFragment threadsListFragment = new ThreadsListFragment();
        Bundle extras = new Bundle();
        extras.putString(Constants.EXTRA_WEBSITE, website);
        extras.putString(Constants.EXTRA_BOARD_NAME, board);
        extras.putBoolean(Constants.EXTRA_PREFER_DESERIALIZED, true);

        threadsListFragment.setArguments(extras);
        mFragNavController.pushFragment(threadsListFragment);

    }

    @Override
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

    @Override
    public void navigateGallery(Uri imageUri, String threadUrl) {
        ImageGalleryFragment imageGalleryFragment = new ImageGalleryFragment();
        Bundle extras = new Bundle();
        extras.putString(Constants.EXTRA_IMAGE_URI, imageUri.toString());
        extras.putString(Constants.EXTRA_THREAD_URL, threadUrl);

        imageGalleryFragment.setArguments(extras);
        mFragNavController.pushFragment(imageGalleryFragment);

    }

    @Override
    public void navigateCatalog(String website, String board) {
        Bundle extras = new Bundle();
        extras.putString(Constants.EXTRA_WEBSITE, website);
        extras.putString(Constants.EXTRA_BOARD_NAME, board);
        extras.putBoolean(Constants.EXTRA_CATALOG, true);

    }
}
