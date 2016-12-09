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
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ProgressBar;

public class LibraryFragment extends Fragment {

	static ArrayList<HashMap<String, String>> libraryData;
	static ListView dataListView;
	Handler getDataHandler;
	static Activity mContext;
	static ProgressBar pBar;
	static LibraryListAdapter adapter;
	public LibraryFragment() {

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstance) {

		View view = inflater.inflate(R.layout.library_fragment, container, false);
		
		mContext = getActivity();
		
		pBar = (ProgressBar) view.findViewById(R.id.lProgressBar);
		
		dataListView = (ListView) view.findViewById(R.id.llist);
		dataListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO Auto-generated method stub
				String path = ((HashMap<String, String>) libraryData
						.get(position)).get(MainActivity.KEY_VIDEO_PATH);
				
				String code = ((HashMap<String, String>) libraryData
						.get(position)).get(MainActivity.KEY_CODE);
				// label.setText(tit);
				// DetailsActivity da = new DetailsActivity(tit);
				Intent playerIntent = new Intent(mContext,
						VideoPlayerActivity.class);
				playerIntent.putExtra("video_path", path);
				playerIntent.putExtra("itemcode", code);
				mContext.startActivity(playerIntent);
			}
		});
		
		getDataHandler = new GetDataHandler();
		
		Thread getDataThread = new Thread(){
			public void run(){
				DatabaseHelper db = new DatabaseHelper(getActivity().getApplicationContext());
				libraryData = db.getLibraryData();
				getDataHandler.sendEmptyMessage(0);
			}
		};
		getDataThread.start();
		
		
		return view;
	}

	

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRetainInstance(true);
	}
	
	static class GetDataHandler extends Handler {

		public void handleMessage(Message msg) {
			
			pBar.setVisibility(View.GONE);
			adapter = new LibraryListAdapter(mContext, libraryData);
			dataListView.setAdapter(adapter);
		}
	};
}
