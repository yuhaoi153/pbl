package flyfish.contoller;


import flyfish.pojo.DTO.M_UpdateSemesterDTO;
import flyfish.pojo.VO.M_UpdateTableVO;

import flyfish.service.M_CheckSemesterService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@Slf4j
public class M_CheckSemesterController {

    @Autowired
    private M_CheckSemesterService m_checkSemesterService;




    //首先查询三个数据库中该学校分别有多少个table，找出其中有year字段的table，同时查询是不是已经更新过
    @GetMapping("/mpi/semester/getAllTable")
    public List<M_UpdateTableVO> getAllTable(String school){
        //查询三个数据库中该学校分别有多少个table，找出其中有year字段的table，同时查询是不是已经更新过
        log.info("查询所有表接口被调用，参数：school={}", school);
        List<M_UpdateTableVO> updateTableVOList = m_checkSemesterService.getAllTable(school);
        return updateTableVOList;
    }

    //更新年级，并记录到数据库中

    //
    @PostMapping("/mpi/semester/updateSemester")
    public List<String> updateSemester(@RequestBody List<M_UpdateSemesterDTO> updateSemesterDTOList){
        log.info("更新年级接口被调用，参数：updateSemesterDTOList={}", updateSemesterDTOList);
        List<String> resp = m_checkSemesterService.updateSemester(updateSemesterDTOList);
        return resp;

    }


}
