package com.gta.scpoa.biz.impl;

import java.util.ArrayList;
import java.util.List;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.widget.EditText;

import com.gta.http.HttpUtil;
import com.gta.http.RequestInfo;
import com.gta.http.RequestInfo.RequestMethod;
import com.gta.http.RequestListener;
import com.gta.http.RequestParams;
import com.gta.http.ResponseInfo;
import com.gta.http.parse.StringParse;
import com.gta.scpoa.application.GTAApplication;
import com.gta.scpoa.biz.ContactListBiz;
import com.gta.scpoa.entity.ContactInfo;
import com.gta.scpoa.util.URLs;

/**
 * 通讯录业务处理类
 * 
 * @author shengping.pan
 * 
 */
public class ContactListBizImpl implements ContactListBiz {

	private Context context = null;
	private Handler handler = null;
	private HttpUtil httpUtil = null;
	private String userId = "";

	public ContactListBizImpl(Context context, Handler handler) {
		this.context = context;
		this.handler = handler;
		httpUtil = HttpUtil.getInstance();
		userId = GTAApplication.instance.getUserID();
	}

	/**
	 * 根据类型获取通讯录信息
	 * type 1个人  2公用   3教职
	 */
	@Override
	public void getContactList(final int type) {
		String url = URLs.getDefaultBaseURL() + "/GetContactsList?userId="
				+ userId + "&conType=" + type;

		RequestInfo requestInfo = new RequestInfo(context, url);
		requestInfo.method = RequestMethod.GET;
		requestInfo.requestCode = 1;
		httpUtil.doRequest(requestInfo, new StringParse(),
				new RequestListener() { // 这里采用局部 也可以activity直接继承

					@Override
					public void onUploadProgress(String url, int progress) {

					}

					@Override
					public void onRequestSucceed(ResponseInfo response) {
						String resultString = response.stringResult;
						if (null != resultString && !"".equals(resultString)) {
							try {

								JSONObject reJsonObject = JSON.parseObject(resultString);
								if (reJsonObject.containsKey("Successed")) {
									if (reJsonObject.getBooleanValue("Successed")) {
										JSONArray jsonArray = reJsonObject
												.getJSONArray("Data");
										List<ContactInfo> contactList = new ArrayList<ContactInfo>();
										for (int i = 0; i < jsonArray.size(); i++) {
											JSONObject jsonObject = jsonArray
													.getJSONObject(i);
											ContactInfo contact = setContact(
													true, jsonObject,type);
											contact.setContactType(String
													.valueOf(type));
											contactList.add(contact);
										}
										if (contactList.size() >= 0) {
											Message msg = handler
													.obtainMessage(1);
											msg.obj = contactList;
											msg.sendToTarget();
											return;
										}
									}
								}
								Message msg = handler.obtainMessage(0); // 失败返回
								msg.obj = "返回数据错误!";
								msg.sendToTarget();
								return;
							} catch (Exception e) {
								e.printStackTrace();
								Message msg = handler.obtainMessage(0); // 失败返回
								msg.obj = "解析数据异常!";
								msg.sendToTarget();
								return;
							}
							
						}
					}

					@Override
					public void onRequestStart(RequestInfo requestInfo) {
					}

					@Override
					public void onRequestError(ResponseInfo responseInfo) {
						Message msg = handler.obtainMessage(0); // 失败返回
						msg.obj = "网络异常，加载数据失败!";
						msg.sendToTarget();
						return;
					}

					@Override
					public void onRequestCancelled(RequestInfo requestInfo) {
					}

					@Override
					public void onNoNetWork() {
						Message msg = handler.obtainMessage(0); // 失败返回
						msg.obj = "网络异常，加载数据失败!";
						msg.sendToTarget();
						return;
					}
				}, context);
	}

