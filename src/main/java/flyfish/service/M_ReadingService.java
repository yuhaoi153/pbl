package flyfish.service;

import flyfish.pojo.DTO.*;
import flyfish.pojo.M_SingleReadTeacher;
import flyfish.pojo.M_TeacherData;
import flyfish.pojo.VO.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface M_ReadingService {
    //根据学校和周几获取早读巡查人员
    List<M_MoringReadingCheckVO> getReadingCheckList(String weekday, String school);

    //根据学校获取班级名称
    M_ClassNameListVO getClassNameList(String school);

    //将登记早读情况记录反馈给后台处理
    String recordReading(M_ReadGradeFeedDTO mReadGradeFeedDTO);

    //将前端传递的教师excel表存入数据库
    String updateTeacherData(MultipartFile file, String school) throws IOException;

    //将前端传递的班级绑定教师excel表存入数据库
    String updateClassTeacherData(MultipartFile file, String school);

    M_ReadingThreeSituationDTO getSingleReadFeedback(String school, LocalDate checkDate);

    //根据学校获取教师数据
    M_TeacherListMap getTeacherData(String school);

    //将前端传递的教师反馈记录存入数据库
    String recordTeacherFeedback(M_ReadTeacherFeedDTO mReadTeacherFeedDTO);

    //根据学校和日期获取教师反馈记录
    M_SingleReadTeacherVO getSelectedTeacherStatus(String school, LocalDate checkDate);

    //新增班级教师关系
    String addClassTeacherRelation(M_ClassTeacherMapDTO mClassTeacherMapDTO);

    //删除班级教师关系
    String deleteClassTeacherRelation(M_ClassTeacherMapDTO mClassTeacherMapDTO);

    //根据起止日期和学校获取班级早读情况
    Map<String,List<M_ClassCountVO>> getClassCount(LocalDate startDate, LocalDate endDate, String school,String type,Integer topNum, List<String> timeZone,Integer praiseAddScore,Integer criticizeSubScore);

    //获得各标签的数量
    Map<String, Integer> getLabelCount(LocalDate startDate, LocalDate endDate, String school, String type, List<String> timeZone);

    //根据日期和学校生成反馈报告
    String getFeedbackReport(String school, LocalDate checkDate);

    String poorPerform(M_PoorPerformDTO poorPerformDTO);
}
