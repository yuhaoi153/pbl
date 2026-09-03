package flyfish.mapper;

import flyfish.pojo.M_StudentInfo;
import flyfish.pojo.M_User;
import flyfish.pojo.VO.M_PersonalCurrentStudentVO;
import flyfish.pojo.VO.M_StudentUserVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface M_StudentInfoMapper {
    @Select("select * from miniprograme.studentInfo where userId = #{id}")
    M_StudentInfo getAllByUserId(Integer id);


    void deleteStudentInfoByUserIds(List<M_User> userList);

    void batchInsertStudentInfo(List<M_StudentInfo> studentInfoList);

    void deleteStudentInfoByUserIdList(List<Integer> idList);

    @Update("update miniprograme.studentInfo set studentName = #{studentName}, school = #{school}, grade = #{grade}, className = #{className},year = #{year} ,studentNumber = #{studentNumber} where userId = #{userId}")
    void updateStudentInfo(M_StudentInfo studentInfo);

    @Select("select * from miniprograme.studentInfo where school = #{school}")
    List<M_StudentInfo> getStudentInfoListBySchool(String school);

    void batchUpdateStudentInfo(List<M_StudentInfo> updatedStudentInfoList);

    @Update("update miniprograme.studentInfo set studentNumber = #{studentNumber} where userId = #{userId}")
    void updateStudentInfoNumber(M_StudentInfo studentInfo);
    @Select("select id from miniprograme.studentInfo where userId = #{id}")
    Integer getIdByUserId(Integer id);

    @Select("select id from miniprograme.studentInfo where school = #{school} and studentNumber = #{studentNumber} limit 1")
    Integer getIdByStudentNumber(String school, String studentNumber);

    @Select("select studentName from miniprograme.studentInfo where userId = #{id}")
    String getStudentNameById(Integer id);

    @Select("select * from miniprograme.studentInfo where school = #{school} and grade = #{grade} and className = #{className}")
    List<M_StudentInfo> getStudentInfoListBySchoolAndGradeClassName(String school, String grade, Integer className);

    @Select("select * from miniprograme.studentInfo where school = #{school} and grade = #{grade}")
    List<M_StudentInfo> getStudentInfoListBySchoolAndGrade(String school, String grade);

    @Select("select userId as id , studentName from miniprograme.studentInfo where school = #{school} and grade = #{grade} and className = #{className}")
    List<M_PersonalCurrentStudentVO> getCurrentStudentList(String school, String grade, Integer className);

    @Select("select userId from miniprograme.studentInfo where school = #{school} and grade = #{grade} and className = #{classNum} and studentName = #{studentName} limit 1")
    Integer getUserIdBySchoolGradeClassStudentName(String school, String grade, Integer classNum, String studentName);

    @Select("select studentName from miniprograme.studentInfo")
    List<String> getAll();

    List<M_StudentInfo> getStudentInfoByUserIdList(List<Integer> idList);

    @Select("select studentNumber from miniprograme.studentInfo where userId = #{id}")
    String getStudentNumberByUserId(Integer id);

    @Select("select id from miniprograme.studentInfo where school = #{school} and grade = #{grade} and className = #{className} and studentName = #{studentName} limit 1")
    Integer getNameByShoolClassAndStudentName(String school, String grade, Integer className, String studentName);

    @Select("select studentName from miniprograme.studentInfo where school = #{school} and grade = #{grade} and className = #{className}")
    List<String> getStudentNamesBySchoolAndClassName(String school, String grade, Integer className);


}
