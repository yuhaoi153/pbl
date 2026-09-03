package flyfish.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface TeacherListMapper {
    /**
     * 根据教师姓名查学科
     * @param teacherName
     * @return
     */
    @Select("select subject from autoEvaluation.teacherlist where name =#{teacherName}")
    String getByTeacherName(String teacherName);



    Integer getIdByNameShool(String teacherName, String school);
}
