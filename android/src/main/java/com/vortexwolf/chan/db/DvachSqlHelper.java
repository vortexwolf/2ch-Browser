package com.vortexwolf.chan.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.vortexwolf.chan.services.SqlCreateTableScriptBuilder;

public class DvachSqlHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "dvach.db";
    private static final int DATABASE_VERSION = 4;

    public static final String TABLE_HISTORY = "history";
    public static final String TABLE_FAVORITES = "favorites";
    public static final String TABLE_HIDDEN_THREADS = "hiddenThreads";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_TITLE = "title";
    public static final String COLUMN_URL = "url";
    public static final String COLUMN_CREATED = "created";
    public static final String COLUMN_BOARD_NAME = "boardName";
    public static final String COLUMN_THREAD_NUMBER = "threadNumber";

    private static final SqlCreateTableScriptBuilder mHistorySqlBuilder = new SqlCreateTableScriptBuilder(TABLE_HISTORY);
    private static final SqlCreateTableScriptBuilder mFavoritesSqlBuilder = new SqlCreateTableScriptBuilder(TABLE_FAVORITES);
    private static final SqlCreateTableScriptBuilder mHiddenThreadsSqlBuilder = new SqlCreateTableScriptBuilder(TABLE_HIDDEN_THREADS);

    public DvachSqlHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createHistoryTableSql = mHistorySqlBuilder.addPrimaryKey(COLUMN_ID).addColumn(COLUMN_TITLE, "text", false).addColumn(COLUMN_URL, "text", false).addColumn(COLUMN_CREATED, "long", false).toSql();
        db.execSQL(createHistoryTableSql);

        String createFavoritesTableSql = mFavoritesSqlBuilder.addPrimaryKey(COLUMN_ID).addColumn(COLUMN_TITLE, "text", false).addColumn(COLUMN_URL, "text", false).toSql();
        db.execSQL(createFavoritesTableSql);

        this.createHiddenThreadsTable(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 4) {
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_HIDDEN_THREADS);
            this.createHiddenThreadsTable(db);
        }
    }

    public void drop(SQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_HISTORY);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_FAVORITES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_HIDDEN_THREADS);
    }

    private void createHiddenThreadsTable(SQLiteDatabase db) {
        String sql = mHiddenThreadsSqlBuilder.addPrimaryKey(COLUMN_ID).addColumn(COLUMN_BOARD_NAME, "text", false).addColumn(COLUMN_THREAD_NUMBER, "integer", false).toSql();
        db.execSQL(sql);
    }
}
