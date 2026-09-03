package flyfish.mapper;

import flyfish.pojo.DTO.DeleteContetnDTO;
import flyfish.pojo.DTO.HomeWorkContentDTO;
import flyfish.pojo.DTO.QueryPassTaskDTO;
import flyfish.pojo.PassTask;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface PassTaskMapper {
    /**
     * 查询是否存在过关任务内容
     * @param homeWorkContentDTO
     * @return
     */

    List<String> queryContent(HomeWorkContentDTO homeWorkContentDTO);

    /**
     * 新增过关任务内容
     * @param homeWorkContentDTO
     */
//    @Insert("insert into homework.passtask (content, class_number, subject,supplementary) VALUES (#{content},#{classNumber},#{subject},#{supplementary})")
    void addContent(HomeWorkContentDTO homeWorkContentDTO);

    /**
     * 获取images字符串
     * @param classNumber
     * @param content
     * @param subject
     * @return
     */
    @Select("select supplementary from homework.passtask where class_number =#{classNumber} and content =#{content} and subject = #{subject}")
    String getImages(String classNumber, String content, String subject);

    /**
     * 批量删除过关任务
     * @param deleteContetnDTO
     */
    void batchDelete(DeleteContetnDTO deleteContetnDTO);

    /**
     * 查询过关任务内容
     * @return
     */
    @Select("select content from homework.passtask")
    List<String> autoqueryPasscontent();

    List<PassTask> queryALlPassContent(QueryPassTaskDTO queryPassTaskDTO);

    Integer addRecordPass(PassTask passTask);

    /**
     * 编辑过关任务
     * @param passTask
     */
    void editRecordPass(PassTask passTask);

    void deleteByIDs(List<Integer> ids);
}
