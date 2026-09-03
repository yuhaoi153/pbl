package flyfish.service.impl;

import flyfish.mapper.M_GradeYearMapper;
import flyfish.mapper.M_WellBadHomeworkPerformMapper;
import flyfish.mapper.RecordMapper;
import flyfish.pojo.M_WellBadHomeworkPerform;
import flyfish.service.M_WellBadPerformService;
import flyfish.utils.AliOSSUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class M_WellBadPerformServiceImpl implements M_WellBadPerformService {

    @Autowired
    private M_WellBadHomeworkPerformMapper wellBadHomeworkPerformMapper;
    @Autowired
    private AliOSSUtils aliOSSUtils;
    @Autowired
    private RecordMapper recordMapper;
    @Autowired
    private M_GradeYearMapper gradeYearMapper;

    /**
     * 获取展示优秀作业图片的接口
     * @param school
     * @param className
     * @param content
     * @param subject
     * @param studentName
     * @param startDate
     * @param endDate
     * @return
     */
    @Override
    public List<M_WellBadHomeworkPerform> getShowImage(String school, String className, String content, String subject, String studentName, LocalDate startDate, LocalDate endDate) throws IOException {

        List<M_WellBadHomeworkPerform> wellBadHomeworkPerformList = wellBadHomeworkPerformMapper.getAllRecord(school,className,content,studentName,subject,startDate,endDate);

        return wellBadHomeworkPerformList;
    }

    /**
     * 上传 指定日期的展示图片
     * @param file
     * @param school
     * @param className
     * @param content
     * @param subject
     * @param studentName
     * @param checkDate
     * @return
     */
    @Override
    public String uploadHomeWorkImage(MultipartFile file, String grade,String situation, String school, String className, String content, String subject, String studentName, LocalDate checkDate) throws IOException {
        String showUrl = aliOSSUtils.uploadByFilePath(file,"homework/homeworkPerform/");
        //拿到指定学生的作业recordId
        Integer homeworkRecordId =  recordMapper.getRecordId(school,className,subject,studentName,checkDate,content);
        Integer year = gradeYearMapper.getYearByGrade(grade);
        //将图片链接和相关信息存入数据库
        M_WellBadHomeworkPerform mWellBadHomeworkPerform = new M_WellBadHomeworkPerform();
        mWellBadHomeworkPerform.setStudentName(studentName);
        if(mWellBadHomeworkPerform.getHomeworkRecordId()!=null){
            mWellBadHomeworkPerform.setHomeworkRecordId(homeworkRecordId);
        }
        mWellBadHomeworkPerform.setSituation(situation);
        mWellBadHomeworkPerform.setSchool(school);
        mWellBadHomeworkPerform.setClassName(className);
        mWellBadHomeworkPerform.setSupplementary(content);
        mWellBadHomeworkPerform.setSubject(subject);
        mWellBadHomeworkPerform.setCheckDate(checkDate);
        mWellBadHomeworkPerform.setShowUrl(showUrl);
        mWellBadHomeworkPerform.setYear(year);
        wellBadHomeworkPerformMapper.insertSingleRecord(mWellBadHomeworkPerform);


        return "上传成功";
    }

    @Override
    public String deleteShowImage(Integer id) {
        wellBadHomeworkPerformMapper.deleteById(id);
        return "删除成功";
    }

    @Override
    public List<M_WellBadHomeworkPerform> getPunishItemRecord(String school, String className, String subject, LocalDate startDate, LocalDate endDate,String showItem) {
        List<M_WellBadHomeworkPerform> wellBadHomeworkPerformList = wellBadHomeworkPerformMapper.getPunishRecord(school,className,subject,startDate,endDate,showItem);

        List<M_WellBadHomeworkPerform> resultList = new ArrayList<>();
        for (M_WellBadHomeworkPerform wellBadHomeworkPerform : wellBadHomeworkPerformList) {
            if(wellBadHomeworkPerform.getSituation().equals("优秀作业")||wellBadHomeworkPerform.getSituation().equals("例题讲解")||wellBadHomeworkPerform.getSituation().equals("不达标作业")){

            }else {
                resultList.add(wellBadHomeworkPerform);}
        }
        return resultList;
    }

    /**
     * 上传惩罚举措记录
     * @param mWellBadHomeworkPerform
     * @return
     */
    @Override
    public String uploadpunishItemRecord(M_WellBadHomeworkPerform mWellBadHomeworkPerform) {
        mWellBadHomeworkPerform.setSituation("课堂表现不佳");
        wellBadHomeworkPerformMapper.insertSingleRecord(mWellBadHomeworkPerform);
        return "上传惩罚举措记录成功";
    }







    /**
     * 核销记录
     * @param id
     * @return
     */
    @Override
    public String cancelPunishItemRecord(Integer id) {
        wellBadHomeworkPerformMapper.cancelById(id);
        return "核销记录成功";
    }




}
