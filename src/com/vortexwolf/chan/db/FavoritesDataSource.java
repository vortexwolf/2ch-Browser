package com.vortexwolf.chan.db;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import com.vortexwolf.chan.common.library.MyLog;
import com.vortexwolf.chan.common.utils.StringUtils;

public class FavoritesDataSource {
    private static final String TABLE = DvachSqlHelper.TABLE_FAVORITES;
    private static final String[] ALL_COLUMNS = {
        DvachSqlHelper.COLUMN_ID, DvachSqlHelper.COLUMN_WEBSITE,
        DvachSqlHelper.COLUMN_BOARD_NAME, DvachSqlHelper.COLUMN_THREAD_NUMBER,
        DvachSqlHelper.COLUMN_TITLE
    };

    private final DvachSqlHelper mDbHelper;

    private SQLiteDatabase mDatabase;
    private boolean mModified = false;

    public FavoritesDataSource(DvachSqlHelper dbHelper) {
        this.mDbHelper = dbHelper;
    }

    public void open() throws SQLException {
        this.mDatabase = this.mDbHelper.getWritableDatabase();
    }

    public void close() {
        this.mDbHelper.close();
    }

    public boolean isModified() {
        return this.mModified;
    }

    public void resetModifiedState() {
        this.mModified = false;
    }

    public void addToFavorites(String website, String board, String thread, String title) {
        if (!this.hasFavorites(website, board, thread)) {
            ContentValues values = new ContentValues();
            values.put(DvachSqlHelper.COLUMN_WEBSITE, website);
            values.put(DvachSqlHelper.COLUMN_BOARD_NAME, board);
            values.put(DvachSqlHelper.COLUMN_THREAD_NUMBER, StringUtils.emptyIfNull(thread));
            values.put(DvachSqlHelper.COLUMN_TITLE, title);

            try {
                this.mDatabase.insertOrThrow(TABLE, null, values);
                this.mModified = true;
            } catch (Exception e) {
                MyLog.e("FavoritesDataSource", e);
            }
        }
    }

    public void removeFromFavorites(String website, String board, String thread) {
        this.mDatabase.delete(TABLE,
                DvachSqlHelper.COLUMN_WEBSITE + " = ?" +
                " and " + DvachSqlHelper.COLUMN_BOARD_NAME + " = ?" +
                " and " + DvachSqlHelper.COLUMN_THREAD_NUMBER + " = ?",
                new String[] { website, board, StringUtils.emptyIfNull(thread) });
        this.mModified = true;
    }

    public List<FavoritesEntity> getAllFavorites() {
        List<FavoritesEntity> favorites = new ArrayList<FavoritesEntity>();

        Cursor cursor = this.mDatabase.query(TABLE, ALL_COLUMNS, null, null, null, null, DvachSqlHelper.COLUMN_ID + " desc");
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            FavoritesEntity he = this.createFavoritesEntity(cursor);
            favorites.add(he);

            cursor.moveToNext();
        }
        cursor.close();

        return favorites;
    }

    public List<FavoritesEntity> getFavoriteThreads() {
        List<FavoritesEntity> favorites = this.getAllFavorites();
        List<FavoritesEntity> threadFavorites = new ArrayList<FavoritesEntity>();

        for (FavoritesEntity f : favorites) {
            if (!StringUtils.isEmpty(f.getThread())) {
                threadFavorites.add(f);
            }
        }

        return threadFavorites;
    }

    public List<FavoritesEntity> getFavoriteBoards() {
        List<FavoritesEntity> favorites = this.getAllFavorites();
        List<FavoritesEntity> boardFavorites = new ArrayList<FavoritesEntity>();

        for (FavoritesEntity f : favorites) {
            if (StringUtils.isEmpty(f.getThread())) {
                boardFavorites.add(f);
            }
        }

        return boardFavorites;
    }

    public boolean hasFavorites(String website, String board, String thread) {
        Cursor cursor = this.mDatabase.rawQuery(
                "select count(*) from " + TABLE +
                " where " + DvachSqlHelper.COLUMN_WEBSITE + " = ?" +
                " and " + DvachSqlHelper.COLUMN_BOARD_NAME + " = ?" +
                " and " + DvachSqlHelper.COLUMN_THREAD_NUMBER + " = ?",
                new String[] { website, board, StringUtils.emptyIfNull(thread) });

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
        return this.createFavoritesEntity(id, website, board, thread, title);
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
