package com.even.media;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.PixelFormat;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.view.Display;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.even.media.bean.LMedia;
import com.even.media.utils.AnimationUtil;
import com.even.media.utils.CollectionUtils;
import com.even.media.utils.FileSizeUtil;
import com.even.media.utils.LoadMediaFile;
import com.even.media.utils.MMediaPlayer;
import com.even.media.utils.SharedPreferencesUtils;

import java.util.ArrayList;
import java.util.Locale;

public class VideoActivity extends Activity implements
OnCompletionListener
,OnSeekBarChangeListener
,OnItemClickListener
,OnClickListener
{
	private static final String TAG = AnimationUtil.class.getSimpleName();
	private MMediaPlayer mMediaPlayer = null;
	private static final int SHOW_PROGRESS = 0x01;
	private static final int GONE_PLAYUI = 0x02;
	private static final int VISIBLE_PLAYUI = 0x03;
	private static final int ADD_VIDEO_DATA = 0X05;
	private int mCurrentPos;  //播放的位置
	private String mTemporaryPath = "";
	private SeekBar mSeekBar;  //进度条
	private TextView mCurrenttime; //当前进度tx
	private TextView mTotaltime; //总长度tx
	private LinearLayout mPlayui; //控制模块
	private ImageView mCt; //大窗口
	private boolean isLandScape = false;
	private boolean isPlayerScape = false;
	private ListView ls_video;
	private TextView load_tx;
	private mListAdapter adapter;
	private ArrayList<LMedia> mList ; //默认视频信息集合
	public static SharedPreferences mSP;
	private ArrayList<LMedia> mCollectionMusicList; //收藏视频信息集合
	private ArrayList<LMedia> mALLMusicList; //所有视频信息集合
	private View mPlayerView;
	private View mListView;
	private ImageView pp;
	private int ShowMode;
	private Uri VIDEO_URI = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
	private FrameLayout video_view;
	/*
	 * the path of the file, or the http/rtsp URL of the stream you want to play
	 */
	private String mPath = "";

	@SuppressLint("HandlerLeak")
	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			try {
				switch (msg.what) {
				case SHOW_PROGRESS:{
					if (mMediaPlayer.isPlaying()) {
						int duration = mMediaPlayer.getDuration();  //视频长度
						int position = mMediaPlayer.getCurrentPosition(); //当前播放位置
						mCurrentPos = mMediaPlayer.getCurrentPosition();
						if(duration < 0) {
							duration = 0;
						}
						if(position < 0) {
							position = 0;
						}
						/*
						 * 换算总长度 00:00格式显示
						 */
						mTotaltime.setText(chengTimeShow(duration));
						/*
						 * 换算当前进度 00:00格式显示
						 */
						mCurrenttime.setText(chengTimeShow(position));
						/*
						 * 设置进度条属性
						 */
						ProgressBar progress = (ProgressBar)findViewById(R.id.progress);
						progress.setMax(duration);
						progress.setProgress(position);
					}
					pp.getDrawable().setLevel(mMediaPlayer.isPlaying() ? 1 : 0);
					mHandler.removeMessages(SHOW_PROGRESS);
					mHandler.sendEmptyMessageDelayed(SHOW_PROGRESS, 250);
				}
				break;
				case GONE_PLAYUI:
					if (isPlayerScape){
						mPlayui.setVisibility(View.GONE);
						mPlayui.setAnimation(AnimationUtil.moveLocationToBottom());
//						mCt.setAnimation(AnimationUtil.moveLocationToTop());
//						mCt.setVisibility(View.GONE);
						getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
						getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams. FLAG_FULLSCREEN);
					}
					break;
				case VISIBLE_PLAYUI:
					if (isPlayerScape) {
						mHandler.removeMessages(GONE_PLAYUI);
						mHandler.sendEmptyMessageDelayed(GONE_PLAYUI, 4000);
//						mCt.setVisibility(View.VISIBLE);
//						mCt.setAnimation(AnimationUtil.moveTopToLocation());
						mPlayui.setVisibility(View.VISIBLE);
						mPlayui.setAnimation(AnimationUtil.moveBottomToLocation());
						getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
						getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
					}
					break;
				case ADD_VIDEO_DATA:
					CollectionUtils.getCollectionMusicList(mSP, mCollectionMusicList);
					ls_video.setAdapter(adapter);
					break;
				}
			} catch (Exception e) {

			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().setFormat(PixelFormat.TRANSLUCENT);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
		setContentView(R.layout.video);
		if (savedInstanceState != null)
			mCurrentPos = savedInstanceState.getInt("currentPos");
		initView();
		initData();
	}

	private void initView() {
		Display display = getWindowManager().getDefaultDisplay();
		mCollectionMusicList = new ArrayList<LMedia>();
		mALLMusicList = new ArrayList<LMedia>();
		LoadMediaFile.getList(this,mALLMusicList,VIDEO_URI);
		mMediaPlayer = new MMediaPlayer(this,display.getWidth(),display.getHeight());
		video_view = (FrameLayout)findViewById(R.id.video);
		pp = ((ImageView)findViewById(R.id.pp));
		mPlayerView = findViewById(R.id.id_player);
		mListView = findViewById(R.id.id_list);
		mSeekBar = (SeekBar) findViewById(R.id.progress);
		mSeekBar.setOnSeekBarChangeListener(this);
		mCurrenttime = (TextView) findViewById(R.id.currenttime);
		mTotaltime = (TextView) findViewById(R.id.totaltime);
		load_tx = (TextView) findViewById(R.id.load_tx);
		load_tx.setOnClickListener(this);
		mPlayui = (LinearLayout) findViewById(R.id.playui);
		mCt = (ImageView) findViewById(R.id.ct);
		mCt.setOnClickListener(this);
		ls_video = (ListView)findViewById(R.id.ls_video);
		adapter = new mListAdapter(this);
		ls_video.setOnItemClickListener(this);
		ls_video.setDividerHeight(0);
	}

	/*
	 * 列表的点击事件
	 */
	@Override
	public void onItemClick(AdapterView<?> arg0, View view, int position, long arg3) {
		mPath = mList.get(position).getUrl();
		mMediaPlayer.setVideoPath(mPath);
		mHandler.sendEmptyMessageDelayed(SHOW_PROGRESS,1000);
		if (mTemporaryPath.equals(mPath)){
			mMediaPlayer.seekTo(mCurrentPos);
		}else {
			mTemporaryPath = mPath;
			mMediaPlayer.seekTo(0);
		}
		showVideo();
	}


	private void initData() {
		mSP = getApplicationContext().getSharedPreferences(TAG, Context.MODE_PRIVATE);
		Configuration mConfiguration = this.getResources().getConfiguration(); //获取设置的配置信息
		int ori = mConfiguration.orientation; //获取屏幕方向
		if (ori == mConfiguration.ORIENTATION_LANDSCAPE) {
			isLandScape = true;
		} else if (ori == mConfiguration.ORIENTATION_PORTRAIT) {
			isLandScape = false;
		}
		CollectionUtils.getCollectionMusicList(mSP, mCollectionMusicList);
		ShowMode = SharedPreferencesUtils.getIntPref(getApplicationContext(), "SHOWMODE", "view");
		((Button)findViewById(R.id.setting_bt)).setText(ShowMode==0?R.string.list_all:R.string.list_collection);
		changePager();
	}

	private void changePager() {
		switch (ShowMode) {
		case 0:
			mList = mALLMusicList;
			break;
		case 1:
			mList = mCollectionMusicList;
			break;
		}
		mHandler.sendEmptyMessage(ADD_VIDEO_DATA);
	}

	@SuppressLint("WrongConstant")
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.setting_bt:
				showSingleChoiceDialog();
				break;
			case R.id.load_tx:
				load_tx.setVisibility(View.VISIBLE);
				load_tx.setText(getResources().getString(R.string.scanning));
				LoadMediaFile.getList(this,mALLMusicList,VIDEO_URI);
				changePager();
				break;
			case R.id.pp:
				mPPause();
				break;
			case R.id.ct:
				if (isopenscreen()) {
					Toast.makeText(this, "开启重力感应时此功能无效", 0).show();
				}else{
					if (isLandScape) {
						setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);//强制为竖屏
						isLandScape = false;
					}else{
						setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);//强制为横屏
						isLandScape = true;
					}
				}
				break;
		}
		mHandler.removeMessages(GONE_PLAYUI);
		mHandler.sendEmptyMessageDelayed(GONE_PLAYUI,4000);
	}

	int screenchange ;
	private boolean isopenscreen(){
		try {
			screenchange = Settings.System.getInt(getContentResolver(), Settings.System.ACCELEROMETER_ROTATION);
		} catch (SettingNotFoundException e) {
			e.printStackTrace();
		}
		return (screenchange == 1);
	}

	/*
	 * 当流媒体播放完毕的时候回调
	 */
	@Override
	public void onCompletion(MediaPlayer mp) {
		showList();
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (mMediaPlayer != null) {
			if (!mMediaPlayer.isPlaying()) {
				mPPause();
			}
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (mMediaPlayer != null) {
			if (mMediaPlayer.isPlaying()) {
				mPPause();
			}
		}
		CollectionUtils.saveCollectionMusicList(mSP, mCollectionMusicList);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		mHandler.removeMessages(SHOW_PROGRESS);
		mMediaPlayer.stopPlayback();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt("currentPos", mCurrentPos);
	}

	/*
	 * 视频进度条
	 */
	@Override
	public void onProgressChanged(SeekBar seekBar, int progress,
			boolean fromUser) {
		if(fromUser) {
			mHandler.removeMessages(GONE_PLAYUI);
			mMediaPlayer.seekTo(progress);
		}
	}

	private void mPPause(){
		if (mMediaPlayer.isPlaying()) {
			mMediaPlayer.pause();
		}else{
			mMediaPlayer.start();
		}
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		mHandler.sendEmptyMessageDelayed(GONE_PLAYUI,4000);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		return gd.onTouchEvent(event);
	}

	/*
	 * 手势动作
	 */
	@SuppressWarnings("deprecation")
	private GestureDetector gd = new GestureDetector(new GestureDetector.SimpleOnGestureListener() {
		@Override
		public boolean onDoubleTap(MotionEvent e) {
			/*
			 * 双击
			 */
			mPPause();
			return true;
		}

		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
							   float velocityY) {
			float x = e2.getX() - e1.getX();
			float y = e2.getY() - e1.getY();
			if(x > 300) {
				mMediaPlayer.mFastforward();
				return true;
			} else if(x < -300) {
				mMediaPlayer.mRewind();
				return true;
			}
			if(y > 200) {
				showList();
				return true;
			}
			return false;
		}

		@Override
		public boolean onDown(MotionEvent e) {
			if (System.currentTimeMillis() - times > 1000) {
				times = System.currentTimeMillis();
				/*
				 * 底部控制栏的显示与隐藏逻辑
				 */
				if (mPlayui.getVisibility() == View.VISIBLE) {
					mHandler.removeMessages(GONE_PLAYUI);
					mHandler.sendEmptyMessage(GONE_PLAYUI);
				}else{
					mHandler.sendEmptyMessage(VISIBLE_PLAYUI);
					mHandler.sendEmptyMessageDelayed(GONE_PLAYUI,4000);
				}
			}
			return super.onDown(e);
		}
	});
	long times = 0;

	/*
	 * 视频列表的加载adapter
	 */
	private class mListAdapter extends BaseAdapter {
		public mListAdapter(Context context) {
			mContext = context;
		}
		@Override
		public int getCount() {
			if(mList == null) {
				return 0;
			} 
			if (mList.size() != 0) {
				load_tx.setVisibility(View.GONE);
			}else{
				load_tx.setVisibility(View.VISIBLE);
				load_tx.setText(getResources().getString(R.string.nofiletx));
			}
			return mList.size();
		}

		@Override
		public Object getItem(int position) {
			return null;
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View v;
			v = LayoutInflater.from(mContext).inflate(R.layout.video_item, parent, false);
			if(convertView == null) {
				v = newView(parent,v);
			} else {
				v = convertView;
			}
			bindView(v, position, parent);
			return v;
		}
		private class ViewHolder {
			ImageView icon;
			TextView title;
			TextView size;
			TextView time;
			TextView type;
			ImageView mCollection;
		}

		private View newView(ViewGroup parent,View v) {
			ViewHolder holder = new ViewHolder();
			holder.icon = (ImageView) v.findViewById(R.id.video_bitmap);
			holder.title = (TextView) v.findViewById(R.id.video_title);
			holder.size = (TextView) v.findViewById(R.id.video_size);
			holder.time = (TextView) v.findViewById(R.id.video_time);
			holder.type = (TextView) v.findViewById(R.id.video_type);
			holder.mCollection = (ImageView) v.findViewById(R.id.im_coll);
			v.setTag(holder);
			return v;
		}

		@SuppressLint("NewApi")
		private void bindView(View v, final int position, ViewGroup parent) {
			final ViewHolder holder = (ViewHolder) v.getTag();
			holder.title.setText(mList.get(position).getName());
			holder.time.setText(chengTimeShow(mList.get(position).getDuration()));
			holder.size.setText(FileSizeUtil.formatFileSize(mList.get(position).getSize(), false));
			holder.type.setText(mList.get(position).getMediaType());
			Glide.with(mContext).load(mList.get(position).getUrl()).placeholder(R.drawable.ic_launcher).into(holder.icon); //利用Glide插件加载视频缩略图
			holder.mCollection.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View arg0) {
					if (CollectionUtils.itBeenCollected(getApplicationContext(), mList.get(position).getUrl(), mCollectionMusicList)) {
						holder.mCollection.getBackground().setLevel(0);
						CollectionUtils.removeMusicFromCollectionList(mList.get(position).getUrl(), mCollectionMusicList);
					}else{
						holder.mCollection.getBackground().setLevel(1);
						CollectionUtils.addMusicToCollectionList(mList.get(position), mCollectionMusicList);
					}
					adapter.notifyDataSetChanged();
				}
			});
			if (CollectionUtils.itBeenCollected(getApplicationContext(), mList.get(position).getUrl(), mCollectionMusicList)) {
				holder.mCollection.getBackground().setLevel(1);
			}else{
				holder.mCollection.getBackground().setLevel(0);
			}
		}
		private Context mContext;
	}
	/*
	 * 列表界面与播放界面的切换
	 */
	private void showVideo() {
		Animation animBottomInt = AnimationUtils.loadAnimation(this,
				R.anim.bottom_int);
		mPlayerView.setVisibility(View.VISIBLE);
		mPlayerView.startAnimation(animBottomInt);
		mListView.setVisibility(View.GONE);
		isPlayerScape = true;
		video_view.addView(mMediaPlayer);
		mMediaPlayer.start();
		mHandler.removeMessages(GONE_PLAYUI);
		mHandler.sendEmptyMessageDelayed(GONE_PLAYUI,4000);
	};

	private void showList(){
		Animation animBottomOut = AnimationUtils.loadAnimation(this,
				R.anim.bottom_out);
		mPlayerView.startAnimation(animBottomOut);
		mListView.setVisibility(View.VISIBLE);
		mHandler.sendEmptyMessage(VISIBLE_PLAYUI);
		mHandler.postDelayed(new Runnable() {
			@Override
			public void run() {
				video_view.removeAllViews();
				mPlayerView.setVisibility(View.INVISIBLE);
				isPlayerScape = false;
			}
		},400);
		mHandler.removeMessages(GONE_PLAYUI);
		getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
	}

	@Override
	public void onBackPressed() {
		if (mPlayerView.getVisibility() == View.VISIBLE) {
			showList();
		}else{
			finish();
		}
	}

	/*
	 * 换算总长度 00:00格式显示
	 */
	private String chengTimeShow(int l){
		int totaltime = l / 1000;
		int stotaltime = totaltime;
		int mtotaltime = stotaltime / 60;
		int htotaltime = mtotaltime / 60;
		stotaltime %= 60;
		mtotaltime %= 60;
		htotaltime %= 24;
		if(htotaltime == 0) {
			return String.format(Locale.US, "%d:%02d", mtotaltime, stotaltime);
		} else {
			return String.format(Locale.US, "%d:%02d:%02d", htotaltime, mtotaltime, stotaltime);
		}
	}

	/**列表单选对话框**/
	final String[] items = {
			"全列表",
			"收藏列表" };
	int mWhich = 0;
	private void showSingleChoiceDialog(){
		AlertDialog.Builder singleChoiceDialog =
				new AlertDialog.Builder(this);
		singleChoiceDialog.setTitle(getString(R.string.action_settings));
		// 第二个参数是默认选项，此处设置为0
		singleChoiceDialog.setSingleChoiceItems(items, ShowMode,
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						mWhich = which;
					}
				});
		singleChoiceDialog.setPositiveButton("Positive",
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						ShowMode = mWhich;
						switch (mWhich){
							case 0:
								mList = mALLMusicList;
								SharedPreferencesUtils.setIntPref(getApplicationContext(), "SHOWMODE", "view", 0);
								break;
							case 1:
								mList = mCollectionMusicList;
								SharedPreferencesUtils.setIntPref(getApplicationContext(), "SHOWMODE", "view", 1);
								break;
						}
						((Button)findViewById(R.id.setting_bt)).setText(ShowMode==0?R.string.list_all:R.string.list_collection);
						adapter.notifyDataSetChanged();
					}
				});
		singleChoiceDialog.show();
	}
	/*private void setSurfaceView(){

        //		if (mVideoWidth > mPhoneWidth || mVideoWidth > mPhoneHeigth) {
        // 如果video的宽或者高超出了当前屏幕的大小，则要进行缩放
        float wRatio = (float) mVideoWidth / (float) mPhoneWidth;
        float hRatio = (float) mVideoHeight / (float) mPhoneHeigth;

        // 选择大的一个进行缩放
        float ratio = Math.max(wRatio, hRatio);
        mVideoWidth = (int) Math.ceil((float) mVideoWidth / ratio);
        mVideoHeight = (int) Math.ceil((float) mVideoHeight / ratio);

        if (mVideoHeight != 0 && mVideoWidth != 0) {
            mSurfaceView.getHolder().setFixedSize(mVideoWidth, mVideoHeight);
            mSurfaceView.requestLayout();
            //			}
        }
    }*/
}
