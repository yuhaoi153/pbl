package flyfish.mapper;

import flyfish.pojo.M_ClubStudentInfo;
import flyfish.pojo.M_Login;
import flyfish.pojo.M_User;
import flyfish.pojo.VO.M_ClubStudentInfoVO;
import org.apache.ibatis.annotations.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface M_ClubStudentInfoMapper {


    //根据userId获取年级
    @Select("select studentGrade from miniprograme.clubStudentInfo where userId = #{id}")
    String getByUserId(Integer id);

    //根据userId获取学生信息
    @Select("select * from miniprograme.clubStudentInfo where userId = #{userId}")
    M_ClubStudentInfo getAllByUserId(Integer userId);

    //根据userId修改学生社团选择
    @Update("update miniprograme.clubStudentInfo set firstChoiceId = #{firstChoiceId}, secondChoiceId = #{secondChoiceId}, thirdChoiceId = #{thirdChoiceId} where userId = #{userId}")
    void updateClubChooseByUserId(Integer firstChoiceId, Integer secondChoiceId, Integer thirdChoiceId, Integer userId);

    //拿到所有第一志愿不为空或者0的学生，而且第一志愿不为1的学生
    @Select("select * from miniprograme.clubStudentInfo where firstChoiceId is not null and firstChoiceId != 0 and firstChooseStatus != 1")
    List<M_ClubStudentInfo> getFirstChoiceNotAdmittedStudents();

    //批量修改学生的第一志愿状态

    void batchUpdateFirstStudentStatus(List<Integer> selectedStudents, Integer activeClubId);

    void batchUpdateSecondStudentStatus(List<Integer> selectedStudents, Integer activeClubId);

    void batchUpdateThirdStudentStatus(List<Integer> selectedStudents, Integer activeClubId);


    //拿到所有第二志愿不为空或者0的学生，而且第二志愿不为1的学生且第一志愿不为1
    @Select("select * from miniprograme.clubStudentInfo where secondChoiceId is not null and secondChoiceId != 0 and secondChooseStatus != 1 and firstChooseStatus != 1")
    List<M_ClubStudentInfo> getSecondChoiceNotAdmittedStudents();

    //拿到所有志愿都为0或者空的学生
    @Select("select * from miniprograme.clubStudentInfo where (firstChooseStatus is null or firstChooseStatus = 0) and (secondChooseStatus is null or secondChooseStatus = 0) and (thirdChooseStatus is null or thirdChooseStatus= 0)")
    List<M_ClubStudentInfo> getNotAdmitedStudents();

    //批量没有被录取学生的第一志愿状态
    void batchUpdateRemainClubStudentStatus(List<Integer> selectedStudents, Integer remainId);

    @Select("select admissionSemester from miniprograme.clubsSupplement ")
    String getAdmissionSemester();



    void deleteAllSameClubStudentInfos(List<M_User> userList);

    @Insert("insert into miniprograme.clubStudentInfo (userId, studentName, studentGrade, studentClass, admissionSemester,phone) values (#{id}, #{name}, #{grade}, #{classNumber}, #{admissionSemester}, #{phone})")
    void insertClubStudentInfoFromUser(M_User userList);

    @Update("update miniprograme.clubStudentInfo set firstChooseStatus = 0, secondChooseStatus = 0, thirdChooseStatus = 0 where admissionSemester = #{admissionSemester}")
    void resetAllClubChooseStatus(String admissionSemester);

    @Select("select * from miniprograme.clubStudentInfo where admissionSemester = #{admissionSemester} order by studentGrade")
    List<M_ClubStudentInfoVO> getAllAdminStudentInfo(String admissionSemester);

    @Update("update miniprograme.clubStudentInfo set firstChooseStatus = 0, firstChoiceId = null  where firstChoiceId = #{clubId}")
    void resetFirstStatusByClubId(Integer clubId);

    @Update( "update miniprograme.clubStudentInfo set secondChooseStatus = 0, secondChoiceId = null  where secondChoiceId = #{clubId}")
    void resetSecondStatusByClubId(Integer clubId);

    @Update(" update miniprograme.clubStudentInfo set thirdChooseStatus = 0, thirdChoiceId = null  where thirdChoiceId = #{clubId}")
    void resetThirdStatusByClubId(Integer clubId);

    @Delete("delete from miniprograme.clubStudentInfo where userId = #{studentId}")
    void deleteClubStudentInfoByUserId(Integer studentId);


    List<Integer> getUserIdsByGrade(String grade, String admissionSemester);

    void deleteClubStudentInfosByUserIds(List<Integer> studentIds,String admissionSemester);

    @Insert("insert into miniprograme.clubStudentInfo (userId, studentName, studentGrade, studentClass,firstChoiceId,firstChooseStatus,secondChoiceId,secondChooseStatus,thirdChoiceId,thirdChooseStatus, admissionSemester, phone)  values (#{userId}, #{studentName}, #{studentGrade}, #{studentClass}, #{firstChoiceId}, #{firstChooseStatus}, #{secondChoiceId}, #{secondChooseStatus}, #{thirdChoiceId}, #{thirdChooseStatus}, #{admissionSemester}, #{phone})")
    void insertClubStudentInfoFromVO(M_ClubStudentInfoVO newStudent);

    @Update("update miniprograme.clubStudentInfo set studentName = #{studentName}, studentGrade = #{studentGrade}, studentClass = #{studentClass}, firstChoiceId = #{firstChoiceId}, secondChoiceId = #{secondChoiceId}, thirdChoiceId = #{thirdChoiceId}, phone = #{phone} where userId = #{id}")
    void updateClubStudentInfoFromVO(M_ClubStudentInfoVO editStudent);

    @Select("select firstChoiceId from miniprograme.clubStudentInfo where userId = #{id}")
    Integer getFirstChoiceId(Integer id);

    @Select("select firstChooseStatus from miniprograme.clubStudentInfo where userId = #{id}")
    Integer getFirstChooseStatus(Integer id);

    @Update("update miniprograme.clubStudentInfo set firstChooseStatus = 0 where userId = #{id}")
    void setFirstStatusZeroByClubId(Integer id);

    @Select("select secondChoiceId from miniprograme.clubStudentInfo where userId = #{id}")
    Integer getSecondChoiceId(Integer id);

    @Select("select secondChooseStatus from miniprograme.clubStudentInfo where userId = #{id}")
    Integer getSecondChooseStatus(Integer id);

    @Update("update miniprograme.clubStudentInfo set secondChooseStatus = 0 where userId = #{id}")
    void setSecondStatusZeroByClubId(Integer id);

    @Select("select thirdChoiceId from miniprograme.clubStudentInfo where userId = #{id}")
    Integer getThirdChoiceId(Integer id);

    @Select("select thirdChooseStatus from miniprograme.clubStudentInfo where userId = #{id}")
    Integer getThirdChooseStatus(Integer id);

    @Update("update miniprograme.clubStudentInfo set thirdChooseStatus = 0 where userId = #{id}")
    void setThirdStatusZeroByClubId(Integer id);

    @Update("update miniprograme.clubStudentInfo set firstChooseStatus = 1 where userId = #{id}")
    void setFirstChooseStatusOne(Integer id);

    @Update("update miniprograme.clubStudentInfo set secondChooseStatus = 1 where userId = #{id}")
    void setSecondChooseStatusOne(Integer id);

    @Update("update miniprograme.clubStudentInfo set thirdChooseStatus = 1 where userId = #{id}")
    void setThirdChooseStatusOne(Integer id);

    @Select("select * from miniprograme.clubStudentInfo where thirdChoiceId is not null and thirdChoiceId != 0 and thirdChooseStatus != 1 and firstChooseStatus != 1 and secondChooseStatus != 1")
    List<M_ClubStudentInfo> getThirdChoiceNotAdmittedStudents();
}
