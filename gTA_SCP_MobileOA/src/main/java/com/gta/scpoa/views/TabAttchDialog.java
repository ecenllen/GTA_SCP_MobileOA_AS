package com.gta.scpoa.views;

import java.util.ArrayList;
import java.util.List;

import com.gta.scpoa.R;
import com.gta.scpoa.adapter.TabAttachAdapter;
import com.gta.scpoa.entity.TabAttachInfo;
import com.gta.scpoa.util.DialogUtil;
import com.gta.scpoa.util.UIUtils;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

public class TabAttchDialog implements OnClickListener, OnItemClickListener {
	
	/**
	 * 
	 * 下载附件的时候使用
	 */
	private ProgressDialog mProgressDialog = null;
	
	/**
	 * 
	 * 上下文切换
	 */
	private Context mContext = null;

	/**
	 * 
	 * 表单中的附件的信息
	 */
	private List<TabAttachInfo> list = new ArrayList<TabAttachInfo>();
	
	/**
	 * 
	 * 返回的按键
	 */
	private ImageButton backBtn  = null;
	
	/**
	 * 
	 * 中间的文字
	 */
	private TextView tittleTv = null;
	
	/**
	 * 
	 * 自定义的dialog
	 */
	private AlertDialog myDialog = null;
	
	/**
	 *
	 * 适配器 
	 */
	private TabAttachAdapter adapter = null;
	
	/**
	 * 显示附件的列表
	 * 
	 */
	private ListView listView  = null;
	
	/**
	 * 传入数据源
	 * 
	 */
	public TabAttchDialog(Context context,List<TabAttachInfo> mList){
		mContext = context;
		list = mList;
		progressInit();
	}
	
	/**
	 * 
	 * 显示对话框
	 */
	public void showDialog(){
		myDialog = new AlertDialog.Builder(mContext).create();
		myDialog.show();
		myDialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
		myDialog.getWindow().setContentView(R.layout.tab_attach_layout);
		
		tittleTv = (TextView) myDialog.getWindow().findViewById(R.id.topbar_title_tv);
		tittleTv.setText("请选择要下载的附件");
		tittleTv.setVisibility(View.VISIBLE);
		
		backBtn = (ImageButton) myDialog.getWindow().findViewById(R.id.topbar_back_ibtn);
		backBtn.setOnClickListener(this);
//		backBtn.setVisibility(View.VISIBLE);
		
		listView = (ListView) myDialog.getWindow().findViewById(R.id.tabAttachListView);
		adapter = new TabAttachAdapter(mContext, list);
		listView.setAdapter(adapter);
		adapter.notifyDataSetChanged();
		listView.setOnItemClickListener(this);
	}
	
	/**
	 * 隐藏对话框
	 */
	public void diglogMiss(){
		myDialog.dismiss();
	}
	
	/**
	 * 显示滚动条 
	 */
	private void progressInit() {
		mProgressDialog = new ProgressDialog(mContext);
	}
	
	/**
	 * 设置滚动框信息 
	 */
	private void setProgressShow(String str) {
		DialogUtil.showDialog(mProgressDialog, str, false);
	}
	
	/**
	 * 用于交互处理
	 */
	@SuppressLint("HandlerLeak")
	private Handler mUIHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
				String str = "";
				switch (msg.what) {
				default:
					break;
				}
			}
	};
	
	
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		int id = v.getId();
		switch (id) {
		case R.id.topbar_back_ibtn:
			diglogMiss();
			break;
		default:
			break;
		}
	}
	
	
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		// TODO Auto-generated method stub
		UIUtils.ToastMessage(mContext, list.get(position).getName());
	}
	
}
