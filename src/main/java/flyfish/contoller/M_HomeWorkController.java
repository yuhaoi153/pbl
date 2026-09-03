package flyfish.contoller;

import flyfish.constant.M_HomeWorkConstant;
import flyfish.exception.ContenttNullException;
import flyfish.pojo.DTO.*;
import flyfish.pojo.M_WellBadHomeworkPerform;
import flyfish.pojo.Result;
import flyfish.pojo.VO.HomeworkMessageVO;
import flyfish.pojo.VO.M_HomeworkDefaultParamsVO;
import flyfish.pojo.VO.M_HomeworkStundentInfoVO;
import flyfish.pojo.VO.PageQueryClassVO;
import flyfish.service.*;
import flyfish.utils.AliOSSUtils;
import flyfish.utils.ClassNameChangeUtills;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;



@RestController
@Slf4j
public class M_HomeWorkController {
    @Autowired
    private HomeWorkContentService homeWorkContentService;
    @Autowired
    private RecordService recordService;
    @Autowired
    private M_UserService mUserService;
    @Autowired
    private ExcelService excelService;
    @Autowired
    private M_HomeworkPageSettingService homeworkPageSettingService;
    @Autowired
    private M_LoginService mLoginService;
    @Autowired
    private M_WellBadPerformService wellBadPerformService;
    @Autowired
    private ClassNameChangeUtills classNameChangeUtills;
    @Autowired
    private AliOSSUtils aliOSSUtils;
    @Autowired
    private M_ChatService chatService;

    /**
     * 查询作业类型接口
     * @param homeWorkContentDTO
     * @return
     */
    @PostMapping("/mpi/homework/queryContent")
    public List<String> queryContent(@RequestBody HomeWorkContentDTO homeWorkContentDTO){
        log.info("要查询的作业类型条件为：{}",homeWorkContentDTO);
        homeWorkContentDTO.setClassNumber(turnChineseClassToNumber(homeWorkContentDTO.getClassNumber()));
        List<String> result = homeWorkContentService.queryContent(homeWorkContentDTO);
        return result;
    }

    /**
     * 新增作业类型接口
     * @param homeWorkContentDTO
     * @return
     */
    @PostMapping(value = "/mpi/homework/addContent" ,produces = "text/plain;charset=UTF-8")
    public String addContent(@RequestBody HomeWorkContentDTO homeWorkContentDTO){

        String result = homeWorkContentService.addcontent(homeWorkContentDTO);
        return result;
    }



    /**
     * 删除作业类型的接口
     * @param deleteContetnDTO
     * @return
     * @throws Exception
     */
    @PostMapping(value = "/mpi/homework/deleteContent", produces = "text/plain;charset=UTF-8")
    public String deleteContent(@RequestBody DeleteContetnDTO deleteContetnDTO) throws Exception {
        log.info("要删除的作业列表为：{}",deleteContetnDTO);
        if (deleteContetnDTO.getContent() ==null  || deleteContetnDTO.getContent().isEmpty()){
            throw new ContenttNullException("没有作业内容");
        }
        deleteContetnDTO.setClassNumber(turnChineseClassToNumber(deleteContetnDTO.getClassNumber()));
        String result = homeWorkContentService.deleteContent(deleteContetnDTO);
        return result;

    }


    


    /**
     * 接收数据，并返回没写完作业同学数据
     * @param recordDTO
     * @return
     */
    @PostMapping(value = "/mpi/homework/uploadHomeWork" , produces = "text/plain;charset=UTF-8")
    public String uploadHomeWork(@RequestBody RecordDTO recordDTO) throws Exception {
        log.info("扫码枪上传数据为：{}",recordDTO);
        recordDTO.setClassNumber(turnChineseClassToNumber(recordDTO.getClassNumber()));
        String result = recordService.uploadFeedback(recordDTO);
        return result;
    }

    /**
     * 接收数据，并返回优秀和未达标作业名单——————特殊操作
     * @param recordDTO
     * @return
     */
    @PostMapping(value = "/mpi/homework/uploadHomeworkSpecial",produces = "text/plain;charset=UTF-8")
    public String SpecialHomeWork(@RequestBody RecordDTO recordDTO) throws Exception {
        log.info("扫码枪上传数据为：{}",recordDTO);
        recordDTO.setClassNumber(turnChineseClassToNumber(recordDTO.getClassNumber()));

        String result = recordService.SpecialHomework(recordDTO);
        return result;
    }


