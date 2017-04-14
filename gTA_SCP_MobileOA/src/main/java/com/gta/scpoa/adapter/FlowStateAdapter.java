package com.gta.scpoa.adapter;

import java.util.ArrayList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.gta.scpoa.R;
import com.gta.scpoa.entity.ProcessState;
import com.gta.scpoa.util.StringUtils;

public class FlowStateAdapter extends BaseAdapter {

	private ArrayList<ProcessState> list;
	private LayoutInflater mInflater;

	public FlowStateAdapter(LayoutInflater mInflater) {
		super();
		this.mInflater = mInflater;
	}

	public void setData(ArrayList<ProcessState> states) {
		if (null != states) {
			this.list = states;		
			hideCycle();
		} else {
			this.list = new ArrayList<ProcessState>();
		}
	}

	private void hideCycle(){
		String taskName_temp ="";
		for (ProcessState process : list) {
			
			if (process.getTaskName().equals(taskName_temp)) {
				process.setShouldHideCycle(true);
			}else {
				taskName_temp = process.getTaskName();
			}
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
			view = mInflater.inflate(R.layout.my_item_task_process, parent,false);
			holder = new ViewHolder();
			assert view != null;
			holder.mCircleIconIV = (ImageView) view.findViewById(R.id.process_state_green_circle_iv);
//			holder.mArrowIV = (ImageView) view.findViewById(R.id.process_state_arrow_iv);
			holder.mArrowIV = (View)view.findViewById(R.id.process_state_arrow_iv);
			holder.mTaskNameTV = (TextView) view.findViewById(R.id.process_state_task_name_tv);
			holder.mStateTV = (TextView) view.findViewById(R.id.process_state_task_state_tv);
			holder.mContentTV = (TextView) view.findViewById(R.id.process_state_content_tv);
			view.setTag(holder);
		} else {
			// 可复用
			view = convertView;
			holder = (ViewHolder) view.getTag();
		}
		bindView(holder, position);
		return view;
	}

	private void bindView(final ViewHolder holder, int position) {
		
		ProcessState ps = list.get(position);
		String endTime = ps.getEndTime();
		

		String taskName = ps.getTaskName()+"-"+ps.getExeFullname();
	
		
		if (position == list.size() -1) {
								
			//最后一个节点要分两种情况考虑
					
			if (!StringUtils.isEmpty(endTime)) {
				
				holder.mCircleIconIV.setImageResource(R.drawable.process_state_green_circle);	
				holder.mArrowIV.setVisibility(View.INVISIBLE);			
				
				holder.mTaskNameTV.setText(taskName);		
				holder.mTaskNameTV.setTextColor(Color.BLACK);
						
				holder.mStateTV.setText(ps.getCheckStatus());
				holder.mStateTV.setTextColor(Color.LTGRAY);

				holder.mContentTV.setText(ps.getOpinion());
				holder.mContentTV.setVisibility(View.VISIBLE);			
																
			} else {
				holder.mCircleIconIV.setVisibility(View.VISIBLE);
				holder.mCircleIconIV.setImageResource(R.drawable.process_state_red_circle);
				
				holder.mTaskNameTV.setText(taskName);		
				holder.mTaskNameTV.setTextColor(Color.RED);
									
				holder.mStateTV.setText(ps.getCheckStatus());
				holder.mStateTV.setTextColor(Color.RED);
				
				if (!ps.isShouldHideCycle()) {					
					holder.mArrowIV.setVisibility(View.INVISIBLE);
					holder.mContentTV.setVisibility(View.INVISIBLE);
				}else {
//					holder.mArrowIV.setImageResource(R.drawable.test_ar1);
					holder.mArrowIV.setVisibility(View.VISIBLE);
//					holder.mContentTV.setVisibility(View.VISIBLE);
//					holder.mContentTV.setText("\n");
				}

												
			}
					
		}else {
			
			
			
			if (!StringUtils.isEmpty(endTime)) {
				
				
				holder.mCircleIconIV.setImageResource(R.drawable.process_state_green_circle);					
				holder.mArrowIV.setVisibility(View.VISIBLE);			
//				holder.mArrowIV.setImageResource(R.drawable.test_ar1);
				
				holder.mTaskNameTV.setText(taskName);		
				holder.mTaskNameTV.setTextColor(Color.BLACK);
						
				holder.mStateTV.setText(ps.getCheckStatus());
				holder.mStateTV.setTextColor(Color.LTGRAY);

				holder.mContentTV.setVisibility(View.VISIBLE);			
				holder.mContentTV.setText(ps.getOpinion());
				
				
				
				
			}else {
				
				holder.mCircleIconIV.setImageResource(R.drawable.process_state_red_circle);					
				holder.mArrowIV.setVisibility(View.VISIBLE);			
//				holder.mArrowIV.setImageResource(R.drawable.test_ar1);
				
				holder.mTaskNameTV.setText(taskName);		
				holder.mTaskNameTV.setTextColor(Color.RED);
						
				holder.mStateTV.setText(ps.getCheckStatus());
				holder.mStateTV.setTextColor(Color.RED);

				holder.mContentTV.setVisibility(View.VISIBLE);			
				holder.mContentTV.setText(ps.getOpinion()==null?"":ps.getOpinion());				
				
			}
			
		
								
		}
		
			
		if (ps.isShouldHideCycle()) {
			holder.mCircleIconIV.setVisibility(View.GONE);
//			holder.mCircleIconIV.setImageResource(R.drawable.test_ar1);	
		}else {			
			holder.mCircleIconIV.setVisibility(View.VISIBLE);
			
		}
		
		
	
		
	}
	
	

	class ViewHolder {
		ImageView mCircleIconIV;
//		ImageView mArrowIV;
		View mArrowIV;
		TextView mTaskNameTV;
		TextView mStateTV;
		TextView mContentTV;
	}

}
