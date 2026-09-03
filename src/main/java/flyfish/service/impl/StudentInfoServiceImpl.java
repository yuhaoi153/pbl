//package flyfish.service.impl;
//
//import flyfish.mapper.StudentInfoMapper;
//import flyfish.service.StudentInfoService;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//
//import java.util.List;
//
//@Service
//public class StudentInfoServiceImpl implements StudentInfoService {
//    @Autowired
//    private StudentInfoMapper studentInfoMapper;
//    /**
//     * 根据班级查询姓名
//     * @param classNumber
//     * @return
//     */
//    @Override
//    public List<String> getNameByClass(String classNumber) {
//        studentInfoMapper.getallName(classNumber);
//        return studentInfoMapper.getallName(classNumber);
//    }
//}
