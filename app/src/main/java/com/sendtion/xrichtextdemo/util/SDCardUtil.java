package com.sendtion.xrichtextdemo.util;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class SDCardUtil {
	public static String SDCardRoot = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator;
	public static String APP_NAME = "XRichText";

	/**
	 * 检查是否存在SDCard
	 */
	public static boolean hasSdcard(){
		String state = Environment.getExternalStorageState();
		if(state.equals(Environment.MEDIA_MOUNTED)){
			return true;
		}else{
			return false;
		}
	}

	/**
	 * 获得文章图片保存路径
	 */
	public static String getPictureDir(){
		String imageCacheUrl = SDCardRoot + APP_NAME + File.separator;
		File file = new File(imageCacheUrl);
		if(!file.exists())
			file.mkdirs();  //如果不存在则创建
		return imageCacheUrl;
	}

	/**
	 * 图片保存到SD卡
	 * @param bitmap
	 * @return
	 */
	public static String saveToSdCard(Bitmap bitmap) {
		String imageUrl = getPictureDir() + System.currentTimeMillis() + "-";
		File file = new File(imageUrl);
		try {
			FileOutputStream out = new FileOutputStream(file);
			if (bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)) {
				out.flush();
				out.close();
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return file.getAbsolutePath();
	}

	/**
	 * 根据Uri获取图片文件的绝对路径
	 */
	public static String getFilePathByUri(Context context, final Uri uri) {
		if (null == uri) {
			return null;
		}
		final String scheme = uri.getScheme();
		String data = null;
		if (scheme == null) {
			data = uri.getPath();
		} else if (ContentResolver.SCHEME_FILE.equals(scheme)) {
			data = uri.getPath();
		} else if (ContentResolver.SCHEME_CONTENT.equals(scheme)) {
			Cursor cursor = context.getContentResolver().query(uri,
					new String[] { MediaStore.Images.ImageColumns.DATA }, null, null, null);
			if (null != cursor) {
				if (cursor.moveToFirst()) {
					int index = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
					if (index > -1) {
						data = cursor.getString(index);
					}
				}
				cursor.close();
			}
		}
		return data;
	}

	/**
	 * 根据Uri获取真实的文件路径
	 *
	 * @param context
	 * @param uri
	 * @return
	 */
	public static String getFilePathFromUri(Context context, Uri uri) {
		if (uri == null) return null;

		ContentResolver resolver = context.getContentResolver();
		FileInputStream input = null;
		FileOutputStream output = null;
		try {
			ParcelFileDescriptor pfd = resolver.openFileDescriptor(uri, "r");
			if (pfd == null) {
				return null;
			}
			FileDescriptor fd = pfd.getFileDescriptor();
			input = new FileInputStream(fd);


			File outputDir = context.getCacheDir();
			File outputFile = File.createTempFile("image", "tmp", outputDir);
			String tempFilename = outputFile.getAbsolutePath();
			output = new FileOutputStream(tempFilename);

			int read;
			byte[] bytes = new byte[4096];
			while ((read = input.read(bytes)) != -1) {
				output.write(bytes, 0, read);
			}

			return new File(tempFilename).getAbsolutePath();
		} catch (Exception ignored) {

			ignored.getStackTrace();
		} finally {
			try {
				if (input != null){
					input.close();
				}
				if (output != null){
					output.close();
				}
			} catch (Throwable t) {
				// Do nothing
			}
		}
		return null;
	}

	/** 删除文件 **/
	public static boolean deleteFile(String filePath) {
		File file = new File(filePath);
		boolean isOk = false;
		if (file.isFile() && file.exists())
			isOk = file.delete(); // 删除文件
		return isOk;
	}

}
