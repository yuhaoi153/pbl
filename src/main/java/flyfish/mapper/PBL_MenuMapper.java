package flyfish.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface PBL_MenuMapper {
    @Update("update pbl.menu set status = 'active' where roleId = #{roleId} and component = #{menuName}")
    void unlock(Integer roleId, String menuName, String school);

    @Update("update pbl.menu set status = 'inactive' where  roleId = #{roleId} ")
    void lockAll(Integer roleId, String school);

    @Select("select status from pbl.menu where roleId = #{roleId} and component = #{menuName}")
    String getStatus(Integer roleId,String menuName);
}
