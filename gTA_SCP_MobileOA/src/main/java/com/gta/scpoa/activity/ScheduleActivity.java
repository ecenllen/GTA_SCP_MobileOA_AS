package com.gta.scpoa.activity;

import java.lang.ref.WeakReference;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.gta.db.annotation.ViewInject;
import com.gta.scpoa.R;
import com.gta.scpoa.adapter.CalendarAdapter;
import com.gta.scpoa.adapter.ScheduleAdapter;
import com.gta.scpoa.biz.IGetDataBiz;
import com.gta.scpoa.biz.impl.GetDataBizImpl;
import com.gta.scpoa.common.Constant;
import com.gta.scpoa.entity.Schedule;
import com.gta.scpoa.util.DialogUtil;
import com.gta.scpoa.util.UIUtils;
import com.gta.scpoa.views.CommonBottomView;
import com.gta.scpoa.views.CommonTopView;
import com.gta.scpoa.views.picker.TimePickerView;
import com.gta.scpoa.views.picker.WheelMain;

/**
 * 日程页面
 * 
 * @author bin.wang1
 * 
 */
public class ScheduleActivity extends BaseActivity implements OnClickListener,
		OnItemClickListener, OnTouchListener, GestureDetector.OnGestureListener {
	@ViewInject(id = R.id.sche_top_view)
	private CommonTopView topView;
	@ViewInject(id = R.id.sche_bottom_view)
	private CommonBottomView bottomView;
	
	private GestureDetector mGestureDetector = null;
	private CalendarAdapter calendarAdapter;

	@ViewInject(id = R.id.schedule_flipper)
	private ViewFlipper mFlipper = null;

	private GridView gridView = null;

	private static int jumpMonth = 0; // 每次滑动，增加或减去一个月,默认为0（即显示当前月）
	private static int jumpYear = 0; // 滑动跨越一年，则增加或者减去一年,默认为0(即当前年)


	@ViewInject(id = R.id.schedule_month_tv)
	private TextView currentMonthTV;

	@ViewInject(id = R.id.schedule_today_tv)
	private TextView mTodayTV;

	@ViewInject(id = R.id.schedule_listview)
	private ListView mScheduleLV;

	private int state = 0;// 用于记录你是跳转到下一月(1)还是上一月(-1)，本月为0。
	private ScheduleHandler mHanlder;

	private ScheduleAdapter scheduleAdapter;

	private ProgressDialog progressDialog;
	
	private AlertDialog alertDialog = null;
	private List<Schedule> list;//所有的日程集合
    public int currentPressedPositon = -1;
    
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA);
    
	private int year_c = 0;// 当前年
	private int month_c = 0;// 当前月
	private int day_c = 0;// 当前日期

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_schedule);
		init();
		setupViews();
		
	}

	@Override
	protected void onResume() {
		super.onResume();	
		startSetData();	
		
	}
	
	private void init() {


		this.progressDialog=new ProgressDialog(this);
		DialogUtil.init(progressDialog,  false);
			
		String currentDate = sdf.format(new Date()); // 当期日期
		String[] array = currentDate.split("-");
		String year = array[0];
		String month = array[1];
		String day = array[2];
		
		year_c = Integer.parseInt(year);
		month_c = noZero(month);
		day_c = noZero(day);
		/**
		 * startSetData->setData
		 * 获取到服务器一个月的日程数据list之后，执行顺序为divideSchedule()->SetCalendarData\setScheduleData
		 * 
		 */
		mHanlder = new ScheduleHandler(this);
	}
	
	
	private static class ScheduleHandler extends Handler{
		WeakReference<ScheduleActivity> wr;
		public ScheduleHandler(ScheduleActivity activity) {
			super();
			this.wr = new WeakReference<ScheduleActivity>(activity);
		}

		@Override
		public void handleMessage(Message msg) {
			ScheduleActivity mActivity=wr.get();
			switch (msg.what) {
			case Constant.MSG_SHOW_PROGRESS:
				DialogUtil.showDialog(mActivity.progressDialog, msg.obj.toString());
				break;
				
			//数据			
			case Constant.MSG_GETDATA_SUCCESS:
				DialogUtil.dismissDialog(mActivity.progressDialog);
				List<Schedule> schedules = (List<Schedule>) msg.obj;
				mActivity.list= mActivity.increase(schedules);
				mActivity.divideSchedules(mActivity.list);
				break;
				
			case Constant.MSG_FAIL://返回失败信息
				DialogUtil.dismissDialog(mActivity.progressDialog);
				UIUtils.ToastMessage(mActivity.getApplicationContext(), msg.obj.toString());
				break;
								
			//操作						
			case Constant.MSG_DELETE_SCHEDULE_SUCCESS:
				DialogUtil.dismissDialog(mActivity.progressDialog);	
				UIUtils.ToastMessage(mActivity.getApplicationContext(), "日程已成功删除 !");				
				mActivity.deleteLocalSchedules(msg.arg1);
				break;			
			case Constant.MSG_UPDATE_SCHEDULE_SUCCESS:
				DialogUtil.dismissDialog(mActivity.progressDialog);	
				UIUtils.ToastMessage(mActivity.getApplicationContext(), "日程已成功更新 !");
				mActivity.updateLocalSchedules(msg.arg1, msg.arg2);
				break;
				
				
//			case 2://返回操作成功
//				DialogUtil.dismissDialog(mActivity.progressDialog);			
//				UIUtils.ToastMessage(mActivity.getApplicationContext(), msg.obj.toString());									
//				break;	
				
			default:
				break;
			}
			
		}
	}
		

	public void startSetData() {
		String year = calendarAdapter.getShowYear();
		String month = calendarAdapter.getShowMonth();
		
		if (month.startsWith("0") && month.length() == 2) {
			month = month.substring(1, month.length());
		}
		setData(Integer.parseInt(year), Integer.parseInt(month));
	}

	public List<Schedule> increase(List<Schedule> schedules) {
		
		
		
		List<Schedule> all = new ArrayList<Schedule>();
			
		Iterator<Schedule> it = schedules.iterator(); 
		while(it.hasNext()){  
		    Schedule e = it.next();
		    
		    if(e.getRemind() == 5 || e.getRemind() == 1){
		    	//再插入一个小集合
				List<Schedule> small = clone(e,calendarAdapter.getShowYear()+"-"+calendarAdapter.getShowMonth()+"-"+"01");
				all.addAll(small);
		    	it.remove();  
		    }  
		}  		
			
		all.addAll(schedules);
		
		return all;
	}
	
	public List<Schedule> clone(Schedule item,String currentTime){
		
		String[] startTimeArray = item.getStartTime().split("T")[0].split("-");
		String[] endTimeArray = item.getEndTime().split("T")[0].split("-");
		String[] currentTimeArray = currentTime.split("-");
				
		int startY = parseInt(startTimeArray[0]);
		int startM = noZero(startTimeArray[1]);
		int startD = noZero(startTimeArray[2]);
		
		int endY = parseInt(endTimeArray[0]);
		int endM = noZero(endTimeArray[1]);
		int endD = noZero(endTimeArray[2]);
		
		int currentY = parseInt(currentTimeArray[0]);
		int currentM = noZero(currentTimeArray[1]);
		
		//月初月末日子的值
		String m_s = getMonthStartDay(currentTime);
		String m_e = getMonthEndDay(currentTime);
		int monthStart = noZero(m_s);
		int monthEnd = noZero(m_e);
		
		
		if (currentY < endY) {
					
			if (startY < currentY ) {
				//整个月
				return all_the_month(item, monthStart, monthEnd);
			}else if (startY == currentY) {
				//比较月份
				if (startM < currentM) {
					//整个月
					return all_the_month(item, monthStart, monthEnd);
				}else if (startM == currentM) {
					//startD到月末
					return startD_end_of_month(item, startD, monthEnd);
				}
				
			}
			
		}else if (currentY == endY) {
			
			if (startY < currentY ) {
				//比较月
				if (currentM <endM) {
					//整个月
					return all_the_month(item, monthStart, monthEnd);
				}else if (currentM == endM) {
					//月初到endD
					return beginning_of_month_endD(item, monthStart, endD);
				}
			}else if (startY == currentY) {
				//比较月
				if (startM == currentM && currentM == endM) {
					//startD到endD
					return startD_endD(item, startD, endD);
				}else if (startM < currentM && currentM < endM) {
					//整个月
					return all_the_month(item, monthStart, monthEnd);
				}else if (startM < currentM && currentM == endM) {
					//月初到endD
					return beginning_of_month_endD(item, monthStart, endD);
				}else if (startM == currentM && currentM < endM) {
					//startD到月末
					return startD_end_of_month(item, startD, monthEnd);
				}
				
				
			}
			
		}
		
	
		
//		if (startY <= currentY && currentY <= endY) {
//		
//			if (startM < currentM && currentM < endM) {
//				//整个currentM
//				clone = doClone(item,monthStart,monthEnd);			
//			}else if (startM < currentM && currentM == endM) {
//				//月初到endDay
//				clone = doClone(item, monthStart, endD);
//			}else if (startM == currentM && currentM < endM) {
//				//startDay到月末
//				clone = doClone(item, startD, monthEnd);
//			}else if (startM == currentM && currentM == endM) {
//				//startDay 到endDay
//				clone = doClone(item, startD, endD);
//			}
//					
//		}	
		return new ArrayList<Schedule>();	
	}
	/**
	 * 月初到endD
	 * @return 
	 */
	List<Schedule> beginning_of_month_endD(Schedule item,int monthStart,int endD){
		//月初到endDay
		return doClone(item, monthStart, endD);
	}
	/**
	 * startD到月末
	 * @return 
	 */
	List<Schedule> startD_end_of_month(Schedule item,int startD,int monthEnd){
		//startDay到月末
		return doClone(item, startD, monthEnd);
	}
	/**
	 * startD到endD
	 * @return 
	 */
	List<Schedule> startD_endD(Schedule item,int startD,int endD){
		//startDay 到endDay
		return doClone(item, startD, endD);
	}
	/**
	 * 整个月
	 * @return 
	 */
	List<Schedule> all_the_month(Schedule item,int monthStart,int monthEnd){
		//整个currentM
		return doClone(item,monthStart,monthEnd);
	}
	
	
	
	
	
	
	public List<Schedule> doClone(Schedule item,int start,int end){
		List<Schedule> small = new ArrayList<Schedule>();
		
		for (int i = start; i <= end; i++) {
						
			String oldStartTime = item.getStartTime().split("T")[1];
			String oldEndTime = item.getEndTime().split("T")[1];
			String newStartTime = year_c+"-"+month_c+"-"+i+"T"+oldStartTime;
			String newEndTime = year_c+"-"+month_c+"-"+i+"T"+oldEndTime;
			
			Schedule schedule = new Schedule();
			schedule.setHasNoti(item.isHasNoti());
			schedule.setId(item.getId());
			schedule.setIsExternal(item.isIsExternal());
			schedule.setRemind(item.getRemind());
			schedule.setScheduleContent(item.getScheduleContent());
			schedule.setScheduleType(item.getScheduleType());
			schedule.setStatus(item.getStatus());
			
			schedule.setStartTime(newStartTime);
			schedule.setEndTime(newEndTime);
			
			small.add(schedule);
		}
		
		Log.i("wangbin", small.toString());
		return small;
	}
	
	
	public int parseInt(String value){
		return Integer.parseInt(value);
	}
	
	
	/** 
     * 获取月份起始日期 
     * @param date 
     * @return 
     * @throws ParseException 
     */  
    public String getMonthStartDay(String date) {  
        Calendar calendar = Calendar.getInstance();  
        try {
			calendar.setTime(sdf.parse(date));
		} catch (ParseException e) {
			e.printStackTrace();
		}  
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMinimum(Calendar.DAY_OF_MONTH)); 
        String monthStart = sdf.format(calendar.getTime());
        
        return monthStart.split("-")[2];  
    }  
      
    /** 
     * 获取月份最后日期 
     * @param date 
     * @return 
     * @throws ParseException 
     */  
    public String getMonthEndDay(String date){  
        Calendar calendar = Calendar.getInstance();  
        try {
			calendar.setTime(sdf.parse(date));
		} catch (ParseException e) {
			e.printStackTrace();
		}  
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH)); 
        String monthEnd = sdf.format(calendar.getTime());
        return  monthEnd.split("-")[2];  
    }  
	
	

	private void setData(int year, int month) {
		String temp = "-01";
		String beginDate = null;
		String endDate = null;

		if (month == 12) {
			beginDate = year + "-" + "12" + temp;
			endDate = (year + 1) + "-" + "01" + temp;
		} else {
			beginDate = year + "-" + (month < 10 ? ("0" + month) : month)+ temp;
			int nextMonth = month + 1;
			endDate = year + "-"+ (nextMonth < 10 ? ("0" + nextMonth) : nextMonth) + temp;
		}
		IGetDataBiz api=new GetDataBizImpl();
		api.getScheduleList(getApplicationContext(), mHanlder, getGTAApplication().getUserID(),beginDate,endDate);
	}

	private void setupViews() {

		
		topView.setAddImageButtonEnable(true);
		topView.setBackImageButtonEnable(true);
		topView.setBackImageButtonOnClickListener(this);
		topView.setTitleTextViewEnable(true);
		topView.setTitleTextViewText(getString(R.string.schedule_title));
			
		bottomView.setIsMainActivity(true);

	

		mGestureDetector = new GestureDetector(this, this);
		mTodayTV.setOnClickListener(this);
		currentMonthTV.setOnClickListener(this);
		mFlipper.removeAllViews();
		jumpMonth = 0;
		jumpYear = 0;

		calendarAdapter = new CalendarAdapter(this, jumpMonth,jumpYear, year_c, month_c, day_c);
		calendarAdapter.setScheduleData(null, null);
		inflaterGridview();

		gridView.setAdapter(calendarAdapter);
		
		mFlipper.addView(gridView, 0);
		addTextToTopTextView(currentMonthTV);

		// 为日程ListView设置适配器
		scheduleAdapter = new ScheduleAdapter(this,	null);
		mScheduleLV.setAdapter(scheduleAdapter);
	}
	
	/**
	 * 对不提醒的 跨天的日程进行扩增
	 */
	
	
	

	/**
	 * 根据status，将服务器返回的List总集合，分成未完成和已完成的两个集合
	 */
	protected void divideSchedules(List<Schedule> all) {	
		
		ArrayList<Schedule> finished= divideFinished(all);
		ArrayList<Schedule> unfinished = divideUnfinished(all);
		
		SetCalendarData(finished, unfinished);
		setScheduleData(calendarAdapter.getDateByClickItem(currentPressedPositon));
	}

	
	//重写方法
	/**
	 * 分离已完成的日程
	 * @param 总日程
	 * @return
	 */
	private ArrayList<Schedule> divideFinished(List<Schedule> all){		
		ArrayList<Schedule> schedules = new ArrayList<Schedule>();		
		for (int i = 0; i < all.size(); i++) {
			 if (all.get(i).getStatus() == 1) {
				// 已完成
				 schedules.add(all.get(i));
			}
		}	
		return schedules;
	}
	/**
	 * 分离未完成的日程 
	 * @param all 总日程
	 * @return
	 */
	private ArrayList<Schedule> divideUnfinished(List<Schedule> all){
		ArrayList<Schedule> schedules = new ArrayList<Schedule>();			
		for (int i = 0; i < all.size(); i++) {
			if (all.get(i).getStatus() == 0) {
				// 未完成
				schedules.add(all.get(i));
			}
		}			
		return schedules;
	}
	
	private void SetCalendarData(List<Schedule> finished, List<Schedule> unfinished) {
		//开始时间
		//已完成
		ArrayList<Integer> startFinished = new ArrayList<Integer>();
		//未完成
		ArrayList<Integer> startUnfinished = new ArrayList<Integer>();

		// 对于已完成的日程
		for (Schedule schedule : finished) {
											
			int start = parseDate(schedule.getStartTime());
			startFinished.add(start);
								
		}
		
		// 对于未完成的日程
		for (Schedule s : unfinished) {
			
			int startTime = parseDate(s.getStartTime());				
			startUnfinished.add(startTime);		
			
		}
		


		calendarAdapter.setScheduleData(startFinished, startUnfinished);
		calendarAdapter.notifyDataSetChanged();
		
		
	}
	
	/**	
	 * 
	 * 根据当前的日期，从集合中获取相关日程数据
	 * date是你要显示的该天的日程。
	 */
	private void setScheduleData(int date) {
		// date 如2015-01-15 由于本次的总的日程中只有本月的，所以只需匹配day
		int currentDay = date;	
		ArrayList<Schedule> currentSchedules = new ArrayList<Schedule>();
		/**
		 * 注意：只以开始时间为比较的标准
		 */
		if (list == null ) {
			return;
		}
		for (Schedule schedule : list) {
			int start = parseDate(schedule.getStartTime());
			if (currentDay == start) {
				currentSchedules.add(schedule);
			}
		}
					
		scheduleAdapter.setData(currentSchedules);
		scheduleAdapter.notifyDataSetChanged();	
		//保存本月今日的所有日程
		if (day_c == date) {				
			getGTAApplication().setTodayScheTemp(currentSchedules);		
		}
	}
	
	/**
	 * 根据年月日获取日，且不是"01"格式
	 * 
	 * @param date
	 */
	private int noZero(String date) {

		Log.i("wangbin", date);
		if (date.startsWith("0") && date.length() >= 2) {
			date = date.substring(1, date.length());
		}
		
		return Integer.parseInt(date);
	}

	private void inflaterGridview() {
		gridView = (GridView) getLayoutInflater().inflate(
				R.layout.view_schedule_gridview, null);
		gridView.setOnItemClickListener(this);
		gridView.setOnTouchListener(this);
	}

	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
			float velocityY) {
		int gvFlag = 0; // 每次添加gridview到viewflipper中时给的标记
		if (e1.getX() - e2.getX() > 30) {
			gvFlag++;
			enterNextMonth(gvFlag);
			return true;
		} else if (e1.getX() - e2.getX() < -30) {
			gvFlag++;
			enterPrevMonth(gvFlag);
			return true;
		}
		return false;
	}

	private float dowmX, dowmY;

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			dowmX = event.getX();
			dowmY = event.getY();
			break;
		case MotionEvent.ACTION_MOVE:
			if (Math.abs(dowmX - event.getX()) < Math.abs(dowmY - event.getY())) {
				return true;
			}
			if (Math.abs(dowmY - event.getY()) > 13) {
				return true;
			}
			break;
		default:
			break;
		}
		return mGestureDetector.onTouchEvent(event);
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		calendarAdapter.setPressedPosition(position);
		calendarAdapter.notifyDataSetChanged();	
		
		int monthStart=calendarAdapter.getMonthStartPosition();
		int monthEnd=calendarAdapter.getMonthEndPosition();
		
		if (position < monthEnd && position >= monthStart) {				
			// 点击任何一个item，得到这个item的日期			
			setScheduleData(calendarAdapter.getDateByClickItem(position));		
			scheduleAdapter.notifyDataSetChanged();
		}else{
			scheduleAdapter.setData(null);
			scheduleAdapter.notifyDataSetChanged();
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.topbar_back_ibtn:
			this.finish();
			break;
		case R.id.schedule_month_tv:		
			showTimePicker();		
			break;
		case R.id.schedule_today_tv:
			if (state == 0) {
				//颜色变为今天			
			calendarAdapter.setPressedPosition(calendarAdapter.getTodayPosition());
			calendarAdapter.notifyDataSetChanged();		
			setScheduleData(day_c);		
			scheduleAdapter.notifyDataSetChanged();		
				return;
			}
			enterCurrentMonth();
			break;
		}

	}

	private void showTimePicker() {
	
		String cdate=currentMonthTV.getText().toString();
		String[] a1 = cdate.split("年");
		String m = a1[1].substring(0, a1[1].indexOf("月"));		
			
		TimePickerView view=new TimePickerView(this,Integer.parseInt(a1[0]),Integer.parseInt(m)-1);	
		final WheelMain wheelMain=view.getWheelMain();	
		
		alertDialog = new AlertDialog.Builder(this).create();
		alertDialog.setView(view);
		alertDialog.show();
		Button okButton = (Button) alertDialog.getWindow().findViewById(R.id.ok_button_date);
		Button cancelButton = (Button) alertDialog.getWindow().findViewById(R.id.cancel_button_date);
		okButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				currentMonthTV.setText(wheelMain.getYearMonth());
				enterSelectedMonth(wheelMain.getYear(), wheelMain.getMonth());
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


	/**
	 * 移动到上一个月
	 */
	private void enterPrevMonth(int gvFlag) {
		
		state--;
		inflaterGridview();// 添加一个gridView
		jumpMonth--; // 上一个月
		calendarAdapter.setData(jumpMonth, jumpYear, year_c, month_c);
		calendarAdapter.setPressedPosition(currentPressedPositon);
		gridView.setAdapter(calendarAdapter);
		addTextToTopTextView(currentMonthTV); // 移动到上一月后，将当月显示在头标题中
		mFlipper.addView(gridView, gvFlag);
		mFlipper.setInAnimation(AnimationUtils.loadAnimation(this,R.anim.push_right_in));
		mFlipper.setOutAnimation(AnimationUtils.loadAnimation(this,	R.anim.push_right_out));
		mFlipper.showPrevious();
		mFlipper.removeViewAt(0);
		startSetData();
	}

	protected void enterSelectedMonth(int targetYear, int targetMonth) {
		
		if (year_c == targetYear) {
			jumpMonth = targetMonth - month_c;
		}else if(targetYear < year_c) {
			jumpMonth = -((12-targetMonth)+(year_c- 1-targetYear)*12+month_c);
		}else if (year_c < targetYear) {
			jumpMonth = (12-month_c)+(targetYear-1-year_c)*12+targetMonth;
		}
		
		//只操作 jumpMonth
				
		inflaterGridview();
		mFlipper.removeAllViews();
		calendarAdapter.setData(jumpMonth, jumpYear, year_c, month_c);
		
		calendarAdapter.setPressedPosition(currentPressedPositon);
		gridView.setAdapter(calendarAdapter);
		addTextToTopTextView(currentMonthTV);
		mFlipper.addView(gridView, 0);
		
		if (targetYear < year_c) {
			//负数
			state = state-((12-targetMonth) + (year_c-targetYear-1)*12 + month_c);		
		}else if (year_c == targetYear) {		
			state = state+(targetMonth -month_c);			
		}else if (year_c < targetYear) {
			//正数
			state = state+(12-month_c) + (targetYear-year_c-1)*12 + targetMonth;
		}
		
		setData(targetYear, targetMonth);
	}
	
	
	private void enterCurrentMonth() {

		jumpMonth = 0;
		jumpYear = 0;
		
		inflaterGridview();
		mFlipper.removeAllViews();

		calendarAdapter.setData(jumpMonth, jumpYear, year_c, month_c);
		this.currentPressedPositon =-1;
		gridView.setAdapter(calendarAdapter);
		addTextToTopTextView(currentMonthTV);
		mFlipper.addView(gridView, 0);
			
		if (state < 0) {
			mFlipper.setInAnimation(AnimationUtils.loadAnimation(this,
					R.anim.push_left_in));
			mFlipper.setOutAnimation(AnimationUtils.loadAnimation(this,
					R.anim.push_left_out));
			mFlipper.showNext();
		} else if (state > 0) {
			mFlipper.setInAnimation(AnimationUtils.loadAnimation(this,
					R.anim.push_right_in));
			mFlipper.setOutAnimation(AnimationUtils.loadAnimation(this,
					R.anim.push_right_out));
			mFlipper.showPrevious();
		}

		state = 0;
		setData(year_c, month_c);
	}

	/**
	 * 移动到下一个月
	 */
	private void enterNextMonth(int gvFlag) {
		state++;
		
		inflaterGridview();
		jumpMonth++; // 下一个月

		calendarAdapter.setData(jumpMonth, jumpYear, year_c, month_c);
		calendarAdapter.setPressedPosition(currentPressedPositon);
		gridView.setAdapter(calendarAdapter);

		addTextToTopTextView(currentMonthTV); // 移动到下一月后，将当月显示在头标题中
		mFlipper.addView(gridView, gvFlag);
		// 设置 过场动画
		mFlipper.setInAnimation(AnimationUtils.loadAnimation(this,
				R.anim.push_left_in));
		mFlipper.setOutAnimation(AnimationUtils.loadAnimation(this,
				R.anim.push_left_out));
		mFlipper.showNext();
		mFlipper.removeViewAt(0);
		startSetData();
	}

	/**
	 * 添加头部的年份 闰哪月等信息
	 */
	public void addTextToTopTextView(TextView view) {
		StringBuffer textDate = new StringBuffer();
		textDate.append(calendarAdapter.getShowYear()).append("年")
				.append(calendarAdapter.getShowMonth()).append("月")
				.append("\t");
		view.setText(textDate);
	}
	
	/*
	 * 手势相关的方法
	 */

	@Override
	public boolean onDown(MotionEvent e) {
		return false;
	}

	@Override
	public void onShowPress(MotionEvent e) {

	}

	@Override
	public boolean onSingleTapUp(MotionEvent e) {
		return false;
	}

	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
			float distanceY) {
		return false;
	}

	@Override
	public void onLongPress(MotionEvent e) {

	}
	
	public void updateScheduleStatus(int id,int status){
		IGetDataBiz api=new GetDataBizImpl();
		api.changeScheduleStatus(getApplicationContext(), mHanlder, id, status);
	}
	public void deleteSchedule(int id){
		IGetDataBiz api=new GetDataBizImpl();
		api.deleteSchedule(getApplicationContext(), mHanlder, id);
	}
	
	/**
	 * 删除本地集合中的一条日程
	 */
	private void deleteLocalSchedules(int id){
		
//		注意： 下面的使用for循环的删除方式是错误的	，必须使用迭代器。
//		for (int i = 0; i < list.size(); i++) {
//			Schedule s = list.get(i);
//			if ( id == s.getId()) {
//				list.remove(i);				
//			}
//		}
			
		Iterator<Schedule> it = list.iterator(); 
		while(it.hasNext()){  
		    Schedule e = it.next();  		    
		    if(e.getId() == id){  
		    	it.remove();  
		    }  
		}  		
		divideSchedules(list);
	}
	/**
	 * 更新本地集合中的一条日程
	 */
	
	private void updateLocalSchedules(int id,int status){		
		for (int i = 0; i < list.size(); i++) {
			Schedule s = list.get(i);
//			boolean b =compareDay(s.getStartTime(), calendarAdapter.getDateByClickItem(currentPressedPositon)); 
			if (s.getId() == id) {
				s.setStatus(status);			
			}
		}
		divideSchedules(list);
	}
	
	private int parseDate(String string){
		string = string.substring(0, string.indexOf("T"));
		String[] array = string.trim().split("-");
		
		return noZero(array[2]);
	}
	
	/**
	 * 
	 * @param startDate StartTime 2015-02-02T14:54:00
	 * @param day
	 * @return
	 */
	private boolean compareDay(String startDate,int day){
		boolean b = false;
	
		int scheduleDay = parseDate(startDate);
		
		if (day == scheduleDay) {
			b = true;
		}		
		return b;
	}
			
}
