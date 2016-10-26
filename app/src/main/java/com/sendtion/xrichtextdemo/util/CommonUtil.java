package com.sendtion.xrichtextdemo.util;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.PowerManager;
import android.util.Log;

import java.io.File;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by sendtion on 2016/3/30.
 */
public class CommonUtil {

    /**
     * 判断应用是否处于后台
     * @param context
     * @return
     */
    public static boolean isAppOnBackground(Context context) {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> tasks = am.getRunningTasks(1);
        if (!tasks.isEmpty()) {
            ComponentName topActivity = tasks.get(0).topActivity;
            if (!topActivity.getPackageName().equals(context.getPackageName())) {
                return true;
            }
        }
        return false;
    }

    /**
     * 判断是否锁屏
     * @param context
     * @return
     */
    public static boolean isLockScreeen(Context context){
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        boolean isScreenOn = pm.isScreenOn();//如果为true，则表示屏幕“亮”了，否则屏幕“暗”了。
        if (isScreenOn){
            return false;
        } else {
            return true;
        }
    }

    /**
     * 判断是否为手机号
     */
    public static boolean isPhone(String inputText) {
        Pattern p = Pattern.compile("^((13[0-9])|(15[^4,\\D])|(18[0,5-9]))\\d{8}$");
        Matcher m = p.matcher(inputText);
        return m.matches();
    }

    /**
     * 判断格式是否为email
     */
    public static boolean isEmail(String email) {
        String str = "^([a-zA-Z0-9_\\-\\.]+)@((\\[[0-9]{1,3}\\.[0-9]" +
                "{1,3}\\.[0-9]{1,3}\\.)|(([a-zA-Z0-9\\-]+\\.)+))([a-zA-Z]{2,4}|[0-9]{1,3})(\\]?)$";
        Pattern p = Pattern.compile(str);
        Matcher m = p.matcher(email);
        return m.matches();
    }

    /**
     * 分享文字笔记
     */
    public static void shareText(Context context, String content){
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_TEXT, content);
        shareIntent.setType("text/plain");
        //设置分享列表的标题，并且每次都显示分享列表
        context.startActivity(Intent.createChooser(shareIntent, "分享到"));
    }

    /**
     * 分享单张图片
     * @param context
     * @param imagePath
     */
    public static void shareImage(Context context, String imagePath) {
        //String imagePath = Environment.getExternalStorageDirectory() + File.separator + "test.jpg";
        Uri imageUri = Uri.fromFile(new File(imagePath));//由文件得到uri
        Log.d("share", "uri:" + imageUri);  //输出：file:///storage/emulated/0/test.jpg

        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_STREAM, imageUri);
        shareIntent.setType("image/*");
        context.startActivity(Intent.createChooser(shareIntent, "分享到"));
    }

    /**
     * 分享功能
     *
     * @param context
     *            上下文
     * @param msgTitle
     *            消息标题
     * @param msgText
     *            消息内容
     * @param imgPath
     *            图片路径，不分享图片则传null
     */
    public static void shareTextAndImage(Context context, String msgTitle, String msgText, String imgPath) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        if (imgPath == null || imgPath.equals("")) {
            intent.setType("text/plain"); // 纯文本
        } else {
            File f = new File(imgPath);
            if (f != null && f.exists() && f.isFile()) {
                intent.setType("image/jpg");
                Uri u = Uri.fromFile(f);
                intent.putExtra(Intent.EXTRA_STREAM, u);
            }
        }
        intent.putExtra(Intent.EXTRA_SUBJECT, msgTitle);
        intent.putExtra(Intent.EXTRA_TEXT, msgText);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(Intent.createChooser(intent, "分享到"));
    }

}
