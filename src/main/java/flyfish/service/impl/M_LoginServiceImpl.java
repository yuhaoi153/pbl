package flyfish.service.impl;

import flyfish.mapper.*;
import flyfish.pojo.M_Login;
import flyfish.pojo.M_StudentInfo;
import flyfish.service.M_LoginService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class M_LoginServiceImpl implements M_LoginService {
    @Autowired
    private M_UserMapper mUserMapper;
    @Autowired
    private M_TeacherRoleMapper mTeacherRoleMapper;
    @Autowired
    private M_ClassTeacherRelationMapper mClassTeacherRelationMapper;
    @Autowired
    private M_StudentInfoMapper mStudentInfoMapper;
    @Autowired
    private  M_TeacherListMapper mTeacherListMapper;

    @Override
    public M_Login login(M_Login mLogin) {
        List<M_Login> respLoginList = mUserMapper.login(mLogin);
        if (respLoginList != null && !respLoginList.isEmpty()) {
            for (M_Login respLogin : respLoginList) {
            //判断mLogin中的phone是否在respLogin中存在,respLogin可能直接存储手机号，或者多个手机号用;隔开
            if (respLogin.getPhone() != null && !respLogin.getPhone().isEmpty()) {

                String userPhones = respLogin.getPhone();
                String loginPhone = mLogin.getPhone();
                if(userPhones.contains(loginPhone)){

                    respLogin.setUsername(respLogin.getName());
                    if(respLogin.getRole().equals("教师")) {
                        //拿到所有的班级
                        List<String> teacherClassNameList = mClassTeacherRelationMapper.getClassNameBySchoolTeacherName(respLogin.getSchool(),respLogin.getUsername());
                        if(teacherClassNameList != null && !teacherClassNameList.isEmpty()) {
                            respLogin.setTeacherClassNameList(teacherClassNameList);
                        }

                        //拿到教师的学科
                        String  subject = mTeacherListMapper.getSubjectBySchoolAndTeacherName(respLogin.getSchool(),respLogin.getUsername());
                        if(subject != null && !subject.isEmpty()) {
                            respLogin.setSubject(subject);
                        }


                        String headTeacher = "是";
                        String headTeacherClassName = mClassTeacherRelationMapper.getClassNameBySchoolTeacherNameAndTitle( respLogin.getSchool(),respLogin.getUsername(),headTeacher);
                        if(headTeacherClassName != null && !headTeacherClassName.isEmpty()) {
                            respLogin.setHeadTeacherClassName(headTeacherClassName);
                        }

                        List<String> titleList = mTeacherRoleMapper.getTitleListByUserId(respLogin.getId());
                        if (titleList != null && !titleList.isEmpty()) {
                            titleList.add("教师");
                            String role =  String.join(";",titleList);
                            respLogin.setRole(role);// 将职称列表用分号连接成一个字符串
                        }
                    }
                    if(respLogin.getRole().equals("学生")) {
                        M_StudentInfo mStudentInfo = mStudentInfoMapper.getAllByUserId(respLogin.getId());
                        String studentClassName = mStudentInfo.getGrade().substring(0,1) + "(" + mStudentInfo.getClassName() + ")班";
                        respLogin.setStudentClassName(studentClassName);
                    }

                    respLogin.setStatus("success");
                    return respLogin;
                }
            }
            }
            M_Login login = new M_Login();
            login.setStatus("phoneError" );
            return login;
        }


        else {
            M_Login login = new M_Login();
            login.setStatus("error");
            return login;
        }

    }

    @Override
    public String confirmUser(String userName, String password, String phone, String school) {
        List<M_Login> respLoginList = mUserMapper.confirmUser(userName, password, phone, school);
        if (respLoginList != null && !respLoginList.isEmpty()) {
            return "success";
        }
        return "failure";
    }

}
