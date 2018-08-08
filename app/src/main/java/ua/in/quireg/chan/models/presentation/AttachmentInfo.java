package ua.in.quireg.chan.models.presentation;

import android.content.res.Resources;
import android.net.Uri;

import java.util.HashMap;

import ua.in.quireg.chan.R;
import ua.in.quireg.chan.common.Factory;
import ua.in.quireg.chan.common.utils.RegexUtils;
import ua.in.quireg.chan.common.utils.StringUtils;
import ua.in.quireg.chan.common.utils.UriUtils;
import ua.in.quireg.chan.interfaces.IUrlBuilder;
import ua.in.quireg.chan.interfaces.IWebsite;
import ua.in.quireg.chan.models.domain.AttachmentModel;

public class AttachmentInfo {

    private static final HashMap<String, Integer> sDefaultThumbnails;

    private final AttachmentModel mModel;
    private final IWebsite mWebsite;
    private final String mBoardName;
    private final String mThreadNumber;
    private final boolean mIsEmpty;
    private final String mImageUrl;
    private final String mThumbnailUrl;
    private final String mSourceExtension;
    private IUrlBuilder mUrlBuilder;

    static {
        sDefaultThumbnails = new HashMap<>();
        sDefaultThumbnails.put("mp3", R.drawable.page_white_sound_4x);
        sDefaultThumbnails.put("pdf", R.drawable.page_white_acrobat_4x);
        sDefaultThumbnails.put("swf", R.drawable.page_white_flash_4x);
    }

    public AttachmentInfo(AttachmentModel item, IWebsite website, String boardName, String threadNumber) {
        mModel = item;
        mWebsite = website;
        mBoardName = boardName;
        mThreadNumber = threadNumber;
        mUrlBuilder = mWebsite.getUrlBuilder();

        SourceWithThumbnailModel urls = getUrls();
        if (urls != null) {
            mIsEmpty = false;
            mImageUrl = urls.imageUrl;
            mThumbnailUrl = urls.thumbnailUrl;
            mSourceExtension = mImageUrl != null
                    ? RegexUtils.getFileExtension(mImageUrl)
                    : null;
        } else {
            mIsEmpty = true;
            mImageUrl = null;
            mThumbnailUrl = null;
            mSourceExtension = null;
        }
    }

    public String getThreadUrl() {
        return mUrlBuilder.getThreadUrlHtml(mBoardName, mThreadNumber);
    }

    public String getSourceUrl() {
        if (!mIsEmpty) {
            return mImageUrl;
        }
        return null;
    }

    public String getImageUrlIfImage() {
        if (isImage()) {
            return mImageUrl;
        }

        return null;
    }

    public String getThumbnailUrl() {
        return StringUtils.emptyIfNull(mThumbnailUrl);
    }

    public String getSourceExtension() {
        return mSourceExtension;
    }

    public boolean isFile() {
        return !StringUtils.isEmpty(mSourceExtension);
    }

    public boolean isImage() {
        return mImageUrl != null && UriUtils.isImageUri(Uri.parse(mImageUrl));
    }

    public boolean isVideo() {
        return mImageUrl != null && UriUtils.isVideoUri(Uri.parse(mImageUrl));
    }

    public boolean isDisplayableInGallery() {
        return isImage() || isVideo();
    }

    public int getDefaultThumbnail() {
        Integer resId = AttachmentInfo.sDefaultThumbnails.get(mSourceExtension);

        return resId != null ? resId : R.drawable.page_white_4x;
    }

    public boolean isEmpty() {
        return mIsEmpty;
    }

    public int getSize() {
        return mModel.getImageSize();
    }

    public String getDescription() {
        String result = "";

        if (mModel.getImageSize() != 0) {
            result += mModel.getImageSize() + Factory.resolve(Resources.class).getString(R.string.data_file_size_measure);

            if ("gif".equalsIgnoreCase(mSourceExtension)) {
                result += "\ngif";
            } else if ("webm".equalsIgnoreCase(mSourceExtension)) {
                result += "\nwebm";
            } else if ("mp4".equalsIgnoreCase(mSourceExtension)) {
                result += "\nmp4";
            }
        }

        return result;
    }

    private SourceWithThumbnailModel getUrls() {
        SourceWithThumbnailModel model = new SourceWithThumbnailModel();

        // Проверяем существование картинки
        String imageUrl = mModel.getPath();
        String imageThumbnail = mModel.getThumbnailUrl();
        if (!StringUtils.isEmpty(imageUrl)) {
            model.imageUrl = mUrlBuilder.getImageUrl(mBoardName, imageUrl);
        }
        if (!StringUtils.isEmpty(imageThumbnail)) {
            model.thumbnailUrl = mUrlBuilder.getThumbnailUrl(mBoardName, imageThumbnail);
        }

        if (model.imageUrl == null && model.thumbnailUrl == null) {
            return null;
        }
        return model;
    }

    private class SourceWithThumbnailModel {

        public String imageUrl;
        public String thumbnailUrl;
    }
}
