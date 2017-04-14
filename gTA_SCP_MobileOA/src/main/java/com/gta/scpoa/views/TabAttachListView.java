package com.gta.scpoa.views;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.app.DownloadManager.Request;
import android.app.Service;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.gta.scpoa.R;
import com.gta.scpoa.adapter.TabAttachAdapter;
import com.gta.scpoa.application.GTAApplication;
import com.gta.scpoa.common.Constant;
import com.gta.scpoa.entity.TabAttachInfo;
import com.gta.scpoa.entity.TableInfor;
import com.gta.scpoa.util.ExplainTableUtil;
import com.gta.scpoa.util.FileUtils;
import com.gta.scpoa.util.PreferencesUtils;

@SuppressLint("NewApi")
public class TabAttachListView implements OnItemClickListener {
	private TableInfor tableInfor; // 表单的某一项的的信息
	private Context context;
	private myListView listView;
	private TabAttachAdapter adapter;
	private List<TabAttachInfo> list = new ArrayList<TabAttachInfo>();
	private int type = 0;
	private int position = 0;

	public TabAttachListView(Context c, TableInfor tableInfor) {
		context = c;
		this.tableInfor = tableInfor;
		dataInit();
	}

	private void dataInit() {
		type = tableInfor.getType();
		if (type == 9) { // 附件
			list = ExplainTableUtil.getTabFJ(tableInfor.getValue());
		} else if (type == 12) { // office 控件
			list = ExplainTableUtil.getTabZWFJ(tableInfor.getValue());
		}
	}

	public View getAttachView(View view) {
		view = LayoutInflater.from(context).inflate(
				R.layout.tab_attach_layout_xml, null);
		LinearLayout mainLayout = (LinearLayout) view
				.findViewById(R.id.attach_layout);
		if (list.size() == 0) {
			mainLayout.setVisibility(View.GONE);
			return view;
		}
		TextView text = (TextView) view.findViewById(R.id.attach_tittle_text);
		text.setText(tableInfor.getKey() + ":");
		text.setTextColor(Color.BLACK);
		listView = (myListView) view.findViewById(R.id.attach_myLv);
		adapter = new TabAttachAdapter(context, list);
		listView.setAdapter(adapter);
		adapter.notifyDataSetChanged();
		listView.setOnItemClickListener(this);
		return view;
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int index, long id) {
		position = index;
		MyTheard(); // 里面涉及一些耗时操作 还是用线程比较好
	}

