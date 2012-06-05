package com.vortexwolf.dvach.adapters;

import java.util.List;

import com.vortexwolf.dvach.R;
import com.vortexwolf.dvach.db.FavoritesEntity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;


public class FavoritesAdapter extends ArrayAdapter<FavoritesEntity> {
	private final LayoutInflater mInflater;
	
	public FavoritesAdapter(Context context, List<FavoritesEntity> objects) {
		super(context, -1, objects);
		mInflater = LayoutInflater.from(context);
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
    	final FavoritesEntity item = this.getItem(position);
    	
    	View view = convertView == null ? mInflater.inflate(R.layout.history_list_item, null) : convertView;
    	
    	TextView titleView = (TextView)view.findViewById(R.id.title);
    	TextView urlView = (TextView)view.findViewById(R.id.url);
    	View starView = view.findViewById(R.id.star);
    	
    	titleView.setText(item.getTitle());
    	urlView.setText(item.getUrl());
    	starView.setVisibility(View.GONE);
    	
    	return view;
	}
}
