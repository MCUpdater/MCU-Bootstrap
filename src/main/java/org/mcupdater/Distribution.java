package org.mcupdater;

import java.util.List;

public class Distribution {
	private String name;
	private String friendlyName;
	private String javaVersion;
	private String mainClass;
	private String message;
	private String params;
	private List<Library> libraries;
	private List<DistributionRuntime> runtimes;
	private List<Extract> extracts;

	public Distribution() {}

	public Distribution(String name, String friendlyName, String javaVersion, String mainClass, String params, List<Library> libraries, List<DistributionRuntime> runtimes, List<Extract> extracts, String message){
		this.setMessage(message);
		this.setName(name);
		this.setFriendlyName(friendlyName);
		this.setJavaVersion(javaVersion);
		this.setMainClass(mainClass);
		this.setParams(params);
		this.setLibraries(libraries);
		this.setRuntimes(runtimes);
		this.setExtracts(extracts);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getFriendlyName() {
		return friendlyName;
	}

	public void setFriendlyName(String releaseType) {
		this.friendlyName = releaseType;
	}

	public String getJavaVersion() {
		return javaVersion;
	}

	public void setJavaVersion(String javaVersion) {
		this.javaVersion = javaVersion;
	}

	public String getMainClass() {
		return mainClass;
	}

	public void setMainClass(String clazz) {
		this.mainClass = clazz;
	}

	public String getParams() {
		return params;
	}

	public void setParams(String params) {
		this.params = params;
	}

	public List<Library> getLibraries() {
		return libraries;
	}

	public void setLibraries(List<Library> libraries) {
		this.libraries = libraries;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public List<DistributionRuntime> getRuntimes() {
		return runtimes;
	}

	public void setRuntimes(List<DistributionRuntime> runtimes) {
		this.runtimes = runtimes;
	}

	public List<Extract> getExtracts() {
		return extracts;
	}

	public void setExtracts(List<Extract> extracts) {
		this.extracts = extracts;
	}

	public List<Library> getRelevantLibraries(PlatformType thisPlatform) {
		return this.libraries.stream().filter(library -> library.getPlatforms().contains(thisPlatform)).toList();
	}

	public List<DistributionRuntime> getRelevantRuntimes(PlatformType thisPlatform) {
		return this.runtimes.stream().filter(runtime -> runtime.getPlatforms().contains(thisPlatform)).toList();
	}

	public List<Extract> getRelevantExtracts(PlatformType thisPlatform) {
		return this.extracts.stream().filter(extract -> extract.getPlatforms().contains(thisPlatform)).toList();
	}

	public DistributionRuntime getPrimaryRuntime(PlatformType thisPlatform) {
		return this.runtimes.stream().filter(runtime -> (runtime.getPlatforms().contains(thisPlatform) && runtime.isPrimary())).findFirst().orElse(null);
	}
}
