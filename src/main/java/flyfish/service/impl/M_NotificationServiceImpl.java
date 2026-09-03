package flyfish.service.impl;

import flyfish.mapper.*;
import flyfish.pojo.*;
import flyfish.pojo.Record;
import flyfish.service.M_NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

@Service
public class M_NotificationServiceImpl implements M_NotificationService {
    @Autowired
    private M_TeacherRoleMapper teacherRoleMapper;
    @Autowired
    private M_ClassTeacherRelationMapper classTeacherRelationMapper;
    @Autowired
    private M_ReadingFeedbackMapper readingFeedbackMapper;
    @Autowired
    private M_SingleReadTeacherRecordMapper singleReadTeacherRecordMapper;
    @Autowired
    private M_GradeClassNumMapper gradeClassNumMapper;
    @Autowired
    private M_SingleMoralRecordMapper singleMoralRecordMapper;
    @Autowired
    private RecordMapper recordMapper;
    @Autowired
    private PerformMapper performMapper;
    @Autowired
    RecordTaskMapper recordTaskMapper;
    @Autowired
    private AccumulateScoreMapper accumulateScoreMapper;

    @Override
    public String getNotificationBySchoolUserName(String school, String username, LocalDate startDate, LocalDate endDate) {
        //查询用户角色，如果不是班主任，直接返回提醒
        String title = "班主任";
        Integer id = teacherRoleMapper.getIdBySchoolAndUsername(school, username, title);
        if (id == null) {
            return "失败";
        }
        //如果是班主任，那么查到是哪个班级的班主任，如果差不多班级，也返回提醒。
        String headTeacher = "是";
        String className = classTeacherRelationMapper.getClassNameBySchoolTeacherNameHeadTeacher(school, username, headTeacher);

        if (className == null) {
            return "失败";
        }
        //根据班级，查询早读巡查的情况，看看是否有被表扬，是否有教师被表扬

        String readingResp = getReadingNotificationBySchoolClassName(school, className, startDate, endDate);
        //根据班级，查询德育巡查的情况，看看是否有路队、纪律、卫生表扬或者批评，学生个人是否有被表扬或者批评（先展示个人情况）

        String moralResp = getMoralNotificationBySchoolClassName(school, className, startDate, endDate);

        //根据班级，查询早操巡查的情况，看看是否有被表扬或者批评
        String sportResp = getSportNotificationBySchoolClassName(school, className);

        //如果没有查询到内容，则返回提醒
        String resp = "";

        if (readingResp != null || !readingResp.equals("")) {
            resp += readingResp;
        }

        if (moralResp != null && !moralResp.equals("")) {
            resp += moralResp;
        }
        if (sportResp != null && !sportResp.equals("")) {
            resp += sportResp;
        }

        return resp;

    }

