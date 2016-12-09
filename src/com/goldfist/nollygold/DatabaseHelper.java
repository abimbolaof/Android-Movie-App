package com.goldfist.nollygold;

import java.util.ArrayList;
import java.util.HashMap;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.goldfist.nollygold.ServiceAccess.ItemData;

public class DatabaseHelper extends SQLiteOpenHelper {
	// Database Version
	private static final int DATABASE_VERSION = 1;

	// Database Name
	private static final String DATABASE_NAME = "ngdat";

	// Labels table name
	private static final String LIBRARY_TABLE = "library";
	private static final String FAVOURITE_TABLE = "favourite";

	private static final String KEY_HASH = "hash";
	private static final String KEY_ITEMCODE = "code";
	private static final String KEY_TITLE = "title";
	private static final String KEY_VIDEO_PATH = "videopath";
	// private static final String KEY_THUMB_URL = "thumburl";
	private static final String KEY_DOWNLOAD_REF = "downloadref";

	public DatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	// Creating Tables
	@Override
	public void onCreate(SQLiteDatabase db) {

		String CREATE_LIBRARY_TABLE = "CREATE TABLE " + LIBRARY_TABLE + "("
				+ KEY_ITEMCODE + " TEXT, " + KEY_TITLE + " TEXT, "
				+ KEY_DOWNLOAD_REF + " INTEGER PRIMARY KEY, " + KEY_VIDEO_PATH + " TEXT);";
		db.execSQL(CREATE_LIBRARY_TABLE);
		
		String CREATE_FAVOURITE_TABLE = "CREATE TABLE " + FAVOURITE_TABLE + "(" + KEY_HASH
				+ " INTEGER PRIMARY KEY, " + KEY_ITEMCODE + " TEXT, "
				+ KEY_TITLE + " TEXT);";
		db.execSQL(CREATE_FAVOURITE_TABLE);
		
		
	}

	// Upgrading database
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// Drop older table if existed
		// db.execSQL("DROP TABLE IF EXISTS " + TABLE_LABELS);

		// Create tables again
		// onCreate(db);
	}

	public void saveFavourite(int hash, String code, String title) {

		SQLiteDatabase db = this.getWritableDatabase();

		ContentValues values = new ContentValues();
		values.put(KEY_HASH, hash);
		values.put(KEY_ITEMCODE, code);
		values.put(KEY_TITLE, title);

		// Inserting Row
		db.insert(FAVOURITE_TABLE, null, values);
		db.close();
	}

	ItemData[] getFavourites() {

		ItemData[] data = null;

		String selectQuery = "SELECT " + KEY_ITEMCODE + ", " + KEY_TITLE
				+ " FROM " + FAVOURITE_TABLE;

		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.rawQuery(selectQuery, null);

		data = new ItemData[cursor.getCount()];
		// looping through all rows and adding to list
		int i=0;
		if (cursor.moveToFirst()) {
			do {
				data[i] = new ItemData();
				data[i].code = cursor.getString(0);
				data[i].title = cursor.getString(1);
				i++;
			} while (cursor.moveToNext());
		}

		// closing connection
		cursor.close();
		db.close();

		return data;
	}

	public void storeLibraryData(String code, String title, String videoPath,
			long downloadref) {

		SQLiteDatabase db = this.getWritableDatabase();

		ContentValues values = new ContentValues();
		values.put(KEY_ITEMCODE, code);
		values.put(KEY_TITLE, title);
		values.put(KEY_DOWNLOAD_REF, downloadref);
		values.put(KEY_VIDEO_PATH, videoPath);

		// Inserting Row
		db.insert(LIBRARY_TABLE, null, values);
		db.close();
	}

	public ArrayList<HashMap<String, String>> getLibraryData() {

		ArrayList<HashMap<String, String>> data = new ArrayList<HashMap<String, String>>();

		String selectQuery = "SELECT " + KEY_ITEMCODE + ", " + KEY_TITLE + ", "
				+ KEY_DOWNLOAD_REF + ", " + KEY_VIDEO_PATH + " FROM "
				+ LIBRARY_TABLE;

		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.rawQuery(selectQuery, null);

		// looping through all rows and adding to list
		if (cursor.moveToFirst()) {
			do {
				HashMap<String, String> hm = new HashMap<String, String>();
				hm.put(KEY_ITEMCODE, cursor.getString(0));
				hm.put(KEY_TITLE, cursor.getString(1));
				hm.put(KEY_DOWNLOAD_REF, String.valueOf(cursor.getLong(2)));
				hm.put(KEY_VIDEO_PATH, cursor.getString(3));
				data.add(hm);
			} while (cursor.moveToNext());
		}

		// closing connection
		cursor.close();
		db.close();

		return data;
	}

}