package com.goldfist.nollygold;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.Toast;

import com.google.android.youtube.player.YouTubeBaseActivity;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayer.ErrorReason;
import com.google.android.youtube.player.YouTubePlayer.PlayerStyle;
import com.google.android.youtube.player.YouTubePlayer.Provider;
import com.google.android.youtube.player.YouTubePlayerView;

public class VideoStreamActivity extends YouTubeBaseActivity implements
		YouTubePlayer.OnInitializedListener,
		YouTubePlayer.OnFullscreenListener, SeekBar.OnSeekBarChangeListener, YouTubePlayer.PlayerStateChangeListener {

	Utilities utils;
	YouTubePlayerView playerView;
	YouTubePlayer yPlayer;
	ProgressBar progressBar;
	String video_id;
	LinearLayout mainControllerLayout;
	ImageButton playButton, forwardButton, backButton;
	SeekBar seekBar;
	Handler mHandler = new Handler();

	public static final int PORTRAIT_ORIENTATION = Build.VERSION.SDK_INT < 9 ? ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
			: ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT;

	public static final int LANDSCAPE_ORIENTATION = Build.VERSION.SDK_INT < 9 ? ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
			: ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_video_stream);

		Intent intent = getIntent();
		video_id = intent.getStringExtra("video_id");

		utils = new Utilities();

		progressBar = (ProgressBar) findViewById(R.id.streamLoadProgressBar);

		seekBar = (SeekBar) findViewById(R.id.streamSeekBar);
		seekBar.setOnSeekBarChangeListener(this);

		mainControllerLayout = (LinearLayout) findViewById(R.id.mainControllerLayout);

		playButton = (ImageButton) findViewById(R.id.playButton);
		playButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				if (yPlayer.isPlaying()) {
					yPlayer.pause();
					playButton.setImageResource(R.drawable.play_normal);
				} else {
					yPlayer.play();
					playButton.setImageResource(R.drawable.pause_normal);
				}

			}
		});
		forwardButton = (ImageButton) findViewById(R.id.forwardButton);
		forwardButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				int newTime = yPlayer.getCurrentTimeMillis() + 5000;

				if (newTime <= yPlayer.getDurationMillis())
					yPlayer.seekToMillis(yPlayer.getCurrentTimeMillis() + 5000);
				else
					yPlayer.seekToMillis(yPlayer.getDurationMillis());

			}
		});
		backButton = (ImageButton) findViewById(R.id.backButton);
		backButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				int newTime = yPlayer.getCurrentTimeMillis() - 5000;

				if (newTime >= 0)
					yPlayer.seekToMillis(yPlayer.getCurrentTimeMillis() - 5000);
				else
					yPlayer.seekToMillis(0);

			}
		});

		playerView = (YouTubePlayerView) findViewById(R.id.youtube_view);
		playerView.initialize("AIzaSyAJAZvhY6AVLGOp71RqRPBpaybEZyXlWyM", this);
	}

	/*@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.you_tube_stream, menu);
		return true;
	}*/

	@Override
	public void onInitializationFailure(Provider arg0,
			YouTubeInitializationResult arg1) {
		// TODO Auto-generated method stub
		mainControllerLayout.setVisibility(View.INVISIBLE);
		progressBar.setVisibility(ProgressBar.INVISIBLE);
		playerView.setVisibility(YouTubePlayerView.GONE);
		mainControllerLayout.setVisibility(LinearLayout.INVISIBLE);
		Toast.makeText(this, "Initialization failed.", Toast.LENGTH_LONG)
				.show();
	}

	@Override
	public void onInitializationSuccess(Provider arg0, YouTubePlayer player,
			boolean wasRestored) {
		// TODO Auto-generated method stub
		progressBar.setVisibility(ProgressBar.GONE);
		Toast.makeText(this, "Initialization Successful.", Toast.LENGTH_LONG)
				.show();
		yPlayer = player;
		yPlayer.setPlayerStateChangeListener(this);
		yPlayer.setPlayerStyle(PlayerStyle.CHROMELESS);

		if (!wasRestored) {
			yPlayer.loadVideo(video_id);
			playButton.setImageResource(R.drawable.pause_normal);
			updateProgressBar();
		}
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);

		if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
			if (yPlayer != null)
				yPlayer.setFullscreen(true);
		} else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
			if (yPlayer != null)
				yPlayer.setFullscreen(false);
		}

	}

	@Override
	public void onFullscreen(boolean fullSize) {
		// TODO Auto-generated method stub
		if (fullSize) {
			setRequestedOrientation(LANDSCAPE_ORIENTATION);
		} else {
			setRequestedOrientation(PORTRAIT_ORIENTATION);
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {

		if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
			AudioManager audioManager = (AudioManager) getBaseContext()
					.getSystemService(Context.AUDIO_SERVICE);
			audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC,
					AudioManager.ADJUST_RAISE,
					AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE
							| AudioManager.FLAG_SHOW_UI);
			return true;
		} else if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
			AudioManager audioManager = (AudioManager) getBaseContext()
					.getSystemService(Context.AUDIO_SERVICE);
			audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC,
					AudioManager.ADJUST_LOWER,
					AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE
							| AudioManager.FLAG_SHOW_UI);
			return true;
		}

		return super.onKeyDown(keyCode, event);

	}

	public void updateProgressBar() {
		mHandler.postDelayed(mUpdateTimeTask, 100);
	}

	/*@Override
	public boolean onTouchEvent(MotionEvent event) {

		return super.onTouchEvent(event);
	}*/

	private Runnable mUpdateTimeTask = new Runnable() {
		public void run() {
			long totalDuration = yPlayer.getDurationMillis();
			long currentDuration = yPlayer.getCurrentTimeMillis();

			// Displaying Total Duration time
			// songTotalDurationLabel.setText(""+utils.milliSecondsToTimer(totalDuration));
			// Displaying time completed playing
			// songCurrentDurationLabel.setText(""+utils.milliSecondsToTimer(currentDuration));

			// Updating progress bar
			int progress = (int) (utils.getProgressPercentage(currentDuration,
					totalDuration));
			// Log.d("Progress", ""+progress);
			seekBar.setProgress(progress);

			// Running this thread after 100 milliseconds
			mHandler.postDelayed(this, 100);
		}
	};

	@Override
	public void onProgressChanged(SeekBar arg0, int arg1, boolean arg2) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onStartTrackingTouch(SeekBar arg0) {
		// TODO Auto-generated method stub
		mHandler.removeCallbacks(mUpdateTimeTask);

	}

	@Override
	public void onStopTrackingTouch(SeekBar arg0) {
		// TODO Auto-generated method stub
		mHandler.removeCallbacks(mUpdateTimeTask);
		int totalDuration = yPlayer.getDurationMillis();
		int currentPosition = utils.progressToTimer(seekBar.getProgress(),
				totalDuration);

		// forward or backward to certain seconds
		yPlayer.seekToMillis(currentPosition);

		// update timer progress again
		updateProgressBar();

	}

	@Override
	public void onDestroy() {
		mHandler.removeCallbacks(mUpdateTimeTask);
		yPlayer.release();
		super.onDestroy();
	}

	@Override
	public void onAdStarted() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onError(ErrorReason arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onLoaded(String arg0) {
		// TODO Auto-generated method stub
		mainControllerLayout.setVisibility(View.VISIBLE);
	}

	@Override
	public void onLoading() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onVideoEnded() {
		// TODO Auto-generated method stub
		playButton.setImageResource(R.drawable.play_normal);		
	}

	@Override
	public void onVideoStarted() {
		// TODO Auto-generated method stub
		//playButton.setImageResource(R.drawable.pause_normal);
		
	}
}
