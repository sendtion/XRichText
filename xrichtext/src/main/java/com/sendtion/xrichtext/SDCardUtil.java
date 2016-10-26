package com.sendtion.xrichtext;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.format.Time;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SDCardUtil {
	public static String SDCardRoot = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator;

	/**
	 * 检查是否存在SDCard
	 * @return
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
	 * @return
	 */
	public static String getPictureDir(){
		String imageCacheUrl = SDCardRoot + "TimeNote" + File.separator + "Picture" + File.separator ;
		File file = new File(imageCacheUrl);
		if(!file.exists())
			file.mkdir();  //如果不存在则创建
		return imageCacheUrl;
	}

	/**
	 * 获得图片缓存路径
	 * @return
	 */
	public static String getImageCacheDir(){
		String imageCacheUrl = SDCardRoot + "TimeNote" + File.separator + "ImageCache" ;
		File file = new File(imageCacheUrl);
		if(!file.exists())
			file.mkdir();  //如果不存在则创建
		return imageCacheUrl;
	}

	/**
	 * 获得缓存目录
	 * @return
	 */
	public static String getCacheDir(){
		String cacheUrl = SDCardRoot + "TimeNote" + File.separator + "Cache" ;
		File file = new File(cacheUrl);
		if(!file.exists())
			file.mkdir();  //如果不存在则创建
		return cacheUrl;
	}

	/**
	 * 笔记保存路径
	 * @return
	 */
	public static String getDownloadDir(){
		String cacheUrl = SDCardRoot + "TimeNote" + File.separator + "Download" ;
		File file = new File(cacheUrl);
		if(!file.exists())
			file.mkdir();  //如果不存在则创建
		return cacheUrl;
	}

	/**
	 * 获得笔记插入的图片地址名称
	 * @return
	 */
	public static String getNoteImageName(){
		Time time = new Time();
		time.setToNow();
		Random r = new Random();
		String imgName = time.year + "" + (time.month + 1) + ""
				+ time.monthDay + "" + time.minute + "" + time.second
				+ "" + r.nextInt(1000) + ".jpg";
		//String imagePath = getImageCacheDir() + "/" + imgName;
		return imgName;
	}

	/**
	 * 图片保存到SD卡，用户头像
	 * @param bitmap
	 * @return
	 */
	public static String saveToSdCard(Bitmap bitmap) {
		String imageUrl = getPictureDir() + getNoteImageName();
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
	 * 保存到指定路径，笔记中插入图片
	 * @param bitmap
	 * @param path
	 * @return
	 */
	public static String saveToSdCard(Bitmap bitmap, String path) {
		//String fileUrl = getImageCacheDir() + File.separator + "head" + DateUtil.getTime2FileName() + ".jpg";
		File file = new File(path);
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
		//System.out.println("文件保存路径："+ file.getAbsolutePath());
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
	 * 把图片保存到系统图库，有问题
	 * @param context
	 * @param bitmap
     */
	public static void saveImageToGallery(Context context,Bitmap bitmap) {
		// 首先保存图片
		String path = getDownloadDir();
		String fileName = getNoteImageName();
		File file = new File(path, fileName);
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

		// 其次把文件插入到系统图库
		try {
			MediaStore.Images.Media.insertImage(context.getContentResolver(),
					file.getAbsolutePath(), fileName, null);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		// 最后通知图库更新
		//context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://" + path)));
		context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,Uri.fromFile(new File(file.getPath()))));
	}

	/** 删除文件 **/
	public static void deleteFile(String filePath) {
		File file = new File(filePath);
		if (file.isFile() && file.exists())
			file.delete(); // 删除文件
	}

	/** 删除文件夹 **/
	public static boolean deleteDir(String filePath) {
		File dir = sdCardOk(filePath);
		if (dir == null || !dir.exists() || !dir.isDirectory())
			return false;

		for (File file : dir.listFiles()) {
			if (file.isFile())
				file.delete(); // 删除所有文件
			else if (file.isDirectory())
				deleteDir(file.getName()); // 递规的方式删除文件夹
		}
		return dir.delete();
	}

	/** 获得备份文件列表 **/
	public static List<String> getFileList() {
		List<String> folderList = new ArrayList<>();
		File file = sdCardOk(null);
		if (file != null) {
			File[] list = file.listFiles();
			if (list != null && list.length > 0) {
				for (int i = 0; i < list.length; i++) {
					folderList.add(list[i].getName());
				}
			}
		}
		return folderList;
	}

	public static File sdCardOk(String dir) {
		File bkFile = null;
		String state = Environment.getExternalStorageState();
		if (Environment.MEDIA_MOUNTED.equals(state)) {
			String sp = File.separator;
			String backUpPath = "";
			if (dir == null){
				backUpPath = Environment.getExternalStorageDirectory() + sp
						+ "TimeNote" + sp + "backup";
			} else {
				backUpPath = Environment.getExternalStorageDirectory() + sp
						+ "TimeNote" + sp + "backup" + sp + dir;
			}
			bkFile = new File(backUpPath);
			if (!bkFile.exists()) {
				bkFile.mkdirs();
			} else
				return bkFile;
		}
		return bkFile;
	}
}
