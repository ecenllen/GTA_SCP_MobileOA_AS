package com.gta.scpoa.adapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.gta.bitmap.core.DisplayImageOptions;
import com.gta.bitmap.core.ImageLoader;
import com.gta.bitmap.core.ImageLoaderConfiguration;
import com.gta.bitmap.display.FadeInBitmapDisplayer;
import com.gta.bitmap.display.RoundedBitmapDisplayer;
import com.gta.bitmap.listener.ImageLoadingListener;
import com.gta.bitmap.listener.SimpleImageLoadingListener;
import com.gta.scpoa.R;
import com.gta.scpoa.entity.TaskNewInfor;
import com.gta.scpoa.util.GetNextPeople;
import com.gta.scpoa.views.ui.SwipeLayout;

public class TaskAdapter extends BaseSwipeAdapter {

	private Context mContext;
	private Handler handler;
	private List<TaskNewInfor> listInfors;

	// 图片缓存处理
	private DisplayImageOptions options;
	private ImageLoadingListener animateFirstListener = new AnimateFirstDisplayListener();

	public TaskAdapter(Context context, List<TaskNewInfor> listInfors,
			Handler handler) {
		if (null != listInfors) {
			this.listInfors = listInfors;
		} else {
			this.listInfors = new ArrayList<TaskNewInfor>();
		}

		this.handler = handler;
		this.mContext = context;

		iniImageLoader();
		options = new DisplayImageOptions.Builder()
				.showImageOnLoading(R.drawable.app_icon)
				.showImageForEmptyUri(R.drawable.app_icon)
				.showImageOnFail(R.drawable.app_icon)
				.displayer(new RoundedBitmapDisplayer(5)).build();
	}

	public void iniImageLoader() {
		ImageLoaderConfiguration configuration = new ImageLoaderConfiguration.Builder(
				mContext).threadPoolSize(3)
				.threadPriority(Thread.NORM_PRIORITY - 2)
				.memoryCacheSize((int) (getIdealMemCacheSize()))
				.diskCacheSize(1024 * 1024 * 50).build();
		ImageLoader imageLoader = ImageLoader.getInstance();
		imageLoader.init(configuration);
	}

	private int getIdealMemCacheSize() {
		return (int) (Runtime.getRuntime().maxMemory() / 10);
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return listInfors.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return listInfors.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public int getSwipeLayoutResourceId(int position) {
		// TODO Auto-generated method stub
		return R.id.task_swipe;
	}

	@Override
	public View generateView(int position, View v, ViewGroup parent) {
		// TODO Auto-generated method stub
		 ViewHolder holder;
		if (v == null) {
			holder = new ViewHolder();
			v = LayoutInflater.from(mContext).inflate(R.layout.task_list_item,
					parent, false);
			holder.swipeLayout = (SwipeLayout) v.findViewById(R.id.task_swipe);
			holder.ll_menu = (LinearLayout) v.findViewById(R.id.task_ll_menu);
			v.setTag(holder);
		} else {
			holder = (ViewHolder) v.getTag();
		}
			FrameLayout.LayoutParams layoutParam = new FrameLayout.LayoutParams(
					FrameLayout.LayoutParams.WRAP_CONTENT,
					FrameLayout.LayoutParams.MATCH_PARENT);
			holder.ll_menu.setLayoutParams(layoutParam);
		return v;
	}

	@Override
	public void fillValues(int position, View convertView) {
		/* 头部的图片 */
		ImageView headImage = (ImageView) convertView
				.findViewById(R.id.task_image_head);
		/* 流程发起人 */
		TextView owerText = (TextView) convertView
				.findViewById(R.id.task_people_name);
		/* 流程名称 */
		TextView task_processName = (TextView) convertView
				.findViewById(R.id.task_processName);
		/* 主题 */
		TextView subjectText = (TextView) convertView
				.findViewById(R.id.task_tittle_text);
		/* 时间 */
		TextView creatTime = (TextView) convertView
				.findViewById(R.id.task_time_text);
		/* 同意 */
		LinearLayout task_agree_layout = (LinearLayout) convertView
				.findViewById(R.id.task_agree_layout);

		TaskNewInfor taskNewInfor = listInfors.get(position);
		String url = taskNewInfor.getAvatarUrl();
		if(null != url && !TextUtils.isEmpty(url)){
			if(!url.contains("http://")){
				url = "";
			}
		}
		/* 使用ImageLoader加载图片 */
		ImageLoader.getInstance().displayImage(url,
				headImage, options, animateFirstListener);
		/* 其他 */
		owerText.setText(taskNewInfor.getCreator());
		task_processName.setText("-"+taskNewInfor.getProcessName());
		subjectText.setText(taskNewInfor.getSubject());
		/* 时间 */
		String time = taskNewInfor.getCreateTime();
		
		try {
			String dataString = time.split(" ")[0];
			String timeString = time.split(" ")[1];
			String hourString = timeString.split(":")[0];
			String minString = timeString.split(":")[1];
			creatTime.setText(dataString + " " + hourString + ":" + minString);
		} catch (Exception e) {
			creatTime.setText(time);
		}

		if (taskNewInfor.getType() == 1) {
			task_agree_layout.setVisibility(View.VISIBLE);
		} else {
			task_agree_layout.setVisibility(View.GONE);
		}

		ViewHolder holder = (ViewHolder) convertView.getTag();
		task_agree_layout.setOnClickListener(new MyOnClickListener(position,
				holder.swipeLayout));

//		if (position % 2 == 0) {
//			convertView.setBackgroundColor(0xffdbeef4);
//		} else {
//			convertView.setBackgroundColor(Color.WHITE);
//		}
	}

	class ViewHolder {
		public SwipeLayout swipeLayout;
		public LinearLayout ll_menu;
		public TextView position;
	}
	
	private static class AnimateFirstDisplayListener extends
			SimpleImageLoadingListener {
		static final List<String> displayedImages = Collections
				.synchronizedList(new LinkedList<String>());

		@Override
		public void onLoadingComplete(String imageUri, View view,
				Bitmap loadedImage) {
			if (loadedImage != null) {
				ImageView imageView = (ImageView) view;
				boolean firstDisplay = !displayedImages.contains(imageUri);
				if (firstDisplay) {
					FadeInBitmapDisplayer.animate(imageView, 500);
					Log.e("url========", "========imageUri======" + imageUri);
					displayedImages.add(imageUri);
				}
			}
		}
	}

	public class MyOnClickListener implements OnClickListener {

		private int index = 0;
		private SwipeLayout swipe;

		public MyOnClickListener(int i, SwipeLayout swipeLayout) {
			this.index = i;
			this.swipe = swipeLayout;
		}

		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.task_agree_layout: // 同意
				close();
				TaskNewInfor taskNewInfor = listInfors.get(index);
				new GetNextPeople(mContext, handler).getPeople(taskNewInfor, null, false);
				break;
			default:
				break;
			}
		}

		private void close() {
			if (null != swipe) {
				swipe.close();
			}
		}
	}

}
