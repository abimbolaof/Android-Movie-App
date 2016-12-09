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
import android.widget.ProgressBar;
import android.widget.Toast;

import com.goldfist.nollygold.ServiceAccess.ItemData;

public class SearchFragment extends Fragment {

	static ListView listView;
	static ArrayList<HashMap<String, String>> moviesList;
	static Activity mContext;
	static HomeListAdapter adapter;
	static Handler loadHandler;
	static ItemData[] itemData = null;
	static String itemCodeName = "";
	static int criteria = 0;
	static int typeCode = 0;
	static int _searchOffset = 0, loadCount = 0;
	static ProgressBar pBar;
	static Button btnLoadMore;

	public SearchFragment() {

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstance) {

		View view = inflater
				.inflate(R.layout.search_fragment, container, false);
		mContext = (Activity) view.getContext();// (Activity)
												// getActivity().getApplicationContext();

		Bundle bundle = getArguments();
		itemCodeName = bundle.getString("itemcodename");
		_searchOffset = bundle.getInt("offset");

		loadHandler = new SearchHandler();
		moviesList = new ArrayList<HashMap<String, String>>();

		pBar = (ProgressBar) view.findViewById(R.id.searchProgressBar);

		btnLoadMore = new Button(mContext);
		btnLoadMore.setText("Load More");
		btnLoadMore.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// Starting a new async task
				searchMovies();
				// SearchActivity.openSearch(itemCodeName, _searchOffset);
			}
		});

		listView = (ListView) view.findViewById(R.id.searchListView);
		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO Auto-generated method stub
				String code = ((HashMap<String, String>) moviesList
						.get(position)).get(MainActivity.KEY_CODE);

				Intent detailsIntent = new Intent(mContext,
						DetailsActivity.class);
				detailsIntent.putExtra("itemCode", code);
				mContext.startActivity(detailsIntent);
			}
		});

		searchMovies();

		return view;
	}

	static void searchMovies() {

		Thread loadThread = new Thread() {
			public void run() {

				ServiceAccess sa = new ServiceAccess();
				itemData = sa.getItems(itemCodeName, typeCode, criteria,
						_searchOffset);

				/*if (itemData == null) {
					loadHandler.sendEmptyMessage(0);
					return;
				}*/

				if (itemData != null){
					
					loadCount = itemData.length;
					// moviesList.clear();

					for (int i = 0; i < itemData.length; i++/*
															 * int i = 0; i &lt;
															 * nl.getLength(); i++
															 */) {
						// creating new HashMap
						HashMap<String, String> map = new HashMap<String, String>();
						map.put(MainActivity.KEY_CODE, itemData[i].code);
						map.put(MainActivity.KEY_TITLE, itemData[i].title);
						map.put(MainActivity.KEY_PRICE,
								String.valueOf(itemData[i].price));
						map.put(MainActivity.KEY_YEAR, itemData[i].year);
						map.put(MainActivity.KEY_THUMB_URL, sa.serverRootUrl
								+ "rok/images/" + itemData[i].code + "_tmb.jpg");
						// adding HashList to ArrayList
						moviesList.add(map);
					}

					_searchOffset += loadCount;
				}
				

				loadHandler.sendEmptyMessage(0);
			}
		};
		loadThread.start();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRetainInstance(true);
	}

	static class SearchHandler extends Handler {

		public void handleMessage(Message msg) {

			pBar.setVisibility(View.GONE);

			if (itemData != null) {
				if (_searchOffset == 10) {
					listView.addFooterView(btnLoadMore);
				} else if (loadCount == 0) {
					listView.removeFooterView(btnLoadMore);
					Toast.makeText(mContext, "No Results.", Toast.LENGTH_LONG)
							.show();
				}

				adapter = new HomeListAdapter(mContext, moviesList);
				listView.setAdapter(adapter);
				listView.setSelection(_searchOffset - loadCount);
			} else {
				listView.removeFooterView(btnLoadMore);
				Toast.makeText(mContext, "No Results.", Toast.LENGTH_LONG)
						.show();
			}

		}
	}
}
