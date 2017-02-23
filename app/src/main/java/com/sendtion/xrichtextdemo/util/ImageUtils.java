package com.sendtion.xrichtextdemo.util;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.util.Base64;
import android.util.Log;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;

/**
 * Created by sendtion on 2016/5/25.
 */
public class ImageUtils {

    /**
     * 图片压缩处理，size参数为压缩比，比如size为2，则压缩为1/4
     **/
    public static Bitmap compressBitmap(String path, byte[] data, Context context, Uri uri, int size, boolean width) {
        BitmapFactory.Options options = null;
        if (size > 0) {
            BitmapFactory.Options info = new BitmapFactory.Options();
            /**如果设置true的时候，decode时候Bitmap返回的为数据将空*/
            info.inJustDecodeBounds = false;
            decodeBitmap(path, data, context, uri, info);
            int dim = info.outWidth;
            if (!width) dim = Math.max(dim, info.outHeight);
            options = new BitmapFactory.Options();
            /**把图片宽高读取放在Options里*/
            options.inSampleSize = size;
        }
        Bitmap bm = null;
        try {
            bm = decodeBitmap(path, data, context, uri, options);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bm;
    }


    /**
     * 把byte数据解析成图片
     */
    private static Bitmap decodeBitmap(String path, byte[] data, Context context, Uri uri, BitmapFactory.Options options) {
        Bitmap result = null;
        if (path != null) {
            result = BitmapFactory.decodeFile(path, options);
        } else if (data != null) {
            result = BitmapFactory.decodeByteArray(data, 0, data.length, options);
        } else if (uri != null) {
            ContentResolver cr = context.getContentResolver();
            InputStream inputStream = null;
            try {
                inputStream = cr.openInputStream(uri);
                result = BitmapFactory.decodeStream(inputStream, null, options);
                inputStream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return result;
    }


    /**
     * 把bitmap转换成String
     *
     * @param filePath
     * @return
     */
    public static String bitmapToString(String filePath) {

        Bitmap bm = getSmallBitmap(filePath, 480, 800);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.JPEG, 40, baos);
        byte[] b = baos.toByteArray();

        return Base64.encodeToString(b, Base64.DEFAULT);

    }

    /**
     * 计算图片的缩放值
     *
     * @param options
     * @param reqWidth
     * @param reqHeight
     * @return
     */
    public static int calculateInSampleSize(BitmapFactory.Options options,
                                            int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            // Calculate ratios of height and width to requested height and
            // width
            final int heightRatio = Math.round((float) height / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);

            // Choose the smallest ratio as inSampleSize value, this will
            // guarantee
            // a final image with both dimensions larger than or equal to the
            // requested height and width.
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
        }

        return inSampleSize;
    }

    /**
     * 根据路径获得突破并压缩返回bitmap用于显示
     *
     * @return
     */
    public static Bitmap getSmallBitmap(String filePath, int newWidth, int newHeight) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filePath, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, newWidth, newHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;

        Bitmap bitmap = BitmapFactory.decodeFile(filePath, options);
        Bitmap newBitmap = compressImage(bitmap, 500);
        if (bitmap != null){
            bitmap.recycle();
        }
        return newBitmap;
    }

    /**
     * 根据view的宽度，动态缩放bitmap尺寸
     *
     * @param width
     *            view的宽度
     */
//    public Bitmap getScaledBitmap(String filePath, int width) {
//        BitmapFactory.Options options = new BitmapFactory.Options();
//        options.inJustDecodeBounds = true;
//        BitmapFactory.decodeFile(filePath, options);
//        int sampleSize = options.outWidth > width ? options.outWidth / width
//                + 1 : 1;
//        options.inJustDecodeBounds = false;
//        options.inSampleSize = sampleSize;
//        return BitmapFactory.decodeFile(filePath, options);
//    }

    /**
     * 对图片进行按比例设置
     * @param bitmap 要处理的图片
     * @return 返回处理好的图片
     */
//    public static Bitmap getScaleBitmap(Bitmap bitmap, float widthScale, float heightScale){
//        Matrix matrix = new Matrix();
//        matrix.postScale(widthScale, heightScale);
//        if(bitmap == null){
//            return null;
//        }
//        Bitmap resizeBmp  =
//                Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
//        return resizeBmp;
//    }

