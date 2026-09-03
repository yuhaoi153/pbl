package flyfish.contoller;

import flyfish.constant.Template;
import flyfish.pojo.DTO.*;
import flyfish.pojo.M_SingleReadTeacher;
import flyfish.pojo.M_TeacherData;
import flyfish.pojo.VO.*;
import flyfish.service.M_ReadingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
@Slf4j
public class M_ReadingController {

    @Autowired
    private M_ReadingService mReadingService;

    //上传待改进情况记录
    @PostMapping(value = "/mpi/reading/poorPerform", produces = "application/json;charset=UTF-8")
    public String poorPerform(@RequestBody M_PoorPerformDTO poorPerformDTO){
        log.info("上传待改进情况记录的参数是{}",poorPerformDTO);
        String resp = mReadingService.poorPerform(poorPerformDTO);
        return resp;
    }


    //根据学校和周几获取早读巡查人员
    @GetMapping("/mpi/reading/getCheckNameList")
    public List<M_MoringReadingCheckVO> getReadingCheckNameList(String weekday,String school){
        log.info("获取早读巡查人员的参数是{}{}",weekday,school);
        return mReadingService.getReadingCheckList(weekday,school);
    }

    //根据学校获取班级名称
    @GetMapping("/mpi/reading/getClassNameList")
    public M_ClassNameListVO getClassNameList(String school){
        log.info("获取班级名称的参数是{}",school);
        return mReadingService.getClassNameList(school);
    }

    //将登记早读情况记录反馈给后台处理
    @PostMapping(value = "/mpi/reading/recordReading", produces = "application/json;charset=UTF-8")
    public String recordReading(@RequestBody M_ReadingThreeSituationDTO readingData){

        log.info("登记早读情况的参数是{}",readingData);
        List<M_ReadGradeFeedDTO> feedDTOS = preprocessData(readingData);

        String resp = "";

        if(readingData.getLabel().equals("manage")){
            resp += mReadingService.recordReading(feedDTOS.get(0));
        } else if ( readingData.getLabel().equals("reading")){
            resp += mReadingService.recordReading(feedDTOS.get(1));
        } else if ( readingData.getLabel().equals("prepare")){
            resp += mReadingService.recordReading(feedDTOS.get(2));

        }

        //nullData是没有任何数据返回；noRevise是没有修改，grade1success是一年级修改成功
        return resp;
    }

