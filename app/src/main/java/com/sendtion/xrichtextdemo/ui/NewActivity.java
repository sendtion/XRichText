package com.sendtion.xrichtextdemo.ui;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.sendtion.xrichtext.RichTextEditor;
import com.sendtion.xrichtextdemo.R;
import com.sendtion.xrichtextdemo.bean.Group;
import com.sendtion.xrichtextdemo.bean.Note;
import com.sendtion.xrichtextdemo.db.GroupDao;
import com.sendtion.xrichtextdemo.db.NoteDao;
import com.sendtion.xrichtextdemo.util.CommonUtil;
import com.sendtion.xrichtextdemo.util.ImageUtils;
import com.sendtion.xrichtextdemo.util.MyGlideEngine;
import com.sendtion.xrichtextdemo.util.SDCardUtil;
import com.sendtion.xrichtextdemo.util.StringUtils;
import com.zhihu.matisse.Matisse;
import com.zhihu.matisse.MimeType;
import com.zhihu.matisse.internal.entity.CaptureStrategy;

import org.reactivestreams.Subscriber;

import java.util.Date;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

/**
 * 新建笔记
 */
public class NewActivity extends BaseActivity {
    private static final String TAG = "NewActivity";

    private static final int REQUEST_CODE_CHOOSE = 23;//定义请求码常量

    private EditText et_new_title;
    private RichTextEditor et_new_content;
    private TextView tv_new_time;
    private TextView tv_new_group;

    private GroupDao groupDao;
    private NoteDao noteDao;
    private Note note;//笔记对象
    private String myTitle;
    private String myContent;
    private String myGroupName;
    private String myNoteTime;
    private int flag;//区分是新建笔记还是编辑笔记

    private static final int cutTitleLength = 20;//截取的标题长度

