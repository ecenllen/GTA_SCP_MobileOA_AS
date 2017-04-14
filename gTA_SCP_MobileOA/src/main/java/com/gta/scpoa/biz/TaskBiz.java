package com.gta.scpoa.biz;

/**
 * 待办/已办  业务处理接口
 * @author jianbin.liang
 *
 */
public interface TaskBiz {
	
	/**********************************/
	public static final int firstLoad  = 1;  //第一次加载
	public static final int loadMore  = 2;  //加载更多
	public static final int onRefresh  = 3;  //加载新的
	
	
	public static final int MSG_TASK_GET_SUCCESS = 1001;
	public static final int MSG_TASK_GET_FAIL = 1000;
	public static final int MSG_TASK_GET_MORE = 1002; //加载更多
	public static final int MSG_TASK_GET_REFRSH = 1003; //没有搜索的下拉刷新
	public static final int MSG_TASK_SEARCH_SUCCESS=1020; //搜索成功
	public static final int MSG_TASK_SEARCH_REFRSH_SUCCESS=1021; //搜索下拉成功
	public static final int MSG_TASK_SEARCH_MORE_SUCCESS=1022; //搜索上拉更多成功
	
	/**
	 * 根据类型获取让任务信息
	 * 
	 * @param type
	 */
	void getTaskList(String id ,int type,int command,int limit,String creatTimeString,String saerchString);

	/**
	 * 获取
	 * @param searchStr 搜索内容
	 * @param limit 分页条数
	 * @param noticeId 公告(会议)id
	 * @param isMore 是否加载更多
	 * @param command 加载数据标识
	 */
	void getMeetingList(String searchStr, int limit, int noticeId, boolean isMore, int command);
	
}
