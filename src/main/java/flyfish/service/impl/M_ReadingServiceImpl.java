package flyfish.service.impl;

import flyfish.mapper.*;
import flyfish.pojo.*;
import flyfish.pojo.DTO.*;
import flyfish.pojo.VO.*;
import flyfish.service.M_ReadingService;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class M_ReadingServiceImpl implements M_ReadingService {

    @Autowired
    private M_MoringReadingCheckNameMapper m_moringReadingCheckNameMapper;
    @Autowired
    private M_GradeClassNumMapper m_gradeClassNumMapper;
    @Autowired
    private M_ReadingFeedbackMapper m_readingFeedbackMapper;
    @Autowired
    private M_TeacherListMapper m_TeacherListMapper;
    @Autowired
    private M_ClassTeacherRelationMapper m_ClassTeacherRelationMapper;
    @Autowired
    private M_SingleReadTeacherRecordMapper m_singleReadTeacherRecordMapper;
    @Autowired
    private M_ReadingFeedbackReportMapper m_readingFeedbackReportMapper;
    @Autowired
    private M_SportRecordMapper m_sportRecordMapper;
    @Autowired
    private M_PoorPerformerMapper m_poorPerformerMapper;
    @Autowired
    private M_TeacherRoleMapper m_teacherRoleMapper;
    @Autowired
    private M_UserMapper m_UserMapper;

    //根据学校和周几获取早读巡查人员
    @Override
    public List<M_MoringReadingCheckVO> getReadingCheckList(String weekday, String school) {
        List<M_MoringReadingCheckVO> CheckNameList=  m_moringReadingCheckNameMapper.getReadingCheckList(weekday,school);
        return CheckNameList;
    }

    //根据学校获取班级名称
    @Override
    public M_ClassNameListVO getClassNameList(String school) {
        String[] NUMBERS = {"一", "二", "三", "四", "五", "六", "七", "八", "九"};


        List<M_GradeClass> gradeClassList = m_gradeClassNumMapper.getGradeClassList(school);
        List<String> originGradeList = new ArrayList<>();

        List<String> classNameList = new ArrayList<>();
        //先把所有的数据都放到两个list里面
        for (M_GradeClass mGradeClass : gradeClassList) {
            originGradeList.add(mGradeClass.getGrade());
            classNameList.add(mGradeClass.getClassName());
        }
        //去重

        // 去重
        Set<String> set = new LinkedHashSet<>(originGradeList);
        // 转回列表
        List<String> uniqueGradeList = new ArrayList<>(set);
        // 排序
        uniqueGradeList.sort((s1, s2) -> {
            String firstChar1 = s1.isEmpty() ? "" : s1.substring(0, 1);
            String firstChar2 = s2.isEmpty() ? "" : s2.substring(0, 1);

            int index1 = Arrays.asList(NUMBERS).indexOf(firstChar1);
            int index2 = Arrays.asList(NUMBERS).indexOf(firstChar2);

            if (index1 != -1 && index2 != -1) {
                // 两个元素的首字符都在 NUMBERS 中，按 NUMBERS 顺序排序
                return Integer.compare(index1, index2);
            } else if (index1 != -1) {
                // 只有 s1 的首字符在 NUMBERS 中，s1 排在前面
                return -1;
            } else if (index2 != -1) {
                // 只有 s2 的首字符在 NUMBERS 中，s2 排在前面
                return 1;
            } else {
                // 两个元素的首字符都不在 NUMBERS 中，按字典序排序
                return firstChar1.compareTo(firstChar2);
            }
        });
        M_ClassNameListVO m_classNameListVO = new M_ClassNameListVO();
        m_classNameListVO.setClassNameList(classNameList);
        m_classNameListVO.setGradeList(uniqueGradeList);

        return m_classNameListVO;
    }

    //将登记早读情况记录反馈给后台处理
    @Override
    public String recordReading(M_ReadGradeFeedDTO mReadGradeFeedDTO) {
        String resp = "";
        //预处理传过来的参数，判断每个年级是不是有值，有值的批量添加到数据库
        List<M_FeedBack> feedBackList = new ArrayList<>();
        if(mReadGradeFeedDTO.getGrade1ClassList() !=null && mReadGradeFeedDTO.getGrade1ClassList().size() > 0 && mReadGradeFeedDTO.getGrade().equals("一年级")){
            //先查询数据库有没有这个年级的数据，没有的话新增，有的话还要看有没有这个班级的数据，没有的话新增，有的话更新
            List<String> grade1ClassList = mReadGradeFeedDTO.getGrade1ClassList();
            //先把mReadGradeFeedDTO的数据复制到mFeedBack里面
            M_FeedBack mFeedBack = new M_FeedBack();
            mFeedBack.setGrade("一年级");
            BeanUtils.copyProperties(mReadGradeFeedDTO, mFeedBack);
            List<String> recordClasNameList = m_readingFeedbackMapper.getClassNameList(mFeedBack);
            Set<String> set1 = new HashSet<>(recordClasNameList);
            Set<String> set2 = new HashSet<>(grade1ClassList);
            boolean areEqual = set1.equals(set2);
            if(areEqual && recordClasNameList.size() == grade1ClassList.size()){
                //如果两个set相等，说明两个集合的元素一样，不需要更新
                resp += "1年级noRevise";}
            else {
            //如果不相等，就要先删除再新增
            m_readingFeedbackMapper.deleteFeedback(mFeedBack);
            //把set2的数据转变为列表
            List<String>  grade1ClassListNew  = new ArrayList<>(set2);
            //新增数据
            for (String className : grade1ClassListNew) {
                M_FeedBack mFeedBackUpdate = new M_FeedBack();
                mFeedBackUpdate.setGrade("一年级");
                mFeedBackUpdate.setTimeZone(mReadGradeFeedDTO.getTimeZone());
                mFeedBackUpdate.setCheckDate(mReadGradeFeedDTO.getCheckDate());
                mFeedBackUpdate.setSchool(mReadGradeFeedDTO.getSchool());
                mFeedBackUpdate.setClassName(className);
                mFeedBackUpdate.setCreateTime(LocalDateTime.now());
                feedBackList.add(mFeedBackUpdate);
            }
            m_readingFeedbackMapper.addClassNameList(feedBackList);
            feedBackList.clear();
            resp += "grade1success";}
        }

        if (mReadGradeFeedDTO.getGrade2ClassList() != null && mReadGradeFeedDTO.getGrade2ClassList().size() > 0 && mReadGradeFeedDTO.getGrade().equals("二年级")) {
            List<String> grade2ClassList = mReadGradeFeedDTO.getGrade2ClassList();
            M_FeedBack mFeedBack = new M_FeedBack();
            mFeedBack.setGrade("二年级");
            BeanUtils.copyProperties(mReadGradeFeedDTO, mFeedBack);
            List<String> recordClasNameList = m_readingFeedbackMapper.getClassNameList(mFeedBack);
            Set<String> set1 = new HashSet<>(recordClasNameList);
            Set<String> set2 = new HashSet<>(grade2ClassList);
            boolean areEqual = set1.equals(set2);
            if(areEqual && recordClasNameList.size() == grade2ClassList.size()){
                resp += "2年级noRevise";}
            else {
            m_readingFeedbackMapper.deleteFeedback(mFeedBack);
            List<String>  grade2ClassListNew  = new ArrayList<>(set2);
            for (String className : grade2ClassListNew) {
                M_FeedBack mFeedBackUpdate = new M_FeedBack();
                mFeedBackUpdate.setGrade("二年级");
                mFeedBackUpdate.setTimeZone(mReadGradeFeedDTO.getTimeZone());
                mFeedBackUpdate.setCheckDate(mReadGradeFeedDTO.getCheckDate());
                mFeedBackUpdate.setSchool(mReadGradeFeedDTO.getSchool());
                mFeedBackUpdate.setClassName(className);
                mFeedBackUpdate.setCreateTime(LocalDateTime.now());
                feedBackList.add(mFeedBackUpdate);
            }
            m_readingFeedbackMapper.addClassNameList(feedBackList);
            feedBackList.clear();
            resp += "grade2success";}
        }

        if (mReadGradeFeedDTO.getGrade3ClassList() != null && mReadGradeFeedDTO.getGrade3ClassList().size() > 0 && mReadGradeFeedDTO.getGrade().equals("三年级")) {
            List<String> grade3ClassList = mReadGradeFeedDTO.getGrade3ClassList();
            M_FeedBack mFeedBack = new M_FeedBack();
            mFeedBack.setGrade("三年级");
            BeanUtils.copyProperties(mReadGradeFeedDTO, mFeedBack);
            List<String> recordClasNameList = m_readingFeedbackMapper.getClassNameList(mFeedBack);
            Set<String> set1 = new HashSet<>(recordClasNameList);
            Set<String> set2 = new HashSet<>(grade3ClassList);
            boolean areEqual = set1.equals(set2);
            if(areEqual && recordClasNameList.size() == grade3ClassList.size()){
                resp += "3年级noRevise";}
            else {
            m_readingFeedbackMapper.deleteFeedback(mFeedBack);
            List<String>  grade3ClassListNew  = new ArrayList<>(set2);
            for (String className : grade3ClassListNew) {
                M_FeedBack mFeedBackUpdate = new M_FeedBack();
                mFeedBackUpdate.setGrade("三年级");
                mFeedBackUpdate.setTimeZone(mReadGradeFeedDTO.getTimeZone());
                mFeedBackUpdate.setCheckDate(mReadGradeFeedDTO.getCheckDate());
                mFeedBackUpdate.setSchool(mReadGradeFeedDTO.getSchool());
                mFeedBackUpdate.setClassName(className);
                mFeedBackUpdate.setCreateTime(LocalDateTime.now());
                feedBackList.add(mFeedBackUpdate);
            }
            m_readingFeedbackMapper.addClassNameList(feedBackList);
            feedBackList.clear();
            resp += "grade3success";}
        }

        if (mReadGradeFeedDTO.getGrade4ClassList() != null && mReadGradeFeedDTO.getGrade4ClassList().size() > 0 && mReadGradeFeedDTO.getGrade().equals("四年级")) {
            List<String> grade4ClassList = mReadGradeFeedDTO.getGrade4ClassList();
            M_FeedBack mFeedBack = new M_FeedBack();
            mFeedBack.setGrade("四年级");
            BeanUtils.copyProperties(mReadGradeFeedDTO, mFeedBack);
            List<String> recordClasNameList = m_readingFeedbackMapper.getClassNameList(mFeedBack);
            Set<String> set1 = new HashSet<>(recordClasNameList);
            Set<String> set2 = new HashSet<>(grade4ClassList);
            boolean areEqual = set1.equals(set2);
            if(areEqual && recordClasNameList.size() == grade4ClassList.size()){
                resp += "4年级noRevise";}
            else {
            m_readingFeedbackMapper.deleteFeedback(mFeedBack);
            List<String>  grade4ClassListNew  = new ArrayList<>(set2);
            for (String className : grade4ClassListNew) {
                M_FeedBack mFeedBackUpdate = new M_FeedBack();
                mFeedBackUpdate.setGrade("四年级");
                mFeedBackUpdate.setTimeZone(mReadGradeFeedDTO.getTimeZone());
                mFeedBackUpdate.setCheckDate(mReadGradeFeedDTO.getCheckDate());
                mFeedBackUpdate.setSchool(mReadGradeFeedDTO.getSchool());
                mFeedBackUpdate.setClassName(className);
                mFeedBackUpdate.setCreateTime(LocalDateTime.now());
                feedBackList.add(mFeedBackUpdate);
            }
            m_readingFeedbackMapper.addClassNameList(feedBackList);
            feedBackList.clear();
            resp += "grade4success";}
        }

        if (mReadGradeFeedDTO.getGrade5ClassList() != null && mReadGradeFeedDTO.getGrade5ClassList().size() > 0 && mReadGradeFeedDTO.getGrade().equals("五年级")) {
            List<String> grade5ClassList = mReadGradeFeedDTO.getGrade5ClassList();
            M_FeedBack mFeedBack = new M_FeedBack();
            mFeedBack.setGrade("五年级");
            BeanUtils.copyProperties(mReadGradeFeedDTO, mFeedBack);
            List<String> recordClasNameList = m_readingFeedbackMapper.getClassNameList(mFeedBack);
            Set<String> set1 = new HashSet<>(recordClasNameList);
            Set<String> set2 = new HashSet<>(grade5ClassList);
            boolean areEqual = set1.equals(set2);
            if(areEqual && recordClasNameList.size() == grade5ClassList.size()){
                resp += "5年级noRevise";}
            else {
            m_readingFeedbackMapper.deleteFeedback(mFeedBack);
            List<String>  grade5ClassListNew  = new ArrayList<>(set2);
            for (String className : grade5ClassListNew) {
                M_FeedBack mFeedBackUpdate = new M_FeedBack();
                mFeedBackUpdate.setGrade("五年级");
                mFeedBackUpdate.setTimeZone(mReadGradeFeedDTO.getTimeZone());
                mFeedBackUpdate.setCheckDate(mReadGradeFeedDTO.getCheckDate());
                mFeedBackUpdate.setSchool(mReadGradeFeedDTO.getSchool());
                mFeedBackUpdate.setClassName(className);
                mFeedBackUpdate.setCreateTime(LocalDateTime.now());
                feedBackList.add(mFeedBackUpdate);
            }
            m_readingFeedbackMapper.addClassNameList(feedBackList);
            feedBackList.clear();
            resp += "grade5success";}
        }

        if (mReadGradeFeedDTO.getGrade6ClassList() != null && mReadGradeFeedDTO.getGrade6ClassList().size() > 0 && mReadGradeFeedDTO.getGrade().equals("六年级")) {
            List<String> grade6ClassList = mReadGradeFeedDTO.getGrade6ClassList();
            M_FeedBack mFeedBack = new M_FeedBack();
            mFeedBack.setGrade("六年级");
            BeanUtils.copyProperties(mReadGradeFeedDTO, mFeedBack);
            List<String> recordClasNameList = m_readingFeedbackMapper.getClassNameList(mFeedBack);
            Set<String> set1 = new HashSet<>(recordClasNameList);
            Set<String> set2 = new HashSet<>(grade6ClassList);
            boolean areEqual = set1.equals(set2);
            if(areEqual && recordClasNameList.size() == grade6ClassList.size()){
                resp += "6年级noRevise";}
            else {
            m_readingFeedbackMapper.deleteFeedback(mFeedBack);
            List<String>  grade6ClassListNew  = new ArrayList<>(set2);
            for (String className : grade6ClassListNew) {
                M_FeedBack mFeedBackUpdate = new M_FeedBack();
                mFeedBackUpdate.setGrade("六年级");
                mFeedBackUpdate.setTimeZone(mReadGradeFeedDTO.getTimeZone());
                mFeedBackUpdate.setCheckDate(mReadGradeFeedDTO.getCheckDate());
                mFeedBackUpdate.setSchool(mReadGradeFeedDTO.getSchool());
                mFeedBackUpdate.setClassName(className);
                mFeedBackUpdate.setCreateTime(LocalDateTime.now());
                feedBackList.add(mFeedBackUpdate);
            }
            m_readingFeedbackMapper.addClassNameList(feedBackList);
            feedBackList.clear();
            resp += "grade6success";}
        }

        if(mReadGradeFeedDTO.getGrade7ClassList() != null && mReadGradeFeedDTO.getGrade7ClassList().size() > 0 && mReadGradeFeedDTO.getGrade().equals("七年级")){
            List<String> grade7ClassList = mReadGradeFeedDTO.getGrade7ClassList();
            M_FeedBack mFeedBack = new M_FeedBack();
            mFeedBack.setGrade("七年级");
            BeanUtils.copyProperties(mReadGradeFeedDTO, mFeedBack);
            List<String> recordClasNameList = m_readingFeedbackMapper.getClassNameList(mFeedBack);
            Set<String> set1 = new HashSet<>(recordClasNameList);
            Set<String> set2 = new HashSet<>(grade7ClassList);
            boolean areEqual = set1.equals(set2);
            if(areEqual && recordClasNameList.size() == grade7ClassList.size()){
                resp += "7年级noRevise";}
            else {
            m_readingFeedbackMapper.deleteFeedback(mFeedBack);
            List<String>  grade7ClassListNew  = new ArrayList<>(set2);
            for (String className : grade7ClassListNew) {
                M_FeedBack mFeedBackUpdate = new M_FeedBack();
                mFeedBackUpdate.setGrade("七年级");
                mFeedBackUpdate.setTimeZone(mReadGradeFeedDTO.getTimeZone());
                mFeedBackUpdate.setCheckDate(mReadGradeFeedDTO.getCheckDate());
                mFeedBackUpdate.setSchool(mReadGradeFeedDTO.getSchool());
                mFeedBackUpdate.setClassName(className);
                mFeedBackUpdate.setCreateTime(LocalDateTime.now());
                feedBackList.add(mFeedBackUpdate);
            }
            m_readingFeedbackMapper.addClassNameList(feedBackList);
            feedBackList.clear();
            resp += "grade7success";}
        }

        if(mReadGradeFeedDTO.getGrade8ClassList() != null && mReadGradeFeedDTO.getGrade8ClassList().size() > 0 && mReadGradeFeedDTO.getGrade().equals("八年级")){
            List<String> grade8ClassList = mReadGradeFeedDTO.getGrade8ClassList();
            M_FeedBack mFeedBack = new M_FeedBack();
            mFeedBack.setGrade("八年级");
            BeanUtils.copyProperties(mReadGradeFeedDTO, mFeedBack);
            List<String> recordClasNameList = m_readingFeedbackMapper.getClassNameList(mFeedBack);
            Set<String> set1 = new HashSet<>(recordClasNameList);
            Set<String> set2 = new HashSet<>(grade8ClassList);
            boolean areEqual = set1.equals(set2);
            if(areEqual && recordClasNameList.size() == grade8ClassList.size()){
                resp += "8年级noRevise";}
            else {
            m_readingFeedbackMapper.deleteFeedback(mFeedBack);
            List<String>  grade8ClassListNew  = new ArrayList<>(set2);
            for (String className : grade8ClassListNew) {
                M_FeedBack mFeedBackUpdate = new M_FeedBack();
                mFeedBackUpdate.setGrade("八年级");
                mFeedBackUpdate.setTimeZone(mReadGradeFeedDTO.getTimeZone());
                mFeedBackUpdate.setCheckDate(mReadGradeFeedDTO.getCheckDate());
                mFeedBackUpdate.setSchool(mReadGradeFeedDTO.getSchool());
                mFeedBackUpdate.setClassName(className);
                mFeedBackUpdate.setCreateTime(LocalDateTime.now());
                feedBackList.add(mFeedBackUpdate);
            }
            m_readingFeedbackMapper.addClassNameList(feedBackList);
            feedBackList.clear();
            resp += "grade8success";}
        }

        if(mReadGradeFeedDTO.getGrade9ClassList() != null && mReadGradeFeedDTO.getGrade9ClassList().size() > 0 && mReadGradeFeedDTO.getGrade().equals("九年级")){
            List<String> grade9ClassList = mReadGradeFeedDTO.getGrade9ClassList();
            M_FeedBack mFeedBack = new M_FeedBack();
            mFeedBack.setGrade("九年级");
            BeanUtils.copyProperties(mReadGradeFeedDTO, mFeedBack);
            List<String> recordClasNameList = m_readingFeedbackMapper.getClassNameList(mFeedBack);
            Set<String> set1 = new HashSet<>(recordClasNameList);
            Set<String> set2 = new HashSet<>(grade9ClassList);
            boolean areEqual = set1.equals(set2);
            if(areEqual && recordClasNameList.size() == grade9ClassList.size()){
                resp += "9年级noRevise";}
            else {
            m_readingFeedbackMapper.deleteFeedback(mFeedBack);
            List<String>  grade9ClassListNew  = new ArrayList<>(set2);
            for (String className : grade9ClassListNew) {
                M_FeedBack mFeedBackUpdate = new M_FeedBack();
                mFeedBackUpdate.setGrade("九年级");
                mFeedBackUpdate.setTimeZone(mReadGradeFeedDTO.getTimeZone());
                mFeedBackUpdate.setCheckDate(mReadGradeFeedDTO.getCheckDate());
                mFeedBackUpdate.setSchool(mReadGradeFeedDTO.getSchool());
                mFeedBackUpdate.setClassName(className);
                mFeedBackUpdate.setCreateTime(LocalDateTime.now());
                feedBackList.add(mFeedBackUpdate);
            }
            m_readingFeedbackMapper.addClassNameList(feedBackList);
            feedBackList.clear();
            resp += "grade9success";}
        }


        if(resp.equals("")){
            return "nullData";
        }
        return resp;
    }

    //将前端传递的教师excel表存入数据库
    @Override
    public String updateTeacherData(MultipartFile file, String school) throws IOException {
        // 文件校验
        if (file.isEmpty()) {
            log.error("上传的文件为空");
            return "文件不能为空";
        }

        String fileName = file.getOriginalFilename();
        if (fileName == null || !fileName.endsWith(".xlsx")) {
            log.error("上传的文件不是 Excel 文件");
            return "请上传有效的 Excel 文件";
        }

        // 解析 Excel 文件并存入数据库
        try (InputStream inputStream = file.getInputStream()) {
            Workbook workbook = new XSSFWorkbook(inputStream);
            Sheet sheet = workbook.getSheetAt(0);

            List<M_TeacherData> teacherDataList = new ArrayList<>();

            for (Row row : sheet) {
                if (row.getRowNum() == 0) {
                    if(row.getCell(0).toString().equals("姓名") && row.getCell(1).toString().equals("学科")){
                        continue;
                    }else{
                        return "请上传教师列表模板";
                    }

                }

                String teacherName = getCellValueAsString(row.getCell(0)); // 教师姓名
                String subject = getCellValueAsString(row.getCell(1));     // 任教学科

                M_TeacherData teacherData = new M_TeacherData();
                teacherData.setSchool(school);
                teacherData.setTeacherName(teacherName);
                teacherData.setSubject(subject);
                teacherDataList.add(teacherData);
            }

            //先删除附小所有的教师数据
            m_TeacherListMapper.deleteTeacherList(school);

            // 将数据存入数据库
            m_TeacherListMapper.uploadTeacherList(teacherDataList);

            return "success";

        } catch (IOException e) {
            log.error("文件读取失败", e);
            return "文件读取失败，请检查文件格式";
        } catch (Exception e) {
            log.error("系统错误", e);
            return "系统错误，请稍后重试";
        }

    }

    //将前端传递的班级绑定教师excel表存入数据库
    //先判断，第一列是不是年级，之后有10列，最多绑定10个教师
    //如果有没有查到的教师，存到一个列表里，返回给前端，返回map，键值是未绑定班级，如果全部绑定了，就提醒为success
    @Override
    public String updateClassTeacherData(MultipartFile file, String school) {
        log.info("开始处理班级绑定教师文件");

        // 文件校验
        if (file.isEmpty()) {
            log.error("上传的文件为空");
            return "文件不能为空";
        }

        String fileName = file.getOriginalFilename();
        if (fileName == null || !fileName.endsWith(".xlsx")) {
            log.error("上传的文件不是 Excel 文件");
            return "请上传有效的 Excel 文件";
        }

        // 解析 Excel 文件并生成对象列表
        List<M_ClassTeacherRelation> relationList = new ArrayList<>();
        List<String> notFoundTeacherList = new ArrayList<>();
        List<String> notFoundClassList = new ArrayList<>();
        List<M_ClassTeacherRelation> headTeacherRelationList = new ArrayList<>();

        try (InputStream inputStream = file.getInputStream();
             Workbook workbook = new XSSFWorkbook(inputStream)) {
            if (workbook.getNumberOfSheets() == 0) {
                return "请上传班级教师绑定模板";
            }

            Sheet sheet = workbook.getSheetAt(0);
            DataFormatter formatter = new DataFormatter();

            for (Row row : sheet) {
                if (row.getRowNum() == 0) {
                    String firstHeader = formatter.formatCellValue(row.getCell(0)).strip();
                    String secondHeader = formatter.formatCellValue(row.getCell(1)).strip();
                    log.info("班级教师绑定模板表头：firstHeader={}, secondHeader={}", firstHeader, secondHeader);

                    if ("年级:例如一年级".equals(firstHeader) && "班级:例如8".equals(secondHeader)) {
                        continue;
                    }

                    return "请上传班级教师绑定模板";
                }

                // 获取年级名（第一列）
                String grade = getCellValueAsString(row.getCell(0));
                if (grade == null || grade.isEmpty()) {
                    continue; // 如果年级名为空，跳过该行
                }
                grade = String.valueOf(grade.charAt(0)); // 只取年级名的第一个字符
                //如果第一个字符，不在 一二三四五六七八九这几个年级中，continue
                String characters = "一二三四五六七八九";
                if (characters.contains(grade)==false){
                    continue;
                }
                String classNum = getCellValueAsString(row.getCell(1));
                if (classNum == null || classNum.isEmpty()) {
                    continue; // 如果班级名为空，跳过该行
                }

                List<String> NumLists = Arrays.asList("1", "2", "3", "4", "5", "6", "7", "8", "9", "10",
                        "11", "12", "13", "14", "15", "16", "17", "18", "19", "20");
                if(!NumLists.contains(classNum)){
                    continue;
                }
                String className = grade +"("+ classNum+")"+ "班";
                grade = grade+"年级";

                String headTeacherName = getCellValueAsString(row.getCell(2));
                if(headTeacherName != null && !headTeacherName.isEmpty()) {
                    M_ClassTeacherRelation headTeacherRelation = setRelationData(school, className, headTeacherName,grade, notFoundTeacherList, notFoundClassList);
                    if(headTeacherRelation != null){
                        headTeacherRelationList.add(headTeacherRelation);
                    }
                }


                // 遍历教师列（第三列到第十二列）
                for (int i = 3; i <= 12; i++) {
                    String teacherName = getCellValueAsString(row.getCell(i));
                    if (teacherName == null || teacherName.isEmpty()) {
                        continue; // 如果教师姓名为空，跳过该列
                    }


                    M_ClassTeacherRelation teacherRelation = setRelationData(school, className, teacherName,grade, notFoundTeacherList, notFoundClassList);
                    if(teacherRelation != null){
                        relationList.add(teacherRelation);
                    }

                }
            }

            //再吧relationList筛选一遍，把classId，teacherId两组都重复的删掉
            Set<M_ClassTeacherRelation> set = new HashSet<>(relationList);
            relationList.clear();
            relationList.addAll(set);


            // 先删除所有的班级-教师关系
            m_ClassTeacherRelationMapper.deleteRelations(relationList);


            // 将数据存入数据库
            m_ClassTeacherRelationMapper.saveRelations(relationList);


            String resp = "未添加班主任数据;";
            //将班主任数据更新
            if(headTeacherRelationList.size() > 0){
                //如果有班主任数据，就更新
            resp = updateHeadTeacher(headTeacherRelationList);}


            return resp+ "未新增的班级："+String.join(",",notFoundClassList)+"未新增的教师："+String.join(",",notFoundTeacherList);
        } catch (IOException e) {
            log.error("文件读取失败", e);
            return "文件读取失败，请检查文件格式";
        } catch (Exception e) {
            log.error("系统错误", e);
            return "系统错误，请稍后重试";
        }

    }

    private String updateHeadTeacher(List<M_ClassTeacherRelation> headTeacherRelationList) {
        for (M_ClassTeacherRelation headTeacherRelation : headTeacherRelationList) {
            //先查询数据库有没有这个班级的班主任数据，如果有的话就更新，没有的话就新增
            Integer id = m_ClassTeacherRelationMapper.geIdByClassNameTeacherNameSchool(headTeacherRelation);
            headTeacherRelation.setHeadTeacher("是");
            if(id != null){
                headTeacherRelation.setId(id);

                m_ClassTeacherRelationMapper.updateClassTeacherRelation(headTeacherRelation);
            }else {
                m_ClassTeacherRelationMapper.insetClassTeacherRelation(headTeacherRelation);
            }
        }

        //拿到所有班主任teacherRole
        String title = "班主任";
        String resp = "";
        List<String> titleHeadTeacherNameList = m_teacherRoleMapper.getTeacherNameByHeadTeacherSchool(headTeacherRelationList.get(0).getSchool(),title);
        List<M_TeacherRole> teacherRoleList = new ArrayList<>();

        for(M_ClassTeacherRelation headTeacherRelation : headTeacherRelationList){
            //如果这个班主任不在teacherRole表里，就新增
            if(titleHeadTeacherNameList != null && titleHeadTeacherNameList.size() > 0){
                if(titleHeadTeacherNameList.contains(headTeacherRelation.getTeacherName())){
                    continue;
                }
            }

            M_TeacherRole m_TeacherRole = new M_TeacherRole();
            Integer userId = m_UserMapper.getIdBySchoolAndTeacherNameRole(headTeacherRelation.getSchool(),headTeacherRelation.getTeacherName());
            if(userId == null){
                //如果没有这个用户，就返回文本提醒
                resp += "教师"+headTeacherRelation.getTeacherName()+"没有账号，请先为他创建账号并分配教师角色;";
                continue;
            }
            m_TeacherRole.setUserId(userId);
            m_TeacherRole.setSchool(headTeacherRelation.getSchool());
            m_TeacherRole.setTeacherName(headTeacherRelation.getTeacherName());
            m_TeacherRole.setTitle(title);
            teacherRoleList.add(m_TeacherRole);


        }
        if(teacherRoleList.size() > 0){
            m_teacherRoleMapper.batchInsertTeacherRole(teacherRoleList);
        }
        return resp;

    }

    //设置班级教师关系对象
    private M_ClassTeacherRelation setRelationData(String school, String className, String teacherName,String grade, List<String> notFoundTeacherList, List<String> notFoundClassList){

        Integer teacherId = m_TeacherListMapper.getOldTeacherId(teacherName,school);
        Integer classId = m_gradeClassNumMapper.getClassId(school,className);
        if (teacherId == null) {
            notFoundTeacherList.add(teacherName);
            //如果查不到教师就新增教师表
            M_TeacherData teacherData = new M_TeacherData();
            teacherData.setSchool(school);
            teacherData.setTeacherName(teacherName);
            teacherData.setSubject("未知");
            m_TeacherListMapper.newTeacher(teacherData);
            teacherId = teacherData.getId();
        }

        if (classId == null) {
            notFoundClassList.add(className);
            // 如果查不到班级就新增班级表
            M_GradeClass gradeClass = new M_GradeClass();
            gradeClass.setSchool(school);
            gradeClass.setGrade(grade);
            gradeClass.setClassName(className);
            gradeClass.setCreateTime(LocalDateTime.now());
            m_gradeClassNumMapper.newGradeClassNum(gradeClass);
            classId = gradeClass.getId();

        }
        //都存在的时候才绑定
        // 生成 M_ClassTeacherRelation 对象并添加到列表
        M_ClassTeacherRelation relation = new M_ClassTeacherRelation();
        relation.setSchool(school);
        relation.setClassName(className);
        relation.setTeacherName(teacherName);
        relation.setTeacherId(teacherId);
        relation.setClassId(classId);
        return relation;
    }

    //从数据库中获取单个早读反馈
    @Override
    public M_ReadingThreeSituationDTO getSingleReadFeedback(String school, LocalDate checkDate) {
        M_ReadingThreeSituationDTO m_readingThreeSituationDTO = new M_ReadingThreeSituationDTO();
        List<String> manageClassNameList = new ArrayList<>();
        List<String> readingClassNameList = new ArrayList<>();
        List<String> prepareClassNameList = new ArrayList<>();
        List<M_FeedBack> feedBackList =  m_readingFeedbackMapper.getSingleReadFeedback(school,checkDate);
        for (M_FeedBack mFeedBack : feedBackList) {
            if(mFeedBack.getTimeZone().equals("自主")){
                manageClassNameList.add(mFeedBack.getClassName());
            }else if(mFeedBack.getTimeZone().equals("早读")){
                readingClassNameList.add(mFeedBack.getClassName());
            }else if(mFeedBack.getTimeZone().equals("课前")){
                prepareClassNameList.add(mFeedBack.getClassName());
            }
        }
        m_readingThreeSituationDTO.setReadingClassNameList(readingClassNameList);
        m_readingThreeSituationDTO.setManageClassNameList(manageClassNameList);
        m_readingThreeSituationDTO.setPrepareClassNameList(prepareClassNameList);

        return m_readingThreeSituationDTO;
    }

    @Override
    public M_TeacherListMap getTeacherData(String school) {
        List<M_ClassTeacherRelation> classTeacherRelationList = m_ClassTeacherRelationMapper.getTeacherData(school);
        Map<String, List<String>> teacherDataMap = new HashMap<>();
        Map<String,List<String>> prepareTeacherDataMap = new HashMap<>();
        List<String> teacherList =  m_TeacherListMapper.getTeacherNameByThreeSubjects(school);
        List<String> prepareTeacherList = m_TeacherListMapper.getTeacherNameByAllSubject(school);

        for (M_ClassTeacherRelation classTeacherRelation : classTeacherRelationList) {
            String className = classTeacherRelation.getClassName();
            String teacherName = classTeacherRelation.getTeacherName();
            if (teacherDataMap.containsKey(className)) {
                //只显示语数英三科教师的列表
                if(teacherList.contains(teacherName)){
                    teacherDataMap.get(className).add(teacherName);
                }
            } else {
                if(teacherList.contains(teacherName)){
                    List<String> teacherNameList = new ArrayList<>();
                    teacherNameList.add(teacherName);
                    teacherDataMap.put(className, teacherNameList);
                }

            }
            if(prepareTeacherDataMap.containsKey(className)){
                if(prepareTeacherList.contains(teacherName)){
                    prepareTeacherDataMap.get(className).add(teacherName);
                }
            }else {
                List<String> prepareTeacherNameList = new ArrayList<>();
                prepareTeacherNameList.add(teacherName);
                prepareTeacherDataMap.put(className,prepareTeacherNameList);
            }

        }


        M_TeacherListMap mTeacherListMap = new M_TeacherListMap();
        mTeacherListMap.setTeacherDataMap(teacherDataMap);
        mTeacherListMap.setPrepareTeacherDataMap(prepareTeacherDataMap);

        return mTeacherListMap;
    }

    @Override
    public String recordTeacherFeedback(M_ReadTeacherFeedDTO mReadTeacherFeedDTO) {
        String resp = "";
        List<String> readingTeacherNameList = mReadTeacherFeedDTO.getReadingTeacherList();
        //如果早读的老师不为空
        if(readingTeacherNameList != null && readingTeacherNameList.size()>0){
            Map<String, String> classTeacherMap = mReadTeacherFeedDTO.getClassTeacherMap();
            //存储筛选年级的班级教师map
            Map<String,String> classTeacherMapByGrade = new HashMap<>();
            List<String> readingTeacherNameListNew = new ArrayList<>();
            List<String> readingClassNameList = new ArrayList<>();
            for (Map.Entry<String, String> entry : classTeacherMap.entrySet()) {
                //把班级名称的第一个字取出来+年级两个字
                String grade = entry.getKey().substring(0,1)+"年级";
                if(grade.equals(mReadTeacherFeedDTO.getGrade())){
                    classTeacherMapByGrade.put(entry.getKey(),entry.getValue());
                    //前端传的早读的老师名字列表
                    readingTeacherNameListNew.add(entry.getValue());
                    //前端传的早读的老师对应的班级名字列表
                    readingClassNameList.add(entry.getKey());
                }

            }
            mReadTeacherFeedDTO.setTimeZone("早读");
            //拿到提交年级的所有教师反馈数据
            List<M_SingleReadTeacher> originsingleReadTeacherList = m_singleReadTeacherRecordMapper.getSingleReadTeacherList(mReadTeacherFeedDTO);
            Map<String,String> originClassTeacherMap = new HashMap<>();
            for (M_SingleReadTeacher originmSingleReadTeacher : originsingleReadTeacherList) {
                //获取已经记录的教师对应的班级名字
                originClassTeacherMap.put(originmSingleReadTeacher.getClassName(),originmSingleReadTeacher.getTeacherName());
            }
            if(originClassTeacherMap.equals(classTeacherMapByGrade)){
                resp += mReadTeacherFeedDTO.getGrade()+"readingTeacherNoRevise;";

            }else {

                List<String> subjectList = new ArrayList<>();
                //根据教师名字获取学科
                for (String s : readingTeacherNameListNew) {
                    String subject = m_TeacherListMapper.getSubjectByName(s, mReadTeacherFeedDTO.getSchool());
                    subjectList.add(subject);

                }

            //先根据学校、日期、年级和timezone删除数据
            m_singleReadTeacherRecordMapper.deleteTeacherFeedback(mReadTeacherFeedDTO);
            List<M_SingleReadTeacher> singleReadTeacherList = new ArrayList<>();
            //封装所有的要修改的数据
            for (int i = 0; i < readingTeacherNameListNew.size(); i++) {
                M_SingleReadTeacher mSingleReadTeacher = new M_SingleReadTeacher();
                mSingleReadTeacher.setSchool(mReadTeacherFeedDTO.getSchool());
                mSingleReadTeacher.setCheckDate(mReadTeacherFeedDTO.getCheckDate());
                mSingleReadTeacher.setTimeZone(mReadTeacherFeedDTO.getTimeZone());
                mSingleReadTeacher.setTeacherName(readingTeacherNameListNew.get(i));
                mSingleReadTeacher.setSubject(subjectList.get(i));
                mSingleReadTeacher.setCreateTime(LocalDateTime.now());
                mSingleReadTeacher.setGrade(mReadTeacherFeedDTO.getGrade());
                mSingleReadTeacher.setClassName(readingClassNameList.get(i));

                singleReadTeacherList.add(mSingleReadTeacher);
            }
            m_singleReadTeacherRecordMapper.addTeacherFeedback(singleReadTeacherList);
            resp +=  mReadTeacherFeedDTO.getGrade()+"readingTeacherSuccess;";}

        }

        //如果课前准备的老师不为空
        List<String> prepareTeacherNameList = mReadTeacherFeedDTO.getPrepareTeacherList();
        if (prepareTeacherNameList != null && prepareTeacherNameList.size() > 0) {
            Map<String,String> prepareClassTeacherMap = mReadTeacherFeedDTO.getPrepareClassTeacherMap();
            Map<String,String> prepareClassTeacherMapByGrade= new HashMap<>();
            List<String> prepareTeacherNameListNew = new ArrayList<>();
            List<String> prepareClassNameList = new ArrayList<>();
            for (Map.Entry<String, String> entry : prepareClassTeacherMap.entrySet()) {
                String grade = entry.getKey().substring(0,1)+"年级";
                if(grade.equals(mReadTeacherFeedDTO.getGrade())) {
                    prepareClassTeacherMapByGrade.put(entry.getKey(), entry.getValue());
                    prepareTeacherNameListNew.add(entry.getValue());
                    prepareClassNameList.add(entry.getKey());
                }
            }
            mReadTeacherFeedDTO.setTimeZone("课前");
            List<M_SingleReadTeacher> originsingleReadTeacherList = m_singleReadTeacherRecordMapper.getSingleReadTeacherList(mReadTeacherFeedDTO);
            Map<String,String> originClassTeacherMap = new HashMap<>();
            for (M_SingleReadTeacher originmSingleReadTeacher : originsingleReadTeacherList) {
                originClassTeacherMap.put(originmSingleReadTeacher.getClassName(),originmSingleReadTeacher.getTeacherName());
            }
            if(originClassTeacherMap.equals(prepareClassTeacherMapByGrade)){
                resp +=  mReadTeacherFeedDTO.getGrade()+"prepareTeacherNoRevise;";}
            else {
                List<String> subjectList = new ArrayList<>();
                for (String s : prepareTeacherNameListNew) {
                     String subject = m_TeacherListMapper.getSubjectByName(s, mReadTeacherFeedDTO.getSchool());
                     subjectList.add(subject);

                }
            //先根据学校、日期年级和timezone删除数据
            m_singleReadTeacherRecordMapper.deleteTeacherFeedback(mReadTeacherFeedDTO);
            List<M_SingleReadTeacher> singleReadTeacherList = new ArrayList<>();

            for (int i = 0; i < prepareTeacherNameListNew.size(); i++) {
                M_SingleReadTeacher mSingleReadTeacher = new M_SingleReadTeacher();
                mSingleReadTeacher.setSchool(mReadTeacherFeedDTO.getSchool());
                mSingleReadTeacher.setCheckDate(mReadTeacherFeedDTO.getCheckDate());
                mSingleReadTeacher.setTimeZone(mReadTeacherFeedDTO.getTimeZone());
                mSingleReadTeacher.setTeacherName(prepareTeacherNameListNew.get(i));
                mSingleReadTeacher.setSubject(subjectList.get(i));
                mSingleReadTeacher.setCreateTime(LocalDateTime.now());
                mSingleReadTeacher.setClassName(prepareClassNameList.get(i));
                mSingleReadTeacher.setGrade(mReadTeacherFeedDTO.getGrade());
                singleReadTeacherList.add(mSingleReadTeacher);
            }
            m_singleReadTeacherRecordMapper.addTeacherFeedback(singleReadTeacherList);
            resp +=  mReadTeacherFeedDTO.getGrade()+"prepareTeacherSuccess;";}
        }

        if(resp.equals("")){
            return "nullData;";
        }


        return resp;
    }

    @Override
    public M_SingleReadTeacherVO getSelectedTeacherStatus(String school, LocalDate checkDate) {
        String timeZone = "早读";
        List<M_SingleReadTeacher> singleReadTeacherList = m_singleReadTeacherRecordMapper.getSelectedTeacherStatus(school,checkDate,timeZone);
        timeZone = "课前";
        List<M_SingleReadTeacher> prepareSingleReadTeacherList = m_singleReadTeacherRecordMapper.getSelectedTeacherStatus(school,checkDate,timeZone);
        Map<String,String> readingClassTeacherMap = new HashMap<>();
        Map<String,String> prepareClassTeacherMap = new HashMap<>();
        if(singleReadTeacherList !=null && singleReadTeacherList.size()>0){

            for (M_SingleReadTeacher mSingleReadTeacher : singleReadTeacherList) {
                readingClassTeacherMap.put(mSingleReadTeacher.getClassName(),mSingleReadTeacher.getTeacherName());
            }

            }
        if(prepareSingleReadTeacherList !=null && prepareSingleReadTeacherList.size()>0){

            for (M_SingleReadTeacher mSingleReadTeacher : prepareSingleReadTeacherList) {
                prepareClassTeacherMap.put(mSingleReadTeacher.getClassName(),mSingleReadTeacher.getTeacherName());
            }
        }
        M_SingleReadTeacherVO m_singleReadTeacherVO = new M_SingleReadTeacherVO();
        m_singleReadTeacherVO.setReadingClassTeacherMap(readingClassTeacherMap);
        m_singleReadTeacherVO.setPrepareClassTeacherMap(prepareClassTeacherMap);

        return m_singleReadTeacherVO;
    }

    //新增班级和教师绑定关系
    @Override
    public String addClassTeacherRelation(M_ClassTeacherMapDTO mClassTeacherMapDTO) {
        List<M_ClassTeacherRelation> relationList = new ArrayList<>();
        String resp = "";
        List<String> teacherNameList = new ArrayList<>();
        List<String> classNameList = new ArrayList<>();
        List<String> notFoundTeacherList = new ArrayList<>();
        Map<String,String> classTeacherMap = mClassTeacherMapDTO.getNewPrepareClassTeacherMap();
        if(classTeacherMap == null || classTeacherMap.size() == 0){
            return "未更改数据";
        }
        for (Map.Entry<String, String> entry : classTeacherMap.entrySet()) {
            M_ClassTeacherRelation relation = new M_ClassTeacherRelation();
            relation.setSchool(mClassTeacherMapDTO.getSchool());
            relation.setClassName(entry.getKey());
            relation.setTeacherName(entry.getValue());
            relationList.add(relation);
            teacherNameList.add(entry.getValue());
            classNameList.add(entry.getKey());

        }

        //拿到所有教师对应的id
        List<Integer> teacherIdList = m_TeacherListMapper.getTeacherIdList(relationList);
        if(teacherIdList == null || teacherIdList.size() == 0){
            for(M_ClassTeacherRelation relation : relationList){
                notFoundTeacherList.add(relation.getTeacherName());
            }
            return "未找到教师:"+String.join(",",notFoundTeacherList)+";";
        }

        //比对数据库中的教师姓名和前端传递的教师姓名，如果有不一样的，就新增
        List<String> newTeacherNameList = m_TeacherListMapper.getTeacherNameById(teacherIdList,mClassTeacherMapDTO.getSchool());
        Map<String,Integer> TeacherNameIdMap = new HashMap<>();
        for(int i = 0; i<teacherIdList.size();i++){
            TeacherNameIdMap.put(newTeacherNameList.get(i),teacherIdList.get(i));
        }
        notFoundTeacherList = teacherNameList.stream().filter(item -> !newTeacherNameList.contains(item)).collect(Collectors.toList());
        if(notFoundTeacherList!=null && notFoundTeacherList.size()>0){
            resp += "未找到教师:"+String.join(",",notFoundTeacherList)+";";
        }
        if(newTeacherNameList == null || newTeacherNameList.size() == 0){
            return resp;
        }



        //拿到所有班级对应的id
        List<Integer> classIdList = m_gradeClassNumMapper.getClassIdList(relationList);
        List<String> classNameListNew = m_gradeClassNumMapper.getClassNameById(classIdList,mClassTeacherMapDTO.getSchool());
        relationList.clear();
        for(int i =0 ; i<newTeacherNameList.size();i++){
            M_ClassTeacherRelation relation = new M_ClassTeacherRelation();
            relation.setSchool(mClassTeacherMapDTO.getSchool());
            relation.setClassId(classIdList.get(i));
            relation.setClassName(classNameListNew.get(i));
            relation.setTeacherName(classTeacherMap.get(classNameListNew.get(i)));
            relation.setTeacherId(TeacherNameIdMap.get(classTeacherMap.get(classNameListNew.get(i))));
            relationList.add(relation);
        }
        //先删除所有的班级-教师关系
        m_ClassTeacherRelationMapper.deleteRelations(relationList);
        //新增班级教师关系数据
        m_ClassTeacherRelationMapper.saveRelations(relationList);
        if(resp.equals("")){
            return "NewClassTeacherSuccess;";
        }
        return resp;
    }

    //删除班级和教师绑定关系
    @Override
    public String deleteClassTeacherRelation(M_ClassTeacherMapDTO mClassTeacherMapDTO) {
        List<M_ClassTeacherRelation> relationList = new ArrayList<>();
        List<String> notFoundTeacherList = new ArrayList<>();
        String resp = "";
        Map<String,String> deleteClassTeacherMap = mClassTeacherMapDTO.getDeleteClassTeacherMap();
        if(deleteClassTeacherMap == null || deleteClassTeacherMap.size() == 0){
            return "未更改数据";
        }
        List<String> teacherNameList = new ArrayList<>();

        for (Map.Entry<String, String> entry : deleteClassTeacherMap.entrySet()) {
            M_ClassTeacherRelation relation = new M_ClassTeacherRelation();
            relation.setSchool(mClassTeacherMapDTO.getSchool());
            relation.setClassName(entry.getKey());
            relation.setTeacherName(entry.getValue());
            relationList.add(relation);
            teacherNameList.add(entry.getValue());
        }

        //拿到所有教师对应的id
        List<Integer> teacherIdList = m_TeacherListMapper.getTeacherIdList(relationList);
        if(teacherIdList == null || teacherIdList.size() == 0){
            for(M_ClassTeacherRelation relation : relationList){
                notFoundTeacherList.add(relation.getTeacherName());
            }
            return "未找到教师:"+String.join(",",notFoundTeacherList)+";";
        }
        //比对数据库中的教师姓名和前端传递的教师姓名，如果有不一样的，就新增
        List<String> newTeacherNameList = m_TeacherListMapper.getTeacherNameById(teacherIdList,mClassTeacherMapDTO.getSchool());
        Map<String,Integer> TeacherNameIdMap = new HashMap<>();
        for(int i = 0; i<teacherIdList.size();i++){
            TeacherNameIdMap.put(newTeacherNameList.get(i),teacherIdList.get(i));
        }
        notFoundTeacherList = teacherNameList.stream().filter(item -> !newTeacherNameList.contains(item)).collect(Collectors.toList());
        if(notFoundTeacherList!=null && notFoundTeacherList.size()>0){
            resp += "未找到教师:"+String.join(",",notFoundTeacherList)+";";
        }
        if(newTeacherNameList == null || newTeacherNameList.size() == 0){
            return resp;
        }


        //拿到所有班级对应的id
        List<Integer> classIdList = m_gradeClassNumMapper.getClassIdList(relationList);
        List<String> classNameListNew = m_gradeClassNumMapper.getClassNameById(classIdList,mClassTeacherMapDTO.getSchool());
        relationList.clear();
        for(int i =0 ; i<newTeacherNameList.size();i++){
            M_ClassTeacherRelation relation = new M_ClassTeacherRelation();
            relation.setSchool(mClassTeacherMapDTO.getSchool());
            relation.setClassId(classIdList.get(i));
            relation.setClassName(classNameListNew.get(i));
            relation.setTeacherName(deleteClassTeacherMap.get(classNameListNew.get(i)));
            relation.setTeacherId(TeacherNameIdMap.get(deleteClassTeacherMap.get(classNameListNew.get(i))));
            relationList.add(relation);
        }


        m_ClassTeacherRelationMapper.deleteRelations(relationList);
        resp += "删除教师："+String.join(",",teacherNameList)+";";
        return resp;
    }

    //早读统计、早操统计放到一起处理
    @Override
    public Map<String,List<M_ClassCountVO>> getClassCount(LocalDate startDate, LocalDate endDate, String school, String type,Integer topNum,List<String> timeZone,Integer praiseAddScore, Integer criticizeSubScore) {
        if(startDate == null){
            startDate = endDate;
        }
        if(timeZone == null || timeZone.size() == 0){
            return null;
        }

        if(type.equals("全校Max")){
        List<M_ClassCountVO>  mClassCountVOS = m_readingFeedbackMapper.getClassCount(startDate,endDate,school,topNum,timeZone);
        Map<String,List<M_ClassCountVO>> classCountMap = new HashMap<>();
        classCountMap.put("全校Max",mClassCountVOS);
            return classCountMap;
        }
        if(type.equals("早操Max")){
            List<String> gradeList = new ArrayList<>();
            List<String> repeatGradeList = m_gradeClassNumMapper.getGrade(school);
            Set<String> set = new HashSet<>(repeatGradeList);
            gradeList.addAll(set);

            Map<String,List<M_ClassCountVO>> classCountMap = new HashMap<>();
            gradeList.add("");
            for(String grade : gradeList){
                List<List<M_ClassCountVO>> classCountList = new ArrayList<>();
                for(String timeZoneStr : timeZone){
                    if(timeZoneStr.equals("体操表扬")||timeZoneStr.equals("跑操表扬")){
                        Integer score = praiseAddScore;
                        List<M_ClassCountVO>  mClassCountVOSAdd = m_sportRecordMapper.getGradeClassCountScore(startDate,endDate,school,grade,topNum,timeZoneStr,score);
                        classCountList.add(mClassCountVOSAdd);
                    } else if ( timeZoneStr.equals("体操批评")||timeZoneStr.equals("跑操批评")){
                        Integer score = criticizeSubScore * -1;
                        List<M_ClassCountVO>  mClassCountVOSSub = m_sportRecordMapper.getGradeClassCountScore(startDate,endDate,school,grade,topNum,timeZoneStr,score);
                        classCountList.add(mClassCountVOSSub);
                    }

                }
                Map<String,Integer> classCountScoreMap = new HashMap<>();
                for(List<M_ClassCountVO> mClassCountVOList : classCountList){
                    for(M_ClassCountVO mClassCountVO : mClassCountVOList){
                        if(classCountScoreMap.containsKey(mClassCountVO.getClassName())){
                            Integer score = classCountScoreMap.get(mClassCountVO.getClassName());
                            score += mClassCountVO.getClassCount();
                            classCountScoreMap.put(mClassCountVO.getClassName(),score);
                        }else {
                            classCountScoreMap.put(mClassCountVO.getClassName(),mClassCountVO.getClassCount());
                        }
                    }
                }
                //再把Map转化为List<M_ClassCountVO>,还要按照分数排序
                List<M_ClassCountVO> mClassCountVOList = new ArrayList<>();
                for(Map.Entry<String,Integer> entry : classCountScoreMap.entrySet()){
                    M_ClassCountVO mClassCountVO = new M_ClassCountVO();
                    mClassCountVO.setClassName(entry.getKey());
                    mClassCountVO.setClassCount(entry.getValue());
                    mClassCountVOList.add(mClassCountVO);
                }
                //按照分数排序
                //如果timeZone列表仅包含体操批评或者跑操批评，就按照分数从小到大排序
                boolean isSort = true;
                for(String item : timeZone){
                    if(!item.equals("体操批评") && !item.equals("跑操批评")){
                        isSort = false;
                    }
                }
                if (isSort){
                    mClassCountVOList.sort(Comparator.comparing(M_ClassCountVO::getClassCount));
                }else {
                    mClassCountVOList.sort(Comparator.comparing(M_ClassCountVO::getClassCount).reversed());
                }
                //取前topNum个
                mClassCountVOList = mClassCountVOList.subList(0,Math.min(topNum,mClassCountVOList.size()));

                if(grade.equals("")){
                    grade = "全校";
                }
                classCountMap.put(grade,mClassCountVOList);
            }
            return classCountMap;
        }

        if(type.equals("年级Max")){
            List<String> gradeList = new ArrayList<>();
            List<String> repeatGradeList = m_gradeClassNumMapper.getGrade(school);
            Set<String> set = new HashSet<>(repeatGradeList);
            gradeList.addAll(set);

            Map<String,List<M_ClassCountVO>> classCountMap = new HashMap<>();
            for(String grade : gradeList){
                List<M_ClassCountVO>  mClassCountVOS = m_readingFeedbackMapper.getGradeClassCount(startDate,endDate,school,grade,topNum,timeZone);
                classCountMap.put(grade,mClassCountVOS);
            }
            return classCountMap;


        }
        if(type.equals("学科Max")){
            List<M_ClassCountVO>  mClassCountVOS = m_singleReadTeacherRecordMapper.getSubjectCount(startDate,endDate,school,topNum,timeZone);
            Map<String,List<M_ClassCountVO>> classCountMap = new HashMap<>();
            classCountMap.put("学科Max",mClassCountVOS);
            return classCountMap;
        }
        if(type.equals("教师Max")){
            List<M_ClassCountVO>  mClassCountVOS = m_singleReadTeacherRecordMapper.getTeacherCount(startDate,endDate,school,topNum,timeZone);
            Map<String,List<M_ClassCountVO>> classCountMap = new HashMap<>();
            classCountMap.put("教师Max",mClassCountVOS);
            return classCountMap;
        }
        return null;
    }

    //获取早读反馈的统计数据
    @Override
    public Map<String, Integer> getLabelCount(LocalDate startDate, LocalDate endDate, String school, String type, List<String> timeZone) {
        if(startDate == null){
            startDate = endDate;
        }
        Map<String,Integer> labelCountMap = new HashMap<>();
        if(type.equals("早读巡查")){
            Integer maxAllClassCount = m_readingFeedbackMapper.getMaxAllClassCount(startDate,endDate,school,timeZone);
            labelCountMap.put("全校Max",maxAllClassCount);
            Integer maxGradeCount = m_readingFeedbackMapper.getMaxGradeCount(startDate,endDate,school,timeZone);
            labelCountMap.put("年级Max",maxGradeCount);
            Integer maxSubjectCount = m_singleReadTeacherRecordMapper.getMaxSubjectCount(startDate,endDate,school,timeZone);
            labelCountMap.put("学科Max",maxSubjectCount);
            Integer maxTeacherCount = m_singleReadTeacherRecordMapper.getMaxTeacherCount(startDate,endDate,school,timeZone);
            labelCountMap.put("教师Max",maxTeacherCount);
            return labelCountMap;
        }
        if(type.equals("全校Max")){
            Integer allRecord = m_readingFeedbackMapper.getAllRecordCount(startDate,endDate,school,timeZone);
            labelCountMap.put("总记录数",allRecord);
            List<String> classNameList = m_readingFeedbackMapper.getAllClassName(startDate,endDate,school,timeZone);
            Integer allClassNum = classNameList.size();
            //求平均值，并取整
            int roundedAverage = 0;
            if (allClassNum != 0) {
                double average = (double) allRecord / allClassNum;
                roundedAverage = (int) Math.round(average);
            } else {
                // 处理除数为零的情况，例如设置平均值为 0
                roundedAverage = 0;
            }
            labelCountMap.put("班级平均数",roundedAverage);
            return labelCountMap;
        }
        if (type.equals("年级Max")){
            Integer allRecord = m_readingFeedbackMapper.getAllRecordCount(startDate,endDate,school,timeZone);
            labelCountMap.put("总记录数",allRecord);
            List<String> gradeList = m_gradeClassNumMapper.getGrade(school);
            HashSet<String> gradeSet = new HashSet<>(gradeList);
            List<String> repeatGradeList = new ArrayList<>(gradeSet);
            Integer  allgradeNum = repeatGradeList.size();
            //求平均值，并取整
            int roundedAverage = 0;
            if (allgradeNum != 0) {
                double average = (double) allRecord / allgradeNum;
                roundedAverage = (int) Math.round(average);
            } else {
                // 处理除数为零的情况，例如设置平均值为 0
                roundedAverage = 0;
            }
            labelCountMap.put("年级平均数",roundedAverage);
            return labelCountMap;
        }
        if(type.equals("学科Max")){
            Integer allRecord = m_singleReadTeacherRecordMapper.getAllRecordCount(startDate,endDate,school,timeZone);
            labelCountMap.put("总记录数",allRecord);
            List<String> subjectList = m_singleReadTeacherRecordMapper.getSubject(school);
            HashSet<String> subjectSet = new HashSet<>(subjectList);
            List<String> repeatSubjectList = new ArrayList<>(subjectSet);



            Integer  allSubjectNum = repeatSubjectList.size();
            //求平均值，并取整
            int roundedAverage = 0;
            if (allSubjectNum != 0) {
                double average = (double) allRecord / allSubjectNum;
                roundedAverage = (int) Math.round(average);
            } else {
                // 处理除数为零的情况，例如设置平均值为 0
                roundedAverage = 0;
            }
            labelCountMap.put("学科平均数",roundedAverage);
            return labelCountMap;
        }
        if(type.equals("教师Max")){
            Integer allRecord = m_singleReadTeacherRecordMapper.getAllRecordCount(startDate,endDate,school,timeZone);
            labelCountMap.put("总记录数",allRecord);
            List<String> teacherList = m_singleReadTeacherRecordMapper.getTeacher(school);
            HashSet<String> teacherSet = new HashSet<>(teacherList);
            List<String> repeatTeacherList = new ArrayList<>(teacherSet);

            Integer  allTeacherNum = repeatTeacherList.size();
            //求平均值，并取整
            int roundedAverage = 0;
            if (allTeacherNum != 0) {
                double average = (double) allRecord / allTeacherNum;
                roundedAverage = (int) Math.round(average);
            } else {
                // 处理除数为零的情况，例如设置平均值为 0
                roundedAverage = 0;
            }
            labelCountMap.put("教师平均数",roundedAverage);
            return labelCountMap;
        }

        return null;

    }

    /**
     * 根据不同年级输出要表扬的班级，要考虑到年级对应的班级为空，要考虑到最后的班级名称不加、
     */
    private String getClassName(String grade,List<M_FeedBack> feedBackList){
        List<M_FeedBack> gradeFeedBacks = new ArrayList<>();
        for(M_FeedBack feedBack: feedBackList){
            if(feedBack.getGrade().equals(grade)){
                gradeFeedBacks.add(feedBack);
            }
        }
        String feedback = "["+grade+"]" + "：";
        if (gradeFeedBacks.isEmpty()) {
            // 列表为空时的逻辑
            feedback = "";
        }else {
            for(M_FeedBack feedbackclass: gradeFeedBacks){
                if(feedbackclass == null){
                    feedback = "";
                }else if(feedbackclass != gradeFeedBacks.get(gradeFeedBacks.size()-1)){
                    feedback += feedbackclass.getClassName();
                    // 再移除最后一个字符
                    if (feedback.length() > 0) {
                        feedback = feedback.substring(0, feedback.length() - 1) + "、";
                    }
                }else {
                    feedback += feedbackclass.getClassName();
                    // 再移除最后一个字符
                    if (feedback.length() > 0) {
                        feedback = feedback.substring(0, feedback.length() - 1) +"<br>";
                    }
                }
            }
        }
        return feedback;
    }



    //根据学校和日期获取早读反馈报告
    @Override
    public String getFeedbackReport(String school, LocalDate checkDate) {
        String resp = "";
        String[] chineseNumbers = {"一", "二", "三", "四", "五", "六", "七", "八", "九", "十"};
        Integer order = 0;
        M_ReadingFeedbackReport mReadingFeedbackReport = m_readingFeedbackReportMapper.getFeedbackReport(school);
        String timeZone = "自主";
        List<M_FeedBack> classManageFeedBackList = m_readingFeedbackMapper.getClassManageFeedback(school,checkDate,timeZone);
        List<M_SingleReadTeacher> teacherManageFeedBackList = m_singleReadTeacherRecordMapper.getSelectedTeacherStatus(school,checkDate,timeZone);
        timeZone = "早读";
        List<M_FeedBack> classReadingFeedBackList = m_readingFeedbackMapper.getClassManageFeedback(school,checkDate,timeZone);
        List<M_SingleReadTeacher> teacherReadingFeedBackList = m_singleReadTeacherRecordMapper.getSelectedTeacherStatus(school,checkDate,timeZone);
        timeZone = "课前";
        List<M_FeedBack> classPrepareFeedBackList = m_readingFeedbackMapper.getClassManageFeedback(school,checkDate,timeZone);
        List<M_SingleReadTeacher> teacherPrepareFeedBackList = m_singleReadTeacherRecordMapper.getSelectedTeacherStatus(school,checkDate,timeZone);
        if(classManageFeedBackList != null && classManageFeedBackList.size()>0){
            order++;
            //把order转换成中文
            String chineseOrder = chineseNumbers[order-1] + "、";
            String grade1manageFeedback = getClassName("一年级",classManageFeedBackList);
            String grade2manageFeedback = getClassName("二年级",classManageFeedBackList);
            String grade3manageFeedback = getClassName("三年级",classManageFeedBackList);
            String grade4manageFeedback = getClassName("四年级",classManageFeedBackList);
            String grade5manageFeedback = getClassName("五年级",classManageFeedBackList);
            String grade6manageFeedback = getClassName("六年级",classManageFeedBackList);
            String grade7manageFeedback = getClassName("七年级",classManageFeedBackList);
            String grade8manageFeedback = getClassName("八年级",classManageFeedBackList);
            String grade9manageFeedback = getClassName("九年级",classManageFeedBackList);
            if(order ==1){
                resp += checkDate.toString() + "<br>";
            }
            if (mReadingFeedbackReport.getPreface() != null && !mReadingFeedbackReport.getPreface().isEmpty() && order == 1) {
                resp += mReadingFeedbackReport.getPreface() + "<br>";

            }
            resp += chineseOrder;
            if(mReadingFeedbackReport.getPreClassManage()!= null && !mReadingFeedbackReport.getPreClassManage().isEmpty()){
                String originPreClassManage = mReadingFeedbackReport.getPreClassManage();
                String preClassManage = originPreClassManage.replace("】","】<br>");
                resp += preClassManage + "<br>";
            }
            resp += grade1manageFeedback + grade2manageFeedback + grade3manageFeedback + grade4manageFeedback + grade5manageFeedback + grade6manageFeedback + grade7manageFeedback + grade8manageFeedback + grade9manageFeedback ;
            if(mReadingFeedbackReport.getPostClassManage()!= null && !mReadingFeedbackReport.getPostClassManage().isEmpty()){
                String PostClassManage = mReadingFeedbackReport.getPostClassManage();
                resp += PostClassManage + "<br>";
            }

        }
        if(classReadingFeedBackList != null && classReadingFeedBackList.size()>0){
            order++;
            //把order转换成中文
            String chineseOrder = chineseNumbers[order-1] + "、";
            String grade1readingFeedback = getClassName("一年级",classReadingFeedBackList);
            String grade2readingFeedback = getClassName("二年级",classReadingFeedBackList);
            String grade3readingFeedback = getClassName("三年级",classReadingFeedBackList);
            String grade4readingFeedback = getClassName("四年级",classReadingFeedBackList);
            String grade5readingFeedback = getClassName("五年级",classReadingFeedBackList);
            String grade6readingFeedback = getClassName("六年级",classReadingFeedBackList);
            String grade7readingFeedback = getClassName("七年级",classReadingFeedBackList);
            String grade8readingFeedback = getClassName("八年级",classReadingFeedBackList);
            String grade9readingFeedback = getClassName("九年级",classReadingFeedBackList);
            if(order ==1){
                resp += checkDate.toString() + "<br>";
            }
            if (mReadingFeedbackReport.getPreface() != null && !mReadingFeedbackReport.getPreface().isEmpty() && order == 1) {
                resp += mReadingFeedbackReport.getPreface() + "<br>";

            }
            resp += chineseOrder;
            if(mReadingFeedbackReport.getPreClassReading()!= null && !mReadingFeedbackReport.getPreClassReading().isEmpty()){
                String originPreClassReading = mReadingFeedbackReport.getPreClassReading();
                String preClassReading = originPreClassReading.replace("】","】<br>");
                resp += preClassReading + "<br>";
            }
            resp += grade1readingFeedback + grade2readingFeedback + grade3readingFeedback + grade4readingFeedback + grade5readingFeedback + grade6readingFeedback + grade7readingFeedback + grade8readingFeedback + grade9readingFeedback ;
            if(mReadingFeedbackReport.getPostClassReading()!= null && !mReadingFeedbackReport.getPostClassReading().isEmpty()){
                String PostClassReading = mReadingFeedbackReport.getPostClassReading();
                resp += PostClassReading + "<br>";
            }
            if (teacherReadingFeedBackList != null && teacherReadingFeedBackList.size() > 0) {
                if(mReadingFeedbackReport.getPreTeacherReading()!= null && !mReadingFeedbackReport.getPreTeacherReading().isEmpty()){
                    String preTeacherReading = mReadingFeedbackReport.getPreTeacherReading();
                    resp += preTeacherReading + "    ";
                }
                List<String> originReadingTeacherNameList = new ArrayList<>();
                for(M_SingleReadTeacher teacherReadingFeedBack: teacherReadingFeedBackList){
                    originReadingTeacherNameList.add(teacherReadingFeedBack.getTeacherName());
                }
                HashSet<String> readingTeacherNameSet = new HashSet<>(originReadingTeacherNameList);
                List<String> readingTeacherNameList = new ArrayList<>(readingTeacherNameSet);

                String readingTeacherFeedback = String.join("、",readingTeacherNameList);
                resp += readingTeacherFeedback ;
                if(mReadingFeedbackReport.getPostTeacherReading()!= null && !mReadingFeedbackReport.getPostTeacherReading().isEmpty()){
                    String postTeacherReading = mReadingFeedbackReport.getPostTeacherReading();
                    resp += postTeacherReading + "<br>";
                }


            }


        }
        if (classPrepareFeedBackList != null && classPrepareFeedBackList.size() > 0) {
            order++;
            //把order转换成中文
            String chineseOrder = chineseNumbers[order - 1] + "、";
            String grade1prepareFeedback = getClassName("一年级", classPrepareFeedBackList);
            String grade2prepareFeedback = getClassName("二年级", classPrepareFeedBackList);
            String grade3prepareFeedback = getClassName("三年级", classPrepareFeedBackList);
            String grade4prepareFeedback = getClassName("四年级", classPrepareFeedBackList);
            String grade5prepareFeedback = getClassName("五年级", classPrepareFeedBackList);
            String grade6prepareFeedback = getClassName("六年级", classPrepareFeedBackList);
            String grade7prepareFeedback = getClassName("七年级", classPrepareFeedBackList);
            String grade8prepareFeedback = getClassName("八年级", classPrepareFeedBackList);
            String grade9prepareFeedback = getClassName("九年级", classPrepareFeedBackList);

            if(order ==1){
                resp += checkDate.toString() + "<br>";
            }
            if (mReadingFeedbackReport.getPreface() != null && !mReadingFeedbackReport.getPreface().isEmpty() && order == 1) {
                resp += mReadingFeedbackReport.getPreface() + "<br>";

            }
            resp += chineseOrder;
            if (mReadingFeedbackReport.getPreClassPrepare() != null && !mReadingFeedbackReport.getPreClassPrepare().isEmpty()) {
                String originPreClassPrepare = mReadingFeedbackReport.getPreClassPrepare();
                String preClassPrepare = originPreClassPrepare.replace("】", "】<br>");
                resp += preClassPrepare + "<br>";
            }
            resp += grade1prepareFeedback + grade2prepareFeedback + grade3prepareFeedback + grade4prepareFeedback + grade5prepareFeedback + grade6prepareFeedback + grade7prepareFeedback + grade8prepareFeedback + grade9prepareFeedback;
            if (mReadingFeedbackReport.getPostClassPrepare() != null && !mReadingFeedbackReport.getPostClassPrepare().isEmpty()) {
                String PostClassPrepare = mReadingFeedbackReport.getPostClassPrepare();
                resp += PostClassPrepare + "<br>";
            }
            if (teacherPrepareFeedBackList != null && teacherPrepareFeedBackList.size() > 0) {
                if (mReadingFeedbackReport.getPreTeacherPrepare() != null && !mReadingFeedbackReport.getPreTeacherPrepare().isEmpty()) {
                    String preTeacherPrepare = mReadingFeedbackReport.getPreTeacherPrepare();
                    resp += preTeacherPrepare + "    ";
                }
                List<String> originPrepareTeacherNameList = new ArrayList<>();
                for (M_SingleReadTeacher teacherPrepareFeedBack : teacherPrepareFeedBackList) {
                    originPrepareTeacherNameList.add(teacherPrepareFeedBack.getTeacherName());
                }
                HashSet<String> prepareTeacherNameSet = new HashSet<>(originPrepareTeacherNameList);
                List<String> prepareTeacherNameList = new ArrayList<>(prepareTeacherNameSet);
                String prepareTeacherFeedback = String.join("、", prepareTeacherNameList);
                resp += prepareTeacherFeedback;
                if (mReadingFeedbackReport.getPostTeacherPrepare() != null && !mReadingFeedbackReport.getPostTeacherPrepare().isEmpty()) {
                    String postTeacherPrepare = mReadingFeedbackReport.getPostTeacherPrepare();
                    resp += postTeacherPrepare + "<br>";
                }


            }
        }
        if(mReadingFeedbackReport.getFinalComment()!=null && !mReadingFeedbackReport.getFinalComment().isEmpty()){
            resp += mReadingFeedbackReport.getFinalComment() + "<br>";
        }

        if(resp.equals("")){
            return "暂无数据";
        }



        return resp;
    }

    @Override
    public String poorPerform(M_PoorPerformDTO poorPerformDTO) {
        //首先查询，教师是不是在教师列表中
        String teacherName = poorPerformDTO.getTeacherName();
        String school = poorPerformDTO.getSchool();
        Integer teacherId = m_TeacherListMapper.getTeacherIdByName(teacherName,school);
        if(teacherId == null){
            return "未找到教师";
        }else {
            String subject = m_TeacherListMapper.getSubjectByName(teacherName,school);
            poorPerformDTO.setSubject(subject);
            m_poorPerformerMapper.addPoorPerform(poorPerformDTO);
            return "success";


        }



    }


    // 获取单元格值的通用方法
    private String getCellValueAsString(Cell cell) {
        if (cell == null) {
            return ""; // 如果单元格为空，返回空字符串
        }

        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue(); // 文本类型
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getLocalDateTimeCellValue().toString(); // 日期类型
                } else {
                    int cellValue = (int) cell.getNumericCellValue();
                    return String.valueOf(cellValue); // 数字类型
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue()); // 布尔类型
            case FORMULA:
                return cell.getCellFormula(); // 公式类型
            default:
                return "";
        }
    }
}

