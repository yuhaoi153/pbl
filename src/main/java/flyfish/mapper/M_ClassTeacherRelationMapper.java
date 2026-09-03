package flyfish.mapper;

import flyfish.pojo.DTO.M_DeleteTeacherInfoDTO;
import flyfish.pojo.M_ClassTeacherRelation;
import flyfish.pojo.M_TeacherInfo;
import flyfish.pojo.M_TeacherRole;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface M_ClassTeacherRelationMapper {


    void saveRelations(List<M_ClassTeacherRelation> relationList);


    void deleteRelations(List<M_ClassTeacherRelation> relationList);

    @Select("select * from miniprograme.class_teachers where school = #{school}")
    List<M_ClassTeacherRelation> getTeacherData(String school);

    List<M_TeacherInfo> getTeacherInfoByTeacherName(String content, String school);

    List<M_TeacherInfo> getTeacherInfoByClassName(String content, String school);

    void deleteClassTeacherRelation(M_DeleteTeacherInfoDTO mDeleteTeacherInfoDTO);


    @Select("select * from miniprograme.class_teachers where school = #{school} and class_id= #{classId} and teacher_Id = #{teacherId}")
    M_ClassTeacherRelation getClassTeacher(M_ClassTeacherRelation mClassTeacherRelation);

    @Select("select class_name from miniprograme.class_teachers where school = #{school} and teacher_name = #{teacherName}")
    List<String> getClassNameBySchoolTeacherName(String school, String teacherName);

    @Select("select class_name from miniprograme.class_teachers where school = #{school} and teacher_name = #{teacherName} and headTeacher = #{headTeacher} limit 1")
    String getClassNameBySchoolTeacherNameAndTitle(String school, String teacherName, String headTeacher);

    @Delete("delete from miniprograme.class_teachers where school = #{school} and headTeacher = #{isheadTeacher}")
    void deleteAllHeadTeacher(String school, String isheadTeacher);

    @Insert("insert into miniprograme.class_teachers(school, class_name, teacher_name, headTeacher,class_id, teacher_id) values(#{school}, #{className}, #{teacherName}, #{headTeacher}, #{classId}, #{teacherId})")
    void insetClassTeacherRelation(M_ClassTeacherRelation classTeacherRelation);

    @Update("update miniprograme.class_teachers set headTeacher = '否' where school = #{school} and teacher_name = #{teacherName} and class_name = #{className}")
    void setHeadTeacherNoBySchoolAndTeacherNameAndClassName(String school, String teacherName, String className);

    @Delete("delete from miniprograme.class_teachers where teacher_id= #{teacherId} and class_id = #{classId}")
    void deleteRelationByTeacherIdAndClassId(Integer teacherId, Integer classId);

    @Delete("delete from miniprograme.class_teachers where school = #{school} and teacher_name = #{teacherName}")
    void deleteClassNameBySchoolAndTeacherName(String school, String teacherName);

    void insetClassTeacherRelationList(List<M_ClassTeacherRelation> mClassTeacherRelationList);

    void deleteRelationBySchoolAndClassNameList(String school, List<String> deleteClassNameList);

    @Update("update miniprograme.class_teachers set headTeacher = '否' where school = #{school} and teacher_name = #{teacherName}")
    void setHeadTeacherNoBySchoolAndTeacherName(String school, String teacherName);

    @Select("select  id from miniprograme.class_teachers where school = #{school} and teacher_name = #{teacherName} and headTeacher = #{headTeacher} limit 1")
    Integer getIdBySchoolAndTeacherNameAndTitle(String school, String teacherName, String headTeacher);

    @Update("update miniprograme.class_teachers set headTeacher = #{headTeacher} , school = #{school} , teacher_name = #{teacherName} , class_name = #{className} ,class_id= #{classId},teacher_id=#{teacherId} where id = #{id}")
    void updateClassTeacherRelation(M_ClassTeacherRelation classTeacherRelation);

    @Select("select id from miniprograme.class_teachers where school = #{school} and class_name = #{className} limit 1")
    Integer getIdByClassNameSchool(String school, String className);

    @Update("update miniprograme.class_teachers set headTeacher = '否' where school = #{school} and class_name = #{className}")
    void setHeadTeacherNoByClassNameSchool(String school, String className);

    @Delete("delete from miniprograme.class_teachers where school = #{school} and class_name = #{className}")
    void deleteByClassSchool(String school, String className);

    @Delete("delete from miniprograme.class_teachers where school = #{school} and class_name = #{className} and teacher_name = #{teacherName}")
    void deleteByClassSchoolTeacher(String school, String className,String teacherName);

    @Select("select id from miniprograme.class_teachers where school = #{school} and class_name = #{className} and teacher_name = #{teacherName} limit 1")
    Integer geIdByClassNameTeacherNameSchool(M_ClassTeacherRelation headTeacherRelation);

    @Select("select class_name from miniprograme.class_teachers where school = #{school} and teacher_name = #{username} and headTeacher = #{headTeacher} limit 1")
    String getClassNameBySchoolTeacherNameHeadTeacher(String school, String username, String headTeacher);

    @Select("select teacher_name from miniprograme.class_teachers where school = #{school} and class_name = #{classNumber}")
    List<String> getTeacherNameByClass(String school, String classNumber);
}
