package com.injoee.ui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.text.format.Time;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.injoee.R;
import com.injoee.model.GameInfo;
import com.injoee.ui.widget.LazyListView;
import com.injoee.ui.widget.LazyListView.LazyListViewListener;
import com.injoee.webservice.GameListRequester;

public class MainActivity extends Activity implements LazyListViewListener {

	protected LazyListView game_ListView;

	protected LinearLayout llNetworkProblem;
	protected Button btnReconnectNetwork;
	private LoaderAdapter mAdapter;
	private ActionBar mActionBar;
	private int mTotal; // count the total number of the featured game
	private int mCurrentLoadedGamesNum; // to count the current games number in
										// the list;
	private ProgressBar mProgressBar;

	// Constant of the parameter
	private final int LOAD = 0;
	private final int REFRESH = 1;
	private final int LOAD_MORE = 2;
	private final int EACH_TIME_NUM = 2; // each time pull from the server;
	private static int mActionTag = -1;

	private Time mTime = new Time();

	private List<GameInfo> gameList = new ArrayList<GameInfo>();

	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {

		mActionBar = getActionBar();
		mActionBar.setIcon(R.drawable.actionbar_icon);

		super.onCreate(savedInstanceState);

		setContentView(R.layout.main_list);
		new FetchGamesTask().execute(LOAD);

		game_ListView = (LazyListView) findViewById(R.id.lv_Games);
		game_ListView.setPullLoadEnable(true);
		game_ListView.setLazyListViewListener(this);
		mAdapter = new LoaderAdapter(MainActivity.this);
		llNetworkProblem = (LinearLayout) findViewById(R.id.ll_network_problem_panel);
		btnReconnectNetwork = (Button) findViewById(R.id.btn_reconnect_internet);

		game_ListView.setAdapter(mAdapter);
		game_ListView.setOnItemClickListener(listItemClickListener);

		btnReconnectNetwork.setOnClickListener(networkReconnectClickListner);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);

		// menu.add(Menu.NONE, 1, 1,
		// getResources().getText(R.string.menu_feedback));

		return true;
	}

	private class FetchGamesTask extends
			AsyncTask<Integer, Integer, List<GameInfo>> {

		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub

			super.onPreExecute();
			mProgressBar = (ProgressBar) findViewById(R.id.pb_featured_game_progress);
			mProgressBar.setVisibility(View.VISIBLE);
		}

		@Override
		protected List<GameInfo> doInBackground(Integer... params) {
			GameListRequester gameDB = new GameListRequester();

			if (params[0] == LOAD || params[0] == REFRESH) {

				try {
					gameList = gameDB.doRequest(0, EACH_TIME_NUM);

					Log.e("return size is!", String.valueOf(gameList.size()));

					mTotal = gameDB.total;

					mActionTag = LOAD;

				} catch (JSONException e) {

					e.printStackTrace();

					return null;

				} catch (IOException e) {

					e.printStackTrace();
					return null;

				}

				return gameList;

			} else if (params[0] == LOAD_MORE) {

				List<GameInfo> newGameList = new ArrayList<GameInfo>();

				try {
					// add one by one

					mActionTag = LOAD_MORE;

					newGameList = gameDB.doRequest(mCurrentLoadedGamesNum,
							EACH_TIME_NUM);

					mTotal = gameDB.total;

				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					return null;
				} catch (IOException e) {
					e.printStackTrace();
					return null;
				}

				return newGameList;

			} else
				return null;

		}

		@Override
		protected void onPostExecute(List<GameInfo> result) {
			super.onPostExecute(result);

			if (result != null) {

				if (mActionTag == LOAD) {  // when load or refresh, just clear the result and update the data
					
					mAdapter.setFeaturedGames(result);
					mAdapter.notifyDataSetInvalidated();
					mCurrentLoadedGamesNum = result.size();
					
				}
				else if(mActionTag == LOAD_MORE)
				{
					for (GameInfo gameInfo : result) {
						gameList.add(gameInfo);
					}
					
					mAdapter.setFeaturedGames(gameList);
					mAdapter.notifyDataSetInvalidated();
					mCurrentLoadedGamesNum = gameList.size();
				}
				
				llNetworkProblem.setVisibility(View.GONE);
				mProgressBar.setVisibility(View.GONE);
				game_ListView.setPullLoadEnable(true);

				if (mTotal == mCurrentLoadedGamesNum) // if reach the end of the list disable the footer
				{
					game_ListView.setPullLoadEnable(false);  
				}

			} else {
				llNetworkProblem.setVisibility(View.VISIBLE);
				mProgressBar.setVisibility(View.GONE);
			}

		}

		@Override
		protected void onProgressUpdate(Integer... values) {
			// TODO Auto-generated method stub
			super.onProgressUpdate(values);
		}

	};

	OnItemClickListener listItemClickListener = new OnItemClickListener() {

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

	OnClickListener networkReconnectClickListner = new OnClickListener() {

		@Override
		public void onClick(View v) {

			new FetchGamesTask().execute();

		}
	};

	@Override
	public void onRefresh() {

		new FetchGamesTask().execute(REFRESH);

		onLoad();

	}

	@Override
	public void onLoadMore() {

		new FetchGamesTask().execute(LOAD_MORE);

		onLoad();
	}

	private void onLoad() {
		mTime.setToNow();
		int hour = mTime.hour;
		int minute = mTime.minute;
		String time = hour + ":" + minute;
		game_ListView.stopRefresh();
		game_ListView.stopLoadMore();
		game_ListView.setRefreshTime(time);
	}
}
