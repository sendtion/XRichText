package com.sendtion.xrichtext;

import android.animation.LayoutTransition;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.CharacterStyle;
import android.text.style.ForegroundColorSpan;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by sendtion on 2016/6/24.
 *
 * 编辑富文本
 * 
 */
@SuppressLint({ "NewApi", "InflateParams" })
public class RichTextEditor extends ScrollView {
	private static final int EDIT_PADDING = 10; // edittext常规padding是10dp
	//private static final int EDIT_FIRST_PADDING_TOP = 10; // 第一个EditText的paddingTop值
	//private static final int BOTTOM_MARGIN = 10;

	private int viewTagIndex = 1; // 新生的view都会打一个tag，对每个view来说，这个tag是唯一的。
	private LinearLayout allLayout; // 这个是所有子view的容器，scrollView内部的唯一一个ViewGroup
	private LayoutInflater inflater;
	private OnKeyListener keyListener; // 所有EditText的软键盘监听器
	private OnClickListener btnListener; // 图片右上角红叉按钮监听器
	private OnFocusChangeListener focusListener; // 所有EditText的焦点监听listener
	private EditText lastFocusEdit; // 最近被聚焦的EditText
	private LayoutTransition mTransitioner; // 只在图片View添加或remove时，触发transition动画
	private int editNormalPadding = 0; //
	private int disappearingImageIndex = 0;
	//private Bitmap bmp;
	private ArrayList<String> imagePaths;//图片地址集合
	private String keywords;//关键词高亮

	/** 自定义属性 **/
	//插入的图片显示高度
	private int rtImageHeight = 500;
	//两张相邻图片间距
    private int rtImageBottom = 10;
	//文字相关属性，初始提示信息，文字大小和颜色
    private String rtTextInitHint = "请输入内容";
    private int rtTextSize = 16;
    private int rtTextColor = Color.parseColor("#757575");
	private int rtTextLineSpace = 8;

	//删除图片的接口
	private OnRtImageDeleteListener onRtImageDeleteListener;
	private OnRtImageClickListener onRtImageClickListener;

	public RichTextEditor(Context context) {
		this(context, null);
	}

	public RichTextEditor(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public RichTextEditor(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);

		//获取自定义属性
		TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.RichTextEditor);
		rtImageHeight = ta.getInteger(R.styleable.RichTextEditor_rt_editor_image_height, 500);
        rtImageBottom = ta.getInteger(R.styleable.RichTextEditor_rt_editor_image_bottom, 10);
        rtTextSize = ta.getDimensionPixelSize(R.styleable.RichTextEditor_rt_editor_text_size, 16);
		//rtTextSize = ta.getInteger(R.styleable.RichTextView_rt_view_text_size, 16);
		rtTextLineSpace = ta.getDimensionPixelSize(R.styleable.RichTextEditor_rt_editor_text_line_space, 8);
		rtTextColor = ta.getColor(R.styleable.RichTextEditor_rt_editor_text_color, Color.parseColor("#757575"));
        rtTextInitHint = ta.getString(R.styleable.RichTextEditor_rt_editor_text_init_hint);

		ta.recycle();

		//onDeleteImageListener = (RichTextEditor.OnDeleteImageListener) context;

		imagePaths = new ArrayList<>();

		inflater = LayoutInflater.from(context);

