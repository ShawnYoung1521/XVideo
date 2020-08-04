package com.even.media.utils;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import com.even.media.bean.LMedia;

import java.util.ArrayList;
import java.util.List;

public class LoadMediaFile {
    /**
     * 获取本地视频信息
     **/
    public static List<LMedia> getList(Context mContext, ArrayList<LMedia> mALLMediaList, Uri mUri) {
        if (mContext != null) {
            Cursor cursor = mContext.getContentResolver().query(
                    mUri, null, null,
                    null, null);
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    int id = cursor.getInt(cursor
                            .getColumnIndexOrThrow(MediaStore.Video.Media._ID));
                    String title = cursor
                            .getString(cursor
                                    .getColumnIndexOrThrow(MediaStore.Video.Media.TITLE));
                    String album = cursor
                            .getString(cursor
                                    .getColumnIndexOrThrow(MediaStore.Video.Media.ALBUM));
                    String artist = cursor
                            .getString(cursor
                                    .getColumnIndexOrThrow(MediaStore.Video.Media.ARTIST));
                    String displayName = cursor
                            .getString(cursor
                                    .getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME));
                    String mimeType = cursor
                            .getString(cursor
                                    .getColumnIndexOrThrow(MediaStore.Video.Media.MIME_TYPE));
                    String path = cursor
                            .getString(cursor
                                    .getColumnIndexOrThrow(MediaStore.Video.Media.DATA));
                    int duration = cursor
                            .getInt(cursor
                                    .getColumnIndexOrThrow(MediaStore.Video.Media.DURATION));
                    long size = cursor
                            .getLong(cursor
                                    .getColumnIndexOrThrow(MediaStore.Video.Media.SIZE));
                    String mediaType = null;
                    if(mimeType.startsWith("video/")) {
                        mediaType = mimeType.substring(6);
                    }
                    LMedia video = new LMedia(title, size, path, duration, id, mediaType);
                    video.setName(title);
                    video.setSize(size);
                    video.setUrl(path);
                    video.setDuration(duration);
                    video.setId(id);
                    video.setMediaType(mediaType);
                    mALLMediaList.add(video);
                }
                cursor.close();
            }
        }
        return mALLMediaList;
    }
}
