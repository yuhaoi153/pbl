package flyfish.service.impl;

import flyfish.service.M_ResumeZipExportService;
import jakarta.servlet.ServletOutputStream;
import org.springframework.stereotype.Service;

import java.io.OutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
public class M_ResumeZipExportServiceImpl implements M_ResumeZipExportService{
    @Override
    public void createFinalZip(ServletOutputStream outputStream, byte[] wordBytes, byte[] imageZipBytes, String userName, String fileName) {
        try (ZipOutputStream zos = new ZipOutputStream(outputStream)) {
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));

            // 添加Word文档
            ZipEntry wordEntry = new ZipEntry(userName + "_word简历_" + timestamp + ".docx");
            zos.putNextEntry(wordEntry);
            zos.write(wordBytes);
            zos.closeEntry();

            // 添加图片压缩包
            ZipEntry imageEntry = new ZipEntry(userName + "_证书图片_" + timestamp + ".zip");
            zos.putNextEntry(imageEntry);
            zos.write(imageZipBytes);
            zos.closeEntry();

            zos.finish();

            // 注意：不需要在这里关闭 outputStream，因为它由调用者管理



        } catch (Exception e) {
            throw new RuntimeException("创建最终压缩包失败: " + e.getMessage());
        }


    }

}