package com.gta.scpoa.adapter;

import java.util.ArrayList;
import java.util.List;

import com.gta.scpoa.R;
import com.gta.scpoa.entity.TaskNewInfor;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * 会议适配器
 * @author shengping.pan
 *
 */
public class MeetingAdapter extends BaseAdapter{

	private Context context;
	private List<TaskNewInfor> list = null;
	
	public MeetingAdapter(Context context){
		this.context = context;
	}
	
	public void setData(List<TaskNewInfor> list) {
		if(null != list){
			this.list = list;
		}else{
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
		HoldView holdView = null;
		TaskNewInfor meeting = list.get(position);
		if(convertView == null){
			convertView = LayoutInflater.from(context).inflate(R.layout.meeting_notice_item, null);
			holdView = new HoldView();
			holdView.has_attend_image = (ImageView) convertView.findViewById(R.id.has_attend_image);
			holdView.meeting_subject_text = (TextView) convertView.findViewById(R.id.meeting_subject_text);
			holdView.meeting_createtime_text =  (TextView) convertView.findViewById(R.id.meeting_createtime_text);
			holdView.meeting_time_text = (TextView) convertView.findViewById(R.id.meeting_time_text);
			convertView.setTag(holdView);
		}else{
			holdView = (HoldView) convertView.getTag();
		}
		
		//0：未读,1：已读
		if (Integer.parseInt(meeting.getIsReaded()) == 1) {
			holdView.has_attend_image.setImageResource(R.drawable.has_read);
		}else {
			holdView.has_attend_image.setImageResource(R.drawable.no_read);
		}
		String title = meeting.getSubject();
		holdView.meeting_subject_text.setText(title);
		if(meeting.getType() == -1){
			holdView.meeting_createtime_text.setText(meeting.getCreateTime());
			holdView.meeting_time_text.setText("会议时间: "+meeting.getMeetStartTime() +" - " +meeting.getMeetEndTime());
		}else{
			holdView.meeting_time_text.setText(meeting.getCreateTime());
		}
		
		return convertView;
	}

	class HoldView{
		private ImageView has_attend_image;
		private TextView meeting_subject_text;
		private TextView meeting_createtime_text;
		private TextView meeting_time_text;
	}

	
}
