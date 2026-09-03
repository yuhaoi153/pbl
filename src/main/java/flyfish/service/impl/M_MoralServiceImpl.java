package flyfish.service.impl;

import flyfish.mapper.*;
import flyfish.pojo.*;
import flyfish.pojo.DTO.M_MoralEightSituationDTO;
import flyfish.pojo.DTO.M_MoralExcelDTO;
import flyfish.pojo.DTO.M_MoralStudentBehaviorListDTO;
import flyfish.pojo.VO.*;
import flyfish.service.M_MoralService;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class M_MoralServiceImpl implements M_MoralService {
    @Autowired
    private M_SingleMoralRecordMapper singleMoralRecordMapper;
    @Autowired
    private M_GradeYearMapper gradeYearMapper;
    @Autowired
    private M_StudentPerformLabelMapper studentPerformLabelMapper;
    @Autowired
    private M_UserMapper userMapper;
    @Autowired
    private M_GradeClassNumMapper m_gradeClassNumMapper;
    @Autowired
    private M_DefaultConfigMapper deafultConfigMapper;
    @Autowired
    private M_ReadingFeedbackMapper readingFeedbackMapper;
    @Autowired
    private M_SportRecordMapper sportRecordMapper;
    @Autowired
    private M_DefaultConfigMapper m_DefaultConfigMapper;
    @Autowired
    private M_StudentInfoMapper studentInfoMapper;
    @Autowired
    private M_PoorPerformerMapper poorPerformerMapper;


    @Override
    // 记录德育八项情况
    public String addMoralRecord(M_MoralEightSituationDTO moralData) {
        //首先判断是哪个标签，判断是什么年级，挑选出班级列表
        String label = moralData.getLabel();
        String grade = moralData.getGrade();
        List<Integer> classList = getClassListByGradeLabel(grade, label, moralData);
        //从数据库中取出该年级该标签的班级列表
        List<Integer> dbClassList = singleMoralRecordMapper.getClassByGradeLabel(moralData.getSchool(), grade, label, moralData.getCheckDate());
        //先把两个列表变差Set，对比是否一样，如果不一样则更新数据库
        Set<Integer> classSet = new HashSet<>(classList);
        Set<Integer> dbClassSet = new HashSet<>(dbClassList);
        if (classSet.equals(dbClassSet)) {
            return grade + label + "norevise";
        } else if (classSet.isEmpty()) {
            //如果是空集合，则表示前端没有选择任何班级,删除数据库中该年级该标签的数据
            singleMoralRecordMapper.deleteByGradeLabel(moralData.getSchool(), grade, label, moralData.getCheckDate());
            return grade + label + "empty";
        } else {
            //不一样则更新数据库，先删除数据库中该年级该标签的数据，再插入新的数据
            singleMoralRecordMapper.deleteByGradeLabel(moralData.getSchool(), grade, label, moralData.getCheckDate());
            //插入新的数据
            //首先从grade_classname中拿出grade对应的year
            int year = gradeYearMapper.getYearByGrade(grade);
            for (Integer className : classList) {
                singleMoralRecordMapper.insertMoralRecord(moralData.getSchool(), year, grade, className, label, moralData.getCheckDate());
            }
            return grade + label + "revised";
        }


    }

    // 获取某日德育巡查反馈
    @Override
    public M_MoralEightSituationDTO getSelectedMoralRecord(String school, LocalDate checkDate) {
        M_MoralEightSituationDTO moralEightSituationDTO = getMoralRecordByCheckDate(school, checkDate);
        return moralEightSituationDTO;
    }

    // 添加行为标签
    @Override
    public String addBehaviorTag(String school, String tag, String type) {
        M_BehaviorTag behaviorTag = new M_BehaviorTag();
        behaviorTag.setSchool(school);
        behaviorTag.setLabel(tag);
        behaviorTag.setWellBad(type);
        behaviorTag.setEducationType("德育");
        Integer id = studentPerformLabelMapper.insertBehaviorTag(behaviorTag);
        if (id != null) {
            return "success";
        } else {
            return "error";
        }


    }

    @Override
    public List<String> getBehaviorTags(String school, String type) {
        String wellBad = type;
        String educationType = "德育";
        List<String> behaviorTags = studentPerformLabelMapper.getBehaviorTags(school, wellBad, educationType);
        return behaviorTags;
    }

    //获取当前班级学生名单
    @Override
    public List<M_PersonalCurrentStudentVO> getCurrentStudentList(String school, String className, LocalDate checkDate, String tag, String label) {
        //首先拿到所有学生的名单，然后逐一验证一下哪些学生是被选择的
        //先处理班级和年级，把二(1)班这样的，二放到grade中，1放到classNum中
        M_GradeClassNum gradeClassNum = parseClassNamesToGradeClassNumList(className);
        List<M_PersonalCurrentStudentVO> studentList = studentInfoMapper.getCurrentStudentList(school, gradeClassNum.getGrade(), gradeClassNum.getClassName());
        //提取学生id列表
        List<Integer> studentIdList = new ArrayList<>();
        for (M_PersonalCurrentStudentVO student : studentList) {
            studentIdList.add(student.getId());
        }
        //查询moralRecord表中，看看哪些id的学生有记录
        List<String> selectedStudentNameList = singleMoralRecordMapper.getNameByStudentIds(school, checkDate, studentIdList, tag, label);
        //遍历studentList，如果名字在selectedStudentNameList中，则设置selected为true
        for (M_PersonalCurrentStudentVO student : studentList) {
            if (selectedStudentNameList.contains(student.getStudentName())) {
                student.setSelected(true);
            } else {
                student.setSelected(false);
            }
        }
        return studentList;

    }

    @Override
    public String addBehaviorRecord(M_MoralStudentBehaviorListDTO moralStudentBehaviorListDTO) {
        //构建被选择学生实体类列表
        String school = moralStudentBehaviorListDTO.getSchool();
        String className = moralStudentBehaviorListDTO.getClassName();
        String tag = moralStudentBehaviorListDTO.getTag();
        String label = moralStudentBehaviorListDTO.getLabel();
        LocalDate checkDate = moralStudentBehaviorListDTO.getCheckDate();
        M_GradeClassNum gradeClassNum = parseClassNamesToGradeClassNumList(className);
        if (gradeClassNum == null) {
            return "NoClassName";
        }
        String grade = gradeClassNum.getGrade();
        Integer classNum = gradeClassNum.getClassName();
        Integer year = gradeYearMapper.getYearByGrade(grade);
        List<String> studentNameList = new ArrayList<>();
        switch (label) {
            case "personalPraise":
                HashMap<String, List<M_ClassStudent>> personalPraiseBehaviorMap = moralStudentBehaviorListDTO.getPersonalPraiseBehaviorMap();
                List<M_ClassStudent> mClassStudents = personalPraiseBehaviorMap.get(tag);
                //如果mClassStudents为空，则表示没有学生被选择，删除数据库数据并返回
                if (mClassStudents == null || mClassStudents.isEmpty()) {
                    singleMoralRecordMapper.deleteByClassTagLabelDate(school, grade, classNum, tag, label, checkDate);
                    return "empty";
                }

                for (M_ClassStudent mClassStudent : mClassStudents) {
                    //如果className和前端传递的className相同，则取出学生名单
                    if (mClassStudent.getClassName().equals(className)) {
                        studentNameList.add(mClassStudent.getStudentName());
                    }
                }

                break;
            case "personalCriticize":
                HashMap<String, List<M_ClassStudent>> personalCriticizeBehaviorMap = moralStudentBehaviorListDTO.getPersonalCriticizeBehaviorMap();
                List<M_ClassStudent> mClassStudents2 = personalCriticizeBehaviorMap.get(tag);
                //如果mClassStudents为空，则表示没有学生被选择，删除数据库数据并返回
                if (mClassStudents2 == null || mClassStudents2.isEmpty()) {
                    singleMoralRecordMapper.deleteByClassTagLabelDate(school, grade, classNum, tag, label, checkDate);
                    return "empty";
                }
                for (M_ClassStudent mClassStudent : mClassStudents2) {
                    //如果className和前端传递的className相同，则取出学生名单
                    if (mClassStudent.getClassName().equals(className)) {
                        studentNameList.add(mClassStudent.getStudentName());
                    }
                }
                break;
            default:
                throw new IllegalArgumentException("Unknown label: " + label);
        }

        //如果学生名单为空
        if (studentNameList.isEmpty()) {
            singleMoralRecordMapper.deleteByClassTagLabelDate(school, grade, classNum, tag, label, checkDate);
            return "empty";
        }
        //取出studentNameList对应的学生ID列表
        List<Integer> studentIdList = new ArrayList<>();
        for (String studentName : studentNameList) {
            Integer studentId = studentInfoMapper.getUserIdBySchoolGradeClassStudentName(school, grade, classNum, studentName);
            if (studentId == null) {
                return "错误：找不到学生ID，学生姓名：" + studentName;
            } else {
                studentIdList.add(studentId);
            }
        }

        //拿到数据库中该班级该标签该日期对应的学生ID列表
        List<Integer> dbStudentIdList = singleMoralRecordMapper.getStudentIdByLabelClass(school, checkDate, tag, label, grade, classNum);

        String judgeResult = judgeStudentIdListDifference(studentIdList, dbStudentIdList);
        if (judgeResult.equals("norevise")) {
            return className + "norevise";
        } else {
            //先删除数据库中该班级该标签该日期的数据
            singleMoralRecordMapper.deleteByClassTagLabelDate(school, grade, classNum, tag, label, checkDate);
            //再插入新的数据,按索引顺序遍历
            for (Integer i = 0; i < studentIdList.size(); i++) {
                Integer studentId = studentIdList.get(i);
                String studentName = studentNameList.get(i);
                singleMoralRecordMapper.insertMoralStudentRecord(school, year, grade, classNum, label, checkDate, tag, studentId, studentName);
            }
            return className + "revised";
        }


    }

    //获取个人表现所选班级名单
    @Override
    public M_MoralPersonalSelectedClassListVO getSelectedStudentClassList(String school, LocalDate checkDate) {
        //从数据库中取出该学校该日期的个人表现所有内容
        List<M_SingleMoralRecordVO> moralRecordList = singleMoralRecordMapper.getPersonalMoralRecordByDate(school, checkDate);
        //处理数据，如果某个标签下有数据，则把对应的班级名称放入对应的列表中
        if (moralRecordList == null || moralRecordList.isEmpty()) {
            return null;
        }
        M_MoralPersonalSelectedClassListVO moralSelectedClassListVO = getPersonalSelectedClassList(moralRecordList);


        return moralSelectedClassListVO;
    }

    //删除行为标签
    @Override
    public String deleteBehaviorTag(String school, String tag, String type) {
        //根据学校、标签和label删除数据库中对应的数据
        studentPerformLabelMapper.deleteBehaviorTag(school, tag, type);
        return "删除" + tag;
    }

    //获取当日德育统计数据
    @Override
    public M_MoralStatisticNumVO getMoralStatisticNum(String school, LocalDate checkDate) {
        //获取所有的班级表扬数据
        List<String> praiseLabelList = Arrays.asList("roadPraise", "hygienePraise", "disciplinePraise", "personalPraise");
        Integer classPraiseNum = singleMoralRecordMapper.getClassNum(school, checkDate, praiseLabelList);
        //获取所有班级批评数据
        List<String> criticizeLabelList = Arrays.asList("roadCriticize", "hygieneCriticize", "disciplineCriticize", "personalCriticize");
        Integer classCriticizeNum = singleMoralRecordMapper.getClassNum(school, checkDate, criticizeLabelList);
        //获取所有个人表扬数据
        String personalPraiseLabel = "personalPraise";
        Integer personalPraiseNum = singleMoralRecordMapper.getPersonalNum(school, checkDate, personalPraiseLabel);
        //获取所有个人批评数据
        String criticizePraiseLabel = "personalCriticize";
        Integer personalCriticizeNum = singleMoralRecordMapper.getPersonalNum(school, checkDate, criticizePraiseLabel);
        //把数据进行统计，构建M_MoralStatisticNumVO返回
        M_MoralStatisticNumVO moralStatisticNumVO = new M_MoralStatisticNumVO();
        moralStatisticNumVO.setClassPraiseNum(classPraiseNum);
        moralStatisticNumVO.setClassCriticizeNum(classCriticizeNum);
        moralStatisticNumVO.setPersonalPraiseNum(personalPraiseNum);
        moralStatisticNumVO.setPersonalCriticizeNum(personalCriticizeNum);
        return moralStatisticNumVO;
    }

    //获取一段时间德育统计柱状图数据
    @Override
    public Map<String, List<M_ClassCountVO>> getClassCount(LocalDate startDate, LocalDate endDate, String school, String statisticType, Integer topNum, List<String> labelList, Integer roadPraiseAddScore, Integer roadCriticizeSubScore, Integer disciplinePraiseAddScore, Integer disciplineCriticizeSubScore, Integer hygienePraiseAddScore, Integer hygieneCriticizeSubScore, Integer personalPraiseAddScore, Integer personalCriticizeSubScore, Integer gymPraiseAddScore, Integer gymCriticizeSubScore, Integer runPraiseAddScore, Integer runCriticizeSubScore, Integer selfmanagePraiseAddScore, Integer readPraiseAddScore, Integer lessonPraiseAddScore) {

        if (startDate == null) {
            startDate = endDate;
        }
        if (labelList == null || labelList.size() == 0) {
            return null;
        }
        //看看统计卡片中是班级数据还是个人表扬或批评数据
        if (statisticType.equals("班级")) {
            List<String> gradeList = new ArrayList<>();
            List<String> repeatGradeList = m_gradeClassNumMapper.getGrade(school);
            Set<String> set = new HashSet<>(repeatGradeList);
            //根据班级列表数据库，获取该校一共多少年级，并去除重复数据
            gradeList.addAll(set);

            Map<String, List<M_ClassCountVO>> classCountMap = new HashMap<>();
//            gradeList.add("");//这一步是为了获取全校数据，一到六年级都可以获取，但是全校数据难获取，所以就单独加一个空字符串代表全校
            for (String grade : gradeList) {
                //拿到当前年级数据列表,每个标签对应一个List<M_ClassCountVO>,把这些列表放到一个大的List<List<M_ClassCountVO>>中
                List<List<M_ClassCountVO>> classCountListList = getClassCountListAndScore(startDate, endDate, school, grade, topNum, labelList, roadPraiseAddScore, roadCriticizeSubScore, disciplinePraiseAddScore, disciplineCriticizeSubScore, hygienePraiseAddScore, hygieneCriticizeSubScore, personalPraiseAddScore, personalCriticizeSubScore, gymPraiseAddScore, gymCriticizeSubScore, runPraiseAddScore, runCriticizeSubScore, selfmanagePraiseAddScore, readPraiseAddScore, lessonPraiseAddScore);
                //对当前年级数据进行汇总并重新排序
                List<M_ClassCountVO> classCountVOList = sumAndSortClassCountList(classCountListList);
                classCountMap.put(grade, classCountVOList);

            }
            //把所有年级数据汇总并排序
            List<List<M_ClassCountVO>> allClassCountListList = new ArrayList<>(classCountMap.values());
            List<M_ClassCountVO> allClassCountVOList = sumAndSortClassCountList(allClassCountListList);
            classCountMap.put("全校", allClassCountVOList);

            //取出各个年级以及全校数据的topNum，构建成新的map并返回
            Map<String, List<M_ClassCountVO>> topClassCountMap = new HashMap<>();
            for (Map.Entry<String, List<M_ClassCountVO>> entry : classCountMap.entrySet()) {
                String grade = entry.getKey();
                List<M_ClassCountVO> classCountVOList = entry.getValue();
                if (classCountVOList.size() > topNum) {
                    classCountVOList = classCountVOList.subList(0, topNum);
                }
                topClassCountMap.put(grade, classCountVOList);
            }
            return topClassCountMap;
        }


        return null;


    }

    //获取德育统计默认参数
    @Override
    public M_MoralStatisticDefaultParamsVO getMoralStatisticDefaultParams(String school) {
        List<M_DefaultConfig> mDefaultConfigList = m_gradeClassNumMapper.getAllBySchool(school);
        M_MoralStatisticDefaultParamsVO defaultParamsVO = new M_MoralStatisticDefaultParamsVO();
        List<String> labelList = new ArrayList<>();
        for (M_DefaultConfig mDefaultConfig : mDefaultConfigList) {
            //拿到topNum
            if (mDefaultConfig.getInfoName().equals("moralTopNum")) {
                Integer topNum = mDefaultConfig.getIntConfig();
                defaultParamsVO.setTopNum(topNum);
            }
            //拿到labelList
            if (mDefaultConfig.getInfoName().equals("德育标签")) {
                labelList.add(mDefaultConfig.getTextConfig());
            }
            //拿到每个默认加分和扣分系数
            //switch case判断add和subscore
            defaultParamsVO = getDefaultAddSubScore(defaultParamsVO, mDefaultConfig);

        }
        defaultParamsVO.setLabelList(labelList);


        return defaultParamsVO;
    }

    //设置德育统计默认参数
    @Override
    public String setMoralStatisticDefaultParams(M_MoralStatisticDefaultParamsVO mMoralStatisticDefaultParamsVO) {
        //设置moralTopNum
        if (mMoralStatisticDefaultParamsVO.getTopNum() != null) {
            //先删除，再新增
            String infoName = "moralTopNum";
            Integer topNum = mMoralStatisticDefaultParamsVO.getTopNum();
            intConfigDeleteAndInsert(infoName, topNum, mMoralStatisticDefaultParamsVO.getSchool());
        }
        //设置labelList
        List<String> labelList = mMoralStatisticDefaultParamsVO.getLabelList();
        if (labelList != null && labelList.size() > 0) {
            //先删除所有德育标签
            String AllinfoName = "德育标签";
            m_gradeClassNumMapper.deleteAllInfoName(AllinfoName, mMoralStatisticDefaultParamsVO.getSchool());
            for (String label : labelList) {
                String infoName = "德育标签";
                textConfigDeleteAndInsert(infoName, label, mMoralStatisticDefaultParamsVO.getSchool());
            }
        } else {
            //如果labelList为空，则删除数据库中该学校该标签的数据
            String infoName = "德育标签";
            String label = null;
            m_DefaultConfigMapper.deleteByInfoNameAndTextConfig(infoName, label, mMoralStatisticDefaultParamsVO.getSchool());
        }
        //设置加分系数和扣分系数
        intConfigDeleteAndInsert("roadPraiseAddScore", mMoralStatisticDefaultParamsVO.getRoadPraiseAddScore(), mMoralStatisticDefaultParamsVO.getSchool());
        intConfigDeleteAndInsert("roadCriticizeSubScore", mMoralStatisticDefaultParamsVO.getRoadCriticizeSubScore(), mMoralStatisticDefaultParamsVO.getSchool());
        intConfigDeleteAndInsert("disciplinePraiseAddScore", mMoralStatisticDefaultParamsVO.getDisciplinePraiseAddScore(), mMoralStatisticDefaultParamsVO.getSchool());
        intConfigDeleteAndInsert("disciplineCriticizeSubScore", mMoralStatisticDefaultParamsVO.getDisciplineCriticizeSubScore(), mMoralStatisticDefaultParamsVO.getSchool());
        intConfigDeleteAndInsert("hygienePraiseAddScore", mMoralStatisticDefaultParamsVO.getHygienePraiseAddScore(), mMoralStatisticDefaultParamsVO.getSchool());
        intConfigDeleteAndInsert("hygieneCriticizeSubScore", mMoralStatisticDefaultParamsVO.getHygieneCriticizeSubScore(), mMoralStatisticDefaultParamsVO.getSchool());
        intConfigDeleteAndInsert("personalPraiseAddScore", mMoralStatisticDefaultParamsVO.getPersonalPraiseAddScore(), mMoralStatisticDefaultParamsVO.getSchool());
        intConfigDeleteAndInsert("personalCriticizeSubScore", mMoralStatisticDefaultParamsVO.getPersonalCriticizeSubScore(), mMoralStatisticDefaultParamsVO.getSchool());
        intConfigDeleteAndInsert("gymPraiseAddScore", mMoralStatisticDefaultParamsVO.getGymPraiseAddScore(), mMoralStatisticDefaultParamsVO.getSchool());
        intConfigDeleteAndInsert("gymCriticizeSubScore", mMoralStatisticDefaultParamsVO.getGymCriticizeSubScore(), mMoralStatisticDefaultParamsVO.getSchool());
        intConfigDeleteAndInsert("runPraiseAddScore", mMoralStatisticDefaultParamsVO.getRunPraiseAddScore(), mMoralStatisticDefaultParamsVO.getSchool());
        intConfigDeleteAndInsert("runCriticizeSubScore", mMoralStatisticDefaultParamsVO.getRunCriticizeSubScore(), mMoralStatisticDefaultParamsVO.getSchool());
        intConfigDeleteAndInsert("selfmanagePraiseAddScore", mMoralStatisticDefaultParamsVO.getSelfmanagePraiseAddScore(), mMoralStatisticDefaultParamsVO.getSchool());
        intConfigDeleteAndInsert("readPraiseAddScore", mMoralStatisticDefaultParamsVO.getReadPraiseAddScore(), mMoralStatisticDefaultParamsVO.getSchool());
        intConfigDeleteAndInsert("lessonPraiseAddScore", mMoralStatisticDefaultParamsVO.getLessonPraiseAddScore(), mMoralStatisticDefaultParamsVO.getSchool());

        return "成功设置默认参数";
    }

    //根据学校和日期生成德育反馈文本
    @Override
    public M_MoralFeedbackVO generateMoralFeedback(String school, LocalDate checkDate) {
        String allFeedback = "";
        String goodFeedback = "";

        String infoName = "德育处反馈前言";
        String preface = checkDate.toString() + deafultConfigMapper.getFeedbackText(school, infoName) + "\n";
        allFeedback += preface;
        goodFeedback += preface;


        //今日提醒，也就是个人批评
        infoName = "德育处反馈个人批评";
        String personalCriticizeLabel = deafultConfigMapper.getFeedbackText(school, infoName) + "\n";
        allFeedback += personalCriticizeLabel;
        goodFeedback += personalCriticizeLabel;
        //是否有个人批评的同学
        String label = "personalCriticize";
        List<M_SingleMoralRecord> personalCriticizeRecordList = singleMoralRecordMapper.getMoralRecordListByDateAndLabel(school, checkDate, label);
        if (personalCriticizeRecordList != null && personalCriticizeRecordList.size() > 0) {
            String personalCriticize = getPersonalFeedback(personalCriticizeRecordList);
            allFeedback += personalCriticize + "\n";
            goodFeedback += personalCriticize + "\n";
        }

        //今日表彰，也就是个人表扬
        infoName = "德育处反馈个人表扬";
        String personalPraiseLabel = deafultConfigMapper.getFeedbackText(school, infoName) + "\n";
        allFeedback += personalPraiseLabel;
        goodFeedback += personalPraiseLabel;
        //是否有个人表扬的同学
        label = "personalPraise";
        List<M_SingleMoralRecord> personalPraiseRecordList = singleMoralRecordMapper.getMoralRecordListByDateAndLabel(school, checkDate, label);
        if (personalPraiseRecordList != null && personalPraiseRecordList.size() > 0) {
            String personalPraise = getPersonalFeedback(personalPraiseRecordList);
            allFeedback += personalPraise + "\n";
            goodFeedback += personalPraise + "\n";
        }

        //路队组织
        List<String> roadFeedbackList = classFeedbackProcess("roadPraise", "roadCriticize", "德育处反馈路队组织", "德育处反馈路队表扬", "德育处反馈路队批评", school, checkDate, allFeedback, goodFeedback);
        goodFeedback = roadFeedbackList.get(0) + "\n";
        allFeedback = roadFeedbackList.get(1) + "\n";

        //班级卫生
        List<String> hygieneFeedbackList = classFeedbackProcess("hygienePraise", "hygieneCriticize", "德育处反馈班级卫生", "德育处反馈卫生表扬", "德育处反馈卫生批评", school, checkDate, allFeedback, goodFeedback);
        goodFeedback = hygieneFeedbackList.get(0) + "\n";
        allFeedback = hygieneFeedbackList.get(1) + "\n";

        //课间纪律
        List<String> disciplineFeedbackList = classFeedbackProcess("disciplinePraise", "disciplineCriticize", "德育处反馈课间纪律", "德育处反馈纪律表扬", "德育处反馈纪律批评", school, checkDate, allFeedback, goodFeedback);
        goodFeedback = disciplineFeedbackList.get(0) + "\n";
        allFeedback = disciplineFeedbackList.get(1) + "\n";


         infoName = "德育处反馈广播体操";
        String roadLabel = deafultConfigMapper.getFeedbackText(school, infoName) + "\n";
        allFeedback += roadLabel;
        goodFeedback += roadLabel;

        //广播体操
        List<String> sportFeedbackList = classFeedbackProcessSport("体操表扬","体操批评","德育处反馈广播体操","德育处反馈体操表扬","德育处反馈体操批评",school,checkDate,allFeedback,goodFeedback);
        goodFeedback = sportFeedbackList.get(0) + "\n";
        allFeedback = sportFeedbackList.get(1) + "\n";

        //大课间跑操
        List<String> runFeedbackList = classFeedbackProcessSport("跑操表扬","跑操批评","德育处反馈广播体操","德育处反馈跑操表扬","德育处反馈跑操批评",school,checkDate,allFeedback,goodFeedback);
        goodFeedback = runFeedbackList.get(0) + "\n";
        allFeedback = runFeedbackList.get(1) + "\n";

        M_MoralFeedbackVO moralFeedbackVO = new M_MoralFeedbackVO();
        moralFeedbackVO.setAllResult(allFeedback);
        moralFeedbackVO.setGoodResult(goodFeedback);

        return moralFeedbackVO;
    }

    //根据学校和日期导出德育反馈Excel表格
    @Override
    public ResponseEntity<byte[]> exportMoralExcel(M_MoralExcelDTO moralExcelDTO) {

        String school = moralExcelDTO.getSchool();
        String grade = moralExcelDTO.getGrade();
        LocalDate startDate = moralExcelDTO.getStartDate();
        LocalDate endDate = moralExcelDTO.getEndDate();
        if(grade.equals("全校")){
            grade = null;
        }


        if(moralExcelDTO.getExportMode()!= null && moralExcelDTO.getExportMode().equals("summary")){

            //拿到早读教师表的所有数据
            List<M_SingleReadTeacher> readingTeacherFeedbackList = readingFeedbackMapper.getReadingFeedbackByDateRange(school, startDate, endDate,grade);


            //拿到所有提醒教师的数据
            List<M_ReadingPoorPerform> readingPoorPerformList = poorPerformerMapper.getReadingPoorPerformByDateRange(school, startDate, endDate,grade);

            // 统计每个教师出现的次数
            Map<String, Integer> countAlertMap = new HashMap<>();

            for (M_ReadingPoorPerform item : readingPoorPerformList) {
                String teacherName = item.getTeacherName();
                countAlertMap.merge(teacherName, 1, Integer::sum);
            }

// 转换为目标实体列表
            List<M_SingleTeacherCountSummary> summaryAlertList = new ArrayList<>();
            for (Map.Entry<String, Integer> entry : countAlertMap.entrySet()) {
                M_SingleTeacherCountSummary summary = new M_SingleTeacherCountSummary();
                summary.setTeacherName(entry.getKey());
                summary.setCountTeacherName(entry.getValue());
                // 如果实体类中有 subject 和 checkDate 属性，可以设为 null（根据业务需要）
                // summary.setSubject(null);
                // summary.setCheckDate(null);
                summaryAlertList.add(summary);
            }

            // 统计早读教师出现的次数（按教师+科目分组，忽略日期）
            Map<String, Map<String, Integer>> countMap = new HashMap<>();

            for (M_SingleReadTeacher t : readingTeacherFeedbackList) {
                countMap.computeIfAbsent(t.getTeacherName(), k -> new HashMap<>())
                        .merge(t.getSubject(), 1, Integer::sum);
            }

            List<M_SingleTeacherCountSummary> summaryList = new ArrayList<>();
            for (var nameEntry : countMap.entrySet()) {
                String teacherName = nameEntry.getKey();
                for (var subjectEntry : nameEntry.getValue().entrySet()) {
                    String subject = subjectEntry.getKey();
                    Integer count = subjectEntry.getValue();

                    M_SingleTeacherCountSummary summary = new M_SingleTeacherCountSummary();
                    summary.setTeacherName(teacherName);
                    summary.setSubject(subject);
                    summary.setCountTeacherName(count);
                    // 如果实体类有 checkDate 属性，但不需要设置，可以忽略或设为 null
                    // summary.setCheckDate(null);

                    summaryList.add(summary);
                }
            }



            //拿出grade,className,studentName,label,supplement,checkDate字段，生成Excel表格
            try (Workbook workbook = new XSSFWorkbook()) {
                // 创建样式
                CellStyle headerStyle = createHeaderStyle(workbook);
                CellStyle dataStyle = createDataStyle(workbook);

            if(summaryList != null && summaryList.size() > 0) {
                for (M_SingleTeacherCountSummary  summary : summaryList) {
                    //根据label字段创建不同的sheet
                    String sheetName = "教师表扬次数汇总";



                    if (workbook.getSheet(sheetName)== null) {
                        workbook.createSheet(sheetName);
                    }
                    //创建标题行，标题行是年级、班级、学生姓名、补充信息、检查日期
                    createTeacherPraiseHeaderRow(workbook.getSheet(sheetName), headerStyle, sheetName);
                    // 填充数据
                    fillReadTeacherPraiseData(workbook.getSheet(sheetName),summary, dataStyle);
                    // 自动调整列宽
                    autoSizeColumns(workbook.getSheet(sheetName));


                }
            }








                if(summaryAlertList != null && summaryAlertList.size() > 0) {
                    for (M_SingleTeacherCountSummary  summary : summaryAlertList) {
                        //根据label字段创建不同的sheet
                        String sheetName = "教师批评次数汇总";



                        if (workbook.getSheet(sheetName)== null) {
                            workbook.createSheet(sheetName);
                        }
                        //创建标题行，标题行是年级、班级、学生姓名、补充信息、检查日期
                        createTeacherPraiseHeaderRow(workbook.getSheet(sheetName), headerStyle, sheetName);
                        // 填充数据
                        fillReadTeacherAlertData(workbook.getSheet(sheetName),summary, dataStyle);
                        // 自动调整列宽
                        autoSizeColumns(workbook.getSheet(sheetName));


                    }
                }


                // 将workbook转换为字节数组
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                workbook.write(outputStream);

                // 设置响应头
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
                headers.setContentDispositionFormData("attachment", "summary_data.xlsx");

                return new ResponseEntity<>(outputStream.toByteArray(), headers, HttpStatus.OK);


            } catch (IOException e) {
                throw new RuntimeException(e);
            }





        }


        //拿到所有的数据
        List<M_SingleMoralRecord> moralRecordList = singleMoralRecordMapper.getMoralRecordByDateRange(school, startDate, endDate,grade);
        List<M_SingleSportRecord> sportRecordList = sportRecordMapper.getSportRecordByDateRange(school, startDate, endDate,grade);
        //拿到早读教师表的所有数据
        List<M_SingleReadTeacher> readingTeacherFeedbackList = readingFeedbackMapper.getReadingFeedbackByDateRange(school, startDate, endDate,grade);
        //拿到早读自主管理的所有数据
        String selectTimeZone= "自主" ;
        List<M_SingleReadFeedback> readingSelfmanageFeedbackList = readingFeedbackMapper.getReadingSelfmanageFeedbackByDateRange(school, startDate, endDate,selectTimeZone,grade);

        List<M_ReadingPoorPerform> readingPoorPerformList = poorPerformerMapper.getReadingPoorPerformByDateRange(school, startDate, endDate,grade);

        //拿出grade,className,studentName,label,supplement,checkDate字段，生成Excel表格
        try (Workbook workbook = new XSSFWorkbook()) {
            // 创建样式
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle dataStyle = createDataStyle(workbook);

            //把德育数据按照标签分类，生成不同的sheet
            if(moralRecordList != null && moralRecordList.size() > 0) {
                for (M_SingleMoralRecord moralRecord : moralRecordList) {
                    //根据label字段创建不同的sheet
                    String sheetName = moralRecord.getLabel();
                    //如果sheetName在labelList中不存在，则不创建sheet
                    if (!moralExcelDTO.getLabelList().contains(sheetName)) {
                        continue;
                    }
                    //把sheetName替换成中文
                    sheetName = changeLabelToChinese(sheetName);

                    if (workbook.getSheet(sheetName)== null) {
                        workbook.createSheet(sheetName);
                    }
                    //创建标题行，标题行是年级、班级、学生姓名、补充信息、检查日期
                    createHeaderRow(workbook.getSheet(sheetName), headerStyle,sheetName);
                    // 填充数据
                    fillData(workbook.getSheet(sheetName), moralRecord, dataStyle);
                    // 自动调整列宽
                    autoSizeColumns(workbook.getSheet(sheetName));


                }
            }

            //把体育数据按照标签分类，生成不同的sheet
            if(sportRecordList != null && sportRecordList.size() > 0) {
                for (M_SingleSportRecord sportRecord : sportRecordList) {
                    //根据label字段创建不同的sheet
                    String sheetName = sportRecord.getTimeZone();
                    List<String> chineseLabelList = new ArrayList<>();
                    for(String label: moralExcelDTO.getLabelList()){
                        String chineseLabel = changeLabelToChinese(label);
                        chineseLabelList.add(chineseLabel);
                    }
                    //如果sheetName在labelList中不存在，则不创建sheet
                    if (!chineseLabelList.contains(sheetName)) {
                        continue;
                    }


                    if (workbook.getSheet(sheetName)== null) {
                        workbook.createSheet(sheetName);
                    }
                    //创建标题行，标题行是年级、班级、学生姓名、补充信息、检查日期
                    createHeaderRow(workbook.getSheet(sheetName), headerStyle,sheetName);
                    // 填充数据
                    fillSportData(workbook.getSheet(sheetName), sportRecord, dataStyle);
                    // 自动调整列宽
                    autoSizeColumns(workbook.getSheet(sheetName));


                }
            }

            //把早读教师表数据生成一个sheet
            if(readingTeacherFeedbackList != null && readingTeacherFeedbackList.size() > 0) {
                for (M_SingleReadTeacher readTeacherFeedback : readingTeacherFeedbackList) {
                    //根据label字段创建不同的sheet
                    String sheetName = readTeacherFeedback.getTimeZone();
                    List<String> chineseLabelList = new ArrayList<>();
                    for(String label: moralExcelDTO.getLabelList()){
                        String chineseLabel = changeLabelToChinese(label);
                        chineseLabelList.add(chineseLabel);
                    }
                    //如果sheetName在labelList中不存在，则不创建sheet
                    if (!chineseLabelList.contains(sheetName)) {
                        continue;
                    }
                    if(sheetName.equals("早读")){
                        sheetName = "早读表扬";
                    }
                    if(sheetName.equals("自主")){
                        sheetName = "自主管理";
                    }
                    if (sheetName.equals("课前")){
                        sheetName ="课堂表扬";
                    }

                    if (workbook.getSheet(sheetName)== null) {
                        workbook.createSheet(sheetName);
                    }
                    //创建标题行，标题行是年级、班级、学生姓名、补充信息、检查日期
                    createHeaderRow(workbook.getSheet(sheetName), headerStyle, sheetName);
                    // 填充数据
                    fillReadTeacherData(workbook.getSheet(sheetName), readTeacherFeedback, dataStyle);
                    // 自动调整列宽
                    autoSizeColumns(workbook.getSheet(sheetName));


                }
            }


            //把早读自主管理数据生成一个sheet
            if(readingSelfmanageFeedbackList != null && readingSelfmanageFeedbackList.size() >0){
                for (M_SingleReadFeedback singleReadFeedback : readingSelfmanageFeedbackList) {
                    //根据label字段创建不同的sheet
                    String sheetName = singleReadFeedback.getTimeZone();
                    List<String> chineseLabelList = new ArrayList<>();
                    for(String label: moralExcelDTO.getLabelList()){
                        String chineseLabel = changeLabelToChinese(label);
                        chineseLabelList.add(chineseLabel);
                    }
                    //如果sheetName在labelList中不存在，则不创建sheet
                    if (!chineseLabelList.contains(sheetName)) {
                        continue;
                    }
                    if(sheetName.equals("早读")){
                        sheetName = "早读表扬";
                    }
                    if(sheetName.equals("自主")){
                        sheetName = "自主管理";
                    }
                    if (sheetName.equals("课前")){
                        sheetName ="课堂表扬";
                    }

                    if (workbook.getSheet(sheetName)== null) {
                        workbook.createSheet(sheetName);
                    }
                    //创建标题行，标题行是年级、班级、学生姓名、补充信息、检查日期
                    createHeaderRow(workbook.getSheet(sheetName), headerStyle, sheetName);
                    // 填充数据
                    fillReadAutoData(workbook.getSheet(sheetName), singleReadFeedback, dataStyle);
                    // 自动调整列宽
                    autoSizeColumns(workbook.getSheet(sheetName));


                }
            }


            //把需提醒的教师数据生成一个sheet
            if(readingPoorPerformList != null && readingPoorPerformList.size() > 0) {
                for (M_ReadingPoorPerform mReadingPoorPerform : readingPoorPerformList) {
                    //根据label字段创建不同的sheet
                    String sheetName = "需提醒教师";


                    //如果sheetName在labelList中不存在，则不创建sheet
                    if (!moralExcelDTO.getLabelList().contains("alert")) {
                        continue;
                    }


                    if (workbook.getSheet(sheetName)== null) {
                        workbook.createSheet(sheetName);
                    }
                    //创建标题行，标题行是年级、班级、学生姓名、补充信息、检查日期
                    createHeaderRow(workbook.getSheet(sheetName), headerStyle, sheetName);
                    // 填充数据
                    fillReadAlertData(workbook.getSheet(sheetName),mReadingPoorPerform, dataStyle);
                    // 自动调整列宽
                    autoSizeColumns(workbook.getSheet(sheetName));


                }
            }





            // 将workbook转换为字节数组
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);

            // 设置响应头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", "moral_data.xlsx");

            return new ResponseEntity<>(outputStream.toByteArray(), headers, HttpStatus.OK);


        } catch (IOException e) {
            throw new RuntimeException(e);
        }


    }

    private void fillReadTeacherAlertData(Sheet sheet, M_SingleTeacherCountSummary summary, CellStyle dataStyle) {
        //先创建行，再一列一列地创建单元格并设置值和样式
        int lastRowNum = sheet.getLastRowNum();//获取当前sheet最后一行的行号，注意这个行号是从0开始的，如果没有数据则返回0，所以需要加1
        Row dataRow = sheet.createRow(lastRowNum + 1);
        Cell cell0 = dataRow.createCell(0);
        cell0.setCellValue(summary.getTeacherName());
        cell0.setCellStyle(dataStyle);

        Cell cell1 = dataRow.createCell(1);

        cell1.setCellStyle(dataStyle);

        Cell cell2 = dataRow.createCell(2);
        cell2.setCellValue(summary.getCountTeacherName());
        cell2.setCellStyle(dataStyle);

    }

    private void createTeacherPraiseHeaderRow(Sheet sheet, CellStyle headerStyle, String sheetName) {
        // 创建标题行
        Row titleRow = sheet.createRow(0);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue(sheetName);
        titleCell.setCellStyle(headerStyle);

        // 合并标题单元格（跨5列）
//        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 4));

        // 定义要合并的区域（第0行第0列到第0行第4列，即A1:E1）
        CellRangeAddress titleMergeRegion = new CellRangeAddress(0, 0, 0, 2);

        // 关键修复：检查该合并区域是否已存在，不存在才添加
        boolean regionExists = false;
        for (int i = 0; i < sheet.getNumMergedRegions(); i++) {
            CellRangeAddress existingRegion = sheet.getMergedRegion(i);
            if (existingRegion.equals(titleMergeRegion)) {
                regionExists = true;
                break;
            }
        }

        // 仅当区域不存在时才添加合并
        if (!regionExists) {
            sheet.addMergedRegion(titleMergeRegion);
        }



        //先创建行，再一列一列地创建单元格并设置值和样式
        Row headerRow = sheet.createRow(1);
        String[] headers = {"教师姓名",  "学科", "次数统计"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

    }

    private void fillReadTeacherPraiseData(Sheet sheet, M_SingleTeacherCountSummary summary, CellStyle dataStyle) {
        //先创建行，再一列一列地创建单元格并设置值和样式
        int lastRowNum = sheet.getLastRowNum();//获取当前sheet最后一行的行号，注意这个行号是从0开始的，如果没有数据则返回0，所以需要加1
        Row dataRow = sheet.createRow(lastRowNum + 1);
        Cell cell0 = dataRow.createCell(0);
        cell0.setCellValue(summary.getTeacherName());
        cell0.setCellStyle(dataStyle);

        Cell cell1 = dataRow.createCell(1);
        cell1.setCellValue(summary.getSubject());
        cell1.setCellStyle(dataStyle);

        Cell cell2 = dataRow.createCell(2);
        cell2.setCellValue(summary.getCountTeacherName());
        cell2.setCellStyle(dataStyle);

    }


    private void fillReadAutoData(Sheet sheet, M_SingleReadFeedback singleReadFeedback, CellStyle dataStyle) {
        //先创建行，再一列一列地创建单元格并设置值和样式
        int lastRowNum = sheet.getLastRowNum();//获取当前sheet最后一行的行号，注意这个行号是从0开始的，如果没有数据则返回0，所以需要加1
        Row dataRow = sheet.createRow(lastRowNum + 1);
        Cell cell0 = dataRow.createCell(0);
        cell0.setCellValue(singleReadFeedback.getGrade());
        cell0.setCellStyle(dataStyle);

        Cell cell1 = dataRow.createCell(1);
        cell1.setCellValue(singleReadFeedback.getGrade().substring(0,1)+singleReadFeedback.getClassName().substring(2,3));
        cell1.setCellStyle(dataStyle);

        Cell cell2 = dataRow.createCell(2);
        cell2.setCellStyle(dataStyle);

        Cell cell3 = dataRow.createCell(3);
        cell3.setCellStyle(dataStyle);

        Cell cell4 = dataRow.createCell(4);
        cell4.setCellValue(singleReadFeedback.getCheckDate().toString());
        cell4.setCellStyle(dataStyle);
    }

    private void fillReadTeacherData(Sheet sheet, M_SingleReadTeacher readTeacherFeedback, CellStyle dataStyle) {
        //先创建行，再一列一列地创建单元格并设置值和样式
        int lastRowNum = sheet.getLastRowNum();//获取当前sheet最后一行的行号，注意这个行号是从0开始的，如果没有数据则返回0，所以需要加1
        Row dataRow = sheet.createRow(lastRowNum + 1);
        Cell cell0 = dataRow.createCell(0);
        cell0.setCellValue(readTeacherFeedback.getGrade());
        cell0.setCellStyle(dataStyle);

        Cell cell1 = dataRow.createCell(1);
        cell1.setCellValue(readTeacherFeedback.getGrade().substring(0,1)+readTeacherFeedback.getClassName().substring(2,3));
        cell1.setCellStyle(dataStyle);

        Cell cell2 = dataRow.createCell(2);
        cell2.setCellValue(readTeacherFeedback.getTeacherName());
        cell2.setCellStyle(dataStyle);

        Cell cell3 = dataRow.createCell(3);
        cell3.setCellValue(readTeacherFeedback.getSubject());
        cell3.setCellStyle(dataStyle);

        Cell cell4 = dataRow.createCell(4);
        cell4.setCellValue(readTeacherFeedback.getCheckDate().toString());
        cell4.setCellStyle(dataStyle);
    }

    private void fillReadAlertData(Sheet sheet, M_ReadingPoorPerform mReadingPoorPerform, CellStyle dataStyle) {
        //先创建行，再一列一列地创建单元格并设置值和样式
        int lastRowNum = sheet.getLastRowNum();//获取当前sheet最后一行的行号，注意这个行号是从0开始的，如果没有数据则返回0，所以需要加1
        Row dataRow = sheet.createRow(lastRowNum + 1);
        Cell cell0 = dataRow.createCell(0);
        cell0.setCellValue(mReadingPoorPerform.getGrade());
        cell0.setCellStyle(dataStyle);

        Cell cell1 = dataRow.createCell(1);
        cell1.setCellValue(mReadingPoorPerform.getGrade().substring(0,1)+mReadingPoorPerform.getClassName().substring(2,3));
        cell1.setCellStyle(dataStyle);

        Cell cell2 = dataRow.createCell(2);
        cell2.setCellValue(mReadingPoorPerform.getTeacherName());
        cell2.setCellStyle(dataStyle);

        Cell cell3 = dataRow.createCell(3);
        cell3.setCellValue(mReadingPoorPerform.getSituation());
        cell3.setCellStyle(dataStyle);

        Cell cell4 = dataRow.createCell(4);
        cell4.setCellValue(mReadingPoorPerform.getCheckDate().toString());
        cell4.setCellStyle(dataStyle);



    }


    private void fillSportData(Sheet sheet, M_SingleSportRecord sportRecord, CellStyle dataStyle) {
        //先创建行，再一列一列地创建单元格并设置值和样式
        int lastRowNum = sheet.getLastRowNum();//获取当前sheet最后一行的行号，注意这个行号是从0开始的，如果没有数据则返回0，所以需要加1
        Row dataRow = sheet.createRow(lastRowNum + 1);
        Cell cell0 = dataRow.createCell(0);
        cell0.setCellValue(sportRecord.getGrade());
        cell0.setCellStyle(dataStyle);

        Cell cell1 = dataRow.createCell(1);
        cell1.setCellValue(sportRecord.getGrade().substring(0,1)+sportRecord.getClassName().substring(2,3));
        cell1.setCellStyle(dataStyle);

        Cell cell2 = dataRow.createCell(2);
        cell2.setCellStyle(dataStyle);

        Cell cell3 = dataRow.createCell(3);
        cell3.setCellStyle(dataStyle);

        Cell cell4 = dataRow.createCell(4);
        cell4.setCellValue(sportRecord.getCheckDate().toString());
        cell4.setCellStyle(dataStyle);
    }

    //设置固定列宽为20个字符宽度
    private void autoSizeColumns(Sheet sheet) {
        for (int i = 0; i < 5; i++) {
            //设置固定宽度
            sheet.setColumnWidth(i, 20 * 256);
        }
    }

    //填充数据，按照年级、班级、学生姓名、补充信息、检查日期的顺序填充
    private void fillData(Sheet sheet, M_SingleMoralRecord moralRecord, CellStyle dataStyle) {
        //先创建行，再一列一列地创建单元格并设置值和样式
        int lastRowNum = sheet.getLastRowNum();//获取当前sheet最后一行的行号，注意这个行号是从0开始的，如果没有数据则返回0，所以需要加1
        Row dataRow = sheet.createRow(lastRowNum + 1);
        Cell cell0 = dataRow.createCell(0);
        cell0.setCellValue(moralRecord.getGrade());
        cell0.setCellStyle(dataStyle);

        Cell cell1 = dataRow.createCell(1);
        cell1.setCellValue(moralRecord.getGrade().substring(0,1) + moralRecord.getClassName().toString());
        cell1.setCellStyle(dataStyle);

        Cell cell2 = dataRow.createCell(2);
        cell2.setCellValue(moralRecord.getStudentName());
        cell2.setCellStyle(dataStyle);

        Cell cell3 = dataRow.createCell(3);
        cell3.setCellValue(moralRecord.getSupplement());
        cell3.setCellStyle(dataStyle);

        Cell cell4 = dataRow.createCell(4);
        cell4.setCellValue(moralRecord.getCheckDate().toString());
        cell4.setCellStyle(dataStyle);

    }

    private void createHeaderRow(Sheet sheet, CellStyle headerStyle,String sheetName) {
        // 创建标题行
        Row titleRow = sheet.createRow(0);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue(sheetName);
        titleCell.setCellStyle(headerStyle);

        // 合并标题单元格（跨5列）
//        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 4));

        // 定义要合并的区域（第0行第0列到第0行第4列，即A1:E1）
        CellRangeAddress titleMergeRegion = new CellRangeAddress(0, 0, 0, 4);

        // 关键修复：检查该合并区域是否已存在，不存在才添加
        boolean regionExists = false;
        for (int i = 0; i < sheet.getNumMergedRegions(); i++) {
            CellRangeAddress existingRegion = sheet.getMergedRegion(i);
            if (existingRegion.equals(titleMergeRegion)) {
                regionExists = true;
                break;
            }
        }

        // 仅当区域不存在时才添加合并
        if (!regionExists) {
            sheet.addMergedRegion(titleMergeRegion);
        }



        //先创建行，再一列一列地创建单元格并设置值和样式
        Row headerRow = sheet.createRow(1);
        String[] headers = {"年级", "班级", "姓名", "补充信息", "检查日期"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }
    }


    //excel标题行样式
    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        //字体样式
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 12);
        style.setFont(font);

        //字体居中
        style.setAlignment(HorizontalAlignment.CENTER);
        //前景色
        style.setFillForegroundColor(IndexedColors.WHITE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    //excel数据行样式
    private CellStyle createDataStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setAlignment(HorizontalAlignment.LEFT);//设置做对齐
        style.setBorderBottom(BorderStyle.THIN);//Excel边框线
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    //把所有个人表现的数据进行处理，生成反馈文本
    private String getPersonalFeedback(List<M_SingleMoralRecord> personalCriticizeRecordList) {
        //按照supplement分类
        Map<String, List<M_SingleMoralRecord>> supplementMap = new HashMap<>();
        for (M_SingleMoralRecord record : personalCriticizeRecordList) {
            String supplement = record.getSupplement();
            if (!supplementMap.containsKey(supplement)) {
                supplementMap.put(supplement, new ArrayList<>());
            }
            supplementMap.get(supplement).add(record);
        }

        //按照supplement分类生成反馈文本，map一定是有值的
        String feedback = "";
        for (Map.Entry<String, List<M_SingleMoralRecord>> entry : supplementMap.entrySet()) {
            String supplement = entry.getKey();
            feedback += "-" + supplement + "\n";

            HashMap<String, List<M_SingleMoralRecord>> sortedFeedbackClassMap = sortFeedbackByGrade(entry.getValue());
            List<String> gradeOrderList = List.of("一年级", "二年级", "三年级", "四年级", "五年级", "六年级");
            for (String grade : gradeOrderList) {
                for (Map.Entry<String, List<M_SingleMoralRecord>> newEntry : sortedFeedbackClassMap.entrySet()) {
                    //还需要按照一年级、二年级、三年级到六年级顺序排序显示
                    if (grade.equals(newEntry.getKey())) {
                        feedback += "[" + grade + "]：";
                        List<M_SingleMoralRecord> recordList = newEntry.getValue();
                        for (M_SingleMoralRecord record : recordList) {
                            //如果是最后一个数据，那么就不加顿号
                            feedback += record.getGrade().substring(0, 1) + record.getClassName().toString() + record.getStudentName();
                            if (recordList.indexOf(record) != recordList.size() - 1) {
                                feedback += "、";
                            } else {
                                feedback += "\n";

                            }
                        }

                    }


                }
            }


        }
        return feedback;

    }

    //重复处理路队组织、班级纪律、课间卫生的反馈文本生成，所以单独抽取一个函数来处理，传入表扬标签、批评标签、标题信息名、表扬信息名、批评信息名，返回一个包含表扬反馈和总反馈的列表
    private List<String> classFeedbackProcessSport(String goodLabel, String badLabel, String titleInfoName, String goodInfoName, String badInfoName, String school, LocalDate checkDate, String allFeedback, String goodFeedback) {
        List<String> feedbackList = new ArrayList<>();

        //是否有路队表扬的班级
        String label = goodLabel;
        List<M_SingleMoralRecord> roadPraiseRecordList = sportRecordMapper.getSportRecordListByDateAndLabel(school, checkDate, label);
        if (roadPraiseRecordList != null && roadPraiseRecordList.size() > 0) {
             String infoName = goodInfoName;
            String roadPraiseLabel = deafultConfigMapper.getFeedbackText(school, infoName);
            allFeedback += roadPraiseLabel + "\n";
            goodFeedback += roadPraiseLabel + "\n";

            String roadPraise = getClassFeedback(roadPraiseRecordList);
            allFeedback += roadPraise;
            goodFeedback += roadPraise;


        }
        feedbackList.add(goodFeedback);

        //是否有路队批评班级
        label = badLabel;
        List<M_SingleMoralRecord> roadCriticizeRecordList = sportRecordMapper.getSportRecordListByDateAndLabel(school, checkDate, label);
        if (roadCriticizeRecordList != null && roadCriticizeRecordList.size() > 0) {
            String infoName = badInfoName;
            String roadCriticizeLabel = deafultConfigMapper.getFeedbackText(school, infoName);
            allFeedback += roadCriticizeLabel + "\n";

            String roadCriticize = getClassFeedback(roadCriticizeRecordList);
            allFeedback += roadCriticize;

        }
        feedbackList.add(allFeedback);
        return feedbackList;

    }


    //重复处理路队组织、班级纪律、课间卫生的反馈文本生成，所以单独抽取一个函数来处理，传入表扬标签、批评标签、标题信息名、表扬信息名、批评信息名，返回一个包含表扬反馈和总反馈的列表
    private List<String> classFeedbackProcess(String goodLabel, String badLabel, String titleInfoName, String goodInfoName, String badInfoName, String school, LocalDate checkDate, String allFeedback, String goodFeedback) {
        List<String> feedbackList = new ArrayList<>();
        //是否有表扬的班级
        //路队组织
        String infoName = titleInfoName;
        String roadLabel = deafultConfigMapper.getFeedbackText(school, infoName) + "\n";
        allFeedback += roadLabel;
        goodFeedback += roadLabel;
        //是否有路队表扬的班级
        String label = goodLabel;
        List<M_SingleMoralRecord> roadPraiseRecordList = singleMoralRecordMapper.getMoralRecordListByDateAndLabel(school, checkDate, label);
        if (roadPraiseRecordList != null && roadPraiseRecordList.size() > 0) {
            infoName = goodInfoName;
            String roadPraiseLabel = deafultConfigMapper.getFeedbackText(school, infoName);
            allFeedback += roadPraiseLabel + "\n";
            goodFeedback += roadPraiseLabel + "\n";

            String roadPraise = getClassFeedback(roadPraiseRecordList);
            allFeedback += roadPraise;
            goodFeedback += roadPraise;


        }
        feedbackList.add(goodFeedback);

        //是否有路队批评班级
        label = badLabel;
        List<M_SingleMoralRecord> roadCriticizeRecordList = singleMoralRecordMapper.getMoralRecordListByDateAndLabel(school, checkDate, label);
        if (roadCriticizeRecordList != null && roadCriticizeRecordList.size() > 0) {
            infoName = badInfoName;
            String roadCriticizeLabel = deafultConfigMapper.getFeedbackText(school, infoName);
            allFeedback += roadCriticizeLabel + "\n";

            String roadCriticize = getClassFeedback(roadCriticizeRecordList);
            allFeedback += roadCriticize;

        }
        feedbackList.add(allFeedback);
        return feedbackList;

    }

    //把所有班级表现的数据进行处理，生成反馈文本
    private String getClassFeedback(List<M_SingleMoralRecord> roadPraiseRecordList) {
        //按grade分类
        HashMap<String, List<M_SingleMoralRecord>> sortedFeedbackClassMap = sortFeedbackByGrade(roadPraiseRecordList);
        String feedback = "";
        List<String> gradeOrderList = List.of("一年级", "二年级", "三年级", "四年级", "五年级", "六年级");
        for (String grade : gradeOrderList) {
            for (Map.Entry<String, List<M_SingleMoralRecord>> entry : sortedFeedbackClassMap.entrySet()) {
                if (grade.equals(entry.getKey())) {
                    feedback += "[" + grade + "]：";
                    List<M_SingleMoralRecord> recordList = entry.getValue();
                    for (M_SingleMoralRecord record : recordList) {
                        feedback += record.getGrade().substring(0, 1) + record.getClassName().toString();
                        if (recordList.indexOf(record) != recordList.size() - 1) {
                            feedback += "、";
                        } else {
                            feedback += "\n";
                        }
                    }
                }

            }

        }

        return feedback;
    }

    //按照年级对班级进行分类
    private HashMap<String, List<M_SingleMoralRecord>> sortFeedbackByGrade(List<M_SingleMoralRecord> recordList) {
        HashMap<String, List<M_SingleMoralRecord>> gradeMap = new HashMap<>();
        for (M_SingleMoralRecord record : recordList) {
            String grade = record.getGrade();
            if (!gradeMap.containsKey(grade)) {
                gradeMap.put(grade, new ArrayList<>());
            }
            gradeMap.get(grade).add(record);
        }
        return gradeMap;
    }


    //textConfig先新增再删除
    private void textConfigDeleteAndInsert(String infoName, String textConfig, String school) {
        m_DefaultConfigMapper.deleteByInfoNameAndTextConfig(infoName, textConfig, school);
        M_DefaultConfig mDefaultConfig = new M_DefaultConfig();
        mDefaultConfig.setSchool(school);
        mDefaultConfig.setInfoName(infoName);
        mDefaultConfig.setTextConfig(textConfig);
        m_DefaultConfigMapper.insertDefaultConfig(mDefaultConfig);
    }

    //intConfig先删除再新增
    private void intConfigDeleteAndInsert(String infoName, Integer intConfig, String school) {
        m_DefaultConfigMapper.deleteByInfoNameAndSchool(infoName, school);
        m_DefaultConfigMapper.insertIntConfig(infoName, intConfig, school);

    }


    //根据默认配置表中的infoName，设置对应的加分和扣分系数
    private M_MoralStatisticDefaultParamsVO getDefaultAddSubScore(M_MoralStatisticDefaultParamsVO defaultParamsVO, M_DefaultConfig mDefaultConfig) {
        switch (mDefaultConfig.getInfoName()) {
            case "roadPraiseAddScore":
                defaultParamsVO.setRoadPraiseAddScore(mDefaultConfig.getIntConfig());
                break;
            case "roadCriticizeSubScore":
                defaultParamsVO.setRoadCriticizeSubScore(mDefaultConfig.getIntConfig());
                break;
            case "disciplinePraiseAddScore":
                defaultParamsVO.setDisciplinePraiseAddScore(mDefaultConfig.getIntConfig());
                break;
            case "disciplineCriticizeSubScore":
                defaultParamsVO.setDisciplineCriticizeSubScore(mDefaultConfig.getIntConfig());
                break;
            case "hygienePraiseAddScore":
                defaultParamsVO.setHygienePraiseAddScore(mDefaultConfig.getIntConfig());
                break;
            case "hygieneCriticizeSubScore":
                defaultParamsVO.setHygieneCriticizeSubScore(mDefaultConfig.getIntConfig());
                break;
            case "personalPraiseAddScore":
                defaultParamsVO.setPersonalPraiseAddScore(mDefaultConfig.getIntConfig());
                break;
            case "personalCriticizeSubScore":
                defaultParamsVO.setPersonalCriticizeSubScore(mDefaultConfig.getIntConfig());
                break;
            case "gymPraiseAddScore":
                defaultParamsVO.setGymPraiseAddScore(mDefaultConfig.getIntConfig());
                break;
            case "gymCriticizeSubScore":
                defaultParamsVO.setGymCriticizeSubScore(mDefaultConfig.getIntConfig());
                break;
            case "runPraiseAddScore":
                defaultParamsVO.setRunPraiseAddScore(mDefaultConfig.getIntConfig());
                break;
            case "runCriticizeSubScore":
                defaultParamsVO.setRunCriticizeSubScore(mDefaultConfig.getIntConfig());
                break;
            case "selfmanagePraiseAddScore":
                defaultParamsVO.setSelfmanagePraiseAddScore(mDefaultConfig.getIntConfig());
                break;
            case "readPraiseAddScore":
                defaultParamsVO.setReadPraiseAddScore(mDefaultConfig.getIntConfig());
                break;
            case "lessonPraiseAddScore":
                defaultParamsVO.setLessonPraiseAddScore(mDefaultConfig.getIntConfig());
                break;
            default:
                break;

        }
        return defaultParamsVO;
    }

    //对当前年级班级计数数据进行汇总并重新排序
    private List<M_ClassCountVO> sumAndSortClassCountList(List<List<M_ClassCountVO>> classCountListList) {
        //把多个List<M_ClassCountVO>进行汇总，按照className进行分组，把classCount进行相加，得到一个新的List<M_ClassCountVO>
        Map<String, Integer> classCountMap = new HashMap<>();
        for (List<M_ClassCountVO> classCountList : classCountListList) {
            for (M_ClassCountVO classCountVO : classCountList) {
                if (classCountMap.containsKey(classCountVO.getClassName())) {
                    Integer count = classCountMap.get(classCountVO.getClassName());
                    count += classCountVO.getClassCount();
                    classCountMap.put(classCountVO.getClassName(), count);
                } else {
                    classCountMap.put(classCountVO.getClassName(), classCountVO.getClassCount());
                }
            }
        }
        //把Map转化为List<M_ClassCountVO>
        List<M_ClassCountVO> classCountVOList = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : classCountMap.entrySet()) {
            M_ClassCountVO classCountVO = new M_ClassCountVO();
            classCountVO.setClassName(entry.getKey());
            classCountVO.setClassCount(entry.getValue());
            classCountVOList.add(classCountVO);
        }
        //按照classCount进行排序
        classCountVOList.sort(Comparator.comparing(M_ClassCountVO::getClassCount).reversed());


        return classCountVOList;
    }

    //根据标签获取班级对应的数量列表，所有的数量列表
    private List<List<M_ClassCountVO>> getClassCountListAndScore(LocalDate startDate, LocalDate endDate, String school, String grade, Integer topNum, List<String> labelList, Integer roadPraiseAddScore, Integer roadCriticizeSubScore, Integer disciplinePraiseAddScore, Integer disciplineCriticizeSubScore, Integer hygienePraiseAddScore, Integer hygieneCriticizeSubScore, Integer personalPraiseAddScore, Integer personalCriticizeSubScore, Integer gymPraiseAddScore, Integer gymCriticizeSubScore, Integer runPraiseAddScore, Integer runCriticizeSubScore, Integer selfmanagePraiseAddScore, Integer readPraiseAddScore, Integer lessonPraiseAddScore) {
        List<List<M_ClassCountVO>> classCountListList = new ArrayList<>();
        for (String label : labelList) {
            Integer score = 0;
            switch (label) {
                case "roadPraise":
                    score = roadPraiseAddScore;
                    break;
                case "roadCriticize":
                    score = roadCriticizeSubScore * -1;
                    break;
                case "disciplinePraise":
                    score = disciplinePraiseAddScore;
                    break;
                case "disciplineCriticize":
                    score = disciplineCriticizeSubScore * -1;
                    break;
                case "hygienePraise":
                    score = hygienePraiseAddScore;
                    break;
                case "hygieneCriticize":
                    score = hygieneCriticizeSubScore * -1;
                    break;
                case "personalPraise":
                    score = personalPraiseAddScore;
                    break;
                case "personalCriticize":
                    score = personalCriticizeSubScore * -1;
                    break;
                case "gymPraise":
                    score = gymPraiseAddScore;
                    break;
                case "gymCriticize":
                    score = gymCriticizeSubScore * -1;
                    break;
                case "runPraise":
                    score = runPraiseAddScore;
                    break;
                case "runCriticize":
                    score = runCriticizeSubScore * -1;
                    break;
                case "selfmanagePraise":
                    score = selfmanagePraiseAddScore;
                    break;
                case "readPraise":
                    score = readPraiseAddScore;
                    break;
                case "lessonPraise":
                    score = lessonPraiseAddScore;
                    break;


            }
            //如果label在前8个标签中，则调用getGradeClassCountScore方法获取班级计数数据
            if (label.equals("roadPraise") || label.equals("roadCriticize") || label.equals("disciplinePraise") || label.equals("disciplineCriticize") || label.equals("hygienePraise") || label.equals("hygieneCriticize") || label.equals("personalPraise") || label.equals("personalCriticize")) {
                List<M_ClassCountVO> classCountVOList = singleMoralRecordMapper.getGradeClassCountScore(startDate, endDate, school, grade, label, score);
                classCountListList.add(classCountVOList);
            } else if (label.equals("gymPraise") || label.equals("gymCriticize") || label.equals("runPraise") || label.equals("runCriticize")) {
                label = changeLabelToChinese(label);
                List<M_ClassCountVO> classCountVOList = sportRecordMapper.getGymRunGradeClassCountScore(startDate, endDate, school, grade, label, score);
                classCountListList.add(classCountVOList);     //如果label在gym和run,则调用体操和跑操的mapper方法获取班级计数数据
            } else if (label.equals("selfmanagePraise") || label.equals("readPraise") || label.equals("lessonPraise")) {
                label = changeLabelToChinese(label);
                List<M_ClassCountVO> classCountVOList = readingFeedbackMapper.getSelfmanageReadLeasonGradeClassCountScore(startDate, endDate, school, grade, label, score);
                classCountListList.add(classCountVOList);
            }
            //如果是label在后面三个标签中，则调用自主管理、读书和听课表扬的mapper方法获取班级计数数据


        }
        return classCountListList;
    }

    private String changeLabelToChinese(String label) {
        switch (label) {
            case "roadPraise":
                return "路队表扬";
            case "roadCriticize":
                return "路队批评";
            case "disciplinePraise":
                return "纪律表扬";
            case "disciplineCriticize":
                return "纪律批评";
            case "hygienePraise":
                return "卫生表扬";
            case "hygieneCriticize":
                return "卫生批评";
            case "personalPraise":
                return "个人表扬";
            case "personalCriticize":
                return "个人批评";
            case "gymPraise":
                return "体操表扬";
            case "gymCriticize":
                return "体操批评";
            case "runPraise":
                return "跑操表扬";
            case "runCriticize":
                return "跑操批评";
            case "selfmanagePraise":
                return "自主";
            case "readPraise":
                return "早读";
            case "lessonPraise":
                return "课前";
            default:
                return label;
        }
    }

    private M_MoralPersonalSelectedClassListVO getPersonalSelectedClassList(List<M_SingleMoralRecordVO> moralRecordList) {
        M_MoralPersonalSelectedClassListVO moralSelectedClassListVO = new M_MoralPersonalSelectedClassListVO();
        //假设moralRecord中有一个字段是label，表示标签
        //还有一个字段是class_name，表示班级名称

        HashMap<String, List<String>> personalPraiseSelectedClassListMap = new HashMap<>();
        HashMap<String, List<String>> personalCriticizeSelectedClassListMap = new HashMap<>();
        //假设moralRecord是一个列表
        for (M_SingleMoralRecordVO record : moralRecordList) {
            String label = record.getLabel();
            String grade = record.getGrade().substring(0, 1);
            String className = grade + "(" + record.getClassName().toString() + ")班";
            String supplement = record.getSupplement();
            if (label.equals("personalPraise")) {
                //拿到对应supplement的className放到map中
                List<String> supplementClassList = personalPraiseSelectedClassListMap.get(supplement);
                if (supplementClassList == null) {
                    supplementClassList = new ArrayList<>();
                    personalPraiseSelectedClassListMap.put(supplement, supplementClassList);
                }
                if (!supplementClassList.contains(className)) {
                    supplementClassList.add(className);
                }
            } else if (label.equals("personalCriticize")) {
                //拿到对应supplement的className放到map中
                List<String> supplementClassList = personalCriticizeSelectedClassListMap.get(supplement);
                if (supplementClassList == null) {
                    supplementClassList = new ArrayList<>();
                    personalCriticizeSelectedClassListMap.put(supplement, supplementClassList);
                }
                if (!supplementClassList.contains(className)) {
                    supplementClassList.add(className);
                }
            }
        }
        moralSelectedClassListVO.setPersonalPraiseSelectedClassListMap(personalPraiseSelectedClassListMap);
        moralSelectedClassListVO.setPersonalCriticizeSelectedClassListMap(personalCriticizeSelectedClassListMap);
        return moralSelectedClassListVO;
    }

    private String judgeStudentIdListDifference(List<Integer> studentIdList, List<Integer> dbStudentIdList) {
        Set<Integer> studentIdSet = new HashSet<>(studentIdList);
        Set<Integer> dbStudentIdSet = new HashSet<>(dbStudentIdList);
        if (studentIdSet.equals(dbStudentIdSet)) {
            return "norevise";
        } else if (dbStudentIdList.isEmpty()) {
            return "dbEmpty";
        } else {
            return "revised";
        }

    }


    private M_GradeClassNum parseClassNamesToGradeClassNumList(String className) {
        //首先判断className是不是空
        if (className == null || className.isEmpty()) {
            return null;
        }
        String grade = className.substring(0, 1) + "年级";
        //提取括号内的数字
        int startIndex = className.indexOf("(") + 1;
        int endIndex = className.indexOf(")");
        Integer classNum = null;
        if (startIndex > 0 && endIndex > startIndex) {
            String classNumStr = className.substring(startIndex, endIndex);
            try {
                classNum = Integer.parseInt(classNumStr);
            } catch (NumberFormatException e) {
                // 处理解析错误
                System.err.println("无法解析班级号码: " + classNumStr);
            }
        }
        M_GradeClassNum gradeClassNum = new M_GradeClassNum();
        gradeClassNum.setGrade(grade);
        gradeClassNum.setClassName(classNum);
        return gradeClassNum;

    }

    private M_MoralEightSituationDTO getMoralRecordByCheckDate(String school, LocalDate checkDate) {
        //从数据库中取出该学校该日期的所有班级名称
        List<M_GradeClassNum> gradeClassNumList = singleMoralRecordMapper.getClassByCheckDate(school, checkDate);
        String label = "roadPraise";
        List<String> roadPraiseClassList = getClassList(gradeClassNumList, label);
        label = "roadCriticize";
        List<String> roadCriticizeClassList = getClassList(gradeClassNumList, label);
        label = "hygienePraise";
        List<String> hygienePraiseClassList = getClassList(gradeClassNumList, label);
        label = "hygieneCriticize";
        List<String> hygieneCriticizeClassList = getClassList(gradeClassNumList, label);
        label = "disciplinePraise";
        List<String> disciplinePraiseClassList = getClassList(gradeClassNumList, label);
        label = "disciplineCriticize";
        List<String> disciplineCriticizeClassList = getClassList(gradeClassNumList, label);
//        label = "personalPraise";
//        List<String> personalPraiseClassList = getClassList(gradeClassNumList, label);
//        label = "personalCriticize";
//        List<String> personalCriticizeClassList = getClassList(gradeClassNumList, label);
        M_MoralEightSituationDTO moralEightSituationDTO = new M_MoralEightSituationDTO();
        moralEightSituationDTO.setSchool(school);
        moralEightSituationDTO.setCheckDate(checkDate);
        moralEightSituationDTO.setRoadPraiseClassNameList(roadPraiseClassList);
        moralEightSituationDTO.setRoadCriticizeClassNameList(roadCriticizeClassList);
        moralEightSituationDTO.setHygienePraiseClassNameList(hygienePraiseClassList);
        moralEightSituationDTO.setHygieneCriticizeClassNameList(hygieneCriticizeClassList);
        moralEightSituationDTO.setDisciplinePraiseClassNameList(disciplinePraiseClassList);
        moralEightSituationDTO.setDisciplineCriticizeClassNameList(disciplineCriticizeClassList);
//        moralEightSituationDTO.setPersonalPraiseClassNameList(personalPraiseClassList);
//        moralEightSituationDTO.setPersonalCriticizeClassNameList(personalCriticizeClassList);
        return moralEightSituationDTO;

    }

    private List<String> getClassList(List<M_GradeClassNum> gradeClassNumList, String label) {
        List<String> classNameList = new ArrayList<>();
        for (M_GradeClassNum gradeClassNum : gradeClassNumList) {
            if (gradeClassNum.getLabel().equals(label)) {
                //假设班级名称是“那么需要把2，4转化为班级名称
                String grade = gradeClassNum.getGrade().substring(0, 1);
                Integer classNum = gradeClassNum.getClassName();
                String className = grade + "(" + classNum + ")班";
                classNameList.add(className);
            }
        }
        return classNameList;
    }


    private List<Integer> getClassListByGradeLabel(String grade, String label, M_MoralEightSituationDTO moralData) {
        //label 分为roadPraise,roadCriticize,hygienePraise,hygieneCriticize,disciplinePraise,disciplineCriticize
        //如果是roadPraise开头的，则是路队相关，那就取出moralData.getRoadPraiseSelectedClasses\
        switch (label) {
            case "roadPraise":
                return convertClassNamesToIds(moralData.getRoadPraiseClassNameList(), grade);
            case "roadCriticize":
                return convertClassNamesToIds(moralData.getRoadCriticizeClassNameList(), grade);
            case "hygienePraise":
                return convertClassNamesToIds(moralData.getHygienePraiseClassNameList(), grade);
            case "hygieneCriticize":
                return convertClassNamesToIds(moralData.getHygieneCriticizeClassNameList(), grade);
            case "disciplinePraise":
                return convertClassNamesToIds(moralData.getDisciplinePraiseClassNameList(), grade);
            case "disciplineCriticize":
                return convertClassNamesToIds(moralData.getDisciplineCriticizeClassNameList(), grade);
//            case "personalPraise":
//                return convertClassNamesToIds(moralData.getPersonalPraiseClassNameList(),grade);
//            case "personalCriticize":
//                return convertClassNamesToIds(moralData.getPersonalCriticizeClassNameList(),grade);
            default:
                throw new IllegalArgumentException("Unknown label: " + label);
        }

    }

    private List<Integer> convertClassNamesToIds(List<String> classNames, String grade) {
        //假设班级名称是“六（1）班,五（2）班，五（4）班等”grade是五年级”，那么需要把2，4转化为班级ID
        List<Integer> ClsassNumList = new ArrayList<>();
        for (String className : classNames) {
            //拿出className的第一个字进行判断
            String firstCharClass = className.substring(0, 1);
            String firstCharGrade = grade.substring(0, 1);
            if (firstCharClass.equals(firstCharGrade)) {

                //提取括号内的数字
                int startIndex = className.indexOf("(") + 1;
                int endIndex = className.indexOf(")");
                if (startIndex > 0 && endIndex > startIndex) {
                    String classNumStr = className.substring(startIndex, endIndex);
                    try {
                        int classNum = Integer.parseInt(classNumStr);
                        ClsassNumList.add(classNum);
                    } catch (NumberFormatException e) {
                        // 处理解析错误
                        System.err.println("无法解析班级号码: " + classNumStr);
                    }
                }

            }
        }
        return ClsassNumList;
    }


}
