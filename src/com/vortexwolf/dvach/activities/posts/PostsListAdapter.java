package com.vortexwolf.dvach.activities.posts;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.res.Resources.Theme;
import android.graphics.Color;
import android.net.Uri;
import android.text.SpannableStringBuilder;
import android.text.method.LinkMovementMethod;
import android.view.ContextThemeWrapper;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.Adapter;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.vortexwolf.dvach.R;
import com.vortexwolf.dvach.activities.browser.BrowserLauncher;
import com.vortexwolf.dvach.api.entities.PostInfo;
import com.vortexwolf.dvach.common.Constants;
import com.vortexwolf.dvach.common.library.MyLog;
import com.vortexwolf.dvach.common.utils.AppearanceUtils;
import com.vortexwolf.dvach.common.utils.ThreadPostUtils;
import com.vortexwolf.dvach.common.utils.UriUtils;
import com.vortexwolf.dvach.interfaces.IBitmapManager;
import com.vortexwolf.dvach.interfaces.IThumbnailOnClickListenerFactory;
import com.vortexwolf.dvach.interfaces.IURLSpanClickListener;
import com.vortexwolf.dvach.presentation.models.AttachmentInfo;
import com.vortexwolf.dvach.presentation.models.FloatImageModel;
import com.vortexwolf.dvach.presentation.models.PostItemViewModel;
import com.vortexwolf.dvach.presentation.models.PostsViewModel;
import com.vortexwolf.dvach.settings.ApplicationSettings;

public class PostsListAdapter extends ArrayAdapter<PostItemViewModel> implements IURLSpanClickListener {

	// private static final String TAG = "PostsListAdapter";
	
	private final ListActivity mActivity;
	private final LayoutInflater mInflater;
	private final IBitmapManager mBitmapManager;
	private final String mBoardName;
	private final String mThreadNumber;
	private final PostsViewModel mPostsViewModel;
	private final Theme mTheme;
	private final ApplicationSettings mSettings;
	private final OnLongClickListener mOnLongClickListener;
	
	private boolean mIsBusy = false;
	private boolean mIsLoadingMore = false;
		
	public PostsListAdapter(ListActivity activity, String boardName, String threadNumber, IBitmapManager bitmapManager, ApplicationSettings settings) {
        super(activity.getApplicationContext(), 0);
        
        this.mBoardName = boardName;
        this.mThreadNumber = threadNumber;
        this.mBitmapManager = bitmapManager;
        this.mActivity = activity;
        this.mInflater = LayoutInflater.from(activity);
        this.mTheme = this.mActivity.getTheme();
        this.mPostsViewModel = new PostsViewModel();
        this.mSettings = settings;
        this.mOnLongClickListener = new OnLongClickListener(){
			@Override
			public boolean onLongClick(View arg0) {
				return false;
			}     	
        };
	}
	
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        
    	if(isStatusView(position)){
    		return mInflater.inflate(R.layout.loading, null);
    	}
    	
    	View view = convertView == null ? mInflater.inflate(R.layout.posts_list_item, null) : convertView;

        this.fillItemView(view, this.getItem(position));
        
