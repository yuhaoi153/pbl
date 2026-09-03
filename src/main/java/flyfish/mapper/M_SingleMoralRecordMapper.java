package flyfish.mapper;

import flyfish.pojo.M_BehaviorTag;
import flyfish.pojo.M_GradeClassNum;
import flyfish.pojo.M_SingleMoralRecord;
import flyfish.pojo.VO.M_ClassCountVO;
import flyfish.pojo.VO.M_SingleMoralRecordVO;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface M_SingleMoralRecordMapper {
    @Select("select class_name from miniprograme.singleMoralRecord" +
            " where school = #{school} and grade = #{grade} and label = #{label} and check_date = #{checkDate}")
    List<Integer> getClassByGradeLabel(String school, String grade, String label, LocalDate checkDate);

    @Delete("delete from miniprograme.singleMoralRecord " +
            "where school = #{school} and grade = #{grade} and label = #{label} and check_date = #{checkDate}")
    void deleteByGradeLabel(String school, String grade, String label, LocalDate checkDate);

    @Insert("insert into miniprograme.singleMoralRecord " +
            "(school, year, grade, class_name, label, check_date) " +
            "values (#{school}, #{year}, #{grade}, #{className}, #{label}, #{checkDate})")
    void insertMoralRecord(String school, int year, String grade, Integer className, String label, LocalDate checkDate);

    @Select("select grade, class_name, label from miniprograme.singleMoralRecord " +
            "where school = #{school} and check_date = #{checkDate}")
    List<M_GradeClassNum> getClassByCheckDate(String school, LocalDate checkDate);


    List<String> getNameByStudentIds(String school, LocalDate checkDate, List<Integer> studentIdList,String tag, String label);

    @Delete("delete from miniprograme.singleMoralRecord "+
            "where school = #{school} and grade = #{grade} and class_name = #{classNum} " +
            "and label = #{label} and check_date = #{checkDate} and supplement = #{tag}")
    void deleteByClassTagLabelDate(String school, String grade, Integer classNum, String tag, String label, LocalDate checkDate);

    @Select("select studentId from miniprograme.singleMoralRecord " +
            "where school = #{school} and check_date = #{checkDate} and supplement = #{tag} " +
            "and label = #{label} and grade = #{grade} and class_name = #{classNum}")
    List<Integer> getStudentIdByLabelClass(String school, LocalDate checkDate, String tag, String label, String grade, Integer classNum);

    @Insert("insert into miniprograme.singleMoralRecord " +
            "(school, year, grade, class_name, label, check_date, supplement, studentId, student_name) " +
            "values (#{school}, #{year}, #{grade}, #{classNum}, #{label}, #{checkDate}, #{tag}, #{studentId}, #{studentName})")
    void insertMoralStudentRecord(String school, Integer year, String grade, Integer classNum, String label, LocalDate checkDate, String tag, Integer studentId, String studentName);

    @Select("select * from miniprograme.singleMoralRecord " +
            "where school = #{school} and check_date = #{checkDate}")
    List<M_SingleMoralRecordVO> getPersonalMoralRecordByDate(String school, LocalDate checkDate);

    //对所有符合条件的班级计数

    Integer getClassNum(String school, LocalDate checkDate, List<String> praiseLabelList);

    //对于所有符合条件的学生计数
    @Select("select count(*) from miniprograme.singleMoralRecord where school = #{school} and check_date = #{checkDate} and label = #{personalPraiseLabel}")
    Integer getPersonalNum(String school, LocalDate checkDate, String personalPraiseLabel);

    //拿到所有符合条件的班级的计数列表
    List<M_ClassCountVO> getGradeClassCountScore(LocalDate startDate, LocalDate endDate, String school, String grade, String label, Integer score);

    @Select("select * from miniprograme.singleMoralRecord where school = #{school} and check_date = #{checkDate} and label = #{label}")
    List<M_SingleMoralRecord> getMoralRecordListByDateAndLabel(String school, LocalDate checkDate, String label);


    List<M_SingleMoralRecord> getMoralRecordByDateRange(String school, LocalDate startDate, LocalDate endDate,String grade);

    @Select("select id from miniprograme.singleMoralRecord where school = #{school} and class_name = #{classNum} and grade = #{grade} and label = #{label} and check_date = #{startDate} limit 1")
    Integer getIdBySchoolClassNameLabel(String school,  String grade, Integer classNum, String label, LocalDate startDate);

    @Select("select * from miniprograme.singleMoralRecord where school = #{school} and class_name = #{classNum} and grade = #{grade} and label = #{label} and check_date  between #{startDate} and #{endDate}")
    List<M_SingleMoralRecord> getRecordBySchoolClassNameLabel(String school, String grade, Integer classNum, String label, LocalDate startDate, LocalDate endDate);

    @Select("select count(*) from miniprograme.singleMoralRecord where school = #{school} and class_name = #{classNum} and grade =#{grade} and label = #{label} and check_date between #{startDate} and #{endDate}")
    Integer getCountNumBySchoolClassNameLabel(String school, String grade, Integer classNum, String label, LocalDate startDate, LocalDate endDate);

    @Select("select count(*) from miniprograme.singleMoralRecord where school = #{school} and grade = #{grade} and label = #{label} and check_date between #{startDate} and #{endDate}")
    Integer getGradeAvgNumByDateTimeZone(String school, String grade, String label, LocalDate startDate, LocalDate endDate);
}
