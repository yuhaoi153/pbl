package flyfish.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import flyfish.mapper.*;
import flyfish.pojo.M_WellBadHomeworkPerform;
import flyfish.pojo.Perform;
import flyfish.service.AIService;
import flyfish.utils.BaiWenXinUtills;
import flyfish.utils.ChineseNameToPinyin;
import flyfish.utils.CommonBaiDuWenXinUtills;
import org.apache.commons.text.similarity.LevenshteinDistance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


@Service
public class AIServiceImpl implements AIService {

    @Autowired
    private BaiWenXinUtills baiWenXinUtills;
    @Autowired
    private StudentInfoMapper studentInfoMapper;
    @Autowired
    private PerformMapper performMapper;
    @Autowired
    private AccumulateScoreMapper accumulateScoreMapper;
    @Autowired
    private ChineseNameToPinyin chineseNameToPinyin;
    @Autowired
    private GroupNameListMapper groupNameListMapper;
    @Autowired
    private CommonBaiDuWenXinUtills commonBaiDuWenXinUtills;
    @Autowired
    private M_WellBadHomeworkPerformMapper m_wellBadHomeworkPerformMapper;


    /**
     * 调用AI接口
     * @param classNumber
     * @param subject
     * @param message
     */
    @Override
    public String aiPerform(String classNumber, String subject, String message,String school,Integer year) {

        //处理AI返回的json数据
        try {
            String jsonFeedback = baiWenXinUtills.getJsonFeedback(message);
            String jsonString = jsonFeedback.replaceAll("(?s)```json\\s*(\\{.*?\\})\\s*```", "$1");
            ObjectMapper mapper = new ObjectMapper();
            Perform perform = mapper.readValue(jsonString, Perform.class);



            String targetPinyin = perform.getName(); // 目标拼音，可以是yuwenzi、yu wen zi等
            List<String> pinyinList = studentInfoMapper.getPinyin(classNumber,school); // 从数据库中获取的拼音列表
            String bestMatch = findBestMatch(targetPinyin.replaceAll("\\s+", ""), pinyinList);
            System.out.println("最佳匹配: " + bestMatch);
            String name = studentInfoMapper.getNameByPinyin(bestMatch,classNumber,school);
            perform.setName(name);
            perform.setClassNumber(classNumber);
            perform.setSubject(subject);
            perform.setScore(1);
            LocalDate checkdate = LocalDate.now();
            perform.setCheckdate(checkdate);
            perform.setSchool(school);
            perform.setYear(year);




            performMapper.addSinglePerformReturnId(perform);
            if(perform.getSituation().equals("表扬")){
                accumulateScoreMapper.updateNFCwellScore(name,classNumber,subject,school);
                return name+"因为"+perform.getReason()+"积分+1";
            }else {
                accumulateScoreMapper.updateNFCbadScore(name,classNumber,subject,school);
                if(perform.getPunishMeasures() == null || perform.getPunishMeasures().equals("无")){
                    return name+"因为"+perform.getReason()+"积分减1";
                }
                M_WellBadHomeworkPerform wellBadHomeworkPerform = new M_WellBadHomeworkPerform();
                wellBadHomeworkPerform.setStudentName(name);
                wellBadHomeworkPerform.setScorePerformId(perform.getId());
                wellBadHomeworkPerform.setSituation(perform.getReason());
                wellBadHomeworkPerform.setClassName(classNumber);
                wellBadHomeworkPerform.setSchool(school);
                wellBadHomeworkPerform.setYear(year);
                wellBadHomeworkPerform.setCheckDate(checkdate);
                wellBadHomeworkPerform.setSupplementary(perform.getPunishMeasures());
                wellBadHomeworkPerform.setSubject(subject);

                m_wellBadHomeworkPerformMapper.insertSingleRecord(wellBadHomeworkPerform);
                return name+"因为"+perform.getReason()+"积分减1并且"+wellBadHomeworkPerform.getSupplementary();


            }




        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;


    }

    /**
     * 调用AI接口分组
     * @param classNumber
     * @param subject
     * @param message
     */
    @Override
    public String group(String classNumber, String subject, String message,String school) {
        String[] originGroup= message.split("组");
        //转化为列表
        List<String> originGroup2 = Arrays.asList(originGroup);
        String[] originGroupNumber = originGroup2.get(0).split("第");
        String groupNumber = originGroupNumber[1];
        if(groupNumber.contains("一")){
            groupNumber = "1";}
        if (groupNumber.contains("二")){
            groupNumber = "2";}
        if (groupNumber.contains("三")){ groupNumber = "3";}
        if (groupNumber.contains("四")){ groupNumber = "4";}
        if (groupNumber.contains("五")){ groupNumber = "5";}
        if (groupNumber.contains("六")){ groupNumber = "6";}
        if (groupNumber.contains("七")){ groupNumber = "7";}
        if (groupNumber.contains("八")){ groupNumber = "8";}
        if (groupNumber.contains("九")){ groupNumber = "9";}
        if (groupNumber.contains("十")){ groupNumber = "10";}
        if (groupNumber.contains("十一")){ groupNumber = "11";}
        if (groupNumber.contains("十二")){ groupNumber = "12";}
        if (groupNumber.contains("十三")){ groupNumber = "13";}
        if (groupNumber.contains("十四")){ groupNumber = "14";}
        if (groupNumber.contains("十五")){ groupNumber = "15";}
        //判断组后是否有内容
        if(originGroup2.size() == 1){
            return "小组没有设置成员";
        }

        if (!originGroup2.get(1).contains("和")){
            List<String> studentList = new ArrayList<>();
            studentList.add(originGroup2.get(1));
        }


        //获得组后面的学生名单
        String[] originStudentList = originGroup2.get(1).split("和");
        List<String> studentList = Arrays.asList(originStudentList);
        List<String> pinyinList = chineseNameToPinyin.convertNamesToPinyin(studentList);

        List<String> nameList = studentInfoMapper.getPinyin(classNumber,school);
        List<String> nameListChinese = new ArrayList<>();
        for (String pinyin : pinyinList) {
            String bestMatch = findBestMatch(pinyin, nameList);
            String chineseName = studentInfoMapper.getNameByPinyin(bestMatch,classNumber,school);
            nameListChinese.add(chineseName);
        }
        String nameListString = String.join("、", nameListChinese);



        //把组别，班级和学生名单存入数据库

        //先删除当前班级当前小组
        groupNameListMapper.deleteGroup(classNumber,groupNumber,subject,school);
        //再新增当前小组
        LocalDateTime createTime = LocalDateTime.now();
        groupNameListMapper.addGroup(classNumber,groupNumber,nameListString,subject,createTime,school);

        return nameListString;






    }

    @Override
    public String groupPerform(String classNumber, String subject, String message,String school,Integer year) {
        String[] split = message.split("组");
        String groupNumber = split[0].split("第")[1];
        if(groupNumber.contains("一")){
            groupNumber = "1";}
        if (groupNumber.contains("二")){
            groupNumber = "2";}
        if (groupNumber.contains("三")){ groupNumber = "3";}
        if (groupNumber.contains("四")){ groupNumber = "4";}
        if (groupNumber.contains("五")){ groupNumber = "5";}
        if (groupNumber.contains("六")){ groupNumber = "6";}
        if (groupNumber.contains("七")){ groupNumber = "7";}
        if (groupNumber.contains("八")){ groupNumber = "8";}
        if (groupNumber.contains("九")){ groupNumber = "9";}
        if (groupNumber.contains("十")){ groupNumber = "10";}
        if (groupNumber.contains("十一")){ groupNumber = "11";}
        if (groupNumber.contains("十二")){ groupNumber = "12";}
        if (groupNumber.contains("十三")){ groupNumber = "13";}
        if (groupNumber.contains("十四")){ groupNumber = "14";}
        if (groupNumber.contains("十五")){ groupNumber = "15";}

        if(split[1].contains("加分")){
            String nameListString = groupNameListMapper.getNameList(classNumber,groupNumber,subject,school);
            if(nameListString == null || nameListString.equals("")){
                return "小组成员为0";
            }
            String[] nameList = nameListString.split("、");

            for (String name : nameList) {
                Perform perform = new Perform();
                perform.setName(name);
                perform.setClassNumber(classNumber);
                perform.setSubject(subject);
                perform.setScore(1);
                perform.setCheckdate(LocalDate.now());
                perform.setSituation("表扬");
                perform.setReason("小组表现优秀");
                perform.setSchool(school);
                perform.setYear(year);
                performMapper.addSinglePerform(perform);
                accumulateScoreMapper.updateNFCwellScore(name,classNumber,subject,school);
            }
            groupNameListMapper.addGroupScore(classNumber,groupNumber,subject,1,school);
            return ("第"+groupNumber+"组表现优秀，积分+1");
        }else if(split[1].contains("减分") || split[1].contains("扣分")) {
            String nameListString = groupNameListMapper.getNameList(classNumber, groupNumber,subject,school);
            String[] nameList = nameListString.split("、");
            if (nameList.length == 0) {
                return "小组成员为0";
            }
            for (String name : nameList) {
                Perform perform = new Perform();
                perform.setName(name);
                perform.setClassNumber(classNumber);
                perform.setSubject(subject);
                perform.setScore(-1);
                perform.setCheckdate(LocalDate.now());
                perform.setSituation("批评");
                perform.setReason("小组表现不佳");
                perform.setSchool(school);
                perform.setYear(year);
                performMapper.addSinglePerform(perform);
                accumulateScoreMapper.updateNFCbadScore(name, classNumber, subject,school);
            }
            groupNameListMapper.punishGroupScore(classNumber,groupNumber,subject,1,school);
            return ("第" + groupNumber + "组表现不佳，积分减1");
        }else {
            return "无法识别";
        }


    }

    @Override
    public String quickPerform(String classNumber, String subject, String message,String school,Integer year) {
        String[] praiseSplit = message.split("表扬");
        List<String> praiseList = Arrays.asList(praiseSplit);
        String[] punishSplit = message.split("批评");
        List<String> punishList = Arrays.asList(punishSplit);
        if(praiseList.size() == 1 && punishList.size() == 1){
            return "无法识别";
        } else if (punishList.size()==1) {
            String nameListString = praiseList.get(1);
            String[] nameSplit = nameListString.split("和");
            List<String> studentList= Arrays.asList(nameSplit);
            List<String> pinyinList = chineseNameToPinyin.convertNamesToPinyin(studentList);
            //拿到班级所有学生的拼音
            List<String> nameList = studentInfoMapper.getPinyin(classNumber,school);
            List<String> nameListChinese = new ArrayList<>();
            //拿前端的拼音逐一匹配
            for (String pinyin : pinyinList) {
                String bestMatch = findBestMatch(pinyin, nameList);
                String chineseName = studentInfoMapper.getNameByPinyin(bestMatch,classNumber,school);
                nameListChinese.add(chineseName);
            }
            for(String name : nameListChinese){
                Perform perform = new Perform();
                perform.setName(name);
                perform.setClassNumber(classNumber);
                perform.setSubject(subject);
                perform.setScore(1);
                perform.setCheckdate(LocalDate.now());
                perform.setSituation("表扬");
                perform.setReason("表现优秀");
                perform.setSchool(school);
                perform.setYear(year);
                performMapper.addSinglePerform(perform);
                accumulateScoreMapper.updateNFCwellScore(name,classNumber,subject,school);
            }
            String nameStrings = String.join("、", nameListChinese);
            return nameStrings+"积分加1";

        } else if (praiseList.size()==1){
            String nameListString = punishList.get(1);
            String[] nameSplit = nameListString.split("和");
            List<String> studentList= Arrays.asList(nameSplit);
            List<String> pinyinList = chineseNameToPinyin.convertNamesToPinyin(studentList);
            //拿到班级所有学生的拼音
            List<String> nameList = studentInfoMapper.getPinyin(classNumber,school);
            List<String> nameListChinese = new ArrayList<>();
            //拿前端的拼音逐一匹配
            for (String pinyin : pinyinList) {
                String bestMatch = findBestMatch(pinyin, nameList);
                String chineseName = studentInfoMapper.getNameByPinyin(bestMatch,classNumber,school);
                nameListChinese.add(chineseName);
            }
            for(String name : nameListChinese){
                Perform perform = new Perform();
                perform.setName(name);
                perform.setClassNumber(classNumber);
                perform.setSubject(subject);
                perform.setScore(-1);
                perform.setCheckdate(LocalDate.now());
                perform.setSituation("批评");
                perform.setReason("表现不佳");
                perform.setSchool(school);
                perform.setYear(year);
                performMapper.addSinglePerform(perform);
                accumulateScoreMapper.updateNFCbadScore(name,classNumber,subject,school);
            }
            String nameStrings = String.join("、", nameListChinese);
            return nameStrings+"积分减1";

        }else {
            return "无法识别";

        }

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
}
