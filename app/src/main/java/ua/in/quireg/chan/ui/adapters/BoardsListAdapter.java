package ua.in.quireg.chan.ui.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import timber.log.Timber;
import ua.in.quireg.chan.R;
import ua.in.quireg.chan.models.presentation.BoardEntity;
import ua.in.quireg.chan.models.presentation.BoardsListEntity;
import ua.in.quireg.chan.models.presentation.SectionEntity;

public class BoardsListAdapter extends ArrayAdapter<BoardsListEntity> {

    private static final int ITEM_VIEW_TYPE_BOARD = 0;
    private static final int ITEM_VIEW_TYPE_SEPARATOR = 1;

    private static final int FAVORITES_SECTION_POSITION = 0;

    private int mFavoritesCount = 0;

    public BoardsListAdapter(Context context) {
        super(context, -1);
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public int getItemViewType(int position) {

        BoardsListEntity item = getItem(position);

        if (item.isSection()) {
            return ITEM_VIEW_TYPE_SEPARATOR;
        } else {
            return ITEM_VIEW_TYPE_BOARD;
        }
    }

    @Override
    public boolean isEnabled(int position) {
        switch (getItemViewType(position)) {
            case ITEM_VIEW_TYPE_SEPARATOR:
                return false;
            case ITEM_VIEW_TYPE_BOARD:
                return true;

        }
        return false;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {

        BoardsListEntity item = getItem(position);

        if (item == null) {
            Timber.e("Something went wrong - getItem() returned null");
            return new View(getContext());
        }

        switch (getItemViewType(position)) {

            case ITEM_VIEW_TYPE_SEPARATOR:
                if (convertView == null) {
                    convertView = LayoutInflater.from(getContext()).inflate(R.layout.pick_board_section, parent, false);
                }

                SectionEntity si = (SectionEntity) item;

                final TextView sectionView = (TextView) convertView;
                sectionView.setText(si.getTitle());

                break;

            case ITEM_VIEW_TYPE_BOARD:
                if (convertView == null) {
                    convertView = LayoutInflater.from(getContext()).inflate(R.layout.pick_board_board, parent, false);
                }

                BoardEntity boardEntity = (BoardEntity) item;

                final TextView boardName = convertView.findViewById(R.id.pick_board_name);
                final TextView boardBumpLimit = convertView.findViewById(R.id.pick_board_bump_limit);

                String description = String.format("%s - %s", boardEntity.id, boardEntity.boardName);
                String bumpLimit = String.valueOf(boardEntity.bumpLimit);

                boardName.setText(description);
                boardBumpLimit.setText(bumpLimit);

                break;
        }

        return convertView;
    }

    @Override
    public void clear() {
        super.clear();
        mFavoritesCount = 0;
    }

    public void addItemToFavoritesSection(BoardEntity boardEntity) {
        if (mFavoritesCount == 0) {
            insert(new SectionEntity(getContext().getString(R.string.favorites)), FAVORITES_SECTION_POSITION);
        }
        mFavoritesCount++;

        insert(boardEntity, mFavoritesCount);
    }

    public void removeItemFromFavoritesSection(BoardEntity boardEntity) {
        mFavoritesCount = Math.max(0, mFavoritesCount - 1);
        remove(boardEntity);

        if (mFavoritesCount == 0) {
            remove(getItem(FAVORITES_SECTION_POSITION));
        }

        notifyDataSetChanged();
    }

}
