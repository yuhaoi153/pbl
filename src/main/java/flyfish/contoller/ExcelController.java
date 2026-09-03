//package flyfish.contoller;
//
//import flyfish.pojo.Result;
//import flyfish.service.ExcelService;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.core.io.InputStreamResource;
//import org.springframework.http.HttpHeaders;
//import org.springframework.http.MediaType;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RequestParam;
//import org.springframework.web.bind.annotation.RestController;
//import org.springframework.web.multipart.MultipartFile;
//
//import java.io.File;
//import java.io.FileInputStream;
//import java.io.IOException;
//import java.io.InputStream;
//import java.net.URL;
//import java.nio.file.Path;
//import java.nio.file.Paths;
//
//
//
//@RestController
//@Slf4j
//public class ExcelController {
//    @Autowired
//    private ExcelService excelService;
//
//
//    /**
//     * 下载模板问卷
//     * @return
//     */
//    @GetMapping("/tpi/download/excel")
//
//        public ResponseEntity<InputStreamResource> downloadExcel() {
//            String fileUrl = "https://webtry.oss-cn-shenzhen.aliyuncs.com/homework/template.xlsx";
//
//            try {
//                URL url = new URL(fileUrl);
//                InputStream inputStream = url.openStream();
//                InputStreamResource resource = new InputStreamResource(inputStream);
//
//                return ResponseEntity.ok()
//                        .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
//                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"template.xlsx\"")
//                        .body(resource);
//            } catch (Exception e) {
//                e.printStackTrace();
//                return ResponseEntity.notFound().build();
//            }
//        }
//
//
//    /**
//     * 检查该班级是否已经有学生信息存在
//     * @param classNumber
//     * @return
//     */
//    @GetMapping("/tpi/check/excel")
//    public Result<String> checkExcel(String classNumber){
//        log.info("检查是否已经有用户信息的班级是：{}",classNumber);
//        String result = excelService.checkExcel(classNumber);
//        return Result.success(result);
//    }
//
//    @PostMapping("/tpi/downloadQR")
//    public ResponseEntity<byte[]> downloadQR(String username) throws Exception {
//        log.info("下载二维码的班级为：{}",username);
//        ResponseEntity<byte[]> result = excelService.downloadQR(username);
//        return result;
//    }
//
//
//    @PostMapping ("/tpi/upload/excel")
//    public ResponseEntity<byte[]> handleFileUpload(@RequestParam("file") MultipartFile file,String username) throws Exception {
//        log.info("接收excel文件：{},对应的班级：{}",file.getOriginalFilename(),username);
//        ResponseEntity<byte[]> result = excelService.geneCode(file,username);
//        return result;
//
//    }
//}