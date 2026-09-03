package flyfish.service.impl;

import flyfish.mapper.M_SportRecordMapper;
import flyfish.pojo.DTO.M_ReadGradeFeedDTO;
import flyfish.pojo.DTO.M_ReadingThreeSituationDTO;
import flyfish.pojo.DTO.M_SportFourSituationDTO;
import flyfish.pojo.M_FeedBack;
import flyfish.pojo.M_ReadingFeedbackReport;
import flyfish.pojo.M_SingleReadTeacher;
import flyfish.service.M_SportService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@Slf4j
public class M_SportServiceImpl implements M_SportService {
    @Autowired
    private M_SportRecordMapper m_sportRecordMapper;

    @Override
    public String recordSport(M_ReadGradeFeedDTO mReadGradeFeedDTO) {
        String resp = "";
        //预处理传过来的参数，判断每个年级是不是有值，有值的批量添加到数据库
        List<M_FeedBack> feedBackList = new ArrayList<>();
        if(mReadGradeFeedDTO.getGrade1ClassList() !=null && mReadGradeFeedDTO.getGrade1ClassList().size() > 0 && mReadGradeFeedDTO.getGrade().equals("一年级")){
            //先查询数据库有没有这个年级的数据，没有的话新增，有的话还要看有没有这个班级的数据，没有的话新增，有的话更新
            List<String> grade1ClassList = mReadGradeFeedDTO.getGrade1ClassList();
            resp = gradeResp("一年级",resp,mReadGradeFeedDTO,grade1ClassList,feedBackList);
        }

        if(mReadGradeFeedDTO.getGrade2ClassList() !=null && mReadGradeFeedDTO.getGrade2ClassList().size() > 0 && mReadGradeFeedDTO.getGrade().equals("二年级")){
            List<String> grade2ClassList = mReadGradeFeedDTO.getGrade2ClassList();
            resp = gradeResp("二年级",resp,mReadGradeFeedDTO,grade2ClassList,feedBackList);
        }

        if(mReadGradeFeedDTO.getGrade3ClassList() !=null && mReadGradeFeedDTO.getGrade3ClassList().size() > 0 && mReadGradeFeedDTO.getGrade().equals("三年级")){
            List<String> grade3ClassList = mReadGradeFeedDTO.getGrade3ClassList();
            resp = gradeResp("三年级",resp,mReadGradeFeedDTO,grade3ClassList,feedBackList);
        }

        if(mReadGradeFeedDTO.getGrade4ClassList() !=null && mReadGradeFeedDTO.getGrade4ClassList().size() > 0 && mReadGradeFeedDTO.getGrade().equals("四年级")){
            List<String> grade4ClassList = mReadGradeFeedDTO.getGrade4ClassList();
            resp = gradeResp("四年级",resp,mReadGradeFeedDTO,grade4ClassList,feedBackList);
        }

        if(mReadGradeFeedDTO.getGrade5ClassList() !=null && mReadGradeFeedDTO.getGrade5ClassList().size() > 0 && mReadGradeFeedDTO.getGrade().equals("五年级")){
            List<String> grade5ClassList = mReadGradeFeedDTO.getGrade5ClassList();
            resp = gradeResp("五年级",resp,mReadGradeFeedDTO,grade5ClassList,feedBackList);
        }

        if(mReadGradeFeedDTO.getGrade6ClassList() !=null && mReadGradeFeedDTO.getGrade6ClassList().size() > 0 && mReadGradeFeedDTO.getGrade().equals("六年级")){
            List<String> grade6ClassList = mReadGradeFeedDTO.getGrade6ClassList();
            resp = gradeResp("六年级",resp,mReadGradeFeedDTO,grade6ClassList,feedBackList);
        }

        if(mReadGradeFeedDTO.getGrade7ClassList() !=null && mReadGradeFeedDTO.getGrade7ClassList().size() > 0 && mReadGradeFeedDTO.getGrade().equals("七年级")){
            List<String> grade7ClassList = mReadGradeFeedDTO.getGrade7ClassList();
            resp = gradeResp("七年级",resp,mReadGradeFeedDTO,grade7ClassList,feedBackList);
        }

        if(mReadGradeFeedDTO.getGrade8ClassList() !=null && mReadGradeFeedDTO.getGrade8ClassList().size() > 0 && mReadGradeFeedDTO.getGrade().equals("八年级")){
            List<String> grade8ClassList = mReadGradeFeedDTO.getGrade8ClassList();
            resp = gradeResp("八年级",resp,mReadGradeFeedDTO,grade8ClassList,feedBackList);
        }

        if(mReadGradeFeedDTO.getGrade9ClassList() !=null && mReadGradeFeedDTO.getGrade9ClassList().size() > 0 && mReadGradeFeedDTO.getGrade().equals("九年级")){
            List<String> grade9ClassList = mReadGradeFeedDTO.getGrade9ClassList();
            resp = gradeResp("九年级",resp,mReadGradeFeedDTO,grade9ClassList,feedBackList);
        }


        if(resp.equals("")){
            return "nullData";
        }
        return resp;
    }

