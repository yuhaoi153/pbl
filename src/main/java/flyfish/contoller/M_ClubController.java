package flyfish.contoller;

import flyfish.constant.Template;
import flyfish.mapper.M_ClubMapper;
import flyfish.mapper.M_ClubStudentInfoMapper;
import flyfish.mapper.M_ClubsSupplementMapper;
import flyfish.pojo.DTO.M_ClubChooseDTO;
import flyfish.pojo.DTO.M_DeadLineDTO;
import flyfish.pojo.M_Club;
import flyfish.pojo.VO.*;
import flyfish.service.M_ClubService;
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
import java.time.LocalDateTime;
import java.util.List;

@RestController
@Slf4j
public class M_ClubController {
    @Autowired
    private M_ClubService mClubService;
    @Autowired
    private M_ClubMapper mClubMapper;
    @Autowired
    private M_ClubStudentInfoMapper m_ClubStudentInfoMapper;
    @Autowired
    private M_ClubsSupplementMapper m_ClubsSupplementMapper;


    /**
     * 查询寻获社团信息
     * @param admissionSemester
     * @param grade
     * @return
     */
    @GetMapping("/mpi/club/getClubInfo")
    public List<ClubVO> getClubInfo(String admissionSemester, String grade) {
        Boolean isActive = true;
        List<ClubVO> clubVOList = mClubService.getClubInfo(admissionSemester, grade, isActive);
        return clubVOList ;
    }

    //修改社团报名信息
    @PostMapping(value = "/mpi/club/updateClubChoose", produces = "application/json;charset=UTF-8")
    public  List<M_ClubApplyNumVO> updateClubChoose(@RequestBody M_ClubChooseDTO clubChooseDTO){
        log.info("用户{}选择的社团为{}",clubChooseDTO.getUserId(),clubChooseDTO.getClubIds());
        List<M_ClubApplyNumVO>  resp = mClubService.updateClubChoose(clubChooseDTO);
        return resp;
    }

    //根据用户信息和招生批次获取志愿信息
    @GetMapping("/mpi/club/getThreeClubChoose")
    public List<M_ThreeClubChooseVO> getThreeClubChoose(Integer userId, String admissionSemester){
        log.info("用户{}和招生批次{}获取志愿信息",userId,admissionSemester);
        List<M_ThreeClubChooseVO> resp = mClubService.getThreeClubChoose(userId,admissionSemester);
        return resp;
    }

    //分配社团名单
    @GetMapping(value = "/mpi/club/assignClubList",produces = "application/json;charset=UTF-8")
    public String assignClubList(){
        log.info("分配社团名单");
        String resp = mClubService.assignClubList();
        return resp;
    }

    //按班级导出学生社团excel名单
    @GetMapping(value = "/mpi/club/exportClubListByClass",produces = "application/json;charset=UTF-8")
    public ResponseEntity<byte[]> downloadClubListByClass()    {
        log.info("按班级导出学生社团excel名单");
        String admissionSemester = m_ClubStudentInfoMapper.getAdmissionSemester();
        ResponseEntity<byte[]> resp = mClubService.downloadClubListByClass(admissionSemester);
        return resp;
    }

    //按社团分类导出学生社团excel名单
    @GetMapping(value = "/mpi/club/exportClubListByClub",produces = "application/json;charset=UTF-8")
    public ResponseEntity<byte[]> downloadClubListByClub()    {
        log.info("按社团分类导出学生社团excel名单");
        String admissionSemester = m_ClubStudentInfoMapper.getAdmissionSemester();
        ResponseEntity<byte[]> resp = mClubService.downloadClubListByClub(admissionSemester);
        return resp;    }

    //上传社团名单
    //上传名单要考虑要不要删除之前的名单，因为这里涉及到不同年级要上传名单的问题。这里可以判断一下如果年级和名称都一样的话，就删除之前的名单
    @PostMapping (value = "/mpi/club/uploadClubList",produces = "application/json;charset=UTF-8")
    public String uploadClubList(@RequestParam("file") MultipartFile file){
        log.info("上传社团名单",file.getOriginalFilename());
        String result = mClubService.uploadClubList(file);
        return result;

    }

    //上传学生名单
    @PostMapping(value = "/mpi/club/uploadStudentList",produces = "application/json;charset=UTF-8")
    public String uploadStudentList(@RequestParam("file") MultipartFile file) {
        log.info("上传学生名单", file.getOriginalFilename());
        String result = mClubService.uploadStudentList(file);
        return result;
    }


    //下载社团模板


