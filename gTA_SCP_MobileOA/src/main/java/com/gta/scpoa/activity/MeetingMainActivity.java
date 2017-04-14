package com.gta.scpoa.activity;

import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.gta.db.annotation.ViewInject;
import com.gta.scpoa.R;
import com.gta.scpoa.adapter.MeetingAdapter;
import com.gta.scpoa.biz.TaskBiz;
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

/**
 * 会议
 * 
 * @author xiaojie.cai
 * 
 */
public class MeetingMainActivity extends BaseActivity implements
		OnClickListener, IXListViewListener, OnItemClickListener {

	/**
	 * 公共顶部布局
	 */
	@ViewInject(id = R.id.meet_top_view)
	private CommonTopView meetTopView = null;

	/**
	 * 公共底部布局
	 */
	@ViewInject(id = R.id.meet_bottom_view)
	private CommonBottomView meetBottomView = null;

	/**
	 * 搜索
	 */
	@ViewInject(id = R.id.meet_search_edit)
	private SearchEditText filter_edit = null;

	/**
	 * 会议通知
	 */
	@ViewInject(id = R.id.meet_tab1)
	private TextView tab1 = null;

	/**
	 * 会议纪要
	 */
	@ViewInject(id = R.id.meet_tab2)
	private TextView tab2 = null;

	/**
	 * 会议通知背景RelativeLayout
	 */
	@ViewInject(id = R.id.meet_tabBg1)
	private RelativeLayout tabBg1 = null;

	/**
	 * 会议纪要背景RelativeLayout
	 */
	@ViewInject(id = R.id.meet_tabBg2)
	private RelativeLayout tabBg2 = null;

	/**
	 * 会议通知数
	 */
	@ViewInject(id = R.id.meet_tab1_num_text)
	private TextView tab1_num_text = null;

	/**
	 * 会议纪要数
	 */
	@ViewInject(id = R.id.meet_tab2_num_text)
	private TextView tab2_num_text = null;

	/**
	 * 数据列表
	 */
	@ViewInject(id = R.id.meetMainListview)
	private XListView meetMainListview = null;

	/**
	 * 数据listView适配器
	 */
	private MeetingAdapter meetingAdapter = null;

	/**
	 * 数据类型(-1会议通知 5会议纪要)
	 */
	public int type = -1;

	/**
	 * 数据集合
	 */
	private List<TaskNewInfor> listInfors = new ArrayList<TaskNewInfor>();

	/**
	 * 搜索数据集合
	 */
	private List<TaskNewInfor> searchTempLists = new ArrayList<TaskNewInfor>();

	private List<TaskNewInfor> list = null;

	/**
	 * 进度条dialog
	 */
	private ProgressDialog progressDialog = null;

	/**
	 * 是否是搜索的状态
	 */
	private boolean isSearch = false;

	/**
	 * 业务处理
	 */
	private TaskBiz taskBiz = null;

	/**
	 * 无数据提醒
	 */
	@ViewInject(id = R.id.notdata_view)
	private TextView notDataView = null;

	/**
	 * Activity请求码
	 */
	public static final int REQUSET_CODE = 1;

	/**
	 * 会议对象
	 */
	private TaskNewInfor meeting = null;

	
	private boolean isMore = false;
	/**
	 * 会议通知未读数
	 */
	//private int count = 0;

	@SuppressLint("HandlerLeak")
	private Handler meetingHandler = new Handler() {

		@SuppressWarnings("unchecked")
		@Override
		public void handleMessage(Message msg) {

			// 有数据返回时，清除进度条dialog
			if (progressDialog != null) {
				progressDialog.dismiss();
			}

			switch (msg.what) {
			case TaskBiz.MSG_TASK_GET_SUCCESS:// 第一次取得数据
				list = (List<TaskNewInfor>) msg.obj;
				if (null != list) {
					if (list.isEmpty()) {
						notDataView.setVisibility(View.VISIBLE);
					} else {
						//getNumber(list);
						notDataView.setVisibility(View.GONE);
					}
					meetMainListview.stopRefresh();
					meetingAdapter.setData(list);
					meetMainListview.setAdapter(meetingAdapter);
					listInfors.clear();
					listInfors.addAll(list);
				}
				isHideTheFootView();
				break;
			case TaskBizImpl.MSG_TASK_GET_FAIL:// 失败
				String failString = (String) msg.obj;
				if (failString.equals("command=" + TaskBizImpl.firstLoad)) { // 首次获取失败
					failString = "网络异常,获取失败!";
					listInfors.clear();
					settingNumber();
					if (listInfors.isEmpty())
						notDataView.setVisibility(View.VISIBLE);
					else
						notDataView.setVisibility(View.GONE);
					list = listInfors;
					meetingAdapter.setData(listInfors);
					meetingAdapter.notifyDataSetChanged();
					meetMainListview.stopRefresh();
					meetMainListview.stopLoadMore();
				} else if (failString.equals("command=" + TaskBizImpl.loadMore)) { // 更多获取失败
					failString = "网络异常,加载更多失败!";
					meetMainListview.stopLoadMore();
				} else if (failString
						.equals("command=" + TaskBizImpl.onRefresh)) { // 最新获取失败
					failString = "网络异常,刷新失败!";
					meetMainListview.stopRefresh();
				}
				UIUtils.ToastMessage(getApplicationContext(), failString);
				isHideTheFootView();
				break;
			case TaskBizImpl.MSG_TASK_SEARCH_SUCCESS: // 搜索成功
				list = (List<TaskNewInfor>) msg.obj;
				if (list.isEmpty()) {
					settingNumber();
					notDataView.setVisibility(View.VISIBLE); // 显示无数据标识
				} else {
					//getNumber(list);
					notDataView.setVisibility(View.GONE); // 隐藏无数据标识
				}

				listInfors.clear();
				listInfors.addAll(list);
				meetingAdapter.setData(listInfors);
				meetingAdapter.notifyDataSetChanged();
				meetMainListview.stopRefresh();
				meetMainListview.stopLoadMore();
				isHideTheFootView();
				break;
			case TaskBizImpl.MSG_TASK_GET_MORE: // 更多加载成功
				if (!progressDialog.isShowing()) {
					list = (List<TaskNewInfor>) msg.obj;
					listInfors.addAll(list);
					meetingAdapter.setData(listInfors);
					meetingAdapter.notifyDataSetChanged();
				}
				meetMainListview.stopLoadMore();
				isHideTheFootView();
				break;
			case TaskBizImpl.MSG_TASK_GET_REFRSH: // 刷新加载成功
				if (!progressDialog.isShowing()) {
					list = (List<TaskNewInfor>) msg.obj;
					if (list.size() > 0) {
						notDataView.setVisibility(View.GONE);
					}
					listInfors.addAll(0, list);
					meetingAdapter.setData(listInfors);
					meetingAdapter.notifyDataSetChanged();
				}
				meetMainListview.stopRefresh();
				break;
			case TaskBizImpl.MSG_TASK_SEARCH_MORE_SUCCESS: // 搜索加载更多成功
				list = (List<TaskNewInfor>) msg.obj;
				listInfors.addAll(list);
				meetingAdapter.setData(listInfors);
				meetingAdapter.notifyDataSetChanged();
				meetMainListview.stopLoadMore();
				isHideTheFootView();
				break;
			case TaskBizImpl.MSG_TASK_SEARCH_REFRSH_SUCCESS: // 搜索刷新成功
				List<TaskNewInfor> tempList2 = (List<TaskNewInfor>) msg.obj;
				listInfors.addAll(0, tempList2);
				meetingAdapter.setData(listInfors);
				meetingAdapter.notifyDataSetChanged();
				meetMainListview.stopRefresh();
				break;
			}
			

			// 非下拉刷新或加载更多时，搜索框可输入
			if (!meetMainListview.isOnLoadMore()
					&& !meetMainListview.isOnRefresh()) {
				filter_edit.setEnabled(true);
			}
		}

		// 设置隐藏未读数
		private void settingNumber() {
			if (type == 5) { // 会议纪要
				tab2_num_text.setVisibility(View.GONE);
			} else {
				tab1_num_text.setVisibility(View.GONE);
			}
		}

	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.meet_main_layout);

		if (null == taskBiz) {
			taskBiz = new TaskBizImpl(getApplicationContext(), meetingHandler);
		}

		if (null == progressDialog) {
			progressDialog = new ProgressDialog(this);
		}

		viewInit();
		setProgressShow("获取数据中...");
		taskBiz.getMeetingList("", 20, 0, false, TaskBiz.firstLoad);
	}

	private void tittleInit() {

		// 头部布局
		meetTopView.setBackImageButtonEnable(true);
		meetTopView.setBackImageButtonOnClickListener(this);
		meetTopView.setAddImageButtonEnable(true);
		meetTopView.setTitleTextViewEnable(true);
		meetTopView.setTitleTextViewText("会议");

		// 底部布局
		meetBottomView.setIsMainActivity(false);
	}

	private void viewInit() {
		tittleInit();

		// 设置未读数
		if (Constant.MeetNumber > 0) {
			tab1_num_text.setVisibility(View.VISIBLE);
			tab1_num_text.setText(String.valueOf(Constant.MeetNumber));
		} else {
			tab1_num_text.setVisibility(View.GONE);
		}
		if (Constant.Record > 0) {
			tab2_num_text.setVisibility(View.VISIBLE);
			tab2_num_text.setText(String.valueOf(Constant.Record));
		} else {
			tab2_num_text.setVisibility(View.GONE);
		}
		
		
		tab1.setTextColor(0xFFFFA749);
		tabBg1.setOnClickListener(this);
		tabBg2.setOnClickListener(this);

		meetMainListview.setPullLoadEnable(false);
		meetMainListview.setPullRefreshEnable(true);
		meetMainListview.setOnItemClickListener(this);
		meetMainListview.setXListViewListener(this);
		meetingAdapter = new MeetingAdapter(this);
		meetMainListview.setAdapter(meetingAdapter);
		meetingAdapter.notifyDataSetChanged();

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
					// 恢复数据
					listInfors.clear();
					listInfors.addAll(searchTempLists);

					if (listInfors.isEmpty()) {

						// 隐藏未读数
						if (type == 5) { // 会议纪要
							tab2_num_text.setVisibility(View.GONE);
						} else {
							tab1_num_text.setVisibility(View.GONE);
						}

						notDataView.setVisibility(View.VISIBLE);
					} else {
						//getNumber(listInfors);
						notDataView.setVisibility(View.GONE);
					}

					searchTempLists.clear();
					meetingAdapter.notifyDataSetChanged();
					isHideTheFootView();
				} else {
					searchString = searchString.trim();
					if (searchString.equals("")) {
						return;
					}
					if (!isSearch) { // 做下缓存
						searchTempLists.clear();
						searchTempLists.addAll(listInfors);
					}
					isSearch = true;
					setProgressShow("获取搜索数据中...");
					if (type == -1) {
						taskBiz.getMeetingList(searchString, 20, 0, false,
								TaskBiz.firstLoad);
					} else {
						taskBiz.getTaskList("", type, TaskBizImpl.firstLoad,
								20, "", searchString);
					}
				}
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {

			}

			@Override
			public void afterTextChanged(Editable s) {
			}
		});
	}

	/**
	 * 是否需要隐藏底部
	 */
	private void isHideTheFootView() {
		if (null != list) {
			int size = 20;
			if(isMore) size = 1;
			if (list.size() < size) {
				meetMainListview.setPullLoadEnable(false);
			} else {
				meetMainListview.setPullLoadEnable(true);
			}
		}
	}

	/**
	 * 进度条提示
	 * 
	 * @param str
	 */
	public void setProgressShow(String str) {
		DialogUtil.showDialog(progressDialog, str, false);
	}

	/**
	 * 切换tab
	 * 
	 * @param currentTag
	 * @param text
	 * @param bg
	 */
	private void changeTab(int currentTag, TextView text, RelativeLayout bg) {

		if (meetMainListview.isOnLoadMore() || meetMainListview.isOnRefresh()) {
			return;
		}

		if (isSearch)
			return;

		if (currentTag == type)
			return;
		else
			type = currentTag;

		tab1.setTextColor(0XFF8E8E90);
		tab2.setTextColor(0XFF8E8E90);

		tabBg1.setBackgroundResource(R.drawable.tab_default);
		tabBg2.setBackgroundResource(R.drawable.tab_default);

		text.setTextColor(0xFFFFA749);
		bg.setBackgroundResource(R.drawable.tab_checked);

		if (type != -1) {
			tab1_num_text.setBackgroundResource(R.drawable.tab_out);
			tab2_num_text.setBackgroundResource(R.drawable.tab_in);
		} else {
			tab2_num_text.setBackgroundResource(R.drawable.tab_out);
			tab1_num_text.setBackgroundResource(R.drawable.tab_in);
		}

		setProgressShow("获取数据中...");
		if (type == 5) {
			taskBiz.getTaskList("", type, TaskBiz.firstLoad, 20, "", "");
		} else {
			taskBiz.getMeetingList("", 20, 0, false, TaskBiz.firstLoad);
		}
	}

	@Override
	public void onClick(View v) {
		int id = v.getId();
		switch (id) {
		case R.id.meet_tabBg1: // 会议通知
			changeTab(-1, tab1, tabBg1);
			break;
		case R.id.meet_tabBg2: // 会议纪要
			changeTab(5, tab2, tabBg2);
			break;
		case R.id.topbar_back_ibtn: // 返回按键
			this.finish();
			break;
		}
	}

	/**
	 * 下拉刷新
	 */
	@Override
	public void onRefresh() {
		filter_edit.setEnabled(false);

		String searchString = "";
		if (isSearch) { // 是搜索状态的情况下
			searchString = filter_edit.getText().toString().trim();
		}
		int size = listInfors.size();

		if (size == 0) {
			if (type == 5) {
				taskBiz.getTaskList("", type, TaskBizImpl.firstLoad, 20, "",
						searchString);
			} else {
				taskBiz.getMeetingList(searchString, 20, 0, false,
						TaskBiz.firstLoad);
			}
		} else {
			if (type == 5) {
				taskBiz.getTaskList("", type, TaskBizImpl.onRefresh, 10,
						listInfors.get(0).getCreateTime(), searchString);
			} else {
				int id = Integer.valueOf(listInfors.get(0).getId());
				taskBiz.getMeetingList(searchString, 10, id, false,
						TaskBiz.onRefresh);
			}
		}

	}

	/**
	 * 加载更多
	 */
	@Override
	public void onLoadMore() {
		isMore = true;
		filter_edit.setEnabled(false);

		String searchString = "";
		if (isSearch) { // 是搜索状态的情况下
			searchString = filter_edit.getText().toString().trim();
		}

		int size = listInfors.size();
		if (size == 0) {
			if (type == 5) {
				taskBiz.getTaskList("", type, TaskBizImpl.firstLoad, 20, "",
						searchString);
			} else {
				taskBiz.getMeetingList(searchString, 20, 0, false,
						TaskBiz.firstLoad);
			}
		} else {
			if (type == 5) {
				taskBiz.getTaskList(listInfors.get(size - 1).getRunId(), type,
						TaskBizImpl.loadMore, 10, listInfors.get(size - 1)
								.getCreateTime(), searchString);
			} else {
				int id = Integer.valueOf(listInfors.get(size - 1).getId());
				taskBiz.getMeetingList(searchString, 10, id, true,
						TaskBiz.loadMore);
			}
		}
	}

	/**
	 * 列表明细详情
	 */
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		meeting = (TaskNewInfor) meetingAdapter.getItem(position - 1);
		Intent intent = new Intent(this, MeetingDetailsActivity.class);
		intent.putExtra("MeetingEntity", meeting);
		startActivityForResult(intent, REQUSET_CODE);
	}

	/**
	 * 查看详细成功后，返回列表时，更新此条数据的未读标识为已读
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == REQUSET_CODE && resultCode == RESULT_OK) {
			boolean isOpenSuccess = data
					.getBooleanExtra("isOpenSuccess", false);
			if (null != meeting && isOpenSuccess) {

				// 处理标识数
				if ("0".equals(meeting.getIsReaded())) {
					if (meeting.getType() == -1) {
						Constant.MeetNumber = Constant.MeetNumber - 1;
						if (Constant.MeetNumber > 0) {
							tab1_num_text.setVisibility(View.VISIBLE);
							tab1_num_text.setText(String.valueOf(Constant.MeetNumber));
						} else {
							tab1_num_text.setVisibility(View.GONE);
						}
					} else {
						Constant.Record = Constant.Record - 1;
						if (Constant.Record > 0) {
							tab2_num_text.setVisibility(View.VISIBLE);
							tab2_num_text.setText(String.valueOf(Constant.Record));
						} else {
							tab2_num_text.setVisibility(View.GONE);
						}
					}
					//Constant.MeetNumber = Constant.MeetNumber - 1;
				}

				// 处理未读标识为已读标识
				for (int i = 0; i < listInfors.size(); i++) {
					TaskNewInfor entity = listInfors.get(i);
					if (meeting.equals(entity) && "0".equals(meeting.getIsReaded())) {
						entity.setIsReaded("1");
					}
				}

				meetingAdapter.setData(listInfors);
				meetingAdapter.notifyDataSetChanged();
			}
		}
	}
	
	/**
	 * 根据集合获取未读数量
	 * @param list
	 */
	/*private void getNumber(List<TaskNewInfor> list) {
		if (list.get(0).getType() == -1) {
			count = 0;
			for (TaskNewInfor infor : list) {
				if (infor.getIsReaded().equals("0")) {
					count++;
				}
			}
		}
		if (type == 5) {
			int num = Constant.MeetNumber - count;
			if (num > 0) {
				tab2_num_text.setVisibility(View.VISIBLE);
				tab2_num_text.setText(String.valueOf(num));
			} else {
				tab2_num_text.setVisibility(View.GONE);
			}

		} else {
			if (count > 0) {
				tab1_num_text.setVisibility(View.VISIBLE);
				tab1_num_text.setText(String.valueOf(count));
			} else {
				tab1_num_text.setVisibility(View.GONE);
			}
		}
	}*/

	@Override
	protected void onDestroy() {
		super.onDestroy();
		progressDialog = null;
	}

}
