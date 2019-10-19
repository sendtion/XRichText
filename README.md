# XRichText
[![](https://jitpack.io/v/sendtion/XRichText.svg)](https://jitpack.io/#sendtion/XRichText)

一个Android富文本类库，支持图文混排，支持编辑和预览，支持插入和删除图片。

### 实现的原理：
- 使用ScrollView作为最外层布局包含LineaLayout，里面填充TextView和ImageView。
- 删除的时候，根据光标的位置，删除TextView和ImageView，文本自动合并。
- 生成的数据为list集合，可自定义处理数据格式。

### 注意事项
- xrichtext库中引入了Glide库版本为4.9.0，自己项目中不需要再引入，如果想引入自己的项目，请把Glide排除在外，support支持库同样也可以排除。
- **V1.9.3版本，xrichtext库中已去掉Glide依赖，开放接口可以自定义图片加载方式。具体使用方式可以参考后面的说明，也可以参考Demo实现。**
- Demo中图片选择器为知乎开源库Matisse，适配Android 7.0系统使用FileProvider获取图片路径。
- 开发环境更新为 AS 3.4.2 + Gradle 4.4 + compileSDK 28 + support library 28.0.0，导入项目报版本错误时，请手动修改为自己的版本。
- **V1.4版本开放了编辑笔记时的删除图片接口，请自己在Activity中设置OnRtDeleteImageListener接口。**
- **V1.6版本升级RxJava到2.2.3版本，RxAndroid到2.1.0版本。设置字体大小时需要带着单位，如app:rt_editor_text_size="16sp"。**
- 请参考Demo的实现，进行了解本库。可以使用Gradle引入，也可以下载源码进行修改。
- 如有问题，欢迎提出。**欢迎加入QQ群交流：745215148。**

## 截图预览

![笔记列表](https://tva1.sinaimg.cn/large/006y8mN6ly1g81jyaviylj30u01hcdid.jpg)
![文字笔记详情](https://tva1.sinaimg.cn/large/006y8mN6ly1g81jn373lwj30u01hc7a8.jpg)
![编辑笔记](https://tva1.sinaimg.cn/large/006y8mN6ly1g81jpvrnwvj30u01hcb29.jpg)
![图片笔记详情](https://tva1.sinaimg.cn/large/006y8mN6ly1g81js6ma28j30u01hc4qp.jpg)

## 使用方式
#### 1. 作为module导入
把xrichtext作为一个module导入你的工程。

#### 2. gradle依赖

```
allprojects {
    repositories {
        ...
        maven { url 'https://jitpack.io' }
    }
}

dependencies {
    implementation 'com.github.sendtion:XRichText:1.9.3'
}
```

如果出现support版本不一致问题，请排除XRichText中的support库，或者升级自己的support库为28.0.0版本。
使用方式：
```
implementation ('com.github.sendtion:XRichText:1.9.3') {
    exclude group: 'com.android.support'
}
```

## 具体使用
在xml布局中添加基于EditText编辑器（可编辑）
```
<com.sendtion.xrichtext.RichTextEditor
    android:id="@+id/et_new_content"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    app:rt_editor_text_line_space="6dp"
    app:rt_editor_image_height="500"
    app:rt_editor_image_bottom="10"
    app:rt_editor_text_init_hint="在这里输入内容"
    app:rt_editor_text_size="16sp"
    app:rt_editor_text_color="@color/grey_900"/>
```

在xml布局中添加基于TextView编辑器（不可编辑）
```
<com.sendtion.xrichtext.RichTextView
    android:id="@+id/tv_note_content"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    app:rt_view_text_line_space="6dp"
    app:rt_view_image_height="0"
    app:rt_view_image_bottom="10"
    app:rt_view_text_size="16sp"
    app:rt_view_text_color="@color/grey_900"/>
```

### 自定义属性

具体参考Demo

- RichTextView
```
rt_view_image_height        图片高度，默认为0自适应，可以设置为固定数值，如500、800等
rt_view_image_bottom        上下两张图片的间隔，默认10
rt_view_text_size           文字大小，使用sp单位，如16sp
rt_view_text_color          文字颜色，使用color资源文件
rt_view_text_line_space     字体行距，跟TextView使用一样，比如6dp
```

- RichTextEditor
```
rt_editor_image_height      图片高度，默认为500，可以设置为固定数值，如500、800等
rt_editor_image_bottom      上下两张图片的间隔，默认10
rt_editor_text_init_hint    默认提示文字，默认为“请输入内容”
rt_editor_text_size         文字大小，使用sp单位，如16sp
rt_editor_text_color        文字颜色，使用color资源文件
rt_editor_text_line_space   字体行距，跟TextView使用一样，比如6dp
```

### 生成数据

**我把数据保存为了html格式，生成字符串存储到了数据库。**

```
String noteContent = getEditData();

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
```

### 显示数据
```
et_new_content.post(new Runnable() {
     @Override
     public void run() {
         showEditData(content);
     }
 });

protected void showEditData(String content) {
    et_new_content.clearAllLayout();
    List<String> textList = StringUtils.cutStringByImgTag(content);
    for (int i = 0; i < textList.size(); i++) {
        String text = textList.get(i);
        if (text.contains("<img")) {
            String imagePath = StringUtils.getImgSrc(text);
            int width = ScreenUtils.getScreenWidth(this);
            int height = ScreenUtils.getScreenHeight(this);
            et_new_content.measure(0,0);
            Bitmap bitmap = ImageUtils.getSmallBitmap(imagePath, width, height);
            if (bitmap != null){
                et_new_content.addImageViewAtIndex(et_new_content.getLastIndex(), bitmap, imagePath);
            } else {
            et_new_content.addEditTextAtIndex(et_new_content.getLastIndex(), text);
            }
            et_new_content.addEditTextAtIndex(et_new_content.getLastIndex(), text);
        }
    }
}
```
### 图片点击事件
```
tv_note_content.setOnRtImageClickListener(new RichTextView.OnRtImageClickListener() {
    @Override
    public void onRtImageClick(String imagePath) {
        ArrayList<String> imageList = StringUtils.getTextFromHtml(myContent, true);
        int currentPosition = imageList.indexOf(imagePath);
        showToast("点击图片："+currentPosition+"："+imagePath);
        // TODO 点击图片预览
    }
});
```
### 图片加载器使用
```
XRichText.getInstance().setImageLoader(new IImageLoader() {
    @Override
    public void loadImage(String imagePath, ImageView imageView, boolean centerCrop) {
       if (centerCrop) {
           Glide.with(MainActivity.this).asBitmap().load(imagePath).centerCrop()
                .placeholder(R.mipmap.img_load_fail).error(R.mipmap.img_load_fail).into(imageView);
       } else {
           Glide.with(MainActivity.this).asBitmap().load(imagePath)
                .placeholder(R.mipmap.img_load_fail).error(R.mipmap.img_load_fail).into(new TransformationScale(imageView));
       }
    }
});
```
**TransformationScale类请参考Demo**


### 具体的使用方式，请参考Demo代码。

### 更新历史

####  v1.9.3  2019.10.19
- 去除依赖的Glide，改为接口回调，可使用自定义图片加载器
- 优化代码结构，提升稳定性

####  v1.9.1  2019.04.30
- 图片点击事件接口返回点击的View
- 修复图片显示时高度拉伸变形问题
- Demo中实现了点击图片放大浏览功能
- Support支持库升级为28.0.0

####  v1.9.0  2019.04.10
- 编辑时支持关键词高亮
- 修复插入图片时空指针异常
- 代码异常处理

#### v1.8  2018.12.02
- 修复编辑时设置文字颜色无效的问题
- 编辑时添加和删除图片加入动画效果

#### v1.6  2018.11.16
- RxJava升级到2.2.3版本，RxAndroid升级到2.1.0版本
- 编辑图片时支持自适应高度，高度设置为0即自适应，比如app:rt_editor_image_height="0"
- 修改字体大小设置方式，像正常使用带着单位，比如app:rt_editor_text_size="16sp"
- 支持设置字体行间距，比如app:rt_editor_text_line_space="6dp"
- 修复xrichtext库及Demo中的各种崩溃异常

#### v1.5  2018.07.10
- 修复详情页连续加载多张图片导致后续图片都跟第一张图片相同高度的问题
- 修复Demo插入图片后点击图片导致空指针异常的问题
- 去掉Demo插入图片后会插入一张网络图片的测试代码

#### v1.4  2018.06.22
- 添加自定义属性，可以设置图片高度，相邻图片间隔，文字大小和颜色
- 修复没有实现图片删除接口导致的崩溃问题，开放图片删除接口
- 添加点击图片查看大图的功能，开放图片点击接口
- 加入崩溃日志信息展示，加入崩溃日志信息发送到邮件
- 优化图片插入代码，删除多余的无用代码

#### v1.3  2018.05.05
- 更新Glide依赖版本为4.7.1，Glide4使用方式：http://bumptech.github.io/glide/doc/getting-started.html
- 开发环境更新到AS 3.1.2 + Gradle 4.4
- 优化图片插入的逻辑
- 在Demo中加入插入网络图片的示例代码
- 在Demo中图片选择器更换为知乎matisse

#### v1.2  2018.04.05
- 编辑笔记时，使用接口回调在外部处理图片的删除操作，可以自行实现删除本地图片还是网络图片
- 实现网络图片的加载，插入图片时，可以传入本地图片SD卡路径，也可以传入网络图片地址
- 在新建或编辑笔记时，连续多张图片之间插入输入框，方便在图片间输入文本内容
- 修复在文件中间插入图片时，导致的后面文字丢失的问题
- 修复连续插入多张图片时，会出现图片倒序插入的问题

#### v1.1	2017.03.27
- 优化内存占用，解决内存溢出问题
- 结合RxJava使用（参考Demo）
- 支持连续插入多张图片不卡顿（参考Demo）
- 解决插入图片导致的卡顿和崩溃

#### v1.0	2016.10.26
- 初次提交
- 实现插入图片
- 实现图文混排
- 实现编辑和保存

## 感谢
本库参考了以下项目，感谢各位大神的优秀作品！
- https://github.com/xmuSistone/android-animate-RichEditor
- https://github.com/KDF5000/RichEditText

## 其他
- 个人博客：http://www.sendtion.cn
- CSDN：http://blog.csdn.net/shuyou612
- GitHub：https://github.com/sendtion
- 欢迎大家fork、star，也欢迎大家参与修改。

## License
```
Copyright 2019 sendtion

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
