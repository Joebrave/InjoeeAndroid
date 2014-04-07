package com.injoee.ui;

import java.io.IOException;

import org.json.JSONException;

import com.injoee.R;

import com.injoee.imageloader.ImageLoader;
import com.injoee.model.GameInfoDetail;

import com.injoee.webservice.GameDetailsRequester;
import com.injoee.webservice.Voter;

import android.os.AsyncTask;
import android.os.Bundle;
import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
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
	private static GameDetailViewHolder sGameDetailViewHolder;
	private FetchGameTask mFetchGameTask = new FetchGameTask();

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
		if (sGameDetailViewHolder == null) {
			sGameDetailViewHolder = new GameDetailViewHolder();
			sGameDetailViewHolder.ivGameIcon = (ImageView) findViewById(R.id.iv_game_detail_icon);
			sGameDetailViewHolder.tvGameType = (TextView) findViewById(R.id.tv_game_type_detail);
			sGameDetailViewHolder.tvGamePackageSize = (TextView) findViewById(R.id.tv_game_size_detail);
			sGameDetailViewHolder.ivScreenShot1 = (ImageView) findViewById(R.id.iv_image1);
			sGameDetailViewHolder.ivScreenShot2 = (ImageView) findViewById(R.id.iv_image2);
			sGameDetailViewHolder.ivScreenShot3 = (ImageView) findViewById(R.id.iv_image3);
			sGameDetailViewHolder.ivScreenShot4 = (ImageView) findViewById(R.id.iv_image4);
			sGameDetailViewHolder.tvDescription = (TextView) findViewById(R.id.tv_game_detail_description);
			sGameDetailViewHolder.tvReputationGoodNum = (TextView) findViewById(R.id.tv_reputation_good_num);
			sGameDetailViewHolder.tvReputationBadNum = (TextView) findViewById(R.id.tv_reputation_bad_num);
			sGameDetailViewHolder.btnReputationBad = (LinearLayout) findViewById(R.id.ll_reputation_bad);
			sGameDetailViewHolder.btnReputationGood = (LinearLayout) findViewById(R.id.ll_reputation_good);
			sGameDetailViewHolder.btnReconnect = (Button) findViewById(R.id.btn_reconnect_internet_game_detail);
			sGameDetailViewHolder.llNetworkProblemPanel = (LinearLayout) findViewById(R.id.ll_network_problem_panel_game_detail);
			sGameDetailViewHolder.pbGameDetail = (ProgressBar) findViewById(R.id.pb_game_detail_progress_bar);
			sGameDetailViewHolder.rlGameDetail = (RelativeLayout) findViewById(R.id.rl_game_detail_panel);
		}

		mFetchGameTask.execute(gameID);
		sGameDetailViewHolder.btnReconnect.setTag(gameID);
		// String featured_Game_Name = intent.getStringExtra("game_title");

		// Toast.makeText(GameDetail.this, featured_Game_Name,
		// Toast.LENGTH_SHORT).show();

		sGameDetailViewHolder.btnReputationBad
				.setOnClickListener(reputationVoterClickListener);
		sGameDetailViewHolder.btnReputationGood
				.setOnClickListener(reputationVoterClickListener);
		sGameDetailViewHolder.btnReconnect
				.setOnClickListener(reputationVoterClickListener);

	}

	OnClickListener reputationVoterClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
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
			// TODO Auto-generated method stub
			super.onPreExecute();

			sGameDetailViewHolder.rlGameDetail.setVisibility(View.INVISIBLE);
			sGameDetailViewHolder.llNetworkProblemPanel
					.setVisibility(View.INVISIBLE);
			sGameDetailViewHolder.pbGameDetail.setVisibility(View.VISIBLE);

		}

		@Override
		protected GameInfoDetail doInBackground(String... params) {
			// TODO Auto-generated method stub
			GameDetailsRequester gameInfoDetail = new GameDetailsRequester();
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

			return gameDetail;
		}

		@Override
		protected void onPostExecute(GameInfoDetail gameInfoDetail) {
			// TODO Auto-generated method stub
			mGameDetail = gameInfoDetail;

			if (gameInfoDetail == null) {
				// TODO: Network exception
				sGameDetailViewHolder.rlGameDetail.setVisibility(View.GONE);
				sGameDetailViewHolder.pbGameDetail.setVisibility(View.GONE);
				sGameDetailViewHolder.llNetworkProblemPanel
						.setVisibility(View.VISIBLE);
			} else {

				ImageLoader imageLoader = new ImageLoader(
						getApplicationContext());

				imageLoader.displayImage(gameInfoDetail.getGameIcon(),
						sGameDetailViewHolder.ivGameIcon, false);

				loadscreenShots(gameInfoDetail.gameScreenShots, imageLoader);

				sGameDetailViewHolder.tvDescription
						.setText(gameInfoDetail.gameDescription);

				sGameDetailViewHolder.tvGamePackageSize
						.setText(gameInfoDetail.gamePackageSize);

				sGameDetailViewHolder.tvGameType
						.setText(gameInfoDetail.gameType);

				sGameDetailViewHolder.tvReputationBadNum.setText(String
						.valueOf(gameInfoDetail.gameBadVote));

				sGameDetailViewHolder.tvReputationGoodNum.setText(String
						.valueOf(gameInfoDetail.gameGoodVote));

				sGameDetailViewHolder.rlGameDetail.setVisibility(View.VISIBLE);

				sGameDetailViewHolder.pbGameDetail.setVisibility(View.GONE);

			}

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
					// TODO Auto-generated catch block
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
					// TODO Auto-generated catch block
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
					sGameDetailViewHolder.tvReputationGoodNum.setText(String
							.valueOf(mGameDetail.gameGoodVote));
				} else if (result.voteType == ReputationVoteParam.TYPE_BAD) {
					mGameDetail.gameBadVote++;
					sGameDetailViewHolder.tvReputationBadNum.setText(String
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
						sGameDetailViewHolder.ivScreenShot1, false);
			} else if (screenshots.length == 2) {
				imageLoader.displayImage(screenshots[0],
						sGameDetailViewHolder.ivScreenShot1, false);
				imageLoader.displayImage(screenshots[1],
						sGameDetailViewHolder.ivScreenShot2, false);
			} else if (screenshots.length == 3) {
				imageLoader.displayImage(screenshots[0],
						sGameDetailViewHolder.ivScreenShot1, false);
				imageLoader.displayImage(screenshots[1],
						sGameDetailViewHolder.ivScreenShot2, false);
				imageLoader.displayImage(screenshots[2],
						sGameDetailViewHolder.ivScreenShot3, false);
			} else {
				imageLoader.displayImage(screenshots[0],
						sGameDetailViewHolder.ivScreenShot1, false);
				imageLoader.displayImage(screenshots[1],
						sGameDetailViewHolder.ivScreenShot2, false);
				imageLoader.displayImage(screenshots[2],
						sGameDetailViewHolder.ivScreenShot3, false);
				imageLoader.displayImage(screenshots[3],
						sGameDetailViewHolder.ivScreenShot4, false);
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

}
