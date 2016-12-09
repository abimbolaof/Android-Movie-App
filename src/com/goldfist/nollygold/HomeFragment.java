package com.goldfist.nollygold;

import java.util.ArrayList;
import java.util.HashMap;

import android.app.Activity;
import android.support.v4.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import com.goldfist.nollygold.ServiceAccess.ItemData;

public class HomeFragment extends Fragment {

	static ItemData[] itemData = null;
	static ListView listView;
	static ProgressBar progressBar;
	static HomeListAdapter adapter;
	static Button homeRefreshButton;
	Handler loadHandler;
	// TextView label;
	static ArrayList<HashMap<String, String>> moviesList;
	String[] menuItems;
	Spinner menuSpinner;
	int selectedType = 0;
	String itemCodeName = "";
	int criteria = 0;
	int offset = 0;
	int typeCode = 0;
	static int loadCount = 0;
	// TextView textView;
	static Activity myContext;

	int screenWidth;
	boolean isExpanded = false;

	public HomeFragment() {

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstance) {

		View view = inflater.inflate(R.layout.home_fragment, container, false);
		myContext = getActivity();// (Activity) view.getContext();// (Activity)
									// getActivity().getApplicationContext();

		listView = (ListView) view.findViewById(R.id.hlist);
		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO Auto-generated method stub
				String code = ((HashMap<String, String>) moviesList
						.get(position)).get(MainActivity.KEY_CODE);
				// label.setText(tit);
				// DetailsActivity da = new DetailsActivity(tit);
				Intent detailsIntent = new Intent(myContext,
						DetailsActivity.class);
				detailsIntent.putExtra("itemCode", code);
				myContext.startActivity(detailsIntent);
			}
		});

		progressBar = (ProgressBar) view.findViewById(R.id.hProgressBar);
		homeRefreshButton = (Button) view.findViewById(R.id.homeRefresh);
		homeRefreshButton.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				homeRefreshButton.setVisibility(View.GONE);
				loadSystem();
			}
			
		});

		moviesList = new ArrayList<HashMap<String, String>>();

		menuSpinner = (Spinner) view.findViewById(R.id.menuSpinner);
		menuItems = new String[4];
		menuItems[0] = "Featured Movies";
		menuItems[1] = "New Movies";
		menuItems[2] = "Top Free Movies";
		menuItems[3] = "Top Rented";

		ArrayAdapter<String> spad = new ArrayAdapter<String>(myContext,
				R.layout.category_menu, menuItems);
		spad.setDropDownViewResource(R.layout.category_menu);
		menuSpinner.setAdapter(spad);
		menuSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int position, long arg3) {
				// TODO Auto-generated method stub
				selectedType = position;
				homeRefreshButton.setVisibility(View.GONE);
				// textView.setText(String.valueOf(selectedType));
				loadSystem();
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub
			}
		});

		if (itemData == null) {
			menuSpinner.setSelection(0);
		}else{
			showData();
		}

		loadHandler = new LoadHandler();

		return view;
	}
	
	void showData(){
		
		moviesList.clear();

		for (int i = 0; i < itemData.length; i++/*
												 * int i = 0; i &lt;
												 * nl.getLength();
												 * i++
												 */) {
			// creating new HashMap
			HashMap<String, String> map = new HashMap<String, String>();
			map.put(MainActivity.KEY_CODE, itemData[i].code);
			map.put(MainActivity.KEY_TITLE, itemData[i].title);
			map.put(MainActivity.KEY_PRICE,
					String.valueOf(itemData[i].price));
			map.put(MainActivity.KEY_YEAR, itemData[i].year);
			map.put(MainActivity.KEY_THUMB_URL, new ServiceAccess().serverRootUrl
					+ "rok/images/" + itemData[i].code + "_tmb.jpg");
			// adding HashList to ArrayList
			moviesList.add(map);
		}
	}

	void loadSystem(/* int type */) {

		progressBar.setVisibility(ProgressBar.VISIBLE);

		Thread loadThread = new Thread() {
			public void run() {

				if (selectedType == 0) {
					typeCode = 1;
					itemCodeName = "";
					criteria = 0;
					offset = 0;
				} else if (selectedType == 1) {
					typeCode = 0;
					itemCodeName = "";
					criteria = 10;
					offset = 0;
				} else if (selectedType == 2) {
					typeCode = 0;
					itemCodeName = "";
					criteria = 11;
					offset = 0;
				} else if (selectedType == 3) {
					typeCode = 0;
					itemCodeName = "";
					criteria = 9;
					offset = 0;
				}

				ServiceAccess sa = new ServiceAccess();
				itemData = sa
						.getItems(itemCodeName, typeCode, criteria, offset);

				if (itemData != null) {

					showData();
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

	static class LoadHandler extends Handler {

		public void handleMessage(Message msg) {

			progressBar.setVisibility(ProgressBar.INVISIBLE);

			if (itemData == null) {
				homeRefreshButton.setVisibility(View.VISIBLE);
				Toast.makeText(myContext, "No Data.",
						Toast.LENGTH_LONG).show();
			} else {
				homeRefreshButton.setVisibility(View.GONE);
				adapter = new HomeListAdapter(myContext, moviesList);
				listView.setAdapter(adapter);
			}
		}
	}
}
