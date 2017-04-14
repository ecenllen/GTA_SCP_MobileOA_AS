package com.gta.scpoa.common;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Properties;

/**
 * 保存用户相关信息及设置
 * 
 * @author bin.wang1
 * 
 */
public class Config {
	
	private Context mContext;
	private final static String APP_CONFIG = "config";
//	public final static String DEFAULT_HOST_ADDRESS="210.75.17.221:7082/oa";
//	public final static String DEFAULT_HOST_ADDRESS="210.75.17.212:7082/oa";
//	public final static String DEFAULT_HOST_ADDRESS="221.182.155.68:7072/OA";
	public final static String DEFAULT_HOST_ADDRESS="218.28.167.66:7072/OA";//郑州财经
//	public final static String DEFAULT_HOST_ADDRESS="218.28.167.66:7072";//郑州财经, 注意：后面修改把OA放在代码里面，只需要输入数字地址
//	http://218.28.99.90:7072/Index/PortalIndex
//	public final static String DEFAULT_HOST_ADDRESS="218.28.99.90:7072/OA";//河南交通
//	public final static String DEFAULT_HOST_ADDRESS="192.168.201.74:7077/OA";
//	public final static String DEFAULT_HOST_ADDRESS="218.28.167.66:8021";
	public final static String DEFAULT_FACE_PATH= "/scpoa/portrait/";//默认的头像存储路径

	private static Config gtaConfig;
	/**
	 * 调试控制变量，发布后，改为false
	 */
	public static boolean ISDEBUG = false;

	/**
	 * 单例，用于传递Context
	 * 
	 * @param context
	 * @return
	 */
	public static Config getGTAConfig(Context context) {
		if (gtaConfig == null) {
			gtaConfig = new Config();
			gtaConfig.mContext = context;
		}
		return gtaConfig;
	}

	/**
	 * 获取Preference设置
	 */
	public static SharedPreferences getSharedPreferences(Context context) {
		return PreferenceManager.getDefaultSharedPreferences(context);
	}

	/**
	 * 获得用户的token
	 * 
	 */
	public String getPrivateToken() {
		return get(Constant.PROP_KEY_PRIVATE_TOKEN);
	}

	/**
	 * 设置Properties
	 * 
	 * @param ps
	 */
	public void set(Properties ps) {
		Properties props = get();
		props.putAll(ps);
		setProps(props);
	}

	public void set(String key, String value) {
		Properties props = get();
		props.setProperty(key, value);
		setProps(props);
	}

	public void remove(String... key) {
		Properties props = get();
		for (String k : key)
			props.remove(k);
		setProps(props);
	}

	private void setProps(Properties p) {
		FileOutputStream fos = null;
		try {
			// 把config建在files目录下
			// fos = activity.openFileOutput(APP_CONFIG, Context.MODE_PRIVATE);
			// 把config建在(自定义)app_config的目录下
			File dirConf = mContext.getDir(APP_CONFIG, Context.MODE_PRIVATE);
			File conf = new File(dirConf, APP_CONFIG);
			fos = new FileOutputStream(conf);

			p.store(fos, null);
			fos.flush();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				fos.close();
			} catch (Exception e) {
			}
		}
	}

	/**htmob
	 * 获取Properties
	 * 
	 * @param key
	 * @return
	 */
	public String get(String key) {
		Properties props = get();
		return (props != null) ? props.getProperty(key) : null;
	}

	public Properties get() {
		FileInputStream fis = null;
		Properties props = new Properties();
		try {
			// 读取files目录下的config
			// fis = activity.openFileInput(APP_CONFIG);
			// 读取app_config目录下的config
			File dirConf = mContext.getDir(APP_CONFIG, Context.MODE_PRIVATE);
			fis = new FileInputStream(dirConf.getPath() + File.separator
					+ APP_CONFIG);

			props.load(fis);
		} catch (Exception e) {
		} finally {
			try {
				fis.close();
			} catch (Exception e) {
			}
		}
		return props;
	}

}
