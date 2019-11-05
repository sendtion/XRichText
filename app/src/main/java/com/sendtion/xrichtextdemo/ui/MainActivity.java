package com.sendtion.xrichtextdemo.ui;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.sendtion.xrichtext.IImageLoader;
import com.sendtion.xrichtext.XRichText;
import com.sendtion.xrichtextdemo.R;
import com.sendtion.xrichtextdemo.adapter.MyNoteListAdapter;
import com.sendtion.xrichtextdemo.bean.Note;
import com.sendtion.xrichtextdemo.comm.TransformationScale;
import com.sendtion.xrichtextdemo.db.NoteDao;
import com.sendtion.xrichtextdemo.view.SpacesItemDecoration;

import java.util.List;

/**
 * 作者：Sendtion on 2016/10/21 0021 16:43
 * 邮箱：sendtion@163.com
 * 博客：http://sendtion.cn
 * 描述：主界面
 */

public class MainActivity extends BaseActivity {
    private MyNoteListAdapter mNoteListAdapter;
    private List<Note> noteList;
    private NoteDao noteDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();

    }

    private void initView() {
        Toolbar toolbar = findViewById(R.id.toolbar_main);
        setSupportActionBar(toolbar);

        noteDao = new NoteDao(this);

        RecyclerView rv_list_main = findViewById(R.id.rv_list_main);
        rv_list_main.addItemDecoration(new SpacesItemDecoration(0));//设置item间距
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);//竖向列表
        rv_list_main.setLayoutManager(layoutManager);

        mNoteListAdapter = new MyNoteListAdapter();
        mNoteListAdapter.setmNotes(noteList);
        rv_list_main.setAdapter(mNoteListAdapter);

        mNoteListAdapter.setOnItemClickListener(new MyNoteListAdapter.OnRecyclerViewItemClickListener() {
            @Override
            public void onItemClick(View view, Note note) {
                //showToast(note.getTitle());

                Intent intent = new Intent(MainActivity.this, NoteActivity.class);
                Bundle bundle = new Bundle();
                bundle.putSerializable("note", note);
                intent.putExtra("data", bundle);
                startActivity(intent);
            }
        });
        mNoteListAdapter.setOnItemLongClickListener(new MyNoteListAdapter.OnRecyclerViewItemLongClickListener() {
            @Override
            public void onItemLongClick(View view, final Note note) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("提示");
                builder.setMessage("确定删除笔记？");
                builder.setCancelable(false);
                builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        int ret = noteDao.deleteNote(note.getId());
                        if (ret > 0){
                            showToast("删除成功");
                            //TODO 删除笔记成功后，记得删除图片（分为本地图片和网络图片）
                            //获取笔记中图片的列表 StringUtils.getTextFromHtml(note.getContent(), true);
                            refreshNoteList();
                        }
                    }
                });
                builder.setNegativeButton("取消", null);
                builder.create().show();
            }
        });
    }

    //刷新笔记列表
    private void refreshNoteList(){
        if (noteDao == null)
            noteDao = new NoteDao(this);
        noteList = noteDao.queryNotesAll(0);
        mNoteListAdapter.setmNotes(noteList);
        mNoteListAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onResume() {
        super.onResume();

        refreshNoteList();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_new_note:
                Intent intent = new Intent(MainActivity.this, NewActivity.class);
                intent.putExtra("groupName", "默认笔记");
                intent.putExtra("flag", 0);
                startActivity(intent);
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
