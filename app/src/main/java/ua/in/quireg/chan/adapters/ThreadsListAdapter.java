package ua.in.quireg.chan.adapters;

import android.content.Context;
import android.content.res.Resources.Theme;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import ua.in.quireg.chan.R;
import ua.in.quireg.chan.common.Factory;
import ua.in.quireg.chan.views.controls.EllipsizingTextView;
import ua.in.quireg.chan.common.utils.StringUtils;
import ua.in.quireg.chan.common.utils.ThreadPostUtils;
import ua.in.quireg.chan.db.HiddenThreadsDataSource;
import ua.in.quireg.chan.interfaces.IBusyAdapter;
import ua.in.quireg.chan.interfaces.IUrlBuilder;
import ua.in.quireg.chan.interfaces.IWebsite;
import ua.in.quireg.chan.models.domain.ThreadModel;
import ua.in.quireg.chan.models.presentation.ThreadItemViewModel;
import ua.in.quireg.chan.models.presentation.ThumbnailViewBag;
import ua.in.quireg.chan.services.ThreadImagesService;
import ua.in.quireg.chan.settings.ApplicationSettings;

import java.util.Timer;
import java.util.TimerTask;

public class ThreadsListAdapter extends ArrayAdapter<ThreadItemViewModel> implements IBusyAdapter {
    private static final int ITEM_VIEW_TYPE_THREAD = 0;
    private static final int ITEM_VIEW_TYPE_HIDDEN_THREAD = 1;

    private final LayoutInflater mInflater;
    private final Theme mTheme;
    private final ApplicationSettings mSettings;
    private final HiddenThreadsDataSource mHiddenThreadsDataSource;
    private final ThreadImagesService mThreadImagesService;
    private final ListView mListView;
    private final IUrlBuilder mUrlBuilder;
    private final Timer mLoadImagesTimer = new Timer();
    private final IWebsite mWebsite;
    private final String mBoardName;

    private LoadImagesTimerTask mCurrentLoadImagesTask;
    private boolean mIsBusy = false;

