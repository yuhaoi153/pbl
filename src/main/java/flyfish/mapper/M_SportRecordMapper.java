package flyfish.mapper;

import flyfish.pojo.DTO.M_DeleteReadingFeedbackDTO;
import flyfish.pojo.DTO.M_FeedbackDTO;
import flyfish.pojo.M_FeedBack;
import flyfish.pojo.M_SingleMoralRecord;
import flyfish.pojo.M_SingleSportRecord;
import flyfish.pojo.VO.M_ClassCountVO;
import flyfish.pojo.VO.M_FeedbackVO;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface M_SportRecordMapper {
    @Select("select class_name from miniprograme.singleSportRecord where school=#{school} and check_date=#{checkDate} and grade=#{grade} and time_zone=#{timeZone}")
    List<String> getClassNameList(M_FeedBack mFeedBack);

    //根据学校、检查日期、检查项目、年级删除反馈信息；
    @Delete("delete from miniprograme.singleSportRecord where school=#{school} and check_date=#{checkDate} and grade=#{grade} and time_zone=#{timeZone}")
    void deleteFeedback(M_FeedBack mFeedBack);

    void addClassNameList(List<M_FeedBack> feedBackList);

    @Select("select * from miniprograme.singleSportRecord where school=#{school} and check_date=#{checkDate}")
    List<M_FeedBack> getSingleSportFeedback(String school, LocalDate checkDate);


    @Select("select * from miniprograme.singleSportRecord where school=#{school} and check_date=#{checkDate} and time_zone=#{timeZone}")
    List<M_FeedBack> getSportRecord(String school, LocalDate checkDate, String timeZone);


    List<M_ClassCountVO> getGradeClassCount(LocalDate startDate, LocalDate endDate, String school, String grade, Integer topNum, List<String> timeZone);


    List<M_ClassCountVO> getGradeClassCountScore(LocalDate startDate, LocalDate endDate, String school, String grade, Integer topNum, String timeZoneStr, Integer score);

    List<M_FeedbackVO> getFeedbackDataByClassName(M_FeedbackDTO mFeedbackDTO);

    List<M_FeedbackVO> getFeedbackDataByGrade(M_FeedbackDTO mFeedbackDTO);

    void deleteSportFeedbackByIdList(M_DeleteReadingFeedbackDTO mDeleteReadingFeedbackDTO);


    //查询数量，统计到德育排名中
    List<M_ClassCountVO> getGymRunGradeClassCountScore(LocalDate startDate, LocalDate endDate, String school, String grade, String label, Integer score);

    //查询，导出原始数据到Excel

    List<M_SingleSportRecord> getSportRecordByDateRange(String school, LocalDate startDate, LocalDate endDate,String grade);

    @Select("SELECT \n" +
            "    grade,\n" +
            "    SUBSTRING_INDEX(SUBSTRING_INDEX(class_name, ')', 1), '(', -1) AS className\n" +
            "FROM miniprograme.singleSportRecord\n" +
            "WHERE school = #{school}\n" +
            "  AND check_date = #{checkDate}\n" +
            "  AND time_zone = #{label}")
    List<M_SingleMoralRecord> getSportRecordListByDateAndLabel(String school, LocalDate checkDate, String label);
}
