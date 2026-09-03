package flyfish.service.impl;

import flyfish.mapper.M_CertificationMapper;
import flyfish.pojo.DTO.M_ImageUrlDTO;
import flyfish.pojo.M_Certification;
import flyfish.service.M_CertificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
@Slf4j
public class M_CertificationServiceImpl implements M_CertificationService {
    @Autowired
    private M_CertificationMapper m_certificationMapper;

    //根据图片URL删除证书信息
    @Override
    public String deleteImageByIdAndUrl(M_ImageUrlDTO mImageUrlDTO) {
        String imageUrl = mImageUrlDTO.getImageUrl();
        Integer id = mImageUrlDTO.getId();


        String originalUrl = m_certificationMapper.getImageUrlById(id);
        if(originalUrl == null || originalUrl.isEmpty()){
             return "该项目不存在图片";
        }

        List<String> originalUrls = Arrays.asList(originalUrl.split(";"));
        // 2. 转换为可修改的 ArrayList（关键修复步骤）
        originalUrls= new ArrayList<>(originalUrls);
        for(String url : originalUrls){
            if(url.equals(imageUrl)){
                originalUrls.remove(url);
                String updatedUrl = String.join(";", originalUrls);
                m_certificationMapper.updateImageUrlById(id, updatedUrl);
                return "删除成功";
            }
        }

        return "未找到匹配的图片URL";
    }

    /**
     * 根据id列表标记重复项
     * @param idList
     * @return
     */
    @Override
    public String markDuplicateByIds(List<Integer> idList) {

        if(idList == null || idList.isEmpty()){
            return "没有提供ID列表";
        }else if(idList.size() == 1){
            return "只有一个ID，无法标记为重复项";
        }else{
            String judgeRepeat = "是";
            Integer repeatId = idList.get(0); // 以第一个ID为基准
            for(Integer id : idList){
                m_certificationMapper.markDuplicateById(id,judgeRepeat,repeatId);
            }
            return "标记重复项成功";
        }

    }

    /**
     * 根据id列表取消标记重复项
     * @param idList
     * @return
     */
    @Override
    public String unmarkDuplicateByIds(List<Integer> idList) {


        //编历列表
        for(Integer id : idList){
            Integer repeatId = id; // 以当前ID为基准
            List<M_Certification> duplicateItems = m_certificationMapper.getCertificationsByRepeatId(repeatId);
            //如果dupliticateItems有值，则说明这个是基准项目，那么就把剩余的项目遍历，并且基准项改为另一个
            //同时把这个基准项目的judgeRepeat改成“否”，repeatId改成null
            if(duplicateItems != null && !duplicateItems.isEmpty()){
                //找到一个新的基准项
                List<M_Certification> newDuplicateItems = new ArrayList<>();
                for (M_Certification mCertification : duplicateItems) {
                    if(mCertification.getId().equals(id)){
                        m_certificationMapper.markDuplicateById(id,"否",null);
                    }else {
                        newDuplicateItems.add(mCertification);
                    }
                }
                if(newDuplicateItems != null && !newDuplicateItems.isEmpty()){
                    Integer newRepeatId = newDuplicateItems.get(0).getId();
                    for (M_Certification mCertification : newDuplicateItems) {
                        m_certificationMapper.markDuplicateById(mCertification.getId(),"是",newRepeatId);
                    }
                }

            }else {

                //把这个基准项目的judgeRepeat改成“否”，repeatId改成null
                m_certificationMapper.markDuplicateById(repeatId,"否",null);
            }





        }


        return "取消标记重复项成功";
    }
}
