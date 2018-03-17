package ua.in.quireg.chan.ui.adapters;

import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import timber.log.Timber;
import ua.in.quireg.chan.R;
import ua.in.quireg.chan.models.presentation.OpenTabModel;
import ua.in.quireg.chan.mvp.presenters.OpenTabsPresenter;

public class OpenTabsRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private OpenTabsPresenter mOpenTabsPresenter;
    private ArrayList<OpenTabModel> mOpenedTabsList = new ArrayList<>();

    private static final int TYPE_BOARD = 0;

    public OpenTabsRecyclerViewAdapter(OpenTabsPresenter openTabsPresenter) {
        mOpenTabsPresenter = openTabsPresenter;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        switch (viewType) {
            case TYPE_BOARD: {
                View itemView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.open_tabs_list_item, parent, false);
                return new OpenTabViewHolder(itemView);
            }
            default: {
                throw new RuntimeException("Unknown view type");
            }
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        if (holder instanceof OpenTabViewHolder) {
            OpenTabModel item = mOpenedTabsList.get(position);
            ((OpenTabViewHolder) holder).update(item);

            ((OpenTabViewHolder) holder).titleView.setText(item.getTitleOrDefault());
            ((OpenTabViewHolder) holder).urlView.setText(item.buildUrl());
            ((OpenTabViewHolder) holder).deleteButton.setOnClickListener(v -> mOpenTabsPresenter.removeItem(item));
            ((OpenTabViewHolder) holder).itemView.setOnClickListener(v -> mOpenTabsPresenter.navigate(item));
        }
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        return TYPE_BOARD;
    }

    @Override
    public int getItemCount() {
        return mOpenedTabsList.size();
    }

    public void addToList(OpenTabModel openTabModel) {

        int position = mOpenedTabsList.indexOf(openTabModel);

        if (position == -1) {
            mOpenedTabsList.add(openTabModel);
            notifyItemInserted(getItemCount());
        } else {
            mOpenedTabsList.remove(position);
            mOpenedTabsList.add(position, openTabModel);
            notifyItemChanged(position);
        }
    }

    public void removeFromList(OpenTabModel openTabModel) {
        int position = mOpenedTabsList.indexOf(openTabModel);
        if (position == -1) {
            Timber.e("Nothing to remove");
            return;
        }
        mOpenedTabsList.remove(position);
        notifyItemRemoved(position);
    }

    public void clearList() {
        int itemCount = getItemCount();
        if (itemCount > 0) {
            mOpenedTabsList.clear();
            notifyItemRangeRemoved(0, itemCount);
        }
    }

    public OpenTabModel getItem(int position) {
        return mOpenedTabsList.get(position);
    }
    private boolean multiSelect = false;
    private ArrayList<OpenTabModel> selectedItems = new ArrayList<>();

    private class OpenTabViewHolder extends RecyclerView.ViewHolder {

        ImageView deleteButton;
        TextView titleView, urlView;

        OpenTabViewHolder(View view) {
            super(view);
            titleView = view.findViewById(R.id.tabs_item_title);
            urlView = view.findViewById(R.id.tabs_item_url);
            deleteButton = view.findViewById(R.id.tabs_item_delete);
        }
        void selectItem(OpenTabModel item) {
            if (multiSelect) {
                if (selectedItems.contains(item)) {
                    selectedItems.remove(item);
                    itemView.setBackgroundColor(Color.WHITE);
                } else {
                    selectedItems.add(item);
                    itemView.setBackgroundColor(Color.LTGRAY);
                }
            }
        }

        void update(final OpenTabModel value) {
            if (selectedItems.contains(value)) {
                itemView.setBackgroundColor(Color.LTGRAY);
            } else {
                itemView.setBackgroundColor(Color.WHITE);
            }

            itemView.setOnLongClickListener(view -> {
                ((AppCompatActivity)view.getContext()).startSupportActionMode(new ActionMode.Callback() {
                    @Override
                    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                        menu.add("Delete");
                        return true;
                    }

                    @Override
                    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                        return false;
                    }

                    @Override
                    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                        return false;
                    }

                    @Override
                    public void onDestroyActionMode(ActionMode mode) {

                    }
                });
                selectItem(value);
                return true;
            });
            itemView.setOnClickListener(view -> selectItem(value));
        }
    }
}
