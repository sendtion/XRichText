package com.sendtion.xrichtextdemo.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.sendtion.xrichtextdemo.bean.Group;
import com.sendtion.xrichtextdemo.util.DateUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static android.content.ContentValues.TAG;

/**
 * 作者：Sendtion on 2016/10/24 0024 17:18
 * 邮箱：sendtion@163.com
 * 博客：http://sendtion.cn
 * 描述：分类处理
 */

public class GroupDao {
    private MyOpenHelper helper;
    private NoteDao noteDataDao;

    public GroupDao(Context context) {
        helper = new MyOpenHelper(context);
        noteDataDao = new NoteDao(context);
    }

    /**
     * 查询所有分类列表
     *
     * @return
     */
    public List<Group> queryGroupAll() {
        SQLiteDatabase db = helper.getWritableDatabase();
        List<Group> groupList = new ArrayList<Group>();

        Group group ;
        Cursor cursor = null;
        try {
            cursor = db.query("db_group", null, null, null, null, null, "g_create_time asc");
            while (cursor.moveToNext()) {
                int groupId = cursor.getInt(cursor.getColumnIndex("g_id"));
                String groupName = cursor.getString(cursor.getColumnIndex("g_name"));
                int order = cursor.getInt(cursor.getColumnIndex("g_order"));
                String color = cursor.getString(cursor.getColumnIndex("g_color"));
                int encrypt = cursor.getInt(cursor.getColumnIndex("g_encrypt"));
                String createTime = cursor.getString(cursor.getColumnIndex("g_create_time"));
                String updateTime = cursor.getString(cursor.getColumnIndex("g_update_time"));
                //生成一个分类
                group = new Group();
                group.setId(groupId);
                group.setName(groupName);
                group.setOrder(order);
                group.setColor(color);
                group.setIsEncrypt(encrypt);
                group.setCreateTime(createTime);
                group.setUpdateTime(updateTime);
                groupList.add(group);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
            if (db != null) {
                db.close();
            }
        }
        return groupList;
    }

    /**
     * 根据分类名查询分类
     *
     * @param groupName
     * @return
     */
    public Group queryGroupByName(String groupName) {
        SQLiteDatabase db = helper.getWritableDatabase();

        Group group = null;
        Cursor cursor = null;
        try {
            Log.i(TAG, "###queryGroupByName: "+groupName);
            cursor = db.query("db_group", null, "g_name=?", new String[]{groupName}, null, null, null);
            while (cursor.moveToNext()) {
                int groupId = cursor.getInt(cursor.getColumnIndex("g_id"));
                int order = cursor.getInt(cursor.getColumnIndex("g_order"));
                String color = cursor.getString(cursor.getColumnIndex("g_color"));
                int encrypt = cursor.getInt(cursor.getColumnIndex("g_encrypt"));
                String createTime = cursor.getString(cursor.getColumnIndex("g_create_time"));
                String updateTime = cursor.getString(cursor.getColumnIndex("g_update_time"));
                //生成一个分类
                group = new Group();
                group.setId(groupId);
                group.setName(groupName);
                group.setOrder(order);
                group.setColor(color);
                group.setIsEncrypt(encrypt);
                group.setCreateTime(createTime);
                group.setUpdateTime(updateTime);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
            if (db != null) {
                db.close();
            }
        }
        return group;
    }

    /**
     * 根据分类ID查询分类
     *
     * @return
     */
    public Group queryGroupById(int groupId) {
        SQLiteDatabase db = helper.getWritableDatabase();

        Group group = null;
        Cursor cursor = null;
        try {
            cursor = db.query("db_group", null, "g_id=?", new String[]{groupId + ""}, null, null, null);
            while (cursor.moveToNext()) {
                int order = cursor.getInt(cursor.getColumnIndex("g_order"));
                String color = cursor.getString(cursor.getColumnIndex("g_color"));
                int encrypt = cursor.getInt(cursor.getColumnIndex("g_encrypt"));
                String groupName = cursor.getString(cursor.getColumnIndex("g_name"));
                String createTime = cursor.getString(cursor.getColumnIndex("g_create_time"));
                String updateTime = cursor.getString(cursor.getColumnIndex("g_update_time"));
                //生成一个订单
                group = new Group();
                group.setId(groupId);
                group.setName(groupName);
                group.setOrder(order);
                group.setColor(color);
                group.setIsEncrypt(encrypt);
                group.setCreateTime(createTime);
                group.setUpdateTime(updateTime);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
            if (db != null) {
                db.close();
            }
        }
        return group;
    }

    /**
     * 添加一个分类
     */
    public void insertGroup(String groupName) {
        SQLiteDatabase db = helper.getWritableDatabase();

        Cursor cursor = null;
        try {
            cursor = db.query("group", null, "g_name=?", new String[]{groupName}, null, null, null);
            if (!cursor.moveToNext()) {//如果订单不存在
                ContentValues values = new ContentValues();
                values.put("g_name", groupName);
                values.put("g_color", "#FFFFFF");
                values.put("g_encrypt", 0);
                values.put("g_create_time", DateUtils.date2string(new Date()));
                values.put("g_update_time", DateUtils.date2string(new Date()));
                db.insert("db_group", null, values);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
            if (db != null) {
                db.close();
            }
        }
    }

    /**
     * 更新一个分类
     */
    public void updateGroup(Group group) {
        SQLiteDatabase db = helper.getWritableDatabase();

        try {
            ContentValues values = new ContentValues();
            values.put("g_name", group.getName());
            values.put("g_order", group.getOrder());
            values.put("g_color", group.getColor());
            values.put("g_encrypt", group.getIsEncrypt());
            values.put("update_time", DateUtils.date2string(new Date()));
            db.update("db_group", values, "g_id=?", new String[]{group.getId() + ""});
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (db != null) {
                db.close();
            }
        }
    }

    /**
     * 删除一个分类
     */
    public int deleteGroup(int groupId) {
        SQLiteDatabase db = helper.getWritableDatabase();

        int ret = 0;
        try {
            ret = db.delete("db_group", "g_id=?", new String[]{groupId + ""});
            //Group group = queryGroupByName("默认笔记");
            //noteDataDao.updateNote2(groupId, group.getGroupId());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (db != null) {
                db.close();
            }
        }
        return ret;
    }
}
