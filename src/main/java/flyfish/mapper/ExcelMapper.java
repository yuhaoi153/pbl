package flyfish.mapper;

import flyfish.pojo.DTO.StudentDTO;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ExcelMapper {

    /**
     * 批量新增学生的姓名、学号、班级信息
     * @param students
     * @param username
     */
    void addStudent(List<StudentDTO> students, String username,String school);

    /**
     * 删除原有的班级信息
     * @param username
     */
    @Delete("delete from homework.studentInfo where class_number = #{username} and school = #{school}")
    void batchDelete(String username,String school);

    /**
     * 检查该班级是否已经有学生信息存在
     * @param classNumber
     * @return
     */
    @Select("select name from homework.studentInfo where class_number = #{classNumber} and school = #{school} limit 1")
    String checkExcel(String classNumber,String school);

    /**
     * 根据班级查询所有的学生信息
     * @param username
     * @return
     */
    @Select("select * from homework.studentInfo where class_number =#{username} and school = #{school}")
    List<StudentDTO> getByUserName(String username,String school);
}
