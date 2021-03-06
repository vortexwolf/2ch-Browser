package ua.in.quireg.chan.mvp.routing;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;

import com.ncapdevi.fragnav.FragNavController;
import com.ncapdevi.fragnav.FragNavTransactionOptions;

import javax.inject.Inject;

import ru.terrakok.cicerone.Navigator;
import ru.terrakok.cicerone.commands.Command;
import ua.in.quireg.chan.R;
import ua.in.quireg.chan.common.Constants;
import ua.in.quireg.chan.common.MainApplication;
import ua.in.quireg.chan.common.Websites;
import ua.in.quireg.chan.mvp.routing.commands.NavigateBackwards;
import ua.in.quireg.chan.mvp.routing.commands.NavigateBoard;
import ua.in.quireg.chan.mvp.routing.commands.NavigateBoardsList;
import ua.in.quireg.chan.mvp.routing.commands.NavigateGallery;
import ua.in.quireg.chan.mvp.routing.commands.NavigateThread;
import ua.in.quireg.chan.mvp.routing.commands.PushFragment;
import ua.in.quireg.chan.mvp.routing.commands.SendShortToast;
import ua.in.quireg.chan.mvp.routing.commands.SwitchTab;
import ua.in.quireg.chan.settings.ApplicationSettings;
import ua.in.quireg.chan.ui.activities.MainActivity;
import ua.in.quireg.chan.ui.fragments.AppPreferenceFragment;
import ua.in.quireg.chan.ui.fragments.BoardsListFragment;
import ua.in.quireg.chan.ui.fragments.FavoritesFragment;
import ua.in.quireg.chan.ui.fragments.HistoryFragment;
import ua.in.quireg.chan.ui.fragments.ImageGalleryFragment;
import ua.in.quireg.chan.ui.fragments.OpenTabsFragment;
import ua.in.quireg.chan.ui.fragments.PostsListFragment;
import ua.in.quireg.chan.ui.fragments.ThreadsListFragment;

/**
 * Created by Arcturus Mengsk on 12/20/2017, 6:21 AM.
 * 2ch-Browser
 */

