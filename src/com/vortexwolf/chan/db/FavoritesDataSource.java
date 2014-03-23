package com.vortexwolf.chan.db;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

import com.vortexwolf.chan.common.utils.UriUtils;

public class FavoritesDataSource {
    private static final String TABLE = DvachSqlHelper.TABLE_FAVORITES;
    private static final String[] ALL_COLUMNS = { DvachSqlHelper.COLUMN_ID, DvachSqlHelper.COLUMN_TITLE,
            DvachSqlHelper.COLUMN_URL };

    private final DvachSqlHelper mDbHelper;

    private SQLiteDatabase mDatabase;

    public FavoritesDataSource(DvachSqlHelper dbHelper) {
        this.mDbHelper = dbHelper;
    }

    public void open() throws SQLException {
        this.mDatabase = this.mDbHelper.getWritableDatabase();
    }

    public void close() {
        this.mDbHelper.close();
    }

    public void addToFavorites(String title, String url) {
        if (!this.hasFavorites(url)) {
            ContentValues values = new ContentValues();
            values.put(DvachSqlHelper.COLUMN_TITLE, title);
            values.put(DvachSqlHelper.COLUMN_URL, url);

            long insertId = this.mDatabase.insert(TABLE, null, values);
        }
    }

    public void removeFromFavorites(String url) {
        this.mDatabase.delete(TABLE, DvachSqlHelper.COLUMN_URL + " = ?", new String[] { url });
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

    public List<FavoritesEntity> getFavoriteBoards() {
        List<FavoritesEntity> favorites = this.getAllFavorites();
        List<FavoritesEntity> boardFavorites = new ArrayList<FavoritesEntity>();

        for (FavoritesEntity f : favorites) {
            Uri uri = Uri.parse(f.getUrl());
            if (UriUtils.isBoardUri(uri) && UriUtils.getBoardPageNumber(uri) == 0) {
                boardFavorites.add(f);
            }
        }

        return boardFavorites;
    }

    public boolean hasFavorites(String url) {
        Cursor cursor = this.mDatabase.rawQuery("select count(*) from " + TABLE + " where " + DvachSqlHelper.COLUMN_URL + " = ?", new String[] { url });

        cursor.moveToFirst();
        long count = cursor.getLong(0);
        cursor.close();

        return count > 0;
    }

    private FavoritesEntity createFavoritesEntity(Cursor c) {
        return this.createFavoritesEntity(c.getLong(0), c.getString(1), c.getString(2));
    }

    private FavoritesEntity createFavoritesEntity(long id, String title, String url) {
        FavoritesEntity result = new FavoritesEntity();
        result.setId(id);
        result.setTitle(title);
        result.setUrl(url);

        return result;
    }
}
