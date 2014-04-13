package com.injoee.func;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

public class GameInstaller {
	private final static String OBB_DIR = "Android";
	
	public static boolean isApkInstalled(Context context, String packageName) {
		PackageManager pm = context.getPackageManager();
		try {
			Drawable d = pm.getApplicationIcon(packageName);
			return (d != null);
		} catch (NameNotFoundException e) {
			return false;
		}
	}
	
	public static void installApk(Context context, String apkFile) {
		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.setDataAndType(Uri.parse("file://" + apkFile), "application/vnd.android.package-archive");
		context.startActivity(intent);
	}
	
	public static boolean installDpk(Context context, String dpkFile) {
		String srcFile = null;
		if(dpkFile.startsWith("file://")) {
			srcFile = dpkFile.substring(7, dpkFile.length());
		} else {
			srcFile = dpkFile;
		}
		
		//decompress dpk
		String unzipDpkDir = unzipFile(context, srcFile);
		
		//copy to right place
		try {
			copyFolder(new File(unzipDpkDir, OBB_DIR), new File(Environment.getExternalStorageDirectory(), OBB_DIR));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		File[] files = new File(unzipDpkDir).listFiles();
		String apkFile = null;
		for(File f : files) {
			if(f.isFile()) {
				if(f.getAbsolutePath().endsWith(".apk")) {
					apkFile = f.getAbsolutePath();
					break;
				}
			}
		}
		
		if(apkFile != null) {
			installApk(context, apkFile);
		} else {
			Toast.makeText(context, "no apk found!", Toast.LENGTH_SHORT).show();
		}
		return true;
	}
	
	public static boolean installArcadeFile(Context context, String arcadeFile) {
		return false;//TODO: temporarily not supported
	}
	
    private static String unzipFile(Context context, String filePath) {
        String folderName = getFolderName(filePath); 
        File file = new File(filePath);
        File dir = getDir(context);
        File tmpFolder = new File(dir, folderName + File.separator + "tmp");
        File targetFolder = new File(dir, folderName);
        
        Log.e("GameInstaller", "tmpFolder: " + tmpFolder.getAbsolutePath());
        Log.e("GameInstaller", "targetFolder: " + targetFolder.getAbsolutePath());
        boolean ret = ZipCompressor.extract(file, tmpFolder, targetFolder);
        
        file.delete();
        
        if (ret) {
            return targetFolder.getAbsolutePath();
        } else {
            return null;
        }
    }
    
    private static File getDir(Context context) {
    	if(isExternalStorageWritable()) {
    		return Environment.getExternalStorageDirectory();
    	} else {
    		return context.getFilesDir();
    	}
    }
    private static boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }
    
    private static String getFolderName(String filePath) {
        return filePath.substring(filePath.lastIndexOf('/') + 1, filePath.lastIndexOf(".") - 1);
    }
    
	/**
	 * 复制一个目录及其子目录、文件到另外一个目录
	 * @param src
	 * @param dest
	 * @throws IOException
	 */
	private static void copyFolder(File src, File dest) throws IOException {
		if (src.isDirectory()) {
			if (!dest.exists()) {
				dest.mkdir();
			}
			String files[] = src.list();
			for (String file : files) {
				Log.i("Installer", "copy: " + file);
				File srcFile = new File(src, file);
				File destFile = new File(dest, file);
				Log.e("copyFolder: ", "srcFile: " + srcFile.getAbsolutePath());
				Log.e("copyFolder: ", "destFile: " + destFile.getAbsolutePath());
				
				// 递归复制
				copyFolder(srcFile, destFile);
			}
		} else {
			InputStream in = new FileInputStream(src);
			OutputStream out = new FileOutputStream(dest);

			byte[] buffer = new byte[1024];

			int length;
			
			while ((length = in.read(buffer)) > 0) {
				out.write(buffer, 0, length);
			}
			in.close();
			out.close();
		}
	}
}
