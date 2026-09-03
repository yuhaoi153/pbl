package flyfish.contoller;


import flyfish.constant.Template;
import flyfish.pojo.DTO.M_EditUserDTO;
import flyfish.pojo.DTO.M_SchoolDTO;
import flyfish.pojo.VO.M_StudentUserVO;
import flyfish.pojo.VO.M_TeacherRoleVO;
import flyfish.pojo.VO.M_TeacherUserVO;
import flyfish.service.M_NotificationService;
import flyfish.service.M_UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.net.URL;
import java.time.LocalDate;
import java.util.List;

@RestController
@Slf4j
public class M_UserController {

    @Autowired
    private M_UserService userService;
    @Autowired
    private M_NotificationService notificationService;

    /**
     * 匹配前端传入的excel名单，找出不同
     * @param file
     * @param school
     * @param className
     * @return
     */
    @PostMapping(value = "/mpi/user/matchStudentName",produces = "application/json;charset=UTF-8")
    public String matchStudentName(@RequestParam("file") MultipartFile file,String school,String grade,Integer className){
        log.info("匹配学生姓名的文件为{},学校为{},班级为{}{}", file.getOriginalFilename(),school,grade,className);
        String result = userService.matchStudentName(file,school,grade,className);
        return result;

    }



    /**
     * 根据前端的文件，批量插入教师信息到数据库中,不删除原有的教师信息，如果有重复的，就不插入
     *
     * @param file
     * @return
     */
    @PostMapping(value = "/mpi/user/batchInsertTeacher", produces = "application/json;charset=UTF-8")
    public String batchInsertTeacher(@RequestParam("file") MultipartFile file) {
        log.info("批量上传的教师信息文件为{}", file.getOriginalFilename());
        String result = userService.batchInsertTeacher(file);
        return result;
    }

    /**
     * 根据前端的文件，批量插入学生信息到数据库中,不删除原有的学生信息，如果有重复的，就不插入
     *
     * @param file
     * @return
     */
    @PostMapping(value = "/mpi/user/batchInsertStudent", produces = "application/json;charset=UTF-8")
    public String batchInsertStudent(@RequestParam("file") MultipartFile file) {
        log.info("批量上传的学生信息文件为{}", file.getOriginalFilename());
        String result = userService.batchInsertStudent(file);
        return result;
    }

    //根据前端文件，批量插入班主任信息到数据库中
    @PostMapping(value = "/mpi/user/batchInsertHeadTeacher", produces = "application/json;charset=UTF-8")
    public String batchInsertHeadTeacher(@RequestParam("file") MultipartFile file) {
        log.info("批量上传的班主任信息文件为{}", file.getOriginalFilename());
        String result = userService.batchInsertHeadTeacher(file);
        return result;
    }

    //根据前端文件，批量插入三类文件到数据库中，教师信息、学生信息和班主任信息
    @PostMapping(value = "/mpi/user/batchInsertThreeFile", produces = "application/json;charset=UTF-8")
    public String batchInsertHeadTeacher(@RequestParam("file") MultipartFile file, @RequestParam("fileType") String fileType) {
        log.info("批量上传的文件为{}, 文件类型为{}", file.getOriginalFilename(), fileType);
        String result = "";
        if (fileType.equals("教师管理")) {
            result = userService.batchInsertTeacher(file);
        } else if (fileType.equals("学生管理")) {
            result = userService.batchInsertStudent(file);
        } else if (fileType.equals("身份管理")) {
            result = userService.batchInsertHeadTeacher(file);
        } else {
            result = "文件类型错误，请上传teacher、student或headTeacher类型的文件";
        }
        return result;
    }


    /**
     * 管理员根据学校获取教师列表
     *
     * @param school
     * @return
     */
    @GetMapping(value = "/mpi/user/getTeacherUser", produces = "application/json;charset=UTF-8")
    public List<M_TeacherUserVO> getTeacherUser(String school) {
        log.info("管理员获取教师列表，学校为{}", school);
        List<M_TeacherUserVO> teacherUserList = userService.getTeacherUserList(school);
        return teacherUserList;
    }

