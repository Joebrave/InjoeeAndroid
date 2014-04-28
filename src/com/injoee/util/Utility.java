package com.injoee.util;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.util.Log;

public class Utility {

	public Utility() {

	}

	public String storeImage(Bitmap imageData, String filename, String gameID) {
		// get path to external storage (SD card)
		String iconsStoragePath = Environment.getExternalStorageDirectory()
				+ "/Injoee/Bufferpics/" + gameID + "/";
		File sdIconStorageDir = new File(iconsStoragePath);
		String filePath;

		String imageName = filename.substring(filename.lastIndexOf("/"));

		// create storage directories, if they don't exist
		sdIconStorageDir.mkdirs();
		filePath = sdIconStorageDir.toString() + imageName;

		File fileExistenceJudger = new File(filePath);

		if (!fileExistenceJudger.exists()) {  // if the file already exist for instance in case that icon re-saving
			try {

				FileOutputStream fileOutputStream = new FileOutputStream(
						filePath);

				BufferedOutputStream bos = new BufferedOutputStream(
						fileOutputStream);

				// choose another format if PNG doesn't suit you
				imageData.compress(CompressFormat.PNG, 100, bos);

				bos.flush();
				bos.close();

			} catch (FileNotFoundException e) {
				Log.w("TAG", "Error saving image file: " + e.getMessage());
				return "";
			} catch (IOException e) {
				Log.w("TAG", "Error saving image file: " + e.getMessage());
				return "";
			}
		}
		return filePath;
	}

	public Bitmap returnBitmapFromSDCard(String fileName) {
		File f = new File(fileName);
		FileInputStream is = null;
		try {
			is = new FileInputStream(f);
		} catch (FileNotFoundException e) {
			Log.d("error: ", String.format(
					"ShowPicture.java file[%s]Not Found", fileName));
			return null;
		}

		Bitmap bm = BitmapFactory.decodeStream(is, null, null);

		return bm;
	}

}
