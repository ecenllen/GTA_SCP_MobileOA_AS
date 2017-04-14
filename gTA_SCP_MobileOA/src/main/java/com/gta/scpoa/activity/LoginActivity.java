package com.gta.scpoa.activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.gta.db.annotation.ViewInject;
import com.gta.scpoa.R;
import com.gta.scpoa.biz.IGetDataBiz;
import com.gta.scpoa.biz.impl.GetDataBizImpl;
import com.gta.scpoa.common.Config;
import com.gta.scpoa.common.Constant;
import com.gta.scpoa.entity.User;
import com.gta.scpoa.util.DialogUtil;
import com.gta.scpoa.util.MD5Utils;
import com.gta.scpoa.util.PreferencesUtils;
import com.gta.scpoa.util.StringUtils;
import com.gta.scpoa.util.UIUtils;

import java.lang.ref.WeakReference;

public class LoginActivity extends BaseActivity implements OnClickListener,
		OnEditorActionListener {

	@ViewInject(id = R.id.iv_login_logo)
	private ImageView mLogoIV;// 头像或者GTA Logo
	@ViewInject(id = R.id.login_gray_line)
	private View mLineV;

	@ViewInject(id = R.id.login_username)
	private AutoCompleteTextView mAccountET;
	@ViewInject(id = R.id.login_password)
	private EditText mPasswordET;
	@ViewInject(id = R.id.login_server_address_et)
	private EditText mServerAddrET;

	@ViewInject(id = R.id.login_server_address_tv)
	private TextView mServerAddrTV;

	@ViewInject(id = R.id.btn_login)
	private Button loginBtn;

	private ProgressDialog mLoginProgressDialog;
	private InputMethodManager imManager;
	private TextWatcher textWatcher;
	// 标识服务器地址设置的输入框是否显示

	private LoginHandler mHandler;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
		init();
		setupViews();
		setData();
		autoLogin();
	}

	private static class LoginHandler extends Handler {
		WeakReference<LoginActivity> wr;

		LoginHandler(LoginActivity activity) {
			wr = new WeakReference<LoginActivity>(activity);
		}

		@Override
		public void handleMessage(Message msg) {
			LoginActivity mActivity = wr.get();
			if(mActivity == null) return;
			switch (msg.what) {
			case Constant.MSG_GETDATA_SUCCESS:
				// 如果程序已经关闭，则不再执行以下处理
				if (mActivity.isFinishing()) {
					return;
				}
				DialogUtil.dismissDialog(mActivity.mLoginProgressDialog);
				User user = (User) msg.obj;
				Log.e("m_tag", "=======user=======" + user);
				mActivity.handleResult(setICONURL(user));
				break;
			case Constant.MSG_FAIL:
				DialogUtil.dismissDialog(mActivity.mLoginProgressDialog);
				UIUtils.ToastMessage(mActivity.getApplicationContext(),msg.obj.toString());
				break;
			case Constant.MSG_SHOW_PROGRESS:
				DialogUtil.showDialog(mActivity.mLoginProgressDialog,msg.obj.toString());
				break;
			default:
				break;
			}

		}
	}

	private void init() {
		mHandler = new LoginHandler(this);
		
	}

	/**
	 * 防止url不正规
	 * @param user
	 * @return
	 */
	public static User setICONURL(User user) {
		String url = user.getAvatarUrl();
		if(null != url && !TextUtils.isEmpty(url)){
			if(!url.contains("http://")){
				user.setAvatarUrl("");
			}
		}
		return user;
	}

	protected void handleResult(User user) {

		// 保存用户的登录信息
		getGTAApplication().saveLoginInfo(user);

		UIUtils.ToastMessage(getApplicationContext(),
				getString(R.string.login_msg_success));
		// 返回成功标识
		setResult(RESULT_OK);
		// 发送用户登录成功的广播
		// GTABroadCastController
		// .sendUserChangeBroadcast(getApplicationContext());
		// TODO 如果有启动界面，可不需此跳转
		UIUtils.goMainActivity(LoginActivity.this);

	}

	private void autoLogin() {
		String pwd = mPasswordET.getText().toString();
		if (!StringUtils.isEmpty(pwd)) {
			checkLogin();
		}
	}

	private void setData() {
		// 从缓存中读取图片
		String url = null;
		if (getGTAApplication().containsProperty(Constant.PROP_KEY_PORTRAIT)) {
			url = getGTAApplication().getProperty(Constant.PROP_KEY_PORTRAIT);
		}
		UIUtils.showLoginPortrait(mLogoIV, url, 200, 200);
	}

	private void setupViews() {

		imManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
		mServerAddrTV.setOnClickListener(this);
		// 登录按钮事件监听
		loginBtn.setOnClickListener(this);

		textWatcher = new TextWatcher() {

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

			}
		};

		// 添加文本变化监听事件
		mAccountET.addTextChangedListener(textWatcher);
		mPasswordET.addTextChangedListener(textWatcher);
		mPasswordET.setOnEditorActionListener(this);
		mServerAddrET.setOnEditorActionListener(this);
		// 缓存中读取
		String pwd = getGTAApplication().getProperty(Constant.ACCOUNT_PWD);

		// pwd = DesUtils.decode(Constant.ACCOUNT_PWD, pwd);// 解密

		/** 使用md5加密，本地保存明文，不解密，解密不了 */

		mAccountET.setText(getGTAApplication().getProperty(
				Constant.ACCOUNT_NAME));
		mPasswordET.setText(pwd);

		// 设置默认的地址
		// 如果是第一次启动，则为mServerAddrET设置地址为192.168.193.120
		// 如果不是首次启动，则直接使用偏好设置中的地址
		boolean isFirstLaunch = !PreferencesUtils.getFirstRun(this,
				"firstLaunch");
		if (isFirstLaunch) {
			//设置通知开启
			getGTAApplication().setProperty(Constant.PROP_KEY_SWITCHER_NOTICE, "true");
			getGTAApplication().setProperty(Constant.PROP_KEY_SWITCHER_RING, "true");
			getGTAApplication().setProperty(Constant.PROP_KEY_SWITCHER_SHAKE, "true");
			mServerAddrET.setText(Config.DEFAULT_HOST_ADDRESS);
			Log.e("tag", "=================="+Config.DEFAULT_HOST_ADDRESS);
		} else {
			mServerAddrET.setText(getGTAApplication().getProperty(
					Constant.SERVER_ADDR));
		}

	}


	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_login:
			imManager.hideSoftInputFromWindow(mPasswordET.getWindowToken(), 0);
			checkLogin();
			break;
		case R.id.login_server_address_tv:
			mLineV.setVisibility(View.VISIBLE);
			mServerAddrET.setVisibility(View.VISIBLE);
			mServerAddrTV.setVisibility(View.GONE);
