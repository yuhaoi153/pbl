package flyfish.service.impl;

import flyfish.mapper.*;
import flyfish.pojo.AccumulateScore;
import flyfish.pojo.DTO.ScoreDTO;
import flyfish.pojo.Perform;
import flyfish.pojo.Record;
import flyfish.pojo.VO.AccumulateScoreVO;
import flyfish.pojo.VO.M_StudentNamePerformByDateVO;
import flyfish.service.AccumulateScoreService;
import flyfish.utils.ChineseNameToPinyin;
import org.apache.ibatis.annotations.Select;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AccumulateScoreServiceImpl implements AccumulateScoreService {

    @Autowired
    private AccumulateScoreMapper accumulateScoreMapper;
    @Autowired
    private PerformMapper performMapper;
    @Autowired
    private StudentInfoMapper studentInfoMapper;
    @Autowired
    private M_GiftRedemptionMapper giftRedemptionMapper;
    @Autowired
    private M_GradeYearMapper gradeYearMapper;

    /**
     * 检查是否存在分数表
     * @param classNumber
     * @param nameList
     */
    @Override
    public void getNameClass(String classNumber, List<String> nameList, String subject,String school) {

        List<String> accumulateScoreNameList =  accumulateScoreMapper.isexist(classNumber,nameList,subject,school);
        List<String> newnameList = new ArrayList<>(nameList);
        newnameList.removeAll(accumulateScoreNameList);
        //要添加的姓名，不在加分列表中
        if(newnameList != null && newnameList.size()>0){
            List<AccumulateScore> accumulateScoreList = new ArrayList<>();
            for(String name :newnameList){
                AccumulateScore accumulateScore = new AccumulateScore();
                accumulateScore.setName(name);
                accumulateScore.setAddscore(0);
                accumulateScore.setMinusscore(0);
                accumulateScore.setPunishscore(0);
                accumulateScore.setClassNumber(classNumber);
                accumulateScore.setSubject(subject);
                accumulateScore.setCreateTime(LocalDateTime.now());
                accumulateScore.setUpdateTime(LocalDateTime.now());
                accumulateScore.setSchool(school);
                accumulateScoreList.add(accumulateScore);

            }
            accumulateScoreMapper.batchadd(accumulateScoreList);
        }


    }

    /**
     * 加分或者减分操作
     * @param addnumber
     * @param classNumber
     * @param nameList
     * @param subject
     */
    @Override
    public void updatescore(Integer addnumber, String classNumber, List<String> nameList, String subject,String school) {

        LocalDateTime updateTime = LocalDateTime.now();
        if (addnumber > 0) {

            accumulateScoreMapper.updateaddscore(addnumber, classNumber, nameList, subject, updateTime,school);
        } else {
            Integer minusnumber = addnumber;
            accumulateScoreMapper.updateminusscore(minusnumber,classNumber,nameList,subject,updateTime,school);
        }
    }

    /**
     * 查询分数
     * @param name
     * @param classNumber
     * @param subject
     * @return
     */
    @Override
    public AccumulateScore getByNameClass(String name, String classNumber, String subject) {
        AccumulateScore accumulateScoreList = accumulateScoreMapper.getByNameClass(name,classNumber,subject);

        return accumulateScoreList;
    }

    /**
     * 三种方式更新分数
     * @param scoreDTO
     * @param nameList
     * @return
     */
    @Override
    public List<AccumulateScoreVO> threeTypescore(ScoreDTO scoreDTO, List<String> nameList) {
        LocalDateTime updateTime = LocalDateTime.now();
        String classNumber = scoreDTO.getClassNumber();
        String subject = scoreDTO.getSubject();
        Integer scorenumber = scoreDTO.getScorenumber();
        String school = scoreDTO.getSchool();
        Integer year = parseClassNameToYear(classNumber);

        if(scoreDTO.getScoreitem().equals("加分")){

            Integer addscore = scorenumber;

            String situation = "表扬";
            String reason = "课堂表现优秀";
            Integer score = addscore;
            LocalDate checkdate = LocalDate.now();
            performMapper.addPerform(checkdate, nameList, subject, reason, situation, score, classNumber,school,year);


            accumulateScoreMapper.updateaddscore(addscore,classNumber,nameList,subject,updateTime,school);
        }else if(scoreDTO.getScoreitem().equals("惩罚扣分")) {

            String situation = "批评";
            String reason = "课堂表现不佳";
            Integer score = scorenumber;
            LocalDate checkdate = LocalDate.now();
            performMapper.addPerform(checkdate, nameList, subject, reason, situation, score, classNumber,school,year);

            Integer minusnumber = 0 - scorenumber;
            accumulateScoreMapper.updateminusscore(minusnumber,classNumber,nameList,subject,updateTime,school);
        }else {
            Integer minusnumber = 0 - scorenumber;
            accumulateScoreMapper.updateminusnopunishscore(minusnumber,classNumber,nameList,subject,updateTime,school);
        }


        //查询各个学科的每个学生的分数分数
        List<AccumulateScore> accumulateScoreList = accumulateScoreMapper.getAllscore(nameList,scoreDTO.getClassNumber(),school);
        Map<String, List<AccumulateScore>> groupedByName = accumulateScoreList.stream()
                .collect(Collectors.groupingBy(AccumulateScore::getName));
        List<AccumulateScoreVO> accumulateScoreVOList = new ArrayList<>();
        for (Map.Entry<String, List<AccumulateScore>> entry : groupedByName.entrySet()) {
            AccumulateScoreVO accumulateScoreVO = new AccumulateScoreVO();
            accumulateScoreVO.setName(entry.getKey());
            for(AccumulateScore accumulateScore :entry.getValue()){
                if(accumulateScore.getSubject().equals("语文")){
                    accumulateScoreVO.setChineseaddscore(accumulateScore.getAddscore());
                    accumulateScoreVO.setChineseconvertscore(accumulateScore.getMinusscore()-accumulateScore.getPunishscore());
                    accumulateScoreVO.setChinesesumscore(accumulateScore.getAddscore() + accumulateScore.getMinusscore());
                    accumulateScoreVO.setChinesepunishscore(accumulateScore.getPunishscore());
                }
                if(accumulateScore.getSubject().equals("数学")){
                    accumulateScoreVO.setMathaddscore(accumulateScore.getAddscore());
                    accumulateScoreVO.setMathconvertscore(accumulateScore.getMinusscore()-accumulateScore.getPunishscore());
                    accumulateScoreVO.setMathsumscore(accumulateScore.getAddscore() + accumulateScore.getMinusscore());
                    accumulateScoreVO.setMathpunishscore(accumulateScore.getPunishscore());
                }
                if(accumulateScore.getSubject().equals("英语")){
                    accumulateScoreVO.setEnglishaddscore(accumulateScore.getAddscore());
                    accumulateScoreVO.setEnglishconvertscore(accumulateScore.getMinusscore()-accumulateScore.getPunishscore());
                    accumulateScoreVO.setEnglishsumscore(accumulateScore.getAddscore() + accumulateScore.getMinusscore());
                    accumulateScoreVO.setEnglishpunishscore(accumulateScore.getPunishscore());
                }

            }
            accumulateScoreVOList.add(accumulateScoreVO);




        }
        return accumulateScoreVOList;
    }

    /**
     * 加分特殊操作
     * @param scoreDTO
     * @param nameList
     * @param addscoreList
     * @return
     */
    @Override
    public List<AccumulateScoreVO> addscorespecial(ScoreDTO scoreDTO, List<String> nameList, List<Integer> addscoreList) {

        LocalDateTime updateTime = LocalDateTime.now();
        String classNumber = scoreDTO.getClassNumber();
        String subject = scoreDTO.getSubject();
        String school = scoreDTO.getSchool();

        this.getNameClass(classNumber,nameList,subject,school);

        String reason = "课堂表现优秀";
        String situation = "表扬";

        for (int i = 0; i < nameList.size(); i++) {
            Integer score = addscoreList.get(i);
            String name = nameList.get(i);
            Perform perform = new Perform();
            perform.setCheckdate(LocalDate.now());
            perform.setName(name);
            perform.setSubject(subject);
            perform.setReason(reason);
            perform.setSituation(situation);
            perform.setScore(score);
            perform.setClassNumber(classNumber);
            perform.setSchool(school);

            performMapper.addSinglePerform(perform);
        }



        accumulateScoreMapper.updateaddscoreSpecial(addscoreList,classNumber,nameList,subject,updateTime,school);



        //查询各个学科的每个学生的分数分数
        List<AccumulateScore> accumulateScoreList = accumulateScoreMapper.getAllscore(nameList,scoreDTO.getClassNumber(),school);
        Map<String, List<AccumulateScore>> groupedByName = accumulateScoreList.stream()
                .collect(Collectors.groupingBy(AccumulateScore::getName));
        List<AccumulateScoreVO> accumulateScoreVOList = new ArrayList<>();
        for (Map.Entry<String, List<AccumulateScore>> entry : groupedByName.entrySet()) {
            AccumulateScoreVO accumulateScoreVO = new AccumulateScoreVO();
            accumulateScoreVO.setName(entry.getKey());
            for(AccumulateScore accumulateScore :entry.getValue()){
                if(accumulateScore.getSubject().equals("语文")){
                    accumulateScoreVO.setChineseaddscore(accumulateScore.getAddscore());
                    accumulateScoreVO.setChineseconvertscore(accumulateScore.getMinusscore()-accumulateScore.getPunishscore());
                    accumulateScoreVO.setChinesesumscore(accumulateScore.getAddscore() + accumulateScore.getMinusscore());
                    accumulateScoreVO.setChinesepunishscore(accumulateScore.getPunishscore());
                }
                if(accumulateScore.getSubject().equals("数学")){
                    accumulateScoreVO.setMathaddscore(accumulateScore.getAddscore());
                    accumulateScoreVO.setMathconvertscore(accumulateScore.getMinusscore()-accumulateScore.getPunishscore());
                    accumulateScoreVO.setMathsumscore(accumulateScore.getAddscore() + accumulateScore.getMinusscore());
                    accumulateScoreVO.setMathpunishscore(accumulateScore.getPunishscore());
                }
                if(accumulateScore.getSubject().equals("英语")){
                    accumulateScoreVO.setEnglishaddscore(accumulateScore.getAddscore());
                    accumulateScoreVO.setEnglishconvertscore(accumulateScore.getMinusscore()-accumulateScore.getPunishscore());
                    accumulateScoreVO.setEnglishsumscore(accumulateScore.getAddscore() + accumulateScore.getMinusscore());
                    accumulateScoreVO.setEnglishpunishscore(accumulateScore.getPunishscore());
                }

            }
            accumulateScoreVOList.add(accumulateScoreVO);




        }
        return accumulateScoreVOList;
    }

    /**
     * 小程序查询积分榜单
     * @param className
     * @param school
     * @param subject
     * @return
     */
    @Override
    public List<AccumulateScoreVO> queryAllScore(String className, String school, String subject) {
        List<String> nameList = studentInfoMapper.getnameListAll(className,school);
        List<AccumulateScore>  accumulateScoreList = accumulateScoreMapper.getAllscoreBySubject(nameList,className,school,subject);

        List<AccumulateScoreVO> accumulateScoreVOList = new ArrayList<>();


        if(accumulateScoreList!=null && accumulateScoreList.size()>0){
        for (AccumulateScore accumulateScore : accumulateScoreList) {

                AccumulateScoreVO accumulateScoreVO = new AccumulateScoreVO();
                accumulateScoreVO.setName(accumulateScore.getName());
                accumulateScoreVO.setAddScore(accumulateScore.getAddscore());
                accumulateScoreVO.setConvertScore(accumulateScore.getMinusscore()-accumulateScore.getPunishscore());
                accumulateScoreVO.setSumScore(accumulateScore.getAddscore() + accumulateScore.getMinusscore());
                accumulateScoreVO.setPunishScore(accumulateScore.getPunishscore());
                String pinyin = ChineseNameToPinyin.convertToPinyin(accumulateScore.getName());
                accumulateScoreVO.setPinyin(pinyin);
                accumulateScoreVOList.add(accumulateScoreVO);

            }



        }



        return accumulateScoreVOList;
    }

    /**
     * 查询分榜榜单
     * @param className
     * @param school
     * @param subject
     * @param startDate
     * @param endDate
     * @param type
     * @return
     */
    @Override
    public List<AccumulateScoreVO> queryPartScore(String className, String school, String subject, LocalDate startDate, LocalDate endDate, String type) {
        //拿到perform表中当前学科班级的所有记录
        List<Perform> performList = performMapper.getPerformByClassSubject(className,school,subject,startDate,endDate);
        List<Perform> performConvertList = giftRedemptionMapper.getPerformByClassSubject(className,school,subject,startDate,endDate);
        //分别拿到作业和课堂表现数据，并且把兑换情况添加进去
        List<Perform> homeworkPerformList = new ArrayList<>();
        List<Perform> classPerformList = new ArrayList<>();
         for(Perform perform : performList){
            if(perform.getReason().contains("作业")){
                homeworkPerformList.add(perform);
            }else {
                classPerformList.add(perform);}
        }
         homeworkPerformList.addAll(performConvertList);
         classPerformList.addAll(performConvertList);

         List<AccumulateScoreVO> accumulateScoreVOList = new ArrayList<>();
        if (type.equals("作业")) {
             accumulateScoreVOList = sortPerformListGetAddMinusConvert(homeworkPerformList);
        } else {
            accumulateScoreVOList = sortPerformListGetAddMinusConvert(classPerformList);
        }
        return accumulateScoreVOList;
    }

    @Override
    public List<M_StudentNamePerformByDateVO> queryScoreByName(String name, String school, String subject, LocalDate startDate, LocalDate endDate, String className) {
        //拿到perform表中当前学科班级的某个学生所有记录
        List<Perform> performList = performMapper.getPerformByClassSubjectAndName(className,school,subject,startDate,endDate,name);
        List<Perform> performConvertList = giftRedemptionMapper.getPerformByClassSubjectAndName(className,school,subject,startDate,endDate,name);
        //分别拿到作业和课堂表现数据，并且把兑换情况添加进去
        List<Perform> homeworkPerformList = new ArrayList<>();
        List<Perform> classPerformList = new ArrayList<>();
        for(Perform perform : performList){
            if(perform.getReason().contains("作业")){
                homeworkPerformList.add(perform);
            }else {
                classPerformList.add(perform);}
        }

        List<M_StudentNamePerformByDateVO> studentNamePerformByDateVOList = sortStudentPerformByCheckdate(homeworkPerformList,classPerformList,performConvertList);
        return studentNamePerformByDateVOList;
    }

    /**
     * 通过扫码的方式新增积分数据
     * @param scoreDTO
     * @return
     */
    @Override
    public String addScoreByScanner(ScoreDTO scoreDTO) {
        //处理数据（多思考各种异常情况）
        //判断是否初始化:当前日期、班级、作业内容是否已经存在
        String classNumber = scoreDTO.getClassNumber();
        if(scoreDTO.getSchool() == null  || scoreDTO.getSchool().equals("")){
            scoreDTO.setSchool("附小");
        }
        String school = scoreDTO.getSchool();
        //处理value
        String value = String.join("",scoreDTO.getValueList());
        if(value.endsWith("--")){
            value = value.substring(0,value.length()-2);
        }
        String[] parts = value.split("--");
        List<String> studentNumberList = new ArrayList<>();
        for(String part :parts){
            String[] numbers = part.split("/");
            if(numbers.length>0){
                if(numbers.length>1){
                    if(numbers[1].equals(classNumber)){
                        studentNumberList.add(numbers[0]);
                    }
                }

            }
        }
        //去掉重复的元素
        Set<String> set = new HashSet<>(studentNumberList);
        studentNumberList = new ArrayList<>(set);
        List<String> nameList = new ArrayList<>();
        if(studentNumberList !=null && studentNumberList.size()>0){
            //获取学号对应的学生姓名
            nameList = studentInfoMapper.getnameList(studentNumberList,classNumber,school);
        }else {
            return null;//如果是null,就抛异常
        }
        //再反过来查询一遍学号，避免学号和姓名对不上
        if(nameList != null  && nameList.size()>0){
            studentNumberList = studentInfoMapper.getnewStudentNumberList(nameList,classNumber,school);
        }else {
            return null;
        }

        //判断积分表是不是有这些数据，如果没有就初始化
        getNameClass(scoreDTO.getClassNumber(),nameList,scoreDTO.getSubject(),school);
        //新增积分数据
        String resp = addThreeTypeScoreByScanner(scoreDTO,nameList);





        return resp;
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

    private String addThreeTypeScoreByScanner(ScoreDTO scoreDTO, List<String> nameList) {
        LocalDateTime updateTime = LocalDateTime.now();
        String classNumber = scoreDTO.getClassNumber();
        String subject = scoreDTO.getSubject();
        Integer scorenumber = scoreDTO.getScorenumber();
        String school = scoreDTO.getSchool();
        String resp = "";
        if(scoreDTO.getScoreitem().equals("表扬")){

            Integer addscore = scorenumber;

            String situation = "表扬";
            String reason = "课堂表现优秀";
            Integer score = addscore;
            LocalDate checkdate = LocalDate.now();
            Integer year = scoreDTO.getYear();
            performMapper.addPerform(checkdate, nameList, subject, reason, situation, score, classNumber,school,year);
            accumulateScoreMapper.updateaddscore(addscore,classNumber,nameList,subject,updateTime,school);
            return resp += String.join("、",nameList) + "表现优秀，积分+" + scorenumber;
        }else if(scoreDTO.getScoreitem().equals("批评")) {

            String situation = "批评";
            String reason = "课堂表现不佳";
            Integer score = scorenumber;
            LocalDate checkdate = LocalDate.now();
            Integer year = scoreDTO.getYear();
            performMapper.addPerform(checkdate, nameList, subject, reason, situation, score, classNumber,school,year);
            Integer minusnumber = 0 - scorenumber;
            accumulateScoreMapper.updateminusscore(minusnumber,classNumber,nameList,subject,updateTime,school);
            return  resp += String.join("、",nameList) + "表现不佳，积分-" + scorenumber;
        }else {
            Integer minusnumber = 0 - scorenumber;
            String situation = "兑换";
            Integer giftValue = scorenumber;
            String giftName = "礼物";
            LocalDate checkdate = LocalDate.now();
            Integer year = scoreDTO.getYear();
            giftRedemptionMapper.addConvertScore(situation,giftName,giftValue,classNumber,nameList,subject,updateTime,school,year,checkdate);
            accumulateScoreMapper.updateminusnopunishscore(minusnumber,classNumber,nameList,subject,updateTime,school);
            return resp += String.join("、",nameList) + "兑换了礼物，积分-" + scorenumber;
        }
    }

    private List<M_StudentNamePerformByDateVO> sortStudentPerformByCheckdate(List<Perform> homeworkPerformList, List<Perform> classPerformList, List<Perform> performConvertList) {
    List<M_StudentNamePerformByDateVO> studentNamePerformByDateVOList = new ArrayList<>();
    HashMap<LocalDate,List<Perform>> studentPerformByDateMap = new HashMap<>();
    for (Perform perform : homeworkPerformList) {
        if (studentPerformByDateMap.containsKey(perform.getCheckdate())) {
            studentPerformByDateMap.get(perform.getCheckdate()).add(perform);
        } else {
            List<Perform> newList = new ArrayList<>();
            newList.add(perform);
            studentPerformByDateMap.put(perform.getCheckdate(), newList);
        }
    }
    for(Map.Entry<LocalDate,List<Perform>> entry : studentPerformByDateMap.entrySet()){
        M_StudentNamePerformByDateVO studentNamePerformByDateVO = new M_StudentNamePerformByDateVO();
        for (Perform perform : entry.getValue()) {
            if(perform.getSituation().equals("表扬")){
                studentNamePerformByDateVO.setHomeworkPraiseCount(studentNamePerformByDateVO.getHomeworkPraiseCount() == null ? 1 : studentNamePerformByDateVO.getHomeworkPraiseCount() + 1);
            }else if(perform.getSituation().equals("批评")){
                studentNamePerformByDateVO.setHomeworkCriticizeCount(studentNamePerformByDateVO.getHomeworkCriticizeCount() == null ? 1 : studentNamePerformByDateVO.getHomeworkCriticizeCount() + 1);
            }
        }
        if(studentNamePerformByDateVO.getHomeworkPraiseCount()==null){
            studentNamePerformByDateVO.setHomeworkPraiseCount(0);
        }
        if(studentNamePerformByDateVO.getHomeworkCriticizeCount()==null) {
            studentNamePerformByDateVO.setHomeworkCriticizeCount(0);
        }
        studentNamePerformByDateVO.setCheckdate(entry.getKey());
        studentNamePerformByDateVO.setHomeworkCount(studentNamePerformByDateVO.getHomeworkPraiseCount() - studentNamePerformByDateVO.getHomeworkCriticizeCount());
        studentNamePerformByDateVOList.add(studentNamePerformByDateVO);
    }


    //同理对课堂表现分类处理
        HashMap<LocalDate,List<Perform>> studentClassPerformByDateMap = new HashMap<>();
        for (Perform perform : classPerformList) {
            if (studentClassPerformByDateMap.containsKey(perform.getCheckdate())) {
                studentClassPerformByDateMap.get(perform.getCheckdate()).add(perform);
            }else {
                List<Perform> newList = new ArrayList<>();
                newList.add(perform);
                studentClassPerformByDateMap.put(perform.getCheckdate(), newList);
            }
        }
        for(Map.Entry<LocalDate,List<Perform>> entry : studentClassPerformByDateMap.entrySet()){
            M_StudentNamePerformByDateVO studentNamePerformByDateVO = new M_StudentNamePerformByDateVO();
            for (Perform perform : entry.getValue()) {
                if(perform.getSituation().equals("表扬")){
                    studentNamePerformByDateVO.setClassPraiseCount(studentNamePerformByDateVO.getClassPraiseCount() == null ? 1 : studentNamePerformByDateVO.getClassPraiseCount() + 1);
                }else if(perform.getSituation().equals("批评")){
                    studentNamePerformByDateVO.setClassCriticizeCount(studentNamePerformByDateVO.getHomeworkCriticizeCount() == null ? 1 : studentNamePerformByDateVO.getClassCriticizeCount() + 1);
                }
            }
            if(studentNamePerformByDateVO.getClassPraiseCount()==null){
                studentNamePerformByDateVO.setClassPraiseCount(0);
            }
            if(studentNamePerformByDateVO.getClassCriticizeCount()==null) {
                studentNamePerformByDateVO.setClassCriticizeCount(0);}
            studentNamePerformByDateVO.setCheckdate(entry.getKey());
            studentNamePerformByDateVO.setClassCount(studentNamePerformByDateVO.getClassPraiseCount() - studentNamePerformByDateVO.getClassCriticizeCount());
            studentNamePerformByDateVOList.add(studentNamePerformByDateVO);
        }

        //同理对兑换表现处理
        HashMap<LocalDate,List<Perform>> studentConvertPerformByDateMap = new HashMap<>();
        for (Perform perform : performConvertList) {
            if (studentConvertPerformByDateMap.containsKey(perform.getCheckdate())) {
                studentConvertPerformByDateMap.get(perform.getCheckdate()).add(perform);
            } else {
                List<Perform> newList = new ArrayList<>();
                newList.add(perform);
                studentConvertPerformByDateMap.put(perform.getCheckdate(), newList);
            }
        }
        for(Map.Entry<LocalDate,List<Perform>> entry : studentConvertPerformByDateMap.entrySet()){
            M_StudentNamePerformByDateVO studentNamePerformByDateVO = new M_StudentNamePerformByDateVO();
            for (Perform perform : entry.getValue()) {
                if(perform.getSituation().equals("兑换")){
                    studentNamePerformByDateVO.setConvertCount(studentNamePerformByDateVO.getConvertCount() == null ? perform.getScore() : studentNamePerformByDateVO.getConvertCount() + perform.getScore());
                }
            }
            if(studentNamePerformByDateVO.getConvertCount()==null){
                studentNamePerformByDateVO.setConvertCount(0);
            }
            studentNamePerformByDateVO.setCheckdate(entry.getKey());
            studentNamePerformByDateVOList.add(studentNamePerformByDateVO);
        }

        return studentNamePerformByDateVOList;







    }


    private List<AccumulateScoreVO> sortPerformListGetAddMinusConvert(List<Perform> performList) {

        HashMap<String,List<Perform>> performMapByName = new HashMap<>();
        for (Perform perform : performList) {
            if(performMapByName.containsKey(perform.getName())){
                performMapByName.get(perform.getName()).add(perform);
            }else {
                List<Perform> newList = new ArrayList<>();
                newList.add(perform);
                performMapByName.put(perform.getName(),newList);}
        }

        List<AccumulateScoreVO> accumulateScoreVOList = new ArrayList<>();
        for (Map.Entry<String, List<Perform>> entry : performMapByName.entrySet()) {
            AccumulateScoreVO accumulateScoreVO = new AccumulateScoreVO();
            for (Perform perform : entry.getValue()) {
                if(perform.getSituation().equals("表扬")){
                    accumulateScoreVO.setAddScore(accumulateScoreVO.getAddScore() == null ? perform.getScore() : accumulateScoreVO.getAddScore() + perform.getScore());
                }else if(perform.getSituation().equals("批评")){
                    accumulateScoreVO.setPunishScore(accumulateScoreVO.getPunishScore() == null ? perform.getScore() : accumulateScoreVO.getPunishScore() + perform.getScore());
                }else if(perform.getSituation().equals("兑换")){
                    accumulateScoreVO.setConvertScore(accumulateScoreVO.getConvertScore() == null ? perform.getScore() : accumulateScoreVO.getConvertScore() + perform.getScore());
                }

            }
            accumulateScoreVO.setName(entry.getKey());
            if(accumulateScoreVO.getAddScore()==null){
                accumulateScoreVO.setAddScore(0);
            }
            if(accumulateScoreVO.getPunishScore()==null){
                accumulateScoreVO.setPunishScore(0);
            }
            if(accumulateScoreVO.getConvertScore()==null){
                accumulateScoreVO.setConvertScore(0);
            }
            accumulateScoreVO.setSumScore(accumulateScoreVO.getAddScore() - accumulateScoreVO.getConvertScore()-accumulateScoreVO.getPunishScore());
            accumulateScoreVOList.add(accumulateScoreVO);
        }
        return accumulateScoreVOList;



    }


}
