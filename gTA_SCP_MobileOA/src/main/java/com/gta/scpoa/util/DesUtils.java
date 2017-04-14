package com.gta.scpoa.util;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class DesUtils {

	/*
	 * 测试
	 */
	public static void main(String[] args) {
		String key = "12345678";
		String text = "12345678";
		try {
			String result1 = encryptDES(text, key);//key放在后面
			String result2 = decryptDES(result1, key);
			System.out.println(result1);
			System.out.println(result2);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private static byte[] iv = { 1, 2, 3, 4, 5, 6, 7, 8 };

	public static String encode(String key, String data) {
		if (data == null)
			return null;
		try {
			return encryptDES(data,key);		
		} catch (Exception e) {
			e.printStackTrace();
    		return data;
		}

	
	}

	public static String decode(String key, String data) {
		if (data == null)
			return null;
		try {
			return decryptDES(data,key);
		} catch (Exception e) {
			e.printStackTrace();
    		return data;
		}	
	}

	private static String encryptDES(String encryptString, String encryptKey)
			throws Exception {
		// IvParameterSpec zeroIv = new IvParameterSpec(new byte[8]);
		IvParameterSpec zeroIv = new IvParameterSpec(iv);
		SecretKeySpec key = new SecretKeySpec(encryptKey.getBytes(), "DES");
		Cipher cipher = Cipher.getInstance("DES/CBC/PKCS5Padding");
		cipher.init(Cipher.ENCRYPT_MODE, key, zeroIv);
		byte[] encryptedData = cipher.doFinal(encryptString.getBytes());

		return Base64.encode(encryptedData);
	}

	private static String decryptDES(String decryptString, String decryptKey)
			throws Exception {
		byte[] byteMi = Base64.decode(decryptString);
		IvParameterSpec zeroIv = new IvParameterSpec(iv);
		// IvParameterSpec zeroIv = new IvParameterSpec(new byte[8]);
		SecretKeySpec key = new SecretKeySpec(decryptKey.getBytes(), "DES");
		Cipher cipher = Cipher.getInstance("DES/CBC/PKCS5Padding");
		cipher.init(Cipher.DECRYPT_MODE, key, zeroIv);
		byte decryptedData[] = cipher.doFinal(byteMi);

		return new String(decryptedData);
	}
}