	/* 要用线程处理 这里没有用线程 要改 */
	private void MyTheard() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				TabAttachInfo tabAttachInfo = list.get(position);
				if (type == 9 || type == 12) {
					/* 有这个key的情况下 */
					if (PreferencesUtils.hasKey(context, tabAttachInfo.getId())) {
						long fileId = PreferencesUtils.getLong(context,
								tabAttachInfo.getId(), -1);
						if (fileId == -1) {
							downFile(tabAttachInfo);
						} else {
							DownloadManager downloadManager = (DownloadManager) context
									.getSystemService(Service.DOWNLOAD_SERVICE);
							/* 查询下载的状态 */
							if (isNeedDownload(tabAttachInfo, fileId,
									downloadManager)) {
								downFile(tabAttachInfo);
							}
						}
					} else { // key不存在的情况下
						if (canDownLoadFile(tabAttachInfo)) {
							downFile(tabAttachInfo);
						}
					}
				}
			}
		}).start();
	}

	/**
	 * 
	 * 调用系统下载功能
	 ** 
	 */
	@SuppressLint("NewApi")
	private void downFile(TabAttachInfo tabAttachInfo) {
		GTAApplication app = (GTAApplication) context.getApplicationContext();
		String url = app.getBpmHost()
				+ "/platform/system/sysFile/download.htmob?fileId="
				+ tabAttachInfo.getId();
		DownloadManager downloadManager = (DownloadManager) context
				.getSystemService(Service.DOWNLOAD_SERVICE);
		Uri resource = Uri.parse(url);
		Request request = new Request(resource);
		MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton(); // 获取文件类型实例
		String mimeString = mimeTypeMap.getMimeTypeFromExtension(MimeTypeMap
				.getFileExtensionFromUrl(url)); // 获取文件类型
		request.setMimeType(mimeString); // 制定下载文件类型
		request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE
				| DownloadManager.Request.NETWORK_WIFI);
		// 设置下载中通知栏提示的标题
		request.setTitle(tabAttachInfo.getName());
		request.setDescription("下载中...");
		request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
		// 表示下载允许的网络类型，默认在任何网络下都允许下载。有NETWORK_MOBILE、NETWORK_WIFI、NETWORK_BLUETOOTH三种及其组合可供选择。
		request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI
				| DownloadManager.Request.NETWORK_MOBILE);
		// 移动网络情况下是否允许漫游。
		request.setAllowedOverRoaming(false);
		request.setVisibleInDownloadsUi(true);
		request.setDestinationInExternalPublicDir(Constant.downLoadPath,
				changeFileName(tabAttachInfo)); // 制定下载的目录里
		long fileId = downloadManager.enqueue(request); // 开始去下载
		/* 将数据保存 */
		PreferencesUtils.putLong(context, tabAttachInfo.getId(), fileId);
	}

	/**
	 * 判断是否需要调用系统下载
	 * 
	 * @param id
	 *            调用downloadManager.enqueue(request)时返回的id，that means an ID for
	 *            the download, unique across the system. This ID is used to
	 *            make future calls related to this download
	 * @return true if need download，else return false
	 */
	private boolean isNeedDownload(TabAttachInfo tabAttachInfo, long id,
			DownloadManager downloadManager) {
		if (downloadManager == null) {
			return true;
		}
		boolean isNeedDownloadAgain = true;
		DownloadManager.Query query = new DownloadManager.Query();
		query.setFilterById(id);
		Cursor cursor = downloadManager.query(query);
		if (cursor != null && cursor.moveToFirst()) {
			int columnStatus = cursor
					.getColumnIndex(DownloadManager.COLUMN_STATUS);
			int status = cursor.getInt(columnStatus);
			switch (status) {
			case DownloadManager.STATUS_FAILED:
				downloadManager.remove(id);
				isNeedDownloadAgain = true; // 需要重新下载
				break;
			case DownloadManager.STATUS_PAUSED: // 暂停
				/* 去除列表 停止下载 */
				downloadManager.remove(id);
				isNeedDownloadAgain = true;
				break;
			case DownloadManager.STATUS_PENDING: // 网络状态改变
				/* 去除列表 停止下载 */
				downloadManager.remove(id);
				isNeedDownloadAgain = true;
				break;
			case DownloadManager.STATUS_RUNNING:
				isNeedDownloadAgain = false;
				break;
			case DownloadManager.STATUS_SUCCESSFUL:
				if (canDownLoadFile(tabAttachInfo)) { // 打不开
					/* 去除列表 停止下载 */
					downloadManager.remove(id);
					isNeedDownloadAgain = true; // 需要重新下载
				} else { // 能打开
					isNeedDownloadAgain = false; // 不需要重新下载
				}
				break;
			default:
				/* 去除列表 停止下载 */
				downloadManager.remove(id);
				isNeedDownloadAgain = true;
				break;
			}
		} else {
			if (canDownLoadFile(tabAttachInfo)) {
				isNeedDownloadAgain = true;
			} else {
				isNeedDownloadAgain = false;
			}
		}
		return isNeedDownloadAgain;
	}

	private void deleteFileID(TabAttachInfo tabAttachInfo) {
		PreferencesUtils.deleteKey(context, tabAttachInfo.getId());
	}

	private boolean canDownLoadFile(TabAttachInfo tabAttachInfo) {
		if (!FileUtils.hasSDCard()) {
			return false;
		}
		String storedir = FileUtils.getSDPath() + Constant.downLoadPath;
		File newDoc = new File(storedir);
		if (!newDoc.exists()) {
			/* 文件夹路径不存在 */
			newDoc.mkdirs();
		}
		String fileName = changeFileName(tabAttachInfo);
		String filePath = storedir + fileName;
		File storefile = new File(filePath);
		if (!storefile.exists()) {
			/* 文件不存在的情况下 */
		} else {
			if (storefile.length() != tabAttachInfo.getSize()) {
				FileUtils.deleteFile(Constant.downLoadPath,
						FileUtils.getFileName(fileName));
				return true;
			}
			deleteFileID(tabAttachInfo);
			FileUtils.openFile(storefile, context);
			return false;
		}
		return true;
	}

	/* 转换文件名 */
	private String changeFileName(TabAttachInfo tabAttachInfo) {
		if (type == 12) {
			return tabAttachInfo.getName();
		}
		String name = tabAttachInfo.getName();
		String id = tabAttachInfo.getId();
		if (name.indexOf(".") == -1) {
			name = name + "-" + id;
		} else {
			int index = name.lastIndexOf(".");
			String end = name.substring(index + 1, name.length()).toLowerCase();
			name = name.substring(0, index) + "-" + id + "." + end;
		}
		return name;
	}
}
