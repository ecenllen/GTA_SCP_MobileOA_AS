package com.gta.scpoa.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.gta.db.annotation.ViewInject;
import com.gta.scpoa.R;
import com.gta.scpoa.application.GTAApplication;
import com.gta.scpoa.biz.ContactListBiz;
import com.gta.scpoa.biz.impl.ContactListBizImpl;
import com.gta.scpoa.entity.ContactInfo;
import com.gta.scpoa.util.DialogUtil;
import com.gta.scpoa.util.UIUtils;
import com.gta.scpoa.views.CommonTopView;
import com.gta.util.BaseStringUtils;

import java.util.Stack;

/**
 * 新增或编辑联系人
 * 
 * @author shengping.pan
 * 
 */
public class AddOrUpdateContactActivity extends BaseActivity implements
		OnClickListener {

	/**
	 * 用户名
	 */
	@ViewInject(id = R.id.et_user_name)
	private EditText etUserName = null;

	/**
	 * 性别
	 */
	@ViewInject(id = R.id.rg_user_sex)
	private RadioGroup rgUserSex = null;
	/**
	 * 男
	 */
	@ViewInject(id = R.id.radio_Male)
	private RadioButton radioMale = null;
	/**
	 * 女
	 */
	@ViewInject(id = R.id.radio_Female)
	private RadioButton radioFemale = null;

	/**
	 * 电话号码
	 */
	@ViewInject(id = R.id.et_mobile_phone)
	private EditText etMobile_phone = null;

	/**
	 * 邮箱
	 */
	@ViewInject(id = R.id.et_email)
	private EditText etEmail = null;

	/**
	 * 部门名称
	 */
	@ViewInject(id = R.id.et_unit_name)
	private EditText etUnitName = null;

	/**
	 * 职位
	 */
	@ViewInject(id = R.id.et_duty)
	private EditText etDuty = null;

	/**
	 * 部门电话
	 */
	@ViewInject(id = R.id.et_unit_phone)
	private EditText etUnitPhone = null;
	
	/**
	 * 单位地址  
	 */
	@ViewInject(id = R.id.et_unit_address)
	private EditText etUnitAddress = null;

	/**
	 * 家庭住址
	 */
	@ViewInject(id = R.id.et_home_address)
	private EditText etHomeAddress = null;

	/**
	 * 家庭电话
	 */
	@ViewInject(id = R.id.et_home_phone)
	private EditText etHomePhone = null;

	/**
	 * 通讯录处理接口
	 */
	private ContactListBiz contactListBiz = null;
	private ContactInfo contactInfo = null;

	/**
	 * 顶部布局
	 */
	@ViewInject(id = R.id.add_contact_topview)
	private CommonTopView addContactTopview = null;

	/**
	 * 公共顶部View
	 */
	//private CommonTopView topView = null;

	/**
	 * 进度条
	 */
	private ProgressDialog progressDialog = null;

	/**
	 * 单位名称/部门名称  转换
	 */
	@ViewInject(id = R.id.tv_unit_name)
	private TextView tvUnitName;
	
	/**
	 * 职位/职务 转换
	 */
	@ViewInject(id = R.id.tv_duty)
	private TextView tvDuty;	
	
	/**
	 * 单位电话/部门电话 转换
	 */
	@ViewInject(id = R.id.tv_unit_phone)
	private TextView tvUnitPhone;		

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_add_contact);

		// 初始化业务处理类
		if (null == contactListBiz) {
			contactListBiz = new ContactListBizImpl(
					AddOrUpdateContactActivity.this, handler);
		}

		// 获取通讯录详情(用于编辑)
		Intent intent = getIntent();
		contactInfo = (ContactInfo) intent.getSerializableExtra("contactInfo");

		// 初始化view
		initView();
	}

	/*
	 * UI数据处理
	 */
	private Handler handler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			if (progressDialog != null) {
				progressDialog.dismiss();
			}
			switch (msg.what) {
			case 0: // 失败处理
				Toast.makeText(getApplicationContext(), msg.obj.toString(),
						Toast.LENGTH_SHORT).show();
				finish();
				break;
			case 1: // 编辑成功处理
				Toast.makeText(getApplicationContext(), "修改成功!",
						Toast.LENGTH_SHORT).show();
				ContactInfo contactInfo = (ContactInfo) msg.obj;
				Intent intent = new Intent();
				Bundle bundle = new Bundle();
				bundle.putSerializable("contactInfo", contactInfo);
				intent.putExtras(bundle);
				setResult(RESULT_OK, intent);
				finish();
				break;
			case 2: // 新增成功处理
				Toast.makeText(getApplicationContext(), "新增成功!",
						Toast.LENGTH_SHORT).show();
				Stack<Activity> list = GTAApplication.instance.getActivityList();
				for (Activity activity : list) {
					if (activity instanceof ContactListActivity) {
						activity.finish();
					}
				}
				startActivity(new Intent(AddOrUpdateContactActivity.this,
						ContactListActivity.class));
				finish();
				break;
			}
		}
	};

	private void initView() {

		// 设置顶部布局
		addContactTopview.setBackImageButtonEnable(true);
		addContactTopview.setBackImageButtonOnClickListener(this); // 左上角返回按钮监听
		addContactTopview.setRightTextViewEnable(true);
		addContactTopview.setRightTextViewOnClickListener(this);
		addContactTopview.setRightTextViewText(getString(R.string.complete_button));
		addContactTopview.setTitleTextViewEnable(true);
		if (null == contactInfo) {
			addContactTopview.setTitleTextViewText(getString(R.string.add_contact_title));
		} else {
			addContactTopview.setTitleTextViewText(getString(R.string.update_contact_title));
		}

		// 编辑时设置属性值
		if (null != contactInfo) {
			etUserName.setText(contactInfo.getContactName());

			if (contactInfo != null && contactInfo.getSex().equals("男")) {
				radioMale.setChecked(true);
				radioFemale.setChecked(false);
			} else {
				radioFemale.setChecked(true);
				radioMale.setChecked(false);
			}

			etMobile_phone.setText(contactInfo.getMobilePhone());
			etEmail.setText(contactInfo.getEmail());
			etUnitName.setText(contactInfo.getDeptName());
			etDuty.setText(contactInfo.getDuty());
			etUnitPhone.setText(contactInfo.getDeptPhone());
			etHomeAddress.setText(contactInfo.getHomeAddress());
			etHomePhone.setText(contactInfo.getHomePhone());
			etUnitAddress.setText(contactInfo.getWorkAddress());
		}
		
		tvUnitName.setText(getString(R.string.unit_name_title0));
		tvDuty.setText(getString(R.string.duty_title));
		tvUnitPhone.setText(getString(R.string.unit_phone_title0));		
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.topbar_right_tv: // 完成
			
			try {
				InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
				//强制隐藏输入法键盘
				imm.hideSoftInputFromWindow(AddOrUpdateContactActivity.this.getCurrentFocus().getWindowToken(), 0);
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			String msg = contactListBiz.verifyData(etUserName,
					etMobile_phone, etEmail, etUnitName,
					etDuty, etUnitPhone, etHomeAddress,
					etHomePhone, etUnitAddress);
			if (!BaseStringUtils.isEmpty(msg)) {
				UIUtils.ToastMessage(
						AddOrUpdateContactActivity.this, msg,
						500);
				return;
			}

			if (null == contactInfo) {
				contactInfo = new ContactInfo();
				contactInfo.setContactId("-1");
			}
			contactInfo.setContactName(etUserName.getText()
					.toString().trim().replaceAll(" ", ""));
			String sex = radioMale.isChecked() ? "男" : "女";
			contactInfo.setSex(sex);
			contactInfo.setMobilePhone(etMobile_phone.getText()
					.toString().trim().replaceAll(" ", ""));
			contactInfo.setEmail(etEmail.getText().toString()
					.trim().replaceAll(" ", ""));
			contactInfo.setDeptName(etUnitName.getText()
					.toString().trim().replaceAll(" ", ""));
			contactInfo.setDuty(etDuty.getText().toString()
					.trim().replaceAll(" ", ""));
			contactInfo.setDeptPhone(etUnitPhone.getText()
					.toString().trim().replaceAll(" ", ""));
			contactInfo.setWorkAddress(etUnitAddress.getText()
					.toString().trim().replaceAll(" ", ""));			
			contactInfo.setHomeAddress(etHomeAddress.getText()
					.toString().trim().replaceAll(" ", ""));
			contactInfo.setHomePhone(etHomePhone.getText()
					.toString().trim().replaceAll(" ", ""));

			contactListBiz.addOrUpdateContact(contactInfo);
			if (null == progressDialog) {
				progressDialog = new ProgressDialog(
						AddOrUpdateContactActivity.this);
			}
			DialogUtil.showDialog(progressDialog, "数据加载中...",
					false);			

			break;
		case R.id.topbar_back_ibtn: // 取消
			// 隐藏键盘
			/*InputMethodManager imm2 = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
			imm2.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);*/	
			finish();
			break;
		}
	}
	
	

}
