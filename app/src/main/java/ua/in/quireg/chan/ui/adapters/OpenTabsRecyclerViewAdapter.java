package ua.in.quireg.chan.ui.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import timber.log.Timber;
import ua.in.quireg.chan.R;
import ua.in.quireg.chan.common.Constants;
import ua.in.quireg.chan.models.presentation.OpenTabModel;
import ua.in.quireg.chan.mvp.presenters.OpenTabsPresenter;
import ua.in.quireg.chan.ui.views.RecyclerViewWithCM;

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

    //    @Override
//    public void onViewRecycled(RecyclerView.ViewHolder holder) {
//        holder.itemView.setOnLongClickListener(null);
//        super.onViewRecycled(holder);
//    }

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

    class OpenTabViewHolder extends RecyclerView.ViewHolder implements View.OnCreateContextMenuListener {

        ImageView deleteButton;
        TextView titleView, urlView;

        OpenTabViewHolder(View view) {
            super(view);
//            view.setOnCreateContextMenuListener(this);
            titleView = view.findViewById(R.id.tabs_item_title);
            urlView = view.findViewById(R.id.tabs_item_url);
            deleteButton = view.findViewById(R.id.tabs_item_delete);
        }

        @Override
        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {

//        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;

            RecyclerViewWithCM.ContextMenuInfo info = (RecyclerViewWithCM.ContextMenuInfo) menuInfo;

            OpenTabModel item = getItem(info.position);

            if (item == null) {
                Timber.e("item == null");
                return;
            }

            menu.add(Menu.NONE, Constants.CONTEXT_MENU_COPY_URL, 0, v.getContext().getString(R.string.cmenu_copy_url));

            if (!item.isFavorite()) {
                menu.add(Menu.NONE, Constants.CONTEXT_MENU_ADD_FAVORITES, 0, v.getContext().getString(R.string.cmenu_add_to_favorites));
            } else {
                menu.add(Menu.NONE, Constants.CONTEXT_MENU_REMOVE_FAVORITES, 0, v.getContext().getString(R.string.cmenu_remove_from_favorites));
            }
        }

    }
}
