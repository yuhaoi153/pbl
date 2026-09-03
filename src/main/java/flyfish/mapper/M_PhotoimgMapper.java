package flyfish.mapper;

import flyfish.pojo.VO.M_BackGroundVO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface M_PhotoimgMapper {

    //根据位置获取图片对象

    List<M_BackGroundVO> getPhotoimg(String position, String school);

    void addPhotoimg(String url, String position, String school, String name);

    void deletePhotoimg(List<Integer> ids);
}
