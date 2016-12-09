package com.goldfist.nollygold;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.security.spec.AlgorithmParameterSpec;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.Toast;
import android.widget.VideoView;

public class VideoPlayerActivity extends Activity {

	static String video_path, play_path, itemcode;
	static VideoView videoView;
	MediaController mediaController;
	Handler loadVideoHandler;
	static boolean isUrl = false, isProcessing = false;
	static String processingPath = null;
	static boolean cancelProcessing = false;
	static ProgressBar progressBar;
	static LinearLayout pLayT;
	static Activity mContext;
	static int fSize, currProgress;
	
	
	static InputStream input;

	static ObjectInputStream objs;

	static FileOutputStream out;

	static CipherInputStream cis;
	// static ProgressBar pBar;
	private static byte[] iv = { (byte) 0xB2, (byte) 0x12, (byte) 0xD5,
			(byte) 0xB2, (byte) 0x44, (byte) 0x21, (byte) 0xC3, (byte) 0xC3,
			(byte) 0xB2, (byte) 0x12, (byte) 0xD5, (byte) 0xB2, (byte) 0x44,
			(byte) 0x21, (byte) 0xC3, (byte) 0xC3 };

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_video_player);

		mContext = this;
		Intent intent = getIntent();
		video_path = intent.getStringExtra("video_path");
		itemcode = intent.getStringExtra("itemcode");
		isUrl = intent.getBooleanExtra("isUrl", false);

		videoView = (VideoView) findViewById(R.id.videoView);
		progressBar = (ProgressBar) findViewById(R.id.videoLoadProgressBar);
		pLayT = (LinearLayout) findViewById(R.id.pLayT);

		mediaController = new MediaController(this);
		mediaController.setAnchorView(videoView);
		videoView.setMediaController(mediaController);

		isProcessing = false;

		loadVideoHandler = new LoadVideoHandler();

		// new ProcessVideoAsync().execute(video_path);

		if (!isUrl) {
			File ff = new File(video_path);
			fSize = (int) ff.length();
			progressBar.setMax(fSize);
			
			currProgress = 0;
		}

		Thread loadVideoThread = new Thread() {
			public void run() {

				if (isUrl)
					play_path = video_path;
				else
					play_path = loadVideo();

				loadVideoHandler.sendEmptyMessage(0);
			}
		};
		loadVideoThread.start();

	}

	String loadVideo() {

		isProcessing = true;

		try {
			InputStream input = new FileInputStream(video_path);

			ObjectInputStream objs = new ObjectInputStream(getResources()
					.openRawResource(R.raw.key));
			SecretKey sKey = (SecretKey) objs.readObject();

			Cipher cipher = Cipher.getInstance("AES/CFB8/NoPadding");

			AlgorithmParameterSpec paramSpec = new IvParameterSpec(iv);

			cipher.init(Cipher.DECRYPT_MODE, sKey, paramSpec);

			File temp = File.createTempFile(itemcode, ".enc", getExternalCacheDir());
			temp.deleteOnExit();

			processingPath = temp.getAbsolutePath();

			FileOutputStream out = new FileOutputStream(processingPath);

			CipherInputStream cis = new CipherInputStream(input, cipher);

			byte data[] = new byte[8092];
			int count = 0, p=0;

			while ((count = cis.read(data)) != -1) {				
				// writing data to file
				out.write(data, 0, count);
				currProgress += count;
				loadVideoHandler.sendEmptyMessage(0);
				
				if (cancelProcessing){
					/*deleteTemp();
					isProcessing = false;
					return null;*/
					//cancelProcessing = false;
					//processingPath = null;
					break;
				}
			}
			isProcessing = false;
			// flushing output
			out.flush();

			cis.close();
			out.close();
			input.close();
			objs.close();

		} catch (Exception e) {
			return null;
		}

		return processingPath;
	}

	static void deleteTemp() {
		try{
			cis.close();
			out.close();
			input.close();
			objs.close();
		}catch(Exception e){}
		
		try{
			File delTemp = new File(processingPath);
			if (delTemp.exists())
				delTemp.delete();
		}catch(Exception e){}
		
	}

	@Override
	public void onDestroy() {
		deleteTemp();
		super.onDestroy();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {

		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {

			if (isProcessing) {
				AlertDialog.Builder alertDialog = new AlertDialog.Builder(
						mContext);

				// Setting Dialog Title
				alertDialog.setTitle("NollyGold");
				alertDialog
						.setMessage("Video processing in progress. Process will restart from the beginning next time.\nDo you wish to cancel?");
				alertDialog.setPositiveButton("Yes",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int which) {
								cancelProcessing = true;
								//VideoPlayerActivity.this.deleteTemp();
								//VideoPlayerActivity.this.finish();
							}
						});

				// Setting Negative "NO" Button
				alertDialog.setNegativeButton("Cancel",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int which) {
								// User pressed No button. Write Logic Here
								dialog.cancel();

							}
						});
				alertDialog.show();
			} else
				return super.onKeyDown(keyCode, event);
		}

		return true;
		// deleteTemp();

		// return super.onKeyDown(keyCode, event);
	}

	/*
	 * @Override public boolean onCreateOptionsMenu(Menu menu) { // Inflate the
	 * menu; this adds items to the action bar if it is present.
	 * getMenuInflater().inflate(R.menu.video_player, menu); return true; }
	 */

	static class LoadVideoHandler extends Handler {

		public void handleMessage(Message msg) {
			/*
			 * Toast.makeText(mContext, play_path, Toast.LENGTH_LONG) .show();
			 */
			if (!isProcessing) {
				if (cancelProcessing){
					cancelProcessing = false;
					deleteTemp();
					mContext.finish();
				}else{
					if (play_path != null) {
						videoView.setVideoPath(play_path);
						videoView.start();
						pLayT.setVisibility(View.GONE);
					}else{
						Toast.makeText(mContext.getApplicationContext(), "Error Processing Video.",
								Toast.LENGTH_LONG).show();
					}
				}
			}
			else{
				progressBar.setProgress(currProgress);
			}
		}
	}

	/*class ProcessVideoAsync extends AsyncTask<String, Integer, String> {

		int size = 0;

		@Override
		protected String doInBackground(String... sUrl) {
			// TODO Auto-generated method stub
			// downloadFile(url[0]);

			if (isUrl)
				play_path = video_path;
			else {
				// play_path = loadVideo();
				isProcessing = true;

				try {
					InputStream input = new FileInputStream(video_path);

					ObjectInputStream objs = new ObjectInputStream(
							getResources().openRawResource(R.raw.key));
					SecretKey sKey = (SecretKey) objs.readObject();

					Cipher cipher = Cipher.getInstance("AES/CFB8/NoPadding");

					AlgorithmParameterSpec paramSpec = new IvParameterSpec(iv);

					cipher.init(Cipher.DECRYPT_MODE, sKey, paramSpec);

					File temp = File.createTempFile(itemcode, ".enc",
							getCacheDir());
					temp.deleteOnExit();

					play_path = temp.getAbsolutePath();

					FileOutputStream out = new FileOutputStream(play_path);

					CipherInputStream cis = new CipherInputStream(input, cipher);

					byte data[] = new byte[8092];
					int count = 0, p = 0;

					while ((count = cis.read(data)) != -1) {
						// writing data to file
						out.write(data, 0, count);
						publishProgress(p += count);
					}
					// flushing output
					out.flush();

					cis.close();
					out.close();
					input.close();
					objs.close();

				} catch (Exception e) {
					play_path = null;
				}
			}

			return null;
		}

		@Override
		protected void onProgressUpdate(Integer... values) {

			int progress = values[0];
			progressBar.setProgress(progress);
		}

		@Override
		protected void onPostExecute(String arg) {

			isProcessing = false;
			if (play_path != null) {
				videoView.setVideoPath(play_path);
				videoView.start();
				pLayT.setVisibility(View.GONE);
			}

		}
	}*/
}
