package flyfish.mapper;


import flyfish.pojo.VO.M_ClubResultByClassVO;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface M_ClubResultMapper {
    //根据社团id获取该社团的报名人数
    @Select("select count(*) from miniprograme.clubResults where clubId = #{activeClubId}")
    Integer getCountByClubId(Integer activeClubId);

    //根据社团id修改社团的分配状态
    @Update("update miniprograme.clubs set finished = #{clubStatus} where id = #{activeClubId}")
    void updateClubFinishedStatus(Integer activeClubId, String clubStatus);

    @Insert("insert into  miniprograme.clubResults (clubId, studentId, studentName, studentClass, admissionSemester, clubName) values (#{activeClubId}, #{studentId}, #{studentName}, #{studentClass}, #{admissionSemester}, #{clubName})")
    void insertClubResult(Integer activeClubId, Integer studentId, String studentName, String studentClass, String admissionSemester, String clubName);

    //根据学生id批量删除社团结果
    void deleteClubResultsByStudentIds(List<Integer> selectedStudents);

    @Select("select * from miniprograme.clubResults where admissionSemester = #{admissionSemester} order by studentClass, clubId")
    List<M_ClubResultByClassVO> getResultsByClass(String admissionSemester);

    @Select("select * from miniprograme.clubResults where admissionSemester = #{admissionSemester} order by clubId, studentClass")
    List<M_ClubResultByClassVO> getResultsByClub(String admissionSemester);

    @Delete("delete from miniprograme.clubResults where admissionSemester = #{admissionSemester}")
    void deleteAllClubResults(String admissionSemester);

    @Select("select clubName from miniprograme.clubResults where studentId = #{userId} order by id desc limit 1")
    String getClubNameByUserId(Integer userId);

    @Delete("delete from miniprograme.clubResults where clubId = #{clubId} and admissionSemester = #{admissionSemester}")
    void deleteClubResultsByClubId(Integer clubId, String admissionSemester);

    void deleteClubResultsByClubIds(List<Integer> clubIds, String admissionSemester);

    @Select("select clubId from miniprograme.clubResults where studentId = #{studentId} and admissionSemester = #{admissionSemester} order by id desc limit 1")
    Integer getClubIdByUserId(Integer studentId, String admissionSemester);


}
