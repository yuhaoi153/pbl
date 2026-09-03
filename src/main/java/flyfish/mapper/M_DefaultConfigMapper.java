package flyfish.mapper;

import flyfish.pojo.M_DefaultConfig;
import flyfish.pojo.VO.M_HomeworkDefaultParamsVO;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface M_DefaultConfigMapper {
    @Select("select textConfig from miniprograme.defaultConfig where school = #{school} and infoName = #{infoName}")
    String getFeedbackText(String school, String infoName);

    //根据德育标签和标签名称删除默认配置
    @Delete("delete from miniprograme.defaultConfig where school = #{school} and infoName = #{infoName} and textConfig = #{textConfig}")
    void deleteByInfoNameAndTextConfig(String infoName, String textConfig, String school);

    //新增默认配置
    @Insert("insert into miniprograme.defaultConfig (school, infoName, textConfig, intConfig, userName) values (#{school}, #{infoName}, #{textConfig}, #{intConfig}, #{userName})")
    void insertDefaultConfig(M_DefaultConfig mDefaultConfig);

    @Delete("delete from miniprograme.defaultConfig where school = #{school} and infoName = #{infoName}")
    void deleteByInfoNameAndSchool(String infoName, String school);

    @Insert("insert into  miniprograme.defaultConfig (infoName,intConfig,school) values (#{infoName},#{intConfig},#{school})")
    void insertIntConfig(String infoName, Integer intConfig, String school);

    @Select("select * from miniprograme.defaultConfig where school = #{school} and userName = #{userName}")
    List<M_DefaultConfig> getContentBySchoolUserName(String school, String userName);

    @Update("update miniprograme.defaultConfig set textConfig = #{textConfig}, intConfig = #{intConfig} where infoName = #{infoName} and school = #{school} and userName = #{userName}")
    void updateDefaultConfig(M_DefaultConfig mDefaultConfig);

    @Delete("delete from miniprograme.defaultConfig where school = #{school} and userName = #{userName}")
    void deleteBySchoolUserName(M_HomeworkDefaultParamsVO mHomeworkDefaultParamsVO);

    @Select("select intConfig from miniprograme.defaultConfig where school = #{school} and userName = #{teacherName} and infoName = #{infoName}")
    List<Integer> selectRankIntConfigList(String school, String teacherName,String infoName);

    @Select("select textConfig from miniprograme.defaultConfig where school = #{school} and infoName = #{infoName}")
    List<String> getAllSemeter(String infoName, String school);
}
