//package flyfish.contoller;
//
//import flyfish.exception.ContenttNullException;
//import flyfish.pojo.DTO.*;
//import flyfish.pojo.PageResult;
//import flyfish.pojo.Result;
//import flyfish.pojo.VO.AutoQueryContentVO;
//import flyfish.pojo.VO.AutoQueryNameVO;
//import flyfish.pojo.VO.PageQueryClassVO;
//import flyfish.pojo.VO.PageQueryNameVO;
//import flyfish.service.HomeWorkContentService;
//import flyfish.service.RecordService;
//import flyfish.service.StudentInfoService;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RequestBody;
//import org.springframework.web.bind.annotation.RestController;
//
//import java.time.LocalDate;
//import java.util.ArrayList;
//import java.util.Date;
//import java.util.HashSet;
//import java.util.List;
//
//@RestController
//@Slf4j
//public class HomeWorkController {
//
//    @Autowired
//    private HomeWorkContentService homeWorkContentService;
//
//    @Autowired
//    private RecordService recordService;
//
//    @Autowired
//    private StudentInfoService studentInfoService;
//
////    /**
////     * 新增作业类型接口
////     * @param homeWorkContentDTO
////     * @return
////     */
////    @PostMapping("/tpi/addContent")
////    public Result<String> addContent(@RequestBody HomeWorkContentDTO homeWorkContentDTO){
////        log.info("接收到新增作业类型为:{}",homeWorkContentDTO);
////        String result = homeWorkContentService.addcontent(homeWorkContentDTO);
////        return Result.success(result);
////    }
////
////    /**
////     * 删除作业类型的接口
////     * @param deleteContetnDTO
////     * @return
////     * @throws Exception
////     */
////    @PostMapping("/tpi/deleteContent")
////    public Result<String> deleteContent(@RequestBody DeleteContetnDTO deleteContetnDTO) throws Exception {
////        log.info("要删除的作业列表为：{}",deleteContetnDTO);
////        if (deleteContetnDTO.getContentList() ==null || deleteContetnDTO.getContentList().size() == 0){
////            throw new ContenttNullException("没有作业内容");
////        }
////        String result = homeWorkContentService.deleteContent(deleteContetnDTO);
////        return Result.success(result);
////
////    }
//
////    /**
////     * 查询作业类型接口
////     * @param homeWorkContentDTO
////     * @return
////     */
////    @PostMapping("/tpi/queryContent")
////    public Result<List<String>> queryContent(@RequestBody HomeWorkContentDTO homeWorkContentDTO){
////        log.info("要查询的作业类型条件为：{}",homeWorkContentDTO);
////        List<String> result = homeWorkContentService.queryContent(homeWorkContentDTO);
////        return Result.success(result);
////    }
//
//    /**
//     * 接收数据，并返回没写完作业同学数据
//     * @param recordDTO
//     * @return
//     */
//    @PostMapping("/tpi/uploadHomeWork")
//    public Result<String> uploadHomeWork(@RequestBody RecordDTO recordDTO) throws Exception {
//        log.info("扫码枪上传数据为：{}",recordDTO);
//        String result = recordService.uploadFeedback(recordDTO);
//        return Result.success(result);
//    }
//
//
//    /**
//     * 接收数据，并返回优秀和未达标作业名单
//     * @param recordDTO
//     * @return
//     */
//    @PostMapping("/tpi/uploadSpecialHomeWork")
//    public Result<String> uploadSpecialHomeWork(@RequestBody RecordDTO recordDTO) throws Exception {
//        log.info("扫码枪上传数据为：{}",recordDTO);
//        String result = recordService.uploadFeedbackSpecial(recordDTO);
//        return Result.success(result);
//    }
//
//
//    /**
//     * 接收数据，并返回优秀和未达标作业名单——————特殊操作
//     * @param recordDTO
//     * @return
//     */
//    @PostMapping("/tpi/uploadHomeworkSpecial")
//    public Result<String> SpecialHomeWork(@RequestBody RecordDTO recordDTO) throws Exception {
//        log.info("扫码枪上传数据为：{}",recordDTO);
//        String result = recordService.SpecialHomework(recordDTO);
//        return Result.success(result);
//    }
//
//    /**
//     * 通知老师的接口
//     * @param notificationDTO
//     * @return
//     */
//    @PostMapping("/tpi/notification")
//    public Result<List<String>> notification(@RequestBody NotificationDTO notificationDTO) throws Exception {
//        log.info("通知老师的参数为：{}",notificationDTO);
//        List<String> result = recordService.notification(notificationDTO);
//        return Result.success(result);
//    }
//
//    @GetMapping("/tpi/quickFindUncompleted")
//    public Result<String> quickUncompleted(String classNumber, LocalDate checkdate){
//        log.info("要查询的未完成作业名单的班级和日期为{},{}",classNumber,checkdate);
//        String result = recordService.quickUncompleted(classNumber,checkdate);
//        return Result.success(result);
//    }
//
//
//
//    /**
//     * 查询班级数据，展示三种名单
//     * @param pageQueryClassDTO
//     * @return
//     */
//    @GetMapping("/tpi/queryclassdata")
//    public Result<PageResult> pageClass(PageQueryClassDTO pageQueryClassDTO){
//        log.info("查询班级名单：{}",pageQueryClassDTO);
//        List<PageQueryClassVO> pageQueryClassVOS = recordService.pageClass(pageQueryClassDTO);
//        Integer total = pageQueryClassVOS.size();
//        List record = pageQueryClassVOS;
//        PageResult pageResult = new PageResult(total,record);
//        return Result.success(pageResult);
//
//    }
//
//    /**
//     * 查询学生个人数据
//     * @param pageQueryNameDTO
//     * @return
//     */
//    @GetMapping("/tpi/querynamedata")
//    public Result<PageResult> pageName(PageQueryNameDTO pageQueryNameDTO){
//        log.info("查询班级名单：{}",pageQueryNameDTO);
//        List<PageQueryNameVO> pageQueryNameVOS = recordService.pageName(pageQueryNameDTO);
//        Integer total = pageQueryNameVOS.size();
//        List record = pageQueryNameVOS;
//        PageResult pageResult = new PageResult(total,record);
//        return Result.success(pageResult);
//    }
//
//    /**
//     * 自动查询加载作业内容列表
//     * @param classNumber
//     * @return
//     */
//    @GetMapping("/tpi/autoquerycontent")
//    public Result<List<AutoQueryContentVO>> loadcontentAll(String classNumber){
//        log.info("要自动加载作业类型的班级是：{}",classNumber);
//        HomeWorkContentDTO queryClassnumberDto = new HomeWorkContentDTO();
//        queryClassnumberDto.setClassNumber(classNumber);
//        List<String> contentlist = homeWorkContentService.queryContent(queryClassnumberDto);
//        HashSet<String> contentset = new HashSet<>(contentlist) ;
//        contentlist = new ArrayList<>(contentset);
//        List<AutoQueryContentVO> autoQueryContentVOS = new ArrayList<>();
//
//        for(String content:contentlist){
//            AutoQueryContentVO autoQueryContentVO = new AutoQueryContentVO();
//            autoQueryContentVO.setValue(content);
//            autoQueryContentVO.setContent(content);
//            autoQueryContentVOS.add(autoQueryContentVO);
//        }
//
//        return Result.success(autoQueryContentVOS);
//    }
//
//    /**
//     * 自动加载学生姓名列表
//     * @param classNumber
//     * @return
//     */
//    @GetMapping("/tpi/autoqueryname")
//    public Result<List<AutoQueryContentVO>> loadnameAll(String classNumber){
//        log.info("要自动加载姓名的班级是：{}",classNumber);
//        List<String> namelist = studentInfoService.getNameByClass(classNumber);
//        ArrayList<AutoQueryNameVO> autoQueryNameVOS = new ArrayList<>();
//
//        for(String name: namelist){
//            AutoQueryNameVO autoQueryNameVO = new AutoQueryNameVO();
//            autoQueryNameVO.setValue(name);
//            autoQueryNameVO.setValue(name);
//            autoQueryNameVOS.add(autoQueryNameVO);
//        }
//
//        return Result.success(autoQueryNameVOS);
//    }
//
//
//
//}
