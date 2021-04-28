package com.yk.scraper;


import com.yk.scraper.entity.FileInfoEntity;
import com.yk.scraper.mapper.ScrapeMapper;
import com.yk.scraper.model.FileInfo;
import com.yk.scraper.model.HankyungUrl;
import com.yk.scraper.service.ScrapeService;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ScrapeServiceTest {

    private static final Logger log = LoggerFactory.getLogger(ScrapeService.class);


    @Autowired
    ScrapeService scrapeService;

    @Autowired
    ScrapeMapper scrapeMapper;

    @Autowired
    HankyungUrl url;

    @Test
    void testSetTerm(){

        Map<String, List> test = scrapeService.setTerm("2020-01-12","2021-01-11");
        //term.put("sDateList", sDateList);
        //term.put("eDateList", eDateList);
        List<String> eDateList = test.get("eDateList");
        List<String> sDateList = test.get("sDateList");

        for (int i = 0; i < eDateList.size(); i++) {
            String eDate = eDateList.get(i);
            String sDate = eDateList.get(i);

            System.out.println(sDate + " ~ " + eDate);
        }

    }

    @Test
    void testFetchHtml() {
        // Jsoup.parse 를 하면 html 형식으로 만들기 때문에 fetchHtml이 정상적으로 실행이 됐다면 html형식이고 null 여부만 판단하면 된다.

        String sDate = "2007-10-24";
        String eDate = "2007-10-24";
        int nowPage = 2;
        Document html = null;
        try {
            html = scrapeService.fetchHtml(url.getUrl(), sDate, eDate, nowPage);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        assertNotNull(html, "html is null");

    }

    @Test
    void testHtmlParse() {

        String html = "<html><body><table><tr>" +
                "<td class=\"first txt_number\">2020-12-08</td>" +
                "<td>채권</td>" +
                "<td><a><div class=\"layerPop\"><strong>Credit Inside</strong></div></a></td>" +
                "<td>김민정</td>" +
                "<td>한화투자증권</td>" +
                "<td>" +
                "<div class=\"dv_input\">\n" +
                "<a href=\"/apps.analysis/analysis.downpdf?report_idx=578107\" title=\"메리츠20201207조선.pdf\" target=\"_blank\">" +
                "<img src=\"/images/btn_attached.gif\" alt=\"메리츠20201207조선.pdf\">" +
                "</a>" +
                "</div>" +
                "</td>" +
                "</tr></table></body></html>";



        Document docs = Jsoup.parse(html);

        List<FileInfo> fileInfoList = scrapeService.fileInfoParse(docs);
        FileInfo fileInfo = fileInfoList.get(0);

        assertEquals("2020-12-08", fileInfo.getWriteDate());
        assertEquals("채권", fileInfo.getPart());
        assertEquals("Credit Inside", fileInfo.getTitle());
        assertEquals("김민정", fileInfo.getWriter());
        assertEquals("한화투자증권", fileInfo.getProvider());
        assertEquals("/apps.analysis/analysis.downpdf?report_idx=578107", fileInfo.getFileUrl());

    }

    @Test
    void testFinalPageCheck() {

        // 마지막 페이지 html
        String html = "<html><body><table><tbody>" +
                "</tbody></table></body></html>";

        Document doc = Jsoup.parse(html);
        boolean exit = scrapeService.finalPageCheck(doc);

        assertTrue(exit);

    }

    @Test
    void testDuplicateCheck() {

        FileInfoEntity fileInfoEntity = new FileInfoEntity();

        // 중복 데이터 설정
        fileInfoEntity.setProvider("엘지증권");
        fileInfoEntity.setWriteDate("2004-11-11");
        fileInfoEntity.setWriter("정승교");
        fileInfoEntity.setPart("기업");
        fileInfoEntity.setTitle("하나로통신");
        fileInfoEntity.setFileUrl("testUrl");

        scrapeMapper.insertInfo(fileInfoEntity);

        boolean duplicateCheck = scrapeService.duplicateCheck(fileInfoEntity);

        log.info("" + duplicateCheck);

        assertFalse(duplicateCheck);

        scrapeMapper.DeleteFileInfo(fileInfoEntity);
    }

    @Test
    void testReplaceTitle(){

        String testTitle = "<strong>때론, 이평선 배열(60일선&lt;20일선 \n" +
                "<주가)을 활용할 필요도 있다.< strong> \n" +
                "  <ul> \n" +
                "   <li>\uDBC0\uDCA0 미 연준의 정책에 대한 불확실성 완화와 국제유가 하락 소식에 빠른 주가 회복 시도. \uDBC0\uDCA0 때로는 이동평균선의 배열을 활용하여 종목 대응을 선택해보는 것도 하나의 투자 방법.</li> \n" +
                "  </ul> \n" +
                " </주가)을></strong>\n" +
                "<strong> </strong>";

        String result = "때론, 이평선 배열(60일선&lt;20일선<주가)을 활용할 필요도 있다.";

        String replaceTitle = scrapeService.replaceTitle(testTitle);

        assertEquals(result,replaceTitle);

    }
    @Test
    void testUnknownTag() {
        String result ="KT(030200)5G커넥티드카<지디><대상> 달린다..판교를 제2의 5G거점으로..<전자>";

        String html = "<table><tr>" +
                "<td class=\"text_l\">\n" +
                "                                    <a href=\"javascript:void(0);\" onmouseover=\"$('#content_413577').show();\" onmouseout=\"$('#content_413577').hide();\">KT(030200)5G커넥티드카 달린다..판교를 제2의 5G거점으로..&lt;전...</a>\n" +
                "                                    <div class=\"layerPop\">\n" +
                "                                        <div id=\"content_413577\" class=\"pop01 disNone\" style=\"display: none;\">\n" +
                "                                            <strong>KT(030200)5G커넥티드카<지디><대상> 달린다..판교를 제2의 5G거점으로..<전자></strong>\n" +
                "                                            <ul>\n" +
                "                                                <li>KT, 평창에 이어 판교에 제2의 5G 테스트베드가 구축. KT, 판교에서 자동차가 5G 통신망을 활용해 주변 자동차 또는 인프라와 통신하며 안</li><li>전을 극대화하는 V2X기술을 시연할 계획. 도심 환경 테스트에 앞서 기술이 ...</li>\n" +
                "                                            </ul>\n" +
                "                                        </div>\n" +
                "                                    </div>\n" +
                "                                </td>\n" +
                "</tr></table>";

        Document docs = Jsoup.parse(html);
        String title = docs.select("td .layerPop strong").toString();
        log.info("[before] : {}", title);
        title = scrapeService.replaceTitle(title);
        log.info("[after] : {}", title);

        assertEquals(result,title);

    }


}

