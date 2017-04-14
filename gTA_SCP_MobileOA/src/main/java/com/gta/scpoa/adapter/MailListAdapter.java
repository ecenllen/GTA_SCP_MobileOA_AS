package com.gta.scpoa.adapter;

import java.util.ArrayList;
import java.util.List;

import com.gta.scpoa.R;
import com.gta.scpoa.activity.MailMainNewActivity;
import com.gta.scpoa.biz.impl.MailInforBizImpl;
import com.gta.scpoa.entity.MailInfor;
import com.gta.scpoa.util.UIUtils;
import com.gta.scpoa.views.ui.SimpleSwipeListener;
import com.gta.scpoa.views.ui.SwipeLayout;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Handler;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;



public class MailListAdapter extends BaseSwipeAdapter{

	
	private Context mContext;
	private Handler handler;
	private List<MailInfor> listInfors = new ArrayList<MailInfor>();
	private boolean isReciveMail = false;   //是否是收件箱
	private boolean isrecycle = false;      //是否是回收站
	private boolean isAllOption = false ;    //是否有批量操作
	public MailListAdapter(Context mContext, List<MailInfor> listInfors, Handler handler) {
		this.mContext = mContext;
		this.listInfors = listInfors;
		this.handler = handler;
	}

	public void setMailStatue(boolean isReciveMail,boolean isrecycle){
		this.isReciveMail = isReciveMail;
		this.isrecycle = isrecycle;
	}
	
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return listInfors.size();
	}

	@Override
	public Object getItem(int position) {
		return position;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public int getSwipeLayoutResourceId(int position) {
		// TODO Auto-generated method stub
		return R.id.recive_swipe;
	}

	public void setList( List<MailInfor> listInfors){
		this.listInfors = listInfors;
	}
	
	
	
	@Override
	public View generateView( int position, View v, ViewGroup parent) {
		// TODO Auto-generated method stub
		ViewHolder holder;
		if (v == null) {
			holder = new ViewHolder();
			v = LayoutInflater.from(mContext).inflate(R.layout.mail_xlistview_item_layout,
					parent, false);
			holder.swipeLayout = (SwipeLayout) v.findViewById(R.id.recive_swipe);
			holder.ll_menu = (LinearLayout) v.findViewById(R.id.recive_ll_menu);
		} else {
			holder = (ViewHolder) v.getTag();
		}
		
		
		holder.swipeLayout.addSwipeListener(new SimpleSwipeListener() {
			@Override
			public void onOpen(SwipeLayout layout) {
//				UIUtils.ToastMessage(mContext, "open");
			}
		});
		
		FrameLayout.LayoutParams layoutParam = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT,
				FrameLayout.LayoutParams.MATCH_PARENT);
		holder.ll_menu.setLayoutParams(layoutParam);
		return v;
	}

	
	@Override
	public void fillValues(int position, View convertView) {
		// TODO Auto-generated method stub
		ImageView isHasReadIamge = (ImageView) convertView.findViewById(R.id.is_read_mail_image);
		TextView senderText  = (TextView) convertView.findViewById(R.id.mail_sender_text);
		TextView subjectText = (TextView) convertView.findViewById(R.id.mail_subject_text);
		TextView contentText = (TextView) convertView.findViewById(R.id.mail_content_text);
		TextView timeText = (TextView) convertView.findViewById(R.id.show_time_text);
		ImageView attachImage  = (ImageView) convertView.findViewById(R.id.is_has_attach_image);
		ImageView checkBox = (ImageView) convertView.findViewById(R.id.mail_checkBox);
 		
		LinearLayout reSendLayout = (LinearLayout) convertView.findViewById(R.id.recive_send_layout);
		LinearLayout lajiLayout = (LinearLayout) convertView.findViewById(R.id.recive_trash_layout);
		
		TextView fastSendText = (TextView) convertView.findViewById(R.id.isFastSendText);
		
		
		reSendLayout.setOnClickListener(new MyOnClickListener(position));
		lajiLayout.setOnClickListener(new MyOnClickListener(position));
		
		
		MailInfor mailInfor = listInfors.get(position);
		
		/*有批量操作的情况*/
		if(isAllOption){
			closeItem(position);
			reSendLayout.setVisibility(View.GONE);// 隐藏快速回复
			lajiLayout.setVisibility(View.GONE);  
			checkBox.setVisibility(View.VISIBLE);  //显示checkBOX
		}else{
			if(isReciveMail){
				fastSendText.setText("  回复  ");
				reSendLayout.setVisibility(View.VISIBLE); //显示快速发送
			}else{
				if(isrecycle){
					fastSendText.setText("  恢复  ");
					reSendLayout.setVisibility(View.VISIBLE);//变为快速恢复
				}else{
					reSendLayout.setVisibility(View.GONE);// 隐藏快速回复
				}
				
			}
			lajiLayout.setVisibility(View.VISIBLE);
			checkBox.setVisibility(View.GONE);   //隐藏checkBox
		}
		
		
		/*是否删除打勾*/
		if(mailInfor.isPrefreDel()){
			checkBox.setImageResource(R.drawable.checkbox_select);
		}else{
			checkBox.setImageResource(R.drawable.checkbox_default);
		}
		
		RelativeLayout contentLayout = (RelativeLayout) convertView.findViewById(R.id.mail_all_contant_layout);
//		if(position%2 == 0) {
//			contentLayout.setBackgroundColor(0xffdbeef4);
//		} else{
//			contentLayout.setBackgroundColor(Color.WHITE);
//		}
		
		
		
		/*是否可读*/
		if(isReciveMail){
			isHasReadIamge.setVisibility(View.VISIBLE);
			if(mailInfor.isRead()){
				isHasReadIamge.setImageResource(R.drawable.has_read);
			}else{
				isHasReadIamge.setImageResource(R.drawable.no_read);
			}
		}else{
			isHasReadIamge.setVisibility(View.GONE);
		}
		
		
		
		senderText.setText(mailInfor.getUserName());
		subjectText.setText("主题:"+mailInfor.getOutBoxTheme());
		contentText.setText("内容:"+mailInfor.getContentString());
		
		String dataString  = mailInfor.getCreateTime().split("T")[0];
		String timeString  = mailInfor.getCreateTime().split("T")[1];
		String hourString = timeString.split(":")[0];
		String minString = timeString.split(":")[1];
		timeText.setText(dataString+"  "+hourString+":"+minString);
		
		/*是否显示附件*/
		if(mailInfor.isAttach()){
			attachImage.setVisibility(View.VISIBLE);
		}else{
			attachImage.setVisibility(View.GONE);
		}
	}

	public class ViewHolder {
		public SwipeLayout swipeLayout;
		public LinearLayout ll_menu;
		public TextView position;
	}
	
	
	
	
	public void setAllOption(boolean isAllOption){
		this.isAllOption = isAllOption;
	}
	
	
	public boolean IsAllOption(){
		return isAllOption;
	}
	
	
	
	/*获取全部删除的的*/
	public List<MailInfor> getAllCheck(){
		List<MailInfor> list = new ArrayList<MailInfor>();
		for(MailInfor mailInfor : listInfors){
			if(mailInfor.isPrefreDel()){
				list.add(mailInfor);
			}
		}
		return  list;
	}
	
	/*是不是回收站*/
	public boolean IsRecycle(){
		return isrecycle;
	}
	
	private void goFastSendAialog(final int tempPosition){
		final AlertDialog myDialog = new AlertDialog.Builder(mContext)
		.create();
		myDialog.show();
		myDialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
		myDialog.getWindow().setContentView(R.layout.fase_send_layout);
		final EditText edit = (EditText) myDialog.getWindow().findViewById(R.id.fastSendEdit);
		edit.setText("");
		edit.setGravity(Gravity.TOP|Gravity.LEFT);
		Button fast_send_ok_button = (Button)myDialog.getWindow().findViewById(R.id.fast_send_ok_button);
		fast_send_ok_button.setText("回复");
		fast_send_ok_button.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if(edit.getText().toString().trim().equals("")){
					UIUtils.ToastMessage(mContext, "内容不能为空");
				}else{
					MailMainNewActivity activity = (MailMainNewActivity) mContext;
					activity.showProgressDialog("发送中...");
					new MailInforBizImpl(mContext).fastSendMail(listInfors.get(tempPosition).getId(),
							edit.getText().toString(), handler);
					myDialog.dismiss();
				}
			}
		});
		Button fast_send_cancel_button = (Button)myDialog.getWindow().findViewById(R.id.fast_send_cancel_button);
		fast_send_cancel_button.setText("取消");
		fast_send_cancel_button.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				myDialog.dismiss();
			}
		});
	}
	
	
	
	private void showDeleteDialog(final int tempPosition1){
		final AlertDialog dialog = new AlertDialog.Builder(mContext).create();
		dialog.show();
		dialog.getWindow().setContentView(R.layout.delete_alter_dialog);
		TextView textView = (TextView) dialog.getWindow().findViewById(R.id.tv_title);
		textView.setText("是否删除该邮件?");
		dialog.getWindow().findViewById(R.id.ok_button)
		.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				MailMainNewActivity activity = (MailMainNewActivity) mContext;
				activity.showProgressDialog("删除中..");
				List<MailInfor> listMailInfors = new ArrayList<MailInfor>();
				listMailInfors.add(listInfors.get(tempPosition1));
				new MailInforBizImpl(mContext)
				.deleteMail(listMailInfors, isrecycle,
							listInfors.get(tempPosition1).getMailType(), handler);
				dialog.dismiss();
			}
		});
		dialog.getWindow().findViewById(R.id.cancel_button)
		.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				dialog.dismiss();
			}
		});
		
	}
	
	
	private void showFastReSumeDialog(final int tempPosition1){
		final AlertDialog dialog = new AlertDialog.Builder(mContext).create();
		dialog.show();
		dialog.getWindow().setContentView(R.layout.delete_alter_dialog);
		TextView textView = (TextView) dialog.getWindow().findViewById(R.id.tv_title);
		textView.setText("是否恢复该邮件?");
		dialog.getWindow().findViewById(R.id.ok_button)
		.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				MailMainNewActivity activity = (MailMainNewActivity) mContext;
				activity.showProgressDialog("恢复中..");
				List<MailInfor> listMailInfors = new ArrayList<MailInfor>();
				listMailInfors.add(listInfors.get(tempPosition1));
				new MailInforBizImpl(mContext)
				.reSumeMails(listMailInfors, handler);
				dialog.dismiss();
			}
		});
		dialog.getWindow().findViewById(R.id.cancel_button)
		.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				dialog.dismiss();
			}
		});
	}
	
	
	  private class MyOnClickListener implements OnClickListener{  
		  
	        private int index=0;  
	        
	        public MyOnClickListener(int i){  
	            index=i;  
	        }  
	        public void onClick(View v) { 
	        	int id = v.getId();
	        	switch (id) {
				case R.id.recive_send_layout:   
					if(isReciveMail&&!isrecycle){//快速回复
						goFastSendAialog(index);
					}
					if(isrecycle&&!isReciveMail){//快速恢复
						showFastReSumeDialog(index);
					}
					closeItem(index);
					break;
				case R.id.recive_trash_layout:  //删除
					showDeleteDialog(index);
					closeItem(index);
					break;
				default:
					break;
				}
	        }  
	          
	    }	
}
