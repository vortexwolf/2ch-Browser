package ua.in.quireg.chan.ui.adapters;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import ua.in.quireg.chan.R;
import ua.in.quireg.chan.models.presentation.OpenTabModel;
import ua.in.quireg.chan.services.presentation.OpenTabsManager;

public class OpenTabsAdapter extends ArrayAdapter<OpenTabModel> {
    private final LayoutInflater mInflater;
    private final OpenTabsManager mOpenTabsManager;

    public OpenTabsAdapter(Context context, ArrayList<OpenTabModel> items, OpenTabsManager openTabsManager) {
        super(context.getApplicationContext(), 0, items);
        this.mInflater = LayoutInflater.from(context);
        this.mOpenTabsManager = openTabsManager;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        final OpenTabModel item = this.getItem(position);

        View view = convertView == null ? this.mInflater.inflate(R.layout.open_tabs_list_item, null) : convertView;

        this.fillItemView(view, item);

        return view;
    }

    private void fillItemView(View view, final OpenTabModel item) {
        TextView titleView = (TextView) view.findViewById(R.id.tabs_item_title);
        TextView urlView = (TextView) view.findViewById(R.id.tabs_item_url);
        ImageView deleteButton = (ImageView) view.findViewById(R.id.tabs_item_delete);

        titleView.setText(item.getTitleOrDefault());
        urlView.setText(item.buildUrl());

        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OpenTabsAdapter.this.mOpenTabsManager.remove(item);
                OpenTabsAdapter.this.remove(item);
                // OpenTabsAdapter.this.notifyDataSetChanged();
            }
        });
    }
}
