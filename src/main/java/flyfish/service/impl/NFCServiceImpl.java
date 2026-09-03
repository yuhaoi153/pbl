//package flyfish.service.impl;
//
//import flyfish.exception.NullNameListException;
//import flyfish.mapper.AccumulateScoreMapper;
//import flyfish.mapper.PerformMapper;
//import flyfish.mapper.StudentInfoMapper;
//import flyfish.pojo.Perform;
//import flyfish.pojo.StudentInfo;
//import flyfish.service.NFCService;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//
//import java.time.LocalDate;
//import java.time.LocalDateTime;
//import java.util.List;
//
//@Service
//public class NFCServiceImpl implements NFCService {
//
//    @Autowired
//    private PerformMapper performMapper;
//    @Autowired
//    private StudentInfoMapper studentInfoMapper;
//    @Autowired
//    private AccumulateScoreMapper accumulateScoreMapper;
//    /**
//     *  发送NFc表扬信息
//     * @param classNumber
//     * @param subject
//     * @param name
//     * @param situation
//     */
//    @Override
//    public void sendWellNFC(String classNumber, String subject, String name, String situation) {
//
//        //首先确认是不是本班学生
//        List<StudentInfo> allContentByCLassName = studentInfoMapper.getAllContentByCLassName(classNumber, name);
//        if(allContentByCLassName.size() == 0 || allContentByCLassName == null){
//            throw new NullNameListException("没有这个学生");
//
//        }else {
//
//        Perform perform = new Perform();
//        perform.setClassNumber(classNumber);
//        perform.setSubject(subject);
//        perform.setName(name);
//        perform.setSituation(situation);
//        String reason = "课堂表现优秀";
//        LocalDate checkdate = LocalDate.now();
//        perform.setCheckdate(checkdate);
//        perform.setReason(reason);
//        Integer score = 1;
//        perform.setScore(score);
//
//        performMapper.insertByNFC(perform);
//        accumulateScoreMapper.updateNFCwellScore( name, classNumber,subject);
//
//
//        }
//
//    }
//}
