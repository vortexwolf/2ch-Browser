package com.vortexwolf.chan.models.presentation;

import java.util.HashMap;

import android.net.Uri;

import com.vortexwolf.chan.R;
import com.vortexwolf.chan.boards.dvach.DvachUriBuilder;
import com.vortexwolf.chan.common.Constants;
import com.vortexwolf.chan.common.utils.RegexUtils;
import com.vortexwolf.chan.common.utils.StringUtils;
import com.vortexwolf.chan.common.utils.UriUtils;
import com.vortexwolf.chan.models.domain.AttachmentModel;
import com.vortexwolf.chan.settings.ApplicationSettings;

public class AttachmentInfo {

    private final AttachmentModel mModel;
    private final String mBoardCode;
    private final boolean mIsEmpty;
    private final boolean mIsVideo;
    private final String mImageUrl;
    private final String mVideoUrl;
    private final String mVideoMobileUrl;
    private final String mThumbnailUrl;
    private final String mSourceExtension;
    private final DvachUriBuilder mDvachUriBuilder;
    private static final HashMap<String, Integer> sDefaultThumbnails;

    static {
        sDefaultThumbnails = new HashMap<String, Integer>();
        sDefaultThumbnails.put("mp3", R.drawable.page_white_sound_4x);
        sDefaultThumbnails.put("pdf", R.drawable.page_white_acrobat_4x);
        sDefaultThumbnails.put("swf", R.drawable.page_white_flash_4x);
    }

    public AttachmentInfo(AttachmentModel item, String boardCode, DvachUriBuilder dvachUriBuilder) {
        this.mModel = item;
        this.mBoardCode = boardCode;
        this.mDvachUriBuilder = dvachUriBuilder;

        SourceWithThumbnailModel urls = this.getUrls();
        if (urls != null) {
            this.mIsEmpty = false;
            this.mIsVideo = urls.isVideo;
            this.mImageUrl = urls.imageUrl;
            this.mThumbnailUrl = urls.thumbnailUrl;
            this.mSourceExtension = this.mImageUrl != null
                    ? RegexUtils.getFileExtension(this.mImageUrl)
                    : null;
            this.mVideoUrl = urls.videoUrl;
            this.mVideoMobileUrl = urls.videoMobileUrl;
        } else {
            this.mIsEmpty = true;
            this.mIsVideo = false;
            this.mImageUrl = null;
            this.mThumbnailUrl = null;
            this.mSourceExtension = null;
            this.mVideoUrl = null;
            this.mVideoMobileUrl = null;
        }
    }

    public String getSourceUrl(ApplicationSettings settings) {
        if (this.mIsEmpty) {
            return null;
        } else if (this.mIsVideo) {
            return settings.isYoutubeMobileLinks() ? this.mVideoMobileUrl : this.mVideoUrl;
        } else {
            return this.mImageUrl;
        }
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

    public String getDescription(String sizeMeasure) {
        String result = "";

        if (this.mModel.getImageSize() != 0) {
            result += this.mModel.getImageSize() + sizeMeasure;

            if ("gif".equalsIgnoreCase(this.mSourceExtension)) {
                result += " gif";
            }
        } else if (this.mIsVideo) {
            result = "YouTube";
        }

        return result;
    }

    private SourceWithThumbnailModel getUrls() {
        SourceWithThumbnailModel model = new SourceWithThumbnailModel();

        // Проверяем существование картинки
        String imageUrl = this.mModel.getPath();
        String imageThumbnail = this.mModel.getThumbnailUrl();
        if (!StringUtils.isEmpty(imageUrl)) {
            model.imageUrl = this.mDvachUriBuilder.createBoardUri(this.mBoardCode, imageUrl).toString();
        }
        if (!StringUtils.isEmpty(imageThumbnail)) {
            model.thumbnailUrl = this.mDvachUriBuilder.createBoardUri(this.mBoardCode, imageThumbnail).toString();
        }
        // Если выше вызвался любой из двух if, значт прикреплен какой-то файл,
        // а не видео
        if (model.imageUrl != null || model.thumbnailUrl != null) {
            return model;
        }

        // И видео
        String videoHtml = this.mModel.getVideoHtml();
        String videoCode = RegexUtils.getYouTubeCode(videoHtml);
        if (!StringUtils.isEmpty(videoCode)) {
            model.isVideo = true;
            model.videoMobileUrl = UriUtils.formatYoutubeMobileUri(videoCode);
            model.videoUrl = UriUtils.formatYoutubeUri(videoCode);
            model.thumbnailUrl = UriUtils.formatYoutubeThumbnailUri(videoCode);
            return model;
        }

        return null;
    }

    private class SourceWithThumbnailModel {
        public boolean isVideo = false;
        public String imageUrl;
        public String videoUrl;
        public String videoMobileUrl;
        public String thumbnailUrl;
    }
}
