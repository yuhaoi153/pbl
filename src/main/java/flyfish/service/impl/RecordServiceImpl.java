package flyfish.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import flyfish.mapper.*;
import flyfish.pojo.AccumulateScore;
import flyfish.pojo.DTO.*;
import flyfish.pojo.M_HomeworkAudio;
import flyfish.pojo.Record;
import flyfish.pojo.VO.PageQueryClassVO;
import flyfish.pojo.VO.PageQueryNameVO;
import flyfish.service.AccumulateScoreService;
import flyfish.service.RecordService;
import flyfish.utils.*;
import lombok.extern.slf4j.Slf4j;
import net.sf.cglib.core.Local;
import org.apache.commons.text.similarity.LevenshteinDistance;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Year;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@Slf4j
public class RecordServiceImpl implements RecordService {

    @Autowired
    private RecordMapper recordMapper;
    @Autowired
    private StudentInfoMapper studentInfoMapper;
    @Autowired
    private DirectMailUtills directMailUtills;
    @Autowired
    private NotificationInfoMapper notificationInfoMapper;
    @Autowired
    private AccumulateScoreService accumulateScoreService;
    @Autowired
    private SendSmsUtills sendSmsUtills;
    @Autowired
    private PerformMapper performMapper;
    @Autowired
    private BaiWenXinUtills baiWenXinUtills;
    @Autowired
    private DeepSeekUtills deepSeekUtills;
    @Autowired
    private AliyunAudioRecognitionUtil aliyunAudioRecognitionUtil;
    @Autowired
    private M_GradeYearMapper gradeYearMapper;
    @Autowired
    private HomeWorkContentMapper homeWorkContentMapper;


    /**
     * 扫码枪上传数据并反馈
     *
     * @param recordDTO
     * @return
     */
    public String uploadFeedback(RecordDTO recordDTO) throws Exception {
        //处理数据（多思考各种异常情况）
        //判断是否初始化:当前日期、班级、作业内容是否已经存在
        String classNumber = recordDTO.getClassNumber();
        String content = recordDTO.getContent();
        LocalDate checkdate = recordDTO.getCheckdate();
        String subject = recordDTO.getSubject();
        String school = recordDTO.getSchool();


        //处理value
        //把recordList转化为value
        String value = String.join("", recordDTO.getValueList());
        if (value.endsWith("--")) {
            value = value.substring(0, value.length() - 2);
        }
        String[] parts = value.split("--");
        List<String> studentNumberList = new ArrayList<>();
        for (String part : parts) {
            String[] numbers = part.split("/");
            if (numbers.length > 0) {
                if (numbers.length > 1) {
                    if (numbers[1].equals(classNumber)) {
                        studentNumberList.add(numbers[0]);
                    }
                }

            }
        }
        //去掉重复的元素
        Set<String> set = new HashSet<>(studentNumberList);
        studentNumberList = new ArrayList<>(set);

        List<String> nameList = new ArrayList<>();
        if (studentNumberList != null && studentNumberList.size() > 0) {
            //获取学号对应的学生姓名
            nameList = studentInfoMapper.getnameList(studentNumberList, classNumber, school);
        } else {
            return "没有扫到任何学生的二维码";
        }
        //再反过来查询一遍学号，避免学号和姓名对不上
        if (nameList != null && nameList.size() > 0) {
            studentNumberList = studentInfoMapper.getnewStudentNumberList(nameList, classNumber, school);
        } else {
            return "没有扫到任何学生的二维码";
        }


        List<Record> recordList = recordMapper.isexist(classNumber, content, checkdate, subject, school);
        List<Record> originrecordList = new ArrayList<>();
        if (recordList != null && recordList.size() > 0) {

        } else {//如果不存在记录
            //给全班的学生、姓名、作业内容、日期初始化，同时完成状态相反化
            List<String> allNameList = studentInfoMapper.getallName(classNumber, school);
            List<String> allstudentNumberList = studentInfoMapper.getallStudentNumber(classNumber, school);
            for (int i = 0; i < allstudentNumberList.size(); i++) {
                Record record = new Record();
                BeanUtils.copyProperties(recordDTO, record);
                record.setStudentNumber(allstudentNumberList.get(i));
                record.setName(allNameList.get(i));
                record.setCreateTime(LocalDateTime.now());
                //反向设置，1代表完成，0代表未完成，扫码登记的同学是完成的，所以初始化的时候设置为未完成
                if (recordDTO.getCompleted() == 1) {
                    record.setCompleted(0);
                } else {
                    record.setCompleted(1);
                }
                originrecordList.add(record);
            }
            ;

            //全部上传到record数据库中
            recordMapper.batchupload(originrecordList);
        }


        //更新数据库
        //开始构造record对象

        Integer completed = recordDTO.getCompleted();//扫码登记的同学是完成的，所以completed传1，反之传0
        LocalDateTime nowtime = LocalDateTime.now();
        Integer level = 0;//0是合格，1是优秀，-1是不达标，null是没有登记完成状态。
        //把所有的不达标的，没有登记的，合格的都完成更改为合格，同时完成状态改变
        recordMapper.updatecompleted(completed, nowtime, content, classNumber, checkdate, studentNumberList, nameList, subject, level, school);
//吧所有的优秀的，完成状态也改变
        recordMapper.updatecompletedLevel1(completed, nowtime, content, classNumber, checkdate, studentNumberList, nameList, subject, school);
        recordMapper.updatecompletedLevelminus1(completed, nowtime, content, classNumber, checkdate, studentNumberList, nameList, subject, school);
        if (completed == 0) {//把所有的优秀的，不达标的，level改为0,把订正状态改为null
            recordMapper.updateUncompleteForLevel1andminus1(nowtime, content, classNumber, checkdate, studentNumberList, nameList, subject, school);
        }//反馈结果
        //将已经初始化过，但是完成状态为null的数据全部更新为相反状态
        Integer changecompleted = 1;
        if (completed == 1) {
            changecompleted = 0;
        }
        recordMapper.updatenull(changecompleted, nowtime, content, classNumber, checkdate, subject, school);


        //再统一更新一次supplemetary
        if(recordDTO.getSupplementary()!= null && !recordDTO.getSupplementary().equals("")){
            String supplementary = recordDTO.getSupplementary();
            recordMapper.updateSupplementary(supplementary,nowtime,content,classNumber,checkdate,subject,school);
        }







        Integer uncompleted = 0;
        List<String> uncompletednameList = recordMapper.getuncompleted(uncompleted, content, classNumber, checkdate, subject, school);
        //没有登记完成作业的同学有：

        if (uncompletednameList != null && uncompletednameList.size() > 0) {
            subject = recordDTO.getSubject();
            String isoDate = checkdate.format(DateTimeFormatter.ISO_LOCAL_DATE);
            String supplementary = recordDTO.getSupplementary();
            String startpage = String.valueOf(recordDTO.getStartpage());
            String endpage = String.valueOf(recordDTO.getEndpage());
            String feedbacknotification = "";
            feedbacknotification += isoDate + "<br>" + subject + "《" + content + "》";
            if (startpage != null && !startpage.isEmpty() && !startpage.equals("null")) {
                feedbacknotification += "P" + startpage + "-" + endpage;
            }
            if (supplementary != null && !supplementary.isEmpty()) {
                feedbacknotification += supplementary + "<br>";
            } else {
                feedbacknotification += "<br>";
            }

            feedbacknotification += "没有登记完成的同学有:" + "<br>";
            feedbacknotification += String.join("、", uncompletednameList);
            return feedbacknotification;
        } else {
            return "全部同学完成了作业";
        }
    }

