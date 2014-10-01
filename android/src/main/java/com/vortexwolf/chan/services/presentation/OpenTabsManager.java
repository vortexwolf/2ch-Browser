package com.vortexwolf.chan.services.presentation;

import java.util.ArrayList;

import android.app.Activity;
import android.net.Uri;

import com.vortexwolf.chan.db.HistoryDataSource;
import com.vortexwolf.chan.interfaces.IOpenTabsManager;
import com.vortexwolf.chan.models.presentation.OpenTabModel;
import com.vortexwolf.chan.services.NavigationService;

public class OpenTabsManager implements IOpenTabsManager {
    private final ArrayList<OpenTabModel> mTabs = new ArrayList<OpenTabModel>();

    private final HistoryDataSource mDataSource;
    private final NavigationService mNavigationService;

    public OpenTabsManager(HistoryDataSource dataSource, NavigationService navigationService) {
        this.mDataSource = dataSource;
        this.mNavigationService = navigationService;
    }

    @Override
    public OpenTabModel add(OpenTabModel newTab) {
        // Не добавляем, если уже добавлено
        for (OpenTabModel openTab : this.mTabs) {
            if (openTab.getUri().equals(newTab.getUri())) {
                return openTab;
            }
        }

        this.mTabs.add(0, newTab);
        this.mDataSource.addHistory(newTab.getTitle(), newTab.getUri().toString());

        return newTab;
    }

    @SuppressWarnings("unchecked")
    @Override
    public ArrayList<OpenTabModel> getOpenTabs() {
        return (ArrayList<OpenTabModel>) this.mTabs.clone();
    }

    @Override
    public void remove(OpenTabModel tab) {
        this.mTabs.remove(tab);
    }

    @Override
    public void removeAll() {
        this.mTabs.clear();
    }

    @Override
    public void navigate(OpenTabModel tab, Activity activity) {
        this.mNavigationService.navigate(tab.getUri(), activity);
    }

    public OpenTabModel getByUri(Uri uri) {
        for (OpenTabModel model : this.mTabs) {
            if (model.getUri().equals(uri)) {
                return model;
            }
        }

        return null;
    }
}
