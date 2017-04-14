package com.gta.scpoa.adapter;


import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.gta.scpoa.R;
import com.gta.scpoa.entity.OfficialNotice;

public class DocumentNoticeAdapter extends BaseAdapter {
	private ArrayList<OfficialNotice> list;// 名字都固定为list
	private LayoutInflater inflater;

	public DocumentNoticeAdapter(Context context ,ArrayList<OfficialNotice> notices) {
		super();
		setData(notices);
		this.inflater = LayoutInflater.from(context);
	}

	public void setData(ArrayList<OfficialNotice> notices) {
		if (null != notices) {
			this.list = notices;
		} else {
			this.list = new ArrayList<OfficialNotice>();
		}
	}

	@Override
	public int getCount() {
		return list == null ? 0 : list.size();
	}

	@Override
	public Object getItem(int position) {
		return list.get(position);
	}

	@Override
	public long getItemId(int position) {
		return list.get(position).getId();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		final View view;
		final ViewHolder holder;

		if (null == convertView) {
			view = inflater
					.inflate(R.layout.item_officialnotice, parent, false);
			holder = new ViewHolder();
			assert view != null;
			holder.mTitleTV = (TextView) view
					.findViewById(R.id.notice_item_title_tv);
//			holder.mTypeIV = (TextView) view
//					.findViewById(R.id.notice_item_type);
			holder.mDepartmentTV = (TextView) view
					.findViewById(R.id.notice_item_depart);
			holder.mDateTV = (TextView) view
					.findViewById(R.id.notice_item_date);
			view.setTag(holder);
		} else {
			// 可复用
			view = convertView;
			holder = (ViewHolder) view.getTag();
		}
		bindView(holder, position);
		return view;
	}

	private void bindView(ViewHolder holder, int position) {
		OfficialNotice notice = list.get(position);
		holder.mTitleTV.setText(notice.getTitle());
		if(notice.getType()==1){
			holder.mTypeIV.setText("【公文】");
		}else{
			holder.mTypeIV.setText("【公告】");
		}
		holder.mDepartmentTV.setText(notice.getDepartment());
		holder.mDateTV.setText(notice.getDate());
	}

	class ViewHolder {
		TextView mTitleTV;
		TextView mTypeIV;
		TextView mDepartmentTV;
		TextView mDateTV;
	}
}
