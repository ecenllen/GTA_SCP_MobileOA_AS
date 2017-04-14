package com.gta.scpoa.application;

import android.app.Activity;
import android.app.Application;

import com.gta.bitmap.core.ImageLoader;
import com.gta.bitmap.core.ImageLoaderConfiguration;
import com.gta.crash.CrashHandler;
import com.gta.scpoa.activity.MainActivity;
import com.gta.scpoa.common.Config;
import com.gta.scpoa.common.Constant;
import com.gta.scpoa.entity.MessageCountBean;
import com.gta.scpoa.entity.Schedule;
import com.gta.scpoa.entity.User;
import com.gta.utils.thirdParty.jPush.JpushManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Stack;

public class GTAApplication extends Application {

	/**
	 * 保存activity集合
	 */
//	protected List<Activity> activityStack = new LinkedList<Activity>();
	private Stack<Activity> activityStack;


	
	private List<Schedule> todayScheTemp;
	/**
	 * 对于缓存集合 todayScheTemp，是否开启完全替换，是则完全替换，否则更新替换。
	 */
	public boolean isAllReplaceOpen = true;
	private String loginUid;
	public static GTAApplication instance;
	public static MessageCountBean mMessageCountBean;

	@Override
	public void onCreate() {
		super.onCreate();
		instance = this;

		// 记录未处理的异常情况
		this.todayScheTemp = new ArrayList<Schedule>();

		CrashHandler crashHandler = CrashHandler.getInstance();
		crashHandler.init(getApplicationContext());
		iniImageLoader();

		
		initJpush();
		
	}

	private void initJpush() {
		//初始化极光推送
		JpushManager.getInstance().setDebugMode(false); //注：该接口需在init接口之前调用，避免出现部分日志没打印的情况
		JpushManager.getInstance().init(this, MainActivity.class);
		// 改到MainActivity 里面注册
//		OAJpushManager.registerJpushManager(this);
	}

	public List<Schedule> getTodayScheTemp() {
		return todayScheTemp;
	}

	private void iniImageLoader() {
		ImageLoaderConfiguration configuration = new ImageLoaderConfiguration.Builder(
				getApplicationContext()).threadPoolSize(3)
				.threadPriority(Thread.NORM_PRIORITY - 2)
				.memoryCacheSize((int) (getIdealMemCacheSize()))
				.diskCacheSize(1024 * 1024 * 50).build();
		ImageLoader imageLoader = ImageLoader.getInstance();
		imageLoader.init(configuration);
	}
	
	private int getIdealMemCacheSize() {
		return (int) (Runtime.getRuntime().maxMemory() / 10);
	}
	
	
	public void setTodayScheTemp(List<Schedule> scheTemp) {

		if (null != todayScheTemp) {
			for (Schedule ts : todayScheTemp) {

				if (ts.isHasNoti()) {
					int id = ts.getId();
					for (Schedule s : scheTemp) {
						if (id == s.getId()) {
							s.setHasNoti(true);
						}
					}
				}
			}
			this.todayScheTemp = scheTemp;
		}
	}

	public void updateTodaySche(int id, boolean isNotify) {

		for (Schedule s : todayScheTemp) {
			if (id == s.getId()) {
				s.setHasNoti(isNotify);
				break;
			}
		}
	}

	/**
	 * 用户退出登录时，清空缓存集合
	 */
	public void clearTodayTempSche() {
		if (null != todayScheTemp) {
			todayScheTemp.clear();
		}
	}

	/**
	 * 初始化数据库
	 */

	public String getUserID() {
		return loginUid;
	}
	
	public String getBpmHost(){
		return getProperty(Constant.PROP_KEY_BPMHOST);
	}

	public String getUserName() {
		return getProperty(Constant.PROP_KEY_USERNAME);
	}
	
	public String getFullName(){
		return getProperty(Constant.PROP_KEY_FULLNAME);
	}

	/**
	 * 保存用户信息
	 * 
	 * @param serverAddr
	 * @param password
	 * @param loginName
	 */
	public void saveAccountInfo(String loginName, String password,
			String serverAddr) {
		setProperty(Constant.ACCOUNT_NAME, loginName);
		setProperty(Constant.ACCOUNT_PWD, password);
		setProperty(Constant.SERVER_ADDR, serverAddr);
	}

