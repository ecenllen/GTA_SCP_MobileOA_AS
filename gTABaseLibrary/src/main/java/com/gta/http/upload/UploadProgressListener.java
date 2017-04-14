package com.gta.http.upload;

public interface UploadProgressListener {
	void transferred(long num, long ContentLength);
}
