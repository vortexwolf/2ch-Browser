package com.vortexwolf.dvach.activities.threads;

import com.vortexwolf.dvach.R;
import com.vortexwolf.dvach.api.entities.ThreadInfo;
import com.vortexwolf.dvach.common.utils.StringUtils;
import com.vortexwolf.dvach.common.utils.ThreadPostUtils;
import com.vortexwolf.dvach.interfaces.IBitmapManager;
import com.vortexwolf.dvach.presentation.models.AttachmentInfo;
import com.vortexwolf.dvach.presentation.models.ThreadItemViewModel;
import com.vortexwolf.dvach.settings.ApplicationSettings;

import android.content.Context;
import android.content.res.Resources.Theme;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class ThreadsListAdapter extends ArrayAdapter<ThreadItemViewModel> {
	private final LayoutInflater mInflater;
	private final IBitmapManager mBitmapManager;
	private final Theme mTheme;
	private final ApplicationSettings mSettings;
	
	private final String mBoardName;
	
	private boolean mIsBusy = false;

	public ThreadsListAdapter(Context context, String boardName, IBitmapManager bitmapManager, ApplicationSettings settings, Theme theme) {
        super(context.getApplicationContext(), 0);
        
        this.mBoardName = boardName;
        this.mBitmapManager = bitmapManager;
        this.mTheme = theme;
        this.mInflater = LayoutInflater.from(context);
        this.mSettings = settings;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        
    	View view = convertView != null ? convertView : mInflater.inflate(R.layout.threads_list_item, null);
        
        ThreadItemViewModel item = this.getItem(position);

        this.fillItemView(view, item);
        
        return view;
    }
    
    private void fillItemView(View view, ThreadItemViewModel item) {
    	//Get inner controls
    	ViewBag vb = (ViewBag)view.getTag();
    	if(vb == null){
    		vb = new ViewBag();
    		vb.titleView = (TextView) view.findViewById(R.id.title);
    		vb.commentView = (TextView) view.findViewById(R.id.comment);
    		vb.repliesNumberView = (TextView) view.findViewById(R.id.repliesNumber);
    		vb.attachmentInfoView = (TextView) view.findViewById(R.id.attachment_info);
    		vb.fullThumbnailView = view.findViewById(R.id.thumbnail_view);
    		vb.thumbnailView = (ImageView) view.findViewById(R.id.thumbnail);
    		vb.indeterminateProgressBar = (ProgressBar) view.findViewById(R.id.indeterminate_progress);
    		
    		view.setTag(vb);
    	}
        
        //Apply info from the data item
        Spanned subject = item.getSpannedSubject();
        if(!StringUtils.isEmpty(subject)){
        	vb.titleView.setVisibility(View.VISIBLE);
        	vb.titleView.setText(subject);
        }
        else {
        	vb.titleView.setVisibility(View.GONE);
        }
        
        //Комментарий
        vb.commentView.setText(item.getSpannedComment());

        //Количество ответов
        String postsQuantity = this.getContext().getResources().getQuantityString(R.plurals.data_posts_quantity, item.getReplyCount(), item.getReplyCount());
        String imagesQuantity = this.getContext().getResources().getQuantityString(R.plurals.data_files_quantity, item.getImageCount(), item.getImageCount());
        String repliesFormat = this.getContext().getString(R.string.data_posts_files);
        String repliesText = String.format(repliesFormat, postsQuantity, imagesQuantity);
        vb.repliesNumberView.setText(repliesText);
        
        //Обрабатываем прикрепленный файл
        AttachmentInfo attachment = item.getAttachment(this.mBoardName);
        ThreadPostUtils.handleAttachmentImage(mIsBusy, attachment, 
        		vb.thumbnailView, vb.indeterminateProgressBar, vb.fullThumbnailView, 
        		this.mBitmapManager, this.mSettings, this.getContext());
        ThreadPostUtils.handleAttachmentDescription(attachment, this.getContext().getResources(), vb.attachmentInfoView);
    }
    
	/** Обновляет адаптер полностью*/
	public void setAdapterData(ThreadInfo[] threads){
		this.clear();
		for(ThreadInfo ti : threads){
			this.add(new ThreadItemViewModel(ti, this.mTheme));
		}
	}
	
	public void setBusy(boolean isBusy, AbsListView view){
		boolean prevBusy = this.mIsBusy;
		this.mIsBusy = isBusy;
		
		if(prevBusy == true && isBusy == false){
			//this.notifyDataSetChanged();
			//MyLog.v("ThreadsListAdapter", "Non busy");
	        int count = view.getChildCount();
	        for (int i=0; i<count; i++) {
	            View v = view.getChildAt(i);
	            int position = view.getPositionForView(v);
	            
	            ViewBag vb = (ViewBag)v.getTag();
	            
	            AttachmentInfo attachment = this.getItem(position).getAttachment(this.mBoardName);
	            if(!ThreadPostUtils.isImageHandledWhenWasBusy(attachment, mSettings, mBitmapManager)){
		            ThreadPostUtils.handleAttachmentImage(isBusy, attachment, 
		            		vb.thumbnailView, vb.indeterminateProgressBar, vb.fullThumbnailView, 
		            		this.mBitmapManager, this.mSettings, this.getContext());
	            }
	        }
		}
	}
	
	static class ViewBag{
    	TextView titleView;
    	TextView commentView;
        TextView repliesNumberView;
        TextView attachmentInfoView;
        ImageView thumbnailView;
        View fullThumbnailView;
        ProgressBar indeterminateProgressBar;
	}
}
