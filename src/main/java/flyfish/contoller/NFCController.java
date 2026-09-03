//package flyfish.contoller;
//
//import flyfish.exception.NullNameListException;
//import flyfish.pojo.Result;
//import flyfish.service.NFCService;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.RestController;
//
//@RestController
//@Slf4j
//public class NFCController {
//    @Autowired
//    private NFCService nfcService;
//
//
//    @GetMapping("/tpi/nfc/sendWellNFC")
//    public Result<String> sendWellNFC(String classNumber, String subject , String name , String situation){
//        log.info("接收NFC的班级号为：{}",classNumber);
//        nfcService.sendWellNFC(classNumber,subject,name,situation);
//
//        return Result.success("发送成功");
//
//
//    }
//}
