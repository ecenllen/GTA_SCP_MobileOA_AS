package com.gta.scpoa.activity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.gta.db.annotation.ViewInject;
import com.gta.scpoa.R;
import com.gta.scpoa.adapter.TimeSpanSpinnerAdapter;
import com.gta.scpoa.application.GTAApplication;
import com.gta.scpoa.biz.IGetDataBiz;
import com.gta.scpoa.biz.impl.GetDataBizImpl;
import com.gta.scpoa.common.Config;
import com.gta.scpoa.common.Constant;
import com.gta.scpoa.entity.User;
import com.gta.scpoa.util.AdvancedFileUtils;
import com.gta.scpoa.util.DialogUtil;
import com.gta.scpoa.util.ImageUtils;
import com.gta.scpoa.util.StringUtils;
import com.gta.scpoa.util.UIUtils;
import com.gta.scpoa.views.CommonTopView;
import com.gta.scpoa.views.SwitchView;

import java.io.File;
import java.io.FilenameFilter;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * 个人设置
 * 
 * @author bin.wang1
 * 
 */
public class PersonalCenterActivity extends BaseActivity implements
		OnClickListener, OnCheckedChangeListener {

	private final static String FILE_SAVEPATH = Environment
			.getExternalStorageDirectory().getAbsolutePath()
			+ Config.DEFAULT_FACE_PATH;

	private final static int CROP = 200;
	private String protraitPath;// 裁剪头像的绝对路径
	private String copyProtraitName;// 裁剪头像的名字

	private File protraitFile;// 裁剪头像的绝对路径形成的文件
	private Uri origUri;
	private Uri cropUri;

	@ViewInject(id = R.id.personalcenter_username_tv)
	private TextView mUserNameTV;
	@ViewInject(id = R.id.personalcenter_department_tv)
	private TextView mDepartmentTV;
	@ViewInject(id = R.id.personalcenter_telephone_tv)
	private TextView mTelephoneTV;
	@ViewInject(id = R.id.personalcenter_email_tv)
	private TextView mEmailTV;
	@ViewInject(id = R.id.personalcenter_headphoto_iv)
	private ImageView mPortraitIV;
	@ViewInject(id = R.id.personalcenter_remindset_spinner)
	private Spinner mTimerSpanSP;

	@ViewInject(id = R.id.personalcenter_switch_notice)
	private SwitchView mNoticeSwitcher;
	@ViewInject(id = R.id.personalcenter_switch_ring)
	private SwitchView mRingSwitcher;
	@ViewInject(id = R.id.personalcenter_switch_shake)
	private SwitchView mShakeSwitcher;
	private Bitmap protraitBitmap;

	@ViewInject(id = R.id.personalcenter_top_view)
	private CommonTopView topView;
	@ViewInject(id = R.id.personal_center_head_rl)
	private RelativeLayout mRelativeLayout;
	@ViewInject(id = R.id.personal_center_logout)
	private Button mLogoutBT;
	
	/**
	 * 
	 * 关于我们
	 * */
	@ViewInject(id = R.id.about_tv)   
	private RelativeLayout about_tv;
	
	/**
	 * 下载路径
	 */
	@ViewInject(id = R.id.path_tv)   
	private TextView path_tv;
	
	private PersonalHandler personalHandler;
	
	private ProgressDialog dialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_personal_center);
		personalHandler = new PersonalHandler(this);
		setupViews();
		setData();
	}

	private static class PersonalHandler extends Handler{		
		WeakReference<PersonalCenterActivity> wr;
		PersonalHandler(PersonalCenterActivity activity) {		
			wr = new WeakReference<PersonalCenterActivity>(activity);			
		}
		@Override
		public void handleMessage(Message msg) {
			
			PersonalCenterActivity mActivity = wr.get();						
			switch (msg.what) {
					
			case Constant.MSG_OPERATE_SUCCESS:
				switch (msg.arg1) {
				
				case 1:// 上传图片成功
					DialogUtil.dismissDialog(mActivity.dialog);
					
					UIUtils.ToastMessage(mActivity.getApplicationContext(), msg.obj.toString());
					mActivity.mPortraitIV.setImageBitmap(UIUtils.toRoundBitmap(mActivity.protraitBitmap));
					
					mActivity.saveProtraitInfo(Constant.PROP_KEY_SERVER_PORTRAIT,mActivity.protraitPath);
					mActivity.saveProtraitInfo(Constant.PROP_KEY_PORTRAIT,mActivity.protraitPath);
					mActivity.clearHistoryProtrait(mActivity.copyProtraitName);
					break;
					
				case 2://加载网络图片成功
					
					Bitmap bm = (Bitmap) msg.obj;
					if (bm !=null) {						
						mActivity.mPortraitIV.setImageBitmap(UIUtils.toRoundBitmap(bm));
					}else {
						UIUtils.showLocalPortrait(mActivity.mPortraitIV, 
								mActivity.getGTAApplication().getProperty(Constant.PROP_KEY_PORTRAIT), 200, 200);
					}
					
					String path =mActivity.getGTAApplication().getProperty(Constant.PROP_KEY_PORTRAIT);
					String newPath = mActivity.getGTAApplication().getProperty(Constant.PROP_KEY_SERVER_PORTRAIT);
										
					if (! StringUtils.isEmpty(newPath)) {								
						String newFileName = AdvancedFileUtils.getFileName(newPath);						
						if (!StringUtils .isEmpty(path)) {																	
							String fileName = AdvancedFileUtils.getFileName(path);
							
							if (! fileName.equals(newFileName)) {
								//删除本地的历史图片
								mActivity.clearHistoryProtrait(fileName);
								//将该本地图片缓存到本地
								boolean successed = AdvancedFileUtils.saveBitmapToLocal(FILE_SAVEPATH, newFileName, bm);
								if (successed) {
									mActivity.saveProtraitInfo(Constant.PROP_KEY_PORTRAIT,FILE_SAVEPATH+newFileName);
								}
							}									
						}else {
							//本地的路径为空，则不用删除，直接存储
							//将该本地图片缓存到本地
							boolean successed = AdvancedFileUtils.saveBitmapToLocal(FILE_SAVEPATH, newFileName, bm);
							if (successed) {
								mActivity.saveProtraitInfo(Constant.PROP_KEY_PORTRAIT,FILE_SAVEPATH+newFileName);
							}
						}					
					}
					break;

				default:
					break;
				}
				
				
				break;
			case Constant.MSG_FAIL:
				
				switch (msg.arg1) {
				case 1:// 上传图片失败	
					DialogUtil.dismissDialog(mActivity.dialog);
					UIUtils.ToastMessage(mActivity.getApplicationContext(), msg.obj.toString());
					break;
				case 2://加载网络图片失败
//					UIUtils.ToastMessage(mActivity.getApplicationContext(),msg.obj.toString());
					UIUtils.showLocalPortrait(mActivity.mPortraitIV,GTAApplication.instance.getProperty(Constant.PROP_KEY_PORTRAIT), 200, 200);
					break;
				default:
					break;
				}
				
				break;
			
			case Constant.MSG_SHOW_PROGRESS:				
				DialogUtil.showDialog(mActivity.dialog, msg.obj.toString());					
				break;
				
			default:
				break;
			}
						
		}
		
	}

	private void setData() {
		User user = getGTAApplication().getLoginInfo();
		//User [Successed=false, ErrorMsg=null, FullName=系统管理员, BpmHost=null, UserId=100010001,
		//LoginName=null, UserName=null, Password=null, 
		//AvatarUrl=/ecloud/face/100010001/20150902115437965.jpg, Department=, Telephone=, Email=]
//		Log.e("m_tag", "=======user=======" + user);
		if (user != null) {
			
			mUserNameTV.setText(user.getFullName());
			mDepartmentTV.setText(user.getDepartment());
			mTelephoneTV.setText(user.getTelephone());
			mEmailTV.setText(user.getEmail());

			String imgUrl = user.getAvatarUrl();
//			if(!imgUrl.contains("http://")){
//				imgUrl = "http://" + imgUrl;
//			}
//			saveProtraitInfo(Constant.PROP_KEY_PORTRAIT, imgUrl);
			String localPath = getGTAApplication().getProperty(Constant.PROP_KEY_PORTRAIT);
			UIUtils.showPortrait(personalHandler,mPortraitIV, imgUrl, localPath, 200, 200);
		}
	}

	private void setupViews() {
		
		
		topView.setBackImageButtonEnable(true);
		topView.setBackImageButtonOnClickListener(this);
		topView.setTitleTextViewEnable(true);
		topView.setTitleTextViewText(getString(R.string.personalcenter_title));
	
		mRelativeLayout.setOnClickListener(this);		
		mLogoutBT.setOnClickListener(this);
		
	
			
		TimeSpanSpinnerAdapter adapter=new TimeSpanSpinnerAdapter(this);
		mTimerSpanSP.setAdapter(adapter);
	
		String tempTimeSpan = getGTAApplication().getProperty(	Constant.PROP_KEY_TIME_SPAN);
		
		if (StringUtils.isEmpty(tempTimeSpan)) {		
			mTimerSpanSP.setSelection(0, true);			
			getGTAApplication().setProperty(Constant.PROP_KEY_TIME_SPAN,String.valueOf(0));
		} else {
			mTimerSpanSP.setSelection(Integer.parseInt(tempTimeSpan)-1, true);
		}
				
		mTimerSpanSP.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
			
				int oldValue = Integer.parseInt(getGTAApplication().getProperty(Constant.PROP_KEY_TIME_SPAN));
				int newValue = position+1;
				//如果与上次的不一致，则发送广播，更新定时服务中定时器的时间间隔
				if (oldValue != newValue) {
					getGTAApplication().setProperty(Constant.PROP_KEY_TIME_SPAN,String.valueOf(newValue));	
					//TODO 设置提醒周期更新数据，如已做推送实不需要此功能
//					Intent intent = new Intent(TimerService.ACTION_TIMER_SPAM_CHANGED);
//					sendBroadcast(intent);
				}				
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				
			}
		});
	
		mNoticeSwitcher.setOnCheckedChangeListener(this);
		mRingSwitcher.setOnCheckedChangeListener(this);
		mShakeSwitcher.setOnCheckedChangeListener(this);

		// 初始化 反的 true代表关，false代表开

		mNoticeSwitcher.setChecked(!Boolean.valueOf(getGTAApplication()
				.getProperty(Constant.PROP_KEY_SWITCHER_NOTICE)));
		mRingSwitcher.setChecked(!Boolean.valueOf(getGTAApplication()
				.getProperty(Constant.PROP_KEY_SWITCHER_RING)));
		mShakeSwitcher.setChecked(!Boolean.valueOf(getGTAApplication()
				.getProperty(Constant.PROP_KEY_SWITCHER_SHAKE)));
		
		//关于我们
		about_tv.setOnClickListener(this);
		//显示下载路径
		path_tv.setText(getSDPath()+Constant.downLoadPath);
	}

	
	private  String getSDPath() {
		File sdDir = null;
		boolean sdCardExist = hasSDCard(); // 判断sd卡是否存在
		if (sdCardExist) {
			sdDir = Environment.getExternalStorageDirectory();// 获取跟目录
		}else{
			return "";
		}
		return sdDir.toString();
	}

	private  boolean hasSDCard() {
		return Environment.MEDIA_MOUNTED.equals(Environment
				.getExternalStorageState());
	}
	
	@Override
	public void onClick(View v) {

		switch (v.getId()) {
		// 返回按钮
		case R.id.topbar_back_ibtn:
			this.finish();
			break;
		// 设置头像
		case R.id.personal_center_head_rl:
			CharSequence[] strs = { getString(R.string.photo_album),
					getString(R.string.photo_camera) };
			selectPicture(strs);
			break;
		// 退出登录
		case R.id.personal_center_logout:
			UIUtils.logout(this);
			break;
		//关于我们
		case R.id.about_tv:
			startActivity(new Intent(this, AboutActivity.class));
			break;
		default:
			break;
		}

	}

