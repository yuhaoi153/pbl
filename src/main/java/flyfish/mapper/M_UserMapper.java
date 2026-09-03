package flyfish.mapper;

import flyfish.pojo.M_Login;
import flyfish.pojo.M_User;
import flyfish.pojo.VO.M_PersonalCurrentStudentVO;
import flyfish.pojo.VO.M_StudentUserVO;
import flyfish.pojo.VO.M_TeacherUserVO;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface M_UserMapper {
    @Select("select * from miniprograme.user where name=#{username} and password=#{password} and school=#{school}")
    List<M_Login> login(M_Login mLogin);

    void deleteAllSameUsers(List<M_User> userList);

    Integer insertUserAndGetId(M_User user);

    @Delete("delete from miniprograme.user where id = #{studentId}")
    void deleteUserById(Integer studentId);

    void deleteUsersByIds(List<Integer> studentIds);

    @Select("select * from miniprograme.user where name = #{name} and phone = #{phone}")
    M_User getUserByNameAndPhone(String name, String phone);

    //修改用户信息
    @Update("update miniprograme.user set name = #{name}, phone = #{phone} where id = #{id}")
    void updateUser(M_User user);
//
//    void deleteBatchSameUsers(List<M_User> batch);

    @Select("select id, name as studentName from miniprograme.user where school = #{school} and grade = #{grade} and className = #{className}")
    List<M_PersonalCurrentStudentVO> getCurrentStudentList(String school, String grade, Integer className);

    @Select("select id from miniprograme.user where school = #{school} and grade = #{grade} and className = #{classNum} and name = #{studentName} limit 1")
    Integer getStudentIdByNameAndClass(String school, String grade, Integer classNum, String studentName);

    void deleteTeacherUser(List<M_User> userList);

    void batchInsertUser(List<M_User> userList);

    @Select("select id, name as teacherName, school,phone ,password from miniprograme.user where school = #{school} and role = '教师'")
    List<M_TeacherUserVO> getTeacherUserListBySchool(String school);

    //根据学校查询学生用户列表
    @Select("select id, name as studentName, phone, school, password from miniprograme.user where school = #{school} and role = '学生'")
    List<M_StudentUserVO> getStudentUserListBySchool(String school);

    void deleteStudentUser(List<M_User> userList);

    @Select("select  * from miniprograme.user where school = #{school} and name = #{teacherName} and role = '教师' limit 1")
    M_User getTeacherUserBySchoolAndTeacherName(String school, String teacherName);

    List<Integer> getIdsByUserList(List<M_User> userList);

    void batchDeleteTeacherUserByIdList(List<Integer> idList);

    @Update("update miniprograme.user set name = #{teacherName}, phone = #{phone}, password = #{password} where id = #{id}")
    void editTeacherUserById(M_TeacherUserVO mTeacherUserVO);

    @Update("update miniprograme.user set name = #{studentName}, phone = #{phone}, password = #{password} where id = #{id}")
    void updateStudentUser(M_StudentUserVO mStudentUserVO);

    @Select("select id from miniprograme.user where school = #{school} and name = #{teacherName} and role = '教师' limit 1")
    Integer getIdBySchoolAndTeacherNameRole(String school, String teacherName);

    @Select("select phone from miniprograme.user where school = #{school} and name = #{studentName} and role = '学生'")
    List<String> getPhoneByStudentNameAndSchool(String studentName, String school);


    void insertStudentUser(M_StudentUserVO mStudentUserVO);

    @Select("select phone from miniprograme.user where id = #{userId} limit 1")
    String getPhoneById(Integer userId);

    @Select("select password from miniprograme.user where id = #{userId} limit 1")
    String getPasswordById(Integer userId);

    @Select("select * from miniprograme.user where name = #{userName} and password = #{password} and phone = #{phone} and school = #{school}")
    List<M_Login> confirmUser(String userName, String password, String phone, String school);
}
