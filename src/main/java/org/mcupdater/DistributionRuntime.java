package org.mcupdater;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DistributionRuntime {
    private String name;
    private String filename;
    private String version;
    private String executable;
    private long size;
    private List<Hash> hashes;
    private List<PlatformType> platforms;
    private List<PrioritizedURL> downloadURLs;
    private boolean primary;

    public DistributionRuntime(){}

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getExecutable() {
        return executable;
    }

    public void setExecutable(String executable) {
        this.executable = executable;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
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

    public List<PrioritizedURL> getDownloadURLs() {
        return downloadURLs;
    }

    public void setDownloadURLs(List<PrioritizedURL> downloadURLs) {
        this.downloadURLs = downloadURLs;
    }

    public boolean isPrimary() {
        return primary;
    }

    public void setPrimary(boolean primary) {
        this.primary = primary;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }
}