//	int item;
	
	/**
	 * 选择头像 相册/相机
	 * 
	 * @param strs
	 */
	private void selectPicture(CharSequence[] strs) {
		AlertDialog photoDialog = new AlertDialog.Builder(this)
				.setTitle("选择头像")
				.setItems(strs, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int item) {
						// 6.0 以上系统需要申请权限，把targetSdkVersion 设置为23（低于6.0）则不需要
//						if (ContextCompat.checkSelfPermission(PersonalCenterActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
//								!= PackageManager.PERMISSION_GRANTED) {
//							// 没有权限，申请权限。
//							if (ActivityCompat.shouldShowRequestPermissionRationale(PersonalCenterActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
//								// 用户拒绝过这个权限了，应该提示用户，为什么需要这个权限。
//							} else {
//								// 申请授权。
//								ActivityCompat.requestPermissions(PersonalCenterActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
//							}
//
//						}else{
//							// 有权限了，去放肆吧。
//							selectPhotoOrCamera();
//
//						}
						selectPhotoOrCamera(item);
							
					}
				}).create();
		photoDialog.show();
	}

	private void selectPhotoOrCamera(int item) {
		// 相册选图
		if ( item == 0) {
            startSelectPhotos();
        }
        // 手机拍照
        else if (item == 1) {
            startActionCamera();
        }
	}

