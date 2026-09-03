package flyfish.mapper;

import flyfish.pojo.AccumulateScore;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface AccumulateScoreMapper {
    /**
     * 判断分数表是否存在
     * @param classNumber
     * @param nameList
     * @return
     */

    List<String> isexist(String classNumber, List<String> nameList,String subject,String school);

    /**
     * 批量新增
     * @param accumulateScoreList
     */

    void batchadd(List<AccumulateScore> accumulateScoreList);

    /**
     * 批量更新
     * @param minusnumber
     * @param classNumber
     * @param nameList
     * @param subject
     */
    void updateminusscore(Integer minusnumber, String classNumber, List<String> nameList, String subject, LocalDateTime updateTime,String school);

    /**
     * 批量加分
     * @param addnumber
     * @param classNumber
     * @param nameList
     * @param subject
     * @param updateTime
     */
    void updateaddscore(Integer addnumber, String classNumber, List<String> nameList, String subject, LocalDateTime updateTime,String school);

    /**
     * 查询分数
     * @param name
     * @param classNumber
     * @param subject
     * @return
     */
    AccumulateScore getByNameClass(String name, String classNumber, String subject);

    /**
     * 跟新兑换分数
     * @param minusnumber
     * @param classNumber
     * @param nameList
     * @param subject
     * @param updateTime
     */
    void updateminusnopunishscore(Integer minusnumber, String classNumber, List<String> nameList, String subject, LocalDateTime updateTime,String school);

    /**
     * 获得所有学科，指定学生名单的积分情况
     * @param nameList
     * @param classNumber
     * @return
     */
    List<AccumulateScore> getAllscore(List<String> nameList, String classNumber, String school);

    /**
     * 加分特殊操作
     * @param addscoreList
     * @param classNumber
     * @param nameList
     * @param subject
     * @param updateTime
     */
    void updateaddscoreSpecial(List<Integer> addscoreList, String classNumber, List<String> nameList, String subject, LocalDateTime updateTime,String school);

    /**
     *  更新NFC表扬分数
     * @param
     * @param name
     * @param classNumber
     * @param subject
     */
    @Update("update homework.accumulateScore set addscore = addscore + 1 where name = #{name} and classNumber = #{classNumber} and subject = #{subject} and school = #{school}")
    void updateNFCwellScore( String name, String classNumber, String subject,String school);

    @Update("update homework.accumulateScore set minusscore = minusscore - 1 , punishscore = punishscore - 1 where name = #{name} and classNumber = #{classNumber} and subject = #{subject} and school = #{school}")
    void updateNFCbadScore(String name, String classNumber, String subject,String school);

    @Update("update homework.accumulateScore set addscore = addscore + #{score} where name = #{name} and classNumber = #{classNumber} and subject = #{subject} and school = #{school}")
    void updateScannerwellScore(String name, String classNumber, String subject, Integer score,String school);

    @Update("update homework.accumulateScore set minusscore = minusscore - #{score} , punishscore = punishscore - #{score} where name = #{name} and classNumber = #{classNumber} and subject = #{subject} and school = #{school}")
    void updateScannerbadScore(String name, String classNumber, String subject,Integer score,String school);

    @Select("select * from homework.accumulateScore where classNumber = #{classNumber} and name = #{username} and subject = #{subject}")
    AccumulateScore getAccumulateScoreBySchoolClassName(String classNumber, String username,  String subject);

    List<AccumulateScore> getAllscoreBySubject(List<String> nameList, String className, String school, String subject);
}
