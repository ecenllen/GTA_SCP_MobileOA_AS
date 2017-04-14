package com.gta.downloader;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.http.HttpResponse;
import org.apache.http.conn.ConnectTimeoutException;

public class FileDownloadHandler implements IDownloadHandler {
	public final static int TIME_OUT = 0 * 1000;
	private final static int BUFFER_SIZE = 1024 * 8;
	private RandomAccessFile outputStream;
	private long downloadSize;
	private long previousFileSize;
	private long totalSize;
	private long networkSpeed;
	private long previousTime;
	private long totalTime;
	private boolean interrupt = false;
	private Timer timer;
	private static final int TIMERSLEEPTIME = 100;
	private DownloadInfo downloadInfo;

	@Override
	public DownloadInfo parseResponse(DownloadInfo downloadInfo,
			HttpResponse response) throws IllegalStateException, IOException {
		if (null == response)
			return downloadInfo;
		this.downloadInfo = downloadInfo;
		File file = downloadInfo.file;
		File tempFile = downloadInfo.tempFile;
		long contentLenght = response.getEntity().getContentLength();
		totalSize = contentLenght;
		// -1的解决方式ContentLength 在手机访问的时候出现了问题，返回为-1
		if (contentLenght == -1) {
			contentLenght = response.getEntity().getContent().available();
		}
		// 文件存在删除
		if (file.exists() && contentLenght == file.length()) {
			totalSize = file.length();
			if (file.isFile()) {
				file.delete();
			}
		}
		if (tempFile.exists()) {
			previousFileSize = tempFile.length();
			totalSize = previousFileSize + contentLenght;
		}
		downloadInfo.fileLength=totalSize;
		outputStream = new ProgressReportingRandomAccessFile(tempFile, "rw");
		InputStream input = response.getEntity().getContent();
		// startTimer();// 更新进度
		int bytesCopied = copy(input, outputStream);
		if ((previousFileSize + bytesCopied) == totalSize) {
			tempFile.renameTo(file);// 重命名文件
		} else if (!interrupt) {
			throw new IOException("Download incomplete: " + bytesCopied
					+ " != " + totalSize);
		}
		// try {
		// Thread.sleep(100);// 等待Timer执行完
		// } catch (InterruptedException e) {
		// e.printStackTrace();
		// }
		// // 停止更新进度
		// stopTimer();
		return downloadInfo;
	}

	public int copy(InputStream input, RandomAccessFile out) {
		if (input == null || out == null) {
			return -1;
		}
		byte[] buffer = new byte[BUFFER_SIZE];
		BufferedInputStream in = new BufferedInputStream(input, BUFFER_SIZE);
		int count = 0, length = 0;
		long errorBlockTimePreviousTime = -1, expireTime = 0;
		try {
			out.seek(out.length());
			previousTime = System.currentTimeMillis();
			while (!interrupt) {
				// Log.i("FileDownloadHandler", "copy " + "interrupt=" +
				// interrupt);
				length = in.read(buffer, 0, BUFFER_SIZE);
				if (length == -1) {
					break;
				}
				out.write(buffer, 0, length);
				count += length;
				if (networkSpeed == 0) {
					if (errorBlockTimePreviousTime > 0) {
						expireTime = System.currentTimeMillis()
								- errorBlockTimePreviousTime;
						if (expireTime > TIME_OUT) {
							throw new ConnectTimeoutException(
									"connection time out.");
						}
					} else {
						errorBlockTimePreviousTime = System.currentTimeMillis();
					}
				} else {
					expireTime = 0;
					errorBlockTimePreviousTime = -1;
				}
			}
		} catch (Exception e) {
			// e.printStackTrace();
		} finally {
			try {
				input.close();
				out.close();
			} catch (Exception e) {
				// e.printStackTrace();
			}
		}
		return count;

	}

	@SuppressWarnings("unused")
	private void startTimer() {
		if (null == timer) {
			timer = new Timer();
		}
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				while (!interrupt) {
					downloadInfo.currentLenght=downloadSize;
					downloadInfo.networkSpeed=networkSpeed;
					downloadInfo.downLoader.onLoading(downloadInfo);
					try {
						Thread.sleep(TIMERSLEEPTIME);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}

			}
		}, 100, 1000);
	}

	@SuppressWarnings("unused")
	private void stopTimer() {
		interrupt = true;
	}

	private class ProgressReportingRandomAccessFile extends RandomAccessFile {
		private int progress = 0;

		public ProgressReportingRandomAccessFile(File file, String mode)
				throws FileNotFoundException {
			super(file, mode);
		}

		@Override
		public void write(byte[] buffer, int offset, int count)
				throws IOException {
			super.write(buffer, offset, count);
			progress += count;
			totalTime = System.currentTimeMillis() - previousTime;
			downloadSize = progress + previousFileSize;
			if (totalTime > 0) {
				networkSpeed = (long) ((progress / totalTime) / 1.024);
			}
			downloadInfo.currentLenght=downloadSize;
			downloadInfo.networkSpeed=networkSpeed;
			downloadInfo.downLoader.onLoading(downloadInfo);
		}
	}

	public boolean isInterrupt() {
		return interrupt;
	}

	public void setInterrupt(boolean interrupt) {
		this.interrupt = interrupt;
	}

	@Override
	public void setCancel(boolean cancel) {
		setInterrupt(cancel);

	}

}
