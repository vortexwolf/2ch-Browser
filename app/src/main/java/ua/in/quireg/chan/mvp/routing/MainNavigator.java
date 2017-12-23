package ua.in.quireg.chan.mvp.routing;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
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
import ua.in.quireg.chan.mvp.presenters.MainActivityPresenter;
import ua.in.quireg.chan.mvp.routing.commands.ExitApp;
import ua.in.quireg.chan.mvp.routing.commands.NavigateBackwards;
import ua.in.quireg.chan.mvp.routing.commands.NavigateBoard;
import ua.in.quireg.chan.mvp.routing.commands.NavigateBoardsList;
import ua.in.quireg.chan.mvp.routing.commands.NavigateThread;
import ua.in.quireg.chan.mvp.routing.commands.OpenAttachment;
import ua.in.quireg.chan.mvp.routing.commands.PushFragment;
import ua.in.quireg.chan.mvp.routing.commands.SendShortToast;
import ua.in.quireg.chan.mvp.routing.commands.SwitchTab;
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

public class MainNavigator implements Navigator, FragNavController.TransactionListener, FragNavController.RootFragmentListener {

    @Inject TabsTransactionHistory mTabsTransactionHistory;
    @Inject Bundle mSavedInstanceState;
    @Inject Context mContext;

    MainActivityPresenter mMainActivityPresenter;
    FragNavController mFragNavController;

    public MainNavigator(MainActivity activity) {
        MainApplication.getAppComponent().inject(this);

        mMainActivityPresenter = activity.getPresenter();

        FragNavTransactionOptions mFragNavTransactionOptions = FragNavTransactionOptions.newBuilder()
                .transition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                .allowStateLoss(true)
                .build();

        mFragNavController = FragNavController.newBuilder(mSavedInstanceState, activity.getSupportFragmentManager(), R.id.base_activity_container)
                .transactionListener(this)
                .rootFragmentListener(this, 5)
                .defaultTransactionOptions(mFragNavTransactionOptions)
                .build();
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

    public void saveNavigationState() {
        if (mFragNavController != null) {
            //re-initialization
            mFragNavController.onSaveInstanceState(mSavedInstanceState);
        }
    }

    @Override
    public void onTabTransaction(@Nullable Fragment fragment, int i) {
        if (mFragNavController == null) {
            return;
        }
        mTabsTransactionHistory.push(i);
        mMainActivityPresenter.updateTabSelection(i);
        mMainActivityPresenter.updateToolbarControls(mFragNavController.isRootFragment());

        //mMainActivityPresenter.updateToolbarTitle(fragment.getArguments().getString(Constants.FRAGMENT_TITLE));
    }

    @Override
    public void onFragmentTransaction(Fragment fragment, FragNavController.TransactionType transactionType) {
        mMainActivityPresenter.updateToolbarControls(mFragNavController.isRootFragment());

    }

    @Override
    public void applyCommand(Command command) {

        if (command instanceof SendShortToast) {
            mMainActivityPresenter.sendShortToast(((SendShortToast) command).getToast());
        }
        if (command instanceof ExitApp) {
            mMainActivityPresenter.exitApplication();
        }
        if (command instanceof NavigateBackwards) {
            onBackPressed();
        }
        if (command instanceof PushFragment) {
            mFragNavController.pushFragment(((PushFragment) command).getFragment());
        }
        if (command instanceof SwitchTab) {
            mFragNavController.switchTab(((SwitchTab) command).getTabPosition());
        }
        if (command instanceof NavigateBoardsList) {
            navigateBoardsList(
                    ((NavigateBoardsList) command).getWebsite()
            );
        }
        if (command instanceof NavigateBoard) {
            navigateBoard(
                    ((NavigateBoard) command).getWebsite(),
                    ((NavigateBoard) command).getBoardCode(),
                    ((NavigateBoard) command).isPreferDeserialized()
            );
        }

        if (command instanceof NavigateThread) {
            navigateThread(
                    ((NavigateThread) command).getWebsite(),
                    ((NavigateThread) command).getBoardCode(),
                    ((NavigateThread) command).getThread(),
                    ((NavigateThread) command).getSubject(),
                    ((NavigateThread) command).getPost(),
                    ((NavigateThread) command).isPreferDeserialized()
            );
        }

        if (command instanceof OpenAttachment) {
//            Intent imageGallery = new Intent(mContext, ImageGalleryActivity.class);
//            imageGallery.setData(uri);
//            imageGallery.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//            imageGallery.putExtra(Constants.EXTRA_THREAD_URL, attachment.getThreadUrl());
//            context.startActivity(imageGallery);
        }
    }

    private boolean mDoubleBackToExitPressedOnce = false;

    private void onBackPressed() {

        if (!mFragNavController.isRootFragment()) {
            //Non-root fragment on top, pop it
            mFragNavController.popFragment();
        } else {
            //root fragment is visible, let's check fragment commit history.
            if (mTabsTransactionHistory.isEmpty()) {
                //No history, proceed with exit
                if (mDoubleBackToExitPressedOnce) {
                    mMainActivityPresenter.exitApplication();
                    return;
                }
                waitForAnotherPressToExit();

            } else {
                if (mTabsTransactionHistory.getStackSize() >= 2) {
                    //History is there. let's go to previous root fragment that has been opened.
                    int position = mTabsTransactionHistory.pop();

                    mFragNavController.switchTab(position);
                    mMainActivityPresenter.updateTabSelection(position);

                } else {
                    //single fragment in stack, go to home fragment.

                    //mMainActivityPresenter.updateTabSelection(0);

                    mFragNavController.switchTab(0);

                    mTabsTransactionHistory.emptyStack();
                }
            }
        }
    }

    private void waitForAnotherPressToExit() {
        mDoubleBackToExitPressedOnce = true;

        mMainActivityPresenter.sendShortToast(R.string.confirm_exit);

        new Handler().postDelayed(() -> mDoubleBackToExitPressedOnce = false, 2000);
    }

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

    private void navigateThread(String website, String board, String thread, String subject, String post, boolean preferDeserialized) {
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
