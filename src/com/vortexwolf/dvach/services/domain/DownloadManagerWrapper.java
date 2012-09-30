package com.vortexwolf.dvach.services.domain;

import java.io.File;
import com.vortexwolf.dvach.R;
import com.vortexwolf.dvach.common.utils.AppearanceUtils;

import android.app.DownloadManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

public class DownloadManagerWrapper {
	public static void downloadFile(Context context, Uri from, File to){
		DownloadManager dm = (DownloadManager) context.getApplicationContext().getSystemService(Context.DOWNLOAD_SERVICE);
		DownloadManager.Request request = new DownloadManager.Request(from)
						.setDestinationUri(Uri.fromFile(to))
						.setTitle(to.getName());
		dm.enqueue(request);
		
		try
		{
	        Intent i = new Intent();
	        i.setAction(DownloadManager.ACTION_VIEW_DOWNLOADS);
	        context.startActivity(i);
		}
		catch (ActivityNotFoundException anfe) {
			// Файл загрузился, но не удалось открыть список загрузок. Просто показываю сообщение об успешной загрузке.
			AppearanceUtils.showToastMessage(context, context.getString(R.string.notification_save_image_success, to.getAbsolutePath()));
		}
	}
}
