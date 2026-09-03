package flyfish.contoller;

import flyfish.mapper.M_CertificationMapper;
import flyfish.pojo.DTO.M_CertificationDTO;
import flyfish.pojo.DTO.M_IdListDTO;
import flyfish.pojo.DTO.M_ImageUrlDTO;
import flyfish.pojo.M_Certification;
import flyfish.pojo.M_ExportRequestDTO;
import flyfish.pojo.OcrResponse;
import flyfish.service.M_CertificationService;
import flyfish.service.M_OcrService;
import flyfish.service.M_ResumeExportService;
import flyfish.utils.AliOSSUtils;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@RestController
@Slf4j
public class M_CertificationController {
    @Autowired
    private M_OcrService m_OcrService;
    @Autowired
    private AliOSSUtils aliOSSUtils;
    @Autowired
    private M_CertificationMapper m_CertificationMapper;
    @Autowired
    private M_ResumeExportService resumeExportService;
    @Autowired
    private M_CertificationService certificationService;


    // 上传单个奖项文件并进行OCR识别和结构化处理
    @PostMapping(value = "/mpi/ocr/upload", produces = "application/json;charset=UTF-8")
    public ResponseEntity<M_Certification> uploadFile(@RequestParam("file") MultipartFile file, @RequestParam String type,@RequestParam String userName, @RequestParam String school) {
        try {
            // 文件类型验证
            if (file.isEmpty()) {
                log.info("上传的文件为空");
            }

            String fileName = file.getOriginalFilename();
            String fileExtension = getFileExtension(fileName);

            if (!isSupportedFile(fileExtension)) {
                log.info("不支持的文件类型: {}", fileExtension);
            }

            log.info("开始处理文件: {}, 文件类型: {}, 用户名: {}", fileName, fileExtension, userName);
            OcrResponse response = m_OcrService.processFile(file);
            String content = response.getExtractedText();

            log.info("识别的文本内容{}",response.getExtractedText());
            String imageUrl = response.getImageUrls();

            //结构化处理ocr文本
            M_Certification mCertification = m_OcrService.structurizeOcrText(content,type,imageUrl,userName,school);
            log.info("结构化后的证书信息: {},username:{},id:{}", mCertification,userName,mCertification.getId());


            return ResponseEntity.ok(mCertification);

        } catch (Exception e) {
            log.error("OCR处理失败", e);
            return null;
        }
    }


    // 上传单个奖项文件并进行OCR识别和结构化处理
    @PostMapping(value = "/mpi/ocr/batchUpload", produces = "application/json;charset=UTF-8")
    public void batchUploadFile(@RequestParam("file") MultipartFile file, @RequestParam String type,@RequestParam String userName, @RequestParam String school) {
        try {
            // 文件类型验证
            if (file.isEmpty()) {
                log.info("上传的文件为空");
            }

            String fileName = file.getOriginalFilename();
            String fileExtension = getFileExtension(fileName);

            if (!isSupportedFile(fileExtension)) {
                log.info("不支持的文件类型: {}", fileExtension);
            }

            log.info("开始处理文件: {}, 文件类型: {}, 用户名: {}", fileName, fileExtension, userName);
            OcrResponse response = m_OcrService.batchProcessFile(file);


            log.info("识别的文本内容{}",response.getExtractedText());

            List<String> imageUrlList = response.getImageUrlList();
            List<String> pageTextList = response.getPageTexts();

            for (int i = 0; i < imageUrlList.size(); i++) {
                String pageText = pageTextList.get(i);
                String pageImageUrl = imageUrlList.get(i);
                //结构化处理ocr文本
                m_OcrService.batchStructurizeOcrText(pageText,type,pageImageUrl,userName,school);
            }
//
//            //结构化处理ocr文本
//            M_Certification mCertification = m_OcrService.structurizeOcrText(content,type,imageUrl,userName,school);
//            log.info("结构化后的证书信息: {},username:{},id:{}", mCertification,userName,mCertification.getId());




        } catch (Exception e) {
            log.error("OCR处理失败", e);

        }
    }



