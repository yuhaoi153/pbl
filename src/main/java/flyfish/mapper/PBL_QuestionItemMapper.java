package flyfish.mapper;

import flyfish.pojo.PBLpojo.PBL_QuestionItem;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;

@Mapper
public interface PBL_QuestionItemMapper {

    @Insert("insert into pbl.questionItem (content, referenceAnswer, pageName, questionType, questionLevel, score, lessonName, school, subject, checkDate, username) values " +
            "(#{content}, #{referenceAnswer}, #{pageName}, #{questionType}, #{questionLevel}, #{score}, #{lessonName}, #{school}, #{subject}, #{checkDate}, #{username})")
    void insertItem(PBL_QuestionItem pblQuestionItem);

    @Delete("delete from pbl.questionItem where username = #{username} and school =#{school} and subject = #{subject} and checkDate = #{checkDate}")
    void deleteItem(PBL_QuestionItem pblQuestionItem);

    @Select("select  * from pbl.questionItem where id = #{questionId}")
    PBL_QuestionItem getItemById(Integer questionId);

    @Delete("delete from pbl.questionItem where pageName = #{pageName} and checkDate = #{checkDate}")
    void resetAllByCheckDate(String pageName, LocalDate checkDate);
}