    /**
     * 接收数据，并返回完成订正的同学名单
     * @param recordDTO
     * @return
     */
    @PostMapping(value = "/mpi/homework/uploadRevision" , produces = "text/plain;charset=UTF-8")
    public String homeWorkRevison(@RequestBody RecordDTO recordDTO) throws Exception {
        log.info("扫码枪上传数据为：{}",recordDTO);
        recordDTO.setClassNumber(turnChineseClassToNumber(recordDTO.getClassNumber()));
        String result = recordService.homeworkRevison(recordDTO);
        return result;
    }



    /**
     * 根据页面类型返回对应的字体内容
     * @param page
     * @return
     */
    @GetMapping(value = "/mpi/homework/getFont",produces = "text/plain;charset=UTF-8")
    public String  getFont(String page)  {
        if(page.equals("homework")) {
            return M_HomeWorkConstant.HOMEWORK_RESULT;
        }
        return null;

    }


    /**
     * 修改作业结果的接口
     * @param 
     * @return
     */
    @PostMapping("/mpi/homework/alterHomeworkData")
    public String postMethodName(@RequestBody AlterHomeworkDataDTO alterHomeworkDataDTO) {
        //TODO: process POST request
        
        String result = recordService.alterHomeworkData(alterHomeworkDataDTO);
        return result;
    }
    




    /**
     * 查询班级数据，展示四种名单
     * @param pageQueryClassDTO
     * @return
     */
    @GetMapping("/mpi/homework/queryclassdata")
    public List<PageQueryClassVO> pageClass(PageQueryClassDTO pageQueryClassDTO){
        log.info("查询班级名单：{}",pageQueryClassDTO);
        pageQueryClassDTO.setClassNumber(turnChineseClassToNumber(pageQueryClassDTO.getClassNumber()));

        List<PageQueryClassVO> pageQueryClassVOS = recordService.pageClass(pageQueryClassDTO);

        return pageQueryClassVOS;

    }


    /**
     * 家长端查询个人作业数据
     * @param pageQueryClassDTO
     * @return
     */
    @GetMapping("/mpi/homework/querystudentdata")
    public List<PageQueryClassVO> pageStudent(PageQueryClassDTO pageQueryClassDTO){
        log.info("查询学生个人作业情况名单：{}",pageQueryClassDTO);
        pageQueryClassDTO.setClassNumber(turnChineseClassToNumber(pageQueryClassDTO.getClassNumber()));

        List<PageQueryClassVO> pageQueryClassVOS = recordService.pageStudent(pageQueryClassDTO);

        return pageQueryClassVOS;

    }




    /**
     * 上传语音，并识别后更新作业数据
     * @param file
     * @param school
     * @param classNumber
     * @param content
     * @param subject
     * @return
     * @throws Exception
     */
    @PostMapping(value = "/mpi/homework/uploadAudioHomework" , produces = "text/plain;charset=UTF-8")
    public String uploadAudioHomeWork(@RequestParam("audioFile") MultipartFile file , String school, String classNumber, String content, String subject, LocalDate checkdate,String supplementary,String minusScoreByHomework,String failRevisionAddScore,Integer addScoreNumber,Integer minusScoreNumber,String completedRevisionAddScore, Integer revisionAddScore) throws Exception {
    log.info("接收语音文件：{},对应的班级：{},作业内容：{},学科：{}",file.getOriginalFilename(),classNumber,content,subject);
    classNumber = turnChineseClassToNumber(classNumber);
    String result = recordService.uploadAudioHomeWork(file,school,classNumber,content,subject,checkdate,supplementary,minusScoreByHomework,failRevisionAddScore,addScoreNumber,minusScoreNumber,completedRevisionAddScore,revisionAddScore);
    return result;
    }

