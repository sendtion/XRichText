# XRichText
一个Android富文本类库，支持图文混排，支持编辑和预览，支持插入和删除图片。

### 实现的原理：
- 使用ScrollView作为最外层布局，里面填充TextView和ImageView。
- 删除的时候，根据光标的位置，删除TextView和ImageView。
- 生成的数据为list集合，可自定义处理数据格式。

## 截图预览
![笔记列表](http://img.blog.csdn.net/20161026140255809)
![新建笔记](http://img.blog.csdn.net/20161026140331684)
![笔记详情](http://img.blog.csdn.net/20161026140122507)

## 使用方式
### 作为类库
把xrichtext作为一个module导入你的工程。
把xrichtext中的文件拷贝到你的工程，可以在你的工程中建一个xrichtextming包名，并把文件拷贝进去。

### gradle依赖
稍后支持。

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
