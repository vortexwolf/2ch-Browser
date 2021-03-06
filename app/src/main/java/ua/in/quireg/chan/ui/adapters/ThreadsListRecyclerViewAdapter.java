package ua.in.quireg.chan.ui.adapters;

import android.annotation.SuppressLint;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import timber.log.Timber;
import ua.in.quireg.chan.R;
import ua.in.quireg.chan.common.GlideApp;
import ua.in.quireg.chan.common.MainApplication;
import ua.in.quireg.chan.common.utils.StringUtils;
import ua.in.quireg.chan.models.presentation.IThreadListEntity;
import ua.in.quireg.chan.models.presentation.PageDividerViewModel;
import ua.in.quireg.chan.models.presentation.ThreadItemViewModel;
import ua.in.quireg.chan.models.presentation.ThumbnailViewBag;
import ua.in.quireg.chan.mvp.presenters.ThreadsListPresenter;
import ua.in.quireg.chan.settings.ApplicationSettings;

/**
 * Created by Arcturus Mengsk on 3/17/2018, 5:36 AM.
 * 2ch-Browser
 */

public class ThreadsListRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    @Inject ApplicationSettings mSettings;
    private ThreadsListPresenter mThreadsListPresenter;
    private ArrayList<IThreadListEntity> mItems = new ArrayList<>();

    private static final int ITEM_VIEW_TYPE_THREAD = 0;
    private static final int ITEM_VIEW_TYPE_HIDDEN_THREAD = 1;
    private static final int ITEM_VIEW_TYPE_PAGE_DIVIDER = 2;

    public ThreadsListRecyclerViewAdapter(ThreadsListPresenter threadsListPresenter) {
        MainApplication.getAppComponent().inject(this);
        mThreadsListPresenter = threadsListPresenter;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        switch (viewType) {
            case ITEM_VIEW_TYPE_THREAD: {
                View itemView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.threads_list_item, parent, false);

                ViewGroup.LayoutParams lp = itemView.getLayoutParams();
                lp.height = parent.getHeight() / 6;
                itemView.setLayoutParams(lp);
                return new ThreadViewHolder(itemView);
            }
            case ITEM_VIEW_TYPE_HIDDEN_THREAD: {
                View itemView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.threads_list_hidden_item, parent, false);
                return new HiddenThreadViewHolder(itemView);
            }
            case ITEM_VIEW_TYPE_PAGE_DIVIDER: {
                View itemView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.threads_list_page_divider, parent, false);
                return new PageDividerViewHolder(itemView);
            }
            default: {
                throw new IllegalArgumentException("Unknown view type");
            }
        }
    }

    @Override
    @SuppressLint("DefaultLocale")
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        mThreadsListPresenter
                .setListPosition(holder.getAdapterPosition(), getItemCount());
        IThreadListEntity item = mItems.get(position);
        if (item instanceof ThreadItemViewModel) {
            if (holder instanceof ThreadViewHolder) {
                fillItemView((ThreadViewHolder) holder, (ThreadItemViewModel) item);
            } else if (holder instanceof HiddenThreadViewHolder) {
                fillHiddenThreadView((HiddenThreadViewHolder) holder, (ThreadItemViewModel) item);
            }
        } else if (item instanceof PageDividerViewModel
                && holder instanceof PageDividerViewHolder) {
                fillPageDividerView((PageDividerViewHolder) holder, (PageDividerViewModel) item);
        }
    }

    @Override
    public int getItemViewType(int position) {
        switch (getItem(position).getType()) {
            case THREAD:
                return ITEM_VIEW_TYPE_THREAD;
            case HIDDEN_THREAD:
                return ITEM_VIEW_TYPE_HIDDEN_THREAD;
            case DIVIDER:
                return ITEM_VIEW_TYPE_PAGE_DIVIDER;
        }
        return -1;
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    public void setList(List<IThreadListEntity> list) {
        mItems.clear();
        mItems.addAll(list);
        notifyDataSetChanged();
    }

    public void addToList(IThreadListEntity item) {
        int position = mItems.indexOf(item);
        if (position == -1) {
            mItems.add(item);
            notifyItemInserted(getItemCount() - 1);
        } else {
            mItems.remove(position);
            mItems.add(position, item);
            notifyItemChanged(position);
        }
    }

    public void removeFromList(IThreadListEntity item) {
        int position = mItems.indexOf(item);
        if (position == -1) {
            Timber.e("Nothing to remove");
            return;
        }
        mItems.remove(position);
        notifyItemRemoved(position);
    }

    public void clearList() {
        int itemCount = getItemCount();
        if (itemCount > 0) {
            mItems.clear();
            notifyItemRangeRemoved(0, itemCount);
        }
    }

    public IThreadListEntity getItem(int position) {
        return mItems.get(position);
    }

    private void fillItemView(final ThreadViewHolder holder, final ThreadItemViewModel item) {
        String subject = item.getSubject();
        if (!StringUtils.isEmpty(subject) && mSettings.isDisplaySubject()) {
            holder.titleView.setVisibility(View.VISIBLE);
            holder.titleView.setText(subject);
        } else {
            holder.titleView.setVisibility(View.GONE);
        }

        // Комментарий
        holder.commentView.setText(item.getSpannedComment());
        holder.commentView.setTag(item);

        // Количество ответов
        String postsQuantity = holder.itemView.getContext().getResources().getQuantityString(R.plurals.data_posts_quantity, item.getReplyCount(), item.getReplyCount());
        String imagesQuantity = holder.itemView.getContext().getResources().getQuantityString(R.plurals.data_files_quantity, item.getImageCount(), item.getImageCount());
        String repliesFormat = holder.itemView.getContext().getString(R.string.data_posts_files);
        String repliesText = String.format(repliesFormat, postsQuantity, imagesQuantity);
        holder.repliesNumberView.setText(repliesText);

        if (mSettings.isMultiThumbnailsInThreads() && item.getAttachmentsNumber() > 1) {
            holder.multiThumbnailsView.setVisibility(View.VISIBLE);
            holder.singleThumbnailView.hide();
            for (int i = 0; i < 4; ++i) {
                if (item.getAttachment(i) == null || item.getAttachment(i).isEmpty()) {
                    holder.thumbnailViews[i].hide();
                    continue;
                }
                GlideApp.with(holder.itemView.getContext())
                        .load(item.getAttachment(i).getThumbnailUrl())
                        .into(holder.thumbnailViews[i].image);

                holder.thumbnailViews[i].container.setVisibility(View.VISIBLE);
                holder.thumbnailViews[i].info.setText(item.getAttachment(i).getDescription());
            }
        } else if (item.getAttachmentsNumber() >= 1) {
            if (item.getAttachment(0) != null && !item.getAttachment(0).isEmpty()) {
                holder.singleThumbnailView.info
                        .setText(item.getAttachment(0).getDescription());
                GlideApp.with(holder.itemView.getContext())
                        .load(item.getAttachment(0).getThumbnailUrl())
                        .placeholder(holder.itemView.getContext().getDrawable(R.drawable.doge))
                        .into(holder.singleThumbnailView.image);
            }
        }
    }

    private void fillHiddenThreadView(final HiddenThreadViewHolder holder,
                                      final ThreadItemViewModel item) {
        holder.threadNumberView.setText(String.format("№%s", item.getNumber()));
        holder.threadDescriptionView.setText(item.getSubjectOrText());
    }

    @SuppressLint("DefaultLocale")
    private void fillPageDividerView(final PageDividerViewHolder holder,
                                     final PageDividerViewModel item) {
        String s;
        if (item.getPage() == ThreadsListPresenter.FIRST_PAGE) {
            s = String.format(holder.itemView.getContext()
                    .getString(R.string.thread_list_page_divider_first_page), "/b");
        } else if (item.getPage() == ThreadsListPresenter.FINAL_PAGE) {
            s = holder.itemView.getContext().getString(R.string.thread_list_page_divider_last_page);
        } else {
            s = String.format(holder.itemView.getContext()
                    .getString(R.string.thread_list_page_divider_page_number), item.getPage());
        }
        holder.itemView.setTag(item);
//        holder.pageNumberTextView.setTag(item);
        holder.pageNumberTextView.setText(s);
    }

    private class ThreadViewHolder extends RecyclerView.ViewHolder {

        TextView titleView;
        TextView commentView;
        TextView repliesNumberView;
        ThumbnailViewBag singleThumbnailView;
        View multiThumbnailsView;
        ThumbnailViewBag[] thumbnailViews;

        ThreadViewHolder(View view) {
            super(view);
            titleView = view.findViewById(R.id.title);
            commentView = view.findViewById(R.id.comment);
            repliesNumberView = view.findViewById(R.id.repliesNumber);
            singleThumbnailView = ThumbnailViewBag.fromView(view.findViewById(R.id.thumbnail_view));

            multiThumbnailsView = view.findViewById(R.id.multi_thumbnails_view);
            thumbnailViews = new ThumbnailViewBag[4];
            thumbnailViews[0] = ThumbnailViewBag.fromView(view.findViewById(R.id.thumbnail_view_1));
            thumbnailViews[1] = ThumbnailViewBag.fromView(view.findViewById(R.id.thumbnail_view_2));
            thumbnailViews[2] = ThumbnailViewBag.fromView(view.findViewById(R.id.thumbnail_view_3));
            thumbnailViews[3] = ThumbnailViewBag.fromView(view.findViewById(R.id.thumbnail_view_4));

            view.setOnClickListener((View v) -> {
                mThreadsListPresenter.onItemClick(
                        (ThreadItemViewModel) mItems.get(getAdapterPosition()));
            });
            view.setOnLongClickListener((View v) -> {
                view.showContextMenu();
                return true;
            });
        }
    }

    private class HiddenThreadViewHolder extends RecyclerView.ViewHolder {
        TextView threadNumberView, threadDescriptionView;

        HiddenThreadViewHolder(View view) {
            super(view);
            threadNumberView = view.findViewById(R.id.thread_number);
            threadDescriptionView = view.findViewById(R.id.thread_description);
            view.setOnClickListener((View v) ->
                    mThreadsListPresenter.unHideThread(
                            (ThreadItemViewModel) mItems.get(getAdapterPosition())));
        }
    }

    private class PageDividerViewHolder extends RecyclerView.ViewHolder {
        TextView pageNumberTextView;
        PageDividerViewHolder(View view) {
            super(view);
            pageNumberTextView = view.findViewById(R.id.page_number);
        }
    }
}