    /**
     * 根据路径删除图片
     *
     * @param path
     */
    public static void deleteTempFile(String path) {
        File file = new File(path);
        if (file.exists()) {
            file.delete();
        }
    }

    /**
     * 添加到图库
     */
    public static void galleryAddPic(Context context, String path) {
        Intent mediaScanIntent = new Intent(
                Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(path);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        context.sendBroadcast(mediaScanIntent);
    }

    //使用Bitmap加Matrix来缩放
    public static Bitmap resizeImage(Bitmap bitmapOrg, int newWidth, int newHeight)
    {
//        Bitmap bitmapOrg = BitmapFactory.decodeFile(imagePath);
        // 获取这个图片的宽和高
        int width = bitmapOrg.getWidth();
        int height = bitmapOrg.getHeight();
        //如果宽度为0 保持原图
        if(newWidth == 0){
            newWidth = width;
            newHeight = height;
        }
        // 创建操作图片用的matrix对象
        Matrix matrix = new Matrix();
        // 计算宽高缩放率
        float scaleWidth = newWidth / width;
        float scaleHeight = newHeight / height;
        // 缩放图片动作
        matrix.postScale(scaleWidth, scaleHeight);
        Bitmap resizedBitmap = Bitmap.createBitmap(bitmapOrg, 0, 0, newWidth,
                newHeight, matrix, true);
        //Log.e("###newWidth=", resizedBitmap.getWidth()+"");
        //Log.e("###newHeight=", resizedBitmap.getHeight()+"");
        resizedBitmap = compressImage(resizedBitmap, 100);//质量压缩
        return resizedBitmap;
    }

    //使用BitmapFactory.Options的inSampleSize参数来缩放
    public static Bitmap resizeImage2(String path, int width,int height)
    {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;//不加载bitmap到内存中
        BitmapFactory.decodeFile(path,options);
        int outWidth = options.outWidth;
        int outHeight = options.outHeight;
        options.inDither = false;
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        options.inSampleSize = 1;

        if (outWidth != 0 && outHeight != 0 && width != 0 && height != 0)
        {
            int sampleSize=(outWidth/width+outHeight/height)/2;
            Log.d("###", "sampleSize = " + sampleSize);
            options.inSampleSize = sampleSize;
        }

        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(path, options);
    }

    /**
     * 通过像素压缩图片，将修改图片宽高，适合获得缩略图，Used to get thumbnail
     * @param srcPath
     * @return
     */
    public static Bitmap compressBitmapByPath(String srcPath, float pixelW, float pixelH) {
        BitmapFactory.Options newOpts = new BitmapFactory.Options();
        //开始读入图片，此时把options.inJustDecodeBounds 设回true了
        newOpts.inJustDecodeBounds = true;
        newOpts.inPreferredConfig = Bitmap.Config.RGB_565;
        Bitmap bitmap = BitmapFactory.decodeFile(srcPath,newOpts);//此时返回bm为空

        newOpts.inJustDecodeBounds = false;
        int w = newOpts.outWidth;
        int h = newOpts.outHeight;
        //现在主流手机比较多是800*480分辨率，所以高和宽我们设置为
        float hh = pixelH;//这里设置高度为800f
        float ww = pixelW;//这里设置宽度为480f
        //缩放比。由于是固定比例缩放，只用高或者宽其中一个数据进行计算即可
        int be = 1;//be=1表示不缩放
        if (w > h && w > ww) {//如果宽度大的话根据宽度固定大小缩放
            be = (int) (newOpts.outWidth / ww);
        } else if (w < h && h > hh) {//如果高度高的话根据宽度固定大小缩放
            be = (int) (newOpts.outHeight / hh);
        }
        if (be <= 0)
            be = 1;
        newOpts.inSampleSize = be;//设置缩放比例
        //重新读入图片，注意此时已经把options.inJustDecodeBounds 设回false了
        bitmap = BitmapFactory.decodeFile(srcPath, newOpts);
        //        return compress(bitmap, maxSize); // 这里再进行质量压缩的意义不大，反而耗资源，删除
        return bitmap;
    }

    /**
     * 通过大小压缩，将修改图片宽高，适合获得缩略图，Used to get thumbnail
     * @param image
     * @param pixelW
     * @param pixelH
     * @return
     */
    public static Bitmap compressBitmapByBmp(Bitmap image, float pixelW, float pixelH) {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 100, os);
        if( os.toByteArray().length / 1024>1024) {//判断如果图片大于1M,进行压缩避免在生成图片（BitmapFactory.decodeStream）时溢出
            os.reset();//重置baos即清空baos
            image.compress(Bitmap.CompressFormat.JPEG, 50, os);//这里压缩50%，把压缩后的数据存放到baos中
        }
        ByteArrayInputStream is = new ByteArrayInputStream(os.toByteArray());
        BitmapFactory.Options newOpts = new BitmapFactory.Options();
        //开始读入图片，此时把options.inJustDecodeBounds 设回true了
        newOpts.inJustDecodeBounds = true;
        newOpts.inPreferredConfig = Bitmap.Config.RGB_565;
        Bitmap bitmap = BitmapFactory.decodeStream(is, null, newOpts);
        newOpts.inJustDecodeBounds = false;
        int w = newOpts.outWidth;
        int h = newOpts.outHeight;
        float hh = pixelH;// 设置高度为240f时，可以明显看到图片缩小了
        float ww = pixelW;// 设置宽度为120f，可以明显看到图片缩小了
        //缩放比。由于是固定比例缩放，只用高或者宽其中一个数据进行计算即可
        int be = 1;//be=1表示不缩放
        if (w > h && w > ww) {//如果宽度大的话根据宽度固定大小缩放
            be = (int) (newOpts.outWidth / ww);
        } else if (w < h && h > hh) {//如果高度高的话根据宽度固定大小缩放
            be = (int) (newOpts.outHeight / hh);
        }
        if (be <= 0) be = 1;
        newOpts.inSampleSize = be;//设置缩放比例
        //重新读入图片，注意此时已经把options.inJustDecodeBounds 设回false了
        is = new ByteArrayInputStream(os.toByteArray());
        bitmap = BitmapFactory.decodeStream(is, null, newOpts);
        int desWidth = (int) (w / be);
        int desHeight = (int) (h / be);
        bitmap = Bitmap.createScaledBitmap(bitmap, desWidth, desHeight, true);
        //压缩好比例大小后再进行质量压缩
//      return compress(bitmap, maxSize); // 这里再进行质量压缩的意义不大，反而耗资源，删除
        return bitmap;
    }

