package flyfish.mapper;

import flyfish.pojo.DTO.DeleteContetnDTO;
import flyfish.pojo.DTO.HomeWorkContentDTO;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface HomeWorkContentMapper {
    /**
     * 新增作业类型
     * @param homeWorkContentDTO
     */
    @Insert("insert into homework.homeworkcontent (content, class_number, type, subject,school,year) " +
            "VALUES (#{content},#{classNumber},#{type},#{subject},#{school},#{year})")
    void addContent(HomeWorkContentDTO homeWorkContentDTO);

    /**
     * 查询所有的作业类型
     * @param homeWorkContentDTO
     * @return
     */

    List<String> queryContent(HomeWorkContentDTO homeWorkContentDTO);

    /**
     * 批量删除作业类型
     * @param deleteContetnDTO
     */
    void batchDelete(DeleteContetnDTO deleteContetnDTO);

    @Select("select content from homework.homeworkcontent where school = #{school} and class_number = #{classNumber} and subject = #{subject} ")
    List<String> queryContentByNameSubjectSchool(String school, String classNumber, String subject);
}
