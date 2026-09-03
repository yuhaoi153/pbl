package flyfish.mapper;

import flyfish.pojo.DTO.M_AddNewTeacher;
import flyfish.pojo.DTO.M_DeleteTeacherInfoDTO;
import flyfish.pojo.M_ClassTeacherRelation;
import flyfish.pojo.M_TeacherData;
import flyfish.pojo.M_TeacherInfo;
import flyfish.pojo.M_TeacherList;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface M_TeacherListMapper {
    //根据条件查询教师信息
    List<M_TeacherInfo> getTeacherInfoByCondition(String type, String content, String school);

    void uploadTeacherList(List<M_TeacherData> teacherDataList);


    //根据学校删除教师列表
    @Delete("delete from miniprograme.teacherList where school = #{school}")
    void deleteTeacherList(String school);

    //根据学校和教师姓名查询教师IDList
    List<Integer> getTeacherIdList(List<M_ClassTeacherRelation> relationList);

    //根据学校和教师姓名查询教师ID
    @Select("select id from miniprograme.teacherList where teacher_name = #{teacherName} and school = #{school} limit 1")
    Integer getTeacherId(String school,String teacherName);

    void newTeacher(M_TeacherData teacherData);


    //根据学校获取教师姓名
    @Select("select teacher_name from miniprograme.teacherList where school = #{school}")
    List<String> getTeacherNameBySubject(String school);


    //根据语数英三个学科获取教师姓名
    @Select("select teacher_name from miniprograme.teacherList where school = #{school} and subject in ('语文','数学','英语')")
    List<String> getTeacherNameByThreeSubjects(String school);


    List<String> getTeacherSubjectList(List<String> readingTeacherNameListNew, String school);


    List<String> getTeacherNameById(List<Integer> teacherIdList, String school);

    @Select("select subject from miniprograme.teacherList where teacher_name = #{s} and school = #{school} limit 1")
    String getSubjectByName(String s, String school);

    @Select("select teacher_name from miniprograme.teacherList where school = #{school} ")
    List<String> getTeacherNameByAllSubject(String school);

    @Select("select subject from miniprograme.teacherList where school = #{school} ")
    List<String> getSubject(String school);

    List<M_TeacherInfo> getTeacherInfoByTeacherName(String content, String school);

    List<M_TeacherInfo> getTeacherInfoBySubject(String content, String school);


    void deleteTeacherListByIDList(M_DeleteTeacherInfoDTO mDeleteTeacherInfoDTO);

    void editTeacherById(M_AddNewTeacher mAddNewTeacher);

    @Select("select id from miniprograme.teacherList where teacher_name = #{teacherName} and subject = #{subject} and school = #{school} limit 1")
    Integer getId(String teacherName, String subject, String school);

    @Select("select id from miniprograme.teacherList where teacher_name = #{teacherName} and school = #{school} limit 1")
    Integer getTeacherIdByName(String teacherName, String school);

    @Select("select school from miniprograme.teacherList where teacher_name = #{userName} limit 1")
    String getSchoolByUserName(String userName);

    @Select("select subject from miniprograme.teacherList where school = #{school} and teacher_name = #{teacherName} limit 1")
    String getSubjectBySchoolAndTeacherName(String school, String teacherName);

    @Update("update miniprograme.teacherList set subject = #{subject} where school = #{school} and teacher_name = #{teacherName}")
    void editSubjectBySchoolAndTeacherName(String school, String teacherName, String subject);

    @Insert("insert into miniprograme.teacherList (teacher_name, subject, school) values (#{teacherName}, #{subject}, #{school})")
    void insetTeacherList(M_TeacherList mTeacherList);

    @Select("select id from miniprograme.teacherList where teacher_name = #{teacherName} and school = #{school} limit 1")
    Integer getOldTeacherId(String teacherName, String school);
}