    /**
     * 管理员根据学校获取教师角色
     *
     * @param school
     * @return
     */
    @GetMapping(value = "/mpi/user/getTeacherRole", produces = "application/json;charset=UTF-8")
    public List<M_TeacherRoleVO> getTeacherRole(String school) {
        log.info("管理员获取教师角色列表，学校为{}", school);
        List<M_TeacherRoleVO> teacherRoleList = userService.getTeacherRoleList(school);
        return teacherRoleList;
    }


    /**
     * 管理员根据学校获取学生列表
     *
     * @param school
     * @return
     */
    @GetMapping(value = "/mpi/user/getStudentUser", produces = "application/json;charset=UTF-8")
    public List<M_StudentUserVO> getStudentUser(String school) {
        log.info("管理员获取学生列表，学校为{}", school);
        List<M_StudentUserVO> studentUserList = userService.getStudentUserList(school);
        return studentUserList;
    }


    /**
     * 管理员获取身份类型列表
     *
     * @param school
     * @return
     */
    @GetMapping(value = "/mpi/user/getTeacherRoleTypeList", produces = "application/json;charset=UTF-8")
    public List<String> getTeacherRoleTypeList(String school) {
        log.info("管理员获取身份类型列表{}", school);
        List<String> teacherRoleTypeList = userService.getTeacherRoleTypeList(school);
        return teacherRoleTypeList;
    }


