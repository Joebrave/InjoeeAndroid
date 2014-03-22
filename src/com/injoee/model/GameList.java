package com.injoee.model;

public class GameList {
	private static GameList sInstance;
	
	private GameList(){
		
	}
	
	public static GameList getInstance() {
		
		if(sInstance == null) {
			synchronized(GameList.class) {
				if(sInstance == null) {
					sInstance = new GameList();
				}
			}
		}
		return sInstance;
	}
}
