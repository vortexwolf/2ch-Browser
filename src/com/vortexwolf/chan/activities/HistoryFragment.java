package com.vortexwolf.chan.activities;

import java.util.List;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.vortexwolf.chan.R;
import com.vortexwolf.chan.adapters.HistoryAdapter;
import com.vortexwolf.chan.common.Constants;
import com.vortexwolf.chan.common.Factory;
import com.vortexwolf.chan.common.utils.AppearanceUtils;
import com.vortexwolf.chan.common.utils.CompatibilityUtils;
import com.vortexwolf.chan.db.FavoritesDataSource;
import com.vortexwolf.chan.db.HistoryDataSource;
import com.vortexwolf.chan.db.HistoryEntity;
import com.vortexwolf.chan.services.NavigationService;

public class HistoryFragment extends BaseListFragment {
    private HistoryDataSource mDatasource;
    private FavoritesDataSource mFavoritesDatasource;
    private NavigationService mNavigationService;

    private HistoryAdapter mAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.mDatasource = Factory.getContainer().resolve(HistoryDataSource.class);
        this.mFavoritesDatasource = Factory.getContainer().resolve(FavoritesDataSource.class);
        this.mNavigationService = Factory.getContainer().resolve(NavigationService.class);

        this.setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.history_list_view, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        this.mAdapter = new HistoryAdapter(this.getActivity(), this.mFavoritesDatasource);
        this.setListAdapter(this.mAdapter);

        this.registerForContextMenu(this.getListView());

        new OpenDataSourceTask().execute();
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser && this.mAdapter != null) {
            // update favorites icons
            this.mAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        HistoryEntity item = this.mAdapter.getItem(position);

        this.mNavigationService.navigate(Uri.parse(item.getUrl()), this.getActivity());
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.history, menu);

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_clear_history_id:
                this.mDatasource.deleteAllHistory();
                this.mAdapter.clear();
                break;
        }

        return true;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        menu.add(Menu.NONE, Constants.CONTEXT_MENU_COPY_URL, 0, this.getString(R.string.cmenu_copy_url));
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo menuInfo = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        HistoryEntity model = this.mAdapter.getItem(menuInfo.position);

        switch (item.getItemId()) {
            case Constants.CONTEXT_MENU_COPY_URL: {
                String uri = model.getUrl();
                CompatibilityUtils.copyText(this.getActivity(), uri, uri);

                AppearanceUtils.showToastMessage(this.getActivity(), model.getUrl());
                return true;
            }
        }

        return false;
    }

    private class OpenDataSourceTask extends AsyncTask<Void, Void, List<HistoryEntity>> {

        @Override
        protected List<HistoryEntity> doInBackground(Void... arg0) {
            List<HistoryEntity> historyItems = HistoryFragment.this.mDatasource.getAllHistory();
            return historyItems;
        }

        @Override
        protected void onPostExecute(List<HistoryEntity> result) {
            HistoryFragment.this.mAdapter.clear();
            for (HistoryEntity item : result) {
                HistoryFragment.this.mAdapter.add(item);
            }

            HistoryFragment.this.switchToListView();
        }

        @Override
        protected void onPreExecute() {
            HistoryFragment.this.switchToLoadingView();
        }
    }
}
