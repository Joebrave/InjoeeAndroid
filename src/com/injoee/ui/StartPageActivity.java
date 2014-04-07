package com.injoee.ui;

import com.injoee.R;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

public class StartPageActivity extends Activity {
	
	private ActionBar mActionBar;
	private static boolean sBooted = false;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.start_page);
		int showTime = 3000;
		if(sBooted) {
			showTime = 0;
		}
		new Handler().postDelayed(new startThread(), showTime);
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

}
