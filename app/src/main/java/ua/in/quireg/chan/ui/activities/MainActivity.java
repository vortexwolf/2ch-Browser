package ua.in.quireg.chan.ui.activities;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.design.widget.TabLayout;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import com.arellomobile.mvp.MvpAppCompatActivity;

import javax.inject.Inject;

import butterknife.BindArray;
import butterknife.BindView;
import butterknife.ButterKnife;
import ru.terrakok.cicerone.NavigatorHolder;
import ua.in.quireg.chan.R;
import ua.in.quireg.chan.common.MainApplication;
import ua.in.quireg.chan.common.utils.AppearanceUtils;
import ua.in.quireg.chan.mvp.routing.MainNavigator;
import ua.in.quireg.chan.mvp.routing.MainRouter;
import ua.in.quireg.chan.settings.ApplicationSettings;

import static android.view.View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
import static android.view.View.SYSTEM_UI_FLAG_LOW_PROFILE;
import static android.view.View.inflate;

public class MainActivity extends MvpAppCompatActivity {

    @Inject NavigatorHolder mNavigatorHolder;
    @Inject MainRouter mMainRouter;
    @Inject ApplicationSettings mApplicationSettings;

    @BindView(R.id.toolbar) protected Toolbar mToolbar;
    @BindView(R.id.bottom_tab) protected TabLayout mBottomTabLayout;
    @BindArray(R.array.tab_name) protected String[] TABS;

    private Toast mToast;
    private MainNavigator mNavigator;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        MainApplication.getAppComponent().inject(this);



        setTheme(mApplicationSettings.getTheme());
        setContentView(R.layout.base_activity);
        ButterKnife.bind(this);
        setSupportActionBar(mToolbar);
//        FrameLayout statusBar = findViewById(R.id.status_bar);
//        statusBar.setPadding(
//                statusBar.getPaddingLeft(),
//                statusBar.getPaddingTop(),
//                statusBar.getPaddingRight(),
//                statusBar.getPaddingBottom() + getStatusBarHeight()
//        );
        if (getSupportActionBar() != null) {
            getSupportActionBar().setLogo(R.drawable.browser_logo_drawable);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        initBottomTabs();

        super.onCreate(savedInstanceState);
        mBottomTabLayout.addOnTabSelectedListener(mTabLayoutListener);

        //this must be called after super::onCreate otherwise won't work
        mNavigator = new MainNavigator(this, savedInstanceState);
    }

    TabLayout.OnTabSelectedListener mTabLayoutListener = new TabLayout.OnTabSelectedListener() {
        @Override
        public void onTabSelected(TabLayout.Tab tab) {
            mMainRouter.switchTab(tab.getPosition());
        }

        @Override
        public void onTabUnselected(TabLayout.Tab tab) {

        }

        @Override
        public void onTabReselected(TabLayout.Tab tab) {
            onTabSelected(tab);
        }
    };

    @Override
    protected void onResumeFragments() {
        super.onResumeFragments();
        mNavigatorHolder.setNavigator(mNavigator);
    }

    @Override
    protected void onPause() {
        mNavigatorHolder.removeNavigator();
        super.onPause();
    }

    @Override
    public void onBackPressed() {
        mMainRouter.onBackPressed();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mNavigator != null) {
            mNavigator.saveNavigationState(outState);
        }

    }

    public void updateToolbarControls(boolean isRootFrag) {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayUseLogoEnabled(isRootFrag);
            getSupportActionBar().setDisplayHomeAsUpEnabled(!isRootFrag);
        }
    }

    public void updateTabSelection(int currentTab) {

        for (int i = 0; i < TABS.length; i++) {
            TabLayout.Tab selectedTab = mBottomTabLayout.getTabAt(i);

            if(selectedTab == null || selectedTab.getCustomView() == null) {
                continue;
            }
            if (currentTab != i) {
                selectedTab.getCustomView().setSelected(false);
            } else {
                selectedTab.getCustomView().setSelected(true);
            }
        }
    }

    public void sendShortToast(@StringRes int id) {
        if (mToast != null) {
            mToast.cancel();
        }
        mToast = Toast.makeText(this, getString(id), Toast.LENGTH_SHORT);
        mToast.show();
    }


    private void initBottomTabs() {
        for (int i = 0; i < TABS.length; i++) {
            TabLayout.Tab tab = mBottomTabLayout.newTab();
            tab.setCustomView(getTabView(i));
            //Reminder - this triggers onTabSelected() on first tab
            mBottomTabLayout.addTab(tab);
        }
    }

    private View getTabView(int position) {
        View view = LayoutInflater.from(this).inflate(R.layout.tab_item_bottom, mBottomTabLayout, false);
        ImageView icon = view.findViewById(R.id.tab_icon);
        Drawable stateListDrawable = AppearanceUtils.getStateListDrawable(this, mTabIconsSelected[position]);
        icon.setImageDrawable(stateListDrawable);
        return view;
    }

    private int[] mTabIconsSelected = {
            R.drawable.browser_home,
            R.drawable.browser_tabs,
            R.drawable.browser_favourites,
            R.drawable.browser_history,
            R.drawable.browser_settings
    };

//    private int getStatusBarHeight() {
//        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
//        int result = 0;
//
//        if (resourceId > 0) {
//            result = getResources().getDimensionPixelSize(resourceId);
//        }
//        return result;
//    }
}
