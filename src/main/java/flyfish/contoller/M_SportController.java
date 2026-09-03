package flyfish.contoller;

import flyfish.pojo.DTO.M_ReadGradeFeedDTO;
import flyfish.pojo.DTO.M_ReadingThreeSituationDTO;
import flyfish.pojo.DTO.M_SportFourSituationDTO;
import flyfish.pojo.VO.M_ClassCountVO;
import flyfish.service.M_ReadingService;
import flyfish.service.M_SportService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@Slf4j
public class M_SportController {
    @Autowired
    private M_SportService mSportService;
    @Autowired
    private M_ReadingService mReadingService;


    //根据学校和日期生成反馈报告
    @GetMapping(value = "/mpi/sport/getFeedbackReport" , produces = "application/json;charset=UTF-8")
    public String getFeedbackReport(String school, LocalDate checkDate){
        log.info("生成反馈报告的参数是{}{}",school,checkDate);
        String resp = mSportService.getFeedbackReport(school,checkDate);
        return resp;
    }

    //将登记早操情况记录反馈给后台处理
    @PostMapping(value = "/mpi/sport/recordSport", produces = "application/json;charset=UTF-8")
    public String recordSport(@RequestBody M_SportFourSituationDTO sportData){

        log.info("登记早操情况的参数是{}",sportData);
        List<M_ReadGradeFeedDTO> feedDTOS = preprocessData(sportData);

        String resp = "";

        if(sportData.getLabel().equals("gymPraise")){
            resp += mSportService.recordSport(feedDTOS.get(0));
        } else if ( sportData.getLabel().equals("gymCriticize")){
            resp += mSportService.recordSport(feedDTOS.get(1));
        } else if ( sportData.getLabel().equals("runPraise")){
            resp += mSportService.recordSport(feedDTOS.get(2));

        }else if(sportData.getLabel().equals("runCriticize")){
            resp += mSportService.recordSport(feedDTOS.get(3));
        }

        //nullData是没有任何数据返回；noRevise是没有修改，grade1success是一年级修改成功
        return resp;
    }

    //把singleSportRecord表中的数据整理之后发给前端，反馈当日数据
    @GetMapping("/mpi/sport/getSelectedSport")
    public M_SportFourSituationDTO getSingleReadFeedback(String school, LocalDate checkDate){
        log.info("获取单次早操反馈的参数是{}{}",school,checkDate);
        M_SportFourSituationDTO mSportFourSituationDTO =  mSportService.getSingleSportFeedback(school,checkDate);
        return mSportFourSituationDTO;
    }

    //根据起止日期和学校 返回给前端班级数量列表，以便展示柱状图
    @GetMapping("/mpi/reading/getClassCount")
    public Map<String,List<M_ClassCountVO>> getClassCount(LocalDate startDate, LocalDate endDate, String school,String type, Integer topNum, String timeZone,Integer praiseAddScore,Integer criticizeSubScore){
        log.info("获取全校MAx柱形图的参数是{}{}{}{}{}{}{}{}",startDate,endDate,school,type,topNum,timeZone,praiseAddScore,criticizeSubScore);
        List<String> timeZoneList = List.of(timeZone.split(","));
        Map<String,List<M_ClassCountVO>> classCountList = mReadingService.getClassCount(startDate,endDate,school,type,topNum,timeZoneList,praiseAddScore,criticizeSubScore);
        return classCountList;
    }

    @GetMapping("/mpi/reading/getLabelCount")
    public Map<String,Integer> getLabelCount(LocalDate startDate, LocalDate endDate, String school,String type,String timeZone){
        log.info("获取全校班级数量的参数是{}{}{}{}{}",startDate,endDate,school,type,timeZone);
        List<String> timeZoneList = List.of(timeZone.split(","));
        Map<String,Integer> classCountList = mReadingService.getLabelCount(startDate,endDate,school,type,timeZoneList);
        return classCountList;
    }


