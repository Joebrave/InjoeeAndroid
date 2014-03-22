package com.injoee.webservice;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import org.json.JSONObject;


import android.util.Log;

public class Voter {
	
	public static class Vote {
	public int bad;
	public int good;
	}

	public Vote getVotes(String gameId) throws Exception {
		Vote voteNum = new Vote();

		String path = "http://www.injoee.com/games/detail.php?";

		String param = "id=" + gameId;

		path = path + param;

		URL url = new URL(path);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();

		conn.setDoInput(true); // ������շ���������
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

			voteNum.good = jsonObjectGameDetail.getInt("game_good_votes");
			
			Log.e("good votes num is -- ", String.valueOf(voteNum.good));

			voteNum.bad = jsonObjectGameDetail.getInt("game_bad_votes");
			
			Log.e("bad votes num is -- ", String.valueOf(voteNum.bad));
		}

		return voteNum;
	}

	public boolean badVoted(String gameId) throws Exception {

		String path = "http://www.injoee.com/games/votes.php?id=";

		path = path+ gameId + "&type=bad";

		URL url = new URL(path);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();

		conn.setDoInput(true); // ������շ���������
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

			Log.e("one bad vote-", json);

			baos.close();
			is.close();

			JSONObject jsonObject = new JSONObject(json);
			int errorCode = jsonObject.getInt("error_code");

			if (errorCode == 1000) {
				return false;
			}

		}
		return true;

	}

	public boolean goodVoted(String gameId) throws Exception {

		String path = "http://www.injoee.com/games/votes.php?id=";

		path = path+ gameId + "&type=good";

		URL url = new URL(path);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();

		conn.setDoInput(true); // ������շ���������
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

			Log.e("one good vote-", json);

			baos.close();
			is.close();

			JSONObject jsonObject = new JSONObject(json);
			int errorCode = jsonObject.getInt("error_code");

			if (errorCode == 1000) {
				return false;
			}

		}
		return true;

	}

}
