package com.gta.scpoa.activity;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.gta.scpoa.application.GTAApplication;
import com.gta.util.ViewInjector;

public class BaseActivity extends FragmentActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//禁止横屏
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		// 每一个activity都添加到activity堆栈中
		GTAApplication.instance.addActivity(this);
	}

	@Override
	public void setContentView(int layoutResID) {
		super.setContentView(layoutResID);
		// 初始化GTA框架，即可使用注解
		ViewInjector.getInstance().inJectAll(this);
	}

	protected GTAApplication getGTAApplication() {
		return GTAApplication.instance;
	}
	@Override
	protected void onDestroy() {	
		super.onDestroy();
		GTAApplication.instance.remove(this);
	}
}