    @Override
    public String getNotificationForStudentBySchoolUserName(String school, String username, LocalDate startDate, LocalDate endDate, String studentClassName) {
        String resp = "";
        //把班级的名称更换样式，原本的四(3)班改成43，需要设置一个私有方法
        String grade = studentClassName.substring(0, 1);
        String gradeNumber = convertChineseToNumber(grade);
        String classNumber = gradeNumber + studentClassName.substring(2, 3);


        //拿到所有学生的作业record数据
        //拿到所有的课堂表现情况
        //拿到所有的积分情况
        String subject = "数学";
        String situation = "表扬";
        String reason = "优秀作业";
        String homeworkgoodReason = "优秀作业";
        String homeworkbadReason = "作业不达标";
        List<Record> mathhomeworkRecordList = recordMapper.getRecordBySchoolClassName(classNumber, username, startDate, endDate, subject);
        List<Perform> mathperformList = performMapper.getPerformBySchoolClassName(classNumber, username, startDate, endDate, subject, homeworkgoodReason, homeworkbadReason);
        AccumulateScore mathaccumulateScore = accumulateScoreMapper.getAccumulateScoreBySchoolClassName(classNumber, username, subject);
        List<RecordTask> mathrecordTaskList = recordTaskMapper.getRecordTaskBySchoolClassName(classNumber, username, startDate, endDate, subject);
        //表扬和扣分的具体情况
        Integer mathhomeworkAddScore = performMapper.getSumScoreBySchoolClassNameReason(classNumber, username, startDate, endDate, subject, situation, reason);
        Integer mathclassAddScore = performMapper.getSumClassScoreBySchoolClassNameReason(classNumber, username, startDate, endDate, subject, situation, reason);
        reason = "作业不达标";
        situation = "批评";
        Integer mathhomeworkMinusScore = performMapper.getSumScoreBySchoolClassNameReason(classNumber, username, startDate, endDate, subject, situation, reason);
        Integer mathclassMinusScore = performMapper.getSumClassScoreBySchoolClassNameReason(classNumber, username, startDate, endDate, subject, situation, reason);
        resp += getHomeWorkNotificationBySubject(subject, mathhomeworkRecordList, mathperformList, mathaccumulateScore, mathrecordTaskList, startDate, endDate, mathhomeworkAddScore, mathhomeworkMinusScore, mathclassAddScore, mathclassMinusScore);


        subject = "语文";
        situation = "表扬";
        reason = "优秀作业";
        List<Record> chinesehomeworkRecordList = recordMapper.getRecordBySchoolClassName(classNumber, username, startDate, endDate, subject);
        List<Perform> chineseperformList = performMapper.getPerformBySchoolClassName(classNumber, username, startDate, endDate, subject, homeworkgoodReason, homeworkbadReason);
        AccumulateScore chineseaccumulateScore = accumulateScoreMapper.getAccumulateScoreBySchoolClassName(classNumber, username, subject);
        List<RecordTask> chineserecordTaskList = recordTaskMapper.getRecordTaskBySchoolClassName(classNumber, username, startDate, endDate, subject);
        //表扬和扣分的具体情况
        Integer chinesehomeworkAddScore = performMapper.getSumScoreBySchoolClassNameReason(classNumber, username, startDate, endDate, subject, situation, reason);
        Integer chineseclassAddScore = performMapper.getSumClassScoreBySchoolClassNameReason(classNumber, username, startDate, endDate, subject, situation, reason);
        reason = "作业不达标";
        situation = "批评";
        Integer chinesehomeworkMinusScore = performMapper.getSumScoreBySchoolClassNameReason(classNumber, username, startDate, endDate, subject, situation, reason);
        Integer chineseclassMinusScore = performMapper.getSumClassScoreBySchoolClassNameReason(classNumber, username, startDate, endDate, subject, situation, reason);
        resp += getHomeWorkNotificationBySubject(subject, chinesehomeworkRecordList, chineseperformList, chineseaccumulateScore, chineserecordTaskList, startDate, endDate, chinesehomeworkAddScore, chinesehomeworkMinusScore, chineseclassAddScore, chineseclassMinusScore);

        subject = "英语";
        situation = "表扬";
        reason = "优秀作业";
        List<Record> englishhomeworkRecordList = recordMapper.getRecordBySchoolClassName(classNumber, username, startDate, endDate, subject);
        List<Perform> englishperformList = performMapper.getPerformBySchoolClassName(classNumber, username, startDate, endDate, subject, homeworkgoodReason, homeworkbadReason);
        AccumulateScore englishaccumulateScore = accumulateScoreMapper.getAccumulateScoreBySchoolClassName(classNumber, username, subject);
        List<RecordTask> englishrecordTaskList = recordTaskMapper.getRecordTaskBySchoolClassName(classNumber, username, startDate, endDate, subject);
        //表扬和扣分的具体情况
        Integer englishhomeworkAddScore = performMapper.getSumScoreBySchoolClassNameReason(classNumber, username, startDate, endDate, subject, situation, reason);
        Integer englishclassAddScore = performMapper.getSumClassScoreBySchoolClassNameReason(classNumber, username, startDate, endDate, subject, situation, reason);
        reason = "作业不达标";
        situation = "批评";
        Integer englishhomeworkMinusScore = performMapper.getSumScoreBySchoolClassNameReason(classNumber, username, startDate, endDate, subject, situation, reason);
        Integer englishclassMinusScore = performMapper.getSumClassScoreBySchoolClassNameReason(classNumber, username, startDate, endDate, subject, situation, reason);
        resp += getHomeWorkNotificationBySubject(subject, englishhomeworkRecordList, englishperformList, englishaccumulateScore, englishrecordTaskList, startDate, endDate, englishhomeworkAddScore, englishhomeworkMinusScore, englishclassAddScore, englishclassMinusScore);
        return resp;

    }

    private String convertChineseToNumber(String grade) {
        String number = "";
        switch (grade) {
            case "一":
                number = "1";
                break;
            case "二":
                number = "2";
                break;
            case "三":
                number = "3";
                break;
            case "四":
                number = "4";
                break;
            case "五":
                number = "5";
                break;
            case "六":
                number = "6";
                break;
            case "七":
                number = "7";
                break;
            case "八":
                number = "8";
                break;
            case "九":
                number = "9";
                break;
            default:
                number = grade;
        }
        return number;
    }

