package com.injoee.util;

import java.text.SimpleDateFormat;

import android.content.Context;
import android.content.SharedPreferences;


public class SavedSharePreferences {
	
	public static final String PREFS = "games";
	public static final String LAST_LOAD_TIME = "last_load_time";
	public static final String DOWNLOAD_TIMES = "down_load_time";
	public static final String GAME_LIST_TOTAL = "game_list_total";
	
	public static final long DAY_MILLIS = 24 * 60 * 60 * 1000L;
	
	private static SavedSharePreferences instance = null;
	private SharedPreferences mPrefs;
	
	private SavedSharePreferences(Context context) {
		this.mPrefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
		if(!this.mPrefs.contains(LAST_LOAD_TIME)) {
			this.mPrefs.edit().putLong(LAST_LOAD_TIME, System.currentTimeMillis()).commit();
		}
	}
	
	public static SavedSharePreferences getInstance(Context context) {
		if(instance == null)
			instance = new SavedSharePreferences(context);
		return instance;
		
	}
	
	public void setGameListTotal(int total) {
		mPrefs.edit().putInt(GAME_LIST_TOTAL, total).commit();	
	}
	
	public int getGameListTotal() {
		return mPrefs.getInt(GAME_LIST_TOTAL, 0);	
	}
	
	public void saveGameLoadTime()
	{	
		long currTime = System.currentTimeMillis();
		mPrefs.edit().putLong(LAST_LOAD_TIME, currTime).commit();	
	}
	
	public boolean needRefresh()   //count if the dif hours between is large than 24, if is true load from the internet
	{
		long currTime = System.currentTimeMillis();
		long lastLoadTime = mPrefs.getLong(LAST_LOAD_TIME, 0);
			
		if(currTime > lastLoadTime + 1 * DAY_MILLIS) {
			return true;
		} else {
			return false;
		}
	}
}