    //处理前端的数据，返回给定格式的数据
    private List<M_ReadGradeFeedDTO> preprocessData(M_ReadingThreeSituationDTO readingData) {
        List<M_ReadGradeFeedDTO> feedDTO = new ArrayList<>();
        M_ReadGradeFeedDTO managefeedDTO = new M_ReadGradeFeedDTO();
        M_ReadGradeFeedDTO readingfeedDTO = new M_ReadGradeFeedDTO();
        M_ReadGradeFeedDTO preparefeedDTO = new M_ReadGradeFeedDTO();

        // 设置 school 和 checkDate
        managefeedDTO.setSchool(readingData.getSchool());
        managefeedDTO.setCheckDate(readingData.getCheckDate());
        managefeedDTO.setTimeZone("自主");
        managefeedDTO.setGrade(readingData.getGrade());

        readingfeedDTO.setSchool(readingData.getSchool());
        readingfeedDTO.setCheckDate(readingData.getCheckDate());
        readingfeedDTO.setTimeZone("早读");
        readingfeedDTO.setGrade(readingData.getGrade());

        preparefeedDTO.setSchool(readingData.getSchool());
        preparefeedDTO.setCheckDate(readingData.getCheckDate());
        preparefeedDTO.setTimeZone("课前");
        preparefeedDTO.setGrade(readingData.getGrade());

        // 遍历 manageClassNameList
        for (String className : readingData.getManageClassNameList()) {
            if (className != null && !className.isEmpty()) {
                // 获取班级名称的第一个字符
                char firstChar = className.charAt(0);

                // 根据第一个字符将班级分类存储
                switch (firstChar) {
                    case '一':
                        if (managefeedDTO.getGrade1ClassList() == null) {
                            managefeedDTO.setGrade1ClassList(new ArrayList<>());
                        }
                        managefeedDTO.getGrade1ClassList().add(className);
                        break;
                    case '二':
                        if (managefeedDTO.getGrade2ClassList() == null) {
                            managefeedDTO.setGrade2ClassList(new ArrayList<>());
                        }
                        managefeedDTO.getGrade2ClassList().add(className);
                        break;
                    case '三':
                        if (managefeedDTO.getGrade3ClassList() == null) {
                            managefeedDTO.setGrade3ClassList(new ArrayList<>());
                        }
                        managefeedDTO.getGrade3ClassList().add(className);
                        break;
                    case '四':
                        if (managefeedDTO.getGrade4ClassList() == null) {
                            managefeedDTO.setGrade4ClassList(new ArrayList<>());
                        }
                        managefeedDTO.getGrade4ClassList().add(className);
                        break;
                    case '五':
                        if (managefeedDTO.getGrade5ClassList() == null) {
                            managefeedDTO.setGrade5ClassList(new ArrayList<>());
                        }
                        managefeedDTO.getGrade5ClassList().add(className);
                        break;
                    case '六':
                        if (managefeedDTO.getGrade6ClassList() == null) {
                            managefeedDTO.setGrade6ClassList(new ArrayList<>());
                        }
                        managefeedDTO.getGrade6ClassList().add(className);
                        break;
                    case '七':
                        if (managefeedDTO.getGrade7ClassList() == null) {
                            managefeedDTO.setGrade7ClassList(new ArrayList<>());
                        }
                        managefeedDTO.getGrade7ClassList().add(className);
                        break;
                    case '八':
                        if (managefeedDTO.getGrade8ClassList() == null) {
                            managefeedDTO.setGrade8ClassList(new ArrayList<>());
                        }
                        managefeedDTO.getGrade8ClassList().add(className);
                        break;
                    case '九':
                        if (managefeedDTO.getGrade9ClassList() == null) {
                            managefeedDTO.setGrade9ClassList(new ArrayList<>());
                        }
                        managefeedDTO.getGrade9ClassList().add(className);
                        break;
                    default:
                        // 如果班级名称的第一个字符不是一到九，可以选择忽略或存储到其他列表
                        break;
                }
            }
        }

        // 遍历 readingClassNameList
        for (String className : readingData.getReadingClassNameList()) {
            if (className != null && !className.isEmpty()) {
                // 获取班级名称的第一个字符
                char firstChar = className.charAt(0);

                // 根据第一个字符将班级分类存储
                switch (firstChar) {
                    case '一':
                        if (readingfeedDTO.getGrade1ClassList() == null) {
                            readingfeedDTO.setGrade1ClassList(new ArrayList<>());
                        }
                        readingfeedDTO.getGrade1ClassList().add(className);
                        break;
                    case '二':
                        if (readingfeedDTO.getGrade2ClassList() == null) {
                            readingfeedDTO.setGrade2ClassList(new ArrayList<>());
                        }
                        readingfeedDTO.getGrade2ClassList().add(className);
                        break;
                    case '三':
                        if (readingfeedDTO.getGrade3ClassList() == null) {
                            readingfeedDTO.setGrade3ClassList(new ArrayList<>());
                        }
                        readingfeedDTO.getGrade3ClassList().add(className);
                        break;
                    case '四':
                        if (readingfeedDTO.getGrade4ClassList() == null) {
                            readingfeedDTO.setGrade4ClassList(new ArrayList<>());
                        }
                        readingfeedDTO.getGrade4ClassList().add(className);
                        break;
                    case '五':
                        if (readingfeedDTO.getGrade5ClassList() == null) {
                            readingfeedDTO.setGrade5ClassList(new ArrayList<>());
                        }
                        readingfeedDTO.getGrade5ClassList().add(className);
                        break;
                    case '六':
                        if (readingfeedDTO.getGrade6ClassList() == null) {
                            readingfeedDTO.setGrade6ClassList(new ArrayList<>());
                        }
                        readingfeedDTO.getGrade6ClassList().add(className);
                        break;
                    case '七':
                        if (readingfeedDTO.getGrade7ClassList() == null) {
                            readingfeedDTO.setGrade7ClassList(new ArrayList<>());
                        }
                        readingfeedDTO.getGrade7ClassList().add(className);
                        break;
                    case '八':
                        if (readingfeedDTO.getGrade8ClassList() == null) {
                            readingfeedDTO.setGrade8ClassList(new ArrayList<>());
                        }
                        readingfeedDTO.getGrade8ClassList().add(className);
                        break;
                    case '九':
                        if (readingfeedDTO.getGrade9ClassList() == null) {
                            readingfeedDTO.setGrade9ClassList(new ArrayList<>());
                        }
                        readingfeedDTO.getGrade9ClassList().add(className);
                        break;
                    default:
                        // 如果班级名称的第一个字符不是一到九，可以选择忽略或存储到其他列表
                        break;
                }
            }
        }

        // 遍历 prepareClassNameList
        for (String className : readingData.getPrepareClassNameList()) {
            if (className != null && !className.isEmpty()) {
                // 获取班级名称的第一个字符
                char firstChar = className.charAt(0);

                // 根据第一个字符将班级分类存储
                switch (firstChar) {
                    case '一':
                        if (preparefeedDTO.getGrade1ClassList() == null) {
                            preparefeedDTO.setGrade1ClassList(new ArrayList<>());
                        }
                        preparefeedDTO.getGrade1ClassList().add(className);
                        break;
                    case '二':
                        if (preparefeedDTO.getGrade2ClassList() == null) {
                            preparefeedDTO.setGrade2ClassList(new ArrayList<>());
                        }
                        preparefeedDTO.getGrade2ClassList().add(className);
                        break;
                    case '三':
                        if (preparefeedDTO.getGrade3ClassList() == null) {
                            preparefeedDTO.setGrade3ClassList(new ArrayList<>());
                        }
                        preparefeedDTO.getGrade3ClassList().add(className);
                        break;
                    case '四':
                        if (preparefeedDTO.getGrade4ClassList() == null) {
                            preparefeedDTO.setGrade4ClassList(new ArrayList<>());
                        }
                        preparefeedDTO.getGrade4ClassList().add(className);
                        break;
                    case '五':
                        if (preparefeedDTO.getGrade5ClassList() == null) {
                            preparefeedDTO.setGrade5ClassList(new ArrayList<>());
                        }
                        preparefeedDTO.getGrade5ClassList().add(className);
                        break;
                    case '六':
                        if (preparefeedDTO.getGrade6ClassList() == null) {
                            preparefeedDTO.setGrade6ClassList(new ArrayList<>());
                        }
                        preparefeedDTO.getGrade6ClassList().add(className);
                        break;
                    case '七':
                        if (preparefeedDTO.getGrade7ClassList() == null) {
                            preparefeedDTO.setGrade7ClassList(new ArrayList<>());
                        }
                        preparefeedDTO.getGrade7ClassList().add(className);
                        break;
                    case '八':
                        if (preparefeedDTO.getGrade8ClassList() == null) {
                            preparefeedDTO.setGrade8ClassList(new ArrayList<>());
                        }
                        preparefeedDTO.getGrade8ClassList().add(className);
                        break;
                    case '九':
                        if (preparefeedDTO.getGrade9ClassList() == null) {
                            preparefeedDTO.setGrade9ClassList(new ArrayList<>());
                        }
                        preparefeedDTO.getGrade9ClassList().add(className);
                        break;
                    default:
                        // 如果班级名称的第一个字符不是一到九，可以选择忽略或存储到其他列表
                        break;
                }
            }



        }
        feedDTO.add(managefeedDTO);
        feedDTO.add(readingfeedDTO);
        feedDTO.add(preparefeedDTO);
        return feedDTO;

    }

