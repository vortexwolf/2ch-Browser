package ua.in.quireg.chan.views.activities;

import android.annotation.SuppressLint;
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
import ua.in.quireg.chan.views.controls.FragmentHistory;
import ua.in.quireg.chan.views.fragments.BoardsListFragment;
import ua.in.quireg.chan.views.fragments.FavoritesFragment;
import ua.in.quireg.chan.views.fragments.HistoryFragment;
import ua.in.quireg.chan.views.fragments.OpenTabsFragment;
import ua.in.quireg.chan.views.fragments.AppPreferenceFragment;


public class BaseActivity extends AppCompatActivity implements FragNavController.TransactionListener, FragNavController.RootFragmentListener {

    //TODO add settings change listener

    @Inject protected ApplicationSettings mApplicationSettings;

    private FragNavController mNavController;
    private FragmentHistory fragmentHistory;

    boolean doubleBackToExitPressedOnce = false;

    private int[] mTabIconsSelected = {
            R.drawable.browser_home,
            R.drawable.browser_tabs,
            R.drawable.browser_favourites,
            R.drawable.browser_history,
            R.drawable.browser_settings};

    @BindArray(R.array.tab_name) String[] TABS;
    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.bottom_tab) TabLayout bottomTabLayout;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MainApplication.getComponent().inject(this);

        setTheme(mApplicationSettings.getTheme());
        setContentView(R.layout.base_activity);

        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        initTab();

        fragmentHistory = new FragmentHistory();
        FragNavTransactionOptions options = FragNavTransactionOptions
                .newBuilder()
                .transition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .build();

        mNavController = FragNavController.newBuilder(savedInstanceState, getSupportFragmentManager(), R.id.container)
                .transactionListener(this)
                .rootFragmentListener(this, TABS.length)
                .defaultTransactionOptions(options)
                .build();

        NavigationService.init(mNavController);

        mNavController.switchTab(0);

        bottomTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                mNavController.switchTab(tab.getPosition());
                fragmentHistory.push(tab.getPosition());
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

    private void initTab() {
        for (int i = 0; i < TABS.length; i++) {
            bottomTabLayout.addTab(bottomTabLayout.newTab());
            TabLayout.Tab tab = bottomTabLayout.getTabAt(i);
            if (tab != null)
                tab.setCustomView(getTabView(i));
        }
    }

    @SuppressLint("InflateParams")
    private View getTabView(int position) {
        View view = LayoutInflater.from(getApplicationContext()).inflate(R.layout.tab_item_bottom, null);

        ImageView icon = (ImageView) view.findViewById(R.id.tab_icon);
        Drawable stateListDrawable = AppearanceUtils.getStateListDrawable(this, mTabIconsSelected[position]);
        icon.setImageDrawable(stateListDrawable);

        return view;
    }

    @Override
    public void onBackPressed() {

        if (!mNavController.isRootFragment()) {
            //Non-root fragment on top, pop it
            mNavController.popFragment();
        } else {
            //root fragment is visible, let's check fragment commit history.
            if (fragmentHistory.isEmpty()) {
                //No history, proceed with exit
                if (doubleBackToExitPressedOnce) {
                    super.onBackPressed();
                    return;
                }
                waitForAnotherPressToExit();

            } else {
                if (fragmentHistory.getStackSize() > 1) {
                    //History is there. let's go to previous root fragment that has been opened.
                    int position = fragmentHistory.pop();
                    mNavController.switchTab(position);
                    updateTabSelection(position);

                } else {
                    //single fragment in stack, go to home fragment.
                    fragmentHistory.pop();
                    updateTabSelection(0);
                    mNavController.switchTab(0);
                    fragmentHistory.emptyStack();

                }
            }
        }
    }

    private void waitForAnotherPressToExit() {
        doubleBackToExitPressedOnce = true;

        Toast.makeText(this, "Press again to exit", Toast.LENGTH_SHORT).show();

        new Handler().postDelayed(() -> doubleBackToExitPressedOnce = false, 2000);
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
            updateToolbar();
        }
    }

    @Override
    public void onFragmentTransaction(Fragment fragment, FragNavController.TransactionType transactionType) {
        if (getSupportActionBar() != null && mNavController != null) {
            updateToolbar();

        }
    }

    private void updateToolbar() {
        if (getSupportActionBar() == null) {
            return;
        }
        getSupportActionBar().setDisplayHomeAsUpEnabled(!mNavController.isRootFragment());
        getSupportActionBar().setDisplayShowHomeEnabled(!mNavController.isRootFragment());
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_arrow_back);
    }

    private void updateTabSelection(int currentTab) {

        for (int i = 0; i < TABS.length; i++) {
            TabLayout.Tab selectedTab = bottomTabLayout.getTabAt(i);
            if (currentTab != i) {
                selectedTab.getCustomView().setSelected(false);
            } else {
                selectedTab.getCustomView().setSelected(true);
            }
        }
    }
}