    /**
     * siri语音识别上传作业数据接口
     * @param mHomeworkDTO
     * @return
     * @throws Exception
     */
    @PostMapping(value = "/mpi/homework/uploadtestHomework" , produces = "text/plain;charset=UTF-8")
    public String uploadtestHomeWork(@RequestBody M_HomeworkDTO mHomeworkDTO) throws Exception {
        //验证用户信息，已确定是否是小程序用户
        String confirmResult = mLoginService.confirmUser(mHomeworkDTO.getUserName(), mHomeworkDTO.getPassword(), mHomeworkDTO.getPhone(), mHomeworkDTO.getSchool());
        if(!confirmResult.equals("success")){
            return "用户信息验证失败";
        }

        String classNumber = mHomeworkDTO.getClassNumber();
        if(mHomeworkDTO.getClassNumber().substring(0,1).equals("一") || mHomeworkDTO.getClassNumber().substring(0,1).equals("二") || mHomeworkDTO.getClassNumber().substring(0,1).equals("三") || mHomeworkDTO.getClassNumber().substring(0,1).equals("四") || mHomeworkDTO.getClassNumber().substring(0,1).equals("五") || mHomeworkDTO.getClassNumber().substring(0,1).equals("六") || mHomeworkDTO.getClassNumber().substring(0,1).equals("七") || mHomeworkDTO.getClassNumber().substring(0,1).equals("八") || mHomeworkDTO.getClassNumber().substring(0,1).equals("九")){
            classNumber = turnChineseClassToNumber(mHomeworkDTO.getClassNumber());
        }


        String message = mHomeworkDTO.getMessage();
        String subject = mHomeworkDTO.getSubject();
        String school = mHomeworkDTO.getSchool();
        LocalDate checkdate = mHomeworkDTO.getCheckdate();
        if(checkdate == null) {
            checkdate = LocalDate.now();
        }
        String supplementary = mHomeworkDTO.getSupplementary();
        String minusScoreByHomework = mHomeworkDTO.getMinusScoreByHomework();
        String failRevisionAddScore = mHomeworkDTO.getFailRevisionAddScore();
        Integer addScoreNumber = mHomeworkDTO.getAddScoreNumber();
        Integer minusScoreNumber = mHomeworkDTO.getMinusScoreNumber();
        String content = mHomeworkDTO.getContent();
        String completedRevisionAddScore = mHomeworkDTO.getCompletedRevisionAddScore();
        Integer revisionAddScore = mHomeworkDTO.getRevisionAddScore();


//        String result = recordService.uploadtestHomeWork(message,school,classNumber,content,subject,checkdate,supplementary,minusScoreByHomework,failRevisionAddScore,addScoreNumber,minusScoreNumber);
        String result = recordService.uploadmessageHomeWork(content,checkdate,message,school,classNumber,subject,supplementary,minusScoreByHomework,failRevisionAddScore,addScoreNumber,minusScoreNumber,completedRevisionAddScore,revisionAddScore);

        return result;
    }


    /**
     * 查询所有的管理员的作业管理用户数据
     * @param school
     * @return
     */
    @GetMapping(value = "/mpi/homework/queryStudentInfo" )
    public List<M_HomeworkStundentInfoVO> queryHomeworkStudentInfo(String school) {
      log.info("管理员查询学生作业信息:{}",school);
        List<M_HomeworkStundentInfoVO> result = mUserService.queryHomeworkStudentInfo(school);

        return result;
    }


    /**
     * 同步小程序用户数据到作业管理中
     * @param school
     * @return
     */
    @GetMapping(value = "/mpi/homework/syncUserList" , produces = "text/plain;charset=UTF-8")
    public String syncUserList(String school) {
        log.info("管理员同步用户列表:{}",school);
        String result = mUserService.syncUserList(school);
        return result;
    }

    /**
     * 删除某个作业管理的班级 ，管理员用户管理里的作业管理
     * @param
     * @return
     */
    @PostMapping(value = "/mpi/homework/deleteClass")
    public String deleteClass(@RequestBody M_DeleteClassDTO mDeleteClassDTO) {
        log.info("删除用户管理的班级:{}",mDeleteClassDTO.getClassNameList());
        String resp = mUserService.deleteClass(mDeleteClassDTO);
        return resp;
    }


    @GetMapping("/mpi/homework/downloadQR")
    public ResponseEntity<byte[]> downloadQR(String className,String school) throws Exception {
        log.info("下载二维码的班级为：{}",className);
        className = turnChineseClassToNumber(className);

        ResponseEntity<byte[]> result = excelService.downloadQR(className,school);
        return result;
    }

    /**
     * 获取小程序作业页面的默认参数
     * @param userName
     * @param school
     * @return
     */
    @GetMapping("/mpi/homework/getHomeworkDefaultParams")
    public M_HomeworkDefaultParamsVO getHomeworkDefaultParams(String userName, String school){
        log.info("获取小程序作业统计的默认参数的参数是 {}{}",school,userName);
        M_HomeworkDefaultParamsVO  mHomeworkDefaultParamsVO = homeworkPageSettingService.getHomeworkDefaultParams(school,userName);
        return mHomeworkDefaultParamsVO;
    }


