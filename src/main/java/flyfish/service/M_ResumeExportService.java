package flyfish.service;

import flyfish.pojo.M_ExportRequestDTO;
import jakarta.servlet.http.HttpServletResponse;

public interface M_ResumeExportService {
    // 导出单个简历
    void exportSingleResume(M_ExportRequestDTO request, HttpServletResponse response);
}
