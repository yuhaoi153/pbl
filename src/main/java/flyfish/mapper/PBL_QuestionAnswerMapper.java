package flyfish.mapper;

import flyfish.pojo.PBLpojo.PBL_NameScoreDTO;
import flyfish.pojo.PBLpojo.PBL_QuestionItem;
import flyfish.pojo.PBLpojo.PBL_VoteDTO;
import org.apache.ibatis.annotations.*;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface PBL_QuestionAnswerMapper {

    void addItem(PBL_VoteDTO pBLVoteDTO);


    List<PBL_QuestionItem> getQuestionInfo(String pageName, String school, String lessonName, String subject, LocalDate checkDate);


    List<PBL_VoteDTO> getVoteResult(String school, String lessonName, String subject, String pageName, String username,LocalDate checkDate);

@Select("select * from pbl.questionAnswer where school = #{school} and lessonName = #{lessonName}  and pageName = #{pageName} and checkDate = #{checkDate}")
    List<PBL_VoteDTO> getVoteClassResult(String school, String lessonName, String subject, String pageName,LocalDate checkDate);

@Select("select * from pbl.questionAnswer where school = #{school} and checkDate = #{checkDate} and username = #{username} and questionId = #{questionId}")
    PBL_VoteDTO getByUsernameAndQuestionId(String school , LocalDate checkDate,String username, Integer questionId);

@Update("update pbl.questionAnswer set supplementary = #{supplementary} , answerContent = #{answerContent}, score = #{score}, comment = #{comment} where school = #{school} and checkDate = #{checkDate} and username = #{username} and questionId = #{questionId}")
    void updateItem(PBL_VoteDTO pblVoteDTO);

@Delete("delete from pbl.questionAnswer where id = #{id}")
    void deleteById(Integer id);
@Delete("delete from pbl.questionAnswer where username = #{username} and school = #{school} and lessonName = #{lessonName} and pageName = #{pageName} and checkDate = #{now}")
    void deleteByUserName(String username, String school, String lessonName, String pageName, LocalDate now);

@Select("select * from pbl.questionAnswer where school = #{school} and lessonName = #{lessonName} and pageName = #{pageName} and checkDate = #{checkDate}")
List<PBL_VoteDTO> getQuestionAnswer(String school, String lessonName, String subject, String pageName, LocalDate checkDate);


@Select("select questionId from pbl.questionAnswer where supplementary = #{key} and checkDate = #{checkDate} limit 1")
    Integer getQuestionInfoBySupplementary(String key,LocalDate checkDate);

@Delete("delete from pbl.questionAnswer where username = #{username} and school = #{school} and lessonName = #{lessonName} and pageName = #{pageName} and checkDate = #{now} and score = #{i}")
    void deleteByUserNameRank(String username, String school, String lessonName, String pageName, LocalDate now, int i);

@Select("select username , COALESCE(SUM(score),0) as sumScore from pbl.questionAnswer where school = #{school} and lessonName = #{lessonName} and checkDate = #{checkDate} and pageName = #{pageName} group by username")
    List<PBL_NameScoreDTO> getNameScoreSum(String school, String lessonName, LocalDate checkDate,String pageName);

@Delete("delete from pbl.questionAnswer where checkDate = #{checkDate}")
    void resetAllByCheckDate(LocalDate checkDate);
}
