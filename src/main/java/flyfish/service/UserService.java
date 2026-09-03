package flyfish.service;

import flyfish.pojo.User;
import flyfish.pojo.UserName;
import flyfish.pojo.VO.UserNameVO;

import java.util.List;

public interface UserService {
    /**
     * 查找用户id
     * @param username
     * @return
     */
    Integer login(String username);

    /**
     * 查找所有的用户名
     * @return
     */
    List<UserNameVO> getAllName();

    /**
     * 拿到真实的密码
     * @param username
     * @return
     */
    String getRealPassword(String username);

    /**
     * 查询所有内容
     * @return
     */
    List<User> getALLContent();

    /**
     * 修改用户信息
     * @param editForm
     * @return
     */
    String editClassPassword(User editForm);

    /**
     * 删除某个用户
     * @param id
     */
    void deleteClassPassword(Integer id);

    /**
     * 新增用户
     * @param newUser
     * @return
     */
    User addclassPassword(User newUser);
}
