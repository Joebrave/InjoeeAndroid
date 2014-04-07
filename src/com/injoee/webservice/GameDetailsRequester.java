package com.injoee.webservice;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.injoee.model.GameInfoDetail;

public class GameDetailsRequester {
	public GameDetailsRequester() {

	}

	// TODO: please use HttpURLConnection
	public GameInfoDetail doRequest(String gameId) throws IOException, JSONException {
		// TODO: to be implemented

		GameInfoDetail gameDetailItem = new GameInfoDetail();

		String path = "http://www.injoee.com/games/detail.php?";

		String param = "id=" + gameId;

		path = path + param;

		URL url = new URL(path);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();

		conn.setDoInput(true); // 
		conn.setRequestMethod("GET");
		conn.setConnectTimeout(5000);

		if (conn.getResponseCode() == 200) {
			InputStream is = conn.getInputStream();

			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			byte[] buffer = new byte[1024];
			int len = 0;

			while ((len = is.read(buffer)) != -1) {
				baos.write(buffer, 0, len);
			}

			String json = baos.toString();

			Log.e("json string result is-", json);

			baos.close();
			is.close();

			JSONObject jsonObject = new JSONObject(json);
			int errorCode = jsonObject.getInt("error_code");

			if (errorCode == 1000) {
				return null;
			}

			String jsonGameDetail = jsonObject.getString("game_detail");

			JSONObject jsonObjectGameDetail = new JSONObject(jsonGameDetail);

			gameDetailItem.setGameName(jsonObjectGameDetail
					.getString("game_name"));

			gameDetailItem.setGameIcon(jsonObjectGameDetail
					.getString("game_icon"));

			gameDetailItem.setGamePackageSize(jsonObjectGameDetail
					.getString("game_package_size"));

			gameDetailItem.setGameType(jsonObjectGameDetail
					.getString("game_type"));

			gameDetailItem.setGameCategory(jsonObjectGameDetail
					.getString("game_category"));

			gameDetailItem
					.setGameScreenShots(JsonStringScreenShotSplit(jsonObjectGameDetail
							.getString("game_screenshots")));

			gameDetailItem.setGameDescription(jsonObjectGameDetail
					.getString("game_description"));

			gameDetailItem.setGamePackageName(jsonObjectGameDetail
					.getString("game_package_name"));

			gameDetailItem.setGameDownLoadURL(jsonObjectGameDetail
					.getString("game_download_url"));

			gameDetailItem.setGameObbPackagename(jsonObjectGameDetail
					.getString("game_obb_pacakge_name"));

			gameDetailItem.setGameObbDownloadURL(jsonObjectGameDetail
					.getString("game_obb_download_url"));

			gameDetailItem.setGameGoodVote(jsonObjectGameDetail
					.getInt("game_good_votes"));

			Log.e("game good votes num is",
					Integer.toString(gameDetailItem.getGameBadVote()));

			gameDetailItem.setGameBadVote(jsonObjectGameDetail
					.getInt("game_bad_votes"));

		}

		return gameDetailItem;
	}

	private String[] JsonStringScreenShotSplit(String jsonString) {
		String[] screenshotArray = null;

		screenshotArray = jsonString.split(",");

		return screenshotArray;
	}

}
