package flyfish.service;

import flyfish.pojo.M_Login;

public interface M_LoginService {
    M_Login login(M_Login mLogin);

    String confirmUser(String userName, String password, String phone, String school);
}
