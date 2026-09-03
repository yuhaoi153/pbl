package flyfish.service;

import flyfish.pojo.DTO.NoticeDTO;

public interface NoticeService {
    /**
     * 更新通知方式
     * @param noticeDTO
     * @return
     */
    String updateNotice(NoticeDTO noticeDTO);

    /**
     * 查询个人通知信息
     * @param username
     * @return
     */
    NoticeDTO queryNotice(String username,String school);
}
