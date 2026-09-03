package flyfish.service;

import com.fasterxml.jackson.core.JsonProcessingException;

import flyfish.pojo.DTO.AlterHomeworkDataDTO;
import flyfish.pojo.DTO.NotificationDTO;
import flyfish.pojo.DTO.PageQueryClassDTO;
import flyfish.pojo.DTO.PageQueryNameDTO;
import flyfish.pojo.DTO.RecordDTO;
import flyfish.pojo.VO.PageQueryClassVO;
import flyfish.pojo.VO.PageQueryNameVO;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

public interface RecordService {
    /**
     * 扫码枪上传数据并反馈
     * @param recordDTO
     * @return
     */
    String uploadFeedback(RecordDTO recordDTO) throws Exception;

    /**
     * 短信或者邮件通知老师
     * @param notificationDTO
     * @return
     */
    List<String> notification(NotificationDTO notificationDTO) throws Exception;

    /**
     * 登记特殊作业
     * @param recordDTO
     * @return
     */
    String uploadFeedbackSpecial(RecordDTO recordDTO);

    /**
     * 查询反馈表格数据
     * @param pageQueryClassDTO
     */
    List<PageQueryClassVO> pageClass(PageQueryClassDTO pageQueryClassDTO);

    /**
     * 查询反馈个人表格数据
     * @param pageQueryNameDTO
     * @return
     */
    List<PageQueryNameVO> pageName(PageQueryNameDTO pageQueryNameDTO);

    /**
     * 上传特殊的操作
     * @param recordDTO
     * @return
     */
    String SpecialHomework(RecordDTO recordDTO);

    /**
     * 作业订正登记
     * @param recordDTO
     * @return
     */
    String homeworkRevison(RecordDTO recordDTO);

    String uploadAudioHomeWork(MultipartFile file, String school, String classNumber, String content, String subject, LocalDate checkdate, String supplementary, String minusScoreByHomework, String failRevisionAddScore,Integer addScoreNumber,Integer minusScoreNumber,String completedRevisionAddScore,Integer revisionAddScore) throws Exception;

    String uploadtestHomeWork(String message, String school, String classNumber, String content, String subject, java.time.LocalDate checkdate, String supplementary, String minusScoreByHomework, String failRevisionAddScore,Integer addScoreNumber,Integer minusScoreNumber) throws Exception;

    /**
     * 家长端查询学生个人作业数据
     * @param pageQueryClassDTO
     * @return
     */
    List<PageQueryClassVO> pageStudent(PageQueryClassDTO pageQueryClassDTO);

    String uploadmessageHomeWork(String content ,LocalDate checkdate,String message, String school, String classNumber, String subject, String supplementary, String minusScoreByHomework, String failRevisionAddScore, Integer addScoreNumber, Integer minusScoreNumber,String completedRevisionAddScore, Integer revisionAddScore) throws Exception;

    String alterHomeworkResult(RecordDTO recordDTO) throws Exception;

    String alterHomeworkData(AlterHomeworkDataDTO alterHomeworkDataDTO);

//    /**
//     * 快速查询未完成作业名单
//     * @param classNumber
//     * @param checkdate
//     * @return
//     */
//    String quickUncompleted(String classNumber, LocalDate checkdate);


}
