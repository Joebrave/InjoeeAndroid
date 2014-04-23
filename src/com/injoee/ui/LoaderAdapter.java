package com.injoee.ui;

import java.util.ArrayList;
import java.util.List;

import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.appflood.AppFlood;
import com.injoee.R;
import com.injoee.func.GameInstaller;
import com.injoee.imageloader.ImageLoader;
import com.injoee.model.GameInfo;
import com.injoee.model.GameInfo.DownloadStatus;
import com.injoee.providers.DownloadManager;
import com.injoee.providers.DownloadManager.Request;
import com.injoee.providers.downloads.DownloadService;
import com.injoee.util.FeaturedGamesListProvider;
import com.injoee.util.SavedSharePreferences;

public class LoaderAdapter extends BaseAdapter {

	private Context mContext;
	private List<GameInfo> mFeaturedGames;
	private List<GameInfo> mLocalGames;
	private ImageLoader mImageLoader;
	private SavedSharePreferences downloadedTimesSharePreference;
	final private int mTitleColumnId;
	final private int mStatusColumnId;
	final private int mReasonColumnId;
	final private int mTotalBytesColumnId;
	final private int mCurrentBytesColumnId;
	final private int mMediaTypeColumnId;
	final private int mDateColumnId;
	final private int mIdColumnId;
	final private int mLocalUriColumnId;

	DownloadManager mDownloadManager;
	Cursor mDownloadsCursor;

	private boolean isPausedForWifi(Cursor cursor) {
		return cursor.getInt(mReasonColumnId) == DownloadManager.PAUSED_QUEUED_FOR_WIFI;
	}

	/**
	 * Move {@link #mDateSortedCursor} to the download with the given ID.
	 * 
	 * @return true if the specified download ID was found; false otherwise
	 */
	private boolean moveToDownload(long downloadId) {
		for (mDownloadsCursor.moveToFirst(); !mDownloadsCursor.isAfterLast(); mDownloadsCursor
				.moveToNext()) {
			if (mDownloadsCursor.getLong(mIdColumnId) == downloadId) {
				return true;
			}
		}
		return false;
	}

