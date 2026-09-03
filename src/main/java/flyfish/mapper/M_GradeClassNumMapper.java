package flyfish.mapper;

import flyfish.pojo.DTO.M_DeleteTeacherInfoDTO;
import flyfish.pojo.DTO.M_GradeClassDTO;
import flyfish.pojo.M_ClassTeacherRelation;
import flyfish.pojo.M_DefaultConfig;
import flyfish.pojo.M_GradeClass;
import flyfish.pojo.M_TeacherInfo;
import org.apache.ibatis.annotations.*;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface M_GradeClassNumMapper {


    void editGradeClassNum(List<M_GradeClass> classList);

    @Delete("delete from miniprograme.grade_classname where grade=#{grade} and school=#{school}")
    void deleteGradeClassNum(String grade, String school);

    //根据学校获取班级名称
    @Select("select grade,class_name from miniprograme.grade_classname where school=#{school}")
    List<M_GradeClass> getGradeClassList(String school);

    //获取班级ID列表
    List<Integer> getClassIdList(List<M_ClassTeacherRelation> relationList);

    @Select("select id from miniprograme.grade_classname where school=#{school} and class_Name=#{className}")
    Integer getClassId(String school, String className);

    void newGradeClassNum(M_GradeClass gradeClass);

    List<String> getClassNameById(List<Integer> classIdList, String school);

    @Select("select grade from miniprograme.grade_classname where school=#{school}")
    List<String> getGrade(String school);

    List<M_TeacherInfo> getTeacherInfoByTeacherName(String content, String school);

    List<M_TeacherInfo> getTeacherInfoBySubject(String content, String school);

    List<M_TeacherInfo> getTeacherInfoByGrade(String content, String school);

    List<M_TeacherInfo> getTeacherInfoByClassName(String content, String school);

    void deleteGradeClassNumByIdList(M_DeleteTeacherInfoDTO mDeleteTeacherInfoDTO);

    @Select("select * from miniprograme.grade_classname where grade=#{grade} and class_Name=#{className} and school=#{school}")
    M_GradeClassDTO getGradeClass(String grade, String className, String school);

    void editGradeClassName(String grade, String className, String school);



    @Delete("delete from miniprograme.grade_classname where grade=#{grade} and class_Name=#{className} and school=#{school}")
    void deleteByAllInfo(String grade, String className, String school);

    @Insert("insert into miniprograme.grade_classname(grade,class_Name,school) values(#{grade},#{className},#{school})")
    void addGradeClass(String grade, String className, String school);

    @Select("select id from miniprograme.grade_classname where school=#{school} and grade=#{grade} and class_Name=#{className}")
    Integer getId(String school, String grade, String className);

    @Select("select * from miniprograme.defaultConfig where school=#{school}")
    List<M_DefaultConfig> getAllBySchool(String school);

    @Update("update miniprograme.defaultConfig set intConfig = #{topNum} where school = #{school} and infoName = #{moralTopNum}")
    void updateIntConfigByInfoName(String moralTopNum, Integer topNum, String school);


    @Delete("delete from miniprograme.defaultConfig where school = #{school} and infoName = #{allinfoName}")
    void deleteAllInfoName(String allinfoName, String school);

    @Select("select id from miniprograme.grade_classname where school=#{school} and class_Name=#{className}")
    Integer getClassIdByGradeAndClassName(String school, String className);

    @Select("select count(*) from miniprograme.grade_classname where school=#{school} and grade=#{grade}")
    Integer getClassNumBySchoolGrade(String school, String grade);

    @Select("select class_Name from miniprograme.grade_classname where school=#{school} and grade=#{grade}")
    List<String> getClassNameBySchoolGrade(String school, String grade);
}
