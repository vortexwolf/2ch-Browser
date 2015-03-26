package com.vortexwolf.chan.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.vortexwolf.chan.R;
import com.vortexwolf.chan.db.FavoritesDataSource;
import com.vortexwolf.chan.db.FavoritesEntity;

public class FavoritesAdapter extends ArrayAdapter<FavoritesEntity> {
    private final LayoutInflater mInflater;
    private final FavoritesDataSource mFavoritesDataSource;

    public FavoritesAdapter(Context context, FavoritesDataSource favoritesDataSource) {
        super(context, -1);
        this.mInflater = LayoutInflater.from(context);
        this.mFavoritesDataSource = favoritesDataSource;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final FavoritesEntity item = this.getItem(position);

        View view = convertView == null ? this.mInflater.inflate(R.layout.open_tabs_list_item, null) : convertView;

        TextView titleView = (TextView) view.findViewById(R.id.tabs_item_title);
        TextView urlView = (TextView) view.findViewById(R.id.tabs_item_url);
        ImageView deleteButton = (ImageView) view.findViewById(R.id.tabs_item_delete);

        titleView.setText(item.getTitleOrDefault());
        urlView.setText(item.buildUrl());

        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FavoritesAdapter.this.removeItem(item);
            }
        });

        return view;
    }

    public void removeItem(FavoritesEntity item) {
        this.mFavoritesDataSource.removeFromFavorites(item.getWebsite(), item.getBoard(), item.getThread());
        this.mFavoritesDataSource.resetModifiedState();
        this.remove(item);
    }
}
