package com.vortexwolf.dvach.db;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

public class FavoritesDataSource {
	private static final String TABLE = DvachSqlHelper.TABLE_FAVORITES;
	private static final String[] ALL_COLUMNS = { DvachSqlHelper.COLUMN_ID, DvachSqlHelper.COLUMN_TITLE, DvachSqlHelper.COLUMN_URL };
	
	private final DvachSqlHelper mDbHelper;
	
	private SQLiteDatabase mDatabase;
	
	public FavoritesDataSource(DvachSqlHelper dbHelper) {
		mDbHelper = dbHelper;
	}

	public void open() throws SQLException {
		mDatabase = mDbHelper.getWritableDatabase();
	}

	public void close() {
		mDbHelper.close();
	}
	
	public void addToFavorites(String title, String url) {
		if(!this.hasFavorites(url)){
			ContentValues values = new ContentValues();
			values.put(DvachSqlHelper.COLUMN_TITLE, title);
			values.put(DvachSqlHelper.COLUMN_URL, url);
			
			long insertId = mDatabase.insert(TABLE, null, values);
		}
	}
	
	public void removeFromFavorites(String url){
		mDatabase.delete(TABLE, DvachSqlHelper.COLUMN_URL + " = ?", new String[] { url });
	}
	
	public List<FavoritesEntity> getAllFavorites() {
		List<FavoritesEntity> favorites = new ArrayList<FavoritesEntity>();

		Cursor cursor = mDatabase.query(TABLE, ALL_COLUMNS, null, null, null, null, DvachSqlHelper.COLUMN_ID + " desc");
		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			FavoritesEntity he = createFavoritesEntity(cursor);
			favorites.add(he);
			
			cursor.moveToNext();
		}
		cursor.close();
		
		return favorites;
	}
	
	public boolean hasFavorites(String url){
		Cursor cursor = mDatabase.rawQuery(
				"select count(*) from " + TABLE + " where " + DvachSqlHelper.COLUMN_URL + " = ?",
				new String[] { url });
		cursor.moveToFirst();
		
		if(cursor.getLong(0) > 0){
			return true;
		}
		
		return false;
	}
	
	
	private FavoritesEntity createFavoritesEntity(Cursor c){
		return createFavoritesEntity(c.getLong(0), c.getString(1), c.getString(2));
	}
	
	private FavoritesEntity createFavoritesEntity(long id, String title, String url){
		FavoritesEntity result = new FavoritesEntity();
		result.setId(id);
		result.setTitle(title);
		result.setUrl(url);
		
		return result;
	}
}
