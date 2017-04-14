package com.gta.scpoa.adapter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import android.graphics.Color;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.gta.scpoa.R;
import com.gta.scpoa.activity.ScheduleActivity;
import com.gta.scpoa.entity.CalendarBox;

public class CalendarAdapter extends BaseAdapter {

	private ScheduleActivity mActivity;

	private int today = 0;
	private int thisMonth = 0;
	private int thisYear = 0;

	private ArrayList<Integer> finishedStartDays;
	private ArrayList<Integer> unfinishedStartDays;
	
	private int todayPosition;

	/** 是否为闰年 */
	private boolean isLeapyear = false;
	/** 某月的总天数 */
	private int daysOfMonth = 0;
	/** 某月第一天为星期几 */
	private int dayOfWeek = 0;
	/** 上一个月的总天数 */
	private int lastDaysOfMonth = 0;

	private String[] dayNumber = new String[42]; // 控制多少GridView多少行，至少5行，共5*7=35个格子
	private CalendarBox sc = null;
	private String targetYear = "";
	private String targetMonth = "";

	
	private int currentFlag = -1; // 用于标记当天
	private int pressedPosition = -1;

	private String showYear = ""; // 用于在头部显示的年份
	private String showMonth = ""; // 用于在头部显示的月份

	// 系统当前时间
	private String sys_year = "";
	private String sys_month = "";
	private String sys_day = "";

	public CalendarAdapter() {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-M-d",	Locale.CHINA);
		String sysDate = sdf.format(new Date()); // 当期日期
		sys_year = sysDate.split("-")[0];
		sys_month = sysDate.split("-")[1];
		sys_day = sysDate.split("-")[2];
	}

	public CalendarAdapter(ScheduleActivity activity, int jumpMonth,
			int jumpYear, int currentYear, int currentMonth, int today) {
		this();
		this.mActivity = activity;
		sc = new CalendarBox();
		this.today = today;
		this.thisMonth = currentMonth;
		this.thisYear = currentYear;
		setData(jumpMonth, jumpYear, currentYear, currentMonth);

	}
	
	public void setScheduleData(ArrayList<Integer> finished,ArrayList<Integer> unfinishedStart) {

		if (null != finished) {
			this.finishedStartDays.clear();
			this.finishedStartDays = finished;
		} else {
			this.finishedStartDays = new ArrayList<Integer>();
		}

		if (null != unfinishedStart) {		
			this.unfinishedStartDays = unfinishedStart;
		} else {
			this.unfinishedStartDays = new ArrayList<Integer>();
		}
	}
	


	public void setData(int jumpMonth, int jumpYear, int currentYear,
			int currentMonth) {
		currentFlag = -1;
		pressedPosition = -1;
		initCalendar(jumpMonth, jumpYear, currentYear, currentMonth);
	}

	/**
	 * 初始化目标日历
	 * 
	 * @param jumpMonth
	 * @param jumpYear
	 * @param currentYear
	 * @param currentMonth
	 */
	private void initCalendar(int jumpMonth, int jumpYear, int currentYear,
			int currentMonth) {
		int stepYear = currentYear + jumpYear;
		int stepMonth = currentMonth + jumpMonth;
		if (stepMonth > 0) {
			// 往下一个月滑动
			if (stepMonth % 12 == 0) {
				stepYear = currentYear + stepMonth / 12 - 1;
				stepMonth = 12;
			} else {
				stepYear = currentYear + stepMonth / 12;
				stepMonth = stepMonth % 12;
			}
		} else {
			// 往上一个月滑动
			stepYear = currentYear - 1 + stepMonth / 12;
			stepMonth = stepMonth % 12 + 12;
			if (stepMonth % 12 == 0) {
			}
		}
		// (jumpMonth为滑动的次数，每滑动一次就增加一月或减一月)
		targetYear = String.valueOf(stepYear); // 得到当前的年份
		targetMonth = String.valueOf(stepMonth); // 得到本月
		getCalendar(Integer.parseInt(targetYear), Integer.parseInt(targetMonth));
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return dayNumber.length;
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		final View view;
		final ViewHolder holder;
		if (null == convertView) {
			view = mActivity.getLayoutInflater().inflate(R.layout.item_schedule_gridview, parent,
					false);
			holder = new ViewHolder();
			assert view != null;
			holder.mContainerFL = (FrameLayout) view
					.findViewById(R.id.schedule_item_container);
			holder.mDateTV = (TextView) view
					.findViewById(R.id.schedule_date_tv);
			holder.mColorTV = (TextView) view
					.findViewById(R.id.schedule_bgcolor_tv);
			view.setTag(holder);
		} else {
			// 可复用
			view = convertView;
			holder = (ViewHolder) view.getTag();
		}

		bindView(holder, position);

		return view;

	}

