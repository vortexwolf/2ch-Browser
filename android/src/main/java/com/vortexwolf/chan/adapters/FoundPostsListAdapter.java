package com.vortexwolf.chan.adapters;

import android.content.Context;
import android.content.res.Resources.Theme;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;

import com.vortexwolf.chan.boards.dvach.DvachUriBuilder;
import com.vortexwolf.chan.interfaces.IBusyAdapter;
import com.vortexwolf.chan.models.domain.AttachmentModel;
import com.vortexwolf.chan.models.domain.PostModel;
import com.vortexwolf.chan.models.presentation.PostItemViewModel;
import com.vortexwolf.chan.services.presentation.ClickListenersFactory;
import com.vortexwolf.chan.services.presentation.PostItemViewBuilder;
import com.vortexwolf.chan.settings.ApplicationSettings;

public class FoundPostsListAdapter extends ArrayAdapter<PostItemViewModel> implements IBusyAdapter {

    private final LayoutInflater mInflater;
    private final String mBoardName;
    private final PostItemViewBuilder mPostItemViewBuilder;
    private final ApplicationSettings mSettings;
    private final Theme mTheme;
    private final DvachUriBuilder mDvachUriBuilder;

    private boolean mIsBusy = false;

    public FoundPostsListAdapter(Context context, String boardName, ApplicationSettings settings, Theme theme, DvachUriBuilder dvachUriBuilder) {
        super(context.getApplicationContext(), 0);

        this.mBoardName = boardName;
        this.mInflater = LayoutInflater.from(context);
        this.mSettings = settings;
        this.mTheme = theme;
        this.mDvachUriBuilder = dvachUriBuilder;

        this.mPostItemViewBuilder = new PostItemViewBuilder(context, this.mBoardName, null, this.mSettings, this.mDvachUriBuilder);
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

    public void setAdapterData(PostModel[] posts) {
        this.clear();

        for (PostModel item : posts) {
            if (item.getAttachments().size() != 0) {
                AttachmentModel attachment = item.getAttachments().get(0);
                String thumbnail = attachment.getThumbnailUrl();
                if (thumbnail != null && thumbnail.startsWith(this.mBoardName + "/")) {
                    attachment.setThumbnailUrl(thumbnail.substring(thumbnail.indexOf("/") + 1, thumbnail.length()));
                }

                String image = attachment.getPath();
                if (image != null && image.startsWith(this.mBoardName + "/")) {
                    attachment.setPath(image.substring(image.indexOf("/") + 1, image.length()));
                }
            }
            PostItemViewModel viewModel = new PostItemViewModel(this.mBoardName, item.getParentThread(), this.getCount(), item, this.mTheme, ClickListenersFactory.getDefaultSpanClickListener(this.mDvachUriBuilder));
            this.add(viewModel);
        }
    }
}
