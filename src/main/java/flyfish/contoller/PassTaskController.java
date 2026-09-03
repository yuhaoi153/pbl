//package flyfish.contoller;
//
//import flyfish.exception.ContenttNullException;
//import flyfish.pojo.DTO.DeleteContetnDTO;
//import flyfish.pojo.DTO.HomeWorkContentDTO;
//import flyfish.pojo.DTO.PassTaskDTO;
//import flyfish.pojo.Result;
//import flyfish.pojo.VO.PassTaskVO;
//import flyfish.service.PassTaskService;
//import flyfish.service.RecordTaskService;
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
//public class PassTaskController {
//
//    @Autowired
//    private PassTaskService passTaskService;
//    @Autowired
//    private RecordTaskService recordTaskService;
//
//    /**
//     * 新增过关任务类型
//     * @param homeWorkContentDTO
//     * @return
//     */
//    @PostMapping("/tpi/addpassContent")
//    public Result<String> addPassContent(@RequestBody HomeWorkContentDTO homeWorkContentDTO){
//        log.info("要新增的打卡任务为：{}",homeWorkContentDTO);
//        String result = passTaskService.addContent(homeWorkContentDTO);
//        return Result.success(result);
//    }
//
//    /**
//     * 查询过关任务内容
//     * @param homeWorkContentDTO
//     * @return
//     */
//    @GetMapping("/tpi/queryPassContent")
//    public Result<List<String>> queryPassContent(HomeWorkContentDTO homeWorkContentDTO){
//        log.info("要查询的打卡任务为：{}",homeWorkContentDTO);
//        List<String> result = passTaskService.queryContent(homeWorkContentDTO);
//        return Result.success(result);
//    }
//
//    /**
//     * 获取图像的url列表
//     * @param classNumber
//     * @param content
//     * @param subject
//     * @return
//     */
//    @GetMapping("/tpi/getimages")
//    public Result<List<String>> getimages(String classNumber, String content, String subject){
//        log.info("查询图片url列表的参数：{},{},{}",classNumber,content,subject);
//        List<String> result = passTaskService.getImages(classNumber,content,subject);
//        System.out.println(result);
//        return Result.success(result);
//    }
//
//    /**
//     * 上传过关学生数据信息
//     * @param passTaskDTO
//     * @return
//     */
//    @PostMapping("/tpi/uploadpassTask")
//    public Result<String> uploadpassTask(@RequestBody PassTaskDTO passTaskDTO ) throws Exception {
//        log.info("要上传学生过关任务数据为：{}",passTaskDTO);
//        String result = recordTaskService.uploadpassTask(passTaskDTO);
//        return Result.success(result);
//    }
//
//    /**
//     * 查询未完成作业名单
//     * @param subject
//     * @param classNumber
//     * @param content
//     * @return
//     */
//    @GetMapping("/tpi/querypasstaskUncompleted")
//    public Result<String> passtaskfeedback(String subject, String classNumber, String content){
//        log.info("要查询未完成作业名单的条件为：{},{},{}",subject,classNumber,content);
//        String result = recordTaskService.querypasstaskUncompleted(subject,classNumber,content);
//        return Result.success(result);
//    }
//
//
//    /**
//     * 删除作业类型的接口
//     * @param deleteContetnDTO
//     * @return
//     * @throws Exception
//     */
//    @PostMapping("/tpi/deletepassContent")
//    public Result<String> deletepassContent(@RequestBody DeleteContetnDTO deleteContetnDTO) throws Exception {
//        log.info("要删除的任务列表为：{}",deleteContetnDTO);
//        if (deleteContetnDTO.getContentList() ==null || deleteContetnDTO.getContentList().size() == 0){
//            throw new ContenttNullException("没有任务内容内容");
//        }
//        String result = passTaskService.deleteContent(deleteContetnDTO);
//        return Result.success(result);
//
//    }
//
//}
