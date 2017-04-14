package com.gta.scpoa.util;

import java.io.File;
import java.io.FilenameFilter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import com.gta.scpoa.common.Constant;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Environment;

public class FileUtils {

	public static String FormetFileSize(long length) // 文件的大小
	{
		DecimalFormat df = new DecimalFormat("#.00");
		String fileSizeString = "";
		String wrongSize = "0B";
		if (length == 0)
			return wrongSize;
		if (length < 1024) {
			fileSizeString = df.format((double) length) + "B";
		} else if (length < 1048576) {
			fileSizeString = df.format((double) length / 1024) + "KB";
		} else if (length < 1073741824) {
			fileSizeString = df.format((double) length / 1048576) + "MB";
		} else {
			fileSizeString = df.format((double) length / 1073741824) + "GB";
		}
		return fileSizeString;
	}

	/* 获取sd路径 */
	public static String getSDPath() {
		File sdDir = null;
		boolean sdCardExist = hasSDCard(); // 判断sd卡是否存在
		if (sdCardExist) {
			sdDir = Environment.getExternalStorageDirectory();// 获取跟目录
		}
		return sdDir.toString();
	}

	public static boolean hasSDCard() {
		return Environment.MEDIA_MOUNTED.equals(Environment
				.getExternalStorageState());
	}

	/* 附件打开操作 */
	public static void openFile(File f, Context context) {
		Intent intent = new Intent();
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.setAction(android.content.Intent.ACTION_VIEW);
		String type = getMIMEType(f);
//		intent.setDataAndType(Uri.fromFile(f), "*/*");
		intent.setDataAndType(Uri.fromParts("file", "", null), type);
		ResolveInfo ri = context.getPackageManager().resolveActivity(intent,
				PackageManager.MATCH_DEFAULT_ONLY);
		if(ri!=null){
			intent.setDataAndType(Uri.fromFile(f), type);
			context.startActivity(intent);
		}else{
			intent.setDataAndType(Uri.fromFile(f), "*/*");
			context.startActivity(intent);
		}
		
	}

	
	
	
	private static String [][]  MIME_MapTable={   
            //{后缀名，MIME类型}    
            {"3gp",    "video/3gpp"},   
            {"apk",    "application/vnd.android.package-archive"},   
            {"asf",    "video/x-ms-asf"},   
            {"avi",    "video/x-msvideo"},   
            {"bin",    "application/octet-stream"},   
            {"bmp",    "image/bmp"},   
            {"c",  "text/plain"},   
            {"class",  "application/octet-stream"},   
            {"conf",   "text/plain"},   
            {"cpp",    "text/plain"},   
            {"doc",    "application/msword"},   
            {"docx",   "application/vnd.openxmlformats-officedocument.wordprocessingml.document"},   
            {"xls",    "application/vnd.ms-excel"},    
            {"xlsx",   "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"},   
            {"exe",    "application/octet-stream"},   
            {"gif",    "image/gif"},   
            {"gtar",   "application/x-gtar"},   
            {"gz", "application/x-gzip"},   
            {"h",  "text/plain"},   
            {"htm",    "text/html"},   
            {"html",   "text/html"},   
            {"jar",    "application/java-archive"},   
            {"java",   "text/plain"},   
            {"jpeg",   "image/jpeg"},   
            {"jpg",    "image/jpeg"},   
            {"js", "application/x-javascript"},   
            {"log",    "text/plain"},   
            {"m3u",    "audio/x-mpegurl"},   
            {"m4a",    "audio/mp4a-latm"},   
            {"m4b",    "audio/mp4a-latm"},   
            {"m4p",    "audio/mp4a-latm"},   
            {"m4u",    "video/vnd.mpegurl"},   
            {"m4v",    "video/x-m4v"},    
            {"mov",    "video/quicktime"},   
            {"mp2",    "audio/x-mpeg"},   
            {"mp3",    "audio/x-mpeg"},   
            {"mp4",    "video/mp4"},   
            {"mpc",    "application/vnd.mpohun.certificate"},          
            {"mpe",    "video/mpeg"},     
            {"mpeg",   "video/mpeg"},     
            {"mpg",    "video/mpeg"},     
            {"mpg4",   "video/mp4"},      
            {"mpga",   "audio/mpeg"},   
            {"msg",    "application/vnd.ms-outlook"},   
            {"ogg",    "audio/ogg"},   
            {"pdf",    "application/pdf"},   
            {"png",    "image/png"},   
            {"pps",    "application/vnd.ms-powerpoint"},   
            {"ppt",    "application/vnd.ms-powerpoint"},   
            {"pptx",   "application/vnd.openxmlformats-officedocument.presentationml.presentation"},   
            {"prop",   "text/plain"},   
            {"rc", "text/plain"},   
            {"rmvb",   "audio/x-pn-realaudio"},   
            {"rtf",    "application/rtf"},   
            {"sh", "text/plain"},   
            {"tar",    "application/x-tar"},      
            {"tgz",    "application/x-compressed"},    
            {"txt",    "text/plain"},   
            {"wav",    "audio/x-wav"},   
            {"wma",    "audio/x-ms-wma"},   
            {"wmv",    "audio/x-ms-wmv"},   
            {"wps",    "application/vnd.ms-works"},   
            {"xml",    "text/plain"},   
            {"z",  "*/*"},   
            {"zip",    "*/*"},   
            {"",        "*/*"}     
        };   
  
	private static String getMIMEType(File f) {
//		String end = f
//				.getName()
//				.substring(f.getName().lastIndexOf(".") + 1,
//						f.getName().length()).toLowerCase();
//		String type = "";
//		if (end.equals("mp3") || end.equals("aac") || end.equals("aac")
//				|| end.equals("amr") || end.equals("mpeg") || end.equals("mp4")) {
//			type = "audio";
//		} else if (end.equals("jpg") || end.equals("gif") || end.equals("png")
//				|| end.equals("jpeg")) {
//			type = "image";
//		} else {
//			type = "*";
//		}
//		type += "/*";
		String end = f.getName()
				.substring(f.getName().lastIndexOf(".") + 1,
						f.getName().length()).toLowerCase();
		String type="*/*";   
		   for(int i=0;i<MIME_MapTable.length;i++){   
		        if(end.equals(MIME_MapTable[i][0])) {
		        	type = MIME_MapTable[i][1]; 
		        	break;
		        } 
		              
		    }          
		return type;
	}
	
	/*使用过滤形式 删除文件*/
	public static void deleteFile(String path,String nameFilter){
		if(!FileUtils.hasSDCard()){
			return ;
		}
		String storedir = FileUtils.getSDPath() + path;
		File newDoc = new File(storedir);
		if (!newDoc.exists()) {
			/*文件夹路径不存在*/
			newDoc.mkdirs();
		}
		File[] files = newDoc.listFiles();
		List<File> list = new ArrayList<File>();
		if(files.length > 0){
			for(int i= 0 ; i < files.length ; i++){
				if(files[i].getName().contains(nameFilter)){
					list.add(files[i]);
				}
			}
			for(File file:list){
				file.delete();
			}
		}else{
			return ;
		}
	}
	
	
	public static String getFileName(String fileName){
    	if(fileName.indexOf(".")==-1){
    	}else{
    		int index = fileName.lastIndexOf(".");
    		fileName = fileName.substring(0, index);;
    	}
		return fileName;
	}
}