    //处理前端的数据，返回给定格式的数据
    private List<M_ReadGradeFeedDTO> preprocessData(M_SportFourSituationDTO sportData) {
        List<M_ReadGradeFeedDTO> feedDTO = new ArrayList<>();
        M_ReadGradeFeedDTO gymPraisefeedDTO = new M_ReadGradeFeedDTO();
        M_ReadGradeFeedDTO gymCriticizefeedDTO = new M_ReadGradeFeedDTO();
        M_ReadGradeFeedDTO runPraisefeedDTO = new M_ReadGradeFeedDTO();
        M_ReadGradeFeedDTO runCriticizefeedDTO = new M_ReadGradeFeedDTO();


        // 设置 school 和 checkDate
        gymPraisefeedDTO .setSchool(sportData.getSchool());
        gymPraisefeedDTO .setCheckDate(sportData.getCheckDate());
        gymPraisefeedDTO .setTimeZone("体操表扬");
        gymPraisefeedDTO .setGrade(sportData.getGrade());

        gymCriticizefeedDTO .setSchool(sportData.getSchool());
        gymCriticizefeedDTO .setCheckDate(sportData.getCheckDate());
        gymCriticizefeedDTO .setTimeZone("体操批评");
        gymCriticizefeedDTO .setGrade(sportData.getGrade());

        runPraisefeedDTO .setSchool(sportData.getSchool());
        runPraisefeedDTO .setCheckDate(sportData.getCheckDate());
        runPraisefeedDTO .setTimeZone("跑操表扬");
        runPraisefeedDTO .setGrade(sportData.getGrade());

        runCriticizefeedDTO .setSchool(sportData.getSchool());
        runCriticizefeedDTO .setCheckDate(sportData.getCheckDate());
        runCriticizefeedDTO .setTimeZone("跑操批评");
        runCriticizefeedDTO .setGrade(sportData.getGrade());



        // 遍历 gymPraiseClassNameList
        for (String className : sportData.getGymPraiseClassNameList()) {
            if (className != null && !className.isEmpty()) {
                // 获取班级名称的第一个字符
                char firstChar = className.charAt(0);

                // 根据第一个字符将班级分类存储
                switch (firstChar) {
                    case '一':
                        if (gymPraisefeedDTO.getGrade1ClassList() == null) {
                            gymPraisefeedDTO.setGrade1ClassList(new ArrayList<>());
                        }
                        gymPraisefeedDTO.getGrade1ClassList().add(className);
                        break;
                    case '二':
                        if (gymPraisefeedDTO.getGrade2ClassList() == null) {
                            gymPraisefeedDTO.setGrade2ClassList(new ArrayList<>());
                        }
                        gymPraisefeedDTO.getGrade2ClassList().add(className);
                        break;
                    case '三':
                        if (gymPraisefeedDTO.getGrade3ClassList() == null) {
                            gymPraisefeedDTO.setGrade3ClassList(new ArrayList<>());
                        }
                        gymPraisefeedDTO.getGrade3ClassList().add(className);
                        break;
                    case '四':
                        if (gymPraisefeedDTO.getGrade4ClassList() == null) {
                            gymPraisefeedDTO.setGrade4ClassList(new ArrayList<>());
                        }
                        gymPraisefeedDTO.getGrade4ClassList().add(className);
                        break;
                    case '五':
                        if (gymPraisefeedDTO.getGrade5ClassList() == null) {
                            gymPraisefeedDTO.setGrade5ClassList(new ArrayList<>());
                        }
                        gymPraisefeedDTO.getGrade5ClassList().add(className);
                        break;
                    case '六':
                        if (gymPraisefeedDTO.getGrade6ClassList() == null) {
                            gymPraisefeedDTO.setGrade6ClassList(new ArrayList<>());
                        }
                        gymPraisefeedDTO.getGrade6ClassList().add(className);
                        break;
                    case '七':
                        if (gymPraisefeedDTO.getGrade7ClassList() == null) {
                            gymPraisefeedDTO.setGrade7ClassList(new ArrayList<>());
                        }
                        gymPraisefeedDTO.getGrade7ClassList().add(className);
                        break;
                    case '八':
                        if (gymPraisefeedDTO.getGrade8ClassList() == null) {
                            gymPraisefeedDTO.setGrade8ClassList(new ArrayList<>());
                        }
                        gymPraisefeedDTO.getGrade8ClassList().add(className);
                        break;
                    case '九':
                        if (gymPraisefeedDTO.getGrade9ClassList() == null) {
                            gymPraisefeedDTO.setGrade9ClassList(new ArrayList<>());
                        }
                        gymPraisefeedDTO.getGrade9ClassList().add(className);
                        break;
                    default:
                        // 如果班级名称的第一个字符不是一到九，可以选择忽略或存储到其他列表
                        break;
                }
            }
        }

        // 遍历 gymCriticizeClassNameList
        for (String className : sportData.getGymCriticizeClassNameList()) {
            if (className != null && !className.isEmpty()) {
                // 获取班级名称的第一个字符
                char firstChar = className.charAt(0);

                // 根据第一个字符将班级分类存储
                switch (firstChar) {
                    case '一':
                        if (gymCriticizefeedDTO.getGrade1ClassList() == null) {
                            gymCriticizefeedDTO.setGrade1ClassList(new ArrayList<>());
                        }
                        gymCriticizefeedDTO.getGrade1ClassList().add(className);
                        break;
                    case '二':
                        if (gymCriticizefeedDTO.getGrade2ClassList() == null) {
                            gymCriticizefeedDTO.setGrade2ClassList(new ArrayList<>());
                        }
                        gymCriticizefeedDTO.getGrade2ClassList().add(className);
                        break;
                    case '三':
                        if (gymCriticizefeedDTO.getGrade3ClassList() == null) {
                            gymCriticizefeedDTO.setGrade3ClassList(new ArrayList<>());
                        }
                        gymCriticizefeedDTO.getGrade3ClassList().add(className);
                        break;
                    case '四':
                        if (gymCriticizefeedDTO.getGrade4ClassList() == null) {
                            gymCriticizefeedDTO.setGrade4ClassList(new ArrayList<>());
                        }
                        gymCriticizefeedDTO.getGrade4ClassList().add(className);
                        break;
                    case '五':
                        if (gymCriticizefeedDTO.getGrade5ClassList() == null) {
                            gymCriticizefeedDTO.setGrade5ClassList(new ArrayList<>());
                        }
                        gymCriticizefeedDTO.getGrade5ClassList().add(className);
                        break;
                    case '六':
                        if (gymCriticizefeedDTO.getGrade6ClassList() == null) {
                            gymCriticizefeedDTO.setGrade6ClassList(new ArrayList<>());
                        }
                        gymCriticizefeedDTO.getGrade6ClassList().add(className);
                        break;
                    case '七':
                        if (gymCriticizefeedDTO.getGrade7ClassList() == null) {
                            gymCriticizefeedDTO.setGrade7ClassList(new ArrayList<>());
                        }
                        gymCriticizefeedDTO.getGrade7ClassList().add(className);
                        break;
                    case '八':
                        if (gymCriticizefeedDTO.getGrade8ClassList() == null) {
                            gymCriticizefeedDTO.setGrade8ClassList(new ArrayList<>());
                        }
                        gymCriticizefeedDTO.getGrade8ClassList().add(className);
                        break;
                    case '九':
                        if (gymCriticizefeedDTO.getGrade9ClassList() == null) {
                            gymCriticizefeedDTO.setGrade9ClassList(new ArrayList<>());
                        }
                        gymCriticizefeedDTO.getGrade9ClassList().add(className);
                        break;
                    default:
                        // 如果班级名称的第一个字符不是一到九，可以选择忽略或存储到其他列表
                        break;
                }
            }
        }

        // 遍历 runPraiseClassNameList
        for (String className : sportData.getRunPraiseClassNameList()) {
            if (className != null && !className.isEmpty()) {
                // 获取班级名称的第一个字符
                char firstChar = className.charAt(0);

                // 根据第一个字符将班级分类存储
                switch (firstChar) {
                    case '一':
                        if (runPraisefeedDTO.getGrade1ClassList() == null) {
                            runPraisefeedDTO.setGrade1ClassList(new ArrayList<>());
                        }
                        runPraisefeedDTO.getGrade1ClassList().add(className);
                        break;
                    case '二':
                        if (runPraisefeedDTO.getGrade2ClassList() == null) {
                            runPraisefeedDTO.setGrade2ClassList(new ArrayList<>());
                        }
                        runPraisefeedDTO.getGrade2ClassList().add(className);
                        break;
                    case '三':
                        if (runPraisefeedDTO.getGrade3ClassList() == null) {
                            runPraisefeedDTO.setGrade3ClassList(new ArrayList<>());
                        }
                        runPraisefeedDTO.getGrade3ClassList().add(className);
                        break;
                    case '四':
                        if (runPraisefeedDTO.getGrade4ClassList() == null) {
                            runPraisefeedDTO.setGrade4ClassList(new ArrayList<>());
                        }
                        runPraisefeedDTO.getGrade4ClassList().add(className);
                        break;
                    case '五':
                        if (runPraisefeedDTO.getGrade5ClassList() == null) {
                            runPraisefeedDTO.setGrade5ClassList(new ArrayList<>());
                        }
                        runPraisefeedDTO.getGrade5ClassList().add(className);
                        break;
                    case '六':
                        if (runPraisefeedDTO.getGrade6ClassList() == null) {
                            runPraisefeedDTO.setGrade6ClassList(new ArrayList<>());
                        }
                        runPraisefeedDTO.getGrade6ClassList().add(className);
                        break;
                    case '七':
                        if (runPraisefeedDTO.getGrade7ClassList() == null) {
                            runPraisefeedDTO.setGrade7ClassList(new ArrayList<>());
                        }
                        runPraisefeedDTO.getGrade7ClassList().add(className);
                        break;
                    case '八':
                        if (runPraisefeedDTO.getGrade8ClassList() == null) {
                            runPraisefeedDTO.setGrade8ClassList(new ArrayList<>());
                        }
                        runPraisefeedDTO.getGrade8ClassList().add(className);
                        break;
                    case '九':
                        if (runPraisefeedDTO.getGrade9ClassList() == null) {
                            runPraisefeedDTO.setGrade9ClassList(new ArrayList<>());
                        }
                        runPraisefeedDTO.getGrade9ClassList().add(className);
                        break;
                    default:
                        // 如果班级名称的第一个字符不是一到九，可以选择忽略或存储到其他列表
                        break;
                }
            }
        }

        // 遍历 runCriticizeClassNameList
        for (String className : sportData.getRunCriticizeClassNameList()) {
            if (className != null && !className.isEmpty()) {
                // 获取班级名称的第一个字符
                char firstChar = className.charAt(0);

                // 根据第一个字符将班级分类存储
                switch (firstChar) {
                    case '一':
                        if (runCriticizefeedDTO.getGrade1ClassList() == null) {
                            runCriticizefeedDTO.setGrade1ClassList(new ArrayList<>());
                        }
                        runCriticizefeedDTO.getGrade1ClassList().add(className);
                        break;
                    case '二':
                        if (runCriticizefeedDTO.getGrade2ClassList() == null) {
                            runCriticizefeedDTO.setGrade2ClassList(new ArrayList<>());
                        }
                        runCriticizefeedDTO.getGrade2ClassList().add(className);
                        break;
                    case '三':
                        if (runCriticizefeedDTO.getGrade3ClassList() == null) {
                            runCriticizefeedDTO.setGrade3ClassList(new ArrayList<>());
                        }
                        runCriticizefeedDTO.getGrade3ClassList().add(className);
                        break;
                    case '四':
                        if (runCriticizefeedDTO.getGrade4ClassList() == null) {
                            runCriticizefeedDTO.setGrade4ClassList(new ArrayList<>());
                        }
                        runCriticizefeedDTO.getGrade4ClassList().add(className);
                        break;
                    case '五':
                        if (runCriticizefeedDTO.getGrade5ClassList() == null) {
                            runCriticizefeedDTO.setGrade5ClassList(new ArrayList<>());
                        }
                        runCriticizefeedDTO.getGrade5ClassList().add(className);
                        break;
                    case '六':
                        if (runCriticizefeedDTO.getGrade6ClassList() == null) {
                            runCriticizefeedDTO.setGrade6ClassList(new ArrayList<>());
                        }
                        runCriticizefeedDTO.getGrade6ClassList().add(className);
                        break;
                    case '七':
                        if (runCriticizefeedDTO.getGrade7ClassList() == null) {
                            runCriticizefeedDTO.setGrade7ClassList(new ArrayList<>());
                        }
                        runCriticizefeedDTO.getGrade7ClassList().add(className);
                        break;
                    case '八':
                        if (runCriticizefeedDTO.getGrade8ClassList() == null) {
                            runCriticizefeedDTO.setGrade8ClassList(new ArrayList<>());
                        }
                        runCriticizefeedDTO.getGrade8ClassList().add(className);
                        break;
                    case '九':
                        if (runCriticizefeedDTO.getGrade9ClassList() == null) {
                            runCriticizefeedDTO.setGrade9ClassList(new ArrayList<>());
                        }
                        runCriticizefeedDTO.getGrade9ClassList().add(className);
                        break;
                    default:
                        // 如果班级名称的第一个字符不是一到九，可以选择忽略或存储到其他列表
                        break;
                }
            }
        }

        feedDTO.add(gymPraisefeedDTO);
        feedDTO.add(gymCriticizefeedDTO);
        feedDTO.add(runPraisefeedDTO);
        feedDTO.add(runCriticizefeedDTO);







        return feedDTO;

    }









}
