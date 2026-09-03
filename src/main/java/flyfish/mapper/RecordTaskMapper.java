package flyfish.mapper;

import flyfish.pojo.DTO.ParentQueryDTO;
import flyfish.pojo.PassTask;
import flyfish.pojo.RecordTask;
import flyfish.pojo.VO.ParentPassTaskVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface RecordTaskMapper {


    @Select("select * from homework.recordtask where class_number = #{classNumber} and content = #{content} and subject =#{subject} and school = #{school}")
    List<PassTask> isexist(String classNumber, String content, String subject,String school);

    /**
     * 批量上传数据
     * @param originrecordList
     */
    void batchupload(List<PassTask> originrecordList);

    /**
     * 更新未完成
     * @param completed
     * @param nowtime
     * @param content
     * @param classNumber
     * @param nameList
     * @param subject
     */
    void updatecompleted(Integer completed, LocalDateTime nowtime, String content, String classNumber, List<String> nameList, String subject,String school);

    /**
     * 获取未完成名单
     * @param uncompleted
     * @param content
     * @param classNumber
     * @param subject
     * @return
     */
    @Select("select name from homework.recordtask where class_number = #{classNumber} and content = #{content} and subject = #{subject} and completed = 0 and school = #{school}")

    List<String> getuncompleted(Integer uncompleted, String content, String classNumber, String subject,String school);

    List<PassTask> queryALlPassContent(List<Integer> ids);

    /**
     * 家长端查询未过关数据
     * @param parentQueryDTO
     * @return
     */
    List<ParentPassTaskVO> queryPassData(ParentQueryDTO parentQueryDTO);

    void parentConfirm(List<Integer> ids);
    @Select("select * from homework.recordtask where class_number = #{classNumber} and name = #{username} and subject = #{subject} and checkdate >= #{startDate} and checkdate <= #{endDate}")
    List<RecordTask> getRecordTaskBySchoolClassName(String classNumber, String username, LocalDate startDate, LocalDate endDate, String subject);
}
