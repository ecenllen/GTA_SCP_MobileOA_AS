package com.gta.scpoa.adapter;

import java.util.ArrayList;
import java.util.List;

import android.graphics.Color;
import android.text.Html;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.gta.scpoa.R;
import com.gta.scpoa.entity.TabAttachInfo;
import com.gta.scpoa.entity.TableInfor;
import com.gta.scpoa.util.ExplainTableUtil;

/**
 * 用于OfficialNoticeDetailActivity中附件的ListView
 * 
 * @author bin.wang1
 * 
 */
public class OfficialNoticeDetailAdapter extends BaseAdapter {
	private List<TableInfor> list;// 名字都固定为list
	private LayoutInflater inflater;

	public OfficialNoticeDetailAdapter(LayoutInflater inflater) {
		super();		
		this.inflater = inflater;
	}

	public void setData(List<TableInfor> tables) {
		if (null != tables) {
			this.list = tables;
		} else {
			this.list = new ArrayList<TableInfor>();
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
			view = inflater	.inflate(R.layout.item_officialdetail, parent, false);
			holder = new ViewHolder();
			assert view != null;
			holder.mKeyTV = (TextView) view.findViewById(R.id.official_detail_key_tv);
			holder.mValueContainerLL = (LinearLayout) view.findViewById(R.id.official_detail_value_ll);
			view.setTag(holder);
		} else {
			// 可复用
			view = convertView;
			holder = (ViewHolder) view.getTag();
		}
		
		//背景颜色
		if(position%2==0){
			view.setBackgroundColor(0xffdbeef4);
		}else{
			view.setBackgroundColor(Color.WHITE);
		}
		
		bindView(holder, position);
		return view;
	}

	private void bindView(ViewHolder holder, int position) {
		
		TableInfor table = list.get(position);
		
				
		int type = table.getType();
																
		if (type == 9) {//是附件
//			变换位置，仍到最后去									
			//解析附件的名称
			List<TabAttachInfo> list = ExplainTableUtil.getTabFJ(table.getValue());								
			for (TabAttachInfo tabAttachInfo : list) {				
				View v = createAttachLayout(tabAttachInfo.getName());
				holder.mValueContainerLL.addView(v);			
			}
			
		}else if (type == 12) {
			
			View v = createAttachLayout(table.getValue());
			holder.mValueContainerLL.addView(v);
			
			
			
			
			
			
		}else {
			holder.mKeyTV.setText(table.getKey());
			TextView tv = createTV(table.getValue());
			holder.mValueContainerLL.addView(tv);
		}
					
	
	}

	class ViewHolder {
		TextView mKeyTV;
		LinearLayout mValueContainerLL;
	}
	private TextView createTV(CharSequence text){
		TextView tv = new TextView(inflater.getContext());
		tv.setTextSize(15);
		tv.setGravity(Gravity.LEFT);
		tv.setText(text);
		return tv;
	}
	
	private View createAttachLayout(CharSequence text){
		
		View view = inflater.inflate(R.layout.item_attach, null);
		TextView nameTV = (TextView) view.findViewById(R.id.officialdetail_attachment_name_tv);
		nameTV.setText(text);
		TextView downloadTV = (TextView) view.findViewById(R.id.officialdetail_attachment_download_tv);
		downloadTV.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				
			}
		});
		return view;
	}
	
}
