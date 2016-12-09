package com.goldfist.nollygold;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.URL;
import java.net.URLConnection;
import java.security.spec.AlgorithmParameterSpec;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.widget.Toast;

//import com.goldfist.nollygold.MainActivity.DownloadReciever;

public class DownloadService extends IntentService {

	String downloadUrl, itemcode, destUrl, title;

	private static byte[] iv = { (byte) 0xB2, (byte) 0x12, (byte) 0xD5,
			(byte) 0xB2, (byte) 0x44, (byte) 0x21, (byte) 0xC3, (byte) 0xC3,
			(byte) 0xB2, (byte) 0x12, (byte) 0xD5, (byte) 0xB2, (byte) 0x44,
			(byte) 0x21, (byte) 0xC3, (byte) 0xC3 };

	public long index = 0;
	public long downloadFileSize = 0;
	static Context _context;

	public DownloadService() {
		super("NollyGold Download Service");
		// TODO Auto-generated constructor stub
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TBD
		_context = this.getApplicationContext();

		downloadUrl = intent.getStringExtra("downloadurl");
		itemcode = intent.getStringExtra("itemcode");
		destUrl = intent.getStringExtra("desturl");
		title = intent.getStringExtra("title");
		// downloadFile(downloadUrl);
		new DownloadFileAsync().execute(downloadUrl);
		Toast.makeText(getApplicationContext(), "Download started...",
				Toast.LENGTH_LONG).show();

		return Service.START_NOT_STICKY;
	}

	/*
	 * void sendTheBroadcast(){ Intent broadcastIntent = new Intent();
	 * broadcastIntent.setAction(DownloadReciever.ACTION_RESP);
	 * broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
	 * broadcastIntent.putExtra("msg", "'" + title + "' Download Complete.");
	 * sendBroadcast(broadcastIntent); }
	 */

	@Override
	protected void onHandleIntent(Intent intent) {
		// TODO Auto-generated method stub
		/*
		 * Intent broadcastIntent = new Intent();
		 * broadcastIntent.setAction(DownloadReciever.ACTION_RESP);
		 * broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
		 * broadcastIntent.putExtra("msg", "'" + title +
		 * "' Download Complete."); sendBroadcast(broadcastIntent);
		 */
	}

	void downloadFile(String downloadurl) {

		String path;

		try {
			URL url = new URL(downloadurl);

			InputStream input = new BufferedInputStream(url.openStream());

			ObjectInputStream objs = new ObjectInputStream(getResources()
					.openRawResource(R.raw.key));
			SecretKey sKey = (SecretKey) objs.readObject();

			AlgorithmParameterSpec paramSpec = new IvParameterSpec(iv);

			Cipher cipher = Cipher.getInstance("AES/CFB8/NoPadding");

			cipher.init(Cipher.ENCRYPT_MODE, sKey, paramSpec);

			File encFile = new File(destUrl);

			/*
			 * if (encFile.exists()) { encFile.delete();
			 * encFile.createNewFile(); }
			 */

			path = encFile.getAbsolutePath();

			FileOutputStream out = new FileOutputStream(path);
			CipherInputStream cis = new CipherInputStream(input, cipher);

			byte data[] = new byte[1024];
			int count = 0;

			while ((count = cis.read(data)) != -1) {

				out.write(data, 0, count);
			}
			// flushing output
			out.flush();

			cis.close();
			out.close();
			input.close();
			objs.close();
		} catch (Exception e) {
		}
	}

	class DownloadFileAsync extends AsyncTask<String, Integer, String> {

		int download_ID = 0;
		int size = 0;
		String path, code, tit, dUrl;

