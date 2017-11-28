package ua.in.quireg.chan.ui.adapters;

import android.content.Context;
import android.content.res.Resources.Theme;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;

import ua.in.quireg.chan.common.Factory;
import ua.in.quireg.chan.interfaces.IBusyAdapter;
import ua.in.quireg.chan.interfaces.IUrlBuilder;
import ua.in.quireg.chan.interfaces.IWebsite;
import ua.in.quireg.chan.models.domain.AttachmentModel;
import ua.in.quireg.chan.models.domain.PostModel;
import ua.in.quireg.chan.models.presentation.PostItemViewModel;
import ua.in.quireg.chan.services.presentation.ClickListenersFactory;
import ua.in.quireg.chan.services.presentation.PostItemViewBuilder;
import ua.in.quireg.chan.settings.ApplicationSettings;

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