	public void saveLoginInfo(final User user) {
		if (null == user) {
			return;
		}
		// 保存用户的信息
		this.loginUid = user.getUserId();

		setProperties(new Properties() {
			private static final long serialVersionUID = 1L;
			{
				setProperty(Constant.PROP_KEY_UID, user.getUserId()==null?"":user.getUserId());
				setProperty(Constant.PROP_KEY_USERNAME, user.getUserName()==null?"":user.getUserName());
				setProperty(Constant.PROP_KEY_DEPARTMENT, user.getDepartment()==null?"":user.getDepartment());
				setProperty(Constant.PROP_KEY_TELEPHONE, user.getTelephone()==null?"":user.getTelephone());
				setProperty(Constant.PROP_KEY_EMAIL, user.getEmail()==null?"":user.getEmail());
				setProperty(Constant.PROP_KEY_SERVER_PORTRAIT,user.getAvatarUrl()==null?"":user.getAvatarUrl());
				setProperty(Constant.PROP_KEY_FULLNAME, user.getFullName()==null?"":user.getFullName());
				setProperty(Constant.PROP_KEY_BPMHOST, user.getBpmHost()==null?"":user.getBpmHost());
			}
		});

	}

	/**
	 * 清除登录信息，用户的私有token也一并清除
	 */
	public void cleanLoginInfo() {
		this.loginUid = "";
		removeProperty(Constant.PROP_KEY_UID, Constant.PROP_KEY_USERNAME,
				Constant.PROP_KEY_DEPARTMENT, Constant.PROP_KEY_TELEPHONE,
				Constant.PROP_KEY_EMAIL, Constant.PROP_KEY_SERVER_PORTRAIT);
	}

	public void cleanPassword() {
		removeProperty(Constant.ACCOUNT_PWD);
	}

	/**
	 * 获取登录信息
	 * 
	 * @return
	 */
	public User getLoginInfo() {
		User user = new User();
		user.setUserId(getProperty(Constant.PROP_KEY_UID));
		
		user.setDepartment(getProperty(Constant.PROP_KEY_DEPARTMENT));
		user.setTelephone(getProperty(Constant.PROP_KEY_TELEPHONE));
		user.setEmail(getProperty(Constant.PROP_KEY_EMAIL));
		user.setAvatarUrl(getProperty(Constant.PROP_KEY_SERVER_PORTRAIT));
		user.setFullName(getProperty(Constant.PROP_KEY_FULLNAME));
		return user;
	}

	/**
	 * 保存
	 */
	public void setProperties(Properties ps) {
		Config.getGTAConfig(this).set(ps);
	}

	public void setProperty(String key, String value) {
		Config.getGTAConfig(this).set(key, value);
	}

	/**
	 * 获取
	 */
	public Properties getProperties() {
		return Config.getGTAConfig(this).get();
	}

	public String getProperty(String key) {
		return Config.getGTAConfig(this).get(key);
	}

	/**
	 * 移除
	 */
	public void removeProperty(String... key) {
		Config.getGTAConfig(this).remove(key);
	}

	/**
	 * 判断
	 */
	public boolean containsProperty(String key) {
		Properties props = getProperties();
		return props.containsKey(key);
	}


	/**
	 * 添加新的Activity
	 * 
	 * @param activity
	 */
	public void addActivity(Activity activity) {
		if(activityStack == null)
			activityStack = new Stack<>();

		if (activityStack.size() > 0) {
			if (!activityStack.contains(activity)) {
				activityStack.add(activity);
			}
		} else {
			activityStack.add(activity);
		}
	}

	/**
	 * 获取所有activity
	 * 
	 * @return
	 */
	public Stack<Activity> getActivityList() {
		return activityStack;
	}

	/**
	 * 移除Activity
	 * 
	 * @param activity
	 */
	public void remove(Activity activity) {
		if(activityStack != null) {
			if (activityStack.contains(activity)) {
				activityStack.remove(activity);
			}
		}

	}

	public static MessageCountBean getmMessageCountBean() {
		if(mMessageCountBean == null)
			mMessageCountBean = new MessageCountBean();
		return mMessageCountBean;
	}

}
