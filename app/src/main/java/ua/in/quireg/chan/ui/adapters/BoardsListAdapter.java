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
import ua.in.quireg.chan.common.utils.StringUtils;
import ua.in.quireg.chan.models.domain.BoardModel;
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

        if (item.isSection()){
            return ITEM_VIEW_TYPE_SEPARATOR;
        } else {
            return ITEM_VIEW_TYPE_BOARD;
        }
    }

    @Override
    public boolean isEnabled(int position) {
        switch (getItemViewType(position)){
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

        IBoardListEntity item = getItem(position);

        if(item == null){
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

                BoardModel boardModel = (BoardModel) item;

                final TextView boardName = convertView.findViewById(R.id.pick_board_name);
                final TextView boardBumpLimit = convertView.findViewById(R.id.pick_board_bump_limit);

                String description = !StringUtils.isEmpty(boardModel.getName())
                        ? boardModel.getId() + " - " + boardModel.getName()
                        : boardModel.getId();

                String bumpLimit = !StringUtils.isEmpty(boardModel.getBumpLimit())
                        ? boardModel.getBumpLimit()
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

    public void addItemToFavoritesSection(BoardModel boardModel) {
        if (mFavoritesCount == 0) {
            insert(new SectionEntity(getContext().getString(R.string.favorites)), FAVORITES_SECTION_POSITION);
        }
        mFavoritesCount++;

        insert(boardModel, mFavoritesCount);
    }

    public void removeItemFromFavoritesSection(BoardModel boardModel) {
        mFavoritesCount = Math.max(0, mFavoritesCount - 1);
        remove(boardModel);

        if (mFavoritesCount == 0) {
            remove(getItem(FAVORITES_SECTION_POSITION));
        }

        notifyDataSetChanged();
    }

}
