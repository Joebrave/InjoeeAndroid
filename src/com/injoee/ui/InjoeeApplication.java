package com.injoee.ui;

import com.umeng.message.PushAgent;
import com.umeng.message.UTrack;
import com.umeng.message.UmengMessageHandler;
import com.umeng.message.UmengNotificationClickHandler;
import com.umeng.message.entity.UMessage;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;

public class InjoeeApplication extends Application {

	private PushAgent mPushAgent;
	private String packageName = "";
	
	@Override
	public void onCreate() {
		super.onCreate();
		mPushAgent = PushAgent.getInstance(this);
		
		UmengMessageHandler messageHandler = new UmengMessageHandler(){
			@Override
			public void dealWithCustomMessage(final Context context, final UMessage msg) {
				new Handler(getMainLooper()).post(new Runnable() {
					
					@Override
					public void run() {
						// TODO Auto-generated method stub
						UTrack.getInstance(getApplicationContext()).trackMsgClick(msg);
						packageName = msg.custom;
						
						
					}
				});
			}
		};
		
		mPushAgent.setMessageHandler(messageHandler);
		
		UmengNotificationClickHandler notificationClickHandler = new UmengNotificationClickHandler(){
			@Override
			public void dealWithCustomAction(Context context, UMessage msg) {
				
				Intent intent = new Intent(context, GameDetail.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				packageName = msg.custom;
				intent.putExtra("package_name", packageName);
				startActivity(intent);
			}
		};
		mPushAgent.setNotificationClickHandler(notificationClickHandler);
	}
}
