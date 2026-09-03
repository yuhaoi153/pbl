package flyfish.service;

import flyfish.pojo.DTO.DeleteContetnDTO;
import flyfish.pojo.DTO.HomeWorkContentDTO;

import java.util.List;

public interface PassTaskService {




    /**
     * 新增过关任务类型
     * @param homeWorkContentDTO
     * @return
     */
    String addContent(HomeWorkContentDTO homeWorkContentDTO);

    /**
     * 查询打卡任务内容
     * @param homeWorkContentDTO
     * @return
     */
    List<String> queryContent(HomeWorkContentDTO homeWorkContentDTO);

    /**
     * 获取images的列表
     * @param classNumber
     * @param content
     * @param subject
     * @return
     */
    List<String> getImages(String classNumber, String content, String subject);

    /**
     * 删除任务类型
     * @param deleteContetnDTO
     * @return
     */
    String deleteContent(DeleteContetnDTO deleteContetnDTO);
}
