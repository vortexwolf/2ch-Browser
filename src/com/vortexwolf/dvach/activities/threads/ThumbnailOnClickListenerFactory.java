package com.vortexwolf.dvach.activities.threads;

import com.vortexwolf.dvach.activities.browser.BrowserLauncher;
import com.vortexwolf.dvach.common.utils.AppearanceUtils;
import com.vortexwolf.dvach.interfaces.IThumbnailOnClickListenerFactory;
import com.vortexwolf.dvach.presentation.models.AttachmentInfo;
import com.vortexwolf.dvach.settings.ApplicationSettings;

import android.content.Context;
import android.os.Debug;
import android.view.View;
import android.view.View.OnClickListener;

public class ThumbnailOnClickListenerFactory implements IThumbnailOnClickListenerFactory {
	
	private static long sMaxVmHeap = Runtime.getRuntime().maxMemory() / 1024;
	private static long sHeapPad = 128; // 128 Kb
	
	@Override
	public OnClickListener getOnClickListener(final AttachmentInfo attachment, final Context context, final ApplicationSettings settings) {
		return new OnClickListener() {
			@Override
			public void onClick(View v) {
				raiseClick(attachment, context, settings);
			}
		};
	}
	
	@Override
	public void raiseClick(final AttachmentInfo attachment, final Context context, final ApplicationSettings settings){
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
