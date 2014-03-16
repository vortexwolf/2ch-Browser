package com.vortexwolf.dvach.adapters;

import android.content.Context;
import android.content.res.Resources.Theme;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;

import com.vortexwolf.chan.R;
import com.vortexwolf.dvach.common.utils.StringUtils;
import com.vortexwolf.dvach.interfaces.IBitmapManager;
import com.vortexwolf.dvach.interfaces.IBusyAdapter;
import com.vortexwolf.dvach.models.domain.PostInfo;
import com.vortexwolf.dvach.models.presentation.PostItemViewModel;
import com.vortexwolf.dvach.services.presentation.ClickListenersFactory;
import com.vortexwolf.dvach.services.presentation.DvachUriBuilder;
import com.vortexwolf.dvach.services.presentation.PostItemViewBuilder;
import com.vortexwolf.dvach.settings.ApplicationSettings;

public class FoundPostsListAdapter extends ArrayAdapter<PostItemViewModel> implements IBusyAdapter {

    private final LayoutInflater mInflater;
    private final IBitmapManager mBitmapManager;
    private final String mBoardName;
    private final PostItemViewBuilder mPostItemViewBuilder;
    private final ApplicationSettings mSettings;
    private final Theme mTheme;
    private final DvachUriBuilder mDvachUriBuilder;
    
    private boolean mIsBusy = false;
    
    public FoundPostsListAdapter(Context context, String boardName, IBitmapManager bitmapManager, ApplicationSettings settings, Theme theme, DvachUriBuilder dvachUriBuilder) {
        super(context.getApplicationContext(), 0);

        this.mBoardName = boardName;
        this.mBitmapManager = bitmapManager;
        this.mInflater = LayoutInflater.from(context);
        this.mSettings = settings;
        this.mTheme = theme;
        this.mDvachUriBuilder = dvachUriBuilder;
        
        this.mPostItemViewBuilder = new PostItemViewBuilder(context, this.mBoardName, null, this.mBitmapManager, this.mSettings, this.mDvachUriBuilder);
    }
    
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = this.mPostItemViewBuilder.getView(this.getItem(position), convertView, this.mIsBusy);

        return view;
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
    
    public void setAdapterData(PostInfo[] posts) {
        this.clear();

        if (posts == null) {
            return;
        }
        
        for(PostInfo item : posts) {
            String thumbnail = item.getThumbnail();
            if(thumbnail != null && thumbnail.startsWith(this.mBoardName + "/")) {
                item.setThumbnail(thumbnail.substring(thumbnail.indexOf("/") + 1, thumbnail.length()));
            }
            
            String image = item.getImage();
            if(image != null && image.startsWith(this.mBoardName + "/")) {
                item.setImage(image.substring(image.indexOf("/") + 1, image.length()));
            }
            
            PostItemViewModel viewModel = new PostItemViewModel(this.getCount(), item, this.mTheme, this.mSettings, ClickListenersFactory.getDefaultSpanClickListener(this.mDvachUriBuilder), this.mDvachUriBuilder);
            this.add(viewModel);
        }
    }
}
