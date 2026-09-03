package flyfish.mapper;

import flyfish.pojo.RoleMenu;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface RoleMenuMapper {


    /**
     * 根据角色ID获取菜单ID
     * @param roleId
     * @return
     */
    List<RoleMenu> getByRoleId(Integer roleId);
}