public class MainNavigator implements Navigator,
        FragNavController.TransactionListener,
        FragNavController.RootFragmentListener {

    private static final int BOTTOM_TABS_AMOUNT = 5;

    @Inject TabsTransactionHistory mTabsTransactionHistory;
    @Inject ApplicationSettings mApplicationSettings;
    @Inject Context mContext;

    private FragNavController mFragNavController;
    private MainActivity mMainActivity;

    private Fragment[] mTabsFragments = new Fragment[]{
            new BoardsListFragment(),
            new OpenTabsFragment(),
            new FavoritesFragment(),
            new HistoryFragment(),
            new AppPreferenceFragment()
    };

    public MainNavigator(MainActivity activity, Bundle savedInstanceState) {
        mMainActivity = activity;
        MainApplication.getAppComponent().inject(this);

        FragNavTransactionOptions mFragNavTransactionOptions =
                FragNavTransactionOptions.newBuilder()
                        .transition(FragmentTransaction.TRANSIT_NONE)
                        .build();

        mFragNavController =
                FragNavController.newBuilder(
                        savedInstanceState,
                        mMainActivity.getSupportFragmentManager(),
                        R.id.base_activity_container)
                        .transactionListener(this)
                        .rootFragmentListener(this, BOTTOM_TABS_AMOUNT)
                        .defaultTransactionOptions(mFragNavTransactionOptions)
                        .selectedTabIndex(mApplicationSettings.isLeftHand()
                                ? FragNavController.TAB1
                                : FragNavController.TAB5)
                        .build();

        mMainActivity.updateTabSelection(mFragNavController.getCurrentStackIndex());
    }

    @Override
    public Fragment getRootFragment(int index) {
        if (mApplicationSettings.isLeftHand()) {
            return mTabsFragments[index];
        } else {
            return mTabsFragments[mTabsFragments.length - 1 - index];
        }
    }

    public void saveNavigationState(Bundle savedInstanceState) {
        if (mFragNavController != null) {
            //re-initialization
            mFragNavController.onSaveInstanceState(savedInstanceState);
        }
    }

    @Override
    public void onTabTransaction(@Nullable Fragment fragment, int i) {
        if (mFragNavController == null) {
            return;
        }
        mTabsTransactionHistory.push(i);
        mMainActivity.updateTabSelection(i);
        mMainActivity.updateToolbarControls(mFragNavController.isRootFragment());
    }

    @Override
    public void onFragmentTransaction(Fragment fragment,
                                      FragNavController.TransactionType transactionType) {
        mMainActivity.updateToolbarControls(mFragNavController.isRootFragment());
    }

    @Override
    public void applyCommand(Command command) {

        if (command instanceof SendShortToast) {
            mMainActivity.sendShortToast(((SendShortToast) command).getToast());
        }
        if (command instanceof NavigateBackwards) {
            onBackPressed();
        }
        if (command instanceof PushFragment) {
            mFragNavController.pushFragment(((PushFragment) command).getFragment());
        }
        if (command instanceof SwitchTab) {
            int activeTabPosition = ((SwitchTab) command).getTabPosition();
            mFragNavController.switchTab(activeTabPosition);
            mMainActivity.updateTabSelection(activeTabPosition);
            mMainActivity.updateToolbarControls(mFragNavController.isRootFragment());
        }
        if (command instanceof NavigateBoardsList) {
            navigateBoardsList(((NavigateBoardsList) command).getWebsite());
        }
        if (command instanceof NavigateBoard) {
            navigateBoard(((NavigateBoard) command).getWebsite(),
                    ((NavigateBoard) command).getBoardCode(),
                    ((NavigateBoard) command).isPreferDeserialized());
        }

        if (command instanceof NavigateThread) {
            navigateThread(((NavigateThread) command).getThread(),
                    ((NavigateThread) command).isPreferDeserialized());
        }

        if (command instanceof NavigateGallery) {
            navigateGallery(((NavigateGallery) command).getUri(),
                    ((NavigateGallery) command).getThreadUrl());
        }
    }

//    private void updateTabSelection(int activeTabPosition) {
//        mMainActivity.updateTabSelection(activeTabPosition);
//    }
//
//    private void updateToolbarControls(boolean isRootFrag) {
//        mMainActivity.updateToolbarControls(isRootFrag);
//    }
//
//    private void sendShortToast(int stringId) {
//        mMainActivity.sendShortToast(stringId);
//    }
//
//    private void exitApplication() {
//        mMainActivity.finish();
//    }

//    private boolean mDoubleBackToExitPressedOnce = false;

    private void onBackPressed() {

        if (!mFragNavController.isRootFragment()) {
            //Non-root fragment on top, pop it
            mFragNavController.popFragment();
        } else {
            //root fragment is visible, let's check fragment commit history.
            if (mTabsTransactionHistory.isEmpty()) {
//                //No history, proceed with exit
                mMainActivity.finish();

//                if (mDoubleBackToExitPressedOnce) {
//                    exitApplication();
//                    return;
//                }
//                waitForAnotherPressToExit();
            } else {
                if (mTabsTransactionHistory.getStackSize() > 1) {
                    //History is there. let's go to previous root fragment that has been opened.
                    int position = mTabsTransactionHistory.pop();

                    mFragNavController.switchTab(position);
                    mMainActivity.updateTabSelection(position);

                } else {
                    //single fragment in stack, go to home fragment.
                    mFragNavController.switchTab(0);
                    mTabsTransactionHistory.emptyStack();
                }
            }
        }
    }

//    private void waitForAnotherPressToExit() {
//        mDoubleBackToExitPressedOnce = true;
//
//        sendShortToast(R.string.confirm_exit);
//
//        new Handler().postDelayed(() -> mDoubleBackToExitPressedOnce = false, 2000);
//    }

    private void navigateBoardsList(String website) {
        BoardsListFragment boardsListFragment = new BoardsListFragment();

        Bundle extras = new Bundle();
        extras.putString(Constants.EXTRA_WEBSITE, website);

        boardsListFragment.setArguments(extras);
        mFragNavController.pushFragment(boardsListFragment);
    }

    private void navigateBoard(String website, String board, boolean preferDeserialized) {
        ThreadsListFragment threadsListFragment = new ThreadsListFragment();
        Bundle extras = new Bundle();
        extras.putString(Constants.EXTRA_WEBSITE, website);
        extras.putString(Constants.EXTRA_BOARD_NAME, board);
        extras.putBoolean(Constants.EXTRA_PREFER_DESERIALIZED, preferDeserialized);

        threadsListFragment.setArguments(extras);
        mFragNavController.pushFragment(threadsListFragment);

    }

    private void navigateThread(String thread, boolean preferDeserialized) {
        PostsListFragment postsListFragment = new PostsListFragment();
        Bundle extras = new Bundle();
        extras.putString(Constants.EXTRA_WEBSITE, Websites.getDefault().name());
//        extras.putString(Constants.EXTRA_BOARD_NAME, board);
        extras.putString(Constants.EXTRA_THREAD_NUMBER, thread);
//        extras.putString(Constants.EXTRA_THREAD_SUBJECT, subject);
//        extras.putString(Constants.EXTRA_POST_NUMBER, post);
        extras.putBoolean(Constants.EXTRA_PREFER_DESERIALIZED, preferDeserialized);

        postsListFragment.setArguments(extras);
        mFragNavController.pushFragment(postsListFragment);
    }

    private void navigateGallery(Uri imageUri, String threadUrl) {
        ImageGalleryFragment imageGalleryFragment = new ImageGalleryFragment();
        Bundle extras = new Bundle();
        extras.putString(Constants.EXTRA_IMAGE_URI, imageUri.toString());
        extras.putString(Constants.EXTRA_THREAD_URL, threadUrl);

        imageGalleryFragment.setArguments(extras);
        mFragNavController.pushFragment(imageGalleryFragment);
    }

    private void navigateCatalog(String website, String board) {
        Bundle extras = new Bundle();
        extras.putString(Constants.EXTRA_WEBSITE, website);
        extras.putString(Constants.EXTRA_BOARD_NAME, board);
        extras.putBoolean(Constants.EXTRA_CATALOG, true);
    }

}
