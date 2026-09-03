package flyfish.contoller;

import flyfish.pojo.DTO.*;

import flyfish.pojo.M_TeacherData;
import flyfish.pojo.M_TeacherInfo;
import flyfish.pojo.VO.M_FeedBackReportVO;
import flyfish.pojo.VO.M_FeedbackVO;
import flyfish.service.M_EditInformationService;
import flyfish.service.M_ReadingService;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@RestController
@Slf4j
public class M_EditInformationController {

    @Autowired
    private M_EditInformationService m_editInformationService;
    @Autowired
    private M_ReadingService m_readingService;



    //批量新增年级班级
    @PostMapping("/mpi/editInformation/gradeClassNum")
    public String editGradeClassNum(@RequestBody M_GradeClassDTO m_gradeClassDTO) {
         String grade = m_gradeClassDTO.getGrade();
         Integer classNum = m_gradeClassDTO.getClassNum();
         String school = m_gradeClassDTO.getSchool();
        log.info("修改年级班级数的参数grade:{},classNum:{},school:{}",grade,classNum,school);
         m_editInformationService.editGradeClassNum(grade,classNum,school);
         return "success";
    }

    //新增年级班级
    @PostMapping(value = "/mpi/editInformation/addGradeClass", produces = "application/json;charset=UTF-8")
    public String addGradeClass(@RequestBody M_GradeClassDTO m_gradeClassDTO) {
        String grade = m_gradeClassDTO.getGrade();
        Integer classNum = m_gradeClassDTO.getClassNum();
        String school = m_gradeClassDTO.getSchool();
        log.info("新增单个年级班级的参数grade:{},classNum:{},school:{}",grade,classNum,school);
        String resp = m_editInformationService.addSingleGradeClassNum(grade,classNum,school);

        return resp;
    }


    @GetMapping("/mpi/editInformation/getTeacherList")
    public List<M_TeacherInfo> getTeacherInfo(
            @RequestParam("type") String type,
            @RequestParam("content") String content,
            @RequestParam("school") String school,
            @RequestParam("label") String label
    ) {
        log.info("根据条件查询教师信息的参数是：type={}, content={}, school={},label={}", type, content, school,label);
        return m_editInformationService.getTeacherInfoByCondition(type, content, school,label);
    }

    @PostMapping("/mpi/editInformation/deleteTeacherList")
    public String deleteTeacherList(@RequestBody M_DeleteTeacherInfoDTO m_deleteTeacherInfoDTO) {
        log.info("删除教师信息的参数是：idList={}, school={}", m_deleteTeacherInfoDTO.getIdList(), m_deleteTeacherInfoDTO.getSchool());
        m_editInformationService.deleteTeacherInfo(m_deleteTeacherInfoDTO);
        return "success";

    }

    @PostMapping(value = "/mpi/editInformation/addNewTeacher", produces = "application/json;charset=UTF-8")
    public String addNewTeacher(@RequestBody M_AddNewTeacher m_addNewTeacher) {
      log.info("新增教师参数{}{}{}{}",m_addNewTeacher.getTeacherName(),m_addNewTeacher.getSubject(),m_addNewTeacher.getSchool(),m_addNewTeacher.getLabel());
        String  resp = m_editInformationService.addNewTeacher(m_addNewTeacher);
        return resp;
    }

    @PostMapping("/mpi/editInformation/editTeacher")
    public String editTeacher(@RequestBody M_AddNewTeacher m_addNewTeacher) {
      log.info("编辑教师参数{}{}{}{}",m_addNewTeacher.getTeacherName(),m_addNewTeacher.getSubject(),m_addNewTeacher.getSchool(),m_addNewTeacher.getLabel());
        m_editInformationService.editTeacher(m_addNewTeacher);
        return "success";
    }

    //新增班级教师
    @PostMapping(value = "/mpi/editInformation/addClassTeacher" ,produces = "application/json;charset=UTF-8" )
    public String addClassTeacher(@RequestBody M_AddNewTeacher m_addNewTeacher) {
        log.info("新增班级教师参数{}{}",m_addNewTeacher.getTeacherName(),m_addNewTeacher.getClassNum());
        String className = m_addNewTeacher.getGrade().substring(0,1) + "(" + m_addNewTeacher.getClassNum() + ")" + "班";

        m_addNewTeacher.setClassName(className);
        String resp = m_editInformationService.addClassTeacher(m_addNewTeacher);
        return resp;
    }

    //修改班级教师
    @PostMapping("/mpi/editInformation/editClassTeacher")
    public String editClassTeacher(@RequestBody M_AddNewTeacher m_addNewTeacher) {
        log.info("修改班级教师参数{}{}",m_addNewTeacher.getTeacherName(),m_addNewTeacher.getClassName());

        String resp = m_editInformationService.editClassTeacher(m_addNewTeacher);
        return resp;
    }

