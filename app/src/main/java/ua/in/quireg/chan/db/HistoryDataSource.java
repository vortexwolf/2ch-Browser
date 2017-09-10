package ua.in.quireg.chan.db;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import ua.in.quireg.chan.common.library.MyLog;
import ua.in.quireg.chan.common.utils.StringUtils;

public class HistoryDataSource {
    private static final String TABLE = DvachSqlHelper.TABLE_HISTORY;
    private static final String[] ALL_COLUMNS = {
        DvachSqlHelper.COLUMN_ID, DvachSqlHelper.COLUMN_WEBSITE,
        DvachSqlHelper.COLUMN_BOARD_NAME, DvachSqlHelper.COLUMN_THREAD_NUMBER,
        DvachSqlHelper.COLUMN_TITLE, DvachSqlHelper.COLUMN_CREATED
    };
    private static final int QUERY_LIMIT = 200;

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

    public void addHistory(String website, String board, String thread, String title) {
        long currentTime = new Date().getTime();

        // добавляю только если не было такой же ссылки последние 24 часа
        if (!this.hasHistoryLastDay(website, board, thread, currentTime)) {
            ContentValues values = new ContentValues();
            values.put(DvachSqlHelper.COLUMN_WEBSITE, website);
            values.put(DvachSqlHelper.COLUMN_BOARD_NAME, board);
            values.put(DvachSqlHelper.COLUMN_THREAD_NUMBER, StringUtils.emptyIfNull(thread));
            values.put(DvachSqlHelper.COLUMN_TITLE, title);
            values.put(DvachSqlHelper.COLUMN_CREATED, currentTime);

            try {
                this.mDatabase.insertOrThrow(TABLE, null, values);
            } catch (Exception e) {
                MyLog.e("HistoryDataSource", e);
            }
        }
    }

    public void updateHistoryItem(String website, String board, String thread, String title) {
        ContentValues values = new ContentValues();
        values.put(DvachSqlHelper.COLUMN_TITLE, title);

        this.mDatabase.update(TABLE, values,
                DvachSqlHelper.COLUMN_WEBSITE + " = ?" +
                " and " + DvachSqlHelper.COLUMN_BOARD_NAME + " = ?" +
                " and " + DvachSqlHelper.COLUMN_THREAD_NUMBER + " = ?",
                new String[] { website, board, StringUtils.emptyIfNull(thread) });
    }

    public List<HistoryEntity> getAllHistory() {
        List<HistoryEntity> history = new ArrayList<HistoryEntity>();

        Cursor cursor = this.mDatabase.query(TABLE, ALL_COLUMNS, null, null, null, null, DvachSqlHelper.COLUMN_CREATED + " desc", QUERY_LIMIT + "");
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

    private boolean hasHistoryLastDay(String website, String board, String thread, long currentTime) {
        long dayAgo = currentTime - 24 * 3600 * 1000;

        Cursor cursor = this.mDatabase.rawQuery("select count(*) from " + TABLE +
                " where " + DvachSqlHelper.COLUMN_WEBSITE + " = ?" +
                " and " + DvachSqlHelper.COLUMN_BOARD_NAME + " = ?" +
                " and " + DvachSqlHelper.COLUMN_THREAD_NUMBER + " = ?" +
                " and " + DvachSqlHelper.COLUMN_CREATED + " > " + dayAgo,
                new String[] { website, board, StringUtils.emptyIfNull(thread) });

        cursor.moveToFirst();
        long count = cursor.getLong(0);
        cursor.close();

        return count > 0;
    }

    private HistoryEntity createHistoryEntity(Cursor c) {
        long id = c.getLong(c.getColumnIndex(DvachSqlHelper.COLUMN_ID));
        String website = c.getString(c.getColumnIndex(DvachSqlHelper.COLUMN_WEBSITE));
        String board = c.getString(c.getColumnIndex(DvachSqlHelper.COLUMN_BOARD_NAME));
        String thread = c.getString(c.getColumnIndex(DvachSqlHelper.COLUMN_THREAD_NUMBER));
        String title = c.getString(c.getColumnIndex(DvachSqlHelper.COLUMN_TITLE));
        return this.createHistoryEntity(id, website, board, thread, title);
    }

    private HistoryEntity createHistoryEntity(long id, String website, String board, String thread, String title) {
        HistoryEntity result = new HistoryEntity();
        result.setId(id);
        result.setWebsite(website);
        result.setBoard(board);
        result.setThread(thread);
        result.setTitle(title);

        return result;
    }
}
