package flyfish.service.impl;

import org.apache.pdfbox.pdmodel.*;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceCMYK;
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceRGB;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

import java.awt.image.BufferedImage;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import flyfish.config.BaiduOCRConfig;
import flyfish.config.QianfanConfig;
import flyfish.constant.M_CertificationConstant;
import flyfish.mapper.M_CertificationMapper;
import flyfish.mapper.M_TeacherListMapper;
import flyfish.mapper.TeacherListMapper;
import flyfish.pojo.DTO.M_CertificationDTO;
import flyfish.pojo.DTO.M_ImageUrlDTO;
import flyfish.pojo.M_Certification;
import flyfish.pojo.OcrResponse;
import flyfish.service.M_OcrService;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.graphics.PDXObject;
import org.apache.pdfbox.pdmodel.graphics.image.JPEGFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.*;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.springframework.web.client.RestTemplate;

import net.coobird.thumbnailator.Thumbnails;

@Service
@Slf4j
public class M_OcrServiceImpl implements M_OcrService {
    @Autowired
    private BaiduOCRConfig config;
    @Autowired
    private WebClient webClient;
    @Autowired
    private QianfanConfig qianfanConfig;
    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private M_CertificationMapper m_certificationMapper;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private flyfish.utils.AliOSSUtils aliOSSUtils;


