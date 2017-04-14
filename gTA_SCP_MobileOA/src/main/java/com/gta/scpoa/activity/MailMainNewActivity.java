package com.gta.scpoa.activity;


import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.gta.db.annotation.ViewInject;
import com.gta.scpoa.R;
import com.gta.scpoa.adapter.MailListAdapter;
import com.gta.scpoa.biz.impl.MailInforBizImpl;
import com.gta.scpoa.common.Constant;
import com.gta.scpoa.entity.MailInfor;
import com.gta.scpoa.util.DialogUtil;
import com.gta.scpoa.util.PreferencesUtils;
import com.gta.scpoa.util.UIUtils;
import com.gta.scpoa.views.CommonBottomView;
import com.gta.scpoa.views.CommonTopView;
import com.gta.scpoa.views.SearchEditText;
import com.gta.scpoa.views.XListView;
import com.gta.scpoa.views.XListView.IXListViewListener;

import java.util.ArrayList;
import java.util.List;


/**
 * 邮件首页
 * 
 * @author xiaojie.cai
 * 
 */
@SuppressLint("HandlerLeak")
public class MailMainNewActivity extends BaseActivity implements OnClickListener, IXListViewListener, OnItemClickListener{
	/**
	 * 公共顶部布局
	 */
	@ViewInject(id = R.id.mail_top_view)
	private CommonTopView mailTopview = null;
	
	@ViewInject(id = R.id.tab1)     //tab1
	private TextView tab1;
	
	@ViewInject(id = R.id.tab2)       //tab2
	private TextView tab2;
	
	@ViewInject(id = R.id.tab3)    //tab3
	private TextView tab3;
	
	@ViewInject(id = R.id.tab4)   //tab4
	private TextView tab4;
	
	@ViewInject(id = R.id.tabBg1)   //tab1背景
	private RelativeLayout tabBg1;
	
	@ViewInject(id = R.id.tabBg2) //tab2背景
	private RelativeLayout tabBg2;
	
	@ViewInject(id = R.id.tabBg3)//tab3背景
	private RelativeLayout tabBg3;
	
	@ViewInject(id = R.id.tabBg4)//tab4背景
	private RelativeLayout tabBg4;
	
	@ViewInject(id = R.id.mailMainListview)   //listview
	private XListView mailListView;
	
	@ViewInject(id = R.id.long_item_click_tittle)//长按出现的头部
	private RelativeLayout long_item_click_tittle;
	
	@ViewInject(id = R.id.long_item_back_ibtn)  //长按的头部的返回按键
	private ImageButton long_item_back_ibtn;
	
	@ViewInject(id = R.id.delete_num_text)   //长按出现的头部的num显示text
	private TextView delete_num_text;
	
	@ViewInject(id = R.id.restore_text)   //长按出现的头部的恢复显示text
	private TextView restore_text;
	
	@ViewInject(id = R.id.delete_text)   //长按出现的头部的删除显示text
	private TextView delete_text;
	
	@ViewInject(id = R.id.mail_bottom_view)   //底部
	private CommonBottomView mainCommonBottom = null;
	
	@ViewInject(id = R.id.tab1_num_text)   //tab1的num显示
	private TextView tab1_num_text;
	
	@ViewInject(id = R.id.mail_search_edit)    //搜索
	private SearchEditText filter_edit;
	
	/**
	 * 显示暂时无数据
	 * */
	@ViewInject(id = R.id.mail_notdata_view)
	private TextView mail_notdata_view;
	
	/***********************************************************/
	
