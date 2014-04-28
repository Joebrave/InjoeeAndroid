package com.injoee.model;

public class GameInfoDetail extends GameInfo {

	public String gameDescription;
	public String[] gameScreenShots = {"","","","",""};
	public int gameGoodVote;
	public int gameBadVote;
	
	public DownloadStatus gameStatus = new DownloadStatus();
	
	public static class DownloadStatus {
		public final static int GAME_NOT_DOWNLOAD = 0;
		public final static int GAME_DOWNLOADING = 1;
		public final static int GAME_DOWNLOAD_PAUSED = 2;
		public final static int GAME_DOWNLOADED = 3;
		public final static int GAME_INSTALLED = 4;
		
		public long id = -1;
		public int status = GAME_NOT_DOWNLOAD;
		public String filePath;
	}

	public int getGameGoodVote() {
		return gameGoodVote;
	}

	public void setGameGoodVote(int gameGoodVote) {
		this.gameGoodVote = gameGoodVote;
	}

	public int getGameBadVote() {
		return gameBadVote;
	}

	public void setGameBadVote(int gameBadVote) {
		this.gameBadVote = gameBadVote;
	}

	public String getGameDescription() {
		return gameDescription;
	}

	public void setGameDescription(String gameDescription) {
		this.gameDescription = gameDescription;
	}

	public String[] getGameScreenShots() {
		return gameScreenShots;
	}

	public void setGameScreenShots(String[] gameScreenShots) {
		this.gameScreenShots = gameScreenShots;
	}

}
