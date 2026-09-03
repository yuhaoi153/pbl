package flyfish.mapper;


import flyfish.pojo.M_TeacherRole;
import flyfish.pojo.M_User;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface M_TeacherRoleMapper {

    @Select("select * from miniprograme.teacherRole where userId = #{id} and title = #{headTeacher}")
    M_TeacherRole getTeacherRoleByUserIdAndTitle(Integer id, String headTeacher);

    /**
     * 批量删除教师角色信息
     * @param userList
     */
    void batchDeleteTeacherRoleByUserIds(List<M_User> userList);

    void batchInsertTeacherRole(List<M_TeacherRole> teacherRoleList);

    @Select("select title from miniprograme.teacherRole where userId = #{id}")
    List<String> getTeacherRoleByUserId(Integer id);

    //根据教师姓名、学校、教师ID和职务添加教师角色信息
    @Insert("insert into miniprograme.teacherRole (teacherName, school, userId, title) values (#{teacherName}, #{school}, #{id}, #{title})")
    void addHeadTeacherRoleByUserId(String teacherName, String school, Integer id, String title);

    @Select("select title from miniprograme.teacherRole where school = #{school}")
    List<String> getTeacherRoleTypeListBySchool(String school);

    @Delete("delete from miniprograme.teacherRole where school = #{school} and title = #{title}")
    void deleteTeacherRoleBySchool(String school, String title);

    void batchDeleteTeacherRoleByIdList(List<Integer> idList);

    List<M_TeacherRole> getTeacherSchoolByUserIds(List<Integer> idList);

    @Select("select teacherName from miniprograme.teacherRole where school = #{school} and title = #{title}")
    List<String> getTeacherNameByHeadTeacherSchool(String school, String title);

    @Select("select title from miniprograme.teacherRole where userId = #{id} ")
    List<String> getTitleListByUserId(Integer id);

    @Select("select id from miniprograme.teacherRole where school = #{school} and title = #{title} and teacherName = #{username}")
    Integer getIdBySchoolAndUsername(String school, String username, String title);


}
