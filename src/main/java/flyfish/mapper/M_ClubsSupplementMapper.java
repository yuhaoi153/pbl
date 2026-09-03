package flyfish.mapper;

import flyfish.pojo.VO.M_ClubsSupplementVO;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface M_ClubsSupplementMapper {

    @Select("select * from miniprograme.clubsSupplement order by id desc limit 1")
    M_ClubsSupplementVO getCurrentInfo();

    @Delete("delete from miniprograme.clubsSupplement")
    void deleteInfo();

    @Insert("insert into miniprograme.clubsSupplement (admissionSemester, description) values (#{admissionSemester}, #{description})")
    void newInsertClubSupplement(M_ClubsSupplementVO clubsSupplementVO);
}
