package ua.in.quireg.chan.ui.activities;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.design.widget.TabLayout;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
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
import ua.in.quireg.chan.mvp.routing.commands.SwitchTab;
import ua.in.quireg.chan.settings.ApplicationSettings;

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
        getSupportActionBar().setLogo(R.drawable.browser_logo_drawable);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        initBottomTabs();

        super.onCreate(savedInstanceState);

        mBottomTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
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
        });

        mNavigator = new MainNavigator(this) {

            @Override
            public void updateTabSelection(int activeTabPosition) {
                MainActivity.this.updateTabSelection(activeTabPosition);
            }

            @Override
            public void updateToolbarControls(boolean isRootFrag) {
                MainActivity.this.updateToolbarControls(isRootFrag);
            }

            @Override
            public void sendShortToast(int stringId) {
                MainActivity.this.showSystemMessage(stringId);
            }

            @Override
            public void exitApplication() {
                MainActivity.this.finish();
            }
        };

    }

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
    protected void onDestroy() {
        super.onDestroy();
        if (mNavigator != null) {
            mNavigator.saveNavigationState();
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
            if (currentTab != i) {
                selectedTab.getCustomView().setSelected(false);
            } else {
                selectedTab.getCustomView().setSelected(true);
            }
        }
    }

    public void showSystemMessage(@StringRes int id) {
        if (mToast != null) {
            mToast.cancel();
        }
        mToast = Toast.makeText(this, getString(id), Toast.LENGTH_SHORT);
        mToast.show();
    }

    @Override
    public void onBackPressed() {
        mMainRouter.onBackPressed();
    }

    public void updateToolbarTitle(@NonNull String title) {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(title);
        }
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

}
