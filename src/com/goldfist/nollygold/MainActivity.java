package com.goldfist.nollygold;

import android.app.Activity;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
//import android.support.v4.app.FragmentActivity;

public class MainActivity extends ActionBarActivity {

	static final String KEY_ID = "id";
	static final String KEY_SONG = "song"; // parent node
	static final String KEY_CODE = "code";
	static final String KEY_TITLE = "title";
	static final String KEY_YEAR = "year";
	static final String KEY_PRICE = "price";
	static final String KEY_EXPIRY_DATE = "expirydate";
	static final String KEY_TRANS_DATE = "date";
	static final String KEY_TRANS_TIME = "time";
	static final String KEY_THUMB_URL = "thumb_url";
	static final String KEY_VIDEO_PATH = "videopath";

	private DrawerLayout mDrawerLayout;
	private ListView mDrawerList;
	private ActionBarDrawerToggle mDrawerToggle;

	private CharSequence mDrawerTitle;
	private CharSequence mTitle;
	private String[] mMenuTitles;
	IntentFilter filter;

	Fragment homeFragment, myAccountFragment, aboutFragment, libraryFragment,
			favouriteFragment;

	Activity mContext;

	//DownloadReciever downloadReciever;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		mContext = this;

		ServiceAccess sa = new ServiceAccess();
		sa.createDirectories();

		// new ImageLoader(getApplicationContext()).clearCache();

		/*filter = new IntentFilter(DownloadReciever.ACTION_RESP);
		filter.addCategory(Intent.CATEGORY_DEFAULT);
		downloadReciever = new DownloadReciever();
		registerReceiver(downloadReciever, filter);*/

		mTitle = mDrawerTitle = getTitle();
		mMenuTitles = getResources().getStringArray(R.array.menu_array);
		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		mDrawerList = (ListView) findViewById(R.id.left_drawer);

		mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow,
				GravityCompat.START);
		// set up the drawer's list view with items and click listener
		mDrawerList.setAdapter(new ArrayAdapter<String>(this,
				R.layout.drawer_list_item, mMenuTitles));
		mDrawerList.setOnItemClickListener(new DrawerItemClickListener());

		// enable ActionBar app icon to behave as action to toggle nav drawer
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setHomeButtonEnabled(true);

		// ActionBarDrawerToggle ties together the the proper interactions
		// between the sliding drawer and the action bar app icon
		mDrawerToggle = new ActionBarDrawerToggle(this, /* host Activity */
		mDrawerLayout, /* DrawerLayout object */
		R.drawable.ic_drawer, /* nav drawer image to replace 'Up' caret */
		R.string.drawer_open, /* "open drawer" description for accessibility */
		R.string.drawer_close /* "close drawer" description for accessibility */
		) {
			public void onDrawerClosed(View view) {
				getSupportActionBar().setTitle(mTitle);
				supportInvalidateOptionsMenu(); // creates call to
											// onPrepareOptionsMenu()
			}

			public void onDrawerOpened(View drawerView) {
				getSupportActionBar().setTitle(mDrawerTitle);
				supportInvalidateOptionsMenu(); // creates call to
											// onPrepareOptionsMenu()
			}
		};
		mDrawerLayout.setDrawerListener(mDrawerToggle);

		homeFragment = new HomeFragment();
		aboutFragment = new AboutFragment();
		myAccountFragment = new MyAccountFragment();
		libraryFragment = new LibraryFragment();
		favouriteFragment = new FavouriteFragment();

		//sa.manageCache(mContext);
		//selectItem(0);
		
		if (savedInstanceState == null) {

			//new FileCache(mContext.getApplicationContext()).clear();
			sa.manageCache(mContext);
			selectItem(0);
		}
		// textView = (TextView) findViewById(R.id.textView1);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main, menu);
		return super.onCreateOptionsMenu(menu);
	}

	/* Called whenever we call invalidateOptionsMenu() */
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		// If the nav drawer is open, hide action items related to the content
		// view
		boolean drawerOpen = mDrawerLayout.isDrawerOpen(mDrawerList);
		menu.findItem(R.id.action_websearch).setVisible(!drawerOpen);
		menu.findItem(R.id.action_login).setVisible(!drawerOpen);
		// menu.findItem(R.id.action_check_downloads).setVisible(!drawerOpen);
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// The action bar home/up action should open or close the drawer.
		// ActionBarDrawerToggle will take care of this.
		if (mDrawerToggle.onOptionsItemSelected(item)) {
			return true;
		}
		// Handle action buttons
		switch (item.getItemId()) {

		case R.id.action_login: {
			new ServiceAccess().showLoginDialog(mContext);
			return true;
		}
		case R.id.action_websearch: {
			new ServiceAccess().showSearchDialog(mContext);
			return true;
		}
		case R.id.action_close: {
			finish();
			System.exit(0);
			return true;
		}
		/*
		 * case R.id.action_check_downloads: { Intent intent = new Intent();
		 * intent.setAction(DownloadManager.ACTION_VIEW_DOWNLOADS);
		 * startActivity(intent); }
		 */
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void setTitle(CharSequence title) {
		mTitle = title;
		getSupportActionBar().setTitle(mTitle);
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		// Sync the toggle state after onRestoreInstanceState has occurred.
		mDrawerToggle.syncState();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		// Pass any configuration change to the drawer toggls
		mDrawerToggle.onConfigurationChanged(newConfig);
	}

	public void selectItem(int position) {

		if (position == 0) {
			// Fragment fragment = new HomeFragment();

			FragmentTransaction transaction = getSupportFragmentManager()
					.beginTransaction();
			transaction.replace(R.id.content_frame, homeFragment);
			// transaction.addToBackStack(null);
			transaction.commit();

		} else if (position == 1) {
			// Fragment fragment2 = new MyAccountFragment();

			FragmentTransaction transaction = getSupportFragmentManager()
					.beginTransaction();
			transaction.replace(R.id.content_frame, myAccountFragment);
			// transaction.addToBackStack(null);
			transaction.commit();

		} else if (position == 2) {
			// Favourites
			FragmentTransaction transaction = getSupportFragmentManager()
					.beginTransaction();
			transaction.replace(R.id.content_frame, favouriteFragment);
			// transaction.addToBackStack(null);
			transaction.commit();

		} else if (position == 3) {
			// Fragment fragment3 = new AboutFragment();

			FragmentTransaction transaction = getSupportFragmentManager()
					.beginTransaction();
			transaction.replace(R.id.content_frame, libraryFragment);
			// transaction.addToBackStack(null); transaction.commit();
			transaction.commit();

		} else if (position == 4) {
			FragmentTransaction transaction = getSupportFragmentManager()
					.beginTransaction();
			transaction.replace(R.id.content_frame, aboutFragment);
			// transaction.addToBackStack(null);
			transaction.commit();
		}/* else if (position == 5) {
			finish();
		}*/
		/*
		 * else if (position == 2) { // Library FragmentTransaction transaction
		 * = getSupportFragmentManager() .beginTransaction();
		 * transaction.replace(R.id.content_frame, libraryFragment); //
		 * transaction.addToBackStack(null); transaction.commit(); }
		 *//*
			 * else if (position == 3) { // Settings }
			 */

		mDrawerList.setItemChecked(position, true);
		setTitle(mMenuTitles[position]);
		mDrawerLayout.closeDrawer(mDrawerList);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {

		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {

			moveTaskToBack(true);
			return true;

		}
		return super.onKeyDown(keyCode, event);
	}

	/*@Override
	protected void onResume() { // TODO Auto-generated method stub

		super.onResume();
		registerReceiver(downloadReciever, filter);
	}
	
	@Override
	protected void onPause() { // TODO Auto-generated method stub
		super.onPause();
		unregisterReceiver(downloadReciever);
	}*/

	/*
	 * @Override public boolean onCreateOptionsMenu(Menu menu) { // Inflate the
	 * menu; this adds items to the action bar if it is present.
	 * getMenuInflater().inflate(R.menu.main, menu); return true; }
	 */

	private class DrawerItemClickListener implements
			ListView.OnItemClickListener {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			selectItem(position);
		}
	}

	/*public class DownloadReciever extends BroadcastReceiver {

		public static final String ACTION_RESP = "com.goldfist.nollygold.ACTION_COMPLETE_DOWNLOAD";

		@Override
		public void onReceive(Context context, Intent intent) {

			Toast.makeText(mContext, "Download Complete.", Toast.LENGTH_LONG)
					.show();
		}
	};*/

}
