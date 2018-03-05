package ua.in.quireg.chan.services.presentation;

import java.util.ArrayList;
import java.util.LinkedList;

import javax.inject.Inject;

import ua.in.quireg.chan.common.MainApplication;
import ua.in.quireg.chan.db.HistoryDataSource;
import ua.in.quireg.chan.models.presentation.OpenTabModel;

public class OpenTabsManager {

    @Inject HistoryDataSource mDataSource;

    private ArrayList<OpenTabModel> mOpenedTabs = new ArrayList<>();
    private LinkedList<OpenTabsManager.Callback> mCallbacks = new LinkedList<>();

    public OpenTabsManager() {
        MainApplication.getAppComponent().inject(this);
    }

    public OpenTabModel add(OpenTabModel newTab) {
        // Не добавляем, если уже добавлено
        for (OpenTabModel openTab : mOpenedTabs) {
            if (openTab.isEqualTo(newTab)) {
                return openTab;
            }
        }

        mOpenedTabs.add(0, newTab);
        mDataSource.addHistory(newTab.getWebsite().name(), newTab.getBoard(), newTab.getThread(), newTab.getTitle());

        for (Callback c : mCallbacks) {
            c.onAdd(newTab);
        }

        return newTab;
    }

    public ArrayList<OpenTabModel> getOpenTabs() {
        return new ArrayList<>(mOpenedTabs);
    }

    public void subscribe(OpenTabsManager.Callback callback) {
        mCallbacks.add(callback);
    }

    public void unsubscribe(OpenTabsManager.Callback callback) {
        mCallbacks.remove(callback);
    }

    public void remove(OpenTabModel tab) {
        mOpenedTabs.remove(tab);
        for (Callback c : mCallbacks) {
            c.onRemove(tab);
        }
    }

    public void removeAll() {
        mOpenedTabs.clear();
        for (Callback c : mCallbacks) {
            c.onRemoveAll();
        }
    }

    //    public OpenTabModel getByUri(IWebsite website, String board, String thread, int page) {
//        for (OpenTabModel model : mOpenedTabs) {
//            if (model.isEqualTo(website, board, thread, page)) {
//                return model;
//            }
//        }
//
//        return null;
//    }
    public interface Callback {

        void onAdd(OpenTabModel openTabModel);

        void onRemove(OpenTabModel openTabModel);

        void onRemoveAll();

    }

}
