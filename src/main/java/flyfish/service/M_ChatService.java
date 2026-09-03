package flyfish.service;

import flyfish.pojo.ChatMessage;
import flyfish.pojo.DTO.M_ChatUserIdDTO;
import flyfish.pojo.DTO.M_ShowHomeWorkDTO;
import flyfish.pojo.DTO.NoticeDTO;
import flyfish.pojo.VO.HomeworkMessageVO;
import flyfish.pojo.VO.PrivateMessageVO;

import java.time.LocalDate;
import java.util.List;

public interface M_ChatService {
    String uploadHomeworkMessage(M_ShowHomeWorkDTO showHomeWorkDTO);

    List<HomeworkMessageVO> getHomeworkMessage(String teacherName, String school, String subject, LocalDate checkDate,String className);

    void deleteHomeworkMessage(Integer id);

    /**
     * 获取聊天的用户ID，方便获取私聊的消息
     * @param userName
     * @param currentClassName
     * @param school
     * @return
     */
    M_ChatUserIdDTO getChatUserIdByName(String userName, String currentClassName, String school);


    //获取私聊信息
    List<PrivateMessageVO> getPrivateMassage(Integer senderId, Integer receiverId, LocalDate checkDate);

    /**
     * 发送消息
     * @param message
     */
    void handlePrivateMessage(ChatMessage message);

    void deletePrivateMessage(Integer messageId);

    NoticeDTO queryNotice(String username, String school);

    String updateNotice(NoticeDTO noticeDTO);

    String getOnlineStatus(Integer receiverId);
}
