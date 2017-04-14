package com.gta.scpoa.activity.fragment;


import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.gta.scpoa.R;
import com.gta.scpoa.application.GTAApplication;
import com.gta.scpoa.entity.TaskNewInfor;

@SuppressLint("ValidFragment")
public class FlowChartFragment extends BaseFragmnet{
	
	private Context mContext;
	private View contentView;  
	private TaskNewInfor taskNewInfor;
	private WebView webView = null;
//	public  FlowChartFragment (Context context,TaskNewInfor taskNewInfor){
//		mContext = context;
//		this.taskNewInfor = taskNewInfor;
//	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mContext = getActivity();
		this.taskNewInfor = (TaskNewInfor)getArguments().getSerializable("infor");
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		if(contentView!=null){
			ViewGroup viewGroup = (ViewGroup)contentView.getParent();
			if(viewGroup!=null){
				viewGroup.removeView(contentView);
			}
		}else{
			contentView = inflater.inflate(R.layout.flow_chart_layout, null);
			/*初始化视图*/
			viewInit();
			/*网络请求  让web自己管理*/
			GTAApplication app = (GTAApplication) mContext.getApplicationContext();
			String baseUrl = app.getBpmHost();
			if(taskNewInfor.getType() == 1){//待办
				webViewInit(baseUrl+"/platform/bpm/processRun/" +
						"processImage.htmob?actInstId="+taskNewInfor.getActInstId());
			} else{
				webViewInit(baseUrl+"/platform/bpm/processRun/" +
						"processImage.htmob?runId="+taskNewInfor.getRunId());
			}
			Log.e("=====================", baseUrl+"/platform/bpm/processRun/" +
						"processImage.htmob?actInstId="+taskNewInfor.getActInstId());
		}
		return contentView;
	}
	
	private void viewInit(){
		webView  = (WebView) contentView.findViewById(R.id.flow_chart_webView);
	}
	

	private void webViewInit(String htmlString) {
		if (htmlString != null && !htmlString.equals("<br>")) {
			webView.getSettings().setDefaultTextEncodingName("UTF-8"); // 注意编码
			webView.loadUrl(htmlString);// 这种写法可以正确解码
			// 设置可以支持缩放 
			webView.getSettings().setSupportZoom(true); 
			webView.setInitialScale(100);//为25%
			webView.getSettings().setBuiltInZoomControls(true);
			//扩大比例的缩放
			webView.getSettings().setUseWideViewPort(true);
			webView.setWebViewClient(new HelloWebViewClient());
		}
	}
	
	 //Web视图  
    private class HelloWebViewClient extends WebViewClient {  
        @Override 
        public boolean shouldOverrideUrlLoading(WebView view, String url) {  
            view.loadUrl(url);  
            return true;  
        }  
    }
}
