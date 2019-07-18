package com.even.video;

import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;

public class AnimationUtil {
    private static final String TAG = AnimationUtil.class.getSimpleName();
 
    /**
     * 从控件所在位置移动到控件的底部
     */
    public static TranslateAnimation moveLocationToBottom() {
        TranslateAnimation mHiddenAction = new TranslateAnimation(
        		Animation.ABSOLUTE, //fromXType
        		0.0f,  //fromXValue
                Animation.ABSOLUTE,//toXType
                0.0f,  //fromYType
                Animation.ABSOLUTE, //fromYType
                0.0f,   //fromYValue
                Animation.ABSOLUTE, //toYType
                120.0f 	//toYValue
                );
        mHiddenAction.setDuration(1000);
        return mHiddenAction;
    }
 
    /**
     * 从控件的底部移动到控件所在位置
     */
    public static TranslateAnimation moveBottomToLocation() {
        TranslateAnimation mHiddenAction = new TranslateAnimation(
        		Animation.ABSOLUTE,
        		0.0f,
                Animation.ABSOLUTE,
                0.0f, 
                Animation.ABSOLUTE,
                120.0f, 
                Animation.ABSOLUTE, 
                0.0f);
        mHiddenAction.setDuration(130);
        return mHiddenAction;
    }
    
    /**
     * 从控件所在位置移动到控件的顶部
     */
    public static TranslateAnimation moveLocationToTop() {
        TranslateAnimation mHiddenAction = new TranslateAnimation(
        		Animation.ABSOLUTE,
        		0.0f,
                Animation.ABSOLUTE, 
                0.0f,
                Animation.ABSOLUTE,
                0.0f,
                Animation.ABSOLUTE, 
                -120f);
        mHiddenAction.setDuration(1000);
        return mHiddenAction;
    }
 
    /**
     * 从控件的顶部移动到控件所在位置
     */
    public static TranslateAnimation moveTopToLocation() {
        TranslateAnimation mHiddenAction = new TranslateAnimation(
        		Animation.ABSOLUTE,
        		0.0f,
                Animation.ABSOLUTE, 
                0.0f,
                Animation.ABSOLUTE,
                -120f,
                Animation.ABSOLUTE, 
                0.0f);
        mHiddenAction.setDuration(130);
        return mHiddenAction;
    }
}
