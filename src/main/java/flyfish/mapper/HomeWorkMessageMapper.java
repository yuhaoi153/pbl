package flyfish.mapper;

import flyfish.pojo.HomeWorkMessage;
import flyfish.pojo.VO.HomeworkMessageVO;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface HomeWorkMessageMapper {
    @Select("select user_id from chat.userList where username = #{classNumber} and school = #{school}")
    Integer getIdByNameShool(String classNumber, String school);

    @Insert("insert into chat.homeworkMessage (sender_id, receiver_id, message_type, message_content,  subject, className, school, check_date, user_name, supplementary, file_size, duration) " +
            "values (#{senderId},#{receiverId},#{messageType},#{messageContent},#{subject},#{className},#{school},#{checkDate},#{userName},#{supplementary},#{fileSize},#{duration})")
    void insertSingleItem(HomeWorkMessage homeWorkMessage);

    @Select("select * from chat.homeworkMessage where sender_id = #{senderId} and check_date = #{checkDate}  order by sent_at desc")
    List<HomeworkMessageVO> getHomeworkMessage(Integer senderId, LocalDate checkDate);

    @Delete("delete from chat.homeworkMessage where id = #{id}")
    void deleteHomeworkMessageById(Integer id);

    @Select("select * from chat.homeworkMessage where sender_id = #{senderId} and check_date = #{checkDate} and className = #{className}  order by sent_at desc")
    List<HomeworkMessageVO> getHomeworkMessageByClassName(Integer senderId, LocalDate checkDate, String className);
}
