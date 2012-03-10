package com.vortexwolf.dvach.common.utils;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.vortexwolf.dvach.R;
import com.vortexwolf.dvach.api.entities.IAttachmentEntity;
import com.vortexwolf.dvach.api.entities.PostInfo;
import com.vortexwolf.dvach.common.Constants;
import com.vortexwolf.dvach.common.MainApplication;
import com.vortexwolf.dvach.interfaces.IBitmapManager;
import com.vortexwolf.dvach.interfaces.IThumbnailOnClickListenerFactory;
import com.vortexwolf.dvach.presentation.models.AttachmentInfo;

public class ThreadPostUtils {
	
	public static boolean isSage(PostInfo item){
		String email = item.getEmail();
		return email != null 
				&& (email.equalsIgnoreCase("mailto:"+Constants.SAGE_EMAIL)
					|| email.equalsIgnoreCase(Constants.SAGE_EMAIL));
				
	}
	
	/** Проверяет, прикреплен ли к посту какой-либо файл */
	public static boolean hasAttachment(IAttachmentEntity item){
		return !StringUtils.isEmpty(item.getImage()) || !StringUtils.isEmpty(item.getVideo());
	}

	/** Возвращает обработчик события при нажатии на картинку-thumbnail */
	public static OnClickListener getAttachmentClickListener(AttachmentInfo attach,
			IThumbnailOnClickListenerFactory clickListenerFactory, Context context){
		
		if(!attach.isEmpty()){
			String attachmentUrl = attach.getSourceUrl();
        	if (attachmentUrl != null) {
        		return clickListenerFactory.getOnClickListener(attachmentUrl, context, attach.getSize());
        	}
		}
		
		return null;
	}
	
	/** Разбирается с прикрепленным файлом для треда или поста; перенес сюда, чтобы не повторять код
	 * @param attachment Модель прикрепленного к треду или посту файла
	 * @param imageView Место для картинки
	 * @param indeterminateProgressBar Индикатор загрузки
	 * @param attachmentInfoView Место для текстовой инфы
	 * @param bitmapManager Для загрузки картинок с интернета
	 * @param thumbnailOnClickListenerFactory Для обработки нажатия по картинке
	 * @param activity
	 */
	public static void handleAttachment(AttachmentInfo attachment, ImageView imageView, 
			ProgressBar indeterminateProgressBar, TextView attachmentInfoView, View fullThumbnailView, IBitmapManager bitmapManager, 
			IThumbnailOnClickListenerFactory clickListenerFactory, Activity activity){

		indeterminateProgressBar.setVisibility(View.GONE);
		imageView.setImageResource(android.R.color.transparent); // clear the image content
		
		//Ищем прикрепленный файл, в случае наличия добавляем его как ссылку
		if(attachment == null || attachment.isEmpty()) {
			imageView.setVisibility(View.GONE);
			attachmentInfoView.setVisibility(View.GONE);
			fullThumbnailView.setVisibility(View.GONE);
		}
		else {
			fullThumbnailView.setVisibility(View.VISIBLE);
			imageView.setVisibility(View.VISIBLE);
			
			//Обработчик события нажатия на картинку
			OnClickListener thumbnailOnClickListener = getAttachmentClickListener(attachment, clickListenerFactory, activity.getApplicationContext());
			if (thumbnailOnClickListener != null) {
            	imageView.setOnClickListener(thumbnailOnClickListener);
            	indeterminateProgressBar.setOnClickListener(thumbnailOnClickListener);
    		}

        	MainApplication app = (MainApplication)activity.getApplication();
        	String thumbnailUrl = attachment.getThumbnailUrl();
        	
        	//Ничего не загружаем, если так установлено в настройках
        	if(app.getSettings().isLoadThumbnails() == false && !bitmapManager.isCached(thumbnailUrl)){
        		imageView.setImageResource(R.drawable.empty_image);
        	}
        	else{
	    		//Также добавляем уменьшенное изображение, нажатие на которое открывает файл в полном размере
	    		if (thumbnailUrl != null){
	        		bitmapManager.fetchBitmapOnThread(thumbnailUrl, imageView, indeterminateProgressBar, activity, R.drawable.error_image);
	    		}
	    		else {
	    			// Иногда можно прикреплять файлы с типом mp3, swf и пр., у которых thumbnail=null. Нужно нарисовать другую картинку в таких случаях
	    			if(!StringUtils.isEmpty(attachment.getSourceUrl())){
	    				imageView.setImageResource(attachment.getDefaultThumbnail());
	    			}
	    			else{
	    				imageView.setImageResource(R.drawable.error_image);
	    			}
	    		}
        	}
        	
            //Информация о картинке или видео
            String attachmentInfo = attachment.getDescription(activity.getString(R.string.data_file_size_measure));
            if(attachmentInfo != null){
            	attachmentInfoView.setText(attachmentInfo);
            	attachmentInfoView.setVisibility(View.VISIBLE);
            }
            else {
            	attachmentInfoView.setVisibility(View.GONE);
            }
		}
	}	
}