	private ProgressDialog mProgressDialog = null;
	private MailListAdapter listViewAdapter;   //适配器
	private AlertDialog myDialog = null;
	private int currentPosition = 0;
	public int num = 10;  //加载条数
	public int firstLoadNum = 20; //加载条数
	public static int firstLoad  = 1;  //第一次加载
	public static int loadMore  = 2;  //加载更多
	public static int onRefresh  = 3;  //加载新的
	public int type = 1;   //邮件类型   1 收件  2发件  3草稿  4回收
	public boolean isback  = false;
	private List<MailInfor> listInfors = new ArrayList<MailInfor>();
	private List<MailInfor> searchTempLists = new ArrayList<MailInfor>();
	/*是否是在搜索状态*/
	private boolean isSearch = false;
	/*返回重新加载标志*/
	private boolean isreLoad = false;
	/*记录搜索的过去的string*/
	private String preSearchString = "";
	/*业务操作类*/
	private MailInforBizImpl mailInforBizImpl = new MailInforBizImpl(this);
	/*用于交互处理*/
	private Handler mUIHandler = new Handler() {
		@SuppressWarnings("unchecked")
		@Override
		public void handleMessage(Message msg){
			if (!isback) {
				List<MailInfor> temInfors = new ArrayList<MailInfor>();
				switch (msg.what) {
				case MailInforBizImpl.GET_Fail:  //失败
					String failString  = (String)msg.obj;
					if(failString.equals("command=1")){  //首次获取失败
						failString = "网络异常，加载数据失败!";
						listInfors.clear();
						listViewAdapter.setList(listInfors);
						listViewAdapter.notifyDataSetChanged();
						mailListView.stopRefresh();
						mailListView.stopLoadMore();
						isHideTheFootView();
						progressDialogDisMiss();
					}else if(failString.equals("command=2")){  //更多获取失败
						failString = "网络异常，加载更多失败!";
						mailListView.stopLoadMore();
					}else if(failString.equals("command=3")){  //最新获取失败
						failString = "网络异常，刷新失败!";
						mailListView.stopRefresh();
					}else{
						progressDialogDisMiss();
					}
					UIUtils.ToastMessage(getApplicationContext(), failString);
					break;
				case MailInforBizImpl.GET_SUCCESS: //第一次进  加载成功
					listInfors.clear();
					listInfors = (List<MailInfor>) msg.obj;
					listViewAdapter.setList(listInfors);
					if(type==1){
						listViewAdapter.setMailStatue(true,false);
						/*刷新未读的数据*/
						int unreadNum = PreferencesUtils.getInt(
								getApplicationContext(), Constant.UNREAD_COUNT);
						setTab1Num(unreadNum);
					}else{
						if(type == 4){
							listViewAdapter.setMailStatue(false,true);
						}else{
							listViewAdapter.setMailStatue(false,false);
						}
					}
					listViewAdapter.notifyDataSetChanged();
// 					UIUtils.ToastMessage(getApplicationContext(), "获取成功");
 					if(listInfors.size() > 0){
 						mailListView.setSelection(1);
 					}
					mailListView.stopLoadMore();
					mailListView.stopRefresh();
					isHideTheFootView();
					progressDialogDisMiss();
					break;
				case MailInforBizImpl.GET_MORE:  //更多
					if(!mProgressDialog.isShowing()){
						temInfors = (List<MailInfor>) msg.obj; 
						listInfors.addAll(temInfors);
						listViewAdapter.setList(listInfors);
						listViewAdapter.notifyDataSetChanged();
						if(temInfors.size()==0){
							mailListView.setPullLoadEnable(false);
						}else{
							mailListView.setPullLoadEnable(true);
						}
					}
					mailListView.stopLoadMore();
					 break;
				case MailInforBizImpl.GET_REFRSH:  //最新 
					if(!mProgressDialog.isShowing()){
						temInfors = (List<MailInfor>) msg.obj; 
						listInfors.addAll(0, temInfors);
						listViewAdapter.setList(listInfors);
						listViewAdapter.notifyDataSetChanged();
						/*获取未读的条数*/
						if(type == 1){
							int unreadNum = PreferencesUtils.getInt(
									getApplicationContext(), Constant.UNREAD_COUNT);
							setTab1Num(unreadNum);
						}
					}
					mailListView.stopRefresh();
					isHideTheFootView();
					break;
				case MailInforBizImpl.DELETE_RESUME:  //删除  //恢复
					List<MailInfor> tempList = (List<MailInfor>) msg.obj;
					List<MailInfor> deleteList  = new ArrayList<MailInfor>();
					if(tempList.size()>0){
						for(MailInfor mailInfor : tempList){
							for(int i = 0 ; i < listInfors.size(); i ++){
								if(mailInfor.getId().equals(listInfors.get(i).getId())){
									deleteList.add(listInfors.get(i));
									break;
								}
							}
						}
						if(deleteList.size() > 0){
							if(type == 1){  //可能有未读的情况
								int deleteUnread = 0;
								for(int i = 0 ;i < deleteList.size() ; i++){
									if(!deleteList.get(i).isRead()){
										deleteUnread ++;
									}
								}
								try {
									int unReadnum = Integer.parseInt(tab1_num_text.getText().toString());
									unReadnum = unReadnum - deleteUnread;
									if(unReadnum <= 0) unReadnum =0;
									setTab1Num(unReadnum);
									PreferencesUtils.putInt(getApplicationContext(), Constant.UNREAD_COUNT, unReadnum);
								} catch (Exception e) {
									e.printStackTrace();
								}
							}
							if(isSearch){   //这时是搜索的情况下
								List<MailInfor> tempSearchDeleteList = new ArrayList<MailInfor>();
								for(int i = 0 ; i < deleteList.size(); i ++){
									for(int j = 0 ; j < searchTempLists.size(); j++){
										if (searchTempLists
												.get(j)
												.getId()
												.equals(deleteList.get(i)
														.getId())) {
											tempSearchDeleteList.add(searchTempLists.get(j));
											break;
										}
									}
								}
								searchTempLists.removeAll(tempSearchDeleteList);
							}
							listInfors.removeAll(deleteList);
							listViewAdapter.setList(listInfors);
							listViewAdapter.notifyDataSetChanged();
							setDeleteNum(); //设置头部的num
							UIUtils.ToastMessage(getApplicationContext(), "操作成功");
						}
					}
					progressDialogDisMiss();
					break;
				case MailInforBizImpl.RETURN_RELOAD: //重新加载   在onreSume里面
					showProgressDialog("数据加载中...");
					mailInforBizImpl.getMailInfor(type, firstLoad, firstLoadNum, "0", "", mUIHandler);
					isreLoad = true;
					filter_edit.setText("");    //清空搜索框
					break;
				case MailInforBizImpl.SEARCH_SUCCESS: //搜索成功   总体的 
					List<MailInfor> tempList1 = (List<MailInfor>) msg.obj;
					searchSuccess(tempList1);
					listViewAdapter.notifyDataSetChanged();
					mailListView.stopRefresh();
					mailListView.stopLoadMore();
					progressDialogDisMiss();
					isHideTheFootView();
					break;
				case MailInforBizImpl.SEARCH_REFRSH_SUCCESS: //搜索下拉更新成功   
					List<MailInfor> tempList2 = (List<MailInfor>) msg.obj;
					listInfors.addAll(0, tempList2);
					listViewAdapter.setList(listInfors);
					listViewAdapter.notifyDataSetChanged();
					mailListView.stopRefresh();
					isHideTheFootView();
					break;
				case MailInforBizImpl.SEARCH_MORE_SUCCESS: //搜索加载更多成功  
					List<MailInfor> tempList3 = (List<MailInfor>) msg.obj; 
					listInfors.addAll(tempList3);
					listViewAdapter.setList(listInfors);
					listViewAdapter.notifyDataSetChanged();
					mailListView.stopLoadMore();
					if(tempList3.size()==0){
						mailListView.setPullLoadEnable(false);
					}else{
						mailListView.setPullLoadEnable(true);
					}
					break;
				default:
					break;
				}
				if(!mailListView.isOnLoadMore()&&!mailListView.isOnRefresh()){
					filter_edit.setEnabled(true);
				}
			}
		}
	};
	
	
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.mail_main_new_layout);
		viewInit();
		dataInit();
	}
	
	
	private void viewInit(){
		// 设置顶部布局
		mailTopview.setBackImageButtonEnable(true);
		mailTopview.setBackImageButtonOnClickListener(this); // 左上角返回按钮监听
		mailTopview.setAddImageButtonEnable(true);
		mailTopview.setTitleTextViewEnable(true);
		mailTopview.setTitleTextViewText("邮箱");

		
		// 底部布局
		mainCommonBottom.setIsMainActivity(false);
				
		
		
		/*长按出现的头部初始化*/
		long_item_back_ibtn.setOnClickListener(this); //长按返回按键
		/*显示删除数的text*/
		delete_num_text.setOnClickListener(this);
		/*恢复的text*/
		restore_text.setOnClickListener(this);
		/*删除的text*/
		delete_text.setOnClickListener(this);
		
		if(mProgressDialog == null)
		mProgressDialog = new ProgressDialog(this);
		
		
		tab1.setTextColor(0xFFFFA749);
		tab1.setOnClickListener(this);
		tab2.setOnClickListener(this);
		tab3.setOnClickListener(this);
		tab4.setOnClickListener(this);
		tab1_num_text.setVisibility(View.GONE);

		// 根据输入框输入值的改变来过滤搜索
		filter_edit.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				/* 去掉收尾空格 */
				String searchString = filter_edit.getText().toString();
				if (searchString.equals("")) {
					
					preSearchString = "";
					
					if (!isSearch) {
						return;
					}
					isSearch = false;
					if (isreLoad) {
						isreLoad = false;
						return; // 因为返回的时候重新刷新的时候要刷新 将搜索设置为“” 触发这个
					}
					/* 要恢复原来的数据 */
					if (listViewAdapter.IsAllOption()) {
						for (MailInfor mailInfor : listInfors) {
							for (int i = 0; i < searchTempLists.size(); i++) {
								if (searchTempLists.get(i).getId()
										.equals(mailInfor.getId())) {
									searchTempLists.get(i).setPrefreDel(
											mailInfor.isPrefreDel());
									break;
								}
							}
						}
					}
					listInfors.clear();
					listInfors.addAll(searchTempLists);
					listViewAdapter.setList(listInfors);
					searchTempLists.clear();
					if (type == 1) {
						listViewAdapter.setMailStatue(true, false);
						/* 刷新未读的数据 */
						int unreadNum = PreferencesUtils.getInt(
								getApplicationContext(), Constant.UNREAD_COUNT);
						setTab1Num(unreadNum);
					} else {
						if (type == 4) {
							listViewAdapter.setMailStatue(false, true);
						} else {
							listViewAdapter.setMailStatue(false, false);
						}
					}
					listViewAdapter.notifyDataSetChanged();
					isHideTheFootView();
					setDeleteNum();
					// UIUtils.ToastMessage(getApplicationContext(),"清空");
				} else {
					searchString = searchString.trim();
					if(searchString.equals("")){
//						filter_edit.setText("");
						return;
					}
					
					if(preSearchString.equals(searchString)){
						return ;
					}
					if (!isSearch) {
						searchTempLists.clear();
						searchTempLists.addAll(listInfors);
					}
					isSearch = true;
					// UIUtils.ToastMessage(getApplicationContext(), "搜索");
					showProgressDialog("搜索中...");
					mailInforBizImpl.getMailInfor(type, firstLoad, firstLoadNum, "0",
									searchString, mUIHandler);
					preSearchString = searchString;
				}
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {

			}

			@Override
			public void afterTextChanged(Editable s) {
				// UIUtils.ToastMessage(getApplicationContext(), "3");
			}
		});
		
		
	}
	

	
	
	
	private void dataInit(){
		PreferencesUtils.putBoolean(this,Constant.RE_LAOD, false);
		mailListView.setPullLoadEnable(false);
		mailListView.setPullRefreshEnable(true);
		listViewAdapter = new MailListAdapter(this,listInfors,mUIHandler);
		listViewAdapter.setMailStatue(true,false);
		mailListView.setAdapter(listViewAdapter);
		listViewAdapter.notifyDataSetChanged();
		mailListView.setXListViewListener(this);
		mailListView.setOnItemClickListener(this);
		
		mailListView.setOnItemLongClickListener(new ListView.OnItemLongClickListener(){

			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view,
					int position, long id) {
				if(listViewAdapter.IsAllOption()) return true;
				
//				if(filter_edit.getText()
//						.toString().equals("")){
//					/*当前没有在搜索*/
//					filter_edit.setEnabled(false);
//				}
				if(type == 4) restore_text.setVisibility(View.VISIBLE);
				else          restore_text.setVisibility(View.GONE);
				
				mailTopview.setVisibility(View.GONE);  //头部隐藏
				mainCommonBottom.setVisibility(View.GONE); //底部隐藏
				long_item_click_tittle.setVisibility(View.VISIBLE); //新头部显示
				listViewAdapter.setAllOption(true);   //设置批量操作是true
				listViewAdapter.notifyDataSetChanged();
				return true;
			}
			
		});
		
		showProgressDialog("数据加载中...");
		mailInforBizImpl.getMailInfor(type, firstLoad, firstLoadNum, "0", "", mUIHandler);
	}


	/**
	 * 设置未读邮件数字
	 * @param unreadNum
	 */
	private void setTab1Num(int unreadNum){
		if(unreadNum <= 0){
	    	tab1_num_text.setVisibility(View.GONE);
	    }else{
	    	tab1_num_text.setVisibility(View.VISIBLE);
	    }
		tab1_num_text.setText(""+unreadNum);
	}
	
	@Override
	protected void onResume() {
		boolean isreLoad = PreferencesUtils.getBoolean(this,Constant.RE_LAOD, false);
		if(isreLoad){
			PreferencesUtils.putBoolean(this, Constant.RE_LAOD, false);
			Message msg = mUIHandler.obtainMessage(8); // 重新加载
			msg.sendToTarget();
		}
			try {
				if(type==1){
					int TempNum = Integer.parseInt(tab1_num_text.getText().toString());
					int unReadConut = PreferencesUtils.getInt(getApplicationContext(), 
							Constant.UNREAD_COUNT, 0);
					setTab1Num(unReadConut);
					if(TempNum > unReadConut){
						listInfors.get(currentPosition).setRead(true);
						listViewAdapter.setList(listInfors);
						listViewAdapter.notifyDataSetChanged();
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			
		super.onResume();
	}
	
	
	public void showProgressDialog(String str){
		DialogUtil.showDialog(mProgressDialog, str, false);
	}
	
	
	private void progressDialogDisMiss(){
		if(mProgressDialog!=null){
			mProgressDialog.dismiss();
		}
	}
	
	
	
	/*搜索成功操作*/
	private void searchSuccess(List<MailInfor> tempList){
		/*先比较当前的*/
		for(MailInfor mailInfor: tempList){
			for(int i = 0 ; i < listInfors.size(); i ++){
				if(listInfors.get(i).getId().equals(mailInfor.getId())){
					mailInfor.setPrefreDel(listInfors.get(i).isPrefreDel());
					break;
				}
			}
		}
		listInfors.clear();
		for(MailInfor mailInfor: tempList){
			for(int i = 0 ; i < searchTempLists.size(); i ++){
				if(searchTempLists.get(i).getId().equals(mailInfor.getId())){
					mailInfor.setPrefreDel(searchTempLists.get(i).isPrefreDel());
					break;
				}
			}
		}
		listInfors.addAll(tempList);   //更新
		listViewAdapter.setList(listInfors);
		if(type==1){
			listViewAdapter.setMailStatue(true,false);
		}else{
			if(type == 4){
				listViewAdapter.setMailStatue(false,true);
			}else{
				listViewAdapter.setMailStatue(false,false);
			}
		}
		listViewAdapter.notifyDataSetChanged();
		if(listInfors.size() > 0){
				mailListView.setSelection(1);
		}
		setDeleteNum(); 
	}
	
	
	/*设置头部num*/
	private void setDeleteNum(){
		if(listViewAdapter.IsAllOption()){  //头部显示
			delete_num_text.setText(mailInforBizImpl.getAllCheckNum(listInfors)+"");
		}
	}
	
	
	/*用于切换界面*/
	private void goWriteMailActivity(){
		Intent intent = new Intent();
		intent.setClass(this, WriteMailActivity.class);
		Bundle bundle = new Bundle();
		bundle.putInt(Constant.SendStatue, Constant.Send_NewMail);   //0为新建邮件    1为快速恢复   2为发件箱查看   3为草稿箱查看
		intent.putExtras(bundle);
		startActivity(intent);
	}
	
	
	private void goLongBackButton(){
		mailTopview.setVisibility(View.VISIBLE);   //顶部显示
		mainCommonBottom.setVisibility(View.VISIBLE); //底部显示
		long_item_click_tittle.setVisibility(View.GONE);  //长按出现的顶部隐藏
		delete_num_text.setText("0");
		listViewAdapter.setAllOption(false);   //不是批量操作
		mailInforBizImpl.setAllMailNotPreDel(listInfors);
		mailInforBizImpl.setAllMailNotPreDel(searchTempLists);
		listViewAdapter.notifyDataSetChanged();
	}
	
	
	/*批量删除数据*/
	private void goAllDelte(){
		final List<MailInfor> tempList = listViewAdapter.getAllCheck();
		if(tempList.size() == 0){
			UIUtils.ToastMessage(this, "未选择数据");
			return ;
		}
		myDialog = new AlertDialog.Builder(this).create();
		myDialog.show();
		myDialog.getWindow().setContentView(R.layout.delete_alter_dialog);
		TextView textView = (TextView) myDialog.getWindow().findViewById(R.id.tv_title);
		textView.setText("是否删除选中的邮件");
		Button ok_button = (Button) myDialog.getWindow().findViewById(R.id.ok_button);
		ok_button.setText("确定");
		ok_button.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				showProgressDialog("删除中...");
				mailInforBizImpl.deleteMail(tempList,listViewAdapter.IsRecycle(),type, mUIHandler);
				myDialog.dismiss();
			}
		});
		Button cancelButton = (Button)myDialog.getWindow().findViewById(R.id.cancel_button);
		cancelButton.setText("取消");
		cancelButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				myDialog.dismiss();
			}
		});
		
	}
	
	/*批量恢复*/
	private void goAllRestore(){
		if(type!=4) return ;
		final List<MailInfor> tempList = listViewAdapter.getAllCheck();
		if(tempList.size() == 0){
			UIUtils.ToastMessage(this, "未选择数据");
			return ;
		}
		myDialog = new AlertDialog.Builder(this).create();
		myDialog.show();
		myDialog.getWindow().setContentView(R.layout.delete_alter_dialog);
		TextView textView = (TextView) myDialog.getWindow().findViewById(R.id.tv_title);
		textView.setText("是否恢复选中的邮件");
		Button ok_button = (Button) myDialog.getWindow().findViewById(R.id.ok_button);
		ok_button.setText("确定");
		ok_button.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				showProgressDialog("恢复中...");
				mailInforBizImpl.reSumeMails(tempList, mUIHandler);
				myDialog.dismiss();
			}
		});
		Button cancelButton = (Button)myDialog.getWindow().findViewById(R.id.cancel_button);
		cancelButton.setText("取消");
		cancelButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				myDialog.dismiss();
			}
		});
	}
	
	@Override
	public void onClick(View v) {
		int id  = v.getId();
		switch (id) {
		case R.id.tab1:  //tab1
			createTab(1,tab1,tabBg1);
			break;
		case R.id.tab2: //tab2
			createTab(2,tab2,tabBg2);
			break;
		case R.id.tab3:	//tab3
			createTab(3,tab3,tabBg3);
			break;
		case R.id.tab4:	//tab4
			createTab(4,tab4,tabBg4);
			break;
		case R.id.topbar_add_ibtn:  //头标+号   新建邮件
			goWriteMailActivity();
			break;
		case R.id.topbar_back_ibtn:  //头标返回按键
			goBack();
			break;
		case R.id.restore_text:     //恢复text
			goAllRestore();
			break;
		case R.id.delete_text:      //删除text
			goAllDelte();
			break;
		case R.id.long_item_back_ibtn:  //长按的返回按键
			goBack();
			break;
		default:
			break;
		}
	}
	
	/*用于切换tab*/
	private void createTab(int currentTag,TextView text,RelativeLayout bg) {
		if(mailListView.isOnLoadMore()||mailListView.isOnRefresh()) {
			return ;
		}
		
		if(listViewAdapter.IsAllOption()){ //批量操作的情况
			return ;
		}
		
		if(isSearch) return;
		
		if(currentTag == type) return;
		if(type<=0||type>4) return ;
		
		type = currentTag;
		
		tab1.setTextColor(0xff8E8E90);
		tab2.setTextColor(0xff8E8E90);
		tab3.setTextColor(0xff8E8E90);
		tab4.setTextColor(0xff8E8E90);
		
		tabBg1.setBackgroundResource(R.drawable.tab_default);
		tabBg2.setBackgroundResource(R.drawable.tab_default);
		tabBg3.setBackgroundResource(R.drawable.tab_default);
		tabBg4.setBackgroundResource(R.drawable.tab_default);
		
		text.setTextColor(0xFFFFA749);
		bg.setBackgroundResource(R.drawable.tab_checked);
		
		if(type != 1){
			tab1_num_text.setBackgroundResource(R.drawable.tab_out);
		}else{
			tab1_num_text.setBackgroundResource(R.drawable.tab_in);
		}
		
	
		showProgressDialog("数据加载中...");
		mailInforBizImpl.getMailInfor(type, firstLoad, firstLoadNum, "0", "", mUIHandler);
	}
	
	
	private void goBack(){   //返回按键
		if(listViewAdapter.IsAllOption()){
			goLongBackButton();
			return;
		} 
		isback = true;
		this.finish();
	}
	
	/**
	 * 监听返回键，直接finish
	 */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch (keyCode) {
		case KeyEvent.KEYCODE_BACK:
			goBack();
			return false;
		}
		return super.onKeyDown(keyCode, event);
	}
	
	
	
	@Override
	public void onRefresh() {  //更新
//		mailListView.stopRefresh();
		filter_edit.setEnabled(false);
		
		String searchString  = "";
		if(isSearch){
			searchString = filter_edit.getText().toString().trim();
		}
		
		if (listInfors.size() == 0) { // 第一次加载
			mailInforBizImpl.getMailInfor(type, firstLoad,
					firstLoadNum, "0", searchString, mUIHandler);
		} else {
			if (type != 4) {
				mailInforBizImpl.getMailInfor(type, onRefresh, num,
						listInfors.get(0).getId(), searchString, mUIHandler);
			} else {
				mailInforBizImpl.getMailInfor(type, onRefresh, num,
						listInfors.get(0).getCreateTime(), searchString, mUIHandler);
			}
		}
	}

	@Override
	public void onLoadMore() {  //更多
//		mailListView.stopLoadMore();
		filter_edit.setEnabled(false);
		
		String searchString  = "";
		if(isSearch){
			searchString = filter_edit.getText().toString().trim();
		}
		
		if (listInfors.size() == 0) { // 第一次加载
			mailInforBizImpl.getMailInfor(type, firstLoad,
					firstLoadNum, "0", searchString, mUIHandler);
		} else {
			if (type != 4) {
				mailInforBizImpl.getMailInfor(type, loadMore, num,
						listInfors.get(listInfors.size() - 1).getId(), searchString,
						mUIHandler);
			} else {
				mailInforBizImpl.getMailInfor(type, loadMore, num,
						listInfors.get(listInfors.size() - 1).getCreateTime(),
						searchString, mUIHandler);
			}
		}
	}
	
	
	private void goReciveMailActivity(int position){
		MailInfor mailInfor = listInfors.get(position);
		Intent intent = new Intent();
		intent.setClass(this, ReciveMailActivity.class);
		Bundle bundle = new Bundle();
		bundle.putString("userName",mailInfor.getUserName());  //发件人
		bundle.putString("outBoxTheme",mailInfor.getOutBoxTheme()); //主题
		bundle.putString("createTime",mailInfor.getCreateTime());  //时间
		bundle.putString("id", mailInfor.getId());  //ID
		if(type == 1){//收件箱
			bundle.putBoolean("isRead", mailInfor.isRead());
			bundle.putInt("MailType", 1);  //类型
		} 
		if(type == 4){
			bundle.putInt("MailType", mailInfor.getMailType());  //类型
			bundle.putBoolean("isRecycle", true);
		}
		intent.putExtras(bundle);
		startActivity(intent);
	}
	
	
	private void goWriteMailActivity(int position){
		MailInfor mailInfor = listInfors.get(position);
		Intent intent = new Intent();
		intent.setClass(this,WriteMailActivity.class);
		Bundle bundle = new Bundle();
		if(type == 2){
			bundle.putInt(Constant.SendStatue, Constant.Send_SendMail);
		}
		if(type == 3){
			bundle.putInt(Constant.SendStatue, Constant.Send_draftMail);
		}
		bundle.putString("outBoxTheme",mailInfor.getOutBoxTheme()); //主题
		bundle.putString("createTime",mailInfor.getCreateTime());  //时间
		bundle.putString("id", mailInfor.getId());  //ID
		intent.putExtras(bundle);
		startActivity(intent);
	}
	
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		
		position = position -1;
		
		if(listViewAdapter.IsAllOption()){    //批量删除的情况
			if(listInfors.get(position).isPrefreDel()){
				listInfors.get(position).setPrefreDel(false);
			}else{
				listInfors.get(position).setPrefreDel(true);
			}
			listViewAdapter.notifyDataSetChanged();
			setDeleteNum();
			return;
		}
		
		
		if(type==1||type==4){  //收件箱和回收站的情况
			currentPosition = position;
			goReciveMailActivity(position);
		}else{   //发件箱和草稿箱的情况
			goWriteMailActivity(position);
		}
//		listInfors.get(position).setRead(true);
		listViewAdapter.closeItem(position);
		listViewAdapter.notifyDataSetChanged();
	}
	
	/*是否需要隐藏底部*/
	private void isHideTheFootView(){
		if(listInfors.size()<20){
			mailListView.setPullLoadEnable(false);
		}else{
			mailListView.setPullLoadEnable(true);
		}
		
		if(listInfors.isEmpty()){
			mail_notdata_view.setVisibility(View.VISIBLE);
		}else{
			mail_notdata_view.setVisibility(View.GONE);
		}
	}
	
	@Override
	protected void onDestroy() {
		isback = true;    
		PreferencesUtils.putBoolean(this, Constant.RE_LAOD, false);
		super.onDestroy();
	}
	
}
