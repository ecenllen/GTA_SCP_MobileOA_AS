package com.gta.scpoa.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;

import com.gta.db.annotation.ViewInject;
import com.gta.scpoa.R;
import com.gta.scpoa.adapter.HomeGridAdapter;
import com.gta.scpoa.application.OAJpushManager;
import com.gta.scpoa.biz.IGetDataBiz;
import com.gta.scpoa.biz.impl.GetDataBizImpl;
import com.gta.scpoa.common.Constant;
import com.gta.scpoa.entity.HomeInfo;
import com.gta.scpoa.entity.HomeItem;
import com.gta.scpoa.service.TimerService;
import com.gta.scpoa.util.DialogUtil;
import com.gta.scpoa.util.ShortcutBadgerUtil;
import com.gta.scpoa.views.CommonBottomView;
import com.gta.scpoa.views.CommonTopView;
import com.gta.version.VersionService;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import static com.gta.scpoa.common.Constant.MSG_GETDATA_SUCCESS;


/**
 * 移动OA的首页,即主界面
 * 
 * @author bin.wang1
 * 
 */
public class MainActivity extends BaseActivity implements OnItemClickListener {

	@ViewInject(id = R.id.home_gridview)
	private GridView gridView;
	@ViewInject(id = R.id.main_top_view)
	private CommonTopView topView;

	@ViewInject(id = R.id.main_bottom_view)
	private CommonBottomView bottomView;

	private HomeGridAdapter adapter;
	private ProgressDialog mRefreshProgressDialog;

	private MainHandler mHandler;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		init();
		setupViews();
		initService();

