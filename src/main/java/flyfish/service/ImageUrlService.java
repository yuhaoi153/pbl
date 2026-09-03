package flyfish.service;

import flyfish.pojo.ImageUrl;

import java.time.LocalDate;
import java.util.List;

public interface ImageUrlService {
    /**
     * 新增数据
     * @param
     */
    void add(String url, LocalDate checkDate);

    List<String> getByDate(LocalDate checkdate);
}
