package com.gta.scpoa.biz.impl;

import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.app.DownloadManager.Request;
import android.app.Service;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.webkit.MimeTypeMap;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.gta.http.HttpUtil;
import com.gta.http.RequestInfo;
import com.gta.http.RequestInfo.RequestMethod;
import com.gta.http.RequestListener;
import com.gta.http.RequestParams;
import com.gta.http.ResponseInfo;
import com.gta.http.parse.StringParse;
import com.gta.scpoa.application.GTAApplication;
import com.gta.scpoa.biz.MailReciveInforBiz;
import com.gta.scpoa.common.Constant;
import com.gta.scpoa.entity.MailAttachInfo;
import com.gta.scpoa.entity.ReciveMailInfor;
import com.gta.scpoa.util.FileUtils;
import com.gta.scpoa.util.PreferencesUtils;
import com.gta.scpoa.util.URLs;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@SuppressLint("NewApi")
public class MailReciveInforBizImpz implements MailReciveInforBiz {

	private Context context;

	public MailReciveInforBizImpz(Context context) {
		this.context = context;
	}

	@Override
	public void getMailReciveInfor(String id, int mailType,
			final Handler handler) {
		GTAApplication app = (GTAApplication)context.getApplicationContext();
		
		String UseID = app.getProperty(Constant.PROP_KEY_UID);
		RequestParams params = new RequestParams();
		String urlString = "";
		if(mailType != 1){
			urlString = URLs.getDefaultBaseURL()+"/GetMailDetail";
			params.addParams("userId", UseID);
			params.addParams("mailId", id);
			params.addParams("mailType", String.valueOf(mailType));
		}else{
			 urlString =URLs.getDefaultBaseURL()+"/GetMailDetail";
			 params.addParams("userId", UseID);
			 params.addParams("userName", app.getFullName());
			 params.addParams("mailId", id);
			 params.addParams("mailType", String.valueOf(mailType));
		}
		
		RequestInfo requestInfo = new RequestInfo(context, urlString);
		requestInfo.method = RequestMethod.POST;// 默认post请求
		requestInfo.requestCode = 1;
		requestInfo.params = params;
		HttpUtil.getInstance().doRequest(requestInfo, new StringParse(),
				new RequestListener() {

					@Override
					public void onRequestError(ResponseInfo responseInfo) {
						// TODO Auto-generated method stub
						Message msg = handler.obtainMessage(0); // 失败返回
						msg.obj = "网络异常，加载数据失败!";
						msg.sendToTarget();
						return;
					}

					@Override
					public void onRequestCancelled(RequestInfo requestInfo) {
						// TODO Auto-generated method stub
						Message msg = handler.obtainMessage(0); // 失败返回
						msg.obj = "网络异常，加载数据失败!";
						msg.sendToTarget();
						return;
					}

					@Override
					public void onRequestSucceed(ResponseInfo responseInfo) {
						// TODO Auto-generated method stub
						String resultString = responseInfo.stringResult;
						try {
							JSONObject reJsonObject = JSON.parseObject(resultString);
							if (reJsonObject.containsKey("Successed")) {
								if (reJsonObject.getBooleanValue("Successed")) {
									JSONObject jsonObject = reJsonObject
											.getJSONObject("Data");
									ReciveMailInfor reciveMailInfor = new ReciveMailInfor();
									/* 邮件id */
									if (jsonObject.containsKey("Id")) {
										reciveMailInfor.setId(String
												.valueOf(jsonObject
														.get("Id")));
									}
									/* 发件人 */
									if (jsonObject.containsKey("UserName")) {
										String UserName = jsonObject
												.getString("UserName");
										if (UserName != null) {
											reciveMailInfor
													.setUserName(UserName);
										}
									}
									
									/* 发件人的ID */
									if (jsonObject.containsKey("UserId")) {
										String UserId = jsonObject
												.get("UserId") + "";
										if (UserId != null) {
											reciveMailInfor.setUserName(reciveMailInfor.getUserName()+"|"+UserId);
										}
									}									
									
									/* 收件人和ID */
									if (jsonObject.containsKey("ReceiverUsers")) {
										String ReceiverUsers = jsonObject
												.getString("ReceiverUsers");
										if (ReceiverUsers != null) {
											reciveMailInfor
													.setReceiverUsers(ReceiverUsers);
										}
									}

									/* 邮件的主题 */
									if (jsonObject.containsKey("OutBoxTheme")) {
										String OutBoxTheme = jsonObject
												.getString("OutBoxTheme");
										if (OutBoxTheme != null) {
											reciveMailInfor
													.setOutBoxTheme(OutBoxTheme);
										}
									}

									/* 抄送人和ID */
									if (jsonObject.containsKey("OutBoxCopyer")) {
										String OutBoxCopyer = jsonObject
												.getString("OutBoxCopyer");
										if (OutBoxCopyer != null) {
											reciveMailInfor.setOutBoxCopyer(jsonObject
													.getString("OutBoxCopyer"));
										}
									}
									/* 密送人和ID */
									if (jsonObject.containsKey("OutBoxSecret")) {
										String OutBoxSecret = jsonObject
												.getString("OutBoxSecret");
										if (OutBoxSecret != null) {
											reciveMailInfor.setOutBoxSecret(jsonObject
													.getString("OutBoxSecret"));
										}
									}

									/* 是不是回执 */
									if (jsonObject.containsKey("IsreturnReceipt"))
										reciveMailInfor.setIsreturnReceipt(jsonObject
												.getBooleanValue("IsreturnReceipt"));
									/* 邮件的创建时间 */
									if (jsonObject.containsKey("CreateTime")) {
										String CreateTime = jsonObject
												.getString("CreateTime");
										if (CreateTime != null) {
											reciveMailInfor
													.setCreateTime(CreateTime);
										}
									}

									/* 邮件正文 */
									if (jsonObject.containsKey("OutBoxContent")) {
										String OutBoxContent = jsonObject
												.getString("OutBoxContent");
										if (OutBoxContent != null) {
											reciveMailInfor
													.setOutBoxContent(OutBoxContent);
										}
									}

									/* 获取附件 */
									if (jsonObject.containsKey("AccessoryList"))
										if (jsonObject.get("AccessoryList")!=null) {
											JSONArray jsonArray = jsonObject
													.getJSONArray("AccessoryList");
											List<MailAttachInfo> attachLists  = new ArrayList<MailAttachInfo>();
											for (int i = 0; i < jsonArray.size(); i++) {
												JSONObject tempJsonObject = jsonArray
														.getJSONObject(i);
												MailAttachInfo mailAttachInfo = new MailAttachInfo();
												
												mailAttachInfo.setFileName(tempJsonObject
														.getString("FileName"));
												mailAttachInfo.setFilePath(tempJsonObject
																.getString("FilePath"));
												mailAttachInfo.setFileSize(tempJsonObject
														.getIntValue("FileSize"));
												mailAttachInfo.setFileType(tempJsonObject
																.getString("FileType"));
												mailAttachInfo.setId(tempJsonObject
														.getString("Id"));
												attachLists.add(mailAttachInfo);
											}
											reciveMailInfor
													.setAttachLists(attachLists);
										}
									/* 获取完毕 判断返回 */
									if (reciveMailInfor != null) {
										Message msg = handler.obtainMessage(1); // 成功
										msg.obj = reciveMailInfor;
										msg.sendToTarget();
										return;
									}
								}
							}
						} catch (Exception e) {
							// TODO: handle exception
							e.printStackTrace();
						}
						Message msg = handler.obtainMessage(0); // 失败返回
						msg.obj = "数据解析错误";
						msg.sendToTarget();
						return;
					}

					@Override
					public void onRequestStart(RequestInfo requestInfo) {
						// TODO Auto-generated method stub

					}

					@Override
					public void onNoNetWork() {
						// TODO Auto-generated method stub
						Message msg = handler.obtainMessage(0); // 失败返回
						msg.obj = "网络异常，加载数据失败!";
						msg.sendToTarget();
						return;
					}

					@Override
					public void onUploadProgress(String url, int progress) {
						// TODO Auto-generated method stub

					}
				}, this);

	}

//	@Override
//	public void downAttachFile(final int position,final DownDbInfor downDbInfor,
//			final Handler handler) {
//		// TODO Auto-generated method stub
//		new Thread(new Runnable() {
//			@Override
//			public void run() {
//				// TODO Auto-generated method stub
//				if(!FileUtils.hasSDCard()){
//					Message msg = handler.obtainMessage(0); // 失败返回
//					msg.obj = "下载"+downDbInfor.getFileName()+"失败";
//					msg.sendToTarget();
//					return;
//				}
//				
//				String storedir = FileUtils.getSDPath() + Constant.downLoadPath;
//				File newDoc = new File(storedir);
//				if (!newDoc.exists()) {
//					newDoc.mkdirs();
//				}
//				String filePath = storedir+downDbInfor.getFileName();
//				File storefile = new File(filePath);
//				BufferedOutputStream bos = null;
//				BufferedInputStream bis = null;
//				URL url = null;
//				try {
//					bos = new BufferedOutputStream(new FileOutputStream(
//							storefile));
//					HttpURLConnection connection = null;
//					InputStream in = null;
//					String UrlString =URLs.getDefaultBaseURL()
//							+"/DownFile?id="
//							+ downDbInfor.getAttachId();
//					url = new URL(UrlString);
//					connection = (HttpURLConnection) url.openConnection();
//					connection.setConnectTimeout(10*1000);
//					in = connection.getInputStream();
//					bis = new BufferedInputStream(in);
//					int len;
//					byte[] b = new byte[1024*5];
//					while ((len = bis.read(b)) != -1) {
//						bos.write(b, 0, len);
//						bos.flush();
//					}
//					
//					try {
//						if(bos!=null && bis!=null){
//							bos.close();
//							bis.close();
//							Message msg = handler.obtainMessage(Constant.AttACH_LOAD_SUCCESS); // 下载成功返回
//							msg.obj = position;
////							msg.obj = "下载到  "+FileUtils.getSDPath() + Constant.downLoadPath;
//							msg.sendToTarget();
//						}
//					} catch (IOException e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//						Message msg = handler.obtainMessage(0); // 失败返回
//						msg.obj = "下载"+downDbInfor.getFileName()+"失败";
//						msg.sendToTarget();
//						storefile.delete();
//						return;
//					}
//				} catch (Exception e1) {
//					// TODO Auto-generated catch block
//					e1.printStackTrace();
//					Message msg = handler.obtainMessage(0); // 失败返回
//					msg.obj = "下载"+downDbInfor.getFileName()+"失败";
//					msg.sendToTarget();
//					storefile.delete();
//					return ;
//				} finally {
//					
//				}
//			}
//		}).start();
//	}

