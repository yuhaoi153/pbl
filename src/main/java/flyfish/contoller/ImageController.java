package flyfish.contoller;

import flyfish.pojo.ImageUrl;
import flyfish.pojo.Result;
import flyfish.service.ImageUrlService;
import flyfish.utils.AliOSSUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;

@RestController
@Slf4j
public class ImageController {

    @Autowired
    private AliOSSUtils aliOSSUtils;
    @Autowired
    private ImageUrlService imageUrlService;

    @PostMapping("/tpi/uploadimg")
    public Result<String> uploadimage( @RequestParam("image") MultipartFile image,
                               @RequestParam("checkDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkDate) throws IOException {
        log.info("上传的照片信息为：{}",image,checkDate);
        String url = aliOSSUtils.upload(image);


        log.info("url为{} ",url);
        imageUrlService.add(url,checkDate);
        return Result.success(url);
    }
}
