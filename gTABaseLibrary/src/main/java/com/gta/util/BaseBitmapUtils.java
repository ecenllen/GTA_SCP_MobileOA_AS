package com.gta.util;

import java.io.FileDescriptor;
import java.io.IOException;
import java.io.InputStream;

import android.annotation.TargetApi;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.gta.cache.ImageCache;

public class BaseBitmapUtils {

	public static Bitmap decodeSampledBitmapFromInputStream(
			InputStream inputStream, int reqWidth, int reqHeight)
			throws IOException {
		byte[] bytes = BaseIoUtils.readStreamToByte(inputStream);
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeByteArray(bytes, 0, bytes.length, options);
		options.inSampleSize = calculateInSampleSize2(options, reqWidth,
				reqHeight);
		options.inJustDecodeBounds = false;
		return BitmapFactory.decodeByteArray(bytes, 0, bytes.length, options);
		// return BitmapFactory.decodeStream(inputStream);
	}

	/**
	 * Decode and sample down a bitmap from resources to the requested width and
	 * height.
	 * 
	 * @param res
	 *            The resources object containing the image data
	 * @param resId
	 *            The resource id of the image data
	 * @param reqWidth
	 *            The requested width of the resulting bitmap
	 * @param reqHeight
	 *            The requested height of the resulting bitmap
	 * @param cache
	 *            The ImageCache used to find candidate bitmaps for use with
	 *            inBitmap
	 * @return A bitmap sampled down from the original with the same aspect
	 *         ratio and dimensions that are equal to or greater than the
	 *         requested width and height
	 */
	public static Bitmap decodeSampledBitmapFromResource(Resources res,
			int resId, int reqWidth, int reqHeight, ImageCache cache) {

		// BEGIN_INCLUDE (read_bitmap_dimensions)
		// First decode with inJustDecodeBounds=true to check dimensions
		final BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeResource(res, resId, options);

		// Calculate inSampleSize
		options.inSampleSize = calculateInSampleSize(options, reqWidth,
				reqHeight);
		// END_INCLUDE (read_bitmap_dimensions)

		// If we're running on Honeycomb or newer, try to use inBitmap
		if (BaseVersionUtils.hasHoneycomb()) {
			addInBitmapOptions(options, cache);
		}

		// Decode bitmap with inSampleSize set
		options.inJustDecodeBounds = false;
		return BitmapFactory.decodeResource(res, resId, options);
	}

	/**
	 * Decode and sample down a bitmap from a file to the requested width and
	 * height.
	 * 
	 * @param filename
	 *            The full path of the file to decode
	 * @param reqWidth
	 *            The requested width of the resulting bitmap
	 * @param reqHeight
	 *            The requested height of the resulting bitmap
	 * @param cache
	 *            The ImageCache used to find candidate bitmaps for use with
	 *            inBitmap
	 * @return A bitmap sampled down from the original with the same aspect
	 *         ratio and dimensions that are equal to or greater than the
	 *         requested width and height
	 */
	public static Bitmap decodeSampledBitmapFromFile(String filename,
			int reqWidth, int reqHeight, ImageCache cache) {

		// First decode with inJustDecodeBounds=true to check dimensions
		final BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(filename, options);

		// Calculate inSampleSize
		options.inSampleSize = calculateInSampleSize(options, reqWidth,
				reqHeight);

		// If we're running on Honeycomb or newer, try to use inBitmap
		if (BaseVersionUtils.hasHoneycomb()) {
			addInBitmapOptions(options, cache);
		}

		// Decode bitmap with inSampleSize set
		options.inJustDecodeBounds = false;
		return BitmapFactory.decodeFile(filename, options);
	}

	/**
	 * Decode and sample down a bitmap from a file input stream to the requested
	 * width and height.
	 * 
	 * @param fileDescriptor
	 *            The file descriptor to read from
	 * @param reqWidth
	 *            The requested width of the resulting bitmap
	 * @param reqHeight
	 *            The requested height of the resulting bitmap
	 * @param cache
	 *            The ImageCache used to find candidate bitmaps for use with
	 *            inBitmap
	 * @return A bitmap sampled down from the original with the same aspect
	 *         ratio and dimensions that are equal to or greater than the
	 *         requested width and height
	 */
	public static Bitmap decodeSampledBitmapFromDescriptor(
			FileDescriptor fileDescriptor, int reqWidth, int reqHeight,
			ImageCache cache) {

		// First decode with inJustDecodeBounds=true to check dimensions
		final BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFileDescriptor(fileDescriptor, null, options);

		// Calculate inSampleSize
		options.inSampleSize = calculateInSampleSize(options, reqWidth,
				reqHeight);

		// Decode bitmap with inSampleSize set
		options.inJustDecodeBounds = false;

		// If we're running on Honeycomb or newer, try to use inBitmap
		if (BaseVersionUtils.hasHoneycomb()) {
			addInBitmapOptions(options, cache);
		}

		return BitmapFactory
				.decodeFileDescriptor(fileDescriptor, null, options);
	}

