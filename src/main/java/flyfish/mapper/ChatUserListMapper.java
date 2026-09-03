package flyfish.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface ChatUserListMapper {
    @Select("select user_id from chat.userList where username = #{teacherName} and school = #{school} limit 1")
    Integer getIdByNameSchool(String teacherName, String school);

    @Select("select status from chat.userList where user_id = #{receiverId} limit 1")
    String getStatusById(Integer receiverId);
}
