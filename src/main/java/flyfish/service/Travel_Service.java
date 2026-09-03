package flyfish.service;

import flyfish.pojo.DTO.*;

import java.util.List;

public interface Travel_Service {
    void addScenery(TravelSceneryDTO travelSceneryDTO);

    List<TravelSceneryDTO> queryScenery(String pageName, String target);

    void updateScenery(TravelSceneryDTO travelSceneryDTO);

    void deleteScenery(String id);

    void addDeliciousFood(TravelDeliciousFood travelDeliciousFood);

    List<TravelDeliciousFood> queryDeliciousFood(String pageName, String target);

    void updateDeliciousFood(TravelDeliciousFood travelDeliciousFood);

    void deleteDeliciousFood(String id);

    void addRecommend(TravelRecommendDTO travelRecommendDTO);

    List<TravelRecommendDTO> queryRecommend(String pageName, String target);


    void updateRecommend(TravelRecommendDTO travelRecommendDTO);

    void deleteRecommend(String id);

    void addImage(TravelImageRecordDTO travelImageRecordDTO);

    List<TravelImageRecordDTO> queryImage(String pageName, String target, String type);

    void deleteImage(String id);

    void moveScenery(TravelSceneryDTO travelSceneryDTO);

    void moveScenerydown(TravelSceneryDTO travelSceneryDTO);
    AmapRouteResponseDTO getAmapDayRoute(AmapRouteRequestDTO requestDTO);

    List<TravelToiletDTO> searchNearbyToilets(Double lon, Double lat, Integer radius);

    void moveSceneryup(Integer id);

    void moveScenerydown(Integer id);

    void addTravelCommnetFunction(TravelCommentFunctionDTO travelCommentFunctionDTO);

    List<TravelCommentFunctionDTO> queryCommentFunction(String pageName, String target);

    void updateCommentFunction(TravelCommentFunctionDTO travelCommentFunctionDTO);

    void deleteCommentFunction(String id);

    void queryPassword(String password, String functionButton);
}