	public LoaderAdapter(Context context) {
		this.mContext = context;
		this.mFeaturedGames = new ArrayList<GameInfo>();
		this.mLocalGames = new ArrayList<GameInfo>();
		this.mImageLoader = new ImageLoader(context);
		this.mDownloadManager = new DownloadManager(
				context.getContentResolver(), context.getPackageName());
		
		startDownloadService(context);
		
		downloadedTimesSharePreference = SavedSharePreferences.getInstance(context);
		
		this.mDownloadManager.setAccessAllDownloads(true);
		DownloadManager.Query baseQuery = new DownloadManager.Query()
											.setOnlyIncludeVisibleInDownloadsUi(true);
		this.mDownloadsCursor = mDownloadManager.query(baseQuery);
		
		Cursor cursor = this.mDownloadsCursor;
		mIdColumnId = cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_ID);
		mTitleColumnId = cursor
				.getColumnIndexOrThrow(DownloadManager.COLUMN_TITLE);
		mStatusColumnId = cursor
				.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS);
		mReasonColumnId = cursor
				.getColumnIndexOrThrow(DownloadManager.COLUMN_REASON);
		mTotalBytesColumnId = cursor
				.getColumnIndexOrThrow(DownloadManager.COLUMN_TOTAL_SIZE_BYTES);
		mCurrentBytesColumnId = cursor
				.getColumnIndexOrThrow(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR);
		mMediaTypeColumnId = cursor
				.getColumnIndexOrThrow(DownloadManager.COLUMN_MEDIA_TYPE);
		mDateColumnId = cursor
				.getColumnIndexOrThrow(DownloadManager.COLUMN_LAST_MODIFIED_TIMESTAMP);
		mLocalUriColumnId = cursor
				.getColumnIndexOrThrow(DownloadManager.COLUMN_LOCAL_URI);
	}

	public void setFeaturedGames(List<GameInfo> games) {
		this.mFeaturedGames = games;
	}

	public ImageLoader getImageLoader() {
		return mImageLoader;
	}

	public void setLocalGames(List<GameInfo> games) {
		this.mLocalGames = games;
	}

	@Override
	public int getCount() {
		return this.mFeaturedGames.size() + mLocalGames.size() + 2; // add
																	// featured
																	// games and
																	// my games
																	// title
	}

	@Override
	public Object getItem(int position) {
		return position;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public int getItemViewType(int position) {
		int localSize = this.mLocalGames.size();

		Log.i("Joe", "getItemViewType: " + position);

		// my games and featured games type
		if (position == 0 || position == (localSize + 1)) {
			return 0;
		} else if (0 < position && position < (localSize + 1)) {
			return 1; // my games
		} else {
			return 2; // featured games
		}
	}

	@Override
	public int getViewTypeCount() {

		return 3; // type sum
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		int localSize = this.mLocalGames.size();

		Log.i("Joe", "View: " + position + ", convertView: " + convertView);

		if (position == 0) {
			TextView tvLocalGameTitle = new TextView(mContext);
			tvLocalGameTitle.setText("My Games");

			return tvLocalGameTitle;
		}

		if (position == localSize + 1) {
			TextView tvLocalGameTitle = new TextView(mContext);
			tvLocalGameTitle.setText("Featured Games");

			return tvLocalGameTitle;
		}

		if (convertView == null) {
			if (0 < position && position < localSize + 1) {
				ViewHolderMyGame viewHolder = null;
				convertView = LayoutInflater.from(mContext).inflate(
						R.layout.my_game_item, null);
				viewHolder = new ViewHolderMyGame();

				viewHolder.tvMyGameName = (TextView) convertView
						.findViewById(R.id.tv_my_game_name);
				viewHolder.tvMyGameSize = (TextView) convertView
						.findViewById(R.id.tv_my_game_size);
				viewHolder.ivMyGameIcon = (ImageView) convertView
						.findViewById(R.id.iv_my_games_icon);
				viewHolder.btnDeleteGame = (Button) convertView
						.findViewById(R.id.btn_delete_game);
				viewHolder.btnPlayGame = (Button) convertView
						.findViewById(R.id.btn_play_game);

				convertView.setTag(viewHolder);
			} else {
				ViewHolder viewHolder = null;
				convertView = LayoutInflater.from(mContext).inflate(
						R.layout.featured_game_item, null);
				viewHolder = new ViewHolder();

				viewHolder.tv_FeaturedGameName = (TextView) convertView
						.findViewById(R.id.tv_game_name);
				viewHolder.tv_FeaturedGameType = (TextView) convertView
						.findViewById(R.id.tv_featured_game_type);
				viewHolder.tv_FeaturedGameSize = (TextView) convertView
						.findViewById(R.id.tv_featured_game_size);
				viewHolder.tv_FeaturedGameID = (TextView) convertView
						.findViewById(R.id.tv_game_id);

				viewHolder.iv_FeaturedGameIcon = (ImageView) convertView
						.findViewById(R.id.iv_featured_games_icon);
				viewHolder.btn_Download = (Button) convertView
						.findViewById(R.id.btn_featured_game_downlaod);
				viewHolder.pb_Download = (ProgressBar) convertView
						.findViewById(R.id.pb_featured_game_download);
				viewHolder.ib_Cancel = (ImageButton) convertView
						.findViewById(R.id.ib_download_cancel);

				viewHolder.ll_GameInfoPanel = (LinearLayout) convertView
						.findViewById(R.id.ll_game_info);
				viewHolder.ll_GameDownloadPanel = (LinearLayout) convertView
						.findViewById(R.id.ll_game_download_panel);
				viewHolder.ib_Cancel = (ImageButton) convertView
						.findViewById(R.id.ib_download_cancel);

				viewHolder.btn_Download.setFocusable(false);
				viewHolder.btn_Download.setFocusableInTouchMode(false);

				convertView.setTag(viewHolder);
			}
		}

		if (0 < position && position < (localSize + 1)) {
			ViewHolderMyGame viewHolder = (ViewHolderMyGame) convertView
					.getTag();

			// waiting to add the content of local games.

		} else {
			final ViewHolder viewHolder = (ViewHolder) convertView.getTag();
			final GameInfo gameInfo = mFeaturedGames.get(position - (localSize + 2));

			// handler
			if (gameInfo != null) {
				//set widgets values
				viewHolder.tv_FeaturedGameName.setText(gameInfo.getGameName());
				viewHolder.tv_FeaturedGameType.setText(gameInfo.getGameType());
				viewHolder.tv_FeaturedGameSize.setText(gameInfo.getGamePackageSize());
				viewHolder.tv_FeaturedGameID.setText(gameInfo.getGameId());
				mImageLoader.displayImage(gameInfo.getGameIcon(), viewHolder.iv_FeaturedGameIcon, false);
				
				int status = DownloadManager.STATUS_FAILED;
				Log.e("Joe", "getView_id: " + gameInfo.gameStatus.id);
				DownloadManager.Query query = new DownloadManager.Query();
				
				if(gameInfo.gameStatus.id <= 0) {
					query = query.setFilterByItemId(gameInfo.gameId);
				} else {
					query = query.setFilterById(gameInfo.gameStatus.id);
				}
				Cursor cursor = mDownloadManager.query(query);
				if(cursor != null) {
					if(cursor.getCount() != 0) {
						cursor.moveToFirst();
						gameInfo.gameStatus.id = cursor.getLong(this.mIdColumnId);
						long totalBytes = cursor.getLong(this.mTotalBytesColumnId);
						long currentBytes = cursor.getLong(this.mCurrentBytesColumnId);
						
						Log.e("Joe", "toalBytesColId: " + this.mTotalBytesColumnId + ", bytes: " + totalBytes);
						Log.e("Joe", "curBytesColId: " + this.mCurrentBytesColumnId + ", bytes: " + currentBytes);
						
						int progress = getProgressValue(totalBytes, currentBytes);
	
						boolean indeterminate = (status == DownloadManager.STATUS_PENDING);
						Log.e("Joe", "progress: " + progress + ", indeterminate: "+ indeterminate);
						viewHolder.pb_Download.setIndeterminate(indeterminate);
						if (!indeterminate) {
							viewHolder.pb_Download.setProgress(progress);
						}
						
						status = cursor.getInt(this.mStatusColumnId);
						String localUri = cursor.getString(this.mLocalUriColumnId);
						gameInfo.gameStatus.filePath = localUri;
						Log.e("Joe", "filePath: " + localUri);
					}
					cursor.close();
				}
				
				Button btn = viewHolder.btn_Download;
				switch(status) {
				case DownloadManager.STATUS_FAILED:
					btn.setText(mContext.getResources().getText(R.string.game_download));
					viewHolder.ll_GameDownloadPanel.setVisibility(View.INVISIBLE);
					viewHolder.ll_GameInfoPanel.setVisibility(View.VISIBLE);
					gameInfo.gameStatus.status = DownloadStatus.GAME_NOT_DOWNLOAD;
					break;
					
				case DownloadManager.STATUS_PAUSED:
					btn.setText(mContext.getResources().getText(R.string.game_download_continue));
					viewHolder.ll_GameDownloadPanel.setVisibility(View.VISIBLE);
					viewHolder.ll_GameInfoPanel.setVisibility(View.INVISIBLE);
					viewHolder.ib_Cancel.setOnClickListener(new OnClickListener(){

						@Override
						public void onClick(View v) {
							gameInfo.gameStatus.status = DownloadStatus.GAME_NOT_DOWNLOAD;
							deleteDownload(gameInfo.gameStatus.id);
							viewHolder.ll_GameDownloadPanel.setVisibility(View.INVISIBLE);
							viewHolder.ll_GameInfoPanel.setVisibility(View.VISIBLE);
						}});
					gameInfo.gameStatus.status = DownloadStatus.GAME_DOWNLOAD_PAUSED;
					break;
					
				case DownloadManager.STATUS_PENDING:
				case DownloadManager.STATUS_RUNNING:
					btn.setText(mContext.getResources().getText(R.string.game_download_pause));
					viewHolder.ll_GameDownloadPanel.setVisibility(View.VISIBLE);
					viewHolder.ll_GameInfoPanel.setVisibility(View.INVISIBLE);
					gameInfo.gameStatus.status = DownloadStatus.GAME_DOWNLOADING;
					viewHolder.ib_Cancel.setOnClickListener(new OnClickListener(){

						@Override
						public void onClick(View v) {
							gameInfo.gameStatus.status = DownloadStatus.GAME_NOT_DOWNLOAD;
							deleteDownload(gameInfo.gameStatus.id);
							viewHolder.ll_GameDownloadPanel.setVisibility(View.INVISIBLE);
							viewHolder.ll_GameInfoPanel.setVisibility(View.VISIBLE);
							refresh();
						}});
					break;
					
				case DownloadManager.STATUS_SUCCESSFUL:
					viewHolder.ll_GameDownloadPanel.setVisibility(View.INVISIBLE);
					viewHolder.ll_GameInfoPanel.setVisibility(View.VISIBLE);
					
					if(!GameInstaller.isApkInstalled(mContext, gameInfo.gamePackageName)) {
						btn.setText(mContext.getResources().getText(R.string.game_install));
						gameInfo.gameStatus.status = DownloadStatus.GAME_DOWNLOADED;
					} else {
						btn.setText(mContext.getResources().getText(R.string.game_play));
						gameInfo.gameStatus.status = DownloadStatus.GAME_INSTALLED;
					}
					break;					
				}
				
				btn.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						performClicked((Button)v, gameInfo);
					}
					
				});


			}
		}

		return convertView;
	}
	
	private void deleteDownload(long downloadId) {
		if (moveToDownload(downloadId)) {
			int status = mDownloadsCursor.getInt(mStatusColumnId);
			boolean isComplete = status == DownloadManager.STATUS_SUCCESSFUL
					|| status == DownloadManager.STATUS_FAILED;
			String localUri = mDownloadsCursor.getString(mLocalUriColumnId);
			if (isComplete && localUri != null) {
				String path = Uri.parse(localUri).getPath();
				if (path.startsWith(Environment.getExternalStorageDirectory()
						.getPath())) {
					mDownloadManager.markRowDeleted(downloadId);
					return;
				}
			}
		}
		mDownloadManager.remove(downloadId);
	}
	
	public int getProgressValue(long totalBytes, long currentBytes) {
		if (totalBytes == -1) {
			return 0;
		}
		return (int) (currentBytes * 100 / totalBytes);
	}
	
	protected void performClicked(Button btn, GameInfo gameInfo) {
		switch(gameInfo.gameStatus.status) {
		case DownloadStatus.GAME_NOT_DOWNLOAD:
			
			boolean donateVote = advPopupJudger();
			
			if(donateVote)
				showAdDailog();
			
			Log.e("Joe", "performClicked_id: " + gameInfo.gameStatus.id);
			gameInfo.gameStatus.id = startDownload(gameInfo.gameDownLoadURL, gameInfo.gameId);
			refresh();
			break;
			
		case DownloadStatus.GAME_DOWNLOADING:
			mDownloadManager.pauseDownload(gameInfo.gameStatus.id);
			break;
		case DownloadStatus.GAME_DOWNLOAD_PAUSED:
			mDownloadManager.resumeDownload(gameInfo.gameStatus.id);
			break;
		case DownloadStatus.GAME_DOWNLOADED:
			btn.setText(R.string.game_install);
			
			downloadedTimesSharePreference.setDownloadedTime();//count for the adv
			downloadedTimesSharePreference.setDonateVote(true);
			
			if(!GameInstaller.isApkInstalled(mContext, gameInfo.gamePackageName)) {
				if(gameInfo.gameCategory.equals("APK")) {
					GameInstaller.installApk(mContext, gameInfo.gameStatus.filePath);
				} else if(gameInfo.gameCategory.equals("DPK")) {
					GameInstaller.installDpk(mContext, gameInfo.gameStatus.filePath);
				}
			}
			break;
		case DownloadStatus.GAME_INSTALLED:
			btn.setText(R.string.game_play);
			if(gameInfo.gameCategory.equals("APK") || gameInfo.gameCategory.equals("DPK")) {
				Intent intent = mContext.getPackageManager().getLaunchIntentForPackage(gameInfo.gamePackageName);
				mContext.startActivity(intent);
			}
			break;
		}

	}

	static class ViewHolder {
		// featured game
		TextView tv_FeaturedGameName;
		TextView tv_FeaturedGameType;
		TextView tv_FeaturedGameSize;
		TextView tv_FeaturedGameID;
		ImageView iv_FeaturedGameIcon;
		Button btn_Download;
		ProgressBar pb_Download;
		ImageButton ib_Cancel;
		LinearLayout ll_GameInfoPanel;
		LinearLayout ll_GameDownloadPanel;

	}

	static class ViewHolderMyGame {
		// my games
		ImageView ivMyGameIcon;
		TextView tvMyGameName;
		TextView tvMyGameSize;
		Button btnDeleteGame;
		Button btnPlayGame;
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

	private ContentObserver mObserver;
	public void unregisterObserver(ContentObserver observer) {
		this.mDownloadsCursor.unregisterContentObserver(observer);
	}
	
	public void registerObserver(ContentObserver observer) {
		mObserver = observer;
		this.mDownloadsCursor.registerContentObserver(observer);
	}
	
	public void refresh() {
		unregisterObserver(mObserver);
		
		this.mDownloadsCursor.close();
		this.mDownloadsCursor = mDownloadManager.query(new DownloadManager.Query().setOnlyIncludeVisibleInDownloadsUi(true));
		
		registerObserver(mObserver);
	}
	
	private boolean advPopupJudger()
	{
		int downloadedTimes = downloadedTimesSharePreference.getDownloadedTime();
		boolean donateVote = downloadedTimesSharePreference.getDonateVote();
		
		if(downloadedTimes/3==0&& donateVote == true)
			return true;
		else
			return false;
	}
	
	private void showAdDailog()
	{
		final Dialog dialog = new Dialog(mContext);
		
		dialog.setContentView(R.layout.ad_dialog);
		dialog.setTitle(mContext.getResources().getString(R.string.ad_favrite_title));
		
		LinearLayout llReject = (LinearLayout) dialog.findViewById(R.id.ll_ad_favor_reject);
		LinearLayout llDonate = (LinearLayout) dialog.findViewById(R.string.ad_favrite_donate);
		
		llReject.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
			
				downloadedTimesSharePreference.setDonateVote(false);
				dialog.dismiss();
			}
		});
		
		llDonate.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
			
				AppFlood.showInterstitial((MainActivity)mContext);
			}
		});
		
	}
}
