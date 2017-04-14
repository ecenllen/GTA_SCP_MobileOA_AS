package com.gta.scpoa.adapter;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.gta.scpoa.R;
import com.gta.scpoa.activity.NewScheduleActivity;
import com.gta.scpoa.activity.ScheduleActivity;
import com.gta.scpoa.entity.Schedule;
import com.gta.scpoa.views.ui.SwipeLayout;
import com.gta.scpoa.views.ui.SwipeLayout.Status;

/**
 * 日程的适配器
 * 
 * @author bin.wang1
 * 
 */
public class ScheduleAdapter extends BaseSwipeAdapter {
	private ScheduleActivity mActivity;
	private ArrayList<Schedule> list;
	
	public ScheduleAdapter(ScheduleActivity activity, ArrayList<Schedule> list) {
		super();
		this.mActivity=activity;
		setData(list);
	}

	public void setData(ArrayList<Schedule> schedules) {
		if (null != schedules) {
			this.list = schedules;		
		} else {
			this.list = new ArrayList<Schedule>();
		}
	}
	
	@Override
	public int getCount() {
		return list == null ? 0 : list.size();
	}

	@Override
	public Object getItem(int position) {
		return position;
	}
	/**
	 * 返回该日程的ID
	 */
	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public int getSwipeLayoutResourceId(int position) {
		return R.id.schedule_swipe;
	}

	@Override
	public View generateView(int position, View convertView, ViewGroup parent) {	
		final View view;
		final ViewHolder holder = new ViewHolder();
		view = mActivity.getLayoutInflater().inflate(R.layout.item_schedule_listview, parent, false);
		assert view != null;
		holder.dateTV=(TextView) view.findViewById(R.id.schedule_lv_date_tv);
		holder.finishTV=(TextView) view.findViewById(R.id.schedule_lv_finish_tv);
		holder.contentTV=(TextView) view.findViewById(R.id.schedule_lv_content_tv);
		holder.containerSL=(SwipeLayout) view.findViewById(R.id.schedule_swipe);
		holder.editLL=(LinearLayout) view.findViewById(R.id.schedule_lv_edit_ll);
		holder.deleteLL=(LinearLayout) view.findViewById(R.id.schedule_lv_delete_ll);	
		view.setTag(holder);	
		return  view;
					
	}

	private void bindView(final ViewHolder holder,  final int position) {
		
		
		
		final Schedule schedule=list.get(position);
		holder.dateTV.setText(makeDate(schedule.getStartTime(),	 schedule.getEndTime()));
			
		holder.finishTV.setText(schedule.getStatus()==0?"未完成":"已完成");
				
		holder.contentTV.setText(schedule.getScheduleContent());
		holder.finishTV.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
					
				if (schedule.getStatus() == 1) {
					mActivity.updateScheduleStatus(schedule.getId(), 0);					
//					schedule.setStatus(0);
//					holder.finishTV.setText("未完成");
				}else if(schedule.getStatus() == 0){
					mActivity.updateScheduleStatus(schedule.getId(), 1);					
//					schedule.setStatus(1);
//					holder.finishTV.setText("已完成");
				}
				
			}
		});
		holder.editLL.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// 跳转
				
				Intent intent=new Intent(mActivity,NewScheduleActivity.class);
				if (schedule.getRemind() == 5||schedule.getRemind() ==1 ) {					
					intent.putExtra("ScheduleId", schedule.getId());
				}else {
					intent.putExtra("Schedule", schedule);
				}
				
				mActivity.startActivity(intent);				
			}
		});
		holder.deleteLL.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				
				final AlertDialog myDialog = new AlertDialog.Builder(mActivity).create();
				myDialog.show();
				myDialog.getWindow().setContentView(R.layout.delete_alter_dialog);
				((TextView) myDialog.getWindow().findViewById(R.id.tv_title)).setText("是否删除该日程？");
							
				myDialog.getWindow().findViewById(R.id.ok_button)
				.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						myDialog.dismiss();
						holder.containerSL.close();
						mActivity.deleteSchedule(schedule.getId());
//						list.remove(position);															
//						notifyDataSetChanged();
						
					}
				});
				myDialog.getWindow().findViewById(R.id.cancel_button)
				.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						myDialog.dismiss();
					}
				});					
			}
		});		
		
		// 双击的回调函数
		holder.containerSL.setOnDoubleClickListener(new SwipeLayout.DoubleClickListener() {
					@Override
					public void onDoubleClick(SwipeLayout layout,
							boolean surface) {
						if (holder.containerSL.getOpenStatus() == Status.Open) {
							
							holder.containerSL.close();
							notifyDataSetChanged();
						}
						
					}
				});

		// 添加删除布局的点击事件
		holder.containerSL.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				
				
				if (holder.containerSL.getOpenStatus() == Status.Open) {
					holder.containerSL.close();
					notifyDataSetChanged();
				}
			}
		});
		
		
		if (holder.containerSL.getOpenStatus() == Status.Open) {
			holder.containerSL.close();
			notifyDataSetChanged();
		}
		

		
	}
	
	
	private String makeDate(String startTime,String endTime){
		String result="";
		String[] arrs=startTime.split("T");
		String[] arre=endTime.split("T");
		String s=arrs[1];
		String e=arre[1];
		String[] strs=s.split(":");
		String[] stre=e.split(":");
		
		if (strs.length==2 && stre.length ==2) {
			result=s+"--"+e;
		}else if (strs.length==3 && stre.length==3) {
			result=strs[0]+":"+strs[1]+"--"+stre[0]+":"+stre[1];
		}		
		return result;
	}
	// 对控件的填值操作独立出来了，我们可以在这个方法里面进行item的数据赋值
	@Override
	public void fillValues(int position, View convertView) {
		// 可以通过findViewByid()找到编辑、删除两个方框中的控件，然后控制这些控件的行为	
		ViewHolder holder=(ViewHolder) convertView.getTag();
		bindView(holder, position);
	}
	
	class ViewHolder{
		TextView dateTV;//该天几点到几点，日程的时间
		TextView finishTV;//点击切换 未完成/已完成
		TextView contentTV;//日程的内容
		SwipeLayout containerSL;
		LinearLayout editLL;
		LinearLayout deleteLL;
	}

}
