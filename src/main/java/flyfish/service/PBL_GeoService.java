package flyfish.service;

import flyfish.pojo.Menu;
import flyfish.pojo.PBLpojo.PBL_QuestionItem;
import flyfish.pojo.PBLpojo.PBL_VoteDTO;
import flyfish.pojo.PBLpojo.PBL_VoteResult;
import flyfish.pojo.VO.M_BackGroundVO;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface PBL_GeoService {
    List<Menu> confirmUser(String username, String password, String school);

    List<M_BackGroundVO> getImgUrl(String position, String school);

    String votePbl(PBL_VoteDTO pBLVoteDTO);

    List<PBL_QuestionItem> getQuestionInfo(String pageName, String school, String lessonName, String subject, LocalDate checkDate);

    List<PBL_VoteDTO> getVoteResult(String school, String lessonName, String subject, String pageName, String username, LocalDate checkDate);

    Map<String,PBL_VoteResult> getClassVoteResult(String school, String lessonName, String subject, String pageName, String voteA, String voteB, String voteC, String voteD, LocalDate checkDate);

    String uploadFinalWork(PBL_VoteDTO pblVoteDTO);

    Map<Integer, Integer> getfinalWorkRank(String school, String lessonName, String subject, String pageName, LocalDate checkDate);


    List<Map<String, Integer>>  getMathMindFirstThree(String school, String lessonName, String subject, String pageName, LocalDate checkDate);

    List<Map<String, Integer>> getMathSumFirstThree(String school, String lessonName, String subject, String pageName, LocalDate checkDate);

    String votefinalPbl(PBL_VoteDTO pBLVoteDTO);

    String unlockMenu(String username, String school,String menuName);

    String getStatus(String menuName);

    void resetAllAnswer(String school, String lessonName, String subject, String pageName, LocalDate checkDate);
}
