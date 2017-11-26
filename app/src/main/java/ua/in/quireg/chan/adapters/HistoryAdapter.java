package ua.in.quireg.chan.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import java.util.List;

import javax.inject.Inject;

import ua.in.quireg.chan.R;
import ua.in.quireg.chan.common.utils.StringUtils;
import ua.in.quireg.chan.db.FavoritesDataSource;
import ua.in.quireg.chan.db.HistoryEntity;

@SuppressWarnings("WeakerAccess")
public class HistoryAdapter extends ArrayAdapter<HistoryEntity> {

    @Inject protected FavoritesDataSource mFavoritesDataSource;

    private List<HistoryEntity> mOriginalItems;
    private String mSearchQuery;

    public HistoryAdapter(@NonNull Context context) {
        super(context, -1);
    }

    @Override
    @NonNull
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.history_list_item, null);
        }
        HistoryEntity item = getItem(position);
        if (item == null) {
            return convertView;
        }

        ViewBag vb = (ViewBag) convertView.getTag();
        if (vb == null) {
            vb = new ViewBag();
            vb.titleView = convertView.findViewById(R.id.title);
            vb.urlView = convertView.findViewById(R.id.url);
            vb.starView = convertView.findViewById(R.id.star);

            convertView.setTag(vb);
        }

        vb.titleView.setText(item.getTitleOrDefault());
        vb.urlView.setText(item.buildUrl());

        boolean isInFavorites = mFavoritesDataSource.hasFavorites(item.getWebsite(), item.getBoard(), item.getThread());
        vb.starView.setOnCheckedChangeListener(null);
        vb.starView.setChecked(isInFavorites);

        vb.starView.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                HistoryAdapter.this.mFavoritesDataSource.addToFavorites(item.getWebsite(), item.getBoard(), item.getThread(), item.getTitle());
            } else {
                HistoryAdapter.this.mFavoritesDataSource.removeFromFavorites(item.getWebsite(), item.getBoard(), item.getThread());
            }
        });

        return convertView;
    }

    public void setItems(List<HistoryEntity> items) {
        mOriginalItems = items;
        refreshVisibleItems();
    }

    public void searchItems(String query) {
        query = !StringUtils.isEmptyOrWhiteSpace(query) ? query.toLowerCase() : null;
        if (StringUtils.areEqual(mSearchQuery, query)) {
            return;
        }

        mSearchQuery = query;
        refreshVisibleItems();
    }

    private void refreshVisibleItems() {
        if (mOriginalItems == null) {
            return;
        }

        clear();
        for (HistoryEntity item : mOriginalItems) {
            if (mSearchQuery == null
                    || item.getTitleOrDefault().toLowerCase().contains(mSearchQuery)
                    || item.getBoard().contains(mSearchQuery)
                    || mSearchQuery.contains("/") && item.buildUrl().contains(mSearchQuery)) {
                add(item);
            }
        }
    }

    private static class ViewBag {
        TextView titleView;
        TextView urlView;
        CheckBox starView;
    }
}
