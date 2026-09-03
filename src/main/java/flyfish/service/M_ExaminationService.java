package flyfish.service;

import flyfish.pojo.DTO.*;
import flyfish.pojo.M_ExamName;
import flyfish.pojo.VO.M_ExamNameListVO;
import flyfish.pojo.VO.M_ExaminationExcelVO;
import flyfish.pojo.VO.M_QueryClassExaminationVO;
import flyfish.pojo.VO.M_StudentExamScoreVO;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface M_ExaminationService {
    byte[] generateImportTemplate(String school, String grade, Integer className) throws IOException;

    M_ExaminationExcelVO importExcel(MultipartFile file, String school, String grade, Integer className, String subject, String examName, String createName) throws IOException;

    M_QueryClassExaminationVO queryClassExamination(M_QueryExaminationDTO queryExaminationDTO);

    M_ExamNameListVO queryExamName(String school, String teacherName,String grade, Integer className, String subject);

    List<M_StudentExamScoreVO> queryAllStudentScore(String school, String grade, Integer className, String subject, String examName);

    String updateSingleScore(M_UpdateExamStudentScoreDTO mUpdateExamStudentScoreDTO);

    String deleteExamRecord(M_ExamDeleteDTO mExamDeleteDTO);

    String insertExamName(M_ExamName mExamName);

    String deleteExamName(M_ExamNameDeleteDTO mExamNameDeleteDTO);

    String insertSemester(M_SemesterInfoDTO mSemesterInfoDTO);

    List<String> querySemester(String school);

    String deleteSemester(String school, String semester);

    List<M_QueryClassExaminationVO> queryGradeCompare(M_QueryExaminationDTO queryExaminationDTO);

    String confirmUpload(String school, String grade, Integer className, String subject,String examName, String teacherName);

    M_QueryClassExaminationVO queryGradeExamination(M_QueryExaminationDTO queryExaminationDTO);

    List<M_QueryClassExaminationVO> queryHistoryExam(M_QueryExaminationDTO queryExaminationDTO);

    List<M_StudentExamScoreVO> queryStudentAllExam(M_QueryStudentAllExamDTO queryStudentALlExamDTO);

    String updateExamHide(M_UpdateExamHideDTO mUpdateExamHideDTO);
}
