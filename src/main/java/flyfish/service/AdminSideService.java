package flyfish.service;

import flyfish.pojo.DTO.QueryPassTaskDTO;
import flyfish.pojo.DTO.RecordQueryDTO;
import flyfish.pojo.EditStudentForm;
import flyfish.pojo.PassTask;
import flyfish.pojo.Record;
import flyfish.pojo.StudentInfo;
import flyfish.pojo.VO.RecordVO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface AdminSideService {
    /**
     * 传新的密码文件
     * @param file
     * @return
     */
    String addPassWordFile(MultipartFile file) throws IOException;


    /**
     * 根据班级和人名查询学生家长
     * @param classNumber
     * @param studentName
     * @return
     */
    List<EditStudentForm> getALLContent(String classNumber, String studentName);

    /**
     * 新增学生用户
     * @param editStudentForm
     * @return
     */
    EditStudentForm addstudentPassword(EditStudentForm editStudentForm);

    /**
     * 编辑学生用户
     * @param editStudentForm
     * @return
     */
    String editstudentPassword(EditStudentForm editStudentForm);

    /**
     * 删除指定学生用户
     * @param id
     */
    void deleteClassPassword(Integer id);

    /**
     * 获取所有的学生信息
     * @param classNumber
     * @param name
     * @return
     */
    List<StudentInfo> getALLStudentInfoContent(String classNumber, String name);

    /**
     * 修改学生信息
     * @param studentInfo
     * @return
     */
    String editstudentInfo(StudentInfo studentInfo);

    /**
     * 删除用户信息
     * @param id
     */
    void deleteClassInfo(Integer id);

    /**
     * 新增学生用户信息
     * @param studentInfo
     * @return
     */
    StudentInfo addstudentInfo(StudentInfo studentInfo);

    /**
     * 查询所有作业表现情况
     * @param recordQueryDTO
     * @return
     */
    List<RecordVO> getALLRecordContent(RecordQueryDTO recordQueryDTO);

    /**
     * 编辑作业表现
     * @param recordVO
     * @return
     */
    String editRecordContent(RecordVO recordVO);

    /**
     * 新增作业表现
     * @param recordVO
     * @return
     */
    RecordVO addRecordContent(RecordVO recordVO);

    /**
     * 删除作业表现
     * @param ids
     */
    void deleteRecord(List<Integer> ids);


    /**
     * 根据id查询所有的记录
     * @param ids
     * @return
     */
    List<Record> findAllById(List<Integer> ids);

    /**
     * 导出记录
     * @param records
     * @return
     */
    ResponseEntity<byte[]> exportRecords(List<Record> records) throws IOException;


    /**
     * 查询所有的班级
     * @return
     */
    List<String> autoqueryclassNumber();

    /**
     * 查询所有的学生姓名

     * @return
     */
    List<String> autoqueryname();


    /**
     * 自动查询所有过关内容
     * @return
     */
    List<String> autoqueryPasscontent();

    /**
     * 查询所有的过关任务
     * @param queryPassTaskDTO
     * @return
     */
    List<PassTask> getALLPassContent(QueryPassTaskDTO queryPassTaskDTO);

    /**
     * 新增过关任务
     * @param passTask
     * @return
     */
    PassTask addRecordPass(PassTask passTask);

    String editRecordPass(PassTask passTask);

    /**
     * 删除过关任务
     * @param ids
     */
    void deleteRecordPass(List<Integer> ids);

    /**
     * 根据id查询所有的过关任务
     * @param ids
     * @return
     */
    List<PassTask> findAllPassById(List<Integer> ids);

    /**
     * 导出过关任务
     * @param records
     * @return
     */
    ResponseEntity<byte[]> exportRecordsPass(List<PassTask> records);
}
