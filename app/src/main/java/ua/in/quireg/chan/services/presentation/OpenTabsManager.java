package ua.in.quireg.chan.services.presentation;

import ua.in.quireg.chan.common.MainApplication;
import ua.in.quireg.chan.common.utils.StringUtils;
import ua.in.quireg.chan.db.HistoryDataSource;
import ua.in.quireg.chan.interfaces.IWebsite;
import ua.in.quireg.chan.models.presentation.OpenTabModel;
import ua.in.quireg.chan.services.NavigationService;

import java.util.ArrayList;

import javax.inject.Inject;

public class OpenTabsManager {

    @Inject protected HistoryDataSource mDataSource;

    private ArrayList<OpenTabModel> mTabs = new ArrayList<>();


    public OpenTabsManager() {
        MainApplication.getAppComponent().inject(this);
    }

    public OpenTabModel add(OpenTabModel newTab) {
        // Не добавляем, если уже добавлено
        for (OpenTabModel openTab : mTabs) {
            if (openTab.isEqualTo(newTab)) {
                return openTab;
            }
        }

        mTabs.add(0, newTab);
        mDataSource.addHistory(newTab.getWebsite().name(), newTab.getBoard(), newTab.getThread(), newTab.getTitle());

        return newTab;
    }

    public ArrayList<OpenTabModel> getOpenTabs() {
        return new ArrayList<>(mTabs);
    }

    public void remove(OpenTabModel tab) {
        mTabs.remove(tab);
    }

    public void removeAll() {
        mTabs.clear();
    }

    public void navigate(OpenTabModel tab) {
        if (StringUtils.isEmpty(tab.getThread())) {
            NavigationService.getInstance().navigateBoard(tab.getWebsite().name(), tab.getBoard());
        } else {
            NavigationService.getInstance().navigateThread(tab.getWebsite().name(), tab.getBoard(), tab.getThread(), tab.getTitle(), null, false);

        }
    }

    public OpenTabModel getByUri(IWebsite website, String board, String thread, int page) {
        for (OpenTabModel model : mTabs) {
            if (model.isEqualTo(website, board, thread, page)) {
                return model;
            }
        }

        return null;
    }
}