    //将前端传递的教师excel表存入数据库
    @PostMapping(value = "/mpi/reading/teacherExcel", produces = "application/json;charset=UTF-8")
    public String teacherExcel (
            @RequestParam("file") MultipartFile file,  // 接收 Excel 文件
            @RequestParam("school") String school      // 接收学校名称
    ) throws IOException {
        log.info("将前端传递的教师excel表存入数据库的参数是{}", school);
        String result = mReadingService.updateTeacherData(file, school);
        return result;
    }

    //从网页端链接下载模板数据
    @GetMapping("/mpi/reading/downLoadTeacherExcel")
    public ResponseEntity<InputStreamResource> downloadTeacherExcel() {
        log.info("下载教师列表模板。。。");
        String fileUrl = Template.TEACHERLIST;

        try {
            URL url = new URL(fileUrl);
            InputStream inputStream = url.openStream();
            InputStreamResource resource = new InputStreamResource(inputStream);

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"teacherList.xlsx\"")
                    .body(resource);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/mpi/reading/downLoadClassTeacherExcel")
    public ResponseEntity<InputStreamResource> downloadClassTeacherExcel() {
        log.info("下载班级教师模板。。。");
        String fileUrl = Template.CLASSTEACHER;

        try {
            URL url = new URL(fileUrl);
            InputStream inputStream = url.openStream();
            InputStreamResource resource = new InputStreamResource(inputStream);

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"ClassTeacher.xlsx\"")
                    .body(resource);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.notFound().build();
        }
    }


