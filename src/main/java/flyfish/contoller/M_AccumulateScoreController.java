package flyfish.contoller;


import flyfish.mapper.M_GradeYearMapper;
import flyfish.pojo.DTO.M_AiPerformDTO;
import flyfish.pojo.DTO.ScoreDTO;
import flyfish.pojo.M_WellBadHomeworkPerform;
import flyfish.pojo.VO.AccumulateScoreVO;
import flyfish.pojo.VO.M_StudentNamePerformByDateVO;
import flyfish.service.AIService;
import flyfish.service.AccumulateScoreService;
import flyfish.service.M_WellBadPerformService;
import flyfish.utils.AliyunAudioRecognitionUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@RestController
@Slf4j
public class M_AccumulateScoreController {

    @Autowired
    private AccumulateScoreService accumulateScoreService;
    @Autowired
    private AIService aiService;
    @Autowired
    private AliyunAudioRecognitionUtil aliyunAudioRecognitionUtil;
    @Autowired
    private M_GradeYearMapper m_gradeYearMapper;
    @Autowired
    private M_WellBadPerformService wellBadPerformService;


    /**
     * 小程序查询积分
     * @param className
     * @param rankingType
     * @param startDate
     * @param endDate
     * @param school
     * @param subject
     * @return
     */
    @GetMapping("/mpi/homework/queryScore")
    public List<AccumulateScoreVO> queryAllScore(String className, String rankingType, LocalDate startDate, LocalDate endDate, String school, String subject) {
        log.info("小程序查询积分：{},{},{},{},{},{}", className, rankingType,startDate, endDate,school,subject);
        className = turnChineseClassToNumber(className);
        List<AccumulateScoreVO> accumulateScoreList = new ArrayList<>();
        if(rankingType.equals("学期总榜")) {
             accumulateScoreList = accumulateScoreService.queryAllScore(className,school,subject);
        }
        if(rankingType.equals("阶段作业榜")) {
            String type = "作业";
            accumulateScoreList = accumulateScoreService.queryPartScore(className,school,subject,startDate,endDate,type);
        }
        if(rankingType.equals("阶段课堂榜")) {
            String type = "课堂";
            accumulateScoreList = accumulateScoreService.queryPartScore(className,school,subject,startDate,endDate,type);
        }

        return accumulateScoreList;

    }

    /**
     * 查询某个学生的表现和积分情况
     * @param name
     * @param startDate
     * @param endDate
     * @param school
     * @param subject
     * @param className
     * @return
     */
    @GetMapping("/mpi/homework/queryScoreByName")
    public List<M_StudentNamePerformByDateVO> queryScoreByName(String name, LocalDate startDate, LocalDate endDate, String school, String subject, String className) {
        log.info("小程序查询个人积分：{},{},{},{},{},{}", name,startDate, endDate,school,subject,className);
        className = turnChineseClassToNumber(className);
        List<M_StudentNamePerformByDateVO> scoreList = accumulateScoreService.queryScoreByName(name,school,subject,startDate,endDate,className);
        return scoreList;
    }



    //新增扫码数据
    @PostMapping(value = "/mpi/homework/addScoreByScanner",produces = "application/json;charset=UTF-8")
    public String  addScoreByScanner(@RequestBody ScoreDTO scoreDTO) {
        log.info("小程序新增扫码数据{}", scoreDTO);
        String grade = scoreDTO.getClassNumber().substring(0,1)+"年级";
        Integer year = m_gradeYearMapper.getYearByGrade(grade);
        scoreDTO.setYear(year);

        scoreDTO.setClassNumber(turnChineseClassToNumber(scoreDTO.getClassNumber()));
        String resp = accumulateScoreService.addScoreByScanner(scoreDTO);
        return resp;
    }

    //新增语音数据
    @PostMapping(value = "/mpi/homework/audioPerform",produces = "application/json;charset=UTF-8")
    public String aiPerform(@RequestParam("audioFile") MultipartFile file , String school, String classNumber, String subject) throws Exception {

        String message = aliyunAudioRecognitionUtil.recognize(file);
        String grade = classNumber.substring(0,1)+"年级";
        Integer year = m_gradeYearMapper.getYearByGrade(grade);
        classNumber = turnChineseClassToNumber(classNumber);

        if(message.startsWith("第")){
            String s = aiService.groupPerform(classNumber,subject,message, school,year);
            return s;
        }else if(message.startsWith("表扬")|| message.startsWith("批评")){
            String s = aiService.quickPerform(classNumber, subject,message, school,year);
            return s;
        }
        else {
            //调用AI接口
            String s = aiService.aiPerform(classNumber,subject,message, school,year);
            return s;
        }


    }


