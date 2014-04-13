package com.injoee.func;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import android.util.Log;

/**
 * Class for zip compressor. Only extract is implemented now.
 * 
 */
public class ZipCompressor {

	/**
	 * Extract a compressed file to the target directory.
	 * 
	 * @param compressedFile
	 *            the compressed file.
	 * @param tmpDirectory
	 *            the target directory to extract files.
	 * @param targetDirectory
	 *            the target directory to save files.
	 */
    public static boolean extract(File compressedFile, File tmpDirectory,
            File targetDirectory) {
        if (tmpDirectory == null) {
            return false;
        }
        if (tmpDirectory.exists() && tmpDirectory.isFile()) {
        	return false;
        }
        if (!tmpDirectory.exists()) {
        	tmpDirectory.mkdir();
        }
        ZipFile zipFile = null;
        boolean status = false;
        File[] files = null;
        File[] targets = null;
        int fi = 0;
        try {
            zipFile = new ZipFile(compressedFile);
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            files = new File[zipFile.size()];
            targets = new File[zipFile.size()];
            while (entries.hasMoreElements()) {

                ZipEntry entry = entries.nextElement();
                String name = entry.getName();

				Log.i("Installer", "extract: " + name);
                File file = new File(tmpDirectory, name);
                if (file.exists()) {
                    file.delete();
                }
                file = new File(tmpDirectory, name);
                File target = new File(targetDirectory, name);
                if (entry.isDirectory()) {
                    file.mkdir();
                } else {
                    createFolder(file.getParentFile());
                    file.createNewFile();
                    FileOutputStream fos = null;
                    InputStream is = null;
                    try {
                        fos = new FileOutputStream(file);
                        is = zipFile.getInputStream(entry);
                        copyFile(is, fos);
                        files[fi] = file;
                        targets[fi] = target;
                        fi++;
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        try {
                            if (fos != null) {
                                fos.close();
                            }
                        } catch (IOException ex) {
                            //
                        }

                        try {
                            if (is != null) {
                                is.close();
                            }

                        } catch (IOException ex) {
                            //
                        }
                    }
                }
            }
            status = true;
        } catch (ZipException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (zipFile != null) {
                    zipFile.close();
                }
            } catch (IOException ex) {
                //
            }
        }
        if (status && files != null) {
        	for (int i = 0; i < fi; i++) {
        		createFolder(targets[i].getParentFile());
        		status &= files[i].renameTo(targets[i]);
        	}
        }
        return status;
    }

    private static void createFolder(File parent) {
        if (!parent.exists()) {
            createFolder(parent.getParentFile());
        }
        parent.mkdir();
    }
    
    public static void copyFile(InputStream in, OutputStream out)
            throws IOException {
        int COPY_FILE_BUFFER_LENGTH = 4 * 1024;
        byte[] buf = new byte[COPY_FILE_BUFFER_LENGTH];
        int l = 0;
        while ((l = in.read(buf)) > 0) {
            out.write(buf, 0, l);
        }
        out.flush();
    }

}
