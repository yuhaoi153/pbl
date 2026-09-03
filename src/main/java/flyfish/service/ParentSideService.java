package flyfish.service;

import flyfish.pojo.DTO.ParentQueryDTO;
import flyfish.pojo.Options;
import flyfish.pojo.VO.ParentPassTaskVO;
import flyfish.pojo.VO.ParentPerformVO;
import flyfish.pojo.VO.ParentRecordVO;

import java.util.List;

public interface ParentSideService {
    /**
     * 自动查询班级号码
     * @return
     */
    List<Options> queryClassNumber();

    /**
     * 检查密码
     * @param parentQueryDTO
     * @return
     */
    String checkPassword(ParentQueryDTO parentQueryDTO);


    /**
     * 查询未过关数据
     * @param parentQueryDTO
     * @return
     */
    List<ParentPassTaskVO> queryPassData(ParentQueryDTO parentQueryDTO);

    /**
     * 家长确认
     * @param ids
     */
    void parentConfirm(List<Integer> ids);

    /**
     * 查询表现数据
     * @param parentQueryDTO
     * @return
     */
    List<ParentPerformVO> queryPerform(ParentQueryDTO parentQueryDTO);
}
