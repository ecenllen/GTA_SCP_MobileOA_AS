package com.gta.scpoa.adapter;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.SectionIndexer;
import android.widget.TextView;

import com.gta.scpoa.R;
import com.gta.scpoa.entity.ContactInfo;

/**
 * 通讯录适配器
 * @author shengping.pan
 *
 */
public class ContactListViewAdapter extends BaseAdapter implements
		SectionIndexer {
	private List<ContactInfo> list = null;
	private Context mContext;
	private ArrayList<String> contactIds = null;

	/**
	 * @param mContext
	 *            上下文
	 */
	public ContactListViewAdapter(Context mContext) {
		this.mContext = mContext;
	}

	/**
	 * @param list
	 *            需要处理的数据集合
	 * @param contactIds
	 *            之前选择的联系人集合
	 */
	public void setData(List<ContactInfo> list, ArrayList<String> contactIds) {
		this.list = list;
		this.contactIds = contactIds;
	}

	/**
	 * 当ListView数据发生变化时,调用此方法来更新ListView
	 * 
	 * @param list
	 */
	public void updateListView(List<ContactInfo> list) {
		this.list = list;
		notifyDataSetChanged();
	}

	public int getCount() {
		return this.list.size();
	}

	public Object getItem(int position) {
		return list.get(position);
	}

	public long getItemId(int position) {
		return position;
	}

	public View getView(final int position, View view, ViewGroup arg2) {
		ViewHolder viewHolder = null;
		ContactInfo contactInfo = list.get(position);
		final ContactInfo mContent = contactInfo;
		if (view == null) {
			viewHolder = new ViewHolder();
			view = LayoutInflater.from(mContext).inflate(R.layout.contact_item,
					null);
			viewHolder.tvTitle = (TextView) view.findViewById(R.id.title);
			viewHolder.tvDept = (TextView) view.findViewById(R.id.tv_dept);
			viewHolder.tvLetter = (TextView) view.findViewById(R.id.catalog);
			viewHolder.tvLine = (TextView) view.findViewById(R.id.line_tv);
			viewHolder.tvPhone = (TextView) view.findViewById(R.id.tv_phone);
			viewHolder.tvDuty = (TextView) view.findViewById(R.id.tv_duty);
			viewHolder.imageView = (ImageView) view
					.findViewById(R.id.check_box);
			view.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) view.getTag();
		}

		// 通讯录添加个人联系人时，处理选中
		if (null != contactIds) {
			viewHolder.imageView.setVisibility(View.VISIBLE);
			for (String id : contactIds) {
				if (contactInfo.getUserId().equals(id)
						/*&& contactInfo.isSelected()*/) {
					viewHolder.imageView
							.setImageResource(R.drawable.checkbox_select);
				}
			}
		}

		if (viewHolder.imageView.getVisibility() == View.VISIBLE) {
			if (contactInfo.isSelected())
				viewHolder.imageView
						.setImageResource(R.drawable.checkbox_select);
			else
				viewHolder.imageView
						.setImageResource(R.drawable.checkbox_default);
		}

		// 根据position获取分类的首字母的Char ascii值
		int section = getSectionForPosition(position);

		// 如果当前位置等于该分类首字母的Char的位置 ，则认为是第一次出现
		if (position == getPositionForSection(section)) {
			viewHolder.tvLetter.setVisibility(View.VISIBLE);
			viewHolder.tvLetter.setText(mContent.getSortLetters());
		} else {
			viewHolder.tvLetter.setVisibility(View.GONE);
		}

		// 用户名处理
		String name = contactInfo.getContactName();
		if (name.length() > 10) {
			name = name.substring(0, 10) + "...";
		}
		viewHolder.tvTitle.setText(name);
		viewHolder.tvPhone.setText(contactInfo.getMobilePhone());

		// 教职类(type=3)或邮箱联系人(type=4)右侧显示部门名称,职位
		String type = contactInfo.getContactType();
		if ("3".equals(type) || "4".equals(type)) {
			viewHolder.tvDept.setText(contactInfo.getDeptName());
			viewHolder.tvDuty.setText(contactInfo.getDuty());
		}
		return view;

	}

	final static class ViewHolder {
		TextView tvLetter;
		TextView tvTitle;
		TextView tvDept;
		TextView tvLine;
		TextView tvPhone;
		TextView tvDuty;
		ImageView imageView;
	}

	/**
	 * 根据ListView的当前位置获取分类的首字母的Char ascii值
	 */
	public int getSectionForPosition(int position) {
		return list.get(position).getSortLetters().charAt(0);
	}

	/**
	 * 根据分类的首字母的Char ascii值获取其第一次出现该首字母的位置
	 */
	public int getPositionForSection(int section) {
		for (int i = 0; i < getCount(); i++) {
			String sortStr = list.get(i).getSortLetters();
			char firstChar = sortStr.toUpperCase().charAt(0);
			if (firstChar == section) {
				return i;
			}
		}

		return -1;
	}

	/**
	 * 提取英文的首字母，非英文字母用#代替。
	 * 
	 * @param str
	 * @return
	 */
	private String getAlpha(String str) {
		String sortStr = str.trim().substring(0, 1).toUpperCase();
		// 正则表达式，判断首字母是否是英文字母
		if (sortStr.matches("[A-Z]")) {
			return sortStr;
		} else {
			return "#";
		}
	}

	@Override
	public Object[] getSections() {
		return null;
	}
}