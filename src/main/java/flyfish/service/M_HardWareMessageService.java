package flyfish.service;

import flyfish.pojo.DTO.M_HardWareSendMessageDTO;
import flyfish.pojo.VO.M_HardWareDeviceVO;
import flyfish.pojo.VO.M_HardWareSendMessageVO;
import flyfish.pojo.VO.M_HardWareMessagePollVO;

import java.util.List;

public interface M_HardWareMessageService {
    List<M_HardWareDeviceVO> findDevices(
            String school, String grade, Integer className, String teacherName);

    M_HardWareSendMessageVO sendMessage(M_HardWareSendMessageDTO request);

    M_HardWareMessagePollVO pollMessages(
            Integer deviceId, Integer afterId, Integer limit);
}