	private void bindView(ViewHolder holder, int position) {
		String d = dayNumber[position];

		SpannableString sp = new SpannableString(d);
		sp.setSpan(new StyleSpan(android.graphics.Typeface.NORMAL), 0,
				d.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
		sp.setSpan(new RelativeSizeSpan(1.2f), 0, d.length(),
				Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

		holder.mDateTV.setText(sp);// 设置日历数字
		holder.mDateTV.setTextColor(Color.parseColor("#C6C6C6"));
		holder.mColorTV.setBackgroundResource(0);//0 to remove the background.
		holder.mColorTV.setVisibility(View.GONE);
		
		
		/**
		 * 设置本月的颜色、背景等。
		 */
		if (position < daysOfMonth + dayOfWeek && position >= dayOfWeek) {
			
			
			
			int currentDay = getDateByClickItem(position);
			
			
			// 针对已完成的日程赋予颜色
			if (finishedStartDays.contains(getDateByClickItem(position))) {
				holder.mColorTV.setVisibility(View.VISIBLE);
				holder.mColorTV.setBackgroundResource(R.drawable.schedule_c2);
			}

			// 针对未完成的日程，根据过去和将来赋予不同的颜色

			
			int currentMonth = cutOutZero(getShowMonth());
			int currentYear = cutOutZero(getShowYear());
	
			if (unfinishedStartDays.contains(currentDay)) {

				if (currentYear == thisYear) {
					if (currentMonth == thisMonth) {
						/** 核心比较开始 */
						holder.mColorTV.setVisibility(View.VISIBLE);
						if (currentDay < today) {
							// 以前未完成 红色
							holder.mColorTV	.setBackgroundResource(R.drawable.schedule_c1);
						} else if (today <= currentDay) {// 今日如果有未完成的，也属于将来未完成
							// 将来未完成 蓝色
							holder.mColorTV	.setBackgroundResource(R.drawable.schedule_c3);
						}
						/** 核心比较结束 */
					} else if (currentMonth < thisMonth) {
						
						
						// 当前月在本月之前，过去，一律显示红色
						drawRed(holder);
						
						
						
					} else if (thisMonth < currentMonth) {
						
						
						// 当前月在本月之后，将来一律显示蓝色
						drawBlue(holder);
						
						
					}
				} else if (currentYear < thisYear) {
					// 当前年在今年之前，过去，一律显示红色
					drawRed(holder);
				} else if (thisYear < currentYear) {
					// 当前年在今年之后，将来，一律显示蓝色
					drawBlue(holder);
				}

			}

			// holder.mDateTV.setText(sp);// 仅设置本月的日历数字，非本月无数字
			holder.mDateTV.setTextColor(Color.BLACK);// 设置本月的数字的字体颜色
			/*
			 * 如果该天是周末
			 */
			if (position % 7 == 0 || position % 7 == 6) {

			}
		}


		
		//设置被点击的颜色
		if (position == pressedPosition) {
			holder.mContainerFL	.setBackgroundResource(R.drawable.schedule_bule_bg_box);
			holder.mDateTV.setTextColor(Color.WHITE);
			mActivity.currentPressedPositon=position;
		}else {
			holder.mContainerFL	.setBackgroundResource(R.drawable.schedule_white_bg_box);
//			holder.mDateTV.setTextColor(Color.BLACK);
		}
		
		
		// 设置当天的背景
		if (currentFlag == position) {
			todayPosition = position;
			holder.mContainerFL	.setBackgroundResource(R.drawable.schedule_bule_bg_box);
			holder.mDateTV.setTextColor(Color.WHITE);
			mActivity.currentPressedPositon=position;
		} else {		
//			holder.mContainerFL	.setBackgroundResource(R.drawable.schedule_white_bg_box);			
		}
				
		// 设置完成的状态
		
	}

	public void setPressedPosition(int positon) {
		pressedPosition = positon;
		currentFlag = -1;
	}

	private void drawBlue(ViewHolder holder) {
		holder.mColorTV.setVisibility(View.VISIBLE);
		// 一般设置为将来未完成
		holder.mColorTV.setBackgroundResource(R.drawable.schedule_c3);
	}

	private void drawRed(ViewHolder holder) {
		holder.mColorTV.setVisibility(View.VISIBLE);
		holder.mColorTV.setBackgroundResource(R.drawable.schedule_c1);
	}

	// 得到某年的某月的天数且这月的第一天是星期几
	public void getCalendar(int year, int month) {
		isLeapyear = sc.isLeapYear(year);
		daysOfMonth = sc.getDaysOfMonth(isLeapyear, month);
		dayOfWeek = sc.getWeekdayOfMonth(year, month);
		lastDaysOfMonth = sc.getDaysOfMonth(isLeapyear, month - 1);
		getweek(year, month);
	}

	// 将一个月中的每一天的值添加入数组dayNuber中
	private void getweek(int year, int month) {
		int j = 1;

		// 得到当前月的所有日程日期(这些日期需要标记)

		for (int i = 0; i < dayNumber.length; i++) {
			// 周一
			if (i < dayOfWeek) { // 前一个月
				int temp = lastDaysOfMonth - dayOfWeek + 1;
				dayNumber[i] = (temp + i) + "";
			} else if (i < daysOfMonth + dayOfWeek) { // 本月
				String day = String.valueOf(i - dayOfWeek + 1); // 得到的日期

				dayNumber[i] = i - dayOfWeek + 1 + "";
				// 对于当前月才去标记当前日期
				if (sys_year.equals(String.valueOf(year))
						&& sys_month.equals(String.valueOf(month))
						&& sys_day.equals(day)) {
					// 标记当前日期
					currentFlag = i;
				}
				setShowYear(String.valueOf(year));
				setShowMonth(String.valueOf(month));
			} else { // 下一个月
				dayNumber[i] = j + "";
				j++;
			}
		}
	}
	
	public int getTodayPosition(){
		return todayPosition;
	}
	/**
	 * 获取本月的开始位置
	 * @return
	 */
	public int getMonthStartPosition(){
		return dayOfWeek;
	}
	/**
	 * 获取本月的结束位置
	 * @return
	 */
	public int getMonthEndPosition(){
		return daysOfMonth + dayOfWeek;
	}
	
	/**
	 * 点击每一个item时返回item中的日期
	 * 
	 * @param position
	 * @return
	 */
	public int getDateByClickItem(int position) {
		return Integer.parseInt((dayNumber[position]));
	}

	/**
	 * 在点击gridView时，得到这个月中第一天的位置
	 * 
	 * @return
	 */
	public int getStartPositon() {
		return dayOfWeek + 7;
	}

	/**
	 * 在点击gridView时，得到这个月中最后一天的位置
	 * 
	 * @return
	 */
	public int getEndPosition() {
		return (dayOfWeek + daysOfMonth + 7) - 1;
	}

	public String getShowYear() {
		return showYear;
	}

	public void setShowYear(String showYear) {
		this.showYear = showYear;
	}

	public String getShowMonth() {
		return showMonth;
	}

	public void setShowMonth(String showMonth) {
		this.showMonth = showMonth;
	}

	/**
	 * 根据年月日获取日，且不是"01"格式
	 * 
	 * @param date
	 */
	private int cutOutZero(String string) {
		if (string.startsWith("0") && string.length() == 2) {
			string = string.substring(1, string.length());
		}
		return Integer.parseInt(string);
	}

	class ViewHolder {
		FrameLayout mContainerFL;
		TextView mDateTV;
		TextView mColorTV;
	}
}
