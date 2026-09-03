package flyfish.pojo;


import lombok.Data;

import java.util.List;

@Data
public class OcrResponse {
    private boolean success;
    private String message;
    private String extractedText;
    private String imageUrls;
    private List<String> imageUrlList;
    private List<String> pageTexts; // 对于多页PDF

    public static OcrResponse success(String text) {
        OcrResponse response = new OcrResponse();
        response.setSuccess(true);
        response.setMessage("OCR识别成功");
        response.setExtractedText(text);

        return response;
    }

    public static OcrResponse error(String message) {
        OcrResponse response = new OcrResponse();
        response.setSuccess(false);
        response.setMessage(message);
        return response;
    }
}