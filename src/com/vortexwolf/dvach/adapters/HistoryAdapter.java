package com.vortexwolf.dvach.adapters;

import java.util.List;

import com.vortexwolf.dvach.R;
import com.vortexwolf.dvach.adapters.ThreadsListAdapter.ViewBag;
import com.vortexwolf.dvach.common.controls.EllipsizingTextView;
import com.vortexwolf.dvach.db.FavoritesDataSource;
import com.vortexwolf.dvach.db.HistoryEntity;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;

public class HistoryAdapter extends ArrayAdapter<HistoryEntity> {

    private final LayoutInflater mInflater;
    private final FavoritesDataSource mFavoritesDataSource;

    public HistoryAdapter(Context context, List<HistoryEntity> objects, FavoritesDataSource favoritesDataSource) {
        super(context, -1, objects);
        mInflater = LayoutInflater.from(context);
        mFavoritesDataSource = favoritesDataSource;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final HistoryEntity item = this.getItem(position);

        View view = convertView == null
                ? mInflater.inflate(R.layout.history_list_item, null)
                : convertView;

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

        boolean isInFavorites = mFavoritesDataSource.hasFavorites(item.getUrl());
        vb.starView.setOnCheckedChangeListener(null);
        vb.starView.setChecked(isInFavorites);

        vb.starView.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    mFavoritesDataSource.addToFavorites(item.getTitle(), item.getUrl());
                } else {
                    mFavoritesDataSource.removeFromFavorites(item.getUrl());
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
