package com.zhonghong.audiofocus;

import java.io.File;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.wifi.WifiManager.WifiLock;
import android.os.Bundle;
import android.os.Environment;
import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class MainActivity extends Activity {
	protected static final String TAG = "alert";
	private EditText et_path;
	private Button btn_play, btn_pause, btn_replay, btn_stop;
	private MediaPlayer mediaPlayer;
	private WifiLock wifiLock;
	private AudioManager audioManager;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		btn_play = (Button) findViewById(R.id.btn_play);
		btn_pause = (Button) findViewById(R.id.btn_pause);
		btn_replay = (Button) findViewById(R.id.btn_replay);
		btn_stop = (Button) findViewById(R.id.btn_stop);

		btn_play.setOnClickListener(click);
		btn_pause.setOnClickListener(click);
		btn_replay.setOnClickListener(click);
		btn_stop.setOnClickListener(click);
	}

	private View.OnClickListener click = new View.OnClickListener() {

		@Override
		public void onClick(View v) {

			switch (v.getId()) {
			case R.id.btn_play:
			{
				File path = Environment.getExternalStorageDirectory();
				File file = new File(path, "KuwoMusic/music/ฐ๋านว้-นุณþาซ.aac");
				
				Intent intent = new Intent(MainActivity.this, MusicService.class);
				intent.putExtra("isPlay", true);
				intent.putExtra("url", file.getAbsolutePath());
				MainActivity.this.startService(intent);
			} break;
			case R.id.btn_pause:
			{
				Intent intent = new Intent(MainActivity.this, MusicService.class);
				intent.putExtra("isPlay", false);
				MainActivity.this.startService(intent);
			} break;
			default:
				break;
			}
		}
	};
}
