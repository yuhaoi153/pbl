package flyfish.contoller;


import flyfish.pojo.DTO.*;
import flyfish.pojo.M_ExamName;
import flyfish.pojo.VO.M_ExamNameListVO;
import flyfish.pojo.VO.M_ExaminationExcelVO;
import flyfish.pojo.VO.M_QueryClassExaminationVO;
import flyfish.pojo.VO.M_StudentExamScoreVO;
import flyfish.service.M_ExaminationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@RestController
@Slf4j
public class M_ExamController {

    @Autowired
    private  M_ExaminationService examExcelService;

    /**
     * 生成班级成绩导入的模板
     * @param grade
     * @param className
     * @param school
     * @return
     * @throws IOException
     */
    @GetMapping("/mpi/exam/geneImportTemplate")
    public ResponseEntity<byte[]> generateImportTemplate(
            String grade,
             Integer className,
             String school,
            String examName,
            String subject
    ) throws IOException {

        // 生成Excel字节数组
        byte[] excelBytes = examExcelService.generateImportTemplate(
                school,
                grade,
                className
        );

        String fileName = school
                + grade
                + className
                + "班"
                +examName+"/"+subject+"/"+
                "成绩导入模板.xlsx";

        HttpHeaders headers = new HttpHeaders();

        headers.setContentType(
                MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument."
                                + "spreadsheetml.sheet"
                )
        );

        // 处理中文文件名
        headers.setContentDisposition(
                ContentDisposition.attachment()
                        .filename(fileName, StandardCharsets.UTF_8)
                        .build()
        );

        headers.setContentLength(excelBytes.length);

