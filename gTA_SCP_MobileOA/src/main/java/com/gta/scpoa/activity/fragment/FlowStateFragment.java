package com.gta.scpoa.activity.fragment;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.gta.scpoa.R;
import com.gta.scpoa.adapter.FlowStateAdapter;
import com.gta.scpoa.biz.IGetDataBiz;
import com.gta.scpoa.biz.impl.GetDataBizImpl;
import com.gta.scpoa.entity.ProcessState;
import com.gta.scpoa.entity.TaskNewInfor;
import com.gta.scpoa.util.DialogUtil;
import com.gta.scpoa.util.UIUtils;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

@SuppressLint("ValidFragment")
public class FlowStateFragment extends BaseFragmnet {

	private View contentView;
	private TaskNewInfor taskNewInfor;
	private ListView lv;
	private FlowStateAdapter adapter;
	private FlowStateHandler mHandler;
	private FrameLayout parent;

//	public FlowStateFragment(Context context, TaskNewInfor taskNewInfor) {
//		this.taskNewInfor = taskNewInfor;
//	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.taskNewInfor = (TaskNewInfor)getArguments().getSerializable("infor");
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		if (contentView != null) {
			ViewGroup viewGroup = (ViewGroup) contentView.getParent();
			if (viewGroup != null) {
				viewGroup.removeView(contentView);
			}
		} else {
			contentView = inflater.inflate(R.layout.flow_state_layout, null);
			parent = (FrameLayout) contentView.findViewById(R.id.parent_layout);
			lv = (ListView) contentView.findViewById(R.id.process_state_lv);
			lv.setDividerHeight(0);
			adapter = new FlowStateAdapter(getActivity().getLayoutInflater());
			lv.setAdapter(adapter);
			setData();
		}
		return contentView;
	}

	private void setData() {
		if (null == mHandler) {
			mHandler = new FlowStateHandler(this);
		}
		IGetDataBiz biz = new GetDataBizImpl();
		biz.getTaskHistory(taskNewInfor.getActInstId(),
				taskNewInfor.getRunId(), mHandler);

	}

	public static class FlowStateHandler extends Handler {
		WeakReference<FlowStateFragment> wr;
		ProgressDialog dialog;
		private LinearLayout refreashLayout;
		private FrameLayout.LayoutParams mParams;
		public FlowStateHandler(FlowStateFragment fragment) {
			super();
			this.wr = new WeakReference<FlowStateFragment>(fragment);
			this.dialog = new ProgressDialog(fragment.getActivity());
			DialogUtil.init(dialog, false);
		}

		@Override
		public void handleMessage(Message msg) {
			FlowStateFragment mFragment = wr.get();
			switch (msg.what) {

			case 1:
				removeRefreashLayout(mFragment);
				DialogUtil.dismissDialog(dialog);
				mFragment.adapter.setData((ArrayList<ProcessState>) msg.obj);
				mFragment.adapter.notifyDataSetChanged();
				break;
			case -1:// 失败		
				addRefreashLayout(mFragment);
				DialogUtil.dismissDialog(dialog);
				UIUtils.ToastMessage(mFragment.getActivity().getApplicationContext(), msg.obj.toString());
				break;
			case 10:
				DialogUtil.showDialog(dialog, msg.obj.toString());
				break;
			default:
				break;
			}
		}

		private void addRefreashLayout(final FlowStateFragment fragment) {
		
			if (null == refreashLayout) {				
				initRefreashLayout(fragment);				
			}
			initLayoutParams(Gravity.CENTER);
			fragment.parent.setLayoutParams(mParams);
			
			fragment.parent.removeView(refreashLayout);
			fragment.parent.addView(refreashLayout);
		}

		private void removeRefreashLayout(FlowStateFragment fragment) {
			if (null != refreashLayout) {
				fragment.parent.removeView(refreashLayout);
			}
			initLayoutParams(Gravity.TOP);
			fragment.parent.setLayoutParams(mParams);
		}
		
		private void initRefreashLayout(final FlowStateFragment fragment){	
			
			refreashLayout = new LinearLayout(fragment.getActivity());
			refreashLayout.setOrientation(LinearLayout.VERTICAL);
			refreashLayout.setGravity(Gravity.CENTER_HORIZONTAL);
			Button bt = new Button(fragment.getActivity());
			bt.setLayoutParams(new LayoutParams(
					android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
					android.view.ViewGroup.LayoutParams.WRAP_CONTENT));
			bt.setText("刷新");
			bt.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					fragment.setData();
				}
			});
			
			TextView tv = new TextView(fragment.getActivity());
			tv.setLayoutParams(new LayoutParams(
					android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
					android.view.ViewGroup.LayoutParams.WRAP_CONTENT));
			tv.setText("加载失败，请按刷新按钮重试");
			refreashLayout.addView(bt);
			refreashLayout.addView(tv);					
		}
		private void initLayoutParams(int newGravity){
			if (null == mParams) {
				mParams = new FrameLayout.LayoutParams(
						LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			}
			mParams.gravity = newGravity;	
		}
	}
}
