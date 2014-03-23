package com.vortexwolf.chan.asynctasks;

import android.os.AsyncTask;
import android.view.Window;

import com.vortexwolf.chan.common.library.MyLog;
import com.vortexwolf.chan.interfaces.ICancelled;
import com.vortexwolf.chan.interfaces.IJsonApiReader;
import com.vortexwolf.chan.interfaces.IJsonProgressChangeListener;
import com.vortexwolf.chan.interfaces.IListView;
import com.vortexwolf.chan.interfaces.IPostsListView;
import com.vortexwolf.chan.models.domain.FoundPostsList;
import com.vortexwolf.chan.models.domain.ThreadsList;

public class SearchPostsTask extends AsyncTask<Void, Long, Boolean> implements IJsonProgressChangeListener, ICancelled {
    private final IJsonApiReader mJsonReader;
    private final IListView<FoundPostsList> mView;
    private final String mBoard;
    private final String mSearchQuery;
    
    private FoundPostsList mFoundPostsList = null;
    private String mUserError = null;
    
    // Progress bar
    private long mContentLength = 0;
    private long mProgressOffset = 0;
    private double mProgressScale = 1;
    
    public SearchPostsTask(String board, String searchQuery, IJsonApiReader jsonReader, IListView<FoundPostsList> view){
        this.mJsonReader = jsonReader;
        this.mView = view;
        this.mBoard = board;
        this.mSearchQuery = searchQuery;
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        try {
            this.mFoundPostsList = this.mJsonReader.searchPostsList(this.mBoard, this.mSearchQuery, this, this);
            return true;
        } catch (Exception e) {
            this.mUserError = e.getMessage();
        }

        return false;
    }
    
    @Override
    public void onPreExecute() {
        this.mView.showLoadingScreen();
    }

    @Override
    public void onPostExecute(Boolean success) {
        this.mView.hideLoadingScreen();

        if (success && this.mFoundPostsList != null) {
            this.mView.setData(this.mFoundPostsList);
        } else if (!success) {
            this.mView.showError(this.mUserError);
        }
    }
    
    @Override
    public void onProgressUpdate(Long... progress) {
        // 0-9999 is ok, 10000 means it's finished
        if (this.mContentLength > 0) {
            double relativeProgress = progress[0].longValue() / (double)this.mContentLength;
            MyLog.v("SearchPostsTask", relativeProgress + "");
            this.mView.setWindowProgress((int)(relativeProgress * 9999));
        }
    }

    @Override
    public void progressChanged(long newValue) {
        if (this.isCancelled()) {
            return;
        }
        long absoluteProgress = this.mProgressOffset + (long)(newValue * this.mProgressScale);
        this.publishProgress(absoluteProgress);
    }

    @Override
    public void indeterminateProgress() {
        this.mView.setWindowProgress(Window.PROGRESS_INDETERMINATE_ON);
    }

    @Override
    public void setContentLength(long value) {
        this.mContentLength = value;
    }

    @Override
    public long getContentLength() {
        return this.mContentLength;
    }

    @Override
    public void setOffsetAndScale(long offset, double scale) {
        this.mProgressOffset = offset;
        this.mProgressScale = scale;
    }

}
