package com.gta.scpoa.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.gta.scpoa.R;
import com.gta.scpoa.entity.HomeItem;

import java.util.ArrayList;


public class HomeGridAdapter extends BaseAdapter{
	private ArrayList<HomeItem> list;
	private LayoutInflater inflater;
	
	
	
	public HomeGridAdapter(ArrayList<HomeItem> homeItems, LayoutInflater inflater) {
		super();
		setData(homeItems);
		this.inflater = inflater;
	}

	public void setData(ArrayList<HomeItem> homeItems) {
		if (null != homeItems) {
			this.list = homeItems;
		} else {
			this.list = new ArrayList<HomeItem>();
		}
	}

	@Override
	public int getCount() {
		return list == null ? 0 : list.size();
	}

	@Override
	public Object getItem(int arg0) {
		return list.get(arg0);
	}

	@Override
	public long getItemId(int arg0) {
		return list.get(arg0).getId();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		final View view;
		final ViewHolder holder;

		if (null == convertView) {
			view = inflater.inflate(R.layout.item_home_gridview, parent, false);
			holder = new ViewHolder();
			assert view != null;
			holder.mBoxLL=(LinearLayout)view.findViewById(R.id.gridview_item_box_ll);
			holder.mIconIV=(ImageView) view.findViewById(R.id.gridview_item_icon_iv);
			holder.mTitleTV=(TextView) view.findViewById(R.id.gridview_item_title_tv);
			holder.mNotiNumTV = (TextView) view	.findViewById(R.id.gridviewitem_notinum_tv);
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
		HomeItem hitem=list.get(position);
		
		holder.mBoxLL.setBackgroundResource(hitem.getLlBackground());
		holder.mIconIV.setBackgroundResource(hitem.getIvDrawable());
		
		holder.mTitleTV.setText(hitem.getTitle());
		
		if (String.valueOf(0).equals(hitem.getNoticeNum())) {
			holder.mNotiNumTV.setVisibility(View.INVISIBLE);
		}else {
			holder.mNotiNumTV.setVisibility(View.VISIBLE);
			holder.mNotiNumTV.setText(hitem.getNoticeNum());
		}

	}

	class ViewHolder{
		LinearLayout mBoxLL;
		ImageView mIconIV;
		TextView mTitleTV;
		TextView mNotiNumTV;
	}
}
