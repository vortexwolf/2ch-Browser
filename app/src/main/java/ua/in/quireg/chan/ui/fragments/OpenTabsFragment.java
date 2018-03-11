package ua.in.quireg.chan.ui.fragments;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.arellomobile.mvp.MvpAppCompatFragment;
import com.arellomobile.mvp.presenter.InjectPresenter;

import javax.inject.Inject;

import timber.log.Timber;
import ua.in.quireg.chan.R;
import ua.in.quireg.chan.common.Constants;
import ua.in.quireg.chan.common.MainApplication;
import ua.in.quireg.chan.db.FavoritesDataSource;
import ua.in.quireg.chan.models.presentation.OpenTabModel;
import ua.in.quireg.chan.mvp.presenters.OpenTabsPresenter;
import ua.in.quireg.chan.mvp.views.OpenTabsView;
import ua.in.quireg.chan.ui.adapters.OpenTabsRecyclerViewAdapter;
import ua.in.quireg.chan.ui.views.RecyclerViewWithCM;

public class OpenTabsFragment extends MvpAppCompatFragment implements OpenTabsView {

    @Inject FavoritesDataSource mFavoritesDatasource;
    @InjectPresenter OpenTabsPresenter mOpenTabsPresenter;

    private OpenTabsRecyclerViewAdapter mOpenTabsRecyclerViewAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MainApplication.getAppComponent().inject(this);

        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.open_tabs_list_view, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if ((getActivity()) != null) {
            ActionBar mActionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
            if (mActionBar != null) {
                mActionBar.setTitle(getString(R.string.tabs_opentabs));
            }
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (mOpenTabsRecyclerViewAdapter == null) {
            mOpenTabsRecyclerViewAdapter = new OpenTabsRecyclerViewAdapter(mOpenTabsPresenter);
        }
        LinearLayoutManager layoutManager = new LinearLayoutManager(view.getContext());

        RecyclerViewWithCM mRecyclerViewWithCM = view.findViewById(R.id.opened_tabs_list);
        mRecyclerViewWithCM.setLayoutManager(layoutManager);
        mRecyclerViewWithCM.addItemDecoration(new DividerItemDecoration(mRecyclerViewWithCM.getContext(), layoutManager.getOrientation()));
        mRecyclerViewWithCM.setAdapter(mOpenTabsRecyclerViewAdapter);

        registerForContextMenu(mRecyclerViewWithCM);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.opentabs, menu);

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_close_tabs_id:
                mOpenTabsPresenter.removeAllItems();
                break;
        }
        return true;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        RecyclerViewWithCM.ContextMenuInfo info = (RecyclerViewWithCM.ContextMenuInfo) menuInfo;

        OpenTabModel item = mOpenTabsRecyclerViewAdapter.getItem(info.position);

        if (item == null) {
            Timber.e("OpenTabModel item == null");
            return;
        }

        menu.add(Menu.NONE, Constants.CONTEXT_MENU_COPY_URL, 0, getString(R.string.cmenu_copy_url));

        if (!item.isFavorite()) {
            menu.add(Menu.NONE, Constants.CONTEXT_MENU_ADD_FAVORITES, 0, getString(R.string.cmenu_add_to_favorites));
        } else {
            menu.add(Menu.NONE, Constants.CONTEXT_MENU_REMOVE_FAVORITES, 0, getString(R.string.cmenu_remove_from_favorites));
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem menuItem) {
        if (!getUserVisibleHint()) {
            return false;
        }
        RecyclerViewWithCM.ContextMenuInfo menuInfo = (RecyclerViewWithCM.ContextMenuInfo) menuItem.getMenuInfo();

        OpenTabModel item = mOpenTabsRecyclerViewAdapter.getItem(menuInfo.position);

        if (item == null) {
            Timber.e("OpenTabModel model == null");
            return false;
        }

        switch (menuItem.getItemId()) {
            case Constants.CONTEXT_MENU_COPY_URL: {
                String uri = item.buildUrl();
                if (getActivity() == null) {
                    Timber.e("getActivity() == null");
                    return false;
                }
                ClipboardManager clipboard = (ClipboardManager) getActivity().getApplicationContext().getSystemService(Context.CLIPBOARD_SERVICE);
                if (clipboard != null) {
                    clipboard.setPrimaryClip(ClipData.newPlainText(uri, uri));
                }
                Toast.makeText(getActivity(), uri, Toast.LENGTH_SHORT).show();

                return true;
            }
            case Constants.CONTEXT_MENU_ADD_FAVORITES: {
                mFavoritesDatasource.addToFavorites(item.getWebsite().name(), item.getBoard(), item.getThread(), item.getTitle());
                return true;
            }
            case Constants.CONTEXT_MENU_REMOVE_FAVORITES: {
                mFavoritesDatasource.removeFromFavorites(item.getWebsite().name(), item.getBoard(), item.getThread());
                return true;
            }
        }
        return false;
    }

    @Override
    public void add(OpenTabModel model) {
        mOpenTabsRecyclerViewAdapter.addToList(model);
    }

    @Override
    public void remove(OpenTabModel model) {
        mOpenTabsRecyclerViewAdapter.removeFromList(model);
    }

    @Override
    public void clearAll() {
        mOpenTabsRecyclerViewAdapter.clearList();
    }
}
