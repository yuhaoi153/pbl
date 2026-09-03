package flyfish.contoller;

import flyfish.mapper.Travel_Mapper;
import flyfish.pojo.DTO.*;
import flyfish.service.Travel_Service;
import flyfish.utils.AliOSSUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@RestController
@Slf4j
public class Travel_Controller {
    @Autowired
    Travel_Service travel_service;
    @Autowired
    private AliOSSUtils aliOSSUtils;

    //分别做增删改查
    //新增美景推荐
    @PostMapping("/mpi/travel/addScenery")
    public void addScenery(@RequestBody TravelSceneryDTO  travelSceneryDTO){
        log.info("新增美景推荐的参数{}",travelSceneryDTO);
        travel_service.addScenery(travelSceneryDTO);

    }

    //移动景点的顺序，通过调换上一个景点和本景点的顺序来实现
    @PostMapping("/mpi/travel/moveSceneryup")
    public void moveScenery(@RequestBody TravelSceneryDTO travelSceneryDTO){
        log.info("移动景点的参数{}",travelSceneryDTO);
        travel_service.moveScenery(travelSceneryDTO);
    }
    //再增加一个向下移动的方式
    @PostMapping("/mpi/travel/moveScenerydown")
    public void moveSceneryDown(@RequestBody TravelSceneryDTO travelSceneryDTO) {
        log.info("向下移动景点的参数{}", travelSceneryDTO);
        travel_service.moveScenerydown(travelSceneryDTO);
    }

    //查询美景信息
    @GetMapping("/mpi/travel/queryScenery")
    public List<TravelSceneryDTO> queryScenery(String pageName, String target){
        log.info("查询美景信息的参数pageName={},target={}",pageName,target);
        //调用service层查询方法
        List<TravelSceneryDTO> travelSceneryDTOList =  travel_service.queryScenery(pageName,target);
        return travelSceneryDTOList;
    }

    //更新美景信息
    @PostMapping("/mpi/travel/updateScenery")
    public void updateScenery(@RequestBody TravelSceneryDTO travelSceneryDTO) {
        log.info("更新美景信息的参数{}", travelSceneryDTO);
        travel_service.updateScenery(travelSceneryDTO);
    }

    //删除美景信息
    @GetMapping("/mpi/travel/deleteScenery")
    public void deleteScenery(String id){
        travel_service.deleteScenery(id);
    }



    //新增食品推荐
    @PostMapping("/mpi/travel/addDeliciousFood")
    public void addDeliciousFood(@RequestBody TravelDeliciousFood travelDeliciousFood){
        log.info("新增美食推荐的参数{}",travelDeliciousFood);
        travel_service.addDeliciousFood(travelDeliciousFood);

    }

    //查询食品信息
    @GetMapping("/mpi/travel/queryDeliciousFood")
    public List<TravelDeliciousFood> queryDeliciousFood(String pageName, String target){
        log.info("查询美食信息的参数pageName={},target={}",pageName,target);
        //调用service层查询方法
        List<TravelDeliciousFood> travelDeliciousFoodList =  travel_service.queryDeliciousFood(pageName,target);
        return travelDeliciousFoodList;
    }

    //更新食品信息
    @PostMapping("/mpi/travel/updateDeliciousFood")
    public void updateDeliciousFood(@RequestBody TravelDeliciousFood travelDeliciousFood) {
        log.info("更新美食信息的参数{}",travelDeliciousFood);
        travel_service.updateDeliciousFood(travelDeliciousFood);
    }

    //删除食品信息
    @GetMapping("/mpi/travel/deleteDeliciousFood")
    public void deleteDeliciousFood(String id){
        travel_service.deleteDeliciousFood(id);
    }



    //新增评论功能
    @PostMapping("/mpi/travel/addCommentFunction")
    public void addCommentFunction(@RequestBody TravelCommentFunctionDTO travelCommentFunctionDTO){
        log.info("新增评论的参数{}",travelCommentFunctionDTO);
        travel_service.addTravelCommnetFunction(travelCommentFunctionDTO);

    }

    //查询评论功能
    @GetMapping("/mpi/travel/queryCommentFunction")
    public List<TravelCommentFunctionDTO> queryCommentFunction(String pageName, String target){
        log.info("查询评论的参数pageName={},target={}",pageName,target);
        //调用service层查询方法
        List<TravelCommentFunctionDTO> travelCommentFunctionList =  travel_service.queryCommentFunction(pageName,target);
        return travelCommentFunctionList;
    }

