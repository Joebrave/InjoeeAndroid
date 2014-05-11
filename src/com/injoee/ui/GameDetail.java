package com.injoee.ui;

import java.io.IOException;

import org.json.JSONException;

import com.appflood.AppFlood;
import com.injoee.R;
import com.injoee.func.GameInstaller;
import com.injoee.imageloader.ImageLoader;
import com.injoee.model.GameInfo;
import com.injoee.model.GameInfoDetail;
import com.injoee.model.GameInfoDetail.DownloadStatus;
import com.injoee.providers.DownloadManager;
import com.injoee.providers.DownloadManager.Request;
import com.injoee.providers.downloads.DownloadService;
import com.injoee.util.GameDetailProvider;
import com.injoee.util.SavedSharePreferences;
import com.injoee.util.Utility;
import com.injoee.webservice.GameDetailsRequester;
import com.injoee.webservice.Voter;
import com.umeng.analytics.MobclickAgent;
import com.umeng.message.PushAgent;

import android.net.NetworkInfo.DetailedState;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.SQLException;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class GameDetail extends Activity {
	private GameInfoDetail mGameDetail;
	private GameDetailViewHolder mGameDetailViewHolder;
	private FetchGameTask mFetchGameTask = new FetchGameTask();
	private static boolean byPackageName;
	private Utility mImageSaveorReadUtiltiy;
//	
//	private int mTitleColumnId;
//	private int mStatusColumnId;
//	private int mReasonColumnId;
//	private int mTotalBytesColumnId;
//	private int mCurrentBytesColumnId;
//	private int mMediaTypeColumnId;
//	private int mDateColumnId;
//	private int mIdColumnId;
//	private int mLocalUriColumnId;
	
	
	DownloadManager mDownloadManager;
	Cursor mDownloadsCursor;

	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.game_detail_all);

		ActionBar actionBar = getActionBar();
		actionBar.setIcon(R.drawable.actionbar_icon);
		actionBar.setDisplayHomeAsUpEnabled(true);

		Intent intent = getIntent();

		String gameID = intent.getStringExtra("game_id");
		String gamePackageName = intent.getStringExtra("package_name");
		
		mGameDetailViewHolder = new GameDetailViewHolder();
		mGameDetailViewHolder.ivGameIcon = (ImageView) findViewById(R.id.iv_game_detail_icon);
		mGameDetailViewHolder.tvGameType = (TextView) findViewById(R.id.tv_game_type_detail);
		mGameDetailViewHolder.tvGamePackageSize = (TextView) findViewById(R.id.tv_game_size_detail);
		mGameDetailViewHolder.ivScreenShot1 = (ImageView) findViewById(R.id.iv_image1);
		mGameDetailViewHolder.ivScreenShot2 = (ImageView) findViewById(R.id.iv_image2);
		mGameDetailViewHolder.ivScreenShot3 = (ImageView) findViewById(R.id.iv_image3);
		mGameDetailViewHolder.ivScreenShot4 = (ImageView) findViewById(R.id.iv_image4);
		mGameDetailViewHolder.tvDescription = (TextView) findViewById(R.id.tv_game_detail_description);
		mGameDetailViewHolder.tvReputationGoodNum = (TextView) findViewById(R.id.tv_reputation_good_num);
		mGameDetailViewHolder.tvReputationBadNum = (TextView) findViewById(R.id.tv_reputation_bad_num);
		mGameDetailViewHolder.btnReputationBad = (LinearLayout) findViewById(R.id.ll_reputation_bad);
		mGameDetailViewHolder.btnReputationGood = (LinearLayout) findViewById(R.id.ll_reputation_good);
		mGameDetailViewHolder.btnReconnect = (Button) findViewById(R.id.btn_reconnect_internet_game_detail);
		mGameDetailViewHolder.llNetworkProblemPanel = (LinearLayout) findViewById(R.id.ll_network_problem_panel_game_detail);
		mGameDetailViewHolder.pbGameDetail = (ProgressBar) findViewById(R.id.pb_game_detail_progress_bar);
		mGameDetailViewHolder.rlGameDetail = (RelativeLayout) findViewById(R.id.rl_game_detail_panel);
		mGameDetailViewHolder.btnDownload = (Button) findViewById(R.id.btn_gamedetail_download);

		String param = "";
		mImageSaveorReadUtiltiy = new Utility();
		
		//downloadmanager
		mDownloadManager = new DownloadManager(getContentResolver(), getPackageName());
		
		startDownloadService(this);
		
		mDownloadManager.setAccessAllDownloads(true);
		
		if(gameID == null && gamePackageName != null)   //by packagename searching request the result;
		{
			param = gamePackageName;
			byPackageName = true;
		}
		else if(gameID != null && gamePackageName == null)
		{
			param = gameID;
			byPackageName = false;
		}
		
		if(!byPackageName)
		{
			boolean searchedResult = loadLocalGameDetail(param);
			if(searchedResult) {
				loadDataToTheWidget(mGameDetail, false);
				updateDownloadStatus();
			} else {
				mFetchGameTask.execute(param);
			}
		}
		else
			mFetchGameTask.execute(param);
		
		mGameDetailViewHolder.btnReconnect.setTag(gameID);
		// String featured_Game_Name = intent.getStringExtra("game_title");

		// Toast.makeText(GameDetail.this, featured_Game_Name,
		// Toast.LENGTH_SHORT).show();

		mGameDetailViewHolder.btnReputationBad
				.setOnClickListener(reputationVoterClickListener);
		mGameDetailViewHolder.btnReputationGood
				.setOnClickListener(reputationVoterClickListener);
		mGameDetailViewHolder.btnReconnect
				.setOnClickListener(reputationVoterClickListener);
		

