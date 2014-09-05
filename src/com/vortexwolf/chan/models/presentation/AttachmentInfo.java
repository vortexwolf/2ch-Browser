package com.vortexwolf.chan.models.presentation;

import java.util.HashMap;

import android.content.res.Resources;
import android.net.Uri;

import com.vortexwolf.chan.R;
import com.vortexwolf.chan.boards.dvach.DvachUriBuilder;
import com.vortexwolf.chan.common.Constants;
import com.vortexwolf.chan.common.Factory;
import com.vortexwolf.chan.common.utils.RegexUtils;
import com.vortexwolf.chan.common.utils.StringUtils;
import com.vortexwolf.chan.common.utils.UriUtils;
import com.vortexwolf.chan.models.domain.AttachmentModel;
import com.vortexwolf.chan.settings.ApplicationSettings;

public class AttachmentInfo {
    private static final HashMap<String, Integer> sDefaultThumbnails;
    
    private final AttachmentModel mModel;
    private final String mBoardName;
    private final String mThreadNumber;
    private final boolean mIsEmpty;
    private final String mImageUrl;
    private final String mThumbnailUrl;
    private final String mSourceExtension;
    private final DvachUriBuilder mDvachUriBuilder = Factory.resolve(DvachUriBuilder.class);
    private final ApplicationSettings mSettings = Factory.resolve(ApplicationSettings.class);

    static {
        sDefaultThumbnails = new HashMap<String, Integer>();
        sDefaultThumbnails.put("mp3", R.drawable.page_white_sound_4x);
        sDefaultThumbnails.put("pdf", R.drawable.page_white_acrobat_4x);
        sDefaultThumbnails.put("swf", R.drawable.page_white_flash_4x);
    }

    public AttachmentInfo(AttachmentModel item, String boardName, String threadNumber) {
        this.mModel = item;
        this.mBoardName = boardName;
        this.mThreadNumber = threadNumber;

        SourceWithThumbnailModel urls = this.getUrls();
        if (urls != null) {
            this.mIsEmpty = false;
            this.mImageUrl = urls.imageUrl;
            this.mThumbnailUrl = urls.thumbnailUrl;
            this.mSourceExtension = this.mImageUrl != null
                    ? RegexUtils.getFileExtension(this.mImageUrl)
                    : null;
        } else {
            this.mIsEmpty = true;
            this.mImageUrl = null;
            this.mThumbnailUrl = null;
            this.mSourceExtension = null;
        }
    }
    
    public String getThreadUrl() {
        return this.mDvachUriBuilder.createThreadUri(this.mBoardName, this.mThreadNumber);
    }

    public String getSourceUrl() {
        if (!this.mIsEmpty){
            return this.mImageUrl;
        }
        return null;
    }

    public String getImageUrlIfImage() {
        if (this.isImage()) {
            return this.mImageUrl;
        }

        return null;
    }

    public String getSourceExtension() {
        return this.mSourceExtension;
    }

    public boolean isFile() {
        return !StringUtils.isEmpty(this.mSourceExtension);
    }

    public boolean isImage() {
        return !StringUtils.isEmpty(this.mSourceExtension) && Constants.IMAGE_EXTENSIONS.contains(this.mSourceExtension);
    }

    public String getThumbnailUrl() {
        return this.mThumbnailUrl;
    }

    public int getDefaultThumbnail() {
        Integer resId = AttachmentInfo.sDefaultThumbnails.get(this.mSourceExtension);

        return resId != null ? resId : R.drawable.page_white_4x;
    }

    public boolean isEmpty() {
        return this.mIsEmpty;
    }

    public int getSize() {
        return this.mModel.getImageSize();
    }

    public String getDescription() {
        String result = "";

        if (this.mModel.getImageSize() != 0) {
            result += this.mModel.getImageSize() + Factory.resolve(Resources.class).getString(R.string.data_file_size_measure);

            if ("gif".equalsIgnoreCase(this.mSourceExtension)) {
                result += "\ngif";
            }else if ("webm".equalsIgnoreCase(this.mSourceExtension)) {
                result += "\nwebm";
            }
        }

        return result;
    }

    private SourceWithThumbnailModel getUrls() {
        SourceWithThumbnailModel model = new SourceWithThumbnailModel();

        // Проверяем существование картинки
        String imageUrl = this.mModel.getPath();
        String imageThumbnail = this.mModel.getThumbnailUrl();
        if (!StringUtils.isEmpty(imageUrl)) {
            model.imageUrl = this.mDvachUriBuilder.createBoardUri(this.mBoardName, imageUrl).toString();
        }
        if (!StringUtils.isEmpty(imageThumbnail)) {
            model.thumbnailUrl = this.mDvachUriBuilder.createBoardUri(this.mBoardName, imageThumbnail).toString();
        }
        // Если выше вызвался любой из двух if, значт прикреплен какой-то файл,
        // а не видео
        if (model.imageUrl != null || model.thumbnailUrl != null) {
            return model;
        }

        return null;
    }

    private class SourceWithThumbnailModel {
        public String imageUrl;
        public String thumbnailUrl;
    }
}
