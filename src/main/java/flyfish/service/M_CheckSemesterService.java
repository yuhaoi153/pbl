package flyfish.service;

import flyfish.pojo.DTO.M_UpdateSemesterDTO;
import flyfish.pojo.VO.M_UpdateTableVO;

import java.util.List;

public interface M_CheckSemesterService {
    List<M_UpdateTableVO> getAllTable(String school);

    List<String> updateSemester(List<M_UpdateSemesterDTO> updateSemesterDTOList);
}
