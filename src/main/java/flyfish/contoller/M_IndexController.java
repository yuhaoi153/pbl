package flyfish.contoller;

import flyfish.pojo.DTO.M_DeleteImageDTO;
import flyfish.pojo.M_Login;
import flyfish.pojo.VO.M_BackGroundVO;
import flyfish.service.M_IndexPhotoService;
import flyfish.service.M_LoginService;
import flyfish.utils.AliOSSUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@RestController
@Slf4j
public class M_IndexController {
    @Autowired
    private AliOSSUtils aliOSSUtils;
    @Autowired
    private M_LoginService mLoginService;

    @Autowired
    private M_IndexPhotoService indexPhotoService;
    //首页自动获取图片url
    @GetMapping("/mpi/index/getimg")
    public List<M_BackGroundVO> getimg(String position, String school){
        log.info("获取{}和{}图片url",position,school);
        List<M_BackGroundVO> MBackGroundVOS = indexPhotoService.getPhotoimg(position,school);
        return MBackGroundVOS;

    }

    //新增图片
    @PostMapping("/mpi/index/addimg")
    public String addimg(@RequestParam("image") MultipartFile image,
                         @RequestParam("position") String position,
                         @RequestParam("school") String school,
                         @RequestParam("name") String name
                         ) throws IOException {
        log.info("上传的照片信息为：{}",image,position,school,name);
        String url = aliOSSUtils.upload(image);
        log.info("url为{} ",url);
        String resp = indexPhotoService.addPhotoimg(url,position,school,name);
        return resp;
    }

    //批量删除图片
    @PostMapping("/mpi/index/deleteimg")
    public String deleteimg(@RequestBody M_DeleteImageDTO deleteImageDTO){
        log.info("删除{}和{}图片",deleteImageDTO.getId(),deleteImageDTO.getSchool());
        List<Integer> ids = new ArrayList<>();
        ids.add(deleteImageDTO.getId());
        String resp = indexPhotoService.deletePhotoimg(ids);
        return resp;

    }

    //登录操作
    @PostMapping(value = "/mpi/index/login", produces = "application/json;charset=UTF-8")
    public M_Login login(@RequestBody M_Login m_login) {
        log.info("登录的用户名和密码还有手机号{}{}{}", m_login.getUsername(), m_login.getPassword(), m_login.getPhone());
        M_Login res = mLoginService.login(m_login);
        //打印res
        System.out.println(res);
        return res;


    }










}
