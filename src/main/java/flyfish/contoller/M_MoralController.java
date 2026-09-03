package flyfish.contoller;


import flyfish.pojo.DTO.*;
import flyfish.pojo.M_BehaviorTag;
import flyfish.pojo.VO.*;
import flyfish.service.M_MoralService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@Slf4j
public class M_MoralController {

    @Autowired
    private M_MoralService m_MoralService;

    //将登记德育情况记录反馈给后台处理
    @PostMapping(value = "/mpi/moral/recordMoral", produces = "application/json;charset=UTF-8")
    public String recordSport(@RequestBody M_MoralEightSituationDTO moralData) {
        log.info("登记德育巡查情况的参数是{}", moralData);
        // 这里可以添加处理逻辑，例如将数据保存到数据库等
        String gradeResp = m_MoralService.addMoralRecord(moralData);
        return gradeResp;
    }


    //把singleSportRecord表中的数据整理之后发给前端
    @GetMapping("/mpi/moral/getSelectedMoral")
    public M_MoralEightSituationDTO getMoralRecord(String school, LocalDate checkDate){
        log.info("获取某日德育巡查反馈的参数是{}{}",school,checkDate);
        M_MoralEightSituationDTO mMoralEightSituationDTO = m_MoralService.getSelectedMoralRecord(school,checkDate);
        return mMoralEightSituationDTO;
    }

    // 添加行为标签
    @PostMapping(value = "/mpi/moral/addBehaviorTag", produces = "application/json;charset=UTF-8")
    public String addBehaviorTag(@RequestBody M_BehaviorDTO mBehaviorTag) {
        log.info("添加行为标签的参数是{}", mBehaviorTag);
        return m_MoralService.addBehaviorTag(mBehaviorTag.getSchool(), mBehaviorTag.getTag(), mBehaviorTag.getType());
    }

    //删除某个行为标签
    @PostMapping(value = "/mpi/moral/deleteBehaviorTag", produces = "application/json;charset=UTF-8")
    public String deleteBehaviorTag(@RequestBody M_BehaviorDTO mBehaviorTag) {
        log.info("删除行为标签的参数是{}", mBehaviorTag);
        return m_MoralService.deleteBehaviorTag(mBehaviorTag.getSchool(), mBehaviorTag.getTag(), mBehaviorTag.getType());
    }

    // 获取行为标签
    @GetMapping("/mpi/moral/getBehaviorTags")
    public List<String> getBehaviorTags(@RequestParam String school, @RequestParam String type){
        log.info("获取行为标签的参数是{}{}",school,type);
        return m_MoralService.getBehaviorTags(school,type);
    }






    //获取班级学生列表
    @GetMapping("/mpi/moral/getStudentList")
    public List<M_PersonalCurrentStudentVO> getStudentList(@RequestParam String school, @RequestParam String className, @RequestParam LocalDate checkDate,@RequestParam String tag,@RequestParam String label){
        log.info("获取班级学生列表的参数是{}{}{}{}{}",school,className,checkDate,tag,label);
        return m_MoralService.getCurrentStudentList(school,className,checkDate,tag,label);
    }


    //记录某班级学生德育表现情况
    @PostMapping(value = "/mpi/moral/recordStudentBehavior", produces = "application/json;charset=UTF-8")
    public String recordStudentBehavior(@RequestBody M_MoralStudentBehaviorListDTO moralStudentBehaviorListDTO) {
        log.info("记录某班级学生德育表现情况的参数是{}", moralStudentBehaviorListDTO);
        String resp = m_MoralService.addBehaviorRecord(moralStudentBehaviorListDTO);
        // 这里可以添加处理逻辑，例如将数据保存到数据库等
        return resp;

    }

    //查询个人德育表现下，所有被选中的班级
    @GetMapping("/mpi/moral/getSelectedStudentClassList")
    public M_MoralPersonalSelectedClassListVO getSelectedStudentClassList(String school, LocalDate checkDate){
        log.info("查询个人德育表现下，所有被选中的班级的参数是{}{}",school,checkDate);
        M_MoralPersonalSelectedClassListVO mMoralPersonalSelectedClassListVO = m_MoralService.getSelectedStudentClassList(school, checkDate);
        return mMoralPersonalSelectedClassListVO;

    }


