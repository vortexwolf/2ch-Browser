package com.vortexwolf.dvach.adapters;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.vortexwolf.dvach.R;
import com.vortexwolf.dvach.models.presentation.BoardEntity;
import com.vortexwolf.dvach.models.presentation.IBoardListEntity;
import com.vortexwolf.dvach.models.presentation.SectionEntity;

public class BoardsListAdapter extends ArrayAdapter<IBoardListEntity> {
    private static final int ITEM_VIEW_TYPE_BOARD = 0;
    private static final int ITEM_VIEW_TYPE_SEPARATOR = 1;
    
    private static final int FAVORITES_SECTION_POSITION = 0;

    private final LayoutInflater mInflater;
    
    private boolean mHasFavoritesSection = false;
    private int mFavoritesCount = 0;

    public BoardsListAdapter(Context context, ArrayList<IBoardListEntity> items) {
    	super(context, -1, items);
        this.mInflater = LayoutInflater.from(context);
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

        final IBoardListEntity item = this.getItem(position);

        if (convertView == null) {
            convertView = this.mInflater.inflate(item.isSection()
                    ? com.vortexwolf.dvach.R.layout.pick_board_section
                    : android.R.layout.simple_list_item_1, null);
        }

        if (item.isSection()) {
            SectionEntity si = (SectionEntity) item;

            final TextView sectionView = (TextView) convertView;
            sectionView.setText(si.getTitle());
        } else {
            BoardEntity bi = (BoardEntity) item;
            final TextView text = (TextView) convertView.findViewById(android.R.id.text1);
            text.setText(bi.getTitle());
        }

        return convertView;
    }
    
    public void addItemToFavoritesSection(String boardName){
    	if(!this.mHasFavoritesSection) {
    		this.insert(new SectionEntity(this.getContext().getString(R.string.favorites)), FAVORITES_SECTION_POSITION);
    		this.mHasFavoritesSection = true;
    	}
    	
    	this.mFavoritesCount++;
    	this.insert(new BoardEntity(boardName, boardName), this.mFavoritesCount);
    }
    
    public void removeItemFromFavoritesSection(BoardEntity item){
    	this.mFavoritesCount = Math.max(0, this.mFavoritesCount - 1);
    	this.remove(item);
    	
    	if(this.mFavoritesCount == 0 && this.mHasFavoritesSection) {
    		this.remove(this.getItem(FAVORITES_SECTION_POSITION));
    		this.mHasFavoritesSection = false;
    	}
    	
    	this.notifyDataSetChanged();
    }
}
