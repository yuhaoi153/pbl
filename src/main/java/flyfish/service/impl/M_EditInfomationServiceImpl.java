package flyfish.service.impl;

import flyfish.mapper.*;
import flyfish.pojo.DTO.*;
import flyfish.pojo.M_ClassTeacherRelation;
import flyfish.pojo.M_GradeClass;
import flyfish.pojo.M_TeacherData;
import flyfish.pojo.M_TeacherInfo;
import flyfish.pojo.VO.M_FeedBackReportVO;
import flyfish.pojo.VO.M_FeedbackVO;
import flyfish.service.M_EditInformationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class M_EditInfomationServiceImpl implements M_EditInformationService {

    @Autowired
    private M_GradeClassNumMapper m_gradeClassNumMapper;
    @Autowired
    private M_TeacherListMapper m_TeacherListMapper;
    @Autowired
    private M_ClassTeacherRelationMapper m_ClassTeacherRelationMapper;
    @Autowired
    private M_ReadingFeedbackReportMapper m_ReadingFeedbackReportMapper;
    @Autowired
    private M_ReadingFeedbackMapper m_ReadingFeedbackMapper;
    @Autowired
    private M_SingleReadTeacherRecordMapper mSingleReadTeacherRecordMapper;
    @Autowired
    private M_SportRecordMapper m_sportRecordMapper;

    //编辑年级班级数
    @Override
    public void editGradeClassNum(String grade, Integer classNum,String school) {

        List<M_GradeClass> classList = new ArrayList<>();
        String gradePrefix = grade.substring(0, 1);
        for (int i = 1; i <= classNum; i++) {
            M_GradeClass m_gradeClass = new M_GradeClass();
            m_gradeClass.setGrade(grade);
            m_gradeClass.setClassName(gradePrefix + "("+i+")"+"班");
            m_gradeClass.setSchool(school);
            classList.add(m_gradeClass);

        }
        //删除数据
        m_gradeClassNumMapper.deleteGradeClassNum(grade,school);
        //新增一条数据
        m_gradeClassNumMapper.editGradeClassNum(classList);

    }


        public  String formatClassName(String className) {
        if(className == null || className.equals("")){
            return null;
        }
            String[] CHINESE_NUMBERS = {"零", "一", "二", "三", "四", "五", "六", "七", "八", "九"};
            Pattern pattern = Pattern.compile("\\d+");
            Matcher matcher = pattern.matcher(className);
            String firstChar = className.substring(0, 1);
            if (Character.isDigit(firstChar.charAt(0))) {
                int digit = Integer.parseInt(firstChar);
                firstChar = CHINESE_NUMBERS[digit];
            }
            String number = "";
            if (matcher.find()) {
                number = matcher.group();
                if (number.length() > 1) {
                    number = number.substring(number.length() - 1);
                }
            }
            if (className.contains("（") && className.contains("）")) {
                return firstChar + "(" + number + ")班";
            } else if (className.contains("(") && className.contains(")")) {
                return className;
            } else {
                return firstChar + "(" + number + ")班";
            }
        }


        @Override
    public List<M_TeacherInfo> getTeacherInfoByCondition(String type, String content, String school, String label) {
        if(label.equals("teacherList")){
            if(type.equals("教师")){
                List<M_TeacherInfo> teacherInfoByTeacherName = m_TeacherListMapper.getTeacherInfoByTeacherName(content, school);
                    return teacherInfoByTeacherName;
            } else if (type.equals("学科")) {
                List<M_TeacherInfo> teacherInfoByTeacherName = m_TeacherListMapper.getTeacherInfoBySubject(content, school);
                return teacherInfoByTeacherName;

            }else {
                List<M_TeacherInfo> teacherInfoByTeacherName = m_TeacherListMapper.getTeacherInfoByTeacherName(content, school);
                return teacherInfoByTeacherName;
            }

    } else if (label.equals("gradeClass")) {
            if (type.equals("年级")) {
                List<M_TeacherInfo> teacherInfoByGrade = m_gradeClassNumMapper.getTeacherInfoByGrade(content, school);
                return teacherInfoByGrade;
            } else if (type.equals("班级")) {
                String contentNew = formatClassName(content);

                List<M_TeacherInfo> teacherInfoByClassName = m_gradeClassNumMapper.getTeacherInfoByClassName(contentNew, school);
                return teacherInfoByClassName;
            }
            else {
                List<M_TeacherInfo> teacherInfoByGrade = m_gradeClassNumMapper.getTeacherInfoByGrade(content, school);
                return teacherInfoByGrade;
            }

        } else if (label.equals("classTeacher")) {
            if (type.equals("教师")) {
                List<M_TeacherInfo> teacherInfoByTeacherName = m_ClassTeacherRelationMapper.getTeacherInfoByTeacherName(content, school);
                return teacherInfoByTeacherName;
            } else if (type.equals("班级")) {
                //把content中的第一个字和数字提取出来
                String contentNew = formatClassName(content);
                List<M_TeacherInfo> teacherInfoByTeacherName = m_ClassTeacherRelationMapper.getTeacherInfoByClassName(contentNew, school);
                return teacherInfoByTeacherName;
            }
            else {
                List<M_TeacherInfo> teacherInfoByTeacherName = m_ClassTeacherRelationMapper.getTeacherInfoByTeacherName(content, school);
                return teacherInfoByTeacherName;
            }

        }else {
            return null;
        }


    }

    @Override
    public void deleteTeacherInfo(M_DeleteTeacherInfoDTO mDeleteTeacherInfoDTO) {
        if(mDeleteTeacherInfoDTO.getLabel().equals("teacherList")){
            m_TeacherListMapper.deleteTeacherListByIDList(mDeleteTeacherInfoDTO);
        } else if (mDeleteTeacherInfoDTO.getLabel().equals("gradeClass")) {
            m_gradeClassNumMapper.deleteGradeClassNumByIdList(mDeleteTeacherInfoDTO);
        } else if (mDeleteTeacherInfoDTO.getLabel().equals("classTeacher")) {
            m_ClassTeacherRelationMapper.deleteClassTeacherRelation(mDeleteTeacherInfoDTO);
        }
    }

    @Override
    public String addNewTeacher(M_AddNewTeacher mAddNewTeacher) {
        M_TeacherData m_TeacherData = new M_TeacherData();
        m_TeacherData.setSchool(mAddNewTeacher.getSchool());
        m_TeacherData.setTeacherName(mAddNewTeacher.getTeacherName());
        m_TeacherData.setSubject(mAddNewTeacher.getSubject());
        Integer teacherId = m_TeacherListMapper.getTeacherId(mAddNewTeacher.getTeacherName(),mAddNewTeacher.getSchool());
        if (teacherId == null) {
            m_TeacherListMapper.newTeacher(m_TeacherData);
            return "success";
        }else {
            return "已存在："+mAddNewTeacher.getTeacherName()+"-"+mAddNewTeacher.getSubject();
        }

    }

    @Override
    public void editTeacher(M_AddNewTeacher mAddNewTeacher) {
        m_TeacherListMapper.editTeacherById(mAddNewTeacher);
    }

    @Override
    public String addClassTeacher(M_AddNewTeacher mAddNewTeacher) {

        String className = mAddNewTeacher.getClassName();
        String school = mAddNewTeacher.getSchool();
        Integer classId = m_gradeClassNumMapper.getClassId(school,className);
        if(classId == null){
            return "未找到："+mAddNewTeacher.getClassName();
        }
        Integer teacherId = m_TeacherListMapper.getTeacherId(mAddNewTeacher.getTeacherName(),mAddNewTeacher.getSchool());
        if(teacherId == null){
            return "未找到："+mAddNewTeacher.getTeacherName();
        }
        M_ClassTeacherRelation m_ClassTeacherRelation = new M_ClassTeacherRelation();
        m_ClassTeacherRelation.setTeacherName(mAddNewTeacher.getTeacherName());
        m_ClassTeacherRelation.setTeacherId(teacherId);
        m_ClassTeacherRelation.setClassName(mAddNewTeacher.getClassName());
        m_ClassTeacherRelation.setClassId(classId);
        m_ClassTeacherRelation.setSchool(mAddNewTeacher.getSchool());
        List<M_ClassTeacherRelation> m_ClassTeacherRelationList = new ArrayList<>();
        m_ClassTeacherRelationList.add(m_ClassTeacherRelation);
        M_ClassTeacherRelation mClassTeacherRelationNew =   m_ClassTeacherRelationMapper.getClassTeacher(m_ClassTeacherRelation);
        if(mClassTeacherRelationNew == null){
            m_ClassTeacherRelationMapper.saveRelations(m_ClassTeacherRelationList);
        } else {
            return "已存在："+mAddNewTeacher.getTeacherName()+"-"+mAddNewTeacher.getClassName();
        }


        return "success";
    }

    @Override
    public String editClassTeacher(M_AddNewTeacher mAddNewTeacher) {
        Integer classId = m_gradeClassNumMapper.getClassId(mAddNewTeacher.getClassName(),mAddNewTeacher.getSchool());
        if(classId == null){
            return "未找到："+mAddNewTeacher.getClassName();
        }
        Integer teacherId = m_TeacherListMapper.getTeacherId(mAddNewTeacher.getTeacherName(),mAddNewTeacher.getSchool());
        if(teacherId == null){
            return "未找到："+mAddNewTeacher.getTeacherName();
        }
        M_ClassTeacherRelation m_ClassTeacherRelation = new M_ClassTeacherRelation();
        m_ClassTeacherRelation.setTeacherName(mAddNewTeacher.getTeacherName());
        m_ClassTeacherRelation.setTeacherId(teacherId);
        m_ClassTeacherRelation.setClassName(mAddNewTeacher.getClassName());
        m_ClassTeacherRelation.setClassId(classId);
        m_ClassTeacherRelation.setSchool(mAddNewTeacher.getSchool());
        List<M_ClassTeacherRelation> m_ClassTeacherRelationList = new ArrayList<>();
        m_ClassTeacherRelationList.add(m_ClassTeacherRelation);
        M_ClassTeacherRelation mClassTeacherRelationNew =   m_ClassTeacherRelationMapper.getClassTeacher(m_ClassTeacherRelation);
        if(mClassTeacherRelationNew == null){
            return "未找到："+mAddNewTeacher.getTeacherName()+"-"+mAddNewTeacher.getClassName();
        } else {
            m_ClassTeacherRelationMapper.deleteRelations(m_ClassTeacherRelationList);
            m_ClassTeacherRelationMapper.saveRelations(m_ClassTeacherRelationList);
        }

        return "success";
    }

    @Override
    public String editGradeClass(M_AddNewTeacher mAddNewTeacher) {

        String grade = mAddNewTeacher.getGrade();
        Integer classNum = mAddNewTeacher.getClassNum();
        String className = "";
        className = grade.substring(0, 1)+"(" + classNum + ")班";

            m_gradeClassNumMapper.deleteByAllInfo(grade,className,mAddNewTeacher.getSchool());
            m_gradeClassNumMapper.addGradeClass(grade,className,mAddNewTeacher.getSchool());

        return "success";
    }

    @Override
    public M_FeedBackReportVO getFeedbackContent(String school, String label) {
        M_FeedBackReportVO mFeedBackReportVO = m_ReadingFeedbackReportMapper.getFeedbackContent(school);
        return mFeedBackReportVO;
    }

    @Override
    public void editFeedbackContent(M_FeedBackReportVO mFeedBackReportVO) {
        m_ReadingFeedbackReportMapper.editFeedbackContent(mFeedBackReportVO);
    }

    @Override
    public String addSingleGradeClassNum(String grade, Integer classNum, String school) {
        String className = grade.substring(0, 1) + "(" + classNum + ")" + "班";
        //先删除再新增
        Integer gradeClassId = m_gradeClassNumMapper.getId(school,grade,className);
        if(gradeClassId != null){
            return "已存在："+className;
        } else {

            m_gradeClassNumMapper.addGradeClass(grade,className,school);
            return "success";


        }

    }

    @Override
    public List<M_FeedbackVO> getFeedbackData(M_FeedbackDTO mFeedbackDTO) {

        if(mFeedbackDTO.getLabel().equals("班级反馈")){
            if(mFeedbackDTO.getType().equals("班级")){
                String contentNew = formatClassName(mFeedbackDTO.getContent());
                mFeedbackDTO.setContent(contentNew);
                List<M_FeedbackVO> feedbackData = m_ReadingFeedbackMapper.getFeedbackDataByClassName(mFeedbackDTO);
                return feedbackData;
            } else if (mFeedbackDTO.getType().equals("年级")) {
                List<M_FeedbackVO> feedbackData = m_ReadingFeedbackMapper.getFeedbackDataByGrade(mFeedbackDTO);
                return feedbackData;
            } else { //其他学科、教师的情况统一作为班级处理
                String contentNew = formatClassName(mFeedbackDTO.getContent());
                mFeedbackDTO.setContent(contentNew);
                List<M_FeedbackVO> feedbackData = m_ReadingFeedbackMapper.getFeedbackDataByClassName(mFeedbackDTO);
                return feedbackData;

            }

        } else if (mFeedbackDTO.getLabel().equals("教师反馈")) {
            if (mFeedbackDTO.getType().equals("教师")) {
                List<M_FeedbackVO> feedbackData = mSingleReadTeacherRecordMapper.getFeedbackDataByTeacherName(mFeedbackDTO);
                return feedbackData;
            } else if (mFeedbackDTO.getType().equals("学科")) {
                List<M_FeedbackVO> feedbackData = mSingleReadTeacherRecordMapper.getFeedbackDataBySubject(mFeedbackDTO);
                return feedbackData;
            } else if (mFeedbackDTO.getType().equals("班级")) {
                String contentNew = formatClassName(mFeedbackDTO.getContent());
                mFeedbackDTO.setContent(contentNew);
                List<M_FeedbackVO> feedbackData = mSingleReadTeacherRecordMapper.getFeedbackDataByClassName(mFeedbackDTO);
                return feedbackData;
            } else {
                String contentNew = formatClassName(mFeedbackDTO.getContent());
                mFeedbackDTO.setContent(contentNew);

                List<M_FeedbackVO> feedbackData = mSingleReadTeacherRecordMapper.getFeedbackDataByClassName(mFeedbackDTO);
                return feedbackData;
            }



        } else if(mFeedbackDTO.getLabel().equals("早操反馈")){
            if(mFeedbackDTO.getType().equals("班级")){
                String contentNew = formatClassName(mFeedbackDTO.getContent());
                mFeedbackDTO.setContent(contentNew);
                List<M_FeedbackVO> feedbackData = m_sportRecordMapper.getFeedbackDataByClassName(mFeedbackDTO);
                return feedbackData;
            } else if (mFeedbackDTO.getType().equals("年级")) {
                List<M_FeedbackVO> feedbackData = m_sportRecordMapper.getFeedbackDataByGrade(mFeedbackDTO);
                return feedbackData;
            } else { //其他学科、教师的情况统一作为班级处理
                String contentNew = formatClassName(mFeedbackDTO.getContent());
                mFeedbackDTO.setContent(contentNew);
                List<M_FeedbackVO> feedbackData = m_sportRecordMapper.getFeedbackDataByClassName(mFeedbackDTO);
                return feedbackData;

            }
        }

        else {
            return null;
        }

    }

    @Override
    public String deleteFeedbackData(M_DeleteReadingFeedbackDTO mDeleteReadingFeedbackDTO) {
        if(mDeleteReadingFeedbackDTO.getLabel().equals("班级反馈")){
            if(mDeleteReadingFeedbackDTO.getType().equals("班级")){
                String contentNew = formatClassName(mDeleteReadingFeedbackDTO.getContent());
                mDeleteReadingFeedbackDTO.setContent(contentNew);
                m_ReadingFeedbackMapper.deleteReadingFeedbackByIdList(mDeleteReadingFeedbackDTO);

            } else if (mDeleteReadingFeedbackDTO.getType().equals("年级")) {
                m_ReadingFeedbackMapper.deleteFeedbackByIdListByGrade(mDeleteReadingFeedbackDTO);
            } else { //其他学科、教师的情况统一作为班级处理

                String contentNew = formatClassName(mDeleteReadingFeedbackDTO.getContent());
                mDeleteReadingFeedbackDTO.setContent(contentNew);
                m_ReadingFeedbackMapper.deleteReadingFeedbackByIdList(mDeleteReadingFeedbackDTO);
            }

        } else if (mDeleteReadingFeedbackDTO.getLabel().equals("教师反馈")) {
            if (mDeleteReadingFeedbackDTO.getType().equals("教师")) {
                mSingleReadTeacherRecordMapper.deleteTeacherFeedbackByTeacherNameIds(mDeleteReadingFeedbackDTO);
            } else if (mDeleteReadingFeedbackDTO.getType().equals("学科")) {
                mSingleReadTeacherRecordMapper.deleteTeacherFeedbackBySubjectIds(mDeleteReadingFeedbackDTO);
            } else if (mDeleteReadingFeedbackDTO.getType().equals("班级")) {
                String contentNew = formatClassName(mDeleteReadingFeedbackDTO.getContent());
                mDeleteReadingFeedbackDTO.setContent(contentNew);
                mSingleReadTeacherRecordMapper.deleteTeacherFeedbackByClassName(mDeleteReadingFeedbackDTO);
            } else {
                String contentNew = formatClassName(mDeleteReadingFeedbackDTO.getContent());
                mDeleteReadingFeedbackDTO.setContent(contentNew);
                mSingleReadTeacherRecordMapper.deleteTeacherFeedbackByClassName(mDeleteReadingFeedbackDTO);

            }
        }else if(mDeleteReadingFeedbackDTO.getLabel().equals("早操反馈")){
            if(mDeleteReadingFeedbackDTO.getType().equals("班级")){
                String contentNew = formatClassName(mDeleteReadingFeedbackDTO.getContent());
                mDeleteReadingFeedbackDTO.setContent(contentNew);
                m_sportRecordMapper.deleteSportFeedbackByIdList(mDeleteReadingFeedbackDTO);

            } else if (mDeleteReadingFeedbackDTO.getType().equals("年级")) {
                m_sportRecordMapper.deleteSportFeedbackByIdList(mDeleteReadingFeedbackDTO);
            } else { //其他学科、教师的情况统一作为班级处理

                String contentNew = formatClassName(mDeleteReadingFeedbackDTO.getContent());
                mDeleteReadingFeedbackDTO.setContent(contentNew);
                m_sportRecordMapper.deleteSportFeedbackByIdList(mDeleteReadingFeedbackDTO);
            }

        }
        else {
            return null;
        }
        return "success";

    }
}
