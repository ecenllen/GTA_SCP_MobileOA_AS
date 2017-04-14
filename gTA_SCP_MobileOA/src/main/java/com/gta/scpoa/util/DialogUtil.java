package com.gta.scpoa.util;

import android.app.ProgressDialog;

/**
 * 公用dialog
 * 
 * @author shengping.pan
 * 
 */
public class DialogUtil {

	/**
	 * 数据加载进度条
	 * @param progressDialog
	 * @param msg   进度条说明
	 * @param isBackup   返回键是否有用  (true:可用  false:不可用)
	 */
	public static void showDialog(ProgressDialog progressDialog, String msg,boolean isBackup) {
		if (null != progressDialog) {
			progressDialog.setCancelable(isBackup);
			progressDialog.setCanceledOnTouchOutside(false);
			progressDialog.setMessage(msg);
			progressDialog.show();
		}
	}
	public static void init(ProgressDialog progressDialog,boolean isBackup){
		if (null != progressDialog) {
			progressDialog.setCancelable(isBackup);
			progressDialog.setCanceledOnTouchOutside(false);
//			progressDialog.setMessage(msg);		
		}
	}
	public static void init(ProgressDialog progressDialog,boolean isBackup,boolean isCanCancle){
		if (null != progressDialog) {
			progressDialog.setCancelable(isBackup);
			progressDialog.setCanceledOnTouchOutside(isCanCancle);

		}
	}
	public static void showDialog(ProgressDialog progressDialog,String msg){
		if (null != progressDialog) {
			if (! StringUtils.isEmpty(msg)) {
				progressDialog.setMessage(msg);
			}else {
				progressDialog.setMessage("数据加载中...");
			}
			progressDialog.show();
		}
	}
	public static void dismissDialog(ProgressDialog progressDialog){
		if (null != progressDialog) {
			progressDialog.dismiss();
		}
	}
	
}
