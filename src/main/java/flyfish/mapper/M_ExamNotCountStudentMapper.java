package flyfish.mapper;

import flyfish.pojo.M_ExamNotCountStudent;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface M_ExamNotCountStudentMapper {

    @Select("select * from miniprograme.examNotCountStudent where school = #{school} and grade = #{grade} and className = #{className} and studentName = #{databaseStudentName}")
    M_ExamNotCountStudent getByStudentName(String school, String grade, Integer className, String databaseStudentName);
    @Select("select studentName from miniprograme.examNotCountStudent where school = #{school} and grade = #{grade} and className = #{className} ")
    List<String> getByclassName(String school, String grade, Integer className);
}