    /**
     * 获取一段时间，惩罚举措的查询
     * @param school
     * @param className
     * @param subject
     * @param startDate
     * @param endDate
     * @return
     */
    @GetMapping("/mpi/homework/getPunishItemRecord")
    public List<M_WellBadHomeworkPerform> getPunishItemRecord(String school, String className, String subject, LocalDate startDate, LocalDate endDate,String showItem)  {
        log.info("获取惩罚记录的参数是：学校{},班级{},学科{},检查日期{},{},是否核销{}",school,className,subject,startDate,endDate,showItem);
        className = turnChineseClassToNumber(className);
        List<M_WellBadHomeworkPerform> result = wellBadPerformService.getPunishItemRecord(school,className,subject,startDate,endDate,showItem);
        return result;

    }

    @PostMapping("mpi/homework/addPunishItemRecord")
    public String addPunishItemRecord(@RequestBody  M_WellBadHomeworkPerform mWellBadHomeworkPerform){
        log.info("新增惩罚记录的参数是：{}",mWellBadHomeworkPerform);
        String grade = mWellBadHomeworkPerform.getClassName().substring(0,1)+"年级";
        Integer year = m_gradeYearMapper.getYearByGrade(grade);
        mWellBadHomeworkPerform.setYear(year);
        mWellBadHomeworkPerform.setClassName(turnChineseClassToNumber(mWellBadHomeworkPerform.getClassName()));
        String result = wellBadPerformService.uploadpunishItemRecord(mWellBadHomeworkPerform);
        return result;

    }

    /**
     * 核销惩罚举措记录
     * @param id
     * @return
     */
    @GetMapping("/mpi/homework/cancelPunishItemRecord")
    public String cancelPunishItemRecord(Integer id){
        log.info("核销惩罚记录的id是：{}",id);
        String result = wellBadPerformService.cancelPunishItemRecord(id);
        return result;
    }


    /**
     * 删除惩罚举措记录
     * @param id
     * @return
     */
    @GetMapping("/mpi/homework/deletePunishItemRecord")
    public String deletePunishItemRecord(Integer id){
        log.info("删除惩罚记录的id是：{}",id);
        String result = wellBadPerformService.deleteShowImage(id);
        return result;
    }





    //新增文本数据
    @PostMapping(value = "/mpi/homework/textPerform",produces = "application/json;charset=UTF-8")
    public String textAiPerform(@RequestBody  M_AiPerformDTO mAiPerformDTO) throws Exception {
        log.info("小程序新增文本数据{}", mAiPerformDTO);


        String message = mAiPerformDTO.getMessage();
        String school = mAiPerformDTO.getSchool();
        String classNumber = mAiPerformDTO.getClassNumber();
        String subject = mAiPerformDTO.getSubject();
        String grade = classNumber.substring(0,1)+"年级";
        Integer year = m_gradeYearMapper.getYearByGrade(grade);
        if(classNumber.startsWith("一")||classNumber.startsWith("二")||classNumber.startsWith("三")||classNumber.startsWith("四")||classNumber.startsWith("五")||classNumber.startsWith("六")||classNumber.startsWith("七")||classNumber.startsWith("八")||classNumber.startsWith("九")){
            classNumber = turnChineseClassToNumber(classNumber);}


        if(message.startsWith("第")){
            String s = aiService.groupPerform(classNumber,subject,message, school,year);
            return s;
        }else if(message.startsWith("表扬")|| message.startsWith("批评")){
            String s = aiService.quickPerform(classNumber, subject,message, school,year);
            return s;
        }
        else {
            //调用AI接口
            String s = aiService.aiPerform(classNumber,subject,message, school,year);
            return s;
        }


    }


    private String turnChineseClassToNumber(String classNumber) {
        if(classNumber != null && !classNumber.isEmpty()) {
            if(classNumber.contains("一")) {
                classNumber = classNumber.replace("一","1");
            }
            if(classNumber.contains("二")) {
                classNumber = classNumber.replace("二","2");
            }
            if(classNumber.contains("三")) {
                classNumber = classNumber.replace("三","3");
            }
            if(classNumber.contains("四")) {
                classNumber = classNumber.replace("四","4");
            }
            if(classNumber.contains("五")) {
                classNumber = classNumber.replace("五","5");
            }
            if(classNumber.contains("六")) {
                classNumber = classNumber.replace("六","6");
            }
            if(classNumber.contains("七")) {
                classNumber = classNumber.replace("七","7");
            }
            if(classNumber.contains("八")) {
                classNumber = classNumber.replace("八","8");
            }
            if(classNumber.contains("九")) {
                classNumber = classNumber.replace("九","9");
            }
        }
        //把(和)替换掉
        if(classNumber != null && !classNumber.isEmpty()) {
            if (classNumber.contains("(")) {
                classNumber = classNumber.replace("(", "");
            }
            if (classNumber.contains(")")) {
                classNumber = classNumber.replace(")", "");
            }
        }
        //把班替换掉
        if(classNumber != null && !classNumber.isEmpty()) {
            if (classNumber.contains("班")) {
                classNumber = classNumber.replace("班", "");
            }
        }
        return classNumber;
    }





}
