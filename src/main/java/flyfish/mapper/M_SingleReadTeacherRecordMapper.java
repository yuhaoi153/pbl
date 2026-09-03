package flyfish.mapper;

import flyfish.pojo.DTO.M_DeleteReadingFeedbackDTO;
import flyfish.pojo.DTO.M_FeedbackDTO;
import flyfish.pojo.DTO.M_ReadTeacherFeedDTO;
import flyfish.pojo.M_SingleReadTeacher;
import flyfish.pojo.VO.M_ClassCountVO;
import flyfish.pojo.VO.M_FeedbackVO;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface M_SingleReadTeacherRecordMapper {

    @Delete("delete from miniprograme.singleReadTeacherRecord where school=#{school} and check_date=#{checkDate} and time_zone = #{timeZone} and grade = #{grade}")
    void deleteTeacherFeedback(M_ReadTeacherFeedDTO mReadTeacherFeedDTO);

    //批量添加教师反馈记录
    void addTeacherFeedback(List<M_SingleReadTeacher> singleReadTeacherList);

    //根据学校和日期和time_zone获取教师反馈记录
    @Select("select * from miniprograme.singleReadTeacherRecord where school=#{school} and check_date=#{checkDate} and time_zone = #{timeZone} and grade = #{grade}")
    List<M_SingleReadTeacher> getSingleReadTeacherList(M_ReadTeacherFeedDTO mReadTeacherFeedDTO);

    //根据学校和日期和time_zone获取教师反馈记录
    @Select("select * from miniprograme.singleReadTeacherRecord where school=#{school} and check_date=#{checkDate} and time_zone = #{timeZone}")
    List<M_SingleReadTeacher> getSelectedTeacherStatus(String school, LocalDate checkDate, String timeZone);

    List<M_ClassCountVO> getSubjectCount(LocalDate startDate, LocalDate endDate, String school,Integer topNum, List<String> timeZone);

    List<M_ClassCountVO> getTeacherCount(LocalDate startDate, LocalDate endDate, String school,Integer topNum, List<String> timeZone);

    Integer getMaxSubjectCount(LocalDate startDate, LocalDate endDate, String school, List<String> timeZone);

    Integer getMaxTeacherCount(LocalDate startDate, LocalDate endDate, String school, List<String> timeZone);

    Integer getAllRecordCount(LocalDate startDate, LocalDate endDate, String school, List<String> timeZone);

    List<String> getSubject(String school);

    List<String> getTeacher(String school);

    List<M_FeedbackVO> getFeedbackDataByTeacherName(M_FeedbackDTO mFeedbackDTO);

    List<M_FeedbackVO> getFeedbackDataBySubject(M_FeedbackDTO mFeedbackDTO);

    List<M_FeedbackVO> getFeedbackDataByClassName(M_FeedbackDTO mFeedbackDTO);

    void deleteTeacherFeedbackByTeacherNameIds(M_DeleteReadingFeedbackDTO mDeleteReadingFeedbackDTO);

    void deleteTeacherFeedbackBySubjectIds(M_DeleteReadingFeedbackDTO mDeleteReadingFeedbackDTO);

    void deleteTeacherFeedbackByClassName(M_DeleteReadingFeedbackDTO mDeleteReadingFeedbackDTO);

    @Select("select teacher_name from miniprograme.singleReadTeacherRecord where school=#{school} and class_name=#{className} and time_zone=#{timeZone} and check_date=#{startDate}")
    String getTeacherNameBySchoolClassNameTimeZone(String school, String className, String timeZone, LocalDate startDate);

    @Select("select teacher_name from miniprograme.singleReadTeacherRecord where school=#{school} and class_name=#{className} and time_zone=#{timeZone} and check_date between #{startDate} and #{endDate}")
    List<String> getTeacherNameListBySchoolClassNameTimeZone(String school, String className, String timeZone, LocalDate startDate, LocalDate endDate);
}
