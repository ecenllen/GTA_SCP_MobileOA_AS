package com.gta.scpoa.adapter;

import java.util.ArrayList;
import java.util.List;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.gta.scpoa.R;
import com.gta.scpoa.entity.TaskNewInfor;

public class OfficialNoticeAdapter extends BaseAdapter {

	private List<TaskNewInfor> list;
	private LayoutInflater inflater;

	public OfficialNoticeAdapter(LayoutInflater inflater) {
		super();		
		this.inflater = inflater;
	}

	public void setData(List<TaskNewInfor> notices) {
		if (null != notices) {
			this.list = notices;
		} else {
			this.list = new ArrayList<TaskNewInfor>();
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
		return position;
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
			holder.mFlagIV = (ImageView) view.findViewById(R.id.notice_item_is_read_image);
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
		TaskNewInfor notice = list.get(position);
		//0或1,1已读
		if (Integer.parseInt(notice.getIsReaded()) == 1) {
			holder.mFlagIV.setImageResource(R.drawable.has_read);
		}else {
			holder.mFlagIV.setImageResource(R.drawable.no_read);
		}
		
		String title = notice.getSubject();
//		if (title.length()>8) {
//			title= title.substring(0, 7)+"...";
//		}
		holder.mTitleTV.setText(title);
		
//		holder.mTypeIV.setText("【公告】");
		holder.mDepartmentTV.setText(notice.getCreator());
		holder.mDateTV.setText(notice.getCreateTime().split(" ")[0]);
	}

	class ViewHolder {
		ImageView mFlagIV;
		TextView mTitleTV;
//		TextView mTypeIV;
		TextView mDepartmentTV;
		TextView mDateTV;
	}
	
	public void changeState(String id){
		for (TaskNewInfor info : list) {
			if (info.getCopyId().equals(id)) {
				info.setIsReaded("1");
				break;
			}
		}
		notifyDataSetChanged();
	}
}
