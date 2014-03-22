package com.injoee.model;

public class GameInfoDetail extends GameInfo {

	public String gameDescription;
	public String[] gameScreenShots;
	public int gameGoodVote;
	public int gameBadVote;

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