    @Override
    public M_SportFourSituationDTO getSingleSportFeedback(String school, LocalDate checkDate) {
        M_SportFourSituationDTO mSportFourSituationDTO = new M_SportFourSituationDTO();
        List<String> gymPraiseClassNameList = new ArrayList<>();
        List<String> gymCriticizeClassNameList = new ArrayList<>();
        List<String> runPraiseClassNameList = new ArrayList<>();
        List<String> runCriticizeClassNameList = new ArrayList<>();
        List<M_FeedBack> feedBackList =  m_sportRecordMapper.getSingleSportFeedback(school,checkDate);
        for (M_FeedBack mFeedBack : feedBackList) {
            if(mFeedBack.getTimeZone().equals("体操表扬")){
                gymPraiseClassNameList.add(mFeedBack.getClassName());
            }else if(mFeedBack.getTimeZone().equals("体操批评")){
                gymCriticizeClassNameList.add(mFeedBack.getClassName());
            }else if(mFeedBack.getTimeZone().equals("跑操表扬")){
                runPraiseClassNameList.add(mFeedBack.getClassName());
            }else if(mFeedBack.getTimeZone().equals("跑操批评")){
                runCriticizeClassNameList.add(mFeedBack.getClassName());
            }
        }

        mSportFourSituationDTO.setGymPraiseClassNameList(gymPraiseClassNameList);
        mSportFourSituationDTO.setGymCriticizeClassNameList(gymCriticizeClassNameList);
        mSportFourSituationDTO.setRunPraiseClassNameList(runPraiseClassNameList);
        mSportFourSituationDTO.setRunCriticizeClassNameList(runCriticizeClassNameList);
        return mSportFourSituationDTO;

    }

    @Override
    public String getFeedbackReport(String school, LocalDate checkDate) {
        String resp = "";
        String[] chineseNumbers = {"一", "二", "三", "四", "五", "六", "七", "八", "九", "十"};
        Integer order = 0;
//        M_ReadingFeedbackReport mReadingFeedbackReport = m_readingFeedbackReportMapper.getFeedbackReport(school);
        String timeZone = "体操表扬";
        List<M_FeedBack> sportGymPraiseList = m_sportRecordMapper.getSportRecord(school,checkDate,timeZone);
        timeZone = "体操批评";
        List<M_FeedBack> sportGymCriticizeList = m_sportRecordMapper.getSportRecord(school,checkDate,timeZone);
        timeZone = "跑操表扬";
        List<M_FeedBack> sportRunPraiseList = m_sportRecordMapper.getSportRecord(school,checkDate,timeZone);
        timeZone = "跑操批评";
        List<M_FeedBack> sportRunCriticizeList = m_sportRecordMapper.getSportRecord(school,checkDate,timeZone);


        if(sportGymPraiseList != null && sportGymPraiseList.size()>0){
            order++;
            resp = respReport(resp,order,chineseNumbers,checkDate,sportGymPraiseList);
        }
        if(sportRunPraiseList != null && sportRunPraiseList.size()>0){
            order++;
            resp = respReport(resp,order,chineseNumbers,checkDate,sportRunPraiseList);
        }
        if(sportGymCriticizeList != null && sportGymCriticizeList.size()>0){
            order++;
            resp = respReport(resp,order,chineseNumbers,checkDate,sportGymCriticizeList);
        }

        if(sportRunCriticizeList != null && sportRunCriticizeList.size()>0){
            order++;
            resp = respReport(resp,order,chineseNumbers,checkDate,sportRunCriticizeList);
        }


        if(resp.equals("")){
            return "暂无数据";
        }



        return resp;
    }
    private String respReport(String resp,Integer order,String[] chineseNumbers,LocalDate checkDate,List<M_FeedBack> classFeedBackList){

        //把order转换成中文
        String chineseOrder = chineseNumbers[order-1] + "、";
        String grade1manageFeedback = getClassName("一年级",classFeedBackList);
        String grade2manageFeedback = getClassName("二年级",classFeedBackList);
        String grade3manageFeedback = getClassName("三年级",classFeedBackList);
        String grade4manageFeedback = getClassName("四年级",classFeedBackList);
        String grade5manageFeedback = getClassName("五年级",classFeedBackList);
        String grade6manageFeedback = getClassName("六年级",classFeedBackList);
        String grade7manageFeedback = getClassName("七年级",classFeedBackList);
        String grade8manageFeedback = getClassName("八年级",classFeedBackList);
        String grade9manageFeedback = getClassName("九年级",classFeedBackList);
        if(order ==1){
            resp += checkDate.toString() + "<br>";
        }


        resp += chineseOrder;
        resp += "【"+classFeedBackList.get(0).getTimeZone()+"】"+"<br>";
        resp += grade1manageFeedback + grade2manageFeedback + grade3manageFeedback + grade4manageFeedback + grade5manageFeedback + grade6manageFeedback + grade7manageFeedback + grade8manageFeedback + grade9manageFeedback ;
        resp += "<br>";
        return resp;
    }

