package com.injoee.func;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.injoee.R;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
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
	
	private static class InstallDpkTask extends AsyncTask<String, Integer, String> {
		private final Context mContext;
		private final ProgressDialog mProgressDialog;
		public InstallDpkTask(Context context) {
			mContext = context;
			mProgressDialog = new ProgressDialog(context);
		}
		
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			mProgressDialog.setIndeterminate(true);
			mProgressDialog.setCanceledOnTouchOutside(false);
			mProgressDialog.setCancelable(true);
			mProgressDialog.setMessage(mContext.getText(R.string.extracting));
			mProgressDialog.show();
		}

		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			
			String apkFile = null;
			
			if(result != null) {
				File[] files = new File(result).listFiles();
				for(File f : files) {
					if(f.isFile()) {
						if(f.getAbsolutePath().endsWith(".apk")) {
							apkFile = f.getAbsolutePath();
							break;
						}
					}
				}
			}
			if(mProgressDialog.isShowing()) {
				mProgressDialog.dismiss();
			}
			
			if(apkFile != null) {
				installApk(mContext, apkFile);
			} else {
				Toast.makeText(mContext, "no apk found!", Toast.LENGTH_SHORT).show();
			}
		}

		@Override
		protected void onProgressUpdate(Integer... values) {
			super.onProgressUpdate(values);
			boolean copyStart = (values[0] == 50);
			if(copyStart) {
				mProgressDialog.setMessage("Loading...");
			}
		}

		@Override
		protected void onCancelled() {
			// TODO Auto-generated method stub
			super.onCancelled();
		}

		@Override
		protected String doInBackground(String... params) {
			String dpkFile = params[0];
			String srcFile = null;
			if(dpkFile.startsWith("file://")) {
				srcFile = dpkFile.substring(7, dpkFile.length());
			} else {
				srcFile = dpkFile;
			}
			
			//decompress dpk
			String unzipDpkDir = unzipFile(mContext, srcFile);
			
			this.publishProgress(50);
			
			//copy to right place
			try {
				copyFolder(new File(unzipDpkDir, OBB_DIR), new File(Environment.getExternalStorageDirectory(), OBB_DIR));
			} catch (IOException e) {
				return null;
			}
			return unzipDpkDir;
		}
		
	}
	
	public static boolean installDpk(Context context, String dpkFile) {
		InstallDpkTask installTask = new InstallDpkTask(context);
		installTask.execute(dpkFile);
		return true;
	}
	
	public static boolean installArcadeFile(Context context, String arcadeFile) {
		return false;//TODO: temporarily not supported
	}
	
	private static boolean dpkUnzipped(Context context, File targetFolder) {
		if(targetFolder.exists() && targetFolder.isDirectory()) {
			File[] list = targetFolder.listFiles(new FilenameFilter(){

				@Override
				public boolean accept(File dir, String filename) {
					if(filename.endsWith(".apk")) {
						return true;
					}
					return false;
				}});
			if(list == null) return false;
			//TODO: judge OBB existence
			return true;
		}
		return false;
	}
	
    private static String unzipFile(Context context, String filePath) {
        String folderName = getFolderName(filePath); 
        File file = new File(filePath);
        File dir = getDir(context);
        File tmpFolder = new File(dir, folderName + File.separator + "tmp");
        File targetFolder = new File(dir, folderName);
        
        Log.e("GameInstaller", "tmpFolder: " + tmpFolder.getAbsolutePath());
        Log.e("GameInstaller", "targetFolder: " + targetFolder.getAbsolutePath());
        boolean ret = true;
        
        if(!dpkUnzipped(context, targetFolder)){
        	ret = ZipCompressor.extract(file, tmpFolder, targetFolder);
        }
        if (ret) {
            return targetFolder.getAbsolutePath();
        } else {
            return null;
        }
    }
    
    private static File getDir(Context context) {
    	if(isExternalStorageWritable()) {
    		return new File(Environment.getExternalStorageDirectory(), "injoeeDownloads");
    	} else {
    		return new File(context.getFilesDir(), "injoeeDownloads");
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
			if(dest.exists()) return; //TODO: should be more restrict
			
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
