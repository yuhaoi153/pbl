package flyfish.service;

import flyfish.pojo.DTO.M_ImageUrlDTO;

import java.util.List;

public interface M_CertificationService {
    //根据图片URL删除证书信息
    String deleteImageByIdAndUrl(M_ImageUrlDTO mImageUrlDTO);

    //根据id列表标记重复项
    String markDuplicateByIds(List<Integer> idList);

    //根据id列表取消标记重复项
    String unmarkDuplicateByIds(List<Integer> idList);
}
