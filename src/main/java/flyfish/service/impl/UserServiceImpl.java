package flyfish.service.impl;

import flyfish.mapper.UserMapper;
import flyfish.pojo.User;
import flyfish.pojo.UserName;
import flyfish.pojo.VO.UserNameVO;
import flyfish.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;
    /**
     * 查找用户id
     */
    public Integer login(String username) {
        Integer userId = userMapper.geyByusername(username);
        return userId;
    }

    /**
     * 查找所有的用户名
     * @return
     */
    @Override
    public List<UserNameVO> getAllName() {
        List<UserName> userNameList = userMapper.getAllName();
        List<UserNameVO> userNameVOList = new ArrayList<>();
        for (UserName username: userNameList
             ) {
            UserNameVO userNameVO = new UserNameVO();
            userNameVO.setValue(username.getUsername());
            userNameVO.setLabel(username.getName());
            userNameVOList.add(userNameVO);
        }
        return userNameVOList;
    }


    /**
     * 拿到真实的密码
     * @param username
     * @return
     */
    @Override
    public String getRealPassword(String username) {

        String password = userMapper.getRealPassword(username);
        return password;
    }

    /**
     * 查询所有内容
     * @return
     */
    @Override
    public List<User> getALLContent() {



        return userMapper.getALL();
    }

    /**
     * 修改用户信息
     * @param editForm
     * @return
     */
    @Override
    public String editClassPassword(User editForm) {
         userMapper.editclassPassword(editForm);
         String result = editForm.getUsername();
        return result;
    }

    /**
     * 删除某个用户
     * @param id
     */
    @Override
    public void deleteClassPassword(Integer id) {
        userMapper.deleteClassPassword(id);
    }

    /**
     * 新增用户
     * @param newUser
     * @return
     */
    @Override
    public User addclassPassword(User newUser) {
        Integer id = userMapper.addclassPassword(newUser);
        newUser.setId(id);
        return newUser;
    }
}
