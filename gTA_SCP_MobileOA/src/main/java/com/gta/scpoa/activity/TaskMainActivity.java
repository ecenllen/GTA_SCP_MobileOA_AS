package com.gta.scpoa.activity;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.gta.db.annotation.ViewInject;
import com.gta.scpoa.R;
import com.gta.scpoa.adapter.TaskAdapter;
import com.gta.scpoa.biz.impl.TaskBizImpl;
import com.gta.scpoa.common.Constant;
import com.gta.scpoa.entity.TaskNewInfor;
import com.gta.scpoa.util.DialogUtil;
import com.gta.scpoa.util.UIUtils;
import com.gta.scpoa.views.CommonBottomView;
import com.gta.scpoa.views.CommonTopView;
import com.gta.scpoa.views.SearchEditText;
import com.gta.scpoa.views.XListView;
import com.gta.scpoa.views.XListView.IXListViewListener;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 待办已办
 * 
 * @author xiaojie.cai
 * 
 */
public class TaskMainActivity extends BaseActivity implements OnClickListener,
		IXListViewListener, OnItemClickListener {
	/**
	 * 公共顶部布局
	 */
	@ViewInject(id = R.id.task_top_view)
	private CommonTopView taskTopview = null;
	/**
	 * 公共底部布局
	 */
	@ViewInject(id = R.id.task_bottom_view)
	private CommonBottomView taskBottomview = null;
	/**
	 * 搜索
	 */
	@ViewInject(id = R.id.task_search_edit)
	private SearchEditText filter_edit = null;
	
	/**
	 * tab1_bg
	 */
	@ViewInject(id = R.id.task_tabBg1)
	private RelativeLayout tabBg1 = null;
	/**
	 * tab2_bg
	 */
	@ViewInject(id = R.id.task_tabBg2)
	private RelativeLayout tabBg2 = null;
	/**
	 * tab1
	 */
	@ViewInject(id = R.id.task_tab1)
	private TextView tab1 = null;
	/**
	 * tab2
	 */
	@ViewInject(id = R.id.task_tab2)
	private TextView tab2 = null;
	/**
	 *数字显示 
	 */
	@ViewInject(id = R.id.task_tab1_num_text)
	private TextView num_tv;
	/**
	 * 显示的list
	 */
	@ViewInject(id = R.id.taskMainListview)
	private XListView taskMainListview = null;

	/**
	 * 暂无数据的显示
	 **/
	@ViewInject(id  = R.id.task_notdata_view)
	private TextView task_notdata_view = null;
	
	private ProgressDialog mProgressDialog = null;

	private TaskAdapter adapter;

	//private CommonTopView topView = null;
	public int type = 1; // 1待办 2已办
	private TaskBizImpl taskBizImpl = null;
	
	private List<TaskNewInfor> listInfors = new ArrayList<TaskNewInfor>();
	
	private List<TaskNewInfor> searchTempLists = new ArrayList<TaskNewInfor>();
	
	/*是否退出*/
	private boolean isback = false;
	/*是否是搜索的状态*/
	private boolean isSearch = false;
	/************************************/
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.task_main_layout);
		viewInit();
		dataInit();
	}

	/* 用于交互处理 */
	@SuppressLint("HandlerLeak")
	private Handler mUIHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			if(!isback){
				switch (msg.what) {
				case TaskBizImpl.MSG_TASK_GET_FAIL:// 失败
					String failString  = (String)msg.obj;
					if(failString.equals("command="+TaskBizImpl.firstLoad)){  //首次获取失败
						failString = "网络异常，加载数据失败!";
						listInfors.clear();
						adapter.notifyDataSetChanged();
						taskMainListview.stopRefresh();
						taskMainListview.stopLoadMore();
						isHideTheFootView();
						progressDialogDisMiss();
					}else if(failString.equals("command="+TaskBizImpl.loadMore)){  //更多获取失败
						failString = "网络异常,加载更多失败!";
						taskMainListview.stopLoadMore();
					}else if(failString.equals("command="+TaskBizImpl.onRefresh)){  //最新获取失败
						failString = "网络异常,刷新失败!";
						taskMainListview.stopRefresh();
					}else{
						progressDialogDisMiss();
					}
					UIUtils.ToastMessage(getApplicationContext(), failString);
					break;
				case TaskBizImpl.MSG_TASK_GET_SUCCESS:// list加载成功
					List<TaskNewInfor> tempList = (List<TaskNewInfor>) msg.obj;
					Log.e("m_tag", "=====listInfors======" + tempList.toString() );
					listInfors.clear();
					listInfors.addAll(tempList);
					adapter.notifyDataSetChanged();
					taskMainListview.stopRefresh();
					taskMainListview.stopLoadMore();
					progressDialogDisMiss();
					isHideTheFootView();
					break;
				case TaskBizImpl.MSG_TASK_GET_MORE: //更多加载成功
					if(!mProgressDialog.isShowing()){
						tempList = (List<TaskNewInfor>) msg.obj; 
						listInfors.addAll(tempList);
						adapter.notifyDataSetChanged();
						if(tempList.size()==0){
							taskMainListview.setPullLoadEnable(false);
						}else{
							taskMainListview.setPullLoadEnable(true);
						}
					}
					taskMainListview.stopLoadMore();
					break;
				case TaskBizImpl.MSG_TASK_GET_REFRSH: //刷新加载成功
					if(!mProgressDialog.isShowing()){
						tempList = (List<TaskNewInfor>) msg.obj;
						try {
							int num = Integer.parseInt(Constant.taskNumber);
								num = num + tempList.size();
								Constant.taskNumber = num + "";
								setNum(num);
						} catch (Exception e) {
							setNum(tempList.size());
						}
						listInfors.addAll(0, tempList);
						adapter.notifyDataSetChanged();
					}
					taskMainListview.stopRefresh();
					isHideTheFootView();
					break;
				case TaskBizImpl.MSG_TASK_SEARCH_SUCCESS: //搜索成功
					List<TaskNewInfor> tempList1 = (List<TaskNewInfor>) msg.obj;
					listInfors.clear();
					listInfors.addAll(tempList1);
					adapter.notifyDataSetChanged();
					taskMainListview.stopRefresh();
					taskMainListview.stopLoadMore();
					progressDialogDisMiss();
					isHideTheFootView();
					break;
				case TaskBizImpl.MSG_TASK_SEARCH_MORE_SUCCESS: //搜索加载更多成功
					List<TaskNewInfor> tempList3 = (List<TaskNewInfor>) msg.obj; 
					if(tempList3.size()==0){
						taskMainListview.setPullLoadEnable(false);
					}else{
						taskMainListview.setPullLoadEnable(true);
					}
					listInfors.addAll(tempList3);
					adapter.notifyDataSetChanged();
					taskMainListview.stopLoadMore();
					break;
				case TaskBizImpl.MSG_TASK_SEARCH_REFRSH_SUCCESS: //搜索刷新成功
					List<TaskNewInfor> tempList2 = (List<TaskNewInfor>) msg.obj;
					listInfors.addAll(0, tempList2);
					adapter.notifyDataSetChanged();
					taskMainListview.stopRefresh();
					isHideTheFootView();
					break;
				case Constant.MSG_TASK_AGREE: //删除ID
					String id  = (String)msg.obj;
					deleteId(id);
					break;
				default:
					break;
				}
				if(!taskMainListview.isOnLoadMore()&&!taskMainListview.isOnRefresh()){
					filter_edit.setEnabled(true);
				}
			}
		}
	};

	/* 顶部和底部的初始化 */
	private void tittleInit() {
		taskTopview.setBackImageButtonEnable(true);
		taskTopview.setBackImageButtonOnClickListener(this);
		taskTopview.setAddImageButtonEnable(true);
		taskTopview.setTitleTextViewEnable(true);
		taskTopview.setTitleTextViewText("消息");

		// 底部布局
		taskBottomview.setIsMainActivity(false);
	}

	/* 视图的初始化 */
	private void viewInit() {
		tittleInit();
		progressInit();

		tab1.setTextColor(0xFFFFA749);
		tabBg1.setOnClickListener(this);
		tabBg2.setOnClickListener(this);

		
		num_tv.setVisibility(View.GONE);
		
		taskMainListview.setPullLoadEnable(false);
		taskMainListview.setPullRefreshEnable(true);
		taskMainListview.setOnItemClickListener(this);
		taskMainListview.setXListViewListener(this);

		adapter = new TaskAdapter(this, listInfors, mUIHandler);
		taskMainListview.setAdapter(adapter);
		adapter.notifyDataSetChanged();
		
		// 根据输入框输入值的改变来过滤搜索
		filter_edit.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				/* 去掉收尾空格 */
				String searchString = filter_edit.getText().toString();
				if (searchString.equals("")) {
					if (!isSearch) {
						return;
					}
					isSearch = false;
					/*恢复数据*/
					listInfors.clear();
					listInfors.addAll(searchTempLists);
					searchTempLists.clear();
					adapter.notifyDataSetChanged();
					isHideTheFootView();
				}else{
					searchString = searchString.trim();
					if(searchString.equals("")){
						return;
					}
					if (!isSearch) {   //做下缓存
						searchTempLists.clear();
						searchTempLists.addAll(listInfors);
					}
					isSearch = true;
					setProgressShow("获取搜索数据中...");
					
					taskBizImpl.getTaskList("",type,TaskBizImpl.firstLoad,20,"",searchString);
				}
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {

			}

			@Override
			public void afterTextChanged(Editable s) {
				// UIUtils.ToastMessage(getApplicationContext(), "3");
			}
		});
		

	}

	private void dataInit() {
		setProgressShow("获取待办数据中...");
		taskBizImpl = new TaskBizImpl(this, mUIHandler);
		taskBizImpl.getTaskList("",type,TaskBizImpl.firstLoad,20,"","");
	}

	/* 显示滚动条 */
	private void progressInit() {
		mProgressDialog = new ProgressDialog(this);
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
	
	public void ChangeToTab1(){
		createTab(1, tab1,tabBg1);
	}
	
	/* 用于切换tab */
	private void createTab(int currentTag, TextView text,RelativeLayout tabbg) {
		if(taskMainListview.isOnLoadMore()||taskMainListview.isOnRefresh()) return ;
		/*判断是否是搜索的状态*/
		if(isSearch)  return;
		
		if (currentTag == type)
			return;
		else
			type = currentTag;

		tab1.setTextColor(0xFF8E8E90);
		tab2.setTextColor(0xFF8E8E90);

		tabBg1.setBackgroundResource(R.drawable.tab_default);
		tabBg2.setBackgroundResource(R.drawable.tab_default);

		text.setTextColor(0xFFFFA749);
		tabbg.setBackgroundResource(R.drawable.tab_checked);
		
		String showString = "获取已办数据中...";
		if(type == 1){
			showString = "获取待办数据中...";
		}
		setProgressShow(showString);
		
		taskBizImpl.getTaskList("",type,TaskBizImpl.firstLoad,20,"","");  //加载数据
	}

	/* 返回结束 */
	private void goBack() { // 返回按键
		this.finish();
	}

	@Override
	public void onClick(View v) {
		int id = v.getId();
		switch (id) {
		case R.id.task_tabBg1: // tab1
			createTab(1, tab1,tabBg1);
			break;
		case R.id.task_tabBg2: // tab2
			createTab(2, tab2,tabBg2);
			break;
		case R.id.topbar_back_ibtn: // 头标返回按键
			goBack();
			break;
		default:
			break;
		}
	}

	/* 下拉刷新    考虑时候有搜索*/
	@Override
	public void onRefresh() {
		filter_edit.setEnabled(false);
		
		String searchString  = "";
		if(isSearch){  //是搜索状态的情况下
			searchString = filter_edit.getText().toString().trim();
		}
		int size = listInfors.size();
		
		if(size == 0){
			taskBizImpl.getTaskList("",type,TaskBizImpl.firstLoad,20,"",searchString); 
		}else{
			taskBizImpl.getTaskList("",type, TaskBizImpl.onRefresh, 10, listInfors.get(0).getCreateTime(), searchString);
		}
		
	}

	/* 上拉加载  考虑时候有搜索*/
	@Override
	public void onLoadMore() {
		filter_edit.setEnabled(false);
		
		String searchString  = "";
		if(isSearch){  //是搜索状态的情况下
			searchString = filter_edit.getText().toString().trim();
		}
		
		int size = listInfors.size();
		if(size == 0){
			taskBizImpl.getTaskList("",type,TaskBizImpl.firstLoad,20,"",searchString); 
		}else{
			String id = "";
			if(type == 1){
				id = listInfors.get(size-1).getId();
			}else{
				id = listInfors.get(size-1).getRunId();
			}
			taskBizImpl.getTaskList(id,type,TaskBizImpl.loadMore, 10, listInfors.get(size-1).getCreateTime(), searchString);
		}
	}

	
	private void goTaskDetailActivity(int position){
		Intent intent = new Intent();
		intent.setClass(this, TaskDetailActivity.class);
		Bundle bundle = new Bundle();
		bundle.putSerializable("taskNewInfor",
				(Serializable) listInfors.get(position));
		intent.putExtras(bundle);
	    startActivityForResult(intent, 1);
	}
	
	/*是否需要隐藏底部*/
	private void isHideTheFootView(){
		if(listInfors.size()<20){
			taskMainListview.setPullLoadEnable(false);
		}else{
			taskMainListview.setPullLoadEnable(true);
		}
		
		if(listInfors.isEmpty()){
			task_notdata_view.setVisibility(View.VISIBLE);
		}else{
			task_notdata_view.setVisibility(View.GONE);
		}
	}
	
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if(data == null) return;
		switch (requestCode) {
		case 1:
			String id = data.getStringExtra("ID");
			String message = data.getStringExtra("message");
			if(message.contains("已经被处理") || "审批成功".equals(message) || "驳回成功".equals(message)){
				deleteId(id);
			}
			break;
		default:
			break;
		}
	}
	
	/* 点击item */
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		position = position - 1;
		adapter.closeItem(position);
		goTaskDetailActivity(position);
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		isback = true;
	}
	
	
	
	private void setNum(int num){
		TextView num_text = (TextView) findViewById(R.id.common_bottom_num_tv);
		if(num>0){
			num_text.setVisibility(View.VISIBLE);
			num_text.setText(""+num);
		}else{
			num_text.setVisibility(View.GONE);
		}
	}
	
	
	/**
	 * 删除ID对应的item
	 * @param id
	 */
	public void deleteId(String id){
		for(TaskNewInfor taskNewInfor : listInfors){
			if(taskNewInfor.getId().equals(id)){
				listInfors.remove(taskNewInfor);
				try {
					int num = Integer.parseInt(Constant.taskNumber);
					if(num > 0){
						num = num -1;
						Constant.taskNumber = num + "";
						setNum(num);
					}
				} catch (Exception e) {
					setNum(0);
				}
				break;
			}
		}
		for(TaskNewInfor taskNewInfor:searchTempLists){
			if(taskNewInfor.getId().equals(id)){
				searchTempLists.remove(taskNewInfor);
				break;
			}
		}
		adapter.notifyDataSetChanged();
	}
}
