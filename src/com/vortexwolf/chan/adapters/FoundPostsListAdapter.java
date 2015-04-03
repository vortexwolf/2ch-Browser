package com.vortexwolf.chan.adapters;

import android.content.Context;
import android.content.res.Resources.Theme;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;

import com.vortexwolf.chan.common.Factory;
import com.vortexwolf.chan.common.Websites;
import com.vortexwolf.chan.interfaces.IBusyAdapter;
import com.vortexwolf.chan.interfaces.IUrlBuilder;
import com.vortexwolf.chan.interfaces.IWebsite;
import com.vortexwolf.chan.models.domain.AttachmentModel;
import com.vortexwolf.chan.models.domain.PostModel;
import com.vortexwolf.chan.models.presentation.PostItemViewModel;
import com.vortexwolf.chan.services.presentation.ClickListenersFactory;
import com.vortexwolf.chan.services.presentation.PostItemViewBuilder;
import com.vortexwolf.chan.settings.ApplicationSettings;

public class FoundPostsListAdapter extends ArrayAdapter<PostItemViewModel> implements IBusyAdapter {

    private final LayoutInflater mInflater;
    private final IWebsite mWebsite;
    private final String mBoardName;
    private final PostItemViewBuilder mPostItemViewBuilder;
    private final ApplicationSettings mSettings;
    private final Theme mTheme;
    private final IUrlBuilder mUrlBuilder;

    private boolean mIsBusy = false;

    public FoundPostsListAdapter(Context context, IWebsite website, String boardName, Theme theme) {
        super(context.getApplicationContext(), 0);

        this.mWebsite = website;
        this.mBoardName = boardName;
        this.mInflater = LayoutInflater.from(context);
        this.mSettings = Factory.resolve(ApplicationSettings.class);
        this.mTheme = theme;
        this.mUrlBuilder = website.getUrlBuilder();
        this.mPostItemViewBuilder = new PostItemViewBuilder(context, this.mWebsite, this.mBoardName, null, this.mSettings);
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
            PostItemViewModel viewModel = new PostItemViewModel(this.mWebsite, this.mBoardName, item.getParentThread(), this.getCount(), item, this.mTheme, ClickListenersFactory.getDefaultSpanClickListener(this.mUrlBuilder));
            this.add(viewModel);
        }
    }
}