    @Override
//    public List<byte[]> convertPdfToImages(byte[] pdfBytes, int maxPages) throws IOException {
//        List<byte[]> images = new ArrayList<>();
//
//        try (PDDocument document = PDDocument.load(pdfBytes)) {
//            int totalPages = document.getNumberOfPages();
//            int pagesToRender = Math.min(maxPages, totalPages);
//
//            if (pagesToRender == 0) {
//                return images;
//            }
//
//            // 创建渲染器（线程不安全，但在单线程中安全）
//            PDFRenderer renderer = new PDFRenderer(document);
//
//            for (int pageIndex = 0; pageIndex < pagesToRender; pageIndex++) {
//                // 渲染当前页（DPI 可根据需求调整）
//                BufferedImage image = renderer.renderImageWithDPI(pageIndex, 150, ImageType.RGB);
//
//                // 转换为 JPEG 字节数组
//                ByteArrayOutputStream baos = new ByteArrayOutputStream();
//                ImageIO.write(image, "jpg", baos);
//                images.add(baos.toByteArray());
//            }
//        } catch (IOException e) {
//            // 可根据需要记录日志或重新抛出
//            throw new IOException("PDF 转换失败", e);
//        }
//
//        return images;
//    }
    public List<byte[]> convertPdfToImages(byte[] pdfBytes, int maxPages) throws Exception {
        List<byte[]> images = Collections.synchronizedList(new ArrayList<>());

        // 预热：在类加载时已完成（见静态块），此处无需重复

        try (PDDocument document = PDDocument.load(pdfBytes)) {
            int totalPages = document.getNumberOfPages();
            int pagesToRender = Math.min(maxPages, totalPages);

            if (pagesToRender == 0) {
                return images;
            }

            // 创建线程池（建议使用可用处理器核心数）
            int threads = 3;
            ExecutorService executor = Executors.newFixedThreadPool(threads);

            for (int pageIndex = 0; pageIndex < pagesToRender; pageIndex++) {
                final int page = pageIndex;
                executor.submit(() -> {
                    try {
                        // 为每个线程创建独立的 PDFRenderer（重要！避免共享状态）
                        try (PDDocument doc = PDDocument.load(pdfBytes)) {
                            PDFRenderer renderer = new PDFRenderer(doc);
                            // 渲染当前页（DPI 可根据需求调整，150 是速度与质量的平衡点）
                            BufferedImage image = renderer.renderImageWithDPI(page, 150, ImageType.RGB);

                            // 转换为 JPEG 字节数组
                            ByteArrayOutputStream baos = new ByteArrayOutputStream();
                            ImageIO.write(image, "jpg", baos);
                            images.add(baos.toByteArray());
                        }
                    } catch (IOException e) {
                        // 建议使用日志记录异常，此处为简化直接抛出运行时异常
                        throw new RuntimeException("渲染第 " + page + " 页失败", e);
                    }
                });
            }

            executor.shutdown();
            // 等待所有任务完成（超时设置为 15分钟，可根据需要调整）

            if (!executor.awaitTermination(15, TimeUnit.MINUTES)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("PDF 转换被中断", e);
        }

        return images;
    }
    // 静态初始化块：用于预热 PDFBox 的字体和颜色空间，避免首次渲染延迟
    static {
        // 设置色彩管理模块（推荐在 Java 8+ 中使用 KCMS）
        System.setProperty("sun.java2d.cmm", "sun.java2d.cmm.kcms.KcmsServiceProvider");
        try {
            // 创建一个虚拟文档，加载常用字体和颜色空间
            try (PDDocument dummyDoc = new PDDocument()) {
                PDPage dummyPage = new PDPage(PDRectangle.A4);
                dummyDoc.addPage(dummyPage);

                try (PDPageContentStream dummyStream = new PDPageContentStream(dummyDoc, dummyPage)) {
                    // 加载标准字体
                    dummyStream.setFont(PDType1Font.HELVETICA, 12);
                    dummyStream.beginText();
                    dummyStream.newLineAtOffset(100, 700);
                    dummyStream.showText("Warm-up");
                    dummyStream.endText();
                }

                // 触发 CMYK/RGB 颜色空间的初始化
                PDDeviceCMYK.INSTANCE.toRGB(new float[]{0, 0, 0, 0});
                PDDeviceRGB.INSTANCE.toRGB(new float[]{0, 0, 0});

                // 可选：渲染一页小型图片以完全初始化渲染器
                PDFRenderer warmupRenderer = new PDFRenderer(dummyDoc);
                warmupRenderer.renderImage(0);
            }
        } catch (IOException e) {
            // 预热失败不影响主流程，仅打印警告
            System.err.println("PDFBox 预热失败: " + e.getMessage());
        }}


    @Override
    public void batchStructurizeOcrText(String pageText, String type, String pageImageUrl, String userName, String school) {
        try {
            String response = batchCallQianfanAPI(pageText);
            String teacherNameResp = batchCallQianfanAPIForTeacherName(pageText);
            log.info("结构文本{}", response);
            String orcContent = pageText;
            batchParseResponse(response,teacherNameResp, pageImageUrl, type, userName, orcContent, school);

        } catch (Exception e) {
            throw new RuntimeException("调用千帆API失败: " + e.getMessage(), e);
        }
    }



    @Override
    public void teacherBatchStructurizeOcrText(String pageText, String type, String pageImageUrl, String userName, String school) {
        try {
            String response = batchCallQianfanAPI(pageText);
            log.info("结构文本{}", response);
            String orcContent = pageText;
            teacherBatchParseResponse(response, pageImageUrl, type, userName, orcContent, school);

        } catch (Exception e) {
            throw new RuntimeException("调用千帆API失败: " + e.getMessage(), e);
        }
    }




    // 调用千帆API进行结构化处理
    @Override
    public M_Certification structurizeOcrText(String content, String type, String imageUrl, String userName, String school) {
        try {
            String response = callQianfanAPI(content);
            log.info("结构文本{}", response);
            String orcContent = content;
            return parseResponse(response, imageUrl, type, userName, orcContent, school);
        } catch (Exception e) {
            throw new RuntimeException("调用千帆API失败: " + e.getMessage(), e);
        }
    }

    @Override
    public void admineditCertification(M_Certification mCertification) {
        // 如果ID存在，更新记录；否则，插入新记录
        //把imageUrls的list转化为以;分隔的字符串存储到imageUrl字段中
        if (mCertification.getImageUrls() != null && !mCertification.getImageUrls().isEmpty()) {
            String imageUrlStr = String.join(";", mCertification.getImageUrls());
            mCertification.setImageUrl(imageUrlStr);
        }
        mCertification.setUserName(mCertification.getTeacherName());
        if (mCertification.getId() != null) {

            //先删除原有记录，再插入新记录
            m_certificationMapper.updateSingleRecord(mCertification);
        } else {
            m_certificationMapper.addSingleRecord(mCertification);
        }
    }

    @Override
    public void editCertification(M_Certification mCertification) {
        // 如果ID存在，更新记录；否则，插入新记录
        //把imageUrls的list转化为以;分隔的字符串存储到imageUrl字段中
        if (mCertification.getImageUrls() != null && !mCertification.getImageUrls().isEmpty()) {
            String imageUrlStr = String.join(";", mCertification.getImageUrls());
            mCertification.setImageUrl(imageUrlStr);
        }
        if (mCertification.getId() != null) {
            //先删除原有记录，再插入新记录
            m_certificationMapper.updateSingleRecord(mCertification);
        } else {
            m_certificationMapper.addSingleRecord(mCertification);
        }
    }

    // 根据用户名获取证书列表
    @Override
    public List<M_Certification> getCertificationsByUserName(M_CertificationDTO mCertificationDTO) {
        String userName = mCertificationDTO.getUserName();
        String school = mCertificationDTO.getSchool();
        List<M_Certification> certifications = m_certificationMapper.getCertificationsByUserName(userName, school);
        return certifications;
    }


    private String batchCallQianfanAPIForTeacherName(String pageText) {
        // 构建请求头
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", qianfanConfig.getAuthorization());
        headers.set("appid", qianfanConfig.getAppid());

        // 构建系统提示词 - 增加日期提取要求
        String systemPrompt = "你是一个专业的文本姓名识别助手。请从提供的文本中提取所有人的姓名，并以JSON格式返回：\n" +

                "teacherNameList (识别出全部人姓名，并且反复确认有没有识别出所有的人姓名，如果有多个姓名则用英文分号连接这些姓名)\n" +

                "请确保返回格式为：{ \"teacherNameList\": \"\"}";
        // 构建请求体
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", qianfanConfig.getModel());

        Map<String, Object>[] messages = new Map[2];

        // 系统消息
        Map<String, Object> systemMessage = new HashMap<>();
        systemMessage.put("role", "system");
        systemMessage.put("content", systemPrompt);
        messages[0] = systemMessage;

        // 用户消息
        Map<String, Object> userMessage = new HashMap<>();
        userMessage.put("role", "user");
        userMessage.put("content", pageText);
        messages[1] = userMessage;

        requestBody.put("messages", messages);
        requestBody.put("web_search", Map.of("enable", false));

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

        // 发送请求
        ResponseEntity<String> response = restTemplate.postForEntity(
                qianfanConfig.getUrl(), request, String.class);

        if (response.getStatusCode() == HttpStatus.OK) {
            return response.getBody();
        } else {
            throw new RuntimeException("API调用失败，状态码: " + response.getStatusCode());
        }
    }


    private String batchCallQianfanAPI(String pageText) {
        // 构建请求头
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", qianfanConfig.getAuthorization());
        headers.set("appid", qianfanConfig.getAppid());

        // 构建系统提示词 - 增加日期提取要求
        String systemPrompt = "你是一个专业的文本分析助手。请从用户提供的文本中提取以下信息，并以JSON格式返回：\n" +
//                "1. teacherName (获奖人姓名，如果多个姓名则用英文分号连接)\n" +
                "1. organization (颁发机构)\n" +
                "2. awardName (活动名称)\n" +
                "3. regionLevel (颁奖机构等级，只能从以下选项中选择：校级、区级、市级、省级、国家级)\n" +
                "4. awardLevel (奖项等级，只能从以下选项中选择：一等奖、二等奖、三等奖、优秀奖，没有等级的话默认优秀奖)\n" +
                "5. personal (判断是个人还是团体奖，如果获奖人数不止一个就是团体奖，如果是个人奖就返回\"个人\"，如果是团体奖就返回\"团体\")\n" +
                "6. supplement (补充信息，文本中其他未被包含的重要信息，如果没有则不填)\n" +
                "7. awardTime (颁发日期，请从文本中提取日期信息，并统一按照数字xxxx-xx-xx的格式返回，如果只有年份和月份，则日设置为01，如果只有年份，则月份和日分别设置为01-01)\n\n" +
                "请确保返回格式为：{ \"organization\": \"\", \"awardName\": \"\", \"regionLevel\": \"\", \"awardLevel\": \"\", \"personal\": \"\", \"supplement\": \"\", \"awardTime\": \"xxxx-xx-xx\"}";
        // 构建请求体
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", qianfanConfig.getModel());

        Map<String, Object>[] messages = new Map[2];

        // 系统消息
        Map<String, Object> systemMessage = new HashMap<>();
        systemMessage.put("role", "system");
        systemMessage.put("content", systemPrompt);
        messages[0] = systemMessage;

        // 用户消息
        Map<String, Object> userMessage = new HashMap<>();
        userMessage.put("role", "user");
        userMessage.put("content", pageText);
        messages[1] = userMessage;

        requestBody.put("messages", messages);
        requestBody.put("web_search", Map.of("enable", false));

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

        // 发送请求
        ResponseEntity<String> response = restTemplate.postForEntity(
                qianfanConfig.getUrl(), request, String.class);

        if (response.getStatusCode() == HttpStatus.OK) {
            return response.getBody();
        } else {
            throw new RuntimeException("API调用失败，状态码: " + response.getStatusCode());
        }
    }


    private String callQianfanAPI(String text) {
        // 构建请求头
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", qianfanConfig.getAuthorization());
        headers.set("appid", qianfanConfig.getAppid());

        // 构建系统提示词 - 增加日期提取要求
        String systemPrompt = "你是一个专业的文本分析助手。请从用户提供的文本中提取以下信息，并以JSON格式返回：\n" +
                "1. organization (颁发机构)\n" +
                "2. awardName (活动名称)\n" +
                "3. regionLevel (颁奖机构等级，只能从以下选项中选择：校级、区级、市级、省级、国家级)\n" +
                "4. awardLevel (奖项等级，只能从以下选项中选择：一等奖、二等奖、三等奖、优秀奖，没有等级的话默认优秀奖)\n" +
                "5. personal (判断是个人还是团体奖，如果获奖人数不止一个就是团体奖,如果是个人奖就返回个人，如果是团体奖就返回团体)\n" +
                "6. supplement (补充信息，文本中其他未被包含的重要信息，如果没有则不填)\n" +
                "7. awardTime (颁发日期，请从文本中提取日期信息，并统一按照数字xxxx-xx-xx的格式返回，如果只有年份和月份，则日设置为01，如果只有年份，则月份和日分别设置为01-01)\n\n" +

                "请确保返回格式为：{\"organization\": \"\", \"awardName\",\"regionLevel\": \"\", \"awardLevel\": \"\", \"personal\": 个人/团体, \"supplement\": \"\",\"awardTime\": \"xxxx-xx-xx\"}";

        // 构建请求体
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", qianfanConfig.getModel());

        Map<String, Object>[] messages = new Map[2];

        // 系统消息
        Map<String, Object> systemMessage = new HashMap<>();
        systemMessage.put("role", "system");
        systemMessage.put("content", systemPrompt);
        messages[0] = systemMessage;

        // 用户消息
        Map<String, Object> userMessage = new HashMap<>();
        userMessage.put("role", "user");
        userMessage.put("content", text);
        messages[1] = userMessage;

        requestBody.put("messages", messages);
        requestBody.put("web_search", Map.of("enable", false));

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

        // 发送请求
        ResponseEntity<String> response = restTemplate.postForEntity(
                qianfanConfig.getUrl(), request, String.class);

        if (response.getStatusCode() == HttpStatus.OK) {
            return response.getBody();
        } else {
            throw new RuntimeException("API调用失败，状态码: " + response.getStatusCode());
        }
    }


    private void teacherBatchParseResponse(String response, String pageImageUrl, String type, String userName, String orcContent, String school) {
        try {
            JsonNode root = objectMapper.readTree(response);

            // 正确的提取路径：choices[0].message.content
            JsonNode choices = root.path("choices");

            JsonNode firstChoice = choices.get(0);
            JsonNode message = firstChoice.path("message");
            String content = message.path("content").asText();


            // 从content中提取JSON字符串
            String jsonStr = extractJsonFromText(content);

            teachersetSingleCertification(jsonStr, pageImageUrl, type, school, orcContent,userName);

        } catch (Exception e) {
            throw new RuntimeException("解析API响应失败: " + e.getMessage(), e);
        }

    }



    //批量解析千帆响应
    private void batchParseResponse(String response,String teacherNameResp, String pageImageUrl, String type, String userName, String orcContent, String school) {
        try {
            JsonNode root = objectMapper.readTree(response);

            // 正确的提取路径：choices[0].message.content
            JsonNode choices = root.path("choices");

            JsonNode firstChoice = choices.get(0);
            JsonNode message = firstChoice.path("message");
            String content = message.path("content").asText();



            // 解析教师姓名的响应
            JsonNode teacherNameRoot = objectMapper.readTree(teacherNameResp);

            JsonNode teacherChoices = teacherNameRoot.path("choices");
            JsonNode teacherFirstChoice = teacherChoices.get(0);
            JsonNode teacherMessage = teacherFirstChoice.path("message");
            String teacherNameContent = teacherMessage.path("content").asText();


            // 从content中提取JSON字符串
            String jsonStr = extractJsonFromText(content);
            String teacherNameJsonStr = extractJsonFromText(teacherNameContent);


            setSingleCertification(jsonStr,teacherNameJsonStr, pageImageUrl, type, school, orcContent);

        } catch (Exception e) {
            throw new RuntimeException("解析API响应失败: " + e.getMessage(), e);
        }
    }
    private void teachersetSingleCertification(String jsonStr, String pageImageUrl, String type, String school, String orcContent,String userName) throws JsonProcessingException {

        JsonNode data = objectMapper.readTree(jsonStr);
        log.info("结构化的文本：{}", data.toString());
        M_Certification certification = new M_Certification();
        certification.setOrganization(data.path("organization").asText());
        certification.setAwardName(data.path("awardName").asText());
        certification.setRegionLevel(data.path("regionLevel").asText());
        certification.setAwardLevel(data.path("awardLevel").asText());
        certification.setPersonal(data.path("personal").asText());
        certification.setUserName(userName);
        certification.setImageUrl(pageImageUrl);
        certification.setType(type);
        certification.setContent(orcContent);
        certification.setSupplement(data.path("supplement").asText());
        certification.setDisplay("显示");

        certification.setSchool(school);


// 处理日期字段
        String awardTimeStr = data.path("awardTime").asText();
        if (!awardTimeStr.isEmpty()) {
            try {
                LocalDate awardTime = LocalDate.parse(awardTimeStr);
                certification.setAwardTime(awardTime);
            } catch (Exception e) {
                log.error("日期解析失败: {}", awardTimeStr, e);
                // 根据业务需求，可以选择设置为null或者抛出异常
                certification.setAwardTime(null);
            }
        } else {
            certification.setAwardTime(null);
        }

        judgeSetRepeat(certification);










    }
    private void setSingleCertification(String jsonStr,String teacherNameJsonStr, String pageImageUrl, String type, String school, String orcContent) throws JsonProcessingException {


        JsonNode data = objectMapper.readTree(jsonStr);
        JsonNode teacherNameData = objectMapper.readTree(teacherNameJsonStr);
        log.info("识别的教师文本：{}", teacherNameData.path("teacherNameList").asText());
        log.info("结构化的文本：{}", data.toString());
        M_Certification certification = new M_Certification();
        certification.setOrganization(data.path("organization").asText());
        certification.setAwardName(data.path("awardName").asText());
        certification.setRegionLevel(data.path("regionLevel").asText());
        certification.setAwardLevel(data.path("awardLevel").asText());
        certification.setPersonal(data.path("personal").asText());
        certification.setImageUrl(pageImageUrl);
        certification.setType(type);
        certification.setContent(orcContent);
        certification.setSupplement(data.path("supplement").asText());
        certification.setDisplay("显示");

        certification.setSchool(school);


// 处理日期字段
        String awardTimeStr = data.path("awardTime").asText();
        if (!awardTimeStr.isEmpty()) {
            try {
                LocalDate awardTime = LocalDate.parse(awardTimeStr);
                certification.setAwardTime(awardTime);
            } catch (Exception e) {
                log.error("日期解析失败: {}", awardTimeStr, e);
                // 根据业务需求，可以选择设置为null或者抛出异常
                certification.setAwardTime(null);
            }
        } else {
            certification.setAwardTime(null);
        }

        if (data.path("personal").asText().equals("个人")) {
            if (teacherNameData.path("teacherNameList").asText().isEmpty() || teacherNameData.path("teacherNameList").asText() == null) {
                certification.setUserName("未知获奖人");
            } else {
                certification.setUserName(teacherNameData.path("teacherNameList").asText());
            }

            //在新增之前，先检查证书是否重复，如果重复则标记为重复证书
            judgeSetRepeat(certification);


//
        } else {
            List<String> teacherNameList = Arrays.asList(teacherNameData.path("teacherNameList").asText().split(";"));
            Integer i = 0;
            if (teacherNameList != null && !teacherNameList.isEmpty()) {

                for (String teacherName : teacherNameList) {
                    M_Certification cert = new M_Certification();
                    cert.setOrganization(certification.getOrganization());
                    cert.setAwardName(certification.getAwardName());
                    cert.setRegionLevel(certification.getRegionLevel());
                    cert.setAwardLevel(certification.getAwardLevel());
                    cert.setPersonal(certification.getPersonal());
                    cert.setImageUrl(certification.getImageUrl());
                    cert.setType(certification.getType());
                    cert.setContent(certification.getContent());
                    cert.setSupplement(certification.getSupplement());
                    cert.setDisplay(certification.getDisplay());
                    cert.setSchool(certification.getSchool());
                    cert.setUserName(teacherName);
                    cert.setAwardTime(certification.getAwardTime());

                    if (i == 0) {

                        judgeSetRepeat(cert);
                        Integer repeatId = m_certificationMapper.getIdByAll(cert);
                        if (repeatId == null) {
                            m_certificationMapper.markDuplicateById(cert.getId(), "是", cert.getId());
                        }


                    } else {
                        //在新增之前，先检查证书是否重复，如果重复则标记为重复证书
                        judgeSetRepeat(cert);
                    }
                    i++;


                }
            } else {
                certification.setUserName("未知获奖人");
                judgeSetRepeat(certification);
            }

        }


    }


    // 解析千帆API响应
    private M_Certification parseResponse(String response, String imageUrl, String type, String userName, String orcContent, String school) {
        try {
            JsonNode root = objectMapper.readTree(response);

            // 正确的提取路径：choices[0].message.content
            JsonNode choices = root.path("choices");

            JsonNode firstChoice = choices.get(0);
            JsonNode message = firstChoice.path("message");
            String content = message.path("content").asText();


            // 从content中提取JSON字符串
            String jsonStr = extractJsonFromText(content);


            JsonNode data = objectMapper.readTree(jsonStr);
            M_Certification certification = new M_Certification();
            certification.setOrganization(data.path("organization").asText());
            certification.setAwardName(data.path("awardName").asText());
            certification.setRegionLevel(data.path("regionLevel").asText());
            certification.setAwardLevel(data.path("awardLevel").asText());
            certification.setPersonal(data.path("personal").asText());
            certification.setImageUrl(imageUrl);
            certification.setType(type);
            certification.setUserName(userName);
            certification.setContent(orcContent);
            certification.setSupplement(data.path("supplement").asText());
            certification.setDisplay("显示");

            certification.setSchool(school);


// 处理日期字段
            String awardTimeStr = data.path("awardTime").asText();
            if (!awardTimeStr.isEmpty()) {
                try {
                    LocalDate awardTime = LocalDate.parse(awardTimeStr);
                    certification.setAwardTime(awardTime);
                } catch (Exception e) {
                    log.error("日期解析失败: {}", awardTimeStr, e);
                    // 根据业务需求，可以选择设置为null或者抛出异常
                    certification.setAwardTime(null);
                }
            } else {
                certification.setAwardTime(null);
            }


            //在新增之前，先检查证书是否重复，如果重复则标记为重复证书
            judgeSetRepeat(certification);

            return certification;
        } catch (Exception e) {
            throw new RuntimeException("解析API响应失败: " + e.getMessage(), e);
        }
    }

    //检查证书是否重复，如果重复则标记为重复证书。
    private void judgeSetRepeat(M_Certification certification) {
        List<M_Certification> existingCerts = m_certificationMapper.getRepeatList(certification);
        Integer i = 0;
        if(existingCerts != null && !existingCerts.isEmpty()){

        for (M_Certification existingCert : existingCerts) {
            String newSupplement = preprocess(certification.getSupplement());
            String existSupplement = preprocess(existingCert.getSupplement());
            double sim = similarity(newSupplement, existSupplement, true);
            if (sim >= 0.4) {
                Integer repeatId = existingCert.getRepeatId() != null ? existingCert.getRepeatId() : existingCert.getId();
                m_certificationMapper.markDuplicateById(existingCert.getId(), "是", repeatId);
                certification.setRepeatId(repeatId);
                certification.setJudgeRepeat("是");
                m_certificationMapper.addSingleRecord(certification);
                break;

            } else {
                certification.setJudgeRepeat("否");
                //如果遍历到最后一个，才新增
                if (i == existingCerts.size() - 1) {
                    certification.setRepeatId(null);
                    m_certificationMapper.addSingleRecord(certification);
                }

            }


        }}else {
            certification.setJudgeRepeat("否");
            m_certificationMapper.addSingleRecord(certification);
        }

    }


    /**
     * 预处理：去除标点符号、空格，并可选转换为简体（需引入额外库，此处仅示例）
     * 实际项目中可以使用 OpenCC 等库进行繁简转换
     */
    private static String preprocess(String str) {
        if (str == null) return "";
        // 去除首尾空格
        str = str.trim();
        // 去除所有标点符号（保留汉字、字母、数字）
        // 此正则保留 \p{L}（字母）和 \p{N}（数字），可根据需求调整
        str = str.replaceAll("[^\\p{L}\\p{N}]", "");
        // 如果需要统一全角/半角，可进行转换（此处略）
        // 如果需要繁简转换，可调用第三方库，如 OpenCC
        return str;
    }


    /**
     * 计算两个字符串的相似度（基于 Levenshtein 距离）
     *
     * @param s1         第一个字符串
     * @param s2         第二个字符串
     * @param preprocess 是否进行预处理（去除标点、空格等）
     * @return 相似度 (0.0 ~ 1.0)
     */
    private static double similarity(String s1, String s2, boolean preprocess) {
        if (s1 == null || s2 == null) return 0.0;
        String a = preprocess ? preprocess(s1) : s1;
        String b = preprocess ? preprocess(s2) : s2;

        int len1 = a.length();
        int len2 = b.length();
        if (len1 == 0 && len2 == 0) return 1.0;
        if (len1 == 0 || len2 == 0) return 0.0;

        int distance = computeLevenshteinDistance(a, b);
        int maxLen = Math.max(len1, len2);
        return 1.0 - (double) distance / maxLen;
    }

    /**
     * 计算 Levenshtein 距离（支持中文，因为使用 char 或 code point 都可以）
     * 使用 char 数组方式简单，但要注意 Java 的 char 不能表示全部 Unicode（比如表情符号），
     * 但中文字符都在 BMP 内，所以使用 char 是安全的。
     */
    private static int computeLevenshteinDistance(String s1, String s2) {
        int len1 = s1.length();
        int len2 = s2.length();
        int[][] dp = new int[len1 + 1][len2 + 1];

        for (int i = 0; i <= len1; i++) dp[i][0] = i;
        for (int j = 0; j <= len2; j++) dp[0][j] = j;

        for (int i = 1; i <= len1; i++) {
            for (int j = 1; j <= len2; j++) {
                int cost = (s1.charAt(i - 1) == s2.charAt(j - 1)) ? 0 : 1;
                dp[i][j] = Math.min(Math.min(
                                dp[i - 1][j] + 1,
                                dp[i][j - 1] + 1),
                        dp[i - 1][j - 1] + cost);
            }
        }
        return dp[len1][len2];
    }

    // 从文本中提取JSON字符串
    private String extractJsonFromText(String content) {
        // 简单的JSON提取逻辑，可以根据实际情况调整
        int start = content.indexOf("{");
        int end = content.lastIndexOf("}") + 1;
        if (start >= 0 && end > start) {
            return content.substring(start, end);
        }
        return content;
    }


    @Override
    public OcrResponse batchProcessFile(MultipartFile file) throws Exception {

        String fileName = file.getOriginalFilename();
        String fileExtension = getFileExtension(fileName);

        // 获取文件字节数组
        byte[] fileBytes = file.getBytes();

        // 如果文件大小超过1MB，则进行压缩
        long oneMB = 1024 * 1024; // 1MB
        if (fileBytes.length > oneMB) {
            log.info("文件大小超过1MB，开始压缩，原始大小：{} bytes", fileBytes.length);
            fileBytes = compressFile(fileBytes, fileExtension);
            log.info("压缩完成，压缩后大小：{} bytes", fileBytes.length);
        }



        if (isImageFile(fileExtension)) {
            // 直接OCR识别
            String text = recognize(file.getBytes());
            String imageUrl = aliOSSUtils.uploadCertification(fileBytes, "jpg", "image/jpeg");
            log.info("上传到阿里云的图片URL: {}", imageUrl);

            OcrResponse ocrResponse = OcrResponse.success(text);
            ocrResponse.setImageUrlList(List.of(imageUrl));
            ocrResponse.setImageUrls(imageUrl);
            ocrResponse.setPageTexts(List.of(text));
            return ocrResponse;

        } else if (isPdfFile(fileExtension)) {
            // PDF转图片后OCR识别
            List<byte[]> images = convertPdfToImages(fileBytes, M_CertificationConstant.MAX_PAGE_SIZE);
            log.info("PDF转换为图片成功，生成了 {} 张图片", images.size());
            //把这些images逐一上传到oss，并返回imageUrl然后用;拼接成一个字符串,并且如果只有一页的话就不加;
            String imageUrls = "";
            List<String> imageUrlList = new ArrayList<>();
            for (int i = 0; i < images.size(); i++) {
                String imageUrl = aliOSSUtils.uploadCertification(images.get(i), "png", "image/png");
                log.info("第" + (i + 1) + "个上传到阿里云的图片URL: {}", imageUrl);
                imageUrlList.add(imageUrl);
                imageUrls += imageUrl;
                if (i != images.size() - 1) {
                    imageUrls += ";";
                }
            }

            StringBuilder allText = new StringBuilder();
            List<String> pageTexts = new ArrayList<>();

            for (int i = 0; i < images.size(); i++) {
                String pageText = recognize(images.get(i));
                allText.append("第").append(i + 1).append("页:\n").append(pageText).append("\n\n");
                pageTexts.add(pageText);
            }

            OcrResponse response = OcrResponse.success(allText.toString());
            response.setPageTexts(pageTexts);
            response.setImageUrls(imageUrls);
            response.setImageUrlList(imageUrlList);
            return response;

        } else {
            throw new IllegalArgumentException("不支持的文件格式");
        }


    }


    // 处理上传的文件并进行OCR识别
    @Override
    public OcrResponse processFile(MultipartFile file) throws Exception {
        String fileName = file.getOriginalFilename();
        String fileExtension = getFileExtension(fileName);

        // 获取文件字节数组
        byte[] fileBytes = file.getBytes();

        // 如果文件大小超过1MB，则进行压缩
        long oneMB = 1024 * 1024; // 1MB
        if (fileBytes.length > oneMB) {
            log.info("文件大小超过1MB，开始压缩，原始大小：{} bytes", fileBytes.length);
            fileBytes = compressFile(fileBytes, fileExtension);
            log.info("压缩完成，压缩后大小：{} bytes", fileBytes.length);
        }


        if (isImageFile(fileExtension)) {
            // 直接OCR识别
            String text = recognize(file.getBytes());
            String imageUrl = aliOSSUtils.uploadCertification(fileBytes, "jpg", "image/jpeg");
            log.info("上传到阿里云的图片URL: {}", imageUrl);

            OcrResponse ocrResponse = OcrResponse.success(text);
            ocrResponse.setImageUrls(imageUrl);
            return ocrResponse;

        } else if (isPdfFile(fileExtension)) {
            // PDF转图片后OCR识别
            // 仅处理前9页
            List<byte[]> images = convertPdfToImages(fileBytes, 9);
            log.info("PDF转换为图片成功，生成了 {} 张图片", images.size());
            //把这些images逐一上传到oss，并返回imageUrl然后用;拼接成一个字符串,并且如果只有一页的话就不加;
            String imageUrls = "";
            for (int i = 0; i < images.size(); i++) {
                String imageUrl = aliOSSUtils.uploadCertification(images.get(i), "png", "image/png");
                log.info("第" + i + "个上传到阿里云的图片URL: {}", imageUrl);
                imageUrls += imageUrl;
                if (i != images.size() - 1) {
                    imageUrls += ";";
                }
            }

            StringBuilder allText = new StringBuilder();
            List<String> pageTexts = new ArrayList<>();

            for (int i = 0; i < images.size(); i++) {
                String pageText = recognize(images.get(i));
                allText.append("第").append(i + 1).append("页:\n").append(pageText).append("\n\n");
                pageTexts.add(pageText);
            }

            OcrResponse response = OcrResponse.success(allText.toString());
            response.setPageTexts(pageTexts);
            response.setImageUrls(imageUrls);
            return response;

        } else {
            throw new IllegalArgumentException("不支持的文件格式");
        }
    }

    /**
     * 根据文件类型对字节数组进行压缩
     * @param data 原始文件字节
     * @param extension 文件扩展名（如 "jpg", "pdf"）
     * @return 压缩后的字节数组
     */
    private byte[] compressFile(byte[] data, String extension) throws IOException {
        if (isImageFile(extension)) {
            // 图片压缩：调整尺寸或质量，例如使用Thumbnails库
            return compressImage(data);
        } else if (isPdfFile(extension)) {
            // PDF压缩：可使用PDFBox或iText等库减少文档大小（如压缩图片、移除元数据等）
            return compressPdf(data);
        } else {
            // 其他格式暂时不压缩，或抛出异常
            return data;
        }
    }

    /**
     * 压缩PDF字节数组（主要压缩内部图片）
     * @param data 原始PDF数据
     * @return 压缩后的PDF数据
     * @throws IOException PDF处理失败时抛出
     */
    private byte[] compressPdf(byte[] data) throws IOException {
        // 使用try-with-resources确保所有流和文档正确关闭
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(data);
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
             PDDocument document = PDDocument.load(inputStream)) {

            // 1. 移除文档元数据
            PDDocumentInformation info = document.getDocumentInformation();
            if (info != null) {
                info.setTitle(null);
                info.setAuthor(null);
                info.setSubject(null);
                info.setKeywords(null);
                info.setCreator(null);
                info.setProducer(null);
                info.setCreationDate(null);
                info.setModificationDate(null);
            }
            // 移除文档目录的Metadata流（如果有）
            document.getDocumentCatalog().setMetadata(null);

            // 2. 遍历所有页面，压缩图片
            for (PDPage page : document.getPages()) {
                PDResources resources = page.getResources();
                if (resources == null) continue;

                // 获取所有XObject（包括图片）
                for (COSName name : resources.getXObjectNames()) {
                    PDXObject xobject = resources.getXObject(name);
                    if (!(xobject instanceof PDImageXObject)) {
                        continue; // 只处理图片对象
                    }

                    PDImageXObject image = (PDImageXObject) xobject;
                    BufferedImage originalImage = null;
                    try {
                        originalImage = image.getImage();
                    } catch (IOException e) {
                        // 如果原始图片读取失败，记录日志并跳过压缩
                        LoggerFactory.getLogger(getClass()).warn("读取图片失败，跳过压缩: {}", name.getName(), e);
                        continue;
                    }

                    // 检查图片尺寸是否有效
                    int width = originalImage.getWidth();
                    int height = originalImage.getHeight();
                    if (width <= 0 || height <= 0) {
                        LoggerFactory.getLogger(getClass()).warn("图片尺寸无效 ({}x{})，跳过压缩", width, height);
                        continue;
                    }

                    // 如果图片已经很小（例如小于50像素），跳过压缩避免失真
                    if (width < 50 || height < 50) {
                        continue;
                    }

                    // 计算目标尺寸（减半），并确保至少为1像素
                    int targetWidth = Math.max(1, width / 2);
                    int targetHeight = Math.max(1, height / 2);

                    ByteArrayOutputStream compressedBaos = new ByteArrayOutputStream();
                    try {
                        // 使用Thumbnailator压缩图片：缩放尺寸 + JPEG质量压缩
                        Thumbnails.of(originalImage)
                                .size(targetWidth, targetHeight)
                                .outputFormat("jpg")
                                .outputQuality(0.6)
                                .toOutputStream(compressedBaos);
                    } catch (Exception e) {
                        // 如果压缩过程异常，记录日志并保留原图
                        LoggerFactory.getLogger(getClass()).warn("图片压缩失败，跳过该图片: {}", name.getName(), e);
                        continue;
                    }

                    // 从压缩后的字节流创建新的PDImageXObject
                    try (ByteArrayInputStream compressedInputStream = new ByteArrayInputStream(compressedBaos.toByteArray())) {
                        // 使用JPEGFactory从流创建图片，避免二次解码
                        PDImageXObject compressedImageXObject = JPEGFactory.createFromStream(document, compressedInputStream);
                        // 替换原图片资源
                        resources.put(name, compressedImageXObject);
                    } catch (IOException e) {
                        // 如果创建新图片失败，记录日志并保留原图
                        LoggerFactory.getLogger(getClass()).warn("替换压缩图片失败，保留原图: {}", name.getName(), e);
                    }
                }
            }

            // 3. 保存压缩后的PDF（启用压缩）
            document.save(outputStream);
            return outputStream.toByteArray();
        }
    }