    /**
     * 短信或者邮件通知老师
     *
     * @param notificationDTO
     * @return
     */
    @Override
    public List<String> notification(NotificationDTO notificationDTO) throws Exception {
        String classNumber = notificationDTO.getClassNumber();
        String subject = notificationDTO.getSubject();
        LocalDate checkdate = notificationDTO.getCheckdate();
        Integer uncompleted = 0;
        String isoDate = checkdate.format(DateTimeFormatter.ISO_LOCAL_DATE);
        String school = notificationDTO.getSchool();

        //通知单科老师的逻辑(考虑到如果没有检索到信息的情况)
        if (notificationDTO.isScopeOfTeacher()) {
            //先查询一次反馈的结果，用于展示在屏幕上
            //先查询有今天本班本学科有哪几种作业，再分别看每个作业没有完成的同学；
            List<String> resultList = new ArrayList<>();
            String feedbacknotification = "";
            String feedbackPhonenotification = "";
//            String feedbacksubject = feedbackgene(uncompleted,classNumber,checkdate,isoDate,subject);
//            feedbacknotification += feedbacksubject;
            feedbacknotification = notificationDTO.getFeedbackText();
            //把所有的/n替换成<br>
            feedbacknotification = feedbacknotification.replaceAll("\n", "<br>");
            feedbackPhonenotification = notificationDTO.getFeedbackPhoneText();
            //把所有的/n替换成<br>
            feedbackPhonenotification = feedbackPhonenotification.replaceAll("\n", "");
//            System.out.println(feedbacknotification);
            System.out.println(feedbackPhonenotification);
            resultList.add(feedbacknotification);

            //生成作业内容对应的未完成作业名单
//            HashMap<String, List<String>> contentUncompletedMap = contentUncompletedMapMethod(uncompleted, classNumber, checkdate, subject);

            boolean checkMail = true;
            boolean checkPhone = true;
            //确认教师是否开启邮件提醒,开启了是否为空呢？
            String mail = notificationInfoMapper.getCheckMail(classNumber, subject, checkMail, school);
            String phone = notificationInfoMapper.getCheckPhone(classNumber, subject, checkPhone, school);
            //只要有一个能通知，就通知，两个都不能则提醒没有联系方式
            if ((mail != null && !mail.isEmpty()) || (phone != null && !phone.isEmpty())) {
                //处理邮件通知
                if (mail != null && !mail.isEmpty()) {

                    String maisubject = isoDate + "作业反馈";
                    String addressfrom = classNumber + "班";
                    directMailUtills.singleSendMail(addressfrom, maisubject, mail, feedbacknotification);
                    resultList.add("邮件通知成功");
                } else {
                    resultList.add("");
                }
                ;
                if (phone != null && !phone.isEmpty() && !phone.equals("")) {
                    String classAndSubject = classNumber + "班--" + subject + "--";

//                    if (contentUncompletedMap != null && !contentUncompletedMap.isEmpty()){
//                        for(Map.Entry<String,List<String>> entry:contentUncompletedMap.entrySet()){
//                            if(entry.getValue().size()<9){
//                                sendSmsUtills.senSingleMessage(phone,isoDate,classAndSubject,"《"+entry.getKey()+"》",entry.getValue().stream().collect(Collectors.joining("、"))+"、","请以上同学需注意" );
//                            }else {
//                                sendSmsUtills.senSingleMessage(phone,isoDate,classAndSubject,"《"+entry.getKey()+"》",entry.getValue().subList(0,8).stream().collect(Collectors.joining("、")),entry.getValue().subList(8,entry.getValue().size()).stream().collect(Collectors.joining("、")) );
//                            }
//                        }
//                    }else {
//                        sendSmsUtills.senSingleMessage(phone,isoDate,classAndSubject,"《全部作业内容》","全部同学完成了作业","再接再厉");
//                    }
                    //如果字符串长度大于35，就拆分成8个变量，如果不够拆的，其他变量就变成''
// 初始化所有变量为空字符串
                    String a1 = "";
                    String a2 = "";
                    String a3 = "";
                    String a4 = "";
                    String a5 = "";
                    String a6 = "";
                    String a7 = "";
                    String a8 = "";


                    int length = feedbackPhonenotification.length();
                    int currentIndex = 0;

// 循环处理每个变量，最多处理8个（a1到a8）
                    for (int i = 0; i < 8 && currentIndex < length; i++) {
                        int endIndex = Math.min(currentIndex + 35, length); // 计算当前分段结束位置
                        String part = feedbackPhonenotification.substring(currentIndex, endIndex);

                        // 根据索引赋值给对应的变量
                        switch (i) {
                            case 0:
                                a1 = part;
                                break;
                            case 1:
                                a2 = part;
                                break;
                            case 2:
                                a3 = part;
                                break;
                            case 3:
                                a4 = part;
                                break;
                            case 4:
                                a5 = part;
                                break;
                            case 5:
                                a6 = part;
                                break;
                            case 6:
                                a7 = part;
                                break;
                            case 7:
                                a8 = part;
                                break;
                        }

                        currentIndex = endIndex; // 更新下一个分段的起始位置
                    }


                    sendSmsUtills.sendPhoneMessage(phone, isoDate, classAndSubject, a1, a2, a3, a4, a5, a6, a7, a8);


                    resultList.add("电话通知成功");
                } else {
                    resultList.add("");
                }
                ;
            } else {
                resultList.add("教师未开启邮件和电话通知");
            }
            return resultList;
        } else {

//通知所有老师的逻辑
            List<String> resultList = new ArrayList<>();
            String feedback = "";
            String querysubject1 = "语文";
            String feedbackchinese = feedbackgene(uncompleted, classNumber, checkdate, isoDate, querysubject1, school);
            feedback += feedbackchinese;
            String querysubject2 = "数学";
            String feedbackmath = feedbackgene(uncompleted, classNumber, checkdate, isoDate, querysubject2, school);
            feedback += feedbackmath;
            String querysubject3 = "英语";
            String feedbackenglish = feedbackgene(uncompleted, classNumber, checkdate, isoDate, querysubject3, school);
            feedback += feedbackenglish;
            resultList.add(feedback);


            boolean checkMail = true;
            boolean checkPhone = true;
            //确认教师是否开启邮件提醒,开启了是否为空呢？
            List<String> mailList = notificationInfoMapper.getCheckMailList(classNumber, checkMail, school);
            List<String> phoneList = notificationInfoMapper.getCheckPhoneList(classNumber, checkPhone, school);
            //获取开通了电话通知的学科名单
            List<String> subjectList = notificationInfoMapper.getCheckPhoneSubject(classNumber, checkPhone, school);
            HashMap<String, String> subjectPhoneMap = new HashMap<>();


            //只要有一个能通知，就通知，两个都不能则提醒没有联系方式
            if ((mailList != null && mailList.size() > 0) || (phoneList != null && phoneList.size() > 0)) {
                //处理邮件通知
                if (mailList != null && mailList.size() > 0) {

                    String maisubject = isoDate + "作业反馈";
                    String addressfrom = classNumber + "班";
                    for (String mail : mailList) {
                        directMailUtills.singleSendMail(addressfrom, maisubject, mail, feedback);
                    }

                    resultList.add("邮件通知成功");
                } else {
                    resultList.add("");
                }
                ;
                if (phoneList != null && phoneList.size() > 0) {

                    //首先知道各个学科各个内容的未完成作业学生名单
                    for (int i = 0; i < phoneList.size(); i++) {
                        subjectPhoneMap.put(subjectList.get(i), phoneList.get(i));
                    }

                    HashMap<String, List<String>> chineseContentedUncompletedMap = new HashMap<>();
                    HashMap<String, List<String>> mathContentedUncompletedMap = new HashMap<>();
                    HashMap<String, List<String>> englishContentedUncompletedMap = new HashMap<>();

                    for (Map.Entry<String, String> entry : subjectPhoneMap.entrySet()) {
                        if (entry.getKey().equals("语文") && subjectPhoneMap.get("语文") != null && !subjectPhoneMap.get("语文").equals("")) {
                            chineseContentedUncompletedMap = contentUncompletedMapMethod(uncompleted, classNumber, checkdate, entry.getKey(), school);

                        } else if (entry.getKey().equals("数学") && subjectPhoneMap.get("数学") != null && !subjectPhoneMap.get("数学").equals("")) {
                            mathContentedUncompletedMap = contentUncompletedMapMethod(uncompleted, classNumber, checkdate, entry.getKey(), school);

                        } else if (entry.getKey().equals("英语") && subjectPhoneMap.get("英语") != null && !subjectPhoneMap.get("英语").equals("")) {
                            englishContentedUncompletedMap = contentUncompletedMapMethod(uncompleted, classNumber, checkdate, entry.getKey(), school);
                        }
                    }


                    //分别电话通知


                    if (chineseContentedUncompletedMap != null && !chineseContentedUncompletedMap.isEmpty()) {
                        String classAndSubject = classNumber + "班--" + "语文--";
                        for (Map.Entry<String, List<String>> entry : chineseContentedUncompletedMap.entrySet()) {
                            if (entry.getValue().size() < 9) {
                                sendSmsUtills.senSingleMessage(subjectPhoneMap.get("语文"), isoDate, classAndSubject, "《" + entry.getKey() + "》", entry.getValue().stream().collect(Collectors.joining("、")) + "、", "请以上同学需注意");
                            } else {
                                sendSmsUtills.senSingleMessage(subjectPhoneMap.get("语文"), isoDate, classAndSubject, "《" + entry.getKey() + "》", entry.getValue().subList(0, 8).stream().collect(Collectors.joining("、")), entry.getValue().subList(8, entry.getValue().size()).stream().collect(Collectors.joining("、")));
                            }
                        }
                    } else {
                        String classAndSubject = classNumber + "班--" + "语文--";
                        if (subjectPhoneMap.get("语文") != null && !subjectPhoneMap.get("语文").equals("")) {
                            sendSmsUtills.senSingleMessage(subjectPhoneMap.get("语文"), isoDate, classAndSubject, "《全部作业内容》", "全部同学完成了作业", "再接再厉");
                        }
                    }

                    if (mathContentedUncompletedMap != null && !mathContentedUncompletedMap.isEmpty()) {
                        String classAndSubject = classNumber + "班--" + "数学--";
                        for (Map.Entry<String, List<String>> entry : mathContentedUncompletedMap.entrySet()) {
                            if (entry.getValue().size() < 9) {
                                sendSmsUtills.senSingleMessage(subjectPhoneMap.get("数学"), isoDate, classAndSubject, "《" + entry.getKey() + "》", entry.getValue().stream().collect(Collectors.joining("、")) + "、", "请以上同学需注意");
                            } else {
                                sendSmsUtills.senSingleMessage(subjectPhoneMap.get("数学"), isoDate, classAndSubject, "《" + entry.getKey() + "》", entry.getValue().subList(0, 8).stream().collect(Collectors.joining("、")), entry.getValue().subList(8, entry.getValue().size()).stream().collect(Collectors.joining("、")));
                            }
                        }
                    } else {
                        String classAndSubject = classNumber + "班--" + "数学--";
                        if (subjectPhoneMap.get("数学") != null && !subjectPhoneMap.get("数学").equals("")) {
                            sendSmsUtills.senSingleMessage(subjectPhoneMap.get("数学"), isoDate, classAndSubject, "《全部作业内容》", "全部同学完成了作业", "再接再厉");
                        }
                    }

                    if (englishContentedUncompletedMap != null && !englishContentedUncompletedMap.isEmpty()) {
                        String classAndSubject = classNumber + "班--" + "英语--";
                        for (Map.Entry<String, List<String>> entry : englishContentedUncompletedMap.entrySet()) {
                            if (entry.getValue().size() < 9) {
                                sendSmsUtills.senSingleMessage(subjectPhoneMap.get("英语"), isoDate, classAndSubject, "《" + entry.getKey() + "》", entry.getValue().stream().collect(Collectors.joining("、")) + "、", "请以上同学需注意");
                            } else {
                                sendSmsUtills.senSingleMessage(subjectPhoneMap.get("英语"), isoDate, classAndSubject, "《" + entry.getKey() + "》", entry.getValue().subList(0, 8).stream().collect(Collectors.joining("、")), entry.getValue().subList(8, entry.getValue().size()).stream().collect(Collectors.joining("、")));
                            }
                        }
                    } else {
                        String classAndSubject = classNumber + "班--" + "英语--";
                        if (subjectPhoneMap.get("英语") != null && !subjectPhoneMap.get("英语").equals("")) {
                            sendSmsUtills.senSingleMessage(subjectPhoneMap.get("英语"), isoDate, classAndSubject, "《全部作业内容》", "全部同学完成了作业", "再接再厉");
                        }
                    }

                    resultList.add("电话通知成功");
                } else {
                    resultList.add("");
                }
                ;
            } else {
                resultList.add("教师未开启邮件和电话通知");
            }
            return resultList;

        }


    }

    private HashMap<String, List<String>> contentUncompletedMapMethod(Integer uncompleted, String classNumber, LocalDate checkdate, String subject, String school) {
        //这里要根据检查作业的内容选择未完成作业的名单
        List<Record> uncompletedRecordList = recordMapper.getalluncompleted(uncompleted, classNumber, checkdate, subject, school);
        Map<String, List<Record>> groupedByContent = uncompletedRecordList.stream()
                .collect(Collectors.groupingBy(Record::getContent));

        HashMap<String, List<String>> contentUncompletedMap = new HashMap<>();
        for (Map.Entry<String, List<Record>> entry : groupedByContent.entrySet()) {
            List<String> uncopletedNameList = new ArrayList<>();
            for (Record record : entry.getValue()) {
                uncopletedNameList.add(record.getName());
                contentUncompletedMap.put(record.getContent(), uncopletedNameList);
            }
        }
        return contentUncompletedMap;
    }


