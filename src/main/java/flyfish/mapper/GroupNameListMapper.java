package flyfish.mapper;

import flyfish.pojo.GroupNameList;
import flyfish.pojo.VO.GroupFeedbackVO;
import org.apache.ibatis.annotations.*;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface GroupNameListMapper {

    //先删除group
    @Delete("delete from homework.groupNameList where class_number = #{classNumber} and group_number = #{groupNumber} and subject = #{subject} and school = #{school}")
    void deleteGroup(String classNumber, String groupNumber, String subject,String school);

    //添加group
    @Insert("insert into homework.groupNameList (class_number, group_number,name,subject,createTime,school) values (#{classNumber}, #{groupNumber}, #{nameListString},#{subject},#{createTime},#{school})")
    void addGroup(String classNumber, String groupNumber, String nameListString,String subject, LocalDateTime createTime,String school);

    //查询group姓名名单
    @Select("select name from homework.groupNameList where class_number = #{classNumber} and group_number = #{groupNumber} and subject = #{subject} and school = #{school}")
    String getNameList(String classNumber, String groupNumber ,String subject,String school);

    //查询所有group积分
    @Select("select * from homework.groupNameList where class_number = #{classNumber} and subject = #{subject}  and school = #{school} order by CAST(group_number AS UNSIGNED)")
    List<GroupFeedbackVO> queryAllGroup(String classNumber, String subject,String school);

    //分组增加分数
    @Update("update homework.groupNameList set sumscore = sumscore + #{score}, addscore = addscore +#{score} where class_number = #{classNumber} and group_number = #{groupNumber} and subject = #{subject} and school = #{school}")
    void addGroupScore(String classNumber, String groupNumber, String subject, Integer score,String school);

    //分组减少分数
    @Update("update homework.groupNameList set sumscore = sumscore - #{score}, punishscore = punishscore +#{score} where class_number = #{classNumber} and group_number = #{groupNumber} and subject = #{subject} and school = #{school}")
    void punishGroupScore(String classNumber, String groupNumber, String subject, Integer score,String school);
}