		//注册推送模块
		OAJpushManager.registerJpushManager(this);
		
	}


	private void initService() {	
		Intent intent = new Intent(this,TimerService.class);
		startService(intent);
//		startService(new Intent(this,ScheduleTimerService.class));
	}
	
	
	@Override
	protected void onResume() {
		super.onResume();
		refresh();
		//开启版本更新service
		Intent intent = new Intent(this,VersionService.class);
		startService(intent);
	}
	
	
	private  static class MainHandler extends Handler{
		
		WeakReference<MainActivity> wr;
		
		MainHandler(MainActivity activity) {		
			wr = new WeakReference<MainActivity>(activity);			
		}
				
		@Override
		public void handleMessage(Message msg) {
			
			MainActivity mActivity = wr.get();			
			if (mActivity == null) {
				return;
			}
						
			switch (msg.what) {
			case MSG_GETDATA_SUCCESS:
				
				//如果程序已关闭，则
				if (mActivity.isFinishing()) {
					return;
				}
				DialogUtil.dismissDialog(mActivity.mRefreshProgressDialog);
				mActivity.handleResult((HomeInfo)msg.obj);
				break;
			case Constant.MSG_FAIL:
				DialogUtil.dismissDialog(mActivity.mRefreshProgressDialog);
//				UIUtils.ToastMessage(mActivity.getApplicationContext(), msg.obj.toString());
				break;
			case Constant.MSG_SHOW_PROGRESS:
				DialogUtil.showDialog(mActivity.mRefreshProgressDialog, msg.obj.toString());
				break;
			default:
				break;
			}
			
			
		}
	}

	private void init() {		
		mHandler = new MainHandler(this);
	}

	public void handleResult(HomeInfo info) {
		if (null != info) {
			// 返回成功标识
			setResult(RESULT_OK);
			update(info);
			ShortcutBadgerUtil.updateUnReadMesNum(this, info);// 更新桌面图标未读消息数字
		}
	}


	private void setupViews() {
		// 是否显示左边Logo 图标
		topView.setLogoImageViewEnable(true);
//		topView.setLogoImageViewEnable(false);
		topView.setAddImageButtonEnable(true);
		// 是否显示首页标题名称
		topView.setTitleTextViewEnable(false);
		topView.setTitleTextViewText(getResources().getString(R.string.title_mainactivity));
		

		
		bottomView.setIsMainActivity(true);
	
		bottomView.setHomeLLOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				refresh();
			}
		});
		

		adapter = new HomeGridAdapter(setData(null), getLayoutInflater());
		gridView.setAdapter(adapter);

		gridView.setOnItemClickListener(this);

	}

	public void refresh() {

		if (mRefreshProgressDialog == null) {
			mRefreshProgressDialog = new ProgressDialog(this);
			DialogUtil.init(mRefreshProgressDialog, true,true);
		}
		IGetDataBiz api=new GetDataBizImpl();
		api.updateHome(getApplicationContext(), mHandler, getGTAApplication().getUserID());
	}

	private void update(HomeInfo info) {
		adapter.setData(setData(info));
		adapter.notifyDataSetChanged();
	}

	private ArrayList<HomeItem> setData(HomeInfo info) {
		ArrayList<HomeItem> list = new ArrayList<HomeItem>();
		HomeItem item = null;
		// 规定：item.setNoticeNum(0)，代表NoticeNum的TextView不可见
		for (int i = 0; i < 6; i++) {
			item = new HomeItem();
			item.setNoticeNum(String.valueOf(0));
			switch (i) {
			case 0:
				item.setTitle("待办/已办");
				if (info != null) {
					String num = info.getTasks();
					item.setNoticeNum(num);				
					Constant.taskNumber = num;
					bottomView.setNumber(num);
				}
				item.setLlBackground(R.drawable.main_item1_selector);
				item.setIvDrawable(R.drawable.main_1);
				break;
			case 1:
				item.setTitle("公文公告");
				if (info != null) {
					item.setNoticeNum(info.getNotice());
				}
				item.setLlBackground(R.drawable.main_item2_selector);
				item.setIvDrawable(R.drawable.main_3);
				break;
			case 2:

				item.setTitle("会议");
				if (info != null && !TextUtils.isEmpty(info.getMeeting())) {
					Constant.MeetNumber = Integer.parseInt(info.getMeeting());
					Constant.Record= info.getRecord();
					item.setNoticeNum(String.valueOf(Constant.MeetNumber+Constant.Record));
				}
				item.setLlBackground(R.drawable.main_item3_selector);
				item.setIvDrawable(R.drawable.main_6);
				break;

			case 3:

				item.setTitle("邮箱");
				if (info != null) {
					item.setNoticeNum(info.getMail());
				}
				item.setLlBackground(R.drawable.main_item4_selector);
				item.setIvDrawable(R.drawable.main_4);

				break;
			case 4:
				item.setTitle("通讯录");
				item.setLlBackground(R.drawable.main_item5_selector);
				item.setIvDrawable(R.drawable.main_8);
				break;
			case 5:
				item.setTitle("日程");
				if (info != null) {
					item.setNoticeNum(info.getSchedule());
				}
				item.setLlBackground(R.drawable.main_item6_selector);
				item.setIvDrawable(R.drawable.main_9);
				break;
			case 6:
				item.setTitle("课程表");
				item.setLlBackground(R.drawable.main_item7_selector);
				item.setIvDrawable(R.drawable.main_7);
				break;
			case 7:
				item.setTitle("德育考核");
				item.setLlBackground(R.drawable.main_item8_selector);
				item.setIvDrawable(R.drawable.main_10);
				break;
			default:
				break;
			}
			list.add(item);
		}
		return list;
	}

	/**
	 * 跳转到相应的功能 模板:startActivity(new Intent(this,Class<T>.class));
	 */
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {

		switch (position) {
		// 待办已办
		case 0:
		 startActivity(new Intent(this, TaskMainActivity.class));
			break;
		// 公文公告
		case 1:
		 startActivity(new Intent(this, OfficialNoticeActivity.class));
			break;
		// 会议
		case 2:
		 startActivity(new Intent(this,MeetingMainActivity.class));
			break;
		// 邮箱
		case 3:
			startActivity(new Intent(this, MailMainNewActivity.class));

			break;
		// 通讯录
		case 4:
			startActivity(new Intent(this, ContactListActivity.class));
			break;
		// 日程
		case 5:
			startActivity(new Intent(this, ScheduleActivity.class));
			break;
		// 课程表
		case 6:
			break;
		// 德育考核
		case 7:
		
			break;
		default:
			break;
		}

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		mHandler.removeCallbacksAndMessages(this);
		mHandler = null;
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			moveTaskToBack(false);
			return true;
		}

		return super.onKeyDown(keyCode, event);
	}
}
