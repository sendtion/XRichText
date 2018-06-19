package com.sendtion.xrichtext;

import android.animation.LayoutTransition;
import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
//import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.FutureTarget;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.Transition;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

/**
 * Created by sendtion on 2016/6/24.
 * 显示富文本
 */
public class RichTextView extends ScrollView {
    private Activity activity;
    private static final int EDIT_PADDING = 10; // edittext常规padding是10dp
    //private static final int EDIT_FIRST_PADDING_TOP = 10; // 第一个EditText的paddingTop值

    private int viewTagIndex = 1; // 新生的view都会打一个tag，对每个view来说，这个tag是唯一的。
    private LinearLayout allLayout; // 这个是所有子view的容器，scrollView内部的唯一一个ViewGroup
    private LayoutInflater inflater;
    private TextView lastFocusText; // 最近被聚焦的TextView
    private LayoutTransition mTransitioner; // 只在图片View添加或remove时，触发transition动画
    private int editNormalPadding = 0; //
    private int disappearingImageIndex = 0;
    //private Bitmap bmp;
    private OnClickListener btnListener;//图片点击事件
    private ArrayList<String> imagePaths;//图片地址集合

    /** 自定义属性 **/
    //插入的图片显示高度
    private int rtImageHeight = 0; //为0显示原始高度
    //两张相邻图片间距
    private int rtImageBottom = 10;
    //图片是否显示边框，以及边框宽度和颜色
    private boolean rtShowBorder = false;
    private int rtImageBorderWidth = 2;
    private int rtImageBorderColor = getResources().getColor(R.color.grey_600);
    //文字相关属性，初始提示信息，文字大小和颜色
    private String rtTextInitHint = "没有内容";
    //getResources().getDimensionPixelSize(R.dimen.text_size_16)
    private int rtTextSize = 16;
    private int rtTextColor = getResources().getColor(R.color.grey_600);

    public RichTextView(Context context) {
        this(context, null);
    }

