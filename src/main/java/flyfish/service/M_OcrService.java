package flyfish.service;

import flyfish.pojo.DTO.M_CertificationDTO;
import flyfish.pojo.DTO.M_ImageUrlDTO;
import flyfish.pojo.M_Certification;
import flyfish.pojo.OcrResponse;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface M_OcrService {
    OcrResponse processFile(MultipartFile file) throws Exception;

    String recognize(byte[] imageBytes) throws Exception;

    List<byte[]> convertPdfToImages(byte[] pdfBytes, int maxPages) throws Exception;

    //结构化处理ocr文本
    M_Certification structurizeOcrText(String content, String type, String imageUrl, String userName,String school);

    //编辑已存在的证书信息或则新增证书信息
    void editCertification(M_Certification mCertification);

    //根据用户名获取证书列表
    List<M_Certification> getCertificationsByUserName(M_CertificationDTO mCertificationDTO);


    OcrResponse batchProcessFile(MultipartFile file) throws Exception;

    //批量结构化处理ocr文本
    void batchStructurizeOcrText(String pageText, String type, String pageImageUrl, String userName, String school);

    void teacherBatchStructurizeOcrText(String pageText, String type, String pageImageUrl, String userName, String school);

    void admineditCertification(M_Certification mCertification);
}
