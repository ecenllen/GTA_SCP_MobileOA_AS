package com.gta.http.upload;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;

import org.apache.http.entity.mime.HttpMultipartMode;

import com.gta.http.entity.MultipartEntity;

public class CustomMultipartEntity extends MultipartEntity {
	
	private  UploadProgressListener listener;

	
	
	public CustomMultipartEntity() {
		super();
		// TODO Auto-generated constructor stub
	}

	public CustomMultipartEntity(HttpMultipartMode mode, String boundary,
			Charset charset) {
		super(mode, boundary, charset);
		// TODO Auto-generated constructor stub
	}

	public CustomMultipartEntity(HttpMultipartMode mode) {
		super(mode);
		// TODO Auto-generated constructor stub
	}

	public CustomMultipartEntity( UploadProgressListener listener) {
		super();
		this.listener = listener;
	}

	public CustomMultipartEntity(final HttpMultipartMode mode,
			UploadProgressListener listener) {
		super(mode);
		this.listener = listener;
	}

	public CustomMultipartEntity(HttpMultipartMode mode,  String boundary,
			final Charset charset,  UploadProgressListener listener) {
		super(mode, boundary, charset);
		this.listener = listener;
	}

	@Override
	public void writeTo(OutputStream outstream) throws IOException {
		super.writeTo(new CountingOutputStream(outstream, this.listener));
	}

	public class CountingOutputStream extends FilterOutputStream {

		private  UploadProgressListener listener;
		private long transferred;

		public CountingOutputStream(final OutputStream out,
				 UploadProgressListener listener) {
			super(out);
			this.listener = listener;
			this.transferred = 0;
		}

		public void write(byte[] b, int off, int len) throws IOException {
			out.write(b, off, len);
			this.transferred += len;
			this.listener.transferred(this.transferred,
					CustomMultipartEntity.this.getContentLength());
		}

	}

	public UploadProgressListener getListener() {
		return listener;
	}

	public void setListener(UploadProgressListener listener) {
		this.listener = listener;
	}

	
	
}
