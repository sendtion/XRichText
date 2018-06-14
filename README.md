# XRichText
[![](https://jitpack.io/v/sendtion/XRichText.svg)](https://jitpack.io/#sendtion/XRichText)

一个Android富文本类库，支持图文混排，支持编辑和预览，支持插入和删除图片。

### 实现的原理：
- 使用ScrollView作为最外层布局包含LineaLayout，里面填充TextView和ImageView。
- 删除的时候，根据光标的位置，删除TextView和ImageView，文本自动合并。
- 生成的数据为list集合，可自定义处理数据格式。

### 注意事项
- xrichtext库中引入了Glide库版本为4.7.1，自己项目中不需要再引入，如果想引入自己的项目，请把Glide排除在外，AppCompat支持库同样也可以排除。
- Demo中图片选择器更换为知乎开源库Matisse，适配Android 7.0系统使用FileProvider获取图片路径。
- 开发环境更新为 AS 3.1.2 + Gradle 4.4 + compileSDK 27 + support library 27.1.1，导入项目报版本错误时，请手动修改为自己的版本。
- <span style="color:red;">V1.3版本开放了编辑笔记时的删除图片接口，这个必须在Activity中实现OnDeleteImageListener接口，否则会发生崩溃！</span>
- 请参考Demo的实现，进行了解本库。可以使用Gradle引入，也可以下载源码进行修改。
- 如有问题，欢迎提出。可以加我QQ：524100248，微信：sendtion

## 截图预览

![笔记列表](http://p695w3yko.bkt.clouddn.com/18-4-5/19166796.jpg)
![文字笔记详情](http://p695w3yko.bkt.clouddn.com/note_detail.jpg?imageMogr2/thumbnail/!35p)
![连续插入多图](http://p695w3yko.bkt.clouddn.com/18-4-5/72572379.jpg)
![编辑笔记](http://p695w3yko.bkt.clouddn.com/18-4-5/55920273.jpg)
![图片笔记详情](http://p695w3yko.bkt.clouddn.com/18-4-5/78527283.jpg)

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
    implementation 'com.github.sendtion:XRichText:1.3'
}
```

如果出现support版本不一致问题，请排除XRichText中的support库，或者升级自己的support库为27.1.1版本。
Glide版本为4.7.1，依赖于27版本库，如果你用的为低版本，同样的处理方式。
使用方式：
```
implementation ('com.github.sendtion:XRichText:1.3') {
    exclude group: 'com.android.support'
    exclude group: 'com.github.bumptech.glide'
}
```

## 具体使用
在xml布局中添加基于EditText编辑器（可编辑）
```
<com.sendtion.xrichtext.RichTextEditor
    android:id="@+id/et_new_content"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:textSize="@dimen/text_size_16"
    android:textColor="@color/grey_600"/>
```

在xml布局中添加基于TextView编辑器（不可编辑）
```
<com.sendtion.xrichtext.RichTextView
    android:id="@+id/tv_note_content"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:textSize="@dimen/text_size_16"
    android:textColor="@color/grey_600"/>
```

**我把数据保存为了html格式，生成字符串存储到了数据库。**
### 生成数据
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

### 具体的使用方式，请参考Demo代码。

### 更新历史

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
本库参考了以下项目，感谢各位大神的辛苦劳作！
- https://github.com/xmuSistone/android-animate-RichEditor
- https://github.com/KDF5000/RichEditText

## 其他
- 个人博客：http://www.sendtion.cn
- CSDN：http://blog.csdn.net/shuyou612
- GitHub：https://github.com/sendtion
- 欢迎大家fork、star，也欢迎大家参与修改。

## License
```
Copyright 2018 sendtion

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
