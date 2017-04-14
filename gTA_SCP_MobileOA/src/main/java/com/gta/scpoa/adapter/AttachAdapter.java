package com.gta.scpoa.adapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


import com.gta.scpoa.R;
import com.gta.scpoa.entity.MailAttachInfo;
import com.gta.scpoa.util.FileUtils;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class AttachAdapter extends BaseAdapter{

	private List<MailAttachInfo> listMap = new ArrayList<MailAttachInfo>(); //邮件存放的list
	private Context mContext;
	private boolean isShowLoad = false;
	private Handler handler;
	public AttachAdapter(Context c, 
			List<MailAttachInfo> listMap,
			boolean isShowLoad,Handler handler){
		mContext = c;
		this.listMap = listMap;
		this.isShowLoad = isShowLoad;
		this.handler = handler;
	}
	
	public AttachAdapter(Context c){
		mContext = c;
	}
	
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return listMap.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return listMap.get(position);
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
		if(convertView == null){
			convertView = LayoutInflater.from(mContext).inflate(R.layout.attach_file_item, null);
			holdView = new HoldView();
			holdView.attachImage = (ImageView) convertView.findViewById(R.id.attach_item_image);
			holdView.nameText = (TextView) convertView.findViewById(R.id.attach_item_name_text);
			holdView.sizeText = (TextView) convertView.findViewById(R.id.attach_item_size_text);
			holdView.loadText = (TextView) convertView.findViewById(R.id.attach_load_text);
			convertView.setTag(holdView);
		}else{
			holdView = (HoldView) convertView.getTag();
		}
		
		if(isShowLoad){  //下载时候显示
			holdView.loadText.setVisibility(View.VISIBLE);
		}else{
			holdView.loadText.setVisibility(View.GONE);
		}
		
		holdView.loadText.setOnClickListener(new MyOnClickListener(position));
		
		MailAttachInfo mailAttachInfo = listMap.get(position);
		
		String fileName ="";
		fileName = mailAttachInfo.getFileName();
		holdView.nameText.setText(fileName+mailAttachInfo.getFileType());
		holdView.sizeText.setText("("+FileUtils.FormetFileSize(mailAttachInfo.getFileSize())+")");
		defineImage(holdView.attachImage, fileName);
		return convertView;
	}

	class HoldView{
		ImageView attachImage ;   //图片
		TextView nameText;        //名字
		TextView sizeText;        //大小
		TextView loadText;        //下载
	}
	
	public void setShowDown(boolean isShowDown){
		this.isShowLoad = isShowDown;
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
	
	
	private class MyOnClickListener implements OnClickListener{  
        private int index=0;  
        public MyOnClickListener(int i){  
            index=i;  
        }  
        public void onClick(View v) { 
        	int id = v.getId();
        	switch (id) {
			case R.id.attach_load_text: //下载
				Message msg = handler.obtainMessage(10); // 失败返回
				msg.obj = index;
				msg.sendToTarget();
				break;
			default:
				break;
			}
        }  
    }
}
