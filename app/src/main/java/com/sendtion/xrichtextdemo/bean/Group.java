package com.sendtion.xrichtextdemo.bean;

/**
 * 作者：Sendtion on 2016/10/24 0024 15:05
 * 邮箱：sendtion@163.com
 * 博客：http://sendtion.cn
 * 描述：笔记分类
 */

public class Group {

    private int id;//ID
    private String name;//分组名称
    private int order;//排列顺序
    private String color;//分类颜色，存储颜色代码
    private int isEncrypt ;//是否加密，0未加密，1加密
    private String createTime;//创建时间
    private String updateTime;//修改时间

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public int getIsEncrypt() {
        return isEncrypt;
    }

    public void setIsEncrypt(int isEncrypt) {
        this.isEncrypt = isEncrypt;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public String getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(String updateTime) {
        this.updateTime = updateTime;
    }
}
