package flyfish.service.impl;

import flyfish.exception.UserNotFoundException;
import flyfish.mapper.MenuMapper;
import flyfish.mapper.RoleMenuMapper;
import flyfish.mapper.UserMapper;
import flyfish.pojo.Menu;
import flyfish.pojo.RoleMenu;
import flyfish.pojo.User;
import flyfish.service.LoginRouterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class LoginRouterServiceImpl implements LoginRouterService {


    @Autowired
    private MenuMapper menuMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private RoleMenuMapper roleMenuMapper;

    @Override
    public List getdata(String username) {
        User user = userMapper.getByName(username);
        List<Menu> menuList = new ArrayList<>();
        if(user != null){
            Integer roleId = user.getRoleId();
            List<RoleMenu> roleMenuList = roleMenuMapper.getByRoleId(roleId);
            List<Integer> menuIdList = new ArrayList<>();
            roleMenuList.forEach(roleMenu -> {
                menuIdList.add(roleMenu.getMenuId());
            });
            menuList = menuMapper.getByIds(menuIdList);
        }else {
            throw new UserNotFoundException("用户不存在"+username);
        }

        return menuList;
    }






}
