package com.gta.scpoa.activity;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;

import com.gta.db.annotation.ViewInject;
import com.gta.scpoa.R;
import com.gta.scpoa.views.BaseView;
import com.gta.scpoa.views.CommonTopView;
/**
 * 帮助和反馈
 * 
 * @author xiaojie.cai
 * 
 */
public class HelpFeedbackActivity extends BaseActivity implements OnClickListener{
	/**
	 * 公共顶部布局
	 */
	@ViewInject(id = R.id.main_top)
	private BaseView baseView = null;
	
	/**
	 * 头部View
	 */
	private CommonTopView topView = null;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.help_feedback);
		viewInit();
	}
	
	private void viewInit() {
		// 设置顶部布局
		topView = new CommonTopView(this);
		topView.setBackImageButtonEnable(true);
		topView.setBackImageButtonOnClickListener(this); // 左上角返回按钮监听
		topView.setAddImageButtonEnable(false);
		topView.setTitleTextViewEnable(true);
		topView.setTitleTextViewText("意见反馈");
		baseView.addView(topView);
	}

	@Override
	public void onClick(View v) {
		int id = v.getId();
		switch (id) {
		//头标返回按键
		case R.id.topbar_back_ibtn: 
			finish();
			break;
		default:
			break;
		}
	}
}
