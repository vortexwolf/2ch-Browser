package ua.in.quireg.chan.ui.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import timber.log.Timber;
import ua.in.quireg.chan.R;
import ua.in.quireg.chan.db.FavoritesDataSource;
import ua.in.quireg.chan.db.FavoritesEntity;

public class FavoritesAdapter extends ArrayAdapter<FavoritesEntity> {

    private final LayoutInflater mInflater;
    private final FavoritesDataSource mFavoritesDataSource;

    public FavoritesAdapter(Context context, FavoritesDataSource favoritesDataSource) {
        super(context, -1);
        mInflater = LayoutInflater.from(context);
        mFavoritesDataSource = favoritesDataSource;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        View view = convertView == null ? mInflater.inflate(R.layout.favorites_list_item, parent, false) : convertView;

        final FavoritesEntity item = getItem(position);

        if(item == null) {
            Timber.e("FavoritesEntity item == null, position %d", position);
            return view;
        }

        TextView titleView = view.findViewById(R.id.favorites_item_title);
        TextView urlView = view.findViewById(R.id.favorites_item_url);

        titleView.setText(item.getTitleOrDefault());
        urlView.setText(item.buildUrl());

        return view;
    }

    public void removeItem(FavoritesEntity item) {
        mFavoritesDataSource.removeFromFavorites(item.getWebsite(), item.getBoard(), item.getThread());
        remove(item);
    }
}
