package com.even.media.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.even.media.VideoActivity;
import com.even.media.bean.LMedia;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;

public class CollectionUtils {
	private static final String COLLECTION = "CollectionMusicList"; 
	private static final String COLLECTIONTAG = "TAG";
	/**
	 * 查询收藏列表
	 * @param mMusicName 需要查询的收藏列表
	 */
	public static ArrayList<LMedia> getCollectionMusicList(SharedPreferences mSP, ArrayList<LMedia> mMusicName){
		try {
			mMusicName.clear();
        	String jsonString = VideoActivity.mSP.getString(COLLECTIONTAG, "");
    		JSONObject json = new JSONObject(jsonString);
    		JSONArray ja1 = json.getJSONArray(COLLECTION);
    		for(int i = 0; i < ja1.length();i++){
    			String name = ja1.getString(i);
    			Long size = ja1.getLong(++i);
    			String url = ja1.getString(++i);
    			int duration = ja1.getInt(++i);
    			int id = ja1.getInt(++i);
    			String mediaType = ja1.getString(++i);
    			File musicFile = new File(url);
    			if(musicFile.exists()){//判断文件是否存在
    				mMusicName.add(new LMedia(name, size, url, duration, id, mediaType));
    			}
    		}
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
		return mMusicName;
    }

	/**
	 * @param mMusicName 需要保存的收藏列表
	 */
	public static void saveCollectionMusicList(SharedPreferences mSP,ArrayList<LMedia> mMusicName){
		try {
   		 JSONObject jo = new JSONObject();
			 JSONArray jsonarr = new JSONArray();
			 int index = 0;
			 for(LMedia item : mMusicName){
				 jsonarr.put(index, item.name);
				 jsonarr.put(++index, item.size);
				 jsonarr.put(++index, item.url);
				 jsonarr.put(++index, item.duration);
				 jsonarr.put(++index, item.id);
				 jsonarr.put(++index, item.mediaType);
				 index++;
			 }
			 jo.put(COLLECTION, jsonarr);
			 mSP.edit().putString(COLLECTIONTAG, jo.toString()).commit();
		} catch (Exception e) {
			e.printStackTrace();
		}
    }
	
	/**
	 * 查询歌曲是否在收藏列表
	 * @param mCurrentAPath 查询的歌曲路径
	 * @param mMusicName 需要查询所在的收藏列表
	 * @return
	 */
	public static boolean itBeenCollected(Context context ,String mCurrentAPath,ArrayList<LMedia> mMusicName){
		try {
			for(LMedia mMusic : mMusicName){
				if(mMusic.url.equals(mCurrentAPath)){
					return true;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}
	
	/**
	 * 添加歌曲到收藏列表
	 * @param mMusicList 收藏列表
	 */
	public static ArrayList<LMedia> addMusicToCollectionList(LMedia mMusicName, ArrayList<LMedia> mMusicList){
		try {
			mMusicList.add(mMusicName);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return mMusicList;
	}
	
	/**
	 * 从收藏列表中删除
	 * @param Path 需要删除的歌曲路径
	 * @param mMusicName 收藏列表
	 */
	public static ArrayList<LMedia> removeMusicFromCollectionList(String Path, ArrayList<LMedia> mMusicName){
		try {
			int index = 0;
			for(LMedia mMusic : mMusicName){
				if(mMusic.getUrl().equals(Path)){
					mMusicName.remove(index);
				}
				index ++;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return mMusicName;
	}
}