    public RichTextView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RichTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        //获取自定义属性
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.RichTextView);
        rtImageHeight = ta.getInteger(R.styleable.RichTextView_rt_view_image_height, 0);
        rtImageBottom = ta.getInteger(R.styleable.RichTextView_rt_view_image_bottom, 10);
        rtShowBorder = ta.getBoolean(R.styleable.RichTextView_rt_view_show_border, false);
        rtImageBorderWidth = ta.getInteger(R.styleable.RichTextView_rt_view_image_border_width, 2);
        rtImageBorderColor = ta.getColor(R.styleable.RichTextView_rt_view_image_border_color, getResources().getColor(R.color.grey_600));
        //rtTextSize = ta.getDimensionPixelSize(R.styleable.RichTextView_rt_view_text_size, getResources().getDimensionPixelSize(R.dimen.text_size_16));
        rtTextSize = ta.getInteger(R.styleable.RichTextView_rt_view_text_size, 16);
        rtTextColor = ta.getColor(R.styleable.RichTextView_rt_view_text_color, getResources().getColor(R.color.grey_600));
        rtTextInitHint = ta.getString(R.styleable.RichTextView_rt_view_text_init_hint);

        ta.recycle();

        activity = (Activity) context;
        imagePaths = new ArrayList<>();

        inflater = LayoutInflater.from(context);

        // 1. 初始化allLayout
        allLayout = new LinearLayout(context);
        allLayout.setOrientation(LinearLayout.VERTICAL);
        //allLayout.setBackgroundColor(Color.WHITE);//去掉背景
        //setupLayoutTransitions();//禁止载入动画
        LayoutParams layoutParams = new LayoutParams(LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT);
        allLayout.setPadding(50,15,50,15);//设置间距，防止生成图片时文字太靠边
        addView(allLayout, layoutParams);

        btnListener = new OnClickListener() {

            @Override
            public void onClick(View v) {
                //Toast.makeText(getContext(),"点击图片",Toast.LENGTH_SHORT).show();
//                int currentItem = 0;
//                //点击图片预览
//                PhotoPreview.builder()
//                        .setPhotos(imagePaths)
//                        .setCurrentItem(currentItem)
//                        .setShowDeleteButton(false)
//                        .start(activity);
            }
        };

        LinearLayout.LayoutParams firstEditParam = new LinearLayout.LayoutParams(
                LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        //editNormalPadding = dip2px(EDIT_PADDING);
        TextView firstText = createTextView(rtTextInitHint, dip2px(context, EDIT_PADDING));
        allLayout.addView(firstText, firstEditParam);
        lastFocusText = firstText;
    }

    private int dip2px(Context context, float dipValue) {
        float m = context.getResources().getDisplayMetrics().density;
        return (int) (dipValue * m + 0.5f);
    }

    /**
     * 清除所有的view
     */
    public void clearAllLayout(){
        allLayout.removeAllViews();
    }

    /**
     * 获得最后一个子view的位置
     * @return
     */
    public int getLastIndex(){
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
        //textView.setTextSize(getResources().getDimensionPixelSize(R.dimen.text_size_16));
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, rtTextSize);
        textView.setTextColor(rtTextColor);
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
        DataImageView imageView = layout.findViewById(R.id.edit_imageView);
        //imageView.setTag(layout.getTag());
        if (rtShowBorder){//画出图片边框
            imageView.setBorderWidth(rtImageBorderWidth);
            imageView.setBorderColor(rtImageBorderColor);
            //imageView.requestLayout();
            imageView.postInvalidate();
        }
		imageView.setOnClickListener(btnListener);
        return layout;
    }

    /**
     * 在特定位置插入EditText
     *
     * @param index
     *            位置
     * @param editStr
     *            EditText显示的文字
     */
    public void addTextViewAtIndex(final int index, CharSequence editStr) {
        TextView textView = createTextView("", EDIT_PADDING);
        textView.setText(editStr);

        // 请注意此处，EditText添加、或删除不触动Transition动画
        //allLayout.setLayoutTransition(null);
        allLayout.addView(textView, index);
        //allLayout.setLayoutTransition(mTransitioner); // remove之后恢复transition动画
    }

    /**
     * 在特定位置添加ImageView
     */
    public void addImageViewAtIndex(final int index, final String imagePath) {
        imagePaths.add(imagePath);
        RelativeLayout imageLayout = createImageLayout();
        final DataImageView imageView = (DataImageView) imageLayout.findViewById(R.id.edit_imageView);
        if (rtShowBorder){//画出图片边框
            imageView.setBorderWidth(rtImageBorderWidth);
            imageView.setBorderColor(rtImageBorderColor);
            //imageView.requestLayout();
            imageView.postInvalidate();
        }
        imageView.setAbsolutePath(imagePath);

        //如果是网络图片
        if (imagePath.startsWith("http://") || imagePath.startsWith("https://")){

            GlideApp.with(getContext()).asBitmap().load(imagePath).dontAnimate()
                    .into(new SimpleTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                            //调整imageView的高度，根据宽度等比获得高度
                            int imageHeight = allLayout.getWidth() * resource.getHeight() / resource.getWidth();
                            RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
                                    LayoutParams.MATCH_PARENT, imageHeight);//固定图片高度，记得设置裁剪剧中
                            lp.bottomMargin = 10;
                            imageView.setLayoutParams(lp);

                            imageView.setScaleType(ImageView.ScaleType.FIT_XY);
                            imageView.setImageBitmap(resource);
                            // 不能使用centerCrop，否则图片显示不全
//							GlideApp.with(getContext()).load(imagePath)
//									.placeholder(R.drawable.img_load_fail).error(R.drawable.img_load_fail)
//									.override(Target.SIZE_ORIGINAL, imageHeight).into(imageView);
                        }
                    });
        } else { //如果是本地图片

            // 调整imageView的高度，根据宽度等比获得高度
            Bitmap bmp = BitmapFactory.decodeFile(imagePath);
            if (rtImageHeight == 0) {
                rtImageHeight = allLayout.getWidth() * bmp.getHeight() / bmp.getWidth();
            }
            RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
                    LayoutParams.MATCH_PARENT, rtImageHeight);//固定图片高度，记得设置裁剪剧中
            lp.bottomMargin = rtImageBottom;
            imageView.setLayoutParams(lp);

            imageView.setScaleType(ImageView.ScaleType.FIT_XY);
            GlideApp.with(getContext()).load(imagePath)
                    .placeholder(R.drawable.img_load_fail).error(R.drawable.img_load_fail).into(imageView);
        }

        // onActivityResult无法触发动画，此处post处理
        allLayout.addView(imageLayout, index);
    }

    /**
     * 根据view的宽度，动态缩放bitmap尺寸
     *
     * @param width
     *            view的宽度
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

    /**
     * 初始化transition动画
     */
    private void setupLayoutTransitions() {
        mTransitioner = new LayoutTransition();
        //allLayout.setLayoutTransition(mTransitioner);
        mTransitioner.addTransitionListener(new LayoutTransition.TransitionListener() {

            @Override
            public void startTransition(LayoutTransition transition,
                                        ViewGroup container, View view, int transitionType) {

            }

            @Override
            public void endTransition(LayoutTransition transition,
                                      ViewGroup container, View view, int transitionType) {
                if (!transition.isRunning()
                        && transitionType == LayoutTransition.CHANGE_DISAPPEARING) {
                    // transition动画结束，合并EditText
                    // mergeEditText();
                }
            }
        });
        mTransitioner.setDuration(300);
    }

}
