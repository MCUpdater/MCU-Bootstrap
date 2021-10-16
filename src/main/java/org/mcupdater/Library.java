package org.mcupdater;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Library {
	private String name;
	private String filename;
	private long size;
	private List<Hash> hashes;
	private List<PlatformType> platforms;
	private List<String> moduleNames;
	private List<PrioritizedURL> downloadURLs;

	public Library() {}

	public Library (String name, String filename, long size, List<Hash> hashes, List<PlatformType> platforms, List<String> moduleNames, List<PrioritizedURL> downloadURLs) {
		this.setName(name);
		this.setFilename(filename);
		this.setSize(size);
		this.setHashes(hashes);
		this.setPlatforms(platforms);
		this.setModuleNames(moduleNames);
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
		List<Hash> filtered = hashes.stream().filter(hash -> hash.getType() == HashEnum.MD5).toList();
		return (filtered.size() > 0) ? filtered.get(0).getValue() : "";
	}

	/*
	public PlatformType getPlatform() {
		return platform;
	}

	public void setPlatform(PlatformType platform) {
		this.platform = platform;
	}
	 */

	public List<PrioritizedURL> getDownloadURLs() {
		return downloadURLs;
	}

	public void setDownloadURLs(List<PrioritizedURL> downloadURLs) {
		this.downloadURLs = downloadURLs;
	}

	public long getSize() {
		return size;
	}

	public void setSize(long size) {
		this.size = size;
	}

	public List<PlatformType> getPlatforms() {
		return platforms;
	}

	public void setPlatforms(List<PlatformType> platforms) {
		this.platforms = platforms;
	}

	public List<String> getModuleNames() {
		return moduleNames;
	}

	public void setModuleNames(List<String> moduleNames) {
		this.moduleNames = moduleNames;
	}

	public boolean hasModules() {
		return this.moduleNames != null && this.moduleNames.size() > 0;
	}

	public List<Hash> getHashes() {
		return hashes;
	}

	public void setHashes(List<Hash> hashes) {
		this.hashes = hashes;
	}

	public List<URL> getURLs() {
		List<URL> result = new ArrayList<>();
		Collections.sort(this.downloadURLs, new PriorityComparator());
		for (PrioritizedURL entry : this.downloadURLs) {
			try {
				result.add(new URL(entry.getUrl()));
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return result;
	}
}
