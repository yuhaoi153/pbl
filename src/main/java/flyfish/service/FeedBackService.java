package flyfish.service;

import flyfish.pojo.Charts;
import flyfish.pojo.DTO.ChartDTO;
import flyfish.pojo.FeedbackList;
import flyfish.pojo.VO.AutoDataVO;

import java.time.LocalDate;
import java.util.List;

public interface FeedBackService {


    String insertFeedback(FeedbackList feedbackList);


    /**
     * 根据日期查询并反馈
     * @param checkdate
     * @return
     */
    String getByDate(LocalDate checkdate);

    /**
     * 根据持续时间创建表格
     * @param chartDTO
     * @return
     */
    List<Charts> getDuration(ChartDTO chartDTO);

    /**
     * 根据年级和日期和时段自动查询数据
     * @param grade1
     * @param checkdate
     * @param timeZone
     * @return
     */
    AutoDataVO getByDateGradeTime(String grade1, LocalDate checkdate, String timeZone);
}
