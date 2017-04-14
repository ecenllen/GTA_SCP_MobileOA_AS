package com.gta.scpoa.activity;

import java.lang.ref.WeakReference;
import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ListView;
import android.widget.TextView;

import com.gta.db.annotation.ViewInject;
import com.gta.scpoa.R;
import com.gta.scpoa.adapter.TabAdapter;
import com.gta.scpoa.biz.AdviceBiz;
import com.gta.scpoa.biz.impl.AdviceBizImpl;
import com.gta.scpoa.entity.TableInfor;
import com.gta.scpoa.entity.TaskNewInfor;
import com.gta.scpoa.util.UIUtils;
import com.gta.scpoa.views.BaseView;
import com.gta.scpoa.views.CommonTopView;

/**
 * 公文公告详情页面
 * 
 * @author bin.wang1
 * 
 */
public class OfficialNoticeDetailActivity extends BaseActivity implements OnClickListener {
	@ViewInject(id = R.id.detail_top)
	private CommonTopView topView;
	@ViewInject(id = R.id.detail_title)
	private TextView mTitleTV;
	@ViewInject(id = R.id.detail_department_tv)
	private TextView mDepartmentTV;
	@ViewInject(id = R.id.detail_date_tv)
	private TextView mDateTV;
	@ViewInject(id = R.id.notice_detail_container)
	private BaseView container;
	@ViewInject(id = R.id.detail_listview)
	private ListView mBodyLV;

	private TabAdapter adapter;
	
	private OfficialDetailHandler handler;
	private AdviceBizImpl biz;
	private String id;
	
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_officialnoticedetail);
		setupViews();
		setData();
	}
	
	private void setupViews() {
		
		topView.setBackImageButtonEnable(true);
		topView.setBackImageButtonOnClickListener(this);
		topView.setTitleTextViewEnable(true);
		topView.setTitleTextViewText(getString(R.string.official_notice_detail_title));
		
		this.adapter = new TabAdapter(getApplicationContext());
		this.handler = new OfficialDetailHandler(this);
		biz = new  AdviceBizImpl(getApplicationContext(),handler);
		
	}

	private void setData() {
		TaskNewInfor notice = (TaskNewInfor) getIntent().getSerializableExtra("OfficialNoticeDetialEntity");
		id = notice.getCopyId();
		
		mTitleTV.setText(notice.getSubject());
		mDepartmentTV.setText(notice.getCreator());
		mDateTV.setText(notice.getCreateTime());
		//显示进度条
		container.addProgressBar(this);
		biz.getTableData(notice);
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
	
	private static class OfficialDetailHandler extends Handler{
		WeakReference<OfficialNoticeDetailActivity> wr;
		
		public OfficialDetailHandler(OfficialNoticeDetailActivity activity) {
			super();
			this.wr = new WeakReference<OfficialNoticeDetailActivity>(activity);
		}

		@Override
		public void handleMessage(Message msg) {
			OfficialNoticeDetailActivity mActivity = wr.get();
			mActivity.container.removeProgressBar();
			switch (msg.what) {
			
			case AdviceBiz.MSG_TABLE_GET_SUCCESS:
				Intent intent = new Intent();
				intent.putExtra("isHaveRead", true);
				intent.putExtra("noticeId",mActivity. id);
				mActivity.setResult(1010, intent);
				
				
				List<TableInfor> data = (List<TableInfor>) msg.obj;
				mActivity.adapter.setData(data);
				mActivity.mBodyLV.setAdapter(mActivity.adapter);
				break;
				
			case AdviceBiz.MSG_TABLE_GET_FAIL:
				UIUtils.ToastMessage(mActivity.getApplicationContext(), msg.obj.toString());
				break;
			default:
				break;
			}
		}
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		
	}
}