    public ThreadsListAdapter(Context context, IWebsite website, String boardName, Theme theme, ListView listView) {
        super(context.getApplicationContext(), 0);

        this.mWebsite = website;
        this.mBoardName = boardName;
        this.mTheme = theme;
        this.mInflater = LayoutInflater.from(context);
        this.mSettings = Factory.resolve(ApplicationSettings.class);
        this.mHiddenThreadsDataSource = Factory.resolve(HiddenThreadsDataSource.class);
        this.mUrlBuilder = this.mWebsite.getUrlBuilder();
        this.mThreadImagesService = Factory.resolve(ThreadImagesService.class);
        this.mListView = listView;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public int getItemViewType(int position) {
        return this.getItem(position).isHidden() ? ITEM_VIEW_TYPE_HIDDEN_THREAD : ITEM_VIEW_TYPE_THREAD;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View view;
        ThreadItemViewModel item = this.getItem(position);

        if (!item.isHidden()) {
            view = convertView != null ? convertView : this.mInflater.inflate(R.layout.threads_list_item, null);

            this.fillItemView(view, item);
        } else {
            view = convertView != null ? convertView : this.mInflater.inflate(R.layout.threads_list_hidden_item, null);

            this.fillHiddenThreadView(view, item);
        }

        return view;
    }

    private void fillHiddenThreadView(final View view, final ThreadItemViewModel item) {
        TextView threadNumberView = (TextView) view.findViewById(R.id.thread_number);
        TextView threadDescriptionView = (TextView) view.findViewById(R.id.thread_description);

        threadNumberView.setText("№" + item.getNumber());

        CharSequence description = item.getSubjectOrText();
        threadDescriptionView.setText(description);
    }

    private void fillItemView(final View view, final ThreadItemViewModel item) {
        // Get inner controls
        ViewBag vb = (ViewBag) view.getTag();
        if (vb == null) {
            vb = new ViewBag();
            vb.titleView = (TextView) view.findViewById(R.id.title);
            vb.commentView = (EllipsizingTextView) view.findViewById(R.id.comment);
            vb.repliesNumberView = (TextView) view.findViewById(R.id.repliesNumber);
            vb.singleThumbnailView = ThumbnailViewBag.fromView(view.findViewById(R.id.thumbnail_view));

            vb.multiThumbnailsView = view.findViewById(R.id.multi_thumbnails_view);
            vb.thumbnailViews = new ThumbnailViewBag[4];
            vb.thumbnailViews[0] = ThumbnailViewBag.fromView(view.findViewById(R.id.thumbnail_view_1));
            vb.thumbnailViews[1] = ThumbnailViewBag.fromView(view.findViewById(R.id.thumbnail_view_2));
            vb.thumbnailViews[2] = ThumbnailViewBag.fromView(view.findViewById(R.id.thumbnail_view_3));
            vb.thumbnailViews[3] = ThumbnailViewBag.fromView(view.findViewById(R.id.thumbnail_view_4));

            view.setTag(vb);
        }

        // Apply info from the data item
        String subject = item.getSubject();
        if (!StringUtils.isEmpty(subject)) {
            vb.titleView.setVisibility(View.VISIBLE);
            vb.titleView.setText(subject);
        } else {
            vb.titleView.setVisibility(View.GONE);
        }

        // Комментарий
        vb.commentView.setText(item.getSpannedComment());
        vb.commentView.setTag(item);
        vb.commentView.setEllipsizeListener(new EllipsizingTextView.EllipsizeListener() {
            @Override
            public void ellipsizeStateChanged(EllipsizingTextView view, boolean ellipsized) {
                ThreadItemViewModel boundItem = (ThreadItemViewModel) view.getTag();
                boundItem.setEllipsized(ellipsized);
            }
        });

        // Количество ответов
        String postsQuantity = this.getContext().getResources().getQuantityString(R.plurals.data_posts_quantity, item.getReplyCount(), item.getReplyCount());
        String imagesQuantity = this.getContext().getResources().getQuantityString(R.plurals.data_files_quantity, item.getImageCount(), item.getImageCount());
        String repliesFormat = this.getContext().getString(R.string.data_posts_files);
        String repliesText = String.format(repliesFormat, postsQuantity, imagesQuantity);
        vb.repliesNumberView.setText(repliesText);

        // Обрабатываем прикрепленные файлы
        String threadUrl = this.mUrlBuilder.getThreadUrlHtml(this.mBoardName, item.getNumber());
        if (!this.mThreadImagesService.hasThreadImages(threadUrl)) {
            for (int i = 0; i < 4; ++i) {
                this.mThreadImagesService.addThreadImage(threadUrl, item.getAttachment(i));
            }
        }

        if (mSettings.isMultiThumbnailsInThreads() && item.getAttachmentsNumber() > 1) {
            vb.multiThumbnailsView.setVisibility(View.VISIBLE);
            vb.singleThumbnailView.hide();
            for (int i = 0; i < 4; ++i) {
                ThreadPostUtils.refreshAttachmentView(this.mIsBusy, item.getAttachment(i), vb.thumbnailViews[i]);
            }
        } else if (item.getAttachmentsNumber() >= 1) {
            vb.multiThumbnailsView.setVisibility(View.GONE);
            ThreadPostUtils.refreshAttachmentView(this.mIsBusy, item.getAttachment(0), vb.singleThumbnailView);
        } else {
            vb.multiThumbnailsView.setVisibility(View.GONE);
            vb.singleThumbnailView.hide();
        }
    }

    /** Обновляет адаптер полностью */
    public void setAdapterData(ThreadModel[] threads) {
        this.clear();

        for (ThreadModel ti : threads) {
            ThreadItemViewModel model = new ThreadItemViewModel(this.mWebsite, this.mBoardName, ti, this.mTheme);
            boolean isHidden = this.mHiddenThreadsDataSource.isHidden(this.mWebsite.name(), this.mBoardName, model.getNumber());
            model.setHidden(isHidden);

            this.add(model);
        }
    }

    @Override
    public void setBusy(boolean value, AbsListView view) {
        if (this.mCurrentLoadImagesTask != null) {
            this.mCurrentLoadImagesTask.cancel();
        }

        if (this.mIsBusy == true && value == false) {
            this.mCurrentLoadImagesTask = new LoadImagesTimerTask();
            this.mLoadImagesTimer.schedule(this.mCurrentLoadImagesTask, 500);
        }

        this.mIsBusy = value;
    }

    private void loadListImages() {
        int count = this.mListView.getChildCount();
        for (int i = 0; i < count; i++) {
            View v = this.mListView.getChildAt(i);
            int position = this.mListView.getPositionForView(v);

            if (this.getItemViewType(position) == ITEM_VIEW_TYPE_HIDDEN_THREAD) {
                continue;
            }

            ViewBag vb = (ViewBag) v.getTag();
            ThreadItemViewModel item = this.getItem(position);
            if (mSettings.isMultiThumbnailsInThreads() && item.getAttachmentsNumber() > 1) {
                for (int j = 0; j < 4; ++j) {
                    ThreadPostUtils.setNonBusyAttachment(item.getAttachment(j), vb.thumbnailViews[j].image);
                }
            } else if (item.getAttachmentsNumber() >= 1) {
                ThreadPostUtils.setNonBusyAttachment(item.getAttachment(0), vb.singleThumbnailView.image);
            }
        }
    }

    private class LoadImagesTimerTask extends TimerTask {
        @Override
        public void run() {
            ThreadsListAdapter.this.mListView.post(new LoadImagesRunnable());
        }
    }

    private class LoadImagesRunnable implements Runnable {
        @Override
        public void run() {
            ThreadsListAdapter.this.loadListImages();
        }
    }

    static class ViewBag {
        TextView titleView;
        EllipsizingTextView commentView;
        TextView repliesNumberView;
        ThumbnailViewBag singleThumbnailView;
        View multiThumbnailsView;
        ThumbnailViewBag[] thumbnailViews;
    }
}
