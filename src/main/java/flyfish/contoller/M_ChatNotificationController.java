package flyfish.contoller;


import flyfish.pojo.ChatMessage;
import flyfish.pojo.DTO.M_ChatUserIdDTO;
import flyfish.pojo.DTO.NoticeDTO;
import flyfish.pojo.Result;
import flyfish.pojo.VO.PrivateMessageVO;
import flyfish.service.M_ChatService;
import flyfish.utils.AliOSSUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

@RestController
@Slf4j
public class M_ChatNotificationController {

    @Autowired
    private M_ChatService chatService;
    @Autowired
    private AliOSSUtils aliOSSUtils;


    /**
     * 获取聊天用户ID
      * @return
      * @author flyfish
      * @date 2024/6/17 17:00
      * @description 获取聊天用户ID接口，返回当前用户的聊天用户ID，供前端使用
      * @version 1.0
     */
    @GetMapping( "/mpi/notification/getUserId")
    public M_ChatUserIdDTO getChatUserId(String userName, String currentClassName, String school){
        log.info("获取聊天用户ID接口被调用，参数：userName={}, currentClassName={}, school={}", userName, currentClassName, school);
        M_ChatUserIdDTO resp = chatService.getChatUserIdByName(userName,currentClassName,school);
        return resp;

    }


    /**
     * 获取私聊消息
     * @param senderId
     * @param receiverId
     * @return
     */
    @GetMapping("/mpi/chat/getPrivateMessage")
    public List<PrivateMessageVO> getPrivateMessage(Integer senderId, Integer receiverId,LocalDate checkDate) {
        log.info( "查询个人聊天的发送者:{},接收者:{},日期为{}", senderId, receiverId, checkDate );
        List<PrivateMessageVO> privateMessageVOList = chatService.getPrivateMassage(senderId,receiverId,checkDate);

        return privateMessageVOList;
    }


    /**
     * 获取对方的在线状态
     * @param receiverId
     * @return
     */
    @GetMapping("/mpi/chat/getOnlineStatus")
    public String getOnlineStatus(Integer receiverId){
        log.info("查询用户在线状态，用户ID:{}", receiverId);
        String status = chatService.getOnlineStatus(receiverId);
        return status;
    }


    /**
     * 删除某个私聊消息
     * @param messageId
     */
    @GetMapping("/mpi/chat/deletePrivateMessage")
    public void deletePrivateMessage(Integer messageId) {
        log.info( "删除个人聊天消息，消息ID:{}", messageId );
        chatService.deletePrivateMessage(messageId);
    }


    // 处理单聊消息
    @PostMapping("/tpi/chat/private")
    public void handlePrivateMessage(@RequestBody ChatMessage message) {
        log.info("处理用户私聊的消息: " + message);
        log.info("接收消息的用户是{}",message.getReceiverId());
        //调用service处理数据
        chatService.handlePrivateMessage(message);

    }





    /**
     * 接收语音消息
     */
    @PostMapping("/mpi/chat/uploadChatAudio")
    public String uploadFile(@RequestParam("file") MultipartFile file) throws IOException {

        String fileUrl = aliOSSUtils.uploadByFilePath(file,"homework/chatAudio/");
        log.info("发送的录音文件是：{}",fileUrl);
        return fileUrl;
    }

    /**
     * 接收图片消息
     * @param file
     * @return
     * @throws IOException
     */
    @PostMapping("/mpi/chat/uploadChatImage")
    public String uploadImageFile(@RequestParam("file") MultipartFile file) throws IOException {
        // 1. 校验
        if (file.isEmpty()) {
            throw new IllegalArgumentException("上传文件不能为空");
        }
        if (file.getSize() > 20 * 1024 * 1024) {
            throw new IllegalArgumentException("文件大小超过20MB");
        }
        String contentType = file.getContentType();
        if (contentType == null || !isAllowedImageType(contentType)) {
            throw new IllegalArgumentException("不支持的图片格式");
        }

        String url = aliOSSUtils.uploadByFilePath(file, "homework/chatImage/");
        log.info("图片上传成功：{}", url);
        return url;
    }

    /**
     * 上传文件类型，pdf,word,excel
     * @param file
     * @return
     * @throws IOException
     */
    @PostMapping("/mpi/chat/uploadChatPureFile")
    public String uploadPureFile(@RequestParam("file") MultipartFile file) throws IOException {
        // 1. 校验
        if (file.isEmpty()) {
            throw new IllegalArgumentException("上传文件不能为空");
        }
        if (file.getSize() > 50 * 1024 * 1024) {
            throw new IllegalArgumentException("文件大小超过20MB");
        }


        String url = aliOSSUtils.uploadByFilePathAddOriginName(file, "homework/chatFile/");
        log.info("文件上传成功：{}", url);
        return url;
    }


    /**
     * 自动查询邮件或者手机号的个人信息
     * @param username
     * @return
     */
    @GetMapping("/mpi/queryNotice")
    public Result<NoticeDTO> queryMailPhone(String username, String school){
        log.info("要查询的邮件或手机号班级为：()",username);
        if(school == null || school.equals("")){
            school = "附小";
        }
        NoticeDTO result = chatService.queryNotice(username,school);
        return Result.success(result);
    }

    /**
     * 修改短信或者邮件个人信息
     * @param noticeDTO
     * @return
     */
    @PostMapping("/tpi/updateNotice")
    public Result<String> updateNotice(@RequestBody NoticeDTO noticeDTO){
        log.info("前端传递的通知信息：{}",noticeDTO);
        log.info("ChineseEmail: {}", noticeDTO.getChineseEmail());
        if(noticeDTO.getSchool() == null || noticeDTO.getSchool().equals("")){
            noticeDTO.setSchool("附小");
        }
        String result = chatService.updateNotice(noticeDTO);
        return Result.success("OK");
    }


    /**
     * 检查 MIME 类型是否为允许的图片格式
     */
    private boolean isAllowedImageType(String contentType) {
        return contentType.equals("image/jpeg") ||
                contentType.equals("image/png") ||
                contentType.equals("image/gif") ||
                contentType.equals("image/webp")||
                contentType.equals(("image/heic"));
    }

}
