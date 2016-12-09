package com.goldfist.nollygold;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Calendar;
import java.util.TimeZone;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class ServiceAccess {

	static SharedPreferences preferenceManager;

	//public String serviceUrl = "http://10.0.2.2/nollygoldmsvcjson/Service.svc/";
	//public String serverRootUrl = "http://10.0.2.2/";

	public String serverRootUrl = "http://www.goldfistng.com/";
	public String serviceUrl = "http://ngms.goldfistng.com/Service.svc/";

	static Dialog loginAlert, searchDialog;
	static DialogInterface loginDialog;
	static EditText usernameText, searchText;
	static EditText passwordText;
	static TextView loginErrorView;
	static ProgressBar loginPbar;
	static Button loginButton, cancelButton, registerButton;
	static RelativeLayout loginLayout;
	static boolean loginResult = false, isRegistration = false;
	static int registrationResult = -1;

	static Activity currContext;
	Handler loginHandler;

	public ServiceAccess() {

	}

	public ItemData getItemData(String itemCode) {
		ItemData itemData = null;

		try {
			String url = serviceUrl + "getitemdata/?itemcode=" + itemCode;

			String data = runMethod(url);

			JSONObject jsonData = new JSONObject(data);

			itemData = new ItemData();

			itemData.code = jsonData.getString("itemcode");// res[0];
			itemData.title = jsonData.getString("title");// res[1];
			itemData.source = jsonData.getString("source");
			itemData.language = jsonData.getString("language");// res[2];
			itemData.genre = jsonData.getString("genre");// res[3];
			itemData.synopsis = jsonData.getString("preview");// res[4];
			itemData.producer = jsonData.getString("producer");// res[5];
			itemData.director = jsonData.getString("director");// res[6];
			itemData.cast = jsonData.getString("cast");// res[7];
			itemData.year = jsonData.getString("year");// res[8];
			itemData.price = jsonData.getInt("price");// res[9];
			itemData.expiryDays = jsonData.getInt("expirydays");// Integer.parseInt(res[10]);
			itemData.movieavailable = jsonData.getString("movieavailable");// res[11];
			itemData.traileravailable = jsonData.getString("traileravailable");// res[12];
			itemData.movieDownloadUrl = jsonData.getString("moviedownloadurl");// res[13];
			itemData.movieStreamUrl = jsonData.getString("moviestreamurl");// res[14];
			itemData.trailerDownloadUrl = jsonData
					.getString("trailerdownloadurl");// res[15];
			itemData.trailerStreamUrl = jsonData.getString("trailerstreamurl");// res[16];

		} catch (Exception ex) {
			itemData = null;
		}

		return itemData;
	}

	public String getUserIMSI(Context context) {

		String imsi = null;

		TelephonyManager tmManager = (TelephonyManager) context
				.getApplicationContext().getSystemService(
						Context.TELEPHONY_SERVICE);
		imsi = tmManager.getSubscriberId();

		return imsi;

	}

	public int getSIMState(Context context) {

		TelephonyManager tmManager = (TelephonyManager) context
				.getApplicationContext().getSystemService(
						Context.TELEPHONY_SERVICE);

		/*
		 * if (tmManager.getPhoneType() == TelephonyManager.PHONE_TYPE_NONE)
		 * return 3;//not a phone else{
		 */
		if (tmManager.getSimState() == TelephonyManager.SIM_STATE_ABSENT)
			return 111;// sim absent
		else
			return 1;
		// }
	}

	// transtype=0 for rent; transtype=1 for buy
	public long makePayment(int type, String itemcode, int amount,
			int expirydays, String userid, int transtype, Context context) {
		long result = 0;

		int simState = getSIMState(context);
		if (simState != 1)
			return simState;

		String pin = null;
		try {
			pin = URLEncoder.encode(userid, "UTF-8");
		} catch (UnsupportedEncodingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		if (pin == null)
			return -1;

		String imsi = getUserIMSI(context);

		String url = serviceUrl + "makepaymentplus/?type=" + type + "&pin="
				+ pin + "&imsi=" + imsi + "&itemcode=" + itemcode + "&amount="
				+ amount + "&expirydays=" + expirydays + "&transtype="
				+ transtype;

		try {
			String data = runMethod(url);

			JSONObject jsonResponse = new JSONObject(data);
			// JSONArray jsonDataArray = new JSONArray(data);
			result = jsonResponse.getLong("result");
		} catch (Exception e) {
			result = -1;
		}

		return result;
	}

	public ItemData[] getItems(String itemCodeName, int type, int criteria,
			int offset) {

		String itm = null;

		try {
			itm = URLEncoder.encode(itemCodeName, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();
		}

		if (itm == null)
			return null;

		ItemData[] itemData = null;

		String url = serviceUrl + "searchitemsplus/?itemcodename=" + itm
				+ "&typecode=" + type + "&criteria=" + criteria + "&offset="
				+ offset;
		// String url = serviceUrl +
		// "searchitemsplus/?itemcodename=&typecode=1&criteria=0&offset=0";

		try {
			String data = runMethod(url);

			JSONArray jsonDataArray = new JSONArray(data);

			int len = jsonDataArray.length();
			itemData = new ItemData[len];

			for (int i = 0; i < len; i++) {
				JSONObject jo = jsonDataArray.getJSONObject(i);
				itemData[i] = new ItemData();

				itemData[i].code = jo.getString("code");
				itemData[i].title = jo.getString("title");
				itemData[i].price = jo.getInt("price");
				itemData[i].year = jo.getString("year");
			}
		}

		catch (Exception ex) {
			itemData = null;
		}

		return itemData;
	}

	public boolean storeFavourites(String userid, String itemcode, String title, int hash) {

		int res = 0;
		String u = null;
		String t = null;
		try {
			u = URLEncoder.encode(userid, "UTF-8");
			t = URLEncoder.encode(title, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if ((u == null) || (t == null))
			return false;

		String url = serviceUrl + "storefavourites/?userid=" + u + "&itemcode="
				+ itemcode + "&title=" + t + "&hash=" + hash;

		try {
			String data = runMethod(url);

			JSONObject jsonResponse = new JSONObject(data);
			// JSONArray jsonDataArray = new JSONArray(data);
			res = jsonResponse.getInt("response");
			if (res == 101)
				return true;
		}

		catch (Exception ex) {
			return false;
		}

		return false;
	}

	public ItemData[] loadFavourites(String userid) {

		String u = null;
		try {
			u = URLEncoder.encode(userid, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (u == null)
			return null;

		ItemData[] itemData = null;

		String url = serviceUrl + "getfavourites/?userid=" + u;

		try {
			String data = runMethod(url);

			JSONArray jsonDataArray = new JSONArray(data);

			int len = jsonDataArray.length();
			itemData = new ItemData[len];

			for (int i = 0; i < len; i++) {
				JSONObject jo = jsonDataArray.getJSONObject(i);
				itemData[i] = new ItemData();

				itemData[i].code = jo.getString("code");
				itemData[i].title = jo.getString("title");
			}
		}

		catch (Exception ex) {
			itemData = null;
		}

		return itemData;
	}

	// type=0 for rent, type=1 for purchase
	public TransactionItemData[] getTransactionItems(String userid, int type,
			int offset) {

		String u = null;
		try {
			u = URLEncoder.encode(userid, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (u == null)
			return null;

		TransactionItemData[] itemData = null;

		String url = serviceUrl + "gettransactionitems/?userid=" + u + "&type="
				+ type + "&offset=" + offset;

		try {
			String data = runMethod(url);

			JSONArray jsonDataArray = new JSONArray(data);

			int len = jsonDataArray.length();
			itemData = new TransactionItemData[len];

			for (int i = 0; i < len; i++) {
				JSONObject jo = jsonDataArray.getJSONObject(i);
				itemData[i] = new TransactionItemData();

				itemData[i].code = jo.getString("code");
				itemData[i].title = jo.getString("title");
				itemData[i].date = jo.getString("date");
				itemData[i].expiryDate = jo.getString("expirydate");
				itemData[i].movieStreamUrl = jo.getString("moviestreamurl");
				itemData[i].time = jo.getString("time");
			}
		}

		catch (Exception ex) {
			itemData = null;
		}

		return itemData;
	}

	public void showLoginDialog(Activity _context) {

		currContext = _context;
		String userid;

		if ((userid = checkLoginStatus(currContext)) != null) {

			AlertDialog.Builder alertDialog = new AlertDialog.Builder(_context);

			// Setting Dialog Title
			alertDialog.setTitle("Login");

			// Setting Dialog Message
			alertDialog.setMessage("'" + userid
					+ "' already logged in.\nDo you want to log out?");

			// Setting Icon to Dialog
			// alertDialog.setIcon(R.drawable.tick);

			// Setting OK Button
			alertDialog.setPositiveButton("Log out",
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							saveLoginStatus(currContext, null, false);
						}
					});
			alertDialog.setNegativeButton("Cancel",
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							dialog.cancel();
						}
					});
			alertDialog.show();

		} else {
			loginAlert = new Dialog(_context);
			loginAlert.setTitle("Login");
			LayoutInflater inflater = _context.getLayoutInflater();
			View view = inflater.inflate(R.layout.login_fragment, null);
			loginAlert.setContentView(view);

			loginHandler = new LoginHandler();

			usernameText = (EditText) view.findViewById(R.id.usernameText);
			passwordText = (EditText) view.findViewById(R.id.passwordText);
			loginPbar = (ProgressBar) view.findViewById(R.id.loginPbar);
			loginErrorView = (TextView) view.findViewById(R.id.pLayT);
			loginLayout = (RelativeLayout) view.findViewById(R.id.loginLayout);

			registerButton = (Button) view.findViewById(R.id.registerButton);
			registerButton.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View arg0) {
					loginErrorView.setText("");
					loginButton.setText("Register");
					loginAlert.setTitle("Registration");
					loginLayout.setBackgroundResource(R.color.blue);
					registerButton.setVisibility(View.GONE);
					isRegistration = true;
				}
			});

			loginButton = (Button) view.findViewById(R.id.loginButton);
			loginButton.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View arg0) {
					// TODO Auto-generated method stub
					loginErrorView.setText("");
					loginPbar.setVisibility(ProgressBar.VISIBLE);

					if (isRegistration) {

						Thread registerThread = new Thread() {
							public void run() {
								registrationResult = registerUser(usernameText
										.getText().toString(), passwordText
										.getText().toString());
								loginHandler.sendEmptyMessage(0);
							}
						};
						registerThread.start();

					} else {

						Thread loginThread = new Thread() {
							public void run() {
								loginResult = authenticateUser(usernameText
										.getText().toString(), passwordText
										.getText().toString());
								loginHandler.sendEmptyMessage(0);
							}
						};
						loginThread.start();
					}
				}

			});
			cancelButton = (Button) view.findViewById(R.id.cButtonI);
			cancelButton.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View arg0) {
					// TODO Auto-generated method stub
					loginAlert.cancel();// .dismiss();
				}
			});
			// Showing Alert Message
			loginAlert.show();
		}
	}

	public void showSearchDialog(Activity _context) {

		currContext = _context;

		searchDialog = new Dialog(_context);
		searchDialog.setTitle("Search");
		LayoutInflater inflater = _context.getLayoutInflater();
		View view = inflater.inflate(R.layout.search_dialog_layout, null);
		searchDialog.setContentView(view);

		searchText = (EditText) view.findViewById(R.id.searchText);

		Button searchButton = (Button) view.findViewById(R.id.searchButton);
		searchButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				String code = searchText.getText().toString();

				Intent searchIntent = new Intent(currContext,
						SearchActivity.class);
				searchIntent.putExtra("itemcodename", code);
				currContext.startActivity(searchIntent);

				searchDialog.cancel();
			}
		});

		Button cButton = (Button) view.findViewById(R.id.cButtonG);
		cButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				searchDialog.cancel();// .dismiss();
			}
		});

		searchDialog.show();

	}

	public int registerUser(String username, String password) {
		int result = -1;

		String u = null, p = null;

		try {
			u = URLEncoder.encode(username, "UTF-8");
			p = URLEncoder.encode(password, "UTF-8");
		} catch (UnsupportedEncodingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		if (u == null || p == null)
			return -1;

		String url = serviceUrl + "createuser/?username=" + u + "&password="
				+ p;

		try {
			String data = runMethod(url);

			JSONObject jsonResponse = new JSONObject(data);
			// JSONArray jsonDataArray = new JSONArray(data);
			result = jsonResponse.getInt("result");
		} catch (Exception e) {
			result = -1;
		}

		return result;
	}

	public boolean authenticateUser(String username, String password) {

		boolean result = false;

		String u = null, p = null;

		try {
			u = URLEncoder.encode(username, "UTF-8");
			p = URLEncoder.encode(password, "UTF-8");
		} catch (UnsupportedEncodingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		if (u == null || p == null)
			return false;

		String url = serviceUrl + "signin/?username=" + u + "&password=" + p;

		try {
			String data = runMethod(url);

			JSONObject jsonResult = new JSONObject(data);

			result = jsonResult.getBoolean("result");
		}

		catch (Exception ex) {
			result = false;
		}

		return result;
	}

	public String[] getItemReviews(String itemCode) {
		String[] res = null;

		String url = serviceUrl + "getuserreviews/?itemcode=" + itemCode;

		try {
			String data = runMethod(url);

			JSONArray jsonDataArray = new JSONArray(data);

			int len = jsonDataArray.length();
			res = new String[len];

			for (int i = 0; i < len; i++) {
				JSONObject jo = jsonDataArray.getJSONObject(i);
				res[i] = jo.getString("review");// jsonDataArray.getJSONObject(i).getString("ct");
			}
		}

		catch (Exception ex) {
			res = null;
		}

		return res;
	}

	public String runMethod(String url) {

		String result = null;

		int retry = 0;

		while (true) {

			try {

				HttpClient client = new DefaultHttpClient();
				HttpGet request = new HttpGet(url/*
												 * URLEncoder.encode(url,
												 * "UTF-8")
												 */);

				HttpResponse response = client.execute(request);
				HttpEntity entity = response.getEntity();

				result = new String(EntityUtils.toByteArray(entity));

			} catch (Exception e) {
			}

			if (retry == 3 || result != null)
				break;

			retry++;

			try {
				Thread.sleep(3000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}// while (result == null);

		return result;
	}

	public String checkLoginStatus(Activity _context) {

		String res = null;

		// SharedPreferences preferenceManager;
		try {
			preferenceManager = PreferenceManager
					.getDefaultSharedPreferences(_context);

			res = preferenceManager.getString("login", null);
		} catch (Exception e) {
		}
		/*
		 * try { File f = new File(Environment.getExternalStorageDirectory()
		 * .getPath() + "/log/log.g"); if (!f.exists()) return null;
		 * 
		 * FileInputStream fis = new FileInputStream(f); int len =
		 * fis.available();
		 * 
		 * byte[] data = new byte[len];
		 * 
		 * fis.read(data);
		 * 
		 * res = new String(data); fis.close(); } catch (Exception e) { }
		 */

		return res;
	}

	public void manageCache(Activity _context) {

		preferenceManager = PreferenceManager
				.getDefaultSharedPreferences(_context);

		long lastDate = preferenceManager.getLong("lastCacheClearMilli", 0);

		Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
		long curr = cal.getTime().getTime();

		if (curr <= (lastDate + 86400000)) {
			new ImageLoader(_context.getApplicationContext()).clearCache();
			// new FileCache(_context.getApplicationContext()).clear();
		} else {
			Editor PrefEdit = preferenceManager.edit();
			PrefEdit.putLong("lastCacheClearMilli", curr);
			PrefEdit.commit();
		}
	}

	public static boolean saveLoginStatus(Activity _context, String username,
			boolean isLogin) {

		boolean res = false;

		try {
			preferenceManager = PreferenceManager
					.getDefaultSharedPreferences(_context);
			Editor PrefEdit = preferenceManager.edit();
			String data = isLogin ? username : null;
			PrefEdit.putString("login", data);
			PrefEdit.commit();
			res = true;
		} catch (Exception e) {

		}

		/*
		 * try { File f = new
		 * File(Environment.getExternalStorageDirectory().getPath(), "log"); if
		 * (!f.exists()) f.mkdirs();
		 * 
		 * File fo = new File(f.getPath() + "/log.g"); if (!fo.exists())
		 * fo.createNewFile();
		 * 
		 * FileOutputStream fos = new FileOutputStream(f);
		 * fos.write(data.getBytes()); fos.flush(); fos.close(); } catch
		 * (Exception e) { }
		 */
		return res;
	}

	public boolean createDirectories() {

		boolean result = false;
		File nollyDownloadFile = new File(Environment
				.getExternalStorageDirectory().getPath(), "nollygold");
		if (!nollyDownloadFile.exists()) {
			nollyDownloadFile.mkdirs();
		}

		return result;
	}

	class TransactionItemData {
		public String code = "";
		public String title = "";
		public String filePath = "";
		public String time = "";
		public String date = "";
		public String expiryDate = "";
		// public long expirytimeMilli = 0;
		public String movieStreamUrl = "";
		// public int price = 0;
	}

	public static class ItemData {

		public String code = "";
		public String title = "";
		public String source = "";
		public String filePath = "";
		public String synopsis = "";
		public String producer = "";
		public String director = "";
		public String cast = "";
		public String language = "";
		public String genre = "";
		public String year = "";
		public int price = 0;
		public int expiryDays = 0;
		public String traileravailable = "";
		public String trailerDownloadUrl = "";
		public String trailerStreamUrl = "";
		public String movieavailable = "";
		public String movieDownloadUrl = "";
		public String movieStreamUrl = "";
		public String purchaseID = "";
		public long lastDownloadTime = 0;
		public long rentTime = 0;
		public long currentPlayerTime = 0;
		public int currentPlayerVolume = 50;
		public byte[] logo = null;

	}

	static class LoginHandler extends Handler {

		public void handleMessage(Message msg) {

			loginPbar.setVisibility(ProgressBar.INVISIBLE);

			if (isRegistration) {

				if (registrationResult == 1) {

					saveLoginStatus(currContext, usernameText.getText()
							.toString(), true);
					loginAlert.cancel();// .dismiss();

					Toast.makeText(
							currContext,
							"Registration successful.\nPlease check your e-mail",
							Toast.LENGTH_LONG).show();
				} else if (registrationResult == 0)
					loginErrorView.setText("User already exists.");
				else
					loginErrorView.setText("Registration failed.");

				loginAlert.setTitle("Login");
				loginButton.setText("Login");
				loginLayout.setBackgroundResource(R.color.black);
				isRegistration = false;
				registerButton.setVisibility(View.VISIBLE);
			} else {

				if (loginResult) {

					saveLoginStatus(currContext, usernameText.getText()
							.toString(), true);
					loginAlert.cancel();// .dismiss();

					Toast.makeText(currContext, "Login successful.",
							Toast.LENGTH_LONG).show();
				} else
					loginErrorView.setText("Login failed.");
			}

		}
	}

}
