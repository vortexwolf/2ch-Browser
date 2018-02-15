package ua.in.quireg.chan.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;
import ua.in.quireg.chan.common.utils.StringUtils;

public class FavoritesDataSource {

    private static final String TABLE = DvachSqlHelper.TABLE_FAVORITES;
    private static final String[] ALL_COLUMNS = {
            DvachSqlHelper.COLUMN_ID, DvachSqlHelper.COLUMN_WEBSITE,
            DvachSqlHelper.COLUMN_BOARD_NAME, DvachSqlHelper.COLUMN_THREAD_NUMBER,
            DvachSqlHelper.COLUMN_TITLE
    };

    private SQLiteDatabase mDatabase;
    private boolean mModified = false;

    public FavoritesDataSource(DvachSqlHelper dbHelper) {
        mDatabase = dbHelper.getWritableDatabase();
    }

    public boolean isModified() {
        return mModified;
    }

    public void resetModifiedState() {
        mModified = false;
    }

    public void addToFavorites(String website, String board, String thread, String title) {

        if (!hasFavorites(website, board, thread)) {
            ContentValues values = new ContentValues();
            values.put(DvachSqlHelper.COLUMN_WEBSITE, website);
            values.put(DvachSqlHelper.COLUMN_BOARD_NAME, board);
            values.put(DvachSqlHelper.COLUMN_THREAD_NUMBER, StringUtils.emptyIfNull(thread));
            values.put(DvachSqlHelper.COLUMN_TITLE, title);

            try {

                mDatabase.insertOrThrow(TABLE, null, values);
                mModified = true;

            } catch (Exception e) {
                Timber.e(e);
            }
        }
    }

    public void removeFromFavorites(String website, String board, String thread) {

        mDatabase.delete(TABLE,
                DvachSqlHelper.COLUMN_WEBSITE + " = ?" +
                        " and " + DvachSqlHelper.COLUMN_BOARD_NAME + " = ?" +
                        " and " + DvachSqlHelper.COLUMN_THREAD_NUMBER + " = ?",
                new String[]{website, board, StringUtils.emptyIfNull(thread)});
        mModified = true;

    }

    private List<FavoritesEntity> getAllFavorites() {

        List<FavoritesEntity> favorites = new ArrayList<>();

        Cursor cursor = mDatabase.query(TABLE, ALL_COLUMNS, null, null, null, null, DvachSqlHelper.COLUMN_ID + " asc");
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            FavoritesEntity he = createFavoritesEntity(cursor);
            favorites.add(he);

            cursor.moveToNext();
        }
        cursor.close();

        return favorites;
    }

    public List<FavoritesEntity> getFavoriteThreads() {
        List<FavoritesEntity> favorites = getAllFavorites();
        List<FavoritesEntity> threadFavorites = new ArrayList<>();

        for (FavoritesEntity f : favorites) {
            if (!StringUtils.isEmpty(f.getThread())) {
                threadFavorites.add(f);
            }
        }

        return threadFavorites;
    }

    public List<FavoritesEntity> getFavoriteBoards() {
        List<FavoritesEntity> favorites = getAllFavorites();
        List<FavoritesEntity> boardFavorites = new ArrayList<>();

        for (FavoritesEntity f : favorites) {
            if (StringUtils.isEmpty(f.getThread())) {
                boardFavorites.add(f);
            }
        }

        return boardFavorites;
    }

    public boolean hasFavorites(String website, String board, String thread) {

        Cursor cursor = mDatabase.rawQuery(
                String.format("SELECT count(*) FROM %s WHERE %s = ? and %s = ? and %s = ?",
                        TABLE,
                        DvachSqlHelper.COLUMN_WEBSITE,
                        DvachSqlHelper.COLUMN_BOARD_NAME,
                        DvachSqlHelper.COLUMN_THREAD_NUMBER),
                new String[]{website, board, StringUtils.emptyIfNull(thread)}
        );

        cursor.moveToFirst();
        long count = cursor.getLong(0);
        cursor.close();

        return count > 0;
    }

    private FavoritesEntity createFavoritesEntity(Cursor c) {
        long id = c.getLong(c.getColumnIndex(DvachSqlHelper.COLUMN_ID));
        String website = c.getString(c.getColumnIndex(DvachSqlHelper.COLUMN_WEBSITE));
        String board = c.getString(c.getColumnIndex(DvachSqlHelper.COLUMN_BOARD_NAME));
        String thread = c.getString(c.getColumnIndex(DvachSqlHelper.COLUMN_THREAD_NUMBER));
        String title = c.getString(c.getColumnIndex(DvachSqlHelper.COLUMN_TITLE));
        return createFavoritesEntity(id, website, board, thread, title);
    }

    private FavoritesEntity createFavoritesEntity(long id, String website, String board, String thread, String title) {
        FavoritesEntity result = new FavoritesEntity();
        result.setId(id);
        result.setWebsite(website);
        result.setBoard(board);
        result.setThread(thread);
        result.setTitle(title);

        return result;
    }
}
