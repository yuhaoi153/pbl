package flyfish.service;

import flyfish.pojo.ChatMessage;
import flyfish.pojo.DTO.M_IDDTO;
import flyfish.pojo.VO.PrivateMessageVO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ChatService {
    void handlePrivateMessage(ChatMessage message);

    List<PrivateMessageVO> getPrivateMassage(Integer userId);

    String deletePBLMessage(M_IDDTO iddto);

    List<String> uploadAudioProcessByAI(MultipartFile file, String fileUrl, String school,String lessonName,String subject,String pageName,String username,Integer questionId) throws Exception;

    String deleteAudioAI(M_IDDTO iddto);
}