    //根据学科，生成不同的通知内容
    private String getHomeWorkNotificationBySubject(String subject, List<Record> homeworkRecordList, List<Perform> performList, AccumulateScore accumulateScore, List<RecordTask> recordTaskList, LocalDate startDate, LocalDate endDate, Integer homeworkAddScore, Integer homeworkMinusScore, Integer classAddScore, Integer classMinusScore) {
        String subjectResp = "";
        if (startDate.equals(endDate)) {
            if (accumulateScore.getAddscore() != 0 || accumulateScore.getMinusscore() != 0 || accumulateScore.getPunishscore() != 0 || (homeworkRecordList != null && homeworkRecordList.size() > 0) || (performList != null && performList.size() > 0) || (recordTaskList != null && recordTaskList.size() > 0)) {
                subjectResp += "【" + subject + "学科】\n";
                Integer count = 0;
                if (homeworkRecordList != null && homeworkRecordList.size() > 0) {
                    List<Record> unsubmitRecordList = new ArrayList<>();
                    List<Record> excellentRecordList = new ArrayList<>();
                    List<Record> unqualifiedRecordList = new ArrayList<>();
                    for (Record record : homeworkRecordList) {
                        if (record.getCompleted() != null) {
                            if (record.getCompleted() == 0) {
                                unsubmitRecordList.add(record);
                            }
                            if (record.getLevel() != null) {
                                if (record.getCompleted() == 1 && record.getLevel() == 1) {
                                    excellentRecordList.add(record);
                                }
                                if (record.getCompleted() == 1 && record.getLevel() == -1) {
                                    unqualifiedRecordList.add(record);
                                }
                            }
                        }
                    }
                    if (unsubmitRecordList.size() > 0) {
                        count += 1;
                        subjectResp += count + ".未提交作业：";
                        for (Record record : unsubmitRecordList) {
                            subjectResp += record.getContent() + "、";
                        }
                        subjectResp = subjectResp.substring(0, subjectResp.length() - 1) + "\n\n";
                    }
                    if (excellentRecordList.size() > 0) {
                        count += 1;
                        subjectResp += count + ".优秀作业：";
                        for (Record record : excellentRecordList) {
                            subjectResp += record.getContent() + "、";
                        }
                        subjectResp = subjectResp.substring(0, subjectResp.length() - 1) + "\n\n";
                    }
                    if (unqualifiedRecordList.size() > 0) {
                        count += 1;
                        subjectResp += count + ".不达标作业：";
                        for (Record record : unqualifiedRecordList) {
                            subjectResp += record.getContent() + "、";
                        }
                        subjectResp = subjectResp.substring(0, subjectResp.length() - 1) + "\n\n";
                    }
                }

                if (performList != null && performList.size() > 0) {
                    Integer praiseCount = 0;
                    Integer criticizeCount = 0;
                    String praiseReason = "";
                    List<String> praiseReasonList = new ArrayList<>();
                    String criticizeReason = "";
                    List<String> criticizeReasonList = new ArrayList<>();
                    for (Perform perform : performList) {
                        if (perform.getSituation().equals("表扬")) {
                            praiseCount += 1;
                            praiseReasonList.add(perform.getReason());
                        }
                        if (perform.getSituation().equals("批评")) {
                            criticizeCount += 1;
                            criticizeReasonList.add(perform.getReason());
                        }
                    }
                    HashSet<String> praiseReasonSet = new HashSet<>(praiseReasonList);
                    for (String reason : praiseReasonSet) {
                        praiseReason += reason + "、";
                    }
                    HashSet<String> criticizeReasonSet = new HashSet<>(criticizeReasonList);
                    for (String reason : criticizeReasonSet) {
                        criticizeReason += reason + "、";
                    }
                    if (praiseCount > 0) {
                        count += 1;
                        subjectResp += count + ".课堂表现：表扬" + praiseCount + "次，因为：" + praiseReason.substring(0, praiseReason.length() - 1) + "\n\n";
                    }
                    if (criticizeCount > 0) {
                        count += 1;
                        subjectResp += count + ".课堂表现：扣分" + criticizeCount + "次，因为：" + criticizeReason.substring(0, criticizeReason.length() - 1) + "\n\n";
                    }
                }


                if (accumulateScore.getAddscore() != 0 || accumulateScore.getMinusscore() != 0 || accumulateScore.getPunishscore() != 0) {
                    count += 1;
                    subjectResp += count + ".当前积分：现有" + (accumulateScore.getAddscore() + accumulateScore.getMinusscore()) + "分，";
                    if (accumulateScore.getAddscore() > 0) {
                        subjectResp += "加分" + accumulateScore.getAddscore() + "分（今日作业表扬" + homeworkAddScore + "分，今日课堂表扬" + classAddScore + "分），";
                    }
                    if (accumulateScore.getPunishscore() * -1 > 0) {
                        subjectResp += "扣分" + accumulateScore.getPunishscore() * -1 + "分（今日作业扣分" + homeworkMinusScore + "分，今日课堂扣分" + classMinusScore + "分），";
                    }
                    if ((accumulateScore.getPunishscore() - accumulateScore.getMinusscore()) > 0) {
                        subjectResp += "兑换" + (accumulateScore.getPunishscore() - accumulateScore.getMinusscore()) + "分，";
                    }
                    subjectResp = subjectResp.substring(0, subjectResp.length() - 1) + "\n\n";
                }

                if (recordTaskList != null && recordTaskList.size() > 0) {
                    List<RecordTask> unqualifiedTaskList = new ArrayList<>();
                    for (RecordTask recordTask : recordTaskList) {
                        if (recordTask.getCompleted() == 0) {
                            unqualifiedTaskList.add(recordTask);
                        }
                    }
                    if (unqualifiedTaskList.size() > 0) {
                        count += 1;
                        subjectResp += count + ".过关任务：";
                        for (RecordTask recordTask : unqualifiedTaskList) {
                            subjectResp += recordTask.getContent() + "、";
                        }
                        subjectResp = subjectResp.substring(0, subjectResp.length() - 1) + "不过关\n\n";
                    }
                }

            }
            return subjectResp;
            //展示当天的信息
            //【数学学科】
            //1.未提交作业：知能、课本
            //2.优秀作业：计算打卡
            //3.不达标作业：错题
            //4.课堂表现：表扬x次，因为：课堂表现优秀；扣分x次，因为：课堂表现不佳
            //5.当前积分：现有x分，加分x分（作业表扬x分，课堂表扬x分），扣分（作业扣分x分，课堂扣分x分），兑换x分。
            //6.过关任务：2222日、22234日不过关
        } else {
            if (accumulateScore.getAddscore() != 0 || accumulateScore.getMinusscore() != 0 || accumulateScore.getPunishscore() != 0 || (homeworkRecordList != null && homeworkRecordList.size() > 0) || (performList != null && performList.size() > 0) || (recordTaskList != null && recordTaskList.size() > 0)) {
                subjectResp += "【" + subject + "学科】\n";
                Integer count = 0;
                if (homeworkRecordList != null && homeworkRecordList.size() > 0) {
                    List<Record> unsubmitRecordList = new ArrayList<>();
                    List<Record> excellentRecordList = new ArrayList<>();
                    List<Record> unqualifiedRecordList = new ArrayList<>();
                    for (Record record : homeworkRecordList) {
                        if (record.getCompleted() != null) {
                            if (record.getCompleted() == 0) {
                                unsubmitRecordList.add(record);
                            }
                            if (record.getLevel() != null) {
                                if (record.getCompleted() == 1 && record.getLevel() == 1) {
                                    excellentRecordList.add(record);
                                }
                                if (record.getCompleted() == 1 && record.getLevel() == -1) {
                                    unqualifiedRecordList.add(record);
                                }
                            }
                        }
                    }
                    if (unsubmitRecordList.size() > 0) {
                        count += 1;
                        subjectResp += count + ".未提交作业：共" + unsubmitRecordList.size() + "次，";

                        for (Record record : unsubmitRecordList) {
                            String date = changeLocalDateToStringDate(record.getCheckdate());
                            subjectResp += date + record.getContent() + "、";
                        }
                        subjectResp = subjectResp.substring(0, subjectResp.length() - 1) + "\n\n";
                    }
                    if (excellentRecordList.size() > 0) {
                        count += 1;
                        subjectResp += count + ".优秀作业：共" + excellentRecordList.size() + "次，";
                        for (Record record : excellentRecordList) {
                            String date = changeLocalDateToStringDate(record.getCheckdate());
                            subjectResp += date + record.getContent() + "、";

                        }
                        subjectResp = subjectResp.substring(0, subjectResp.length() - 1) + "\n\n";
                    }
                    if (unqualifiedRecordList.size() > 0) {
                        count += 1;
                        subjectResp += count + ".不达标作业：共" + unqualifiedRecordList.size() + "次，";
                        for (Record record : unqualifiedRecordList) {
                            String date = changeLocalDateToStringDate(record.getCheckdate());
                            subjectResp += date + record.getContent() + "、";
                        }
                        subjectResp = subjectResp.substring(0, subjectResp.length() - 1) + "\n\n";
                    }
                }

                if (performList != null && performList.size() > 0) {
                    Integer praiseCount = 0;
                    Integer criticizeCount = 0;
                    String praiseReason = "";
                    List<String> praiseReasonList = new ArrayList<>();
                    String criticizeReason = "";
                    List<String> criticizeReasonList = new ArrayList<>();
                    for (Perform perform : performList) {
                        if (perform.getSituation().equals("表扬")) {
                            praiseCount += 1;
                            String date = changeLocalDateToStringDate(perform.getCheckdate());
                            praiseReasonList.add(date + perform.getReason());
                        }
                        if (perform.getSituation().equals("批评")) {
                            criticizeCount += 1;
                            String date = changeLocalDateToStringDate(perform.getCheckdate());
                            criticizeReasonList.add(date + perform.getReason());
                        }
                    }
                    HashSet<String> praiseReasonSet = new HashSet<>(praiseReasonList);
                    for (String reason : praiseReasonSet) {
                        praiseReason += reason + "、";
                    }
                    HashSet<String> criticizeReasonSet = new HashSet<>(criticizeReasonList);
                    for (String reason : criticizeReasonSet) {
                        criticizeReason += reason + "、";
                    }
                    if (praiseCount > 0) {
                        count += 1;
                        subjectResp += count + ".课堂表现：表扬共" + praiseCount + "次，因为：" + praiseReason.substring(0, praiseReason.length() - 1) + "\n\n";
                    }
                    if (criticizeCount > 0) {
                        count += 1;
                        subjectResp += count + ".课堂表现：扣分共" + criticizeCount + "次，因为：" + criticizeReason.substring(0, criticizeReason.length() - 1) + "\n\n";
                    }
                }
                if (accumulateScore.getAddscore() != 0 || accumulateScore.getMinusscore() != 0 || accumulateScore.getPunishscore() != 0) {
                    count += 1;
                    String start = changeLocalDateToStringDate(startDate);
                    String end = changeLocalDateToStringDate(endDate);
                    String dateRange = start.equals(end) ? start : start + "至" + end;
                    subjectResp += count + ".当前积分：现有" + (accumulateScore.getAddscore() + accumulateScore.getMinusscore()) + "分，";
                    if (accumulateScore.getAddscore() > 0) {
                        subjectResp += "加分" + accumulateScore.getAddscore() + "分（" + dateRange + "作业表扬" + homeworkAddScore + "分，" + dateRange + "课堂表扬" + classAddScore + "分），";
                    }
                    if (accumulateScore.getPunishscore() * -1 > 0) {
                        subjectResp += "扣分" + accumulateScore.getPunishscore() * -1 + "分（" + dateRange + "作业扣分" + homeworkMinusScore + "分，" + dateRange + "课堂扣分" + classMinusScore + "分），";
                    }
                    if ((accumulateScore.getPunishscore() - accumulateScore.getMinusscore()) > 0) {
                        subjectResp += "兑换" + (accumulateScore.getPunishscore() - accumulateScore.getMinusscore()) + "分，";
                    }
                    subjectResp = subjectResp.substring(0, subjectResp.length() - 1) + "\n\n";
                }
                if (recordTaskList != null && recordTaskList.size() > 0) {
                    List<RecordTask> unqualifiedTaskList = new ArrayList<>();
                    for (RecordTask recordTask : recordTaskList) {
                        if (recordTask.getCompleted() == 0) {
                            unqualifiedTaskList.add(recordTask);
                        }
                    }
                    if (unqualifiedTaskList.size() > 0) {
                        count += 1;
                        subjectResp += count + ".过关任务：共" + unqualifiedTaskList.size() + "次，";
                        for (RecordTask recordTask : unqualifiedTaskList) {

                            subjectResp += recordTask.getContent() + "、";
                        }
                        subjectResp = subjectResp.substring(0, subjectResp.length() - 1) + "不过关\n\n";
                    }
                }


                //展示一段时间的信息
                //【数学学科】
                //1.未提交作业：共x次，3月12日知能、3月13日课本
                //2.优秀作业：共x次，3月12日计算打卡、3月13日计算打卡
                //3.不达标作业：共x次，3月12日错题、3月13日错题
                //4.课堂表现：表扬共x次，因为：3月11日课堂表现优秀；扣分共x次，因为：3月12日课堂表现不佳
                //5.当前积分：现有x分，加分x分（作业表扬x分，课堂表扬x分），扣分（作业扣分x分，课堂扣分x分），兑换x分。
                //6.过关任务：2222日、22234日不过关
            }
            return subjectResp;

        }
    }

