package com.sendtion.xrichtext;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

/**
 * Created by sendtion on 2016/6/24.
 * 显示富文本
 */
public class RichTextView extends ScrollView {
    private static final int EDIT_PADDING = 10; // edittext常规padding是10dp

    private int viewTagIndex = 1; // 新生的view都会打一个tag，对每个view来说，这个tag是唯一的。
    private LinearLayout allLayout; // 这个是所有子view的容器，scrollView内部的唯一一个ViewGroup
    private LayoutInflater inflater;
    private int editNormalPadding = 0; //

    public RichTextView(Context context) {
        this(context, null);
    }

    public RichTextView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RichTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        inflater = LayoutInflater.from(context);

        // 1. 初始化allLayout
        allLayout = new LinearLayout(context);
        allLayout.setOrientation(LinearLayout.VERTICAL);
        //allLayout.setBackgroundColor(Color.WHITE);//去掉背景
        LayoutParams layoutParams = new LayoutParams(LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT);
        allLayout.setPadding(50, 15, 50, 15);//设置间距，防止生成图片时文字太靠边
        addView(allLayout, layoutParams);

        LinearLayout.LayoutParams firstEditParam = new LinearLayout.LayoutParams(
                LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        //editNormalPadding = dip2px(EDIT_PADDING);
        TextView firstText = createTextView("没有内容", dip2px(context, EDIT_PADDING));
        allLayout.addView(firstText, firstEditParam);
    }

    public int dip2px(Context context, float dipValue) {
        float m = context.getResources().getDisplayMetrics().density;
        return (int) (dipValue * m + 0.5f);
    }

    /**
     * 清除所有的view
     */
    public void clearAllLayout() {
        allLayout.removeAllViews();
    }

    /**
     * 获得最后一个子view的位置
     *
     * @return
     */
    public int getLastIndex() {
        int lastEditIndex = allLayout.getChildCount();
        return lastEditIndex;
    }

    /**
     * 生成文本输入框
     */
    public TextView createTextView(String hint, int paddingTop) {
        TextView textView = (TextView) inflater.inflate(R.layout.rich_textview, null);
        textView.setTag(viewTagIndex++);
        textView.setPadding(editNormalPadding, paddingTop, editNormalPadding, paddingTop);
        textView.setHint(hint);
        return textView;
    }

    /**
     * 生成图片View
     */
    private RelativeLayout createImageLayout() {
        RelativeLayout layout = (RelativeLayout) inflater.inflate(
                R.layout.edit_imageview, null);
        layout.setTag(viewTagIndex++);
        View closeView = layout.findViewById(R.id.image_close);
        closeView.setVisibility(GONE);
        return layout;
    }

    /**
     * 在特定位置插入EditText
     *
     * @param index   位置
     * @param editStr EditText显示的文字
     */
    public void addTextViewAtIndex(final int index, CharSequence editStr) {
        TextView textView = createTextView("", EDIT_PADDING);
        textView.setText(editStr);

        allLayout.addView(textView, index);
    }

    /**
     * 在特定位置添加ImageView
     */
    public void addImageViewAtIndex(final int index, String imagePath) {
        Bitmap bmp = BitmapFactory.decodeFile(imagePath);

        final RelativeLayout imageLayout = createImageLayout();
        DataImageView imageView = (DataImageView) imageLayout.findViewById(R.id.edit_imageView);
        Glide.with(getContext()).load(imagePath).crossFade().centerCrop().into(imageView);
        //imageView.setImageBitmap(bmp);//这里改用Glide加载图片
        //imageView.setBitmap(bmp);//这句去掉，保留下面的图片地址即可，优化图片占用
        imageView.setAbsolutePath(imagePath);

        // 调整imageView的高度
        int imageHeight = 500;
        if (bmp != null) {
            imageHeight = allLayout.getWidth() * bmp.getHeight() / bmp.getWidth();
            // 使用之后，还是回收掉吧
            bmp.recycle();
        }
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
                LayoutParams.MATCH_PARENT, imageHeight);
        lp.bottomMargin = 10;
        imageView.setLayoutParams(lp);

        allLayout.addView(imageLayout, index);
    }

    /**
     * 根据view的宽度，动态缩放bitmap尺寸
     *
     * @param width view的宽度
     */
    public Bitmap getScaledBitmap(String filePath, int width) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filePath, options);
        int sampleSize = options.outWidth > width ? options.outWidth / width
                + 1 : 1;
        options.inJustDecodeBounds = false;
        options.inSampleSize = sampleSize;
        return BitmapFactory.decodeFile(filePath, options);
    }

}
