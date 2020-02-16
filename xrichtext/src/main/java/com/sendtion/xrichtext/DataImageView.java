package com.sendtion.xrichtext;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import androidx.appcompat.widget.AppCompatImageView;
import android.util.AttributeSet;

/**
 * 自定义ImageView，可以存放Bitmap和Path等信息
 *
 */
public class DataImageView extends AppCompatImageView {

    private boolean showBorder = false; //是否显示边框
    private int borderColor = Color.GRAY;//边框颜色
    private int borderWidth = 5;//边框大小

    private String absolutePath;
    private Bitmap bitmap;
    private Paint paint;

    public DataImageView(Context context) {
        this(context, null);
    }

    public DataImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DataImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initData();
    }

    private void initData() {
        //画笔
        paint = new Paint();
        paint.setColor(borderColor);//设置颜色
        paint.setStrokeWidth(borderWidth);//设置画笔的宽度
        paint.setStyle(Paint.Style.STROKE);//设置画笔的风格-不能设成填充FILL否则看不到图片了
    }

    public String getAbsolutePath() {
        return absolutePath;
    }

    public void setAbsolutePath(String absolutePath) {
        this.absolutePath = absolutePath;
    }

    public boolean isShowBorder() {
        return showBorder;
    }

    public void setShowBorder(boolean showBorder) {
        this.showBorder = showBorder;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    public int getBorderColor() {
        return borderColor;
    }

    public void setBorderColor(int borderColor) {
        this.borderColor = borderColor;
    }

    public int getBorderWidth() {
        return borderWidth;
    }

    public void setBorderWidth(int borderWidth) {
        this.borderWidth = borderWidth;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (showBorder) {
            //画边框
            Rect rec = canvas.getClipBounds();
            // 这两句可以使底部和右侧边框更大
            //rec.bottom -= 2;
            //rec.right -= 2;
            canvas.drawRect(rec, paint);
        }
    }
}
