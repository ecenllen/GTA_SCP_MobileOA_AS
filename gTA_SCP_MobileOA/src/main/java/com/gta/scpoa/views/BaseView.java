package com.gta.scpoa.views;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.gta.scpoa.application.GTAApplication;

/**
 * 自定义View的基类
 * 
 * @author bin.wang1
 * 
 */
public class BaseView extends FrameLayout implements OnClickListener {
	protected Activity mActivity;
	protected Context mContext;
	protected View mView;
	protected LayoutInflater mInflater;
	private ProgressBar mProgressBar;
	
	private TextView errorTV;
	private boolean isErrorViewShow;
	public BaseView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
//		bpm
	}

	public BaseView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public BaseView(Context context) {
		super(context);
	}

	protected void setContentView(int layoutId) {

		mContext = getContext();
		if (mContext instanceof Activity) {
			mActivity = (Activity) mContext;
		}

		mInflater = LayoutInflater.from(mContext);
		mView = mInflater.inflate(layoutId, null);
		addView(mView);
	}

	public void addProgressBar(Context c) {
		if (null == mProgressBar) {
			FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(
					LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			layoutParams.gravity = Gravity.CENTER;		
			mProgressBar = new ProgressBar(c);
			mProgressBar.setLayoutParams(layoutParams);
			mProgressBar.setVisibility(View.VISIBLE);
		}	
		addView(mProgressBar);
	}

	public void removeProgressBar() {
		if (null != mProgressBar) {
			removeView(mProgressBar);
			mProgressBar = null;
		}
	}
	
	protected boolean isProgressBarShow() {
		
		if (null != mProgressBar) {
			
			return mProgressBar.isShown();
			
		}else {
			return false;
		}
		
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub

	}

	public GTAApplication getGTAApplication() {
		return GTAApplication.instance;
	}
	
	public void addErrorView(Context context,String text){
		if (null == errorTV) {
			FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(
					LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			layoutParams.gravity = Gravity.CENTER;		
			
			errorTV = new TextView(context);
			errorTV.setLayoutParams(layoutParams);
			errorTV.setVisibility(View.VISIBLE);
						
			errorTV.setTextSize(15);
			errorTV.setTextColor(Color.GRAY);			
		}
		errorTV.setText(text);
		addView(errorTV);
		isErrorViewShow = true;
	}
	
	public void removeErrorView(){
		if (null != errorTV) {
			removeView(errorTV);
		}
		isErrorViewShow = false;
	}
	
	public boolean isErrorViewShow(){
		return isErrorViewShow;
	}

}
