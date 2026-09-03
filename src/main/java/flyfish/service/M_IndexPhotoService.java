package flyfish.service;

import flyfish.pojo.VO.M_BackGroundVO;

import java.util.List;

public interface M_IndexPhotoService {
    //根据位置获取图片对象
    List<M_BackGroundVO> getPhotoimg(String position, String school);

    String addPhotoimg(String url, String position, String school, String name);

    String deletePhotoimg(List<Integer> ids);
}
