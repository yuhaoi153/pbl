package flyfish.service.impl;


import flyfish.mapper.M_ClassTeacherRelationMapper;
import flyfish.mapper.M_DefaultConfigMapper;
import flyfish.mapper.M_TeacherListMapper;
import flyfish.pojo.M_DefaultConfig;
import flyfish.pojo.VO.M_HomeworkDefaultParamsVO;
import flyfish.service.M_HomeworkPageSettingService;
import flyfish.utils.ClassNameChangeUtills;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class M_HomeworkPageSettingServiceImpl implements M_HomeworkPageSettingService {
    @Autowired
    private M_DefaultConfigMapper defaultConfigMapper;
    @Autowired
    private ClassNameChangeUtills classNameChangeUtills;
    @Autowired
    private M_ClassTeacherRelationMapper classTeacherRelationMapper;
    @Autowired
    private M_TeacherListMapper teacherListMapper;


    /**
     * 获取网页端作业登记默认参数
     * @param school
     * @param classNumber
     * @param subject
     * @return
     */
    @Override
    public M_HomeworkDefaultParamsVO getHomeworkDefaultParamsBySubject(String school, String classNumber, String subject) {
        //首先根据班级和学科获取教师的userName;
        //改造classNumber
        classNumber = classNameChangeUtills.formatToChinese(classNumber);
        List<String> teacherList = classTeacherRelationMapper.getTeacherNameByClass(school,classNumber);
        String userName = null;
        for(String teacher:teacherList){
            String subjectGet = teacherListMapper.getSubjectByName(teacher,school);
            if(subject.equals(subjectGet)){
                userName = teacher;
            }
        }
        if(userName==null){
            throw new RuntimeException("未找到对应教师，请检查班级和学科是否正确");
        }
        return getHomeworkDefaultParams(school,userName);


    }


    @Override
    public M_HomeworkDefaultParamsVO getHomeworkDefaultParams(String school,String userName) {
        List<M_DefaultConfig> m_defaultConfigList = defaultConfigMapper.getContentBySchoolUserName(school,userName);
        M_HomeworkDefaultParamsVO m_homeworkDefaultParamsVO = new M_HomeworkDefaultParamsVO();
        if (m_defaultConfigList!= null && m_defaultConfigList.size() > 0) {
            for(M_DefaultConfig mDefaultConfig : m_defaultConfigList){
                if(mDefaultConfig.getInfoName().equals("minusScoreByHomework")){
                    m_homeworkDefaultParamsVO.setMinusScoreByHomework(mDefaultConfig.getTextConfig());
                }
                if(mDefaultConfig.getInfoName().equals("failRevisionAddScore")){
                    m_homeworkDefaultParamsVO.setFailRevisionAddScore(mDefaultConfig.getTextConfig());
                }
                if(mDefaultConfig.getInfoName().equals("completedRevisionAddScore")){
                    m_homeworkDefaultParamsVO.setCompletedRevisionAddScore(mDefaultConfig.getTextConfig());

                }
                if(mDefaultConfig.getInfoName().equals("hiRemind")){
                    m_homeworkDefaultParamsVO.setHiRemind(mDefaultConfig.getTextConfig());
                }
                if(mDefaultConfig.getInfoName().equals("homeworkResultRemind")){
                    m_homeworkDefaultParamsVO.setHomeworkResultRemind(mDefaultConfig.getTextConfig());
                }
                if(mDefaultConfig.getInfoName().equals("pageStudentSize")){
                    m_homeworkDefaultParamsVO.setPageStudentSize(mDefaultConfig.getIntConfig());
                }
                if(mDefaultConfig.getInfoName().equals("showScanner")){
                    m_homeworkDefaultParamsVO.setShowScanner(mDefaultConfig.getTextConfig());
                }
                if(mDefaultConfig.getInfoName().equals("showCompletedRevision")){
                    m_homeworkDefaultParamsVO.setShowCompletedRevision(mDefaultConfig.getTextConfig());
                }
                if(mDefaultConfig.getInfoName().equals("addScoreNumber")){
                    m_homeworkDefaultParamsVO.setAddScoreNumber(mDefaultConfig.getIntConfig());
                }
                if(mDefaultConfig.getInfoName().equals("minusScoreNumber")){
                    m_homeworkDefaultParamsVO.setMinusScoreNumber(mDefaultConfig.getIntConfig());
                }
                if(mDefaultConfig.getInfoName().equals("revisionAddScore")){
                    m_homeworkDefaultParamsVO.setRevisionAddScore(mDefaultConfig.getIntConfig());
                }
            }

        }



        return m_homeworkDefaultParamsVO;
    }

    @Override
    public String setMoralStatisticDefaultParams(M_HomeworkDefaultParamsVO mHomeworkDefaultParamsVO) {
        List<M_DefaultConfig> m_defaultConfigList = defaultConfigMapper.getContentBySchoolUserName(mHomeworkDefaultParamsVO.getSchool(),mHomeworkDefaultParamsVO.getUserName());
        if (m_defaultConfigList != null && m_defaultConfigList.size() > 0) {
            M_DefaultConfig mDefaultConfig = new M_DefaultConfig();
            mDefaultConfig.setInfoName("minusScoreByHomework");
            mDefaultConfig.setTextConfig(mHomeworkDefaultParamsVO.getMinusScoreByHomework());
            mDefaultConfig.setSchool(mHomeworkDefaultParamsVO.getSchool());
            mDefaultConfig.setUserName(mHomeworkDefaultParamsVO.getUserName());
            defaultConfigMapper.updateDefaultConfig(mDefaultConfig);
            mDefaultConfig.setInfoName("failRevisionAddScore");
            mDefaultConfig.setTextConfig(mHomeworkDefaultParamsVO.getFailRevisionAddScore());
            mDefaultConfig.setIntConfig(null);
            defaultConfigMapper.updateDefaultConfig(mDefaultConfig);
            mDefaultConfig.setInfoName("completedRevisionAddScore");
            mDefaultConfig.setTextConfig(mHomeworkDefaultParamsVO.getCompletedRevisionAddScore());
            mDefaultConfig.setIntConfig(null);
            defaultConfigMapper.updateDefaultConfig(mDefaultConfig);
            mDefaultConfig.setInfoName("hiRemind");
            mDefaultConfig.setTextConfig(mHomeworkDefaultParamsVO.getHiRemind());
            mDefaultConfig.setIntConfig(null);
            defaultConfigMapper.updateDefaultConfig(mDefaultConfig);
            mDefaultConfig.setInfoName("homeworkResultRemind");
            mDefaultConfig.setTextConfig(mHomeworkDefaultParamsVO.getHomeworkResultRemind());
            mDefaultConfig.setIntConfig(null);
            defaultConfigMapper.updateDefaultConfig(mDefaultConfig);
            mDefaultConfig.setInfoName("pageStudentSize");
            mDefaultConfig.setIntConfig(mHomeworkDefaultParamsVO.getPageStudentSize());
            mDefaultConfig.setTextConfig(null);
            defaultConfigMapper.updateDefaultConfig(mDefaultConfig);
            mDefaultConfig.setInfoName("showScanner");
            mDefaultConfig.setTextConfig(mHomeworkDefaultParamsVO.getShowScanner());
            mDefaultConfig.setIntConfig(null);
            defaultConfigMapper.updateDefaultConfig(mDefaultConfig);
            mDefaultConfig.setInfoName("showCompletedRevision");
            mDefaultConfig.setTextConfig(mHomeworkDefaultParamsVO.getShowCompletedRevision());
            mDefaultConfig.setIntConfig(null);
            defaultConfigMapper.updateDefaultConfig(mDefaultConfig);
            mDefaultConfig.setInfoName("addScoreNumber");
            mDefaultConfig.setIntConfig(mHomeworkDefaultParamsVO.getAddScoreNumber());
            mDefaultConfig.setTextConfig(null);
            defaultConfigMapper.updateDefaultConfig(mDefaultConfig);
            mDefaultConfig.setInfoName("minusScoreNumber");
            mDefaultConfig.setIntConfig(mHomeworkDefaultParamsVO.getMinusScoreNumber());
            mDefaultConfig.setTextConfig(null);
            defaultConfigMapper.updateDefaultConfig(mDefaultConfig);
            mDefaultConfig.setInfoName("revisionAddScore");
            mDefaultConfig.setIntConfig(mHomeworkDefaultParamsVO.getRevisionAddScore());
            mDefaultConfig.setTextConfig(null);
            defaultConfigMapper.updateDefaultConfig(mDefaultConfig);

        }else {


            M_DefaultConfig mDefaultConfig = new M_DefaultConfig();
            mDefaultConfig.setInfoName("minusScoreByHomework");
            mDefaultConfig.setTextConfig(mHomeworkDefaultParamsVO.getMinusScoreByHomework());
            mDefaultConfig.setSchool(mHomeworkDefaultParamsVO.getSchool());
            mDefaultConfig.setUserName(mHomeworkDefaultParamsVO.getUserName());
            defaultConfigMapper.insertDefaultConfig(mDefaultConfig);
            mDefaultConfig.setInfoName("failRevisionAddScore");
            mDefaultConfig.setTextConfig(mHomeworkDefaultParamsVO.getFailRevisionAddScore());
            mDefaultConfig.setIntConfig(null);
            defaultConfigMapper.insertDefaultConfig(mDefaultConfig);
            mDefaultConfig.setInfoName("completedRevisionAddScore");
            mDefaultConfig.setTextConfig(mHomeworkDefaultParamsVO.getCompletedRevisionAddScore());
            mDefaultConfig.setIntConfig(null);
            defaultConfigMapper.insertDefaultConfig(mDefaultConfig);
            mDefaultConfig.setInfoName("hiRemind");
            mDefaultConfig.setTextConfig(mHomeworkDefaultParamsVO.getHiRemind());
            mDefaultConfig.setIntConfig(null);
            defaultConfigMapper.insertDefaultConfig(mDefaultConfig);
            mDefaultConfig.setInfoName("homeworkResultRemind");
            mDefaultConfig.setTextConfig(mHomeworkDefaultParamsVO.getHomeworkResultRemind());
            mDefaultConfig.setIntConfig(null);
            defaultConfigMapper.insertDefaultConfig(mDefaultConfig);
            mDefaultConfig.setInfoName("pageStudentSize");
            mDefaultConfig.setIntConfig(mHomeworkDefaultParamsVO.getPageStudentSize());
            mDefaultConfig.setTextConfig(null);
            defaultConfigMapper.insertDefaultConfig(mDefaultConfig);
            mDefaultConfig.setInfoName("showScanner");
            mDefaultConfig.setTextConfig(mHomeworkDefaultParamsVO.getShowScanner());
            mDefaultConfig.setIntConfig(null);
            defaultConfigMapper.insertDefaultConfig(mDefaultConfig);
            mDefaultConfig.setInfoName("showCompletedRevision");
            mDefaultConfig.setTextConfig(mHomeworkDefaultParamsVO.getShowCompletedRevision());
            mDefaultConfig.setIntConfig(null);
            defaultConfigMapper.insertDefaultConfig(mDefaultConfig);
            mDefaultConfig.setInfoName("addScoreNumber");
            mDefaultConfig.setIntConfig(mHomeworkDefaultParamsVO.getAddScoreNumber());
            mDefaultConfig.setTextConfig(null);
            defaultConfigMapper.insertDefaultConfig(mDefaultConfig);
            mDefaultConfig.setInfoName("minusScoreNumber");
            mDefaultConfig.setIntConfig(mHomeworkDefaultParamsVO.getMinusScoreNumber());
            mDefaultConfig.setTextConfig(null);
            defaultConfigMapper.insertDefaultConfig(mDefaultConfig);
            mDefaultConfig.setInfoName("revisionAddScore");
            mDefaultConfig.setIntConfig(mHomeworkDefaultParamsVO.getRevisionAddScore());
            mDefaultConfig.setTextConfig(null);
            defaultConfigMapper.insertDefaultConfig(mDefaultConfig);
        }



        return "设置默认参数成功";
    }




}