    /**
     * 根据不同年级输出要表扬的班级，要考虑到年级对应的班级为空，要考虑到最后的班级名称不加、
     */
    private String getClassName(String grade,List<M_FeedBack> feedBackList){
        List<M_FeedBack> gradeFeedBacks = new ArrayList<>();
        for(M_FeedBack feedBack: feedBackList){
            if(feedBack.getGrade().equals(grade)){
                gradeFeedBacks.add(feedBack);
            }
        }
        String feedback = "["+grade+"]" + "：";
        if (gradeFeedBacks.isEmpty()) {
            // 列表为空时的逻辑
            feedback = "";
        }else {
            for(M_FeedBack feedbackclass: gradeFeedBacks){
                if(feedbackclass == null){
                    feedback = "";
                }else if(feedbackclass != gradeFeedBacks.get(gradeFeedBacks.size()-1)){
                    feedback += feedbackclass.getClassName() + "、";
                }else {
                    feedback += feedbackclass.getClassName() +"<br>";
                }
            }
        }
        return feedback;
    }




    private String gradeResp(String grade,String resp,M_ReadGradeFeedDTO mReadGradeFeedDTO,List<String> gradeClassList,List<M_FeedBack> feedBackList){
        M_FeedBack mFeedBack = new M_FeedBack();
        mFeedBack.setGrade(grade);
        BeanUtils.copyProperties(mReadGradeFeedDTO, mFeedBack);
        List<String> recordClasNameList = m_sportRecordMapper.getClassNameList(mFeedBack);
        Set<String> set1 = new HashSet<>(recordClasNameList);
        Set<String> set2 = new HashSet<>(gradeClassList);
        boolean areEqual = set1.equals(set2);
        if(areEqual && recordClasNameList.size() == gradeClassList.size()){
            //如果两个set相等，说明两个集合的元素一样，不需要更新
            resp += grade+"noRevise";}
        else {
            //如果不相等，就要先删除再新增
            m_sportRecordMapper.deleteFeedback(mFeedBack);
            //把set2的数据转变为列表
            List<String>  grade1ClassListNew  = new ArrayList<>(set2);
            //新增数据
            for (String className : grade1ClassListNew) {
                M_FeedBack mFeedBackUpdate = new M_FeedBack();
                mFeedBackUpdate.setGrade(grade);
                mFeedBackUpdate.setTimeZone(mReadGradeFeedDTO.getTimeZone());
                mFeedBackUpdate.setCheckDate(mReadGradeFeedDTO.getCheckDate());
                mFeedBackUpdate.setSchool(mReadGradeFeedDTO.getSchool());
                mFeedBackUpdate.setClassName(className);
                mFeedBackUpdate.setCreateTime(LocalDateTime.now());
                feedBackList.add(mFeedBackUpdate);
            }
            m_sportRecordMapper.addClassNameList(feedBackList);
            feedBackList.clear();
            resp += grade+"success";}
        return resp;
    }

}
