package com.sendtion.xrichtextdemo;

import android.app.Application;

import com.wenming.library.LogReport;
import com.wenming.library.save.imp.CrashWriter;
import com.wenming.library.save.imp.LogWriter;
import com.wenming.library.upload.email.EmailReporter;
import com.wenming.library.upload.http.HttpReporter;

public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        // 初始化崩溃日志上传 https://github.com/wenmingvs/LogReport
        initCrashReport();
        // 在任意地方，调用以下方法即可，崩溃发生后，会在下一次App启动的时候使用Service异步打包日志，
        // 然后上传日志，发送成功与否，Service都会自动退出释放内存
        //LogReport.getInstance().upload(this);
        // 使用以下方法，打印Log的同时，把Log信息保存到本地（保存的时候会附带线程名称，线程id，打印时间），
        // 并且随同崩溃日志一起，发送到特定的邮箱或者服务器上。帮助开发者还原用户的操作路径，更好的分析崩溃产生的原因
        //LogWriter.writeLog("wenming", "打Log测试！！！！");
    }

    private void initCrashReport() {
        //this.getString(this.getApplicationInfo().labelRes)
        LogReport.getInstance()
                .setCacheSize(30 * 1024 * 1024)//支持设置缓存大小，超出后清空
                .setLogDir(getApplicationContext(), "sdcard/XRichText/")//定义路径为：sdcard/[app name]/
                .setWifiOnly(true)//设置只在Wifi状态下上传，设置为false为Wifi和移动网络都上传
                .setLogSaver(new CrashWriter(getApplicationContext()))//支持自定义保存崩溃信息的样式
                //.setEncryption(new AESEncode()) //支持日志到AES加密或者DES加密，默认不开启
                .init(getApplicationContext());
        //默认使用email发送。如果您只需要在本地存储崩溃信息，不需要发送出去，请把initEmailReport（）删掉即可。
        initEmailReporter();
    }

    /**
     * 使用EMAIL发送日志
     */
    private void initEmailReporter() {
        EmailReporter email = new EmailReporter(this);
        email.setReceiver("sendtion2018@163.com");//收件人
        email.setSender("sendtion2018@163.com");//发送人邮箱
        email.setSendPassword("xrichtext2018");//邮箱的客户端授权码，注意不是邮箱密码
        email.setSMTPHost("smtp.163.com");//SMTP地址
        email.setPort("465");//SMTP 端口
        LogReport.getInstance().setUploadType(email);
    }

    /**
     * 使用HTTP发送日志
     */
    private void initHttpReporter() {
        HttpReporter http = new HttpReporter(this);
        http.setUrl("http://crashreport.jd-app.com/your_receiver");//发送请求的地址
        http.setFileParam("fileName");//文件的参数名
        http.setToParam("to");//收件人参数名
        http.setTo("你的接收邮箱");//收件人
        http.setTitleParam("subject");//标题
        http.setBodyParam("message");//内容
        LogReport.getInstance().setUploadType(http);
    }
}
