//package flyfish.contoller;
//
//import flyfish.pojo.*;
//import flyfish.pojo.DTO.PageQueryClassDTO;
//import flyfish.pojo.DTO.QueryPassTaskDTO;
//import flyfish.pojo.DTO.RecordDTO;
//import flyfish.pojo.DTO.RecordQueryDTO;
//import flyfish.pojo.Record;
//import flyfish.pojo.VO.PageQueryClassVO;
//import flyfish.pojo.VO.RecordVO;
//import flyfish.service.AdminSideService;
//import flyfish.service.UserService;
//import lombok.extern.slf4j.Slf4j;
//import org.apache.poi.ss.usermodel.Row;
//import org.apache.poi.ss.usermodel.Sheet;
//import org.apache.poi.ss.usermodel.Workbook;
//import org.apache.poi.xssf.usermodel.XSSFWorkbook;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.core.io.ByteArrayResource;
//import org.springframework.http.HttpHeaders;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//import org.springframework.web.multipart.MultipartFile;
//
//import java.io.ByteArrayOutputStream;
//import java.io.IOException;
//import java.util.*;
//
//import org.springframework.http.HttpHeaders;
//import org.springframework.http.MediaType;
//import org.springframework.http.ResponseEntity;
//
//
//@RestController
//@Slf4j
//public class AdminSideController {
//
//    @Autowired
//    private AdminSideService adminSideService;
//
//    @Autowired
//    private UserService userService;
//
//    /**
//     * 上传新的密码文件
//     * @param file
//     * @return
//     */
//    @PostMapping("/tpi/uploadParentPassword")
//    public Result<String> uploadFile(@RequestParam("file") MultipartFile file) throws IOException {
//        log.info("上传的密码文件为，{}",file);
//        String result = adminSideService.addPassWordFile(file);
//        return Result.success(result);
//
//    }
//
//    /**
//     * 查询班级用户
//     * @return
//     */
//    @GetMapping("/tpi/queryclassPassword")
//    public Result<List<User>> queryclassPassword(){
//        log.info("查询班级密码列表");
//        List<User> userList = userService.getALLContent();
//        return Result.success(userList);
//    }
//
//    /**
//     * 修改班级用户
//     * @param editForm
//     * @return
//     */
//    @PostMapping("/tpi/editClassPassword")
//    public Result<String> editClassPassword(@RequestBody User editForm){
//        log.info("要修改的用户信息是：{}",editForm);
//        String result = userService.editClassPassword(editForm);
//        return Result.success(result);
//    }
//
//    /**
//     * 删除班级用户
//     * @param id
//     * @return
//     */
//    @GetMapping("/tpi/deleteClassPassword")
//    public Result<String> deletePassWord(Integer id){
//        log.info("要删除的用户是：{},{}",id);
//        userService.deleteClassPassword(id);
//        return null;
//    }
//
//    /**
//     * 新增班级用户
//     * @param newUser
//     * @return
//     */
//    @PostMapping("/tpi/addclassPassword")
//    public Result<User> addclassPassword(@RequestBody User newUser){
//        log.info("新增用户的信息是：{}",newUser);
//        User result = userService.addclassPassword(newUser);
//        return Result.success(result);
//    }
//
//
//    /**
//     * 查询学生用户
//     * @return
//     */
//    @GetMapping("/tpi/queryStudentPassword")
//    public Result<List<EditStudentForm>> querystudentPassword(String classNumber,String studentName){
//        log.info("查询学生密码列表,{},{}",classNumber,studentName);
//        List<EditStudentForm> parentPasswordList = adminSideService.getALLContent(classNumber,studentName);
//        return Result.success(parentPasswordList);
//    }
//
//
//    /**
//     * 新增学生用户
//     * @param editStudentForm
//     * @return
//     */
//    @PostMapping("/tpi/addStudentPassword")
//    public Result<EditStudentForm> addclassPassword(@RequestBody EditStudentForm editStudentForm){
//        log.info("新增用户的信息是：{}",editStudentForm);
//        EditStudentForm result = adminSideService.addstudentPassword(editStudentForm);
//        return Result.success(result);
//    }
//
//
//    /**
//     * 修改学生用户的密码
//     * @param editStudentForm
//     * @return
//     */
//    @PostMapping("/tpi/editStudentPassword")
//    public Result<String> editstudentPassword(@RequestBody EditStudentForm editStudentForm){
//        log.info("要修改的用户信息是：{}",editStudentForm);
//        String result = adminSideService.editstudentPassword(editStudentForm);
//        return Result.success(result);
//    }
//
//
//    /**
//     * 删除学生用户家长端
//     * @param id
//     * @return
//     */
//    @GetMapping( "/tpi/deleteStudentPassword")
//    public Result<String> deletestudentPassWord(Integer id){
//        log.info("要删除的用户是：{},{}",id);
//        adminSideService.deleteClassPassword(id);
//        return null;
//    }
//
//
//    /**
//     * 查询学生信息
//     * @param classNumber
//     * @param name
//     * @return
//     */
//    @GetMapping("/tpi/queryStudentInfo")
//    public Result<List<StudentInfo>> querystudentInfo(String classNumber, String name){
//        log.info("查询学生信息表列表,{},{}",classNumber,name);
//        List<StudentInfo> studentInfoList = adminSideService.getALLStudentInfoContent(classNumber,name);
//        return Result.success(studentInfoList);
//    }
//
//
//    /**
//     * 修改学生信息
//     * @param studentInfo
//     * @return
//     */
//    @PostMapping( "/tpi/editStudentInfo")
//    public Result<String> editstudentInfo(@RequestBody StudentInfo studentInfo){
//        log.info("要修改的用户信息是：{}",studentInfo);
//        String result = adminSideService.editstudentInfo(studentInfo);
//        return Result.success(result);
//    }
//
//
//    /**
//     * 删除学生信息
//     * @param id
//     * @return
//     */
//    @GetMapping( "/tpi/deleteStudentInfo")
//    public Result<String> deletestudentInfo(Integer id){
//        log.info("要删除的学生信息是：{}",id);
//        adminSideService.deleteClassInfo(id);
//        return null;
//    }
//
//
//    /**
//     * 新增学生信息
//     * @param studentInfo
//     * @return
//     */
//    @PostMapping("/tpi/addStudentInfo")
//    public Result<StudentInfo> addclassInfo(@RequestBody StudentInfo studentInfo){
//        log.info("新增学生的信息是：{}",studentInfo);
//        StudentInfo result = adminSideService.addstudentInfo(studentInfo);
//        return Result.success(result);
//    }
//
//
//    @PostMapping("/tpi/queryrecord")
//    public Result<List<RecordVO>> queryRecord(@RequestBody RecordQueryDTO recordQueryDTO){
//        log.info("查询学生作业表现情况的数据,{}",recordQueryDTO);
//        List<RecordVO> recordList = adminSideService.getALLRecordContent(recordQueryDTO);
//        return Result.success(recordList);
//    }
//
//    @PostMapping("/tpi/editrecord")
//    public Result<String> editRecord(@RequestBody RecordVO recordVO){
//        log.info("修改学生作业表现情况的数据,{}",recordVO);
//        String result = adminSideService.editRecordContent(recordVO);
//        return Result.success(result);
//    }
//
//    @PostMapping("/tpi/addrecord")
//    public Result<RecordVO> addRecord(@RequestBody RecordVO recordVO){
//        log.info("新增学生作业表现情况的数据,{}",recordVO);
//        RecordVO result = adminSideService.addRecordContent(recordVO);
//        return Result.success(result);};
//
//    @PostMapping("/tpi/deleterecord")
//    public Result<String> deleteRecord(@RequestBody List<Integer> ids){
//        log.info("删除学生作业表现情况的数据,{}",ids);
//        adminSideService.deleteRecord(ids);
//        return null;
//    }
//
//    @PostMapping("/tpi/deleteSingleRecord")
//    public Result<String> deleteSingleRecord(@RequestBody List<Integer> ids){
//        log.info("删除单个学生作业表现情况的数据,{}",ids);
//        adminSideService.deleteRecord(ids);
//        return null;
//    }
//
//    @PostMapping("/tpi/downloadrecords")
//    public ResponseEntity<byte[]> downloadRecords(@RequestBody List<Integer> ids) throws IOException {
//        List<Record> records = adminSideService.findAllById(ids);
//        ResponseEntity<byte[]> responseEntity = adminSideService.exportRecords(records);
//        return responseEntity;
//    }
//
//
//    @GetMapping("/tpi/autoqueryclassNumber")
//    public Result<?> autoqueryclassNumber(){
//        List<String> classNumberList = adminSideService.autoqueryclassNumber();
//
//        List<Object> objectsList = new ArrayList<>();
//        for (String s : classNumberList) {
//            HashMap<String, String> stringStringHashMap = new HashMap<>();
//            stringStringHashMap.put("value", s);
//            objectsList.add(stringStringHashMap);
//        }
//
//        return Result.success(objectsList);
//    }
//
//    @GetMapping("/tpi/autoquerynameall")
//    public Result<?> autoqueryname(){
//        List<String> nameList = adminSideService.autoqueryname();
//        HashSet<String> hashSet = new HashSet<>(nameList);
//        List<String> newnameList = new ArrayList<>(hashSet);
//        List<Object> objectsList = new ArrayList<>();
//        for (String s : newnameList) {
//            HashMap<String, String> stringStringHashMap = new HashMap<>();
//            stringStringHashMap.put("value", s);
//            objectsList.add(stringStringHashMap);
//        }
//
//        return Result.success(objectsList);
//    }
//
//
//    @GetMapping( "/tpi/autoqueryPasscontent")
//    public Result<?> autoqueryPasscontent(){
//        List<String> passcontentList = adminSideService.autoqueryPasscontent();
//        HashSet<String> hashSet = new HashSet<>(passcontentList);
//        List<String> newpasscontentList = new ArrayList<>(hashSet);
//        List<Object> objectsList = new ArrayList<>();
//        for (String s : newpasscontentList) {
//            HashMap<String, String> stringStringHashMap = new HashMap<>();
//            stringStringHashMap.put("value", s);
//            objectsList.add(stringStringHashMap);
//        }
//        return Result.success(objectsList);
//    }
//
//
//    @PostMapping("/tpi/queryrecordPass")
//    public Result<List<PassTask>> queryALlRecordPass(@RequestBody QueryPassTaskDTO queryPassTaskDTO){
//        log.info("查询学生过关情况的数据,{}",queryPassTaskDTO);
//        List<PassTask> passTaskList = adminSideService.getALLPassContent(queryPassTaskDTO);
//        return Result.success(passTaskList);
//    }
//
//    @PostMapping("/tpi/addrecordPass")
//    public Result<PassTask> addRecordPass(@RequestBody PassTask passTask){
//        log.info("新增学生过关情况的数据,{}",passTask);
//        PassTask result = adminSideService.addRecordPass(passTask);
//        return Result.success(result);
//    }
//
//    @PostMapping("/tpi/editrecordPass")
//    public Result<String> editRecordPass(@RequestBody PassTask passTask) {
//        log.info("修改学生过关情况的 数据,{}", passTask);
//        String result = adminSideService.editRecordPass(passTask);
//        return Result.success(result);
//    }
//
//
//    @PostMapping("/tpi/deleteSingleRecordPass")
//    public Result<String> deleteSingleRecordPass(@RequestBody List<Integer> ids){
//        log.info("删除单个学生过关情况的数据,{}",ids);
//        adminSideService.deleteRecordPass(ids);
//        return null;
//    }
//
//    @PostMapping("/tpi/deleterecordPass")
//    public Result<String> deleteRecordPass(@RequestBody List<Integer> ids){
//        log.info("删除学生过关情况的数据,{}",ids);
//        adminSideService.deleteRecordPass(ids);
//        return null;
//    }
//
//    @PostMapping("/tpi/downloadrecordsPass")
//    public ResponseEntity<byte[]> downloadRecordsPass(@RequestBody List<Integer> ids) throws IOException {
//        List<PassTask> records = adminSideService.findAllPassById(ids);
//        ResponseEntity<byte[]> responseEntity = adminSideService.exportRecordsPass(records);
//        return responseEntity;
//    }
//
//
//
//
//
//
//
//
//}
