package com.yk.scraper.model;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Getter
public class HankyungUrl {

    @Value("${hankyung.url}")
    private String url;


}
