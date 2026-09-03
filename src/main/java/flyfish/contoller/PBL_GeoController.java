package flyfish.contoller;

import flyfish.pojo.ChatMessage;
import flyfish.pojo.DTO.M_IDDTO;
import flyfish.pojo.Menu;
import flyfish.pojo.PBLpojo.PBL_LoginDTO;
import flyfish.pojo.PBLpojo.PBL_QuestionItem;
import flyfish.pojo.PBLpojo.PBL_VoteDTO;
import flyfish.pojo.PBLpojo.PBL_VoteResult;
import flyfish.pojo.VO.M_BackGroundVO;
import flyfish.pojo.VO.PrivateMessageVO;
import flyfish.service.ChatService;
import flyfish.service.PBL_GeoService;
import flyfish.utils.AliOSSUtils;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@Slf4j
public class PBL_GeoController {
    @Autowired
    private PBL_GeoService pBL_GeoService;
    @Autowired
    private AliOSSUtils aliOSSUtils;
    @Autowired
    private ChatService chatService;


    private final SimpMessagingTemplate messagingTemplate;
    public PBL_GeoController(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @MessageMapping("/private")
    public void handlePrivateMessage(@Payload ChatMessage message) {
        log.info("处理截图私聊的消息: " + message);
        log.info("接收消息的用户是{}",message.getReceiverId());
        //调用service处理数据
        chatService.handlePrivateMessage(message);

    }

    /**
     * 获取私聊消息

     * @return
     */
    @GetMapping("/mpi/chat/getPBLPrivateMessage")
    public List<PrivateMessageVO> getPrivateMessage(Integer userId) {
        log.info( "查询截图聊天:{}", userId);
        List<PrivateMessageVO> privateMessageVOList = chatService.getPrivateMassage(userId);

        return privateMessageVOList;
    }

    @PostMapping("/mpi/chat/deletePBLMessage")
    public String deletePBLMessage(@RequestBody M_IDDTO iddto) {
        log.info("删除截图聊天消息: {}", iddto);
        String resp = chatService.deletePBLMessage(iddto);
        return resp;
    }



    @PostMapping("/mpi/chat/deleteAudioAI")
    public String deleteAudioAI(@RequestBody M_IDDTO iddto){
        log.info("删除AI评分的语音消息: {}", iddto);
        String resp = chatService.deleteAudioAI(iddto);
        return resp;
    }


    @PostMapping("/mpi/pbl/uploadFinalWork")
    public  String uploadFinalWork(@RequestBody PBL_VoteDTO pblVoteDTO){
        log.info("上传最终作品的信息为{}", pblVoteDTO);
        String resp = pBL_GeoService.uploadFinalWork(pblVoteDTO);
        return resp;
    }


    @PostMapping("/mpi/chat/uploadImage")
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
     * 接收语音消息,
     //上传语音消息并交给大模型判断评分，我猜想可能和什么有关，理由是什么，
     */
    @PostMapping("/mpi/chat/uploadAudioFile")
    public List<String> uploadFile(@RequestParam("file") MultipartFile file,String school,String lessonName,String subject,String pageName,String username,Integer questionId) throws Exception {
        log.info("发送的录音用户信息是是：{}",username);
        String fileUrl = aliOSSUtils.uploadByFilePath(file,"homework/chatAudio/");
        List<String> resp = chatService.uploadAudioProcessByAI(file,fileUrl,school,lessonName,subject,pageName,username,questionId);
        return resp;
    }








    /**
     * 登录操作
     * @param pBL_LoginDTO
     * @return
     */
    @PostMapping("/mpi/pbl/login")
    public List<Menu> login(@RequestBody PBL_LoginDTO pBL_LoginDTO) {
        log.info("公开课登录的用户: {}", pBL_LoginDTO.getUsername());
        List<Menu> resp = pBL_GeoService.confirmUser(pBL_LoginDTO.getUsername(), pBL_LoginDTO.getPassword(), pBL_LoginDTO.getSchool());
        return resp;

    }

    @PostMapping("/mpi/pbl/unlockMenu")
    public String unlockMenu(@RequestBody PBL_LoginDTO pBL_LoginDTO) {
        log.info("解锁菜单的用户: {}", pBL_LoginDTO.getUsername());
        String resp = pBL_GeoService.unlockMenu(pBL_LoginDTO.getUsername(), pBL_LoginDTO.getSchool(),pBL_LoginDTO.getMenuName());
        return resp;

    }

    @GetMapping("/mpi/pbl/getStatus")
    public String getStatus(String menuName) {

        String status = pBL_GeoService.getStatus(menuName);
        return status;
    }


    /**
     * 获取公开课页面的图片链接
     * @param position
     * @param school
     * @return
     */
    @GetMapping("/mpi/pbl/getImgUrl")
    public List<M_BackGroundVO> getImgUrl(String position, String school) {
        log.info("获取公开课页面{}和{}图片url",position,school);
        List<M_BackGroundVO> resp = pBL_GeoService.getImgUrl(position,school);

        return resp;

    }


    /**
     * 进行班级投票
     * @param pBL_VoteDTO
     * @return
     */
    @PostMapping("/mpi/pnl/vote")
    public String votePbl(@RequestBody PBL_VoteDTO pBL_VoteDTO) {
        log.info("进行投票的信息为{}", pBL_VoteDTO);
        pBL_VoteDTO.setCheckDate(LocalDate.now());
        String resp = pBL_GeoService.votePbl(pBL_VoteDTO);
        return resp;
    }


    /**
     * 对最终作品投票，作为用户的回答
     * @param
     * @return
     */
    @PostMapping("/mpi/pnl/voteResult")
    public String votePblResult(@RequestBody PBL_VoteDTO pBL_VoteDTO) {
        log.info("进行最终作品投票的信息为{}", pBL_VoteDTO);
        String resp = pBL_GeoService.votefinalPbl(pBL_VoteDTO);
        return resp;
    }


    //获取班级最终作品投票结果，并为每个用户的作品打分，都是根据supplementary来进行判断
    @GetMapping("/mpi/pbl/getFinalWorkRankScore")
    public Map<Integer,Integer> getFinalWorkRankScore(String school, String lessonName, String subject, String pageName, LocalDate checkDate) {
        log.info("获取班级最终作品投票结果的信息为{}和{}和{}和{}", school, lessonName, subject, pageName);
        //根据学校、课题、学科、页面获取班级最终作品投票结果
        Map<Integer, Integer> finalWorkRankScore = pBL_GeoService.getfinalWorkRank(school, lessonName, subject, pageName,checkDate);

        return finalWorkRankScore;
    }



    //获取学生数学思维的得分前三名
    @GetMapping("/mpi/pbl/getMathMindScore")
    public List<Map<String, Integer>>  getMathMindScore(String school, String lessonName, String subject, String pageName, LocalDate checkDate) {
        log.info("获取数学思维前三的信息为{}和{}和{}和{}", school, lessonName, subject, pageName);
        //根据学校、课题、学科、页面获取班级最终作品投票结果
        List<Map<String, Integer>>  getMathMindThree = pBL_GeoService.getMathMindFirstThree(school, lessonName, subject, pageName,checkDate);


        return getMathMindThree;
    }

    //获取学生总分前三名
    @GetMapping("/mpi/pbl/getMathSumScore")
    public List<Map<String, Integer>>  getMathSumThree(String school, String lessonName, String subject, String pageName, LocalDate checkDate) {
        log.info("获取数学总分前三的信息为{}和{}和{}和{}", school, lessonName, subject, pageName);
        //根据学校、课题、学科、页面获取班级最终作品投票结果
        List<Map<String, Integer>>  getMathSumThree = pBL_GeoService.getMathSumFirstThree(school, lessonName, subject, pageName,checkDate);


        return getMathSumThree;
    }



    /**
     * 获取投票结果，个人的
     * @param school
     * @param lessonName
     * @param subject
     * @param pageName
     * @param username
     * @return
     */
    @GetMapping("/mpi/pbl/getVoteResult")
    public List<PBL_VoteDTO> getVoteResult(String school, String lessonName, String subject, String pageName,String username,LocalDate checkDate) {
        log.info("获取投票结果的信息为{}和{}和{}和{}和{}", school, lessonName, subject, pageName);
        List<PBL_VoteDTO> voteResults = pBL_GeoService.getVoteResult(school, lessonName, subject, pageName,username,checkDate);
        return voteResults;
    }

    @GetMapping("/mpi/pbl/getQuestionInfo")
    public List<PBL_QuestionItem> getQuestionInfo(String pageName, String school, String lessonName, String subject,LocalDate checkDate) {
        log.info("获取题目信息的参数为{}和{}和{}和{}和{}", pageName,school,lessonName,subject,checkDate);
        //根据页面、学校、课题、学科、用户名获取题目信息
        List<PBL_QuestionItem> pblQuestionItems= pBL_GeoService.getQuestionInfo(pageName,school,lessonName,subject,checkDate);
        return pblQuestionItems;
    }

//

    /**
     * 获取全班的投票结果
     * @param school
     * @param lessonName
     * @param subject
     * @param pageName
     * @param voteA
     * @param voteB
     * @param voteC
     * @param voteD
     * @return
     */
    @GetMapping("mpi/pbl/getClassVoteResult")
    public Map<String,PBL_VoteResult> getClassVoteResult(String school, String lessonName, String subject, String pageName, String voteA, String voteB, String voteC, String voteD,LocalDate checkDate) {
        log.info("获取班级投票结果的信息为{}和{}和{}和{}", school, lessonName, subject, pageName);
        //根据学校、课题、学科、页面获取班级投票结果
        Map<String,PBL_VoteResult>pblVoteResult = pBL_GeoService.getClassVoteResult(school, lessonName, subject, pageName,voteA,voteB,voteC,voteD,checkDate);
        return pblVoteResult;
    }


    @GetMapping("/mpi/pbl/resetAllAnswer")
    public void resetAllAnswer(String school, String lessonName, String subject, String pageName, LocalDate checkDate) {
        log.info("重置所有答案的信息为{}和{}和{}和{}和{}", school, lessonName, subject, pageName,checkDate);
        pBL_GeoService.resetAllAnswer(school, lessonName, subject, pageName,checkDate);

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
