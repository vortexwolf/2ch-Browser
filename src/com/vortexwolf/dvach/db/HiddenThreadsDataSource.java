package com.vortexwolf.dvach.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

public class HiddenThreadsDataSource {
    private static final String TABLE = DvachSqlHelper.TABLE_HIDDEN_THREADS;
    private static final String[] ALL_COLUMNS = {
            DvachSqlHelper.COLUMN_THREAD_NUMBER,
            DvachSqlHelper.COLUMN_BOARD_NAME };

    private final DvachSqlHelper mDbHelper;

    private SQLiteDatabase mDatabase;

    public HiddenThreadsDataSource(DvachSqlHelper dbHelper) {
        mDbHelper = dbHelper;
    }

    public void open() throws SQLException {
        mDatabase = mDbHelper.getWritableDatabase();
    }

    public void close() {
        mDbHelper.close();
    }

    public void addToHiddenThreads(String boardName, String threadNumber) {
        if (!this.isHidden(boardName, threadNumber)) {
            ContentValues values = new ContentValues();
            values.put(DvachSqlHelper.COLUMN_BOARD_NAME, boardName);
            values.put(DvachSqlHelper.COLUMN_THREAD_NUMBER, threadNumber);
            mDatabase.insert(TABLE, null, values);
        }
    }

    public void removeFromHiddenThreads(String boardName, String threadNumber) {
        mDatabase.delete(TABLE, DvachSqlHelper.COLUMN_BOARD_NAME + " = ? AND "
                + DvachSqlHelper.COLUMN_THREAD_NUMBER + " = ?", new String[] {
                boardName, threadNumber });
    }

    public boolean isHidden(String boardName, String threadNumber) {
        Cursor cursor = mDatabase.rawQuery("select count(*) from " + TABLE
                + " where " + DvachSqlHelper.COLUMN_BOARD_NAME + " = ? AND "
                + DvachSqlHelper.COLUMN_THREAD_NUMBER + " = ?", new String[] {
                boardName, threadNumber });

        cursor.moveToFirst();
        long count = cursor.getLong(0);
        cursor.close();

        return count > 0;
    }
}
