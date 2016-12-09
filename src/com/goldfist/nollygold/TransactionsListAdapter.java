package com.goldfist.nollygold;

import java.util.ArrayList;
import java.util.HashMap;
 
import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
//import android.widget.ImageView;
import android.widget.TextView;
 
public class TransactionsListAdapter extends BaseAdapter {
 
    private Activity activity;
    private ArrayList <HashMap<String, String>> data;
    private static LayoutInflater inflater=null;
    public ImageLoader imageLoader;
 
    public TransactionsListAdapter(Activity a, ArrayList<HashMap<String, String>> d) {
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
            vi = inflater.inflate(R.layout.transaction_list_row, null);
 
        TextView title = (TextView)vi.findViewById(R.id.rented_title); // title
        TextView rented = (TextView)vi.findViewById(R.id.rentedText); // artist name
        TextView expires = (TextView)vi.findViewById(R.id.expiresText); // price
        ImageView thumb_image=(ImageView)vi.findViewById(R.id.rented_list_image); // thumb image
 
        HashMap<String, String> movie = new HashMap<String, String>();
        movie = data.get(position);
 
        int loader = R.drawable.gradient_bg;
        // Setting all values in listview
        title.setText(movie.get(MainActivity.KEY_TITLE));
        rented.setText(movie.get(MainActivity.KEY_TRANS_DATE) + " - " + movie.get(MainActivity.KEY_TRANS_TIME));
        expires.setText(movie.get(MainActivity.KEY_EXPIRY_DATE));
        imageLoader.DisplayImage(movie.get(MainActivity.KEY_THUMB_URL), loader,  thumb_image);
        return vi;
    }
}
