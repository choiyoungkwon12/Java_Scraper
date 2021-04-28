package com.yk.scraper.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class FilePathEntity {

    private long Id;
    private String fileUrl;
    private String filePath;

}