    //将前端传递的班级绑定的教师excel表存入数据库
    @PostMapping(value = "/mpi/reading/ClassTeacherExcel" , produces = "application/json;charset=UTF-8")
    public String ClassteacherExcel(
            @RequestParam("file") MultipartFile file,  // 接收 Excel 文件
            @RequestParam("school") String school      // 接收学校名称
    ) throws IOException {
        log.info("将前端传递的班级绑定教师excel表存入数据库的参数是{}", school);

        String result = mReadingService.updateClassTeacherData(file, school);
        return result;
    }

    //把single_readfeedback表中的数据整理之后发给前端
    @GetMapping("/mpi/reading/getSelectedStatus")
    public M_ReadingThreeSituationDTO getSingleReadFeedback(String school, LocalDate checkDate){
        log.info("获取单次早读反馈的参数是{}{}{}",school,checkDate);
        M_ReadingThreeSituationDTO mReadingThreeSituationDTO =  mReadingService.getSingleReadFeedback(school,checkDate);
        return mReadingThreeSituationDTO;
    }

    //获取每个班级对应的教师数据
    @GetMapping("/mpi/reading/getTeacherData")
    public M_TeacherListMap getTeacherData(String school){
        log.info("获取每个班级对应的教师数据的参数是{}",school);
        M_TeacherListMap classTeacherMap =  mReadingService.getTeacherData(school);
        return classTeacherMap;
    }


    //将前端传递过来的教师反馈数据存入数据库
    @PostMapping(value = "/mpi/reading/recordTeacherFeedback" , produces = "application/json;charset=UTF-8")
    public String recordTeacherFeedback(@RequestBody M_ReadTeacherFeedDTO m_readTeacherFeedDTO){
        log.info("将前端传递过来的教师反馈数据存入数据库的参数是{}",m_readTeacherFeedDTO);
        String resp = mReadingService.recordTeacherFeedback(m_readTeacherFeedDTO);
        return resp;
    }

    //把singleReadTeacherRecord表中的数据整理之后发给前端
    @GetMapping("/mpi/reading/getSelectedTeacherStatus")
    public M_SingleReadTeacherVO getSelectedTeacherStatus(String school, LocalDate checkDate){
        log.info("获取单次早读反馈的参数是{}{}",school,checkDate);
        M_SingleReadTeacherVO mTeacherDataList =  mReadingService.getSelectedTeacherStatus(school,checkDate);
        return mTeacherDataList;
    }

    //新增班级和教师绑定关系
    @PostMapping(value = "/mpi/reading/addClassTeacherRelation", produces = "application/json;charset=UTF-8")
    public String addClassTeacherRelation(@RequestBody M_ClassTeacherMapDTO mClassTeacherMapDTO ){
        log.info("新增班级和教师绑定关系的参数是{}",mClassTeacherMapDTO);
        String resp = mReadingService.addClassTeacherRelation(mClassTeacherMapDTO);
        return resp;
    }

    //删除班级和教师绑定关系
    @PostMapping(value = "/mpi/reading/deleteClassTeacherRelation", produces = "application/json;charset=UTF-8")
    public String deleteClassTeacherRelation(@RequestBody M_ClassTeacherMapDTO mClassTeacherMapDTO ){
        log.info("删除班级和教师绑定关系的参数是{}",mClassTeacherMapDTO);
        String resp = mReadingService.deleteClassTeacherRelation(mClassTeacherMapDTO);
        return resp;
    }



    //根据学校和日期生成反馈报告
    @GetMapping(value = "/mpi/reading/getFeedbackReport" , produces = "application/json;charset=UTF-8")
    public String getFeedbackReport(String school, LocalDate checkDate){
        log.info("生成反馈报告的参数是{}{}",school,checkDate);
        String resp = mReadingService.getFeedbackReport(school,checkDate);
        return resp;
    }






}


