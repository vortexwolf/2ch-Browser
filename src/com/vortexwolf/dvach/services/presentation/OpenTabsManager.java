package com.vortexwolf.dvach.services.presentation;

import java.util.ArrayList;

import android.app.Activity;
import android.os.Bundle;

import com.vortexwolf.dvach.common.Constants;
import com.vortexwolf.dvach.db.HistoryDataSource;
import com.vortexwolf.dvach.interfaces.INavigationService;
import com.vortexwolf.dvach.interfaces.IOpenTabsManager;
import com.vortexwolf.dvach.models.presentation.OpenTabModel;

public class OpenTabsManager implements IOpenTabsManager {
    private final ArrayList<OpenTabModel> mTabs = new ArrayList<OpenTabModel>();

    private final HistoryDataSource mDataSource;
    private final INavigationService mNavigationService;

    public OpenTabsManager(HistoryDataSource dataSource, INavigationService navigationService) {
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

    @Override
    public ArrayList<OpenTabModel> getOpenTabs() {
        return this.mTabs;
    }

    @Override
    public void remove(OpenTabModel tab) {
        this.mTabs.remove(tab);
    }
    
    public void removeAll() {
        this.mTabs.clear();
    }

    @Override
    public void navigate(OpenTabModel tab, Activity activity) {
        Bundle extras = new Bundle();
        extras.putBoolean(Constants.EXTRA_PREFER_DESERIALIZED, true);

        this.mNavigationService.navigate(tab.getUri(), activity, extras);
    }
}
