package flyfish.mapper;


import flyfish.pojo.User;
import flyfish.pojo.UserName;
import flyfish.pojo.VO.UserNameVO;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface UserMapper {

    /*
    根据用户名获取用户
     */
    @Select("select * from homework.user where username = #{username}")
    User getByName(String username);

    /**
     * 根据用户名查找用户id
     * @param username
     * @return
     */
    @Select("select id from homework.user where username = #{username}")
    Integer geyByusername(String username);

    /**
     * 根据用户名查教师姓名
     * @param censor
     * @return
     */
    @Select("select name from homework.user where username = #{username}")
    String getCensorByUsername(String censor);

    /**
     * 获取所有的用户名
     * @return
     */
    @Select("select username, name from homework.user")
    List<UserName> getAllName();

    /**
     * 拿到真实密码
     * @param username
     * @return
     */
    @Select("select password from homework.user where username = #{username}")
    String getRealPassword(String username);

    /**
     * 获取所有内容
     * @return
     */
    @Select("select * from homework.user")
    List<User> getALL();

    /**
     * 修改用户信息
     * @param editForm
     * @return
     */

    @Update("update homework.user set username = #{username}, password = #{password}, role_id =#{roleId} " +
            "where id =#{id} ")
    void editclassPassword(User editForm);


    /**
     * 删除用户
     * @param id
     */
    @Delete("delete from homework.user where id =#{id}")
    void deleteClassPassword(Integer id);

    /**
     * 新增用户
     * @param newUser
     * @return
     */

    Integer addclassPassword(User newUser);

    @Select("select username from homework.user")
    List<String> parentQueryClassNumber();

    @Select("select username from pbl.user where id = #{senderId}")

    String getUsernameBySenderId(Integer integer);

    @Select("select id from pbl.user where username = #{username} and school = #{school}")
    Integer getPBLIdByUserNameAndSchool(String school, String username);

    @Select("select username from pbl.user where role_id = #{roleId} and school = #{school}")
    List<String> getAdminUserNameList(Integer roleId, String school);


}
