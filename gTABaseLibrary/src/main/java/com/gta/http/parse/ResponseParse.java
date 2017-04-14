package com.gta.http.parse;

import com.gta.http.RequestInfo;
import com.gta.http.ResponseInfo;

/**
 * 返回数据解析接口
 */
public interface ResponseParse {

	/**
	 *  解析类需要实现
	 * @param requestInfo  请求信息类
	 * @param responseInfo 响应信息类
	 * @return 对responseInfo填入数据并返回
	 */
	ResponseInfo parseResponse(RequestInfo requestInfo,
			ResponseInfo responseInfo);

}
