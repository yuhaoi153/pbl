package flyfish.mapper;

import flyfish.pojo.Charts;
import flyfish.pojo.FeedBack;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface FeedBackMapper {


    /**
     * 根据日期和年级，批量删除数据
     * @param checkDate
     * @param grade
     */
    void deletebyDateGrade(LocalDate checkDate, String grade);

    /**
     * 批量新增
     * @param feedBacks
     */

    void batchadd(List<FeedBack> feedBacks);

    /**
     * 根据年级和日期查询反馈数据
     * @param grade1
     * @param checkdate
     * @return
     */
    @Select("select * from autoEvaluation.feedback where grade = #{grade1} and check_date =#{checkdate}")
    List<FeedBack> getByDateGrade(String grade1, LocalDate checkdate);

    /**
     * 根据日期查询所有被记录的数据
     * @param checkdate
     * @return
     */
    @Select("select * from autoEvaluation.feedback where check_date = #{checkdate} and time_zone = #{timeZone}")
    List<FeedBack> getbyDate(LocalDate checkdate, String timeZone);

    /**
     * 根据起zhi日期查询统计班级结果
     *
     * @param start
     * @param end
     * @return
     */
    @Select("select class_name, count(*) as praise_classcount from autoEvaluation.feedback where check_date between #{start} and #{end}  group by class_name " +
            "order by praise_classcount desc limit #{limit}")
    List<Charts> getClassNameDuration(LocalDate start, LocalDate end,String grade, Integer limit);

    /**
     * 根据起止日期查询年级统计结果
     * @param start
     * @param end
     * @param limit
     * @return
     */
    @Select("select grade, count(*) as praise_gradecount from autoEvaluation.feedback where check_date between #{start} and #{end} group by  grade order by praise_gradecount desc limit #{limit}")
    List<Charts> getGradeDuration(LocalDate start, LocalDate end, Integer limit);

    /**
     * 根据起止日期查询教师统计结果
     * @param start
     * @param end
     * @param limit
     * @return
     */
    @Select("select teacher_name, count(*) as praise_teachercount from autoEvaluation.feedback where check_date between #{start} and #{end} group by teacher_name order by praise_teachercount desc limit #{limit}")
    List<Charts> getTeacherDuration(LocalDate start, LocalDate end, Integer limit);

    /**
     * 根据起止日期查询学科统计结果
     * @param start
     * @param end
     * @param limit
     * @return
     */
    @Select("select subject, count(*) as praise_subjectcount from autoEvaluation.feedback where check_date between #{start} and #{end} group by subject order by praise_subjectcount desc limit #{limit}")
    List<Charts> getSubjectDuration(LocalDate start, LocalDate end, Integer limit);

    /**
     * 根据起止日期查询指定年级的班级数据
     * @param start
     * @param end
     * @param grade
     * @param limit
     * @return
     */
    @Select("select class_name, count(*) as praise_classcount from autoEvaluation.feedback where check_date between #{start} and #{end} and grade = #{grade} group by class_name " +
            "order by praise_classcount desc limit #{limit}")
    List<Charts> getGradeClassNameDuration(LocalDate start, LocalDate end, String grade, Integer limit);

    /**
     * @param grade1
     * @param checkdate
     * @param timeZone
     * @return
     */
    @Select("select * from autoEvaluation.feedback where grade = #{grade1} and check_date =#{checkdate} and time_zone =#{timeZone}")
    List<FeedBack> getByDateGradeTime(String grade1, LocalDate checkdate, String timeZone);


    @Delete("delete from autoEvaluation.feedback where check_date = #{checkDate} and grade=#{grade} and time_zone =#{timeZone}")
    void deletebyDateGradeTime(LocalDate checkDate, String grade, String timeZone);

    @Delete("delete from homework.feedbackConstant where class_number = #{username} and subject = #{subject} and school = #{school}")
    void deleteConstant(String username, String subject, String school);
    @Insert("insert into homework.feedbackConstant (class_number, subject, collected_number, praise_number, uncompleted_number, warning_number, school) " +
            "values (#{username}, #{subject}, #{collectedNumber}, #{praiseNumber}, #{uncompletedNumber}, #{warningNumber}, #{school})")
    void addConsant(String username, String subject, Integer collectedNumber, Integer praiseNumber, Integer uncompletedNumber, Integer warningNumber, String school);






}
