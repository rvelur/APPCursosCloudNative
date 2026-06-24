package com.duoc.cursos.dto;

public class S3ObjectInfo {
    private String key;
    private long size;
    private String lastModified;

    public S3ObjectInfo(String key, long size, String lastModified) {
        this.key = key;
        this.size = size;
        this.lastModified = lastModified;
    }

    // Getters y Setters
    public String getKey() { return key; }
    public void setKey(String key) { this.key = key; }
    public long getSize() { return size; }
    public void setSize(long size) { this.size = size; }
    public String getLastModified() { return lastModified; }
    public void setLastModified(String lastModified) { this.lastModified = lastModified; }
}