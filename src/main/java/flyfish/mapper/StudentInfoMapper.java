package flyfish.mapper;

import flyfish.pojo.StudentInfo;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface StudentInfoMapper {
    /**
     * 根据学号批量获取学生的姓名
     * @param studentNumberList
     * @return
     */
    List<String> getnameList(List<String> studentNumberList,String classNumber,String school);

    /**
     * 根据姓名反过来再次批量获取学号
     * @param nameList
     * @return
     */
    List<String> getnewStudentNumberList(List<String> nameList,String classNumber,String school);

    /**
     * 获得全班学生名单
     * @param classNumber
     * @return
     */
    @Select("select name from homework.studentInfo where class_number = #{classNumber} and school = #{school}")
    List<String> getallName(String classNumber,String school);

    /**
     * 获得全班学生学号
     * @param classNumber
     * @return
     */
    @Select("select student_number from homework.studentInfo where class_number = #{classNumber} and school = #{school}")
    List<String> getallStudentNumber(String classNumber,String school);

    /**
     * 根据班级和姓名查询所有的学生信息
     * @param classNumber
     * @param name
     * @return
     */
    List<StudentInfo> getAllContentByCLassName(String classNumber, String name,String school);

    /**
     * 修改学生信息
     * @param studentInfo
     */
    @Update("update homework.studentInfo set name =#{name},student_number =#{studentNumber},class_number =#{classNumber},school = #{school} where id =#{id}")
    void editstudentInfo(StudentInfo studentInfo);

    /**
     * 删除学生信息
     * @param id
     */
    @Delete("delete from homework.studentInfo where id=#{id}")
    void deleteById(Integer id);

    /**
     * 新增学生信息
     * @param studentInfo
     * @return
     */

    Integer addStudentInfo(StudentInfo studentInfo);


    @Select("select username from homework.user")
    List<String> autoqueryclassNumber();

    /**
     * 查询所有的学生姓名
     * @return
     */
    @Select("select name from homework.studentInfo")
    List<String> autoqueryname();

    @Select("select pinyin from homework.studentInfo where class_number = #{classNumber} and school = #{school}")
    List<String> getPinyin(String classNumber,String school);

    @Select("select name from homework.studentInfo where pinyin = #{bestMatch} and class_number = #{classNumber} and school = #{school}")
    String getNameByPinyin(String bestMatch, String classNumber,String school);


    @Select("select name from homework.studentInfo where class_number = #{classNumber} and school = #{school}")
    List<String> getnameListAll(String classNumber,String school);

    Integer queryClassCount(String classNumber);

    List<String> getStudentNumberList(List<String> bestMatchList, String classNumber, String school);

    @Select("select * from homework.studentInfo where school = #{school}")
    List<StudentInfo> getStudentInfoBySchool(String school);

    @Select("select distinct class_number from homework.studentInfo where school = #{school}")
    List<String> getAllHomeworkClassNameBySchool(String school);

    void batchInsertStudentInfo(List<StudentInfo> studentInfoList);

    @Select("select name from homework.studentInfo")
    List<String> getAll();

    @Delete("delete from homework.studentInfo where class_number = #{className} and school = #{school}")
    void deleteByClassAndSchool(String className, String school);

    @Delete("delete from homework.studentInfo where class_number = #{classNumber} and student_number = #{studentNumber} and school = #{school}")
    void deleteBySchoolClassAndStudentNumber(String school, String classNumber, String studentNumber);


    @Select("select student_number from homework.studentInfo where name = #{studentName} and class_number = #{classNumber} and school = #{school} limit 1")
    String getStudentNumberByName(String classNumber, String studentName, String school);
}
