package com.vortexwolf.chan.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;

import com.vortexwolf.chan.R;
import com.vortexwolf.chan.common.utils.StringUtils;
import com.vortexwolf.chan.db.FavoritesDataSource;
import com.vortexwolf.chan.db.HistoryEntity;

import java.util.List;

public class HistoryAdapter extends ArrayAdapter<HistoryEntity> {

    private final LayoutInflater mInflater;
    private final FavoritesDataSource mFavoritesDataSource;

    private List<HistoryEntity> mOriginalItems;
    private String mSearchQuery;

    public HistoryAdapter(Context context, FavoritesDataSource favoritesDataSource) {
        super(context, -1);
        this.mInflater = LayoutInflater.from(context);
        this.mFavoritesDataSource = favoritesDataSource;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final HistoryEntity item = this.getItem(position);

        View view = convertView == null ? this.mInflater.inflate(R.layout.history_list_item, null) : convertView;

        ViewBag vb = (ViewBag) view.getTag();
        if (vb == null) {
            vb = new ViewBag();
            vb.titleView = (TextView) view.findViewById(R.id.title);
            vb.urlView = (TextView) view.findViewById(R.id.url);
            vb.starView = (CheckBox) view.findViewById(R.id.star);

            view.setTag(vb);
        }

        vb.titleView.setText(item.getTitleOrDefault());
        vb.urlView.setText(item.buildUrl());

        boolean isInFavorites = this.mFavoritesDataSource.hasFavorites(item.getWebsite(), item.getBoard(), item.getThread());
        vb.starView.setOnCheckedChangeListener(null);
        vb.starView.setChecked(isInFavorites);

        vb.starView.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    HistoryAdapter.this.mFavoritesDataSource.addToFavorites(item.getWebsite(), item.getBoard(), item.getThread(), item.getTitle());
                } else {
                    HistoryAdapter.this.mFavoritesDataSource.removeFromFavorites(item.getWebsite(), item.getBoard(), item.getThread());
                }
            }
        });

        return view;
    }

    public void setItems(List<HistoryEntity> items) {
        this.mOriginalItems = items;
        this.refreshVisibleItems();
    }

    public void searchItems(String query) {
        query = !StringUtils.isEmptyOrWhiteSpace(query) ? query.toLowerCase() : null;
        if (StringUtils.areEqual(this.mSearchQuery, query)) {
            return;
        }

        this.mSearchQuery = query;
        this.refreshVisibleItems();
    }

    private void refreshVisibleItems() {
        if (this.mOriginalItems == null) {
            return;
        }

        this.clear();
        for (HistoryEntity item : this.mOriginalItems) {
            if (this.mSearchQuery == null
                || item.getTitleOrDefault().toLowerCase().contains(this.mSearchQuery)
                || item.getBoard().contains(this.mSearchQuery)
                || this.mSearchQuery.contains("/") && item.buildUrl().contains(this.mSearchQuery)) {
                this.add(item);
            }
        }
    }

    static class ViewBag {
        TextView titleView;
        TextView urlView;
        CheckBox starView;
    }
}
