package com.injoee.webservice;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.injoee.model.GameInfo;

public class GameListRequester {
	public int total;

	public GameListRequester() {

	}

	public List<GameInfo> doRequest(int start, int count) throws JSONException,
			IOException {

		List<GameInfo> list = new ArrayList<GameInfo>();

		String path = "http://192.168.0.112//games/list.php?";
		String param = "start=" + start + "&count=" + count; // 
		path = path + param;

		URL url = new URL(path);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();

		conn.setDoInput(true); // ������շ��������
		conn.setRequestMethod("GET");
		conn.setConnectTimeout(5000);

		if (conn.getResponseCode() == 200) {

			InputStream is = conn.getInputStream();
			// to get the stream
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
			
			// json
			JSONObject jsonObject = new JSONObject(json);
			int errorCode = jsonObject.getInt("error_code");

			if (errorCode == 1000) {
				return null;
			}
			total = jsonObject.getInt("total");

			JSONArray jsonArrayForOwner = jsonObject.getJSONArray("games");

			for (int j = 0; j < jsonArrayForOwner.length(); j++) {

				GameInfo gameInfoItem = new GameInfo();

				JSONObject jsonObjectGameDetail = (JSONObject) jsonArrayForOwner
						.opt(j);
				gameInfoItem.setGameId(jsonObjectGameDetail
						.getString("game_id"));

				Log.e("gameid", gameInfoItem.getGameId());

				gameInfoItem.setGameName(jsonObjectGameDetail
						.getString("game_name"));

				Log.e("gamename", gameInfoItem.getGameName());

				gameInfoItem.setGameIcon(jsonObjectGameDetail
						.getString("game_icon"));
				gameInfoItem.setGameCategory(jsonObjectGameDetail
						.getString("game_category"));
				gameInfoItem.setGamePackageName(jsonObjectGameDetail
						.getString("game_package_name"));
				gameInfoItem.setGamePackageSize(jsonObjectGameDetail
						.getString("game_package_size"));
				gameInfoItem.setGameDownLoadURL(jsonObjectGameDetail
						.getString("game_download_url"));
				gameInfoItem.setGameObbDownloadURL(jsonObjectGameDetail
						.getString("game_obb_pacakge_name"));
				gameInfoItem.setGameObbDownloadURL(jsonObjectGameDetail
						.getString("game_obb_download_url"));
				gameInfoItem.setGameType(jsonObjectGameDetail
						.getString("game_type"));

				list.add(gameInfoItem);
			}

		}
		return list;

	}
}
