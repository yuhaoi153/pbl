package flyfish.mapper;

import flyfish.pojo.M_BehaviorTag;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface M_StudentPerformLabelMapper {

    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertBehaviorTag(M_BehaviorTag behaviorTag);

    @Select("select label from miniprograme.studentPerformLabel where school = #{school} and wellBad = #{wellBad} and educationType = #{educationType}")
    List<String> getBehaviorTags(String school, String wellBad, String educationType);

    //删除行为标签
    //这里需要注意一下，label这里是tag，label是wellbad
    @Delete("delete from miniprograme.studentPerformLabel where school = #{school} and label = #{tag} and wellBad = #{type}")
    void deleteBehaviorTag(String school, String tag, String type);
}

