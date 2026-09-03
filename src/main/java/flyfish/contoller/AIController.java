//package flyfish.contoller;
//
//import flyfish.pojo.DTO.AIAudioDTO;
//import flyfish.pojo.Result;
//import flyfish.service.AIService;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.web.bind.annotation.*;
//
//@RestController
//@Slf4j
//public class AIController {
//    @Autowired
//    private AIService aiService;
//
//    @PostMapping("/tpi/audio/aiPerform")
//    public Result<String> aiPerform(@RequestBody AIAudioDTO aiAudioDTO){
//        log.info("接收AI的班级、学科、信息：{}{}{}",aiAudioDTO.getClassNumber(),aiAudioDTO.getSubject(),aiAudioDTO.getMessage());
//        if(aiAudioDTO.getMessage().startsWith("第")){
//            String s = aiService.groupPerform(aiAudioDTO.getClassNumber(), aiAudioDTO.getSubject(), aiAudioDTO.getMessage());
//            return Result.success(s);
//        }else {
//            //调用AI接口
//            String s = aiService.aiPerform(aiAudioDTO.getClassNumber(), aiAudioDTO.getSubject(), aiAudioDTO.getMessage());
//            return Result.success(s);
//        }
//
//
//    }
//
//    @PostMapping("/tpi/audio/group")
//    public Result<String> up(@RequestBody AIAudioDTO aiAudioDTO){
//        log.info("接收AI的班级、学科、信息：{}{}{}",aiAudioDTO.getClassNumber(),aiAudioDTO.getSubject(),aiAudioDTO.getMessage());
//        //调用AI接口
//        String groupList = aiService.group(aiAudioDTO.getClassNumber(), aiAudioDTO.getSubject(), aiAudioDTO.getMessage());
//
//        return Result.success(groupList);
//    }
//
////    @PostMapping("/tpi/audio/groupperform")
////    public Result<String> groupPerform(@RequestBody AIAudioDTO aiAudioDTO){
////        log.info("接收AI的班级、学科、信息：{}{}{}",aiAudioDTO.getClassNumber(),aiAudioDTO.getSubject(),aiAudioDTO.getMessage());
////        //调用AI接口
////        aiService.groupPerform(aiAudioDTO.getClassNumber(), aiAudioDTO.getSubject(), aiAudioDTO.getMessage());
////        return Result.success("小组记录成功");
////    }
//
//
//}
