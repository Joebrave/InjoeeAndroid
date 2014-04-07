package com.injoee.ui;

import java.util.ArrayList;
import java.util.List;

import com.injoee.R;
import com.injoee.imageloader.ImageLoader;
import com.injoee.model.GameInfo;

import android.content.Context;
import android.content.res.Resources;
import android.os.Handler;
import android.os.Message;
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

public class LoaderAdapter extends BaseAdapter {

	private Context mContext;
	private List<GameInfo> mFeaturedGames;
	private List<GameInfo> mLocalGames;
	private ImageLoader mImageLoader;
	private final int GAMENOTINSTALLED = 0;
	private final int GAMEINSTALLING = 1;
	private final int GAMEPAUSE = 2;
	private final int GAMEINSTALLED = 3;
	private final int STOP = 0x10000;
	private final int GO = 0x10001;

	public LoaderAdapter(Context context) {
		this.mContext = context;
		this.mFeaturedGames = new ArrayList<GameInfo>();
		this.mLocalGames = new ArrayList<GameInfo>();
		this.mImageLoader = new ImageLoader(context);
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

				viewHolder.pb_Download = (ProgressBar) convertView
						.findViewById(R.id.pb_featured_game_download);
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
			GameInfo gameInfo = mFeaturedGames.get(position - (localSize + 2));

			// handler

			

			if (gameInfo != null) {
				viewHolder.tv_FeaturedGameName.setText(gameInfo.getGameName());
				viewHolder.tv_FeaturedGameType.setText(gameInfo.getGameType());
				viewHolder.tv_FeaturedGameSize.setText(gameInfo
						.getGamePackageSize());
				viewHolder.btn_Download.setTag(GAMENOTINSTALLED);

				viewHolder.tv_FeaturedGameID.setText(gameInfo.getGameId());
				mImageLoader.displayImage(gameInfo.getGameIcon(),
						viewHolder.iv_FeaturedGameIcon, false);
				
				
				 final Handler mHandler = new Handler() {
					public void handleMessage(Message msg) {
						
						if(msg.what<100&&!Thread.currentThread().isInterrupted())
						{
							viewHolder.pb_Download.setProgress(msg.what);
							viewHolder.pb_Download.setVisibility(View.VISIBLE);
						}
						else if(msg.what==100)
						{
							viewHolder.ll_GameDownloadPanel.setVisibility(View.INVISIBLE);
							viewHolder.ll_GameInfoPanel.setVisibility(View.VISIBLE);
							viewHolder.btn_Download.setText(mContext.getResources().getString(R.string.game_play));
							viewHolder.btn_Download.setTag(GAMEINSTALLED);
							Thread.currentThread().interrupt();
						}
							
							
					}
				};

				viewHolder.btn_Download
						.setOnClickListener(new OnClickListener() {

							@Override
							public void onClick(View v) {

								// progressbar counter
								Thread progressbarThread = new Thread(
										new Runnable() {

											@Override
											public void run() {

												int progressbarPrecentage = 0;

												for (int i = 0; i < 20; i++) {
													try {
														progressbarPrecentage = (i + 1) * 5;
														Thread.sleep(1000);

														Message msg = new Message();
														msg.what = progressbarPrecentage;
														mHandler.sendMessage(msg);
													

													} catch (Exception e) {
														e.printStackTrace();
													}
												}

											}
										});

								Button btnDownload = (Button) v;

								int gameStatus = (Integer) btnDownload.getTag();

								if (gameStatus == GAMENOTINSTALLED) {
									Log.e("download button touched",
											"download button is touched");
									btnDownload.setText(mContext.getResources().getText(R.string.game_download_pause));
									btnDownload.setTag(GAMEINSTALLING);

									viewHolder.ll_GameDownloadPanel.setVisibility(View.VISIBLE);
									viewHolder.ll_GameInfoPanel.setVisibility(View.INVISIBLE);

									viewHolder.pb_Download.setMax(100);
									viewHolder.pb_Download.setProgress(0);
									
									
									progressbarThread.start();

								} else if (gameStatus == GAMEINSTALLING) {
									btnDownload.setText(mContext.getResources().getText(R.string.game_download_pause));
									btnDownload.setTag(GAMEPAUSE);
								} else if (gameStatus == GAMEPAUSE) {
									btnDownload.setText(mContext.getResources().getText(R.string.game_download_continue));
									btnDownload.setTag(GAMEINSTALLING);
								} else if (gameStatus == GAMEINSTALLED) {
									btnDownload.setText(mContext.getResources().getText(R.string.game_play));

								}

	

							}
						});
				
				//set the status changed after the cancel button clicked
				viewHolder.ib_Cancel.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View v) {
						
						viewHolder.btn_Download.setTag(GAMENOTINSTALLED);
						viewHolder.btn_Download.setText(mContext.getResources().getText(R.string.game_download));
						viewHolder.pb_Download.setProgress(0);
						viewHolder.ll_GameDownloadPanel.setVisibility(View.INVISIBLE);
						viewHolder.ll_GameInfoPanel.setVisibility(View.VISIBLE);
						Thread.currentThread().interrupt();;
						
					}
				});

			}
		}

		return convertView;
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
}
