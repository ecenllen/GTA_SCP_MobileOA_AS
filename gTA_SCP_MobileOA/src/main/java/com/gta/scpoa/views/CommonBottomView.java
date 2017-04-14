package com.gta.scpoa.views;

import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.gta.scpoa.R;
import com.gta.scpoa.activity.PersonalCenterActivity;
import com.gta.scpoa.activity.TaskMainActivity;
import com.gta.scpoa.common.Constant;
import com.gta.scpoa.util.StringUtils;
import com.gta.scpoa.util.UIUtils;

public class CommonBottomView extends BaseView {
	private boolean isMainActivity = false;
	private LinearLayout ll2;
	private TextView numberTV;

	public CommonBottomView(Context context, AttributeSet attrs) {
		super(context, attrs);
		setupViews();
	}

	public CommonBottomView(Context context) {
		super(context);
		setupViews();
	}

	private void setupViews() {
		setContentView(R.layout.common_bottom_bar);

		if (isInEditMode()) {
			return;
		}

		numberTV = (TextView) findViewById(R.id.common_bottom_num_tv);

		RelativeLayout ll1 = (RelativeLayout) findViewById(R.id.ll_todo);
		ll2 = (LinearLayout) findViewById(R.id.ll_home);
		LinearLayout ll3 = (LinearLayout) findViewById(R.id.ll_personal_center);

		ll1.setOnClickListener(this);
		if (!isMainActivity) {
			ll2.setOnClickListener(this);
		}
		ll3.setOnClickListener(this);
		setNumber(Constant.taskNumber);
	}

	public void setIsMainActivity(boolean isMain) {
		isMainActivity = isMain;
	}

	public void setHomeLLOnClickListener(OnClickListener listener) {
		ll2.setOnClickListener(listener);
	}

	@Override
	public void onClick(View v) {
		Intent intent = null;
		switch (v.getId()) {

		case R.id.ll_todo:
			if (mActivity instanceof TaskMainActivity) {
				((TaskMainActivity) mActivity).ChangeToTab1();
				return;
			}
			intent = new Intent(mActivity, TaskMainActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			mActivity.startActivity(intent);
			// mActivity.finish();
			break;
		case R.id.ll_home:
			UIUtils.goMainActivity(mActivity);
			// mActivity.finish();
			break;

		case R.id.ll_personal_center:
			intent = new Intent(mActivity, PersonalCenterActivity.class);
			mActivity.startActivity(intent);
			break;

		default:
			break;
		}
	}

	public void setNumber(String number) {
		
		if (!StringUtils.isEmpty(number)) {

			if (numberTV != null) {
				// 设置底部数字
				if (!String.valueOf(0).equals(number)) {
					numberTV.setVisibility(View.VISIBLE);
					numberTV.setText(number);
				} else {
					numberTV.setVisibility(View.INVISIBLE);
				}
			}
		}
	}
	
}
