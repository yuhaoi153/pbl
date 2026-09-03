package flyfish.service;

import jakarta.servlet.ServletOutputStream;

public interface M_ResumeZipExportService {
    void createFinalZip(ServletOutputStream outputStream, byte[] wordBytes, byte[] imageZipBytes, String userName, String fileName);
}