    private String changeLocalDateToStringDate(LocalDate checkdate) {
        //原本的日期格式是2024-03-12，转换成3月12日
        String date = "";
        String month = checkdate.getMonthValue() + "月";
        String day = checkdate.getDayOfMonth() + "日";
        date = month + day;
        return date;
    }

    private String getSportNotificationBySchoolClassName(String school, String className) {
        return null;
    }

    private String getMoralNotificationBySchoolClassName(String school, String className, LocalDate startDate, LocalDate endDate) {
        String resp = "";
        String grade = className.substring(0, 1) + "年级";
        Integer classNum = Integer.valueOf(className.substring(2, 3));
        if (startDate.equals(endDate)) {
            //查询当天的德育情况
            String label = "personalCriticize";
            Integer personalCriticizeNum = singleMoralRecordMapper.getIdBySchoolClassNameLabel(school, grade, classNum, label, startDate);
            label = "personalPraise";
            Integer personalPraiseNum = singleMoralRecordMapper.getIdBySchoolClassNameLabel(school, grade, classNum, label, startDate);
            label = "roadCriticize";
            Integer roadCriticizeNum = singleMoralRecordMapper.getIdBySchoolClassNameLabel(school, grade, classNum, label, startDate);
            label = "roadPraise";
            Integer roadPraiseNum = singleMoralRecordMapper.getIdBySchoolClassNameLabel(school, grade, classNum, label, startDate);
            label = "disciplineCriticize";
            Integer disciplineCriticizeNum = singleMoralRecordMapper.getIdBySchoolClassNameLabel(school, grade, classNum, label, startDate);
            label = "disciplinePraise";
            Integer disciplinePraiseNum = singleMoralRecordMapper.getIdBySchoolClassNameLabel(school, grade, classNum, label, startDate);
            label = "hygieneCriticize";
            Integer hygieneCriticizeNum = singleMoralRecordMapper.getIdBySchoolClassNameLabel(school, grade, classNum, label, startDate);
            label = "hygienePraise";
            Integer hygienePraiseNum = singleMoralRecordMapper.getIdBySchoolClassNameLabel(school, grade, classNum, label, startDate);

            //先呈现 德育提醒 再呈现德育表彰。表彰和批评分开展示，先个人，再路队、纪律、卫生

            if (personalCriticizeNum != null || roadCriticizeNum != null || disciplineCriticizeNum != null || hygieneCriticizeNum != null) {
                resp += "【德育提醒】\n";
                Integer count = 0;
                if (personalCriticizeNum != null) {
                    count += 1;
                    label = "personalCriticize";
                    List<M_SingleMoralRecord> personalCriticizeRecordList = singleMoralRecordMapper.getRecordBySchoolClassNameLabel(school, grade, classNum, label, startDate, endDate);
                    String studentSupplement = "";
                    if (personalCriticizeRecordList != null && personalCriticizeRecordList.size() > 0) {
                        for (M_SingleMoralRecord singleMoralRecord : personalCriticizeRecordList) {
                            studentSupplement += singleMoralRecord.getStudentName() + "(" + singleMoralRecord.getSupplement() + ")";
                            //如果不是最后一个，就加、分隔
                            if (singleMoralRecord != personalCriticizeRecordList.get(personalCriticizeRecordList.size() - 1)) {
                                studentSupplement += "、";
                            }
                        }
                    }
                    resp += count + ".学生扣分" + 1 + "次，" + "扣分原因:" + studentSupplement + "\n";
                }
                if (roadCriticizeNum != null) {
                    count += 1;
                    resp += count + ".路队扣分" + 1 + "次\n";
                }
                if (hygieneCriticizeNum != null) {
                    count += 1;
                    resp += count + ".卫生扣分" + 1 + "次\n";
                }
                if (disciplineCriticizeNum != null) {
                    count += 1;
                    resp += count + ".纪律扣分" + 1 + "次\n";
                }
                resp += "\n";


            }
            if (personalPraiseNum != null || roadPraiseNum != null || disciplinePraiseNum != null || hygienePraiseNum != null) {
                resp += "【德育表彰】\n";
                Integer count = 0;
                if (personalPraiseNum != null) {
                    count += 1;
                    label = "personalPraise";
                    List<M_SingleMoralRecord> personalPraiseRecordList = singleMoralRecordMapper.getRecordBySchoolClassNameLabel(school, grade, classNum, label, startDate, endDate);
                    String studentSupplement = "";
                    if (personalPraiseRecordList != null && personalPraiseRecordList.size() > 0) {
                        for (M_SingleMoralRecord singleMoralRecord : personalPraiseRecordList) {
                            studentSupplement += singleMoralRecord.getStudentName() + "(" + singleMoralRecord.getSupplement() + ")";
                            //如果不是最后一个，就加、分隔
                            if (singleMoralRecord != personalPraiseRecordList.get(personalPraiseRecordList.size() - 1)) {
                                studentSupplement += "、";
                            }
                        }
                    }
                    resp += count + ".学生加分" + 1 + "次，" + "加分原因:" + studentSupplement + "\n";
                }
                if (roadPraiseNum != null) {
                    count += 1;
                    resp += count + ".路队加分" + 1 + "次\n";
                }
                if (hygienePraiseNum != null) {
                    count += 1;
                    resp += count + ".卫生加分" + 1 + "次\n";
                }
                if (disciplinePraiseNum != null) {
                    count += 1;
                    resp += count + ".纪律加分" + 1 + "次\n";
                }
                resp += "\n";

            }


        } else {

            //获取年级平均数量

            //获得年级的班级数量
            Integer gradeClassNum = gradeClassNumMapper.getClassNumBySchoolGrade(school, grade);
            if (gradeClassNum == null || gradeClassNum == 0) {
                gradeClassNum = 1;
            }

            String label = "personalCriticize";
            String personalCriticizeFeedback = getMoralFeedbackPersonal(school, classNum, startDate, endDate, grade, label, gradeClassNum);

            label = "personalPraise";
            String personalPraiseFeedback = getMoralFeedbackPersonal(school, classNum, startDate, endDate, grade, label, gradeClassNum);

            label = "roadCriticize";
            String roadCriticizeFeedback = getMoralFeedbackClassCriticize(school, classNum, startDate, endDate, grade, label, gradeClassNum);

            label = "roadPraise";
            String roadPraiseFeedback = getMoralFeedbackClassPraise(school, classNum, startDate, endDate, grade, label, gradeClassNum);

            label = "disciplineCriticize";
            String disciplineCriticizeFeedback = getMoralFeedbackClassCriticize(school, classNum, startDate, endDate, grade, label, gradeClassNum);

            label = "disciplinePraise";
            String disciplinePraiseFeedback = getMoralFeedbackClassPraise(school, classNum, startDate, endDate, grade, label, gradeClassNum);

            label = "hygieneCriticize";
            String hygieneCriticizeFeedback = getMoralFeedbackClassCriticize(school, classNum, startDate, endDate, grade, label, gradeClassNum);

            label = "hygienePraise";
            String hygienePraiseFeedback = getMoralFeedbackClassPraise(school, classNum, startDate, endDate, grade, label, gradeClassNum);

            if (!personalCriticizeFeedback.equals("") || !roadCriticizeFeedback.equals("") || !disciplineCriticizeFeedback.equals("") || !hygieneCriticizeFeedback.equals("")) {
                resp += "【德育提醒】\n";
                Integer count = 0;
                if (!personalCriticizeFeedback.equals("")) {
                    count += 1;
                    resp += count + "." + personalCriticizeFeedback;
                }
                if (!roadCriticizeFeedback.equals("")) {
                    count += 1;
                    resp += count + "." + roadCriticizeFeedback;
                }
                if (!hygieneCriticizeFeedback.equals("")) {
                    count += 1;
                    resp += count + "." + hygieneCriticizeFeedback;
                }
                if (!disciplineCriticizeFeedback.equals("")) {
                    count += 1;
                    resp += count + "." + disciplineCriticizeFeedback;
                }

                resp += "\n";
            }

            if (!personalPraiseFeedback.equals("") || !roadPraiseFeedback.equals("") || !disciplinePraiseFeedback.equals("") || !hygienePraiseFeedback.equals("")) {
                resp += "【德育表彰】\n";
                Integer count = 0;
                if (!personalPraiseFeedback.equals("")) {
                    count += 1;
                    resp += count + "." + personalPraiseFeedback;
                }
                if (!roadPraiseFeedback.equals("")) {
                    count += 1;
                    resp += count + "." + roadPraiseFeedback;
                }
                if (!hygienePraiseFeedback.equals("")) {
                    count += 1;
                    resp += count + "." + hygienePraiseFeedback;
                }
                if (!disciplinePraiseFeedback.equals("")) {
                    count += 1;
                    resp += count + "." + disciplinePraiseFeedback;
                }

                resp += "\n";
            }


        }
        return resp;


    }