    private ProgressDialog loadingDialog;
    private ProgressDialog insertDialog;
    private int screenWidth;
    private int screenHeight;
    private Disposable subsLoading;
    private Disposable subsInsert;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new);

        initView();

    }

    private void initView() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_new);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //toolbar.setNavigationIcon(R.drawable.ic_dialog_info);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dealwithExit();
            }
        });

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab_new);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        groupDao = new GroupDao(this);
        noteDao = new NoteDao(this);
        note = new Note();

        screenWidth = CommonUtil.getScreenWidth(this);
        screenHeight = CommonUtil.getScreenHeight(this);

        insertDialog = new ProgressDialog(this);
        insertDialog.setMessage("正在插入图片...");
        insertDialog.setCanceledOnTouchOutside(false);

        et_new_title = (EditText) findViewById(R.id.et_new_title);
        et_new_content = (RichTextEditor) findViewById(R.id.et_new_content);
        tv_new_time = (TextView) findViewById(R.id.tv_new_time);
        tv_new_group = (TextView) findViewById(R.id.tv_new_group);

        // 图片删除事件
        et_new_content.setOnRtImageDeleteListener(new RichTextEditor.OnRtImageDeleteListener() {

            @Override
            public void onRtImageDelete(String imagePath) {
                if (!TextUtils.isEmpty(imagePath)) {
                    boolean isOK = SDCardUtil.deleteFile(imagePath);
                    if (isOK) {
                        showToast("删除成功：" + imagePath);
                    }
                }
            }
        });
        // 图片点击事件
        et_new_content.setOnRtImageClickListener(new RichTextEditor.OnRtImageClickListener() {
            @Override
            public void onRtImageClick(String imagePath) {
                myContent = getEditData();
                if (!TextUtils.isEmpty(myContent)){
                    List<String> imageList = StringUtils.getTextFromHtml(myContent, true);
                    if (!TextUtils.isEmpty(imagePath)) {
                        int currentPosition = imageList.indexOf(imagePath);
                        showToast("点击图片：" + currentPosition + "：" + imagePath);
                    }
                }
            }
        });

        openSoftKeyInput();//打开软键盘显示

        Intent intent = getIntent();
        flag = intent.getIntExtra("flag", 0);//0新建，1编辑
        if (flag == 1){//编辑
            Bundle bundle = intent.getBundleExtra("data");
            note = (Note) bundle.getSerializable("note");

            myTitle = note.getTitle();
            myContent = note.getContent();
            myNoteTime = note.getCreateTime();
            Group group = groupDao.queryGroupById(note.getGroupId());
            myGroupName = group.getName();

            loadingDialog = new ProgressDialog(this);
            loadingDialog.setMessage("数据加载中...");
            loadingDialog.setCanceledOnTouchOutside(false);
            loadingDialog.show();

            setTitle("编辑笔记");
            tv_new_time.setText(note.getCreateTime());
            tv_new_group.setText(myGroupName);
            et_new_title.setText(note.getTitle());
            et_new_content.post(new Runnable() {
                @Override
                public void run() {
                    //showEditData(note.getContent());
                    et_new_content.clearAllLayout();
                    showDataSync(note.getContent());
                }
            });
        } else {
            setTitle("新建笔记");
            if (myGroupName == null || "全部笔记".equals(myGroupName)) {
                myGroupName = "默认笔记";
            }
            tv_new_group.setText(myGroupName);
            myNoteTime = CommonUtil.date2string(new Date());
            tv_new_time.setText(myNoteTime);
        }

    }

    /**
     * 关闭软键盘
     */
    private void closeSoftKeyInput(){
        InputMethodManager imm = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
        //boolean isOpen=imm.isActive();//isOpen若返回true，则表示输入法打开
        if (imm.isActive()){
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),
                    InputMethodManager.HIDE_NOT_ALWAYS);
            //imm.hideSoftInputFromInputMethod();//据说无效
            //imm.hideSoftInputFromWindow(et_content.getWindowToken(), 0); //强制隐藏键盘
            //如果输入法在窗口上已经显示，则隐藏，反之则显示
            //imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    /**
     * 打开软键盘
     */
    private void openSoftKeyInput(){
        InputMethodManager imm = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
        //boolean isOpen=imm.isActive();//isOpen若返回true，则表示输入法打开
        if (!imm.isActive()){
            et_new_content.requestFocus();
            //第二个参数可设置为0
            //imm.showSoftInput(et_content, InputMethodManager.SHOW_FORCED);//强制显示
            imm.showSoftInputFromInputMethod(getCurrentFocus().getWindowToken(),
                    InputMethodManager.SHOW_FORCED);
        }
    }

    /**
     * 异步方式显示数据
     * @param html
     */
    private void showDataSync(final String html){
        Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(ObservableEmitter<String> emitter) {
                showEditData(emitter, html);
            }
        })
        //.onBackpressureBuffer()
        .subscribeOn(Schedulers.io())//生产事件在io
        .observeOn(AndroidSchedulers.mainThread())//消费事件在UI线程
        .subscribe(new Observer<String>() {
            @Override
            public void onComplete() {
                if (loadingDialog != null){
                    loadingDialog.dismiss();
                }
                //在图片全部插入完毕后，再插入一个EditText，防止最后一张图片后无法插入文字
                et_new_content.addEditTextAtIndex(et_new_content.getLastIndex(), "");
            }

            @Override
            public void onError(Throwable e) {
                if (loadingDialog != null){
                    loadingDialog.dismiss();
                }
                showToast("解析错误：图片不存在或已损坏");
            }

            @Override
            public void onSubscribe(Disposable d) {
                subsLoading = d;
            }

            @Override
            public void onNext(String text) {
                if (text.contains("<img") && text.contains("src=")) {
                    //imagePath可能是本地路径，也可能是网络地址
                    String imagePath = StringUtils.getImgSrc(text);
                    //插入空的EditText，以便在图片前后插入文字
                    et_new_content.addEditTextAtIndex(et_new_content.getLastIndex(), "");
                    et_new_content.addImageViewAtIndex(et_new_content.getLastIndex(), imagePath);
                } else {
                    et_new_content.addEditTextAtIndex(et_new_content.getLastIndex(), text);
                }
            }
        });
    }

    /**
     * 显示数据
     */
    protected void showEditData(ObservableEmitter<String> emitter, String html) {
        try{
            List<String> textList = StringUtils.cutStringByImgTag(html);
            for (int i = 0; i < textList.size(); i++) {
                String text = textList.get(i);
                emitter.onNext(text);
            }
            emitter.onComplete();
        }catch (Exception e){
            e.printStackTrace();
            emitter.onError(e);
        }
    }

    /**
     * 负责处理编辑数据提交等事宜，请自行实现
     */
    private String getEditData() {
        List<RichTextEditor.EditData> editList = et_new_content.buildEditData();
        StringBuffer content = new StringBuffer();
        for (RichTextEditor.EditData itemData : editList) {
            if (itemData.inputStr != null) {
                content.append(itemData.inputStr);
            } else if (itemData.imagePath != null) {
                content.append("<img src=\"").append(itemData.imagePath).append("\"/>");
            }
        }
        return content.toString();
    }

    /**
     * 保存数据,=0销毁当前界面，=1不销毁界面，为了防止在后台时保存笔记并销毁，应该只保存笔记
     */
    private void saveNoteData(boolean isBackground) {
        String noteTitle = et_new_title.getText().toString();
        String noteContent = getEditData();
        String groupName = tv_new_group.getText().toString();
        String noteTime = tv_new_time.getText().toString();

        Group group = groupDao.queryGroupByName(myGroupName);
        if (group != null) {
            if (noteTitle.length() == 0 ){//如果标题为空，则截取内容为标题
                if (noteContent.length() > cutTitleLength){
                    noteTitle = noteContent.substring(0,cutTitleLength);
                } else if (noteContent.length() > 0 && noteContent.length() <= cutTitleLength){
                    noteTitle = noteContent;
                }
            }
            int groupId = group.getId();
            note.setTitle(noteTitle);
            note.setContent(noteContent);
            note.setGroupId(groupId);
            note.setGroupName(groupName);
            note.setType(2);
            note.setBgColor("#FFFFFF");
            note.setIsEncrypt(0);
            note.setCreateTime(CommonUtil.date2string(new Date()));
            if (flag == 0 ) {//新建笔记
                if (noteTitle.length() == 0 && noteContent.length() == 0) {
                    if (!isBackground){
                        Toast.makeText(NewActivity.this, "请输入内容", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    long noteId = noteDao.insertNote(note);
                    //Log.i("", "noteId: "+noteId);
                    //查询新建笔记id，防止重复插入
                    note.setId((int) noteId);
                    flag = 1;//插入以后只能是编辑
                    if (!isBackground){
                        Intent intent = new Intent();
                        setResult(RESULT_OK, intent);
                        finish();
                    }
                }
            }else if (flag == 1) {//编辑笔记
                if (!noteTitle.equals(myTitle) || !noteContent.equals(myContent)
                        || !groupName.equals(myGroupName) || !noteTime.equals(myNoteTime)) {
                    noteDao.updateNote(note);
                }
                if (!isBackground){
                    finish();
                }
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_new, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_insert_image:
                closeSoftKeyInput();//关闭软键盘
                callGallery();
                break;
            case R.id.action_new_save:
                saveNoteData(false);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * 调用图库选择
     */
    private void callGallery(){
//        //调用系统图库
//        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
//        intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");// 相片类型
//        startActivityForResult(intent, 1);

        Matisse.from(this)
                .choose(MimeType.of(MimeType.JPEG, MimeType.PNG, MimeType.GIF))//照片视频全部显示MimeType.allOf()
                .countable(true)//true:选中后显示数字;false:选中后显示对号
                .maxSelectable(3)//最大选择数量为9
                //.addFilter(new GifSizeFilter(320, 320, 5 * Filter.K * Filter.K))
                .gridExpectedSize(getResources().getDimensionPixelSize(R.dimen.grid_expected_size))//图片显示表格的大小
                .restrictOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED)//图像选择和预览活动所需的方向
                .thumbnailScale(0.85f)//缩放比例
                .theme(R.style.Matisse_Zhihu)//主题  暗色主题 R.style.Matisse_Dracula
                .imageEngine(new MyGlideEngine())//图片加载方式，Glide4需要自定义实现
                .capture(true) //是否提供拍照功能，兼容7.0系统需要下面的配置
                //参数1 true表示拍照存储在共有目录，false表示存储在私有目录；参数2与 AndroidManifest中authorities值相同，用于适配7.0系统 必须设置
                .captureStrategy(new CaptureStrategy(true,"com.sendtion.matisse.fileprovider"))//存储到哪里
                .forResult(REQUEST_CODE_CHOOSE);//请求码
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (data != null) {
                if (requestCode == 1){
                    //处理调用系统图库
                } else if (requestCode == REQUEST_CODE_CHOOSE){
                    //异步方式插入图片
                    insertImagesSync(data);
                }
            }
        }
    }

    /**
     * 异步方式插入图片
     * @param data
     */
    private void insertImagesSync(final Intent data){
        insertDialog.show();

        Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(ObservableEmitter<String> emitter) {
                try{
                    et_new_content.measure(0, 0);
                    List<Uri> mSelected = Matisse.obtainResult(data);
                    // 可以同时插入多张图片
                    for (Uri imageUri : mSelected) {
                        String imagePath = SDCardUtil.getFilePathFromUri(NewActivity.this,  imageUri);
                        //Log.e(TAG, "###path=" + imagePath);
                        Bitmap bitmap = ImageUtils.getSmallBitmap(imagePath, screenWidth, screenHeight);//压缩图片
                        //bitmap = BitmapFactory.decodeFile(imagePath);
                        imagePath = SDCardUtil.saveToSdCard(bitmap);
                        //Log.e(TAG, "###imagePath="+imagePath);
                        emitter.onNext(imagePath);
                    }

                    // 测试插入网络图片 http://p695w3yko.bkt.clouddn.com/18-5-5/44849367.jpg
                    //subscriber.onNext("http://p695w3yko.bkt.clouddn.com/18-5-5/30271511.jpg");

                    emitter.onComplete();
                }catch (Exception e){
                    e.printStackTrace();
                    emitter.onError(e);
                }
            }
        })
        //.onBackpressureBuffer()
        .subscribeOn(Schedulers.io())//生产事件在io
        .observeOn(AndroidSchedulers.mainThread())//消费事件在UI线程
        .subscribe(new Observer<String>() {
            @Override
            public void onComplete() {
                if (insertDialog != null && insertDialog.isShowing()) {
                    insertDialog.dismiss();
                }
                showToast("图片插入成功");
            }

            @Override
            public void onError(Throwable e) {
                if (insertDialog != null && insertDialog.isShowing()) {
                    insertDialog.dismiss();
                }
                showToast("图片插入失败:"+e.getMessage());
            }

            @Override
            public void onSubscribe(Disposable d) {
                subsInsert = d;
            }

            @Override
            public void onNext(String imagePath) {
                et_new_content.insertImage(imagePath, et_new_content.getMeasuredWidth());
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onStop() {
        super.onStop();
        //如果APP处于后台，或者手机锁屏，则保存数据
        if (CommonUtil.isAppOnBackground(getApplicationContext()) ||
                CommonUtil.isLockScreeen(getApplicationContext())){
            saveNoteData(true);//处于后台时保存数据
        }

        if (subsLoading != null && subsLoading.isDisposed()){
            subsLoading.dispose();
        }
        if (subsInsert != null && subsInsert.isDisposed()){
            subsInsert.dispose();
        }
    }

    /**
     * 退出处理
     */
    private void dealwithExit(){
        String noteTitle = et_new_title.getText().toString();
        String noteContent = getEditData();
        String groupName = tv_new_group.getText().toString();
        String noteTime = tv_new_time.getText().toString();
        if (flag == 0) {//新建笔记
            if (noteTitle.length() > 0 || noteContent.length() > 0) {
                saveNoteData(false);
            }
        }else if (flag == 1) {//编辑笔记
            if (!noteTitle.equals(myTitle) || !noteContent.equals(myContent)
                    || !groupName.equals(myGroupName) || !noteTime.equals(myNoteTime)) {
                saveNoteData(false);
            }
        }
        finish();
    }

    @Override
    public void onBackPressed() {
        dealwithExit();
    }

}
