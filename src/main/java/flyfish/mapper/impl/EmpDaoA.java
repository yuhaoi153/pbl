package flyfish.mapper.impl;

import flyfish.mapper.Empdao;
import flyfish.pojo.Emp;
import flyfish.utils.XmlParserUtils;
import org.springframework.stereotype.Repository;

import java.util.List;

//@Component  //将当前类交给Ioc容器管理，控制器转移，生成bean对象-控制反转
@Repository
public class EmpDaoA implements Empdao {
    @Override
    public List<Emp> listEmp() {
        String file = this.getClass().getClassLoader().getResource("emp.xml").getFile();
        System.out.println(file);

        List<Emp> empList = XmlParserUtils.parse(file, Emp.class);
        return empList;
    }


}
