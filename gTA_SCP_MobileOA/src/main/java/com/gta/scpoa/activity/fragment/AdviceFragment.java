package com.gta.scpoa.activity.fragment;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.gta.scpoa.R;
import com.gta.scpoa.activity.TaskDetailActivity;
import com.gta.scpoa.adapter.TabAdapter;
import com.gta.scpoa.application.GTAApplication;
import com.gta.scpoa.biz.IGetDataBiz;
import com.gta.scpoa.biz.impl.AdviceBizImpl;
import com.gta.scpoa.biz.impl.GetDataBizImpl;
import com.gta.scpoa.common.Constant;
import com.gta.scpoa.entity.TableInfor;
import com.gta.scpoa.entity.TaskNewInfor;
import com.gta.scpoa.util.DialogUtil;
import com.gta.scpoa.util.GetNextPeople;
import com.gta.scpoa.util.UIUtils;
import com.gta.scpoa.views.myListView;

import java.util.ArrayList;
import java.util.List;

/**
 * 意见 @author xiaojie.cai
 * 
 */
@SuppressLint("ValidFragment")
public class AdviceFragment extends BaseFragmnet implements OnClickListener{

	private Context mContext;
	private View contentView;
	private TaskNewInfor taskNewInfor;
	private LinearLayout loadLayout = null;
	private RelativeLayout advice_main_layout = null;
	private AdviceBizImpl adviceBizImpl = null;
	private Button load_btn = null; // 刷新数据
	private ProgressDialog mProgressDialog = null;
	private myListView listView; // 表单数据
	private TabAdapter tabAdapter;
	private List<TableInfor> lists = new ArrayList<TableInfor>();
	private TextView tittleText = null;

	private EditText content_et; // 审核意见
	private Button agreeBtn; // 同意
	private Button disagreeBtn; // 驳回
	private TextView adviceTv;
	
//	public AdviceFragment(Context context, TaskNewInfor taskNewInfor) {
//		mContext = context;
//		this.taskNewInfor = taskNewInfor;
//		adviceBizImpl = new AdviceBizImpl(mContext, mUIHandler);
//	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mContext = getActivity();
		this.taskNewInfor = (TaskNewInfor)getArguments().getSerializable("infor");
		adviceBizImpl = new AdviceBizImpl(mContext, mUIHandler);
		
