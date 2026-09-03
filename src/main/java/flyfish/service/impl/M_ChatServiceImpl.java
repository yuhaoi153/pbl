package flyfish.service.impl;

import flyfish.mapper.*;
import flyfish.pojo.ChatMessage;
import flyfish.pojo.DTO.M_ChatUserIdDTO;
import flyfish.pojo.DTO.M_ShowHomeWorkDTO;
import flyfish.pojo.DTO.NoticeDTO;
import flyfish.pojo.HomeWorkMessage;
import flyfish.pojo.Notice;
import flyfish.pojo.VO.HomeworkMessageVO;
import flyfish.pojo.VO.PrivateMessageVO;
import flyfish.service.M_ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class M_ChatServiceImpl implements M_ChatService {
    @Autowired
    private M_GradeYearMapper gradeYearMapper;
    @Autowired
    private HomeWorkMessageMapper homeWorkMessageMapper;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;
    @Autowired
    private ChatUserListMapper userListMapper;
    @Autowired
    private NoticeMapper noticeMapper;

    @Autowired
    private ChatMessageMapper chatMessageMapper;



    @Override
    public String uploadHomeworkMessage(M_ShowHomeWorkDTO showHomeWorkDTO) {
//根据school,className拿到receiverId
        String school = showHomeWorkDTO.getSchool();
        String grade = showHomeWorkDTO.getClassName().substring(0,1)+"年级";
        String number = "0"+showHomeWorkDTO.getClassName().substring(2,3) ;
        String classNumber = String.valueOf(gradeYearMapper.getYearByGrade(grade)) + number;
        Integer receiverId = homeWorkMessageMapper.getIdByNameShool(classNumber,school);

        //根据userName 和school 拿到senderId
        Integer senderId = userListMapper.getIdByNameSchool(showHomeWorkDTO.getTeacherName(),showHomeWorkDTO.getSchool());
        //遍历messageTypeList，把消息上传到对应的数据库
        List<HomeWorkMessage> homeWorkMessageList = new ArrayList<>();
        List<String> messageTypeList = showHomeWorkDTO.getMessageTypeList();
        List<String> messageContentList = showHomeWorkDTO.getMessageContentList();
        // 如果两个列表长度不一致，实际开发中建议添加校验或异常处理

        if (messageTypeList.size() != messageContentList.size()) {
            throw new IllegalArgumentException("消息类型列表与内容列表长度不一致");
        }

        // 使用索引循环保证类型与内容一一对应
        for (int i = 0; i < messageTypeList.size(); i++) {
            HomeWorkMessage homeWorkMessage = new HomeWorkMessage();

            homeWorkMessage.setSenderId(senderId);
            homeWorkMessage.setReceiverId(receiverId);
            homeWorkMessage.setMessageType(messageTypeList.get(i));
            homeWorkMessage.setMessageContent(messageContentList.get(i));  // 修正：直接通过索引取对应内容

            homeWorkMessage.setSubject(showHomeWorkDTO.getSubject());
            homeWorkMessage.setClassName(showHomeWorkDTO.getClassName());
            homeWorkMessage.setSchool(showHomeWorkDTO.getSchool());
            homeWorkMessage.setCheckDate(showHomeWorkDTO.getCheckDate());
            homeWorkMessage.setDuration(showHomeWorkDTO.getDuration());

            // 修正 fileSize 赋值：应从 DTO 获取，而不是自己给自己赋值
            homeWorkMessage.setFileSize(showHomeWorkDTO.getFileSize());

            homeWorkMessageMapper.insertSingleItem(homeWorkMessage);
            homeWorkMessageList.add(homeWorkMessage);
        }

        // 发送给特定用户
        messagingTemplate.convertAndSendToUser(
                String.valueOf(receiverId),
                "/homeworkMessageNotification",
                homeWorkMessageList
        );

        return "布置作业成功";
    }

    /**
     * 教师查看已经布置到班级的作业
     * @param teacherName
     * @param school
     * @param subject
     * @param checkDate
     * @return
     */
    @Override
    public List<HomeworkMessageVO> getHomeworkMessage(String teacherName, String school, String subject, LocalDate checkDate,String className) {
       Integer senderId = userListMapper.getIdByNameSchool(teacherName,school);
        List<HomeworkMessageVO> homeworkMessageVOList = homeWorkMessageMapper.getHomeworkMessageByClassName(senderId, checkDate,className);

        return homeworkMessageVOList;
    }

    /**
     * 删掉布置作业的通知
     * @param id
     */
    @Override
    public void deleteHomeworkMessage(Integer id) {
            homeWorkMessageMapper.deleteHomeworkMessageById(id);
    }

    /**
     * 拿到聊天用户的ID，以便于获取私聊消息
     * @param userName
     * @param currentClassName
     * @param school
     * @return
     */
    @Override
    public M_ChatUserIdDTO getChatUserIdByName(String userName, String currentClassName, String school) {
        Integer senderId = userListMapper.getIdByNameSchool(userName,school);
        String grade = currentClassName.substring(0,1)+"年级";
        String number = "0"+currentClassName.substring(2,3) ;
        String classNumber = gradeYearMapper.getYearByGrade(grade) + number;
        Integer receiverId = homeWorkMessageMapper.getIdByNameShool(classNumber,school);
        M_ChatUserIdDTO chatUserIdDTO = new M_ChatUserIdDTO();
        chatUserIdDTO.setSenderId(senderId);
        chatUserIdDTO.setReceiverId(receiverId);
        return chatUserIdDTO;


    }

    @Override
    public List<PrivateMessageVO> getPrivateMassage(Integer senderId, Integer receiverId, LocalDate checkDate) {

        List<PrivateMessageVO> privateMessageVOList = new ArrayList<>();
        if(checkDate==null){
            checkDate = LocalDate.now();
            //根据checkDate倒推10天,并且转化成LocalDateTime,结束日期是23点59，开始时00点00
            LocalDateTime endDate = checkDate.atTime(23, 59, 59);
            LocalDateTime startDate = checkDate.minusDays(30).atTime(0, 0, 0);
            privateMessageVOList = chatMessageMapper.getPrivateMassageByDate(senderId, receiverId, startDate, endDate);
        }else {

            //根据checkDate倒推10天,并且转化成LocalDateTime,结束日期是23点59，开始时00点00
            LocalDateTime endDate = checkDate.atTime(23, 59, 59);
            LocalDateTime startDate = checkDate.minusDays(10).atTime(0, 0, 0);
             privateMessageVOList = chatMessageMapper.getPrivateMassageByDate(senderId, receiverId, startDate, endDate);
        }
        Integer newSenderId = senderId;
        Integer newReceiverId = receiverId;
        senderId= newReceiverId;
        receiverId= newSenderId;
        //这里我希望对方的消息，也就是班级对象作为发送者，自己作为接收者的消息变为已读
        chatMessageMapper.setMessageRead(senderId, receiverId);

        messagingTemplate.convertAndSendToUser(
                senderId.toString(),
                "/messageRead",
                receiverId.toString() // 通知发送者消息已读
        );

        return privateMessageVOList;


    }

    @Override
    public void handlePrivateMessage(ChatMessage message) {
        //把message消息存储到对应的数据库
        chatMessageMapper.storePrivateMessage(message);
        // 发送给特定用户
        messagingTemplate.convertAndSendToUser(
                message.getReceiverId(),
                "/notificationMessage", // 对应前端订阅的 /user/{receiverId}/notificationMessage
                message
        );
    }

    /**
     * 删除私聊消息
     * @param messageId
     */
    @Override
    public void deletePrivateMessage(Integer messageId) {
        chatMessageMapper.deleteById(messageId);
    }

    @Override
    public NoticeDTO queryNotice(String username, String school) {
        List<Notice> noticeList = noticeMapper.queryNoticeByClass(username,school);
        NoticeDTO noticeDTO = new NoticeDTO();
        for (Notice notice:
                noticeList) {
            if(notice.getSubject().equals("语文")){
                noticeDTO.setChineseEmail(notice.getMail());
                noticeDTO.setChinesePhone(notice.getPhone());
                noticeDTO.setCES(notice.isCheckMail());
                noticeDTO.setCPS(notice.isCheckPhone());
                noticeDTO.setSchool(notice.getSchool());
            } else if (notice.getSubject().equals("数学")) {
                noticeDTO.setMathEmail(notice.getMail());
                noticeDTO.setMathPhone(notice.getPhone());
                noticeDTO.setMES(notice.isCheckMail());
                noticeDTO.setMPS(notice.isCheckPhone());
                noticeDTO.setSchool(notice.getSchool());
            }else {
                noticeDTO.setEnglishEmail(notice.getMail());
                noticeDTO.setEnglishPhone(notice.getPhone());
                noticeDTO.setEES(notice.isCheckMail());
                noticeDTO.setEPS(notice.isCheckPhone());
                noticeDTO.setSchool(notice.getSchool());
            }
        }

        return noticeDTO;
    }

    @Override
    public String updateNotice(NoticeDTO noticeDTO) {
        List<Notice> noticeList = new ArrayList<>();
        Notice notice = new Notice();
        notice.setName("语文老师");
        notice.setSubject("语文");
        notice.setMail(noticeDTO.getChineseEmail());
        notice.setPhone(noticeDTO.getChinesePhone());
        notice.setCheckMail(noticeDTO.isCES());
        notice.setCheckPhone(noticeDTO.isCPS());
        notice.setSchool(noticeDTO.getSchool());
        noticeList.add(notice);

        Notice noticeMath = new Notice();
        noticeMath.setName("数学老师");
        noticeMath.setSubject("数学");
        noticeMath.setMail(noticeDTO.getMathEmail());
        noticeMath.setPhone(noticeDTO.getMathPhone());
        noticeMath.setCheckMail(noticeDTO.isMES());
        noticeMath.setCheckPhone(noticeDTO.isMPS());
        noticeMath.setSchool(noticeDTO.getSchool());
        noticeList.add(noticeMath);

        Notice noticeEnglish = new Notice();
        noticeEnglish.setName("英语老师");
        noticeEnglish.setSubject("英语");
        noticeEnglish.setMail(noticeDTO.getEnglishEmail());
        noticeEnglish.setPhone(noticeDTO.getEnglishPhone());
        noticeEnglish.setCheckMail(noticeDTO.isEES());
        noticeEnglish.setCheckPhone(noticeDTO.isEPS());
        noticeEnglish.setSchool(noticeDTO.getSchool());
        noticeList.add(noticeEnglish);
        String classNumber = noticeDTO.getUsername();
        String school = noticeDTO.getSchool();

        Integer teacherNumber = noticeMapper.existNotice(classNumber,school);
        if(teacherNumber == 3){
            for (Notice n:noticeList) {
                System.out.println(n.getMail());
                noticeMapper.updateNotice(n,classNumber,school);

            }}
        else {
            noticeMapper.deleteNotice(classNumber,school);
            noticeMapper.addNotice(noticeList,classNumber,school);

        }
        //还需要有一个List，一次性把通知信息都放进去。
        //把对应班级的对应教师的通知方式都更新进去，姓名就是数学教师、语文教师
        return null;
    }

    @Override
    public String getOnlineStatus(Integer receiverId) {
        String status = userListMapper.getStatusById(receiverId);
        return status;


    }
}