    /**
     * 质量压缩
     * @param image
     * @param maxSize
     */
    public static Bitmap compressImage(Bitmap image, int maxSize){
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        // scale
        int options = 80;
        // Store the bitmap into output stream(no compress)
        image.compress(Bitmap.CompressFormat.JPEG, options, os);
        // Compress by loop
        while ( os.toByteArray().length / 1024 > maxSize) {
            // Clean up os
            os.reset();
            // interval 10
            options -= 10;
            image.compress(Bitmap.CompressFormat.JPEG, options, os);
        }

        Bitmap bitmap = null;
        byte[] b = os.toByteArray();
        if (b.length != 0) {
            bitmap = BitmapFactory.decodeByteArray(b, 0, b.length);
        }
        return bitmap;
    }


    /**
     * 对图片进行缩放
     * @param bgimage
     * @param newWidth
     * @param newHeight
     * @return
     */
    public static Bitmap zoomImage(Bitmap bgimage, double newWidth, double newHeight) {
//        //使用方式
//        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.default_img);
//        int paddingLeft = getPaddingLeft();
//        int paddingRight = getPaddingRight();
//        int bmWidth = bitmap.getWidth();//图片高度
//        int bmHeight = bitmap.getHeight();//图片宽度
//        int zoomWidth = getWidth() - (paddingLeft + paddingRight);
//        int zoomHeight = (int) (((float)zoomWidth / (float)bmWidth) * bmHeight);
//        Bitmap newBitmap = zoomImage(bitmap, zoomWidth,zoomHeight);
        // 获取这个图片的宽和高
        float width = bgimage.getWidth();
        float height = bgimage.getHeight();
        //如果宽度为0 保持原图
        if(newWidth == 0){
            newWidth = width;
            newHeight = height;
        }
        // 创建操作图片用的matrix对象
        Matrix matrix = new Matrix();
        // 计算宽高缩放率
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        // 缩放图片动作
        matrix.postScale(scaleWidth, scaleHeight);
        Bitmap bitmap = Bitmap.createBitmap(bgimage, 0, 0, (int) width,
                (int) height, matrix, true);
        bitmap = compressImage(bitmap, 100);//质量压缩
        return bitmap;
    }

}
