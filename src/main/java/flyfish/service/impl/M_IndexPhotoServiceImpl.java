package flyfish.service.impl;

import flyfish.mapper.M_PhotoimgMapper;
import flyfish.pojo.VO.M_BackGroundVO;
import flyfish.service.M_IndexPhotoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class M_IndexPhotoServiceImpl implements M_IndexPhotoService {

    @Autowired
    private M_PhotoimgMapper MPhotoimgMapper;
    //根据位置获取图片对象
    @Override
    public List<M_BackGroundVO> getPhotoimg(String position, String school) {
        log.info("获取{}{}图片url",position,school);
        List<M_BackGroundVO> MBackGroundVOS = MPhotoimgMapper.getPhotoimg(position,school);
        return MBackGroundVOS;
    }

    @Override
    public String addPhotoimg(String url, String position, String school, String name) {
        if(url ==null || url.equals("")){
            return "url不能为空";
        }else {
            MPhotoimgMapper.addPhotoimg(url,position,school,name);
            return "添加成功";
        }
    }

    @Override
    public String deletePhotoimg(List<Integer> ids) {
        if(ids==null || ids.size()==0){
            return "ids不能为空";
        }else {
            MPhotoimgMapper.deletePhotoimg(ids);
            return "删除成功";
        }

    }
}