//		DownloadManager.Query baseQuery = new DownloadManager.Query().setOnlyIncludeVisibleInDownloadsUi(true);
//		mDownloadsCursor = mDownloadManager.query(baseQuery);
		
//		Cursor cursor = this.mDownloadsCursor;
//		
//		mIdColumnId = cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_ID);
//		mTitleColumnId = cursor
//				.getColumnIndexOrThrow(DownloadManager.COLUMN_TITLE);
//		mStatusColumnId = cursor
//				.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS);
//		mReasonColumnId = cursor
//				.getColumnIndexOrThrow(DownloadManager.COLUMN_REASON);
//		mTotalBytesColumnId = cursor
//				.getColumnIndexOrThrow(DownloadManager.COLUMN_TOTAL_SIZE_BYTES);
//		mCurrentBytesColumnId = cursor
//				.getColumnIndexOrThrow(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR);
//		mMediaTypeColumnId = cursor
//				.getColumnIndexOrThrow(DownloadManager.COLUMN_MEDIA_TYPE);
//		mDateColumnId = cursor
//				.getColumnIndexOrThrow(DownloadManager.COLUMN_LAST_MODIFIED_TIMESTAMP);
//		mLocalUriColumnId = cursor
//				.getColumnIndexOrThrow(DownloadManager.COLUMN_LOCAL_URI);
		
		PushAgent.getInstance(this).onAppStart();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		MobclickAgent.onResume(this);
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		MobclickAgent.onPause(this);
	}

	private void updateDownloadStatus() {
		if(mGameDetail == null) return;
		
		int status = DownloadManager.STATUS_FAILED;
		//Log.e("Joe", "getView_id: " + mGameDetail.gameStatus.id);
		DownloadManager.Query query = new DownloadManager.Query();
		Log.e("Joe", "gameId: " + mGameDetail.gameId);
		if(mGameDetail.gameStatus.id <= 0) {
			query = query.setFilterByItemId(mGameDetail.gameId);
		} else {
			query = query.setFilterById(mGameDetail.gameStatus.id);
		}
		Log.e("Joe", "getView_id: " + mGameDetail.gameStatus.id);
		Cursor cursorExecution = mDownloadManager.query(query);
		if(cursorExecution != null) {
			if(cursorExecution.getCount() != 0) {
				cursorExecution.moveToFirst();
				mGameDetail.gameStatus.id = cursorExecution.getLong(cursorExecution.getColumnIndex(DownloadManager.COLUMN_ID));
				long totalBytes = cursorExecution.getLong(cursorExecution.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES));
				long currentBytes = cursorExecution.getLong(cursorExecution.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR));

				Log.e("Joe", "gameStatus.id: " + DownloadManager.COLUMN_ID + ", id: " + mGameDetail.gameStatus.id);
				Log.e("Joe", "toalBytesColId: " + DownloadManager.COLUMN_TOTAL_SIZE_BYTES + ", bytes: " + totalBytes);
				Log.e("Joe", "curBytesColId: " + DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR + ", bytes: " + currentBytes);
				
				status = cursorExecution.getInt(cursorExecution.getColumnIndex(DownloadManager.COLUMN_STATUS));
				String localUri = cursorExecution.getString(cursorExecution.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI));
				mGameDetail.gameStatus.filePath = localUri;
				Log.e("Joe", "filePath: " + localUri);
			}
			cursorExecution.close();
		}
		
		Button btn = mGameDetailViewHolder.btnDownload;
		
		switch(status) {
		case DownloadManager.STATUS_FAILED:
			btn.setText(getResources().getText(R.string.game_download));
			mGameDetail.gameStatus.status = DownloadStatus.GAME_NOT_DOWNLOAD;
			break;
			
		case DownloadManager.STATUS_PAUSED:
			btn.setText(getResources().getText(R.string.game_download_continue));
			mGameDetail.gameStatus.status = DownloadStatus.GAME_DOWNLOAD_PAUSED;
			break;			
		case DownloadManager.STATUS_PENDING:
		case DownloadManager.STATUS_RUNNING:
			btn.setText(getResources().getText(R.string.game_download_pause));
			mGameDetail.gameStatus.status = DownloadStatus.GAME_DOWNLOADING;
			break;			
		case DownloadManager.STATUS_SUCCESSFUL:
			if(!GameInstaller.isApkInstalled(this, mGameDetail.gamePackageName)) {
				btn.setText(this.getResources().getText(R.string.game_install));
				mGameDetail.gameStatus.status = DownloadStatus.GAME_DOWNLOADED;
			} else {
				btn.setText(getResources().getText(R.string.game_play));
				mGameDetail.gameStatus.status = DownloadStatus.GAME_INSTALLED;
			}
			break;					
		}
		
		btn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				performClicked((Button)v, mGameDetail);				
			}
		});
		
	}
	
	protected void performClicked(Button btn, GameInfoDetail gameInfoDetail) {
		switch(gameInfoDetail.gameStatus.status) {
		case DownloadStatus.GAME_NOT_DOWNLOAD:
			
			boolean donateVote = advPopupJudger();
			
			if(donateVote)
				showAdDailog();
			
			Log.e("Joe", "performClicked_id: " + gameInfoDetail.gameStatus.id);
			gameInfoDetail.gameStatus.id = startDownload(gameInfoDetail.gameDownLoadURL, gameInfoDetail.gameId);
//			refresh();
			break;
			
		case DownloadStatus.GAME_DOWNLOADING:
			mDownloadManager.pauseDownload(gameInfoDetail.gameStatus.id);
			break;
		case DownloadStatus.GAME_DOWNLOAD_PAUSED:
			mDownloadManager.resumeDownload(gameInfoDetail.gameStatus.id);
			break;
		case DownloadStatus.GAME_DOWNLOADED:
			btn.setText(R.string.game_install);
			
			SavedSharePreferences.getInstance(this).setDownloadedTime();//count for the adv
			SavedSharePreferences.getInstance(this).setDonateVote(true);
			
			if(!GameInstaller.isApkInstalled(this, gameInfoDetail.gamePackageName)) {
				if(gameInfoDetail.gameCategory.equals("APK")) {
					GameInstaller.installApk(this, gameInfoDetail.gameStatus.filePath);
				} else if(gameInfoDetail.gameCategory.equals("DPK")) {
					GameInstaller.installDpk(this, gameInfoDetail.gameStatus.filePath);
				}
			}
			break;
		case DownloadStatus.GAME_INSTALLED:
			btn.setText(R.string.game_play);
			if(gameInfoDetail.gameCategory.equals("APK") || gameInfoDetail.gameCategory.equals("DPK")) {
				Intent intent = getPackageManager().getLaunchIntentForPackage(gameInfoDetail.gamePackageName);
				this.startActivity(intent);
			}
			break;
		}
		updateDownloadStatus();
		finish();
	}
	
	
	private long startDownload(String url, String itemId) {
		Uri srcUri = Uri.parse(url);
		DownloadManager.Request request = new Request(srcUri);
		request.setItemId(itemId);
		request.setDestinationInExternalPublicDir(
				Environment.DIRECTORY_DOWNLOADS, "/");
		request.setDescription("Just for test");
		request.setShowRunningNotification(false);
		long ret = mDownloadManager.enqueue(request);
		return ret;
	}

	private void startDownloadService(Context context) {
		Intent intent = new Intent();
		intent.setClass(context, DownloadService.class);
		context.startService(intent);
	}

