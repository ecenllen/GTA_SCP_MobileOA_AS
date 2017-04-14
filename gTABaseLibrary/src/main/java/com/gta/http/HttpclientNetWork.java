package com.gta.http;

import android.content.Context;

import com.gta.http.RequestInfo.RequestMethod;
import com.gta.http.entity.GZipDecompressingEntity;
import com.gta.http.parse.ResponseParse;
import com.gta.http.upload.CustomMultipartEntity;
import com.gta.http.upload.UploadProgressListener;

import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HttpEntity;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.HttpResponse;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.NoHttpResponseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.conn.params.ConnPerRouteBean;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.HashMap;

public class HttpclientNetWork extends AbstractNetWork {
	public int CONNECT_TIMEOUT = 4 * 1000;
	public int READ_DATA_TIMEOUT = 20 * 1000;
	public DefaultHttpClient httpClient;
	public RequestInfo requestInfo;
	public ResponseParse responseHandler;
	public NetworkRequestTask networkRequestTask;
	public static HttpContext httpContext;
	public Context appContext;

	public HttpclientNetWork(Context context) {
		appContext = context.getApplicationContext();
		ini();
	}

	/**
	 * 初始化网络
	 */
	public void ini() {
		createHttpContext();
		createHttpclient();
		setRetryHandler();
		addInterceptor();
		setTimeout(CONNECT_TIMEOUT, READ_DATA_TIMEOUT);
	}

	public void createHttpContext() {
		if (httpContext == null) {
			httpContext = new BasicHttpContext();
			CookieUtils cookieUtils = new CookieUtils(
					appContext.getApplicationContext());
			httpContext.setAttribute(ClientContext.COOKIE_STORE, cookieUtils);
		}
	}

	public void createHttpclient() {
		HttpParams httpParams = iniHttpParams();
		SchemeRegistry schemeRegistry = new SchemeRegistry();
		schemeRegistry.register(new Scheme("http", PlainSocketFactory
				.getSocketFactory(), 80));
		schemeRegistry.register(new Scheme("https", DefaultSSLSocketFactory
				.getSocketFactory(), 443));
		// 使用线程安全的连接管理来创建HttpClient
		httpClient = new DefaultHttpClient(new ThreadSafeClientConnManager(
				httpParams, schemeRegistry), httpParams);
	}

	public HttpParams iniHttpParams() {
		// 设置连接超时时间和数据读取超时时间
		HttpParams httpParams = new BasicHttpParams();
		// HttpConnectionParams.setConnectionTimeout(httpParams,
		// CONNECT_TIMEOUT);
		// HttpConnectionParams.setSoTimeout(httpParams, READ_DATA_TIMEOUT);

		HttpConnectionParams.setTcpNoDelay(httpParams, true);
		HttpConnectionParams.setSocketBufferSize(httpParams, 1024 * 8);
		HttpProtocolParams.setVersion(httpParams, HttpVersion.HTTP_1_1);

		ConnManagerParams.setTimeout(httpParams, CONNECT_TIMEOUT);
		ConnManagerParams.setMaxConnectionsPerRoute(httpParams,
				new ConnPerRouteBean(10));
		ConnManagerParams.setMaxTotalConnections(httpParams, 10);
		return httpParams;

	}

	public void setRetryHandler() {
		httpClient.setHttpRequestRetryHandler(new DefaultRetryHandler());
	}

