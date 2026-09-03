//package flyfish.contoller;
//
//import flyfish.pojo.DTO.NoticeDTO;
//import flyfish.pojo.Result;
//import flyfish.service.NoticeService;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RequestBody;
//import org.springframework.web.bind.annotation.RestController;
//
//@RestController
//@Slf4j
//public class NotificationController {
//    @Autowired
//    private NoticeService noticeService;
//
//    /**
//     * 修改短信或者邮件个人信息
//     * @param noticeDTO
//     * @return
//     */
//    @PostMapping("/tpi/updateNotice")
//    public Result<String> updateNotice(@RequestBody NoticeDTO noticeDTO){
//        log.info("前端传递的通知信息：{}",noticeDTO);
//        log.info("ChineseEmail: {}", noticeDTO.getChineseEmail());
//        String result = noticeService.updateNotice(noticeDTO);
//        return Result.success("OK");
//
//    }
//
//    /**
//     * 自动查询邮件或者手机号的个人信息
//     * @param username
//     * @return
//     */
//    @GetMapping("/tpi/queryNotice")
//    public Result<NoticeDTO> queryMailPhone(String username){
//        log.info("要查询的邮件或手机号班级为：()",username);
//        NoticeDTO result = noticeService.queryNotice(username);
//        return Result.success(result);
//    }
//}
