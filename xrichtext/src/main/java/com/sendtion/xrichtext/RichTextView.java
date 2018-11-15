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
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.CharacterStyle;
import android.text.style.ForegroundColorSpan;
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
import android.widget.Toast;

import com.bumptech.glide.Glide;
//import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.FutureTarget;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.Transition;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
    private String keywords;//关键词高亮

    private OnRtImageClickListener onRtImageClickListener;

    /** 自定义属性 **/
    //插入的图片显示高度
    private int rtImageHeight = 0; //为0显示原始高度
    //两张相邻图片间距
    private int rtImageBottom = 10;
    //文字相关属性，初始提示信息，文字大小和颜色
    private String rtTextInitHint = "没有内容";
    //getResources().getDimensionPixelSize(R.dimen.text_size_16)
    private int rtTextSize = 16; //相当于16sp
    private int rtTextColor = Color.parseColor("#757575");
    private int rtTextLineSpace = 8; //相当于8dp

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
        rtTextSize = ta.getDimensionPixelSize(R.styleable.RichTextView_rt_view_text_size, 16);
        //rtTextSize = ta.getInteger(R.styleable.RichTextView_rt_view_text_size, 16);
        rtTextLineSpace = ta.getDimensionPixelSize(R.styleable.RichTextView_rt_view_text_line_space, 8);
        rtTextColor = ta.getColor(R.styleable.RichTextView_rt_view_text_color, Color.parseColor("#757575"));
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
                if (v instanceof DataImageView){
                    DataImageView imageView = (DataImageView) v;
                    //int currentItem = imagePaths.indexOf(imageView.getAbsolutePath());
                    //Toast.makeText(getContext(),"点击图片："+currentItem+"："+imageView.getAbsolutePath(), Toast.LENGTH_SHORT).show();
                    // 开放图片点击接口
                    if (onRtImageClickListener != null){
                        onRtImageClickListener.onRtImageClick(imageView.getAbsolutePath());
                    }

                    //点击图片预览
//                    PhotoPreview.builder()
//                            .setPhotos(imagePaths)
//                            .setCurrentItem(currentItem)
//                            .setShowDeleteButton(false)
//                            .start(activity);
                }
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

    public interface OnRtImageClickListener{
        void onRtImageClick(String imagePath);
    }

    public void setOnRtImageClickListener(OnRtImageClickListener onRtImageClickListener) {
        this.onRtImageClickListener = onRtImageClickListener;
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
        textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, rtTextSize);
        textView.setLineSpacing(rtTextLineSpace, 1.0f);
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
		imageView.setOnClickListener(btnListener);
        return layout;
    }

    /**
     * 关键字高亮显示
     * @param target  需要高亮的关键字
     * @param text	     需要显示的文字
     * @return spannable 处理完后的结果，记得不要toString()，否则没有效果
     * SpannableStringBuilder textString = TextUtilTools.highlight(item.getItemName(), KnowledgeActivity.searchKey);
     * vHolder.tv_itemName_search.setText(textString);
     */
    public static SpannableStringBuilder highlight(String text, String target) {
        SpannableStringBuilder spannable = new SpannableStringBuilder(text);
        CharacterStyle span;

        Pattern p = Pattern.compile(target);
        Matcher m = p.matcher(text);
        while (m.find()) {
            span = new ForegroundColorSpan(Color.parseColor("#EE5C42"));// 需要重复！
            spannable.setSpan(span, m.start(), m.end(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        return spannable;
    }

    public void setKeywords(String keywords) {
        this.keywords = keywords;
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
        if (!TextUtils.isEmpty(keywords)) {//搜索关键词高亮
            SpannableStringBuilder textStr = highlight(editStr.toString(), keywords);
            textView.setText(textStr);
        } else {
            textView.setText(editStr);
        }

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
        imageView.setAbsolutePath(imagePath);

        //如果是网络图片
        if (imagePath.startsWith("http://") || imagePath.startsWith("https://")){

            GlideApp.with(getContext()).asBitmap().load(imagePath).dontAnimate()
                    .into(new SimpleTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                            //调整imageView的高度，根据宽度等比获得高度
                            int imageHeight ; //解决连续加载多张图片导致后续图片都跟第一张高度相同的问题
                            if (rtImageHeight > 0) {
                                imageHeight = rtImageHeight;
                            } else {
                                imageHeight = allLayout.getWidth() * resource.getHeight() / resource.getWidth();
                            }
                            RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
                                    LayoutParams.MATCH_PARENT, imageHeight);//固定图片高度，记得设置裁剪剧中
                            lp.bottomMargin = rtImageBottom;
                            imageView.setLayoutParams(lp);

                            if (rtImageHeight > 0) {
                                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                            } else {
                                imageView.setScaleType(ImageView.ScaleType.FIT_XY);
                            }
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
            int imageHeight ; //解决连续加载多张图片导致后续图片都跟第一张高度相同的问题
            if (rtImageHeight > 0) {
                imageHeight = rtImageHeight;
            } else {
                imageHeight = allLayout.getWidth() * bmp.getHeight() / bmp.getWidth();
            }
            RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
                    LayoutParams.MATCH_PARENT, imageHeight);//固定图片高度，记得设置裁剪剧中
            lp.bottomMargin = rtImageBottom;
            imageView.setLayoutParams(lp);

            if (rtImageHeight > 0){
                GlideApp.with(getContext()).load(imagePath).centerCrop()
                        .placeholder(R.drawable.img_load_fail).error(R.drawable.img_load_fail).into(imageView);
            } else {
                GlideApp.with(getContext()).load(imagePath)
                        .placeholder(R.drawable.img_load_fail).error(R.drawable.img_load_fail).into(imageView);
            }
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

    public int getRtImageHeight() {
        return rtImageHeight;
    }

    public void setRtImageHeight(int rtImageHeight) {
        this.rtImageHeight = rtImageHeight;
    }

    public int getRtImageBottom() {
        return rtImageBottom;
    }

    public void setRtImageBottom(int rtImageBottom) {
        this.rtImageBottom = rtImageBottom;
    }

    public String getRtTextInitHint() {
        return rtTextInitHint;
    }

    public void setRtTextInitHint(String rtTextInitHint) {
        this.rtTextInitHint = rtTextInitHint;
    }

    public int getRtTextSize() {
        return rtTextSize;
    }

    public void setRtTextSize(int rtTextSize) {
        this.rtTextSize = rtTextSize;
    }

    public int getRtTextColor() {
        return rtTextColor;
    }

    public void setRtTextColor(int rtTextColor) {
        this.rtTextColor = rtTextColor;
    }

    public int getRtTextLineSpace() {
        return rtTextLineSpace;
    }

    public void setRtTextLineSpace(int rtTextLineSpace) {
        this.rtTextLineSpace = rtTextLineSpace;
    }
}
