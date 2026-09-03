package flyfish.contoller;

import flyfish.constant.JwtClaimsConstant;
import flyfish.exception.UserNotFoundException;
import flyfish.exception.WrongPasswordException;
import flyfish.pojo.Menu;
import flyfish.pojo.Result;
import flyfish.pojo.User;
import flyfish.pojo.UserName;
import flyfish.pojo.VO.UserNameVO;
import flyfish.pojo.VO.UserVO;
import flyfish.properties.JwtProperties;
import flyfish.service.LoginRouterService;
import flyfish.service.UserService;
import flyfish.utils.JwtTokenUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@Slf4j
public class LoginRouterController {
    @Autowired
    private LoginRouterService routerService;
    @Autowired
    private UserService userService;
    @Autowired
    private JwtProperties jwtProperties;

    //用于返回menu菜单
    @GetMapping("/tpi/login/menu/{username}")
    public Result<List<Menu>> handlemenu(@PathVariable String username) {
        // 方法实现
        if (username==null){
            throw new UserNotFoundException("用户不存在"+username);
        }
        log.info("参数:{}",username);
        List routerList = routerService.getdata(username);
        return Result.success(routerList); // 示例返回
    }


    //用于检验令牌
    @GetMapping("/tpi/login")
    public Result<String> login(String username,String password) {
        log.info("员工登录：{},{}", username,password);

        Integer userId = userService.login(username);
        //登录成功后，生成jwt令牌
        Map<String, Object> claims = new HashMap<>();
        //把一些明码的信息封装到token中，一方面增加复杂度，另一方面方便从中解析
        claims.put(JwtClaimsConstant.EMP_ID, userId);
        //
        claims.put(JwtClaimsConstant.USERNAME,username);

        //验证密码
        String realPassword = userService.getRealPassword(username);
        if(!password.equals(realPassword)){
            throw new WrongPasswordException("密码/错误，请重新输入");
        }

        String token = JwtTokenUtils.createJWT(
                jwtProperties.getAdminSecretKey(),
                jwtProperties.getAdminTtl(),
                claims);

        return Result.success(token);
    }

    @GetMapping("/tpi/login/autoUsername")
    public Result<List<UserNameVO>> autoName(String noname){
        log.info("自动查询用户名");
        List<UserNameVO> userNameVOList = userService.getAllName();
        return Result.success(userNameVOList);
    }
}
