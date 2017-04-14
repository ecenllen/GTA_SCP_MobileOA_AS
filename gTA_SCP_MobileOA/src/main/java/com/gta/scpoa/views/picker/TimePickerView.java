package com.gta.scpoa.views.picker;

import android.content.Context;
import android.util.AttributeSet;

import com.gta.scpoa.R;
import com.gta.scpoa.views.BaseView;

import java.util.Calendar;
import java.util.Date;

public class TimePickerView extends BaseView {
	private boolean isAll = true;
	private WheelMain wheelMain;
	private int mSelectedYear;
	private int mSelectedMonth;

	public TimePickerView(Context context, AttributeSet attrs,boolean isAllVisible) {
		super(context, attrs);
		this.isAll = isAllVisible;
		setupViews();
	}

	public TimePickerView(Context context,boolean isAllVisible) {
		super(context);
		this.isAll = isAllVisible;
		setupViews();
	}
	
	public TimePickerView(Context context,int selectedYear,int selectedMonth) {
		super(context);
		this.isAll = false;
		setSelectDate(selectedYear, selectedMonth);
		setupViews();
	}
	
	public void setSelectDate(int selectedYear,int selectedMonth){	
			this.mSelectedYear=selectedYear;
			this.mSelectedMonth=selectedMonth;		
	}

	private void setupViews() {
		setContentView(R.layout.view_time_picker);
		init();

	}

	private void init() {
		ScreenInfo screenInfo = new ScreenInfo(mActivity);
		wheelMain = new WheelMain(this, isAll);
		wheelMain.screenheight = screenInfo.getHeight();
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(new Date());
		int year = calendar.get(Calendar.YEAR);
		int month = calendar.get(Calendar.MONTH);
		int day = calendar.get(Calendar.DAY_OF_MONTH);
		int hour = calendar.get(Calendar.HOUR_OF_DAY);
		int min = calendar.get(Calendar.MINUTE);

		if (isAll) {
			wheelMain.initDateTimePicker(year, month, day, hour, min);
		} else {
				wheelMain.initDateTimePicker(mSelectedYear, mSelectedMonth, day);				
//				wheelMain.initDateTimePicker(year, month, day);

		}
	}
	public WheelMain getWheelMain(){
		return wheelMain;
	}

}
