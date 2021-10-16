package org.mcupdater;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Extract {
    private String name;
    private String filename;
    private String path;
    private long size;
    private List<Hash> hashes;
    private List<PlatformType> platforms;
    private String includePath;
    private List<String> moduleNames;
    private List<PrioritizedURL> downloadURLs;

    public Extract(){}

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public List<Hash> getHashes() {
        return hashes;
    }

    public void setHashes(List<Hash> hashes) {
        this.hashes = hashes;
    }

    public List<PlatformType> getPlatforms() {
        return platforms;
    }

    public void setPlatforms(List<PlatformType> platforms) {
        this.platforms = platforms;
    }

    public String getIncludePath() {
        return includePath;
    }

    public void setIncludePath(String includePath) {
        this.includePath = includePath;
    }

    public List<String> getModuleNames() {
        return moduleNames;
    }

    public void setModuleNames(List<String> moduleNames) {
        this.moduleNames = moduleNames;
    }

    public List<PrioritizedURL> getDownloadURLs() {
        return downloadURLs;
    }

    public void setDownloadURLs(List<PrioritizedURL> downloadURLs) {
        this.downloadURLs = downloadURLs;
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

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public boolean hasModules() {
        return this.moduleNames != null && this.moduleNames.size() > 0;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }
}
