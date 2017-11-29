package ua.in.quireg.chan.ui.activities;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.ncapdevi.fragnav.FragNavController;
import com.ncapdevi.fragnav.FragNavTransactionOptions;

import javax.inject.Inject;

import butterknife.BindArray;
import butterknife.BindView;
import butterknife.ButterKnife;
import ua.in.quireg.chan.R;
import ua.in.quireg.chan.common.MainApplication;
import ua.in.quireg.chan.common.utils.AppearanceUtils;
import ua.in.quireg.chan.services.NavigationService;
import ua.in.quireg.chan.settings.ApplicationSettings;
import ua.in.quireg.chan.ui.views.FragmentHistory;
import ua.in.quireg.chan.ui.fragments.BoardsListFragment;
import ua.in.quireg.chan.ui.fragments.FavoritesFragment;
import ua.in.quireg.chan.ui.fragments.HistoryFragment;
import ua.in.quireg.chan.ui.fragments.OpenTabsFragment;
import ua.in.quireg.chan.ui.fragments.AppPreferenceFragment;


public class BaseActivity extends AppCompatActivity implements FragNavController.TransactionListener, FragNavController.RootFragmentListener {

    //TODO add settings change listener

    @Inject protected ApplicationSettings mApplicationSettings;

    @BindArray(R.array.tab_name) protected String[] TABS;
    @BindView(R.id.toolbar) protected Toolbar mToolbar;
    @BindView(R.id.bottom_tab) protected TabLayout mBottomTabLayout;

    private FragNavController mNavController;
    private FragmentHistory mFragmentHistory;

    private int[] mTabIconsSelected = {
            R.drawable.browser_home,
            R.drawable.browser_tabs,
            R.drawable.browser_favourites,
            R.drawable.browser_history,
            R.drawable.browser_settings
    };

    boolean mDoubleBackToExitPressedOnce = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        MainApplication.getComponent().inject(this);

        setTheme(mApplicationSettings.getTheme());
        setContentView(R.layout.base_activity);

        ButterKnife.bind(this);

        setSupportActionBar(mToolbar);
        getSupportActionBar().setLogo(R.drawable.browser_logo_drawable);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        updateToolbar(true);
        initBottomTabs();

        super.onCreate(savedInstanceState);


        mFragmentHistory = new FragmentHistory();
        FragNavTransactionOptions options = FragNavTransactionOptions.newBuilder()
                .transition(FragmentTransaction.TRANSIT_NONE)
                //.transition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                .build();

        mNavController = FragNavController.newBuilder(savedInstanceState, getSupportFragmentManager(), R.id.container)
                .transactionListener(this)
                .rootFragmentListener(this, TABS.length)
                .defaultTransactionOptions(options)
                .build();

        NavigationService.init(mNavController);

        mNavController.switchTab(0);

        mBottomTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                mNavController.switchTab(tab.getPosition());
                mFragmentHistory.push(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                onTabSelected(tab);
            }
        });

    }

    @Override
    public void onBackPressed() {

        if (!mNavController.isRootFragment()) {
            //Non-root fragment on top, pop it
            mNavController.popFragment();
        } else {
            //root fragment is visible, let's check fragment commit history.
            if (mFragmentHistory.isEmpty()) {
                //No history, proceed with exit
                if (mDoubleBackToExitPressedOnce) {
                    super.onBackPressed();
                    return;
                }
                waitForAnotherPressToExit();

            } else {
                if (mFragmentHistory.getStackSize() > 1) {
                    //History is there. let's go to previous root fragment that has been opened.
                    int position = mFragmentHistory.pop();
                    mNavController.switchTab(position);
                    updateTabSelection(position);

                } else {
                    //single fragment in stack, go to home fragment.
                    mFragmentHistory.pop();
                    updateTabSelection(0);
                    mNavController.switchTab(0);
                    mFragmentHistory.emptyStack();

                }
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mNavController != null) {
            mNavController.onSaveInstanceState(outState);
        }
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

    @Override
    public void onTabTransaction(Fragment fragment, int i) {
        // If we have a backstack, show the back button
        if (getSupportActionBar() != null && mNavController != null) {
            updateToolbar(mNavController.isRootFragment());
        }
    }

    @Override
    public void onFragmentTransaction(Fragment fragment, FragNavController.TransactionType transactionType) {
        if (getSupportActionBar() != null && mNavController != null) {
            updateToolbar(mNavController.isRootFragment());
        }
    }

    private void initBottomTabs() {
        for (int i = 0; i < TABS.length; i++) {
            mBottomTabLayout.addTab(mBottomTabLayout.newTab());
            TabLayout.Tab tab = mBottomTabLayout.getTabAt(i);
            if (tab != null)
                tab.setCustomView(getTabView(i));
        }
    }

    private void updateToolbar(boolean isRootFrag) {
        if (getSupportActionBar() == null) {
            return;
        }
        getSupportActionBar().setDisplayUseLogoEnabled(isRootFrag);
        getSupportActionBar().setDisplayHomeAsUpEnabled(!isRootFrag);
    }

    private void updateTabSelection(int currentTab) {

        for (int i = 0; i < TABS.length; i++) {
            TabLayout.Tab selectedTab = mBottomTabLayout.getTabAt(i);
            if (currentTab != i) {
                selectedTab.getCustomView().setSelected(false);
            } else {
                selectedTab.getCustomView().setSelected(true);
            }
        }
    }

    private View getTabView(int position) {
        View view = LayoutInflater.from(this).inflate(R.layout.tab_item_bottom, mBottomTabLayout, false);

        ImageView icon = view.findViewById(R.id.tab_icon);
        Drawable stateListDrawable = AppearanceUtils.getStateListDrawable(this, mTabIconsSelected[position]);
        icon.setImageDrawable(stateListDrawable);

        return view;
    }

    private void waitForAnotherPressToExit() {
        mDoubleBackToExitPressedOnce = true;

        Toast.makeText(this, "Press again to exit", Toast.LENGTH_SHORT).show();

        new Handler().postDelayed(() -> mDoubleBackToExitPressedOnce = false, 2000);
    }



}
