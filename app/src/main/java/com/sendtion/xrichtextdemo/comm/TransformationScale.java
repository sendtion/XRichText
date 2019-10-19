package com.sendtion.xrichtextdemo.comm;

import android.graphics.Bitmap;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.bumptech.glide.request.target.ImageViewTarget;

/**
 * Glide加载图片时，根据图片宽度等比缩放
 */
public class TransformationScale extends ImageViewTarget<Bitmap> {

    private ImageView target;

    public TransformationScale(ImageView target) {
        super(target);
        this.target = target;
    }

    @Override
    protected void setResource(Bitmap resource) {
        view.setImageBitmap(resource);

        if (resource != null) {
            //获取原图的宽高
            int width = resource.getWidth();
            int height = resource.getHeight();

            //获取imageView的宽
            int imageViewWidth = target.getWidth();

            //计算缩放比例
            float sy = (float) (imageViewWidth * 0.1) / (float) (width * 0.1);

            //计算图片等比例放大后的高
            int imageHeight = (int) (height * sy);
            //ViewGroup.LayoutParams params = target.getLayoutParams();
            //params.height = imageHeight;
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.MATCH_PARENT, imageHeight);//固定图片高度，记得设置裁剪剧中
            params.bottomMargin = 10;
            target.setLayoutParams(params);
        }
    }
}
