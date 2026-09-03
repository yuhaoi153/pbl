//package flyfish.contoller;
//
//import flyfish.exception.NullNameListException;
//import flyfish.mapper.AccumulateScoreMapper;
//import flyfish.mapper.PerformMapper;
//import flyfish.mapper.StudentInfoMapper;
//import flyfish.pojo.AccumulateScore;
//import flyfish.pojo.DTO.ScoreDTO;
//import flyfish.pojo.Result;
//import flyfish.pojo.VO.AccumulateScoreVO;
//import flyfish.service.AccumulateScoreService;
//import lombok.extern.slf4j.Slf4j;
//import net.sourceforge.pinyin4j.PinyinHelper;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.format.annotation.DateTimeFormat;
//import org.springframework.web.bind.annotation.*;
//
//import java.time.LocalDate;
//import java.util.*;
//import java.util.stream.Collectors;
//
//@RestController
//@Slf4j
//public class AccumulateScoreController {
//    @Autowired
//    private AccumulateScoreService accumulateScoreService;
//    @Autowired
//    private StudentInfoMapper studentInfoMapper;
//    @Autowired
//    private AccumulateScoreMapper accumulateScoreMapper;
//    @Autowired
//    private PerformMapper performMapper;
//
//
//    @PostMapping("/tpi/updatescore")
//    public Result<List<AccumulateScoreVO>> updatescore(@RequestBody  ScoreDTO scoreDTO){
//        log.info("提交更新分数的信息-加分管理模块：{}",scoreDTO);
//        //处理数据（多思考各种异常情况）
//        //判断是否初始化:当前日期、班级、作业内容是否已经存在
//        String classNumber = scoreDTO.getClassNumber();
//        //处理value
//        String value = scoreDTO.getValue();
//        if(value.endsWith("--")){
//            value = value.substring(0,value.length()-2);
//        }
//        String[] parts = value.split("--");
//        List<String> studentNumberList = new ArrayList<>();
//        for(String part :parts){
//            String[] numbers = part.split("/");
//            if(numbers.length>0){
//                if(numbers.length>1){
//                    if(numbers[1].equals(classNumber)){
//                        studentNumberList.add(numbers[0]);
//                    }
//                }
//
//            }
//        }
//        //去掉重复的元素
//        Set<String> set = new HashSet<>(studentNumberList);
//        studentNumberList = new ArrayList<>(set);
//
//        System.out.println(studentNumberList);
//        List<String> nameList = new ArrayList<>();
//        if(studentNumberList !=null && studentNumberList.size()>0){
//            //获取学号对应的学生姓名
//            nameList = studentInfoMapper.getnameList(studentNumberList,classNumber);
//        }else {
//            return null;//如果是null,就抛异常
//        }
//        //再反过来查询一遍学号，避免学号和姓名对不上
//        if(nameList != null  && nameList.size()>0){
//            studentNumberList = studentInfoMapper.getnewStudentNumberList(nameList,classNumber);
//        }else {
//            return null;
//        }
//
//
//        accumulateScoreService.getNameClass(scoreDTO.getClassNumber(),nameList,scoreDTO.getSubject());
//
//
//        List<AccumulateScoreVO> accumulateScoreVOList = accumulateScoreService.threeTypescore(scoreDTO,nameList);
//
//
//        return Result.success(accumulateScoreVOList);
//    }
//
//    @GetMapping("/tpi/queryScoreAll")  public Result<List<AccumulateScoreVO>> queryScore( String classNumber){
//        log.info("查询所有积分：{}",classNumber);
//        List<String> nameList = studentInfoMapper.getnameListAll(classNumber);
//
//
//        //由于积分表是按照学科存储的，一个姓名会有好多学科的积分内容，现在需要把不同的学科整合到一个字段中
//        List<AccumulateScore>  accumulateScoreList = accumulateScoreMapper.getAllscore(nameList,classNumber);
//        Map<String, List<AccumulateScore>> groupedByName = accumulateScoreList.stream()
//                .collect(Collectors.groupingBy(AccumulateScore::getName));
//        List<AccumulateScoreVO> accumulateScoreVOList = new ArrayList<>();
//        for (Map.Entry<String, List<AccumulateScore>> entry : groupedByName.entrySet()) {
//            AccumulateScoreVO accumulateScoreVO = new AccumulateScoreVO();
//            accumulateScoreVO.setName(entry.getKey());
//            for(AccumulateScore accumulateScore :entry.getValue()){
//                if(accumulateScore.getSubject().equals("语文")){
//                    accumulateScoreVO.setChineseaddscore(accumulateScore.getAddscore());
//                    accumulateScoreVO.setChineseconvertscore(accumulateScore.getMinusscore()-accumulateScore.getPunishscore());
//                    accumulateScoreVO.setChinesesumscore(accumulateScore.getAddscore() + accumulateScore.getMinusscore());
//                    accumulateScoreVO.setChinesepunishscore(accumulateScore.getPunishscore());
//                }
//                if(accumulateScore.getSubject().equals("数学")){
//                    accumulateScoreVO.setMathaddscore(accumulateScore.getAddscore());
//                    accumulateScoreVO.setMathconvertscore(accumulateScore.getMinusscore()-accumulateScore.getPunishscore());
//                    accumulateScoreVO.setMathsumscore(accumulateScore.getAddscore() + accumulateScore.getMinusscore());
//                    accumulateScoreVO.setMathpunishscore(accumulateScore.getPunishscore());
//                }
//                if(accumulateScore.getSubject().equals("英语")){
//                    accumulateScoreVO.setEnglishaddscore(accumulateScore.getAddscore());
//                    accumulateScoreVO.setEnglishconvertscore(accumulateScore.getMinusscore()-accumulateScore.getPunishscore());
//                    accumulateScoreVO.setEnglishsumscore(accumulateScore.getAddscore() + accumulateScore.getMinusscore());
//                    accumulateScoreVO.setEnglishpunishscore(accumulateScore.getPunishscore());
//                }
//
//            }
//            accumulateScoreVOList.add(accumulateScoreVO);
//
//        }
//        // 按姓氏拼音正序排序
//        Collections.sort(accumulateScoreVOList, new Comparator<AccumulateScoreVO>() {
//            @Override
//            public int compare(AccumulateScoreVO o1, AccumulateScoreVO o2) {
//                String pinyin1 = getPinyin(o1.getName().substring(0, 1));
//                String pinyin2 = getPinyin(o2.getName().substring(0, 1));
//                return pinyin1.compareTo(pinyin2);
//            }
//        });
//        return Result.success(accumulateScoreVOList) ;
//    }
//
//    // 获取拼音的方法
//    private static String getPinyin(String chineseCharacter) {
//        String[] pinyinArray = PinyinHelper.toHanyuPinyinStringArray(chineseCharacter.charAt(0));
//        if (pinyinArray != null) {
//            // 返回第一个拼音（有些字有多个读音，取第一个）
//            return pinyinArray[0];
//        }
//        return chineseCharacter; // 如果无法转换为拼音，返回原字符
//    }
//
//    @GetMapping("/tpi/queryScore")
//    public Result<List<AccumulateScoreVO>> queryScore( String classNumber,String value, LocalDate checkdate){
//        log.info("单纯查询积分：{},{},{}",classNumber,value,checkdate);
//        //处理数据（多思考各种异常情况）
//        //判断是否初始化:当前日期、班级、作业内容是否已经存在
//        if(value.endsWith("--")){
//            value = value.substring(0,value.length()-2);
//        }
//        String[] parts = value.split("--");
//        List<String> studentNumberList = new ArrayList<>();
//        for(String part :parts){
//            String[] numbers = part.split("/");
//            if(numbers.length>0){
//                if(numbers.length>1){
//                    if(numbers[1].equals(classNumber)){
//                        studentNumberList.add(numbers[0]);
//                    }
//                }
//
//            }
//        }
//        //去掉重复的元素
//        Set<String> set = new HashSet<>(studentNumberList);
//        studentNumberList = new ArrayList<>(set);
//
//        System.out.println(studentNumberList);
//        List<String> nameList = new ArrayList<>();
//        if(studentNumberList !=null && studentNumberList.size()>0){
//            //获取学号对应的学生姓名
//            nameList = studentInfoMapper.getnameList(studentNumberList,classNumber);
//        }else {
//            return null;//如果是null,就抛异常
//        }
//        //再反过来查询一遍学号，避免学号和姓名对不上
//        if(nameList != null  && nameList.size()>0){
//            studentNumberList = studentInfoMapper.getnewStudentNumberList(nameList,classNumber);
//        }else {
//            return null;
//        }
//
//
//        //由于积分表是按照学科存储的，一个姓名会有好多学科的积分内容，现在需要把不同的学科整合到一个字段中
//        List<AccumulateScore>  accumulateScoreList = accumulateScoreMapper.getAllscore(nameList,classNumber);
//        Map<String, List<AccumulateScore>> groupedByName = accumulateScoreList.stream()
//                .collect(Collectors.groupingBy(AccumulateScore::getName));
//        List<AccumulateScoreVO> accumulateScoreVOList = new ArrayList<>();
//        for (Map.Entry<String, List<AccumulateScore>> entry : groupedByName.entrySet()) {
//            AccumulateScoreVO accumulateScoreVO = new AccumulateScoreVO();
//            accumulateScoreVO.setName(entry.getKey());
//            for(AccumulateScore accumulateScore :entry.getValue()){
//                if(accumulateScore.getSubject().equals("语文")){
//                    accumulateScoreVO.setChineseaddscore(accumulateScore.getAddscore());
//                    accumulateScoreVO.setChineseconvertscore(accumulateScore.getMinusscore()-accumulateScore.getPunishscore());
//                    accumulateScoreVO.setChinesesumscore(accumulateScore.getAddscore() + accumulateScore.getMinusscore());
//                    accumulateScoreVO.setChinesepunishscore(accumulateScore.getPunishscore());
//                }
//                if(accumulateScore.getSubject().equals("数学")){
//                    accumulateScoreVO.setMathaddscore(accumulateScore.getAddscore());
//                    accumulateScoreVO.setMathconvertscore(accumulateScore.getMinusscore()-accumulateScore.getPunishscore());
//                    accumulateScoreVO.setMathsumscore(accumulateScore.getAddscore() + accumulateScore.getMinusscore());
//                    accumulateScoreVO.setMathpunishscore(accumulateScore.getPunishscore());
//                }
//                if(accumulateScore.getSubject().equals("英语")){
//                    accumulateScoreVO.setEnglishaddscore(accumulateScore.getAddscore());
//                    accumulateScoreVO.setEnglishconvertscore(accumulateScore.getMinusscore()-accumulateScore.getPunishscore());
//                    accumulateScoreVO.setEnglishsumscore(accumulateScore.getAddscore() + accumulateScore.getMinusscore());
//                    accumulateScoreVO.setEnglishpunishscore(accumulateScore.getPunishscore());
//                }
//
//            }
//            accumulateScoreVOList.add(accumulateScoreVO);
//
//
//        }
//        // 按姓氏拼音正序排序
//        Collections.sort(accumulateScoreVOList, new Comparator<AccumulateScoreVO>() {
//            @Override
//            public int compare(AccumulateScoreVO o1, AccumulateScoreVO o2) {
//                String pinyin1 = getPinyin(o1.getName().substring(0, 1));
//                String pinyin2 = getPinyin(o2.getName().substring(0, 1));
//                return pinyin1.compareTo(pinyin2);
//            }
//        });
//        return Result.success(accumulateScoreVOList) ;
//    }
//
//
//
//    @PostMapping("/tpi/updatescorespecial")
//    public Result<List<AccumulateScoreVO>> updatescorespecial(@RequestBody  ScoreDTO scoreDTO){
//        log.info("提交更新分数的信息-便捷操作：{}",scoreDTO);
//
//
//
//
//        String classNumber = scoreDTO.getClassNumber();
//        //处理value
//        String value = scoreDTO.getValue();
//        if(value.endsWith("--")){
//            value = value.substring(0,value.length()-2);
//        }
//        String[] parts = value.split("--");
//        List<String> studentNumberList = new ArrayList<>();
//        for(String part :parts){
//            String[] numbers = part.split("/");
//            if(numbers.length>0){
//                if(numbers.length>1){
//                    if(numbers[1].equals(classNumber)){
//                        studentNumberList.add(numbers[0]);
//                    }
//                }
//
//            }
//        }
//        Map<String, Integer> elementFrequency = getElementFrequency(studentNumberList);
//        List<String> nameList = new ArrayList<>();
//        List<Integer> addscoreList = new ArrayList<>();
//        List<String> repeatedStudentList = new ArrayList<>();
//
//        for (Map.Entry<String, Integer> entry : elementFrequency.entrySet()) {
//            repeatedStudentList.add(entry.getKey());
//            addscoreList.add(entry.getValue());
//        }
//
//        if(repeatedStudentList !=null && repeatedStudentList.size()>0){
//            //获取学号对应的学生姓名
//            nameList = studentInfoMapper.getnameList(repeatedStudentList,classNumber);
//        }else {
//            return null;//如果是null,就抛异常
//        }
//
//        if(nameList == null || nameList.size() == 0){
//            throw new NullNameListException("姓名列表为空");
//        }
//
//        List<AccumulateScoreVO> accumulateScoreVOList = accumulateScoreService.addscorespecial(scoreDTO,nameList,addscoreList);
//
//
//
//
//        return Result.success(accumulateScoreVOList);
//    }
//
//
//    private static Map<String, Integer> getElementFrequency(List<String> elements) {
//        Map<String, Integer> frequencyMap = new HashMap<>();
//        for (String element : elements) {
//            frequencyMap.put(element, frequencyMap.getOrDefault(element, 0) + 1);
//        }
//        return frequencyMap;
//    }
//}
