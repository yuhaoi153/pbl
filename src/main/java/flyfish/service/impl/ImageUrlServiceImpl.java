package flyfish.service.impl;

import flyfish.mapper.ImageUrlMapper;
import flyfish.pojo.ImageUrl;
import flyfish.service.ImageUrlService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
public class ImageUrlServiceImpl implements ImageUrlService {
    @Autowired
    private ImageUrlMapper imageUrlMapper;
    /**
     * 新增数据
     * @param
     */
    @Override
    public void add(String url, LocalDate checkDate) {
        log.info("新增照片的链接为：{}",url);
        ImageUrl imageUrl = new ImageUrl();
        imageUrl.setUrl(url);
        imageUrl.setCheckDate(checkDate);
        imageUrl.setCreateTime(LocalDateTime.now());
        imageUrlMapper.add(imageUrl);
    }

    @Override
    public List<String> getByDate(LocalDate checkdate) {
        List<String> images = imageUrlMapper.getByDate(checkdate);
        return images;
    }
}