	/**
	 * 下载附件   新的处理   3.10 蔡晓杰
	 * 
	 * */
	@Override
	public void downMailAttachFile(MailAttachInfo mailAttachInfo) {
		MyTheard(mailAttachInfo);
	}
	
	
	private void MyTheard(final MailAttachInfo mailAttachInfo){
		new Thread(new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				/*有这个key的情况下*/
				if(PreferencesUtils.hasKey(context,  mailAttachInfo.getId())){
					long fileId = PreferencesUtils.getLong(context, mailAttachInfo.getId(), -1);
					if(fileId == -1){
						downFile(mailAttachInfo);
					}else{
						DownloadManager downloadManager = (DownloadManager) context.getSystemService(Service.DOWNLOAD_SERVICE);
						/*查询下载的状态*/
						if(isNeedDownload(mailAttachInfo,fileId, downloadManager)){
							downFile(mailAttachInfo);
						}
					}
				}else{ //key不存在的情况下    
					if(canDownLoadFile(mailAttachInfo)){
						downFile(mailAttachInfo);
					}
				}
			}
		}).start();
			
	}
	
	@SuppressLint("NewApi")
	private void  downFile(MailAttachInfo mailAttachInfo){
		String url = URLs.getDefaultBaseURL()+"/DownFile?id="+mailAttachInfo.getId();
		DownloadManager downloadManager = (DownloadManager) context.getSystemService(Service.DOWNLOAD_SERVICE);
		Uri resource = Uri.parse(url); 
		Request request = new Request(resource);
		MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();   //获取文件类型实例
        String mimeString = mimeTypeMap.getMimeTypeFromExtension(MimeTypeMap.getFileExtensionFromUrl(url) );   //获取文件类型
        request.setMimeType(mimeString);  //制定下载文件类型
		request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE|DownloadManager.Request.NETWORK_WIFI);  
		//设置下载中通知栏提示的标题  
        request.setTitle(mailAttachInfo.getFileName()+mailAttachInfo.getFileType());  
        request.setDescription("下载中...");  
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);  
        //表示下载允许的网络类型，默认在任何网络下都允许下载。有NETWORK_MOBILE、NETWORK_WIFI、NETWORK_BLUETOOTH三种及其组合可供选择。  
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE);  
        //移动网络情况下是否允许漫游。  
        request.setAllowedOverRoaming(false);  
        request.setVisibleInDownloadsUi(true);  
        request.setDestinationInExternalPublicDir(Constant.downLoadPath,changeFileName(mailAttachInfo));  //制定下载的目录里
        long fileId = downloadManager.enqueue(request);  //开始去下载
        /*将数据保存*/
        PreferencesUtils.putLong(context, mailAttachInfo.getId(), fileId);
	}
	
	
	/**
     * 判断是否需要调用系统下载
     * @param id 调用downloadManager.enqueue(request)时返回的id，that means  an ID for the download, unique across the system. This ID is used to make future calls related to this download
     * @return true if need download，else return false
     */
    private  boolean isNeedDownload(MailAttachInfo mailAttachInfo,long id, DownloadManager downloadManager) {
        if (downloadManager == null) {
            return true;
        }
        boolean isNeedDownloadAgain = true;
        DownloadManager.Query query = new DownloadManager.Query();
        query.setFilterById(id);
        Cursor cursor = downloadManager.query(query);
        if (cursor != null && cursor.moveToFirst()) {
            int columnStatus = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS);
            int status = cursor.getInt(columnStatus);
            switch (status) {
                case DownloadManager.STATUS_FAILED:
                    downloadManager.remove(id);
                	isNeedDownloadAgain = true;  //需要重新下载
                    break;
                case DownloadManager.STATUS_PAUSED:   //暂停
                	/*去除列表  停止下载*/
                	downloadManager.remove(id); 
                	isNeedDownloadAgain = true;
                    break;
                case DownloadManager.STATUS_PENDING:  //网络状态改变
                	/*去除列表  停止下载*/
                	downloadManager.remove(id); 
                	isNeedDownloadAgain = true;
                    break;
                case DownloadManager.STATUS_RUNNING:
                    isNeedDownloadAgain = false;
                    break;
                case DownloadManager.STATUS_SUCCESSFUL:
                	if(canDownLoadFile(mailAttachInfo)){  //打不开
                		/*去除列表  停止下载*/
                    	downloadManager.remove(id);
                		isNeedDownloadAgain = true;  //需要重新下载
                	}else{  //能打开
                		isNeedDownloadAgain = false;  //不需要重新下载
                	}
                    break;
                default :
                	/*去除列表  停止下载*/
                	downloadManager.remove(id); 
                	isNeedDownloadAgain = true;
                	break;
            }
        }else{
        	if(canDownLoadFile(mailAttachInfo)){
        		isNeedDownloadAgain = true;
        	}else{
        		isNeedDownloadAgain = false;
        	}
        }
        return isNeedDownloadAgain;
    }
	
    
    
    private void deleteFileID(MailAttachInfo mailAttachInfo){
    	PreferencesUtils.deleteKey(context, mailAttachInfo.getId());
    }
    
    private boolean canDownLoadFile(MailAttachInfo mailAttachInfo){
    	if(!FileUtils.hasSDCard()){
			return false;
		}
		String storedir = FileUtils.getSDPath() + Constant.downLoadPath;
		File newDoc = new File(storedir);
		if (!newDoc.exists()) {
			/*文件夹路径不存在*/
			newDoc.mkdirs();
		}
		String fileName = changeFileName(mailAttachInfo);
		String filePath = storedir+fileName;
		File storefile = new File(filePath);
		if(!storefile.exists()){
			/*文件不存在的情况下*/
		}else{
			if(storefile.length()!=mailAttachInfo.getFileSize()){
				FileUtils.deleteFile(Constant.downLoadPath,FileUtils.getFileName(fileName));
				return true;
			}
			deleteFileID(mailAttachInfo);
			FileUtils.openFile(storefile,context);
			return false;
		}
		return true;
    }
    
    
    
    
    /*转换文件名*/
    private String changeFileName(MailAttachInfo mailAttachInfo){
    	String name = mailAttachInfo.getFileName()+"-"+mailAttachInfo.getId()+mailAttachInfo.getFileType();
    	return name;
    }
	
}
