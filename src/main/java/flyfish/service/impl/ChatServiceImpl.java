package flyfish.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import flyfish.mapper.*;
import flyfish.pojo.ChatMessage;
import flyfish.pojo.DTO.M_IDDTO;
import flyfish.pojo.PBLpojo.PBL_AudioPerform;
import flyfish.pojo.PBLpojo.PBL_VoteDTO;
import flyfish.pojo.Perform;
import flyfish.pojo.VO.PrivateMessageVO;
import flyfish.service.ChatService;
import flyfish.utils.AliyunAudioRecognitionUtil;
import flyfish.utils.BaiWenXinUtills;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class ChatServiceImpl implements ChatService {

    @Autowired
    private PBL_ChatMessageMapper chatMessageMapper;
    @Autowired
    private SimpMessagingTemplate messagingTemplate;
    @Autowired
    private UserMapper userMapper;

    @Autowired
    private AliyunAudioRecognitionUtil aliyunAudioRecognitionUtil;
    @Autowired
    private BaiWenXinUtills baiWenXinUtills;

    @Autowired
    private PBL_GeoMapper  pbL_GeoMapper;

    @Autowired
    PBL_QuestionAnswerMapper  pbL_QuestionAnswerMapper;

    /**
     * 存储并转发私聊消息
     * @param message
     */
    @Override
    public void handlePrivateMessage(ChatMessage message) {


        //根据senderId拿到发送者用户名


        message.setCheckDate(LocalDate.now());
        String username= userMapper.getUsernameBySenderId(Integer.valueOf(message.getSenderId()));
        message.setSenderName(username);
        //把message消息存储到对应的数据库
        chatMessageMapper.storePrivateMessage(message);


        // 发送给特定用户
        messagingTemplate.convertAndSendToUser(
                message.getReceiverId(),
                "/notificationMessage", // 对应前端订阅的 /user/{userId}/message
                message
        );


        // 发送给特定用户
        messagingTemplate.convertAndSendToUser(
                message.getSenderId(),
                "/notificationMessage", // 对应前端订阅的 /user/{userId}/message
                message
        );
    }

    @Override
    public List<PrivateMessageVO> getPrivateMassage(Integer userId) {
        LocalDate checkDate = LocalDate.now();
        List<PrivateMessageVO> privateMessageVOList = chatMessageMapper.getPrivateMassageByIdCheckDate(userId,checkDate);

        return privateMessageVOList;
    }

    @Override
    public String deletePBLMessage(M_IDDTO iddto) {
        for(Integer id : iddto.getIdList())
        {
            chatMessageMapper.deleteById(id);
        }
        return "";
    }

    @Override
    public List<String> uploadAudioProcessByAI(MultipartFile file, String fileUrl, String school,String lessonName,String subject,String pageName,String username,Integer questionId) throws Exception {
        String audioText = aliyunAudioRecognitionUtil.recognize(file);
        if(audioText.length()<5){
            List<String> list = new ArrayList<>();
            list.add(audioText);
            list.add("语音识别结果过短，请重新录制");
            return list;
        }


        String jsonFeedback = baiWenXinUtills.getJsonPBLFindAndRank(audioText);
        String jsonString = jsonFeedback.replaceAll("(?s)```json\\s*(\\{.*?\\})\\s*```", "$1");
        ObjectMapper mapper = new ObjectMapper();
        PBL_AudioPerform pblAudioPerform = mapper.readValue(jsonString, PBL_AudioPerform.class);


        PBL_VoteDTO pblVoteDTO = new PBL_VoteDTO();


        pblVoteDTO.setUsername(username);
        pblVoteDTO.setSchool(school);
        pblVoteDTO.setLessonName(lessonName);
        pblVoteDTO.setCheckDate(LocalDate.now());
        pblVoteDTO.setQuestionType("问答题");
        pblVoteDTO.setQuestionName("图形密铺和什么有关？为什么？");
        pblVoteDTO.setAnswerContent(audioText);
        pblVoteDTO.setPageName(pageName);
        pblVoteDTO.setReferenceAnswer("和角度之和是否和360度相关");
        pblVoteDTO.setAnswerType("audio");
        pblVoteDTO.setQuestionId(questionId);
        pblVoteDTO.setScore(pblAudioPerform.getScore());
        pblVoteDTO.setComment(pblAudioPerform.getComment());
        pblVoteDTO.setSupplementary(fileUrl);

        LocalDate checkDate = LocalDate.now();


        PBL_VoteDTO checkExisting = pbL_QuestionAnswerMapper.getByUsernameAndQuestionId(school,checkDate,username, questionId);
        if (checkExisting != null) {
            // 如果存在，执行更新操作
            pbL_QuestionAnswerMapper.updateItem(pblVoteDTO);

            //
        }else {


        pbL_QuestionAnswerMapper.addItem(pblVoteDTO);}


        List<String> feedbackList = new ArrayList<>();
        feedbackList.add(audioText);
        feedbackList.add(pblAudioPerform.getComment());


        //推送到教师端
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setMessageId(pblVoteDTO.getId());
        Integer userId = userMapper.getPBLIdByUserNameAndSchool(school,username);
        chatMessage.setReceiverId("1");
        chatMessage.setSenderId(String.valueOf(userId));
        chatMessage.setSenderName(username);
        chatMessage.setAudioText(audioText);
        chatMessage.setMessageContent(fileUrl);
        chatMessage.setSupplementary(pageName);
        chatMessage.setMessageType(pblAudioPerform.getComment());
        chatMessage.setSentAt(LocalDateTime.now());




        // 发送给特定用户
        messagingTemplate.convertAndSendToUser(
                chatMessage.getReceiverId(),
                "/notificationMessage", // 对应前端订阅的 /user/{userId}/message
                chatMessage
        );

        return feedbackList;







    }

    @Override
    public String deleteAudioAI(M_IDDTO iddto) {
        for(Integer id : iddto.getIdList())
        {
            pbL_QuestionAnswerMapper.deleteById(id);
        }
        return "";
    }
}
