package com.gta.scpoa.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.gta.db.annotation.ViewInject;
import com.gta.scpoa.R;
import com.gta.scpoa.activity.fragment.AdviceFragment;
import com.gta.scpoa.activity.fragment.BaseFragmnet;
import com.gta.scpoa.activity.fragment.FlowChartFragment;
import com.gta.scpoa.activity.fragment.FlowStateFragment;
import com.gta.scpoa.entity.TaskNewInfor;
import com.gta.scpoa.views.CommonTopView;

/**
 * 任务详情
 * 
 * @author 蔡晓杰
 * 
 */
public class TaskDetailActivity extends BaseActivity implements OnClickListener {
	/**
	 * 公共顶部布局
	 */
	@ViewInject(id = R.id.task_detail_topview)
	private CommonTopView taskDetailTopview = null;

	/**
	 *tab1背景 
	 */
	@ViewInject(id = R.id.task_detail_tabBg1)
	private RelativeLayout tabBg1 = null;
	/**
	 *tab2背景 
	 */
	@ViewInject(id = R.id.task_detail_tabBg2)
	private RelativeLayout tabBg2 = null;
	/**
	 *tab3背景 
	 */
	@ViewInject(id = R.id.task_detail_tabBg3)
	private RelativeLayout tabBg3 = null;
	/**
	 * tab1
	 */
	@ViewInject(id = R.id.task_detail_tab1)
	private TextView tab1 = null;
	/**
	 * tab2
	 */
	@ViewInject(id = R.id.task_detail_tab2)
	private TextView tab2 = null;
	/**
	 * tab3
	 */
	@ViewInject(id = R.id.task_detail_tab3)
	private TextView tab3 = null;
	
	public int type = 1; // 1意见  2流程图  3流程状态
	
	private AdviceFragment adviceFragment = null;
	private FlowChartFragment flowChartFragment = null;
	private FlowStateFragment flowStateFragment = null;
	private TaskNewInfor taskNewInfor = null;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_task_detail);
		getData();
		initView();
	}

	
	private void getData() {
		Bundle bundle = this.getIntent().getExtras();
		taskNewInfor = (TaskNewInfor) bundle.getSerializable("taskNewInfor");
	}
	
	private void titleInit() {
		// 设置顶部布局
		taskDetailTopview.setBackImageButtonEnable(true);
		taskDetailTopview.setBackImageButtonOnClickListener(this); // 左上角返回按钮监听
		taskDetailTopview.setTitleTextViewEnable(true);
		if(taskNewInfor.getType()==1){
			taskDetailTopview.setTitleTextViewText("待办审核");
		}else{
			taskDetailTopview.setTitleTextViewText("已办审核");
			tab1.setText("详情");
		}
	}
	
	private void initView() {
		titleInit();
		tab1.setTextColor(0xFFFFA749);
		tabBg1.setOnClickListener(this);
		tabBg2.setOnClickListener(this);
		tabBg3.setOnClickListener(this);
		
		Bundle bundle=new Bundle();
		bundle.putSerializable("infor", taskNewInfor);
//		adviceFragment = new AdviceFragment(this,taskNewInfor);
//		flowChartFragment = new FlowChartFragment(this, taskNewInfor);
//		flowStateFragment = new FlowStateFragment(this, taskNewInfor);
		adviceFragment = new AdviceFragment();
		flowChartFragment = new FlowChartFragment();
		flowStateFragment = new FlowStateFragment();
		adviceFragment.setArguments(bundle);
		flowChartFragment.setArguments(bundle);
		flowStateFragment.setArguments(bundle);
		addFragment();
	}
    private Fragment cacheFragment=null;
	private void addFragment(){	
		FragmentManager manager = getSupportFragmentManager();
		FragmentTransaction transaction =  manager.beginTransaction();
		cacheFragment=adviceFragment;
		transaction.add(R.id.task_fragment,adviceFragment);
		transaction.commit();
	}
	
	private void changeFragment(BaseFragmnet fragment){
		/*切换页面*/
		FragmentManager manager = getSupportFragmentManager();
		FragmentTransaction transaction=manager.beginTransaction();
		if(cacheFragment!=null){
			transaction.remove(cacheFragment);
		}
		transaction.replace(R.id.task_fragment,fragment,"");
		cacheFragment=fragment;
//		transaction.addToBackStack(null); 
		transaction.commit();
	}
	
	/* 用于切换tab */
	private void createTab(int currentTag, TextView text, RelativeLayout bg) {
		if (currentTag == type)
			return;
		else
			type = currentTag;

		tab1.setTextColor(0xFF8E8E90);
		tab2.setTextColor(0xFF8E8E90);
		tab3.setTextColor(0xFF8E8E90);

		tabBg1.setBackgroundResource(R.drawable.tab_default);
		tabBg2.setBackgroundResource(R.drawable.tab_default);
		tabBg3.setBackgroundResource(R.drawable.tab_default);

		text.setTextColor(0xFFFFA749);
		bg.setBackgroundResource(R.drawable.tab_checked);
	}
	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.task_detail_tabBg1:
			createTab(1, tab1, tabBg1);
			changeFragment(adviceFragment);  //意见
			break;
		case R.id.task_detail_tabBg2:
			createTab(2, tab2, tabBg2);
			changeFragment(flowChartFragment);  //流程图
			break;
		case R.id.task_detail_tabBg3:
			createTab(3, tab3, tabBg3);
			changeFragment(flowStateFragment);  //流程状态
			break;
		case R.id.topbar_back_ibtn: // 返回
			finish();
			break;
		}
	}
	
	
	public void goBackForResult(String id ,int code, String message){
		Intent intent = new Intent();
		intent.putExtra("ID", id);
		intent.putExtra("message", message);
		this.setResult(code, intent);
		this.finish();
	}
	
	
	/*返回键的捕捉*/
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(keyCode ==  KeyEvent.KEYCODE_BACK){
			finish();
			return false;
		}
		return true;
	}
}
