package com.gta.scpoa.activity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;

import com.gta.db.annotation.ViewInject;
import com.gta.scpoa.R;
import com.gta.scpoa.adapter.ScheduleSpinnerAdapter;
import com.gta.scpoa.biz.IGetDataBiz;
import com.gta.scpoa.biz.impl.GetDataBizImpl;
import com.gta.scpoa.common.Constant;
import com.gta.scpoa.entity.Schedule;
import com.gta.scpoa.util.DialogUtil;
import com.gta.scpoa.util.StringUtils;
import com.gta.scpoa.util.UIUtils;
import com.gta.scpoa.views.CommonTopView;
import com.gta.scpoa.views.picker.TimePickerView;
import com.gta.scpoa.views.picker.WheelMain;

import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * 新建日程
 * 
 * @author bin.wang1
 * 
 */
public class NewScheduleActivity extends BaseActivity implements
		OnClickListener, OnItemSelectedListener {
	@ViewInject(id = R.id.new_sche_top_view)
	private CommonTopView topView;

	@ViewInject(id = R.id.schedule_new_selectstart_ll)
	private LinearLayout mSelectStartLL;

	@ViewInject(id = R.id.schedule_new_selectend_ll)
	private LinearLayout mSelectEndLL;

	@ViewInject(id = R.id.schedule_new_startdate_tv)
	private TextView mStartDateTV;
	@ViewInject(id = R.id.schedule_new_starttime_tv)
	private TextView mStartTimeTV;

	@ViewInject(id = R.id.schedule_new_enddate_tv)
	private TextView mEndDateTV;
	@ViewInject(id = R.id.schedule_new_endtime_tv)
	private TextView mEndTimeTV;

	@ViewInject(id = R.id.schedule_new_rb1)
	private RadioButton mWorkRB;
	@ViewInject(id = R.id.schedule_new_rb2)
	private RadioButton mPersonalRB;

	@ViewInject(id = R.id.schedule_new_spinner)
	private Spinner mRemindCycleSP;

	@ViewInject(id = R.id.schedule_new_remaining_tv)
	private TextView mRemainingTV;

	@ViewInject(id = R.id.schedule_new_content_et)
	private EditText mContentET;

	private NewScheduleHandler mHandler;
	private int checkstate = 1;// 默认是工作事务
	private int mRemind = 5;// // 将选择的赋值给mRmind 提醒周期(1:单次提醒,5:不提醒,6:工作日提醒,2:每日提醒,3:每周提醒,4:每月提醒)
	private int id_1 =-1;
	private int id_2 =-1;
	private ProgressDialog dialog;
	private AlertDialog alertDialog = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_new_schedule);
		init();
		setupViews();
	}
	
	public static final class NewScheduleHandler extends Handler{
		WeakReference<NewScheduleActivity> wr;
		
		public NewScheduleHandler(NewScheduleActivity activity) {
			super();
			this.wr = new WeakReference<>(activity);
		}

		@Override
		public void handleMessage(Message msg) {
			NewScheduleActivity mActivity = wr.get();
			if(mActivity == null)return;
			switch (msg.what) {
			case Constant.MSG_OPERATE_SUCCESS:
				DialogUtil.dismissDialog(mActivity.dialog);
				UIUtils.ToastMessage(mActivity.getApplicationContext(), msg.obj.toString());
				//新增或修改日程成功
				//如果是修改，首先修改本地集合的通知状态，将已通知过更改为未通知过。
				if (msg.arg1!=-1 && msg.arg1 != 0) {
					mActivity.getGTAApplication().updateTodaySche(msg.arg1, false);
				}
				mActivity.finish();
				break;
			case Constant.MSG_FAIL:
				DialogUtil.dismissDialog(mActivity.dialog);
				UIUtils.ToastMessage(mActivity.getApplicationContext(),msg.obj.toString());
				break;
			case Constant.MSG_SHOW_PROGRESS:
				DialogUtil.showDialog(mActivity.dialog, msg.obj.toString());
				break;
			case Constant.MSG_GETDATA_SUCCESS:
				DialogUtil.dismissDialog(mActivity.dialog);
				Schedule s = (Schedule) msg.obj;
				mActivity.bindViews(s);
				break;
			default:
				break;
			}
			
		}
	}

	private void init() {
		
		if (dialog == null) {
			dialog=new ProgressDialog(this);
			DialogUtil.init(dialog, false);
		}
			
		
		mHandler = new NewScheduleHandler(this);

		mWorkRB.setChecked(true);
		mPersonalRB.setChecked(false);	
		
		TextWatcher textWatcher = new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			@Override
			public void afterTextChanged(Editable s) {
				int length = mContentET.getText().toString().length();
				int remainWords = 50-length;
				mRemainingTV.setText("您还可以输入"+remainWords+"个字");
			}
		};
		//设置当前时间
		mContentET.addTextChangedListener(textWatcher);
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm",Locale.CHINA);
		String d=df.format(new Date());
		String[] arr=d.split(" ");
		if (null!= arr && arr.length == 2) {
			mStartDateTV.setText(arr[0]);
			mEndDateTV.setText(arr[0]);
			mStartTimeTV.setText(arr[1]);
			mEndTimeTV.setText(arr[1]);
		}
		
	}

	private void setupViews() {
		
		topView.setTitleTextViewEnable(true);
		topView.setTitleTextViewText("新增日程");
		topView.setBackImageButtonEnable(true);
		topView.setBackImageButtonOnClickListener(this);
		topView.setRightTextViewEnable(true);
		topView.setRightTextViewText("保存");
		topView.setLeftTextViewOnClickListener(this);
		topView.setRightTextViewOnClickListener(this);
					
		id_1 = getIntent().getIntExtra("ScheduleId", -2);
		Schedule schedule = (Schedule) getIntent().getSerializableExtra("Schedule");
		if (id_1 >0 || schedule != null) {
			topView.setTitleTextViewText("编辑日程");					
		}
		
		

		// 初始化内容区
		ScheduleSpinnerAdapter adapter = new ScheduleSpinnerAdapter(getLayoutInflater());
		mRemindCycleSP.setAdapter(adapter);
		mRemindCycleSP.setSelection(5, true);

		mSelectStartLL.setOnClickListener(this);
		mSelectEndLL.setOnClickListener(this);
		mWorkRB.setOnClickListener(this);
		mPersonalRB.setOnClickListener(this);
		mRemindCycleSP.setOnItemSelectedListener(this);
		
		if (id_1 >0) {
			//请求数据
			IGetDataBiz biz = new GetDataBizImpl();
			biz.getSchedule(getApplicationContext(), mHandler, id_1);
		}
		if (schedule != null) {
			id_2 = schedule.getId();
			bindViews(schedule);
		}
	}
	
	
	private void bindViews(Schedule s) {
		String startTime=s.getStartTime();
		String endTime = s.getEndTime();
		
		String[] arr1=startTime.split("T");
		if (arr1!=null && arr1.length ==2) {
			mStartDateTV.setText(arr1[0]);
			mStartTimeTV.setText(makeTime(arr1[1]));
		}
		String[] arr2=endTime.split("T");
		if (arr2!=null && arr2.length == 2) {
			mEndDateTV.setText(arr2[0]);
			mEndTimeTV.setText(makeTime(arr2[1]));
		}
		
		int type = s.getScheduleType();
		if (type==1) {
			mWorkRB.setChecked(true);
			mPersonalRB.setChecked(false);
		}else if (type==2) {
			mWorkRB.setChecked(false);
			mPersonalRB.setChecked(true);
		}
					
		String content=s.getScheduleContent();
		mContentET.setText(content);		
			
		
		int r = s.getRemind();
		switch (r) {
		case 1:
			mRemindCycleSP.setSelection(0, true);
			break;
		case 2:
			mRemindCycleSP.setSelection(2, true);
			break;
		case 3:
			mRemindCycleSP.setSelection(3, true);
			break;
		case 4:
			mRemindCycleSP.setSelection(4, true);
			break;
		case 5:
			mRemindCycleSP.setSelection(5, true);
			break;
		case 6:
			mRemindCycleSP.setSelection(1, true);
			break;
		default:
			break;
		}
	}
	
	private String makeTime(String time){
		String result="";		
		String[] str=time.split(":");
	
		
		if (str.length==2 ) {
			result=time;
		}else if (str.length==3) {
			result=str[0]+":"+str[1];
		}		
		return result;
	}
	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.topbar_back_ibtn:// 取消
			this.finish();
			break;

		case R.id.topbar_right_tv:// 保存
					
			if (! checkDateValidity()) {
				UIUtils.ToastMessage(getApplicationContext(), "开始时间必须小于结束时间 ");
				return;
			}
			if (! checkRemindValidity()) {
				UIUtils.ToastMessage(getApplicationContext(), "循环提醒时，结束时间必须和开始时间为同一天");
				return;
			}	
			
			String content = mContentET.getText().toString();
			if (StringUtils.isEmpty(content)) {
				UIUtils.ToastMessage(getApplicationContext(), "请输入日程内容 ");
				return;
			}
								
			IGetDataBiz api = new GetDataBizImpl();
			api.sendNewSchedule(getApplicationContext(), mHandler,getGTAApplication().getUserID(), getData());
			break;
		case R.id.schedule_new_selectstart_ll:// 选择开始时间
			selectTime(true,true);
			break;
		case R.id.schedule_new_selectend_ll:// 选择结束时间
			selectTime(true,false);
			break;
		case R.id.schedule_new_rb1:// 单选框 工作事务
			checkstate = 1;
			mWorkRB.setChecked(true);
			mPersonalRB.setChecked(false);
			break;
		case R.id.schedule_new_rb2:// 单选框 个人事务
			checkstate = 2;
			mWorkRB.setChecked(false);
			mPersonalRB.setChecked(true);
			break;
		default:
			break;
		}

	}
	
	/**
	 * 结束时间必须大于起始时间
	 * @return
	 */
	private boolean checkDateValidity() {			
		String start= mStartDateTV.getText().toString()+" "+ mStartTimeTV .getText().toString();
		String end= mEndDateTV.getText().toString() +" "+mEndTimeTV .getText().toString();					
		return compareDate(start, end);
	}
	private boolean checkRemindValidity(){
		boolean b = false;
											
		if (mRemind == 1 || mRemind == 5) {
			b= true;//除了1和5不检查，其他都检查
		}else {
			String startDate = mStartDateTV.getText().toString().trim();
			String endDate = mEndDateTV.getText().toString().trim();
			if (startDate.equals(endDate)) {
				b = true;
			}	
		}
		
		return b;
	}

	
	boolean compareDate(String start,String end){
		String[] startArray = start.split(" ");
		String[] endArray = end.split(" ");
		
		
		String[] startDate = startArray[0].split("-");
		String[] endDate =endArray[0].split("-");
		
		int startYear = Integer.parseInt(startDate[0]);
		int startMonth = Integer.parseInt(deleteZero(startDate[1]));
		int startDay = Integer.parseInt(deleteZero(startDate[2]));
		
		int endYear = Integer.parseInt(endDate[0]);
		int endMonth = Integer.parseInt(deleteZero(endDate[1]));
		int endDay = Integer.parseInt(deleteZero(endDate[2]));
		
		if (startYear < endYear) {
			return true;
		}else if (startYear == endYear) {
			
			if (startMonth < endMonth) {
				return true;
			}else if (startMonth == endMonth) {
				if (startDay < endDay) {
					return true;
				}else if (startDay == endDay) {
					return compareTime(startArray[1], endArray[1]);
				}
			}
			
			
		}
		
		
		
		return false;
	}
	
	boolean compareTime(String s,String e){
	
		String[] startArr = s.split(":");
		String[] endArr = e.split(":");
						
		int startHour = Integer.parseInt(deleteZero(startArr[0]));
		int startMin = Integer.parseInt(deleteZero(startArr[1]));
								
		int endHour = Integer.parseInt(deleteZero(endArr[0]));
		int endMin = Integer.parseInt(deleteZero(endArr[1]));
		
		
		

		if (startHour < endHour) {				
			return true;					
		}else if (startHour == endHour) {
			int d = endMin - startMin;
			if (d > 0 ) {
				return true;
			}
		} 
		return false;
	}
	
	
	private String deleteZero(String t) {
		String s = null;
		if (!StringUtils.isEmpty(t)) {
			if (t.startsWith("0") && t.length() == 2) {
				s = t.substring(1, t.length());
			} else {
				s = t;
			}
		}
		return s;
	}

	private Schedule getData() {
		Schedule schedule = new Schedule();
		if (id_1 >0) {			
			schedule.setId(id_1);// 用户id(新增/修改)
		}else {
			schedule.setId(id_2);
		}
		
		
		
		String startTime = mStartDateTV.getText().toString() + " "
				+ mStartTimeTV.getText().toString();
		if (!StringUtils.isEmpty(startTime)) {
			schedule.setStartTime(startTime);
		}
		String endTime = mEndDateTV.getText().toString() + " "
				+ mEndTimeTV.getText().toString();
		if (!StringUtils.isEmpty(endTime)) {
			schedule.setEndTime(endTime);
		}
		schedule.setScheduleType(checkstate);
		schedule.setRemind(mRemind);
		
		/*
		 *替换掉所有的空格
		 */
		String content = mContentET.getText().toString().trim().replaceAll(" ", "");		
		schedule.setScheduleContent(content);
		
		return schedule;
	}

	private void selectTime(boolean isAll ,final boolean isStart) {

		final TimePickerView testView=new TimePickerView(this,true);
		
		final WheelMain wheelMain=testView.getWheelMain();
			
		alertDialog = new AlertDialog.Builder(this).create();
		alertDialog.setView(testView);
		alertDialog.show();
		TextView titleTextView = (TextView) alertDialog.getWindow().findViewById(R.id.dialog_tv_title);
		titleTextView.setText(isStart?"选择开始时间":"选择结束时间");
		Button okButton = (Button) alertDialog.getWindow().findViewById(R.id.ok_button_date);
		Button cancelButton = (Button) alertDialog.getWindow().findViewById(R.id.cancel_button_date);
		
		
		okButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if (isStart) {
					mStartDateTV.setText(wheelMain.getYearMonthDay());
					mStartTimeTV.setText(wheelMain.getHourMinute());	

				}else {
					mEndDateTV.setText(wheelMain.getYearMonthDay());
					mEndTimeTV.setText(wheelMain.getHourMinute());

				}		
				alertDialog.dismiss();
			}
		});
		cancelButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				alertDialog.dismiss();
			}
		});
	}

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position,
			long id) {
		// 将选择的赋值给mRmind 提醒周期(1:单次提醒,5:不提醒,6:工作日提醒,2:每日提醒,3:每周提醒,4:每月提醒)
		/**
		 * 			集合中的顺序<——>mRemind的值
		 * 单次：			0			1
		 * 工作日：		1			6	
		 * 每日：			2			2
		 * 每周：			3			3
		 * 每月：			4			4
		 * 不提醒：		5			5
		 */
		
		//根据position来确定mRemind
		switch (position) {
		case 0:		
			mRemind =1;
			break;
		case 1:
			mRemind = 6;
			break;
		case 2:
		case 3:
		case 4:
		case 5:
			mRemind = position;
			break;			
		default:
			break;
		}
		

	}

	@Override
	public void onNothingSelected(AdapterView<?> parent) {

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		mHandler.removeCallbacksAndMessages(this);
		mHandler = null;
	}
}