    /**
     * 压缩图片字节数组
     * @param data 原始图片数据
     * @return 压缩后的图片数据（JPEG格式）
     * @throws IOException 图片处理失败时抛出
     */
    private byte[] compressImage(byte[] data) throws IOException {
        // 输入流
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(data);
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            // 使用 Thumbnails 进行压缩：设置目标尺寸和质量
            Thumbnails.of(inputStream)
                    .size(800, 600)           // 目标最大宽高（保持原比例）
                    .outputFormat("jpg")       // 输出格式为 JPEG
                    .outputQuality(0.8)        // 压缩质量 (0.0~1.0)
                    .toOutputStream(outputStream);

            return outputStream.toByteArray();
        }
    }




    // 使用百度OCR识别图片
    public String recognize(byte[] imageBytes) throws Exception {
        // 检查图片大小并自动压缩
        byte[] processedImage = processImageSize(imageBytes);

        String accessToken = getAccessToken();
        String base64Image = Base64.getEncoder().encodeToString(processedImage);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("image", base64Image);

        String response = webClient.post()
                .uri(config.getOcrUrl() + "?access_token=" + accessToken)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData("image", base64Image))
                .retrieve()
                .bodyToMono(String.class)
                .block();

        return parseOcrResult(response);
    }

    // 处理图片大小，确保不超过4MB
    private byte[] processImageSize(byte[] imageBytes) throws Exception {
        long maxSize = 4 * 1024 * 1024; // 4MB

        // 如果图片小于4MB，直接返回
        if (imageBytes.length <= maxSize) {
            return imageBytes;
        }

        // 压缩图片
        return compressImage(imageBytes, maxSize);
    }

    // 递归压缩图片直到满足大小要求
    private byte[] compressImage(byte[] imageBytes, long maxSize) throws Exception {
        try {
            // 读取原始图片
            ByteArrayInputStream inputStream = new ByteArrayInputStream(imageBytes);
            BufferedImage image = ImageIO.read(inputStream);

            if (image == null) {
                throw new IllegalArgumentException("无法读取图片格式");
            }

            // 计算压缩比例 (目标大小/原始大小)
            double compressionRatio = Math.sqrt((double) maxSize / imageBytes.length);

            // 计算新的尺寸
            int newWidth = (int) (image.getWidth() * compressionRatio);
            int newHeight = (int) (image.getHeight() * compressionRatio);

            // 确保最小尺寸
            newWidth = Math.max(newWidth, 100);
            newHeight = Math.max(newHeight, 100);

            // 创建缩放后的图片
            BufferedImage resizedImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = resizedImage.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g.drawImage(image, 0, 0, newWidth, newHeight, null);
            g.dispose();

            // 输出为JPEG格式（可调整质量）
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ImageIO.write(resizedImage, "jpg", outputStream);

            byte[] compressedBytes = outputStream.toByteArray();

            // 如果压缩后仍然太大，递归压缩
            if (compressedBytes.length > maxSize) {
                return compressImage(compressedBytes, maxSize);
            }

            return compressedBytes;

        } catch (Exception e) {
            // 如果压缩失败，返回原始图片（可能会继续报错）
            return imageBytes;
        }
    }


    // 解析OCR结果，提取文字内容，这里还需要根据实际返回格式进行调整
    private String parseOcrResult(String response) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> result = mapper.readValue(response, Map.class);

        List<Map<String, Object>> wordsResult = (List<Map<String, Object>>) result.get("words_result");
        StringBuilder text = new StringBuilder();

        for (Map<String, Object> word : wordsResult) {
            text.append(word.get("words")).append("\n");
        }

        return text.toString();
    }


    // 获取百度OCR的access_token
    private String getAccessToken() throws Exception {
        // 这里实现获取百度OCR access_token的逻辑
        // 由于access_token有缓存，建议在实际项目中缓存token
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "client_credentials");
        params.add("client_id", config.getApiKey());
        params.add("client_secret", config.getSecretKey());

        String response = webClient.post()
                .uri(config.getAccessTokenUrl())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData(params))
                .retrieve()
                .bodyToMono(String.class)
                .block();

        // 解析response获取access_token
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> result = mapper.readValue(response, Map.class);
        return (String) result.get("access_token");
    }


    private boolean isImageFile(String extension) {
        return Arrays.asList("png", "jpg", "jpeg").contains(extension);
    }

    private boolean isPdfFile(String extension) {
        return "pdf".equals(extension);
    }


    private String getFileExtension(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            return "";
        }
        return fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
    }


}
