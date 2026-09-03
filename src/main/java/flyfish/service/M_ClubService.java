package flyfish.service;


import flyfish.pojo.DTO.M_ClubChooseDTO;
import flyfish.pojo.M_Club;
import flyfish.pojo.VO.ClubVO;
import flyfish.pojo.VO.M_ClubApplyNumVO;
import flyfish.pojo.VO.M_ClubStudentInfoVO;
import flyfish.pojo.VO.M_ThreeClubChooseVO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface M_ClubService {
    List<ClubVO> getClubInfo(String admissionSemester, String grade, Boolean isActive);

    //修改社团报名信息
    List<M_ClubApplyNumVO>  updateClubChoose(M_ClubChooseDTO clubChooseDTO);

    //根据用户信息和招生批次获取志愿信息
    List<M_ThreeClubChooseVO> getThreeClubChoose(Integer userId, String admissionSemester);


    //分配社团名单
    String assignClubList();

    //按班级导出学生社团excel名单
    ResponseEntity<byte[]> downloadClubListByClass(String admissionSemester);

    //按社团导出学生社团excel名单
    ResponseEntity<byte[]> downloadClubListByClub(String admissionSemester);

    //导入学生社团名单
    String uploadClubList(MultipartFile file);

    //导入学生名单
    String uploadStudentList(MultipartFile file);

    //重置所有学生社团选择,以便于重新分配学生
    String resetAllClubChoose();

    //获取当前招生批次的所有社团信息
    List<ClubVO> getAllAdminClub(String admissionSemester);

    //获取当前招生批次的所有学生信息
    List<M_ClubStudentInfoVO> getAllAdminStudentInfo(String admissionSemester);

    //根据社团id重置该社团的分配状态
    String resetChooseStatusByClubId(Integer clubId);

    //根据年级删除社团信息
    String deleteClubsByGrade(String grade);

    //根据学生id删除学生信息
    String deleteUserById(Integer studentId);

    String deleteUserByGrade(String grade);

    //添加新的社团
    String addNewClub(M_Club newClub);

    //添加新的学生
    String addNewStudent(M_ClubStudentInfoVO newStudent);


    //修改社团信息
    String editClubInfo(M_Club editClub);

    //修改学生信息
    String editStudentInfo(M_ClubStudentInfoVO editStudent);
}