    private String getMoralFeedbackClassPraise(String school, Integer classNum, LocalDate startDate, LocalDate endDate, String grade, String label, Integer gradeClassNum) {
        Integer countNum = singleMoralRecordMapper.getCountNumBySchoolClassNameLabel(school, grade, classNum, label, startDate, endDate);
        Integer gradeAllCountNum = singleMoralRecordMapper.getGradeAvgNumByDateTimeZone(school, grade, label, startDate, endDate);
        Integer gradeAvgCountNum = gradeAllCountNum / gradeClassNum;
        String resp = "";
        if (countNum != null && countNum > 0) {
            if (countNum >= gradeAvgCountNum) {
                switch (label) {
                    case "roadPraise":
                        resp += "路队加分" + countNum + "次，数量在年级前50%\n";
                        break;
                    case "disciplinePraise":
                        resp += "纪律加分" + countNum + "次，数量在年级前50%\n";
                        break;
                    case "hygienePraise":
                        resp += "卫生加分" + countNum + "次，数量在年级前50%\n";
                        break;
                }

            } else {
                switch (label) {
                    case "roadPraise":
                        resp += "路队加分" + countNum + "次，数量在年级后50%\n";
                        break;
                    case "disciplinePraise":
                        resp += "纪律加分" + countNum + "次，数量在年级后50%\n";
                        break;
                    case "hygienePraise":
                        resp += "卫生加分" + countNum + "次，数量在年级后50%\n";
                        break;
                }
            }
        }
        return resp;
    }