    /**
     * 查询个人表格数据
     *
     * @param pageQueryNameDTO
     * @return
     */
    @Override
    public List<PageQueryNameVO> pageName(PageQueryNameDTO pageQueryNameDTO) {
        String classNumber = pageQueryNameDTO.getClassNumber();
        String name = pageQueryNameDTO.getName();
        LocalDate startdate = pageQueryNameDTO.getStartdate();
        LocalDate enddate = pageQueryNameDTO.getEnddate();
        String subject = pageQueryNameDTO.getSubject();
        String content = pageQueryNameDTO.getContent();
        String school = pageQueryNameDTO.getSchool();
        List<Record> recordList = recordMapper.getByNameDateClass(startdate, enddate, classNumber, content, name, subject, school);
        //        将相同作业类型的content放一起
        Map<AbstractMap.SimpleEntry<LocalDate, String>, List<Record>> groupedrecord = recordList.stream()
                .collect(Collectors.groupingBy(record -> new AbstractMap.SimpleEntry<>(record.getCheckdate(), record.getName())));
        List<PageQueryNameVO> pageQueryNameVOS = new ArrayList<>();
        for (Map.Entry<AbstractMap.SimpleEntry<LocalDate, String>, List<Record>> entry : groupedrecord.entrySet()) {
            List<PageQueryNameVO> result = getpageNameVO(entry.getValue());
            pageQueryNameVOS.addAll(result);
        }

        // 自定义比较器，先按日期倒序排序，然后按学科倒序排序
        Comparator<PageQueryNameVO> comparator = Comparator.comparing(PageQueryNameVO::getCheckdate, Comparator.reverseOrder());
        // 使用Collections.sort()方法和自定义比较器对records进行排序
        Collections.sort(pageQueryNameVOS, comparator);
        return pageQueryNameVOS;
    }

    @Override
    public String SpecialHomework(RecordDTO recordDTO) {
        //处理数据（多思考各种异常情况）
        //判断是否初始化:当前日期、班级、作业内容是否已经存在
        String classNumber = recordDTO.getClassNumber();
        String content = recordDTO.getContent();
        String subject = recordDTO.getSubject();
        LocalDate checkdate = recordDTO.getCheckdate();
        Integer level = 1;
        String school = recordDTO.getSchool();
        Integer addScoreNumber = recordDTO.getAddScoreNumber();
        Integer minusScoreNumber = recordDTO.getMinusScoreNumber();
        Integer year = parseClassNameToYear(classNumber);


        //处理value
        String value = String.join("", recordDTO.getValueList());
        if (value.endsWith("--")) {
            value = value.substring(0, value.length() - 2);
        }
        String[] parts = value.split("--");
        List<String> studentNumberList = new ArrayList<>();
        for (String part : parts) {
            String[] numbers = part.split("/");
            if (numbers.length > 0) {
                if (numbers.length > 1) {
                    if (numbers[1].equals(classNumber)) {
                        studentNumberList.add(numbers[0]);
                    }
                }

            }
        }


        Map<String, Integer> frequencyMap = getElementFrequency(studentNumberList);

        List<String> uniqueElements = new ArrayList<>();
        List<String> repeatedElements = new ArrayList<>();

        for (Map.Entry<String, Integer> entry : frequencyMap.entrySet()) {
            if (entry.getValue() == 1) {
                uniqueElements.add(entry.getKey());
            } else {
                repeatedElements.add(entry.getKey());
            }
        }

        String feedbacknotification = "完成达标情况登记<br>";
        if (uniqueElements.size() > 0) {
            level = 1;
            List<String> nameList = new ArrayList<>();
            //如果优秀作业的名单不为空，但是查询不到姓名，说明学号和班级不匹配，或者学号不存在，所以要先判断一下名单是否为空
            if (uniqueElements != null && uniqueElements.size() > 0) {
                //获取学号对应的学生姓名
                nameList = studentInfoMapper.getnameList(uniqueElements, classNumber, school);
            } else {
                return "没有扫到任何学生的二维码，请检查二维码是否有误";
            }
            //再反过来查询一遍学号，避免学号和姓名对不上
            if (nameList != null && nameList.size() > 0) {
            } else {
                return "没有扫到任何学生的二维码，请检查二维码是否有误";
            }


            List<Record> recordList = recordMapper.isexist(classNumber, content, checkdate, subject, school);
            List<Record> originrecordList = new ArrayList<>();
            if (recordList != null && recordList.size() > 0) {

            } else {
                //给全班的学生、姓名、作业内容、日期初始化，同时完成状态相反化
                List<String> allNameList = studentInfoMapper.getallName(classNumber, school);
                List<String> allstudentNumberList = studentInfoMapper.getallStudentNumber(classNumber, school);
                for (int i = 0; i < allstudentNumberList.size(); i++) {
                    Record record = new Record();
                    BeanUtils.copyProperties(recordDTO, record);
                    record.setStudentNumber(allstudentNumberList.get(i));
                    record.setName(allNameList.get(i));
                    record.setCreateTime(LocalDateTime.now());
                    //在没有登记完成作业情况的时候，初始化设置完成状态未null
                    record.setCompleted(null);
                    //初始化登记都登记为一般
                    record.setLevel(0);
                    originrecordList.add(record);
                }
                ;

                //全部上传到record数据库中
                recordMapper.batchupload(originrecordList);
            }


            //正式更新等级水平
            Integer completed = 1;
            LocalDateTime nowtime = LocalDateTime.now();
            Integer level1 = level;
            boolean revision = false;
            if (level == 1) {
                revision = true;
            }

            recordMapper.updatelevel(completed, nowtime, content, classNumber, checkdate, level1, studentNumberList, nameList, revision, subject, school);

            String situation = "表扬";
            String reason = "优秀作业";
            Integer score = addScoreNumber;

            performMapper.addPerform(checkdate, nameList, subject, reason, situation, score, classNumber, school,year);

            Integer addnumber = addScoreNumber;
            accumulateScoreService.updatescore(addnumber, classNumber, nameList, subject, school);
            feedbacknotification += "<br>" + String.join("、", nameList);
            feedbacknotification += "<br>" + "以上同学+" + addnumber + "分";
        }

        if (repeatedElements.size() > 0) {
            level = -1;
            List<String> nameList = new ArrayList<>();
            if (repeatedElements != null && repeatedElements.size() > 0) {
                //获取学号对应的学生姓名
                nameList = studentInfoMapper.getnameList(repeatedElements, classNumber, school);
            } else {
                return "没有扫到任何学生的二维码，请检查二维码是否有误";
            }
            //再反过来查询一遍学号，避免学号和姓名对不上
            if (nameList != null && nameList.size() > 0) {
                repeatedElements = studentInfoMapper.getnewStudentNumberList(nameList, classNumber, school);
            } else {
                return "没有扫到任何学生的二维码，请检查二维码是否有误";
            }


            List<Record> recordList = recordMapper.isexist(classNumber, content, checkdate, subject, school);
            List<Record> originrecordList = new ArrayList<>();
            if (recordList != null && recordList.size() > 0) {

            } else {
                //给全班的学生、姓名、作业内容、日期初始化，同时完成状态相反化
                List<String> allNameList = studentInfoMapper.getallName(classNumber, school);
                List<String> allstudentNumberList = studentInfoMapper.getallStudentNumber(classNumber, school);
                for (int i = 0; i < allstudentNumberList.size(); i++) {
                    Record record = new Record();
                    BeanUtils.copyProperties(recordDTO, record);
                    record.setStudentNumber(allstudentNumberList.get(i));
                    record.setName(allNameList.get(i));
                    record.setCreateTime(LocalDateTime.now());
                    //在没有登记完成作业情况的时候，初始化设置完成状态未null
                    record.setCompleted(null);
                    //初始化登记都登记为一般
                    record.setLevel(0);
                    originrecordList.add(record);
                }
                ;

                //全部上传到record数据库中
                recordMapper.batchupload(originrecordList);
            }


            //正式更新等级水平
            Integer completed = 1;
            LocalDateTime nowtime = LocalDateTime.now();
            Integer level1 = level;
            boolean revision = false;
            if (level == 1) {
                revision = true;
            }

            recordMapper.updatelevel(completed, nowtime, content, classNumber, checkdate, level1, studentNumberList, nameList, revision, subject, school);



            String situation = "批评";
            String reason = "作业不达标";
            Integer score = minusScoreNumber;
            performMapper.addPerform(checkdate, nameList, subject, reason, situation, score, classNumber, school,year);
            //加分和减分；（就算是更换二维码，这些人的分数也不能消失，所以只能按照姓名来算，不能按学号）
            //如果不在就要新增分数表，如果存在就更新分数表
            feedbacknotification += "<br>" + String.join("、", nameList);
            if (recordDTO.getMinusScoreByHomework().equals("是")) {
                accumulateScoreService.getNameClass(classNumber, nameList, subject, school);

                Integer addnumber = - minusScoreNumber;
                accumulateScoreService.updatescore(addnumber, classNumber, nameList, subject, school);
                feedbacknotification += "<br>" + "以上同学 " + addnumber + "分";
            }


        }
        //再统一更新一次supplemetary
        if(recordDTO.getSupplementary()!= null && !recordDTO.getSupplementary().equals("")){
            String supplementary = recordDTO.getSupplementary();
            LocalDateTime nowtime = LocalDateTime.now();
            recordMapper.updateSupplementary(supplementary,nowtime,content,classNumber,checkdate,subject,school);
        }


        return feedbacknotification;

    }

    private Integer parseClassNameToYear(String classNumber) {
        Integer grade = Integer.valueOf(classNumber.substring(0,1));
        String gradeChinese = turnInterToChinese(grade);
        String gradeStr = gradeChinese+ "年级";
        Integer year = gradeYearMapper.getYearByGrade(gradeStr);
        return year;
    }

    private String turnInterToChinese(Integer grade) {
        //把数字1变成一然后是2到10都变成汉字
        if(grade ==1){
            return "一";
        }
        if(grade ==2){
            return "二";
        }
        if(grade ==3){
            return "三";
        }
        if(grade ==4){
            return "四";
        }
        if(grade ==5){
            return "五";
        }
        if(grade ==6){
            return "六";
        }
        if(grade ==7){
            return "七";
        }
        if(grade ==8){
            return "八";
        }
        if(grade ==9){
            return "九";
        }
        return null;
    }

