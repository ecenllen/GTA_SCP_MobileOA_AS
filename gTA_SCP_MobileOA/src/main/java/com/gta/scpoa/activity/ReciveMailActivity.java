package com.gta.scpoa.activity;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Html;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.gta.db.annotation.ViewInject;
import com.gta.scpoa.R;
import com.gta.scpoa.adapter.AttachAdapter;
import com.gta.scpoa.biz.impl.MailInforBizImpl;
import com.gta.scpoa.biz.impl.MailReciveInforBizImpz;
import com.gta.scpoa.common.Constant;
import com.gta.scpoa.entity.MailAttachInfo;
import com.gta.scpoa.entity.MailInfor;
import com.gta.scpoa.entity.ReciveMailInfor;
import com.gta.scpoa.util.DialogUtil;
import com.gta.scpoa.util.FileUtils;
import com.gta.scpoa.util.PreferencesUtils;
import com.gta.scpoa.util.StringUtils;
import com.gta.scpoa.util.UIUtils;
import com.gta.scpoa.views.myListView;
import com.gta.util.ViewInjector;

import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@SuppressLint("HandlerLeak")
@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR1)
public class ReciveMailActivity extends BaseActivity implements OnClickListener, OnItemClickListener{
	@ViewInject(id = R.id.topbar_title_tv)  //标题中间的text
	private TextView tittle_text;
	
	@ViewInject(id = R.id.topbar_back_ibtn)
	private ImageButton backBtn;		//标题返回按键
	
	@ViewInject(id = R.id.topbar_logo_iv)
	private ImageView hideImage;		//标题需要隐藏的图片
	
	@ViewInject(id = R.id.topbar_right_tv)
	private TextView reSendText;		//标题回复按键
	
	@ViewInject(id = R.id.reciveWebView)
	private WebView reciveWebView = null;  //正文内容显示
	
	@ViewInject(id = R.id.subjectText)
	private TextView subjectText = null;  //主题
	
	@ViewInject(id = R.id.writerText)
	private TextView writerText = null;  //发件人
	
	@ViewInject(id = R.id.reciverText)
	private TextView reciverText = null;  //收件人
	
	@ViewInject(id = R.id.copyerText)
	private TextView copyerText = null;  //抄件人
	
	@ViewInject(id = R.id.writeTimeText)
	private TextView writeTimeText = null;  //时间
	
	@ViewInject(id = R.id.recive_file_size_text)
	private TextView Num_size_text = null;  //文件大小显示
	
	@ViewInject(id = R.id.recive_attach_listview)
	private myListView listview = null;  //用于显示附件
	
	@ViewInject(id = R.id.recive_delete_btn)   //回复or发送按键
	private Button deleteBtn = null;
	/*适配器*/
	private AttachAdapter adapter = null;
	/*附件信息*/
	private List<MailAttachInfo> listMap = new ArrayList<MailAttachInfo>(); //邮件存放的list
	private AlertDialog myDialog = null;
	private ProgressDialog mProgressDialog= null; 
	private ReciveMailInfor reciveMailInfor;
	private int mailType =1;   //邮件类型请求
	private boolean isRecycle;   //是否回收站标志
	private boolean isRead ;  //邮件是不是读过的
	
