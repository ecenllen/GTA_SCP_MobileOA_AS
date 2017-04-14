package com.gta.scpoa.adapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.gta.bitmap.core.DisplayImageOptions;
import com.gta.bitmap.core.ImageLoader;
import com.gta.bitmap.core.ImageLoaderConfiguration;
import com.gta.bitmap.display.FadeInBitmapDisplayer;
import com.gta.bitmap.display.RoundedBitmapDisplayer;
import com.gta.bitmap.listener.ImageLoadingListener;
import com.gta.bitmap.listener.SimpleImageLoadingListener;
import com.gta.scpoa.R;
import com.gta.scpoa.entity.TaskInfo;
import com.gta.scpoa.util.UIUtils;
import com.gta.scpoa.views.TaskView;
import com.gta.scpoa.views.ui.SimpleSwipeListener;
import com.gta.scpoa.views.ui.SwipeLayout;
import com.gta.scpoa.views.ui.SwipeLayout.Status;

/**
 * 
 * 
 */
public class ListViewAdapter extends BaseSwipeAdapter {
	// 上下文对象
	private Context mContext;	
	private List<TaskInfo> userInfos;
	
	// 图片缓存处理
	private DisplayImageOptions options;
	private ImageLoadingListener animateFirstListener = new AnimateFirstDisplayListener();	
	


	// 构造函数
/*	public ListViewAdapter(Context mContext, List<String> strList) {
		this.mContext = mContext;
		this.strList = strList;
	}*/
	
	public ListViewAdapter(Context context, List<TaskInfo> userInfos) {
		this.mContext = context;
		if (userInfos != null) {
			this.userInfos = userInfos;
		} else {
			this.userInfos = new ArrayList<TaskInfo>();
		}
		
		iniImageLoader();
		
		options = new DisplayImageOptions.Builder()
		.showImageOnLoading(R.drawable.ic_launcher)
		.showImageForEmptyUri(R.drawable.ic_launcher)
		.showImageOnFail(R.drawable.ic_launcher)
		.displayer(new RoundedBitmapDisplayer(20)).build();		
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
	

	// SwipeLayout的布局id
	@Override
	public int getSwipeLayoutResourceId(int position) {
		return R.id.swipe;
	}

	@Override
	public View generateView(final int position, View convertView, ViewGroup parent) {
		// 加载或复用item界面
		final ViewHolder holder;
		if (convertView == null) {
			convertView = LayoutInflater.from(mContext).inflate(R.layout.listview_item,
					parent, false);
			holder = new ViewHolder();
			holder.swipeLayout = (SwipeLayout) convertView.findViewById(R.id.swipe);
			holder.ll_menu = (LinearLayout) convertView.findViewById(R.id.ll_menu);
			holder.tvId = (TextView) convertView.findViewById(R.id.tv_id);
			holder.ivHead = (ImageView) convertView.findViewById(R.id.iv_head);
			holder.tvName = (TextView) convertView.findViewById(R.id.tv_name);
			holder.tvApplyTitle = (TextView) convertView.findViewById(R.id.tv_apply_title);
			holder.tvApplyDate = (TextView) convertView.findViewById(R.id.tv_apply_date);
			holder.tvDetail = (TextView) convertView.findViewById(R.id.tv_detail);			
			
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
				

		// 当隐藏的删除menu被打开的时候的回调函数
		holder.swipeLayout.addSwipeListener(new SimpleSwipeListener() {
			@Override
			public void onOpen(SwipeLayout layout) {
				Toast.makeText(mContext, "Open", Toast.LENGTH_SHORT).show();
			}
		});

		// 双击的回调函数
		holder.swipeLayout.setOnDoubleClickListener(new SwipeLayout.DoubleClickListener() {
					@Override
					public void onDoubleClick(SwipeLayout layout,
							boolean surface) {
						if (holder.swipeLayout.getOpenStatus() == Status.Open) {
							Toast.makeText(
									mContext, "aoe" + (holder.swipeLayout.getOpenStatus() == Status.Open),
									Toast.LENGTH_SHORT).show();
							holder.swipeLayout.close();
							notifyDataSetChanged();
						}
						Toast.makeText(
								mContext,
								"DoubleClick"
										+ (holder.swipeLayout.getOpenStatus() == Status.Open),
								Toast.LENGTH_SHORT).show();
					}
				});

		// 添加删除布局的点击事件
		holder.swipeLayout.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				Toast.makeText(mContext, "###", Toast.LENGTH_SHORT).show();
				System.out.println(holder.swipeLayout.getOpenStatus() == Status.Open);
				if (holder.swipeLayout.getOpenStatus() == Status.Open) {
					holder.swipeLayout.close();
					notifyDataSetChanged();
				}
			}
		});

/*		// 添加删除布局的点击事件
		holder.ll_menu.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// 点击完成之后，关闭删除menu
				holder.swipeLayout.close();
				UIUtils.ToastMessage(mContext, "位置：" + position);
				
				userInfos.remove(position);
//				strList.remove(position);
				Toast.makeText(mContext, "删除成功", Toast.LENGTH_SHORT).show();
				notifyDataSetChanged();
			}
		});*/
		FrameLayout.LayoutParams layoutParam = new FrameLayout.LayoutParams(
				200, FrameLayout.LayoutParams.MATCH_PARENT);
		holder.ll_menu.setLayoutParams(layoutParam);
		
		return convertView;
	}

	// 对控件的填值操作独立出来了，我们可以在这个方法里面进行item的数据赋值
	@Override
	public void fillValues(final int position, View convertView) {
		// 获取指定位置的数据
		TaskInfo userInfo = getItem(position);
		// 将数据写入到item界面
		final ViewHolder holder = (ViewHolder) convertView.getTag();
		
		holder.tvId.setText(userInfo.getId());
		holder.tvName.setText(userInfo.getName());
		holder.tvApplyTitle.setText(userInfo.getTitle());
		holder.tvDetail.setText(userInfo.getDetail());
		holder.tvApplyDate.setText(userInfo.getApplyDate());
		
		// 获取图片
		ImageLoader.getInstance().displayImage(getItem(position).getPicPath(),
				holder.ivHead, options, animateFirstListener);	
		
		// 添加删除布局的点击事件
		holder.ll_menu.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// 点击完成之后，关闭删除menu
				holder.swipeLayout.close();		
		
//				TaskView v = new TaskView(mContext, true);
//				AlertDialog d = UIUtils.showDialog(mContext, v);
//				v.setAlertDialog(d);
			}
		});		
	}

	@Override
	public int getCount() {
		return userInfos.size();
	}

	@Override
	public TaskInfo getItem(int position) {
		return userInfos.get(position);
	}

	@Override
	public long getItemId(int position) {
		return Integer.parseInt(userInfos.get(position).getId());
	}

	public class ViewHolder {
		public SwipeLayout swipeLayout;
		public LinearLayout ll_menu;
		
		public ImageView ivHead;
		public TextView tvId;
		public TextView tvName;
		public TextView tvHeadPath;
		public TextView tvApplyTitle;
		public TextView tvDetail;
		public TextView tvApplyDate;
		
		public TextView position;
		
//		private String id;				//事项id
//		private String name;			//姓名
//		private String picPath;			//头像url路径
//		private String title;			//事项标题
//		private String detail;			//事项详细信息
//		private String applyDate;		//申请时间
//		private String dealState;		//办理状态 ，待办/已办		
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
					displayedImages.add(imageUri);
				}
			}
		}
	}	
}
