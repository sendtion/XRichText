package com.sendtion.xrichtext;

import android.widget.ImageView;

public class XRichText {
    private static XRichText instance;
    private IImageLoader imageLoader;

    public static XRichText getInstance(){
        if (instance == null){
            synchronized (XRichText.class){
                if (instance == null){
                    instance = new XRichText();
                }
            }
        }
        return instance;
    }

    public void setImageLoader(IImageLoader imageLoader){
        this.imageLoader = imageLoader;
    }

    public void loadImage(String imagePath, ImageView imageView, int imageHeight){
        if (imageLoader != null){
            imageLoader.loadImage(imagePath, imageView, imageHeight);
        }
    }
}
