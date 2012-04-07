package com.vortexwolf.dvach.presentation.services;

import com.vortexwolf.dvach.activities.browser.BrowserLauncher;
import com.vortexwolf.dvach.common.utils.AppearanceUtils;
import com.vortexwolf.dvach.interfaces.IClickListenersFactory;
import com.vortexwolf.dvach.presentation.models.AttachmentInfo;
import com.vortexwolf.dvach.settings.ApplicationSettings;

import android.content.Context;
import android.os.Debug;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;

public class ClickListenersFactory implements IClickListenersFactory {
	
	private static long sMaxVmHeap = Runtime.getRuntime().maxMemory() / 1024;
	private static long sHeapPad = 1024; // 128 Kb
	
	public static final OnLongClickListener sIgnoreOnLongClickListener = new OnLongClickListener() {
		@Override
		public boolean onLongClick(View v) {
			return false;
		}
	};
	
	@Override
	public OnClickListener getThumbnailOnClickListener(final AttachmentInfo attachment, final Context context, final ApplicationSettings settings) {
		return new OnClickListener() {
			@Override
			public void onClick(View v) {
				raiseThumbnailClick(attachment, context, settings);
			}
		};
	}
	
	@Override
	public void raiseThumbnailClick(final AttachmentInfo attachment, final Context context, final ApplicationSettings settings){
		int imageSize = attachment.getSize();
		String url = attachment.getSourceUrl(settings);
		
		long allocatedSize = Debug.getNativeHeapAllocatedSize() / 1024 + imageSize + sHeapPad;
		if(allocatedSize > sMaxVmHeap){
			long freeSize = Math.max(0, imageSize - (allocatedSize - sMaxVmHeap));
			AppearanceUtils.showToastMessage(context, "Image is " + imageSize+"Kb. Available Memory is " + freeSize + "Kb");
			return;
		}
		
		BrowserLauncher.launchInternalBrowser(context, url);
	}
}
