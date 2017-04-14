package com.gta.scpoa.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.gta.db.annotation.ViewInject;
import com.gta.scpoa.R;
import com.gta.scpoa.adapter.ContactListViewAdapter;
import com.gta.scpoa.biz.ContactListBiz;
import com.gta.scpoa.biz.impl.ContactListBizImpl;
import com.gta.scpoa.entity.ContactInfo;
import com.gta.scpoa.util.CharacterParser;
import com.gta.scpoa.util.DialogUtil;
import com.gta.scpoa.util.PinyinComparator;
import com.gta.scpoa.views.CommonBottomView;
import com.gta.scpoa.views.CommonTopView;
import com.gta.scpoa.views.LetterSideBar;
import com.gta.scpoa.views.LetterSideBar.OnTouchingLetterChangedListener;
import com.gta.scpoa.views.SearchEditText;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 *     へ　　　　　／|
 * 　　/＼7　　  ∠＿/
 * 　 /　│　　 ／　／
 * 　│　Z ＿,＜　／　　 /`ヽ
 * 　│　　　　　ヽ　　 /　　〉
 * 　 Y　　　　　`　 /　　/
 * 　ｲ●　､　●　　⊂⊃〈　　/
 * 　()　 へ　　　　|　＼〈
 * 　　>ｰ ､_　 ィ　 │ ／／
 * 　 / へ　　 /　ﾉ＜| ＼＼
 * 　 ヽ_ﾉ　　(_／　 │／／
 * 　　7　　　　　　　|／
 * 　　＞―r￣￣`ｰ―＿
 *
 *     去吧,皮卡丘!!!
 */

/**
 * 通讯录
 * 
 * @author shengping.pan
 * 
 */
public class ContactListActivity extends BaseActivity implements
		OnClickListener {

	/**
	 * 个人tab
	 */
	@ViewInject(id = R.id.oneself_tab)
	private TextView oneselfTab = null;

	/**
	 * 教职tab
	 */
	@ViewInject(id = R.id.teacher_tab)
	private TextView teacherTab = null;

	/**
	 * 公用tab
	 */
	@ViewInject(id = R.id.common_tab)
	private TextView commonTab = null;

	/**
	 * 联系人业务处理接口
	 */
	private ContactListBiz contactListBiz = null;

	/**
	 * 公共顶部布局
	 */
	@ViewInject(id = R.id.contact_top_view)
	private CommonTopView contactTopView = null;

	/**
	 * 通讯录listView
	 */
	@ViewInject(id = R.id.contact_list_view)
	private ListView sortListView;

	/**
	 * 右侧字母View
	 */
	@ViewInject(id = R.id.sidrbar)
	private LetterSideBar sideBar;

	/**
	 * 显示字母的dialog
	 */
	@ViewInject(id = R.id.dialog)
	private TextView dialog;

	/**
	 * 通讯录适配器
	 */
	private ContactListViewAdapter adapter;

	/**
	 * 搜索框
	 */
	@ViewInject(id = R.id.filter_edit)
	private SearchEditText mClearEditText;

	/**
	 * 公用底部布局
	 */
	@ViewInject(id = R.id.contact_bottom_view)
	private CommonBottomView commonBottom = null;

	/**
	 * 汉字转换成拼音的类
	 */
	private CharacterParser characterParser;

	/**
	 * 包含首字母的list对象
	 */
	private List<ContactInfo> sourceDateList;

	/**
	 * 根据拼音来排列ListView里面的数据类
	 */
	private PinyinComparator pinyinComparator;

	/**
	 * 通讯录类型
	 */
	private int type = 1;

	/**
	 * 发邮件选择的联系人集合
	 */
	private ArrayList<ContactInfo> checkedContacts = new ArrayList<ContactInfo>();

	/**
	 * 发邮件选择联系人的Id
	 */
	private ArrayList<String> contactIds = null;

	/**
	 * Activity请求码
	 */
	public static final int REQUSET_CODE = 1;

	/**
	 * 进度条
	 */
	private ProgressDialog progressDialog = null;

	/**
	 * 包含单选全选的布局
	 */
	@ViewInject(id = R.id.ll_check)
	private LinearLayout llCheck = null;

	/**
	 * 全选
	 */
	@ViewInject(id = R.id.bt_checkall)
	private Button btCheckAll = null;

	/**
	 * 取消全选
	 */
	@ViewInject(id = R.id.bt_checkcancel)
	private Button btCheckCancel = null;

	@ViewInject(id = R.id.notdata_view)
	private TextView notDataView = null;

	private boolean isBack = false;

	private List<ContactInfo> filterDateList = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_contact_list);

		// 业务处理接口
		if (null == contactListBiz) {
			contactListBiz = new ContactListBizImpl(this, handler);
		}

		// 适配器初始化
		if (null == adapter) {
			adapter = new ContactListViewAdapter(this);
		}

		// 用于发邮件选择教职类通讯录信息
		Intent intent = getIntent();
		contactIds = intent.getStringArrayListExtra("contactIds");

		// 初始化view
		initView();

		if (null == progressDialog) {
			progressDialog = new ProgressDialog(this);
		}

		// 根据不同入口获取相应数据
		if (null == contactIds) { // 主界面入口
			contactListBiz.getContactList(1);
		} else { // 发邮件选择教职类通讯录入口
			contactListBiz.getContactList(4);
		}

		DialogUtil.showDialog(progressDialog, "数据加载中...", false);
	}

	/*
	 * 加载的数据进行UI处理
	 */
	private Handler handler = new Handler() {

		@SuppressWarnings("unchecked")
		@Override
		public void handleMessage(Message msg) {

			if (!isBack) {
				if (progressDialog != null) {
					progressDialog.dismiss();
				}
				switch (msg.what) {
				case 0: // 失败处理
					notDataView.setVisibility(View.VISIBLE);
					Toast.makeText(ContactListActivity.this,
							msg.obj.toString(), Toast.LENGTH_SHORT).show();
					adapter.setData(new ArrayList<ContactInfo>(), null);
					sortListView.setAdapter(adapter);
					break;
				case 1: // 成功处理
					List<ContactInfo> list = (List<ContactInfo>) msg.obj;

					if (list.isEmpty()) {
						notDataView.setVisibility(View.VISIBLE);
					} else {
						notDataView.setVisibility(View.GONE);
					}

					// 填充首字母
					sourceDateList = filledData(list);

					// 根据a-z进行排序源数据
					Collections.sort(sourceDateList, pinyinComparator);

					if (null == contactIds) { // 通讯录主界面的数据加载处理
						adapter.setData(sourceDateList, null);
					} else { // 发邮件时选择的教职类通讯录处理

						// 把之前选择的联系人信息保存到返回的结果中
						for (String id : contactIds) {
							for (ContactInfo contactInfo : sourceDateList) {
								String userId = String.valueOf(contactInfo
										.getUserId());
								if (id.equals(userId)) {
									contactInfo.setSelected(true);
									checkedContacts.add(contactInfo);
								}
							}
						}
						adapter.setData(sourceDateList, contactIds);
					}
					sortListView.setAdapter(adapter);
					break;
				}
			}
		}
	};

	/**
	 * 初始化View
	 */
	private void initView() {
		// 设置顶部布局
		contactTopView.setBackImageButtonEnable(true);
		contactTopView.setBackImageButtonOnClickListener(this); // 左上角返回按钮监听
		if (null != contactIds) {
			contactTopView.setRightTextViewEnable(true);
			contactTopView.setRightTextViewOnClickListener(this);
			contactTopView.setRightTextViewText("确定");
		} else {
			contactTopView.setAddImageButtonEnable(true);
		}
		contactTopView.setTitleTextViewEnable(true);
		contactTopView.setTitleTextViewText(getString(R.string.title_contact));

		// 底部布局
		commonBottom.setIsMainActivity(false);

		// 根据contactIds判断tab显示
		if (null != contactIds) {
			oneselfTab.setVisibility(View.GONE);
			commonTab.setVisibility(View.GONE);

			teacherTab.setBackgroundResource(R.drawable.tab_checked);
			teacherTab.setTextColor(getResources().getColor(R.color.tab_font));

			DisplayMetrics dm = new DisplayMetrics();
			dm = getApplicationContext().getResources().getDisplayMetrics();
			int width = dm.widthPixels;
			LinearLayout.LayoutParams oneselfTab_params = new LinearLayout.LayoutParams(
					LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			oneselfTab_params.width = (int) (width);
			teacherTab.setLayoutParams(oneselfTab_params);

			commonBottom.setVisibility(View.GONE);

			llCheck.setVisibility(View.VISIBLE);
		} else {

			// tab添加点击事件
			oneselfTab.setOnClickListener(this);
			teacherTab.setOnClickListener(this);
			commonTab.setOnClickListener(this);
		}

		// 实例化汉字转拼音类
		characterParser = CharacterParser.getInstance();
		pinyinComparator = new PinyinComparator();
		sideBar.setTextView(dialog);

		// 设置右侧触摸监听
		sideBar.setOnTouchingLetterChangedListener(new OnTouchingLetterChangedListener() {

			@Override
			public void onTouchingLetterChanged(String s) {
				// 该字母首次出现的位置
				int position = adapter.getPositionForSection(s.charAt(0));
				if (position != -1) {
					sortListView.setSelection(position);
				}

			}
		});

		// listView点击事件
		sortListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				ContactInfo contact = (ContactInfo) adapter.getItem(position);
				if (null == contactIds) { // 点击事件
					Intent intent = new Intent(ContactListActivity.this,
							ContactListDetailActivity.class);
					intent.putExtra("contact", contact);
					intent.putExtra("type", type);
					startActivityForResult(intent, REQUSET_CODE);
				} else { // 邮箱选联系人的点击事件

//					ImageView img = (ImageView) view
//							.findViewById(R.id.check_box);

					if (contact.isSelected()) {
						contact.setSelected(false);
						checkedContacts.remove(contact);
					} else {
						contact.setSelected(true);
						checkedContacts.add(contact);
					}
					adapter.notifyDataSetChanged();
				}
			}
		});

		// 根据输入框输入值的改变来过滤搜索
		mClearEditText.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				// 当输入框里面的值为空，更新为原来的列表，否则为过滤数据列表
				filterData(s.toString());
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {

			}

			@Override
			public void afterTextChanged(Editable s) {
			}
		});

		btCheckAll.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				
				// 有搜索的情况全选
				if(!"".equals(mClearEditText.getText().toString())){
					if (contactIds != null && null != filterDateList
							&& filterDateList.size() > 0) {
						
						// 把之前选择的联系人信息保存到返回的结果中
						for (ContactInfo contact : filterDateList) {
							for (ContactInfo contactInfo : sourceDateList) {
								if(contactInfo.equals(contact) && !contactInfo.isSelected()){
									contactInfo.setSelected(true);
									checkedContacts.add(contactInfo);
								}
							}
						}
						adapter.notifyDataSetChanged();
					}
				}else{
					if (contactIds != null && null != sourceDateList
							&& sourceDateList.size() > 0) {

						// 把之前选择的联系人信息保存到返回的结果中
						for (ContactInfo contact : sourceDateList) {
							if (!contact.isSelected()) {
								contact.setSelected(true);
								checkedContacts.add(contact);
							}
						}
						adapter.notifyDataSetChanged();
					}
				}
				
				
			}
		});

		btCheckCancel.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				
				// 有搜索的情况下取消
				if(!"".equals(mClearEditText.getText().toString())){
					if (contactIds != null && null != filterDateList
							&& filterDateList.size() > 0) {
						// 把之前选择的联系人信息保存到返回的结果中
						for (ContactInfo contact : filterDateList) {
							if (contact.isSelected()) {
								contact.setSelected(false);
								checkedContacts.remove(contact);
							}
						}
						adapter.notifyDataSetChanged();
					}
				}else{
					
					if (contactIds != null && null != sourceDateList
							&& sourceDateList.size() > 0) {
						// 把之前选择的联系人信息保存到返回的结果中
						for (ContactInfo contact : sourceDateList) {
							if (contact.isSelected()) {
								contact.setSelected(false);
								checkedContacts.remove(contact);
							}
						}
						adapter.notifyDataSetChanged();
					}
				}
				
			}
		});

	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.oneself_tab: // 个人
			mClearEditText.setText(""); // 搜索框置空
			if (type == 1) {
				break;
			}
			type = 1;
			oneselfTab.setBackgroundResource(R.drawable.tab_checked);
			teacherTab.setBackgroundResource(R.drawable.tab_default);
			commonTab.setBackgroundResource(R.drawable.tab_default);

			oneselfTab.setTextColor(getResources().getColor(R.color.tab_font));
			teacherTab.setTextColor(getResources().getColor(R.color.tab_font1));
			commonTab.setTextColor(getResources().getColor(R.color.tab_font1));

			contactListBiz.getContactList(1);
			DialogUtil.showDialog(progressDialog, "数据加载中...", false);
			break;
		case R.id.teacher_tab: // 教职
			mClearEditText.setText(""); // 搜索框置空
			if (type == 3) {
				break;
			}
			type = 3;
			oneselfTab.setBackgroundResource(R.drawable.tab_default);
			teacherTab.setBackgroundResource(R.drawable.tab_checked);
			commonTab.setBackgroundResource(R.drawable.tab_default);

			oneselfTab.setTextColor(getResources().getColor(R.color.tab_font1));
			teacherTab.setTextColor(getResources().getColor(R.color.tab_font));
			commonTab.setTextColor(getResources().getColor(R.color.tab_font1));

			contactListBiz.getContactList(3);
			DialogUtil.showDialog(progressDialog, "数据加载中...", false);
			break;
		case R.id.common_tab: // 公用
			mClearEditText.setText(""); // 搜索框置空
			if (type == 2) {
				break;
			}
			type = 2;
			oneselfTab.setBackgroundResource(R.drawable.tab_default);
			teacherTab.setBackgroundResource(R.drawable.tab_default);
			commonTab.setBackgroundResource(R.drawable.tab_checked);

			oneselfTab.setTextColor(getResources().getColor(R.color.tab_font1));
			teacherTab.setTextColor(getResources().getColor(R.color.tab_font1));
			commonTab.setTextColor(getResources().getColor(R.color.tab_font));

			contactListBiz.getContactList(2);
			DialogUtil.showDialog(progressDialog, "数据加载中...", false);
			break;
		case R.id.topbar_back_ibtn: // 返回
			finish();
			break;
		case R.id.topbar_right_tv: // 选择联系人确定按钮
			Intent intent = new Intent();
			ArrayList<HashMap<String, String>> tempList  = new ArrayList<HashMap<String,String>>();
			tempList.clear();
			for(ContactInfo contactInfo:checkedContacts){
				HashMap<String, String> map = new HashMap<String, String>();
				map.put("name", contactInfo.getContactName());
				map.put("id", contactInfo.getUserId());
				tempList.add(map);
			}
			intent.putExtra("checkedContacts", tempList);
			setResult(RESULT_OK, intent);
			this.finish();
//			Bundle bundle = new Bundle();
//			bundle.putSerializable("checkedContacts",
//					(Serializable) checkedContacts);
//			intent.putExtras(bundle);
//			setResult(RESULT_OK, intent);
//			finish();
			break;
		}
	}

	/**
	 * 为ListView填充数据
	 * 
	 * @param list
	 * @return
	 */
	private List<ContactInfo> filledData(List<ContactInfo> list) {
		List<ContactInfo> mSortList = new ArrayList<ContactInfo>();

		for (int i = 0; i < list.size(); i++) {
			ContactInfo contact = list.get(i);

			// 汉字转换成拼音
			String pinyin = characterParser.getSelling(list.get(i)
					.getContactName());
			String sortString = pinyin.substring(0, 1).toUpperCase();

			// 正则表达式，判断首字母是否是英文字母
			if (sortString.matches("[A-Z]")) {
				contact.setSortLetters(sortString.toUpperCase());
			} else {
				contact.setSortLetters("#");
			}
			mSortList.add(contact);
		}
		return mSortList;
	}

	/**
	 * 根据输入框中的值来过滤数据并更新ListView
	 * 
	 * @param filterStr
	 */
	private void filterData(String filterStr) {
		filterDateList = new ArrayList<ContactInfo>();
		filterStr = filterStr.trim();
		if (TextUtils.isEmpty(filterStr)) {
			filterDateList = sourceDateList;
		} else {
			if (null != sourceDateList) {
				filterDateList.clear();
				for (ContactInfo contact : sourceDateList) {
					String name = contact.getContactName();
					String deptName = contact.getDeptName();
					String mobilePhone = contact.getMobilePhone();

					// 检索姓名或者职位名称及电话号码
					if ((!TextUtils.isEmpty(deptName) && deptName
							.contains(filterStr))
							|| (!TextUtils.isEmpty(mobilePhone) && mobilePhone
									.contains(filterStr))
							|| name.indexOf(filterStr) != -1
							|| characterParser.getSelling(name).startsWith(
									filterStr)
							|| characterParser.getSelling(name)
									.equalsIgnoreCase(filterStr)) {
						filterDateList.add(contact);
					}
				}
			}
		}
		if (filterDateList != null) {
			if (filterDateList.isEmpty()) {
				notDataView.setVisibility(View.VISIBLE);
			} else {
				notDataView.setVisibility(View.GONE);
			}
			// 根据a-z进行排序
			Collections.sort(filterDateList, pinyinComparator);
			adapter.updateListView(filterDateList);
		} else {
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == ContactListActivity.REQUSET_CODE
				&& resultCode == RESULT_OK) {
			if (!data.getBooleanExtra("isUpdate", false)) {
				Toast.makeText(getApplicationContext(), "删除成功!",
						Toast.LENGTH_SHORT).show();
			}
			contactListBiz.getContactList(1);
		}
	}

	@Override
	protected void onDestroy() {
		isBack = true;
		super.onDestroy();
	}
}
