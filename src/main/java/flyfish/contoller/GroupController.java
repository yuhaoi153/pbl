//package flyfish.contoller;
//
//import flyfish.pojo.DTO.GroupFeedbackDTO;
//import flyfish.pojo.Result;
//import flyfish.pojo.VO.GroupFeedbackVO;
//import flyfish.service.GroupFeedbackService;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RequestBody;
//import org.springframework.web.bind.annotation.RestController;
//
//import java.util.List;
//
//@RestController
//@Slf4j
//public class GroupController {
//    @Autowired
//    private GroupFeedbackService groupService;
//
//    @PostMapping("/tpi/uploadGroup")
//    public Result<List<GroupFeedbackVO>> uploadGroup(@RequestBody GroupFeedbackDTO groupFeedbackDTO){
//        log.info("上传分组的信息：{},{},{},{}",groupFeedbackDTO.getClassNumber(),groupFeedbackDTO.getSubject(),groupFeedbackDTO.getValue(),groupFeedbackDTO.getGroup());
//        List<GroupFeedbackVO> groupFeedbackVO = groupService.uploadGroup(groupFeedbackDTO);
//       return Result.success(groupFeedbackVO);
//    }
//    @GetMapping("/tpi/queryGroup")
//    public Result<List<GroupFeedbackVO>> queryGroup( String subject, String classNumber,String school){
//
//        if(school == null || school.equals("")){
//            school = "附小";
//        }
//        List<GroupFeedbackVO> groupFeedbackVO = groupService.queryGroup(subject,classNumber,school);
//        return Result.success(groupFeedbackVO);
//    }
//
//    @PostMapping("/tpi/addGroupScore")
//    public Result<List<GroupFeedbackVO>> addGroupScore(@RequestBody GroupFeedbackDTO groupFeedbackDTO){
//        log.info("上传分组的信息：{},{},{},{}",groupFeedbackDTO.getClassNumber(),groupFeedbackDTO.getSubject(),groupFeedbackDTO.getScore(),groupFeedbackDTO.getGroup());
//        List<GroupFeedbackVO> groupFeedbackVO = groupService.addGroupScore(groupFeedbackDTO);
//       return Result.success(groupFeedbackVO);
//    }
//
//    @PostMapping("/tpi/punishGroupScore")
//    public Result<List<GroupFeedbackVO>> punishGroupScore(@RequestBody GroupFeedbackDTO groupFeedbackDTO){
//        log.info("上传分组的信息：{},{},{},{}",groupFeedbackDTO.getClassNumber(),groupFeedbackDTO.getSubject(),groupFeedbackDTO.getScore(),groupFeedbackDTO.getGroup());
//        List<GroupFeedbackVO> groupFeedbackVO = groupService.punishGroupScore(groupFeedbackDTO);
//       return Result.success(groupFeedbackVO);
//    }
//
//
//}
