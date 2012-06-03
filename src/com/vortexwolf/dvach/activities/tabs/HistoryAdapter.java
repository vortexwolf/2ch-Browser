package com.vortexwolf.dvach.activities.tabs;

import java.util.List;

import com.vortexwolf.dvach.R;
import com.vortexwolf.dvach.db.HistoryEntity;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class HistoryAdapter extends ArrayAdapter<HistoryEntity> {

	private LayoutInflater mInflater;
	
	public HistoryAdapter(Context context, List<HistoryEntity> objects) {
		super(context, -1, objects);
		mInflater = LayoutInflater.from(context);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
    	final HistoryEntity item = this.getItem(position);
    	
    	View view = convertView == null ? mInflater.inflate(R.layout.history_list_item, null) : convertView;
    	
    	TextView titleView = (TextView)view.findViewById(R.id.title);
    	TextView urlView = (TextView)view.findViewById(R.id.url);
    	
    	titleView.setText(item.getTitle());
    	urlView.setText(item.getUrl());
    	
    	return view;
	}
}
