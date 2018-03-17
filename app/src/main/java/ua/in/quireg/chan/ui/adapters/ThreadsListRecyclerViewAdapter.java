package ua.in.quireg.chan.ui.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import timber.log.Timber;
import ua.in.quireg.chan.R;
import ua.in.quireg.chan.models.presentation.IThreadListEntity;
import ua.in.quireg.chan.models.presentation.ThreadItemViewModel;
import ua.in.quireg.chan.mvp.presenters.ThreadsListPresenter;

/**
 * Created by Arcturus Mengsk on 3/17/2018, 5:36 AM.
 * 2ch-Browser
 */

public class ThreadsListRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private ThreadsListPresenter mThreadsListPresenter;
    private ArrayList<IThreadListEntity> mItems = new ArrayList<>();

    private static final int ITEM_VIEW_TYPE_THREAD = 0;
    private static final int ITEM_VIEW_TYPE_HIDDEN_THREAD = 1;
    private static final int ITEM_VIEW_TYPE_DIVIDER = 2;

    public ThreadsListRecyclerViewAdapter(ThreadsListPresenter threadsListPresenter) {
        mThreadsListPresenter = threadsListPresenter;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        switch (viewType) {
            case ITEM_VIEW_TYPE_THREAD: {
                View itemView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.threads_list_item, parent, false);
                return new ThreadViewHolder(itemView);
            }
            case ITEM_VIEW_TYPE_HIDDEN_THREAD: {
                View itemView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.threads_list_hidden_item, parent, false);
                return new ThreadViewHolder(itemView);
            }
            default: {
                throw new RuntimeException("Unknown view type");
            }
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        if (holder instanceof ThreadViewHolder) {
            ThreadItemViewModel item = (ThreadItemViewModel) mItems.get(position);

//            ((ThreadsListRecyclerViewAdapter.ThreadViewHolder) holder).titleView.setText(item.getTitleOrDefault());
//            ((ThreadsListRecyclerViewAdapter.ThreadViewHolder) holder).urlView.setText(item.buildUrl());
//            ((ThreadsListRecyclerViewAdapter.ThreadViewHolder) holder).deleteButton.setOnClickListener(v -> mOpenTabsPresenter.removeItem(item));
//            ((ThreadsListRecyclerViewAdapter.ThreadViewHolder) holder).itemView.setOnClickListener(v -> mOpenTabsPresenter.navigate(item));
        }
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        switch (getItem(position).getType()){
            case THREAD:
                return ITEM_VIEW_TYPE_THREAD;
            case HIDDEN_THREAD:
                return ITEM_VIEW_TYPE_HIDDEN_THREAD;
            case DIVIDER:
                return ITEM_VIEW_TYPE_DIVIDER;
        }
        throw new RuntimeException("Unknown view type!");
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    public void addToList(ThreadItemViewModel item) {

        int position = mItems.indexOf(item);

        if (position == -1) {
            mItems.add(item);
            notifyItemInserted(getItemCount());
        } else {
            mItems.remove(position);
            mItems.add(position, item);
            notifyItemChanged(position);
        }
    }

    public void removeFromList(ThreadItemViewModel item) {
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

    private class ThreadViewHolder extends RecyclerView.ViewHolder {

        ImageView deleteButton;
        TextView titleView, urlView;

        ThreadViewHolder(View view) {
            super(view);
            titleView = view.findViewById(R.id.tabs_item_title);
            urlView = view.findViewById(R.id.tabs_item_url);
            deleteButton = view.findViewById(R.id.tabs_item_delete);

            view.setOnLongClickListener((View v) -> {
                v.showContextMenu();
                return true;
            });
        }
    }
}
