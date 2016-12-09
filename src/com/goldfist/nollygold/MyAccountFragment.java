package com.goldfist.nollygold;

import java.util.ArrayList;
import java.util.HashMap;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;
import android.widget.Toast;

import com.goldfist.nollygold.ServiceAccess.TransactionItemData;

public class MyAccountFragment extends Fragment {

	static Activity mContext;
	static int dType = 0;

	static TransactionItemData[] itm;
	static TransactionItemData[] rentedItemData, expiredItemData,
			purchasedItemData;
	static ListView rentedListView, expiredListView, purchasedListView;
	static TransactionsListAdapter rentedAdapter, expiredAdapter,
			purchasedAdapter;
	static Handler transactionHandler;
	static int rLoadedCount = 0, rOffset = 0, eLoadedCount = 0, eOffset = 0,
			pLoadedCount = 0, pOffset = 0;

	// Handler purchasedHandler;
	static ArrayList<HashMap<String, String>> rentedList, expiredList,
			purchasedList;

	public MyAccountFragment() {

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View root = (View) inflater.inflate(R.layout.my_account_layout, null);

		mContext = getActivity();// (Activity)
									// root.getContext();//getActivity().getApplicationContext();

		rentedList = new ArrayList<HashMap<String, String>>();
		expiredList = new ArrayList<HashMap<String, String>>();
		purchasedList = new ArrayList<HashMap<String, String>>();

		transactionHandler = new LoadTransactionsHandler();
		// purchasedHandler = new LoadTransactionsHandler();

		rentedListView = (ListView) root.findViewById(R.id.rentedListView);
		rentedListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO Auto-generated method stub
				String code = ((HashMap<String, String>) rentedList
						.get(position)).get(MainActivity.KEY_CODE);

				Intent detailsIntent = new Intent(mContext,
						DetailsActivity.class);
				detailsIntent.putExtra("itemCode", code);
				detailsIntent.putExtra("isRented", true);
				mContext.startActivity(detailsIntent);
			}
		});

		purchasedListView = (ListView) root
				.findViewById(R.id.purchasedListView);
		purchasedListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO Auto-generated method stub
				String code = ((HashMap<String, String>) purchasedList
						.get(position)).get(MainActivity.KEY_CODE);

				Intent detailsIntent = new Intent(mContext,
						DetailsActivity.class);
				detailsIntent.putExtra("itemCode", code);
				detailsIntent.putExtra("isPurchased", true);
				mContext.startActivity(detailsIntent);
			}
		});

		expiredListView = (ListView) root.findViewById(R.id.expiredListView);
		expiredListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO Auto-generated method stub
				String code = ((HashMap<String, String>) expiredList
						.get(position)).get(MainActivity.KEY_CODE);

				Intent detailsIntent = new Intent(mContext,
						DetailsActivity.class);
				detailsIntent.putExtra("itemCode", code);
				// detailsIntent.putExtra("isPurchased", true);
				mContext.startActivity(detailsIntent);
			}
		});

		TabHost tabHost = (TabHost) root.findViewById(android.R.id.tabhost);
		tabHost.setup();

		TabSpec rentedspec = tabHost.newTabSpec("Rented");
		View rentedIndicator = (View) inflater.inflate(
				R.layout.rented_tab_indicator, null);
		rentedspec.setIndicator(rentedIndicator);
		rentedspec.setContent(R.id.tab1);

		TabSpec expiredspec = tabHost.newTabSpec("Expired");
		View expiredIndicator = (View) inflater.inflate(
				R.layout.expired_tab_indicator, null);
		expiredspec.setIndicator(expiredIndicator);
		expiredspec.setContent(R.id.tab2);

		TabSpec purchasedspec = tabHost.newTabSpec("Purchased");
		purchasedspec.setIndicator("Purchased", null);
		View purchasedIndicator = (View) inflater.inflate(
				R.layout.purchased_tab_indicator, null);
		purchasedspec.setIndicator(purchasedIndicator);
		purchasedspec.setContent(R.id.tab3);

		tabHost.addTab(rentedspec);
		tabHost.addTab(expiredspec);
		tabHost.addTab(purchasedspec);
		// tabHost.addTab(historyspec);
		rLoadedCount = 0;
		rOffset = 0;
		eLoadedCount = 0;
		eOffset = 0;
		pLoadedCount = 0;
		pOffset = 0;

		loadData(1);

		return root;
	}

	static void loadData(int type) {

		// DatabaseHelper db = new
		// DatabaseHelper(mContext.getApplicationContext());

		dType = type;
		// SharedPreferences preferenceManager;
		// preferenceManager =
		// PreferenceManager.getDefaultSharedPreferences(mContext);
		final String userid = new ServiceAccess().checkLoginStatus(mContext);// preferenceManager.getString("iLogin",
																				// null);

		if (userid != null) {
			final int t = type;
			Thread loadThread = new Thread() {
				public void run() {

					ServiceAccess sa = new ServiceAccess();

					int off = 0;
					if (t == 0)
						off = rOffset;
					else if (t == 1)
						off = pOffset;
					else if (t == 2)
						off = eOffset;

					itm = sa.getTransactionItems(userid, t, off);

					if (itm != null) {

						if (t == 0) {
							rentedItemData = itm;
							// rentedList.clear();
							rLoadedCount = itm.length;
							rOffset += rLoadedCount;
						} else if (t == 1) {
							purchasedItemData = itm;
							// expiredList.clear();
							pLoadedCount = itm.length;
							pOffset += eLoadedCount;
						} else if (t == 2) {
							expiredItemData = itm;
							// expiredList.clear();
							eLoadedCount = itm.length;
							eOffset += eLoadedCount;
						}

						for (int i = 0; i < itm.length; i++/*
															 * int i = 0; i &lt;
															 * nl.getLength();
															 * i++
															 */) {
							// creating new HashMap
							HashMap<String, String> map = new HashMap<String, String>();
							map.put(MainActivity.KEY_CODE, itm[i].code);
							map.put(MainActivity.KEY_TITLE, itm[i].title);
							map.put(MainActivity.KEY_TRANS_DATE, itm[i].date);
							map.put(MainActivity.KEY_EXPIRY_DATE,
									itm[i].expiryDate);
							map.put(MainActivity.KEY_TRANS_TIME, itm[i].time);
							map.put(MainActivity.KEY_THUMB_URL,
									sa.serverRootUrl + "rok/images/"
											+ itm[i].code + "_tmb.jpg");

							if (t == 0)
								rentedList.add(map);
							else if (t == 1)
								purchasedList.add(map);
							else if (t == 2)
								expiredList.add(map);
						}
					}

					/*
					 * Message msg = new Message(); Bundle bundle = new
					 * Bundle(); bundle.putInt("type", t); msg.setData(bundle);
					 */

					transactionHandler.sendEmptyMessage(0);// .sendMessage(msg);
				}
			};
			loadThread.start();
		} else {
			Toast.makeText(mContext, "Please Login.", Toast.LENGTH_LONG).show();
		}
	}

	static class LoadTransactionsHandler extends Handler {

		public void handleMessage(Message msg) {

			// progressBar.setVisibility(ProgressBar.INVISIBLE);

			// int type = msg.getData().getInt("type");

			if (itm != null) {
				if (dType == 0) {

					Button btnLoadMore = new Button(mContext);
					if (rOffset == 10) {

						btnLoadMore.setText("Load More");
						btnLoadMore
								.setOnClickListener(new View.OnClickListener() {

									@Override
									public void onClick(View arg0) {
										loadData(0);
									}
								});
						rentedListView.addFooterView(btnLoadMore);
					} else if (rLoadedCount == 0) {
						rentedListView.removeFooterView(btnLoadMore);
						/*
						 * Toast.makeText(mContext, "No Rented Items.",
						 * Toast.LENGTH_LONG).show();
						 */
					}

					rentedAdapter = new TransactionsListAdapter(
							(Activity) mContext, rentedList);
					rentedListView.setAdapter(rentedAdapter);
					rentedListView.setSelection(rOffset - rLoadedCount);

					loadData(2);

				} else if (dType == 1) {

					Button btnLoadMore = new Button(mContext);
					if (pOffset == 10) {

						btnLoadMore.setText("Load More");
						btnLoadMore
								.setOnClickListener(new View.OnClickListener() {

									@Override
									public void onClick(View arg0) {
										loadData(1);
									}
								});
						purchasedListView.addFooterView(btnLoadMore);
					} else if (pLoadedCount == 0) {
						purchasedListView.removeFooterView(btnLoadMore);
						/*
						 * Toast.makeText(mContext, "No Purchased Items.",
						 * Toast.LENGTH_LONG).show();
						 */
					}

					purchasedAdapter = new TransactionsListAdapter(
							(Activity) mContext, purchasedList);
					purchasedListView.setAdapter(purchasedAdapter);
					purchasedListView.setSelection(pOffset - pLoadedCount);

					loadData(0);

				} else if (dType == 2) {

					Button btnLoadMore = new Button(mContext);
					if (eOffset == 10) {

						btnLoadMore.setText("Load More");
						btnLoadMore
								.setOnClickListener(new View.OnClickListener() {

									@Override
									public void onClick(View arg0) {
										loadData(2);
									}
								});
						expiredListView.addFooterView(btnLoadMore);
					} else if (eLoadedCount == 0) {
						expiredListView.removeFooterView(btnLoadMore);
						/*
						 * Toast.makeText(mContext, "No Expired Items.",
						 * Toast.LENGTH_LONG).show();
						 */
					}

					expiredAdapter = new TransactionsListAdapter(
							(Activity) mContext, expiredList);
					expiredListView.setAdapter(expiredAdapter);
					expiredListView.setSelection(eOffset - eLoadedCount);
				}
			}/*
			 * else { String ty = ""; if (dType == 0) ty = "Rented"; else if
			 * (dType == 1) ty = "Purchased"; else if (dType == 2) ty =
			 * "Expired";
			 * 
			 * Toast.makeText(mContext, "Could not load " + ty + "data.",
			 * Toast.LENGTH_LONG).show(); }
			 */
		}
	}

}
