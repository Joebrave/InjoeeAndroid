package com.injoee.util;

import java.util.HashMap;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

public class FeaturedGamesListProvider extends ContentProvider {
	
	static final String PROVIDER_NAME = "com.injoee.provider.FeaturedGames";
	static final String URL = "content://"+PROVIDER_NAME + "/games";
	public static final Uri CONTENT_URI = Uri.parse(URL);
	
	//fields for the data set into the database
	
	public static final String ID = "id";
	public static final String ICON = "icon";
	public static final String NAME = "name";
	public static final String TYPE = "type";
	public static final String CATEGORY = "category";
	public static final String PACKAGENAME = "packagename";
	public static final String PACKAGESIZE = "packagesize";
	public static final String DOWNLOADURL = "downloadurl";
	
	static final int GAMES = 1;
	static final int GAMES_ID = 2;
	
	DBHelper dbHelper;
	
	static final UriMatcher uriMatcher;
	
	private static HashMap<String, String> GameMap;
	
	static {
		uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		uriMatcher.addURI(PROVIDER_NAME, "games", GAMES);
		uriMatcher.addURI(PROVIDER_NAME, "games/#", GAMES_ID);
	}
	
	private SQLiteDatabase database;
	static final String DATABASE_NAME =  "injoee.db";
	static final String TABLE_NAME = "featuredGames";
	static final int DATABASE_VERSION = 4;
	
	// problem with the blob import 
	static final String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + "(id INTEGER PRIMARY KEY UNIQUE, " + 
	"name TEXT not null, icon TEXT, type TEXT not null, "
	+ "category TEXT not null, packagename TEXT not null, packagesize TEXT not null, downloadurl TEXT not null)";

	
	private static class DBHelper extends SQLiteOpenHelper
	{
		public DBHelper (Context context)
		{
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
			Log.e("DBHelper", "constructure");
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			// TODO Auto-generated method stub
			db.execSQL(CREATE_TABLE);
			Log.e("DBHelper", "onCreate");
			
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			// TODO Auto-generated method stub
			db.execSQL("DROP TABLE IF EXISTS " +  TABLE_NAME);
			onCreate(db);
			Log.e("DBHelper", "onUpgrade");
		}
	}
	
	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		int count = 0;
		
		switch(uriMatcher.match(uri))
		{
		case GAMES:
			count = database.delete(TABLE_NAME, selection, selectionArgs);
			break;
		case GAMES_ID:
			String id = uri.getLastPathSegment(); //gets the id;
			count = database.delete(TABLE_NAME, ID +  " = " + id +(!TextUtils.isEmpty(selection) ? " AND (" +
			selection + ')' : ""), selectionArgs);
			break;
		default:
			throw new IllegalArgumentException("Unsupported URI " + uri);
		}
		
		getContext().getContentResolver().notifyChange(uri, null);
		return count;

	}

	@Override
	public String getType(Uri uri) {
		switch(uriMatcher.match(uri))
		{
		case GAMES:
			return "com.injoee.cursor.dir/com.injoee.games";
		case GAMES_ID:
			return "com.injoee.cursor.item/com.injoee.games";
		default:
			throw new IllegalArgumentException("Unsupported URI: " + uri);
		}
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		long row = database.insert(TABLE_NAME, "", values);
		
		if(row > 0)
		{
			Uri newUri = ContentUris.withAppendedId(CONTENT_URI, row);
			getContext().getContentResolver().notifyChange(newUri, null);
			return newUri;
		}
		throw new SQLException("Fail to add a new record into " + uri);
	}

	@Override
	public boolean onCreate() {
		Context context = getContext();
		dbHelper = new DBHelper(context);
		database = dbHelper.getWritableDatabase();
		
		if(database == null)
			return false;
		else
		return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
		
		queryBuilder.setTables(TABLE_NAME);
		
		switch(uriMatcher.match(uri))
		{
		case GAMES:
			queryBuilder.setProjectionMap(GameMap);
			break;
		case GAMES_ID:
			queryBuilder.appendWhere(ID + "=" + uri.getLastPathSegment());
			break;
			default:
				throw new IllegalArgumentException("Unknow URI" + uri);
		}
		
		Cursor cursor = queryBuilder.query(database, projection, selection, selectionArgs, null, null, sortOrder);
		
		cursor.setNotificationUri(getContext().getContentResolver(), uri);
		return cursor;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		int count = 0;
		
		switch (uriMatcher.match(uri))
		{
		case GAMES:
			count = database.update(TABLE_NAME, values, selection, selectionArgs);
			break;
		case GAMES_ID:
			count = database.update(TABLE_NAME, values, ID +
		                     " = " + uri.getLastPathSegment() +
		                     (!TextUtils.isEmpty(selection) ? " AND (" +
		                     selection + ')' : ""), selectionArgs);
			break;
			default:
				throw new IllegalArgumentException("Unsupported URI " + uri );
		}
		          getContext().getContentResolver().notifyChange(uri, null);
		          
		          return count;
		          
	}

}
