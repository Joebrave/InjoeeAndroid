package com.injoee.ui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.appflood.AppFlood.*;
import com.appflood.AFBannerView;
import com.appflood.AppFlood;
import com.appflood.AppFlood.AFEventDelegate;
import com.appflood.AppFlood.AFRequestDelegate;

import org.json.JSONException;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.DialogPreference;
import android.text.format.Time;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.injoee.R;
import com.injoee.imageloader.ImageLoader;
import com.injoee.model.GameInfo;
import com.injoee.ui.widget.LazyListView;
import com.injoee.ui.widget.LazyListView.LazyListViewListener;
import com.injoee.util.FeaturedGamesListProvider;
import com.injoee.util.GameDetailProvider;
import com.injoee.util.SavedSharePreferences;
import com.injoee.util.Utility;
import com.injoee.webservice.GameListRequester;
import com.umeng.analytics.MobclickAgent;
import com.umeng.fb.FeedbackAgent;
import com.umeng.message.PushAgent;
import com.umeng.update.UmengUpdateAgent;

public class MainActivity extends Activity implements LazyListViewListener {
	// Constant of the parameter
	private final static int LOAD = 0;
	private final static int REFRESH = 1;
	private final static int LOAD_MORE = 2;
	private final static int EACH_TIME_NUM = 10; // each time pull from the server;
	
	protected LazyListView mGameListView;
	protected LinearLayout mNetworkProblem;
	protected Button mBtnReconnectNetwork;
	private ActionBar mActionBar;
	private ProgressBar mProgressBar;
	private Utility mSaveOrLoadImageUtility;

	private SavedSharePreferences mSharePreferences;
	
	private List<GameInfo> mGameList = new ArrayList<GameInfo>();
	private int mTotal; // count the total number of the featured game
	
	private LoaderAdapter mAdapter;
	private MyContentObserver mContentObserver;
	
	private Time mTime = new Time();
	private int mActionTag = -1;
	
	/**
	 * Called when there's a change to the downloads database.
	 */
	void handleDownloadsChanged() {
		this.mAdapter.notifyDataSetChanged();
	}
	
	private class MyContentObserver extends ContentObserver {
		public MyContentObserver() {
			super(new Handler());
		}