//	@Override
//	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//		switch (requestCode) {
//			case 0: {
//				if (grantResults.length > 0
//						&& grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//					// 权限被用户同意，可以去放肆了。
//					// 相册选图
//					selectPhotoOrCamera();
//
//				} else {
//					// 权限被用户拒绝了，洗洗睡吧。
//					
//				}
//				return;
//			}
//		}
//	}

	/**
	 * 选择图片并裁剪
	 */
	/*protected void startSelectPhotos() {
		Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
		intent.addCategory(Intent.CATEGORY_OPENABLE);
		intent.setType("image*//*");
		startActivityForResult(Intent.createChooser(intent, "选择图片"),
				ImageUtils.REQUEST_CODE_GETIMAGE_BYCROP);

	}*/

	/**
	 * 从手机中选择
	 */
	private void startSelectPhotos() {
		Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
//		打开相册（方式二）
//      Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
//		intent.addCategory(Intent.CATEGORY_OPENABLE);
		intent.setType("image/*");
		startActivityForResult(Intent.createChooser(intent, "选择图片"), ImageUtils.REQUEST_CODE_GETIMAGE_BYCROP);
	}

	/**
	 * 打开相机
	 */
	protected void startActionCamera() {
		Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		intent.putExtra(MediaStore.EXTRA_OUTPUT, this.getCameraTempFile());
		startActivityForResult(intent,
				ImageUtils.REQUEST_CODE_GETIMAGE_BYCAMERA);

	}

	// 拍照保存的绝对路径
	private Uri getCameraTempFile() {
		String storageState = Environment.getExternalStorageState();
		if (storageState.equals(Environment.MEDIA_MOUNTED)) {
			File savedir = new File(FILE_SAVEPATH);
			if (!savedir.exists()) {
				savedir.mkdirs();
			}
		} else {
			UIUtils.ToastMessage(getGTAApplication(), "无法保存上传的头像，请检查SD卡是否挂载");
			return null;
		}
		String timeStamp = new SimpleDateFormat("yyyyMMddHHmmss", Locale.CHINA)
				.format(new Date());
		// 照片命名
		String cropFileName = "scpoa_"+getGTAApplication().getUserID()+"_" + timeStamp+ ".jpg";
		copyProtraitName = cropFileName;

		// 裁剪头像的绝对路径
		protraitPath = FILE_SAVEPATH + cropFileName;
		protraitFile = new File(protraitPath);
		if(Build.VERSION.SDK_INT >= 24) {
			cropUri = FileProvider.getUriForFile(this, getGTAApplication().getPackageName() + ".fileprovider", protraitFile);
		} else {
			cropUri = Uri.fromFile(protraitFile);
		}
		this.origUri = this.cropUri;
		return this.cropUri;
	}

	// 裁剪头像的绝对路径
	private Uri getUploadTempFile(Uri uri) {
		String storageState = Environment.getExternalStorageState();
		if (storageState.equals(Environment.MEDIA_MOUNTED)) {
			File savedir = new File(FILE_SAVEPATH);
			if (!savedir.exists()) {
				savedir.mkdirs();
			}
		} else {
			UIUtils.ToastMessage(getGTAApplication(), "无法保存上传的头像，请检查SD卡是否挂载");
			return null;
		}
		String timeStamp = new SimpleDateFormat("yyyyMMddHHmmss", Locale.CHINA)
				.format(new Date());
		String thePath = ImageUtils.getAbsolutePathFromNoStandardUri(uri);

		// 如果是标准Uri
		if (StringUtils.isEmpty(thePath)) {
			thePath = ImageUtils.getAbsoluteImagePath(
					PersonalCenterActivity.this, uri);
		}
		String ext = AdvancedFileUtils.getFileFormat(thePath);
		ext = StringUtils.isEmpty(ext) ? "jpg" : ext;

		// 照片命名
		String cropFileName = "scpoa_"+getGTAApplication().getUserID()+"_" + timeStamp + "." + ext;
		copyProtraitName = cropFileName;

		// 裁剪头像的绝对路径
		protraitPath = FILE_SAVEPATH + cropFileName;

		protraitFile = new File(protraitPath);
		cropUri = Uri.fromFile(protraitFile);
		return this.cropUri;
	}

	/**
	 * 拍照后裁剪
	 * 
	 * @param data
	 *            原始图片
	 */
	private void startActionCrop(Uri data) {
		Intent intent = new Intent("com.android.camera.action.CROP");
		intent.setDataAndType(data, "image/*");
		intent.putExtra("output", this.getUploadTempFile(data));
		intent.putExtra("crop", "true");
		intent.putExtra("aspectX", 1);// 裁剪框比例
		intent.putExtra("aspectY", 1);
		intent.putExtra("outputX", CROP);// 输出图片大小
		intent.putExtra("outputY", CROP);
		intent.putExtra("scale", true);// 去黑边
		intent.putExtra("scaleUpIfNeeded", true);// 去黑边
		startActivityForResult(intent,
				ImageUtils.REQUEST_CODE_GETIMAGE_BYSDCARD);
	}

	public void doCropImage(Uri uri) {
		try {
			Intent mIntent = new Intent("com.android.camera.action.CROP");
//            mIntent.setDataAndType(uri, "image/*");
			if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
				String url = ImageUtils.getPath(this, uri);
				mIntent.setDataAndTypeAndNormalize(Uri.fromFile(new File(url)), "image/*");
			} else {
				mIntent.setDataAndType(uri, "image/*");
			}

			cropUri = null;
			Uri uri1 = getUploadTempFile(uri);
			mIntent.putExtra("crop", "true");
			mIntent.putExtra("scale", true);// 去黑边
			mIntent.putExtra("scaleUpIfNeeded", true);// 去黑边
			mIntent.putExtra("aspectX", 1);   // 设置裁剪的宽、高比例为9：9
			mIntent.putExtra("aspectY", 1);
			mIntent.putExtra("outputX", 300); // outputX，outputY是裁剪的宽、高度
			mIntent.putExtra("outputY", 300);
			mIntent.putExtra(MediaStore.EXTRA_OUTPUT, uri1);
			mIntent.putExtra("return-data", false); //若为false则表示不返回数据
//            mIntent.putExtra(MediaStore.EXTRA_OUTPUT,uri);
//            mIntent.putExtra("return-data", false);
			mIntent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
			mIntent.putExtra("noFaceDetection", true); // 关闭人脸检测
			startActivityForResult(mIntent, ImageUtils.REQUEST_CODE_GETIMAGE_BYSDCARD);
		} catch (Exception e) {
			Toast.makeText(this, "裁剪失败，无法上传", Toast.LENGTH_SHORT).show();
		}
	}

	@Override
	protected void onActivityResult(final int requestCode,
			final int resultCode, final Intent data) {
		if (resultCode != RESULT_OK)
			return;
		switch (requestCode) {
		case ImageUtils.REQUEST_CODE_GETIMAGE_BYCAMERA:
//			startActionCrop(origUri);// 拍照后裁剪
			doCropImage(origUri);
			break;
		case ImageUtils.REQUEST_CODE_GETIMAGE_BYCROP:
//			startActionCrop(data.getData());// 选图后裁剪
			doCropImage(data.getData());
			break;
		case ImageUtils.REQUEST_CODE_GETIMAGE_BYSDCARD:
			uploadNewPhoto();// 上传新照片
			break;
		}
	}

	private void uploadNewPhoto() {

		if (dialog == null) {
			dialog=new ProgressDialog(this);
			DialogUtil.init(dialog, false);
		}
		
		// 获取头像缩略图
		if (!StringUtils.isEmpty(protraitPath) && protraitFile.exists()) {
			protraitBitmap = ImageUtils
					.loadImgThumbnail(protraitPath, 200, 200);
		} else {
			UIUtils.ToastMessage(getApplicationContext(), "图像不存在");
		}
		if (protraitBitmap != null && protraitFile != null) {
			dialog.show();
							
			IGetDataBiz api=new GetDataBizImpl();
			api.uploadProtrait(personalHandler, getGTAApplication().getUserID(), protraitFile);
		}
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		isChecked = isChecked ? false : true;// 转换一下
		switch (buttonView.getId()) {
		case R.id.personalcenter_switch_notice:			
			
			if (!isChecked) {
				//先关闭再变灰
				mRingSwitcher.setChecked(true);
				mShakeSwitcher.setChecked(true);
				
				mRingSwitcher.setEnabled(false);
				mShakeSwitcher.setEnabled(false);
				getGTAApplication().setProperty(Constant.PROP_KEY_SWITCHER_RING,String.valueOf(isChecked));
				getGTAApplication().setProperty(Constant.PROP_KEY_SWITCHER_SHAKE,String.valueOf(isChecked));
			} else {
				mRingSwitcher.setEnabled(true);
				mShakeSwitcher.setEnabled(true);
			}			
			getGTAApplication().setProperty(Constant.PROP_KEY_SWITCHER_NOTICE,	String.valueOf(isChecked));
			
			break;
		case R.id.personalcenter_switch_ring:
			getGTAApplication().setProperty(Constant.PROP_KEY_SWITCHER_RING,
					String.valueOf(isChecked));
			break;
		case R.id.personalcenter_switch_shake:
			getGTAApplication().setProperty(Constant.PROP_KEY_SWITCHER_SHAKE,
					String.valueOf(isChecked));
			break;

		default:
			break;
		}

	}

	private void saveProtraitInfo(String key,String path) {
		getGTAApplication().setProperty(key,	path);
	}

	private void clearHistoryProtrait(final String fileName) {

		String storageState = Environment.getExternalStorageState();
		if (storageState.equals(Environment.MEDIA_MOUNTED)) {
			File savedir = new File(FILE_SAVEPATH);
			if (savedir.exists()) {
				// 文件名过滤器
				FilenameFilter filter = new FilenameFilter() {

					@Override
					public boolean accept(File dir, String filename) {

						return !filename.equals(fileName);
					}
				};
				String[] list = savedir.list(filter);
				for (String fs : list) {
					File f = new File(savedir, fs);
					f.delete();
				}
			}
		} else {
			UIUtils.ToastMessageInDebugMode(getGTAApplication(), "无法保存上传的头像，请检查SD卡是否挂载");
		}
	}

}
