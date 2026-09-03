package flyfish.service;

import flyfish.pojo.M_Certification;

import java.util.List;

public interface M_ResumeImageExportService {
    byte[] generateImageZip(List<M_Certification> certifications);
}