    @Override
    public String homeworkRevison(RecordDTO recordDTO) {
        // 如果没有查询到数据，那么就返回哪些学生没有作业数据
        String classNumber = recordDTO.getClassNumber();
        String content = recordDTO.getContent();

        String subject = recordDTO.getSubject();
        LocalDate checkdate = recordDTO.getCheckdate();
        String school = recordDTO.getSchool();
        List<String> valueList = recordDTO.getValueList();
        Integer year = parseClassNameToYear(classNumber);

        //首先是验证二维码扫描的学生名单是否在这个班级里，学号和姓名是否匹配
        List<String> nameList = confirmStudentInfo(valueList, classNumber, school);
        if (nameList == null || nameList.size() == 0) {
            return "没有扫到任何学生的二维码，请检查二维码是否有误";
        }

        //其次是验证这些学生是否有登记作业，如果没有登记作业，那么就返回这些学生没有登记作业
        List<Record> recordList = recordMapper.getByNameListDateClass(checkdate, checkdate, classNumber, content, nameList, subject, school);
        //修改这些学生的作业完成情况和订正状态

        List<String> noSubmitNameList = new ArrayList<>();
        List<String> submitLevel0NameList = new ArrayList<>();
        List<String> submitLevelMinus1NameList = new ArrayList<>();
        List<String> submitLevel1NameList = new ArrayList<>();

        for( Record record : recordList) {
            if(record.getCompleted() != null && record.getCompleted() == 0) {
                noSubmitNameList.add(record.getName());
            } else if (record.getCompleted() != null && record.getCompleted() == 1 && (record.getLevel() == null || record.getLevel() == 0)) {
                submitLevel0NameList.add(record.getName());
            } else if (record.getCompleted() != null && record.getCompleted() == 1 && record.getLevel() != null && record.getLevel() == -1) {
                submitLevelMinus1NameList.add(record.getName());
            } else if (record.getCompleted() != null && record.getCompleted() == 1 && record.getLevel() != null && record.getLevel() == 1) {
                submitLevel1NameList.add(record.getName());
            }

        }




        //没有提交，也就是completed 为0 的，revision改为1，completed改为1，level改为0
        if(noSubmitNameList.size() > 0) {
        recordMapper.updateRevisionNoSubmit(checkdate, content, classNumber, subject, school, noSubmitNameList);}
        //提交了的，level是0 的，revision改为1，
        if(submitLevel0NameList.size() > 0) {
        recordMapper.updateRevisionSubmitLevel0(checkdate, content, classNumber, subject, school, submitLevel0NameList);
        //这里做一个判断
            if(recordDTO.getCompletedRevisionAddScore().equals("是")){
                String situation = "表扬";
                String reason = "积极订正作业";
                Integer score = recordDTO.getRevisionAddScore();
                performMapper.addPerform(checkdate,submitLevel0NameList,subject,reason,situation,score,classNumber,school,year);

                Integer addnumber = recordDTO.getRevisionAddScore();
                accumulateScoreService.updatescore(addnumber,classNumber,submitLevel0NameList,subject,school);
            }
        }
        //提交了的，level是-1 的，revision改为1，并且加分，也就是add perform和accumulateScore
        if(submitLevelMinus1NameList.size() > 0) {
        recordMapper.updateRevisionSubmitLevelMinus1(checkdate, content, classNumber, subject, school, submitLevelMinus1NameList);
        if(recordDTO.getFailRevisionAddScore().equals("是")){
            String situation = "表扬";
            String reason = "积极订正作业";
            Integer score = recordDTO.getRevisionAddScore();
            performMapper.addPerform(checkdate,submitLevelMinus1NameList,subject,reason,situation,score,classNumber,school,year);

            Integer addnumber = recordDTO.getRevisionAddScore();
            accumulateScoreService.updatescore(addnumber,classNumber,submitLevelMinus1NameList,subject,school);
        }}
        //提交了的，level是1 的，revision改为1
        if(submitLevel1NameList.size() > 0) {
        recordMapper.updateRevisionSubmitLevel1(checkdate, content, classNumber, subject, school, submitLevel1NameList);}

        String resp = "完成订正的同学有："+ String.join("、", nameList) + "<br><br>";
        if(submitLevelMinus1NameList.size() > 0 && recordDTO.getFailRevisionAddScore().equals("是")){
            resp += "不达标作业的同学：<br>" + String.join("、", submitLevelMinus1NameList) + "，分数已经加回了哦！！<br><br>";
        }

        if(submitLevel0NameList.size() > 0 && recordDTO.getCompletedRevisionAddScore().equals("是")) {
            resp += "合格作业订正的同学：<br>" + String.join("、", submitLevel0NameList) + "，加了"+recordDTO.getRevisionAddScore()+"分！！<br><br>";
        }

        //再统一更新一次supplemetary
        if(recordDTO.getSupplementary()!= null && !recordDTO.getSupplementary().equals("")){
            String supplementary = recordDTO.getSupplementary();
            LocalDateTime nowtime = LocalDateTime.now();
            recordMapper.updateSupplementary(supplementary,nowtime,content,classNumber,checkdate,subject,school);
        }











        return resp;
    }


    /**
     * 登记优差作业,语音输入的时候进行判断
     *
     * @param recordDTO
     * @return
     */
    @Override
    public String uploadFeedbackSpecial(RecordDTO recordDTO) {
        //处理数据（多思考各种异常情况）
        //判断是否初始化:当前日期、班级、作业内容是否已经存在
        String classNumber = recordDTO.getClassNumber();
        String content = recordDTO.getContent();
        Integer level = recordDTO.getLevel();
        String subject = recordDTO.getSubject();
        LocalDate checkdate = recordDTO.getCheckdate();
        String school = recordDTO.getSchool();
        Integer addScoreNumber = recordDTO.getAddScoreNumber();
        Integer minusScoreNumber = recordDTO.getMinusScoreNumber();
        Integer year = parseClassNameToYear(classNumber);


        //处理value
        String value = String.join("", recordDTO.getValueList());
        if (value.endsWith("--")) {
            value = value.substring(0, value.length() - 2);
        }
        String[] parts = value.split("--");
        List<String> studentNumberList = new ArrayList<>();
        for (String part : parts) {
            String[] numbers = part.split("/");
            if (numbers.length > 0) {
                if (numbers.length > 1) {
                    if (numbers[1].equals(classNumber)) {
                        studentNumberList.add(numbers[0]);
                    }
                }

            }
        }
        //去掉重复的元素
        Set<String> set = new HashSet<>(studentNumberList);
        studentNumberList = new ArrayList<>(set);

        System.out.println(studentNumberList);
        List<String> nameList = new ArrayList<>();
        if (studentNumberList != null && studentNumberList.size() > 0) {
            //获取学号对应的学生姓名
            nameList = studentInfoMapper.getnameList(studentNumberList, classNumber, school);
        } else {
            return "没有扫到任何学生的二维码，请检查二维码是否有误";
        }
        //再反过来查询一遍学号，避免学号和姓名对不上
        if (nameList != null && nameList.size() > 0) {
            studentNumberList = studentInfoMapper.getnewStudentNumberList(nameList, classNumber, school);
        } else {
            return "没有扫到任何学生的二维码，请检查二维码是否有误";
        }


        List<Record> recordList = recordMapper.isexist(classNumber, content, checkdate, subject, school);
        List<Record> originrecordList = new ArrayList<>();
        if (recordList != null && recordList.size() > 0) {

        } else {
            //给全班的学生、姓名、作业内容、日期初始化，同时完成状态相反化
            List<String> allNameList = studentInfoMapper.getallName(classNumber, school);
            List<String> allstudentNumberList = studentInfoMapper.getallStudentNumber(classNumber, school);
            for (int i = 0; i < allstudentNumberList.size(); i++) {
                Record record = new Record();
                BeanUtils.copyProperties(recordDTO, record);
                record.setStudentNumber(allstudentNumberList.get(i));
                record.setName(allNameList.get(i));
                record.setCreateTime(LocalDateTime.now());
                //在先登记优秀和不达标的时候，登记完成作业情况的时候，初始化设置完成状态为完成
                record.setCompleted(1);
                //初始化登记都登记为一般
                record.setLevel(0);
                originrecordList.add(record);
            }


            //全部上传到record数据库中
            recordMapper.batchupload(originrecordList);
        }

        //正式更新等级水平
        Integer completed = 1;
        LocalDateTime nowtime = LocalDateTime.now();
        Integer level1 = level;
        boolean revision = false;
        if (level == 1) {
            revision = true;
        }

        recordMapper.updatelevel(completed, nowtime, content, classNumber, checkdate, level1, studentNumberList, nameList, revision, subject, school);

        //反馈结果

        List<String> levelfeedbackList = recordMapper.getlevelfeedback(level, content, classNumber, checkdate, subject, school);
        System.out.println(levelfeedbackList);

        subject = recordDTO.getSubject();
        String isoDate = checkdate.format(DateTimeFormatter.ISO_LOCAL_DATE);
        String supplementary = recordDTO.getSupplementary();
        String startpage = String.valueOf(recordDTO.getStartpage());
        String endpage = String.valueOf(recordDTO.getEndpage());
        String feedbacknotification = "";
        feedbacknotification += isoDate + "<br>" + subject + "《" + content + "》";
        if (startpage != null && !startpage.isEmpty() && !startpage.equals("null")) {
            feedbacknotification += "P" + startpage + "-" + endpage;
        }
        if (supplementary != null && !supplementary.isEmpty()) {
            feedbacknotification += supplementary + "<br>";
        } else {
            feedbacknotification += "<br>";
        }

        String feedInfo = "作业表现优秀的同学有：";
        if (level == -1) {
            feedInfo = "作业不达标的同学有：";
        }
        feedbacknotification += feedInfo + "<br>";
        feedbacknotification += String.join("、", nameList);


        //加分和减分；（就算是更换二维码，这些人的分数也不能消失，所以只能按照姓名来算，不能按学号）
        //查询一次是否存在分数表(如果不存在，已经新增了)
        accumulateScoreService.getNameClass(classNumber, nameList, subject, school);

        //登记不代表作业
        if (recordDTO.getLevel() == -1) {
            String situation = "批评";
            String reason = "作业不达标";
            performMapper.addPerform(checkdate, nameList, subject, reason, situation, minusScoreNumber, classNumber, school,year);

            //如果确认是true，确认减分
            if (recordDTO.isScore()) {
                Integer addnumber = - minusScoreNumber;
                accumulateScoreService.updatescore(addnumber, classNumber, nameList, subject, school);
                feedbacknotification += "<br>" + "以上同学" + addnumber + "分";
            }


        } else {
            String situation = "表扬";
            String reason = "优秀作业";
            performMapper.addPerform(checkdate, nameList, subject, reason, situation, addScoreNumber, classNumber, school,year);


            Integer addnumber = addScoreNumber;
            accumulateScoreService.updatescore(addnumber, classNumber, nameList, subject, school);
            feedbacknotification += "<br>" + "以上同学+" + addnumber + "分";
        }
        //再统一更新一次supplemetary
        if(recordDTO.getSupplementary()!= null && !recordDTO.getSupplementary().equals("")){
            supplementary = recordDTO.getSupplementary();
            recordMapper.updateSupplementary(supplementary,nowtime,content,classNumber,checkdate,subject,school);
        }

        return feedbacknotification;

    }


