package com.vortexwolf.dvach.db;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

public class HistoryDataSource {
	private final DvachSqlHelper dbHelper;
	private final String[] allColumns = { DvachSqlHelper.COLUMN_ID, DvachSqlHelper.COLUMN_TITLE, DvachSqlHelper.COLUMN_URL, DvachSqlHelper.COLUMN_CREATED };
	private SQLiteDatabase database;
	
	public HistoryDataSource(Context context) {
		dbHelper = new DvachSqlHelper(context);
	}

	public void open() throws SQLException {
		database = dbHelper.getWritableDatabase();
	}

	public void close() {
		dbHelper.close();
	}
	
	public void addHistory(String title, String url) {
		long currentTime = new Date().getTime();
		
		// добавляю только если не было такой же ссылки последние 24 часа
		if(!this.hasHistoryLastDay(url, currentTime)){
			ContentValues values = new ContentValues();
			values.put(DvachSqlHelper.COLUMN_TITLE, title);
			values.put(DvachSqlHelper.COLUMN_URL, url);
			values.put(DvachSqlHelper.COLUMN_CREATED, currentTime);
			
			long insertId = database.insert(DvachSqlHelper.TABLE_HISTORY, null, values);
		}
	}
	
	public List<HistoryEntity> getAllHistory() {
		List<HistoryEntity> history = new ArrayList<HistoryEntity>();

		Cursor cursor = database.query(DvachSqlHelper.TABLE_HISTORY, allColumns, null, null, null, null, DvachSqlHelper.COLUMN_CREATED + " desc", "0,200");
		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			HistoryEntity he = createHistoryEntity(cursor);
			history.add(he);
			
			cursor.moveToNext();
		}
		cursor.close();
		
		return history;
	}
	
	private boolean hasHistoryLastDay(String url, long currentTime){
		long dayAgo = currentTime - 24 * 3600 * 1000;
		
		Cursor cursor = database.rawQuery(
				"select count(*) from " + DvachSqlHelper.TABLE_HISTORY +
				" where " + DvachSqlHelper.COLUMN_URL + " = ? and " + DvachSqlHelper.COLUMN_CREATED + " > " + dayAgo,
				new String[] { url });
		cursor.moveToFirst();
		
		if(cursor.getLong(0) > 0){
			return true;
		}
		
		return false;

	}
	
	private HistoryEntity createHistoryEntity(Cursor c){
		return createHistoryEntity(c.getLong(0), c.getString(1), c.getString(2));
	}
	
	private HistoryEntity createHistoryEntity(long id, String title, String url){
		HistoryEntity result = new HistoryEntity();
		result.setId(id);
		result.setTitle(title);
		result.setUrl(url);
		
		return result;
	}
}