    //获取初始化当日德育统计数据
    @GetMapping ("/mpi/moral/getMoralStatisticNum")
    public M_MoralStatisticNumVO getMoralStatisticNum(String school, LocalDate checkDate){
        log.info("获取初始化当日德育统计数据的参数是{}{}",school,checkDate);
        M_MoralStatisticNumVO mMoralStatisticNumVO = m_MoralService.getMoralStatisticNum(school, checkDate);
        return mMoralStatisticNumVO;
    }


    //根据起止日期和学校 返回给前端班级数量列表，以便展示柱状图
    @PostMapping("/mpi/moral/getClassBarChart")
    public Map<String,List<M_ClassCountVO>> getClassCount(@RequestBody M_MoralClassCountDTO mMoralClassCountDTO){
        LocalDate startDate= mMoralClassCountDTO.getStartDate();
        LocalDate endDate= mMoralClassCountDTO.getEndDate();
        String school= mMoralClassCountDTO.getSchool();
        String statisticType= mMoralClassCountDTO.getStatisticType();
        Integer topNum= mMoralClassCountDTO.getTopNum();
        List<String> labelList= mMoralClassCountDTO.getLabelList();
        Integer roadPraiseAddScore= mMoralClassCountDTO.getRoadPraiseAddScore();
        Integer roadCriticizeSubScore= mMoralClassCountDTO.getRoadCriticizeSubScore();
        Integer disciplinePraiseAddScore= mMoralClassCountDTO.getDisciplinePraiseAddScore();
        Integer disciplineCriticizeSubScore= mMoralClassCountDTO.getDisciplineCriticizeSubScore();
        Integer hygienePraiseAddScore= mMoralClassCountDTO.getHygienePraiseAddScore();
        Integer hygieneCriticizeSubScore= mMoralClassCountDTO.getHygieneCriticizeSubScore();
        Integer personalPraiseAddScore= mMoralClassCountDTO.getPersonalPraiseAddScore();
        Integer personalCriticizeSubScore= mMoralClassCountDTO.getPersonalCriticizeSubScore();
        Integer gymPraiseAddScore= mMoralClassCountDTO.getGymPraiseAddScore();
        Integer gymCriticizeSubScore= mMoralClassCountDTO.getGymCriticizeSubScore();
        Integer runPraiseAddScore= mMoralClassCountDTO.getRunPraiseAddScore();
        Integer runCriticizeSubScore= mMoralClassCountDTO.getRunCriticizeSubScore();
        Integer selfmanagePraiseAddScore= mMoralClassCountDTO.getSelfmanagePraiseAddScore();
        Integer readPraiseAddScore= mMoralClassCountDTO.getReadPraiseAddScore();
        Integer lessonPraiseAddScore= mMoralClassCountDTO.getLessonPraiseAddScore();
        log.info("获取德育反馈柱形图的参数是 {}{}{}{}{}{}{}{}{}{}{}{}{}{}{}{}{}{}{}{}{}",startDate,endDate,school,statisticType,topNum,labelList,roadPraiseAddScore,roadCriticizeSubScore, disciplinePraiseAddScore  , disciplineCriticizeSubScore, hygienePraiseAddScore, hygieneCriticizeSubScore, personalPraiseAddScore, personalCriticizeSubScore, disciplinePraiseAddScore,disciplineCriticizeSubScore,hygienePraiseAddScore,hygieneCriticizeSubScore,personalPraiseAddScore,personalCriticizeSubScore,gymPraiseAddScore,gymCriticizeSubScore,runPraiseAddScore,runCriticizeSubScore,selfmanagePraiseAddScore,readPraiseAddScore,lessonPraiseAddScore);

        Map<String,List<M_ClassCountVO>> classCountMap = m_MoralService.getClassCount(startDate,endDate,school,statisticType,topNum,labelList,roadPraiseAddScore,roadCriticizeSubScore, disciplinePraiseAddScore  , disciplineCriticizeSubScore, hygienePraiseAddScore, hygieneCriticizeSubScore, personalPraiseAddScore, personalCriticizeSubScore, gymPraiseAddScore, gymCriticizeSubScore, runPraiseAddScore, runCriticizeSubScore, selfmanagePraiseAddScore, readPraiseAddScore, lessonPraiseAddScore);
        return classCountMap;
    }


