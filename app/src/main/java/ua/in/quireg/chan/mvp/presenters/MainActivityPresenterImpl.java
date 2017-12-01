package ua.in.quireg.chan.mvp.presenters;

import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import com.arellomobile.mvp.InjectViewState;
import com.arellomobile.mvp.MvpPresenter;
import com.ncapdevi.fragnav.FragNavController;

import javax.inject.Inject;

import ua.in.quireg.chan.common.MainApplication;
import ua.in.quireg.chan.mvp.views.MainActivityView;
import ua.in.quireg.chan.services.NavigationController;

/**
 * Created by Arcturus Mengsk on 12/1/2017, 6:36 AM.
 * 2ch-Browser
 */

@InjectViewState
public class MainActivityPresenterImpl extends MvpPresenter<MainActivityView>  implements MainActivityPresenter, TabLayout.OnTabSelectedListener, FragNavController.TransactionListener{

    @Inject NavigationController mNavigationController;

    private FragmentManager mFragmentManager;

    public MainActivityPresenterImpl() {
        super();
        MainApplication.getAppComponent().inject(this);
    }

    @Override
    protected void onFirstViewAttach() {
        super.onFirstViewAttach();
        getViewState().requestProvideFragmentManager();

        mNavigationController.init(mFragmentManager, this, this);

        getViewState().updateToolbar(true);
    }

    @Override
    public void onTabTransaction(@Nullable Fragment fragment, int i) {
        getViewState().updateToolbar(mNavigationController.isRootFragment());
    }

    @Override
    public void onFragmentTransaction(Fragment fragment, FragNavController.TransactionType transactionType) {
        getViewState().updateToolbar(mNavigationController.isRootFragment());
    }

    @Override
    public void showToast(String message) {
        getViewState().showToast(message);
    }

    @Override
    public void setFragmentManager(FragmentManager fragmentManager) {
        mFragmentManager = fragmentManager;
    }

    @Override
    public void updateTabSelection(int position) {
        getViewState().updateTabSelection(position);
    }

    @Override
    public void onBackPressed() {
        mNavigationController.onBackPressed();
    }

    @Override
    public void onTabSelected(TabLayout.Tab tab) {
        mNavigationController.switchTab(tab.getPosition());
    }

    @Override
    public void onTabUnselected(TabLayout.Tab tab) {

    }

    @Override
    public void onTabReselected(TabLayout.Tab tab) {
        onTabSelected(tab);
    }
}
