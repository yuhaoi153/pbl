package flyfish.mapper;

import flyfish.pojo.ClassTeacher;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;


@Mapper
public interface ClassTeacherMapper {

    /**
     * 通过年级查询教师信息
     * @param grade1
     * @return
     */
    @Select("select * from autoEvaluation.class_teacher where grade =#{grade1}")
    List<ClassTeacher> getByGrade(String grade1);
}
