package com.gta.scpoa.biz;

import android.widget.EditText;

import com.gta.scpoa.entity.ContactInfo;

/**
 * 通讯录业务处理接口
 * 
 * @author shengping.pan
 * 
 */
public interface ContactListBiz {

	/**
	 * 根据类型获取通讯录信息
	 * 
	 * @param type
	 */
	void getContactList(int type);

	/**
	 * 新增或修改通讯录信息
	 * 
	 * @param contact
	 */
	void addOrUpdateContact(ContactInfo contact);

	/**
	 * 删除通讯录信息
	 * 
	 * @param contactInfo
	 */
	void deleteContactList(ContactInfo contactInfo);

	/**
	 * 验证数据
	 * 
	 * @param etHomePhone
	 * @param etHomeAddress
	 * @param etUnitPhone
	 * @param etDuty
	 * @param etUnitName
	 * @param etEmail
	 * @param etMobile_phone
	 * @param etUserName
	 * @return 返回验证信息
	 */
	String verifyData(EditText etUserName, EditText etMobile_phone,
			EditText etEmail, EditText etUnitName, EditText etDuty,
			EditText etUnitPhone, EditText etHomeAddress, EditText etHomePhone, EditText etUnitAddress);

	/**
	 * 查询通讯录明细
	 * @param contact
	 * @param type 
	 */
	void getContactDetail(ContactInfo contact, int type);
}
