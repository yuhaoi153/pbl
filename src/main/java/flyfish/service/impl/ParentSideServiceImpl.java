package flyfish.service.impl;

import flyfish.exception.ParentPasswordException;
import flyfish.mapper.ParentPasswordMapper;
import flyfish.mapper.PerformMapper;
import flyfish.mapper.RecordTaskMapper;
import flyfish.mapper.UserMapper;
import flyfish.pojo.DTO.ParentQueryDTO;
import flyfish.pojo.Options;
import flyfish.pojo.VO.ParentPassTaskVO;
import flyfish.pojo.VO.ParentPerformVO;
import flyfish.pojo.VO.ParentRecordVO;
import flyfish.service.ParentSideService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ParentSideServiceImpl implements ParentSideService {

    @Autowired
    private UserMapper userMapper;
    @Autowired
    private ParentPasswordMapper parentPasswordMapper;
    @Autowired
    private RecordTaskMapper recordTaskMapper;
    @Autowired
    private PerformMapper performMapper;

    /**
     * 家长端自动查询班级号码
     * @return
     */
    @Override
    public List<Options> queryClassNumber() {
        List<String> classNumberList =  userMapper.parentQueryClassNumber();
        List<Options> optionsList = new ArrayList<>();
        for (   String classNumber : classNumberList
             ) {
            Options options = new Options();
            options.setValue(classNumber);
            options.setLabel(classNumber);
            optionsList.add(options);
        }
        return optionsList;
    }

    /*
    家长端检查密码
     */
    @Override
    public String checkPassword(ParentQueryDTO parentQueryDTO) {
        String classNumber = parentQueryDTO.getClassNumber();
        String name = parentQueryDTO.getName();
        List<String> realPassword = parentPasswordMapper.getRealPassword(classNumber, name);
        if(realPassword != null && realPassword.size() == 1){
            if(realPassword.get(0).equals(parentQueryDTO.getPassword())){
                parentPasswordMapper.addOneLoginTime(classNumber, name);
                return "success";
            }else{
                throw new ParentPasswordException("密码/用户名错误");
            }
        }else {
            throw new ParentPasswordException("密码/用户名错误");
        }


    }

    /**
     * 家长端查询未过关数据
     * @param parentQueryDTO
     * @return
     */
    @Override
    public List<ParentPassTaskVO> queryPassData(ParentQueryDTO parentQueryDTO) {
        List<ParentPassTaskVO> parentPassTaskVOS = recordTaskMapper.queryPassData(parentQueryDTO);

        return parentPassTaskVOS;
    }

    /**
     * 家长端确认
     * @param ids
     */
    @Override
    public void parentConfirm(List<Integer> ids) {
        recordTaskMapper.parentConfirm(ids);
    }

    /**
     * 家长端查询表现数据
     * @param parentQueryDTO
     * @return
     */
    @Override
    public List<ParentPerformVO> queryPerform(ParentQueryDTO parentQueryDTO) {
        List<ParentPerformVO> parentPerformVOS = performMapper.queryPerform(parentQueryDTO);

        return parentPerformVOS;
    }


}
