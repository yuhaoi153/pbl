package flyfish.mapper;

import flyfish.pojo.M_ReadingFeedbackReport;
import flyfish.pojo.VO.M_FeedBackReportVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface M_ReadingFeedbackReportMapper {
    @Select("select * from miniprograme.readingFeedbackReport where school=#{school}")
    M_ReadingFeedbackReport getFeedbackReport(String school);

    @Select("select * from miniprograme.readingFeedbackReport where school=#{school}")
    M_FeedBackReportVO getFeedbackContent(String school);



    void editFeedbackContent(M_FeedBackReportVO mFeedBackReportVO);
}
