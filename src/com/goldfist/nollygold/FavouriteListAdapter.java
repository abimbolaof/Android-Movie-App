package com.goldfist.nollygold;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.goldfist.nollygold.ServiceAccess.ItemData;
//import android.widget.ImageView;
 
public class FavouriteListAdapter extends BaseAdapter {
 
    private Activity activity;
    private ItemData[] data;
    private static LayoutInflater inflater=null;
    public ImageLoader imageLoader;
 
    public FavouriteListAdapter(Activity a, ItemData[] d) {
        activity = a;
        data=d;
        inflater = (LayoutInflater)activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        imageLoader=new ImageLoader(activity.getApplicationContext());
    }
 
    public int getCount() {
        return data.length;
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
        
        title.setText(data[position].title);
        return vi;
    }
}