//			mLogoIV.setBackgroundResource(0);
//			mLogoIV.setImageResource(R.drawable.login_gta_logo);
			break;
		default:
			break;
		}
	}

	/**
	 * 监听输入法事件
	 */
	@Override
	public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
		if (actionId == EditorInfo.IME_ACTION_DONE) {
			checkLogin();
			// 将输入法隐藏
			InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(mPasswordET.getWindowToken(), 0);
			return true;
		}
		return false;
	}

	/**
	 * 检查登录的合法性
	 */
	private void checkLogin() {
		String userName = mAccountET.getText().toString();
		String password = mPasswordET.getText().toString();
		String serverAddr = mServerAddrET.getText().toString();

		// 检查用户的输入
		if (StringUtils.isEmpty(userName)) {
			UIUtils.ToastMessage(getApplicationContext(),
					getString(R.string.login_msg_error));
			return;
		}
		if (StringUtils.isEmpty(password)) {
			UIUtils.ToastMessage(getApplicationContext(),
					getString(R.string.login_msg_error));
			return;
		}
		if (StringUtils.isEmpty(serverAddr)) {
			UIUtils.ToastMessage(getApplicationContext(),
					getString(R.string.login_msg_serveraddr_null));
			return;
		}

		if (!isServerAddr(serverAddr)) {
			UIUtils.ToastMessage(getApplicationContext(), "服务器地址的格式不正确，请重新输入 !");
			return;
		}
		// // 对密码加密
		// password = DesUtils.encode(Constant.ACCOUNT_PWD, password);
		// 保存信息
		getGTAApplication().saveAccountInfo(userName, password, serverAddr);

		// 对密码Md5加密
		password = MD5Utils.GetMD5Code(password+"{000000}");
		// 登录
		login(userName, password);

	}

	public boolean isServerAddr(String addr) {
		boolean b = false;
		if (addr.contains(":")) {
			String[] arr = addr.split(":");
			if (null != arr && arr.length == 2) {
				boolean isIp = isIp(arr[0]);
				String[] port = arr[1].split("/");
				boolean isPort = isPort(port[0]);
//				boolean isPort = isPort(arr[1]);
				if (isIp && isPort) {
					b = true;
				}
			}

		} else {
			b = isIp(addr);
		}
		return b;
	}

	public boolean isIp(String IP) {// 判断是否是一个IP
		boolean b = false;
		if (IP.matches("\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}")) {
			String s[] = IP.split("\\.");
			if (Integer.parseInt(s[0]) < 255)
				if (Integer.parseInt(s[1]) < 255)
					if (Integer.parseInt(s[2]) < 255)
						if (Integer.parseInt(s[3]) < 255) {
							b = true;
						}
		}
		return b;
	}

	// 端口号支持2位、4位、5位
	public boolean isPort(String host) {
		boolean b = false;
		if (host.matches("\\d{2,4}")||host.matches("\\d{2,5}")) {
			b = true;
		}

		return b;
	}

	/**
	 * 登录验证
	 * 
	 * @param userName
	 * @param password
	 */
	private void login(final String userName, String password) {
		if (mLoginProgressDialog == null) {
			mLoginProgressDialog = new ProgressDialog(this);
			DialogUtil.init(mLoginProgressDialog, false);
		}

		IGetDataBiz api = new GetDataBizImpl();
		api.login(getApplicationContext(), mHandler, userName, password);

	}
}