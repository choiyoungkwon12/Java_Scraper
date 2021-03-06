package com.yk.scraper.service;

import com.yk.scraper.entity.FileInfoEntity;
import com.yk.scraper.mapper.ScrapeMapper;
import com.yk.scraper.model.FileInfo;
import com.yk.scraper.model.HankyungUrl;
import org.jsoup.Connection;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.UnsupportedMimeTypeException;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Tag;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class ScrapeService {
    private static final Logger log = LoggerFactory.getLogger(ScrapeService.class);

    @Autowired
    private ScrapeMapper scrapeMapper;

    @Autowired
    private HankyungUrl url;

    public Map<String, List> setTerm(String sDate, String eDate) {

        // yyyy-MM-dd

        // ex) sDate = 2018-02-13
        // ex) eDate = 2020-11-10

        // 2018-02-13 ~ 2018-02-31
        // 2020-11-01 ~ 2020-11-10

        String sDay;
        String eDay;

        String month[] = {"01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12"};

        String minYear = sDate.substring(0, 4);
        String maxYear = eDate.substring(0, 4);

        String sSearchDay = sDate.substring(8, 10);
        String eSearchDay = eDate.substring(8, 10);


        if (sSearchDay.equals("01")) {
            sDay = "01";
        } else {
            sDay = sSearchDay;
        }

        if (eSearchDay.equals("31")) {
            eDay = "31";
        } else {
            eDay = eSearchDay;
        }

        int startMonth = Integer.parseInt(sDate.substring(5, 7));
        int endMonth = Integer.parseInt(eDate.substring(5, 7));

        int sYear = Integer.parseInt(minYear);
        int eYear = Integer.parseInt(maxYear);

        int year[] = new int[(eYear - sYear) + 1];

        int count = 0;
        for (int i = sYear; i <= eYear; i++) {
            year[count] = i;
            count++;
        }

        log.info(year.toString());

        List<String> sDateList = new ArrayList<>();
        List<String> eDateList = new ArrayList<>();

        for (int i = 0; i < year.length; i++) {
            if (sYear == eYear) { // ????????? ????????? ????????? ??? ????????? ?????? ??????

                if (startMonth == endMonth) {
                    sDateList.add(year[i] + "-" + month[startMonth - 1] + "-" + sDay); // ????????? ????????? ??????
                    eDateList.add(year[i] + "-" + month[startMonth - 1] + "-" + eDay); // ????????? ?????? 31??????
                } else { // ????????? ?????? ?????? ????????????

                    for (int j = startMonth - 1; j < endMonth; j++) {

                        if (j == startMonth - 1) {
                            sDateList.add(year[i] + "-" + month[j] + "-" + sDay); // ????????? ????????? ??????
                            eDateList.add(year[i] + "-" + month[j] + "-" + "31"); // ????????? ?????? ?????? ?????????
                        } else if (j == endMonth - 1) {
                            sDateList.add(year[i] + "-" + month[j] + "-" + "01"); // ????????? ????????? ??????
                            eDateList.add(year[i] + "-" + month[j] + "-" + eDay); // ????????? ?????? ?????? ?????????
                        } else {
                            sDateList.add(year[i] + "-" + month[j] + "-" + "01"); // ????????? ????????? ??????
                            eDateList.add(year[i] + "-" + month[j] + "-" + "31"); // ????????? ?????? 31??????
                        }

                    }

                }

            } else {

                if (year[i] == sYear) {
                    for (int j = startMonth - 1; j < month.length; j++) {
                        if (j == startMonth - 1) {
                            sDateList.add(year[i] + "-" + month[j] + "-" + sDay); // ????????? ????????? ??????
                        } else {
                            sDateList.add(year[i] + "-" + month[j] + "-" + "01"); // ????????? ????????? ??????
                        }

                        eDateList.add(year[i] + "-" + month[j] + "-" + "31"); // ????????? ?????? 31??????
                    }
                } else if (year[i] == eYear) {
                    for (int j = 0; j < endMonth; j++) { // ????????? ?????????
                        if (j == endMonth - 1) {      // ????????? ??? ??? ?????? ????????? ??? ??????
                            eDateList.add(year[i] + "-" + month[j] + "-" + eDay);
                        } else {                        // ????????? ??? ?????? ?????? 31?????????
                            eDateList.add(year[i] + "-" + month[j] + "-" + "31");
                        }
                        sDateList.add(year[i] + "-" + month[j] + "-" + "01"); // ????????? 01??? ??????
                    }
                } else {
                    for (int j = 0; j < month.length; j++) {
                        sDateList.add(year[i] + "-" + month[j] + "-" + "01");
                        eDateList.add(year[i] + "-" + month[j] + "-" + "31");
                    }
                }

            }


        }

        Map<String, List> term = new HashMap<>();

        term.put("sDateList", sDateList);
        term.put("eDateList", eDateList);

        return term;
    }

    // ?????? ??????(2002-01-01 ~ ????????????)
    public void allScrape(String sDate, String eDate) {
        log.info("allScrape ??????");
        int socketTimeoutCount = 0;

        if (eDate == null) {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
            eDate = simpleDateFormat.format(new Date());
        }

        Map<String, List> term = setTerm(sDate, eDate);

        List<String> sDateList = term.get("sDateList");
        List<String> eDateList = term.get("eDateList");

        log.info("sDateList >> " + sDateList);
        log.info("eDateList >> " + eDateList);

        for (int i = sDateList.size() - 1; i >= 0; i--) {
            sDate = sDateList.get(i);
            eDate = eDateList.get(i);
            log.info("sDate >> " + sDate);
            log.info("eDate >> " + eDate);
            int nowPage = 0;

            List<FileInfo> infoList = null;
            while (true) {
                nowPage++;
                log.info("nowPage >> " + nowPage);
                Document html = null;

                try {

                    html = fetchHtml(url.getUrl(), sDate, eDate, nowPage);

                } catch (MalformedURLException e) {
                    e.printStackTrace();
                    break;
                } catch (HttpStatusException e) {
                    e.printStackTrace();
                    break;
                } catch (UnsupportedMimeTypeException e) {
                    e.printStackTrace();
                    break;
                } catch (SocketTimeoutException e) {
                    log.info(e.getMessage());
                    nowPage--;
                    socketTimeoutCount++;
                    if (socketTimeoutCount > 5) {
                        e.printStackTrace();
                        break;
                    }
                    continue;
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    break;
                } catch (IOException e) {
                    e.printStackTrace();
                    break;
                }

                infoList = fileInfoParse(html);

                if (infoList.isEmpty()) {
                    boolean exit = finalPageCheck(html);
                    if (exit) {
                        log.info("????????? ????????? ???????????? ?????? ??????");
                        break;
                    }
                }

                for (FileInfo fileInfo : infoList) {
                    fileInfo.setFileUrl("http://consensus.hankyung.com/" + fileInfo.getFileUrl());
                    FileInfoEntity fileInfoEntity = fileInfo.toFileInfoEntity();

                    boolean duplicateCheck = duplicateCheck(fileInfoEntity);

                    if (duplicateCheck) {
                        scrapeMapper.insertInfo(fileInfoEntity);
                        log.info("FileUrl : " + fileInfo.getFileUrl() + " ??????");
                    } else {
                        log.info("FileUrl : " + fileInfoEntity.getFileUrl() + " ?????? ??????");
                    }
                }
            } // end of while
        } // end of for
    }

    // ?????? ???????????? ?????? ?????? ??? ?????? ???????????? ????????? ???????????? ?????? ??????
    public void incrementScrape(String sDate, String eDate) {

        if (eDate == null) {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
            eDate = simpleDateFormat.format(new Date());
        }

        int nowPage = 0;
        boolean finishFlag = false;
        List<FileInfo> infoList = null;

        Map<String, List> term = setTerm(sDate, eDate);

        List<String> sDateList = term.get("sDateList");
        List<String> eDateList = term.get("eDateList");

        log.info("sDateList >> " + sDateList);
        log.info("eDateList >> " + eDateList);

        int socketTimeoutCount = 0;

        for (int i = sDateList.size() - 1; i >= 0; i--) {
            sDate = sDateList.get(i);
            eDate = eDateList.get(i);
            log.info("sDate >> " + sDate);
            log.info("eDate >> " + eDate);
            while (true) {
                nowPage++;

                Document html = null;

                try {
                    html = fetchHtml(url.getUrl(), sDate, eDate, nowPage);
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                    break;
                } catch (HttpStatusException e) {
                    e.printStackTrace();
                    break;
                } catch (UnsupportedMimeTypeException e) {
                    e.printStackTrace();
                    break;
                } catch (SocketTimeoutException e) {
                    log.info(e.getMessage());
                    nowPage--;
                    socketTimeoutCount++;
                    if (socketTimeoutCount > 5) {
                        e.printStackTrace();
                        break;
                    }
                    continue;
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    break;
                } catch (IOException e) {
                    e.printStackTrace();
                    break;
                }

                infoList = fileInfoParse(html);

                if (infoList.isEmpty()) {
                    boolean exit = finalPageCheck(html);
                    if (exit) {
                        log.info("????????? ????????? ???????????? ?????? ??????");
                        break;
                    }
                } else {
                    socketTimeoutCount = 0;
                }

                for (FileInfo fileInfo : infoList) {
                    fileInfo.setFileUrl("http://consensus.hankyung.com/" + fileInfo.getFileUrl());
                    FileInfoEntity fileInfoEntity = fileInfo.toFileInfoEntity();

                    boolean duplicateCheck = duplicateCheck(fileInfoEntity);

                    if (duplicateCheck) {
                        scrapeMapper.insertInfo(fileInfoEntity);
                        log.info(fileInfoEntity.getFileUrl() + " ?????? ??????");
                    } else {
                        log.info("FileUrl : " + fileInfoEntity.getFileUrl() + " ?????? ??????");
                        finishFlag = true;
                        break;
                    }

                }

                if (finishFlag) {
                    log.info("?????? ????????? ??????");
                    break;
                }

            } // end of while
            if (finishFlag) {
                break;
            }
        }
    }

    private Connection.Response UrlRequest(String url) throws IOException, InterruptedException {
        Connection conn = Jsoup.connect(url)
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/87.0.4280.66 Safari/537.36")
                .ignoreHttpErrors(true);

        Thread.sleep(1000);
        Connection.Response resp = conn.execute();
        resp.charset("euc-kr");

        return resp;
    }

    private Document responseParse(Connection.Response resp) throws IOException {
        Document html = resp.parse();
        return html;
    }

    // url??? ?????? connection ??? html ?????????
    public Document fetchHtml(String url, String sDate, String eDate, int nowPage) throws IOException, InterruptedException {

        log.info("url??? ???????????? html ?????????");

        url = url + "&sdate=" + sDate + "&edate=" + eDate + "&order_type=10000000" + "&now_page=" + nowPage;

        Connection.Response resp = UrlRequest(url);
        Document html = responseParse(resp);

        return html;
    }

    public boolean duplicateCheck(FileInfoEntity fileInfoEntity) {

        boolean duplicateCheck = scrapeMapper.duplicateCheck(fileInfoEntity);
        return duplicateCheck;

    }

    public boolean finalPageCheck(Document html) {

        // tbody tr ??? ??????
        // tr ??? ????????? ????????? ?????????
        // Elements tr = html.getElementsByTag("tbody tr");
        // ?????? tr??? null??? ?????????.

        log.info("????????? ????????? ??????");

        Elements tbody = html.getElementsByTag("tbody");

        String tbodyText = tbody.text().trim();

        // tbody??? ???????????? ?????????(????????? ?????????)
        if (tbodyText.equals("") || tbodyText.equals("????????? ????????????.")) {
            return true;
        } else {
            return false;
        }
    }


    public String replaceTitle(String title) {

        title = title.replaceAll("\n", "");

        //<\/.+?> ????????????
        //<.+?> ????????????

        Pattern pattern = Pattern.compile("<\\/.+?>");

        Matcher matcher = pattern.matcher(title);
        //log.info("title : {}" , title);

        // ?????? ?????? ??????
        while (matcher.find()) {

            title = title.replace(matcher.group(), "");

            matcher = pattern.matcher(title);

        }


        pattern = Pattern.compile("<.+?>");

        matcher = pattern.matcher(title);

        int start = 0;
        int count = 1;

        // ?????? ?????? ??? Tag ???????????? ?????? ?????? ?????? ????????? ??????
        // ?????? ?????? ?????? ?????? ????????? ?????? ????????? ???????????? ????????? ???????????? ????????? ????????? ??????.
        while (matcher.find(start)) {

            String tagName = title.substring(matcher.start() + 1, matcher.end() - 1);

            if (tagName.equals(" strong")) {

                title = title.substring(0, matcher.start());

                break;

            }
            tagName = tagName.trim();

            if (Tag.isKnownTag(tagName)) {

                title = title.replace(matcher.group(), "");

            } else {

                for (int i = 0; i < count; i++) {
                    title = title.replace(" " + matcher.group(), matcher.group());
                }
                start = matcher.start();
                count++;

            }

            if (matcher.find()) {
                title = title.replace(matcher.group() + "   ", matcher.group());
            }

            matcher = pattern.matcher(title);

        }

        log.info("file title : {}", title);

        return title.trim();

    }

    // ?????? html?????? ????????? ?????? ????????? ?????????
    public List<FileInfo> fileInfoParse(Document html) {

        FileInfo fileInfo;

        List<FileInfo> infoList = new ArrayList<>();

        Elements fileBlocks = html.select("tbody tr");

        for (Element fileBlock : fileBlocks) {

            String titleElements = fileBlock.select("td .layerPop strong").toString();

            // 1. ?????? ?????? ?????? ?????? ?????? ??????
            String title = replaceTitle(titleElements);

            fileInfo = new FileInfo();

            fileInfo.setWriteDate(fileBlock.select("td.first.txt_number").text());
            fileInfo.setPart(fileBlock.select("td:nth-child(2)").text());

            fileInfo.setTitle(title);
            fileInfo.setWriter(fileBlock.select("td:nth-child(4)").text());
            fileInfo.setProvider(fileBlock.select("td:nth-child(5)").text());
            String fileUrl = fileBlock.select(".dv_input a").attr("href");
            fileInfo.setFileUrl(fileUrl);

            if (!fileUrl.equals("")) {
                infoList.add(fileInfo);
            }

        }

        return infoList;
    }

}
