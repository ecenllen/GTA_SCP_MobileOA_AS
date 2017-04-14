package com.gta.scpoa.activity;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.gta.db.annotation.ViewInject;
import com.gta.scpoa.R;
import com.gta.scpoa.views.BaseView;
import com.gta.scpoa.views.CommonTopView;
import com.gta.version.VersionService;

/**
 * 关于
 * 
 * @author xiaojie.cai
 * 
 */
public class AboutActivity extends BaseActivity implements OnClickListener{
	/**
	 * 公共顶部布局
	 */
	@ViewInject(id = R.id.main_top)
	private BaseView baseView = null;
	/**
	 * 版本更新
	 * */
	@ViewInject(id = R.id.version_layout)
	private RelativeLayout version_layout = null;
	/**
	 * 功能介绍
	 * */
	@ViewInject(id = R.id.fun_introduction_layout)
	private RelativeLayout fun_introduction_layout = null;
	/**
	 * 帮助和反馈
	 * */
	@ViewInject(id = R.id.help_feedback_layout)
	private RelativeLayout help_feedback_layout = null;
	/**
	 * 头部View
	 */
	private CommonTopView topView = null;
	private TextView tv_version=null;
	private TextView tv_versioncode=null;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.about_layout);
		viewInit();
	}
	
	private void viewInit() {
		// 设置顶部布局
		topView = new CommonTopView(this);
		topView.setBackImageButtonEnable(true);
		topView.setBackImageButtonOnClickListener(this); // 左上角返回按钮监听
		topView.setAddImageButtonEnable(false);
		topView.setTitleTextViewEnable(true);
		topView.setTitleTextViewText("关于我们");
		baseView.addView(topView);

		version_layout.setOnClickListener(this);
		fun_introduction_layout.setOnClickListener(this);
		help_feedback_layout.setOnClickListener(this);
		//
		tv_version=(TextView)findViewById(R.id.tv_about_version);
		tv_versioncode=(TextView)findViewById(R.id.tv_about_versioncode);
		PackageManager manager=getApplication().getPackageManager();
		try {
			PackageInfo info = manager.getPackageInfo(getApplication().getPackageName(), 0);
			tv_version.setText("移动OA V"+info.versionName);
			tv_versioncode.setText("V"+info.versionName);
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onClick(View v) {
		int id = v.getId();
		switch (id) {
		//版本
		case R.id.version_layout:
//			UIUtils.ToastMessage(this, "开始检测新版本!");
			Intent intent=new Intent(new VersionService().filter3);
			sendBroadcast(intent);
			break;
		//功能介绍
		case R.id.fun_introduction_layout:
			startActivity(new Intent(this, FunIntroductionActivity.class));
			break;
		//帮助和反馈
		case R.id.help_feedback_layout:
			startActivity(new Intent(this, HelpFeedbackActivity.class));
			break;
		//头标返回按键
		case R.id.topbar_back_ibtn: 
			finish();
			break;
		default:
			break;
		}
	}
}
