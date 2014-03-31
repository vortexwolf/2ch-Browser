package com.vortexwolf.chan.test.mocks;

import com.vortexwolf.chan.models.domain.IAttachmentEntity;

public class MockAttachmentEntity implements IAttachmentEntity {
    private String mImage;
    private String mThumbnail;
    private String mVideo;
    private int mSize;

    public MockAttachmentEntity(String image, String thumbnail, int size, String video) {
        this.mImage = image;
        this.mThumbnail = thumbnail;
        this.mVideo = video;
        this.mSize = size;
    }

    @Override
    public String getImageUrl() {
        return this.mImage;
    }

    @Override
    public String getVideoUrl() {
        return this.mVideo;
    }

    @Override
    public int getImageSize() {
        return this.mSize;
    }

    @Override
    public String getThumbnailUrl() {
        return this.mThumbnail;
    }
}