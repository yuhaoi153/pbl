package flyfish.service;

import flyfish.pojo.DTO.M_AddNewTeacher;
import flyfish.pojo.DTO.M_DeleteReadingFeedbackDTO;
import flyfish.pojo.DTO.M_DeleteTeacherInfoDTO;
import flyfish.pojo.DTO.M_FeedbackDTO;
import flyfish.pojo.M_TeacherInfo;
import flyfish.pojo.VO.M_FeedBackReportVO;
import flyfish.pojo.VO.M_FeedbackVO;
import org.apache.ibatis.annotations.Update;

import java.util.List;

public interface M_EditInformationService {
    //编辑年级班级数
    void editGradeClassNum(String grade, Integer classNum,String school);
    
    //根据条件查询教师信息
    List<M_TeacherInfo> getTeacherInfoByCondition(String type, String content, String school, String label);

    //根据idlist删除教师信息表
    void deleteTeacherInfo(M_DeleteTeacherInfoDTO mDeleteTeacherInfoDTO);

    String addNewTeacher(M_AddNewTeacher mAddNewTeacher);

    void editTeacher(M_AddNewTeacher mAddNewTeacher);

    String addClassTeacher(M_AddNewTeacher mAddNewTeacher);

    String editClassTeacher(M_AddNewTeacher mAddNewTeacher);

    String editGradeClass(M_AddNewTeacher mAddNewTeacher);

    M_FeedBackReportVO getFeedbackContent(String school, String label);


    void editFeedbackContent(M_FeedBackReportVO mFeedBackReportVO);

    String addSingleGradeClassNum(String grade, Integer classNum, String school);

    List<M_FeedbackVO> getFeedbackData(M_FeedbackDTO mFeedbackDTO);

    String deleteFeedbackData(M_DeleteReadingFeedbackDTO mDeleteReadingFeedbackDTO);
}