	/**
	 * 新增或修改通讯录信息
	 */
	@Override
	public void addOrUpdateContact(final ContactInfo contact) {

		String url = URLs.getDefaultBaseURL() + "/AddOrUpdateContacts";
		RequestInfo requestInfo = new RequestInfo(context, url);
		requestInfo.method = RequestMethod.POST;
		requestInfo.requestCode = 1;
		RequestParams requestParams = new RequestParams();
		requestParams.addParams("Id", contact.getContactId().toString());
		requestParams.addParams("Name", contact.getContactName());
		requestParams.addParams("Gender", contact.getSex());
		requestParams.addParams("MobilePhone", contact.getMobilePhone());
		requestParams.addParams("Email", contact.getEmail());
		requestParams.addParams("UnitName", contact.getDeptName());
		requestParams.addParams("Duty", contact.getDuty());
		requestParams.addParams("WorkPhone", contact.getDeptPhone());
		requestParams.addParams("WorkAddress", contact.getWorkAddress());
		requestParams.addParams("HomeAddress", contact.getHomeAddress());
		requestParams.addParams("HomePhone", contact.getHomePhone());
		requestParams.addParams("CreateBy", userId.toString());
		requestInfo.params = requestParams;
		httpUtil.doRequest(requestInfo, new StringParse(),
				new RequestListener() { // 这里采用局部 也可以activity直接继承

					@Override
					public void onUploadProgress(String url, int progress) {
					}

					@Override
					public void onRequestSucceed(ResponseInfo response) {
						String resultString = response.stringResult;
						if (null != resultString && !"".equals(resultString)) {
							try {
								JSONObject reJsonObject = JSON.parseObject(resultString);
								if (reJsonObject.containsKey("Successed")
										&& reJsonObject.getBooleanValue("Successed")) {
									if (!"-1".equals(contact.getContactId())) { // 编辑返回
										JSONObject data = reJsonObject
												.getJSONObject("Data");
										ContactInfo contactInfo = setContact(
												false, data ,1);

										Message msg = handler.obtainMessage(1); 
										msg.obj = contactInfo;
										msg.sendToTarget();
										return;
									} else { // 新增返回
										Message msg = handler.obtainMessage(2); 
										msg.sendToTarget();
										return;
									}

								} else {
									Message msg = handler.obtainMessage(0); // 失败返回
									msg.obj = "返回数据错误!";
									msg.sendToTarget();
									return;
								}
							} catch (Exception e) {
								e.printStackTrace();
								Message msg = handler.obtainMessage(0); // 失败返回
								msg.obj = "解析数据异常!";
								msg.sendToTarget();
								return;
							}
						}
					}

					@Override
					public void onRequestStart(RequestInfo requestInfo) {
					}

					@Override
					public void onRequestError(ResponseInfo responseInfo) {
						Message msg = handler.obtainMessage(0); // 失败返回
						msg.obj = "网络异常，保存数据失败!";
						msg.sendToTarget();
						return;
					}

					@Override
					public void onRequestCancelled(RequestInfo requestInfo) {
					}

					@Override
					public void onNoNetWork() {
						Message msg = handler.obtainMessage(0); // 失败返回
						msg.obj = "网络异常，保存数据失败!";
						msg.sendToTarget();
						return;
					}
				}, context);
	}

	/**
	 * 删除通讯录信息
	 */
	@Override
	public void deleteContactList(final ContactInfo contactInfo) {

		String url = URLs.getDefaultBaseURL() + "/DeleteContacts";
		RequestInfo requestInfo = new RequestInfo(context, url);
		requestInfo.method = RequestMethod.POST;
		requestInfo.requestCode = 1;
		RequestParams requestParams = new RequestParams();
		requestParams.addParams("Id", contactInfo.getContactId().toString());
		requestInfo.params = requestParams;
		httpUtil.doRequest(requestInfo, new StringParse(),
				new RequestListener() { // 这里采用局部 也可以activity直接继承

					@Override
					public void onUploadProgress(String url, int progress) {

					}

					@Override
					public void onRequestSucceed(ResponseInfo response) {
						String resultString = response.stringResult;
						if (null != resultString && !"".equals(resultString)) {
							try {

								JSONObject reJsonObject = JSON.parseObject(resultString);
								if (reJsonObject.containsKey("Successed") && reJsonObject.getBooleanValue("Successed")) {
									Message msg = handler.obtainMessage(1); // 成功返回
									msg.obj = contactInfo;
									msg.sendToTarget();
									return;
								} else {
									Message msg = handler.obtainMessage(0); // 失败返回
									msg.obj = "删除失败!";
									msg.sendToTarget();
									return;
								}
							} catch (Exception e) {
								e.printStackTrace();
								Message msg = handler.obtainMessage(0); // 失败返回
								msg.obj = "数据异常，删除失败!";
								msg.sendToTarget();
								return;
							}
						}
					}

					@Override
					public void onRequestStart(RequestInfo requestInfo) {
					}

					@Override
					public void onRequestError(ResponseInfo responseInfo) {
						Message msg = handler.obtainMessage(0); // 失败返回
						msg.obj = "网络异常，删除失败!";
						msg.sendToTarget();
						return;
					}

					@Override
					public void onRequestCancelled(RequestInfo requestInfo) {
					}

					@Override
					public void onNoNetWork() {
						Message msg = handler.obtainMessage(0); // 失败返回
						msg.obj = "网络异常，删除失败!";
						msg.sendToTarget();
						return;
					}
				}, context);
	}

