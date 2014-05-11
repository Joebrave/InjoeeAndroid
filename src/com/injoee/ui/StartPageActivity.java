package com.injoee.ui;

import com.injoee.R;
import com.injoee.util.SavedSharePreferences;
import com.umeng.analytics.MobclickAgent;
import com.umeng.fb.FeedbackAgent;
import com.umeng.message.PushAgent;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

public class StartPageActivity extends Activity {
	
	private ActionBar mActionBar;
	private static boolean sBooted = false;
	private SavedSharePreferences mFirstTimeLauncherMark;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.start_page);
		mFirstTimeLauncherMark = SavedSharePreferences.getInstance(this);
		
		//for user feedback
		FeedbackAgent agent = new FeedbackAgent(this);
		agent.sync();
		
		int showTime = 3000;
		if(sBooted) {
			showTime = 0;
		}
		new Handler().postDelayed(new startThread(), showTime);
		
		//umeng functionality
		MobclickAgent.updateOnlineConfig(this);
		PushAgent.getInstance(this).onAppStart();
	}
	
	public class startThread implements Runnable
	{

		@Override
		public void run() {
			
			Intent intent = new Intent(StartPageActivity.this, MainActivity.class);
			startActivity(intent);
			finish();
			sBooted = true;
		}
		
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

}
