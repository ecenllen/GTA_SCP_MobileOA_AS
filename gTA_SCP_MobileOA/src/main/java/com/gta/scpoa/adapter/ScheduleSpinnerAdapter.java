package com.gta.scpoa.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.gta.scpoa.R;

public class ScheduleSpinnerAdapter extends BaseAdapter{
	String[] list=new String[]{"单次提醒","工作日提醒","每日提醒","每周提醒","每月提醒","不提醒"};
	private LayoutInflater mInflater;


	public ScheduleSpinnerAdapter(LayoutInflater mInflater) {
		super();
		this.mInflater = mInflater;
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
			view=mInflater.inflate(R.layout.item_schedule_sp_tv, parent, false);
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
