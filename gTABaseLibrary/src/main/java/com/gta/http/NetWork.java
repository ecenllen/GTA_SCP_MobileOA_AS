package com.gta.http;


public interface NetWork {
	public static final int SUCCESS = 0x000001;
	/** 没有网络连接 */
	public static final int NO_NETWORK = -0x000001;

	/** 服务器连接失败 */
	public static final int CONNECTION_SERVER_ERROR = -0x000002;

	/** 请求参数错误 */
	public static final int PARAMETER_ERROR = -0x000003;
	/** 请求URI错误 */
	public static final int URI_ERROR = -0x000004;

	/** 请求超时 */
	public static final int REQ_TIME_OUT = -0x000009;
	/** 读取超时 */
	public static final int READ_TIME_OUT = -0x000010;
	/** 服务器无响应 */
	public static final int HOST_NORESPONSE = -0x000011;
	/** 未知错误 */
	public static final int UNKNOW_ERROR = -0xFFFFFF;


}