    //从网页端链接下载教师用户模板数据
    @GetMapping("/mpi/user/downLoadTeacherUserExcel")
    public ResponseEntity<InputStreamResource> downloadTeacherUserExcel() {
        log.info("下载教师用户模板。。。");
        String fileUrl = Template.TEACHERUSER;

        try {
            URL url = new URL(fileUrl);
            InputStream inputStream = url.openStream();
            InputStreamResource resource = new InputStreamResource(inputStream);

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"teacherUser.xlsx\"")
                    .body(resource);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.notFound().build();
        }
    }


    //从网页端链接下载学生用户模板数据
    @GetMapping("/mpi/user/downLoadStudentUserExcel")
    public ResponseEntity<InputStreamResource> downloadStudentrUserExcel() {
        log.info("下载学生用户模板。。。");
        String fileUrl = Template.STUDENTUSER;

        try {
            URL url = new URL(fileUrl);
            InputStream inputStream = url.openStream();
            InputStreamResource resource = new InputStreamResource(inputStream);

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"studentUser.xlsx\"")
                    .body(resource);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.notFound().build();
        }
    }


    //从网页端链接下载班主任模板数据
    @GetMapping("/mpi/user/downLoadHeadTeacherExcel")
    public ResponseEntity<InputStreamResource> downloadHeadTeacherExcel() {
        log.info("下载班主任模板。。。");
        String fileUrl = Template.HEADTEACHER;

        try {
            URL url = new URL(fileUrl);
            InputStream inputStream = url.openStream();
            InputStreamResource resource = new InputStreamResource(inputStream);

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"headTeacher.xlsx\"")
                    .body(resource);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.notFound().build();
        }
    }

    //删除教师用户信息
    @PostMapping(value = "/mpi/user/batchDeleteTeacherUser", produces = "application/json;charset=UTF-8")
    public String batchDeleteTeacherUser(@RequestBody M_EditUserDTO editUserDTO) {
        List<Integer> idList = editUserDTO.getIdList();
        log.info("批量删除教师用户信息，教师用户ID列表为{}", idList);
        String result = userService.batchDeleteTeacherUser(idList);
        return result;
    }


    //删除学生用户信息
    @PostMapping(value = "/mpi/user/batchDeleteStudentUser", produces = "application/json;charset=UTF-8")
    public String batchDeleteStudentUser(@RequestBody M_EditUserDTO editUserDTO) {
        List<Integer> idList = editUserDTO.getIdList();
        log.info("批量删除学生用户信息，学生用户ID列表为{}", idList);
        String result = userService.batchDeleteStudentUser(idList);
        return result;
    }

    //删除教师角色信息
    @PostMapping(value = "/mpi/user/batchDeleteTeacherRole", produces = "application/json;charset=UTF-8")
    public String batchDeleteTeacherRole(@RequestBody M_EditUserDTO editUserDTO) {
        List<Integer> idList = editUserDTO.getIdList();
        log.info("批量删除教师角色信息，教师角色ID列表为{}", idList);
        String result = userService.batchDeleteTeacherRoleByIdList(idList);
        return result;

    }


    //编辑教师用户
    @PostMapping(value = "/mpi/user/editTeacherUser", produces = "application/json;charset=UTF-8")
    public String editTeacherUser(@RequestBody M_TeacherUserVO mTeacherUserVO) {
        log.info("编辑教师用户信息，教师用户信息列表为{}", mTeacherUserVO);
        String result = userService.editTeacherUser(mTeacherUserVO);
        return "编辑教师用户成功";
    }

    //新增教师用户
    @PostMapping(value = "/mpi/user/addTeacherUser", produces = "application/json;charset=UTF-8")
    public String addTeacherUser(@RequestBody M_TeacherUserVO mTeacherUserVO) {
        log.info("新增教师用户信息，教师用户信息列表为{}", mTeacherUserVO);
        String result = userService.addTeacherUser(mTeacherUserVO);
        return result;
    }

    //编辑学生用户
    @PostMapping(value = "/mpi/user/editStudentUser", produces = "application/json;charset=UTF-8")
    public String editStudentUser(@RequestBody M_StudentUserVO mStudentUserVO) {
        log.info("编辑学生用户信息，学生用户信息列表为{}", mStudentUserVO);
        String resp = userService.editStudentUser(mStudentUserVO);
        return resp;
    }

    //新增学生用户
    @PostMapping(value = "/mpi/user/addStudentUser", produces = "application/json;charset=UTF-8")
    public String addStudentUser(@RequestBody M_StudentUserVO mStudentUserVO) {
        log.info("新增学生用户信息，学生用户信息列表为{}", mStudentUserVO);
        String resp = userService.addStudentUser(mStudentUserVO);
        return resp;
    }

    //编辑班主任卡片
    @PostMapping(value = "/mpi/user/editHeadTeacher", produces = "application/json;charset=UTF-8")
    public String editHeadTeacher(@RequestBody M_TeacherRoleVO headTeacherRoleVO) {
        log.info("编辑班主任用户信息，班主任用户信息列表为{}", headTeacherRoleVO);
        String resp = userService.editHeadTeacher(headTeacherRoleVO);
        return resp;
    }

    //新增班主任卡片
    @PostMapping(value = "/mpi/user/addHeadTeacher", produces = "application/json;charset=UTF-8")
    public String addHeadTeacher(@RequestBody M_TeacherRoleVO headTeacherRoleVO) {
        log.info("新增班主任用户信息，班主任用户信息列表为{}", headTeacherRoleVO);
        String resp = userService.addHeadTeacher(headTeacherRoleVO);
        return resp;
    }

    /// 重新分配所有学生的学号
    @PostMapping(value = "/mpi/user/reAssignStudentNumber", produces = "application/json;charset=UTF-8")
    public String reAssignStudentNumber(@RequestBody M_SchoolDTO mSchoolDTO) {
        String school = mSchoolDTO.getSchool();
        log.info("重新分配学生学号，学校为{}", school);
        String resp = userService.reAssignStudentNumber(school);
        return resp;
    }

    //给没有学号的学生分配学号
    @PostMapping(value = "/mpi/user/assignStudentNumberForNoStudentNumber", produces = "application/json;charset=UTF-8")
    public String assignStudentNumberForNoStudentNumber(@RequestBody M_SchoolDTO mSchoolDTO) {
        String school = mSchoolDTO.getSchool();
        log.info("给没有学号的学生分配学号，学校为{}", school);
        String resp = userService.assignStudentNumberForNoStudentNumber(school);
        return resp;
    }

    //班主任获取公示栏消息
    @GetMapping(value = "/mpi/headTeacher/getNotification", produces = "application/json;charset=UTF-8")
    public String getNotification(String school, String username, LocalDate startDate, LocalDate endDate,String role,String studentClassName){
        log.info("班主任获取公示栏消息，学校为{},用户名为{}, 开始日期为{},结束日期为{}，角色为{},学生班级为{}", school,username, startDate, endDate, role,studentClassName);

        //如果角色字符串中包含"班主任"，就调用获取班主任消息的方法
        if(role.contains("班主任")){
            String resp = notificationService.getNotificationBySchoolUserName(school,username, startDate, endDate);
             return resp;
        }
        if(role.contains("学生")){
            String resp = notificationService.getNotificationForStudentBySchoolUserName(school,username, startDate, endDate,studentClassName);
             return resp;
        }
        return null;

    }





}
