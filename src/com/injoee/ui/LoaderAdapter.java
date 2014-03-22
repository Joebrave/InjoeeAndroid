package com.injoee.ui;

import java.util.ArrayList;
import java.util.List;

import com.injoee.R;
import com.injoee.imageloader.ImageLoader;
import com.injoee.model.GameInfo;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class LoaderAdapter extends BaseAdapter {

	private Context mContext;
	private List<GameInfo> mFeaturedGames;
	private List<GameInfo> mLocalGames;
	private ImageLoader mImageLoader;

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
			ViewHolder viewHolder = (ViewHolder) convertView.getTag();
			GameInfo gameInfo = mFeaturedGames.get(position - (localSize + 2));
			if (gameInfo != null) {
				viewHolder.tv_FeaturedGameName.setText(gameInfo.getGameName());
				viewHolder.tv_FeaturedGameType.setText(gameInfo.getGameType());
				viewHolder.tv_FeaturedGameSize.setText(gameInfo
						.getGamePackageSize());
				viewHolder.tv_FeaturedGameID.setText(gameInfo.getGameId());
				mImageLoader.displayImage(gameInfo.getGameIcon(),
						viewHolder.iv_FeaturedGameIcon, false);
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
