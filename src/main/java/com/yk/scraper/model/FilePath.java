package com.yk.scraper.model;

import com.yk.scraper.entity.FilePathBuilder;
import com.yk.scraper.entity.FilePathEntity;

public class FilePath {

    private String filePath;
    private String fileUrl;

    public FilePath(String filePath, String fileUrl) {
        this.filePath = filePath;
        this.fileUrl = fileUrl;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getFileUrl() {
        return fileUrl;
    }

    public void setFileUrl(String fileUrl) {
        this.fileUrl = fileUrl;
    }

    public FilePathEntity toFilePathEntity(){
        FilePathBuilder filePathBuilder = new FilePathBuilder();

        FilePathEntity filePathEntity = filePathBuilder.setFileUrl(fileUrl)
                .setFilePath(filePath)
                .build();
        return filePathEntity;
    }

}
