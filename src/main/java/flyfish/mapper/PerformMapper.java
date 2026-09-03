package flyfish.mapper;

import flyfish.pojo.DTO.ParentQueryDTO;
import flyfish.pojo.Perform;
import flyfish.pojo.VO.ParentPerformVO;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface PerformMapper {

    /**
     * 家长端查询表现数据
     * @param parentQueryDTO
     * @return
     */

    List<ParentPerformVO> queryPerform(ParentQueryDTO parentQueryDTO);

    /**
     * 添加课内外表现
     * @param checkdate
     * @param nameList
     * @param subject
     * @param reason
     * @param situation
     * @param score
     * @param classNumber
     */
    void addPerform(LocalDate checkdate, List<String> nameList, String subject, String reason, String situation, Integer score, String classNumber, String school,Integer year);

    @Insert("insert into homework.perform(checkdate,name,subject,reason,situation,score,class_number,school) values(#{checkdate},#{name},#{subject},#{reason},#{situation},#{score},#{classNumber},#{school})")
    void insertByNFC(Perform perform);

    @Insert("insert into homework.perform(checkdate,name,subject,reason,situation,score,class_number,school,year) values(#{checkdate},#{name},#{subject},#{reason},#{situation},#{score},#{classNumber},#{school},#{year})")
    void addSinglePerform(Perform perform);

//    List<FeedBackLineChartVO> feedbackLineChart(FeedBackDTO feedBackDTO);
//
//    List<FeedBackLineChartVO> feedbackNotCloude(FeedBackDTO feedBackDTO);













    @Select("select * from homework.perform where class_number = #{classNumber} and name = #{username} and subject = #{subject} and checkdate >= #{startDate} and checkdate <= #{endDate} and reason != #{homeworkgoodReason} and reason != #{homeworkbadReason}")
    List<Perform> getPerformBySchoolClassName(String classNumber, String username, LocalDate startDate, LocalDate endDate, String subject ,String homeworkgoodReason, String homeworkbadReason);

    @Select("select count(*) from homework.perform where class_number = #{classNumber} and name = #{username} and subject = #{subject} and checkdate >= #{startDate} and checkdate <= #{endDate} and situation = #{situation} and reason = #{reason}")
    Integer getSumScoreBySchoolClassNameReason(String classNumber, String username, LocalDate startDate, LocalDate endDate, String subject, String situation, String reason);

    //所有表扬的减去作业优秀表扬
    @Select("select count(*) from homework.perform where class_number = #{classNumber} and name = #{username} and subject = #{subject} and checkdate >= #{startDate} and checkdate <= #{endDate} and situation = #{situation} and reason != #{reason}")
    Integer getSumClassScoreBySchoolClassNameReason(String classNumber, String username, LocalDate startDate, LocalDate endDate, String subject, String situation, String reason);

    @Select("select * from homework.perform where class_number = #{className} and subject = #{subject} and checkdate >= #{startDate} and checkdate <= #{endDate}  ")
    List<Perform> getPerformByClassSubject(String className, String school, String subject, LocalDate startDate, LocalDate endDate);

    @Select("select * from homework.perform where class_number = #{className} and subject = #{subject} and checkdate >= #{startDate} and checkdate <= #{endDate} and name = #{name}")
    List<Perform> getPerformByClassSubjectAndName(String className, String school, String subject, LocalDate startDate, LocalDate endDate, String name);

    void addSinglePerformReturnId(Perform perform);
}
