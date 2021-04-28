package com.yk.scraper.controller;


import com.yk.scraper.service.ScrapeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
public class HKController {

    // 비즈니스 로직 수행
    private final ScrapeService scrapeService;

    private static final Logger log = LoggerFactory.getLogger(HKController.class);


    public HKController(@Autowired ScrapeService scrapeService) {
        this.scrapeService = scrapeService;
    }

    // 모든 데이터 요청
    @RequestMapping("/hkscrape/all")
    public void allScrape(@RequestParam(value = "sDate", required = false, defaultValue = "2002-01-01") String sDate,
                          @RequestParam(value = "eDate", required = false) String eDate) throws IOException {
        scrapeService.allScrape(sDate,eDate);
    }

    // 증분 수집에 대한 요청
    @RequestMapping("/hkscrape/increment")
    public void incrementScrape(@RequestParam(value = "sDate", required = false, defaultValue = "2002-01-01") String sDate,
                                @RequestParam(value = "eDate", required = false) String eDate) throws  IOException{
        log.info("controller increment 호출");
        scrapeService.incrementScrape(sDate,eDate);

    }



}