    @Override
    public String uploadAudioHomeWork(MultipartFile file, String school, String classNumber, String content, String subject,LocalDate checkdate,String supplementary,String minusScoreByHomework,String failRevisionAddScore,Integer addScoreNumber,Integer minusScoreNumber,String completedRevisionAddScore,Integer revisionAddScore) throws Exception {
        //首先要调用百度语音识别的接口，把语音转换成文字
        String audioText = aliyunAudioRecognitionUtil.recognize(file);

        audioText = replaceLastDingzheng(audioText);


//        String jsonFeedback = baiWenXinUtills.getJsonHomeWork(audioText);
        String jsonFeedback = deepSeekUtills.getJsonHomeWork(audioText);
        String jsonString = jsonFeedback.replaceAll("(?s)```json\\s*(\\{.*?\\})\\s*```", "$1");
        ObjectMapper mapper = new ObjectMapper();
        M_HomeworkAudio mHomeworkAudio = mapper.readValue(jsonString, M_HomeworkAudio.class);


        String testJson = mHomeworkAudio.getCheckdateStr()+":"+mHomeworkAudio.getNamelistStr()+":"+mHomeworkAudio.getTypeStr();

        if(mHomeworkAudio.getTypeStr().equals("无")){
            return "未识别出作业状态，请重新上传语音";
        }
        if(mHomeworkAudio.getNamelistStr().equals("无")){
            return "未识别出学生名单，请重新上传语音";
        }

        //首先对日期的字符串处理，把汉字的处理为数字，同时要加上当前的年份，并返回为标准的日期格式，方便后续的数据库存储和查询。
        LocalDate parsecheckdate = parseCheckDateStr(mHomeworkAudio.getCheckdateStr());
        if(parsecheckdate != null){
            checkdate = parsecheckdate;
        }
        List<String> valuelist = parseNameListStr(mHomeworkAudio.getNamelistStr(),classNumber,school);
        if(valuelist == null || valuelist.size() == 0){
            return "未在学生信息表找到最佳匹配，请重新上传语音";
        }

        String resp = "";
        if(mHomeworkAudio.getTypeStr().equals("完成作业") ){
            //构造recordDTO
            RecordDTO recordDTO = new RecordDTO();
            recordDTO.setClassNumber(classNumber);
            recordDTO.setValueList(valuelist);
            recordDTO.setSubject(subject);
            recordDTO.setContent(content);
            recordDTO.setSupplementary(supplementary);
            recordDTO.setCompleted(1);
            recordDTO.setCheckdate(checkdate);
            recordDTO.setSchool(school);

            resp = uploadFeedback(recordDTO);

        } else if (mHomeworkAudio.getTypeStr().equals("未提交作业")) {

            //构造recordDTO
            RecordDTO recordDTO = new RecordDTO();
            recordDTO.setClassNumber(classNumber);
            recordDTO.setValueList(valuelist);
            recordDTO.setSubject(subject);
            recordDTO.setContent(content);
            recordDTO.setSupplementary(supplementary);
            recordDTO.setCompleted(0);
            recordDTO.setCheckdate(checkdate);
            recordDTO.setSchool(school);

            resp = uploadFeedback(recordDTO);
        } else if (mHomeworkAudio.getTypeStr().equals("优秀作业")) {
            //构造recordDTO
            RecordDTO recordDTO = new RecordDTO();
            recordDTO.setClassNumber(classNumber);
            recordDTO.setValueList(valuelist);
            recordDTO.setSubject(subject);
            recordDTO.setContent(content);
            recordDTO.setSupplementary(supplementary);
            recordDTO.setLevel(1);
            recordDTO.setCheckdate(checkdate);
            recordDTO.setSchool(school);
            recordDTO.setAddScoreNumber(addScoreNumber);
            if(minusScoreByHomework.equals("是")){
                recordDTO.setScore(true);
            } else {
                recordDTO.setScore(false);
            }
            resp = uploadFeedbackSpecial(recordDTO);
        }else if (mHomeworkAudio.getTypeStr().equals("不达标作业")) {
            RecordDTO recordDTO = new RecordDTO();
            recordDTO.setClassNumber(classNumber);
            recordDTO.setValueList(valuelist);
            recordDTO.setSubject(subject);
            recordDTO.setContent(content);
            recordDTO.setSupplementary(supplementary);
            recordDTO.setLevel(-1);
            recordDTO.setCheckdate(checkdate);
            recordDTO.setSchool(school);
            recordDTO.setMinusScoreNumber(minusScoreNumber);
            if(minusScoreByHomework.equals("是")){
                recordDTO.setScore(true);
            } else {
                recordDTO.setScore(false);
            }
            resp = uploadFeedbackSpecial(recordDTO);
        }else if (mHomeworkAudio.getTypeStr().equals("订正作业")) {
            RecordDTO recordDTO = new RecordDTO();
            recordDTO.setClassNumber(classNumber);
            recordDTO.setValueList(valuelist);
            recordDTO.setSubject(subject);
            recordDTO.setContent(content);
            recordDTO.setSupplementary(supplementary);
            recordDTO.setCheckdate(checkdate);
            recordDTO.setSchool(school);
            recordDTO.setFailRevisionAddScore(failRevisionAddScore);
            recordDTO.setCompletedRevisionAddScore(completedRevisionAddScore);
            recordDTO.setRevisionAddScore(revisionAddScore);
            resp = homeworkRevison(recordDTO);
        }else {
            return "未识别出作业状态，请重新上传语音";
        }

        if(resp.equals("")){
            return "没有登记任何记录";
        }


        return  resp;





    }

    private List<String> parseNameListStr(String namelistStr,String classNumber,String school) {
        if(namelistStr.equals("无")){
            return new ArrayList<>();
        }
        List<String> pinyinList = studentInfoMapper.getPinyin(classNumber,school);
        List<String> valueList = new ArrayList<>();
        //以、为分隔符，生成列表
        List<String> originNamePinyinList = List.of(namelistStr.split("、"));
        List<String> bestMatchList = new ArrayList<>();
        for(String namePinyin:originNamePinyinList){
            String bestMatch = findBestMatch(namePinyin.replaceAll("\\s+", ""), pinyinList);
            bestMatchList.add(bestMatch);
        }
        if(bestMatchList.size()>0){
            List<String> studentNumberList = studentInfoMapper.getStudentNumberList(bestMatchList,classNumber,school);
            if(studentNumberList != null && studentNumberList.size()>0){
                for(String studentNumber:studentNumberList){
                    //如果学号是8个字符
                    if(studentNumber.length()==8){
                        String value = studentNumber+"/"+classNumber+ "--";
                        valueList.add(value);
                    }
                }
            }

        }
        return valueList;

    }


