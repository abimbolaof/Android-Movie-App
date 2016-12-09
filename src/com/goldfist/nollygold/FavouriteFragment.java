package com.goldfist.nollygold;

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
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.goldfist.nollygold.ServiceAccess.ItemData;

public class FavouriteFragment extends Fragment {

	static ItemData[] fData;
	static ListView dataListView;
	Handler getDataHandler;
	static Activity mContext;
	static FavouriteListAdapter adapter;
	static ProgressBar pBar;

	public FavouriteFragment() {

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstance) {

		View view = inflater.inflate(R.layout.favourite_fragment, container,
				false);

		mContext = getActivity();

		pBar = (ProgressBar) view.findViewById(R.id.fProgressBar);

		dataListView = (ListView) view.findViewById(R.id.flist);
		dataListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO Auto-generated method stub

				String code = fData[position].code;

				Intent detailsIntent = new Intent(mContext,
						DetailsActivity.class);
				detailsIntent.putExtra("itemCode", code);
				detailsIntent.putExtra("isFavourite", true);
				mContext.startActivity(detailsIntent);
			}
		});

		getDataHandler = new GetDataHandler();
		
		loadFavourites();

		return view;
	}

	void loadFavourites() {

		final String userid = new ServiceAccess().checkLoginStatus(mContext);

		if (userid != null) {

			Thread getDataThread = new Thread() {
				public void run() {
					// DatabaseHelper db = new
					// DatabaseHelper(getActivity().getApplicationContext());
					fData = new ServiceAccess().loadFavourites(userid);
					getDataHandler.sendEmptyMessage(0);
				}
			};
			getDataThread.start();
		} else {
			pBar.setVisibility(View.GONE);
			Toast.makeText(mContext, "Please Login.", Toast.LENGTH_LONG).show();
		}

	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRetainInstance(true);
	}

	static class GetDataHandler extends Handler {

		public void handleMessage(Message msg) {

			pBar.setVisibility(View.GONE);
			
			if (fData != null) {
				adapter = new FavouriteListAdapter(mContext, fData);
				dataListView.setAdapter(adapter);
			} else {
				Toast.makeText(mContext, "No data found.", Toast.LENGTH_LONG)
						.show();
			}

		}
	};
}
