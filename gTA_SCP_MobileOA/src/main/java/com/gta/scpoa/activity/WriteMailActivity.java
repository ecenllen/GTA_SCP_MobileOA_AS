package com.gta.scpoa.activity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Html;
import android.text.InputType;
import android.view.Display;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.gta.db.annotation.ViewInject;
import com.gta.scpoa.R;
import com.gta.scpoa.adapter.AttachAdapter;
import com.gta.scpoa.application.GTAApplication;
import com.gta.scpoa.biz.impl.MailReciveInforBizImpz;
import com.gta.scpoa.biz.impl.MailWriteBizImpl;
import com.gta.scpoa.common.Constant;
import com.gta.scpoa.entity.MailAttachInfo;
import com.gta.scpoa.entity.ReciveMailInfor;
import com.gta.scpoa.util.DialogUtil;
import com.gta.scpoa.util.FileUtils;
import com.gta.scpoa.util.PreferencesUtils;
import com.gta.scpoa.util.StringUtils;
import com.gta.scpoa.util.UIUtils;

@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR1)
public class WriteMailActivity extends BaseActivity implements OnClickListener,
		OnItemClickListener {

	@ViewInject(id = R.id.topbar_title_tv)
	// 标题中间的text
	private TextView tittle_text;

	@ViewInject(id = R.id.topbar_back_ibtn)
	private ImageButton backBtn; // 标题返回按键

	@ViewInject(id = R.id.topbar_logo_iv)
	private ImageView hideImage; // 标题需要隐藏的图片

	@ViewInject(id = R.id.topbar_add_ibtn)
	private ImageButton hideBtn; // 标题要隐藏的按键

	@ViewInject(id = R.id.topbar_right_tv)
	private TextView topbar_right_tv; // 标题右边的textview

	@ViewInject(id = R.id.write_mail_reciver_edit)
	// 收件人
	private TextView reciver_edit;

	@ViewInject(id = R.id.write_mail_copyer_edit)
	// 抄送人
	private TextView copyer_edit;

	@ViewInject(id = R.id.write_mail_Secretor_edit)
	// 密送人
	private TextView secretor_edit;

	@ViewInject(id = R.id.add_reciver_image)
	private ImageView add_reciver_image; // 收件人添加

	@ViewInject(id = R.id.add_copyer_image)
	private ImageView add_copyer_image; // 抄送人添加

	@ViewInject(id = R.id.add_Secretor_image)
	private ImageView add_Secretor_image; // 密送人添加

	@ViewInject(id = R.id.write_mail_subject_edit)
	// 主题
	private EditText subject_edit;

	@ViewInject(id = R.id.add_attach_file)
	private ImageView attactFile = null; // 添加附件为文件

	@ViewInject(id = R.id.add_attach_image)
	private ImageView attachImage = null; // 附件为图片

	@ViewInject(id = R.id.write_mail_send_btn)
	// 发送按键
	private Button sendBtn = null;

	@ViewInject(id = R.id.write_mail_edit_layout)
	// 写邮件的时候的编辑主要放在这个里面
	private RelativeLayout write_mail_edit_layout = null;

	@ViewInject(id = R.id.write_mail_attach_listView)
	// 附件列表
	private ListView listView = null;

	@ViewInject(id = R.id.file_size_text)
	// 附件个数和大小
	private TextView Num_size_text = null;

	private AlertDialog myDialog = null;
	private List<HashMap<String, String>> reciverLists = new ArrayList<HashMap<String, String>>();
	private List<HashMap<String, String>> coperLists = new ArrayList<HashMap<String, String>>();
	private List<HashMap<String, String>> secretorLists = new ArrayList<HashMap<String, String>>();

	private EditText writeEdit = null; // 正文文本框
	private WebView webView = null;
	private AttachAdapter adapter = null; // 附件适配器
	private List<MailAttachInfo> listMap = new ArrayList<MailAttachInfo>(); // 邮件存放的list
	private int fileNum = 0; // 文件个数
	private long fileSize = 0; // 文件大小
	private int sendSatue = -1;// 0为新建邮件 1为快速恢复 2为发件箱查看 3为草稿箱查看
	private String myMessageIdString = ""; // 邮件ID 用于本地存到数据库的时候 新建和回复的时候
											// 使用当前时间作为Id
	private MailWriteBizImpl mailWriteBizImpl = new MailWriteBizImpl(this);
	private ReciveMailInfor mailInfor = new ReciveMailInfor();// 邮件信息
	private ProgressDialog mProgressDialog = null;
	private String saveAddString = "收件人"; // 记录当前按下添加按键的image
	private final static int SEND_MAIL = 100001;

	@Override
	protected void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		setContentView(R.layout.write_mail_layout);
		viewInit(); // 初始化所有控件
		dataInit(); // 初始化一些数据
	}

	/* 标题部分初始化 */
	private void titteleInit() {
		Bundle bundle = this.getIntent().getExtras();
		sendSatue = bundle.getInt(Constant.SendStatue);
		tittle_text.setVisibility(View.VISIBLE);
		backBtn.setVisibility(View.VISIBLE);
		hideImage.setVisibility(View.GONE);
		hideBtn.setVisibility(View.GONE);
		topbar_right_tv.setOnClickListener(this);
		backBtn.setOnClickListener(this);
		if (sendSatue == Constant.Send_draftMail) {// 草稿箱
		// topbar_right_tv.setVisibility(View.VISIBLE);
		// topbar_right_tv.setText("编辑");
			tittle_text.setText("草稿箱");
		} else if (sendSatue == Constant.Send_FastRepaly) { // 回复
			tittle_text.setText("回复");
		} else if (sendSatue == Constant.Send_NewMail) { // 新建
			tittle_text.setText("新建邮件");
		} else if (sendSatue == Constant.Send_SendMail) { // 发件箱
			tittle_text.setText("发件箱");
		}
	}

	/* 邮件body部分初始化 */
	@SuppressWarnings("deprecation")
	private void bodyInit() {
		Bundle bundle = this.getIntent().getExtras();
		sendSatue = bundle.getInt(Constant.SendStatue);

		add_reciver_image.setOnClickListener(this);
		add_copyer_image.setOnClickListener(this);
		add_Secretor_image.setOnClickListener(this);

		Display display = getWindowManager().getDefaultDisplay();
		int height = display.getHeight();
		int width = display.getWidth();

		RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
				width, height / 3);
		RelativeLayout relativeLayout = new RelativeLayout(this);
		relativeLayout.setLayoutParams(layoutParams);
		write_mail_edit_layout.addView(relativeLayout);

		RelativeLayout.LayoutParams editLayoutParams = new RelativeLayout.LayoutParams(
				width, height / 3);
		int margin = width / 25;
		editLayoutParams.leftMargin = margin;
		editLayoutParams.rightMargin = margin;
		editLayoutParams.bottomMargin = margin;
		editLayoutParams.topMargin = margin;
		writeEdit = new EditText(this);
		writeEdit.setLayoutParams(editLayoutParams);
		writeEdit.setBackgroundResource(R.drawable.edit_bg);
		writeEdit.setGravity(Gravity.TOP | Gravity.LEFT);

		webView = new WebView(this);
		// hardwareAccelerated
		webView.setLayoutParams(editLayoutParams);

		relativeLayout.addView(writeEdit);
		relativeLayout.addView(webView);

		if (sendSatue == Constant.Send_draftMail) {// 草稿箱
			writeEdit.setVisibility(View.VISIBLE);
			webView.setVisibility(View.GONE);
		} else if (sendSatue == Constant.Send_FastRepaly) { // 回复
			webView.setVisibility(View.GONE);
		} else if (sendSatue == Constant.Send_NewMail) { // 新建
			webView.setVisibility(View.GONE);
		} else if (sendSatue == Constant.Send_SendMail) { // 发件箱
			add_reciver_image.setVisibility(View.GONE);
			add_copyer_image.setVisibility(View.GONE);
			add_Secretor_image.setVisibility(View.GONE);
			subject_edit.setInputType(InputType.TYPE_NULL);
			writeEdit.setVisibility(View.GONE);
		}
	}

	/* 附件部分初始化 */
	private void attachViewInit() {
		Bundle bundle = this.getIntent().getExtras();
		sendSatue = bundle.getInt(Constant.SendStatue);
		attactFile.setOnClickListener(this);
		attachImage.setOnClickListener(this);
		attactFile.setVisibility(View.GONE); // 隐藏 添加附件的模块
		attachImage.setVisibility(View.GONE); // 隐藏 添加附件的模块

		if (sendSatue == Constant.Send_draftMail) {// 草稿箱

		} else if (sendSatue == Constant.Send_FastRepaly) { // 回复
		// listView.setVisibility(View.GONE);
		} else if (sendSatue == Constant.Send_NewMail) { // 新建
			listView.setVisibility(View.GONE);
		} else if (sendSatue == Constant.Send_SendMail) { // 发件箱

		}
		adapter = new AttachAdapter(this, listMap, false, mUIHandler);
		listView.setAdapter(adapter);
		adapter.notifyDataSetChanged();
		listView.setOnItemClickListener(this);
	}

	/* 发送按键初始化 */
	private void sendButtonInit() {
		Bundle bundle = this.getIntent().getExtras();
		sendSatue = bundle.getInt(Constant.SendStatue);
		sendBtn.setOnClickListener(this); // 发送按键

		if (sendSatue == Constant.Send_draftMail) {// 草稿箱

		} else if (sendSatue == Constant.Send_FastRepaly) { // 回复

		} else if (sendSatue == Constant.Send_NewMail) { // 新建

		} else if (sendSatue == Constant.Send_SendMail) { // 发件箱
			sendBtn.setVisibility(View.GONE);
		}
	}

	private void viewInit() {
		titteleInit();
		progressInit(); // 初始化滚动框
		bodyInit();
		attachViewInit();
		sendButtonInit();
	}

	@SuppressWarnings("unchecked")
	private void dataInit() {
		Bundle bundle = this.getIntent().getExtras();
		sendSatue = bundle.getInt(Constant.SendStatue);
		String outBoxTheme;
		String id;
		if (sendSatue == Constant.Send_draftMail
				|| sendSatue == Constant.Send_SendMail) {
			/* 请求网络数据 */
			setProgressShow("加载数据中...");
			outBoxTheme = bundle.getString("outBoxTheme", "");
			id = bundle.getString("id", "-1");
			mailInfor.setId(id);
			subject_edit.setText(outBoxTheme);
			if (sendSatue == Constant.Send_SendMail) {
				new MailReciveInforBizImpz(this).getMailReciveInfor(id, 2,
						mUIHandler);
			} else { // 草稿箱
				new MailReciveInforBizImpz(this).getMailReciveInfor(id, 3,
						mUIHandler);
			}
		} else if (sendSatue == Constant.Send_NewMail) {

		} else if (sendSatue == Constant.Send_FastRepaly) { // 收件箱的回复过来的时候
			String reciverName = bundle.getString("reciveName", ""); // 写邮件的 收件人
			showReciver(reciverName);
			String copyerName = bundle.getString("OutBoxCopyer", ""); // 写邮件的抄送人
			showCopyer(copyerName);
			String secretName = bundle.getString("OutBoxSecret", ""); // 写邮件的密送人
			showSecretor(secretName);
			mailInfor.setOutBoxTheme(bundle.getString("OutBoxTheme", "")); // 主题
			mailInfor.setOutBoxContent(bundle.getString("OutBoxContent", ""));
			// 邮件的内容
			List<MailAttachInfo> tempList = new ArrayList<MailAttachInfo>();
			tempList = (List<MailAttachInfo>) bundle
					.getSerializable("AttachList");
			listMap.addAll(tempList);
			adapter.notifyDataSetChanged();
			subject_edit.setText(mailInfor.getOutBoxTheme());
			writeEdit.setText(mailInfor.getOutBoxContent()); // 正文文本
		}

	}

	private void showPeople(List<HashMap<String, String>> tempList,
			TextView editText) {
		editText.setText("");
		String names = "";
		for (HashMap<String, String> hashMap : tempList) {
			names += hashMap.get("name") + ",";
		}
		if (names.length() > 0) {
			names = names.substring(0, names.length() - 1);
		}
		editText.setText(names);
	}

	/* 收件人显示 */
	private void showReciver(String str) {
		reciverLists.clear();
		reciverLists = StringUtils.explainTheText(str);
		showPeople(reciverLists, reciver_edit);
	}

	/* 抄送人显示 */
	private void showCopyer(String str) {
		coperLists.clear();
		coperLists = StringUtils.explainTheText(str);
		showPeople(coperLists, copyer_edit);
	}

	/* 密送人显示 */
	private void showSecretor(String str) {
		secretorLists.clear();
		secretorLists = StringUtils.explainTheText(str);
		showPeople(secretorLists, secretor_edit);
	}

	/* 显示滚动条 */
	private void progressInit() {
		mProgressDialog = new ProgressDialog(this);
	}

	/* 计算文件的大小 */
	private void allFileSize() {
		fileNum = listMap.size();
		fileSize = 0;
		for (int i = 0; i < fileNum; i++) {
			fileSize = fileSize + listMap.get(i).getFileSize();
		}

		// if(sendSatue !=
		// Constant.Send_draftMail&&sendSatue!=Constant.Send_NewMail){
		// //发件箱和草稿箱不处理
		// if(fileSize>Constant.allAttachSize){
		// UIUtils.ToastMessage(this, "附件总大小不能超过12M");
		// listMap.remove(listMap.size()-1);
		// allFileSize();
		// }
		// }

		String fileSizeString = FileUtils.FormetFileSize(fileSize);
		Num_size_text.setText("(" + fileNum + "个文件," + fileSizeString + ")");
	}

	/********************** 按下标题发送按键的时候 ************************/
	private void goSendBtn() {
		/* 判断是否有空数据的存在 */
		// if (!mailWriteBizImpl.defineMailCanSend(reciver_edit, copyer_edit,
		// secretor_edit, subject_edit, fileSize)) {
		// return;
		// }
		/* 获取当前时间 */
		if (myMessageIdString == null || myMessageIdString.equals("")) {
			myMessageIdString = StringUtils.getCurrentData();
		}
		/* 发送邮件 */
		/* 这里ID不要赋值 */
		mailInfor.setAttachLists(listMap);
		mailInfor.setCreateTime(myMessageIdString);
		mailInfor.setIsreturnReceipt(false);
		mailInfor.setOutBoxCopyer(copyString);
		mailInfor.setOutBoxSecret(seString);
		mailInfor.setReceiverUsers(reString);
		// mailInfor.setOutBoxTheme(subject_edit.getText().toString());
		// mailInfor.setOutBoxContent(writeEdit.getText().toString());
		GTAApplication app = (GTAApplication) getApplicationContext();
		mailInfor.setUserName(app.getUserName());
		if (sendSatue == Constant.Send_draftMail) {// 草稿箱的情况
			mailWriteBizImpl.sendMail(mailInfor, reciverLists, coperLists,
					secretorLists, true, true, mUIHandler);
		} else {
			mailWriteBizImpl.sendMail(mailInfor, reciverLists, coperLists,
					secretorLists, false, true, mUIHandler);
		}

	}

	/***************************************************/

	@SuppressWarnings("unchecked")
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == Activity.RESULT_OK) {
			switch (requestCode) {
			case 1:// 联系人添加返回
				// ArrayList<ContactInfo> checkedContacts =
				// (ArrayList<ContactInfo>) data
				// .getSerializableExtra("checkedContacts");
				// ArrayList<HashMap<String, String>> tempList = new
				// ArrayList<HashMap<String,String>>();
				// for(ContactInfo contactInfo:checkedContacts){
				// HashMap<String, String> map = new HashMap<String, String>();
				// map.put("name", contactInfo.getContactName());
				// map.put("id", contactInfo.getUserId());
				// tempList.add(map);
				// }

				ArrayList<HashMap<String, String>> tempList = (ArrayList<HashMap<String, String>>) data
						.getSerializableExtra("checkedContacts");
				if (saveAddString.equals("收件人")) {
					reciverLists.clear();
					reciverLists.addAll(tempList);
					showPeople(reciverLists, reciver_edit); // 显示
				} else if (saveAddString.equals("抄送人")) {
					coperLists.clear();
					coperLists.addAll(tempList); // 显示
					showPeople(coperLists, copyer_edit);
				} else if (saveAddString.equals("密送人")) {
					secretorLists.clear();
					secretorLists.addAll(tempList);
					showPeople(secretorLists, secretor_edit);// 显示
				}

				break;
			case 10: // FILE_SELECT_CODE 附件添加
				// addAttachFile(data); //添加附件 + 图片
				break;
			default:
				break;
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	private void goCaoGaoBianJi() { // 草稿箱编辑
		/* 变成能添加 */
		add_reciver_image.setEnabled(true);
		add_copyer_image.setEnabled(true);
		add_Secretor_image.setEnabled(true);
		/* 主题能编辑 */
		subject_edit.setInputType(InputType.TYPE_CLASS_TEXT);
		/* 内容能编辑 */
		webView.setVisibility(View.GONE);
		writeEdit.setVisibility(View.VISIBLE);
		writeEdit.setText(Html.fromHtml(mailInfor.getOutBoxContent())
				.toString());
		sendBtn.setVisibility(View.VISIBLE);
		/* 附件可发送 */
	}

	private void showSaveCaoGaoDialog() {
		myDialog = new AlertDialog.Builder(this).create();
		myDialog.show();
		myDialog.getWindow().setContentView(R.layout.delete_alter_dialog);
		TextView textView = (TextView) myDialog.getWindow().findViewById(
				R.id.tv_title);
		textView.setText("是否保存草稿?");
		Button ok_button = (Button) myDialog.getWindow().findViewById(
				R.id.ok_button);
		ok_button.setText("确定");
		ok_button.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (writeEdit.getVisibility() == View.GONE) {
					goFinishActivity(false);
					return;
				}

				setProgressShow("保存草稿中...");
				mailInfor.setOutBoxTheme(subject_edit.getText().toString());
				mailInfor.setOutBoxContent(writeEdit.getText().toString());
				new Thread(new Runnable() {
					@Override
					public void run() {
						/* 数据处理 */
						mailInfor.setAttachLists(listMap);
						mailInfor.setIsreturnReceipt(false);
						mailInfor.setOutBoxCopyer(copyString);
						mailInfor.setOutBoxSecret(seString);
						mailInfor.setReceiverUsers(reString);
						GTAApplication app = (GTAApplication) getApplicationContext();
						mailInfor.setUserName(app.getUserName());
						if (sendSatue == Constant.Send_draftMail) {// 草稿箱的情况
							mailWriteBizImpl.sendMail(mailInfor, reciverLists,
									coperLists, secretorLists, true, false,
									mUIHandler);
						} else {
							mailWriteBizImpl.sendMail(mailInfor, reciverLists,
									coperLists, secretorLists, false, false,
									mUIHandler);
						}
					}
				}).start();

				myDialog.dismiss();
			}
		});
		Button cancelButton = (Button) myDialog.getWindow().findViewById(
				R.id.cancel_button);
		cancelButton.setText("取消");
		cancelButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				goFinishActivity(false);
				myDialog.dismiss();
			}
		});
	}

	private void goBackButton() { // 返回按键
		if (sendSatue == Constant.Send_SendMail) { // 发件箱 直接返回
			goFinishActivity(false);
		} else {
			showSaveCaoGaoDialog();
		}
	}

	private void goAddReciverImage() { // 收件人添加
		saveAddString = "收件人";
		goContactListActivity();
	}

	private void goAddCopyerImage() { // 抄送人添加
		saveAddString = "抄送人";
		goContactListActivity();
	}

	private void goAddSecretorImage() { // 密送人添加
		saveAddString = "密送人";
		goContactListActivity();
	}

	/* 获取邮件列表的界面 */
	private void goContactListActivity() {
		Intent intent = new Intent();
		intent.setClass(getApplicationContext(), ContactListActivity.class);
		List<HashMap<String, String>> listMaps = new ArrayList<HashMap<String, String>>();
		if (saveAddString.equals("收件人")) {
			listMaps = reciverLists;
		} else if (saveAddString.equals("抄送人")) {
			listMaps = coperLists;
		} else if (saveAddString.equals("密送人")) {
			listMaps = secretorLists;
		}

		ArrayList<String> list = new ArrayList<String>();
		if (listMaps.size() == 0) {

		} else {
			for (int i = 0; i < listMaps.size(); i++) {
				list.add(listMaps.get(i).get("id"));
			}
		}
		intent.putExtra("contactIds", list);
		startActivityForResult(intent, 1);
	}

	private String reString = "";
	private String copyString = "";
	private String seString = "";

	@Override
	public void onClick(View v) {
		int id = v.getId();
		switch (id) {
		case R.id.add_attach_file: // 添加附件为文件的情况
			mailWriteBizImpl.goShowAttachFile(false);
			break;
		case R.id.add_attach_image: // 添加附件为图片的情况
			mailWriteBizImpl.goShowAttachFile(true);
			break;
		case R.id.write_mail_send_btn: // 发送
			/* 判断是否有空数据的存在 */
			if (!mailWriteBizImpl.defineMailCanSend(reciver_edit, copyer_edit,
					secretor_edit, subject_edit, fileSize)) {
				return;
			}
			/* 这里ID不要赋值 */
			setProgressShow("发送邮件中...");
			mailInfor.setOutBoxTheme(subject_edit.getText().toString());
			mailInfor.setOutBoxContent(writeEdit.getText().toString());
			new Thread(new Runnable() {
				@Override
				public void run() {
					/* 数据处理 */
					// reString = "";
					// for (HashMap<String, String> hashMap : reciverLists) {
					// reString += hashMap.get("name")+",";
					// }
					// if(reString.length() > 0){
					// reString = reString.substring(0,reString.length()-1);
					// }
					//
					// copyString = "";
					// for (HashMap<String, String> hashMap : coperLists) {
					// copyString += hashMap.get("name")+",";
					// }
					// if(copyString.length() > 0){
					// copyString =
					// copyString.substring(0,copyString.length()-1);
					// }
					//
					// seString = "";
					// for (HashMap<String, String> hashMap : secretorLists) {
					// seString += hashMap.get("name")+",";
					// }
					// if(seString.length() > 0){
					// seString = seString.substring(0,seString.length()-1);
					// }
					goSendBtn(); // 不操作UI
					// Message msg = mUIHandler.obtainMessage();
					// msg.what = SEND_MAIL;
					// mUIHandler.sendMessageDelayed(msg, 500);
				}
			}).start();
			break;
		case R.id.add_reciver_image: // 收件人添加
			goAddReciverImage();
			break;
		case R.id.add_copyer_image: // 抄送人添加
			goAddCopyerImage();
			break;
		case R.id.add_Secretor_image: // 密送人添加
			goAddSecretorImage();
			break;
		case R.id.topbar_right_tv: // 编辑
			goCaoGaoBianJi();
			break;
		case R.id.topbar_back_ibtn: // 标题返回按键
			goBackButton();
			break;
		default:
			break;
		}
	}

	// /*添加附件*/
	// private void addAttachFile(Intent data){
	// if (null != data) {
	// Uri uri = data.getData();
	// String pathString = uri.getPath();
	// File file = new File(pathString);
	// if (file.length() == 0) {
	// pathString = mailWriteBizImpl.uriToPath(this, uri);
	// if(pathString==null||pathString.equals("")){
	// UIUtils.ToastMessage(this, "请安装文件管理器");
	// return;
	// }
	// }
	// file = new File(pathString);
	// UIUtils.ToastMessage(this, pathString);
	// /* 判断是否有相同的名字 上传附件不允许有相同的名字*/
	// String fileName = file.getName();
	// for (int i = 0; i < listMap.size(); i++) {
	// if (listMap.get(i).get("FileName").equals(fileName)) {
	// return;
	// }
	// }
	// /*判断单个文件的大小*/
	// if(file.length()>Constant.singleAttachSize){
	// UIUtils.ToastMessage(this, "单个文件大小不可以超过6M");
	// return;
	// }
	//
	// HashMap<String, String> map = new HashMap<String, String>();
	// map.put("FileName", file.getName());
	// map.put("FilePath", pathString);
	// map.put("FileSize", file.length() + "");
	// map.put("isFromNet", "0"); //0为false 1为true 来自网络
	// listMap.add(map);
	// allFileSize();
	// adapter.notifyDataSetChanged();
	// }
	// }

	/* 设置滚动框信息 */
	public void setProgressShow(String str) {
		DialogUtil.showDialog(mProgressDialog, str, false);
	}

	/* 设置界面的数据 */
	private void setData(ReciveMailInfor mailInfor) {
		showReciver(mailInfor.getReceiverUsers()); // 接收人
		showCopyer(mailInfor.getOutBoxCopyer()); // 抄送人
		showSecretor(mailInfor.getOutBoxSecret()); // 抄送人
		subject_edit.setText(mailInfor.getOutBoxTheme()); // 主题
		if (sendSatue == Constant.Send_SendMail
				|| sendSatue == Constant.Send_draftMail) {
			webViewInit(mailInfor.getOutBoxContent());
			String str = Html.fromHtml(mailInfor.getOutBoxContent()).toString();
			writeEdit.setText(str);
			listMap.clear();
			listMap.addAll(mailInfor.getAttachLists());
			if (sendSatue == Constant.Send_draftMail) {
				adapter.setShowDown(false);
			} else {
				adapter.setShowDown(false);
			}
			adapter.notifyDataSetChanged();
			allFileSize();
		}

	}

	/* 用于交互处理 */
	@SuppressLint("HandlerLeak")
	private Handler mUIHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			String str = "";
			switch (msg.what) {
			case 0: // 失败
				str = (String) msg.obj;
				if (str != null)
					UIUtils.ToastMessage(getApplicationContext(), str);
				break;
			case 1: // 加载成功
				mailInfor = (ReciveMailInfor) msg.obj;
				if (mailInfor != null) {
					setData(mailInfor);
				}
				break;
			case 2: // 发送成功或者失败
				str = (String) msg.obj;
				if (str != null && !str.equals("")) {
					UIUtils.ToastMessage(getApplicationContext(), str);
					goFinishActivity(true);
				} else {
					UIUtils.ToastMessage(getApplicationContext(), "网络异常,操作失败!");
				}
				break;
			case 10: // 适配器里面点击下载
				int position = (Integer) msg.obj;
				// showIsDownLoadDialog(position);
				return;
			case Constant.AttACH_LOAD_SUCCESS: // 下载附件成功
				int position1 = (Integer) msg.obj;
				// openTheFile(position1);
				UIUtils.ToastMessage(getApplicationContext(), "下载到  "
						+ FileUtils.getSDPath() + Constant.downLoadPath);
				break;
			case SEND_MAIL:
				goSendBtn();
				break;
			default:
				break;
			}
			mProgressDialog.dismiss();
		}
	};

	private void webViewInit(String htmlString) {
		if (htmlString != null && !htmlString.equals("<br>")) {
			webView.getSettings().setDefaultTextEncodingName("UTF-8"); // 注意编码
			webView.loadData(htmlString, "text/html; charset=UTF-8", null);// 这种写法可以正确解码
			webView.getSettings().setBuiltInZoomControls(true);
			webView.setWebViewClient(new HelloWebViewClient());
			webView.setBackgroundColor(Color.WHITE);
		}
	}

	// Web视图
	private class HelloWebViewClient extends WebViewClient {
		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url) {
			view.loadUrl(url);
			return true;
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		if (sendSatue == Constant.Send_SendMail
				|| sendSatue == Constant.Send_draftMail) { // 只有发件箱和草稿箱能下载
			new MailReciveInforBizImpz(this).downMailAttachFile(listMap
					.get(position));
		}
	}

	/* 返回 */
	private void goFinishActivity(boolean isReLoad) {
		if (isReLoad) {
			PreferencesUtils.putBoolean(this, Constant.RE_LAOD, true);
		} else { // 直接返回
			PreferencesUtils.putBoolean(this, Constant.RE_LAOD, false);
		}
		this.finish();
	}

	/* 返回键的捕捉 */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			goBackButton();
			return false;
		}
		return true;
	}
}
