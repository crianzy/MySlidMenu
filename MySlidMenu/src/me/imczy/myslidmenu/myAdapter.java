package me.imczy.myslidmenu;

import java.util.zip.Inflater;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class myAdapter extends BaseAdapter {

	private Context mContext;

	public myAdapter(Context c) {
		mContext = c;
	}

	@Override
	public int getCount() {
		return MainActivity.dataList.size();
	}

	@Override
	public Object getItem(int position) {
		return position;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		Holder myHolder;
		if (convertView == null) {
			myHolder = new Holder();
			LayoutInflater inflater = LayoutInflater.from(mContext);
			convertView = inflater.inflate(R.layout.list_item, null);
			myHolder.textView = (TextView) convertView.findViewById(R.id.textView);
			convertView.setTag(myHolder);
		} else {
			myHolder = (Holder) convertView.getTag();
		}
		myHolder.textView.setText(MainActivity.dataList.get(position));
		return convertView;
	}

	class Holder {
		public TextView textView;
	}

}