    //修改年级班级
    @PostMapping("/mpi/editInformation/editGradeClass")
    public String editGradeClass(@RequestBody M_AddNewTeacher m_addNewTeacher) {
        log.info("修改年级班级参数{}{}",m_addNewTeacher.getGrade(),m_addNewTeacher.getClassNum());

        String resp = m_editInformationService.editGradeClass(m_addNewTeacher);
        return resp;
    }

    //查询反馈文本
    @GetMapping("/mpi/editInformation/getFeedbackContent")
    public M_FeedBackReportVO getFeedbackContent(@RequestParam("school") String school, @RequestParam("label") String label) {
        log.info("查询反馈文本的参数是：school={}{}", school,label);
        M_FeedBackReportVO mFeedBackReportVO = m_editInformationService.getFeedbackContent(school,label);
        return mFeedBackReportVO;

    }

    //编辑反馈文本
    @PostMapping("/mpi/editInformation/editFeedbackContent")
    public String editFeedbackContent(@RequestBody M_FeedBackReportVO mFeedBackReportVO) {

        m_editInformationService.editFeedbackContent(mFeedBackReportVO);
        return "success";
    }


    //获取反馈数据
    @GetMapping("/mpi/editInformation/editData")
    public List<M_FeedbackVO> getFeedbackData(String school, String label, LocalDate checkDate, String content, String timeZone, String type) {
        log.info("获取反馈数据的参数是：school={},label={},checkDate={},content={},timeZone={},type={}", school,label,checkDate,content,timeZone,type);
        M_FeedbackDTO mFeedbackDTO = new M_FeedbackDTO();
        mFeedbackDTO.setSchool(school);
        mFeedbackDTO.setLabel(label);
        mFeedbackDTO.setCheckDate(checkDate);
        mFeedbackDTO.setContent(content);
        mFeedbackDTO.setTimeZone(timeZone);
        mFeedbackDTO.setType(type);
        List<M_FeedbackVO> mFeedbackVOList = m_editInformationService.getFeedbackData(mFeedbackDTO);
        return mFeedbackVOList;
    }

    //一键删除反馈数据
    @PostMapping("/mpi/editInformation/deleteFeedbackData")
    public String deleteFeedbackData(@RequestBody M_DeleteReadingFeedbackDTO mDeleteReadingFeedbackDTO) {
        log.info("删除反馈数据的参数是：ids={},checkDate={},type={},content={},timeZone={},label={},school={}", mDeleteReadingFeedbackDTO.getIds(),mDeleteReadingFeedbackDTO.getCheckDate(),mDeleteReadingFeedbackDTO.getType(),mDeleteReadingFeedbackDTO.getContent(),mDeleteReadingFeedbackDTO.getTimeZone(),mDeleteReadingFeedbackDTO.getLabel(),mDeleteReadingFeedbackDTO.getSchool());
       String resp =  m_editInformationService.deleteFeedbackData(mDeleteReadingFeedbackDTO);
        return resp;
    }

    //上传文件
    @PostMapping(value = "/mpi/editInformation/uploadFile", produces = "application/json;charset=UTF-8")
    public String uploadFile(@RequestParam("file") MultipartFile file, @RequestParam("school") String school) {
        log.info("上传文件的参数是：{}", file);

        if (file == null || file.isEmpty()) {
            return "文件错误：上传文件为空";
        }

        // 解析 Excel 文件并存入数据库
        try (InputStream inputStream = file.getInputStream();
             Workbook workbook = new XSSFWorkbook(inputStream)) {
            if (workbook.getNumberOfSheets() == 0) {
                return "文件错误：Excel 中没有工作表";
            }

            Sheet sheet = workbook.getSheetAt(0);
            Row headerRow = sheet.getRow(0);
            if (headerRow == null) {
                return "文件错误：缺少表头";
            }

            DataFormatter formatter = new DataFormatter();
            String firstHeader = formatter.formatCellValue(headerRow.getCell(0)).strip();
            String secondHeader = formatter.formatCellValue(headerRow.getCell(1)).strip();
            log.info("上传文件的表头是：firstHeader={}, secondHeader={}", firstHeader, secondHeader);

            if ("姓名".equals(firstHeader) && "学科".equals(secondHeader)) {
                return m_readingService.updateTeacherData(file, school);
            }

            if ("年级:例如一年级".equals(firstHeader) && "班级:例如8".equals(secondHeader)) {
                return m_readingService.updateClassTeacherData(file, school);
            }

            return "文件错误：表头不匹配，实际表头为：" + firstHeader + " / " + secondHeader;

        } catch (IOException e) {
            log.error("读取上传的 Excel 文件失败", e);
            throw new RuntimeException(e);
        }


    }







}
