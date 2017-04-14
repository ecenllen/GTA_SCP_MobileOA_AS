package com.gta.version;

import java.util.Date;

import org.json.JSONException;
import org.json.JSONObject;
import org.xml.sax.XMLReader;

import com.gta.http.HttpUtil;
import com.gta.http.RequestInfo;
import com.gta.http.RequestListener;
import com.gta.http.RequestParams;
import com.gta.http.ResponseInfo;
import com.gta.http.RequestInfo.RequestMethod;
import com.gta.http.parse.StringParse;
import com.gta.scpoa.common.Constant;
import com.gta.scpoa.entity.ImageFile;
import com.gta.scpoa.entity.VersionEntity;
import com.gta.scpoa.util.PreferencesUtils;
import com.gta.scpoa.util.StringUtils;
import com.gta.scpoa.util.UIUtils;
import com.gta.scpoa.util.URLs;

import android.app.DownloadManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Binder;
import android.os.Environment;
import android.os.IBinder;
import android.os.Message;
import android.provider.ContactsContract.CommonDataKinds.Nickname;
import android.text.Editable;
import android.text.Html;
import android.text.Html.TagHandler;
import android.widget.Toast;

public class VersionService extends Service {
	private RequestInfo request = null;
	private VersionReceiver receiver = null;
	public String filter1 = "com.gta.app.version.update";
	public String filter2 = "com.gta.app.version.close";
	public String filter3 = "com.gta.app.version.check";
	private VersionEntity entity = null;
	private String apkName = "GTA_SCP_MobileOA.apk";
	private long downID = 0;
	private String keyString="VersionTime";
	@Override
	public void onCreate() {
		super.onCreate();
		receiver = new VersionReceiver();
		IntentFilter filter = new IntentFilter();
		filter.addAction(filter1);
		filter.addAction(filter2);
		filter.addAction(filter3);
		filter.addAction(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
		registerReceiver(receiver, filter);
		request = new RequestInfo(this, URLs.getVersionURL());
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		long time1=PreferencesUtils.getLong(getApplicationContext(),keyString, 0);
		long time2=new Date().getTime();
		if(time1==0){
			PreferencesUtils.putLong(getApplicationContext(), keyString,time2);
		}else{
			if((time2-time1)>12*3600*1000){
				PreferencesUtils.putLong(getApplicationContext(), keyString,time2);
				VersionRequest();
			}
		}
		return super.onStartCommand(intent, flags, startId);
	}

	private void VersionRequest() {
		// 开启版本网络请求
		request.method = RequestMethod.GET;
		RequestParams params = new RequestParams();
		request.params = params;
		HttpUtil.getInstance().doRequest(request, new StringParse(),
				new RequestListener() {
					@Override
					public void onUploadProgress(String url, int progress) {

					}

					@Override
					public void onRequestSucceed(ResponseInfo responseInfo) {
						String result = responseInfo.stringResult;
						if (result != null) {
							if (!"".equals(result)) {
								entity = new VersionEntity();
								try {
									JSONObject jobj = new JSONObject(result);
									entity.setVersionCode(jobj
											.getInt("versionCode"));
									entity.setVersionName(jobj
											.getString("versionName"));
									entity.setUrl(jobj.getString("url"));
									entity.setExplain(jobj.getString("explain"));
									entity.setSize(jobj.getString("size"));
									// 开启版本更新提示页
									PackageManager manager = getApplication()
											.getPackageManager();
									PackageInfo info = manager.getPackageInfo(
											getApplication().getPackageName(),
											0);
									if (info.versionCode < entity
											.getVersionCode()) {
										// 执行更新
										Intent intent = new Intent(
												getBaseContext(),
												VersionActivity.class);
										intent.putExtra("version", entity);
										intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
										intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
										getApplication().startActivity(intent);
									} else {
										UIUtils.ToastMessage(
												getApplicationContext(),
												"已是最新版本");
									}
								} catch (JSONException e) {
									e.printStackTrace();
								} catch (NameNotFoundException e) {
									e.printStackTrace();
								}
							}
						}
					}

					@Override
					public void onRequestStart(RequestInfo requestInfo) {

					}

					@Override
					public void onRequestError(ResponseInfo responseInfo) {
						String result = responseInfo.stringResult;
					}

					@Override
					public void onRequestCancelled(RequestInfo requestInfo) {

					}

					@Override
					public void onNoNetWork() {
					}

				}, this);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (request != null) {
			// 取消当前请求
			HttpUtil.getInstance().cancelRequest(this);
		}
		if (receiver != null) {
			// 取消注册
			unregisterReceiver(receiver);
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	public class VersionReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (filter1.equalsIgnoreCase(intent.getAction())) {
				// 开始下载最新APP
				DownloadManager manager = (DownloadManager) getApplicationContext()
						.getSystemService(getApplication().DOWNLOAD_SERVICE);
				DownloadManager.Request request = new DownloadManager.Request(
						Uri.parse(entity.getUrl()));
				// 设置更新时候
				request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE
						| DownloadManager.Request.NETWORK_WIFI);
				// 下载时，通知栏显示途中
				request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE);
				// 显示下载界面
				request.setVisibleInDownloadsUi(true);
				// 设置下载后文件存放的位置
				request.setDestinationInExternalFilesDir(
						getApplicationContext(),
						Environment.DIRECTORY_DOWNLOADS, apkName);
				// 将下载请求放入队列
				downID = manager.enqueue(request);
			} else if (filter2.equalsIgnoreCase(intent.getAction())) {
				// 关闭下载的服务
				VersionService.this.stopSelf();
			} else if (DownloadManager.ACTION_DOWNLOAD_COMPLETE
					.equalsIgnoreCase(intent.getAction())) {
				DownloadManager manager = (DownloadManager) getApplicationContext()
						.getSystemService(getApplication().DOWNLOAD_SERVICE);
				long sysdownID = intent.getLongExtra(
						DownloadManager.EXTRA_DOWNLOAD_ID, 0);
				if (sysdownID == downID) {
					Intent install = new Intent(Intent.ACTION_VIEW);
					Uri downloadFileUri = manager
							.getUriForDownloadedFile(downID);
					install.setDataAndType(downloadFileUri,
							"application/vnd.android.package-archive");
					install.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					context.startActivity(install);
				}
			} else if (filter3.equalsIgnoreCase(intent.getAction())) {
				long time2=new Date().getTime();
				PreferencesUtils.putLong(getApplicationContext(), keyString,time2);
				VersionRequest();
			}
		}

	}
}