	/**
	 * 验证数据
	 */
	@Override
	public String verifyData(EditText etUserName, EditText etMobile_phone,
			EditText etEmail, EditText etUnitName, EditText etDuty,
			EditText etUnitPhone, EditText etHomeAddress, EditText etHomePhone, EditText etUnitAddress) {
		 String name = etUserName.getText().toString().trim();
		 String email = etEmail.getText().toString().trim();

		// 验证邮件
		String regexEmail = "(\\w+\\.)*\\w+@(\\w+\\.)+([cC][oO][mM]|[cC][nN]|[nN][eE][tT])";

		if ("".equals(name)) {
			return "姓名不能为空!";
		}

		if (!email.equals("") && email != null) {
			if (!email.matches(regexEmail)) {
				return "请输入正确的邮件格式!";
			}
		}

		return "";
	}

	/**	
	 * 设置contac信息
	 * 
	 * @param isList		是否显示列表信息
	 * @param jsonObject
	 * @param type			通讯录类型   1个人   2公用   3教职
	 * @return
	 * @throws JSONException
	 */
	private ContactInfo setContact(boolean isList, JSONObject jsonObject, int type)
			 {
		ContactInfo contact = new ContactInfo();

		String id = jsonObject.getString("Id");
		contact.setContactId(id);

		String name = jsonObject.getString("Name");
		contact.setContactName(name);

		String mobilePhone = jsonObject.getString("MobilePhone");
		contact.setMobilePhone(mobilePhone);

		String unitName = jsonObject.getString("UnitName");
		contact.setDeptName(unitName);
		
		String duty = jsonObject.getString("Duty");
		contact.setDuty(duty);		
		
		if (!isList) {

			String email = jsonObject.getString("Email");
			contact.setEmail(email);
			
			String sex = jsonObject.getString("Gender");
			contact.setSex(sex);
			
			if(type != 3){
				String deptPhone = jsonObject.getString("WorkPhone");
				contact.setDeptPhone(deptPhone);
				
				String workAddress = jsonObject.getString("WorkAddress");
				contact.setWorkAddress(workAddress);			
				
				String homeAddress = jsonObject.getString("HomeAddress");
				contact.setHomeAddress(homeAddress);
				
				String homePhone = jsonObject.getString("HomePhone");
				contact.setHomePhone(homePhone);
			}

		}else if (type == 4) {
			String userId = jsonObject.getString("UserId");
			contact.setUserId(userId);
		}
		return contact;
	}

	/**
	 * 获取通讯录明细
	 */
	@Override
	public void getContactDetail(ContactInfo contact, final int type) {
		String url = URLs.getDefaultBaseURL() + "/GetContactsDetail?conType=" + type; 
		
		/*if(type == 3){  // 教职类通过userId获取详情
			url += "&userId=" + contact.getUserId();
		}else{  // 个人及公用通过contactId获取详情
			url += "&contactsId=" + contact.getContactId();
		}*/
		
		// 统一通过contactId获取
		url += "&contactsId=" + contact.getContactId();
		

		RequestInfo requestInfo = new RequestInfo(context, url);
		requestInfo.method = RequestMethod.GET;
		requestInfo.requestCode = 1;
		httpUtil.doRequest(requestInfo, new StringParse(),
				new RequestListener() { // 这里采用局部 也可以activity直接继承

					@Override
					public void onUploadProgress(String url, int progress) {

					}

					@Override
					public void onRequestSucceed(ResponseInfo response) {
						String resultString = response.stringResult;
						if (null != resultString && !"".equals(resultString)) {
							try {

								JSONObject reJsonObject = JSON.parseObject(resultString);
								if (reJsonObject.containsKey("Successed")) {
									if (reJsonObject.getBooleanValue("Successed")) {
										JSONObject jsonObject = reJsonObject
												.getJSONObject("Data");
										ContactInfo contact = setContact(false,
												jsonObject,type);
										Message msg = handler.obtainMessage(2);
										msg.obj = contact;
										msg.sendToTarget();
										return;
									}else{
										Message msg = handler.obtainMessage(0); // 失败返回
										msg.obj = "返回数据错误!";
										msg.sendToTarget();
										return;
									}
								} else {
									Message msg = handler.obtainMessage(0); // 失败返回
									msg.obj = "返回数据错误!";
									msg.sendToTarget();
									return;
								}
							} catch (Exception e) {
								e.printStackTrace();
								Message msg = handler.obtainMessage(0); // 失败返回
								msg.obj = "解析数据异常!";
								msg.sendToTarget();
								return;
							}
						}
					}

					@Override
					public void onRequestStart(RequestInfo requestInfo) {
					}

					@Override
					public void onRequestError(ResponseInfo responseInfo) {
						Message msg = handler.obtainMessage(0); // 失败返回
						msg.obj = "网络异常，加载数据失败!";
						msg.sendToTarget();
						return;
					}

					@Override
					public void onRequestCancelled(RequestInfo requestInfo) {
					}

					@Override
					public void onNoNetWork() {
						Message msg = handler.obtainMessage(0); // 失败返回
						msg.obj = "网络异常，加载数据失败!";
						msg.sendToTarget();
						return;
					}
				}, context);
	}

}
