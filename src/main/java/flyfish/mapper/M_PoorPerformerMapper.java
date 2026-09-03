package flyfish.mapper;

import flyfish.pojo.DTO.M_PoorPerformDTO;
import flyfish.pojo.M_ReadingPoorPerform;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface M_PoorPerformerMapper {
    @Insert("insert into miniprograme.poorPerformer(school, class_name, teacher_name, situation, remark, grade, check_date, class_period, subject) values(#{school}, #{className}, #{teacherName}, #{situation}, #{remark}, #{grade}, #{checkDate}, #{classPeriod}, #{subject})")
    void addPoorPerform(M_PoorPerformDTO poorPerformDTO);

    List<M_ReadingPoorPerform> getReadingPoorPerformByDateRange(String school, LocalDate startDate, LocalDate endDate, String grade);
}
