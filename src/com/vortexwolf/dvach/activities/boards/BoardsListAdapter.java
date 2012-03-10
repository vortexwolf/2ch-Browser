package com.vortexwolf.dvach.activities.boards;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class BoardsListAdapter extends ArrayAdapter<IBoardListEntity>  {
	private static final int ITEM_VIEW_TYPE_BOARD = 0;
	private static final int ITEM_VIEW_TYPE_SEPARATOR = 1;
	
	private LayoutInflater mInflater;
	
	public BoardsListAdapter(Context context, IBoardListEntity[] items) {
		super(context, 0, items);
        mInflater = LayoutInflater.from(context);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}
	
	@Override
	public int getViewTypeCount() {
		return 2;
	}
	
	@Override
	public int getItemViewType(int position) {
		return this.getItem(position).isSection() ? ITEM_VIEW_TYPE_SEPARATOR : ITEM_VIEW_TYPE_BOARD;
	}
	
	@Override
	public boolean isEnabled(int position) {
		return !this.getItem(position).isSection();
	}
	
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        
    	//final IBoardListEntity item = mItems[position];
    	final IBoardListEntity item = this.getItem(position);
    	
        if (convertView == null) {
        	convertView = mInflater.inflate(
        			item.isSection() ? com.vortexwolf.dvach.R.layout.pick_board_section : android.R.layout.simple_list_item_1, 
            		null);
        }

        if(item.isSection()){
        	SectionEntity si = (SectionEntity)item;

			final TextView sectionView = (TextView) convertView;
			sectionView.setText(si.getTitle());
        }
        else{
        	BoardEntity bi = (BoardEntity)item;
	        final TextView text = (TextView) convertView.findViewById(android.R.id.text1);
	        text.setText(bi.getTitle());
        }
        
        return convertView;
    }
}
