package com.vortexwolf.dvach.services.presentation;

import android.app.Dialog;
import android.content.Context;
import android.content.res.Resources.Theme;
import android.graphics.Color;
import android.text.SpannableStringBuilder;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.vortexwolf.dvach.R;
import com.vortexwolf.dvach.common.Constants;
import com.vortexwolf.dvach.common.utils.ThreadPostUtils;
import com.vortexwolf.dvach.interfaces.IBitmapManager;
import com.vortexwolf.dvach.models.presentation.AttachmentInfo;
import com.vortexwolf.dvach.models.presentation.FloatImageModel;
import com.vortexwolf.dvach.models.presentation.PostItemViewModel;
import com.vortexwolf.dvach.settings.ApplicationSettings;

public class PostItemViewBuilder {
	private final LayoutInflater mInflater;
	private final IBitmapManager mBitmapManager;
	private final String mBoardName;
	private final String mThreadNumber;
	private final Context mAppContext;
	private final ApplicationSettings mSettings;
	
	public PostItemViewBuilder(Context context, String boardName, String threadNumber, IBitmapManager bitmapManager, ApplicationSettings settings) {
		this.mAppContext = context.getApplicationContext();
		this.mInflater = LayoutInflater.from(context);
		this.mBitmapManager = bitmapManager;
		this.mBoardName = boardName;
		this.mThreadNumber = threadNumber;
		this.mSettings = settings;
	}

	public View getView(final PostItemViewModel item, final View convertView, final boolean isBusy) {
    	View view = convertView == null ? mInflater.inflate(R.layout.posts_list_item, null) : convertView;

        //Get inner controls
		ViewBag vb = (ViewBag)view.getTag();
		if(vb == null){
			vb = new ViewBag();
			vb.postIdView = (TextView) view.findViewById(R.id.post_id);
			vb.postIndexView = (TextView) view.findViewById(R.id.post_index);
			vb.commentView = (TextView) view.findViewById(R.id.comment);
			vb.attachmentInfoView = (TextView) view.findViewById(R.id.attachment_info);
			vb.postRepliesView = (TextView)view.findViewById(R.id.post_replies);
			vb.fullThumbnailView = view.findViewById(R.id.thumbnail_view);
			vb.imageView = (ImageView) view.findViewById(R.id.thumbnail);
			vb.indeterminateProgressBar = (ProgressBar) view.findViewById(R.id.indeterminate_progress);
		    view.setTag(vb);
		}
		
		if(FlowTextHelper.sNewClassAvailable){
		    if(item.hasAttachment()){
		    	FlowTextHelper.setFloatLayoutPosition(vb.fullThumbnailView, vb.commentView);
		    }
		    else {
		    	FlowTextHelper.setDefaultLayoutPosition(vb.fullThumbnailView, vb.commentView);
		    }
		}
		
		//Apply info from the data item
		//Номер поста
		String postNumber = item.getNumber();
		vb.postIdView.setText(postNumber);
		
		//Номер по порядку
		int postIndex = item.getPosition() + 1;
		vb.postIndexView.setText(String.valueOf(postIndex));
		if(postIndex >= Constants.BUMP_LIMIT){
			vb.postIndexView.setTextColor(Color.parseColor("#C41E3A"));
		}
		else {
			vb.postIndexView.setTextColor(Color.parseColor("#4F7942"));
		}
		
		//Дата поста
		if(this.mSettings.isDisplayPostItemDate()){
		    TextView dateView = (TextView)view.findViewById(R.id.post_item_date_id);
		    dateView.setVisibility(View.VISIBLE);
			dateView.setText(item.getPostDate(this.mAppContext));
		}
		
		//Обрабатываем прикрепленный файл
		AttachmentInfo attachment = item.getAttachment(this.mBoardName);
		ThreadPostUtils.handleAttachmentImage(isBusy, attachment, 
				vb.imageView, vb.indeterminateProgressBar, vb.fullThumbnailView, 
				this.mBitmapManager, this.mSettings, this.mAppContext);
		ThreadPostUtils.handleAttachmentDescription(attachment, this.mAppContext.getResources(), vb.attachmentInfoView);
		
		//Комментарий (обновляем после файла)
		if(item.canMakeCommentFloat()){
			WindowManager wm = ((WindowManager)this.mAppContext.getSystemService(Context.WINDOW_SERVICE));
			FloatImageModel floatModel = new FloatImageModel(vb.fullThumbnailView, vb.commentView.getPaint(), wm.getDefaultDisplay(), this.mAppContext.getResources());
			item.makeCommentFloat(floatModel);
		}
		
		vb.commentView.setText(item.getSpannedComment());
		vb.commentView.setMovementMethod(LinkMovementMethod.getInstance());
		
		//Ответы на сообщение
		if(this.mThreadNumber != null && item.hasReferencesFrom()){
		    SpannableStringBuilder replies = item.getReferencesFromAsSpannableString(this.mAppContext.getResources(), this.mBoardName, this.mThreadNumber);
		    vb.postRepliesView.setText(replies);
		    vb.postRepliesView.setMovementMethod(LinkMovementMethod.getInstance());
		    vb.postRepliesView.setVisibility(View.VISIBLE);
		}
		else{
			vb.postRepliesView.setVisibility(View.GONE);
		}
		
		// Почему-то LinkMovementMethod отменяет контекстное меню. Пустой listener вроде решает проблему
		view.setOnLongClickListener(ClickListenersFactory.sIgnoreOnLongClickListener);
        
        return view;
    }
	
    public void displayPopupDialog(final PostItemViewModel item, Context activityContext, Theme theme){
		View view = this.getView(item, null, false);
		
		//убираем фон в виде рамки с закругленными краями и ставим обычный
		int backColor = theme.obtainStyledAttributes(R.styleable.Theme).getColor(R.styleable.Theme_activityRootBackground, android.R.color.transparent);
		view.setBackgroundColor(backColor);
		
		//Перемещаем текст в ScrollView
		ScrollView scrollView = (ScrollView)view.findViewById(R.id.post_item_scroll);
		RelativeLayout contentLayout = (RelativeLayout)view.findViewById(R.id.post_item_content_layout);
		
		((ViewGroup)contentLayout.getParent()).removeView(contentLayout);
		scrollView.addView(contentLayout);
		scrollView.setVisibility(View.VISIBLE);
		
		//Отображаем созданное view в диалоге
		Dialog currentDialog = new Dialog(activityContext);
		currentDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		currentDialog.setCanceledOnTouchOutside(true);
		currentDialog.setContentView(view);
		currentDialog.show();
    }
	
	public void displayThumbnail(final View v, final PostItemViewModel item){
        if(item != null){
            ViewBag vb = (ViewBag)v.getTag();
            AttachmentInfo attachment = item.getAttachment(this.mBoardName);
            
            if(!ThreadPostUtils.isImageHandledWhenWasBusy(attachment, mSettings, mBitmapManager)){
	            ThreadPostUtils.handleAttachmentImage(false, attachment, 
	            		vb.imageView, vb.indeterminateProgressBar, vb.fullThumbnailView, 
	            		this.mBitmapManager, this.mSettings, this.mAppContext);
            }
        }
	}
    
    private static class ViewBag{
    	TextView postIdView;
    	TextView postIndexView;
    	TextView commentView;
        TextView attachmentInfoView;
        TextView postRepliesView;
        View fullThumbnailView;
        ImageView imageView;
        ProgressBar indeterminateProgressBar;
	}
}
