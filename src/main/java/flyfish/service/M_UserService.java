package flyfish.service;

import flyfish.pojo.DTO.M_DeleteClassDTO;
import flyfish.pojo.VO.M_HomeworkStundentInfoVO;
import flyfish.pojo.VO.M_StudentUserVO;
import flyfish.pojo.VO.M_TeacherRoleVO;
import flyfish.pojo.VO.M_TeacherUserVO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface M_UserService {

    /// 根据前端的文件，批量插入教师信息到数据库中
    String batchInsertTeacher(MultipartFile file);

    // 管理员根据学校获取教师列表
    List<M_TeacherUserVO> getTeacherUserList(String school);

        //管理员根据学校获取学生列表
    List<M_StudentUserVO> getStudentUserList(String school);

    // 根据前端的文件，批量插入学生信息到数据库中
    String batchInsertStudent(MultipartFile file);

    List<M_TeacherRoleVO> getTeacherRoleList(String school);

    // 管理员根据学校获取教师的职称类型列表
    List<String> getTeacherRoleTypeList(String school);

    /// 根据前端的文件，批量插入班主任信息到数据库中
    String batchInsertHeadTeacher(MultipartFile file);

    /// 批量删除教师用户，根据前端传来的教师用户id列表，删除数据库中对应的教师用户信息
    String batchDeleteTeacherUser(List<Integer> idList);

    String batchDeleteStudentUser(List<Integer> idList);

    String batchDeleteTeacherRoleByIdList(List<Integer> idList);

    String editTeacherUser(M_TeacherUserVO mTeacherUserVO);

    String editStudentUser(M_StudentUserVO mStudentUserVO);

    String editHeadTeacher(M_TeacherRoleVO headTeacherRoleVO);

    String reAssignStudentNumber(String school);

    String assignStudentNumberForNoStudentNumber(String school);

    String addTeacherUser(M_TeacherUserVO mTeacherUserVO);

    String addStudentUser(M_StudentUserVO mStudentUserVO);

    String addHeadTeacher(M_TeacherRoleVO headTeacherRoleVO);

    //班主任根据学校和班级获取学生列表
    List<M_StudentUserVO> getStudentUserListByHeadTeacher(String school, String headTeacherClassName);

    //管理员查询作业管理中的班级名单和学生姓名
    List<M_HomeworkStundentInfoVO> queryHomeworkStudentInfo(String school);

    //管理员同步学生用户名单到作业管理中
    String syncUserList(String school);

    String deleteClass(M_DeleteClassDTO mDeleteClassDTO);

    /**
     * 匹配前端传入的excel名单，找出不同
     * @param file
     * @param school
     * @param className
     * @return
     */
    String matchStudentName(MultipartFile file, String school,String grade,Integer className);
}
