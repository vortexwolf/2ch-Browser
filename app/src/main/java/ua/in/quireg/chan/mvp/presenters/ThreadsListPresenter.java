package ua.in.quireg.chan.mvp.presenters;

import android.os.AsyncTask;

import com.arellomobile.mvp.InjectViewState;
import com.arellomobile.mvp.MvpPresenter;

import javax.inject.Inject;

import timber.log.Timber;
import ua.in.quireg.chan.common.Factory;
import ua.in.quireg.chan.common.MainApplication;
import ua.in.quireg.chan.common.utils.AppearanceUtils;
import ua.in.quireg.chan.db.HiddenThreadsDataSource;
import ua.in.quireg.chan.models.domain.ThreadModel;
import ua.in.quireg.chan.models.presentation.ThreadItemViewModel;
import ua.in.quireg.chan.mvp.views.ThreadsListView;
import ua.in.quireg.chan.services.SerializationService;
import ua.in.quireg.chan.services.presentation.PagesSerializationService;

/**
 * Created by Arcturus Mengsk on 3/17/2018, 4:34 AM.
 * 2ch-Browser
 */

@InjectViewState
public class ThreadsListPresenter extends MvpPresenter<ThreadsListView> {

    @Inject HiddenThreadsDataSource mHiddenThreadsDataSource;
    @Inject PagesSerializationService mPagesSerializationService;



    public ThreadsListPresenter() {
        super();
        MainApplication.getAppComponent().inject(this);
    }

    public void requestThreadsList() {

    }

    public void onListItemClick(ThreadItemViewModel item) {
        if (item == null) {
            Timber.e("User clicked null item!");
            return;
        }

        if (item.isHidden()) {
            mHiddenThreadsDataSource.removeFromHiddenThreads(item.getWebsite().name(), item.getBoardName(), item.getNumber());
            item.setHidden(false);
        } else {
            String threadSubject = item.getSubjectOrText();
//            navigateToThread(item.getNumber(), threadSubject);
        }
    }



    private class LoadThreadsTask extends AsyncTask<Void, Long, ThreadModel[]> {

        @Override
        protected ThreadModel[] doInBackground(Void... arg0) {
            if (!mIsCatalog) {
                return mPagesSerializationService.deserializeThreads(mWebsite.name(), mBoardName, mPageNumber);
            }
            return null;
        }

        @Override
        public void onPreExecute() {
            mThreadsReaderListener.showLoadingScreen();
        }

        @Override
        public void onPostExecute(ThreadModel[] threads) {
            mThreadsReaderListener.hideLoadingScreen();

            if (threads == null) {
                refreshThreads(false);
            } else {
                setAdapterData(threads);
                // Устанавливаем позицию, если открываем как уже открытую вкладку
                if (mTabModel.getPosition() != null) {
                    AppearanceUtils.ListViewPosition p = mTabModel.getPosition();
                    mListView.setSelectionFromTop(p.position, p.top);
                }
            }
        }
    }
}
