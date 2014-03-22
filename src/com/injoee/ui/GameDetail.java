package com.injoee.ui;

import com.injoee.R;
import com.injoee.R.layout;
import com.injoee.R.menu;
import com.injoee.imageloader.ImageLoader;
import com.injoee.model.GameInfoDetail;
import com.injoee.model.GameList;
import com.injoee.webservice.GameDetailsRequester;
import com.injoee.webservice.Voter;
import com.injoee.webservice.Voter.Vote;

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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class GameDetail extends Activity {

	private String mGameID = null;
	private GameInfoDetail mGameDetail = new GameInfoDetail();
	private static GameDetailViewHolder mGameDetailViewHolder = new GameDetailViewHolder();

	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.game_detail_all);

		ActionBar actionBar = getActionBar();
		actionBar.setIcon(R.drawable.actionbar_icon);
		actionBar.setDisplayHomeAsUpEnabled(true);

		Intent intent = getIntent();

		mGameID = intent.getStringExtra("game_id");

		mGameDetailViewHolder.mivGameIcon = (ImageView) findViewById(R.id.iv_game_detail_icon);
		mGameDetailViewHolder.mtvGameType = (TextView) findViewById(R.id.tv_game_type_detail);
		mGameDetailViewHolder.mtvGamePackageSize = (TextView) findViewById(R.id.tv_game_size_detail);
		mGameDetailViewHolder.mivScreenShot1 = (ImageView) findViewById(R.id.iv_image1);
		mGameDetailViewHolder.mivScreenShot2 = (ImageView) findViewById(R.id.iv_image2);
		mGameDetailViewHolder.mivScreenShot3 = (ImageView) findViewById(R.id.iv_image3);
		mGameDetailViewHolder.mivScreenShot4 = (ImageView) findViewById(R.id.iv_image4);
		mGameDetailViewHolder.mtvDescription = (TextView) findViewById(R.id.tv_game_detail_description);
		mGameDetailViewHolder.mtvReputationGoodNum = (TextView) findViewById(R.id.tv_reputation_good_num);
		mGameDetailViewHolder.mtvReputationBadNum = (TextView) findViewById(R.id.tv_reputation_bad_num);
		mGameDetailViewHolder.mbtnReputationBad = (LinearLayout) findViewById(R.id.ll_reputation_bad);
		mGameDetailViewHolder.mbtnReputationGood = (LinearLayout) findViewById(R.id.ll_reputation_good);

		new FetchGameTask().execute(mGameID);
		// String featured_Game_Name = intent.getStringExtra("game_title");

		// Toast.makeText(GameDetail.this, featured_Game_Name,
		// Toast.LENGTH_SHORT).show();

		mGameDetailViewHolder.mbtnReputationBad
				.setOnClickListener(reputationVoterClickListener);
		mGameDetailViewHolder.mbtnReputationGood
				.setOnClickListener(reputationVoterClickListener);

	}

	OnClickListener reputationVoterClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			ReputationVoteParam voteParam = new ReputationVoteParam();
			voteParam.mGameID = mGameID;

			switch (v.getId()) {
			case R.id.ll_reputation_bad:
				voteParam.result = 1;
				new VoteForReputation().execute(voteParam);
				break;
			case R.id.ll_reputation_good:
				voteParam.result = 0;
				new VoteForReputation().execute(voteParam);
				break;
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
		protected GameInfoDetail doInBackground(String... params) {
			// TODO Auto-generated method stub
			GameDetailsRequester gameInfoDetail = new GameDetailsRequester();
			try {
				mGameDetail = gameInfoDetail.doRequest(params[0]);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			return mGameDetail;
		}

		@Override
		protected void onPostExecute(GameInfoDetail gameInfoDetail) {
			// TODO Auto-generated method stub

			ImageLoader imageLoader = new ImageLoader(getApplicationContext());

			imageLoader.DisplayImage(gameInfoDetail.getGameIcon(),
					mGameDetailViewHolder.mivGameIcon, false);

			ScreenShotLoader(gameInfoDetail.gameScreenShots, imageLoader);

			mGameDetailViewHolder.mtvDescription
					.setText(gameInfoDetail.gameDescription);

			mGameDetailViewHolder.mtvGamePackageSize
					.setText(gameInfoDetail.gamePackageSize);

			mGameDetailViewHolder.mtvGameType.setText(gameInfoDetail.gameType);

			mGameDetailViewHolder.mtvReputationBadNum.setText(String
					.valueOf(gameInfoDetail.gameBadVote));

			mGameDetailViewHolder.mtvReputationGoodNum.setText(String
					.valueOf(gameInfoDetail.gameGoodVote));

		}

	}

	private class VoteForReputation extends
			AsyncTask<ReputationVoteParam, Integer, ReputationResult> {
		Voter voteForReputation = new Voter();

		@Override
		protected ReputationResult doInBackground(
				ReputationVoteParam... voteParam) {

			ReputationResult reputationResult = new ReputationResult();

			switch (voteParam[0].result) {
			case 0: // good
				try {
					reputationResult.result = voteForReputation
							.goodVoted(voteParam[0].mGameID);
					reputationResult.goodorBad = 0;
					return reputationResult;
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();

				}
				break;
			case 1: // bad
				try {
					reputationResult.result = voteForReputation
							.badVoted(voteParam[0].mGameID);

					reputationResult.goodorBad = 1;

					return reputationResult;
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				break;
			}

			reputationResult.result = false;
			reputationResult.goodorBad = 2;

			return reputationResult;
		}

		@Override
		protected void onPostExecute(ReputationResult result) {
			// TODO Auto-generated method stub
			Vote voteResult = new Vote();

			super.onPostExecute(result);

			if (result.result == true) {

				int num = 0;

				if (result.goodorBad == 0) {
					num = Integer
							.valueOf((String) mGameDetailViewHolder.mtvReputationGoodNum
									.getText()) + 1;

					mGameDetailViewHolder.mtvReputationGoodNum.setText(String
							.valueOf(num));
					
				} else if (result.goodorBad == 1) {
					num = Integer
							.valueOf((String) mGameDetailViewHolder.mtvReputationBadNum
									.getText()) + 1;

					mGameDetailViewHolder.mtvReputationBadNum.setText(String
							.valueOf(num));
				}
			}
		}

	}

	private boolean ScreenShotLoader(String[] screenshots,
			ImageLoader imageLoader) {

		Log.e("screenshot length is ", Integer.toString(screenshots.length));

		if (screenshots.length != 0 && screenshots.length <= 4) {
			if (screenshots.length == 1) {
				imageLoader.DisplayImage(screenshots[0],
						mGameDetailViewHolder.mivScreenShot1, false);
			} else if (screenshots.length == 2) {
				imageLoader.DisplayImage(screenshots[0],
						mGameDetailViewHolder.mivScreenShot1, false);
				imageLoader.DisplayImage(screenshots[1],
						mGameDetailViewHolder.mivScreenShot2, false);
			} else if (screenshots.length == 3) {
				imageLoader.DisplayImage(screenshots[0],
						mGameDetailViewHolder.mivScreenShot1, false);
				imageLoader.DisplayImage(screenshots[1],
						mGameDetailViewHolder.mivScreenShot2, false);
				imageLoader.DisplayImage(screenshots[2],
						mGameDetailViewHolder.mivScreenShot3, false);
			} else {
				imageLoader.DisplayImage(screenshots[0],
						mGameDetailViewHolder.mivScreenShot1, false);
				imageLoader.DisplayImage(screenshots[1],
						mGameDetailViewHolder.mivScreenShot2, false);
				imageLoader.DisplayImage(screenshots[2],
						mGameDetailViewHolder.mivScreenShot3, false);
				imageLoader.DisplayImage(screenshots[3],
						mGameDetailViewHolder.mivScreenShot4, false);
			}
		}

		return false;
	}

	static class GameDetailViewHolder {
		private ImageView mivGameIcon;
		private TextView mtvGameType;
		private TextView mtvGamePackageSize;
		private ImageView mivScreenShot1;
		private ImageView mivScreenShot2;
		private ImageView mivScreenShot3;
		private ImageView mivScreenShot4;
		private TextView mtvDescription;
		private TextView mtvReputationGoodNum;
		private TextView mtvReputationBadNum;
		private LinearLayout mbtnReputationGood;
		private LinearLayout mbtnReputationBad;
	}

	static class ReputationVoteParam {
		private String mGameID;
		private int result; // 0 stands for good, 1 stands for bad
	}

	static class ReputationResult {
		private boolean result;
		private int goodorBad;
	}

}