	private boolean isBackOnReLoad = false; //返回的时候是不是要reload
	/*发件人信息   name 名字       id  表示发件人id*/
	private List<HashMap<String, String>> senderLists = new ArrayList<HashMap<String,String>>();
	private List<HashMap<String, String>> reciverLists = new ArrayList<HashMap<String,String>>();
	private List<HashMap<String, String>> coperLists = new ArrayList<HashMap<String,String>>();
	private UIHandler mUIHandler;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.recive_mail_layout);
		ViewInjector.getInstance().inJectAll(this);
		mUIHandler = new UIHandler(this);
		viewInit();   //控件初始化
		dataInit();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		boolean isReload = PreferencesUtils.getBoolean(this, Constant.RE_LAOD,
				false);
		if(isReload){
			isBackOnReLoad = true;
		}
	}
	
	private void tittle_Init(){
		Bundle bundle = this.getIntent().getExtras();
		isRecycle = bundle.getBoolean("isRecycle",false); //是否回收站
		
		tittle_text.setVisibility(View.VISIBLE);
		hideImage.setVisibility(View.GONE); //隐藏图片   标题不用的东西
		backBtn.setVisibility(View.VISIBLE);
		
		reSendText.setVisibility(View.VISIBLE);
		if(isRecycle) reSendText.setText("恢复");
		else           reSendText.setText("回复");
		
		if(isRecycle){//回收站
			tittle_text.setText("回收站");
		} else{
			tittle_text.setText("收件箱");
		}
		
		backBtn.setOnClickListener(this);
		reSendText.setOnClickListener(this);
	}
	
	private void progressDialogInit(){
		mProgressDialog = new ProgressDialog(this);
		setProgressShow("加载数据中...");
	}
	
	private void viewInit(){
		tittle_Init();
		progressDialogInit();
		
		deleteBtn.setOnClickListener(this);
		deleteBtn.setVisibility(View.VISIBLE);

		
		/*添加适配器*/
		if(isRecycle){
			adapter = new AttachAdapter(this, listMap,false,mUIHandler);
		}else{
			adapter = new AttachAdapter(this, listMap,false,mUIHandler);
		}
		listview.setAdapter(adapter);
		adapter.notifyDataSetChanged();
		listview.setOnItemClickListener(this);
		
		reciveWebView.getSettings().setDefaultTextEncodingName("UTF-8") ;  //注意编码
		reciveWebView.getSettings().setBuiltInZoomControls(true);
		reciveWebView.setWebViewClient(new HelloWebViewClient());
	}

	/* 一开始初始化 */
	private void dataInit() {
		Bundle bundle = this.getIntent().getExtras();
		String userName = bundle.getString("userName", "123");
		String outBoxTheme = bundle.getString("outBoxTheme", "123");
		String createTime = bundle.getString("createTime", "2015-1-1");
		String id = bundle.getString("id", "0");
		isRead = bundle.getBoolean("isRead",false);
		mailType = bundle.getInt("MailType", mailType);
		writerText.setText(userName);
		subjectText.setText(outBoxTheme);
		writeTimeText.setText(createTime);
		subTheTime(createTime);
		/* 请求网络数据 */
		new MailReciveInforBizImpz(this).getMailReciveInfor(id, mailType,
				mUIHandler);
	}

	
	
	/*分离时间*/
	private void subTheTime(String createTime){
		if(createTime.contains("T")){  
		    int index = createTime.indexOf("T");
		    String temp1 = createTime.substring(0, index);
		    String temp2 = createTime.substring(index+1, createTime.length());
		    writeTimeText.setText(temp1 + " " + temp2 );
		}else{
			writeTimeText.setText(createTime);
		}
	}
	
	/*计算所有文件的大小*/
	private void allFileSize(ReciveMailInfor reciveMailInfor){
		if(reciveMailInfor.getAttachLists().size() == 0){
			return ;
		}
		long allFileSize = 0;
		int fileNum  = reciveMailInfor.getAttachLists().size();
		for(int i = 0; i < fileNum;i ++){
			MailAttachInfo mailAttachInfo = listMap.get(i);
			long fSize = mailAttachInfo.getFileSize(); //获取文件大小
			allFileSize = fSize + allFileSize;
		}
		String fileSizeString  = FileUtils.FormetFileSize(allFileSize);
		Num_size_text.setText("("+fileNum +"个文件,"+fileSizeString+")");
	}
	
	/*设置数据*/
	private void setData(final ReciveMailInfor reciveMailInfor){
		/*主题*/
		subjectText.setText(reciveMailInfor.getOutBoxTheme());
		/*发送人*/
		senderLists = StringUtils.explainTheText(reciveMailInfor.getUserName());
		writerText.setText("");	
		for(int i = 0 ; i < senderLists.size() ; i ++){
			if(i == senderLists.size() -1){
				writerText.append(senderLists.get(i).get("name"));
			}else{
				writerText.append(senderLists.get(i).get("name")+",");
			}
		}
		/*收件人*/
		reciverLists = StringUtils.explainTheText(reciveMailInfor.getReceiverUsers());
		reciverText.setText("");	
		for(int i = 0 ; i < reciverLists.size() ; i ++){
			if(i == reciverLists.size() -1){
				reciverText.append(reciverLists.get(i).get("name"));
			}else{
				reciverText.append(reciverLists.get(i).get("name")+",");
			}
		}
		/*抄送人*/
		coperLists = StringUtils.explainTheText(reciveMailInfor.getOutBoxCopyer());
		copyerText.setText("");	
		for(int i = 0 ; i < coperLists.size() ; i ++){
			if(i == coperLists.size() -1){
				copyerText.append(coperLists.get(i).get("name"));
			}else{
				copyerText.append(coperLists.get(i).get("name")+",");
			}
		}
		/*时间*/
		subTheTime(reciveMailInfor.getCreateTime());
		
		/*未读邮件减少*/
		if(!isRead){
			int unReadCount = PreferencesUtils.getInt(this, 
					Constant.UNREAD_COUNT, 0);
			if(unReadCount <= 0){
				unReadCount =0;
			}else{
				unReadCount = unReadCount -1 ;
			}
			PreferencesUtils.putInt(this, Constant.UNREAD_COUNT, unReadCount);
		}
		
		listMap.clear();
		listMap.addAll(reciveMailInfor.getAttachLists());
		/*计算所有文件的大小*/
		allFileSize(reciveMailInfor);
		adapter.notifyDataSetChanged();
		/*延迟150毫秒加载  决解webView的闪屏问题 */
		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				webViewInit(reciveMailInfor.getOutBoxContent());
			}
		}, 150);
	}
	
	
	/*设置滚动框信息*/
	public void setProgressShow(String str){
		DialogUtil.showDialog(mProgressDialog, str, false);
	}
	
	
	private void webViewInit(String htmlString){
		if (htmlString != null
				&& !htmlString.equals("<br>")) {
			reciveWebView.loadData(htmlString, "text/html; charset=UTF-8", null);//这种写法可以正确解码  
//			UIUtils.ToastMessage(this, htmlString);
//			reciveWebView.loadDataWithBaseURL(null, htmlString,  "text/html", "UTF-8", null);
		}
	}

	
	
	 //Web视图  
    private class HelloWebViewClient extends WebViewClient {  
        @Override 
        public boolean shouldOverrideUrlLoading(WebView view, String url) {  
            view.loadUrl(url);  
            return true;  
        }  
    } 
    
    
    /*用于交互处理*/
	private static final class UIHandler extends Handler{
		WeakReference<ReciveMailActivity> wr;
		public UIHandler(ReciveMailActivity activity) {
			super();
			wr = new WeakReference<ReciveMailActivity>(activity);
			
		}
		@Override
		public void handleMessage(Message msg){
			ReciveMailActivity activity = wr.get();
			if(activity == null)return;
				switch (msg.what) {
				case 0:  //失败
					String str = (String)msg.obj;
					if(str != null)
					UIUtils.ToastMessage(activity.getApplicationContext(), str);
					break;
				case 1: //加载成功
					activity.reciveMailInfor = (ReciveMailInfor) msg.obj;
					if(activity.reciveMailInfor!=null){
						activity.setData(activity.reciveMailInfor);
					}
					break;
				case 4: //删除   恢复成功
					List<MailInfor> tempList = (List<MailInfor>) msg.obj;
					if(tempList.size()>0){
						UIUtils.ToastMessage(activity.getApplicationContext(), "操作成功");
						activity.isBackOnReLoad = true;
						activity.goFinishActivity();
					}
					break;
				case 10:  //适配器里面点击下载
					int position = (Integer) msg.obj;
//					showIsDownLoadDialog(position);
					return;
				case Constant.AttACH_LOAD_SUCCESS:  //下载附件成功
					int position1 = (Integer) msg.obj;
//					openTheFile(position1);
					UIUtils.ToastMessage(activity.getApplicationContext(), "下载到  "
							+ FileUtils.getSDPath() + Constant.downLoadPath);
					break;
				default:
					break;
				}
				activity.mProgressDialog.dismiss();
		}
	};
	
	
	/*回复邮件的时候*/
	private void goReMailBtn(){
		Intent intent = new Intent();
		intent.setClass(this, WriteMailActivity.class);
		Bundle bundle = new Bundle();
		bundle.putInt(Constant.SendStatue, Constant.Send_FastRepaly);  //快速回复
		bundle.putString("reciveName",reciveMailInfor.getUserName());  //写邮件的 收件人
 		bundle.putString("OutBoxCopyer", reciveMailInfor.getOutBoxCopyer());  //邮件的抄送人
 		bundle.putString("OutBoxSecret", reciveMailInfor.getOutBoxSecret());  //邮件的密送人
		bundle.putString("OutBoxTheme", reciveMailInfor.getOutBoxTheme()); //主题
		bundle.putSerializable("AttachList", (Serializable) listMap);
		String OutBoxContent = Html.fromHtml(reciveMailInfor.getOutBoxContent()).toString();
		bundle.putString("OutBoxContent", OutBoxContent);
		intent.putExtras(bundle);
		startActivity(intent);
//		goFinishActivity(false);
	}
	
	
	
	/*是否恢复邮件*/
	private void showIsResumeDialog(){
		myDialog = new AlertDialog.Builder(this).create();
		myDialog.show();
		myDialog.getWindow().setContentView(R.layout.delete_alter_dialog);
		TextView textView = (TextView) myDialog.getWindow().findViewById(R.id.tv_title);
		textView.setText("是否恢复该邮件?");
		myDialog.getWindow().findViewById(R.id.ok_button)
		.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				setProgressShow("恢复邮件中...");
				MailInfor mailInfor = new MailInfor();
				List<MailInfor> listMailInfors = new ArrayList<MailInfor>();
				mailInfor.setId(reciveMailInfor.getId());
				mailInfor.setMailType(mailType);
				listMailInfors.add(mailInfor);
				new MailInforBizImpl(ReciveMailActivity.this)
				.reSumeMails(listMailInfors, mUIHandler);
				myDialog.dismiss();
			}
		});
		myDialog.getWindow().findViewById(R.id.cancel_button)
		.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				myDialog.dismiss();
			}
		});
	}
	
	/*删除邮件的操作*/
	private void goDeleteBtn(){
		myDialog = new AlertDialog.Builder(this).create();
		myDialog.show();
		myDialog.getWindow().setContentView(R.layout.delete_alter_dialog);
		TextView textView = (TextView) myDialog.getWindow().findViewById(R.id.tv_title);
		textView.setText("是否删除该邮件?");
		myDialog.getWindow().findViewById(R.id.ok_button)
		.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				setProgressShow("删除中...");
				MailInfor mailInfor = new MailInfor();
				List<MailInfor> listMailInfors = new ArrayList<MailInfor>();
				mailInfor.setId(reciveMailInfor.getId());
				mailInfor.setMailType(mailType);
				listMailInfors.add(mailInfor);
				if(isRecycle){
					new MailInforBizImpl(ReciveMailActivity.this)
					.deleteMail(listMailInfors,isRecycle,mailType,mUIHandler);
				}else{
					new MailInforBizImpl(ReciveMailActivity.this)
					.deleteMail(listMailInfors,false,mailType,mUIHandler);
				}
				myDialog.dismiss();
			}
		});
		myDialog.getWindow().findViewById(R.id.cancel_button)
		.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				myDialog.dismiss();
			}
		});
	}
	
	
	private void goBackBtn(){
		goFinishActivity();
	}
	
	@Override
	public void onClick(View v) {
		int id = v.getId();
		switch (id) {
		case R.id.topbar_back_ibtn:   //返回
			goBackBtn();
			break;
		case R.id.topbar_right_tv:   
			if(!isRecycle){
				goReMailBtn();//回复 
			}else{
				showIsResumeDialog();  //恢复邮件
			}
			break;
		case R.id.recive_delete_btn:  //删除按键
			goDeleteBtn();
			break;
		default:
			break;
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		new MailReciveInforBizImpz(this).downMailAttachFile(listMap.get(position));
	}
	
	
	
	private void goFinishActivity(){
			if(isBackOnReLoad){
				PreferencesUtils.putBoolean(this,Constant.RE_LAOD, true);
			}else{
				PreferencesUtils.putBoolean(this,Constant.RE_LAOD, false);
			}
		this.finish();
	}
	
	/*返回键的捕捉*/
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(keyCode ==  KeyEvent.KEYCODE_BACK){
			goBackBtn();
			return false;
		}
		return true;
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		mUIHandler.removeCallbacksAndMessages(this);
		mUIHandler = null;
	}
}