	public void setTimeout(int connectionTimeout, int soTimeout) {
		httpClient.getParams().setParameter(
				CoreConnectionPNames.CONNECTION_TIMEOUT,
				connectionTimeout <= 0 ? CONNECT_TIMEOUT : connectionTimeout);
		httpClient.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT,
				connectionTimeout <= 0 ? READ_DATA_TIMEOUT : soTimeout);
	}

	public void addInterceptor() {
		addRequestInterceptor();
		addResponseInterceptor();
	}

	public void addRequestInterceptor() {
		httpClient.addRequestInterceptor(new HttpRequestInterceptor() {
			@Override
			public void process(org.apache.http.HttpRequest httpRequest,
					HttpContext httpContext)
					throws org.apache.http.HttpException, IOException {
				if (!httpRequest.containsHeader("gzip")) {
					httpRequest.addHeader("Accept-Encoding", "gzip");
				}
			}
		});
	}

	public void addResponseInterceptor() {
		httpClient.addResponseInterceptor(new HttpResponseInterceptor() {
			@Override
			public void process(HttpResponse response, HttpContext httpContext)
					throws org.apache.http.HttpException, IOException {
				final HttpEntity entity = response.getEntity();
				if (entity == null) {
					return;
				}
				final Header encoding = entity.getContentEncoding();
				if (encoding != null) {
					for (HeaderElement element : encoding.getElements()) {
						if (element.getName().equalsIgnoreCase("gzip")) {
							response.setEntity(new GZipDecompressingEntity(
									response.getEntity()));
							return;
						}
					}
				}
			}
		});
	}


	@Override
	public ResponseInfo getResponse(RequestInfo requestInfo,
			ResponseParse responseHandler, NetworkRequestTask networkRequestTask) {
		this.requestInfo = requestInfo;
		this.responseHandler = responseHandler;
		this.networkRequestTask = networkRequestTask;
		ResponseInfo responseInfo = new ResponseInfo();
		responseInfo.requestCode = requestInfo.requestCode;
		if (requestInfo.method.equals(RequestMethod.GET)) {
			responseInfo = HttpGet(responseInfo);
		} else if (requestInfo.method.equals(RequestMethod.POST)) {
			responseInfo = HttpPost(responseInfo);
		}
		return responseInfo;
	}

	private ResponseInfo HttpGet(ResponseInfo responseInfo) {
		String url = requestInfo.url;
		RequestParams requestParams = requestInfo.params;
		if (requestParams != null && requestParams.params != null
				&& requestParams.params.size() > 0) {
			url = url
					+ "?"
					+ URLEncodedUtils.format(requestParams.params,
							requestParams.charset);
		}
		HttpGet httpGet = new HttpGet(url);
		try {
			HttpResponse httpResponse = httpClient
					.execute(httpGet, httpContext);
			int httpStatus = httpResponse.getStatusLine().getStatusCode();
			responseInfo.httpResponse = httpResponse;
			responseInfo.httpStatus = httpStatus;
			if (httpStatus == HttpStatus.SC_OK) {
				responseInfo.stateCode = SUCCESS;
				if (null != responseHandler) {
					responseInfo = responseHandler.parseResponse(requestInfo,
							responseInfo);
				}
			} else {
				responseInfo.errorMessage = "连接服务器失败";
				responseInfo.stateCode = CONNECTION_SERVER_ERROR;
			}
		} catch (ConnectTimeoutException e) {
			// 捕获ConnectionTimeout
			responseInfo.errorMessage = "连接服务器超时";
			responseInfo.stateCode = REQ_TIME_OUT;
		} catch (SocketTimeoutException e) {
			// 捕获SocketTimeout
			responseInfo.errorMessage = "读取数据超时";
			responseInfo.stateCode = READ_TIME_OUT;
		} catch (NoHttpResponseException e) {
			// 无服务器响应
			responseInfo.stateCode = HOST_NORESPONSE;
			responseInfo.errorMessage = "服务器无响应";
		} catch (ClientProtocolException e) {
			responseInfo.errorMessage = "连接服务器失败";
			responseInfo.stateCode = CONNECTION_SERVER_ERROR;
		} catch (IOException e) {
			responseInfo.errorMessage = "连接服务器失败";
			responseInfo.stateCode = CONNECTION_SERVER_ERROR;
		} finally {
			if (httpGet != null) {
				httpGet.abort();
			}
		}
		return responseInfo;

	}

	private ResponseInfo HttpPost(ResponseInfo responseInfo) {
		HttpPost httpPost = null;
		if (requestInfo.params == null) {
			responseInfo.errorMessage = "post params must be not null";
			responseInfo.stateCode = PARAMETER_ERROR;
			return responseInfo;
		}

		try {
			HttpEntity httpEntity = requestInfo.params.getEntity();
			if (httpEntity instanceof CustomMultipartEntity) {
				((CustomMultipartEntity) httpEntity)
						.setListener(new UploadProgressListener() {

							@Override
							public void transferred(long num, long ContentLength) {
								networkRequestTask.uploadProgress(num,
										ContentLength);
							}
						});

			}
			httpPost = new HttpPost(requestInfo.url);
			httpPost.setEntity(httpEntity);
			HttpResponse httpResponse = httpClient.execute(httpPost,
					httpContext);
			int httpStatus = httpResponse.getStatusLine().getStatusCode();
			responseInfo.httpResponse = httpResponse;
			responseInfo.httpStatus = httpStatus;
			if (httpStatus == HttpStatus.SC_OK) {
				responseInfo.stateCode = SUCCESS;
				if (null != responseHandler) {
					responseInfo = responseHandler.parseResponse(requestInfo,
							responseInfo);
				}
			} else {
				responseInfo.errorMessage = "连接服务器失败";
				responseInfo.stateCode = CONNECTION_SERVER_ERROR;
			}
		} catch (ConnectTimeoutException e) {
			// 捕获ConnectionTimeout
			responseInfo.errorMessage = "连接服务器超时";
			responseInfo.stateCode = REQ_TIME_OUT;
		} catch (SocketTimeoutException e) {
			// 捕获SocketTimeout
			responseInfo.errorMessage = "读取数据超时";
			responseInfo.stateCode = READ_TIME_OUT;
		} catch (NoHttpResponseException e) {
			// 无服务器响应
			responseInfo.stateCode = HOST_NORESPONSE;
			responseInfo.errorMessage = "服务器无响应";
		} catch (ClientProtocolException e) {
			responseInfo.errorMessage = "连接服务器失败";
			responseInfo.stateCode = CONNECTION_SERVER_ERROR;
		} catch (IOException e) {
			responseInfo.errorMessage = "连接服务器失败";
			responseInfo.stateCode = CONNECTION_SERVER_ERROR;
		} finally {
			if (httpPost != null) {
				httpPost.abort();
			}
		}
		return responseInfo;

	}

	@Override
	void addHeader(HashMap<String, String> map) {
		// TODO Auto-generated method stub

	}

	@Override
	void addCookie(HashMap<String, String> map) {
		// TODO Auto-generated method stub

	}

}
