package com.zhonghong.audiofocus;

import java.io.IOException;

import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.AudioManager.OnAudioFocusChangeListener;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PowerManager;

public class MusicService extends Service {

    private AudioManager mAm;
    private boolean isPlaymusic;
    private String url;
    private MediaPlayer mediaPlayer;

    private static final int AUDIO_STREAM_TYPE = AudioManager.STREAM_MUSIC;
    private static final int REQUEST_AUDIO_FOCUS_TYPE = AudioManager.AUDIOFOCUS_GAIN;
    
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
                	play();
                }
                else
                {
//                    stop();
                	pause();
                	
                }
            }
        }
		return super.onStartCommand(intent, flags, startId);
	}

    OnAudioFocusChangeListener afChangeListener = new OnAudioFocusChangeListener() {
        public void onAudioFocusChange(int focusChange) {
            if (focusChange == AudioManager.AUDIOFOCUS_GAIN) {
            	// 获得音频焦点
                // Resume playback
                resume();
            } 
            else if (focusChange == AudioManager.AUDIOFOCUS_LOSS) {
            	// 长久的失去音频焦点，释放MediaPlayer
                mAm.abandonAudioFocus(afChangeListener);
                // Stop playback
                stop();
            } 
            else if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT) {
            	// 暂时失去音频焦点，暂停播放等待重新获得音频焦点 
            	// Pause playback
                 pause();
             }
            else if(focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK)
            {
            	// 失去音频焦点，无需停止播放，降低声音即可也可以暂停播放。
//            	lowerVolume();
            	pause();
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
		return result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED;
	}

    private void resume() {
        if (mediaPlayer != null) {
            mediaPlayer.start();
            // 还原音量
            mediaPlayer.setVolume(1.0f, 1.0f);
        }
    }

    private void pause() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
        }
    }
    
    private void lowerVolume()
    {
    	if (mediaPlayer != null) {
            mediaPlayer.setVolume(0.1f, 0.1f);
        }
    }
    
    MediaPlayer.OnCompletionListener completionListener = new MediaPlayer.OnCompletionListener() {

        @Override
        public void onCompletion(MediaPlayer player) {
            if(!player.isLooping()){
                mAm.abandonAudioFocus(afChangeListener);
            }
        }
    };
    private void play() {
        if (requestFocus()) {
            if (mediaPlayer == null) {
                try {
                    mediaPlayer = new MediaPlayer();
                    mediaPlayer.setDataSource(url);
                    mediaPlayer.prepare();
                    mediaPlayer.setOnCompletionListener(completionListener);
                    //设定CUP锁定
    				mediaPlayer.setWakeMode(getApplicationContext(),
    						PowerManager.PARTIAL_WAKE_LOCK);
    				
    				// 设置音频流的类型
    				mediaPlayer.setAudioStreamType(AUDIO_STREAM_TYPE);
    				/*
					// 通过异步的方式装载媒体资源
					mediaPlayer.prepareAsync();
					mediaPlayer.setOnPreparedListener(new OnPreparedListener() {
						@Override
						public void onPrepared(MediaPlayer mp) {
							// 装载完毕 开始播放流媒体
							if (!mediaPlayer.isPlaying()) {
								mediaPlayer.start();
								Toast.makeText(MusicService.this, "开始播放", Toast.LENGTH_SHORT)
										.show();
							}
						}
					});*/
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if (!mediaPlayer.isPlaying()) {
                mediaPlayer.start();
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null)
            mediaPlayer.release();
    }

    private void stop() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
        }
    }
    @Override
    public IBinder onBind(Intent arg0) {
        // TODO Auto-generated method stub
        return null;
    }
  }

