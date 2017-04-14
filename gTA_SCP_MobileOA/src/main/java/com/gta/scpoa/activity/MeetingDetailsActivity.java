package com.gta.scpoa.activity;

import java.util.List;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
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
 * 会议详情
 */
public class MeetingDetailsActivity extends BaseActivity implements
		OnClickListener {

	/**
	 * 头部布局
	 */
	@ViewInject(id = R.id.meeti_top_view)
	private CommonTopView meetingTopview = null;

	/**
	 * 标题
	 */
	@ViewInject(id = R.id.detail_title)
	private TextView mTitleTV;

	@ViewInject(id = R.id.notice_detail_container)
	private BaseView container;

	@ViewInject(id = R.id.meeting_attach_listview)
	private ListView attach_listview = null;

	/**
	 * 详细界面的表单适配器 附件
	 */
	private TabAdapter adapter = null;

	/**
	 * 业务处理接口
	 */
	private AdviceBiz biz;

	/**
	 * 详细表单数据集合
	 */
	private List<TableInfor> data = null;

	/**
	 * 打开详细成功标识
	 */
	private boolean isOpenSuccess = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.meeting_notice_details_layout);
		viewInit();
		if (null == biz) {
			biz = new AdviceBizImpl(this, mUIHandler);
		}
		adapter = new TabAdapter(getApplicationContext());

		getData();
	}

	private void viewInit() {

		// 设置顶部布局
		meetingTopview.setBackImageButtonEnable(true);
		meetingTopview.setBackImageButtonOnClickListener(this); // 左上角返回按钮监听

		// 界面中间标题
		meetingTopview.setTitleTextViewEnable(true);
		
	}

	/**
	 * 获取表单数据
	 */
	private void getData() {
		TaskNewInfor meeting = (TaskNewInfor) getIntent().getSerializableExtra(
				"MeetingEntity");
		if(meeting.getType() == -1){
			meetingTopview.setTitleTextViewText("会议详情");
		}else{
			meetingTopview.setTitleTextViewText("会议纪要详情");
		}
		mTitleTV.setText(meeting.getSubject());

		// 显示进度条
		container.addProgressBar(this);
		biz.getTableData(meeting);
	}

	@Override
	public void onClick(View v) {
		int id = v.getId();
		switch (id) {
		case R.id.topbar_back_ibtn: // 返回按键
			Intent intent = new Intent();
			intent.putExtra("isOpenSuccess", isOpenSuccess);
			setResult(RESULT_OK, intent);
			this.finish();
			break;
		default:
			break;
		}
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			Intent intent = new Intent();
			intent.putExtra("isOpenSuccess", isOpenSuccess);
			setResult(RESULT_OK, intent);
			this.finish();
			return true;
		}

		return super.onKeyDown(keyCode, event);
	}

	@SuppressLint("HandlerLeak")
	private Handler mUIHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			container.removeProgressBar();
			switch (msg.what) {
			case AdviceBiz.MSG_TABLE_GET_SUCCESS: // 成功处理

				data = (List<TableInfor>) msg.obj;
				if (null != data && data.size() > 0) {
					isOpenSuccess = true;
					adapter.setData(data);
					attach_listview.setAdapter(adapter);
				}
				break;

			case AdviceBiz.MSG_TABLE_GET_FAIL: // 失败处理
				UIUtils.ToastMessage(getApplicationContext(),
						msg.obj.toString());
				break;
			}
		}
	};
	
}
