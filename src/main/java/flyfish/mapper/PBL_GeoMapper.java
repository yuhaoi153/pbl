package flyfish.mapper;


import flyfish.pojo.Menu;
import flyfish.pojo.VO.M_BackGroundVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface PBL_GeoMapper {
    @Select("select password from pbl.user where username = #{username} and school =#{school}")
    String getPassword(String username, String school);

    @Select("select role_id from pbl.user where username = #{username} and school =#{school}")
    Integer getRoleId(String username, String school);

    @Select("select * from pbl.menu where roleId = #{roleId}")
    List<Menu> getMenuByRoleId(Integer roleId);

    @Select("select * from pbl.photo where position = #{position} and school = #{school}")
    List<M_BackGroundVO> getImageUrlList(String position, String school);

    @Select("select id from pbl.user where username = #{username} and school = #{school}")
    Integer getUserId(String username, String school);
}
