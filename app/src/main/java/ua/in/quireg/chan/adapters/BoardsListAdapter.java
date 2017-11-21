package ua.in.quireg.chan.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import timber.log.Timber;
import ua.in.quireg.chan.R;
import ua.in.quireg.chan.common.library.MyLog;
import ua.in.quireg.chan.common.utils.StringUtils;
import ua.in.quireg.chan.models.domain.BoardModel;
import ua.in.quireg.chan.models.presentation.BoardEntity;
import ua.in.quireg.chan.models.presentation.IBoardListEntity;
import ua.in.quireg.chan.models.presentation.SectionEntity;

public class BoardsListAdapter extends ArrayAdapter<IBoardListEntity> {

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
        IBoardListEntity item = getItem(position);
        if (item != null) {
            return item.isSection() ? ITEM_VIEW_TYPE_SEPARATOR : ITEM_VIEW_TYPE_BOARD;
        }
        return -1;
    }

    @Override
    public boolean isEnabled(int position) {
        IBoardListEntity item = getItem(position);
        return item != null && item.isSection();
    }

    @NonNull
    @Override
    @SuppressLint("InflateParams")
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {

        IBoardListEntity item = getItem(position);

        if(item == null){
            //Stub to return non-null value in case of fire.
            Timber.e("Something went wrong - getItem returned null");
            return new View(getContext());
        }

        switch (getItemViewType(position)) {

            case ITEM_VIEW_TYPE_SEPARATOR:
                if (convertView == null) {
                    convertView = LayoutInflater.from(getContext()).inflate(R.layout.pick_board_section, null);
                }
                SectionEntity si = (SectionEntity) item;

                final TextView sectionView = (TextView) convertView;
                sectionView.setText(si.getTitle());

                break;

            case ITEM_VIEW_TYPE_BOARD:
                if (convertView == null) {
                    convertView = LayoutInflater.from(getContext()).inflate(R.layout.pick_board_board, null);
                }
                BoardEntity bi = (BoardEntity) item;

                final TextView boardName = convertView.findViewById(R.id.pick_board_name);
                final TextView boardBumpLimit = convertView.findViewById(R.id.pick_board_bump_limit);

                String description = !StringUtils.isEmpty(bi.getTitle())
                        ? bi.getCode() + " - " + bi.getTitle()
                        : bi.getCode();

                String bumpLimit = !StringUtils.isEmpty(bi.getBumpLimit())
                        ? bi.getBumpLimit()
                        : "?";

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

    public void addItemToFavoritesSection(String boardName, BoardModel boardModel) {
        if (mFavoritesCount == 0) {
            insert(new SectionEntity(getContext().getString(R.string.favorites)), FAVORITES_SECTION_POSITION);
        }

        mFavoritesCount++;

        BoardEntity newItem;
        if (boardModel != null) {
            newItem = new BoardEntity(
                    boardName,
                    boardModel.getName() != null ? boardModel.getName() : "?",
                    boardModel.getBumpLimit() != null ? boardModel.getBumpLimit() : "?"
            );
        } else {
            newItem = new BoardEntity(boardName, boardName, "?");
        }

        insert(newItem, mFavoritesCount);
    }

    public void removeItemFromFavoritesSection(BoardEntity item) {
        mFavoritesCount = Math.max(0, mFavoritesCount - 1);
        remove(item);

        if (mFavoritesCount == 0) {
            remove(getItem(FAVORITES_SECTION_POSITION));
        }

        notifyDataSetChanged();
    }

}
