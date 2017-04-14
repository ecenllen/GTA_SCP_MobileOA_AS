package com.gta.scpoa.views;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.view.Display;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.gta.scpoa.R;

public class CommonTopView extends BaseView {
	private ImageButton backIBtn;
	private ImageView logoIV;
	private TextView leftTV;
	private TextView titleTV;
	private ImageButton addIBtn;
	private ImageButton forwardIBtn;
	private TextView rightTV;

	public CommonTopView(Context context, AttributeSet attrs) {
		super(context, attrs);
		setupViews();
	}

	public CommonTopView(Context context) {
		super(context);
		setupViews();
	}

	private void setupViews() {
		setContentView(R.layout.common_top_bar);
		if (isInEditMode()) {
			return;
		}
	}

	/*
	 * 左边的控件
	 */
	public void setBackImageButtonEnable(boolean isEnable) {
		if (backIBtn == null) {
			backIBtn = (ImageButton) findViewById(R.id.topbar_back_ibtn);
		}

		if (isEnable) {
			backIBtn.setVisibility(View.VISIBLE);
		} else {
			backIBtn.setVisibility(View.GONE);
			backIBtn = null;
		}
	}

	public void setBackImageButtonOnClickListener(OnClickListener listener) {

		if (backIBtn != null) {
			backIBtn.setOnClickListener(listener);
		}
	}

	public void setLogoImageViewEnable(boolean isEnable) {
		if (logoIV == null) {
			logoIV = (ImageView) findViewById(R.id.topbar_logo_iv);
		}
		if (isEnable) {
			logoIV.setVisibility(View.VISIBLE);
		} else {
			logoIV.setVisibility(View.GONE);
			logoIV = null;
		}

	}

	public void setLeftTextViewEnable(boolean isEnable) {
		if (leftTV == null) {
			leftTV = (TextView) findViewById(R.id.topbar_left_tv);
		}

		if (isEnable) {
			leftTV.setVisibility(View.VISIBLE);
		} else {
			leftTV.setVisibility(View.GONE);
			leftTV = null;

		}
	}

	public void setLeftTextViewText(String text) {
		if (leftTV != null) {
			leftTV.setText(text);
		}
	}

	public void setLeftTextViewOnClickListener(OnClickListener listener) {
		if (leftTV != null) {
			leftTV.setOnClickListener(listener);
		}
	}

	// 中间
	public void setTitleTextViewEnable(boolean isEnable) {
		if (titleTV == null) {
			titleTV = (TextView) findViewById(R.id.topbar_title_tv);
		}
		if (isEnable) {
			titleTV.setVisibility(View.VISIBLE);
		} else {
			titleTV.setVisibility(View.GONE);
			titleTV = null;
		}
	}

	public void setTitleTextViewText(String text) {
		if (titleTV != null) {
			titleTV.setText(text);
		}
	}

	// 右边

	public void setAddImageButtonEnable(boolean isEnable) {
		if (addIBtn == null) {
			addIBtn = (ImageButton) findViewById(R.id.topbar_add_ibtn);
		}
		if (isEnable) {
			addIBtn.setVisibility(View.VISIBLE);
			addIBtn.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					showPopupWindow(v);
				}
			});
		} else {
			addIBtn.setVisibility(View.GONE);
			addIBtn = null;
		}
	}

	public void setForwardImageButtonEnable(boolean isEnable) {
		if (forwardIBtn == null) {
			forwardIBtn = (ImageButton) findViewById(R.id.topbar_forward_ibtn);
		}
		if (isEnable) {
			forwardIBtn.setVisibility(View.VISIBLE);
		} else {
			forwardIBtn.setVisibility(View.GONE);
			forwardIBtn = null;
		}
	}

	public void setForwardImageButtonOnClickListener(OnClickListener listener) {
		if (forwardIBtn != null) {
			forwardIBtn.setOnClickListener(listener);
		}
	}

	public void setRightTextViewEnable(boolean isEnable) {
		if (rightTV == null) {
			rightTV = (TextView) findViewById(R.id.topbar_right_tv);
		}
		if (isEnable) {
			rightTV.setVisibility(View.VISIBLE);
		} else {
			rightTV.setVisibility(View.GONE);
			rightTV = null;

		}

	}

	public void setRightTextViewOnClickListener(OnClickListener listener) {
		if (rightTV != null) {
			rightTV.setOnClickListener(listener);
		}
	}

	public void setRightTextViewText(String text) {
		if (rightTV != null) {
			rightTV.setText(text);
		}
	}

	/**
	 * 显示快捷界面
	 */
	public void showPopupWindow(View v) {
		ShortCutDialogView view = new ShortCutDialogView(mActivity);
		Display display = mActivity.getWindowManager().getDefaultDisplay() ;
		int width = display.getWidth();
		PopupWindow popupWindow = new PopupWindow(view,
				android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
				android.view.ViewGroup.LayoutParams.WRAP_CONTENT, true);
		popupWindow.setOutsideTouchable(true);
		popupWindow.setBackgroundDrawable(new BitmapDrawable());
		popupWindow.setAnimationStyle(R.style.popupwindow_anim_style);
		view.setOnCancelClicked(popupWindow);
		
		RelativeLayout tempLaout =(RelativeLayout) findViewById(R.id.topbar_layout);
		popupWindow.showAsDropDown(tempLaout,width,0);
		//popupWindow.showAtLocation(v, Gravity.TOP, 250, 145);
	}

}
