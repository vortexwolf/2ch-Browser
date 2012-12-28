package com.vortexwolf.dvach.db;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

public class HistoryDataSource {
    private static final String TABLE = DvachSqlHelper.TABLE_HISTORY;
    private static final String[] ALL_COLUMNS = { DvachSqlHelper.COLUMN_ID,
            DvachSqlHelper.COLUMN_TITLE, DvachSqlHelper.COLUMN_URL,
            DvachSqlHelper.COLUMN_CREATED };

    private final DvachSqlHelper mDbHelper;

    private SQLiteDatabase mDatabase;

    public HistoryDataSource(DvachSqlHelper dbHelper) {
        this.mDbHelper = dbHelper;
    }

    public void open() throws SQLException {
        mDatabase = mDbHelper.getWritableDatabase();
    }

    public void close() {
        mDbHelper.close();
    }

    public void addHistory(String title, String url) {
        long currentTime = new Date().getTime();

        // добавляю только если не было такой же ссылки последние 24 часа
        if (!this.hasHistoryLastDay(url, currentTime)) {
            ContentValues values = new ContentValues();
            values.put(DvachSqlHelper.COLUMN_TITLE, title);
            values.put(DvachSqlHelper.COLUMN_URL, url);
            values.put(DvachSqlHelper.COLUMN_CREATED, currentTime);

            long insertId = mDatabase.insert(TABLE, null, values);
        }
    }

    public List<HistoryEntity> getAllHistory() {
        List<HistoryEntity> history = new ArrayList<HistoryEntity>();

        Cursor cursor = mDatabase.query(TABLE, ALL_COLUMNS, null, null, null, null, DvachSqlHelper.COLUMN_CREATED
                + " desc", "200");
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            HistoryEntity he = createHistoryEntity(cursor);
            history.add(he);

            cursor.moveToNext();
        }
        cursor.close();

        return history;
    }

    public void deleteAllHistory() {
        mDatabase.delete(TABLE, null, null);
    }

    private boolean hasHistoryLastDay(String url, long currentTime) {
        long dayAgo = currentTime - 24 * 3600 * 1000;

        Cursor cursor = mDatabase.rawQuery("select count(*) from " + TABLE
                + " where " + DvachSqlHelper.COLUMN_URL + " = ? and "
                + DvachSqlHelper.COLUMN_CREATED + " > " + dayAgo, new String[] { url });

        cursor.moveToFirst();
        long count = cursor.getLong(0);
        cursor.close();

        return count > 0;
    }

    private HistoryEntity createHistoryEntity(Cursor c) {
        return createHistoryEntity(c.getLong(0), c.getString(1), c.getString(2));
    }

    private HistoryEntity createHistoryEntity(long id, String title, String url) {
        HistoryEntity result = new HistoryEntity();
        result.setId(id);
        result.setTitle(title);
        result.setUrl(url);

        return result;
    }
}
