package com.example.mp11.videoplayer.mydynvideoplayer;

import android.os.Bundle;

import java.util.ArrayList;


//класс для хранения параметров видео
public class MediaItem {

    private String mTitle;
    private String mSubTitle;
    private String mStudio;
    private String mUrl;
    private String mContentType;
    private int mDuration;
    private ArrayList<String> mImageList = new ArrayList<String>();

    public static final String KEY_TITLE = "title";
    public static final String KEY_SUBTITLE = "subtitle";
    public static final String KEY_STUDIO = "studio";
    public static final String KEY_URL = "movie-urls";
    public static final String KEY_IMAGES = "images";
    public static final String KEY_CONTENT_TYPE = "content-type";

    public String getUrl() {
        return mUrl;
    }

    public void setUrl(String url) {
        mUrl = url;
    }

    public String getTitle() {

        return mTitle;
    }

    public int getDuration() {
        return mDuration;
    }

    public void setDuration(int duration) {
        mDuration = duration;
    }

    public String getContentType() {
        return mContentType;
    }

    public void setContentType(String contentType) {
        mContentType = contentType;
    }

    public void setTitle(String title) {
        mTitle = title;
    }

    public String getSubTitle() {
        return mSubTitle;
    }

    public void setSubTitle(String subTitle) {
        mSubTitle = subTitle;
    }


    public void setStudio(String studio) {
        mStudio = studio;
    }



    public boolean hasImage() {
        return !mImageList.isEmpty();
    }

    public ArrayList<String> getImages() {
        return mImageList;
    }

    public Bundle toBundle() {
        Bundle wrapper = new Bundle();
        wrapper.putString(KEY_TITLE, mTitle);
        wrapper.putString(KEY_SUBTITLE, mSubTitle);
        wrapper.putString(KEY_URL, mUrl);
        wrapper.putString(KEY_STUDIO, mStudio);
        wrapper.putStringArrayList(KEY_IMAGES, mImageList);
        wrapper.putString(KEY_CONTENT_TYPE, "video/mp4");
        return wrapper;
    }

    public static final MediaItem fromBundle(Bundle wrapper) {
        if (null == wrapper) {
            return null;
        }
        MediaItem media = new MediaItem();
        media.setUrl(wrapper.getString(KEY_URL));
        media.setTitle(wrapper.getString(KEY_TITLE));
        media.setSubTitle(wrapper.getString(KEY_SUBTITLE));


        media.setContentType(wrapper.getString(KEY_CONTENT_TYPE));
        return media;
    }

}
