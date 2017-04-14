package com.gta.scpoa.views;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.gta.scpoa.R;
import com.gta.scpoa.adapter.TaskDialogAdapter;
import com.gta.scpoa.application.GTAApplication;
import com.gta.scpoa.biz.IGetDataBiz;
import com.gta.scpoa.biz.impl.GetDataBizImpl;
import com.gta.scpoa.common.Constant;
import com.gta.scpoa.entity.TaskPeople;
import com.gta.scpoa.util.UIUtils;

public class TaskView extends BaseView {
	/**
	 * 是否为转交 true:转交 false:提交
	 */
	private boolean flag ;
	private ListView lv;
	private TaskDialogAdapter adapter;
	private TaskHandler mHandler;
	private String taskId;
	private String acountName;
	private String content;
	
	/**
	 * 0=弃权、1=同意、2=反对、3=驳回、4=追回、5=会签通过、6=会签不通
	 */
	private int mType;
	private Handler mainHandler;
	private AlertDialog d;
	private Button mConfirmBt;
	private Button mCancleBt;
	
	
	
	/**
	 * 
	 * @param context
	 * @param attrs
	 * @param mainHandler 关联待办已办页面的Activity中的Handler
	 * @param taskId 任务的ID
	 * @param type 0=弃权、1=同意、2=反对、3=驳回、4=追回、5=会签通过、6=会签不通
	 * @param content 审批意见
	 * @param flag 是否为转交(true:转交 /false:提交)
	 */
	public TaskView(Context context, AttributeSet attrs, Handler mainHandler,String taskId,int type,String content,boolean flag) {
		super(context, attrs);		
		this.flag = flag;
		this.taskId = taskId;
		this.mType = type;
		this.mainHandler = mainHandler;
		setupViews();
	}
	/**
	 * 
	 * @param context
	 * @param mainHandler 关联待办已办页面的Activity中的Handler
	 * @param taskId 任务的ID
	 * @param type 0=弃权、1=同意、2=反对、3=驳回、4=追回、5=会签通过、6=会签不通
	 * @param content 审批意见
	 * @param flag 是否为转交(true:转交 /false:提交)
	 */
	public TaskView(Context context,Handler mainHandler,String taskId,int type, String content,boolean flag) {
		super(context);	
		this.flag = flag;
		this.taskId = taskId;
		this.mType = type;
		this.mainHandler = mainHandler;
		this.content = content;
		setupViews();
	}

	public void setAlertDialog(AlertDialog dialog){
		this.d = dialog;
	}

	private void setupViews() {
		acountName = GTAApplication.instance.getProperty(Constant.ACCOUNT_NAME);
		
		setContentView(R.layout.view_task_dialog);
		
		TextView mTitleTV = (TextView) findViewById(R.id.task_dialog_title_tv);
		if (flag) {
			mTitleTV.setText("请选择需要转交的人员");
		} else {
			mTitleTV.setText("请选择需要提交的人员");
		}
		lv = (ListView) findViewById(R.id.task_dialog_lv);
		mConfirmBt = (Button) findViewById(R.id.task_dialog_confirm_bt);
		mCancleBt = (Button) findViewById(R.id.task_dialog_cancle_bt);

		adapter = new TaskDialogAdapter(mInflater);


	}
	public void setData(TaskPeople taskPeople){
		if (null == mHandler) {
			mHandler = new TaskHandler(this);
		}	
		adapter.setData(taskPeople);
		lv.setAdapter(adapter);
	}


	public void setConfirmBtnOnClickListener(OnClickListener listener){
		if (null != mConfirmBt) {
			mConfirmBt.setOnClickListener(listener);
		}
	}
	public void setCancleBtnOnClickListener(OnClickListener listener){
		if (null != mCancleBt) {
			mCancleBt.setOnClickListener(listener);
		}
	}

	
	public void dismiss(boolean result) {
		if (result) {
			Message msg = mainHandler.obtainMessage(Constant.MSG_TASK_AGREE);			
			msg.obj = taskId;		
			mainHandler.sendMessage(msg);	
		}
			
		if (null != d) {
			d.dismiss();
		}
	}
	public void submit() {
		
		ArrayList<TaskPeople> lists = adapter.getSelectedPeoples();

		if (null != lists) {

			if (lists.isEmpty()) {
				UIUtils.ToastMessage(getContext(), flag ? "请选择转交人员 !"
						: "请选择提交人员 !");
				Message msg = mainHandler.obtainMessage(-2);			
				msg.obj = "请选择转交人员";		
				mainHandler.sendMessage(msg);	
				return;
			}
			
			boolean roleValidity = true;
			for (TaskPeople taskPeople : lists) {
				
				if (! taskPeople.getType().equals("user")) {
					roleValidity = false;
				}
			}
			if (roleValidity == false) {
				UIUtils.ToastMessage(getContext(), "请不要选择\"组织\"类型的角色 !");
				return;
			}
			
								
			IGetDataBiz biz = new GetDataBizImpl();		
			
			biz.TaskSubmit(taskId, acountName, mType, content, lists,adapter.getNextNodeId(),"0", flag, mHandler);
		}

	}

	public static class TaskHandler extends Handler {

		WeakReference<TaskView> wr;

		TaskHandler(TaskView v) {
			wr = new WeakReference<TaskView>(v);
		}

		@Override
		public void handleMessage(Message msg) {
			
			TaskView taskView = (TaskView) wr.get();

			switch (msg.what) {
			case 1:
//				taskView.removeProgressBar();
//				ArrayList<TaskPeople> mList = (ArrayList<TaskPeople>) msg.obj;
//				taskView.adapter.setData(mList);
//				taskView.lv.setAdapter(taskView.adapter);
				break;	
				
			case -1:// 返回并提示错误信息 如 网络异常，获取转交/提交人员列表失败 ！
//				taskView.removeProgressBar();
//				ArrayList<TaskPeople> list = new ArrayList<TaskPeople>();
//				TaskPeople p= new TaskPeople();
//				p.setExecutor(msg.obj.toString());
//				p.setTickHidden(true);
//				list.add(p);
//				taskView.adapter.setData(list);
//				taskView.lv.setAdapter(taskView.adapter);	
				break;	
			case 10://显示对话框
				taskView.addProgressBar(taskView.mContext);		
				break;
			case 2:
				taskView.removeProgressBar();
				UIUtils.ToastMessage(taskView.getContext().getApplicationContext(), msg.obj.toString());
				sendEmptyMessageDelayed(5, 500);
				break;
			case -2:
				taskView.removeProgressBar();
				UIUtils.ToastMessage(taskView.getContext().getApplicationContext(), msg.obj.toString());
				sendEmptyMessageDelayed(-5, 500);
				break;
			case 5:
				taskView.dismiss(true);			
				break;
			case -5:
				taskView.dismiss(false);
				break;
			default:								
				break;
			}

		}
	}


	
}
