package com.injoee.func;

public class DownloadUtil {
	public static interface DownloadListener {
		public void onStarted(String id);
		public void onResumed(String id);
		public void onPaused(String id);
		public void onFailed(String id);
		public void onCancelled(String id);
		public void onFinished(String id);
		public void onProgress(String id, double progress);
	}
}
