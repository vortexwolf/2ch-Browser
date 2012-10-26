package com.vortexwolf.dvach.services.presentation;

import com.vortexwolf.dvach.common.utils.AppearanceUtils;
import com.vortexwolf.dvach.common.utils.UriUtils;
import com.vortexwolf.dvach.interfaces.IClickListenersFactory;
import com.vortexwolf.dvach.interfaces.IURLSpanClickListener;
import com.vortexwolf.dvach.models.presentation.AttachmentInfo;
import com.vortexwolf.dvach.services.BrowserLauncher;
import com.vortexwolf.dvach.settings.ApplicationSettings;

import android.content.Context;
import android.net.Uri;
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
	
	public static IURLSpanClickListener getDefaultSpanClickListener(final DvachUriBuilder dvachUriBuilder) {
		return new IURLSpanClickListener() {
			@Override
			public void onClick(View v, String url) {
				Uri absoluteUri = dvachUriBuilder.adjust2chRelativeUri(Uri.parse(url));
				BrowserLauncher.launchExternalBrowser(v.getContext(), absoluteUri.toString());
			}
		};
	}
	
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
