package com.vortexwolf.chan.activities;

import java.util.List;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.vortexwolf.chan.R;
import com.vortexwolf.chan.adapters.FavoritesAdapter;
import com.vortexwolf.chan.common.Constants;
import com.vortexwolf.chan.common.Factory;
import com.vortexwolf.chan.common.utils.AppearanceUtils;
import com.vortexwolf.chan.common.utils.CompatibilityUtils;
import com.vortexwolf.chan.common.utils.StringUtils;
import com.vortexwolf.chan.db.FavoritesDataSource;
import com.vortexwolf.chan.db.FavoritesEntity;
import com.vortexwolf.chan.services.NavigationService;

public class FavoritesFragment extends BaseListFragment {
    private FavoritesDataSource mDatasource;
    private NavigationService mNavigationService;

    private FavoritesAdapter mAdapter;
    private OpenDataSourceTask mCurrentTask = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.mDatasource = Factory.getContainer().resolve(FavoritesDataSource.class);
        this.mNavigationService = Factory.getContainer().resolve(NavigationService.class);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.favorites_list_view, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        this.mAdapter = new FavoritesAdapter(this.getActivity(), this.mDatasource);
        this.setListAdapter(this.mAdapter);

        this.registerForContextMenu(this.getListView());

        this.mDatasource.resetModifiedState();
        this.mCurrentTask = new OpenDataSourceTask();
        this.mCurrentTask.execute();
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (!this.isAdded()) {
            return;
        }

        if (isVisibleToUser && this.mDatasource.isModified()) {
            this.mDatasource.resetModifiedState();
            this.mCurrentTask = new OpenDataSourceTask();
            this.mCurrentTask.execute();
        }
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        FavoritesEntity item = this.mAdapter.getItem(position);

        if (StringUtils.isEmpty(item.getThread())) {
            this.mNavigationService.navigateBoardPage(this.getActivity(), null, item.getWebsite(), item.getBoard(), 0, true);
        } else {
            this.mNavigationService.navigateThread(this.getActivity(), null, item.getWebsite(), item.getBoard(), item.getThread(), item.getTitle(), null, true);
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        menu.add(Menu.NONE, Constants.CONTEXT_MENU_COPY_URL, 0, this.getString(R.string.cmenu_copy_url));
        menu.add(Menu.NONE, Constants.CONTEXT_MENU_REMOVE_FAVORITES, 0, this.getString(R.string.cmenu_remove_from_favorites));
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if (!this.getUserVisibleHint()) {
            return false;
        }

        AdapterView.AdapterContextMenuInfo menuInfo = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        FavoritesEntity model = this.mAdapter.getItem(menuInfo.position);

        switch (item.getItemId()) {
            case Constants.CONTEXT_MENU_COPY_URL: {
                String uri = model.buildUrl();
                CompatibilityUtils.copyText(this.getActivity(), uri, uri);

                AppearanceUtils.showToastMessage(this.getActivity(), uri);
                return true;
            }
            case Constants.CONTEXT_MENU_REMOVE_FAVORITES: {
                // removes from the SQL data source and from UI
                this.mAdapter.removeItem(model);
                return true;
            }
        }

        return false;
    }

    private class OpenDataSourceTask extends AsyncTask<Void, Void, List<FavoritesEntity>> {

        @Override
        protected List<FavoritesEntity> doInBackground(Void... arg0) {
            List<FavoritesEntity> favorites = FavoritesFragment.this.mDatasource.getFavoriteThreads();
            return favorites;
        }

        @Override
        protected void onPostExecute(List<FavoritesEntity> result) {
            FavoritesFragment.this.mAdapter.clear();
            for (FavoritesEntity item : result) {
                FavoritesFragment.this.mAdapter.add(item);
            }

            FavoritesFragment.this.switchToListView();
        }

        @Override
        protected void onPreExecute() {
            FavoritesFragment.this.switchToLoadingView();
        }
    }
}
