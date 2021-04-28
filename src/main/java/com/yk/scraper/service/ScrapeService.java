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
            if (sYear == eYear) { // 조회한 기간의 시작과 끝 년도가 같은 경우

                if (startMonth == endMonth) {
                    sDateList.add(year[i] + "-" + month[startMonth - 1] + "-" + sDay); // 시작은 조회일 부터
                    eDateList.add(year[i] + "-" + month[startMonth - 1] + "-" + eDay); // 끝나는 날은 31까지
                } else { // 년도는 같고 월이 다른경우

                    for (int j = startMonth - 1; j < endMonth; j++) {

                        if (j == startMonth - 1) {
                            sDateList.add(year[i] + "-" + month[j] + "-" + sDay); // 시작은 조회일 부터
                            eDateList.add(year[i] + "-" + month[j] + "-" + "31"); // 끝나는 날은 조회 일까지
                        } else if (j == endMonth - 1) {
                            sDateList.add(year[i] + "-" + month[j] + "-" + "01"); // 시작은 조회일 부터
                            eDateList.add(year[i] + "-" + month[j] + "-" + eDay); // 끝나는 날은 조회 일까지
                        } else {
                            sDateList.add(year[i] + "-" + month[j] + "-" + "01"); // 시작은 조회일 부터
                            eDateList.add(year[i] + "-" + month[j] + "-" + "31"); // 끝나는 날은 31까지
                        }

                    }

                }

            } else {

                if (year[i] == sYear) {
                    for (int j = startMonth - 1; j < month.length; j++) {
                        if (j == startMonth - 1) {
                            sDateList.add(year[i] + "-" + month[j] + "-" + sDay); // 시작은 조회일 부터
                        } else {
                            sDateList.add(year[i] + "-" + month[j] + "-" + "01"); // 시작은 조회일 부터
                        }

                        eDateList.add(year[i] + "-" + month[j] + "-" + "31"); // 끝나는 날은 31까지
                    }
                } else if (year[i] == eYear) {
                    for (int j = 0; j < endMonth; j++) { // 조회한 월까지
                        if (j == endMonth - 1) {      // 마지막 달 일 경우 요청한 날 까지
                            eDateList.add(year[i] + "-" + month[j] + "-" + eDay);
                        } else {                        // 마지막 달 아닐 경우 31일까지
                            eDateList.add(year[i] + "-" + month[j] + "-" + "31");
                        }
                        sDateList.add(year[i] + "-" + month[j] + "-" + "01"); // 시작은 01일 부터
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

    // 전체 수집(2002-01-01 ~ 현재날짜)
    public void allScrape(String sDate, String eDate) {
        log.info("allScrape 호출");
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
                        log.info("마지막 페이지 도달하여 수집 종료");
                        break;
                    }
                }

                for (FileInfo fileInfo : infoList) {
                    fileInfo.setFileUrl("http://consensus.hankyung.com/" + fileInfo.getFileUrl());
                    FileInfoEntity fileInfoEntity = fileInfo.toFileInfoEntity();

                    boolean duplicateCheck = duplicateCheck(fileInfoEntity);

                    if (duplicateCheck) {
                        scrapeMapper.insertInfo(fileInfoEntity);
                        log.info("FileUrl : " + fileInfo.getFileUrl() + " 수집");
                    } else {
                        log.info("FileUrl : " + fileInfoEntity.getFileUrl() + " 중복 있음");
                    }
                }
            } // end of while
        } // end of for
    }

    // 증분 수집으로 자료 수집 시 이미 존재하는 자료와 중복되면 수집 종료
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
                        log.info("마지막 페이지 도달하여 수집 종료");
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
                        log.info(fileInfoEntity.getFileUrl() + " 수집 완료");
                    } else {
                        log.info("FileUrl : " + fileInfoEntity.getFileUrl() + " 중복 있음");
                        finishFlag = true;
                        break;
                    }

                }

                if (finishFlag) {
                    log.info("중복 되어서 종료");
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

    // url에 대한 connection 후 html 가져옴
    public Document fetchHtml(String url, String sDate, String eDate, int nowPage) throws IOException, InterruptedException {

        log.info("url에 해당하는 html 가져옴");

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

        // tbody tr 로 추출
        // tr 이 없으면 마지막 페이지
        // Elements tr = html.getElementsByTag("tbody tr");
        // 해도 tr은 null이 아니다.

        log.info("마지막 페이지 확인");

        Elements tbody = html.getElementsByTag("tbody");

        String tbodyText = tbody.text().trim();

        // tbody에 아무것도 없을때(마지막 페이지)
        if (tbodyText.equals("") || tbodyText.equals("결과가 없습니다.")) {
            return true;
        } else {
            return false;
        }
    }


    public String replaceTitle(String title) {

        title = title.replaceAll("\n", "");

        //<\/.+?> 닫는태그
        //<.+?> 여는태그

        Pattern pattern = Pattern.compile("<\\/.+?>");

        Matcher matcher = pattern.matcher(title);
        //log.info("title : {}" , title);

        // 닫는 태그 삭제
        while (matcher.find()) {

            title = title.replace(matcher.group(), "");

            matcher = pattern.matcher(title);

        }


        pattern = Pattern.compile("<.+?>");

        matcher = pattern.matcher(title);

        int start = 0;
        int count = 1;

        // 여는 태그 중 Tag 클래스에 지정 되어 있는 태그는 삭제
        // 지정 되어 있지 않은 태그는 계층 구조를 나타내는 공백만 삭제하고 태그는 그대로 둔다.
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

    // 현재 html에서 자료에 대한 정보를 가져옴
    public List<FileInfo> fileInfoParse(Document html) {

        FileInfo fileInfo;

        List<FileInfo> infoList = new ArrayList<>();

        Elements fileBlocks = html.select("tbody tr");

        for (Element fileBlock : fileBlocks) {

            String titleElements = fileBlock.select("td .layerPop strong").toString();

            // 1. 명시 되어 있지 않은 태그 삭제
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
