package com.gta.scpoa.activity;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

import com.gta.db.annotation.ViewInject;
import com.gta.scpoa.R;
import com.gta.scpoa.adapter.OfficialNoticeAdapter;
import com.gta.scpoa.biz.TaskBiz;
import com.gta.scpoa.biz.impl.TaskBizImpl;
import com.gta.scpoa.entity.TaskNewInfor;
import com.gta.scpoa.util.UIUtils;
import com.gta.scpoa.views.BaseView;
import com.gta.scpoa.views.CommonBottomView;
import com.gta.scpoa.views.CommonTopView;
import com.gta.scpoa.views.XListView;
import com.gta.scpoa.views.XListView.IXListViewListener;

/**
 * 公文公告页面
 * 
 * @author bin.wang1
 * 
 */
public class OfficialNoticeActivity extends BaseActivity implements OnClickListener , OnItemClickListener, IXListViewListener{
	
	@ViewInject(id = R.id.notice_container)
	private BaseView container;
	
	@ViewInject(id = R.id.notice_commontop)
	private CommonTopView topView;
	
	@ViewInject(id = R.id.notice_listview)
	private XListView mNoticeLV;
	private OfficialNoticeAdapter adapter;
	@ViewInject(id = R.id.notice_commonbottom)
	private CommonBottomView bottomView;
	
	
	private OfficialNoticeHandler handler;
	
