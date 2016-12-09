package com.goldfist.nollygold;

import java.util.ArrayList;
import java.util.HashMap;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
//import android.widget.ImageView;
 
public class LibraryListAdapter extends BaseAdapter {
 
    private Activity activity;
    private ArrayList <HashMap<String, String>> data;
    private static LayoutInflater inflater=null;
    public ImageLoader imageLoader;
 
    public LibraryListAdapter(Activity a, ArrayList<HashMap<String, String>> d) {
        activity = a;
        data=d;
        inflater = (LayoutInflater)activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        imageLoader=new ImageLoader(activity.getApplicationContext());
    }
 
    public int getCount() {
        return data.size();
    }
 
    public Object getItem(int position) {
        return position;
    }
 
    public long getItemId(int position) {
        return position;
    }
 
    public View getView(int position, View convertView, ViewGroup parent) {
        View vi=convertView;
        if(convertView==null)
            vi = inflater.inflate(R.layout.library_list_row, null);
 
        TextView title = (TextView)vi.findViewById(R.id.lltitle);
 
        HashMap<String, String> movie = new HashMap<String, String>();
        movie = data.get(position);
        
        title.setText(movie.get(MainActivity.KEY_TITLE));
        return vi;
    }
}
