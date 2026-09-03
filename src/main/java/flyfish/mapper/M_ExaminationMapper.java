package flyfish.mapper;


import flyfish.pojo.DTO.M_ExamDeleteDTO;
import flyfish.pojo.DTO.M_UpdateExamHideDTO;
import flyfish.pojo.M_Examination;
import flyfish.pojo.VO.M_StudentExamScoreVO;
import org.apache.ibatis.annotations.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface M_ExaminationMapper {

    /**
     * 查询当前考试有多少数据
     * @param school
     * @param grade
     * @param className
     * @param subject
     * @param testName
     * @return
     */
    @Select("select count(*) from miniprograme.examination where school = #{school} and grade = #{grade} and className = #{className} and subject = #{subject} and testName = #{testName} and semester = #{semester}")
    Integer countExistingExam(String school, String grade, Integer className, String subject, String semester,String testName);


    /**
     * 通过Excel批量插入数据
     * @param examinationList
     * @return
     */
    Integer batchInsert(List<M_Examination> examinationList);


    /**
     * 查询有多少条数据
     * @param school
     * @param grade
     * @param className
     * @param subject
     * @param testName
     * @return
     */
    @Select("select * from miniprograme.examination where school = #{school} and grade = #{grade} and className = #{className} and subject = #{subject} and testName = #{testName} and semester = #{semester}")
    List<M_Examination> selectCurrentClassExamRecords(String school, String grade, Integer className, String subject, String testName,String semester);


    /**
     * 查询年级有多少数据
     * @param school
     * @param grade
     * @param subject
     * @param testName
     * @return
     */
    @Select("select * from miniprograme.examination where school = #{school} and grade = #{grade}  and subject = #{subject} and testName = #{testName} and semester = #{semester}")
    List<M_Examination> selectGradeExamRecords(String school, String grade, String subject, String testName,String semester);


    /**
     * 拿到上次考试的考试名称
     * @param school
     * @param grade
     * @param className
     * @param subject
     * @param currentCreateTime
     * @return
     */
    @Select("select testName,semester,createTime from miniprograme.examination where school = #{school} AND grade = #{grade}" +
            "          AND className = #{className}" +
            "          AND subject = #{subject}" +
            "          AND createTime < #{currentCreateTime}" +
            "        ORDER BY" +
            "            createTime DESC" +
            "        LIMIT 1 ")
    M_Examination selectPreviousExamInfo(String school, String grade, Integer className, String subject, LocalDateTime currentCreateTime);

    /**
     * 查询年级维度的上一次考试，不把当前考试中较早上传的班级误认为上一次考试。
     */
    @Select("select testName, semester, createTime from miniprograme.examination " +
            "where school = #{school} and grade = #{grade} and subject = #{subject} " +
            "and createTime < #{currentCreateTime} " +
            "and not (semester = #{currentSemester} and testName = #{currentTestName}) " +
            "order by createTime desc limit 1")
    M_Examination selectPreviousGradeExamInfo(String school,
                                              String grade,
                                              String subject,
                                              String currentSemester,
                                              String currentTestName,
                                              LocalDateTime currentCreateTime);


    /**
     * 拿到已经考过的所有考试名称
     * @param school
     * @param teacherName
     * @return
     */
    @Select("SELECT DISTINCT semester, testName, createTime FROM miniprograme.examination WHERE school = #{school} AND createName = #{teacherName}  and grade = #{grade} and className = #{className} and subject = #{subject} ORDER BY createTime DESC")
    List<M_Examination> getNameBySchool(String school,String teacherName,String grade,Integer className,String subject);


    /**
     * 拿到所有的学生考试信息
     * @param school
     * @param grade
     * @param className
     * @param subject
     * @param semester
     * @param testName
     * @return
     */
    @Select("SELECT * FROM miniprograme.examination WHERE school = #{school} AND grade = #{grade} AND className = #{className} AND subject = #{subject} AND semester = #{semester} AND testName = #{testName}")
    List<M_StudentExamScoreVO> getAllStudentRecord(String school, String grade, Integer className, String subject, String semester, String testName);

    /**
     * 查询一个班级、一个学科的全部历史考试记录。
     */
    @Select("select * from miniprograme.examination " +
            "where school = #{school} and grade = #{grade} " +
            "and className = #{className} and subject = #{subject} " +
            "order by createTime asc, id asc")
    List<M_Examination> selectAllClassExamHistoryRecords(
            String school,
            String grade,
            Integer className,
            String subject
    );

    @Insert("insert into miniprograme.examination (studentName, school, grade, className, score, subject, hide, createName, createTime, updateTime, updateName, imageUrl, testName, status, year, semester) " +
            "VALUES (#{studentName}, #{school}, #{grade}, #{className}, #{score}, #{subject}, #{hide}, #{createName}, #{createTime}, #{updateTime}, #{updateName}, #{imageUrl}, #{testName}, #{status}, #{year}, #{semester})")
    void insertSingle(M_StudentExamScoreVO spercificResult);


    @Update("update miniprograme.examination set status = #{status},score = #{score} where id = #{id}")
    void updateSingleRecord(String status, Integer id, BigDecimal score);

    @Delete("delete from miniprograme.examination where school = #{school} and grade = #{grade} and className = #{className} and semester = #{semester} and testName = #{testName} and subject = #{subject}")
    void deleteExamRecord(M_ExamDeleteDTO mExamDeleteDTO);

    @Select("select distinct className from miniprograme.examination where school = #{school} and grade = #{grade} and subject = #{subject} and semester = #{semester} and testName = #{testName}")
    List<Integer> getClassName(String school, String grade, String subject, String semester, String testName);

    @Update("update miniprograme.examination set hide = #{hide} where school = #{school} and grade = #{grade} and className = #{className} and subject = #{subject} and semester = #{semester} and testName = #{testName}")
    void updateExamHideByClass(M_UpdateExamHideDTO mUpdateExamHideDTO);

    @Update("update miniprograme.examination set hide = #{hide} where school = #{school} and grade = #{grade} and className = #{className} and subject = #{subject} and studentName = #{studentName} and semester = #{semester} and testName = #{testName}")
    void updateExamHideByStudent(M_UpdateExamHideDTO mUpdateExamHideDTO);
}
