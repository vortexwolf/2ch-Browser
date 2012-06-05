package com.vortexwolf.dvach.test.mocks;

import com.vortexwolf.dvach.models.domain.IAttachmentEntity;

public class MockAttachmentEntity implements IAttachmentEntity
{
	private String mImage;
	private String mThumbnail;
	private String mVideo;
	private int mSize;

	public MockAttachmentEntity(String image, String thumbnail, int size, String video){
		this.mImage = image;
		this.mThumbnail = thumbnail;
		this.mVideo = video;
		this.mSize = size;
	}
	
	@Override
	public String getImage() {
		return mImage;
	}

	@Override
	public String getVideo() {
		return mVideo;
	}

	@Override
	public int getSize() {
		return mSize;
	}

	@Override
	public String getThumbnail() {
		return mThumbnail;
	}
}