	private List<TaskNewInfor> officialNotices;
	private TaskBizImpl biz;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_officialnotice);
		setupViews();
		getFirstData();
	}

	private void setupViews() {
		
		topView.setTitleTextViewEnable(true);
		topView.setTitleTextViewText(getString(R.string.official_notice_title));
		
		topView.setBackImageButtonEnable(true);
		topView.setBackImageButtonOnClickListener(this);
		topView.setAddImageButtonEnable(true);
				
		bottomView.setIsMainActivity(false);		
		
		adapter = new OfficialNoticeAdapter(getLayoutInflater());
		
		this.officialNotices = new ArrayList<TaskNewInfor>();
		this.handler = new OfficialNoticeHandler(this);
		biz = new TaskBizImpl(getApplicationContext(), handler);
		mNoticeLV.setXListViewListener(this);
		
		
		mNoticeLV.setPullLoadEnable(false);
		mNoticeLV.setPullRefreshEnable(true);
		
		mNoticeLV.setOnItemClickListener(this);
						
	}

	private void getFirstData() {	
		//打开进度条
		container.addProgressBar(this);
		biz.getTaskList("",3, TaskBiz.firstLoad, 20, "", "");
	}
	
	private void getMoreData(){
		if (officialNotices.size() == 0) {
			getFirstData();
		}else {
			TaskNewInfor task = officialNotices.get(officialNotices.size()-1);
			biz.getTaskList(task.getRunId(),3, TaskBiz.loadMore, 10,task.getCreateTime(), "");	
		}
		
	}
	private void getRefreshData(){
		if (officialNotices.size() == 0) {
			biz.getTaskList("",3, TaskBiz.firstLoad, 20, "", "");
		}else {
			biz.getTaskList("",3, TaskBiz.onRefresh, 10, officialNotices.get(0).getCreateTime(), "");
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.topbar_back_ibtn:
			this.finish();
			break;

		default:
			break;
		}	
	}
	
	
	/*
	 * 考虑点：
	 * 如果第一次获取数据失败
	 * 
	 * 不给listview设置数据
	 * 
	 * 在中间给提示语，或者显示下拉刷新
	 * 
	 * 如果是用下拉刷新，则提示语为："网络异常，获取数据失败，请下拉刷新重试！"
	 * 则刷新时需要判断officialNotices的size()，如果为0，则请求20条，如果不为0，则请求10条
	 * 
	 */
	
	private static class OfficialNoticeHandler extends Handler{
		WeakReference<OfficialNoticeActivity> wr;
				
		public OfficialNoticeHandler(OfficialNoticeActivity activity) {
			this.wr = new WeakReference<OfficialNoticeActivity>(activity);
		}

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			OfficialNoticeActivity mActivity = wr.get();
			
			switch (msg.what) {
			
			case TaskBiz.MSG_TASK_GET_SUCCESS://第一次取得数据
				
				//取消进度条
				mActivity.container.removeProgressBar();
			
				List<TaskNewInfor> list = (List<TaskNewInfor>) msg.obj;

//				Log.i("info", list.size()+"--"+HttpGrabUtils.temp.size());
//				for (TaskNewInfor taskNewInfor : list) {
//					taskNewInfor.setIsReaded("0");
//				}
//												

				if (null != list ) {
					
					if (list.size() < 20) {
						mActivity.mNoticeLV.setPullLoadEnable(false);						
					}else {
						mActivity.mNoticeLV.setPullLoadEnable(true);						
					}
									
					if (! list.isEmpty()) {	
						
						if (mActivity.container.isErrorViewShow()) {					
							mActivity.container.removeErrorView();
						}
						
						mActivity.officialNotices.addAll(list);			
						
					}else {					
						if (! mActivity.container.isErrorViewShow()) {						
							mActivity.container.addErrorView(mActivity,"暂无数据");
						}							
					}	
																							
				}
				
				mActivity.adapter.setData(list);
				mActivity.mNoticeLV.setAdapter(mActivity.adapter);
				
				
				mActivity.mNoticeLV.stopRefresh();
				mActivity.mNoticeLV.stopLoadMore();
			
				break;
				
			case TaskBiz.MSG_TASK_GET_REFRSH:
				/**
				 * 刷新是将listRefresh加到officialNotices的前面
				 */
				List<TaskNewInfor> listRefresh = (List<TaskNewInfor>) msg.obj;
				
				if (null != listRefresh && ! listRefresh.isEmpty()) {
					
					//从最后一个开始，将其加到officialNotices的首位
					for (int i = listRefresh.size()-1; i >= 0; i--) {					
						mActivity.officialNotices.add(0, listRefresh.get(i));
					}					
				}
				
				mActivity.adapter.setData(mActivity.officialNotices);
				//取消进度条
				mActivity.mNoticeLV.stopRefresh();
				
				mActivity.adapter.notifyDataSetChanged();
								
				break;
				
				
			case TaskBiz.MSG_TASK_GET_MORE:
				/**
				 * 加载更多是将listMore加到officialNotices的后面
				 */
				List<TaskNewInfor> listMore = (List<TaskNewInfor>) msg.obj;
				
				if (null != listMore ) {
					
								
//		原先的条件		if (listMore.size() < 10) 
					if (listMore.size() == 0) {
						mActivity.mNoticeLV.setPullLoadEnable(false);						
					}else {
						mActivity.mNoticeLV.setPullLoadEnable(true);						
					}
					
				}
				
				mActivity.officialNotices.addAll(listMore);
				
				mActivity.adapter.setData(mActivity.officialNotices);
				
				//取消进度条
				mActivity.mNoticeLV.stopLoadMore();
				mActivity.adapter.notifyDataSetChanged();
				
				break;
				
			case TaskBiz.MSG_TASK_GET_FAIL://获取数据失败
				
				String failStr  = (String)msg.obj;
			
				
				if(failStr.equals("command="+TaskBizImpl.firstLoad)){  //首次获取失败
					
					mActivity.container.removeProgressBar();										
					mActivity.mNoticeLV.stopRefresh();
					
					mActivity.mNoticeLV.setPullLoadEnable(false);
					
					if (! mActivity.container.isErrorViewShow()) {						
						mActivity.container.addErrorView(mActivity,"暂无数据");
					}
					mActivity.adapter.setData(null);
					mActivity.mNoticeLV.setAdapter(mActivity.adapter);
					failStr = "网络异常,加载数据失败!";
					UIUtils.ToastMessage(mActivity, failStr);
				}else if(failStr.equals("command="+TaskBizImpl.loadMore)){  //更多获取失败
					mActivity.mNoticeLV.stopLoadMore();
					failStr = "加载更多失败";
					UIUtils.ToastMessage(mActivity, failStr);
				}else if(failStr.equals("command="+TaskBizImpl.onRefresh)){  //最新获取失败
					mActivity.mNoticeLV.stopRefresh();
					failStr = "刷新失败";					
					UIUtils.ToastMessage(mActivity, failStr);
				}else{
					UIUtils.ToastMessage(mActivity, failStr);
				}
				break;
			default:
				break;
			}
			
			
		}
	}
	/**
	 * 刷新
	 */
	@Override
	public void onRefresh() {
		getRefreshData();
		
	}
	/**
	 * 加载更多
	 */
	@Override
	public void onLoadMore() {
		getMoreData();
		
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		TaskNewInfor notice = (TaskNewInfor) adapter.getItem(position-1);
		
		Intent intent = new Intent(this,OfficialNoticeDetailActivity.class);
		intent.putExtra("OfficialNoticeDetialEntity", notice);
		
		startActivityForResult(intent, 100);
				
	}
	
	@Override
	protected void onActivityResult(int arg0, int arg1, Intent intent) {
		super.onActivityResult(arg0, arg1, intent);
		if (arg1== 1010) {
			String id = intent.getStringExtra("noticeId");
			boolean isRead = intent.getBooleanExtra("isHaveRead", false);
			
			if (isRead) {
				adapter.changeState(id);
			}
		}
	
	}
}
