package com.injoee.model;

public class GameInfo {
	public String gameId;
	public String gameName;
	public String gameIcon;
	public String gameType;

	public String gameCategory;


	public String gamePackageName;
	public String gamePackageSize;

	public String gameDownLoadURL;
	public String gameObbPackagename;
	public String gameObbDownloadURL;
	
	public DownloadStatus gameStatus = new DownloadStatus();
	public static class DownloadStatus {
		public final static int GAME_NOT_DOWNLOAD = 0;
		public final static int GAME_DOWNLOADING = 1;
		public final static int GAME_DOWNLOAD_PAUSED = 2;
		public final static int GAME_DOWNLOADED = 3;
		public final static int GAME_INSTALLED = 4;
		
		public long id = -1;
		public int status = GAME_NOT_DOWNLOAD;
		public int progress = -1;
	}
	
	public String getGameCategory() {
		return gameCategory;
	}

	public void setGameCategory(String gameCategory) {
		this.gameCategory = gameCategory;
	}


	public String getGameId() {
		return gameId;
	}

	public void setGameId(String gameId) {
		this.gameId = gameId;
	}

	public String getGameName() {
		return gameName;
	}

	public void setGameName(String gameName) {
		this.gameName = gameName;
	}

	public String getGameIcon() {
		return gameIcon;
	}

	public void setGameIcon(String gameIcon) {
		this.gameIcon = gameIcon;
	}

	public String getGameType() {
		return gameType;
	}

	public void setGameType(String gameType) {
		this.gameType = gameType;
	}

	public String getGamePackageName() {
		return gamePackageName;
	}

	public void setGamePackageName(String gamePackageName) {
		this.gamePackageName = gamePackageName;
	}

	public String getGameDownLoadURL() {
		return gameDownLoadURL;
	}

	public void setGameDownLoadURL(String gameDownLoadURL) {
		this.gameDownLoadURL = gameDownLoadURL;
	}

	public String getGameObbPackagename() {
		return gameObbPackagename;
	}

	public void setGameObbPackagename(String gameObbPackagename) {
		this.gameObbPackagename = gameObbPackagename;
	}

	public String getGameObbDownloadURL() {
		return gameObbDownloadURL;
	}

	public void setGameObbDownloadURL(String gameObbDownloadURL) {
		this.gameObbDownloadURL = gameObbDownloadURL;
	}

	public String getGamePackageSize() {
		return gamePackageSize;
	}

	public void setGamePackageSize(String gamePackageSize) {
		this.gamePackageSize = gamePackageSize;
	}

	// TODO: implemented by Qian
}