    //从网页端链接下载社团模板数据
    @GetMapping("/mpi/club/downLoadClubExcel")
    public ResponseEntity<InputStreamResource> downloadClubExcel() {
        log.info("下载社团名单模板。。。");
        String fileUrl = Template.CLUBLIST;

        try {
            URL url = new URL(fileUrl);
            InputStream inputStream = url.openStream();
            InputStreamResource resource = new InputStreamResource(inputStream);

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"clubList.xlsx\"")
                    .body(resource);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.notFound().build();
        }
    }

    //下载学生模板
    @GetMapping("/mpi/club/downLoadStudentExcel")
    public ResponseEntity<InputStreamResource> downloadStudentExcel() {
        log.info("下载学生名单模板。。。");
        String fileUrl = Template.STUDENTLIST;
        try {
            URL url = new URL(fileUrl);
            InputStream inputStream = url.openStream();
            InputStreamResource resource = new InputStreamResource(inputStream);

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"studentList.xlsx\"")
                    .body(resource);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.notFound().build();
        }
    }




    //重置全部的社团选择信息
    @PostMapping(value = "/mpi/club/resetAllClubChoose",produces = "application/json;charset=UTF-8")
    public String resetAllClubChoose() {
        log.info("重置全部的社团选择信息");
        String resp = mClubService.resetAllClubChoose();
        return resp;
    }

    //重新统一设置社团选择截止时间
    @PostMapping(value = "/mpi/club/resetClubDeadline",produces = "application/json;charset=UTF-8")
    public String resetClubDeadline(@RequestBody M_DeadLineDTO deadLineDTO) {
        log.info("重新统一设置社团选择截止时间");
        String admissionSemester = m_ClubStudentInfoMapper.getAdmissionSemester();
        LocalDateTime deadline = deadLineDTO.getDeadline();
        log.info("要重置的日期是:{}",deadline);
        mClubMapper.resetClubDeadline(admissionSemester, deadline);
        String resp = "成功更新了"+admissionSemester+"社团选择截止时间";
        return resp;
    }


    //查询clubsSupplement表中的数据
    @GetMapping(value = "/mpi/club/getClubSupplement",produces = "application/json;charset=UTF-8")
    public M_ClubsSupplementVO getClubSupplement() {
        log.info("查询clubsSupplement表中的数据");
        M_ClubsSupplementVO  resp= m_ClubsSupplementMapper.getCurrentInfo();

        return resp;
    }

    //修改clubsSupplement表中的数据
    @PostMapping(value = "/mpi/club/updateClubSupplement",produces = "application/json;charset=UTF-8")
    public String updateClubSupplement(@RequestBody M_ClubsSupplementVO clubsSupplementVO) {
        log.info("修改clubsSupplement表中的数据");
        //先删除之前的社团选择信息
        m_ClubsSupplementMapper.deleteInfo();
         m_ClubsSupplementMapper.newInsertClubSupplement(clubsSupplementVO);
        return null;
    }

    //查看社团管理员页面的所有社团分配状态
    @GetMapping(value = "/mpi/club/getAllAdminClub",produces = "application/json;charset=UTF-8")
    public List<ClubVO> getAllAdminClub() {
        log.info("查看社团管理员页面的所有社团分配状态");
        String admissionSemester = mClubMapper.getCurrentAdmissionSemester();
        List<ClubVO> resp = mClubService.getAllAdminClub(admissionSemester);
        return resp;
    }

    //查看社团管理员页面的所有学生分配状态
    @GetMapping(value = "/mpi/club/getAllAdminStudentInfo",produces = "application/json;charset=UTF-8")
    public List<M_ClubStudentInfoVO> getAllAdminStudent(){
        log.info("查看社团管理员页面的所有学生分配状态");
        String admissionSemester = m_ClubStudentInfoMapper.getAdmissionSemester();
        List<M_ClubStudentInfoVO> resp = mClubService.getAllAdminStudentInfo(admissionSemester);
        return resp;
    }



    //删除指定社团
    @GetMapping(value = "/mpi/club/deleteClubById",produces = "application/json;charset=UTF-8")
    public String deleteClubById(Integer id) {
        log.info("删除指定社团{}",id);
        Integer clubId = id;
        //删除此社团前，先把选择此社团的学生志愿清空，同时要把所有这个result社团的学生志愿状态清空

        String resp = mClubService.resetChooseStatusByClubId(clubId);
        return resp;
    }

    //根据年级批量删除社团
    @GetMapping(value = "/mpi/club/deleteClubsByGrade",produces = "application/json;charset=UTF-8")
    public String deleteClubsByGrade(String grade) {
        log.info("根据年级批量{}删除社团",grade);
        String resp = mClubService.deleteClubsByGrade(grade);
        return resp;}

    //删除指定学生
    @GetMapping(value = "/mpi/club/deleteStudentById",produces = "application/json;charset=UTF-8")
    public String deleteStudentById(Integer studentId) {
        log.info("删除指定学生:{}",studentId);
        String resp = mClubService.deleteUserById(studentId);

        return resp;
    }

    //根据年级批量删除学生
    @GetMapping(value = "/mpi/club/deleteStudentsByGrade",produces = "application/json;charset=UTF-8")
    public String deleteStudentsByGrade(String grade) {
        log.info("根据年级批量删除{}学生",grade);
        String resp = mClubService.deleteUserByGrade(grade);
        return resp;
    }





    //新增社团
    @PostMapping(value = "/mpi/club/addNewClub",produces = "application/json;charset=UTF-8")
    public String addNewClub(@RequestBody M_Club newClub) {
        log.info("新增社团{}",newClub.getClubName());
        String resp = mClubService.addNewClub(newClub);
        return resp;
    }

    //编辑社团信息、
    @PostMapping(value = "/mpi/club/editClubInfo",produces = "application/json;charset=UTF-8")
    public String editClubInfo(@RequestBody M_Club editClub) {
        log.info("编辑社团信息{}", editClub.getClubName());
        String resp = mClubService.editClubInfo(editClub);
        return resp;
    }

    //新增学生
    @PostMapping(value = "/mpi/club/addNewStudent",produces = "application/json;charset=UTF-8")
    public String addNewStudent(@RequestBody M_ClubStudentInfoVO newStudent) {
        log.info("新增学生{}", newStudent.getStudentName());
        String resp = mClubService.addNewStudent(newStudent);
        return resp;

    }

    //修改学生信息
    @PostMapping(value = "/mpi/club/editStudentInfo",produces = "application/json;charset=UTF-8")
    public String editStudentInfo(@RequestBody M_ClubStudentInfoVO editStudent) {
        log.info("修改学生信息{}", editStudent.getStudentName());
        String resp = mClubService.editStudentInfo(editStudent);
        return resp;
    }














}
