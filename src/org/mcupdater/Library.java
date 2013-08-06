package org.mcupdater;

import java.net.URL;
import java.util.List;

public class Library {
	private String name;
	private String filename;
	private long size;
	private String md5;
	private PlatformType platform;
	private List<URL> downloadURLs;
	
	public Library (String name, String filename, long size, String md5, String platform, List<URL> downloadURLs) {
		this.setName(name);
		this.setFilename(filename);
		this.setSize(size);
		this.setMd5(md5);
		this.setPlatform(PlatformType.valueOf(platform));
		this.setDownloadURLs(downloadURLs);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	public String getMd5() {
		return md5;
	}

	public void setMd5(String md5) {
		this.md5 = md5;
	}

	public PlatformType getPlatform() {
		return platform;
	}

	public void setPlatform(PlatformType platform) {
		this.platform = platform;
	}

	public List<URL> getDownloadURLs() {
		return downloadURLs;
	}

	public void setDownloadURLs(List<URL> downloadURLs) {
		this.downloadURLs = downloadURLs;
	}

	public long getSize() {
		return size;
	}

	public void setSize(long size) {
		this.size = size;
	}
}
