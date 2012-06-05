package com.vortexwolf.dvach.adapters;

import java.util.List;

import com.vortexwolf.dvach.R;
import com.vortexwolf.dvach.db.FavoritesDataSource;
import com.vortexwolf.dvach.db.HistoryEntity;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
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
    	
    	View view = convertView == null ? mInflater.inflate(R.layout.history_list_item, null) : convertView;
    	
    	TextView titleView = (TextView)view.findViewById(R.id.title);
    	TextView urlView = (TextView)view.findViewById(R.id.url);
    	CheckBox starView = (CheckBox)view.findViewById(R.id.star);
    	
    	titleView.setText(item.getTitle());
    	urlView.setText(item.getUrl());
    	
    	boolean isInFavorites = mFavoritesDataSource.hasFavorites(item.getUrl());
    	starView.setChecked(isInFavorites);
    	
    	starView.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if(isChecked){
					mFavoritesDataSource.addToFavorites(item.getTitle(), item.getUrl());
				}
				else {
					mFavoritesDataSource.removeFromFavorites(item.getUrl());
				}
			}
		});
    	
    	return view;
	}
}
