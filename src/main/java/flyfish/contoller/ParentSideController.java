//package flyfish.contoller;
//
//import flyfish.mapper.RecordMapper;
//import flyfish.pojo.DTO.PageQueryNameDTO;
//import flyfish.pojo.DTO.ParentQueryDTO;
//import flyfish.pojo.Options;
//import flyfish.pojo.Result;
//import flyfish.pojo.VO.PageQueryNameVO;
//import flyfish.pojo.VO.ParentPassTaskVO;
//import flyfish.pojo.VO.ParentPerformVO;
//import flyfish.pojo.VO.ParentRecordVO;
//import flyfish.service.ParentSideService;
//import flyfish.service.RecordService;
//import flyfish.service.RecordTaskService;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.BeanUtils;
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
//public class ParentSideController {
//    @Autowired
//    private ParentSideService parentSideService;
//    @Autowired
//    private RecordService recordService;
//    @Autowired
//    private RecordTaskService recordTaskService;
//
//
//    @GetMapping("/tpi/parent/queryclassnumber")
//    public Result<List<Options>> queryClassNumber() {
//        log.info("家长端查询班级号码");
//        List<Options> classNumberList = parentSideService.queryClassNumber();
//        return Result.success(classNumberList);
//
//    }
//
//    /**
//     * 家长端检查密码
//     * @param parentQueryDTO
//     * @return
//     */
//    @PostMapping("/tpi/parent/checkpassword")
//    public Result<String> checkPassword(@RequestBody ParentQueryDTO parentQueryDTO) {
//        log.info("家长端检查密码parentQueryDTO:{}", parentQueryDTO);
//        String result = parentSideService.checkPassword(parentQueryDTO);
//        return Result.success(result);
//    }
//
//    @PostMapping("/tpi/parent/querynamedata")
//    public Result<List<ParentRecordVO>> queryNameData(@RequestBody ParentQueryDTO parentQueryDTO) {
//        log.info("家长端查询作业数据parentQueryDTO:{}", parentQueryDTO);
//        PageQueryNameDTO pageQueryNameDTO = new PageQueryNameDTO();
//        BeanUtils.copyProperties( parentQueryDTO,pageQueryNameDTO);
//        List<PageQueryNameVO> pageQueryNameVOS = recordService.pageName(pageQueryNameDTO);
//
//        return Result.success(pageQueryNameVOS);
//    }
//
//    @PostMapping("/tpi/parent/queryPassData")
//    public Result<List<ParentPassTaskVO> > queryPassData(@RequestBody ParentQueryDTO parentQueryDTO) {
//        log.info("家长端查询未过关数据parentQueryDTO:{}", parentQueryDTO);
//        List<ParentPassTaskVO> parentPassTaskVOS = parentSideService.queryPassData(parentQueryDTO);
//        return Result.success(parentPassTaskVOS);
//    }
//
//    @PostMapping("/tpi/parent/parentConfirm")
//    public void parentConfirm(@RequestBody List<Integer> ids) {
//        log.info("家长端确认完成ids:{}", ids);
//        parentSideService.parentConfirm(ids);
//    }
//
//    @PostMapping("/tpi/parent/queryPerform")
//    public Result<List<ParentPerformVO>> queryPerform(@RequestBody ParentQueryDTO parentQueryDTO) {
//        log.info("家长端查询表现数据parentQueryDTO:{}", parentQueryDTO);
//        List<ParentPerformVO> parentPerformVOS = parentSideService.queryPerform(parentQueryDTO);
//        return Result.success(parentPerformVOS);
//    }
//
//
//
//
//}