    // 上传单个奖项文件并进行OCR识别和结构化处理
    @PostMapping(value = "/mpi/ocr/teacherBatchUpload", produces = "application/json;charset=UTF-8")
    public void teacherBatchUploadFile(@RequestParam("file") MultipartFile file, @RequestParam String type,@RequestParam String userName, @RequestParam String school) {
        try {
            // 文件类型验证
            if (file.isEmpty()) {
                log.info("上传的文件为空");
            }

            String fileName = file.getOriginalFilename();
            String fileExtension = getFileExtension(fileName);

            if (!isSupportedFile(fileExtension)) {
                log.info("不支持的文件类型: {}", fileExtension);
            }

            log.info("开始处理文件: {}, 文件类型: {}, 用户名: {}", fileName, fileExtension, userName);
            OcrResponse response = m_OcrService.batchProcessFile(file);


            log.info("识别的文本内容{}",response.getExtractedText());

            List<String> imageUrlList = response.getImageUrlList();
            List<String> pageTextList = response.getPageTexts();

            for (int i = 0; i < imageUrlList.size(); i++) {
                String pageText = pageTextList.get(i);
                String pageImageUrl = imageUrlList.get(i);
                //结构化处理ocr文本
                m_OcrService.teacherBatchStructurizeOcrText(pageText,type,pageImageUrl,userName,school);
            }
//
//            //结构化处理ocr文本
//            M_Certification mCertification = m_OcrService.structurizeOcrText(content,type,imageUrl,userName,school);
//            log.info("结构化后的证书信息: {},username:{},id:{}", mCertification,userName,mCertification.getId());




        } catch (Exception e) {
            log.error("OCR处理失败", e);

        }
    }

