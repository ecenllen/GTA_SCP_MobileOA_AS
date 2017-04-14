/*******************************************************************************
 * Copyright 2011-2014 Sergey Tarasevich
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package com.gta.bitmap.core;

import android.content.res.Resources;
import android.graphics.drawable.Drawable;

import com.gta.bitmap.display.BitmapDisplayer;
import com.gta.bitmap.process.BitmapProcessor;

public final class DisplayImageOptions {
	/* 图片加载中的资源id */
	private final int imageResOnLoading;
	/* 空URL的加载图片资源id */
	private final int imageResForEmptyUri;
	/* 加载失败的图片资源id */
	private final int imageResOnFail;
	private final Drawable imageForEmptyUri;
	private final Drawable imageOnFail;
	/* 存入缓存之前处理类 */
	private final BitmapProcessor preProcessor;
	/* 加载完成，显示处理的类 */
	private final BitmapDisplayer displayer;

	private DisplayImageOptions(Builder builder) {
		imageResOnLoading = builder.imageResOnLoading;
		imageResForEmptyUri = builder.imageResForEmptyUri;
		imageResOnFail = builder.imageResOnFail;
		imageForEmptyUri = builder.imageForEmptyUri;
		imageOnFail = builder.imageOnFail;
		preProcessor = builder.preProcessor;
		displayer = builder.displayer;
	}

	public boolean shouldShowImageForEmptyUri() {
		return imageForEmptyUri != null || imageResForEmptyUri != 0;
	}

	public boolean shouldShowImageOnFail() {
		return imageOnFail != null || imageResOnFail != 0;
	}

	public boolean shouldPreProcess() {
		return preProcessor != null;
	}

	public Drawable getImageForEmptyUri(Resources res) {
		return imageResForEmptyUri != 0 ? res.getDrawable(imageResForEmptyUri)
				: imageForEmptyUri;
	}

	public Drawable getImageOnFail(Resources res) {
		return imageResOnFail != 0 ? res.getDrawable(imageResOnFail)
				: imageOnFail;
	}

	public int getImageResOnLoading() {
		return imageResOnLoading;
	}

	public BitmapProcessor getPreProcessor() {
		return preProcessor;
	}

	public BitmapDisplayer getDisplayer() {
		return displayer;
	}

	/**
	 * Builder for {@link DisplayImageOptions}
	 * 
	 * @author Sergey Tarasevich (nostra13[at]gmail[dot]com)
	 */
	public static class Builder {
		private int imageResOnLoading = 0;
		private int imageResForEmptyUri = 0;
		private int imageResOnFail = 0;
		private Drawable imageForEmptyUri = null;
		private Drawable imageOnFail = null;
		private BitmapProcessor preProcessor = null;
		private BitmapDisplayer displayer = DefaultConfigurationFactory
				.createBitmapDisplayer();

		public Builder() {
		}

		/**
		 * 配置图片加载中的资源id
		 * 
		 * @param imageRes
		 * @return
		 */
		public Builder showImageOnLoading(int imageRes) {
			imageResOnLoading = imageRes;
			return this;
		}

		/**
		 * 配置空URL的资源id
		 * 
		 * @param imageRes
		 * @return
		 */
		public Builder showImageForEmptyUri(int imageRes) {
			imageResForEmptyUri = imageRes;
			return this;
		}

		/**
		 * 配置图片加载失败的资源id
		 * 
		 * @param imageRes
		 * @return
		 */
		public Builder showImageOnFail(int imageRes) {
			imageResOnFail = imageRes;
			return this;
		}

		/**
		 * 配置空URL的资源drawable
		 * 
		 * @param drawable
		 * @return
		 */
		public Builder showImageForEmptyUri(Drawable drawable) {
			imageForEmptyUri = drawable;
			return this;
		}

		/**
		 * 配置图片加载失败的资源drawable
		 * 
		 * @param drawable
		 * @return
		 */
		public Builder showImageOnFail(Drawable drawable) {
			imageOnFail = drawable;
			return this;
		}

		/**
		 * 配置存入缓存之前需要处理的类
		 * 
		 * @param preProcessor
		 * @return
		 */
		public Builder preProcessor(BitmapProcessor preProcessor) {
			this.preProcessor = preProcessor;
			return this;
		}

		/**
		 * 配置显示处理类
		 * 
		 * @param displayer
		 * @return
		 */
		public Builder displayer(BitmapDisplayer displayer) {
			if (displayer == null)
				throw new IllegalArgumentException("displayer can't be null");
			this.displayer = displayer;
			return this;
		}

		/** Sets all options equal to incoming options */
		public Builder cloneFrom(DisplayImageOptions options) {
			imageResOnLoading = options.imageResOnLoading;
			imageResForEmptyUri = options.imageResForEmptyUri;
			imageResOnFail = options.imageResOnFail;
			imageForEmptyUri = options.imageForEmptyUri;
			imageOnFail = options.imageOnFail;
			preProcessor = options.preProcessor;
			displayer = options.displayer;
			return this;
		}

		/** Builds configured {@link DisplayImageOptions} object */
		public DisplayImageOptions build() {
			return new DisplayImageOptions(this);
		}
	}

	public static DisplayImageOptions createSimple() {
		return new Builder().build();
	}

}
