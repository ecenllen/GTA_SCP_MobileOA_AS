package com.gta.scpoa.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.os.Environment;

/**
 * 高级的文件处理工具
 * 
 * @author bin.wang1
 * 
 */
public class AdvancedFileUtils {
	/**
	 * 获取文件扩展名
	 * 
	 * @param fileName
	 * @return
	 */
	public static String getFileFormat(String fileName) {
		if (StringUtils.isEmpty(fileName))
			return "";

		int point = fileName.lastIndexOf('.');
		return fileName.substring(point + 1);
	}

	/**
	 * 根据文件绝对路径获取文件名
	 * 
	 * @param filePath
	 * @return
	 */
	public static String getFileName(String filePath) {
		if (StringUtils.isEmpty(filePath))
			return "";
		return filePath.substring(filePath.lastIndexOf(File.separator) + 1);
	}
	/**
	 * 
	 * @param path 文件路径
	 * @param fileName 文件名，如xxx.jpg
	 * @param b
	 * @return 返回保存本地成功与否
	 */
	public static boolean saveBitmapToLocal(String path,String fileName,Bitmap b){
		if (b == null) {
			return false;
		}
		
		boolean result = false ;
		String storageState = Environment.getExternalStorageState();
		if (storageState.equals(Environment.MEDIA_MOUNTED)) {
			
			File dir = new File(path);
			if (!dir.exists()) {
				dir.mkdirs();
			}
			FileOutputStream fos=null;
			
			try {
				fos = new FileOutputStream(path + fileName);
				b.compress(CompressFormat.JPEG, 100, fos);
				fos.flush();
				result = true;
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}finally{
				try {
					fos.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
								
		}	
		return result;
	}

}