    private String getMoralFeedbackClassCriticize(String school, Integer classNum, LocalDate startDate, LocalDate endDate, String grade, String label, Integer gradeClassNum) {
        Integer countNum = singleMoralRecordMapper.getCountNumBySchoolClassNameLabel(school, grade, classNum, label, startDate, endDate);
        Integer gradeAllCountNum = singleMoralRecordMapper.getGradeAvgNumByDateTimeZone(school, grade, label, startDate, endDate);
        Integer gradeAvgCountNum = gradeAllCountNum / gradeClassNum;

        String resp = "";
        if (countNum != null && countNum > 0) {
            if (countNum >= gradeAvgCountNum) {
                switch (label) {
                    case "roadCriticize":
                        resp += "路队扣分" + countNum + "次，数量在年级前50%\n";
                        break;
                    case "disciplineCriticize":
                        resp += "纪律扣分" + countNum + "次，数量在年级前50%\n";
                        break;
                    case "hygieneCriticize":
                        resp += "卫生扣分" + countNum + "次，数量在年级前50%\n";
                        break;
                }
            } else {
                switch (label) {
                    case "roadCriticize":
                        resp += "路队扣分" + countNum + "次，数量在年级后50%\n";
                        break;
                    case "disciplineCriticize":
                        resp += "纪律扣分" + countNum + "次，数量在年级后50%\n";
                        break;
                    case "hygieneCriticize":
                        resp += "卫生扣分" + countNum + "次，数量在年级后50%\n";
                        break;
                }
            }
        }
        return resp;

    }

