package com.martinporto.model;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.content.MutableContextWrapper;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.lifecycle.MutableLiveData;

import com.martinporto.App;

import timber.log.Timber;

public class JdiDatabase extends SQLiteOpenHelper {
	
	private static final String DB_NAME = "samllWebServerDb.sqlite";
	private static final int DB_VERSION = 1;
	private static JdiDatabase ourInstance = new JdiDatabase();
	MutableLiveData<Integer> stats;
	
	public JdiDatabase(Context context) {
		super(App.getContext(), DB_NAME, null, DB_VERSION);
	}
	
	public JdiDatabase() {
		super(App.getContext(), DB_NAME, null, DB_VERSION);
		stats = new MutableLiveData<>(0);
	}
	
	public static JdiDatabase get() {
		return ourInstance;
	}
	
	public MutableLiveData<Integer> getStats() {
		return stats;
	}
	
	@Override
	public void onCreate(SQLiteDatabase db) {
		
		/* logs */
		String query = "CREATE TABLE logs ("
			+ " id INTEGER PRIMARY KEY AUTOINCREMENT, "
			+ " log TEXT)";
		db.execSQL(query);
		
		/* vwStats */
		String vwStats = "CREATE VIEW vwStats AS"
			+ " SELECT count(*) as total"
			+ " FROM logs";
		db.execSQL(vwStats);
	}
	
	public void addNewLog(String log) {
		
		SQLiteDatabase db = this.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put("log", log);
		db.insert("logs", null, values);
		db.close();
		
		updateStats();
	}
	
	public void updateStats() {
		
		SQLiteDatabase db = this.getWritableDatabase();
		Cursor c = db.rawQuery("SELECT * FROM vwStats", null);
		
		Integer total = 0;
		c.moveToFirst();
		while (c.isAfterLast() == false) {
			total = c.getInt(0);
			c.moveToNext();
		}
		
		Timber.d("total %s", total);
		stats.postValue(total);
		
		c.close();
		db.close();
	}
	
	public Integer resetStats() {
		
		//Integer total = getStats();
		Integer total = 0;
		
		SQLiteDatabase db = this.getWritableDatabase();
		Cursor c = db.rawQuery("DELETE FROM logs", null);
		c.moveToFirst();
		c.close();
		db.close();
		
		return total;
		
	}
	
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("DROP TABLE IF EXISTS logs");
		onCreate(db);
	}
}