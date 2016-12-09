package com.goldfist.nollygold;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.view.Menu;

public class SearchActivity extends FragmentActivity {
	
	static String itemCodeName = "";	
	static FragmentActivity _context;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_search);
		
		Intent intent = getIntent();
		itemCodeName = intent.getStringExtra("itemcodename");
		
		_context = this;
		
		openSearch(itemCodeName, 0);		
	}
	
	public static void openSearch(String itemcodename, int offset){
		
		Fragment fragment = new SearchFragment();
		
		Bundle args = new Bundle();
		args.putString("itemcodename", itemcodename);
		args.putInt("offset", offset);
		fragment.setArguments(args);
		
		FragmentTransaction transaction = _context.getSupportFragmentManager()
				.beginTransaction();
		transaction.replace(R.id.search_content_frame, fragment);
		//transaction.addToBackStack(null);
		transaction.commit();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.search, menu);
		return true;
	}
	
	

}
