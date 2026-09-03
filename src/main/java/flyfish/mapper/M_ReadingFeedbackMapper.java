package flyfish.mapper;

import flyfish.pojo.DTO.M_DeleteReadingFeedbackDTO;
import flyfish.pojo.DTO.M_FeedbackDTO;
import flyfish.pojo.DTO.M_ReadTeacherFeedDTO;
import flyfish.pojo.M_FeedBack;
import flyfish.pojo.M_SingleReadFeedback;
import flyfish.pojo.M_SingleReadTeacher;
import flyfish.pojo.VO.M_ClassCountVO;
import flyfish.pojo.VO.M_FeedbackVO;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface M_ReadingFeedbackMapper {
    //根据学校、检查日期、检查项目、年级获取班级名称、
    @Select("select class_name from miniprograme.single_readfeedback where school=#{school} and check_date=#{checkDate} and grade=#{grade} and time_zone=#{timeZone}")
    List<String> getClassNameList(M_FeedBack mFeedBack);

    //根据学校、检查日期、检查项目、年级删除反馈信息；
    @Delete("delete from miniprograme.single_readfeedback where school=#{school} and check_date=#{checkDate} and grade=#{grade} and time_zone=#{timeZone}")
    void deleteFeedback(M_FeedBack mFeedBack);

    //根据学校、检查日期、检查项目、年级、班级列表添加班级名称；
    void addClassNameList(List<M_FeedBack> feedBackList);

    //根据学校、检查日期获取早读反馈信息；
    @Select("select * from miniprograme.single_readfeedback where school=#{school} and check_date=#{checkDate}")
    List<M_FeedBack> getSingleReadFeedback(String school, LocalDate checkDate);

    //根据学校、检查日期、检查项目、年级、班级获取早读反馈信息；

    List<M_ClassCountVO> getClassCount(LocalDate startDate, LocalDate endDate, String school,Integer topNum, List<String> timeZone);


    List<M_ClassCountVO> getGradeClassCount(LocalDate startDate, LocalDate endDate, String school, String grade,Integer topNum, List<String> timeZone);

    Integer getMaxAllClassCount(LocalDate startDate, LocalDate endDate, String school, List<String> timeZone);

    Integer getMaxGradeCount(LocalDate startDate, LocalDate endDate, String school, List<String> timeZone);

    Integer getAllRecordCount(LocalDate startDate, LocalDate endDate, String school, List<String> timeZone);

    List<String> getAllClassName(LocalDate startDate, LocalDate endDate, String school, List<String> timeZone);

    @Select("select * from miniprograme.single_readfeedback where school=#{school} and check_date=#{checkDate} and time_zone=#{timeZone}")
    List<M_FeedBack> getClassManageFeedback(String school, LocalDate checkDate, String timeZone);



    List<M_FeedbackVO> getFeedbackDataByClassName(M_FeedbackDTO mFeedbackDTO);

    List<M_FeedbackVO> getFeedbackDataByGrade(M_FeedbackDTO mFeedbackDTO);

    void deleteReadingFeedbackByIdList(M_DeleteReadingFeedbackDTO mDeleteReadingFeedbackDTO);

    void deleteFeedbackByIdListByGrade(M_DeleteReadingFeedbackDTO mDeleteReadingFeedbackDTO);

    //查询早读数量，统计到德育排名中
    List<M_ClassCountVO> getSelfmanageReadLeasonGradeClassCountScore(LocalDate startDate, LocalDate endDate, String school, String grade, String label, Integer score);

    //查询，导出到Excel
//    @Select("select * from miniprograme.singleReadTeacherRecord where school=#{school} and check_date between #{startDate} and #{endDate} and grade=#{grade}")
    List<M_SingleReadTeacher> getReadingFeedbackByDateRange(String school, LocalDate startDate, LocalDate endDate,String grade);

    //查询早读表，导出到Excel
//    @Select("select * from miniprograme.single_readfeedback where school=#{school} and check_date between #{startDate} and #{endDate} and time_zone=#{selectTimeZone} and grade=#{grade}")
    List<M_SingleReadFeedback> getReadingSelfmanageFeedbackByDateRange(String school, LocalDate startDate, LocalDate endDate, String selectTimeZone ,String grade);

    @Select("select id from miniprograme.single_readfeedback where school=#{school} and class_name=#{className} and time_zone=#{timeZone} and check_date=#{startDate} limit 1")
    Integer getIdBySchoolClassNameTimeZone(String school, String className, String timeZone, LocalDate startDate);

    @Select("select count(*) from miniprograme.single_readfeedback where school=#{school} and class_name=#{className} and time_zone=#{timeZone} and check_date between #{startDate} and #{endDate}")
    Integer getCountNumbyClasNameDateTimeZone(LocalDate startDate, LocalDate endDate, String school, String className, String timeZone);

    @Select("select count(*) from miniprograme.single_readfeedback where school=#{school} and grade=#{grade} and time_zone=#{timeZone} and check_date between #{startDate} and #{endDate}")
    Integer getGradeAvgNumbyDateTimeZone(LocalDate startDate, LocalDate endDate, String school, String grade, String timeZone);
}