		// 1. 初始化allLayout
		allLayout = new LinearLayout(context);
		allLayout.setOrientation(LinearLayout.VERTICAL);
		//allLayout.setBackgroundColor(Color.WHITE);
		setupLayoutTransitions();//禁止载入动画
		LayoutParams layoutParams = new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.WRAP_CONTENT);
		allLayout.setPadding(50,15,50,15);//设置间距，防止生成图片时文字太靠边，不能用margin，否则有黑边
		addView(allLayout, layoutParams);

		// 2. 初始化键盘退格监听
		// 主要用来处理点击回删按钮时，view的一些列合并操作
		keyListener = new OnKeyListener() {

			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if (event.getAction() == KeyEvent.ACTION_DOWN
						&& event.getKeyCode() == KeyEvent.KEYCODE_DEL) {
					EditText edit = (EditText) v;
					onBackspacePress(edit);
				}
				return false;
			}
		};

		// 3. 图片叉掉处理
		btnListener = new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (v instanceof DataImageView){
					DataImageView imageView = (DataImageView)v;
					//onImageClick(imageView);
                    // 开放图片点击接口
					if (onRtImageClickListener != null){
						onRtImageClickListener.onRtImageClick(imageView, imageView.getAbsolutePath());
					}
				} else if (v instanceof ImageView){
					//Toast.makeText(getContext(),"点击关闭",Toast.LENGTH_SHORT).show();
					RelativeLayout parentView = (RelativeLayout) v.getParent();
					onImageCloseClick(parentView);
				}
			}
		};

		focusListener = new OnFocusChangeListener() {

			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (hasFocus) {
					lastFocusEdit = (EditText) v;
				}
			}
		};

		LinearLayout.LayoutParams firstEditParam = new LinearLayout.LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		//editNormalPadding = dip2px(EDIT_PADDING);
		EditText firstEdit = createEditText(rtTextInitHint, dip2px(context, EDIT_PADDING));
		allLayout.addView(firstEdit, firstEditParam);
		lastFocusEdit = firstEdit;
	}

    private int dip2px(Context context, float dipValue) {
		float m = context.getResources().getDisplayMetrics().density;
		return (int) (dipValue * m + 0.5f);
	}

	/**
	 * 处理软键盘backSpace回退事件
	 * 
	 * @param editTxt 光标所在的文本输入框
	 */
	private void onBackspacePress(EditText editTxt) {
		try {
			int startSelection = editTxt.getSelectionStart();
			// 只有在光标已经顶到文本输入框的最前方，在判定是否删除之前的图片，或两个View合并
			if (startSelection == 0) {
				int editIndex = allLayout.indexOfChild(editTxt);
				View preView = allLayout.getChildAt(editIndex - 1); // 如果editIndex-1<0,
																	// 则返回的是null
				if (null != preView) {
					if (preView instanceof RelativeLayout) {
						// 光标EditText的上一个view对应的是图片
						onImageCloseClick(preView);
					} else if (preView instanceof EditText) {
						// 光标EditText的上一个view对应的还是文本框EditText
						String str1 = editTxt.getText().toString();
						EditText preEdit = (EditText) preView;
						String str2 = preEdit.getText().toString();

						// 合并文本view时，不需要transition动画
						allLayout.setLayoutTransition(null);
						allLayout.removeView(editTxt);
						allLayout.setLayoutTransition(mTransitioner); // 恢复transition动画

						// 文本合并
						preEdit.setText(String.valueOf(str2 + str1));
						preEdit.requestFocus();
						preEdit.setSelection(str2.length(), str2.length());
						lastFocusEdit = preEdit;
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public interface OnRtImageDeleteListener{
		void onRtImageDelete(String imagePath);
	}

	public void setOnRtImageDeleteListener(OnRtImageDeleteListener onRtImageDeleteListener) {
		this.onRtImageDeleteListener = onRtImageDeleteListener;
	}

	public interface OnRtImageClickListener{
		void onRtImageClick(View view, String imagePath);
	}

	public void setOnRtImageClickListener(OnRtImageClickListener onRtImageClickListener) {
		this.onRtImageClickListener = onRtImageClickListener;
	}

	/**
	 * 处理图片叉掉的点击事件
	 * 
	 * @param view 整个image对应的relativeLayout view
	 * @type 删除类型 0代表backspace删除 1代表按红叉按钮删除
	 */
	private void onImageCloseClick(View view) {
		try {
			if (!mTransitioner.isRunning()) {
				disappearingImageIndex = allLayout.indexOfChild(view);
				//删除文件夹里的图片
				List<EditData> dataList = buildEditData();
				EditData editData = dataList.get(disappearingImageIndex);
				if (editData.imagePath != null){
					if (onRtImageDeleteListener != null){
						//TODO 通过接口回调，在笔记编辑界面处理图片的删除操作
						onRtImageDeleteListener.onRtImageDelete(editData.imagePath);
					}
					//SDCardUtil.deleteFile(editData.imagePath);
					imagePaths.remove(editData.imagePath);
				}
				allLayout.removeView(view);
				mergeEditText();//合并上下EditText内容
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 清空所有布局
	 */
	public void clearAllLayout(){
		allLayout.removeAllViews();
	}

	/**
	 * 获取索引位置
     */
	public int getLastIndex(){
		int childCount = allLayout.getChildCount();
		return childCount;
	}

	/**
	 * 生成文本输入框
	 */
	public EditText createEditText(String hint, int paddingTop) {
		EditText editText = (EditText) inflater.inflate(R.layout.rich_edittext, null);
		editText.setOnKeyListener(keyListener);
		editText.setTag(viewTagIndex++);
		editText.setPadding(editNormalPadding, paddingTop, editNormalPadding, paddingTop);
		editText.setHint(hint);
		//editText.setTextSize(rtTextSize);
		editText.setTextSize(TypedValue.COMPLEX_UNIT_PX, rtTextSize);
		editText.setTextColor(rtTextColor);
		editText.setLineSpacing(rtTextLineSpace, 1.0f);
		editText.setOnFocusChangeListener(focusListener);
		return editText;
	}

	/**
	 * 生成图片View
	 */
	private RelativeLayout createImageLayout() {
		RelativeLayout layout = (RelativeLayout) inflater.inflate(
				R.layout.edit_imageview, null);
		layout.setTag(viewTagIndex++);
		View closeView = layout.findViewById(R.id.image_close);
		//closeView.setVisibility(GONE);
		closeView.setTag(layout.getTag());
		closeView.setOnClickListener(btnListener);
		DataImageView imageView = layout.findViewById(R.id.edit_imageView);
		imageView.setOnClickListener(btnListener);
		return layout;
	}

	/**
	 * 插入一张图片
	 */
	public void insertImage(String imagePath) {
		//bitmap == null时，可能是网络图片，不能做限制
		if (TextUtils.isEmpty(imagePath)){
			return;
		}
		try {
			//lastFocusEdit获取焦点的EditText
			String lastEditStr = lastFocusEdit.getText().toString();
			int cursorIndex = lastFocusEdit.getSelectionStart();//获取光标所在位置
			String editStr1 = lastEditStr.substring(0, cursorIndex).trim();//获取光标前面的字符串
			String editStr2 = lastEditStr.substring(cursorIndex).trim();//获取光标后的字符串
			int lastEditIndex = allLayout.indexOfChild(lastFocusEdit);//获取焦点的EditText所在位置

			if (lastEditStr.length() == 0) {
				//如果当前获取焦点的EditText为空，直接在EditText下方插入图片，并且插入空的EditText
				addEditTextAtIndex(lastEditIndex + 1, "");
				addImageViewAtIndex(lastEditIndex + 1, imagePath);
			} else if (editStr1.length() == 0) {
				//如果光标已经顶在了editText的最前面，则直接插入图片，并且EditText下移即可
				addImageViewAtIndex(lastEditIndex, imagePath);
				//同时插入一个空的EditText，防止插入多张图片无法写文字
				addEditTextAtIndex(lastEditIndex + 1, "");
			} else if (editStr2.length() == 0) {
				// 如果光标已经顶在了editText的最末端，则需要添加新的imageView和EditText
				addEditTextAtIndex(lastEditIndex + 1, "");
				addImageViewAtIndex(lastEditIndex + 1, imagePath);
			} else {
				//如果光标已经顶在了editText的最中间，则需要分割字符串，分割成两个EditText，并在两个EditText中间插入图片
				//把光标前面的字符串保留，设置给当前获得焦点的EditText（此为分割出来的第一个EditText）
				lastFocusEdit.setText(editStr1);
				//把光标后面的字符串放在新创建的EditText中（此为分割出来的第二个EditText）
				addEditTextAtIndex(lastEditIndex + 1, editStr2);
				//在第二个EditText的位置插入一个空的EditText，以便连续插入多张图片时，有空间写文字，第二个EditText下移
				addEditTextAtIndex(lastEditIndex + 1, "");
				//在空的EditText的位置插入图片布局，空的EditText下移
				addImageViewAtIndex(lastEditIndex + 1, imagePath);
			}
			hideKeyBoard();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 隐藏小键盘
	 */
	public void hideKeyBoard() {
		InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
		if (imm != null && lastFocusEdit != null) {
			imm.hideSoftInputFromWindow(lastFocusEdit.getWindowToken(), 0);
		}
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
		try {
			Pattern p = Pattern.compile(target);
			Matcher m = p.matcher(text);
			while (m.find()) {
				span = new ForegroundColorSpan(Color.parseColor("#EE5C42"));// 需要重复！
				spannable.setSpan(span, m.start(), m.end(),
						Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
			}
		} catch (Exception e) {
			e.printStackTrace();
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
	public void addEditTextAtIndex(final int index, CharSequence editStr) {
		try {
			EditText editText2 = createEditText("插入文字", EDIT_PADDING);
			if (!TextUtils.isEmpty(keywords)) {//搜索关键词高亮
				SpannableStringBuilder textStr = highlight(editStr.toString(), keywords);
				editText2.setText(textStr);
			} else if (!TextUtils.isEmpty(editStr)) {//判断插入的字符串是否为空，如果没有内容则显示hint提示信息
				editText2.setText(editStr);
			}
			editText2.setOnFocusChangeListener(focusListener);

			// 请注意此处，EditText添加、或删除不触动Transition动画
			allLayout.setLayoutTransition(null);
			allLayout.addView(editText2, index);
			allLayout.setLayoutTransition(mTransitioner); // remove之后恢复transition动画
			//插入新的EditText之后，修改lastFocusEdit的指向
			lastFocusEdit = editText2;
			lastFocusEdit.requestFocus();
			lastFocusEdit.setSelection(editStr.length(), editStr.length());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 在特定位置添加ImageView
	 */
	public void addImageViewAtIndex(final int index, final String imagePath) {
		if (TextUtils.isEmpty(imagePath)){
			return;
		}
		try {
			imagePaths.add(imagePath);
			final RelativeLayout imageLayout = createImageLayout();
			DataImageView imageView = imageLayout.findViewById(R.id.edit_imageView);
			imageView.setAbsolutePath(imagePath);
			//imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);//裁剪剧中
			XRichText.getInstance().loadImage(imagePath, imageView, rtImageHeight);

//			// 调整imageView的高度，根据宽度等比获得高度
//			int imageHeight ; //解决连续加载多张图片导致后续图片都跟第一张高度相同的问题
//			if (rtImageHeight > 0) {
//				imageHeight = rtImageHeight;
//			} else {
//				Bitmap bmp = BitmapFactory.decodeFile(imagePath);
//				int layoutWidth = allLayout.getWidth() - allLayout.getPaddingLeft() - allLayout.getPaddingRight();
//				imageHeight = layoutWidth * bmp.getHeight() / bmp.getWidth();
//				//imageHeight = allLayout.getWidth() * bmp.getHeight() / bmp.getWidth();
//			}
//			RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
//					LayoutParams.MATCH_PARENT, imageHeight);//固定图片高度，记得设置裁剪剧中
//			lp.bottomMargin = rtImageBottom;
//			imageView.setLayoutParams(lp);
//
//			if (rtImageHeight > 0){
//				XRichText.getInstance().loadImage(imagePath, imageView, true);
//			} else {
//				XRichText.getInstance().loadImage(imagePath, imageView, false);
//			}

			// onActivityResult无法触发动画，此处post处理
			allLayout.addView(imageLayout, index);
//			allLayout.postDelayed(new Runnable() {
//				@Override
//				public void run() {
//					allLayout.addView(imageLayout, index);
//				}
//			}, 200);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/**
	 * 根据view的宽度，动态缩放bitmap尺寸
	 * 
	 * @param width
	 *            view的宽度
	 */
	public Bitmap getScaledBitmap(String filePath, int width) {
		if (TextUtils.isEmpty(filePath)){
			return null;
		}
		BitmapFactory.Options options = new BitmapFactory.Options();
		try {
			options.inJustDecodeBounds = true;
			BitmapFactory.decodeFile(filePath, options);
			int sampleSize = options.outWidth > width ? options.outWidth / width
					+ 1 : 1;
			options.inJustDecodeBounds = false;
			options.inSampleSize = sampleSize;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return BitmapFactory.decodeFile(filePath, options);
	}

	/**
	 * 初始化transition动画
	 */
	private void setupLayoutTransitions() {
		mTransitioner = new LayoutTransition();
		allLayout.setLayoutTransition(mTransitioner);
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
					 mergeEditText();
				}
			}
		});
		mTransitioner.setDuration(300);
	}

	/**
	 * 图片删除的时候，如果上下方都是EditText，则合并处理
	 */
	private void mergeEditText() {
		try {
			View preView = allLayout.getChildAt(disappearingImageIndex - 1);
			View nextView = allLayout.getChildAt(disappearingImageIndex);
			if (preView instanceof EditText && nextView instanceof EditText) {
				Log.d("LeiTest", "合并EditText");
				EditText preEdit = (EditText) preView;
				EditText nextEdit = (EditText) nextView;
				String str1 = preEdit.getText().toString();
				String str2 = nextEdit.getText().toString();
				String mergeText = "";
				if (str2.length() > 0) {
					mergeText = str1 + "\n" + str2;
				} else {
					mergeText = str1;
				}

				allLayout.setLayoutTransition(null);
				allLayout.removeView(nextEdit);
				preEdit.setText(mergeText);
				preEdit.requestFocus();
				preEdit.setSelection(str1.length(), str1.length());
				allLayout.setLayoutTransition(mTransitioner);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 对外提供的接口, 生成编辑数据上传
	 */
	public List<EditData> buildEditData() {
		List<EditData> dataList = new ArrayList<EditData>();
		try {
			int num = allLayout.getChildCount();
			for (int index = 0; index < num; index++) {
				View itemView = allLayout.getChildAt(index);
				EditData itemData = new EditData();
				if (itemView instanceof EditText) {
					EditText item = (EditText) itemView;
					itemData.inputStr = item.getText().toString();
				} else if (itemView instanceof RelativeLayout) {
					DataImageView item = (DataImageView) itemView.findViewById(R.id.edit_imageView);
					itemData.imagePath = item.getAbsolutePath();
					//itemData.bitmap = item.getBitmap();//去掉这个防止bitmap一直被占用，导致内存溢出
				}
				dataList.add(itemData);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return dataList;
	}

	public class EditData {
		public String inputStr;
		public String imagePath;
		public Bitmap bitmap;
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
