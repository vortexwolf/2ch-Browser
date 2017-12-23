package ua.in.quireg.chan.ui.fragments;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import java.util.List;

import javax.inject.Inject;

import ua.in.quireg.chan.R;
import ua.in.quireg.chan.common.Constants;
import ua.in.quireg.chan.common.MainApplication;
import ua.in.quireg.chan.common.utils.AppearanceUtils;
import ua.in.quireg.chan.common.utils.CompatibilityUtils;
import ua.in.quireg.chan.common.utils.StringUtils;
import ua.in.quireg.chan.db.FavoritesDataSource;
import ua.in.quireg.chan.db.FavoritesEntity;
import ua.in.quireg.chan.ui.adapters.FavoritesAdapter;

public class FavoritesFragment extends BaseListFragment {


    @Inject FavoritesDataSource mDatasource;
    //@Inject MainActivityPresenter mNavigationController;

    private FavoritesAdapter mAdapter;
    private OpenDataSourceTask mCurrentTask = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        MainApplication.getAppComponent().inject(this);
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if(mAdapter == null){
            mAdapter = new FavoritesAdapter(getActivity(), mDatasource);
        }
        mDatasource.resetModifiedState();

        return inflater.inflate(R.layout.favorites_list_view, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mCurrentTask = new OpenDataSourceTask();
        mCurrentTask.execute();
        mListView.setAdapter(mAdapter);
        setTitle(getString(R.string.tabs_bookmarks));
        registerForContextMenu(mListView);
        mListView.setOnItemClickListener((adapterView, view1, position, l) -> {
            FavoritesEntity item = mAdapter.getItem(position);

            if (StringUtils.isEmpty(item.getThread())) {
                //mNavigationController.navigateBoard(item.getWebsite(), item.getBoard());
            } else {
                //mNavigationController.navigateThread(item.getWebsite(), item.getBoard(), item.getThread(), item.getTitle(), null, false);
            }
        });
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (!isAdded()) {
            return;
        }

        if (isVisibleToUser && mDatasource.isModified()) {
            mDatasource.resetModifiedState();
            mCurrentTask = new OpenDataSourceTask();
            mCurrentTask.execute();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mDatasource.isModified()) {
            mDatasource.resetModifiedState();
            mCurrentTask = new OpenDataSourceTask();
            mCurrentTask.execute();
        }
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

    @Override
    public void onRefresh() {
        
    }

    private class OpenDataSourceTask extends AsyncTask<Void, Void, List<FavoritesEntity>> {

        @Override
        protected List<FavoritesEntity> doInBackground(Void... arg0) {
            return mDatasource.getFavoriteThreads();
        }

        @Override
        protected void onPostExecute(List<FavoritesEntity> result) {
            mAdapter.clear();
            for (FavoritesEntity item : result) {
                mAdapter.add(item);
            }

            switchToListView();
        }

        @Override
        protected void onPreExecute() {
            switchToLoadingView();
        }
    }
}
