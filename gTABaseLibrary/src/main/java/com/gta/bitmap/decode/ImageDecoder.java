
package com.gta.bitmap.decode;

import java.io.IOException;
import java.io.InputStream;

import android.graphics.Bitmap;

import com.gta.bitmap.core.ImageLoadingInfo;


public interface ImageDecoder {


	Bitmap decode(InputStream inputStream,ImageLoadingInfo imageLoadingInfo ) throws IOException;
}
