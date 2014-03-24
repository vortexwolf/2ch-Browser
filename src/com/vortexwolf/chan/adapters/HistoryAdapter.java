package com.vortexwolf.chan.adapters;

import java.util.List;

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
import com.vortexwolf.chan.db.FavoritesDataSource;
import com.vortexwolf.chan.db.HistoryEntity;

public class HistoryAdapter extends ArrayAdapter<HistoryEntity> {

    private final LayoutInflater mInflater;
    private final FavoritesDataSource mFavoritesDataSource;

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

        vb.titleView.setText(item.getTitle());
        vb.urlView.setText(item.getUrl());

        boolean isInFavorites = this.mFavoritesDataSource.hasFavorites(item.getUrl());
        vb.starView.setOnCheckedChangeListener(null);
        vb.starView.setChecked(isInFavorites);

        vb.starView.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    HistoryAdapter.this.mFavoritesDataSource.addToFavorites(item.getTitle(), item.getUrl());
                } else {
                    HistoryAdapter.this.mFavoritesDataSource.removeFromFavorites(item.getUrl());
                }
            }
        });

        return view;
    }

    static class ViewBag {
        TextView titleView;
        TextView urlView;
        CheckBox starView;
    }
}
