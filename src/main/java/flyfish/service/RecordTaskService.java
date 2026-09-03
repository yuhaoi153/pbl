package flyfish.service;

import flyfish.pojo.DTO.PassTaskDTO;
import flyfish.pojo.VO.PassTaskVO;

import java.util.List;

/**
 * 上传过关任务的数据
 */
public interface RecordTaskService {
    String uploadpassTask(PassTaskDTO passTaskDTO) throws Exception;


    String querypasstaskUncompleted(String subject, String classNumber, String content,String school);
}