    private String getMoralFeedbackPersonal(String school, Integer classNum, LocalDate startDate, LocalDate endDate, String grade, String label, Integer gradeClassNum) {
        Integer countNum = singleMoralRecordMapper.getCountNumBySchoolClassNameLabel(school, grade, classNum, label, startDate, endDate);
        Integer gradeAllCountNum = singleMoralRecordMapper.getGradeAvgNumByDateTimeZone(school, grade, label, startDate, endDate);
        Integer gradeAvgCountNum = gradeAllCountNum / gradeClassNum;
        List<M_SingleMoralRecord> personalPraiseRecordList = singleMoralRecordMapper.getRecordBySchoolClassNameLabel(school, grade, classNum, label, startDate, endDate);
        String studentSupplement = "";
        if (personalPraiseRecordList != null && personalPraiseRecordList.size() > 0) {
            for (M_SingleMoralRecord singleMoralRecord : personalPraiseRecordList) {
                studentSupplement += singleMoralRecord.getStudentName() + "(" + singleMoralRecord.getSupplement() + ")";
                //如果不是最后一个，就加、分隔
                if (singleMoralRecord != personalPraiseRecordList.get(personalPraiseRecordList.size() - 1)) {
                    studentSupplement += "、";
                }
            }
        }
        String resp = "";
        if (countNum != null && countNum > 0) {
            if (label.equals("personalCriticize")) {
                if (countNum >= gradeAvgCountNum) {
                    resp += "学生扣分" + countNum + "次，数量在年级前50%";
                } else {
                    resp += "学生扣分" + countNum + "次，数量在年级后50%";
                }

                if (studentSupplement != null && !studentSupplement.equals("")) {
                    resp += " ，扣分原因：" + studentSupplement + "\n";
                }


            } else if (label.equals("personalPraise")) {

                if (countNum >= gradeAvgCountNum) {
                    resp += "学生加分" + countNum + "次，数量在年级前50%";
                } else {
                    resp += "学生加分" + countNum + "次，数量在年级后50%";
                }

                if (studentSupplement != null && !studentSupplement.equals("")) {
                    resp += " ，加分原因：" + studentSupplement + "\n";
                }
            }
        }
        return resp;
    }