    /**
     * 获取网页作业页面的默认参数
     * @param subject
     * @param school
     * @return
     */
    @GetMapping("/mpi/homework/getHomeworkDefaultParamsBySubject")
    public M_HomeworkDefaultParamsVO getHomeworkDefaultParamsBySubject(String subject,String classNumber, String school){
        log.info("获取网页作业统计的默认参数的参数是 {}{}{}",school,classNumber,subject);
        M_HomeworkDefaultParamsVO  mHomeworkDefaultParamsVO = homeworkPageSettingService.getHomeworkDefaultParamsBySubject(school,classNumber,subject);
        return mHomeworkDefaultParamsVO;
    }


    /**
     * 设置作业默认参数
     * @param m_HomeworkDefaultParamsVO
     * @return
     */
    @PostMapping(value = "/mpi/homework/setHomeworkDefaultParams" , produces = "application/json;charset=UTF-8")
    public String setHomeworkDefaultParams(@RequestBody M_HomeworkDefaultParamsVO m_HomeworkDefaultParamsVO) {
        log.info("设置小程序作业的默认参数的参数是 {}", m_HomeworkDefaultParamsVO);
        String resp = homeworkPageSettingService.setMoralStatisticDefaultParams(m_HomeworkDefaultParamsVO);
        return resp;
    }


    /**
     * 获取一段时间，全部的作业展示图片
     * @param school
     * @param className
     * @param content
     * @param subject
     * @param studentName
     * @param startDate
     * @param endDate
     * @return
     */
    @GetMapping("/mpi/homework/getShowImage")
    public List<M_WellBadHomeworkPerform> getShowImage(String school, String className,String classNumber, String content, String subject,String studentName,LocalDate startDate, LocalDate endDate) throws IOException {
        log.info("获取展示图片的参数是：学校{},班级{}{},作业内容{},学科{},学生姓名{},检查日期{},{}",school,className,classNumber,content,subject,studentName,startDate,endDate);

        className = turnChineseClassToNumber(className);
        if(classNumber != null){
            className = classNumber;
        }
        List<M_WellBadHomeworkPerform> result = wellBadPerformService.getShowImage(school,className,content,subject,studentName,startDate,endDate);
        return result;

    }

    @GetMapping("mpi/homework/deleteShowImage")
    public String deleteShowImage(Integer id) {
        log.info("删除展示图片的id是：{}",id);
        String result = wellBadPerformService.deleteShowImage(id);
        return result;
    }

    /**
     * 上传指定日期作业的图片展示
     * @param file
     * @param school
     * @param className
     * @param content
     * @param subject
     * @param studentName
     * @param checkDate
     * @return
     * @throws Exception
     */
    @PostMapping(value = "mpi/homework/uploadHomeWorkImage",produces = "text/plain;charset=UTF-8")
    public String uploadHomeWorkImage(@RequestParam("imagefile") MultipartFile file ,String situation, String school, String className, String content, String subject, String studentName, LocalDate checkDate) throws Exception {
        log.info("上传作业图片的参数是：学校{},班级{},作业内容{},学科{},学生姓名{},检查日期{},文件名{}",school,className,content,subject,studentName,checkDate,file.getOriginalFilename());

        String grade = null;
        if(className.contains("一") || className.contains("二") || className.contains("三") || className.contains("四") || className.contains("五") || className.contains("六") || className.contains("七") || className.contains("八") || className.contains("九")) {
             grade = className.substring(0,1)+"年级";
            className = turnChineseClassToNumber(className);
        }else {
            String LongclassName = classNameChangeUtills.formatToChinese(className);
            grade = LongclassName.substring(0,1)+"年级";

        }
        String result = wellBadPerformService.uploadHomeWorkImage(file,grade,situation,school,className,content,subject,studentName,checkDate);
        return result;
    }



    /**
     * 点击矩形框修改订正和未完成数据
     * @param recordDTO
     * @return
     */
    @PostMapping( "/mpi/homework/alterHomeworkResult")
    public String alterHomeworkResult(@RequestBody RecordDTO recordDTO) throws Exception {
        log.info("手机端修改作业结果的上传数据为：{}",recordDTO);
        String classNumber = turnChineseClassToNumber(recordDTO.getClassNumber());
        recordDTO.setClassNumber(classNumber);

        String result = recordService.alterHomeworkResult(recordDTO);
        return result;
    }







