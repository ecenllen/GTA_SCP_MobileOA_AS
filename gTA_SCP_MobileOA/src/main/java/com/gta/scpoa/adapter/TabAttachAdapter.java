package com.gta.scpoa.adapter;

import java.util.List;

import com.gta.scpoa.R;
import com.gta.scpoa.entity.TabAttachInfo;
import com.gta.scpoa.util.FileUtils;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class TabAttachAdapter extends BaseAdapter{

	private Context context;
	private List<TabAttachInfo> list;
	
	public TabAttachAdapter(Context context,List<TabAttachInfo> list){
		this.context = context;
		this.list = list;
	}
	
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return list.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return list.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		HoldView holdView = null;
		if(convertView==null){
			convertView = LayoutInflater.from(context).inflate(R.layout.tab_attch_item, null);
			holdView = new HoldView();
			holdView.text =(TextView) convertView.findViewById(R.id.attch_item_text);
			holdView.attachImage = (ImageView) convertView.findViewById(R.id.tab_attach_image);
			holdView.text_size = (TextView) convertView.findViewById(R.id.attach_text_size);
			convertView.setTag(holdView);
		}else{
			holdView = (HoldView) convertView.getTag();
		}
		
		TabAttachInfo tabAttachInfo = list.get(position);
		holdView.text.setText(tabAttachInfo.getName());
		defineImage(holdView.attachImage, tabAttachInfo.getName());
		holdView.text_size.setText("("+FileUtils.FormetFileSize(tabAttachInfo
				.getSize())+")");
		return convertView;
	}

	private void defineImage(ImageView imageView,String fileName){
		 String end=fileName.substring(fileName.lastIndexOf(".")
                +1,fileName.length()).toLowerCase();
		   if (end.equals("jpg")||end.equals("png")||   
                end.equals("jpeg")||end.equals("bmp")){
			   imageView.setImageResource(R.drawable.pic);
		   }else{
			   imageView.setImageResource(R.drawable.other);
		   }
	}
	
	class HoldView{
		ImageView attachImage ;   //图片
		TextView text;
		TextView text_size;
	}
	
}
