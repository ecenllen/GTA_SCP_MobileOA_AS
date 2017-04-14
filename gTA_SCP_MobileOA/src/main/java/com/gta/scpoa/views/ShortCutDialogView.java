package com.gta.scpoa.views;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.View;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.gta.scpoa.R;
import com.gta.scpoa.activity.AddOrUpdateContactActivity;
import com.gta.scpoa.activity.NewScheduleActivity;
import com.gta.scpoa.activity.WriteMailActivity;
import com.gta.scpoa.common.Constant;
import com.gta.scpoa.util.UIUtils;

/**
 * 首页中的快捷界面
 * 
 * @author bin.wang1
 * 
 */
public class ShortCutDialogView extends BaseView {
	private PopupWindow mPopupWindow;

	private TextView mAddEmailTV;
	private TextView mAddContactTV;
	private TextView mAddScheduleTV;
	private Context  context = null;

	public ShortCutDialogView(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.context = context;
		setupViews();
	}

	public ShortCutDialogView(Context context) {
		super(context);
		this.context = context;
		setupViews();
	}

	private void setupViews() {
		setContentView(R.layout.view_main_popupwindow);
		mAddEmailTV = (TextView) findViewById(R.id.add_email_tv);
		mAddContactTV = (TextView) findViewById(R.id.add_contacts_tv);
		mAddScheduleTV = (TextView) findViewById(R.id.add_schedule_tv);
	}

	/**
	 * 取消PopupWindow
	 * 
	 * @param popupWindow
	 */
	public void setOnCancelClicked(PopupWindow popupWindow) {
		mPopupWindow = popupWindow;
		mAddEmailTV.setOnClickListener(this);
		mAddContactTV.setOnClickListener(this);
		mAddScheduleTV.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		super.onClick(v);

		if (null != mPopupWindow) {
			mPopupWindow.dismiss();
		}
		switch (v.getId()) {
		case R.id.add_email_tv:
			// 具体业务
			Intent intent1 = new Intent();
			intent1.setClass(context, WriteMailActivity.class);
			Bundle bundle1 = new Bundle();
			bundle1.putInt(Constant.SendStatue, Constant.Send_NewMail);   //0为新建邮件    1为快速恢复   2为发件箱查看   3为草稿箱查看
			intent1.putExtras(bundle1);
			context.startActivity(intent1);
			break;
		case R.id.add_contacts_tv:
			//UIUtils.ToastMessage(mContext, "添加联系人", 400);
			Intent intent = new Intent(context,AddOrUpdateContactActivity.class); 
			Bundle bundle = new Bundle();
			bundle.putSerializable("contactInfo", null);
			intent.putExtras(bundle);
			context.startActivity(intent);
			break;

		case R.id.add_schedule_tv:
//			UIUtils.ToastMessage(mContext, "添加日程", 400);
			Intent intent2=new Intent(context,NewScheduleActivity.class);
			context.startActivity(intent2);
			break;
		default:
			break;
		}
	}

}