    //更新评论内容
    @PostMapping("/mpi/travel/updateCommentFunction")
    public void updateCommentFunction(@RequestBody TravelCommentFunctionDTO travelCommentFunctionDTO) {
        log.info("更新评论的参数{}",travelCommentFunctionDTO);
        travel_service.updateCommentFunction(travelCommentFunctionDTO);
    }

    //删除评论功能
    @GetMapping("/mpi/travel/deleteCommentFunction")
    public void deleteCommentFunction(String id){
        travel_service.deleteCommentFunction(id);
    }









    //接下来分别完成对recommend的增删改查
    @PostMapping("/mpi/travel/addRecommend")
    public void addRecommend(@RequestBody TravelRecommendDTO travelRecommendDTO ){
        travel_service.addRecommend(travelRecommendDTO);
    }

    //查询推荐信息
    @GetMapping("/mpi/travel/queryRecommend")
    public List<TravelRecommendDTO> queryRecommend(String pageName, String target){
        log.info("查询推荐信息的参数pageName={},target={}",pageName,target);
        List<TravelRecommendDTO> recommendList = travel_service.queryRecommend(pageName, target);
        return recommendList;
    }

    //更新推荐信息
    @PostMapping("/mpi/travel/updateRecommend")
    public void updateRecommend(@RequestBody TravelRecommendDTO travelRecommendDTO){
        log.info("更新推荐信息的参数{}",travelRecommendDTO);
        travel_service.updateRecommend(travelRecommendDTO);
    }

    //删除推荐信息
    @GetMapping("/mpi/travel/deleteRecommend")
    public void deleteRecommend(String id){
        log.info("删除推荐信息的参数id={}",id);
        travel_service.deleteRecommend(id);
    }

    //生成图片url
    @PostMapping("/mpi/travel/uploadimg")
    public String uploadimage(@RequestParam("image") MultipartFile image) throws IOException {
        log.info("上传的照片信息为：{}",image);
        String url = aliOSSUtils.upload(image);
        log.info("url为{} ",url);
        return url;
    }

    //新增图片信息
    @PostMapping("/mpi/travel/addImage")
    public void addImage(@RequestBody TravelImageRecordDTO  travelImageRecordDTO){
        log.info("新增图片信息的参数{}",travelImageRecordDTO);
        //调用service层方法
        travel_service.addImage(travelImageRecordDTO);

    }

    //查询图片信息
    @GetMapping("/mpi/travel/queryImage")
    public List<TravelImageRecordDTO> queryImage(String pageName, String target,String type){
        log.info("查询图片信息的参数pageName={},target={},type{}",pageName,target,type);
        List<TravelImageRecordDTO> imageList = travel_service.queryImage(pageName, target,type);
        return imageList;
    }



    //删除图片信息
    @GetMapping("/mpi/travel/deleteImage")
    public void deleteImage(String id){
        log.info("删除图片信息的参数id={}",id);
        travel_service.deleteImage(id);
    }



    // 查询高德每日路线地图
    @PostMapping("/mpi/travel/getAmapDayRoute")
    public AmapRouteResponseDTO getAmapDayRoute(@RequestBody AmapRouteRequestDTO requestDTO) {
        log.info("查询高德路线参数{}", requestDTO);
        return travel_service.getAmapDayRoute(requestDTO);
    }

    // 查询附近厕所
    @GetMapping("/mpi/travel/searchNearbyToilets")
    public List<TravelToiletDTO> searchNearbyToilets(Double lon, Double lat, Integer radius) {
        log.info("查询附近厕所 lon={}, lat={}, radius={}", lon, lat, radius);
        return travel_service.searchNearbyToilets(lon, lat, radius);
    }

    // 景点上移
    @GetMapping("/mpi/travel/moveSceneryup")
    public void moveSceneryup(Integer id) {
        log.info("景点上移 id={}", id);
        travel_service.moveSceneryup(id);
    }

    // 景点下移
    @GetMapping("/mpi/travel/moveScenerydown")
    public void moveScenerydown(Integer id) {
        log.info("景点下移 id={}", id);
        travel_service.moveScenerydown(id);
    }

    @GetMapping("/mpi/travel/queryPassword")
    public void queryPassword(String password,String functionButton){
        log.info("查询密码的参数password={},functionButton= {}",password,functionButton);
        travel_service.queryPassword(password,functionButton);

    }




    //查询彩云天气的key和secrect
//    @GetMapping("/mpi/travel/getWeatherInfo")
//    public List<String> getWeatherInfo(){
//        List<String> KeySecret = new ArrayList<>();
//        KeySecret.add("fgebgqsdyv1649yj");
//        KeySecret.add("dniM1pkv1bKRFFHG4FwoHUBvhb1Dn7wM");
//        return KeySecret;
//    }



}
