package ua.in.quireg.chan.ui.activities;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.arellomobile.mvp.MvpAppCompatActivity;
import com.arellomobile.mvp.presenter.InjectPresenter;

import javax.inject.Inject;

import butterknife.BindArray;
import butterknife.BindView;
import butterknife.ButterKnife;
import ua.in.quireg.chan.R;
import ua.in.quireg.chan.common.MainApplication;
import ua.in.quireg.chan.common.utils.AppearanceUtils;
import ua.in.quireg.chan.mvp.presenters.MainActivityPresenterImpl;
import ua.in.quireg.chan.mvp.views.MainActivityView;
import ua.in.quireg.chan.settings.ApplicationSettings;


public class MainActivity extends MvpAppCompatActivity implements MainActivityView{

    //TODO add settings change listener

    @Inject ApplicationSettings mApplicationSettings;
    @InjectPresenter MainActivityPresenterImpl mMainActivityPresenterImpl;

    @BindView(R.id.toolbar) protected Toolbar mToolbar;
    @BindView(R.id.bottom_tab) protected TabLayout mBottomTabLayout;
    @BindArray(R.array.tab_name) protected String[] TABS;

    private Toast mToast;

    private int[] mTabIconsSelected = {
            R.drawable.browser_home,
            R.drawable.browser_tabs,
            R.drawable.browser_favourites,
            R.drawable.browser_history,
            R.drawable.browser_settings
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        MainApplication.getAppComponent().inject(this);

        setTheme(mApplicationSettings.getTheme());
        setContentView(R.layout.base_activity);

        ButterKnife.bind(this);

        setSupportActionBar(mToolbar);
        getSupportActionBar().setLogo(R.drawable.browser_logo_drawable);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        initBottomTabs();

        super.onCreate(savedInstanceState);

        mBottomTabLayout.addOnTabSelectedListener(mMainActivityPresenterImpl);

    }

    @Override
    public void updateToolbar(boolean isRootFrag) {
        if (getSupportActionBar() == null) {
            return;
        }
        getSupportActionBar().setDisplayUseLogoEnabled(isRootFrag);
        getSupportActionBar().setDisplayHomeAsUpEnabled(!isRootFrag);
    }

    @Override
    public void updateTabSelection(int currentTab) {
        for (int i = 0; i < TABS.length; i++) {
            TabLayout.Tab selectedTab = mBottomTabLayout.getTabAt(i);
            if (currentTab != i) {
                selectedTab.getCustomView().setSelected(false);
            } else {
                selectedTab.getCustomView().setSelected(true);
            }
        }
    }

    @Override
    public void showToast(String message) {
        if (mToast != null) {
            mToast.cancel();
        }
        mToast = Toast.makeText(this, message, Toast.LENGTH_SHORT);
        mToast.show();
    }

    @Override
    public void requestProvideFragmentManager() {
        mMainActivityPresenterImpl.setFragmentManager(getSupportFragmentManager());
    }

    @Override
    public void onBackPressed() {
        mMainActivityPresenterImpl.onBackPressed();
    }

    private void initBottomTabs() {
        //Reminder - this triggers onTabSelected()

        for (int i = 0; i < TABS.length; i++) {
            mBottomTabLayout.addTab(mBottomTabLayout.newTab());
            TabLayout.Tab tab = mBottomTabLayout.getTabAt(i);
            if (tab != null)
                tab.setCustomView(getTabView(i));
        }
    }

    private View getTabView(int position) {
        View view = LayoutInflater.from(this).inflate(R.layout.tab_item_bottom, mBottomTabLayout, false);

        ImageView icon = view.findViewById(R.id.tab_icon);
        Drawable stateListDrawable = AppearanceUtils.getStateListDrawable(this, mTabIconsSelected[position]);
        icon.setImageDrawable(stateListDrawable);

        return view;
    }

}