    @PostMapping("/tpi/chat/uploadFile")
    public String uploadFile(@RequestParam("file") MultipartFile file) throws IOException {

        String fileUrl = aliOSSUtils.uploadByFilePath(file,"homework/chatAudio/");
        log.info("发送的录音文件是：{}",fileUrl);
        return fileUrl;
    }

    @PostMapping("/tpi/chat/uploadImage")
    public String uploadImageFile(@RequestParam("file") MultipartFile file) throws IOException {
        // 1. 校验
        if (file.isEmpty()) {
            throw new IllegalArgumentException("上传文件不能为空");
        }
        if (file.getSize() > 20 * 1024 * 1024) {
            throw new IllegalArgumentException("文件大小超过20MB");
        }
        String contentType = file.getContentType();
        if (contentType == null || !isAllowedImageType(contentType)) {
            throw new IllegalArgumentException("不支持的图片格式");
        }

        String url = aliOSSUtils.uploadByFilePath(file, "homework/chatImage/");
        log.info("图片上传成功：{}", url);
        return url;
    }


    /**
     * 布置作业到班级电脑
     * @param showHomeWorkDTO
     * @return
     * @throws IOException
     */
    @PostMapping(value = "/tpi/chat/showHomework",produces = "text/plain;charset=UTF-8")
    public String showHomework(@RequestBody M_ShowHomeWorkDTO showHomeWorkDTO) throws IOException {

        log.info("小程序布置作业的参数{}",showHomeWorkDTO);
        String resp =chatService.uploadHomeworkMessage(showHomeWorkDTO);


        return resp;
    }

    /**
     * 查看已经布置的作业
     * @param checkDate
     * @return
     */
    @GetMapping("/mpi/chat/getHomeworkMessage")
    public List<HomeworkMessageVO> getHomeworkMessage(String teacherName,String school,String subject, LocalDate checkDate,String className) {
        log.info("查询作业消息的用户姓名{}日期{}",teacherName,checkDate);
        List<HomeworkMessageVO> homeworkMessageVOList = chatService.getHomeworkMessage(teacherName,school,subject,checkDate,className);
        return homeworkMessageVOList;
    }


    /**
     * 删除已经布置的作业信息
     * @param idDTO
     * @return
     */
    @PostMapping("/mpi/chat/deleteHomeworkMessage")
    public String deleteHomeworkMessage(@RequestBody M_IDDTO idDTO) {
        log.info("删除某日用户布置作业的消息: " + idDTO);
        for (Integer id : idDTO.getIdList()) {
            chatService.deleteHomeworkMessage(id);
        }

        return "删除成功";
    }

    /**
     * 检查 MIME 类型是否为允许的图片格式
     */
    private boolean isAllowedImageType(String contentType) {
        return contentType.equals("image/jpeg") ||
                contentType.equals("image/png") ||
                contentType.equals("image/gif") ||
                contentType.equals("image/webp")||
                contentType.equals(("image/heic"));
    }






    private String turnChineseClassToNumber(String classNumber) {
        if(classNumber != null && !classNumber.isEmpty()) {
            if(classNumber.contains("一")) {
                classNumber = classNumber.replace("一","1");
            }
            if(classNumber.contains("二")) {
                classNumber = classNumber.replace("二","2");
            }
            if(classNumber.contains("三")) {
                classNumber = classNumber.replace("三","3");
            }
            if(classNumber.contains("四")) {
                classNumber = classNumber.replace("四","4");
            }
            if(classNumber.contains("五")) {
                classNumber = classNumber.replace("五","5");
            }
            if(classNumber.contains("六")) {
                classNumber = classNumber.replace("六","6");
            }
            if(classNumber.contains("七")) {
                classNumber = classNumber.replace("七","7");
            }
            if(classNumber.contains("八")) {
                classNumber = classNumber.replace("八","8");
            }
            if(classNumber.contains("九")) {
                classNumber = classNumber.replace("九","9");
            }
        }
        //把(和)替换掉
        if(classNumber != null && !classNumber.isEmpty()) {
            if (classNumber.contains("(")) {
                classNumber = classNumber.replace("(", "");
            }
            if (classNumber.contains(")")) {
                classNumber = classNumber.replace(")", "");
            }
        }
        //把班替换掉
        if(classNumber != null && !classNumber.isEmpty()) {
            if (classNumber.contains("班")) {
                classNumber = classNumber.replace("班", "");
            }
        }
        return classNumber;
    }
}
