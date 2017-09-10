package ua.in.quireg.chan.ui.fragments;

import android.net.Uri;
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

import ua.in.quireg.chan.R;
import ua.in.quireg.chan.adapters.OpenTabsAdapter;
import ua.in.quireg.chan.common.Constants;
import ua.in.quireg.chan.common.Factory;
import ua.in.quireg.chan.common.utils.AppearanceUtils;
import ua.in.quireg.chan.common.utils.CompatibilityUtils;
import ua.in.quireg.chan.db.FavoritesDataSource;
import ua.in.quireg.chan.models.presentation.OpenTabModel;
import ua.in.quireg.chan.services.presentation.OpenTabsManager;

public class OpenTabsFragment extends BaseListFragment {

    private OpenTabsAdapter mAdapter;
    private OpenTabsManager mTabsManager;
    private FavoritesDataSource mFavoritesDatasource;


    private Uri mCurrentUri;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mTabsManager = Factory.getContainer().resolve(OpenTabsManager.class);
        mFavoritesDatasource = Factory.getContainer().resolve(FavoritesDataSource.class);

        Bundle extras = getArguments();
        if (extras != null && extras.containsKey(Constants.EXTRA_CURRENT_URL)) {
            mCurrentUri = Uri.parse(extras.getString(Constants.EXTRA_CURRENT_URL));
        }

        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.open_tabs_list_view, container, false);
        ListView listView = (ListView) view.findViewById(android.R.id.list);
        View listViewHeaderView = inflater.inflate(R.layout.open_tabs_list_header, null);
        listView.addHeaderView(listViewHeaderView);
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mAdapter = new OpenTabsAdapter(getActivity(), mTabsManager.getOpenTabs(), mTabsManager);
        mListView.setAdapter(mAdapter);
        registerForContextMenu(mListView);
        setTitle(getString(R.string.tabs_opentabs));
        mListView.setOnItemClickListener((adapterView, view1, position, l) -> {
            OpenTabModel item = mAdapter.getItem(position - mListView.getHeaderViewsCount());
            mTabsManager.navigate(item);
        });
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.opentabs, menu);

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_close_tabs_id:
                mTabsManager.removeAll();
                mAdapter.clear();
                break;
        }

        return true;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
        OpenTabModel item = mAdapter.getItem(info.position);

        menu.add(Menu.NONE, Constants.CONTEXT_MENU_COPY_URL, 0, getString(R.string.cmenu_copy_url));

        if (!mFavoritesDatasource.hasFavorites(item.getWebsite().name(), item.getBoard(), item.getThread())) {
            menu.add(Menu.NONE, Constants.CONTEXT_MENU_ADD_FAVORITES, 0, getString(R.string.cmenu_add_to_favorites));
        } else {
            menu.add(Menu.NONE, Constants.CONTEXT_MENU_REMOVE_FAVORITES, 0, getString(R.string.cmenu_remove_from_favorites));
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if (!getUserVisibleHint()) {
            return false;
        }

        AdapterView.AdapterContextMenuInfo menuInfo = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        OpenTabModel model = mAdapter.getItem(menuInfo.position);

        switch (item.getItemId()) {
            case Constants.CONTEXT_MENU_COPY_URL: {
                String uri = model.buildUrl();

                CompatibilityUtils.copyText(getActivity(), uri, uri);
                AppearanceUtils.showToastMessage(getActivity(), uri);
                return true;
            }
            case Constants.CONTEXT_MENU_ADD_FAVORITES: {
                mFavoritesDatasource.addToFavorites(model.getWebsite().name(), model.getBoard(), model.getThread(), model.getTitle());
                return true;
            }
            case Constants.CONTEXT_MENU_REMOVE_FAVORITES: {
                mFavoritesDatasource.removeFromFavorites(model.getWebsite().name(), model.getBoard(), model.getThread());
                return true;
            }
        }

        return false;
    }

    @Override
    public void onRefresh() {

    }
}
