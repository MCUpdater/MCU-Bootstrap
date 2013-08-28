package org.mcupdater;

import java.util.List;

public class Distribution {
	private String name;
	private String friendlyName;
	private String javaVersion;
	private String mainClass;
	private String params;
	private List<Library> libraries;
	
	public Distribution(String name, String friendlyName, String javaVersion, String mainClass, String params, List<Library> libraries){
		this.setName(name);
		this.setFriendlyName(friendlyName);
		this.setJavaVersion(javaVersion);
		this.setMainClass(mainClass);
		this.setParams(params);
		this.setLibraries(libraries);
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
}
