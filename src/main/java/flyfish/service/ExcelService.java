package flyfish.service;

import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface ExcelService {

    /**
     * 生成学生二维码
     * @param file
     * @return
     */
    ResponseEntity<byte[]> geneCode(MultipartFile file,String username, String school) throws Exception;

    /**
     * 检查该班级是否已经有学生信息存在
     * @param classNumber
     * @return
     */
    String checkExcel(String classNumber,String school);

    /**
     * 直接下载学号二维码
     * @param username
     * @return
     */
    ResponseEntity<byte[]> downloadQR(String username,String school) throws Exception;
}
