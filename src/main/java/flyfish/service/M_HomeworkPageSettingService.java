package flyfish.service;

import flyfish.pojo.VO.M_HomeworkDefaultParamsVO;

public interface M_HomeworkPageSettingService {
    M_HomeworkDefaultParamsVO getHomeworkDefaultParams(String school,String userName);

    String setMoralStatisticDefaultParams(M_HomeworkDefaultParamsVO mHomeworkDefaultParamsVO);

    M_HomeworkDefaultParamsVO getHomeworkDefaultParamsBySubject(String school, String classNumber, String subject);
}
