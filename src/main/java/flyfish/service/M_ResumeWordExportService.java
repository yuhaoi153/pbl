package flyfish.service;

import java.util.Map;

public interface M_ResumeWordExportService {
    // 生成Word文档的字节数组
    byte[] generateWordDocument(Map<String, Object> classifiedData, String userName);
}
