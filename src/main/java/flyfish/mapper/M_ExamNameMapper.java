package flyfish.mapper;

import flyfish.pojo.DTO.M_ExamNameDeleteDTO;
import flyfish.pojo.M_ExamName;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface M_ExamNameMapper {

    @Select("SELECT * FROM miniprograme.examName WHERE school = #{school} AND showItem = #{showItem} AND createName = #{teacherName}  ORDER BY createTime DESC;")
    List<M_ExamName> getBySchool(String school,String showItem,String teacherName);

    @Insert("insert into miniprograme.examName (testName, createName, semester, school) " +
            "VALUES (#{testName}, #{createName}, #{semester}, #{school})")
    void insertSingeRecord(M_ExamName mExamName);

    @Delete("delete from miniprograme.examName where id = #{id}")
    void deleteExamName(M_ExamNameDeleteDTO mExamNameDeleteDTO);

    @Select("select * from miniprograme.examName where id = #{id}")
    M_ExamName getById(Integer id);

    @Select("select count(*) from miniprograme.examName where school = #{school} and (createName = #{createName} or createName = #{adminName} ) and semester = #{semester} and testName = #{testName}")
    Integer confirmName(M_ExamName mExamName);

    @Delete("delete from miniprograme.examName where school = #{school} and semester = #{semester} and testName = #{testName}")
    void deleteByExamName(String school, String semester, String testName);
}