//	private ContentObserver mObserver;
//	public void unregisterObserver(ContentObserver observer) {
//		this.mDownloadsCursor.unregisterContentObserver(observer);
//	}
//	
//	public void registerObserver(ContentObserver observer) {
//		mObserver = observer;
//		this.mDownloadsCursor.registerContentObserver(observer);
//	}
//	
//	public void refresh() {
//		unregisterObserver(mObserver);
//		
//		this.mDownloadsCursor.close();
//		this.mDownloadsCursor = mDownloadManager.query(new DownloadManager.Query().setOnlyIncludeVisibleInDownloadsUi(true));
//		
//		registerObserver(mObserver);
//	}

	OnClickListener reputationVoterClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			if (mGameDetail == null) {
				if (v.getId() == R.id.btn_reconnect_internet_game_detail) {
					Button btnReconnect = (Button) v;
					String gameID = (String) btnReconnect.getTag();

					FetchGameTask fetchGameTask = new FetchGameTask();

					fetchGameTask.execute(gameID);
				}
				return;
			}

			ReputationVoteParam voteParam = new ReputationVoteParam();
			voteParam.gameID = mGameDetail.gameId;

			
			if(v.getId()==R.id.ll_reputation_bad)
			{
				voteParam.voteType = ReputationVoteParam.TYPE_BAD;
				new VoteForReputation().execute(voteParam);
			}
			else if(v.getId()==R.id.ll_reputation_good)
			{	voteParam.voteType = ReputationVoteParam.TYPE_GOOD;
				new VoteForReputation().execute(voteParam);
			}

		}
	};

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.game_detail, menu);
		return true;
	}

	private class FetchGameTask extends
			AsyncTask<String, Integer, GameInfoDetail> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();

			mGameDetailViewHolder.rlGameDetail.setVisibility(View.INVISIBLE);
			mGameDetailViewHolder.llNetworkProblemPanel
					.setVisibility(View.INVISIBLE);
			mGameDetailViewHolder.pbGameDetail.setVisibility(View.VISIBLE);

		}

		@Override
		protected GameInfoDetail doInBackground(String... params) {
			GameDetailsRequester gameInfoDetail = new GameDetailsRequester(byPackageName);
			GameInfoDetail gameDetail = new GameInfoDetail();
			try {
				gameDetail = gameInfoDetail.doRequest(params[0]);
			} catch (JSONException jsonEx) {
				// TODO:
				return null;
			} catch (IOException ioEx) {
				// TODO:
				return null;
			}
			
			if(gameDetail!=null&&!byPackageName)
			{
				insertGameDetail(gameDetail); //contentprovider data insert
			}

			return gameDetail;
		}

		@Override
		protected void onPostExecute(GameInfoDetail gameInfoDetail) {
			// TODO Auto-generated method stub
			mGameDetail = gameInfoDetail;

			if (gameInfoDetail == null) {
				// TODO: Network exception
				mGameDetailViewHolder.rlGameDetail.setVisibility(View.GONE);
				mGameDetailViewHolder.pbGameDetail.setVisibility(View.GONE);
				mGameDetailViewHolder.llNetworkProblemPanel
						.setVisibility(View.VISIBLE);
			} else {

				loadDataToTheWidget(gameInfoDetail, true);

				mGameDetailViewHolder.rlGameDetail.setVisibility(View.VISIBLE);

				mGameDetailViewHolder.pbGameDetail.setVisibility(View.GONE);

			}
			updateDownloadStatus();
		}

	}

	private class VoteForReputation extends
			AsyncTask<ReputationVoteParam, Integer, ReputationResult> {
		Voter voteForReputation = new Voter();

		@Override
		protected ReputationResult doInBackground(
				ReputationVoteParam... voteParam) {

			ReputationResult reputationResult = new ReputationResult();

			switch (voteParam[0].voteType) {
			case 0: // good
				try {
					reputationResult.result = voteForReputation
							.goodVoted(voteParam[0].gameID);
					reputationResult.voteType = ReputationVoteParam.TYPE_GOOD;
					return reputationResult;
				} catch (Exception e) {
					e.printStackTrace();

				}
				break;
			case 1: // bad
				try {
					reputationResult.result = voteForReputation
							.badVoted(voteParam[0].gameID);

					reputationResult.voteType = ReputationVoteParam.TYPE_BAD;

					return reputationResult;
				} catch (Exception e) {
					e.printStackTrace();
				}

				break;
			}

			reputationResult.result = false;
			reputationResult.voteType = ReputationVoteParam.NONE;

			return reputationResult;
		}

		@Override
		protected void onPostExecute(ReputationResult result) {

			super.onPostExecute(result);

			if (result.result == true) {

				if (result.voteType == ReputationVoteParam.TYPE_GOOD) {
					mGameDetail.gameGoodVote++;
					mGameDetailViewHolder.tvReputationGoodNum.setText(String
							.valueOf(mGameDetail.gameGoodVote));
				} else if (result.voteType == ReputationVoteParam.TYPE_BAD) {
					mGameDetail.gameBadVote++;
					mGameDetailViewHolder.tvReputationBadNum.setText(String
							.valueOf(mGameDetail.gameBadVote));
				}
			}
		}

	}

	private boolean loadscreenShots(String[] screenshots,
			ImageLoader imageLoader) {

		Log.e("screenshot length is ", Integer.toString(screenshots.length));

		if (screenshots.length != 0 && screenshots.length <= 4) {
			if (screenshots.length == 1) {
				imageLoader.displayImage(screenshots[0],
						mGameDetailViewHolder.ivScreenShot1, false);
			} else if (screenshots.length == 2) {
				imageLoader.displayImage(screenshots[0],
						mGameDetailViewHolder.ivScreenShot1, false);
				imageLoader.displayImage(screenshots[1],
						mGameDetailViewHolder.ivScreenShot2, false);
			} else if (screenshots.length == 3) {
				imageLoader.displayImage(screenshots[0],
						mGameDetailViewHolder.ivScreenShot1, false);
				imageLoader.displayImage(screenshots[1],
						mGameDetailViewHolder.ivScreenShot2, false);
				imageLoader.displayImage(screenshots[2],
						mGameDetailViewHolder.ivScreenShot3, false);
			} else {
				imageLoader.displayImage(screenshots[0],
						mGameDetailViewHolder.ivScreenShot1, false);
				imageLoader.displayImage(screenshots[1],
						mGameDetailViewHolder.ivScreenShot2, false);
				imageLoader.displayImage(screenshots[2],
						mGameDetailViewHolder.ivScreenShot3, false);
				imageLoader.displayImage(screenshots[3],
						mGameDetailViewHolder.ivScreenShot4, false);
			}
		}

		return false;
	}

	static class GameDetailViewHolder {
		private ImageView ivGameIcon;
		private TextView tvGameType;
		private TextView tvGamePackageSize;
		private ImageView ivScreenShot1;
		private ImageView ivScreenShot2;
		private ImageView ivScreenShot3;
		private ImageView ivScreenShot4;
		private TextView tvDescription;
		private TextView tvReputationGoodNum;
		private TextView tvReputationBadNum;
		private LinearLayout btnReputationGood;
		private LinearLayout btnReputationBad;
		private LinearLayout llNetworkProblemPanel;
		private Button btnReconnect;
		private ProgressBar pbGameDetail;
		private RelativeLayout rlGameDetail;
		private Button btnDownload;
	}

	static class ReputationVoteParam {
		public static int NONE = -1;

		public static int TYPE_GOOD = 0;
		public static int TYPE_BAD = 1;

		private String gameID;
		private int voteType;
	}

	static class ReputationResult {
		private boolean result;
		private int voteType;
	}

	private boolean advPopupJudger()
	{
		int downloadedTimes = SavedSharePreferences.getInstance(this).getDownloadedTime();
		boolean donateVote = SavedSharePreferences.getInstance(this).getDonateVote();
		
		if(downloadedTimes / 3 == 0 && donateVote == true)
			return true;
		else
			return false;
	}
	
	private void showAdDailog()
	{
		final Dialog dialog = new Dialog(this);
		
		dialog.setContentView(R.layout.ad_dialog);
		dialog.setTitle(getResources().getString(R.string.ad_favrite_title));
		
		LinearLayout llReject = (LinearLayout) dialog.findViewById(R.id.ll_ad_favor_reject);
		LinearLayout llDonate = (LinearLayout) dialog.findViewById(R.string.ad_favrite_donate);
		
		llReject.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
			
				SavedSharePreferences.getInstance(GameDetail.this).setDonateVote(false);
				dialog.dismiss();
			}
		});
		
		llDonate.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
			
				AppFlood.showInterstitial(GameDetail.this);
			}
		});
		
	}
	
	private void insertGameDetail(GameInfoDetail gameDetail)
	{
		String screenShots [] = {"","","",""};
		String icon ;
		ContentValues values = new ContentValues();
		ImageLoader imageHelper = new ImageLoader(getApplicationContext());
		String gameID = gameDetail.getGameId();
		
		values.put(GameDetailProvider.CATEGORY, gameDetail.getGameCategory());
		values.put(GameDetailProvider.ID, gameID);
		values.put(GameDetailProvider.DOWNLOADURL, gameDetail.getGameDownLoadURL());
		values.put(GameDetailProvider.NAME, gameDetail.getGameName());
		values.put(GameDetailProvider.PACKAGENAME, gameDetail.getGamePackageName());
		values.put(GameDetailProvider.PACKAGESIZE, gameDetail.getGamePackageSize());
		values.put(GameDetailProvider.TYPE, gameDetail.getGameType()); //there's problem with bitmap store method...
		//save icon local path to the sdcard;
		String iconTemp = gameDetail.getGameIcon();
		icon = mImageSaveorReadUtiltiy.storeImage(imageHelper.getBitmap(iconTemp), iconTemp, gameID);
		values.put(GameDetailProvider.ICON, icon);
		//save image local path to the sdcard;
		String screenURLTemp [] = gameDetail.getGameScreenShots();
		screenShots[0]= mImageSaveorReadUtiltiy.storeImage(imageHelper.getBitmap(screenURLTemp[0]), screenURLTemp[0], gameID);
		screenShots[1]= mImageSaveorReadUtiltiy.storeImage(imageHelper.getBitmap(screenURLTemp[1]), screenURLTemp[1], gameID);
		screenShots[2]= mImageSaveorReadUtiltiy.storeImage(imageHelper.getBitmap(screenURLTemp[2]), screenURLTemp[2], gameID);
		screenShots[3]= mImageSaveorReadUtiltiy.storeImage(imageHelper.getBitmap(screenURLTemp[3]), screenURLTemp[3], gameID);
		values.put(GameDetailProvider.GAMESCREENSHOTS1, screenShots[0]);
		values.put(GameDetailProvider.GAMESCREENSHOTS2, screenShots[1]);
		values.put(GameDetailProvider.GAMESCREENSHOTS3, screenShots[2]);
		values.put(GameDetailProvider.GAMESCREENSHOTS4, screenShots[3]);
		
		values.put(GameDetailProvider.GAMEGOODVOTE, gameDetail.gameGoodVote);
		values.put(GameDetailProvider.GAMEBADVOTE, gameDetail.gameBadVote);
		values.put(GameDetailProvider.GAMEDESCRIPTION, gameDetail.gameDescription);
		
		getContentResolver().insert(GameDetailProvider.CONTENT_URI, values);
	}
	
	private boolean loadLocalGameDetail(String gameID)
	{
		
		String requestURL = GameDetailProvider.CONTENT_URI + "/" + gameID;
		Uri gameURI = Uri.parse(requestURL);
		Cursor c;
		
		try{
		
			c = getContentResolver().query(gameURI, null, null, null, null);
		}
		catch(SQLException content)
		{
			content.printStackTrace();
			return false;
		}
		
		boolean returnOneResult = c.isFirst()&&c.isLast();
		
		if( c == null && !returnOneResult)  //check if the return is right
			return false; 
		
		if(!c.moveToFirst()) {
			c.close();
			return false;
		}
		else {
			
				GameInfoDetail gameInfoDetail = new GameInfoDetail();
				gameInfoDetail.gameIcon = c.getString(c.getColumnIndex(GameDetailProvider.ICON));
				gameInfoDetail.gameName = c.getString(c.getColumnIndex(GameDetailProvider.NAME));
				gameInfoDetail.gameCategory = c.getString(c.getColumnIndex(GameDetailProvider.CATEGORY));
				gameInfoDetail.gamePackageName = c.getString(c.getColumnIndex(GameDetailProvider.PACKAGENAME));
				gameInfoDetail.gamePackageSize = c.getString(c.getColumnIndex(GameDetailProvider.PACKAGESIZE));
				gameInfoDetail.gameType = c.getString(c.getColumnIndex(GameDetailProvider.TYPE));
				gameInfoDetail.gameDownLoadURL = c.getString(c.getColumnIndex(GameDetailProvider.DOWNLOADURL));
				gameInfoDetail.gameId = c.getString(c.getColumnIndex(GameDetailProvider.ID));			
				gameInfoDetail.gameScreenShots[0] = c.getString(c.getColumnIndex(GameDetailProvider.GAMESCREENSHOTS1));
				
				
				Log.e("wu qian icon load path is", gameInfoDetail.gameScreenShots[0]);
				
				gameInfoDetail.gameScreenShots[1] = c.getString(c.getColumnIndex(GameDetailProvider.GAMESCREENSHOTS2));
				gameInfoDetail.gameScreenShots[2] = c.getString(c.getColumnIndex(GameDetailProvider.GAMESCREENSHOTS3));
				gameInfoDetail.gameScreenShots[3] = c.getString(c.getColumnIndex(GameDetailProvider.GAMESCREENSHOTS4));
				gameInfoDetail.gameDescription = c.getString(c.getColumnIndex(GameDetailProvider.GAMEDESCRIPTION));
				gameInfoDetail.gameBadVote = c.getInt(c.getColumnIndex(GameDetailProvider.GAMEBADVOTE));
				gameInfoDetail.gameGoodVote = c.getInt(c.getColumnIndex(GameDetailProvider.GAMEGOODVOTE));
			
			c.close();
		
			mGameDetail = gameInfoDetail;
			
			return true;
		}	
	}
	
	private void loadDataToTheWidget(GameInfoDetail gameInfoDetail, boolean dataFromInternet)
	{
		ImageLoader imageLoader = new ImageLoader(
				getApplicationContext());

		if(dataFromInternet)
		{
			imageLoader.displayImage(gameInfoDetail.getGameIcon(),
				mGameDetailViewHolder.ivGameIcon, false);
		
			loadscreenShots(gameInfoDetail.gameScreenShots, imageLoader);
		}
		else
		{
			mGameDetailViewHolder.ivGameIcon.
			setImageBitmap(mImageSaveorReadUtiltiy.returnBitmapFromSDCard(gameInfoDetail.gameIcon));
			
			mGameDetailViewHolder.ivScreenShot1.
			setImageBitmap(mImageSaveorReadUtiltiy.returnBitmapFromSDCard(gameInfoDetail.gameScreenShots[0]));
			
			mGameDetailViewHolder.ivScreenShot2.
			setImageBitmap(mImageSaveorReadUtiltiy.returnBitmapFromSDCard(gameInfoDetail.gameScreenShots[1]));
			
			mGameDetailViewHolder.ivScreenShot3.
			setImageBitmap(mImageSaveorReadUtiltiy.returnBitmapFromSDCard(gameInfoDetail.gameScreenShots[2]));
			
			mGameDetailViewHolder.ivScreenShot4.
			setImageBitmap(mImageSaveorReadUtiltiy.returnBitmapFromSDCard(gameInfoDetail.gameScreenShots[3]));
		}
		
		mGameDetailViewHolder.tvDescription
				.setText(gameInfoDetail.gameDescription);

		mGameDetailViewHolder.tvGamePackageSize
				.setText(gameInfoDetail.gamePackageSize);
		
		setTitle(gameInfoDetail.gameName);

		mGameDetailViewHolder.tvGameType
				.setText(gameInfoDetail.gameType);

		mGameDetailViewHolder.tvReputationBadNum.setText(String
				.valueOf(gameInfoDetail.gameBadVote));

		mGameDetailViewHolder.tvReputationGoodNum.setText(String
				.valueOf(gameInfoDetail.gameGoodVote));
	}
	

}