		@Override
		public void onChange(boolean selfChange) {
			handleDownloadsChanged();
		}
	}

	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_list);

		mActionBar = getActionBar();
		mActionBar.setIcon(R.drawable.actionbar_icon);

		mSharePreferences = SavedSharePreferences.getInstance(this);

		mTotal = mSharePreferences.getGameListTotal();
		
		mGameListView = (LazyListView) findViewById(R.id.lv_Games);
		mGameListView.setPullLoadEnable(true);
		mGameListView.setLazyListViewListener(this);
		mAdapter = new LoaderAdapter(MainActivity.this);

		mContentObserver = new MyContentObserver();
		
		mNetworkProblem = (LinearLayout) findViewById(R.id.ll_network_problem_panel);
		mBtnReconnectNetwork = (Button) findViewById(R.id.btn_reconnect_internet);

		mGameListView.setAdapter(mAdapter);
		mGameListView.setOnItemClickListener(mListItemClickListener);
		mBtnReconnectNetwork.setOnClickListener(mNetworkReconnectClickListner);
		
		loadList();  // load list from internet or local database 
		
		//Umeng functionality added;
		UmengUpdateAgent.silentUpdate(this);
		PushAgent mPushAgent = PushAgent.getInstance(this);
		mPushAgent.enable();
		PushAgent.getInstance(this).onAppStart();
		
		mSaveOrLoadImageUtility = new Utility();
		
		AppFlood.initialize(this, "BoWFT1pq4XMeuLys", "bPGpGAEG32f9L53036dd1", AppFlood.AD_ALL);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);

		// menu.add(Menu.NONE, 1, 1,
		// getResources().getText(R.string.menu_feedback));

		return true;
	}
	// menu items on the actionbar selected 
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		
		super.onOptionsItemSelected(item);
		
		switch(item.getItemId())
		{
		case R.id.action_feedback:  // feedback to Injoee
			FeedbackAgent agent = new FeedbackAgent(this);
			agent.startFeedbackActivity();
			break;
		}
		
		return true;
	}

	private class FetchGamesTask extends
			AsyncTask<Integer, Integer, List<GameInfo>> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			mProgressBar = (ProgressBar) findViewById(R.id.pb_featured_game_progress);
			mProgressBar.setVisibility(View.VISIBLE);
		}

		@Override
		protected List<GameInfo> doInBackground(Integer... params) {
			GameListRequester gameDB = new GameListRequester();
			int start = 0;
			int count = EACH_TIME_NUM;
			
			if (params[0] == LOAD || params[0] == REFRESH) {
				start = 0;
				mActionTag = LOAD;
			} else if (params[0] == LOAD_MORE) {
				start = mGameList.size();
				mActionTag = LOAD_MORE;
			}
			
			List<GameInfo> retList = null;
			try {
				retList = gameDB.doRequest(start, count);
				if(retList != null) {
					Log.e("return size is!", String.valueOf(retList.size()));
				}
				mTotal = gameDB.total;				
			} catch (JSONException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			if(retList != null) {
				if(mActionTag == LOAD) {
					clearLocalData();
				}
				insertToLocalData(retList);
			}
			return retList;
		}

		@Override
		protected void onPostExecute(List<GameInfo> result) {
			super.onPostExecute(result);

			if (result != null) {
				if (mActionTag == LOAD) {  // when load or refresh, just clear the result and update the data
					mGameList = result;
				} else if(mActionTag == LOAD_MORE) {
					mGameList.addAll(result);
				}
				
				mAdapter.setFeaturedGames(mGameList);
				mAdapter.notifyDataSetInvalidated();
			
				mNetworkProblem.setVisibility(View.GONE);
				mProgressBar.setVisibility(View.GONE);
				mGameListView.setPullLoadEnable(true);

				if (mTotal == mGameList.size()) {// if reach the end of the list disable the footer
					mGameListView.setPullLoadEnable(false);
					
					AppFlood.showInterstitial(MainActivity.this);  //ad show for more game
				}
				
				mSharePreferences.setGameListTotal(mTotal);
			} else {
				mNetworkProblem.setVisibility(View.VISIBLE);
				mProgressBar.setVisibility(View.GONE);
			}
			
			mIsFetching = false;
		}
	};

	OnItemClickListener mListItemClickListener = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> arg0, View featured_Game_Item,
				int position, long arg3) {

			String game_ID = null;

			TextView tv_Game_ID = (TextView) featured_Game_Item
					.findViewById(R.id.tv_game_id);

			game_ID = (String) tv_Game_ID.getText();

			Intent intent = new Intent(MainActivity.this, GameDetail.class);
			intent.putExtra("game_id", game_ID);

			startActivity(intent);

		}

	};

	OnClickListener mNetworkReconnectClickListner = new OnClickListener() {

		@Override
		public void onClick(View v) {
			new FetchGamesTask().execute(REFRESH);
		}
	};

	private boolean mIsFetching = false;
	private void fetchGames(int loadType) {
		if(mIsFetching) return;
		
		FetchGamesTask fetchTask = new FetchGamesTask();
		fetchTask.execute(loadType);
		
		mIsFetching = true;
	}
	
	@Override
	public void onRefresh() {
		fetchGames(REFRESH);
		onLoad();

	}

	@Override
	public void onLoadMore() {
		fetchGames(LOAD_MORE);
		onLoad();
	}

	private void onLoad() {
		mTime.setToNow();
		int hour = mTime.hour;
		int minute = mTime.minute;
		String time = hour + ":" + minute;
		mGameListView.stopRefresh();
		mGameListView.stopLoadMore();
		mGameListView.setRefreshTime(time);
	}

	@Override
	protected void onResume() {
		super.onResume();
		this.mAdapter.registerObserver(mContentObserver);
		this.mAdapter.notifyDataSetChanged();
		MobclickAgent.onResume(this);
	}

	@Override
	protected void onPause() {
		super.onPause();
		this.mAdapter.unregisterObserver(mContentObserver);
		MobclickAgent.onPause(this);
	}
	
	public void loadList()
	{
		if(mSharePreferences.needRefresh()){  //need to load from the internet 
			fetchGames(LOAD);
		} else{
			boolean loaded = loadLocalData();
			
			if(!loaded) {//check if there's data in the contentprovider if not load from internet
				fetchGames(LOAD);
			}
		}
	}

	void insertToLocalData(List<GameInfo> gameList){
		String icon;
		ImageLoader imageHelper = new ImageLoader(this);
		
		for(GameInfo gameInfo : gameList) {
			ContentValues values = new ContentValues();
			//add the value to the contentprovider  by qian
			values.put(FeaturedGamesListProvider.CATEGORY, gameInfo.getGameCategory());
			values.put(FeaturedGamesListProvider.ID, gameInfo.getGameId());
			values.put(FeaturedGamesListProvider.DOWNLOADURL, gameInfo.getGameDownLoadURL());
			values.put(FeaturedGamesListProvider.NAME, gameInfo.getGameName());
			values.put(FeaturedGamesListProvider.PACKAGENAME, gameInfo.getGamePackageName());
			values.put(FeaturedGamesListProvider.PACKAGESIZE, gameInfo.getGamePackageSize());
			values.put(FeaturedGamesListProvider.TYPE, gameInfo.getGameType()); //there's problem with bitmap store method...
			String iconTemp = gameInfo.getGameIcon();
			icon = mSaveOrLoadImageUtility.storeImage(imageHelper.getBitmap(iconTemp), iconTemp, gameInfo.getGameId());
			values.put(FeaturedGamesListProvider.ICON, icon);
			getContentResolver().insert(FeaturedGamesListProvider.CONTENT_URI, values);
		}
	}
	
	public boolean loadLocalData()
	{	
		Cursor c = getContentResolver().query(FeaturedGamesListProvider.CONTENT_URI, null, null, null, null);
		if( c == null)return false; 
		
		if(!c.moveToFirst()) {
			c.close();
			return false;
		} else {
			do {
				GameInfo gameInfo = new GameInfo();
				gameInfo.gameIcon = c.getString(c.getColumnIndex(FeaturedGamesListProvider.ICON));
				gameInfo.gameName = c.getString(c.getColumnIndex(FeaturedGamesListProvider.NAME));
				gameInfo.gameCategory = c.getString(c.getColumnIndex(FeaturedGamesListProvider.CATEGORY));
				gameInfo.gamePackageName = c.getString(c.getColumnIndex(FeaturedGamesListProvider.PACKAGENAME));
				gameInfo.gamePackageSize = c.getString(c.getColumnIndex(FeaturedGamesListProvider.PACKAGESIZE));
				gameInfo.gameType = c.getString(c.getColumnIndex(FeaturedGamesListProvider.TYPE));
				gameInfo.gameDownLoadURL = c.getString(c.getColumnIndex(FeaturedGamesListProvider.DOWNLOADURL));
				gameInfo.gameId = c.getString(c.getColumnIndex(FeaturedGamesListProvider.ID));
				
				mGameList.add(gameInfo);
			} while(c.moveToNext());
			
			c.close();
			
			if(mGameList != null && mTotal == mGameList.size()) {
				mGameListView.setPullLoadEnable(false);
			}	
			mAdapter.setFeaturedGames(mGameList);
			mAdapter.notifyDataSetChanged();
			
			return true;
		}
	}
	
	public void clearLocalData()
	{
		getContentResolver().delete(FeaturedGamesListProvider.CONTENT_URI, null, null);
	}
	
}
