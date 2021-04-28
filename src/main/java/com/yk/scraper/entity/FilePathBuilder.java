package com.yk.scraper.entity;


public class FilePathBuilder {

    private long id;
    private String fileUrl;
    private String filePath;

    public FilePathBuilder setId(long id) {
        this.id = id;
        return this;
    }

    public FilePathBuilder setFileUrl(String fileUrl) {
        this.fileUrl = fileUrl;
        return this;
    }

    public FilePathBuilder setFilePath(String filePath) {
        this.filePath = filePath;
        return this;
    }

    public FilePathEntity build() {
        FilePathEntity FilePathEntity = new FilePathEntity(id,fileUrl,filePath);
        return FilePathEntity;
    }
}
