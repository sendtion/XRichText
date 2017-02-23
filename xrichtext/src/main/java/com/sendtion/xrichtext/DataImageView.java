package com.sendtion.xrichtext;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * 这只是一个简单的ImageView，可以存放Bitmap和Path等信息
 *
 * @author xmuSistone
 */
public class DataImageView extends ImageView {

    private String absolutePath;
    private Bitmap bitmap;

    public DataImageView(Context context) {
        this(context, null);
    }

    public DataImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DataImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public String getAbsolutePath() {
        return absolutePath;
    }

    public void setAbsolutePath(String absolutePath) {
        this.absolutePath = absolutePath;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

}
