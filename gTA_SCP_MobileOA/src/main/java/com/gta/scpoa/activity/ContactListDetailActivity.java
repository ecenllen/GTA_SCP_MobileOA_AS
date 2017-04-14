package com.gta.scpoa.activity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.telephony.TelephonyManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.gta.db.annotation.ViewInject;
import com.gta.scpoa.R;
import com.gta.scpoa.biz.ContactListBiz;
import com.gta.scpoa.biz.impl.ContactListBizImpl;
import com.gta.scpoa.entity.ContactInfo;
import com.gta.scpoa.util.DialogUtil;
import com.gta.scpoa.views.CommonTopView;

/**
 * 通讯录详情
 * 
 * @author shengping.pan
 * 
 */
public class ContactListDetailActivity extends BaseActivity implements
		OnClickListener {

	@ViewInject(id = R.id.topbar_title_tv)
	private TextView tvTitle = null;

	/**
	 * 用户名
	 */
	@ViewInject(id = R.id.tv_user_name)
	private TextView tvUserName = null;

	/**
	 * 性别
	 */
	@ViewInject(id = R.id.tv_sex)
	private TextView tvSex = null;

	/**
	 * 号码
	 */
	@ViewInject(id = R.id.tv_mobile_phone)
	private TextView tvMobilePhone = null;

	/**
	 * 邮箱
	 */
	@ViewInject(id = R.id.tv_email)
	private TextView tvEmail = null;

	/**
	 * 单位名称/部门名称 的显示
	 */
	@ViewInject(id = R.id.tv_unit_name0)
	private TextView tvUnitName0 = null;
	/**
	 * 单位名称
	 */
	@ViewInject(id = R.id.tv_unit_name)
	private TextView tvUnitName = null;

	/**
	 * 职务/职位 的显示
	 */
	@ViewInject(id = R.id.tv_dept0)
	private TextView tvDuty0 = null;
	/**
	 * 职务
	 */
	@ViewInject(id = R.id.tv_dept)
	private TextView tvDuty = null;

	/**
	 * 部门电话/单位电话 的显示
	 */
	@ViewInject(id = R.id.tv_dept_phone0)
	private TextView tvUnitPhone0 = null;
	/**
	 * 工作号码
	 */
	@ViewInject(id = R.id.tv_dept_phone)
	private TextView tvUnitPhone = null;

	/**
	 * 单位地址
	 */
	@ViewInject(id = R.id.tv_unit_address)
	private TextView tvUnitAddress = null;

	/**
	 * 家庭地址
	 */
	@ViewInject(id = R.id.tv_home_address)
	private TextView tvHomeAddress = null;

	/**
	 * 家庭电话
	 */
	@ViewInject(id = R.id.tv_home_phone)
	private TextView tvHomePhone = null;

	/**
	 * 其它信息layout
	 */
	@ViewInject(id = R.id.other_layout)
	private LinearLayout otherLayout = null;

	/**
	 * 拨打电话imageview
	 */
	@ViewInject(id = R.id.iv_mobile_phone)
	private ImageView ivCall = null;

	/**
	 * 发送信息imageview
	 */
	@ViewInject(id = R.id.iv_sms)
	private ImageView ivSMS = null;

	/**
	 * 通讯录信息
	 */
	private ContactInfo contactInfo = null;

	/**
	 * 通讯录类型(1:个人、2：公用、3:教职)
	 */
	private int type;

	/**
	 * 删除按钮
	 */
	@ViewInject(id = R.id.delete_contact_button)
	private Button deleteButton = null;

	/**
	 * 通讯录业务处理接口
	 */
	private ContactListBiz contactListBiz = null;

	/**
	 * 顶部布局
	 */
	@ViewInject(id = R.id.contact_detail_topview)
	private CommonTopView contactDetailTopView = null;

	/**
	 * 弹出框
	 */
	private AlertDialog myDialog = null;

	/**
	 * 进度条
	 */
	private ProgressDialog progressDialog = null;

	/**
	 * 请求返回code
	 */
	private final int REQUEST_CODE = 1;

	/**
	 * 通讯录是否修改
	 */
	private boolean isUpdate;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_contact_list_detail);

		if (null == contactListBiz) {
			contactListBiz = new ContactListBizImpl(this, handler);
		}

		Intent intent = getIntent();
		type = intent.getIntExtra("type", 1);
		ContactInfo contact = (ContactInfo) intent.getSerializableExtra("contact");
		contactListBiz.getContactDetail(contact, type);

		initView();
		if (null == progressDialog) {
			progressDialog = new ProgressDialog(this);
		}
		DialogUtil.showDialog(progressDialog, "数据加载中...", false);
	}

	private void initView() {
		// 设置顶部布局
		contactDetailTopView.setBackImageButtonEnable(true);
		contactDetailTopView.setBackImageButtonOnClickListener(this); // 左上角返回按钮监听
		
		contactDetailTopView.setTitleTextViewEnable(true);
		contactDetailTopView.setTitleTextViewText("详情");
		
		if (type == 1) {
			contactDetailTopView.setRightTextViewEnable(true);
			contactDetailTopView.setRightTextViewOnClickListener(this);
			contactDetailTopView.setRightTextViewText(getString(R.string.right_button_title));
		}

		// 根据类型展示相应功能
		if (type != 1) {
			deleteButton.setVisibility(View.GONE);
		} else {
			deleteButton.setOnClickListener(this);
		}

		// 根据类型显示相应信息 部门名称/单位名称
		if (type != 3) {
			tvUnitName0.setText("单位名称");
			tvDuty0.setText("职位");
			tvUnitPhone0.setText("单位电话");
		}
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
				break;
			case 1: // 刪除成功处理
				Intent intent = new Intent();
				setResult(RESULT_OK, intent);
				finish();
				break;
			case 2: // 加载成功处理

				contactInfo = (ContactInfo) msg.obj;

				// 展示信息
				if (null != contactInfo) {
					tvUserName.setText(contactInfo.getContactName());
					tvSex.setText(contactInfo.getSex());
					String mobilePhone = contactInfo.getMobilePhone();
					TelephonyManager manager = (TelephonyManager) getSystemService(ContactListDetailActivity.this.TELEPHONY_SERVICE);
					int number = manager.getPhoneType();

					// 号码为空或未平板设备，按钮图为灰色图，并不可点击，否则可点击
					if (null == mobilePhone || "".equals(mobilePhone)
							|| number != 1) {
						ivCall.setImageResource(R.drawable.no_call_phone);
						ivSMS.setImageResource(R.drawable.no_sms);
					} else {

						// 添加发信息和打电话点击事件
						ivCall.setOnClickListener(ContactListDetailActivity.this);
						ivSMS.setOnClickListener(ContactListDetailActivity.this);
					}

					tvMobilePhone.setText(contactInfo.getMobilePhone());
					tvEmail.setText(contactInfo.getEmail());
					tvUnitName.setText(contactInfo.getDeptName());
					tvDuty.setText(contactInfo.getDuty());
					tvUnitPhone.setText(contactInfo.getDeptPhone());
					tvUnitAddress.setText(contactInfo.getWorkAddress());

					if (type == 3) { // type != 1
						otherLayout.setVisibility(View.GONE);
					} else {

						tvHomeAddress.setText(contactInfo.getHomeAddress());
						tvHomePhone.setText(contactInfo.getHomePhone());
					}
				}
				break;
			}
		}
	};

	@Override
	public void onClick(View v) {
		String phone_number = tvMobilePhone.getText().toString();
		phone_number = phone_number.trim();// 删除字符串首部和尾部的空格
		switch (v.getId()) {
		case R.id.iv_mobile_phone: // 打电话

			// 调用系统的拨号服务实现电话拨打功能
			if (phone_number != null && !phone_number.equals("")) {

				// 调用系统的拨号服务实现电话拨打功能
				// 封装一个拨打电话的intent，并且将电话号码包装成一个Uri对象传入
				Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:"
						+ phone_number));
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(intent);// 内部类
			}
			break;

		case R.id.iv_sms: // 发短信
			if (phone_number != null && !phone_number.equals("")) {

				Uri uri = Uri.parse("smsto:" + phone_number);
				Intent it = new Intent(Intent.ACTION_SENDTO, uri);
				ContactListDetailActivity.this.startActivity(it);
			}
			break;
		case R.id.delete_contact_button:
			myDialog = new AlertDialog.Builder(ContactListDetailActivity.this)
					.create();
			myDialog.show();
			myDialog.getWindow().setContentView(R.layout.delete_alter_dialog);
			TextView textView = (TextView) myDialog.getWindow().findViewById(
					R.id.tv_title);
			textView.setText("是否删除该联系人？");
			myDialog.getWindow().findViewById(R.id.ok_button)
					.setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							myDialog.dismiss();
							contactListBiz.deleteContactList(contactInfo);
							DialogUtil.showDialog(progressDialog, "数据删除中...",
									false);
						}
					});
			myDialog.getWindow().findViewById(R.id.cancel_button)
					.setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							myDialog.dismiss();
						}
					});
			break;
		case R.id.topbar_back_ibtn: // 返回
			if (isUpdate) {
				Intent intent = new Intent();
				intent.putExtra("isUpdate", isUpdate);
				setResult(RESULT_OK, intent);
				finish();
			} else {
				finish();
			}
			break;
		case R.id.topbar_right_tv: // 编辑
			Intent intent = new Intent(this, AddOrUpdateContactActivity.class);
			Bundle bundle = new Bundle();
			bundle.putSerializable("contactInfo", contactInfo);
			intent.putExtras(bundle);
			startActivityForResult(intent, REQUEST_CODE);
			break;
		}
	}

	/**
	 * 编辑成功返回
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == ContactListActivity.REQUSET_CODE
				&& resultCode == RESULT_OK) {
			isUpdate = true;
			contactInfo = (ContactInfo) data
					.getSerializableExtra("contactInfo");
			tvUserName.setText(contactInfo.getContactName());
			tvSex.setText(contactInfo.getSex());
			tvMobilePhone.setText(contactInfo.getMobilePhone());
			tvEmail.setText(contactInfo.getEmail());
			tvUnitName.setText(contactInfo.getDeptName());
			tvDuty.setText(contactInfo.getDuty());
			tvUnitPhone.setText(contactInfo.getDeptPhone());
			tvUnitAddress.setText(contactInfo.getWorkAddress());
			tvHomeAddress.setText(contactInfo.getHomeAddress());
			tvHomePhone.setText(contactInfo.getHomePhone());

		}
	}

}