    private LocalDate parseCheckDateStr(String checkdateStr) {
        // 如果是"无"则直接返回 null
        if (checkdateStr == null || "无".equals(checkdateStr)) {
            return null;
        }

        // 获取当前年份
        int year = Year.now().getValue();

        // 中文数字到整数的映射（覆盖 1~31）
        Map<String, Integer> chineseNumMap = new HashMap<>();
        chineseNumMap.put("一", 1);
        chineseNumMap.put("二", 2);
        chineseNumMap.put("三", 3);
        chineseNumMap.put("四", 4);
        chineseNumMap.put("五", 5);
        chineseNumMap.put("六", 6);
        chineseNumMap.put("七", 7);
        chineseNumMap.put("八", 8);
        chineseNumMap.put("九", 9);
        chineseNumMap.put("十", 10);
        chineseNumMap.put("十一", 11);
        chineseNumMap.put("十二", 12);
        chineseNumMap.put("十三", 13);
        chineseNumMap.put("十四", 14);
        chineseNumMap.put("十五", 15);
        chineseNumMap.put("十六", 16);
        chineseNumMap.put("十七", 17);
        chineseNumMap.put("十八", 18);
        chineseNumMap.put("十九", 19);
        chineseNumMap.put("二十", 20);
        chineseNumMap.put("二十一", 21);
        chineseNumMap.put("二十二", 22);
        chineseNumMap.put("二十三", 23);
        chineseNumMap.put("二十四", 24);
        chineseNumMap.put("二十五", 25);
        chineseNumMap.put("二十六", 26);
        chineseNumMap.put("二十七", 27);
        chineseNumMap.put("二十八", 28);
        chineseNumMap.put("二十九", 29);
        chineseNumMap.put("三十", 30);
        chineseNumMap.put("三十一", 31);

        int month = -1;
        int day = -1;

        // 1. 尝试匹配中文格式：例如 "六月二十九日"
        Pattern chinesePattern = Pattern.compile("([一二三四五六七八九十]+)月([一二三四五六七八九十]+)日");
        Matcher chineseMatcher = chinesePattern.matcher(checkdateStr);
        if (chineseMatcher.find()) {
            String monthStr = chineseMatcher.group(1);
            String dayStr = chineseMatcher.group(2);
            month = chineseNumMap.getOrDefault(monthStr, -1);
            day = chineseNumMap.getOrDefault(dayStr, -1);
        }

        // 2. 如果中文匹配失败，尝试数字格式 "X月Y日"
        if (month == -1 || day == -1) {
            Pattern numPattern = Pattern.compile("(\\d{1,2})月(\\d{1,2})日");
            Matcher numMatcher = numPattern.matcher(checkdateStr);
            if (numMatcher.find()) {
                month = Integer.parseInt(numMatcher.group(1));
                day = Integer.parseInt(numMatcher.group(2));
            }
        }

        // 3. 如果仍然失败，尝试数字格式 "X-Y" 或 "X/Y"
        if (month == -1 || day == -1) {
            Pattern simplePattern = Pattern.compile("(\\d{1,2})[/-](\\d{1,2})");
            Matcher simpleMatcher = simplePattern.matcher(checkdateStr);
            if (simpleMatcher.find()) {
                month = Integer.parseInt(simpleMatcher.group(1));
                day = Integer.parseInt(simpleMatcher.group(2));
            }
        }

        // 若仍未解析出月日，返回 null
        if (month == -1 || day == -1) {
            return null;
        }

        // 构造 LocalDate，如果日期非法则返回 null
        try {
            return LocalDate.of(year, month, day);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public String uploadtestHomeWork(String message, String school, String classNumber, String content, String subject,LocalDate checkdate,String supplementary,String minusScoreByHomework,String failRevisionAddScore,Integer addScoreNumber,Integer minusScoreNumber) throws Exception {
//        String jsonFeedback = baiWenXinUtills.getJsonHomeWork(message);
        String jsonFeedback = deepSeekUtills.getJsonHomeWork(message);
        String jsonString = jsonFeedback.replaceAll("(?s)```json\\s*(\\{.*?\\})\\s*```", "$1");
        ObjectMapper mapper = new ObjectMapper();
        M_HomeworkAudio mHomeworkAudio = mapper.readValue(jsonString, M_HomeworkAudio.class);
        System.out.println(mHomeworkAudio);

        String testJson = mHomeworkAudio.getCheckdateStr()+":"+mHomeworkAudio.getNamelistStr()+":"+mHomeworkAudio.getTypeStr();


        if(mHomeworkAudio.getTypeStr().equals("无")){
            return "未识别出作业状态，请重新上传语音";
        }
        if(mHomeworkAudio.getNamelistStr().equals("无")){
            return "未识别出学生名单，请重新上传语音";
        }

        //首先对日期的字符串处理，把汉字的处理为数字，同时要加上当前的年份，并返回为标准的日期格式，方便后续的数据库存储和查询。
        LocalDate parsecheckdate = parseCheckDateStr(mHomeworkAudio.getCheckdateStr());
        if(parsecheckdate != null){
            checkdate = parsecheckdate;
        }
        List<String> valuelist = parseNameListStr(mHomeworkAudio.getNamelistStr(),classNumber,school);
        if(valuelist == null || valuelist.size() == 0){
            return "未在学生信息表找到最佳匹配，请重新上传语音";
        }

        String resp = "";
        if(mHomeworkAudio.getTypeStr().equals("完成作业") ){
            //构造recordDTO
            RecordDTO recordDTO = new RecordDTO();
            recordDTO.setClassNumber(classNumber);
            recordDTO.setValueList(valuelist);
            recordDTO.setSubject(subject);
            recordDTO.setContent(content);
            recordDTO.setSupplementary(supplementary);
            recordDTO.setCompleted(1);
            recordDTO.setCheckdate(checkdate);
            recordDTO.setSchool(school);

            resp = uploadFeedback(recordDTO);

        } else if (mHomeworkAudio.getTypeStr().equals("未提交作业")) {

            //构造recordDTO
            RecordDTO recordDTO = new RecordDTO();
            recordDTO.setClassNumber(classNumber);
            recordDTO.setValueList(valuelist);
            recordDTO.setSubject(subject);
            recordDTO.setContent(content);
            recordDTO.setSupplementary(supplementary);
            recordDTO.setCompleted(0);
            recordDTO.setCheckdate(checkdate);
            recordDTO.setSchool(school);

            resp = uploadFeedback(recordDTO);
        } else if (mHomeworkAudio.getTypeStr().equals("优秀作业")) {
            //构造recordDTO
            RecordDTO recordDTO = new RecordDTO();
            recordDTO.setClassNumber(classNumber);
            recordDTO.setValueList(valuelist);
            recordDTO.setSubject(subject);
            recordDTO.setContent(content);
            recordDTO.setSupplementary(supplementary);
            recordDTO.setLevel(1);
            recordDTO.setCheckdate(checkdate);
            recordDTO.setSchool(school);
            recordDTO.setAddScoreNumber(addScoreNumber);
            if(minusScoreByHomework.equals("是")){
                recordDTO.setScore(true);
            } else {
                recordDTO.setScore(false);
            }
            resp = uploadFeedbackSpecial(recordDTO);
        }else if (mHomeworkAudio.getTypeStr().equals("不达标作业")) {
            RecordDTO recordDTO = new RecordDTO();
            recordDTO.setClassNumber(classNumber);
            recordDTO.setValueList(valuelist);
            recordDTO.setSubject(subject);
            recordDTO.setContent(content);
            recordDTO.setSupplementary(supplementary);
            recordDTO.setLevel(-1);
            recordDTO.setCheckdate(checkdate);
            recordDTO.setSchool(school);
            recordDTO.setMinusScoreNumber(minusScoreNumber);
            if(minusScoreByHomework.equals("是")){
                recordDTO.setScore(true);
            } else {
                recordDTO.setScore(false);
            }
            resp = uploadFeedbackSpecial(recordDTO);
        }else if (mHomeworkAudio.getTypeStr().equals("订正作业")) {
            RecordDTO recordDTO = new RecordDTO();
            recordDTO.setClassNumber(classNumber);
            recordDTO.setValueList(valuelist);
            recordDTO.setSubject(subject);
            recordDTO.setContent(content);
            recordDTO.setSupplementary(supplementary);
            recordDTO.setCheckdate(checkdate);
            recordDTO.setSchool(school);
            recordDTO.setFailRevisionAddScore(failRevisionAddScore);
            resp = homeworkRevison(recordDTO);
        }else {
            return "未识别出作业状态，请重新上传语音";
        }

        if(resp.equals("")){
            return "没有登记任何记录";
        }


        return  resp;
    }




    public  String findBestMatch(String target, List<String> pinyinList) {
        LevenshteinDistance levenshtein = new LevenshteinDistance();
        String bestMatch = null;
        int minDistance = Integer.MAX_VALUE;

        for (String pinyin : pinyinList) {
            int distance = levenshtein.apply(target, pinyin);
            if (distance < minDistance) {
                minDistance = distance;
                bestMatch = pinyin;
            }
        }

        return bestMatch;
    }




    private List<String> confirmStudentInfo(List<String> valueList, String classNumber, String school) {
        //处理value
        //把recordList转化为value
        String value = String.join("", valueList);
        if (value.endsWith("--")) {
            value = value.substring(0, value.length() - 2);
        }
        String[] parts = value.split("--");
        List<String> studentNumberList = new ArrayList<>();
        for (String part : parts) {
            String[] numbers = part.split("/");
            if (numbers.length > 0) {
                if (numbers.length > 1) {
                    if (numbers[1].equals(classNumber)) {
                        studentNumberList.add(numbers[0]);
                    }
                }

            }
        }
        //去掉重复的元素
        Set<String> set = new HashSet<>(studentNumberList);
        studentNumberList = new ArrayList<>(set);

        List<String> nameList = new ArrayList<>();
        if (studentNumberList != null && studentNumberList.size() > 0) {
            //获取学号对应的学生姓名
            nameList = studentInfoMapper.getnameList(studentNumberList, classNumber, school);
        } else {
            return null;
        }
        //再反过来查询一遍学号，避免学号和姓名对不上
        if (nameList != null && nameList.size() > 0) {
            studentNumberList = studentInfoMapper.getnewStudentNumberList(nameList, classNumber, school);
        } else {
            return null;
        }

        return nameList;









    }

//    /**
//     * 快速查询未完成作业名单
//     * @param classNumber
//     * @param checkdate
//     * @return
//     */
//    @Override
//    public String quickUncompleted(String classNumber, LocalDate checkdate,String school) {
//
//        Integer uncompleted = 0;
//        String isoDate = checkdate.format(DateTimeFormatter.ISO_LOCAL_DATE);
//
//        String feedback = "";
//        String querysubject1 = "语文";
//        String feedbackchinese = feedbackgene(uncompleted, classNumber, checkdate, isoDate, querysubject1,school);
//        feedback += feedbackchinese;
//        String querysubject2 = "数学";
//        String feedbackmath = feedbackgene(uncompleted, classNumber, checkdate, isoDate, querysubject2,school);
//        feedback += feedbackmath;
//        String querysubject3 = "英语";
//        String feedbackenglish = feedbackgene(uncompleted, classNumber, checkdate, isoDate, querysubject3,school);
//        feedback += feedbackenglish;
//        return feedback;
//    }


    //    统计元素出现的频次
    private static Map<String, Integer> getElementFrequency(List<String> elements) {
        Map<String, Integer> frequencyMap = new HashMap<>();
        for (String element : elements) {
            frequencyMap.put(element, frequencyMap.getOrDefault(element, 0) + 1);
        }
        return frequencyMap;
    }

    private List<PageQueryNameVO> getpageNameVO(List<Record> recordList) {
        if (recordList != null && recordList.size() > 0) {
            ArrayList<List<String>> listArrayList = new ArrayList<>();
            ArrayList<String> uncompletedcontentlist = new ArrayList<>();
            ArrayList<String> wellcontentlist = new ArrayList<>();
            ArrayList<String> badcontentlist = new ArrayList<>();
            listArrayList.add(uncompletedcontentlist);
            listArrayList.add(wellcontentlist);
            listArrayList.add(badcontentlist);
            PageQueryNameVO pageQueryNameVO = new PageQueryNameVO();
            List<PageQueryNameVO> pageQueryNameVOS = new ArrayList<>();
            HashMap<String, List<List<String>>> namecontentmap = new HashMap<>();
            for (Record record : recordList) {
                if (record.getCompleted() != null && record.getCompleted() == 0) {
                    String pageAndSupplementary = record.getSubject() + "《" + record.getContent() + "》";
                    if (record.getStartpage() != null) {
                        pageAndSupplementary += "P" + record.getStartpage() + "-" + record.getEndpage();
                    }
                    if (record.getSupplementary() != null && !record.getSupplementary().isEmpty()) {
                        pageAndSupplementary += "----" + record.getSupplementary();
                    }
                    listArrayList.get(0).add(pageAndSupplementary + "、");
                    namecontentmap.put(record.getName(), listArrayList);
                }
                if (record.getLevel() != null && record.getLevel() == 1) {
                    String pageAndSupplementary = record.getSubject() + "《" + record.getContent() + "》";
                    if (record.getStartpage() != null) {
                        pageAndSupplementary += "P" + record.getStartpage() + "-" + record.getEndpage();
                    }
                    if (record.getSupplementary() != null && !record.getSupplementary().isEmpty()) {
                        pageAndSupplementary += "----" + record.getSupplementary();
                    }
                    listArrayList.get(1).add(pageAndSupplementary + "、");
                    namecontentmap.put(record.getName(), listArrayList);
                }
                if (record.getLevel() != null && record.getLevel() == -1) {
                    String pageAndSupplementary = record.getSubject() + "《" + record.getContent() + "》";
                    if (record.getStartpage() != null) {
                        pageAndSupplementary += "P" + record.getStartpage() + "-" + record.getEndpage();
                    }
                    if (record.getSupplementary() != null && !record.getSupplementary().isEmpty()) {
                        pageAndSupplementary += "----" + record.getSupplementary();
                    }
                    listArrayList.get(2).add(pageAndSupplementary + "、");
                    namecontentmap.put(record.getName(), listArrayList);
                }
            }
            String name = recordList.get(0).getName();
            String classNumber = recordList.get(0).getClassNumber();
            String subject = "数学";
            AccumulateScore accumulateScoremath = accumulateScoreService.getByNameClass(name, classNumber, subject);
            subject = "语文";
            AccumulateScore accumulateScorechinese = accumulateScoreService.getByNameClass(name, classNumber, subject);
            subject = "英语";
            AccumulateScore accumulateScoreenglish = accumulateScoreService.getByNameClass(name, classNumber, subject);
            Integer addScore = 0;
            Integer minusScore = 0;
            Integer punishScore = 0;
            if (accumulateScoremath != null) {

                addScore += accumulateScoremath.getAddscore();
                punishScore += accumulateScoremath.getPunishscore();
                minusScore += accumulateScoremath.getMinusscore();
            }
            if (accumulateScorechinese != null) {
                addScore += accumulateScorechinese.getAddscore();
                punishScore += accumulateScorechinese.getPunishscore();
                minusScore += accumulateScorechinese.getMinusscore();
            }
            if (accumulateScoreenglish != null) {
                addScore += accumulateScoreenglish.getAddscore();
                punishScore += accumulateScoreenglish.getPunishscore();
                minusScore += accumulateScoreenglish.getMinusscore();
            }


            //对日期的hashmap进行遍历
            for (Map.Entry<String, List<List<String>>> entryname : namecontentmap.entrySet()) {

                pageQueryNameVO.setName(entryname.getKey());
                pageQueryNameVO.setCheckdate(recordList.get(0).getCheckdate());
                pageQueryNameVO.setUncompletecontent(entryname.getValue().get(0));
                pageQueryNameVO.setWellcontent(entryname.getValue().get(1));
                pageQueryNameVO.setBadcontent(entryname.getValue().get(2));
                pageQueryNameVO.setAddscore(addScore);
                pageQueryNameVO.setPunishscore(punishScore);
                pageQueryNameVO.setConvertscore(minusScore - punishScore);
                pageQueryNameVO.setSumscore(addScore + minusScore);
                pageQueryNameVOS.add(pageQueryNameVO);

            }
            return pageQueryNameVOS;
        } else {
            return null;
        }
    }

    /**
     * 家长端查询学生个人作业数据
     * @param pageQueryClassDTO
     * @return
     */
    @Override
    public List<PageQueryClassVO> pageStudent(PageQueryClassDTO pageQueryClassDTO) {
        String classNumber = pageQueryClassDTO.getClassNumber();
        String subject = pageQueryClassDTO.getSubject();
        String content = pageQueryClassDTO.getContent();
        LocalDate enddate = pageQueryClassDTO.getEnddate();
        LocalDate startdate = pageQueryClassDTO.getStartdate();
        String school = pageQueryClassDTO.getSchool();
        String name = pageQueryClassDTO.getStudentName();


        List<Record> recordList = recordMapper.pageQueryStudent(startdate, enddate, subject, content, classNumber, school,name);
//        将相同作业类型的content放一起
        Map<SubjectContentCheckDateKey, List<Record>> groupedrecord = recordList.stream()
                .collect(Collectors.groupingBy(record -> new SubjectContentCheckDateKey(record.getSubject(), record.getContent(), record.getCheckdate())));
        List<PageQueryClassVO> pageQueryClassVOS = new ArrayList<>();
        for (Map.Entry<SubjectContentCheckDateKey, List<Record>> entry : groupedrecord.entrySet()) {
            List<PageQueryClassVO> result = getpageVO(entry.getValue());
            pageQueryClassVOS.addAll(result);
        }
        // 自定义比较器，先按日期倒序排序，然后按学科倒序排序
        Comparator<PageQueryClassVO> comparator = Comparator.comparing(PageQueryClassVO::getCheckdate, Comparator.reverseOrder())
                .thenComparing(PageQueryClassVO::getSubject, Comparator.reverseOrder());

        // 使用Collections.sort()方法和自定义比较器对records进行排序
        Collections.sort(pageQueryClassVOS, comparator);

        return pageQueryClassVOS;

    }

    @Override
    public String uploadmessageHomeWork(String content,LocalDate checkdate ,String message, String school, String classNumber, String subject, String supplementary, String minusScoreByHomework, String failRevisionAddScore, Integer addScoreNumber, Integer minusScoreNumber,String completedRevisionAddScore,Integer revisionAddScore) throws Exception {

        message = replaceLastDingzheng(message);



//        String jsonFeedback = baiWenXinUtills.getJsonMessageHomeWork(message);
        String jsonFeedback = deepSeekUtills.getJsonMessageHomeWork(message);
        String jsonString = jsonFeedback.replaceAll("(?s)```json\\s*(\\{.*?\\})\\s*```", "$1");
        ObjectMapper mapper = new ObjectMapper();
        M_HomeworkAudio mHomeworkAudio = mapper.readValue(jsonString, M_HomeworkAudio.class);
        System.out.println(mHomeworkAudio);

        String testJson = mHomeworkAudio.getCheckdateStr()+":"+mHomeworkAudio.getNamelistStr()+":"+mHomeworkAudio.getTypeStr();


        if(mHomeworkAudio.getTypeStr().equals("无")){
            return "未识别出作业状态，请重新上传语音";
        }
        if(mHomeworkAudio.getNamelistStr().equals("无")){
            return "未识别出学生名单，请重新上传语音";
        }
        if(mHomeworkAudio.getHomeworkName().equals("无")){
            return "未识别出作业内容，请重新上传语音";
        }

        //首先对日期的字符串处理，把汉字的处理为数字，同时要加上当前的年份，并返回为标准的日期格式，方便后续的数据库存储和查询。
        LocalDate parsecheckdate = parseCheckDateStr(mHomeworkAudio.getCheckdateStr());

        if(parsecheckdate != null){
            checkdate = parsecheckdate;
        }
        List<String> valuelist = parseNameListStr(mHomeworkAudio.getNamelistStr(),classNumber,school);
        List<String> contetnList = homeWorkContentMapper.queryContentByNameSubjectSchool(school,classNumber,subject);
        if(contetnList == null || contetnList.size() == 0){
            return "未在作业内容表找到相关内容，请重新上传语音";
        }


        if(content == null || content.equals("")){
            content = parseHomeworkNameAndMatch(mHomeworkAudio.getHomeworkName(),classNumber,school,subject,contetnList);
        }

        if(valuelist == null || valuelist.size() == 0){
            return "未在学生信息表找到最佳匹配，请重新上传语音";
        }

        String resp = "";
        if(mHomeworkAudio.getTypeStr().equals("完成作业") ){
            //构造recordDTO
            RecordDTO recordDTO = new RecordDTO();
            recordDTO.setClassNumber(classNumber);
            recordDTO.setValueList(valuelist);
            recordDTO.setSubject(subject);
            recordDTO.setContent(content);
            recordDTO.setSupplementary(supplementary);
            recordDTO.setCompleted(1);
            recordDTO.setCheckdate(checkdate);
            recordDTO.setSchool(school);

            resp = uploadFeedback(recordDTO);

        } else if (mHomeworkAudio.getTypeStr().equals("未提交作业")) {

            //构造recordDTO
            RecordDTO recordDTO = new RecordDTO();
            recordDTO.setClassNumber(classNumber);
            recordDTO.setValueList(valuelist);
            recordDTO.setSubject(subject);
            recordDTO.setContent(content);
            recordDTO.setSupplementary(supplementary);
            recordDTO.setCompleted(0);
            recordDTO.setCheckdate(checkdate);
            recordDTO.setSchool(school);

            resp = uploadFeedback(recordDTO);
        } else if (mHomeworkAudio.getTypeStr().equals("优秀作业")) {
            //构造recordDTO
            RecordDTO recordDTO = new RecordDTO();
            recordDTO.setClassNumber(classNumber);
            recordDTO.setValueList(valuelist);
            recordDTO.setSubject(subject);
            recordDTO.setContent(content);
            recordDTO.setSupplementary(supplementary);
            recordDTO.setLevel(1);
            recordDTO.setCheckdate(checkdate);
            recordDTO.setSchool(school);
            recordDTO.setAddScoreNumber(addScoreNumber);
            if(minusScoreByHomework.equals("是")){
                recordDTO.setScore(true);
            } else {
                recordDTO.setScore(false);
            }
            resp = uploadFeedbackSpecial(recordDTO);
        }else if (mHomeworkAudio.getTypeStr().equals("不达标作业")) {
            RecordDTO recordDTO = new RecordDTO();
            recordDTO.setClassNumber(classNumber);
            recordDTO.setValueList(valuelist);
            recordDTO.setSubject(subject);
            recordDTO.setContent(content);
            recordDTO.setSupplementary(supplementary);
            recordDTO.setLevel(-1);
            recordDTO.setCheckdate(checkdate);
            recordDTO.setSchool(school);
            recordDTO.setMinusScoreNumber(minusScoreNumber);
            if(minusScoreByHomework.equals("是")){
                recordDTO.setScore(true);
            } else {
                recordDTO.setScore(false);
            }
            resp = uploadFeedbackSpecial(recordDTO);
        }else if (mHomeworkAudio.getTypeStr().equals("订正作业")) {
            RecordDTO recordDTO = new RecordDTO();
            recordDTO.setClassNumber(classNumber);
            recordDTO.setValueList(valuelist);
            recordDTO.setSubject(subject);
            recordDTO.setContent(content);
            recordDTO.setSupplementary(supplementary);
            recordDTO.setCheckdate(checkdate);
            recordDTO.setSchool(school);
            recordDTO.setFailRevisionAddScore(failRevisionAddScore);
            recordDTO.setCompletedRevisionAddScore(completedRevisionAddScore);
            recordDTO.setRevisionAddScore(revisionAddScore);

            resp = homeworkRevison(recordDTO);
        }else {
            return "未识别出作业状态，请重新上传语音";
        }

        if(resp.equals("")){
            return "没有登记任何记录";
        }


        return  resp;
    }

    /**
     * 手机端修改作业结果
     * @param recordDTO
     * @return
     */
    @Override
    public String alterHomeworkResult(RecordDTO recordDTO) throws Exception {
        //根据拿到的姓名，构建扫码的值
        String studentNumber = studentInfoMapper.getStudentNumberByName(recordDTO.getClassNumber(),recordDTO.getStudentName(),recordDTO.getSchool());
        String value = studentNumber+"/"+recordDTO.getClassNumber()+"--";
        recordDTO.setValueList(List.of(value));
        if(recordDTO.getFromCategory().equals("未提交")){
            recordDTO.setCompleted(1);
            uploadFeedback(recordDTO);

        }else {
            recordDTO.setCompleted(1);
            recordDTO.setScore(true);
            homeworkRevison(recordDTO);





        }
        return "成功修改手机端数据";
    }

    @Override
    public String alterHomeworkData(AlterHomeworkDataDTO alterHomeworkDataDTO) {
        return "";
    }

    private String replaceLastDingzheng(String message) {
        String target = "丁正";
        String replacement = "订正";

        // 找到最后一个 target 的位置
        int lastIndex = message.lastIndexOf(target);

        if (lastIndex != -1) {
            // 将字符串分割并替换
            String before =  message.substring(0, lastIndex);
            String after =  message.substring(lastIndex + target.length());
            return before + replacement + after;
        }
        // 如果没有找到，返回原字符串
        return  message;
    }

    private String parseHomeworkNameAndMatch(String homeworkName, String classNumber, String school, String subject,List<String> contentList) {
        List<String> contentPinyinList  = new ArrayList<>();
        for(String content : contentList){
            String contentpinyin = ChineseNameToPinyin.convertToPinyin(content);
            contentPinyinList.add(contentpinyin);
        }
        String bestMatch = findBestMatch(homeworkName, contentPinyinList);
        String contentChinese = contentList.get(contentPinyinList.indexOf(bestMatch));
        return contentChinese;
    }

    /**
     * 查询反馈班级表格数据
     *
     * @param pageQueryClassDTO
     */
    @Override
    public List<PageQueryClassVO> pageClass(PageQueryClassDTO pageQueryClassDTO) {
        String classNumber = pageQueryClassDTO.getClassNumber();
        String subject = pageQueryClassDTO.getSubject();
        String content = pageQueryClassDTO.getContent();
        LocalDate enddate = pageQueryClassDTO.getEnddate();
        LocalDate startdate = pageQueryClassDTO.getStartdate();
        String school = pageQueryClassDTO.getSchool();


        List<Record> recordList = recordMapper.pageQueryClass(startdate, enddate, subject, content, classNumber, school);
//        将相同作业类型的content放一起
        Map<SubjectContentCheckDateKey, List<Record>> groupedrecord = recordList.stream()
                .collect(Collectors.groupingBy(record -> new SubjectContentCheckDateKey(record.getSubject(), record.getContent(), record.getCheckdate())));
        List<PageQueryClassVO> pageQueryClassVOS = new ArrayList<>();
        for (Map.Entry<SubjectContentCheckDateKey, List<Record>> entry : groupedrecord.entrySet()) {
            List<PageQueryClassVO> result = getpageVO(entry.getValue());
            pageQueryClassVOS.addAll(result);
        }
        // 自定义比较器，先按日期倒序排序，然后按学科倒序排序
        Comparator<PageQueryClassVO> comparator = Comparator.comparing(PageQueryClassVO::getCheckdate, Comparator.reverseOrder())
                .thenComparing(PageQueryClassVO::getSubject, Comparator.reverseOrder());

        // 使用Collections.sort()方法和自定义比较器对records进行排序
        Collections.sort(pageQueryClassVOS, comparator);

        return pageQueryClassVOS;


    }


    //遍历得到未完成、优秀姓名列表,订正列表
    private List<PageQueryClassVO> getpageVO(List<Record> recordList) {

        ArrayList<List<String>> listArrayList = new ArrayList<>();
        ArrayList<String> uncompletedlist = new ArrayList<>();
        ArrayList<String> welllist = new ArrayList<>();
        ArrayList<String> badlist = new ArrayList<>();
        ArrayList<String> revisionListGeneral = new ArrayList<>();
        ArrayList<String> revisionListfail = new ArrayList<>();
        listArrayList.add(uncompletedlist);
        listArrayList.add(welllist);
        listArrayList.add(badlist);
        listArrayList.add(revisionListGeneral);
        listArrayList.add(revisionListfail);
        PageQueryClassVO pageQueryClassVO = new PageQueryClassVO();
        List<PageQueryClassVO> pageQueryClassVOS = new ArrayList<>();
        HashMap<String, List<List<String>>> contentsubjectmap = new HashMap<>();

        HashMap<LocalDate, HashMap<String, List<List<String>>>> checkdatemap = new HashMap<>();
        HashMap<String, String> contentSupplementarymap = new HashMap<>();
        for (Record record : recordList) {
            if (record.getCompleted() != null && record.getCompleted() == 0) {
                listArrayList.get(0).add(record.getName() + "、");
                contentsubjectmap.put(record.getSubject() + record.getContent(), listArrayList);
                checkdatemap.put(record.getCheckdate(), contentsubjectmap);
            }
            if (record.getLevel() != null && record.getLevel() == 1) {
                listArrayList.get(1).add(record.getName() + "、");
                contentsubjectmap.put(record.getSubject() + record.getContent(), listArrayList);
                checkdatemap.put(record.getCheckdate(), contentsubjectmap);
            }
            if (record.getLevel() != null && record.getLevel() == -1) {
                listArrayList.get(2).add(record.getName() + "、");
                contentsubjectmap.put(record.getSubject() + record.getContent(), listArrayList);
                checkdatemap.put(record.getCheckdate(), contentsubjectmap);
            }
            if(record.getLevel() == null || record.getLevel() == 0) {
                if (record.getCompleted() != null && record.getCompleted() == 1 && (record.getRevision() == null || record.getRevision() == false )) {
                    listArrayList.get(3).add(record.getName() + "、");
                    contentsubjectmap.put(record.getSubject() + record.getContent(), listArrayList);
                    checkdatemap.put(record.getCheckdate(), contentsubjectmap);
                }
            }
            if(record.getLevel() != null && record.getLevel() == -1 ){
                if (record.getCompleted() != null && record.getCompleted() == 1 && (record.getRevision() == null || record.getRevision() == false)) {
                    listArrayList.get(4).add(record.getName() + "、");
                    contentsubjectmap.put(record.getSubject() + record.getContent(), listArrayList);
                    checkdatemap.put(record.getCheckdate(), contentsubjectmap);
                }
            }
            String pageAndSupplementary = "";
            if (record.getStartpage() != null) {
                pageAndSupplementary += "P" + record.getStartpage() + "-" + record.getEndpage();
            }
            if (record.getSupplementary() != null && !record.getSupplementary().isEmpty()) {
                pageAndSupplementary += "----" + record.getSupplementary();
            }
            String supplementary = record.getSupplementary();
//            contentSupplementarymap.put(record.getSubject() + record.getContent(), pageAndSupplementary);
            contentSupplementarymap.put(record.getSubject() + record.getContent(), supplementary);

        }

        //对日期的hashmap进行遍历
        for (Map.Entry<LocalDate, HashMap<String, List<List<String>>>> entrycheckdate : checkdatemap.entrySet()) {
            LocalDate checkdate = entrycheckdate.getKey();
            HashMap<String, List<List<String>>> contentmap = entrycheckdate.getValue();
            for (Map.Entry<String, List<List<String>>> entrycontent : contentmap.entrySet()) {
                String subjectcontent = entrycontent.getKey();
                List<List<String>> alllist = entrycontent.getValue();
                if (subjectcontent.substring(0, 2).equals("语文")) {
                    pageQueryClassVO.setSubject("语文");
                } else if (subjectcontent.substring(0, 2).equals("数学")) {
                    pageQueryClassVO.setSubject("数学");
                } else {
                    pageQueryClassVO.setSubject("英语");
                }
                pageQueryClassVO.setPageAndSupplementary(contentSupplementarymap.get(subjectcontent));
                pageQueryClassVO.setSupplementary(contentSupplementarymap.get(subjectcontent));
                pageQueryClassVO.setCheckdate(checkdate);
                pageQueryClassVO.setContent(subjectcontent.substring(2));
                pageQueryClassVO.setUncompletelist(alllist.get(0));
                pageQueryClassVO.setWelllist(alllist.get(1));
                pageQueryClassVO.setBadlist(alllist.get(2));
                pageQueryClassVO.setRevisionListGeneral(alllist.get(3));
                pageQueryClassVO.setRevisionListFail(alllist.get(4));
                pageQueryClassVOS.add(pageQueryClassVO);
            }
        }

        return pageQueryClassVOS;
    }


    private String feedbackgene(Integer uncompleted, String classNumber, LocalDate checkdate, String isoDate, String subject, String school) {

        List<Record> uncompletedRecordList = recordMapper.getalluncompleted(uncompleted, classNumber, checkdate, subject, school);
        Map<String, List<Record>> groupedByContent = uncompletedRecordList.stream()
                .collect(Collectors.groupingBy(Record::getContent));

        HashMap<String, List<String>> contentUncompletedMap = new HashMap<>();
        for (Map.Entry<String, List<Record>> entry : groupedByContent.entrySet()) {
            List<String> uncopletedNameList = new ArrayList<>();
            for (Record record : entry.getValue()) {
                uncopletedNameList.add(record.getName());
                contentUncompletedMap.put(record.getContent(), uncopletedNameList);
            }
        }


        //生成反馈信息，不管怎么样子，这个信息要生成

        String feedbacknotification = "";
        if (contentUncompletedMap != null && !contentUncompletedMap.isEmpty()) {

            for (Map.Entry<String, List<String>> entry : contentUncompletedMap.entrySet()) {
                feedbacknotification += isoDate + subject + "《" + entry.getKey() + "》" + "<br>";

                feedbacknotification += "没有登记完成的同学有:" + "<br>";
                feedbacknotification += String.join("、", entry.getValue());
                feedbacknotification += "<br><br>";
            }
        } else {
            feedbacknotification += "全部同学完成了" + subject + "作业" + "<br><br>";
        }
        return feedbacknotification;
    }

    ;


}
