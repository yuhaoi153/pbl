package flyfish.service;

import flyfish.pojo.DTO.M_HardWareOledUploadDTO;
import flyfish.pojo.VO.M_HardWareOledNotificationVO;
import flyfish.pojo.VO.M_HardWareOledUploadResultVO;

import java.util.Optional;

public interface M_HardWareOledService {
    M_HardWareOledUploadResultVO uploadNotification(
            M_HardWareOledUploadDTO request);

    Optional<M_HardWareOledNotificationVO> getLatestNotification(
            String deviceId,
            Long knownVersion);
}
