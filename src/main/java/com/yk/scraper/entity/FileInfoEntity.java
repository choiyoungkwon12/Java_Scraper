package com.yk.scraper.entity;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FileInfoEntity {

    private Long id;
    private String writeDate;
    private String part;
    private String title;
    private String writer;
    private String provider;
    private String fileUrl;

}
