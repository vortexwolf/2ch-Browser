package com.vortexwolf.dvach.adapters;

import android.content.Context;
import android.content.res.Resources.Theme;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.Adapter;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import com.vortexwolf.dvach.R;
import com.vortexwolf.dvach.common.Constants;
import com.vortexwolf.dvach.common.utils.AppearanceUtils;
import com.vortexwolf.dvach.common.utils.StringUtils;
import com.vortexwolf.dvach.common.utils.UriUtils;
import com.vortexwolf.dvach.interfaces.IBitmapManager;
import com.vortexwolf.dvach.interfaces.IBusyAdapter;
import com.vortexwolf.dvach.interfaces.IURLSpanClickListener;
import com.vortexwolf.dvach.models.domain.PostInfo;
import com.vortexwolf.dvach.models.presentation.PostItemViewModel;
import com.vortexwolf.dvach.models.presentation.PostsViewModel;
import com.vortexwolf.dvach.services.presentation.ClickListenersFactory;
import com.vortexwolf.dvach.services.presentation.PostItemViewBuilder;
import com.vortexwolf.dvach.settings.ApplicationSettings;

public class PostsListAdapter extends ArrayAdapter<PostItemViewModel> implements IURLSpanClickListener, IBusyAdapter {
	private static final String TAG = "PostsListAdapter";

	private final LayoutInflater mInflater;
	private final IBitmapManager mBitmapManager;
	private final String mBoardName;
	private final String mThreadNumber;
	private final PostsViewModel mPostsViewModel;
	private final Theme mTheme;
	private final ApplicationSettings mSettings;
	private final ListView mListView;
	private final Context mActivityContext;
	private final PostItemViewBuilder mPostItemViewBuilder;
	
	private boolean mIsBusy = false;
	private boolean mIsLoadingMore = false;
		
	public PostsListAdapter(Context context, String boardName, String threadNumber, IBitmapManager bitmapManager, ApplicationSettings settings, Theme theme, ListView listView) {
        super(context.getApplicationContext(), 0);
        
        this.mBoardName = boardName;
        this.mThreadNumber = threadNumber;
        this.mBitmapManager = bitmapManager;
        this.mInflater = LayoutInflater.from(context);
        this.mTheme = theme;
        this.mPostsViewModel = new PostsViewModel();
        this.mSettings = settings;
        this.mListView = listView;
        this.mActivityContext = context;
        this.mPostItemViewBuilder = new PostItemViewBuilder(this.mActivityContext, this.mBoardName, this.mThreadNumber, this.mBitmapManager, this.mSettings);
	}
	
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
    	if(isStatusView(position)){
    		return mInflater.inflate(R.layout.loading, null);
    	}
    	
    	View view = this.mPostItemViewBuilder.getView(this.getItem(position), convertView, this.mIsBusy);

        return view;
    }

	@Override
	public void onClick(View v, String url) {

		Uri uri = Uri.parse(url);
		String pageName = UriUtils.getThreadNumber(uri);
		
		// Если ссылка указывает на этот тред - перескакиваем на нужный пост, иначе открываем в браузере
		if(mThreadNumber.equals(pageName)){
			String postNumber = uri.getFragment();
			// Переходим на тот пост, куда указывает ссылка
			int position = postNumber != null ? findPostByNumber(postNumber) : Constants.OP_POST_POSITION;
			if(position == -1){
				AppearanceUtils.showToastMessage(this.getContext(), this.getContext().getString(R.string.notification_post_not_found));
				return;
			}
			
			if(this.mSettings.isLinksInPopup()){
				this.mPostItemViewBuilder.displayPopupDialog(this.getItem(position), this.mActivityContext, this.mTheme);
			}
			else{
				this.mListView.setSelection(position);
			}
		}
		else {
			ClickListenersFactory.sDvachUrlSpanClickListener.onClick(v, url);
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
		Integer lastPostNumber = !StringUtils.isEmpty(from) ? Integer.valueOf(from) : 0;
		int newPostsCount = 0;

		for(PostInfo pi : posts){
			if(Integer.valueOf(pi.getNum()) > lastPostNumber) {
				this.add(this.mPostsViewModel.createModel(pi, this.mTheme, this));
				newPostsCount++;
			}
		}
		
		// обновить все видимые элементы, чтобы правильно перерисовался список ссылок replies
		if(newPostsCount > 0){
			this.notifyDataSetChanged();
		}

		return newPostsCount;
	}
	
	@Override
	public void setBusy(boolean value, AbsListView listView){
		if(this.mIsBusy == true && value == false){
	        int count = listView.getChildCount();
	        for (int i=0; i<count; i++) {
	            View v = listView.getChildAt(i);
	            int position = listView.getPositionForView(v);

	            this.mPostItemViewBuilder.displayThumbnail(v, this.getItem(position));
	        }
		}
		
		this.mIsBusy = value;
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
}
