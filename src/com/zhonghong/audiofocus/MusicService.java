package com.zhonghong.audiofocus;

import java.io.IOException;

import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.AudioManager.OnAudioFocusChangeListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;


public class MusicService extends Service {

    private AudioManager mAm;
    private boolean isPlaymusic;
    private String url;
    private MediaPlayer mediaPlayer;

    private static final int AUDIO_STREAM_TYPE = AudioManager.STREAM_MUSIC;
    private static final int REQUEST_AUDIO_FOCUS_TYPE = AudioManager.AUDIOFOCUS_GAIN;
    
    private int mAudioStatus = AUDIO_STATUS_IDLE;
    public static final int AUDIO_STATUS_IDLE = 0;
    public static final int AUDIO_STATUS_INITIALIZED = 1;
    public static final int AUDIO_STATUS_PREPARING = 2;
    public static final int AUDIO_STATUS_PREPARED = 3;
    public static final int AUDIO_STATUS_STARTED = 4;
    public static final int AUDIO_STATUS_PAUSED = 5;
    public static final int AUDIO_STATUS_PLAYBACKCOMPLETED = 6;
    public static final int AUDIO_STATUS_STOPED = 7;
	private static final String TAG = "leo";
    
    @Override
    public void onCreate() {
        super.onCreate();
        mAm = (AudioManager) getSystemService(AUDIO_SERVICE);
    }

