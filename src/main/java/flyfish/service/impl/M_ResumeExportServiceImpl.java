package flyfish.service.impl;

import flyfish.mapper.M_CertificationMapper;
import flyfish.pojo.M_Certification;
import flyfish.pojo.M_ExportRequestDTO;
import flyfish.service.M_ResumeExportService;
import flyfish.service.M_ResumeImageExportService;
import flyfish.service.M_ResumeWordExportService;
import flyfish.service.M_ResumeZipExportService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;


@Service
@Slf4j
public class M_ResumeExportServiceImpl implements M_ResumeExportService {
    @Autowired
    private M_CertificationMapper certificationMapper;
    @Autowired
    private M_ResumeWordExportService wordExportService;
    @Autowired
    private M_ResumeImageExportService imageExportService;
    @Autowired
    private M_ResumeZipExportService zipExportService;


    // 导出单个简历
    public void exportSingleResume(M_ExportRequestDTO request, HttpServletResponse response) {
        try {
            // 1. 查询数据

            List<M_Certification> certifications = new ArrayList<>();
            if(request.getUserName() == null || request.getUserName().isEmpty()){
//                certifications = certificationMapper.getAllCertifications(request.getSchool());
                certifications = certificationMapper.getAllCertificationsByItems(request);
                List<M_Certification> newCertificationList = new ArrayList<>();

                for(M_Certification certification : certifications) {
                    //如果judgeRepeat为“否”，则直接添加到新的列表中

                    if(certification.getJudgeRepeat().equals("否")){
                        newCertificationList.add(certification);}
                    else {
                        if(certification.getId().equals(certification.getRepeatId())){

                            List<String> userNameList = certificationMapper.getTeacherNameListByRepeatId(certification.getRepeatId());
                            String userNames = String.join(";", userNameList);
                            certification.setTeacherName(userNames);
                            newCertificationList.add(certification);
                        }
                    }
                    //如果judgeRepeat为“是”，则只保留id和repeatId相同的记录到列表中，其他的不添加
                }
                certifications = newCertificationList;

            }else {
                certifications = certificationMapper.getAllCertificationsByItems(request);
//                certifications = certificationMapper.getCertificationsByUserName(request.getUserName(), request.getSchool());
            }


            if (certifications == null || certifications.isEmpty()) {
                log.warn("用户 {} 没有找到证书信息", request.getUserName());
                // 可以返回错误信息或者空文件
                return;
            }
            // 2. 数据分类和排序,拿到分类整理后的数据
            Map<String, Object> classifiedData = classifyAndSortData(certifications, request);

            String exportContent = request.getExportContent();

            exportResult(exportContent,request,response,certifications,classifiedData);


            } catch (Exception e) {
            log.error("导出简历失败", e);

        }

    }