	@TargetApi(11)
	private static void addInBitmapOptions(BitmapFactory.Options options,
			ImageCache cache) {
		// BEGIN_INCLUDE(add_bitmap_options)
		// inBitmap only works with mutable bitmaps so force the decoder to
		// return mutable bitmaps.
		options.inMutable = true;

		if (cache != null) {
			// Try and find a bitmap to use for inBitmap
			Bitmap inBitmap = cache.getBitmapFromReusableSet(options);

			if (inBitmap != null) {
				options.inBitmap = inBitmap;
			}
		}
		// END_INCLUDE(add_bitmap_options)
	}

	/**
	 * Calculate an inSampleSize for use in a
	 * {@link android.graphics.BitmapFactory.Options} object when decoding
	 * bitmaps using the decode* methods from
	 * {@link android.graphics.BitmapFactory}. This implementation calculates
	 * the closest inSampleSize that is a power of 2 and will result in the
	 * final decoded bitmap having a width and height equal to or larger than
	 * the requested width and height.
	 * 
	 * @param options
	 *            An options object with out* params already populated (run
	 *            through a decode* method with inJustDecodeBounds==true
	 * @param reqWidth
	 *            The requested width of the resulting bitmap
	 * @param reqHeight
	 *            The requested height of the resulting bitmap
	 * @return The value to be used for inSampleSize
	 */
	public static int calculateInSampleSize(BitmapFactory.Options options,
			int reqWidth, int reqHeight) {
		// BEGIN_INCLUDE (calculate_sample_size)
		// Raw height and width of image
		final int height = options.outHeight;
		final int width = options.outWidth;
		int inSampleSize = 1;

		if (height > reqHeight || width > reqWidth) {

			final int halfHeight = height / 2;
			final int halfWidth = width / 2;

			// Calculate the largest inSampleSize value that is a power of 2 and
			// keeps both
			// height and width larger than the requested height and width.
			while ((halfHeight / inSampleSize) > reqHeight
					&& (halfWidth / inSampleSize) > reqWidth) {
				inSampleSize *= 2;
			}

			// This offers some additional logic in case the image has a strange
			// aspect ratio. For example, a panorama may have a much larger
			// width than height. In these cases the total pixels might still
			// end up being too large to fit comfortably in memory, so we should
			// be more aggressive with sample down the image (=larger
			// inSampleSize).

			long totalPixels = width * height / inSampleSize;

			// Anything more than 2x the requested pixels we'll sample down
			// further
			final long totalReqPixelsCap = reqWidth * reqHeight * 2;

			while (totalPixels > totalReqPixelsCap) {
				inSampleSize *= 2;
				totalPixels /= 2;
			}
		}
		return inSampleSize;
		// END_INCLUDE (calculate_sample_size)
	}

	public static int calculateInSampleSize2(BitmapFactory.Options options,
			int reqWidth, int reqHeight) {
		int w = options.outWidth;
		int h = options.outHeight;
		// 从服务器端获取的图片大小为：80x120
		// 我们想要的图片大小为：40x40
		// 缩放比：120/40 = 3，也就是说我们要的图片大小为原图的1/3
		// 缩放比。由于是固定比例缩放，只用高或者宽其中一个数据进行计算即可
		int ratio = 1; // 默认为不缩放
		if (w >= h && w > reqWidth) {
			ratio = (int) (w / reqWidth);
		} else if (w < h && h > reqHeight) {
			ratio = (int) (h / reqHeight);
		}
		if (ratio <= 0) {
			ratio = 1;
		}
		// 属性值inSampleSize表示缩略图大小为原始图片大小的几分之一，即如果这个值为2，
		// 则取出的缩略图的宽和高都是原始图片的1/2，图片大小就为原始大小的1/4。
		return ratio;

	}
}
