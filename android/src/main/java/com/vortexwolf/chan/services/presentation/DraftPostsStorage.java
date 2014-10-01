package com.vortexwolf.chan.services.presentation;

import com.vortexwolf.chan.common.utils.StringUtils;
import com.vortexwolf.chan.interfaces.IDraftPostsStorage;
import com.vortexwolf.chan.models.presentation.DraftPostModel;

public class DraftPostsStorage implements IDraftPostsStorage {
    private String mBoardName = "";
    private String mThreadNumber = "";
    private DraftPostModel mDraft = null;

    @Override
    public void saveDraft(String boardName, String threadNumber, DraftPostModel draft) {
        this.mBoardName = StringUtils.emptyIfNull(boardName);
        this.mThreadNumber = StringUtils.emptyIfNull(threadNumber);
        this.mDraft = draft;
    }

    @Override
    public DraftPostModel getDraft(String boardName, String threadNumber) {
        if (this.mBoardName.equals(boardName) && this.mThreadNumber.equals(threadNumber)) {
            return this.mDraft;
        }

        return null;
    }

    @Override
    public void clearDraft(String boardName, String threadNumber) {
        if (this.mBoardName.equals(boardName) && this.mThreadNumber.equals(threadNumber)) {
            this.mBoardName = "";
            this.mThreadNumber = "";
            this.mDraft = null;
        }
    }
}