		progressInit();
		tabAdapter = new TabAdapter(mContext, lists);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		if (contentView != null) {
			ViewGroup viewGroup = (ViewGroup) contentView.getParent();
			if (viewGroup != null) {
				viewGroup.removeView(contentView);
			}
		} else {
			contentView = inflater.inflate(R.layout.task_advice_fragment, null);
			viewInit();  //初始化视图
			dataInit();  //请求网络  获取表单数据
		}
		return contentView;
	}

	/*对视图进行初始化*/
	private void viewInit() {
		loadLayout = (LinearLayout) contentView.findViewById(R.id.loadLayout);
		advice_main_layout = (RelativeLayout) contentView
				.findViewById(R.id.advice_main_layout);

		/*这个listview用来存放表单的数据*/
		listView = (myListView) contentView.findViewById(R.id.table_listview);
		listView.setAdapter(tabAdapter);
		tabAdapter.notifyDataSetChanged();

		load_btn = (Button) contentView.findViewById(R.id.load_btn);
		load_btn.setOnClickListener(this);

		tittleText = (TextView) contentView.findViewById(R.id.advice_tittle);
		tittleText.setText(taskNewInfor.getSubject());

		adviceTv = (TextView) contentView.findViewById(R.id.adviceTv);
		content_et = (EditText) contentView
				.findViewById(R.id.task_detail_content_et);
		LinearLayout contentBgEv = (LinearLayout) contentView.findViewById(R.id.contentBgEv);
		agreeBtn = (Button) contentView.findViewById(R.id.agree_btn);
		disagreeBtn = (Button) contentView.findViewById(R.id.disagree_btn);
		agreeBtn.setOnClickListener(this);
		disagreeBtn.setOnClickListener(this);

		/*已办的时候  UI的不同之处显示*/
		if (taskNewInfor.getType() == 2) { // 已办
			contentBgEv.setVisibility(View.GONE);
			content_et.setEnabled(false);
			content_et.setVisibility(View.GONE);
			adviceTv.setVisibility(View.GONE);
			agreeBtn.setVisibility(View.GONE);
			disagreeBtn.setVisibility(View.GONE);
		}
	}

	/* 显示滚动条 */
	private void progressInit() {
		mProgressDialog = new ProgressDialog(mContext);
	}

	/* 设置滚动框信息 */
	public void setProgressShow(String str) {
		DialogUtil.showDialog(mProgressDialog, str, false);
	}

	/*获取表单数据*/
	private void dataInit() {
		setProgressShow("获取表格信息中...");
		adviceBizImpl.getTableData(taskNewInfor);
	}

	
	private void showBackDialog(final String message){
		mProgressDialog.dismiss();
		final AlertDialog dialog = new AlertDialog.Builder(mContext).create();
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
				TaskDetailActivity activity = (TaskDetailActivity) mContext;
				activity.goBackForResult(taskNewInfor.getId(), 1, message);
				dialog.dismiss();
			}
		});
		
	}
	
	
	
	/* 用于交互处理 */
	@SuppressLint("HandlerLeak")
	private Handler mUIHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			String str = "";
			switch (msg.what) {
			case AdviceBizImpl.MSG_TABLE_GET_FAIL:// 失败
				str = (String) msg.obj;
				if(str.contains("已经被处理")){
					loadLayout.setVisibility(View.GONE);
					advice_main_layout.setVisibility(View.GONE);
					showBackDialog("此任务已经被处理!");
				}
//				else if(str.contains("此任务不允许在移动端审批")){
//					loadLayout.setVisibility(View.GONE);
//					advice_main_layout.setVisibility(View.GONE);
//					showBackDialog("此任务需在PC端进行审批!");
//				}
				else{
					UIUtils.ToastMessage(mContext, "获取数据失败!");
					loadLayout.setVisibility(View.VISIBLE);
					advice_main_layout.setVisibility(View.GONE);
				}
				break;
			case AdviceBizImpl.MSG_TABLE_GET_SUCCESS:// 获取列表成功
				loadLayout.setVisibility(View.GONE);
				advice_main_layout.setVisibility(View.VISIBLE);
				List<TableInfor> tempList = (List<TableInfor>) msg.obj;
				lists.clear();
				tabAdapter.setData(tempList);
				tabAdapter.notifyDataSetChanged();
				break;
			case Constant.MSG_TASK_AGREE: // 同意成功返回
//				UIUtils.ToastMessage(mContext, "审批成功!");
				TaskDetailActivity activity = (TaskDetailActivity) mContext;
				activity.goBackForResult(taskNewInfor.getId(), 1, "审批成功");
				break;
			case AdviceBizImpl.MSG_TABLE_BACK_SUCCESS:  //驳回成功返回
				str = (String) msg.obj;
				UIUtils.ToastMessage(mContext, str);
				TaskDetailActivity activity1 = (TaskDetailActivity) mContext;
				activity1.goBackForResult(taskNewInfor.getId(), 1, "驳回成功");
				break;
			case AdviceBizImpl.MSG_TABLE_BACK_FAIL:  //获取表单数据失败
				str = (String) msg.obj;
				UIUtils.ToastMessage(mContext, str);
				break;
			case -1:  // 驳回失败
				str = (String) msg.obj;
				if("3".equals(str)){
					UIUtils.ToastMessage(mContext, "此节点不允许驳回,驳回失败!");
				}else
					UIUtils.ToastMessage(mContext, str);
				break;
			default:
				break;
			}
			if (msg.what != 10) {
				mProgressDialog.dismiss();
			}
		}
	};

	/*点击同意按键的时候*/
	private void goAgreeButton() {
//		TableInfor tableInfor = lists.get(0);
//		UIUtils.ToastMessage(mContext, tableInfor.getKey()+":"+tableInfor.getValue());
		String adviceString = content_et.getText().toString();
		if(adviceString.trim().equals("")){
			adviceString = "同意";
			content_et.setText(adviceString);
		}
		new GetNextPeople(mContext, mUIHandler).getPeople(taskNewInfor,
				adviceString, false);
	}

	/*点击驳回按键的时候*/
	private void goAdviceBack() {
		setProgressShow("正在驳回操作");
		String adviceString = content_et.getText().toString();
		if(adviceString.trim().equals("")){
			adviceString = "不同意";
			content_et.setText(adviceString);
		}
		String id = taskNewInfor.getId();
		// 如果是已办页面
		IGetDataBiz biz = new GetDataBizImpl();
		String acountName =GTAApplication.instance.getProperty(Constant.ACCOUNT_NAME);
		biz.TaskSubmit(id, acountName, 3, adviceString, null,null,"1",false, mUIHandler);
	}

	@Override
	public void onClick(View v) {
		int id = v.getId();
		switch (id) {
		case R.id.load_btn: // 重新加载
			dataInit();
			break;
		case R.id.agree_btn: // 同意
			goAgreeButton();
			break;
		case R.id.disagree_btn: // 驳回
			goAdviceBack();
			break;
		default:
			break;
		}
	}
	@Override
	public void onDestroyView() {
		super.onDestroyView();
		if(mProgressDialog!=null){
			if(mProgressDialog.isShowing()){
				mProgressDialog.dismiss();
			}
		}
	}
}
