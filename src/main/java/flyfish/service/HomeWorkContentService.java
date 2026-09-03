package flyfish.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import flyfish.pojo.DTO.DeleteContetnDTO;
import flyfish.pojo.DTO.HomeWorkContentDTO;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

public interface HomeWorkContentService {
    /**
     * 新增作业类型
     * @param homeWorkContentDTO
     * @return
     */
    String addcontent(HomeWorkContentDTO homeWorkContentDTO);

    /**
     *查询所有的作业类型
     * @param homeWorkContentDTO
     * @return
     */
    List<String> queryContent(HomeWorkContentDTO homeWorkContentDTO);

    /**
     * 批量删除作业类型
     * @param deleteContetnDTO
     * @return
     */
    String deleteContent(DeleteContetnDTO deleteContetnDTO);


}
