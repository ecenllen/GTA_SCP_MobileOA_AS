package com.gta.scpoa.adapter;

import java.util.ArrayList;
import java.util.List;

import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.gta.scpoa.R;
import com.gta.scpoa.entity.TaskPeople;

public class TaskDialogAdapter extends BaseAdapter {
	private List<TaskPeople> list;
	private LayoutInflater mInflater;
	private String nextNodeId;
	
	public TaskDialogAdapter(LayoutInflater inflater) {
		this.mInflater = inflater;	
	}

	public void setData(TaskPeople taskPeople ) {
		
		if (taskPeople != null) {
			this.list = taskPeople.getData() ;
			this.nextNodeId = taskPeople.getNextNodeId();
		}else {
			this.list = new ArrayList<TaskPeople>();
		}
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return list == null ? 0 : list.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return list.get(position);
	}

	@Override
	public long getItemId(int position) {
		//return list.get(position).getId();
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		final View view;
		final ViewHolder holder;
		if (null == convertView) {
			view = mInflater.inflate(R.layout.item_task_dialog, parent, false);
			holder = new ViewHolder();
			assert view != null;
			holder.mNameTV = (TextView) view.findViewById(R.id.task_dialog_name_tv);
			holder.mTickIV = (ImageView) view.findViewById(R.id.task_dialog_tick_iv);	
			holder.mContainerRL = (RelativeLayout) view.findViewById(R.id.task_dialog_container_rl);
			view.setTag(holder);
		} else {
			// 可复用
			view = convertView;
			holder = (ViewHolder) view.getTag();
		}
		bindView(holder, position);
		return view;
	}

	private void bindView(final ViewHolder holder,final int position) {
		final TaskPeople people = list.get(position);
		//设置数据
		holder.mNameTV .setText(people.getExecutor());
		
		if (people.getType().equals("user")) {
			people.setTickHidden(false);
		}else {
			people.setTickHidden(true);
		}
		
		if (people.isTickHidden()) {
			holder.mTickIV.setVisibility(View.INVISIBLE);		
		}else {
			holder.mTickIV.setVisibility(View.VISIBLE);		
		}
		
		if (people.isTick()) {		
			holder.mTickIV.setImageResource(R.drawable.checkbox_select);		
		}else {		
			holder.mTickIV.setImageResource(R.drawable.checkbox_default);
		}	
		
		//事件监听回调
		holder.mContainerRL.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if (! people.isTickHidden()) {
					
					if (people.isTick()) {
						
						//取消打钩
						holder.mTickIV.setImageResource(R.drawable.checkbox_default);
						people.setTick(false);
						
					}else {
						//打上钩
						holder.mTickIV.setImageResource(R.drawable.checkbox_select);
						people.setTick(true);
//						cancleTick(position);
						
					}
					
					
					
				}
			}
		});
	}
	
	private void cancleTick(int position){
		for (int i = 0; i < list.size(); i++) {
			if (i != position) {
				TaskPeople p = list.get(i);
				p.setTick(false);
			}
		}
		this.notifyDataSetChanged();
	}

	class ViewHolder {
		RelativeLayout mContainerRL;
		TextView mNameTV;
		ImageView mTickIV;
	}
	
	public ArrayList<TaskPeople> getSelectedPeoples(){
		ArrayList<TaskPeople> peoples = new ArrayList<TaskPeople>();
		for (TaskPeople p : list) {
			if (p.isTick()) {
				peoples.add(p);
			}
		}
		return peoples;
	}
	public String getNextNodeId(){
		return nextNodeId;
	}

}
