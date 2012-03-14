package com.vortexwolf.dvach.presentation.models;

import java.util.HashMap;

import android.net.Uri;

import com.vortexwolf.dvach.R;
import com.vortexwolf.dvach.api.entities.IAttachmentEntity;
import com.vortexwolf.dvach.common.utils.StringUtils;
import com.vortexwolf.dvach.common.utils.UriUtils;

public class AttachmentInfo {
	
	private final IAttachmentEntity mModel;
	private final String mBoardCode;
	private final String mSourceUrl;
	private final String mThumbnailUrl;
	private final String mSourceExtension;
	private static final HashMap<String, Integer> sDefaultThumbnails;
	
	static{
		sDefaultThumbnails = new HashMap<String, Integer>();
		sDefaultThumbnails.put("mp3", R.drawable.page_white_sound_4x);
		sDefaultThumbnails.put("pdf", R.drawable.page_white_acrobat_4x);
		sDefaultThumbnails.put("swf", R.drawable.page_white_flash_4x);
	}
	
	public AttachmentInfo(IAttachmentEntity item, String boardCode) {
		this.mModel = item;
		this.mBoardCode = boardCode;
		
		SourceWithThumbnailModel urls = getUrls();
		this.mSourceUrl = urls != null ? urls.sourceUrl : null;
		this.mThumbnailUrl = urls != null ? urls.thumbnailUrl : null;
		this.mSourceExtension = this.mSourceUrl != null ? UriUtils.getFileExtension(Uri.parse(this.mSourceUrl)) : null;
	}

	public String getSourceUrl() {
		return mSourceUrl;
	}
	
	public String getSourceExtension() {
		return mSourceExtension;
	}
	
	public boolean isFile(){
		return !StringUtils.isEmpty(mSourceExtension);
	}
	
	public String getThumbnailUrl() {
		return mThumbnailUrl;
	}
	
	public int getDefaultThumbnail(){
		Integer resId = this.sDefaultThumbnails.get(this.mSourceExtension);

		return resId != null ? resId : R.drawable.page_white_4x;
	}
	
	public boolean isEmpty(){
		return mSourceUrl == null;
	}
	
	public int getSize(){
		return this.mModel.getSize();
	}
	
	public String getDescription(String sizeMeasure) {
		String result = "";
		
		if(this.mModel.getSize() != 0){		
			result += this.mModel.getSize() + sizeMeasure;
			
			if("gif".equalsIgnoreCase(this.mSourceExtension)){
				result += " gif";
			}
		}
		else if (!StringUtils.isEmpty(this.mModel.getVideo())){
			result = "YouTube";
		}
		
		return result;
	}

	private SourceWithThumbnailModel getUrls(){
		SourceWithThumbnailModel model = new SourceWithThumbnailModel();
		
		//Проверяем существование картинки
		String imageUrl = this.mModel.getImage();
		String imageThumbnail = this.mModel.getThumbnail();
		if(!StringUtils.isEmpty(imageUrl)){
			model.sourceUrl = UriUtils.create2chURL(this.mBoardCode, imageUrl).toString();
		}
		if(!StringUtils.isEmpty(imageThumbnail)){
			model.thumbnailUrl = UriUtils.create2chURL(this.mBoardCode, imageThumbnail).toString();
		}
		// Если выше вызвался любой из двух if, значт прикреплен какой-то файл, а не видео
		if(model.sourceUrl != null || model.thumbnailUrl != null){
			return model;
		}
		
		//И видео
		String videoHtml = this.mModel.getVideo();	
		String videoCode = UriUtils.parseYouTubeCode(videoHtml);
		if(!StringUtils.isEmpty(videoCode)){
			model.sourceUrl = "http://www.youtube.com/v/"+videoCode;
			model.thumbnailUrl = "http://img.youtube.com/vi/"+videoCode+"/default.jpg";
			return model;
		}
		
		return null;
	}
	
	private class SourceWithThumbnailModel
	{
		public String sourceUrl;
		public String thumbnailUrl;
	}
}