		@Override
		protected String doInBackground(String... sUrl) {
			// TODO Auto-generated method stub
			// downloadFile(url[0]);
			dUrl = destUrl;
			code = itemcode;
			tit = title;
			download_ID = code.hashCode();
			int p = 0;

			try {
				URL url = new URL(sUrl[0]);

				URLConnection con = url.openConnection();
				con.connect();
				size = con.getContentLength();

				InputStream input = new BufferedInputStream(url.openStream());

				ObjectInputStream objs = new ObjectInputStream(getResources()
						.openRawResource(R.raw.key));
				SecretKey sKey = (SecretKey) objs.readObject();

				AlgorithmParameterSpec paramSpec = new IvParameterSpec(iv);

				Cipher cipher = Cipher.getInstance("AES/CFB8/NoPadding");

				cipher.init(Cipher.ENCRYPT_MODE, sKey, paramSpec);

				File encFile = new File(dUrl);

				/*
				 * if (encFile.exists()) { encFile.delete();
				 * encFile.createNewFile(); }
				 */

				path = encFile.getAbsolutePath();

				FileOutputStream out = new FileOutputStream(path);
				CipherInputStream cis = new CipherInputStream(input, cipher);

				byte data[] = new byte[8092];
				int count = 0;

				while ((count = cis.read(data)) != -1) {

					out.write(data, 0, count);
					p += count;
					publishProgress(p);
				}
				// flushing output
				out.flush();

				cis.close();
				out.close();
				input.close();
				objs.close();
			} catch (Exception e) {
			}
			return null;
		}

		@Override
		protected void onProgressUpdate(Integer... values) {

			int progress = values[0];

			String text = "'" + tit + "'";
			NotificationCompat.Builder nBuilder = new NotificationCompat.Builder(
					_context).setSmallIcon(R.drawable.nolly_icon)
					.setContentTitle("NollyGold Download")
					.setProgress(size, progress, false).setContentText(text)
					.setContentInfo(
							"Total Size: "
									+ String.valueOf((int) Math.floor(size / 1024 / 1024)
											+ "MB"));

			Intent intent = new Intent(_context, DetailsActivity.class);
			intent.putExtra("itemCode", code);

			TaskStackBuilder stackBuilder = TaskStackBuilder.create(_context);
			stackBuilder.addParentStack(DetailsActivity.class);
			stackBuilder.addNextIntent(intent);

			PendingIntent pi = stackBuilder.getPendingIntent(0,
					PendingIntent.FLAG_UPDATE_CURRENT);
			nBuilder.setContentIntent(pi);

			NotificationManager nm = (NotificationManager) _context
					.getSystemService(Context.NOTIFICATION_SERVICE);
			nm.notify(download_ID, nBuilder.build());
		}

		@Override
		protected void onPostExecute(String arg) {

			DatabaseHelper db = new DatabaseHelper(getApplicationContext());
			db.storeLibraryData(code, tit, dUrl, download_ID);

			String text = "'" + tit + "' Complete.";
			NotificationCompat.Builder nBuilder = new NotificationCompat.Builder(
					_context)
					.setSmallIcon(R.drawable.nolly_icon)
					.setContentTitle("NollyGold Download")
					.setProgress(size, size, false)
					.setContentText(text)
					.setContentInfo(
							"Total Size: "
									+ String.valueOf((int) Math.floor(size / 1024 / 1024)
											+ "MB"));

			Intent intent = new Intent(_context, DetailsActivity.class);
			intent.putExtra("itemCode", code);

			TaskStackBuilder stackBuilder = TaskStackBuilder.create(_context);
			stackBuilder.addParentStack(DetailsActivity.class);
			stackBuilder.addNextIntent(intent);

			PendingIntent pi = stackBuilder.getPendingIntent(0,
					PendingIntent.FLAG_UPDATE_CURRENT);
			nBuilder.setContentIntent(pi);

			NotificationManager nm = (NotificationManager) _context
					.getSystemService(Context.NOTIFICATION_SERVICE);
			nm.notify(download_ID, nBuilder.build());

			Toast.makeText(getApplicationContext(), "Download Completed.",
					Toast.LENGTH_LONG).show();
			/*
			 * Intent broadcastIntent = new Intent();
			 * broadcastIntent.setAction(DownloadReciever.ACTION_RESP);
			 * broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
			 * broadcastIntent.putExtra("msg", "'" + title +
			 * "' Download Complete."); sendBroadcast(broadcastIntent);
			 */
		}
	}

}
