package com.gta.bitmap.decode;

import java.io.IOException;
import java.io.InputStream;

import android.graphics.Bitmap;

import com.gta.bitmap.core.ImageLoadingInfo;
import com.gta.util.BaseBitmapUtils;

public class BaseImageDecoder implements ImageDecoder {

	@Override
	public Bitmap decode(InputStream inputStream,
			ImageLoadingInfo imageLoadingInfo) throws IOException {
		Bitmap bitmap = BaseBitmapUtils.decodeSampledBitmapFromInputStream(
				inputStream, imageLoadingInfo.targetSize.getWidth(),
				imageLoadingInfo.targetSize.getHeight());
		return bitmap;
	}

}