    private String getReadingNotificationBySchoolClassName(String school, String className, LocalDate startDate, LocalDate endDate) {
        //如果startDate和endDate是同一天，那么返回当天的早读情况，如果不是同一天，那么返回一周的早读情况
        if (startDate.equals(endDate)) {
            //查询当天的早读情况
            String timeZone = "课前";
            Integer preNum = readingFeedbackMapper.getIdBySchoolClassNameTimeZone(school, className, timeZone, startDate);
            timeZone = "早读";
            Integer readNum = readingFeedbackMapper.getIdBySchoolClassNameTimeZone(school, className, timeZone, startDate);
            timeZone = "自主";
            Integer autoNum = readingFeedbackMapper.getIdBySchoolClassNameTimeZone(school, className, timeZone, startDate);
            Integer count = 0;
            if (preNum == null && readNum == null && autoNum == null) {
                return "";
            }
            String resp = "【教学情况】\n";
            if (autoNum != null) {
                count += 1;
                resp += count + ".自主管理积分+1\n";
            }

            if (readNum != null) {
                count += 1;
                timeZone = "早读";
                String teacherName = singleReadTeacherRecordMapper.getTeacherNameBySchoolClassNameTimeZone(school, className, timeZone, startDate);
                if (teacherName == null || teacherName.equals("")) {
                    resp += count + ".早读表现积分+1\n";
                } else {
                    resp += count + ".早读表现积分+1 执教老师：" + teacherName + "\n";
                }
            }

            if (preNum != null) {
                count += 1;
                timeZone = "课前";
                String teacherName = singleReadTeacherRecordMapper.getTeacherNameBySchoolClassNameTimeZone(school, className, timeZone, startDate);
                if (teacherName == null || teacherName.equals("")) {
                    resp += count + ".课堂表现积分+1\n";
                } else {
                    resp += count + ".课堂表现积分+1 执教老师：" + teacherName + "\n";
                }
            }

            resp += "\n";

            return resp;
        } else {
            //查询一周的早读情况
            String timeZone = "课前";
            Integer countPreNum = readingFeedbackMapper.getCountNumbyClasNameDateTimeZone(startDate, endDate, school, className, timeZone);
            List<String> preTeacherNameList = singleReadTeacherRecordMapper.getTeacherNameListBySchoolClassNameTimeZone(school, className, timeZone, startDate, endDate);
            //获取年级平均数量
            String grade = className.substring(0, 1) + "年级";
            Integer gradeAllPreNum = readingFeedbackMapper.getGradeAvgNumbyDateTimeZone(startDate, endDate, school, grade, timeZone);
            //获得年级的班级数量
            Integer gradeClassNum = gradeClassNumMapper.getClassNumBySchoolGrade(school, grade);
            if (gradeClassNum == null || gradeClassNum == 0) {
                gradeClassNum = 1;
            }
            Integer gradeAvgPreNum = gradeAllPreNum / gradeClassNum;

            timeZone = "早读";
            Integer countReadNum = readingFeedbackMapper.getCountNumbyClasNameDateTimeZone(startDate, endDate, school, className, timeZone);
            List<String> readTeacherNameList = singleReadTeacherRecordMapper.getTeacherNameListBySchoolClassNameTimeZone(school, className, timeZone, startDate, endDate);
            Integer gradeAllReadNum = readingFeedbackMapper.getGradeAvgNumbyDateTimeZone(startDate, endDate, school, grade, timeZone);
            Integer gradeAvgReadNum = gradeAllReadNum / gradeClassNum;

            timeZone = "自主";
            Integer countAutoNum = readingFeedbackMapper.getCountNumbyClasNameDateTimeZone(startDate, endDate, school, className, timeZone);
            List<String> autoTeacherNameList = singleReadTeacherRecordMapper.getTeacherNameListBySchoolClassNameTimeZone(school, className, timeZone, startDate, endDate);
            Integer gradeAllAutoNum = readingFeedbackMapper.getGradeAvgNumbyDateTimeZone(startDate, endDate, school, grade, timeZone);
            Integer gradeAvgAutoNum = gradeAllAutoNum / gradeClassNum;

            if (countPreNum == 0 && countReadNum == 0 && countAutoNum == 0) {
                return "";
            }
            String resp = "【教学情况】\n";
            Integer count = 0;
            if (countAutoNum > 0) {
                count += 1;

                if (countAutoNum > gradeAvgAutoNum) {
                    resp += count + ".自主管理积分+" + countAutoNum + "，数量在年级前50%\n";
                } else {
                    resp += count + ".自主管理积分+" + countAutoNum + ",数量在年级后50%\n";
                }


            }

            if (countReadNum > 0) {
                count += 1;
                if (readTeacherNameList != null && readTeacherNameList.size() > 0) {
                    if (countReadNum > gradeAvgReadNum) {
                        if (readTeacherNameList != null && readTeacherNameList.size() > 0) {
                            resp += count + ".早读表现积分+" + countReadNum + "，数量在年级前50%，执教老师：" + String.join("、", readTeacherNameList) + "\n";
                        } else {
                            resp += count + ".早读表现积分+" + countReadNum + "，数量在年级前50%\n";
                        }
                    } else {
                        if (readTeacherNameList != null && readTeacherNameList.size() > 0) {
                            resp += count + ".早读表现积分+" + countReadNum + ",数量在年级后50%，执教老师：" + String.join("、", readTeacherNameList) + "\n";
                        } else {
                            resp += count + ".早读表现积分+" + countReadNum + ",数量在年级后50%\n";
                        }

                    }

                }
            }

            if (countPreNum > 0) {
                count += 1;
                if (preTeacherNameList != null && preTeacherNameList.size() > 0) {
                    if (countPreNum > gradeAvgPreNum) {
                        if (preTeacherNameList != null && preTeacherNameList.size() > 0) {
                            resp += count + ".课堂表现积分+" + countPreNum + "，数量在年级前50%，执教老师：" + String.join("、", preTeacherNameList) + "\n";
                        } else {
                            resp += count + ".课堂表现积分+" + countPreNum + "，数量在年级前50%\n";
                        }
                    } else {
                        if (preTeacherNameList != null && preTeacherNameList.size() > 0) {
                            resp += count + ".课堂表现积分+" + countPreNum + ",数量在年级后50%，执教老师：" + String.join("、", preTeacherNameList) + "\n";
                        } else {
                            resp += count + ".课堂表现积分+" + countPreNum + ",数量在年级后50%\n";
                        }

                    }

                }

            }

            resp += "\n";
            return resp;


        }


    }
}
