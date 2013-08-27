package com.vortexwolf.dvach.adapters;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
import com.vortexwolf.dvach.common.library.MyLog;
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
import com.vortexwolf.dvach.services.presentation.DvachUriBuilder;
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
    private final DvachUriBuilder mDvachUriBuilder;

    private boolean mIsBusy = false;
    private boolean mIsLoadingMore = false;

    public PostsListAdapter(Context context, String boardName, String threadNumber, IBitmapManager bitmapManager, ApplicationSettings settings, Theme theme, ListView listView, DvachUriBuilder dvachUriBuilder) {
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
        this.mDvachUriBuilder = dvachUriBuilder;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (this.isStatusView(position)) {
            return this.mInflater.inflate(R.layout.loading, null);
        }

        View view = this.mPostItemViewBuilder.getView(this.getItem(position), convertView, this.mIsBusy);

        // cut long posts if necessary
        int maxPostHeight = this.mSettings.getLongPostsMaxHeight();
        if (maxPostHeight > 0) {
            this.mPostItemViewBuilder.setMaxHeight(view, maxPostHeight, this.mTheme);
        } else {
            this.mPostItemViewBuilder.removeMaxHeight(view);
        }
        
        return view;
    }

    @Override
    public void onClick(View v, String url) {

        Uri uri = Uri.parse(url);
        String pageName = UriUtils.getThreadNumber(uri);

        // Если ссылка указывает на этот тред - перескакиваем на нужный пост,
        // иначе открываем в браузере
        if (this.mThreadNumber.equals(pageName)) {
            String postNumber = uri.getFragment();
            // Переходим на тот пост, куда указывает ссылка
            int position = postNumber != null ? this.findPostByNumber(postNumber) : Constants.OP_POST_POSITION;
            if (position == -1) {
                AppearanceUtils.showToastMessage(this.getContext(), this.getContext().getString(R.string.notification_post_not_found));
                return;
            }

            if (this.mSettings.isLinksInPopup()) {
                this.mPostItemViewBuilder.displayPopupDialog(this.getItem(position), this.mActivityContext, this.mTheme);
            } else {
                this.mListView.setSelection(position);
            }
        } else {
            ClickListenersFactory.getDefaultSpanClickListener(this.mDvachUriBuilder).onClick(v, url);
        }
    }

    private int findPostByNumber(String postNumber) {
        PostItemViewModel vm = this.mPostsViewModel.getModel(postNumber);
        if (vm != null) {
            return vm.getPosition();
        }
        return -1;
    }

    /** Возвращает номер последнего сообщения */
    public String getLastPostNumber() {
        return this.mPostsViewModel.getLastPostNumber();
    }

    /** Обновляет адаптер полностью */
    public void setAdapterData(PostInfo[] posts) {
        this.clear();
        
        List<PostItemViewModel> models = this.mPostsViewModel.addModels(Arrays.asList(posts), this.mTheme, this.mSettings, this, this.mDvachUriBuilder);
        for (PostItemViewModel model : models) {
            this.add(model);
        }
    }
    
    public void scrollToPost(String postNumber) {
        if (StringUtils.isEmpty(postNumber)) {
            return;
        }
        
        int position = this.findPostByNumber(postNumber);
        if (position == -1) {
            AppearanceUtils.showToastMessage(this.getContext(), this.getContext().getString(R.string.notification_post_not_found));
            return;
        }
        
        this.mListView.setSelection(position);
    }

    /**
     * Добавляет новые данные в адаптер
     * 
     * @param from
     *            Начиная с какого сообщения добавлять данные
     * @param posts
     *            Список сообщений (можно и всех, они потом отфильтруются)
     */
    public int updateAdapterData(String from, PostInfo[] posts) {
        Integer lastPostNumber;
        try {
            lastPostNumber = !StringUtils.isEmpty(from) ? Integer.valueOf(from) : 0;
        } catch (NumberFormatException e) {
            lastPostNumber = 0;
        }

        ArrayList<PostInfo> newPosts = new ArrayList<PostInfo>();
        for (PostInfo pi : posts) {
            Integer currentNumber = !StringUtils.isEmpty(pi.getNum()) ? Integer.valueOf(pi.getNum()) : 0;
            if (currentNumber > lastPostNumber) {
                newPosts.add(pi);
            }
        }
        
        List<PostItemViewModel> newModels = this.mPostsViewModel.addModels(newPosts, this.mTheme, this.mSettings, this, this.mDvachUriBuilder);
        for (PostItemViewModel model : newModels) {
            this.add(model);
        }

        // обновить все видимые элементы, чтобы правильно перерисовался список
        // ссылок replies
        if (newPosts.size() > 0) {
            this.notifyDataSetChanged();
        }

        return newPosts.size();
    }

    @Override
    public void setBusy(boolean value, AbsListView listView) {
        if (this.mIsBusy == true && value == false) {
            int count = listView.getChildCount();
            for (int i = 0; i < count; i++) {
                View v = listView.getChildAt(i);
                int position = listView.getPositionForView(v);

                this.mPostItemViewBuilder.displayThumbnail(v, this.getItem(position));
            }
        }

        this.mIsBusy = value;
    }

    public void setLoadingMore(boolean isLoadingMore) {
        this.mIsLoadingMore = isLoadingMore;
        this.notifyDataSetChanged();
    }

    private final boolean hasStatusView() {
        return this.mIsLoadingMore;
    }

    private final boolean isStatusView(int position) {
        return this.hasStatusView() && position == this.getCount() - 1;
    }

    @Override
    public int getCount() {
        int i = super.getCount();
        if (this.hasStatusView()) {
            i++;
        }
        return i;
    }

    @Override
    public PostItemViewModel getItem(int position) {
        return (position < super.getCount()) ? super.getItem(position) : null;
    }

    @Override
    public long getItemId(int position) {
        return this.isStatusView(position) ? Long.MIN_VALUE : super.getItemId(position);
    }

    @Override
    public int getItemViewType(int position) {
        return this.isStatusView(position) ? Adapter.IGNORE_ITEM_VIEW_TYPE : super.getItemViewType(position);
    }

    @Override
    public boolean isEnabled(int position) {
        return !this.isStatusView(position);
    }
}
