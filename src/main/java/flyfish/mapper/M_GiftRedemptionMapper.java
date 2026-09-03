package flyfish.mapper;



import flyfish.pojo.Perform;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface M_GiftRedemptionMapper {
    @Select("select giftValue as score, situation ,checkDate as checkdate, name from homework.giftRedemption where className = #{className} and school = #{school} and subject = #{subject} and checkdate >= #{startDate} and checkdate <= #{endDate}")
    List<Perform> getPerformByClassSubject(String className, String school, String subject, LocalDate startDate, LocalDate endDate);

    @Select("select giftValue as score, situation ,checkDate as checkdate,name from homework.giftRedemption where className = #{className} and school = #{school} and subject = #{subject} and checkdate >= #{startDate} and checkdate <= #{endDate} and name = #{name}")
    List<Perform> getPerformByClassSubjectAndName(String className, String school, String subject, LocalDate startDate, LocalDate endDate, String name);

    void addConvertScore(String situation,String giftName,Integer giftValue, String classNumber, List<String> nameList, String subject, LocalDateTime updateTime, String school,Integer year, LocalDate checkDate);
}
