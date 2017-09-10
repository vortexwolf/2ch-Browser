package ua.in.quireg.chan.services.presentation;

import ua.in.quireg.chan.common.utils.StringUtils;
import ua.in.quireg.chan.db.HistoryDataSource;
import ua.in.quireg.chan.interfaces.IWebsite;
import ua.in.quireg.chan.models.presentation.OpenTabModel;
import ua.in.quireg.chan.services.NavigationService;

import java.util.ArrayList;

public class OpenTabsManager {
    private final ArrayList<OpenTabModel> mTabs = new ArrayList<OpenTabModel>();

    private final HistoryDataSource mDataSource;

    public OpenTabsManager(HistoryDataSource dataSource) {
        this.mDataSource = dataSource;
    }

    public OpenTabModel add(OpenTabModel newTab) {
        // Не добавляем, если уже добавлено
        for (OpenTabModel openTab : this.mTabs) {
            if (openTab.isEqualTo(newTab)) {
                return openTab;
            }
        }

        this.mTabs.add(0, newTab);
        this.mDataSource.addHistory(newTab.getWebsite().name(), newTab.getBoard(), newTab.getThread(), newTab.getTitle());

        return newTab;
    }

    @SuppressWarnings("unchecked")
    public ArrayList<OpenTabModel> getOpenTabs() {
        return (ArrayList<OpenTabModel>) this.mTabs.clone();
    }

    public void remove(OpenTabModel tab) {
        this.mTabs.remove(tab);
    }

    public void removeAll() {
        this.mTabs.clear();
    }

    public void navigate(OpenTabModel tab) {
        if (StringUtils.isEmpty(tab.getThread())) {
            NavigationService.getInstance().navigateBoard(tab.getWebsite().name(), tab.getBoard());
        } else {
            NavigationService.getInstance().navigateThread(tab.getWebsite().name(), tab.getBoard(), tab.getThread(), tab.getTitle(), null, false);

        }
    }

    public OpenTabModel getByUri(IWebsite website, String board, String thread, int page) {
        for (OpenTabModel model : this.mTabs) {
            if (model.isEqualTo(website, board, thread, page)) {
                return model;
            }
        }

        return null;
    }
}
