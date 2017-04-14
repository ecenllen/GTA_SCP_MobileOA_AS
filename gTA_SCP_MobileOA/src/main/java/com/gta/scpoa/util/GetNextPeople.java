package com.gta.scpoa.util;



import com.gta.scpoa.R;
import com.gta.scpoa.activity.TaskDetailActivity;
import com.gta.scpoa.activity.TaskMainActivity;
import com.gta.scpoa.application.GTAApplication;
import com.gta.scpoa.biz.IGetDataBiz;
import com.gta.scpoa.biz.impl.GetDataBizImpl;
import com.gta.scpoa.common.Constant;
import com.gta.scpoa.entity.TaskNewInfor;
import com.gta.scpoa.entity.TaskPeople;
import com.gta.scpoa.views.TaskView;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

public class GetNextPeople {
	private Handler mHandler;  //主界面传来的handler
	private String adviceString; //意见
	private ProgressDialog mProgressDialog = null;  //滚动框
	private Context context;
	private TaskNewInfor taskNewInfor;
	private boolean flag;
	private AlertDialog dialog;
	
	private static final int MSG_TABLE_GET_FAIL = -1;
	private static final int MSG_TABLE_GET_SUCCESS = 1;
	private  TaskView view;
	public GetNextPeople(Context context,Handler mHandler) {
		this.context = context;
		this.mHandler = mHandler;
		progressInit();
	}
	
	public void getPeople(TaskNewInfor taskNewInfor,String adviceString,boolean flag){
		this.taskNewInfor = taskNewInfor;
		this.flag = flag;
		if(adviceString==null||adviceString.equals("")){
			adviceString = null;
		}
		this.adviceString = adviceString;
		setProgressShow("获取联系人中...");
		String acountName =GTAApplication.instance.getProperty(Constant.ACCOUNT_NAME);
		IGetDataBiz biz = new GetDataBizImpl();	
		biz.getPeopleList(acountName, taskNewInfor.getId(),flag,handler);
	}
	
	/* 显示滚动条 */
	private void progressInit() {
		mProgressDialog = new ProgressDialog(context);
	}

	/* 设置滚动框信息 */
	public void setProgressShow(String str) {
		DialogUtil.showDialog(mProgressDialog, str, false);
	}

	private void progressDialogDisMiss(){
		if(mProgressDialog!=null){
			mProgressDialog.dismiss();
		}
	}
	
	
	private void showBackDialog(final String message){
		mProgressDialog.dismiss();
		final AlertDialog dialog = new AlertDialog.Builder(context).create();
		dialog.show();
		dialog.setCancelable(false);
		dialog.setCanceledOnTouchOutside(false);
		dialog.getWindow().setContentView(R.layout.task_be_dealed_dialog);
		TextView tView = (TextView) dialog.getWindow().findViewById(R.id.error_message);
		tView.setText(message);
		dialog.getWindow().findViewById(R.id.back_btn)
		.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Activity activity = (Activity) context;
				dialog.dismiss();
				if(activity instanceof TaskMainActivity){  //待办主页
					if(message.contains("已经被处理")){  // 已被处理的任务，进行删除
						((TaskMainActivity) activity).deleteId(taskNewInfor.getId());
					}
					return ;
				}
				
				if(activity instanceof TaskDetailActivity){  //待办详情
					if(message.contains("已经被处理")){  // 已被处理的任务，进行删除
						((TaskDetailActivity) activity).goBackForResult(taskNewInfor.getId(), 1, message);
					}
					return ;
				}
			}
		});
		
	}
	
	
	/* 用于交互处理 */
	@SuppressLint("HandlerLeak")
	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
				String str = "";
				switch (msg.what) {
				case 2:// 同意成功
					str = (String)msg.obj;
					UIUtils.ToastMessage(context, str);
					Message msg1 = mHandler.obtainMessage(Constant.MSG_TASK_AGREE); // 成功返回主界面
					msg1.obj = taskNewInfor.getId();
					msg1.sendToTarget();
					progressDialogDisMiss();
					break;
				case -2:// 同意失败
					str = (String)msg.obj;
					UIUtils.ToastMessage(context, str);
					progressDialogDisMiss();
					break;
				case MSG_TABLE_GET_FAIL:// 失败
					progressDialogDisMiss();
					str = (String)msg.obj;
					if(str.contains("已经被处理")){
						showBackDialog("此任务已经被处理!");
					}else if(str.contains("此任务不允许在移动端审批")){
						showBackDialog("此任务需在PC端进行审批!");
					}else{
						UIUtils.ToastMessage(context, "操作失败 !");
					}
					break;
				case MSG_TABLE_GET_SUCCESS:// 获取列表成功
					/*mHandler,adviceString,context*/
					TaskPeople taskPeople = (TaskPeople)msg.obj;
					setProgressShow("正在审批中...");
					if(taskPeople.getData()==null||taskPeople.getData().size()==0){
						IGetDataBiz biz = new GetDataBizImpl();	
						String acountName =GTAApplication.instance.getProperty(Constant.ACCOUNT_NAME);
						biz.TaskSubmit(taskNewInfor.getId(), acountName, 1, adviceString, null, null,"0",flag, handler);
					}else{
						view = new TaskView(context, mHandler, taskNewInfor.getId(),1,adviceString,flag);
						view.setData(taskPeople);
						view.setConfirmBtnOnClickListener(new OnClickListener() {
							@Override
							public void onClick(View v) {
								view.submit();
							}
						});
						view.setCancleBtnOnClickListener(new OnClickListener() {

							@Override
							public void onClick(View v) {
								view.dismiss(false);
							}
						});

						if (null == dialog) {
							dialog = UIUtils.createAlertDailog(context);
						}
						view.setAlertDialog(dialog);
						UIUtils.showAlertDialog(dialog, view);
						progressDialogDisMiss();
					}
					break;
				default:
					break;
				}
			}
	};
}