    //获取文件扩展名
    private String getFileExtension(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            return "";
        }
        return fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
    }

    //检查是否是支持的文件类型
    private boolean isSupportedFile(String extension) {
        return Arrays.asList("png", "jpg", "jpeg", "pdf").contains(extension);
    }


    //上传修改表单信息，这里要判断是不是有id，如果没有id则是新增，有id则是修改
    @PostMapping(value = "/mpi/ocr/editCertification", produces = "application/json;charset=UTF-8")
    public void editCertification(@RequestBody M_Certification mCertification) {
        log.info("接收到的证书信息: {}", mCertification);
        m_OcrService.editCertification(mCertification);
    }

    @PostMapping(value = "/mpi/ocr/admineditCertification", produces = "application/json;charset=UTF-8")
    public void admineditCertification(@RequestBody M_Certification mCertification) {
        log.info("接收到的证书信息: {}", mCertification);
        m_OcrService.admineditCertification(mCertification);
    }


    //查询所有的证书信息，根据用户姓名
    @PostMapping(value = "/mpi/ocr/getCertificationsByUserName", produces = "application/json;charset=UTF-8")
    public List<M_Certification> getCertificationsByUserName(@RequestBody M_CertificationDTO mCertificationDTO) {
        log.info("查询证书信息，用户名: {}", mCertificationDTO.getUserName());
        return m_OcrService.getCertificationsByUserName(mCertificationDTO);
    }

    //管理员查询所有的证书信息
    @PostMapping(value = "/mpi/ocr/getAllCertifications", produces = "application/json;charset=UTF-8")
    public List<M_Certification> getAllCertifications(@RequestBody M_CertificationDTO mCertificationDTO) {
        String school = mCertificationDTO.getSchool();

        List<M_Certification> certifications = m_CertificationMapper.getAllCertifications(school);
        return certifications;
    }


    // 单纯上传图片或PDF并转换为图片，返回图片URL，注意多个url以;分隔
    @PostMapping(value = "/mpi/ocr/uploadImage",produces = "application/json;charset=UTF-8")
    public ResponseEntity<String> uploadImage(@RequestParam("file") MultipartFile file, Integer id) {
        try {
            // 文件类型验证
            if (file.isEmpty()) {
                log.info("上传的文件为空");
                return ResponseEntity.badRequest().body("上传的文件为空");
            }
            // 获取文件扩展名
            String fileName = file.getOriginalFilename();
            String fileExtension = getFileExtension(fileName);
            if (!Arrays.asList("png", "jpg", "jpeg","pdf").contains(fileExtension)) {
                log.info("不支持的类型: {}", fileExtension);
                return ResponseEntity.badRequest().body("不支持的类型: " + fileExtension);
            }
            //如果文件是图片，直接上传
            if (Arrays.asList("png","jpg","jpeg").contains(fileExtension)) {
                String imageUrl = aliOSSUtils.uploadImage(file.getBytes());

                //根据id查询原本的证书信息，更新图片url
                String imageNew = "";
                String imageUrlAppend = m_CertificationMapper.getImageUrlById(id);
                if (imageUrlAppend != null && !imageUrlAppend.isEmpty()) {
                    imageNew =  imageUrlAppend + ";" + imageUrl;
                }else {
                    imageNew = imageUrl;
                }
                //把新的图片url更新到证书信息中
                m_CertificationMapper.updateImageUrlById(id,imageNew);

                return ResponseEntity.ok(imageUrl);
            }else {
                //如果是pdf，则需要转化为图片后上传
                byte[] pdfBytes = file.getBytes();
                List<byte[]> images = new ArrayList<>();

                try (PDDocument document = PDDocument.load(pdfBytes)) {
                    PDFRenderer pdfRenderer = new PDFRenderer(document);

                    int pageCount = Math.min(document.getNumberOfPages(), 10); // 最多处理前10页

                    for (int page = 0; page < pageCount; page++) {
                        BufferedImage bufferedImage = pdfRenderer.renderImageWithDPI(page, 300);
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        ImageIO.write(bufferedImage, "png", baos);
                        images.add(baos.toByteArray());
                    }
                }
                String imageUrls = "";
                for (int i = 0; i < images.size(); i++) {
                    String imageUrl = aliOSSUtils.uploadCertification(images.get(i), "png", "image/png");
                    imageUrls += imageUrl;
                    if (i != images.size() - 1) {
                        imageUrls += ";";
                    }
                }
                //根据id查询原本的证书信息，更新图片url
                String imageNew = "";
                String imageUrlAppend = m_CertificationMapper.getImageUrlById(id);
                if (imageUrlAppend != null && !imageUrlAppend.isEmpty()) {
                    imageNew =  imageUrlAppend + ";" + imageUrls;
                }else {
                    imageNew = imageUrls;
                }
                //把新的图片url更新到证书信息中
                m_CertificationMapper.updateImageUrlById(id,imageNew);

                return ResponseEntity.ok(imageUrls);
            }

        } catch (Exception e) {
            log.error("图片上传失败", e);
            return ResponseEntity.status(500).body("图片上传失败: " + e.getMessage());
        }
    }



    //删除单张图片，根据id和图片url，注意如果有多个图片url以;分隔，这里要把对应的url删除掉
    @PostMapping(value = "/mpi/ocr/deleteImage",produces = "application/json;charset=UTF-8")
    public String deleteImage(@RequestBody M_ImageUrlDTO mImageUrlDTO) {
        log.info("删除图片请求，id: {}, 图片url: {}", mImageUrlDTO.getId(), mImageUrlDTO.getImageUrl());
        String resp = certificationService.deleteImageByIdAndUrl(mImageUrlDTO);
        return resp;

    }

    //删除单条证书记录，根据id
    @GetMapping(value = "/mpi/ocr/deleteCertificationById",produces = "application/json;charset=UTF-8")
    public String deleteCertificationById(Integer id){
        log.info("删除证书记录请求，id: {}", id);
        m_CertificationMapper.deleteById(id);
        return "删除成功";
    }

    //删除多条证书记录，根据id列表
    @PostMapping(value = "/mpi/ocr/deleteCertificationsByIds",produces = "application/json;charset=UTF-8")
    public String deleteCertificationsByIds(@RequestBody M_IdListDTO mIdListDTO){
        List<Integer> idList = mIdListDTO.getIdList();
        log.info("批量删除证书记录请求，ids: {}", idList);
        for(Integer id : idList){
            m_CertificationMapper.deleteById(id);
        }
        return "批量删除成功";
    }


    //根据前端传递的要求，导出word信息和图片压缩包
    //因为已经创设了响应头，所以这里不需要返回值，直接通过response输出流返回文件
    @GetMapping("/mpi/resume/exportAllResume")
    public void exportAllResume(@RequestParam String userName,@RequestParam String school, @RequestParam String primaryClassifications, @RequestParam String secondaryClassifications,@RequestParam String exportContent,@RequestParam String regionLevel,@RequestParam String awardLevel,@RequestParam String type,@RequestParam String personal,@RequestParam String awardTime,  HttpServletResponse response) {


        log.info("导出简历请求: {}", userName);
        M_ExportRequestDTO request = new M_ExportRequestDTO();
        request.setPrimaryClassifications(primaryClassifications);
        request.setSecondaryClassifications(secondaryClassifications);
        request.setExportContent(exportContent);
        request.setSchool(school);


        request.setRegionLevel(regionLevel);
        request.setAwardLevel(awardLevel);
        request.setType(type);
        request.setPersonal(personal);

        //如果awardTime是年份字符串，那么startDate设置为该年1月1日，endDate设置为该年12月31日；如果awardTime是日期字符串中间用波浪号拼接，则startDate和endDate都设置为该日期

        //如果awardTime 是2020前几个字，则也要解析
        if (awardTime.matches("\\d{4}")) {
            request.setStartDate(LocalDateTime.of(Integer.parseInt(awardTime), 1, 1, 0, 0).toLocalDate());
            request.setEndDate(LocalDateTime.of(Integer.parseInt(awardTime), 12, 31, 23, 59).toLocalDate());
        } else if (awardTime.contains("~")) {
            String[] dates = awardTime.split("~");
            if (dates.length == 2) {
                request.setStartDate(LocalDate.parse(dates[0].trim()));
                request.setEndDate(LocalDate.parse(dates[1].trim()));
            }
        } else if(awardTime.matches("2020前.*")) {
            // 默认设置为一个较大的时间范围，或者根据实际需求进行处理
            request.setStartDate(LocalDate.of(1900, 1, 1));
            request.setEndDate(LocalDate.of(2019, 12, 31));
        }else {
            // 默认设置为一个较大的时间范围，或者根据实际需求进行处理
            request.setStartDate(LocalDate.of(1900, 1, 1));
            request.setEndDate(LocalDate.of(2100, 12, 31));
        }


        // 在调用服务之前设置响应头
        if(exportContent.equals("word")){
            response.setContentType("application/vnd.openxmlformats-officedocument.wordprocessingml.document");
        }else {
            response.setContentType("application/zip");}
        setupResponse(response, request.getUserName(),request);
        // 然后调用导出服务
        resumeExportService.exportSingleResume(request, response);
        log.info("导出简历完成");
    }



    //根据前端传递的要求，导出word信息和图片压缩包
    //因为已经创设了响应头，所以这里不需要返回值，直接通过response输出流返回文件
    @GetMapping("/mpi/resume/exportSingleResume")
    public void exportResume(@RequestParam String userName,@RequestParam String school, @RequestParam String primaryClassifications, @RequestParam String secondaryClassifications,@RequestParam String exportContent,@RequestParam String regionLevel,@RequestParam String awardLevel,@RequestParam String type,@RequestParam String personal,@RequestParam String awardTime, HttpServletResponse response) {


        log.info("导出简历请求: {}", userName);
        M_ExportRequestDTO request = new M_ExportRequestDTO();
        request.setUserName(userName);
        request.setPrimaryClassifications(primaryClassifications);
        request.setSecondaryClassifications(secondaryClassifications);
        request.setExportContent(exportContent);
        request.setSchool(school);

        request.setRegionLevel(regionLevel);
        request.setAwardLevel(awardLevel);
        request.setType(type);
        request.setPersonal(personal);

        //如果awardTime是年份字符串，那么startDate设置为该年1月1日，endDate设置为该年12月31日；如果awardTime是日期字符串中间用波浪号拼接，则startDate和endDate都设置为该日期

        if (awardTime.matches("\\d{4}")) {
            request.setStartDate(LocalDateTime.of(Integer.parseInt(awardTime), 1, 1, 0, 0).toLocalDate());
            request.setEndDate(LocalDateTime.of(Integer.parseInt(awardTime), 12, 31, 23, 59).toLocalDate());
        } else if (awardTime.contains("~")) {
            String[] dates = awardTime.split("~");
            if (dates.length == 2) {
                request.setStartDate(LocalDate.parse(dates[0].trim()));
                request.setEndDate(LocalDate.parse(dates[1].trim()));
            }
        } else if(awardTime.matches("2020前.*")) {
            // 默认设置为一个较大的时间范围，或者根据实际需求进行处理
            request.setStartDate(LocalDate.of(1900, 1, 1));
            request.setEndDate(LocalDate.of(2019, 12, 31));
        }else {
            // 默认设置为一个较大的时间范围，或者根据实际需求进行处理
            request.setStartDate(LocalDate.of(1900, 1, 1));
            request.setEndDate(LocalDate.of(2100, 12, 31));
        }
        // 在调用服务之前设置响应头
        if(exportContent.equals("word")){
            response.setContentType("application/vnd.openxmlformats-officedocument.wordprocessingml.document");
        }else {
            response.setContentType("application/zip");}
        setupResponse(response, request.getUserName(),request);
        // 然后调用导出服务
        resumeExportService.exportSingleResume(request, response);
        log.info("导出简历完成");
    }

    // 设置HTTP响应头以支持文件下载
    private void setupResponse(HttpServletResponse response, String userName,M_ExportRequestDTO request) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String filename = userName + "_word和图片压缩包合集_" + timestamp + ".zip";

        try {
            filename = URLEncoder.encode(filename, "UTF-8").replaceAll("\\+", "%20");
        } catch (UnsupportedEncodingException e) {
            log.warn("文件名编码失败", e);
        }

//        response.setContentType("application/zip");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + filename);
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Expires", "0");
    }



    //标记重复，根据id列表，标记这些记录为重复
    @PostMapping(value = "/mpi/ocr/markDuplicate",produces = "application/json;charset=UTF-8")
    public String markDuplicate(@RequestBody M_IdListDTO mIdListDTO) {
        List<Integer> idList = mIdListDTO.getIdList();
        log.info("标记重复请求，ids: {}", idList);
        String resp = certificationService.markDuplicateByIds(idList);
        return resp;
    }

    //取消标记重复，根据id列表，取消标记这些记录为重复
    @PostMapping(value = "/mpi/ocr/unmarkDuplicate",produces = "application/json;charset=UTF-8")
    public String unmarkDuplicate(@RequestBody M_IdListDTO mIdListDTO) {
        List<Integer> idList = mIdListDTO.getIdList();
        log.info("取消标记重复请求，ids: {}", idList);
        String resp = certificationService.unmarkDuplicateByIds(idList);
        return resp;
    }




}



