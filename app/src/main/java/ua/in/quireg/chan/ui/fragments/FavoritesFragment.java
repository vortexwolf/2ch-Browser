package ua.in.quireg.chan.ui.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.arellomobile.mvp.MvpAppCompatFragment;

import javax.inject.Inject;

import timber.log.Timber;
import ua.in.quireg.chan.R;
import ua.in.quireg.chan.common.Constants;
import ua.in.quireg.chan.common.MainApplication;
import ua.in.quireg.chan.common.utils.AppearanceUtils;
import ua.in.quireg.chan.common.utils.CompatibilityUtils;
import ua.in.quireg.chan.common.utils.StringUtils;
import ua.in.quireg.chan.db.FavoritesDataSource;
import ua.in.quireg.chan.db.FavoritesEntity;
import ua.in.quireg.chan.mvp.routing.MainRouter;
import ua.in.quireg.chan.ui.adapters.FavoritesAdapter;

public class FavoritesFragment extends MvpAppCompatFragment {

    @Inject FavoritesDataSource mDatasource;
    @Inject MainRouter mMainRouter;

    private FavoritesAdapter mAdapter;
    private ListView mListView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        MainApplication.getAppComponent().inject(this);
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.favorites_list_view, container, false);

        mListView = view.findViewById(android.R.id.list);
        mListView.setNestedScrollingEnabled(true);

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if ((getActivity()) != null) {
            ActionBar mActionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
            if (mActionBar != null) {
                mActionBar.setTitle(getString(R.string.tabs_bookmarks));
            }
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (mAdapter == null) {
            mAdapter = new FavoritesAdapter(getActivity(), mDatasource);
        }

        mListView.setAdapter(mAdapter);
        mAdapter.clear();
        mAdapter.addAll(mDatasource.getFavoriteThreads());

        registerForContextMenu(mListView);

        mListView.setOnItemClickListener((adapterView, view1, position, l) -> {
            FavoritesEntity item = mAdapter.getItem(position);

            if (item == null) {
                Timber.e("item == null, position %d", position);
                return;
            }
            if (StringUtils.isEmpty(item.getThread())) {
                mMainRouter.navigateBoard(item.getWebsite(), item.getBoard(), true);
            } else {
                mMainRouter.navigateThread(item.getThread(), false);
            }
        });
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        menu.add(Menu.NONE, Constants.CONTEXT_MENU_COPY_URL, 0, getString(R.string.cmenu_copy_url));
        menu.add(Menu.NONE, Constants.CONTEXT_MENU_REMOVE_FAVORITES, 0, getString(R.string.cmenu_remove_from_favorites));
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if (!getUserVisibleHint()) {
            return false;
        }

        AdapterView.AdapterContextMenuInfo menuInfo = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        FavoritesEntity model = mAdapter.getItem(menuInfo.position);

        if (model == null) {
            Timber.e("FavoritesEntity model == null, position %d", menuInfo.position);
            return false;
        }

        switch (item.getItemId()) {
            case Constants.CONTEXT_MENU_COPY_URL: {
                String uri = model.buildUrl();
                CompatibilityUtils.copyText(getActivity(), uri, uri);

                AppearanceUtils.showLongToast(getActivity(), uri);
                return true;
            }
            case Constants.CONTEXT_MENU_REMOVE_FAVORITES: {
                // removes from the SQL data source and from UI
                mAdapter.removeItem(model);
                return true;
            }
        }

        return false;
    }
}
