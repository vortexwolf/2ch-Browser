package com.vortexwolf.chan.db;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.vortexwolf.chan.common.library.MyLog;
import com.vortexwolf.chan.common.utils.StringUtils;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

public class HistoryDataSource {
    private static final String TABLE = DvachSqlHelper.TABLE_HISTORY;
    private static final String[] ALL_COLUMNS = { DvachSqlHelper.COLUMN_ID, DvachSqlHelper.COLUMN_TITLE,
            DvachSqlHelper.COLUMN_URL, DvachSqlHelper.COLUMN_CREATED };

    private final DvachSqlHelper mDbHelper;

    private SQLiteDatabase mDatabase;

    public HistoryDataSource(DvachSqlHelper dbHelper) {
        this.mDbHelper = dbHelper;
    }

    public void open() throws SQLException {
        this.mDatabase = this.mDbHelper.getWritableDatabase();
    }

    public void close() {
        this.mDbHelper.close();
    }

    public void addHistory(String title, String url) {
        long currentTime = new Date().getTime();

        // добавляю только если не было такой же ссылки последние 24 часа
        if (!this.hasHistoryLastDay(url, currentTime)) {
            ContentValues values = new ContentValues();
            values.put(DvachSqlHelper.COLUMN_TITLE, StringUtils.emptyIfNull(title));
            values.put(DvachSqlHelper.COLUMN_URL, url);
            values.put(DvachSqlHelper.COLUMN_CREATED, currentTime);

            try {
                long insertId = this.mDatabase.insertOrThrow(TABLE, null, values);
            } catch (Exception e) {
                MyLog.e("HistoryDataSource", e);
            }
        }
    }

    public List<HistoryEntity> getAllHistory() {
        List<HistoryEntity> history = new ArrayList<HistoryEntity>();

        Cursor cursor = this.mDatabase.query(TABLE, ALL_COLUMNS, null, null, null, null, DvachSqlHelper.COLUMN_CREATED + " desc", "200");
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            HistoryEntity he = this.createHistoryEntity(cursor);
            history.add(he);

            cursor.moveToNext();
        }
        cursor.close();

        return history;
    }

    public void deleteAllHistory() {
        this.mDatabase.delete(TABLE, null, null);
    }

    private boolean hasHistoryLastDay(String url, long currentTime) {
        long dayAgo = currentTime - 24 * 3600 * 1000;

        Cursor cursor = this.mDatabase.rawQuery("select count(*) from " + TABLE + " where " + DvachSqlHelper.COLUMN_URL + " = ? and " + DvachSqlHelper.COLUMN_CREATED + " > " + dayAgo, new String[] { url });

        cursor.moveToFirst();
        long count = cursor.getLong(0);
        cursor.close();

        return count > 0;
    }

    private HistoryEntity createHistoryEntity(Cursor c) {
        return this.createHistoryEntity(c.getLong(0), c.getString(1), c.getString(2));
    }

    private HistoryEntity createHistoryEntity(long id, String title, String url) {
        HistoryEntity result = new HistoryEntity();
        result.setId(id);
        result.setTitle(title);
        result.setUrl(url);

        return result;
    }
}
