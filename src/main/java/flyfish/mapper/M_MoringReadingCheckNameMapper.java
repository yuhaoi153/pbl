package flyfish.mapper;

import flyfish.pojo.VO.M_MoringReadingCheckVO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface M_MoringReadingCheckNameMapper {

    List<M_MoringReadingCheckVO> getReadingCheckList(String weekday, String school);
}
