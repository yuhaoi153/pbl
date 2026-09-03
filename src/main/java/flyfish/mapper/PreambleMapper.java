package flyfish.mapper;

import flyfish.pojo.Preamble;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface PreambleMapper {

    @Select("select * from autoEvaluation.preamble")
    Preamble getclarification();
}
