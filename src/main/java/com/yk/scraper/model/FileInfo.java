package com.yk.scraper.model;

import com.yk.scraper.entity.FileInfoBuilder;
import com.yk.scraper.entity.FileInfoEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FileInfo {

    private String title;
    private String writer;
    private String writeDate;
    private String part;
    private String provider;
    private String fileUrl;


    public FileInfoEntity toFileInfoEntity() {
        FileInfoBuilder fileInfoBuilder = new FileInfoBuilder();

        FileInfoEntity fIleInfoEntity = fileInfoBuilder.setPart(part)
                .setTitle(title)
                .setWriteDate(writeDate)
                .setWriter(writer)
                .setProvide(provider)
                .setFileUrl(fileUrl)
                .build();

        return fIleInfoEntity;
    }
}

