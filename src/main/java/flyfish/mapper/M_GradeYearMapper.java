package flyfish.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface M_GradeYearMapper {

    @Select("select year from miniprograme.grade_year where grade = #{grade} limit 1")
    int getYearByGrade(String grade);
}
