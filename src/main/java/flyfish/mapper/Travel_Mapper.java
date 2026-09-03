package flyfish.mapper;

import flyfish.pojo.DTO.*;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface Travel_Mapper {
    @Insert("insert into travel.scenery (target, destination, ranklevel, detail, supplementary, shortname,  comment, pageName,rankorder) values " +
            "(#{target}, #{destination},#{ranklevel},#{detail},#{supplementary},#{shortname},#{comment},#{pageName},#{order})")
    void addScenery(TravelSceneryDTO travelSceneryDTO);

    @Select("select id, target, destination, ranklevel, detail, supplementary, shortname, " +
            "comment, pageName, rankorder as `order` from travel.scenery " +
            "where pageName = #{pageName} and target = #{target} " +
            "order by rankorder is null, rankorder asc, id asc")
    List<TravelSceneryDTO> queryScenery(String pageName, String target);

    @Select("select coalesce(max(rankorder), 0) from travel.scenery " +
            "where pageName = #{pageName} and target = #{target}")
    Integer queryMaxSceneryOrder(String pageName, String target);

    @Update("update travel.scenery set destination = #{destination}, ranklevel = #{ranklevel}, " +
            "detail = #{detail}, supplementary = #{supplementary}, shortname = #{shortname}, " +
            " comment = #{comment} " +
            "where id=#{id}")
    void updateScenery(TravelSceneryDTO travelSceneryDTO);

    @Delete("delete from travel.scenery where id = #{id}")
    void deleteScenery(String id);

    @Insert("insert into travel.delecacy (target, name, destinationUrl, ranklevel, detail, supplementary, comment, pageName) values " +
            "(#{target}, #{name}, #{destinationUrl}, #{ranklevel}, #{detail}, #{supplementary}, #{comment}, #{pageName})")
    void addDelecacy(TravelDeliciousFood travelDeliciousFood);

    @Select("select * from travel.delecacy where pageName = #{pageName} and target = #{target}")
    List<TravelDeliciousFood> queryDeliciousFoood(String pageName, String target);

    @Update("update travel.delecacy set name = #{name}, destinationUrl = #{destinationUrl}, ranklevel = #{ranklevel}, " +
            "detail = #{detail}, supplementary = #{supplementary}, comment = #{comment} " +
            "where id=#{id}")
    void updateDeliciousFood(TravelDeliciousFood travelDeliciousFood);

    @Delete("delete from travel.delecacy where id = #{id}")
    void deleteDeleciousFood(String id);

    @Insert("insert into travel.recommend(target, type, name, recommendUrl, recommendRank, comment, pageName) VALUES " +
            "(#{target}, #{type}, #{name}, #{recommendUrl}, #{recommendRank}, #{comment}, #{pageName})")
    void addRecommend(TravelRecommendDTO travelRecommendDTO);

    @Select("select * from travel.recommend where pageName = #{pageName} and target = #{target}")
    List<TravelRecommendDTO> queryRecommend(String pageName, String target);

    @Update("update travel.recommend set type = #{type}, name = #{name}, recommendUrl = #{recommendUrl}, " +
            "recommendRank = #{recommendRank}, comment = #{comment} " +
            "where id=#{id}")
    void updateRecommend(TravelRecommendDTO travelRecommendDTO);

    @Delete("delete from travel.recommend where id = #{id}")
    void deleteRecommend(String id);

    @Insert("insert into travel.imageRecord (image_url, name, type, target, pageName) VALUES " +
            "(#{imageUrl},#{name},#{type},#{target},#{pageName})")
    void addImage(TravelImageRecordDTO travelImageRecordDTO);

    @Select("select * from travel.imageRecord where target = #{target} and pageName = #{pageName} and type = #{type}")
    List<TravelImageRecordDTO> queryImage(String pageName, String target, String type);

    @Delete("delete from travel.imageRecord where id = #{id}")
    void deleteImage(String id);

    @Update("update travel.scenery set rankorder = #{order} where id = #{id}")
    void updateOrder(Integer id, Integer order);


    @Select("select id, target, destination, ranklevel, detail, supplementary, shortname, comment, pageName, rankorder as `order` from travel.scenery where id = #{id}")
    TravelSceneryDTO querySceneryById(Integer id);

    @Insert("insert into travel.commentFunction (replyId, target, name, userName, ranklevel, createTime, detail, supplementary, pageName) values "+
            "(#{replyId}, #{target}, #{name}, #{userName}, #{ranklevel}, #{createTime}, #{detail}, #{supplementary}, #{pageName})")
    void addCommentFunction(TravelCommentFunctionDTO travelCommentFunctionDTO);

    @Select("select * from travel.commentFunction where pageName = #{pageName} and target = #{target}")
    List<TravelCommentFunctionDTO> queryCommentFunction(String pageName, String target);

    @Update("update travel.commentFunction set name = #{name}, userName = #{userName}, ranklevel = #{ranklevel},  detail = #{detail}, supplementary = #{supplementary} where id=#{id}")
    void updateCommentFunction(TravelCommentFunctionDTO travelCommentFunctionDTO);

    @Delete("delete from travel.commentFunction where id = #{id}")
    void deleteCommentFunction(String id);

    @Select("select id from travel.passwordTable where password = #{password} and functionButton = #{functionButton}")
    Integer queryPassword(String password, String functionButton);
}
