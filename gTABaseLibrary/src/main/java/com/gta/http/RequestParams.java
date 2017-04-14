package com.gta.http;

import java.io.File;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.entity.mime.content.ContentBody;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.InputStreamBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;

import com.gta.http.entity.MultipartEntity;
import com.gta.http.upload.CustomMultipartEntity;

/**
 * 请求参数类
 * 
 * @author qinghua.liu
 * 
 */
public class RequestParams {
	/* 参数列表 */
	public List<NameValuePair> params;
	/* 文件参数列表 */
	public HashMap<String, ContentBody> fileParams;
	/* 编码 */
	public String charset = HTTP.UTF_8;

	public String JoinParams() {
		String param = "";
		if (null != params) {
			for (NameValuePair pair : params) {
				param += pair.getName() + "=" + pair.getValue() + "&";
			}
		}
		if (param.length() > 0) {
			param = param.substring(0, param.length() - 1);
		}
		return param;
	}

	/**
	 * 添加请求参数
	 * 
	 * @param name
	 *            key值
	 * @param value
	 *            value值
	 */
	public void addParams(String name, String value) {
		if (null == params) {
			params = new ArrayList<NameValuePair>();
		}
		this.params.add(new BasicNameValuePair(name, value));
	}

	/**
	 * 添加文件
	 * 
	 * @param name
	 * @param file
	 */
	public void addBodyParameter(String name, File file) {
		if (fileParams == null) {
			fileParams = new HashMap<String, ContentBody>();
		}
		fileParams.put(name, new FileBody(file));
	}

	public void addBodyParameter(String name, File file, String mimeType) {
		if (fileParams == null) {
			fileParams = new HashMap<String, ContentBody>();
		}
		fileParams.put(name, new FileBody(file, mimeType));
	}

	public void addBodyParameter(String name, File file, String mimeType,
			String charset) {
		if (fileParams == null) {
			fileParams = new HashMap<String, ContentBody>();
		}
		fileParams.put(name, new FileBody(file, mimeType, charset));
	}

	public void addBodyParameter(String name, File file, String fileName,
			String mimeType, String charset) {
		if (fileParams == null) {
			fileParams = new HashMap<String, ContentBody>();
		}
		fileParams.put(name, new FileBody(file, fileName, mimeType, charset));
	}

	public void addBodyParameter(String name, InputStream stream,
			String fileName) {
		if (fileParams == null) {
			fileParams = new HashMap<String, ContentBody>();
		}
		fileParams.put(name, new InputStreamBody(stream, fileName));
	}

	public void addBodyParameter(String name, InputStream stream,
			String fileName, String mimeType) {
		if (fileParams == null) {
			fileParams = new HashMap<String, ContentBody>();
		}
		fileParams.put(name, new InputStreamBody(stream, fileName, mimeType));
	}

	public void addBodyParameter(String name, byte[] byteArrayBody,
			String fileName) {
		if (fileParams == null) {
			fileParams = new HashMap<String, ContentBody>();
		}
		fileParams.put(name, new ByteArrayBody(byteArrayBody, fileName));
	}

	public void addBodyParameter(String name, String mimeType,
			byte[] byteArrayBody, String fileName) {
		if (fileParams == null) {
			fileParams = new HashMap<String, ContentBody>();
		}
		fileParams.put(name, new ByteArrayBody(byteArrayBody, mimeType,
				fileName));
	}

	public void addBodyParameter(String name, String text) {
		if (fileParams == null) {
			fileParams = new HashMap<String, ContentBody>();
		}
		try {
			fileParams
					.put(name, new StringBody(text, Charset.forName(charset)));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}

	public void addBodyParameter(String name, String text, String mimeType) {
		if (fileParams == null) {
			fileParams = new HashMap<String, ContentBody>();
		}
		try {
			fileParams.put(name,
					new StringBody(text, mimeType, Charset.forName(charset)));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}

	// POST
	public HttpEntity getEntity() {
		HttpEntity httpEntity = null;
		if (null != fileParams && fileParams.size() > 0) {
			httpEntity = new CustomMultipartEntity(HttpMultipartMode.STRICT,
					null, Charset.forName(charset));
			for (Entry<String, ContentBody> entry : fileParams.entrySet()) {
				((MultipartEntity) httpEntity).addPart(entry.getKey(),
						entry.getValue());
			}
			if (null != params && params.size() > 0) {
				Charset cs = Charset.forName(charset);
				for (NameValuePair valuePair : params) {
					StringBody stringBody = null;
					try {
						stringBody = new StringBody(valuePair.getValue(), cs);
						if (stringBody != null)
							((MultipartEntity) httpEntity).addPart(
									valuePair.getName(), stringBody);
					} catch (Exception e) {
					}

				}
			}
		} else if(null != params && params.size() > 0) {
			try {
				httpEntity = new UrlEncodedFormEntity(params,charset);
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}

		return httpEntity;
	}
}
