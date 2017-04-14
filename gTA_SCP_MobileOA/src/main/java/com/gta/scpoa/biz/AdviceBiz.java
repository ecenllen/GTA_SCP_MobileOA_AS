package com.gta.scpoa.biz;

import com.gta.scpoa.entity.TaskNewInfor;

/**
 * 意见使用到的接口
 * @author xiaojie.cai
 *
 */
public interface AdviceBiz {
	
	/*获取表单数据成功*/
	public static final int MSG_TABLE_GET_SUCCESS = 1001;
	/*获取表单数据失败*/
	public static final int MSG_TABLE_GET_FAIL = 1000;
	/*驳回成功*/
	public static final int MSG_TABLE_BACK_SUCCESS = 2;
	/*驳回失败*/
	public static final int MSG_TABLE_BACK_FAIL = -2;
	/*获取表单的数据*/
	void getTableData(TaskNewInfor taskNewInfor);
}
