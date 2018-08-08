package ua.in.quireg.chan.mvp.presenters;

import com.arellomobile.mvp.InjectViewState;
import com.arellomobile.mvp.MvpPresenter;

import javax.inject.Inject;

import ua.in.quireg.chan.common.MainApplication;
import ua.in.quireg.chan.common.utils.StringUtils;
import ua.in.quireg.chan.models.presentation.OpenTabModel;
import ua.in.quireg.chan.mvp.routing.MainRouter;
import ua.in.quireg.chan.mvp.views.OpenTabsView;
import ua.in.quireg.chan.services.presentation.OpenTabsManager;

/**
 * Created by Arcturus Mengsk on 2/15/2018, 2:51 PM.
 * 2ch-Browser
 */

@InjectViewState
public class OpenTabsPresenter extends MvpPresenter<OpenTabsView> implements OpenTabsManager.Callback {

    @Inject MainRouter mMainRouter;
    @Inject OpenTabsManager mOpenTabsManager;

    public OpenTabsPresenter() {
        MainApplication.getAppComponent().inject(this);
    }

    @Override
    protected void onFirstViewAttach() {
        super.onFirstViewAttach();
        mOpenTabsManager.subscribe(this);

        for (OpenTabModel model:mOpenTabsManager.getOpenTabs()) {
            getViewState().add(model);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mOpenTabsManager.unsubscribe(this);
    }

    public void navigate(OpenTabModel openTabModel) {
        if (StringUtils.isEmpty(openTabModel.getThread())) {
            mMainRouter.navigateBoard(openTabModel.getWebsite().name(), openTabModel.getBoard(), false);
        } else {
            mMainRouter.navigateThread(openTabModel.getThread(), false);
        }
    }

    public void removeItem(OpenTabModel openTabModel) {
        mOpenTabsManager.remove(openTabModel);
    }

    public void removeAllItems() {
        mOpenTabsManager.removeAll();
    }

    @Override
    public void onAdd(OpenTabModel openTabModel) {
        getViewState().add(openTabModel);
    }

    @Override
    public void onRemove(OpenTabModel openTabModel) {
        getViewState().remove(openTabModel);
    }

    @Override
    public void onRemoveAll() {
        getViewState().clearAll();
    }
}
