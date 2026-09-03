package flyfish.service;

import flyfish.pojo.DTO.M_MoralEightSituationDTO;
import flyfish.pojo.DTO.M_MoralExcelDTO;
import flyfish.pojo.DTO.M_MoralStudentBehaviorListDTO;
import flyfish.pojo.M_BehaviorTag;
import flyfish.pojo.VO.*;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface M_MoralService {

    // 记录德育八项情况
    String addMoralRecord(M_MoralEightSituationDTO moralData);

    // 获取某日德育巡查反馈
    M_MoralEightSituationDTO getSelectedMoralRecord(String school, LocalDate checkDate);

    String addBehaviorTag(String school, String tag, String type);

    // 获取行为标签
    List<String> getBehaviorTags(String school, String type);

    //获取当前班级学生名单
    List<M_PersonalCurrentStudentVO> getCurrentStudentList(String school, String className, LocalDate checkDate, String tag, String label);

    // 添加学生行为记录
    String addBehaviorRecord(M_MoralStudentBehaviorListDTO moralStudentBehaviorListDTO);

    //获取个人表现所选班级名单
    M_MoralPersonalSelectedClassListVO getSelectedStudentClassList(String school, LocalDate checkDate);

    // 删除行为标签
    String deleteBehaviorTag(String school, String tag, String label);

    //获取当日德育统计数据
    M_MoralStatisticNumVO getMoralStatisticNum(String school, LocalDate checkDate);

    //获取一段时间德育统计柱状图数据
    Map<String, List<M_ClassCountVO>> getClassCount(LocalDate startDate, LocalDate endDate, String school, String statisticType, Integer topNum, List<String> labelList, Integer roadPraiseAddScore, Integer roadCriticizeSubScore, Integer disciplinePraiseAddScore, Integer disciplineCriticizeSubScore, Integer hygienePraiseAddScore, Integer hygieneCriticizeSubScore, Integer personalPraiseAddScore, Integer personalCriticizeSubScore,Integer gymPraiseAddScore, Integer gymCriticizeSubScore, Integer runPraiseAddScore, Integer runCriticizeSubScore, Integer selfmanagePraiseAddScore, Integer readPraiseAddScore, Integer leasonPraiseAddScore);

    //获取德育统计默认参数
    M_MoralStatisticDefaultParamsVO getMoralStatisticDefaultParams(String school);

    //设置德育统计默认参数
    String setMoralStatisticDefaultParams(M_MoralStatisticDefaultParamsVO mMoralStatisticDefaultParamsVO);

    //根据学校和日期生成德育反馈
    M_MoralFeedbackVO generateMoralFeedback(String school, LocalDate checkDate);

    //根据学校和日期导出德育反馈Excel
    ResponseEntity<byte[]> exportMoralExcel(M_MoralExcelDTO moralExcelDTO);
}
