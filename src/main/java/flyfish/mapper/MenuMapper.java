package flyfish.mapper;

import flyfish.pojo.Menu;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface MenuMapper {

    /**
     * 根据ID选择所有的菜单项
     * @param menuIdList
     * @return
     */
    List<Menu> getByIds(List<Integer> menuIdList);
}
