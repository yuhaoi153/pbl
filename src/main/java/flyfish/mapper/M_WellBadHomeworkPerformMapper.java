package flyfish.mapper;


import flyfish.pojo.M_WellBadHomeworkPerform;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface M_WellBadHomeworkPerformMapper {

    List<M_WellBadHomeworkPerform> getAllRecord(String school, String className, String content, String studentName, String subject, LocalDate startDate, LocalDate endDate);

    @Insert("insert into miniprograme.wellBadHomeworkPerform (studentName, homeworkRecordId,scorePerformId,  situation, showUrl, className, school, year, supplementary, checkDate, subject)" +
            "values (#{studentName}, #{homeworkRecordId}, #{scorePerformId},#{situation}, #{showUrl}, #{className}, #{school}, #{year}, #{supplementary}, #{checkDate}, #{subject})")
    void insertSingleRecord(M_WellBadHomeworkPerform mWellBadHomeworkPerform);

    @Delete("delete from miniprograme.wellBadHomeworkPerform where id = #{id}")
    void deleteById(Integer id);

    List<M_WellBadHomeworkPerform> getPunishRecord(String school, String className, String subject, LocalDate startDate, LocalDate endDate,String showItem);

    @Update("update miniprograme.wellBadHomeworkPerform set showItem = '否' where id = #{id}")
    void cancelById(Integer id);
}
