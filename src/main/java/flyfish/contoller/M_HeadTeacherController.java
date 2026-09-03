package flyfish.contoller;

import flyfish.pojo.VO.M_StudentUserVO;
import flyfish.service.M_NotificationService;
import flyfish.service.M_UserService;
import flyfish.service.impl.M_UserServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

@RestController
@Slf4j
public class M_HeadTeacherController {
    @Autowired
    private M_UserService userService;
    @Autowired
    private M_NotificationService notificationService;

    /**
     * 班主任根据学校获取学生列表
     * @param school
     * @return
     */
    @GetMapping(value = "/mpi/headTeacher/getStudentUser", produces = "application/json;charset=UTF-8")
    public List<M_StudentUserVO> getStudentUser(String school,String headTeacherClassName){
        log.info("班主任获取学生列表，学校为{},班级为{}", school,headTeacherClassName);
        List<M_StudentUserVO> studentUserList = userService.getStudentUserListByHeadTeacher(school,headTeacherClassName);
        return studentUserList;
    }




    /**
     * 班主任匹配前端传入的excel名单，找出不同
     * @param file
     * @param school
     * @return
     */
    @PostMapping(value = "/mpi/headTeacher/matchStudentName",produces = "application/json;charset=UTF-8")
    public String matchStudentName(@RequestParam("file") MultipartFile file, String school, String headTeacherClassName){

        String grade = headTeacherClassName.substring(0,1)+"年级";
        Integer className = Integer.parseInt(headTeacherClassName.substring(2,3));
        log.info("班主任匹配学生名单，学校为{},年级为{},班级为{}", school,grade,className);
        String result = userService.matchStudentName(file,school,grade,className);
        return result;

    }


}
