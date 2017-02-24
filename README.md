# XRichText
[![](https://jitpack.io/v/sendtion/XRichText.svg)](https://jitpack.io/#sendtion/XRichText)

一个Android富文本类库，支持图文混排，支持编辑和预览，支持插入和删除图片。

### 实现的原理：
- 使用ScrollView作为最外层布局，里面填充TextView和ImageView。
- 删除的时候，根据光标的位置，删除TextView和ImageView。
- 生成的数据为list集合，可自定义处理数据格式。

## 截图预览
<img src="http://ww3.sinaimg.cn/large/6c6cd56djw1fax2yzxqqvj20u01hcmze.jpg" width = "400" height = "650" />
<img src="http://ww3.sinaimg.cn/large/6c6cd56djw1fax2wp3ngbj20u01hcar0.jpg" width = "400" height = "650" />
<img src="http://ww3.sinaimg.cn/large/6c6cd56djw1fax2u2j24jj20u01hc41b.jpg" width = "400" height = "650" />
<img src="http://ww3.sinaimg.cn/large/6c6cd56djw1fax2xcszo1j20u01hc4o9.jpg" width = "400" height = "650" />

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
    compile 'com.github.sendtion:XRichText:1.1'
}
```

#### 3. Maven方式

```
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>

<dependency>
    <groupId>com.github.sendtion</groupId>
    <artifactId>XRichText</artifactId>
    <version>1.1</version>
</dependency>
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
#### v1.1
- 优化内存占用，解决内存溢出问题
- 结合RxJava使用（参考Demo）
- 支持连续插入多张图片不卡顿（参考Demo）
- 解决插入图片导致的卡顿和崩溃

#### v1.0
- 初次提交
- 实现插入图片
- 实现图文混排
- 实现编辑和保存

## 感谢
本库在前人的基础上进行修改，感谢各位大神的辛苦劳作！
参考了以下项目：
- https://github.com/xmuSistone/android-animate-RichEditor
- https://github.com/KDF5000/RichEditText

## 其他
- 个人博客：http://www.sendtion.cn
- CSDN：http://blog.csdn.net/shuyou612
- GitHub：https://github.com/sendtion
- 欢迎大家fork、star，也欢迎大家参与修改。

## License
```
Copyright [yyyy] [name of copyright owner]

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
