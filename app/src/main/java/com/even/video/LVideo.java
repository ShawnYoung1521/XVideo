package com.even.video;

//电影视频的model类  
public class LVideo {  
	String name;  
	long size;  
	String url;  
	int duration;  
	int id;
	String mediaType;
	public LVideo(String aname,long asize,String aurl,int aduration,int aid,String amediaType) {
		name = aname;
		size = asize;  
		url = aurl;  
		duration = aduration;  
		id = aid;
		mediaType = amediaType;
	}

	public String getName() {  
		return name;  
	}  
	public void setName(String name) {  
		this.name = name;  
	}  
	public long getSize() {  
		return size;  
	}  
	public void setSize(long size) {  
		this.size = size;  
	}  
	public String getUrl() {  
		return url;  
	}  
	public void setUrl(String url) {  
		this.url = url;  
	}  
	public int getDuration() {  
		return duration;  
	}  
	public void setDuration(int duration) {  
		this.duration = duration;  
	}  
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getMediaType() {
		return mediaType;
	}
	public void setMediaType(String mediaType) {
		this.mediaType = mediaType;
	}
}  