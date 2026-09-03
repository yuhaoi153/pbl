package flyfish.mapper;

import flyfish.pojo.M_Club;
import flyfish.pojo.VO.ClubVO;
import org.apache.ibatis.annotations.*;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface M_ClubMapper {
    @Select("select * from miniprograme.clubs where admission_semester = #{admissionSemester} and grade = #{grade} and is_active = #{isActive}")
    List<ClubVO> getClubInfo(String admissionSemester, String grade, Boolean isActive);

    //根据社团id获取社团名称
    @Select("select club_name from miniprograme.clubs where id = #{clubId}")
    String getClubNameById(Integer clubId);

    //根据社团id修改社团报名人数
    @Update("update miniprograme.clubs set clubs.currentStudents = currentStudents + #{j} where id = #{clubId}")
    void updateClubNumberById(Integer clubId, Integer j);


    @Select("select currentStudents from miniprograme.clubs where id = #{integer}")
    Integer getCurrentStudentsById(Integer integer);

    @Select("select admissionSemester from miniprograme.clubsSupplement ")
    String getCurrentAdmissionSemester();

    //获取当前招生批次的所有启用社团id
    @Select("select id from miniprograme.clubs where admission_semester = #{admissionSemester} and is_active = true")
    List<Integer> getActiveClubIds(String admissionSemester);

    //根据ID获得社团的分配状态
    @Select("select finished from miniprograme.clubs where id = #{activeClubId}")
    String getFinishedById(Integer activeClubId);

    //根据社团id获取该社团的最大招生人数
    @Select("select maxStudents from miniprograme.clubs where id = #{activeClubId}")
    Integer getMaxStudentsById(Integer activeClubId);

    @Select("select id from miniprograme.clubs where admission_semester = #{admissionSemester} and finished = #{clubStatus}")
    List<Integer> getActiveClubIdsAndStatus(String admissionSemester, String clubStatus);

    @Select("select grade from miniprograme.clubs where id = #{remainId}")
    String getGradeById(Integer remainId);

    //根据社团id和招生批次获取社团指导老师
    @Select("select teacher from miniprograme.clubs where id = #{clubId} and admission_semester = #{admissionSemester}")
    String getTeacherByClubId(Integer clubId, String admissionSemester);

    //根据社团id和招生批次获取社团地点
    @Select("select position from miniprograme.clubs where id = #{clubId} and admission_semester = #{admissionSemester}")
    String getPositionByClubId(Integer clubId, String admissionSemester);

    //批量插入社团信息
    void insertClubList(List<M_Club> clubList);

    //批量删除，同年级同名称的重复社团
    void deleteAllSameClubs(List<M_Club> clubList);

    @Update("update miniprograme.clubs set deadline = #{deadline} where admission_semester = #{admissionSemester}")
    void resetClubDeadline(String admissionSemester, LocalDateTime deadline);

    @Select("select * from miniprograme.clubs where admission_semester = #{admissionSemester} order by grade, club_name")
    List<ClubVO> getAllAdminClub(String admissionSemester);

    @Update("update miniprograme.clubs set finished = '未分配完' where admission_semester = #{admissionSemester}")
    void setAllFinishedStatusToNotAssigned(String admissionSemester);

    //根据社团id删除社团
    @Delete("delete from miniprograme.clubs where id = #{clubId}")
    void deleteClubById(Integer clubId);

    List<Integer> getClubIdsByGrade(String grade, String admissionSemester);


    void deleteClubsByGrade(String grade, String admissionSemester);

    //新增社团
    @Insert("insert into miniprograme.clubs (club_name, teacher, description, maxStudents, category, grade, deadline, admission_semester, currentStudents, position, finished) values (#{clubName}, #{teacher}, #{description}, #{maxStudents}, #{category}, #{grade}, #{deadline}, #{admissionSemester}, 0, #{position}, '未分配完')")
    void insertClub(M_Club newClub);

    //根据社团名称和年级获取社团id
    @Select("select id from miniprograme.clubs where club_name = #{firstChoiceName} and grade = #{grade} and admission_semester = #{admissionSemester} limit 1")
    Integer getClubIdByNameAndGrade(String firstChoiceName, String grade, String admissionSemester);

    //修改社团信息
    @Update("update miniprograme.clubs set club_name = #{clubName}, teacher = #{teacher}, description = #{description}, maxStudents = #{maxStudents}, category = #{category}, grade = #{grade}, deadline = #{deadline}, position = #{position},finished = #{finished} where id = #{id}")
    void updateClub(M_Club editClub);


}
