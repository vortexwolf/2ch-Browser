package ua.in.quireg.chan.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import timber.log.Timber;
import ua.in.quireg.chan.common.library.MyLog;

public class HiddenThreadsDataSource {

    private static final String TABLE = DvachSqlHelper.TABLE_HIDDEN_THREADS;

    /*private static final String[] ALL_COLUMNS = {
        DvachSqlHelper.COLUMN_ID, DvachSqlHelper.COLUMN_WEBSITE,
        DvachSqlHelper.COLUMN_BOARD_NAME, DvachSqlHelper.COLUMN_THREAD_NUMBER
    };*/

    private SQLiteDatabase mDatabase;

    public HiddenThreadsDataSource(DvachSqlHelper dbHelper) {
        mDatabase = dbHelper.getWritableDatabase();
    }

    public void addToHiddenThreads(String website, String board, String thread) {
        if (!isHidden(website, board, thread)) {
            ContentValues values = new ContentValues();
            values.put(DvachSqlHelper.COLUMN_WEBSITE, website);
            values.put(DvachSqlHelper.COLUMN_BOARD_NAME, board);
            values.put(DvachSqlHelper.COLUMN_THREAD_NUMBER, thread);
            try {
                mDatabase.insertOrThrow(TABLE, null, values);
            } catch (Exception e) {
                Timber.e("HiddenThreadsDataSource", e);
            }
        }
    }

    public void removeFromHiddenThreads(String website, String board, String thread) {
        mDatabase.delete(TABLE,
            DvachSqlHelper.COLUMN_WEBSITE + " = ?" +
            " and " + DvachSqlHelper.COLUMN_BOARD_NAME + " = ?" +
            " and " + DvachSqlHelper.COLUMN_THREAD_NUMBER + " = ?",
            new String[] { website, board, thread });
    }

    public boolean isHidden(String website, String board, String thread) {
        Cursor cursor = mDatabase.rawQuery("select count(*) from " + TABLE +
                " where " + DvachSqlHelper.COLUMN_WEBSITE + " = ?" +
                " and " + DvachSqlHelper.COLUMN_BOARD_NAME + " = ?" +
                " and " + DvachSqlHelper.COLUMN_THREAD_NUMBER + " = ?",
                new String[] { website, board, thread });

        cursor.moveToFirst();
        long count = cursor.getLong(0);
        cursor.close();

        return count > 0;
    }
}
