package com.gta.scpoa.adapter;

import java.util.ArrayList;
import java.util.List;

import com.gta.scpoa.R;
import com.gta.scpoa.entity.TableInfor;
import com.gta.scpoa.views.TabAttachListView;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
public class TabAdapter extends BaseAdapter{
	private List<TableInfor> lists = new ArrayList<TableInfor>();
	private Context context ;
	
	public TabAdapter(Context context,List<TableInfor> mlists){
		this.context = context;
		lists = mlists;
	}
	
	public TabAdapter(Context context){
		this.context = context;
	}
	
	/**
	 * 注册返回的类型
	 **/
	@Override 
	public int getItemViewType(int position) { 
		int type = lists.get(position).getType();
		if(type == 9||type == 12) return 1;
		else return 0;
	} 
	
	@Override 
	 public int getViewTypeCount() { 
		return 2; 
	} 
	
	
	public void setData(List<TableInfor> mlists){
		lists.clear();
		lists.addAll(mlists);
		List<TableInfor> tempList = new ArrayList<TableInfor>();
		for (TableInfor tableInfor : lists) {
			if ((tableInfor.getType() == 9 || tableInfor.getType() == 12)
					&& !tableInfor.getValue().equals("")) {
				tempList.add(tableInfor);
			}
		}
		lists.removeAll(tempList);
		lists.addAll(tempList);
	}
	
	public List<TableInfor> getList(){
		return lists;
	}
	
	@Override
	public int getCount() {
		return lists.size();
	}

	@Override
	public Object getItem(int position) {
		return lists.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		TableInfor tableInfor = lists.get(position);
		int type = tableInfor.getType();//download
		if((type == 9||type == 12)&&!tableInfor.getValue().equals("")){
			convertView = new TabAttachListView(context, tableInfor)
			.getAttachView(convertView);
		}else{
			convertView = LayoutInflater.from(context).inflate(R.layout.tab_item, null);
			TextView keyText = (TextView) convertView.findViewById(R.id.keyText);
			EditText valueEt = (EditText) convertView.findViewById(R.id.valueEt);
			TextView tittleText = (TextView) convertView.findViewById(R.id.tab_tittle_text);
			LinearLayout tittleLayout = (LinearLayout) convertView.findViewById(R.id.tittleLayout);
			LinearLayout bobyLayout = (LinearLayout) convertView.findViewById(R.id.childLayout);
			
			if(tableInfor.isTitle()){
				bobyLayout.setVisibility(View.GONE);
				tittleLayout.setVisibility(View.VISIBLE);
				tittleText.setText(tableInfor.getValue());
			}else{
				bobyLayout.setVisibility(View.VISIBLE);
				tittleLayout.setVisibility(View.GONE);
				keyText.setText(tableInfor.getKey());
				valueEt.setText(tableInfor.getValue());
				valueEt.setEnabled(false);
				valueEt.addTextChangedListener(new MyTextWatcher(position));
			}
		}
		return convertView;
	}
	
	
	class MyTextWatcher implements TextWatcher{

		int index  = -1;
		public MyTextWatcher(int position){
			index = position;
		}
		
		@Override
		public void beforeTextChanged(CharSequence s, int start, int count,
				int after) {
		}

		@Override
		public void onTextChanged(CharSequence s, int start, int before,
				int count) {
		}

		@Override
		public void afterTextChanged(Editable s) {
			String  str = s.toString();
			lists.get(index).setValue(str);
		}
		
	}
}
