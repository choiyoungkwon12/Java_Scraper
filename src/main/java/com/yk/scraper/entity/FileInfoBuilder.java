package com.yk.scraper.entity;


public class FileInfoBuilder {
    private Long id;
    private String writeDate;
    private String part;
    private String title;
    private String writer;
    private String provide;
    private String fileUrl;

    public FileInfoBuilder setId(Long id) {
        this.id = id;
        return this;
    }

    public FileInfoBuilder setWriteDate(String writeDate) {
        this.writeDate = writeDate;
        return this;
    }

    public FileInfoBuilder setPart(String part) {
        this.part = part;
        return this;
    }

    public FileInfoBuilder setTitle(String title) {
        this.title = title;
        return this;
    }

    public FileInfoBuilder setWriter(String writer) {
        this.writer = writer;
        return this;
    }

    public FileInfoBuilder setProvide(String provide) {
        this.provide = provide;
        return this;
    }

    public FileInfoBuilder setFileUrl(String fileUrl) {
        this.fileUrl = fileUrl;
        return this;
    }

    public FileInfoEntity build() {
        FileInfoEntity fIleInfoEntity = new FileInfoEntity(id, writeDate, part, title, writer, provide, fileUrl);
        return fIleInfoEntity;
    }
}