    //获取德育统计的默认参数
    @GetMapping("/mpi/moral/getMoralStatisticDefaultParams")
    public M_MoralStatisticDefaultParamsVO getMoralStatisticDefaultParams(String school){
        log.info("获取德育统计的默认参数的参数是 {}",school);
        M_MoralStatisticDefaultParamsVO mMoralStatisticDefaultParamsVO = m_MoralService.getMoralStatisticDefaultParams(school);
        return mMoralStatisticDefaultParamsVO;
    }

    //设置德育统计的默认参数
    @PostMapping(value = "/mpi/moral/setMoralStatisticDefaultParams" , produces = "application/json;charset=UTF-8")
    public String setMoralStatisticDefaultParams(@RequestBody M_MoralStatisticDefaultParamsVO mMoralStatisticDefaultParamsVO) {
        log.info("设置德育统计的默认参数的参数是 {}", mMoralStatisticDefaultParamsVO);
        String resp = m_MoralService.setMoralStatisticDefaultParams(mMoralStatisticDefaultParamsVO);
        return resp;
    }


    //生成德育当日反馈
    @GetMapping(value = "/mpi/moral/getFeedbackReport", produces = "application/json;charset=UTF-8")
    public M_MoralFeedbackVO generateMoralFeedback(String school, LocalDate checkDate) {
        log.info("生成德育当日反馈的参数是 {}{}", school, checkDate);
        M_MoralFeedbackVO resp = m_MoralService.generateMoralFeedback(school, checkDate);
        return resp;
    }

    //生成德育数据的Excel汇总文件
    @GetMapping(value = "/mpi/moral/exportMoralExcel", produces = "application/json;charset=UTF-8")
    public ResponseEntity<byte[]> exportMoralExcel(String school, LocalDate startDate, LocalDate endDate, String labelString,String grade,String exportMode) {
        List<String> labelList = List.of(labelString.split(","));
        M_MoralExcelDTO moralExcelDTO = new M_MoralExcelDTO(school, startDate, endDate, labelList,grade,exportMode);
        log.info("生成早读汇总的参数是 {}{}{}{}{}", moralExcelDTO.getSchool(), moralExcelDTO.getStartDate(), moralExcelDTO.getEndDate(), moralExcelDTO.getLabelList(), moralExcelDTO.getGrade());
        //ResponseEntity<byte[]>中间包裹了字节流和响应头，可以直接返回给前端
        ResponseEntity<byte[]> excelData = m_MoralService.exportMoralExcel(moralExcelDTO);

        return excelData;
    }


    //生成德育数据的Excel文件
    @GetMapping(value = "/mpi/moral/exportMoralSingleExcel", produces = "application/json;charset=UTF-8")
    public ResponseEntity<byte[]> exportMoralSingleExcel(String school, LocalDate startDate, LocalDate endDate, String labelString,String grade) {
        List<String> labelList = List.of(labelString.split(","));
        M_MoralExcelDTO moralExcelDTO = new M_MoralExcelDTO();
        moralExcelDTO.setSchool(school);
        moralExcelDTO.setStartDate(startDate);
        moralExcelDTO.setEndDate(endDate);
        moralExcelDTO.setLabelList(labelList);
        moralExcelDTO.setGrade(grade);
        log.info("生成德育数据的Excel文件的参数是 {}{}{}{}{}", moralExcelDTO.getSchool(), moralExcelDTO.getStartDate(), moralExcelDTO.getEndDate(), moralExcelDTO.getLabelList(), moralExcelDTO.getGrade());
        //ResponseEntity<byte[]>中间包裹了字节流和响应头，可以直接返回给前端
        ResponseEntity<byte[]> excelData = m_MoralService.exportMoralExcel(moralExcelDTO);

        return excelData;
    }


}