        return ResponseEntity.ok()
                .headers(headers)
                .body(excelBytes);
    }



    /**
     *  批量上传成绩前确认是否已有成绩
     * @param school
     * @param grade
     * @param className
     * @param subject
     * @param examName
     * @param teacherName
     * @return
     */
    @GetMapping(value = "/mpi/exam/confirmUpload" , produces = "application/json;charset=UTF-8")
    public String confirmUpload(String school,String grade, Integer className,String subject,String examName,String teacherName){
        log.info("批量上传成绩前确认是否已有成绩{},{}",examName,teacherName);
        String resp = examExcelService.confirmUpload(school,grade,className,subject ,examName,teacherName);
        return resp;
    }



    /**
     * 导入学生考试成绩
     * 请求类型：multipart/form-data
     */
    @PostMapping(
            value = "/mpi/exam/importExcel",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<M_ExaminationExcelVO> importExcel(
            @RequestParam("file")
            MultipartFile file,

            @RequestParam("school")
            String school,

            @RequestParam("grade")
            String grade,

            @RequestParam("className")
            Integer className,

            @RequestParam("subject")
            String subject,

            @RequestParam("examName")
            String examName,

            @RequestParam("createName")
            String createName
    ) throws IOException {

        M_ExaminationExcelVO result =
                examExcelService.importExcel(
                        file,
                        school,
                        grade,
                        className,
                        subject,
                        examName,
                        createName
                );

        return ResponseEntity.ok(result);
    }

    /**
     * 查询班级概览情况
     * @param queryExaminationDTO
     * @return
     */
    @PostMapping("/mpi/exam/queryClassExam")
    public M_QueryClassExaminationVO  queryClassExam(M_QueryExaminationDTO queryExaminationDTO){
        log.info("查询班级某次测试的概览数据：{}",queryExaminationDTO);
        M_QueryClassExaminationVO m_QueryClassExaminationVO = examExcelService.queryClassExamination(queryExaminationDTO);
        return m_QueryClassExaminationVO;
    }

    /**
     * 查询年级的概览情况
     * @param queryExaminationDTO
     * @return
     */
    @PostMapping("/mpi/exam/queryGradeExam")
    public M_QueryClassExaminationVO   queryGradeExam(M_QueryExaminationDTO queryExaminationDTO){
        log.info("查询年级某次测试的概览数据：{}",queryExaminationDTO);
        M_QueryClassExaminationVO mQueryClassExaminationVO = examExcelService.queryGradeExamination(queryExaminationDTO);
        return  mQueryClassExaminationVO;}


    /**
     * 查询所有的已经录入的考试名称，并按照时间来排序
     * @param school
     * @param teacherName
     * @return
     */
    @GetMapping("/mpi/exam/queryExamName")
    public M_ExamNameListVO queryExamName(String school,String teacherName,String grade,Integer className,String subject){
        log.info("查询预设和已考的所有的考试名称：{}{}",school,teacherName);
        M_ExamNameListVO m_ExamNameListVO = examExcelService.queryExamName(school,teacherName,grade,className,subject);
        return m_ExamNameListVO;
    }

    /**
     * 查询某个班级某次考试全部的成绩
     * @param school
     * @param grade
     * @param className
     * @param subject
     * @param examName
     * @return
     */
    @GetMapping("/mpi/exam/queryAllStudentScore")
    public List<M_StudentExamScoreVO> queryAllStudentScore(String school, String grade, Integer className, String subject, String examName){
        List<M_StudentExamScoreVO> m_StudentExamScoreVOList = examExcelService.queryAllStudentScore(school,grade,className,subject,examName);
        return m_StudentExamScoreVOList;
    }


    //修改，删除，某个学生分数或者覆盖某个考试成绩之前，还需呀提供图片证据
    //如果从默认数据库里查询到这个考试是属于限制修改的，那么就需要提供图片证据，才能修改成绩
    //图片可以添加多个，多个图片的url以；来链接
    //图片可以是和管理员聊天的截图，可以是试卷拍照


    /**
     * 修改某个已经考试的学生分数
     * @param mUpdateExamStudentScoreDTO
     * @return
     */
    @PostMapping("/mpi/exam/updateSingleScore")
    public String updateSingleScore(M_UpdateExamStudentScoreDTO mUpdateExamStudentScoreDTO){
        log.info("修改某次考试某个考生的成绩{}",mUpdateExamStudentScoreDTO);
        String resp = examExcelService.updateSingleScore(mUpdateExamStudentScoreDTO);

        return resp;
    }


    //删除某次考试的成绩
    @PostMapping("/mpi/exam/deleteExamRecord")
    public String deleteExamRecord(M_ExamDeleteDTO mExamDeleteDTO){
        log.info("删除某次考试的信息：{}",mExamDeleteDTO);
        String resp = examExcelService.deleteExamRecord(mExamDeleteDTO);
        return resp;
    }



    //新增考试名称
    @PostMapping(value = "/mpi/exam/addExamName",produces =  "application/json;charset=UTF-8" )
    public String addExamName(M_ExamName mExamName){
        log.info("新增考试名称{}",mExamName);
        String resp = examExcelService.insertExamName(mExamName);
        return resp;
    }



    //删除考试名称
    @PostMapping("/mpi/exam/deleteExamName")
    public String deleteExamName(M_ExamNameDeleteDTO mExamNameDeleteDTO){
        log.info("删除某次考试名称{}",mExamNameDeleteDTO);
        String resp = examExcelService.deleteExamName(mExamNameDeleteDTO);
        return resp;
    }



    //新增考试学年学期
    @PostMapping("/mpi/exam/insertSemester")
    public String insertSemester(M_SemesterInfoDTO mSemesterInfoDTO){
        log.info("新增考试学年学期{}",mSemesterInfoDTO);
        String resp = examExcelService.insertSemester(mSemesterInfoDTO);
        return resp;
    }

    //删除考试学年学期
    @GetMapping("/mpi/exam/deleteSemester")
    public String deleterSemester(String semester,String school){
        log.info("删除某次考试学年信息{}{}",school,semester);
        String resp = examExcelService.deleteSemester(school,semester);
        return resp;
    }

    //查询考试学年学期
    @GetMapping("/mpi/exam/querySemester")
    public List<String> querySemester(String school){
        log.info("查询学年学期信息{}",school);
        List<String> resp = examExcelService.querySemester(school);
        return resp;
    }



    //查询年级对比信息
    @GetMapping("/mpi/exam/queryGradeCompare")
    public List<M_QueryClassExaminationVO>  queryGradeCompare(M_QueryExaminationDTO queryExaminationDTO){
        log.info("查询年级对比信息{}",queryExaminationDTO);
        List<M_QueryClassExaminationVO> mQueryClassExaminationVOList = examExcelService.queryGradeCompare(queryExaminationDTO);
        return mQueryClassExaminationVOList;

    }


    //查询历次考试的概览数据
    @GetMapping("/mpi/exam/queryHistoryExam")
    public List<M_QueryClassExaminationVO> queryHistoryExam(M_QueryExaminationDTO queryExaminationDTO){
        log.info("查询历次考试的概览数据{}",queryExaminationDTO);
        List<M_QueryClassExaminationVO> mQueryClassExaminationVOList = examExcelService.queryHistoryExam(queryExaminationDTO);
        return mQueryClassExaminationVOList;}


    //查询某个学生历次考试全部的成绩
    @PostMapping("/mpi/exam/queryStudentAllExam")
    public List<M_StudentExamScoreVO> queryStudentAllExam(M_QueryStudentAllExamDTO  queryStudentALlExamDTO){
        log.info("查询学生历次考试的成绩{}",queryStudentALlExamDTO);
        List<M_StudentExamScoreVO> mStudentExamScoreVOList = examExcelService.queryStudentAllExam(queryStudentALlExamDTO);
        return mStudentExamScoreVOList;
    }


    //更改考试的发布状态
    @PostMapping("/mpi/exam/updateExamHide")
    public String updateExamHide(M_UpdateExamHideDTO mUpdateExamHideDTO){
        log.info("更改考试的发布状态{}",mUpdateExamHideDTO);
        String resp = examExcelService.updateExamHide(mUpdateExamHideDTO);
        return resp;
    }













}