    private void exportResult(String exportContent, M_ExportRequestDTO request, HttpServletResponse response, List<M_Certification> certifications, Map<String, Object> classifiedData) {
        try {
            if (exportContent.equals("word")) {
                if(request.getUserName() == null || request.getUserName().isEmpty()){
                    request.setUserName("全校");
                }
                // 3. 生成Word文档
                byte[] wordBytes = wordExportService.generateWordDocument(classifiedData, request.getUserName());

                String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));

                String filename = request.getUserName() + "_简历文本_" + timestamp + ".docx";

                try {
                    filename = URLEncoder.encode(filename, "UTF-8").replaceAll("\\+", "%20");
                } catch (UnsupportedEncodingException e) {
                    log.warn("文件名编码失败", e);
                }
                // 4. 将Word文档直接写入response的输出流
                response.setContentType("application/vnd.openxmlformats-officedocument.wordprocessingml.document");
                response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");
                response.getOutputStream().write(wordBytes);
                response.getOutputStream().flush();
            } else if (exportContent.equals("images")) {
                if(request.getUserName() == null || request.getUserName().isEmpty()){
                    request.setUserName("全校");
                }
                // 3. 生成图片压缩包
                byte[] imageZipBytes = imageExportService.generateImageZip(certifications);

                String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));

                String filename = request.getUserName() + "_证书图片压缩包_" + timestamp + ".zip";

                try {
                    filename = URLEncoder.encode(filename, "UTF-8").replaceAll("\\+", "%20");
                } catch (UnsupportedEncodingException e) {
                    log.warn("文件名编码失败", e);
                }
                // 4. 将图片压缩包直接写入response的输出流
                response.setContentType("application/zip");
                response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");
                response.getOutputStream().write(imageZipBytes);
                response.getOutputStream().flush();
            }else if (exportContent.equals("all")){
                if(request.getUserName() == null || request.getUserName().isEmpty()){
                    request.setUserName("全校");
                }
                // 3. 生成Word文档
                byte[] wordBytes = wordExportService.generateWordDocument(classifiedData, request.getUserName());

                // 4. 生成图片压缩包
                byte[] imageZipBytes = imageExportService.generateImageZip(certifications);

                String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));

                String filename = request.getUserName() + "_简历word和图片压缩包_" + timestamp + ".zip";

                try {
                    filename = URLEncoder.encode(filename, "UTF-8").replaceAll("\\+", "%20");
                } catch (UnsupportedEncodingException e) {
                    log.warn("文件名编码失败", e);
                }
                // 5. 创建包含Word和图片的总压缩包，直接写入response的输出流
                zipExportService.createFinalZip(response.getOutputStream(), wordBytes, imageZipBytes, request.getUserName(),filename);
            } else {
                log.warn("未知的导出内容类型: {}", exportContent);
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write("未知的导出内容类型: " + exportContent);
            }
        } catch (IOException e) {
            log.error("导出结果写入响应失败", e);
            throw new RuntimeException("导出结果失败: " + e.getMessage());
        }
    }

    // 数据分类和排序的实现
    private Map<String, Object> classifyAndSortData(List<M_Certification> certifications, M_ExportRequestDTO request) {
        // 先对原始数据按获奖时间从近到远排序（默认排序规则）
        certifications.sort((c1, c2) -> {
            LocalDate date1 = c1.getAwardTime();
            LocalDate date2 = c2.getAwardTime();

            // 处理空值情况
            if (date1 == null && date2 == null) return 0;
            if (date1 == null) return 1; // 空值放后面
            if (date2 == null) return -1; // 空值放后面

            // 从近到远排序（最近的在前）
            return date2.compareTo(date1);
        });

        // 获取分类字段，如果没有选择则默认为awardTime
        String primaryField = (request.getPrimaryClassifications() != null &&
                !request.getPrimaryClassifications().isEmpty())
                ? request.getPrimaryClassifications()
                : "awardTime";

        // 第一级分类
        Map<String, List<M_Certification>> primaryGroups = groupByFieldWithOrder(certifications, primaryField);

        // 第二级分类和排序
        Map<String, Object> result = new LinkedHashMap<>();

        for (Map.Entry<String, List<M_Certification>> entry : primaryGroups.entrySet()) {
            String primaryKey = entry.getKey();
            List<M_Certification> primaryGroup = entry.getValue();

            if (request.getSecondaryClassifications() != null &&
                    !request.getSecondaryClassifications().isEmpty()) {
                // 有第二级分类：先按第二级分类，然后在第二级内按时间从近到远排序

                // 第二级分类
                Map<String, List<M_Certification>> secondaryGroups =
                        groupByFieldWithOrder(primaryGroup, request.getSecondaryClassifications());

                // 对第二级分组内的数据进行时间排序
                for (Map.Entry<String, List<M_Certification>> secondaryEntry : secondaryGroups.entrySet()) {
                    List<M_Certification> secondaryList = secondaryEntry.getValue();

                    // 按时间从近到远排序
                    secondaryList.sort((c1, c2) -> {
                        LocalDate date1 = c1.getAwardTime();
                        LocalDate date2 = c2.getAwardTime();

                        if (date1 == null && date2 == null) return 0;
                        if (date1 == null) return 1;
                        if (date2 == null) return -1;

                        return date2.compareTo(date1);
                    });
                }

                result.put(primaryKey, secondaryGroups);
            } else {
                // 没有第二级分类：第一级分组内按时间从近到远排序
                // 由于已经排序过，这里可以不用再排序
                result.put(primaryKey, primaryGroup);
            }
        }

        return result;
    }

    // 根据指定字段进行分组，并按照特定顺序排序
    private Map<String, List<M_Certification>> groupByFieldWithOrder(
            List<M_Certification> certifications, String field) {

        // 先进行分组
        Map<String, List<M_Certification>> groups = certifications.stream()
                .collect(Collectors.groupingBy(cert -> {
                    switch (field) {
                        case "type":
                            return cert.getType() != null ? cert.getType() : "未分类";
                        case "regionLevel":
                            return cert.getRegionLevel() != null ? cert.getRegionLevel() : "未分级";
                        case "awardLevel":
                            return cert.getAwardLevel() != null ? cert.getAwardLevel() : "未评级";
                        case "personal":
                            return "个人".equals(cert.getPersonal()) ? "个人" : "团体";
                        case "organization":
                            return cert.getOrganization() != null ? cert.getOrganization() : "未知单位";
                        case "awardTime":
                            return cert.getAwardTime() != null ?
                                    String.valueOf(cert.getAwardTime().getYear()) : "未知年份";
                        default:
                            return "其他";
                    }
                }));

        // 根据字段类型对分组进行排序
        return orderGroupsByField(groups, field);
    }

    // 根据字段类型对分组进行排序
    private Map<String, List<M_Certification>> orderGroupsByField(
            Map<String, List<M_Certification>> groups, String field) {

        // 创建有序的LinkedHashMap
        Map<String, List<M_Certification>> orderedMap = new LinkedHashMap<>();

        switch (field) {
            case "regionLevel":
                // 按照国家级、省级、市级、区级、校级顺序
                String[] regionOrder = {"国家级", "省级", "市级", "区级", "校级"};
                for (String region : regionOrder) {
                    if (groups.containsKey(region)) {
                        orderedMap.put(region, groups.get(region));
                    }
                }
                // 添加其他不在预设顺序中的regionLevel
                groups.keySet().stream()
                        .filter(key -> !Arrays.asList(regionOrder).contains(key))
                        .sorted()
                        .forEach(key -> orderedMap.put(key, groups.get(key)));
                break;

            case "awardLevel":
                // 按照一等奖、二等奖、三等奖、优秀奖、其他顺序
                String[] awardOrder = {"一等奖", "二等奖", "三等奖", "优秀奖", "其他"};
                for (String award : awardOrder) {
                    if (groups.containsKey(award)) {
                        orderedMap.put(award, groups.get(award));
                    }
                }
                // 添加其他不在预设顺序中的awardLevel
                groups.keySet().stream()
                        .filter(key -> !Arrays.asList(awardOrder).contains(key))
                        .sorted()
                        .forEach(key -> orderedMap.put(key, groups.get(key)));
                break;

            case "personal":
                // 先个人再团体
                if (groups.containsKey("个人")) {
                    orderedMap.put("个人", groups.get("个人"));
                }
                if (groups.containsKey("团体")) {
                    orderedMap.put("团体", groups.get("团体"));
                }
                // 添加其他
                groups.keySet().stream()
                        .filter(key -> !"个人".equals(key) && !"团体".equals(key))
                        .sorted()
                        .forEach(key -> orderedMap.put(key, groups.get(key)));
                break;

            case "tpye":
                // 先荣誉称号、再现场比赛、再非现场比赛、再公开课和讲座、再课题、再论文发表、再著作和校本课程
                String[] typeOrder = {
                        "荣誉称号", "现场比赛", "非现场比赛",
                        "公开课和讲座", "课题", "论文发表", "著作和校本课程"
                };
                for (String type : typeOrder) {
                    if (groups.containsKey(type)) {
                        orderedMap.put(type, groups.get(type));
                    }
                }
                // 添加其他不在预设顺序中的type
                groups.keySet().stream()
                        .filter(key -> !Arrays.asList(typeOrder).contains(key))
                        .sorted()
                        .forEach(key -> orderedMap.put(key, groups.get(key)));
                break;

            case "awardTime":
                // 按年份从近到远排序
                groups.entrySet().stream()
                        .sorted((e1, e2) -> {
                            if ("未知年份".equals(e1.getKey())) return 1;
                            if ("未知年份".equals(e2.getKey())) return -1;

                            try {
                                int year1 = Integer.parseInt(e1.getKey());
                                int year2 = Integer.parseInt(e2.getKey());
                                return Integer.compare(year2, year1); // 降序，从近到远
                            } catch (NumberFormatException e) {
                                return e1.getKey().compareTo(e2.getKey());
                            }
                        })
                        .forEach(entry -> orderedMap.put(entry.getKey(), entry.getValue()));
                break;

            default:
                // 其他字段按自然顺序排序
                groups.entrySet().stream()
                        .sorted(Map.Entry.comparingByKey())
                        .forEach(entry -> orderedMap.put(entry.getKey(), entry.getValue()));
                break;
        }

        return orderedMap;
    }

}