    @Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub
    	if (intent != null) {
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                isPlaymusic = bundle.getBoolean("isPlay", true);
                url = bundle.getString("url");
                if (isPlaymusic)
                {
                	playAudio();
                }
                else
                {
                    stopAudio();
//                	pauseAudio();
                	
                }
            }
        }
		return super.onStartCommand(intent, flags, startId);
	}

    OnAudioFocusChangeListener afChangeListener = new OnAudioFocusChangeListener() {
        public void onAudioFocusChange(int focusChange) {
        	
        	Log.i(TAG, "onAudioFocusChange...");
        	dump();
        	
            if (focusChange == AudioManager.AUDIOFOCUS_GAIN) {
            	// 获得音频焦点
                // Resume playback
            	startAudio();
            } 
            else if (focusChange == AudioManager.AUDIOFOCUS_LOSS) {
            	// 长久的失去音频焦点，释放MediaPlayer
                mAm.abandonAudioFocus(afChangeListener);
                // Stop playback
                stopAudio();
            } 
            else if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT) {
            	// 暂时失去音频焦点，暂停播放等待重新获得音频焦点 
            	// Pause playback
                 pauseAudio();
             }
            else if(focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK)
            {
            	// 失去音频焦点，无需停止播放，降低声音即可也可以暂停播放。
//            	lowerAudioVolume();
            	pauseAudio();
            }

        }
    };

	private boolean requestFocus() {
		// Request audio focus for playback
		int result = mAm.requestAudioFocus(afChangeListener,
				// Use the music stream.
				AUDIO_STREAM_TYPE,
				// Request permanent focus.
				REQUEST_AUDIO_FOCUS_TYPE);
		Log.i("leo", "result="+result);
		return result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED;
	}

    
    
    private void lowerAudioVolume()
    {
    	Log.i(TAG, "lowerAudioVolume...");
    	dump();
    	
    	if(mAudioStatus == AUDIO_STATUS_STARTED)
    	{
    		if (mediaPlayer != null) {
    			mediaPlayer.setVolume(0.1f, 0.1f);
    		}
    	}
    }
    
    MediaPlayer.OnCompletionListener completionListener = new MediaPlayer.OnCompletionListener() {

        @Override
        public void onCompletion(MediaPlayer player) {
            if(!player.isLooping()){
                mAm.abandonAudioFocus(afChangeListener);
                mAudioStatus = AUDIO_STATUS_PLAYBACKCOMPLETED;
            }
        }
    };
    
    private void playAudio() {
    	
    	Log.i(TAG, "playAudio...");
    	dump();
        
    	if (requestFocus()) {
            if (mAudioStatus == AUDIO_STATUS_IDLE) {
            	Log.i("leo", "url="+url);
                mediaPlayer = new MediaPlayer();
//              mediaPlayer.reset();
//              mAudioStatus = AUDIO_STATUS_IDLE;
                
                // 设置音频流的类型
                mediaPlayer.setAudioStreamType(AUDIO_STREAM_TYPE);
                // 设置播放完成监听
                mediaPlayer.setOnCompletionListener(completionListener);
                //设定CUP锁定
                mediaPlayer.setWakeMode(getApplicationContext(),
                		PowerManager.PARTIAL_WAKE_LOCK);
                
                try {
					mediaPlayer.setDataSource(url);
				} catch (IllegalArgumentException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					Log.i(TAG, "set data source error");
				} catch (SecurityException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IllegalStateException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
                mAudioStatus = AUDIO_STATUS_INITIALIZED;
                
                // 设置文件装载监听
                mediaPlayer.setOnPreparedListener(new OnPreparedListener() {
                	@Override
                	public void onPrepared(MediaPlayer mp) {
                		// 装载完毕 开始播放流媒体
                		mAudioStatus = AUDIO_STATUS_PREPARED;
                		
                		startAudio();
                	}
                });
                
            }

            if(mAudioStatus == AUDIO_STATUS_INITIALIZED 
            		|| mAudioStatus == AUDIO_STATUS_STOPED )
            {
            	//通过同步的方式装载媒体资源
				//mediaPlayer.prepare();
                //mAudioStatus = AUDIO_STATUS_PREPARED;
				
				// 通过异步的方式装载媒体资源
				mediaPlayer.prepareAsync();
				mAudioStatus = AUDIO_STATUS_PREPARING;
            }
            
            
            startAudio();
            
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        releaseAudio();
    }

    @Override
    public IBinder onBind(Intent arg0) {
        // TODO Auto-generated method stub
        return null;
    }
    
    private void dump()
    {
    	Log.i(TAG, "current audio status is = "+audioStatusToString());
    }
    private String  audioStatusToString()
    {
    	switch(mAudioStatus)
    	{
    	case AUDIO_STATUS_IDLE:
    		return "audio_idle";
    	case AUDIO_STATUS_INITIALIZED:
    		return "audio_initialized";
    	case AUDIO_STATUS_PREPARING:
    		return "audio_preparing";
    	case AUDIO_STATUS_PREPARED:
    		return "audio_prepared";
    	case AUDIO_STATUS_STARTED:
    		return "audio_started";
    	case AUDIO_STATUS_PAUSED:
    		return "audio_paused";
    	case AUDIO_STATUS_PLAYBACKCOMPLETED:
    		return "audio_playbackcompleted";
    	case AUDIO_STATUS_STOPED:
    		return "audio_stoped";
    	}
    	return "audio_null";
    }
    
    
    private void startAudio()
    {
    	Log.i(TAG, "startAudio...");
    	dump();
    	
    	if(mAudioStatus == AUDIO_STATUS_PAUSED
        		|| mAudioStatus == AUDIO_STATUS_PREPARED
        		|| mAudioStatus == AUDIO_STATUS_PLAYBACKCOMPLETED)
        {
    		if (mediaPlayer != null) {
    			mediaPlayer.start();
    			mAudioStatus = AUDIO_STATUS_STARTED;
    			// 还原音量
    			mediaPlayer.setVolume(1.0f, 1.0f);
    		}
        }
    }
    
    private void pauseAudio() {
    	
    	Log.i(TAG, "pauseAudio...");
    	dump();
    	
    	if(mAudioStatus == AUDIO_STATUS_STARTED)
    	{
    		if (mediaPlayer != null && mediaPlayer.isPlaying()) {
    			mediaPlayer.pause();
    			mAudioStatus = AUDIO_STATUS_PAUSED;
    		}
    	}
    	else
    	{
    		Log.i("leo", "resumeAudio error, current audio status = "+mAudioStatus);
    	}
    }
    
    private void stopAudio() {
    	
    	Log.i(TAG, "stopAudio...");
    	dump();
    	
    	if(mAudioStatus == AUDIO_STATUS_PREPARED 
			|| mAudioStatus == AUDIO_STATUS_STARTED
			|| mAudioStatus == AUDIO_STATUS_PAUSED
			|| mAudioStatus == AUDIO_STATUS_PLAYBACKCOMPLETED )
    	{
    		if (mediaPlayer != null) {
    			mediaPlayer.stop();
    			mAudioStatus = AUDIO_STATUS_STOPED;
    		}
    	}
    }
    
    private void releaseAudio()
    {
    	Log.i(TAG, "releaseAudio...");
    	dump();
    	
    	mAm.abandonAudioFocus(afChangeListener);
        if (mediaPlayer != null)
        {
        	mediaPlayer.release();
        	mAudioStatus = AUDIO_STATUS_IDLE;
        	mediaPlayer = null;
        }
    }
    
  }

