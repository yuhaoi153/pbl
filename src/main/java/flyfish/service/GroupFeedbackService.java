package flyfish.service;

import flyfish.pojo.DTO.GroupFeedbackDTO;
import flyfish.pojo.VO.GroupFeedbackVO;

import java.util.List;

public interface GroupFeedbackService {
    List<GroupFeedbackVO> uploadGroup(GroupFeedbackDTO groupFeedbackDTO);

    //查询分组信息
    List<GroupFeedbackVO> queryGroup(String subject, String classNumber, String school);

    //添加分组分数
    List<GroupFeedbackVO> addGroupScore(GroupFeedbackDTO groupFeedbackDTO);

    //惩罚分组分数
    List<GroupFeedbackVO> punishGroupScore(GroupFeedbackDTO groupFeedbackDTO);
}
