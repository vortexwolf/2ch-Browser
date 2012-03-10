package com.vortexwolf.dvach.activities.threads;

import java.text.MessageFormat;

import com.vortexwolf.dvach.R;
import com.vortexwolf.dvach.api.entities.PostInfo;
import com.vortexwolf.dvach.api.entities.ThreadInfo;
import com.vortexwolf.dvach.common.utils.HtmlUtils;
import com.vortexwolf.dvach.common.utils.StringUtils;
import com.vortexwolf.dvach.common.utils.ThreadPostUtils;
import com.vortexwolf.dvach.interfaces.IBitmapManager;
import com.vortexwolf.dvach.interfaces.IThumbnailOnClickListenerFactory;
import com.vortexwolf.dvach.presentation.models.ThreadItemViewModel;

import android.app.Activity;
import android.content.res.Resources.Theme;
import android.text.Html;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class ThreadsListAdapter extends ArrayAdapter<ThreadItemViewModel> {
	
	private final Activity mActivity;
	private final LayoutInflater mInflater;
	private final IBitmapManager mBitmapManager;
	private final IThumbnailOnClickListenerFactory mThumbnailOnClickListenerFactory;
	private final Theme mTheme;
	
	private final String mBoardName;

	public ThreadsListAdapter(Activity activity, String boardName, IBitmapManager bitmapManager, IThumbnailOnClickListenerFactory thumbnailOnClickListenerFactory) {
        super(activity.getApplicationContext(), 0);
        
        this.mBoardName = boardName;
        this.mBitmapManager = bitmapManager;
        this.mThumbnailOnClickListenerFactory = thumbnailOnClickListenerFactory;
        this.mActivity = activity;
        this.mTheme = activity.getTheme();
        this.mInflater = LayoutInflater.from(activity);
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
        String postsQuantity = mActivity.getResources().getQuantityString(R.plurals.data_posts_quantity, item.getReplyCount(), item.getReplyCount());
        String imagesQuantity = mActivity.getResources().getQuantityString(R.plurals.data_files_quantity, item.getImageCount(), item.getImageCount());
        String repliesFormat = mActivity.getString(R.string.data_posts_files);
        String repliesText = String.format(repliesFormat, postsQuantity, imagesQuantity);
        vb.repliesNumberView.setText(repliesText);
        
        //Обрабатываем прикрепленный файл
        ThreadPostUtils.handleAttachment(item.getAttachment(this.mBoardName), 
        		vb.thumbnailView, vb.indeterminateProgressBar, vb.attachmentInfoView, vb.fullThumbnailView, 
        		this.mBitmapManager, this.mThumbnailOnClickListenerFactory, this.mActivity);
    }
    
	/** Обновляет адаптер полностью*/
	public void setAdapterData(ThreadInfo[] threads){
		this.clear();
		for(ThreadInfo ti : threads){
			this.add(new ThreadItemViewModel(ti, this.mTheme));
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
