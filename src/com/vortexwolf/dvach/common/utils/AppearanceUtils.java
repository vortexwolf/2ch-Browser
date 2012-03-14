package com.vortexwolf.dvach.common.utils;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

public class AppearanceUtils {

	public static void showToastMessage(Context context, String msg){
		Toast toast = Toast.makeText(context, msg, Toast.LENGTH_LONG);
		toast.show();
	}
	
	public static ListViewPosition getCurrentListPosition(ListView listView){
		int index = 0;
		int top = 0;
		
		if(listView != null){
			index = listView.getFirstVisiblePosition();
			View v = listView.getChildAt(0);
			top = (v == null) ? 0 : v.getTop();
		}
		
		ListViewPosition position = new ListViewPosition(index, top);
		return position;
	}
	
    public static void unbindDrawables(View view) {
        if (view.getBackground() != null) {
            view.getBackground().setCallback(null);
            view.setBackgroundDrawable(null);  
        }
        if(view instanceof ImageView){
            ImageView imageView = (ImageView)view;  
            imageView.setImageDrawable(null);  
        }
        
        if (view instanceof ViewGroup) {
            for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
                unbindDrawables(((ViewGroup) view).getChildAt(i));
            }
        }
    }

		
	public static class ListViewPosition {
		
		public ListViewPosition(int position, int top){
			this.position = position;
			this.top = top;
		}
		
		public int position;
		public int top;
	}
}
