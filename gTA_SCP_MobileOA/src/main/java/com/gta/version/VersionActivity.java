package com.gta.version;

import com.gta.scpoa.R;
import com.gta.scpoa.entity.VersionEntity;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class VersionActivity extends Activity implements OnClickListener{
	private VersionEntity entity=null;
	private TextView tv_current=null;
	private TextView tv_new=null;
	private TextView tv_content=null;
	private Button btn_update=null;
	private Button btn_cancel=null;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.version_layout);
		entity=(VersionEntity)getIntent().getSerializableExtra("version");
		if(entity==null){
			this.finish();
		}else{
			initView();
		}
		PackageManager manager=getApplication().getPackageManager();
		try {
			PackageInfo info = manager.getPackageInfo(getApplication().getPackageName(), 0);
			tv_current.setText(""+info.versionName);
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		tv_new.setText(""+entity.getVersionName());
		tv_content.setText(entity.getExplain());
	}
	private void initView(){
		tv_current=(TextView)findViewById(R.id.tv_version_current);
		tv_new=(TextView)findViewById(R.id.tv_version_new);
		tv_content=(TextView)findViewById(R.id.tv_version_content);
		tv_content.setMovementMethod(new ScrollingMovementMethod());
		btn_update=(Button)findViewById(R.id.btn_version_update);
		btn_update.setOnClickListener(this);
		btn_cancel=(Button)findViewById(R.id.btn_version_cancel);
		btn_cancel.setOnClickListener(this);
	}
	@Override
	public void onClick(View v) {
		Intent intent=null;
		switch(v.getId()){
		case R.id.btn_version_update:
			intent=new Intent("com.gta.app.version.update");
			sendBroadcast(intent);
			finish();
			break;
		case R.id.btn_version_cancel:
			intent=new Intent("com.gta.app.version.close");
//			sendBroadcast(intent);
			finish();
			break;
		}
	}
}