        return view;
    }
    
    private void fillItemView(View view, final PostItemViewModel item) {
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
        
        //Дата поста
        if(this.mSettings.isDisplayPostItemDate()){
            TextView dateView = (TextView)view.findViewById(R.id.post_item_date_id);
            dateView.setVisibility(View.VISIBLE);
        	dateView.setText(item.getPostDate(this.mActivity.getApplicationContext()));
        }

        //Обрабатываем прикрепленный файл
        AttachmentInfo attachment = item.getAttachment(this.mBoardName);
        ThreadPostUtils.handleAttachmentImage(mIsBusy, attachment, 
        		vb.imageView, vb.indeterminateProgressBar, vb.fullThumbnailView, 
        		this.mBitmapManager, this.mActivity);
        ThreadPostUtils.handleAttachmentDescription(attachment, this.mActivity.getResources(), vb.attachmentInfoView);
        
        //Комментарий (обновляем после файла)
        if(item.canMakeCommentFloat()){
        	WindowManager wm = ((WindowManager)this.mActivity.getSystemService(Context.WINDOW_SERVICE));
        	FloatImageModel floatModel = new FloatImageModel(vb.fullThumbnailView, vb.commentView.getPaint(), wm.getDefaultDisplay(), this.mActivity.getResources());
        	item.makeCommentFloat(floatModel);
        }
        
        vb.commentView.setText(item.getSpannedComment());
        vb.commentView.setMovementMethod(LinkMovementMethod.getInstance());
        
        //Ответы на сообщение
        if(item.hasReferencesFrom()){
	        SpannableStringBuilder replies = item.getReferencesFromAsSpannableString(this.mActivity.getResources(), this.mBoardName, this.mThreadNumber);
	        vb.postRepliesView.setText(replies);
	        vb.postRepliesView.setMovementMethod(LinkMovementMethod.getInstance());
	        vb.postRepliesView.setVisibility(View.VISIBLE);
        }
        else{
        	vb.postRepliesView.setVisibility(View.GONE);
        }
	        
        // Почему-то LinkMovementMethod отменяет контекстное меню. Пустой listener вроде решает проблему
        view.setOnLongClickListener(this.mOnLongClickListener);
    }

	@Override
	public void onClick(View v, String url) {

		Uri uri = Uri.parse(url);
		String pageName = UriUtils.getPageName(uri);
		
		// Если ссылка указывает на этот тред - перескакиваем на нужный пост, иначе открываем в браузере
		if(mThreadNumber.equals(pageName)){
			String postNumber = uri.getFragment();
//			// Переходим на тот пост, куда указывает ссылка
			int position = postNumber != null ? findPostByNumber(postNumber) : 0;
			if(position == -1){
				AppearanceUtils.showToastMessage(this.mActivity, this.mActivity.getString(R.string.notification_post_not_found));
				return;
			}
			
			if(this.mSettings.isLinksInPopup()){
				View view = this.getView(position, null, null);
				
				//убираем фон в виде рамки с закругленными краями и ставим обычный
				int backColor = this.mTheme.obtainStyledAttributes(R.styleable.Theme).getColor(R.styleable.Theme_activityRootBackground, android.R.color.transparent);
				view.setBackgroundColor(backColor);
				
				//Отображаем созданное view в диалоге
				Dialog currentDialog = new Dialog(this.mActivity);
				currentDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
				currentDialog.setCanceledOnTouchOutside(true);
				currentDialog.setContentView(view);
				currentDialog.show();
			}
			else{
				mActivity.getListView().setSelection(position);
			}
		}
		else {
			Uri absoluteUri = UriUtils.adjust2chRelativeUri(uri);
			BrowserLauncher.launchExternalBrowser(v.getContext(), absoluteUri.toString());
		}
	}
	
	private int findPostByNumber(String postNumber){
		PostItemViewModel vm = this.mPostsViewModel.getModel(postNumber);
		if(vm != null){
			return vm.getPosition();
		}
		return -1;
	}
	
	/** Возвращает номер последнего сообщения */
	public String getLastPostNumber() {
		return this.mPostsViewModel.getLastPostNumber();
	}
		
	/** Обновляет адаптер полностью*/
	public void setAdapterData(PostInfo[] posts){
		this.clear();
		for(PostInfo pi : posts){
			this.add(this.mPostsViewModel.createModel(pi, this.mTheme, this));	
		}
	}
	
	/** Добавляет новые данные в адаптер
	 * @param from Начиная с какого сообщения добавлять данные
	 * @param posts Список сообщений (можно и всех, они потом отфильтруются)
	 */
	public int updateAdapterData(String from, PostInfo[] posts){

		boolean afterFrom = false;
		int newPostsCount = 0;

		for(PostInfo pi : posts){
			if(afterFrom){
				this.add(this.mPostsViewModel.createModel(pi, this.mTheme, this));
				newPostsCount++;
			}
			// Исключая сам from, поэтому этот код в конце
			if(pi.getNum().equals(from)) {
				afterFrom = true;
			}
		}
		
		// обновить все видимые элементы, чтобы правильно перерисовался список ссылок replies
		if(newPostsCount > 0){
			this.notifyDataSetChanged();
		}

		return newPostsCount;
	}
	
	public void setBusy(boolean isBusy, AbsListView view){
		boolean prevBusy = this.mIsBusy;
		this.mIsBusy = isBusy;
		
		if(prevBusy == true && isBusy == false){
			//this.notifyDataSetChanged();
	        int count = view.getChildCount();
	        for (int i=0; i<count; i++) {
	            View v = (View)view.getChildAt(i);
	            int position = view.getPositionForView(v);
	            
	            ViewBag vb = (ViewBag)v.getTag();
	            
	            PostItemViewModel item = this.getItem(position);
	            if(item != null){
		            AttachmentInfo attachment = item.getAttachment(this.mBoardName);
		            if(!ThreadPostUtils.isImageHandledWhenWasBusy(attachment, mSettings, mBitmapManager)){
			            ThreadPostUtils.handleAttachmentImage(isBusy, attachment, 
			            		vb.imageView, vb.indeterminateProgressBar, vb.fullThumbnailView, 
			            		this.mBitmapManager, this.mActivity);
		            }
	            }
	        }
		}
	}
	
	public void setLoadingMore(boolean isLoadingMore){
		mIsLoadingMore = isLoadingMore;
		this.notifyDataSetChanged();
	}
	
	private final boolean hasStatusView() {
		return mIsLoadingMore && super.getCount() > 0;
	}
	
	private final boolean isStatusView(int position) {
		return hasStatusView() && position == getCount() - 1;
	}

	@Override
	public int getCount() {
		int i = super.getCount();
		if (hasStatusView()){
			i++;
		}
		return i;
	}
	
	@Override
	public PostItemViewModel getItem(int position) {
		return isStatusView(position) ? null : super.getItem(position);
	}
	
	@Override
	public long getItemId(int position) {
		return isStatusView(position) ? Long.MIN_VALUE : super.getItemId(position);
	}
	
	@Override
	public int getItemViewType(int position) {
		return isStatusView(position) ? Adapter.IGNORE_ITEM_VIEW_TYPE : super.getItemViewType(position);
	}
	
	@Override
	public boolean isEnabled(int position) {
		return !isStatusView(position);
	}
	
	static class ViewBag{
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
