package com.vortexwolf.dvach.activities;

import android.app.ListActivity;
import android.content.res.TypedArray;
import android.net.Uri;
import android.os.Bundle;
import android.text.ClipboardManager;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.vortexwolf.dvach.R;
import com.vortexwolf.dvach.adapters.OpenTabsAdapter;
import com.vortexwolf.dvach.common.Constants;
import com.vortexwolf.dvach.common.Factory;
import com.vortexwolf.dvach.common.utils.AppearanceUtils;
import com.vortexwolf.dvach.db.FavoritesDataSource;
import com.vortexwolf.dvach.interfaces.IOpenTabsManager;
import com.vortexwolf.dvach.models.presentation.OpenTabModel;
import com.vortexwolf.dvach.settings.ApplicationSettings;

public class OpenTabsActivity extends ListActivity {

    private OpenTabsAdapter mAdapter;
    private IOpenTabsManager mTabsManager;
    private FavoritesDataSource mFavoritesDatasource;
    private ApplicationSettings mApplicationSettings;

    private Uri mCurrentUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.mTabsManager = Factory.getContainer().resolve(IOpenTabsManager.class);
        this.mFavoritesDatasource = Factory.getContainer().resolve(FavoritesDataSource.class);
        this.mApplicationSettings = Factory.getContainer().resolve(ApplicationSettings.class);

        Bundle extras = this.getIntent().getExtras();
        if (extras != null) {
            this.mCurrentUri = Uri.parse(extras.getString(Constants.EXTRA_CURRENT_URL));
        }

        this.resetUI();

        this.mAdapter = new OpenTabsAdapter(this, this.mTabsManager.getOpenTabs(), this.mTabsManager);
        this.getListView().setAdapter(this.mAdapter);
    }

    private void resetUI() {
        this.setTheme(this.mApplicationSettings.getTheme());
        TypedArray a = this.getTheme().obtainStyledAttributes(R.styleable.Theme);
        this.getListView().setBackgroundColor(a.getColor(R.styleable.Theme_activityRootBackground, -1));

        this.registerForContextMenu(this.getListView());
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        OpenTabModel item = this.mAdapter.getItem(position);
        if (item.getUri().equals(this.mCurrentUri)) {
            this.finish();
        } else {
            this.mTabsManager.navigate(item, this);
        }
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_close_tabs_id:
                this.mTabsManager.removeAll();
                this.mAdapter.clear();
                break;
        }

        return true;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
        OpenTabModel item = this.mAdapter.getItem(info.position);

        menu.add(Menu.NONE, Constants.CONTEXT_MENU_COPY_URL, 0, this.getString(R.string.cmenu_copy_url));

        if (!this.mFavoritesDatasource.hasFavorites(item.getUri().toString())) {
            menu.add(Menu.NONE, Constants.CONTEXT_MENU_ADD_FAVORITES, 0, this.getString(R.string.cmenu_add_to_favorites));
        } else {
            menu.add(Menu.NONE, Constants.CONTEXT_MENU_REMOVE_FAVORITES, 0, this.getString(R.string.cmenu_remove_from_favorites));
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo menuInfo = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        OpenTabModel model = this.mAdapter.getItem(menuInfo.position);

        switch (item.getItemId()) {
            case Constants.CONTEXT_MENU_COPY_URL: {
                ClipboardManager clipboard = (ClipboardManager) this.getSystemService(CLIPBOARD_SERVICE);
                clipboard.setText(model.getUri().toString());

                AppearanceUtils.showToastMessage(this, model.getUri().toString());
                return true;
            }
            case Constants.CONTEXT_MENU_ADD_FAVORITES: {
                this.mFavoritesDatasource.addToFavorites(model.getTitle(), model.getUri().toString());
                return true;
            }
            case Constants.CONTEXT_MENU_REMOVE_FAVORITES: {
                this.mFavoritesDatasource.removeFromFavorites(model.getUri().toString());
                return true;
            }
        }

        return false;
    }
}
