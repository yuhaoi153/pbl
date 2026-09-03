package flyfish.service.impl;

import flyfish.mapper.NoticeMapper;
import flyfish.pojo.DTO.NoticeDTO;
import flyfish.pojo.Notice;
import flyfish.service.NoticeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class NoticeServiceImpl implements NoticeService {

    @Autowired
    private NoticeMapper noticeMapper;
    /**
     * 更新通知状态
     * @param noticeDTO
     * @return
     */
    @Override
    public String updateNotice(NoticeDTO noticeDTO) {
        List<Notice> noticeList = new ArrayList<>();
        Notice notice = new Notice();
        notice.setName("语文老师");
        notice.setSubject("语文");
        notice.setMail(noticeDTO.getChineseEmail());
        notice.setPhone(noticeDTO.getChinesePhone());
        notice.setCheckMail(noticeDTO.isCES());
        notice.setCheckPhone(noticeDTO.isCPS());
        noticeList.add(notice);

        Notice noticeMath = new Notice();
        noticeMath.setName("数学老师");
        noticeMath.setSubject("数学");
        noticeMath.setMail(noticeDTO.getMathEmail());
        noticeMath.setPhone(noticeDTO.getMathPhone());
        noticeMath.setCheckMail(noticeDTO.isMES());
        noticeMath.setCheckPhone(noticeDTO.isMPS());
        noticeList.add(noticeMath);

        Notice noticeEnglish = new Notice();
        noticeEnglish.setName("英语老师");
        noticeEnglish.setSubject("英语");
        noticeEnglish.setMail(noticeDTO.getEnglishEmail());
        noticeEnglish.setPhone(noticeDTO.getEnglishPhone());
        noticeEnglish.setCheckMail(noticeDTO.isEES());
        noticeEnglish.setCheckPhone(noticeDTO.isEPS());
        noticeList.add(noticeEnglish);
        String classNumber = noticeDTO.getUsername();

        Integer teacherNumber = noticeMapper.existNotice(classNumber,noticeDTO.getSchool());
        if(teacherNumber == 3){
            for (Notice n:noticeList) {
                System.out.println(n.getMail());
                noticeMapper.updateNotice(n,classNumber, noticeDTO.getSchool());

            }}
        else {
            noticeMapper.deleteNotice(classNumber, noticeDTO.getSchool());
            noticeMapper.addNotice(noticeList,classNumber, noticeDTO.getSchool());

        }
        //还需要有一个List，一次性把通知信息都放进去。
        //把对应班级的对应教师的通知方式都更新进去，姓名就是数学教师、语文教师
        return null;
    }


    /**
     * 查询个人信息
     * @param username
     * @return
     */
    @Override
    public NoticeDTO queryNotice(String username,String school) {
        List<Notice> noticeList = noticeMapper.queryNoticeByClass(username,school);
        NoticeDTO noticeDTO = new NoticeDTO();
        for (Notice notice:
             noticeList) {
            if(notice.getSubject().equals("语文")){
                noticeDTO.setChineseEmail(notice.getMail());
                noticeDTO.setChinesePhone(notice.getPhone());
                noticeDTO.setCES(notice.isCheckMail());
                noticeDTO.setCPS(notice.isCheckPhone());
            } else if (notice.getSubject().equals("数学")) {
                noticeDTO.setMathEmail(notice.getMail());
                noticeDTO.setMathPhone(notice.getPhone());
                noticeDTO.setMES(notice.isCheckMail());
                noticeDTO.setMPS(notice.isCheckPhone());
            }else {
                noticeDTO.setEnglishEmail(notice.getMail());
                noticeDTO.setEnglishPhone(notice.getPhone());
                noticeDTO.setEES(notice.isCheckMail());
                noticeDTO.setEPS(notice.isCheckPhone());
            }
        }

        return noticeDTO;
    }
}
