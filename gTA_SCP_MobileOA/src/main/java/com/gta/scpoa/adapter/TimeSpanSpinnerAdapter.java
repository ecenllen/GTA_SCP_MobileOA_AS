package com.gta.scpoa.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.gta.scpoa.R;

public class TimeSpanSpinnerAdapter extends BaseAdapter{
	String[] list=new String[]{"10分钟","20分钟","30分钟"};
	private Context mContext;

	
	
	public TimeSpanSpinnerAdapter(Context context) {
		super();
		this.mContext=context;
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return list == null ? 0 : list.length;
	}

	@Override
	public String getItem(int position) {
		// TODO Auto-generated method stub
		return list[position];
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		final View view;
		final ViewHolder holder;
		
		if (null == convertView) {
			view=LayoutInflater.from(mContext).inflate(R.layout.item_textview, parent, false);
			holder = new ViewHolder();
			assert view != null;
			holder.tv=(TextView) view;
			view.setTag(holder);
		}else {
			// 可复用
			view = convertView;
			holder = (ViewHolder) view.getTag();
		}
		
		bindView(holder, position);
		return view;
	}
	
	private void bindView(ViewHolder holder, int position) {
		holder.tv.setText(getItem(position));
		
	}

	class ViewHolder{
		TextView tv;
	}

}
