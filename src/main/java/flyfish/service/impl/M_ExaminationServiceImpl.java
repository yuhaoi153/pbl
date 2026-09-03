package flyfish.service.impl;


import com.alibaba.excel.util.StringUtils;
import flyfish.mapper.*;
import flyfish.pojo.*;
import flyfish.pojo.DTO.*;
import flyfish.pojo.VO.M_ExamNameListVO;
import flyfish.pojo.VO.M_ExaminationExcelVO;
import flyfish.pojo.VO.M_QueryClassExaminationVO;
import flyfish.pojo.VO.M_StudentExamScoreVO;
import flyfish.service.M_ExaminationService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Service
public class M_ExaminationServiceImpl implements M_ExaminationService {

    @Autowired
    private M_ExaminationMapper m_ExaminationMapper;
    @Autowired
    private M_StudentInfoMapper m_StudentInfoMapper;
    @Autowired
    private M_ExamNotCountStudentMapper m_ExamNotCountStudentMapper;
    @Autowired
    private M_GradeYearMapper gradeYearMapper;
    @Autowired
    private M_DefaultConfigMapper m_DefaultConfigMapper;
    @Autowired
    private M_ExamNameMapper m_ExamNameMapper;
    @Autowired
    private M_ExamEditRecordMapper m_ExamEditRecordMapper;
    @Autowired
    private M_GradeClassNumMapper gradeClassNumMapper;


    /**
     * 生成导入成绩的模板
     *
     * @param school
     * @param grade
     * @param className
     * @return
     * @throws IOException
     */
    @Override
    public byte[] generateImportTemplate(
            String school,
            String grade,
            Integer className
    ) throws IOException {

        // 1. 查询指定班级的学生
        List<M_StudentInfo> students = m_StudentInfoMapper.getStudentInfoListBySchoolAndGradeClassName(
                school,
                grade,
                className
        );

        if (students == null || students.isEmpty()) {
            throw new IllegalArgumentException(
                    "没有查询到该班级的学生信息"
            );
        }

        // 2. 创建Excel
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream outputStream =
                     new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("成绩导入模板");

            // 冻结首行
            sheet.createFreezePane(0, 1);

            // 设置列宽
            sheet.setColumnWidth(0, 18 * 256); // 学生姓名
            sheet.setColumnWidth(1, 15 * 256); // 分数
            sheet.setColumnWidth(2, 20 * 256); // 学号

            // 3. 创建表头样式
            CellStyle headerStyle = createHeaderStyle(workbook);

            Row headerRow = sheet.createRow(0);
            headerRow.setHeightInPoints(28);

            String[] headers = {"学生姓名", "分数", "学号"};

            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // 添加筛选按钮
            sheet.setAutoFilter(new CellRangeAddress(0, 0, 0, 2));

            // 4. 创建普通单元格样式
            CellStyle dataStyle = createDataStyle(workbook);

            // 学号使用文本格式，防止前导0丢失
            CellStyle studentNoStyle = createDataStyle(workbook);
            studentNoStyle.setDataFormat(
                    workbook.createDataFormat().getFormat("@")
            );

            // 5. 写入学生信息
            for (int i = 0; i < students.size(); i++) {

                M_StudentInfo student = students.get(i);
                Row row = sheet.createRow(i + 1);
                row.setHeightInPoints(24);

                // 学生姓名
                Cell nameCell = row.createCell(0);
                nameCell.setCellValue(student.getStudentName());
                nameCell.setCellStyle(dataStyle);

                // 分数列留空
                Cell scoreCell = row.createCell(1, CellType.BLANK);
                scoreCell.setCellStyle(dataStyle);

                // 学号
                Cell studentNoCell =
                        row.createCell(2, CellType.STRING);

                studentNoCell.setCellValue(
                        student.getStudentNumber() == null
                                ? ""
                                : student.getStudentNumber()
                );
                studentNoCell.setCellStyle(studentNoStyle);
            }

            // 6. 限制分数只能填写0～100
            addScoreValidation(sheet, students.size());

            // 7. 输出Excel
            workbook.write(outputStream);

            return outputStream.toByteArray();
        }
    }


    /**
     * 创建表头样式
     *
     * @param workbook
     * @return
     */
    private CellStyle createHeaderStyle(Workbook workbook) {

        CellStyle style = workbook.createCellStyle();

        style.setFillForegroundColor(
                IndexedColors.ROYAL_BLUE.getIndex()
        );
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);

        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);

        Font font = workbook.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());
        font.setFontHeightInPoints((short) 12);

        style.setFont(font);

        return style;
    }

    /**
     * 创建数据行格式
     *
     * @param workbook
     * @return
     */
    private CellStyle createDataStyle(Workbook workbook) {

        CellStyle style = workbook.createCellStyle();

        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);

        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);

        return style;
    }

    /**
     * 添加分数列的验证规则，限制只能填写0～100
     *
     * @param sheet
     * @param studentCount
     */
    private void addScoreValidation(
            Sheet sheet,
            int studentCount
    ) {

        DataValidationHelper helper =
                sheet.getDataValidationHelper();

        DataValidationConstraint constraint =
                helper.createDecimalConstraint(
                        DataValidationConstraint.OperatorType.BETWEEN,
                        "0",
                        "100"
                );

        // 第2行到最后一个学生所在行，分数为第2列
        CellRangeAddressList addressList =
                new CellRangeAddressList(
                        1,
                        studentCount,
                        1,
                        1
                );

        DataValidation validation =
                helper.createValidation(
                        constraint,
                        addressList
                );

        validation.setShowErrorBox(true);
        validation.createErrorBox(
                "分数格式错误",
                "分数必须是0到100之间的数字"
        );

        validation.setShowPromptBox(true);
        validation.createPromptBox(
                "填写说明",
                "请输入0到100之间的分数"
        );

        sheet.addValidationData(validation);
    }


    /**
     * 导入学生考试成绩
     */
    @Transactional(rollbackFor = Exception.class)
    public M_ExaminationExcelVO importExcel(
            MultipartFile file,
            String school,
            String grade,
            Integer className,
            String subject,
            String examName,
            String createName
    ) throws IOException {

        // 1. 检查请求参数
        validateRequest(
                file,
                school,
                grade,
                className,
                subject,
                examName,
                createName
        );

        school = school.trim();
        grade = grade.trim();
        subject = subject.trim();
        String semester = examName.split("/")[0];
        String testName = examName.split("/")[1];

        createName = createName.trim();

        // 2. 检查是否已经导入过相同考试
        Integer existingCount =
                m_ExaminationMapper.countExistingExam(
                        school,
                        grade,
                        className,
                        subject,
                        semester,
                        testName
                );

        if (existingCount > 0) {
            //先删除相关的数据，再新增
            M_ExamDeleteDTO dto = new M_ExamDeleteDTO();
            dto.setSchool(school);
            dto.setGrade(grade);
            dto.setClassName(className);
            dto.setSubject(subject);
            dto.setTestName(testName);
            dto.setSemester(semester);
            m_ExaminationMapper.deleteExamRecord(dto);


            //记录

            M_ExamEditRecord mExamEditRecord = new M_ExamEditRecord();
            mExamEditRecord.setActionWay("修改");
            mExamEditRecord.setUpdateName(createName);
            mExamEditRecord.setSchool(school);
            mExamEditRecord.setDatabaseName("examination");
            mExamEditRecord.setContentInfo(examName+"-"+subject+"-"+existingCount+"条数据");
            mExamEditRecord.setClassOrName(grade+className+"班");
            mExamEditRecord.setCreateTime(LocalDateTime.now());

            m_ExamEditRecordMapper.insertSingleRecord(mExamEditRecord);


        }

        // 3. 查询studentInfo表中的班级学生
        List<M_StudentInfo> classStudents =
                m_StudentInfoMapper.getStudentInfoListBySchoolAndGradeClassName(
                        school,
                        grade,
                        className
                );

        if (classStudents == null
                || classStudents.isEmpty()) {

            throw new IllegalArgumentException(
                    "studentInfo表中没有查询到"
                            + grade + className + "班的学生"
            );
        }

        // 学号作为Key，方便校验学生
        Map<String, M_StudentInfo> studentMap =
                classStudents.stream()
                        .filter(student ->
                                StringUtils.hasText(
                                        student.getStudentNumber()
                                )
                        )
                        .collect(
                                Collectors.toMap(
                                        student ->
                                                student
                                                        .getStudentNumber()
                                                        .trim(),
                                        Function.identity(),
                                        (first, second) -> first
                                )
                        );

        if (studentMap.isEmpty()) {
            throw new IllegalArgumentException(
                    "该班级学生没有设置学号"
            );
        }

        // 成功数据
        List<M_Examination> examinationList =
                new ArrayList<>();

        // 失败学生名单，使用LinkedHashSet防止重复并保持顺序
        Set<String> failStudentNameSet =
                new LinkedHashSet<>();

        // 检查Excel内部是否存在重复学号
        Set<String> importedStudentNos =
                new HashSet<>();

        // 4. 读取Excel
        try (
                InputStream inputStream =
                        file.getInputStream();

                Workbook workbook =
                        WorkbookFactory.create(inputStream)
        ) {

            if (workbook.getNumberOfSheets() == 0) {
                throw new IllegalArgumentException(
                        "Excel文件中没有工作表"
                );
            }

            Sheet sheet = workbook.getSheetAt(0);

            DataFormatter formatter =
                    new DataFormatter(Locale.CHINA);

            // 5. 检查表头
            validateHeader(
                    sheet.getRow(0),
                    formatter
            );


            Integer year = gradeYearMapper.getYearByGrade(grade);
            // 同一场考试只生成一次时间
            LocalDateTime createExamTime =
                    LocalDateTime.now().withNano(0);

            List<String> mExamNotCountStudentList = m_ExamNotCountStudentMapper.getByclassName(school, grade, className);

            // 6. 从第二行开始读取
            for (
                    int rowIndex = 1;
                    rowIndex <= sheet.getLastRowNum();
                    rowIndex++
            ) {

                Row row = sheet.getRow(rowIndex);

                if (row == null) {
                    continue;
                }

                int excelRowNumber = rowIndex + 1;

                /*
                 * 先初始化学生姓名。
                 * 如果后续校验失败，用来加入失败名单。
                 */
                String studentName = "";

                try {
                    // 第一列：学生姓名
                    studentName = getCellValue(
                            row.getCell(0),
                            formatter,
                            excelRowNumber
                    );

                    // 第二列：分数
                    String scoreText = getCellValue(
                            row.getCell(1),
                            formatter,
                            excelRowNumber
                    );

                    // 第三列：学号
                    String studentNo = getCellValue(
                            row.getCell(2),
                            formatter,
                            excelRowNumber
                    );

                    // 整行为空，直接跳过
                    if (!StringUtils.hasText(studentName)
                            && !StringUtils.hasText(scoreText)
                            && !StringUtils.hasText(studentNo)) {

                        continue;
                    }

                    // 学生姓名不能为空
                    if (!StringUtils.hasText(studentName)) {
                        throw new IllegalArgumentException(
                                "学生姓名不能为空"
                        );
                    }

                    // 学号不能为空
                    if (!StringUtils.hasText(studentNo)) {
                        throw new IllegalArgumentException(
                                "学号不能为空"
                        );
                    }

                    studentName = studentName.trim();
                    studentNo = studentNo.trim();

                    // Excel中学号不能重复
                    if (!importedStudentNos.add(studentNo)) {
                        throw new IllegalArgumentException(
                                "Excel中学号重复"
                        );
                    }

                    // 检查学生是否属于当前班级
                    M_StudentInfo databaseStudent =
                            studentMap.get(studentNo);

                    if (databaseStudent == null) {
                        throw new IllegalArgumentException(
                                "该学号不属于当前班级"
                        );
                    }

                    String databaseStudentName =
                            databaseStudent
                                    .getStudentName()
                                    == null
                                    ? ""
                                    : databaseStudent
                                    .getStudentName()
                                    .trim();

                    // 检查姓名和学号是否对应
                    if (!databaseStudentName
                            .equals(studentName)) {

                        throw new IllegalArgumentException(
                                "学生姓名与学号不匹配"
                        );
                    }

                    /*
                     * 分数为空表示缺考。
                     *
                     * 缺考学生仍然属于成功导入，
                     * 不加入失败名单。
                     */
                    BigDecimal score;
                    String status;

                    if (!StringUtils.hasText(scoreText)) {

                        score = null;
                        status = "缺考";

                    } else {

                        score = parseScore(
                                scoreText,
                                excelRowNumber
                        );

                        // 正常成绩暂时不设置status
                        status = "正常";
                    }

                    // 创建成绩对象
                    M_Examination examination =
                            new M_Examination();


                    if (mExamNotCountStudentList != null && mExamNotCountStudentList.size() > 0) {
                        if (mExamNotCountStudentList.contains(databaseStudentName)) {
                            status = "免考";
                        }

                    }


                    examination.setStudentName(
                            databaseStudentName
                    );
                    examination.setStudentNo(studentNo);
                    examination.setScore(score);

                    examination.setSchool(school);
                    examination.setGrade(grade);
                    examination.setClassName(className);
                    examination.setSubject(subject);
                    examination.setTestName(testName);
                    examination.setSemester(semester);
                    examination.setCreateName(createName);
                    examination.setCreateTime(createExamTime);

                    examination.setHide("未发布");
                    examination.setStatus(status);


                    examination.setYear(year);


                    /*
                     * 首次导入时不设置：

                     * id
                     * updateName
                     * updateTime
                     * imageUrl
                     */

                    examinationList.add(examination);

                } catch (IllegalArgumentException exception) {

                    /*
                     * 单个学生数据错误时，不中断整个Excel导入，
                     * 只将该学生加入失败名单。
                     */
                    addFailStudentName(
                            failStudentNameSet,
                            studentName,
                            excelRowNumber
                    );
                }
            }
        }

        /*
         * 如果既没有成功数据，也没有失败数据，
         * 说明Excel中没有填写任何学生记录。
         */
        if (examinationList.isEmpty()
                && failStudentNameSet.isEmpty()) {

            throw new IllegalArgumentException(
                    "Excel中没有可以导入的学生成绩"
            );
        }

        // 7. 批量插入成功的数据
        Integer importedCount = 0;

        if (!examinationList.isEmpty()) {

            importedCount =
                    m_ExaminationMapper.batchInsert(
                            examinationList
                    );

            if (importedCount
                    != examinationList.size()) {

                throw new IllegalStateException(
                        "实际写入数量与预期数量不一致"
                );
            }
        }

        // 8. 返回成功数量和失败学生名单
        return new M_ExaminationExcelVO(
                importedCount,
                new ArrayList<>(failStudentNameSet)
        );
    }


    /**
     * 添加失败学生姓名
     */
    private void addFailStudentName(
            Set<String> failStudentNameSet,
            String studentName,
            int excelRowNumber
    ) {

        if (StringUtils.hasText(studentName)) {
            failStudentNameSet.add(
                    studentName.trim()
            );
        } else {
            /*
             * 如果学生姓名本身就是空的，
             * 无法记录姓名，只能记录Excel行号。
             */
            failStudentNameSet.add(
                    "Excel第" + excelRowNumber
                            + "行（姓名为空）"
            );
        }
    }

    /**
     * 校验请求参数
     */
    private void validateRequest(
            MultipartFile file,
            String school,
            String grade,
            Integer className,
            String subject,
            String testName,
            String createName
    ) {

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException(
                    "请选择需要导入的Excel文件"
            );
        }

        String originalFilename =
                file.getOriginalFilename();

        if (!StringUtils.hasText(originalFilename)) {
            throw new IllegalArgumentException(
                    "无法获取Excel文件名"
            );
        }

        if (!originalFilename
                .toLowerCase(Locale.ROOT)
                .endsWith(".xlsx")) {

            throw new IllegalArgumentException(
                    "只支持.xlsx格式的Excel文件"
            );
        }

        if (!StringUtils.hasText(school)) {
            throw new IllegalArgumentException(
                    "学校不能为空"
            );
        }

        if (!StringUtils.hasText(grade)) {
            throw new IllegalArgumentException(
                    "年级不能为空"
            );
        }

        if (className == null || className <= 0) {
            throw new IllegalArgumentException(
                    "班级数据不正确"
            );
        }

        if (!StringUtils.hasText(subject)) {
            throw new IllegalArgumentException(
                    "学科不能为空"
            );
        }

        if (!StringUtils.hasText(testName)) {
            throw new IllegalArgumentException(
                    "考试名称不能为空"
            );
        }

        if (!StringUtils.hasText(createName)) {
            throw new IllegalArgumentException(
                    "教师姓名不能为空"
            );
        }
    }

    /**
     * 校验Excel表头
     */
    private void validateHeader(
            Row headerRow,
            DataFormatter formatter
    ) {

        if (headerRow == null) {
            throw new IllegalArgumentException(
                    "Excel表头不存在"
            );
        }

        String[] expectedHeaders = {
                "学生姓名",
                "分数",
                "学号"
        };

        for (
                int columnIndex = 0;
                columnIndex < expectedHeaders.length;
                columnIndex++
        ) {

            Cell headerCell =
                    headerRow.getCell(columnIndex);

            String actualHeader =
                    headerCell == null
                            ? ""
                            : formatter
                            .formatCellValue(headerCell)
                            .trim();

            if (!expectedHeaders[columnIndex]
                    .equals(actualHeader)) {

                throw new IllegalArgumentException(
                        "Excel第"
                                + (columnIndex + 1)
                                + "列表头应为“"
                                + expectedHeaders[columnIndex]
                                + "”，当前为“"
                                + actualHeader + "”"
                );
            }
        }
    }

    /**
     * 读取Excel单元格
     */
    private String getCellValue(
            Cell cell,
            DataFormatter formatter,
            int excelRowNumber
    ) {

        if (cell == null) {
            return "";
        }

        if (cell.getCellType()
                == CellType.FORMULA) {

            throw new IllegalArgumentException(
                    "Excel第" + excelRowNumber
                            + "行不能使用公式"
            );
        }

        return formatter
                .formatCellValue(cell)
                .trim();
    }

    /**
     * 转换并校验分数
     */
    private BigDecimal parseScore(
            String scoreText,
            int excelRowNumber
    ) {

        try {
            BigDecimal score =
                    new BigDecimal(scoreText.trim());

            if (score.compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalArgumentException(
                        "Excel第" + excelRowNumber
                                + "行分数不能小于0"
                );
            }

            if (score.compareTo(
                    new BigDecimal("100")
            ) > 0) {

                throw new IllegalArgumentException(
                        "Excel第" + excelRowNumber
                                + "行分数不能超过100"
                );
            }

            if (score.stripTrailingZeros().scale() > 2) {
                throw new IllegalArgumentException(
                        "Excel第" + excelRowNumber
                                + "行分数最多保留两位小数"
                );
            }

            return score;

        } catch (NumberFormatException exception) {

            throw new IllegalArgumentException(
                    "Excel第" + excelRowNumber
                            + "行分数格式不正确"
            );
        }
    }


    /**
     * 查询某个班级某次考试的数据概览
     */
    @Transactional(readOnly = true)
    public M_QueryClassExaminationVO queryClassExamination
    (
            M_QueryExaminationDTO mQueryExaminationDTO
    ) {

        String school = mQueryExaminationDTO.getSchool();
        String grade = mQueryExaminationDTO.getGrade();
        Integer className = mQueryExaminationDTO.getClassName();
        String subject = mQueryExaminationDTO.getSubject();
        String examName = mQueryExaminationDTO.getExamName();
        String semester = examName.split("/")[0];

        String testName = examName.split("/")[1];
        String teacherName = mQueryExaminationDTO.getTeacherName();


        // 1. 检查参数
        validateRequest(
                school,
                grade,
                className,
                subject,
                testName,
                teacherName
        );

        school = school.trim();
        grade = grade.trim();
        subject = subject.trim();
        testName = testName.trim();
        semester = semester.trim();
        teacherName = teacherName.trim();

        /*
         * 2. 查询当前考试的所有学生记录。
         *
         * Mapper需要保证：如果出现同名考试，
         * 查询createTime最新的一次。
         */
        List<M_Examination> currentExamRecords =
                m_ExaminationMapper
                        .selectCurrentClassExamRecords(
                                school,
                                grade,
                                className,
                                subject,
                                testName,
                                semester
                        );

        if (currentExamRecords == null
                || currentExamRecords.isEmpty()) {

            throw new IllegalArgumentException(
                    "没有查询到当前考试数据"
            );
        }

        // 当前考试日期
        LocalDateTime currentCreateTime =
                currentExamRecords.get(0)
                        .getCreateTime();

        if (currentCreateTime == null) {
            throw new IllegalArgumentException(
                    "当前考试没有设置createTime"
            );
        }

        /*
         * status为正常的所有学生。
         *
         * 实考人数只根据status判断，
         * 即使某个正常学生分数为空，也计入实考人数。
         */
        List<M_Examination> normalStudents =
                currentExamRecords.stream()
                        .filter(this::isNormal)
                        .collect(Collectors.toList());

        /*
         * status正常且score不为空。
         *
         * 平均分、最高分、最低分、
         * 分数段和等级排名使用这些数据。
         */
        List<M_Examination> normalScoredStudents =
                normalStudents.stream()
                        .filter(record ->
                                record.getScore() != null
                        )
                        .collect(Collectors.toList());

        M_QueryClassExaminationVO result =
                new M_QueryClassExaminationVO();

        // 3. 当前考试名称
        result.setExamtName(semester + "/" + testName);
        result.setGrade(grade);
        result.setClassName(className);

        // 箱体图仅统计正常参加考试且成绩不为空的学生
        populateBoxPlotStatistics(
                result,
                normalScoredStudents
        );

        // 4. 实考、缺考、免考人数
        result.setActualTestNum(
                normalStudents.size()
        );


        List<M_StudentInfo> mStudentInfoList = m_StudentInfoMapper.getStudentInfoListBySchoolAndGradeClassName(school, grade, className);
        Integer studentCount = mStudentInfoList.size();

        int exemptedTestNum = countByStatus(
                currentExamRecords,
                "免考"
        );
        result.setExemptedTestNum(exemptedTestNum);
        result.setAbsentTestNum(
                Math.max(
                        studentCount
                                - normalStudents.size()
                                - exemptedTestNum,
                        0
                )
        );

        // 5. 当前班级平均分、最高分、最低分
        result.setAverageScore(
                calculateAverage(
                        normalScoredStudents
                )
        );

        result.setMaxScore(
                calculateMax(
                        normalScoredStudents
                )
        );

        result.setMinScore(
                calculateMin(
                        normalScoredStudents
                )
        );

        // 6. 分数段人数
        result.setDistinctionNum(
                countScore(
                        normalScoredStudents,
                        score ->
                                score.compareTo(
                                        new BigDecimal("90")
                                ) >= 0
                                        &&
                                        score.compareTo(
                                                new BigDecimal("100")
                                        ) <= 0
                )
        );

        result.setAboveAverageNum(
                countScore(
                        normalScoredStudents,
                        score ->
                                score.compareTo(
                                        new BigDecimal("80")
                                ) >= 0
                                        &&
                                        score.compareTo(
                                                new BigDecimal("90")
                                        ) < 0
                )
        );

        result.setAverageNum(
                countScore(
                        normalScoredStudents,
                        score ->
                                score.compareTo(
                                        new BigDecimal("60")
                                ) >= 0
                                        &&
                                        score.compareTo(
                                                new BigDecimal("80")
                                        ) < 0
                )
        );

        result.setBelowAverageNum(
                countScore(
                        normalScoredStudents,
                        score ->
                                score.compareTo(
                                        new BigDecimal("60")
                                ) < 0
                )
        );

        /*
         * 重点关注人数会与不达标人数重叠。
         *
         * 例如30分学生：
         * BelowAverageNum会统计
         * WatchListNum也会统计
         */
        result.setWatchListNum(
                countScore(
                        normalScoredStudents,
                        score ->
                                score.compareTo(
                                        new BigDecimal("40")
                                ) < 0
                )
        );

        /*
         * 7. 查询全年级本次考试数据。
         *
         * 不传className，查询相同学校、年级、
         * 学科、考试名称和考试日期的全部班级。
         */
        List<M_Examination> gradeExamRecords =
                m_ExaminationMapper
                        .selectGradeExamRecords(
                                school,
                                grade,
                                subject,
                                testName,
                                semester
                        );

        if (gradeExamRecords == null) {
            gradeExamRecords =
                    new ArrayList<>();
        }

        // 年级中status正常且分数不为空的数据
        List<M_Examination> gradeNormalStudents =
                gradeExamRecords.stream()
                        .filter(this::isNormal)
                        .filter(record ->
                                record.getScore() != null
                        )
                        .collect(Collectors.toList());

        // 8. 计算年级平均分
        result.setGrageAverage(
                calculateAverage(
                        gradeNormalStudents
                )
        );

        /*
         * 9. 查询等级配置。
         *
         * 优先查询当前教师配置。
         * 如果当前教师没有配置，则查询“教学处”配置。
         */
        List<BigDecimal> rankBoundaries =
                loadRankBoundaries(
                        school,
                        teacherName
                );

        /*
         * 10. 计算当前班级学生在全年级中的等级人数。
         *
         * 返回顺序：
         * [A+人数, A人数, B人数, C人数, D人数]
         */
        int[] rankCounts =
                calculateRankCounts(
                        normalScoredStudents,
                        gradeNormalStudents,
                        rankBoundaries
                );

        result.setRankAPlus(rankCounts[0]);
        result.setRankA(rankCounts[1]);
        result.setRankB(rankCounts[2]);
        result.setRankC(rankCounts[3]);
        result.setRankD(rankCounts[4]);

        /*
         * 11. 查询上一次考试。
         *
         * 同学校、同年级、同班级、同学科，
         * createTime小于当前考试日期，
         * 按createTime倒序取第一场。
         */
        M_Examination previousExam =
                m_ExaminationMapper
                        .selectPreviousExamInfo(
                                school,
                                grade,
                                className,
                                subject,
                                currentCreateTime
                        );

        if (previousExam != null
                && StringUtils.hasText(
                previousExam.getTestName()
        )
                && previousExam.getCreateTime() != null) {

            // 设置上次考试名称
            result.setPriviousExamName(
                    previousExam.getSemester() + "/" + previousExam.getTestName()
            );

            /*
             * 查询上次考试的班级学生记录。
             */
            List<M_Examination> previousRecords =
                    m_ExaminationMapper
                            .selectCurrentClassExamRecords(
                                    school,
                                    grade,
                                    className,
                                    subject,
                                    previousExam.getTestName(),
                                    previousExam.getSemester()
                            );

            if (previousRecords != null
                    && !previousRecords.isEmpty()) {

                /*
                 * 上次考试同样只统计：
                 * status正常且score不为空的学生。
                 */
                List<M_Examination>
                        previousNormalStudents =
                        previousRecords.stream()
                                .filter(this::isNormal)
                                .filter(record ->
                                        record.getScore()
                                                != null
                                )
                                .collect(
                                        Collectors.toList()
                                );

                result.setPrivisousAverageScore(
                        calculateAverage(
                                previousNormalStudents
                        )
                );
            }
        }

        /*
         * 没有上一次考试时：
         *
         * priviousTestName保持null
         * PrivisousAverageScore保持null
         */

        return result;
    }

    /**
     * 查询所有的考试名称
     *
     * @param school
     * @return
     */
    @Override
    public M_ExamNameListVO queryExamName(String school, String teacherName,String grade ,Integer className,String subject) {
        //首先查询的所有的预设考试名称
        String showItem = "正常";
        List<M_ExamName> m_examNameListTeacher = m_ExamNameMapper.getBySchool(school, showItem, teacherName);
        String adminName = "教学处";
        List<M_ExamName> m_examNameListAdmin = m_ExamNameMapper.getBySchool(school, showItem, adminName);

        // 合并，admin 在前
        List<M_ExamName> m_examNameList = new ArrayList<>();
        if (m_examNameListAdmin != null) {
            m_examNameList.addAll(m_examNameListAdmin);
        }
        if (m_examNameListTeacher != null) {
            m_examNameList.addAll(m_examNameListTeacher);
        }

        List<String> preExamNameList = new ArrayList<>();
        if (m_examNameList != null && !m_examNameList.isEmpty()) {
            for (M_ExamName m_examName : m_examNameList) {
                String preExamName = m_examName.getSemester() + "/" + m_examName.getTestName();
                preExamNameList.add(preExamName);
            }
        }
        M_ExamNameListVO result = new M_ExamNameListVO();
        result.setPreSetExamNameList(preExamNameList);

        List<String> historyExamNameList = new ArrayList<>();
        //查询到所有当前教师
        List<M_Examination> examinationListHistory = m_ExaminationMapper.getNameBySchool(school, teacherName,grade,className,subject);
        if (examinationListHistory != null && !examinationListHistory.isEmpty()) {
            for (M_Examination examination : examinationListHistory) {
                String historyExamName = examination.getSemester() + "/" + examination.getTestName();
                historyExamNameList.add(historyExamName);
            }
        }

        M_ExamNameListVO mExamNameListVO = new M_ExamNameListVO();
        mExamNameListVO.setHistoryExamNameList(historyExamNameList);
        mExamNameListVO.setPreSetExamNameList(preExamNameList);


        return mExamNameListVO;
    }


    @Override
    public List<M_StudentExamScoreVO> queryAllStudentScore(String school, String grade, Integer className, String subject, String examName) {
        if (!StringUtils.hasText(school)
                || !StringUtils.hasText(grade)
                || className == null
                || !StringUtils.hasText(subject)) {
            throw new IllegalArgumentException(
                    "学校、年级、班级和学科不能为空"
            );
        }
        if (!StringUtils.hasText(examName)) {
            throw new IllegalArgumentException("考试名称不能为空");
        }

        String[] examNameParts = examName.split("/", -1);
        if (examNameParts.length != 2
                || !StringUtils.hasText(examNameParts[0])
                || !StringUtils.hasText(examNameParts[1])) {
            throw new IllegalArgumentException(
                    "考试名称格式应为“学期/考试名称”"
            );
        }

        school = school.trim();
        grade = grade.trim();
        subject = subject.trim();
        String semester = examNameParts[0].trim();
        String testName = examNameParts[1].trim();

        List<M_StudentExamScoreVO> queriedScores =
                m_ExaminationMapper.getAllStudentRecord(
                        school,
                        grade,
                        className,
                        subject,
                        semester,
                        testName
                );
        List<M_StudentExamScoreVO> studentScores = queriedScores == null
                ? new ArrayList<>()
                : new ArrayList<>(queriedScores);

        List<M_Examination> currentRecords =
                m_ExaminationMapper.selectCurrentClassExamRecords(
                        school,
                        grade,
                        className,
                        subject,
                        testName,
                        semester
                );

        if (currentRecords == null || currentRecords.isEmpty()) {
            throw new IllegalArgumentException(
                    "没有查询到当前考试数据"
            );
        }

        LocalDateTime currentCreateTime = currentRecords.stream()
                .filter(Objects::nonNull)
                .map(M_Examination::getCreateTime)
                .filter(Objects::nonNull)
                .max(LocalDateTime::compareTo)
                .orElseThrow(() -> new IllegalArgumentException(
                        "当前考试没有设置createTime"
                ));

        List<M_Examination> currentNormalScoredStudents =
                currentRecords.stream()
                        .filter(this::isNormal)
                        .filter(record -> record.getScore() != null)
                        .collect(Collectors.toList());
        BigDecimal averageScore = calculateAverage(
                currentNormalScoredStudents
        );
        Map<String, Integer> currentRankByStudent =
                calculateClassRankLevels(
                        currentNormalScoredStudents
                );

        // 把在籍但没有考试记录的学生补为缺考；查询接口不写数据库。
        List<String> allStudentNames =
                m_StudentInfoMapper.getStudentNamesBySchoolAndClassName(
                        school,
                        grade,
                        className
                );
        if (allStudentNames == null) {
            allStudentNames = Collections.emptyList();
        }

        Set<String> existingStudentNames = studentScores.stream()
                .filter(Objects::nonNull)
                .map(M_StudentExamScoreVO::getStudentName)
                .filter(StringUtils::hasText)
                .map(String::trim)
                .collect(Collectors.toSet());
        String currentHide = studentScores.stream()
                .filter(Objects::nonNull)
                .map(M_StudentExamScoreVO::getHide)
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);

        allStudentNames.stream()
                .filter(StringUtils::hasText)
                .map(String::trim)
                .filter(name -> !existingStudentNames.contains(name))
                .forEach(name -> {
                    M_StudentExamScoreVO absentStudent =
                            new M_StudentExamScoreVO();
                    absentStudent.setStudentName(name);
                    absentStudent.setStatus("缺考");
                    absentStudent.setHide(currentHide);
                    studentScores.add(absentStudent);
                    existingStudentNames.add(name);
                });

        String previousExamName = null;
        BigDecimal previousAverageScore = null;
        Map<String, BigDecimal> previousScoreByStudent =
                new HashMap<>();
        Map<String, Integer> previousRankByStudent =
                new HashMap<>();

        M_Examination previousExam =
                m_ExaminationMapper.selectPreviousExamInfo(
                        school,
                        grade,
                        className,
                        subject,
                        currentCreateTime
                );

        if (previousExam != null
                && StringUtils.hasText(previousExam.getSemester())
                && StringUtils.hasText(previousExam.getTestName())) {
            previousExamName = previousExam.getSemester().trim()
                    + "/"
                    + previousExam.getTestName().trim();

            List<M_Examination> previousRecords =
                    m_ExaminationMapper.selectCurrentClassExamRecords(
                            school,
                            grade,
                            className,
                            subject,
                            previousExam.getTestName().trim(),
                            previousExam.getSemester().trim()
                    );

            if (previousRecords != null) {
                List<M_Examination> previousNormalScoredStudents =
                        previousRecords.stream()
                                .filter(this::isNormal)
                                .filter(record ->
                                        record.getScore() != null
                                )
                                .collect(Collectors.toList());
                previousAverageScore = calculateAverage(
                        previousNormalScoredStudents
                );

                previousNormalScoredStudents.forEach(record -> {
                    if (StringUtils.hasText(record.getStudentName())) {
                        previousScoreByStudent.put(
                                record.getStudentName().trim(),
                                record.getScore()
                        );
                    }
                });
                previousRankByStudent.putAll(
                        calculateClassRankLevels(
                                previousNormalScoredStudents
                        )
                );
            }
        }

        for (M_StudentExamScoreVO studentScore : studentScores) {
            if (studentScore == null) {
                continue;
            }
            studentScore.setAverageScore(averageScore);
            studentScore.setCurrentExamName(
                    semester + "/" + testName
            );
            studentScore.setPreviousAverageScore(
                    previousAverageScore
            );
            studentScore.setPreviousExamName(previousExamName);

            if (StringUtils.hasText(studentScore.getStudentName())) {
                String studentName =
                        studentScore.getStudentName().trim();
                studentScore.setPreviousScore(
                        previousScoreByStudent.get(
                                studentName
                        )
                );
                studentScore.setRankLevel(
                        currentRankByStudent.get(studentName)
                );
                studentScore.setPreviousRankLevel(
                        previousRankByStudent.get(studentName)
                );
            }
        }

        return studentScores;
    }

    /**
     * 修改某次考试某个考生的成绩
     * @param mUpdateExamStudentScoreDTO
     * @return
     */
    @Override
    public String updateSingleScore(M_UpdateExamStudentScoreDTO mUpdateExamStudentScoreDTO) {




        //首先判断，这个学生的status是什么？是正常还是缺考或者是免考
            m_ExaminationMapper.updateSingleRecord(mUpdateExamStudentScoreDTO.getStatus(),mUpdateExamStudentScoreDTO.getId(),mUpdateExamStudentScoreDTO.getScore());
            addExamEditRecord(mUpdateExamStudentScoreDTO.getId(),mUpdateExamStudentScoreDTO.getTeahcerName(),"修改","examination",mUpdateExamStudentScoreDTO.getGrade()+mUpdateExamStudentScoreDTO.getClassName(),"修改了<"+mUpdateExamStudentScoreDTO.getStudentName()+">的+"+mUpdateExamStudentScoreDTO.getExamName()+"分数为"+mUpdateExamStudentScoreDTO.getScore()+"《"+mUpdateExamStudentScoreDTO.getStatus()+"》",mUpdateExamStudentScoreDTO.getSchool(),null);


        return "修改"+mUpdateExamStudentScoreDTO.getStudentName()+"成绩成功";
    }


    /**
     * 删除某次考试记录
     * @param mExamDeleteDTO
     * @return
     */
    @Override
    public String deleteExamRecord(M_ExamDeleteDTO mExamDeleteDTO) {
        String semester = mExamDeleteDTO.getExamName().split("/")[0];
        String testName  = mExamDeleteDTO.getExamName().split("/")[1];
        mExamDeleteDTO.setTestName(testName);
        mExamDeleteDTO.setSemester(semester);

        m_ExaminationMapper.deleteExamRecord(mExamDeleteDTO);


        M_ExamEditRecord mExamEditRecord = new M_ExamEditRecord();
        mExamEditRecord.setActionWay("删除");
        mExamEditRecord.setUpdateName(mExamDeleteDTO.getTeacherName());
        mExamEditRecord.setSchool(mExamDeleteDTO.getSchool());
        mExamEditRecord.setDatabaseName("examination");
        mExamEditRecord.setContentInfo(mExamDeleteDTO.getExamName());
        mExamEditRecord.setClassOrName(mExamDeleteDTO.getGrade()+mExamDeleteDTO.getClassName());
        mExamEditRecord.setCreateTime(LocalDateTime.now());

        m_ExamEditRecordMapper.insertSingleRecord(mExamEditRecord);
        return "删除"+mExamDeleteDTO.getSubject()+ "-"+mExamDeleteDTO.getExamName() +"考试数据成功";
    }


    /**
     * 新增考试名称
     * @param mExamName
     * @return
     */
    @Override
    public String insertExamName(M_ExamName mExamName) {
        mExamName.setCreateTime(LocalDateTime.now());
        String adminName = "教学处";
        mExamName.setAdminName(adminName);
        Integer countNum = m_ExamNameMapper.confirmName(mExamName);
        if (countNum > 0) {
            if(mExamName.getCreateName().equals(adminName)){

               m_ExamNameMapper.deleteByExamName(mExamName.getSchool(),mExamName.getSemester(),mExamName.getTestName());

            }else {
            return "该考试名称已被创建";}
        }
        m_ExamNameMapper.insertSingeRecord(mExamName);
        return "新增"+ mExamName.getSemester() +"/"+mExamName.getTestName()+"考试成功";
    }


    /**
     * 删除某次考试名称
     * @param mExamNameDeleteDTO
     * @return
     */
    @Override
    public String deleteExamName(M_ExamNameDeleteDTO mExamNameDeleteDTO) {

        M_ExamName mExamName = m_ExamNameMapper.getById(mExamNameDeleteDTO.getId());
        M_ExamEditRecord mExamEditRecord = new M_ExamEditRecord();
        if(mExamNameDeleteDTO.getRole().equals("教师")){
            mExamEditRecord.setUpdateName(mExamNameDeleteDTO.getTeacherName());
        }
        if(mExamNameDeleteDTO.getRole().equals("教学处") || mExamNameDeleteDTO.getRole().equals("管理员")){
            mExamEditRecord.setUpdateName(mExamNameDeleteDTO.getRole()+"/"+mExamNameDeleteDTO.getTeacherName());
        }

        mExamEditRecord.setSchool(mExamName.getSchool());
        mExamEditRecord.setDatabaseName("examName");
        mExamEditRecord.setDatabaseId(mExamNameDeleteDTO.getId());
        mExamEditRecord.setActionWay("删除");
        mExamEditRecord.setCreateTime(LocalDateTime.now());
        mExamEditRecord.setContentInfo(mExamName.getSemester()+"/"+ mExamName.getTestName());

        m_ExamEditRecordMapper.insertSingleRecord(mExamEditRecord);

        m_ExamNameMapper.deleteExamName(mExamNameDeleteDTO);


        return "删除" +mExamName.getSemester()+"/"+mExamName.getTestName();
    }


    /**
     * 在默认数据库中新增考试学年学期信息
     * @param mSemesterInfoDTO
     * @return
     */
    @Override
    public String insertSemester(M_SemesterInfoDTO mSemesterInfoDTO) {
        M_DefaultConfig mDefaultConfig = new M_DefaultConfig();
        mDefaultConfig.setSchool(mSemesterInfoDTO.getSchool());
        mDefaultConfig.setTextConfig(mSemesterInfoDTO.getSemester());
        mDefaultConfig.setInfoName("考试学年学期");
        mDefaultConfig.setUserName(mSemesterInfoDTO.getTeacherName());
        m_DefaultConfigMapper.insertDefaultConfig(mDefaultConfig);

        return "新增"+mDefaultConfig.getTextConfig()+"考试学年信息成功";
    }

    /**
     * 查询学年学期信息
     * @param school
     * @return
     */
    @Override
    public List<String> querySemester(String school) {

        String infoName= "学年学期信息";
        List<String> semesterList = m_DefaultConfigMapper.getAllSemeter(infoName,school);

        return semesterList;
    }


    /**
     * 删除学年学期信息
     * @param school
     * @param semester
     * @return
     */
    @Override
    public String deleteSemester(String school, String semester) {
        String infoName = "学年学期信息";
        m_DefaultConfigMapper.deleteByInfoNameAndTextConfig(infoName,semester,school);
        return "删除"+ semester +"/"+school;
    }


    /**
     * 查询年级对比数据
     * @param queryExaminationDTO
     * @return
     */
    @Override
    public List<M_QueryClassExaminationVO> queryGradeCompare(M_QueryExaminationDTO queryExaminationDTO) {
        //首先拿到当前年级所有的班级，拿到班级，再拿到所有班级的考试概览数据
        List<String> classNameList = gradeClassNumMapper.getClassNameBySchoolGrade(
                queryExaminationDTO.getSchool(),
                queryExaminationDTO.getGrade()
        );

        List<Integer> classNumList = new ArrayList<>();

        if (CollectionUtils.isNotEmpty(classNameList)) {
            classNumList = classNameList.stream()
                    .map(className -> className.replaceAll("[^0-9]", ""))
                    .filter(s -> !s.isEmpty())
                    .map(Integer::parseInt)
                    .collect(Collectors.toList());
        }

        if(classNumList.size()>0){
            List<M_QueryClassExaminationVO> resultList = new ArrayList<>();
            for (Integer classNum : classNumList) {
                queryExaminationDTO.setClassName(classNum);



                //先查询这个班级是否有该项考试
                Integer countExam = m_ExaminationMapper.countExistingExam(
                        queryExaminationDTO.getSchool(),
                        queryExaminationDTO.getGrade(),
                        classNum,
                        queryExaminationDTO.getSubject(),
                        queryExaminationDTO.getExamName().split("/")[0],
                        queryExaminationDTO.getExamName().split("/")[1]
                );
                if(countExam==0){
                    continue;
                }

                M_QueryClassExaminationVO classExaminationVO = queryClassExamination(queryExaminationDTO);
                resultList.add(classExaminationVO);
            }
            return resultList;
        }else {
            return null;
        }

    }

    /**
     * 批量上传成绩前确认是否已有成绩
     * @param school
     * @param grade
     * @param className
     * @param subject
     * @param teacherName
     * @return
     */
    @Override
    public String confirmUpload(String school, String grade, Integer className, String subject, String examName,String teacherName) {
        String semester = examName.split("/")[0];
        String testName = examName.split("/")[1];
        Integer countNum = m_ExaminationMapper.countExistingExam(school,grade,className,subject,semester,testName);
        if(countNum>0){
            return grade+className+"班"+ testName+"已存在"+countNum+"条记录，是否覆盖？";
        }else {
            return "没有已存在的记录";
        }

    }

    @Override
    public M_QueryClassExaminationVO queryGradeExamination(M_QueryExaminationDTO queryExaminationDTO) {
        if (queryExaminationDTO == null) {
            throw new IllegalArgumentException("查询参数不能为空");
        }

        String school = queryExaminationDTO.getSchool();
        String grade = queryExaminationDTO.getGrade();
        String subject = queryExaminationDTO.getSubject();
        String examName = queryExaminationDTO.getExamName();
        String teacherName = queryExaminationDTO.getTeacherName();

        if (!StringUtils.hasText(examName)) {
            throw new IllegalArgumentException("考试名称不能为空");
        }

        String[] examNameParts = examName.split("/", -1);
        if (examNameParts.length != 2
                || !StringUtils.hasText(examNameParts[0])
                || !StringUtils.hasText(examNameParts[1])) {
            throw new IllegalArgumentException(
                    "考试名称格式应为“学期/考试名称”"
            );
        }

        String semester = examNameParts[0].trim();
        String testName = examNameParts[1].trim();

        // 年级概览不需要班级参数，复用其余查询参数校验。
        validateRequest(
                school,
                grade,
                null,
                subject,
                testName,
                teacherName
        );

        school = school.trim();
        grade = grade.trim();
        subject = subject.trim();
        teacherName = teacherName.trim();

        List<M_Examination> gradeExamRecords =
                m_ExaminationMapper.selectGradeExamRecords(
                        school,
                        grade,
                        subject,
                        testName,
                        semester
                );

        if (gradeExamRecords == null
                || gradeExamRecords.isEmpty()) {
            throw new IllegalArgumentException(
                    "没有查询到当前年级考试数据"
            );
        }

        LocalDateTime currentCreateTime = gradeExamRecords.stream()
                .filter(Objects::nonNull)
                .map(M_Examination::getCreateTime)
                .filter(Objects::nonNull)
                .max(LocalDateTime::compareTo)
                .orElseThrow(() -> new IllegalArgumentException(
                        "当前考试没有设置createTime"
                ));

        List<M_Examination> normalStudents = gradeExamRecords.stream()
                .filter(this::isNormal)
                .collect(Collectors.toList());

        List<M_Examination> normalScoredStudents = normalStudents.stream()
                .filter(record -> record.getScore() != null)
                .collect(Collectors.toList());

        M_QueryClassExaminationVO result =
                new M_QueryClassExaminationVO();

        result.setExamtName(semester + "/" + testName);
        result.setGrade(grade);
        result.setClassName(null);

        List<M_StudentInfo> gradeStudents =
                m_StudentInfoMapper.getStudentInfoListBySchoolAndGrade(
                        school,
                        grade
                );
        int gradeStudentCount = gradeStudents == null
                ? 0
                : gradeStudents.size();

        int exemptedTestNum = countByStatus(
                gradeExamRecords,
                "免考"
        );
        result.setActualTestNum(normalStudents.size());
        result.setExemptedTestNum(exemptedTestNum);
        result.setAbsentTestNum(
                Math.max(
                        gradeStudentCount
                                - normalStudents.size()
                                - exemptedTestNum,
                        0
                )
        );

        result.setAverageScore(
                calculateAverage(normalScoredStudents)
        );
        result.setMaxScore(
                calculateMax(normalScoredStudents)
        );
        result.setMinScore(
                calculateMin(normalScoredStudents)
        );

        result.setDistinctionNum(countScore(
                normalScoredStudents,
                score -> score.compareTo(new BigDecimal("90")) >= 0
                        && score.compareTo(new BigDecimal("100")) <= 0
        ));
        result.setAboveAverageNum(countScore(
                normalScoredStudents,
                score -> score.compareTo(new BigDecimal("80")) >= 0
                        && score.compareTo(new BigDecimal("90")) < 0
        ));
        result.setAverageNum(countScore(
                normalScoredStudents,
                score -> score.compareTo(new BigDecimal("60")) >= 0
                        && score.compareTo(new BigDecimal("80")) < 0
        ));
        result.setBelowAverageNum(countScore(
                normalScoredStudents,
                score -> score.compareTo(new BigDecimal("60")) < 0
        ));
        result.setWatchListNum(countScore(
                normalScoredStudents,
                score -> score.compareTo(new BigDecimal("40")) < 0
        ));

        // 年级概览本身就是年级总体，因此两个平均分字段相同。
        result.setGrageAverage(result.getAverageScore());

        List<BigDecimal> rankBoundaries =
                loadRankBoundaries(school, teacherName);
        int[] rankCounts = calculateRankCounts(
                normalScoredStudents,
                normalScoredStudents,
                rankBoundaries
        );
        result.setRankAPlus(rankCounts[0]);
        result.setRankA(rankCounts[1]);
        result.setRankB(rankCounts[2]);
        result.setRankC(rankCounts[3]);
        result.setRankD(rankCounts[4]);

        populateBoxPlotStatistics(
                result,
                normalScoredStudents
        );

        M_Examination previousExam =
                m_ExaminationMapper.selectPreviousGradeExamInfo(
                        school,
                        grade,
                        subject,
                        semester,
                        testName,
                        currentCreateTime
                );

        if (previousExam != null
                && StringUtils.hasText(previousExam.getTestName())
                && StringUtils.hasText(previousExam.getSemester())) {
            result.setPriviousExamName(
                    previousExam.getSemester()
                            + "/"
                            + previousExam.getTestName()
            );

            List<M_Examination> previousRecords =
                    m_ExaminationMapper.selectGradeExamRecords(
                            school,
                            grade,
                            subject,
                            previousExam.getTestName(),
                            previousExam.getSemester()
                    );

            if (previousRecords != null) {
                List<M_Examination> previousNormalScoredStudents =
                        previousRecords.stream()
                                .filter(this::isNormal)
                                .filter(record ->
                                        record.getScore() != null
                                )
                                .collect(Collectors.toList());

                result.setPrivisousAverageScore(
                        calculateAverage(
                                previousNormalScoredStudents
                        )
                );
            }
        }

        return result;
    }

    @Override
    public List<M_QueryClassExaminationVO> queryHistoryExam(M_QueryExaminationDTO queryExaminationDTO) {
        if(queryExaminationDTO.getHistoryExamNameList() == null || queryExaminationDTO.getHistoryExamNameList().isEmpty()){
            throw new IllegalArgumentException("历史考试名称列表不能为空");
        }
        List<M_QueryClassExaminationVO> resultList = new ArrayList<>();
        for (String examName : queryExaminationDTO.getHistoryExamNameList()) {
            if (!StringUtils.hasText(examName)) {
                throw new IllegalArgumentException("历史考试名称不能为空");
            }
            queryExaminationDTO.setExamName(examName);
            String semester = examName.split("/")[0];
            String testName = examName.split("/")[1];
            List<Integer> classNameList = m_ExaminationMapper.getClassName(
                    queryExaminationDTO.getSchool(),
                    queryExaminationDTO.getGrade(),
                    queryExaminationDTO.getSubject(),
                    semester,
                    testName
            );
            if(classNameList == null || classNameList.isEmpty()){
                continue;
            }
            for (Integer className : classNameList) {
                queryExaminationDTO.setClassName(className);
           M_QueryClassExaminationVO result =  queryClassExamination(queryExaminationDTO);
           if(result != null){
               resultList.add(result);}
           }
            M_QueryClassExaminationVO gradeResult = queryGradeExamination(queryExaminationDTO);
            if(gradeResult != null){
                resultList.add(gradeResult);}




        }
        return resultList;



    }

    /**
     * 查询一名学生所有考试成绩
     * @param queryStudentALlExamDTO
     * @return
     */
    @Override
    @Transactional(readOnly = true)
    public List<M_StudentExamScoreVO> queryStudentAllExam(M_QueryStudentAllExamDTO queryStudentALlExamDTO) {
        if (queryStudentALlExamDTO == null) {
            throw new IllegalArgumentException("查询参数不能为空");
        }

        String school = queryStudentALlExamDTO.getSchool();
        String grade = queryStudentALlExamDTO.getGrade();
        Integer className = queryStudentALlExamDTO.getClassName();
        String subject = queryStudentALlExamDTO.getSubject();
        String studentName = queryStudentALlExamDTO.getStudentName();

        if (!StringUtils.hasText(school)
                || !StringUtils.hasText(grade)
                || className == null
                || !StringUtils.hasText(subject)
                || !StringUtils.hasText(studentName)) {
            throw new IllegalArgumentException(
                    "学校、年级、班级、学科和学生姓名不能为空"
            );
        }

        school = school.trim();
        grade = grade.trim();
        subject = subject.trim();
        String normalizedStudentName = studentName.trim();

        List<M_Examination> historyRecords =
                m_ExaminationMapper.selectAllClassExamHistoryRecords(
                        school,
                        grade,
                        className,
                        subject
                );

        if (historyRecords == null || historyRecords.isEmpty()) {
            return new ArrayList<>();
        }

        /*
         * 同一场考试的全部学生记录归为一组。
         * LinkedHashMap先保留数据库顺序，随后仍按每组最新createTime
         * 显式排序，保证最早考试在前、最近考试在最后。
         */
        Map<String, List<M_Examination>> recordsByExam =
                new LinkedHashMap<>();

        historyRecords.stream()
                .filter(Objects::nonNull)
                .filter(record ->
                        StringUtils.hasText(record.getSemester())
                                && StringUtils.hasText(
                                record.getTestName()
                        )
                )
                .forEach(record -> {
                    String examName = record.getSemester().trim()
                            + "/"
                            + record.getTestName().trim();
                    recordsByExam.computeIfAbsent(
                            examName,
                            key -> new ArrayList<>()
                    ).add(record);
                });

        List<Map.Entry<String, List<M_Examination>>> examGroups =
                new ArrayList<>(recordsByExam.entrySet());
        examGroups.sort((left, right) -> compareExamCreateTime(
                left.getValue(),
                right.getValue()
        ));

        List<M_StudentExamScoreVO> resultList =
                new ArrayList<>();

        String previousExamName = null;
        BigDecimal previousAverageScore = null;
        BigDecimal previousStudentScore = null;
        Integer previousRankLevel = null;

        for (Map.Entry<String, List<M_Examination>> examGroup
                : examGroups) {
            String currentExamName = examGroup.getKey();
            List<M_Examination> currentRecords =
                    examGroup.getValue();

            List<M_Examination> normalScoredStudents =
                    currentRecords.stream()
                            .filter(this::isNormal)
                            .filter(record ->
                                    record.getScore() != null
                            )
                            .collect(Collectors.toList());

            BigDecimal averageScore = calculateAverage(
                    normalScoredStudents
            );
            Map<String, Integer> rankByStudent =
                    calculateClassRankLevels(
                            normalScoredStudents
                    );

            M_Examination studentRecord = currentRecords.stream()
                    .filter(record ->
                            StringUtils.hasText(
                                    record.getStudentName()
                            )
                                    && normalizedStudentName.equals(
                                    record.getStudentName().trim()
                            )
                    )
                    .reduce((first, second) -> second)
                    .orElse(null);

            M_StudentExamScoreVO result =
                    buildStudentExamScoreVO(
                            normalizedStudentName,
                            studentRecord,
                            currentRecords
                    );
            result.setCurrentExamName(currentExamName);
            result.setAverageScore(averageScore);
            result.setRankLevel(
                    rankByStudent.get(normalizedStudentName)
            );
            result.setPreviousExamName(previousExamName);
            result.setPreviousAverageScore(
                    previousAverageScore
            );
            result.setPreviousScore(previousStudentScore);
            result.setPreviousRankLevel(previousRankLevel);
            resultList.add(result);

            // 当前考试成为下一轮的“上一次考试”。
            previousExamName = currentExamName;
            previousAverageScore = averageScore;

            if (isNormal(studentRecord)
                    && studentRecord.getScore() != null) {
                previousStudentScore = studentRecord.getScore();
                previousRankLevel = rankByStudent.get(
                        normalizedStudentName
                );
            } else {
                previousStudentScore = null;
                previousRankLevel = null;
            }
        }

        return resultList;
    }

    @Override
    public String updateExamHide(M_UpdateExamHideDTO mUpdateExamHideDTO) {
       //没有没有学生姓名，那么就更新整个年级的，如果有学生的姓名，就更新该学生的发布状态
        String semester = mUpdateExamHideDTO.getExamName().split("/")[0];
        String testName = mUpdateExamHideDTO.getExamName().split("/")[1];
        mUpdateExamHideDTO.setSemester(semester);
        mUpdateExamHideDTO.setTestName(testName);
        if(!StringUtils.hasText(mUpdateExamHideDTO.getStudentName())){


            m_ExaminationMapper.updateExamHideByClass(mUpdateExamHideDTO
            );
            addExamEditRecord(null, mUpdateExamHideDTO.getTeacherName(),"修改","examination",mUpdateExamHideDTO.getGrade()+mUpdateExamHideDTO.getClassName(),"修改了全班"+mUpdateExamHideDTO.getExamName()+"考试的发布状态"+"变为"+mUpdateExamHideDTO.getHide(),mUpdateExamHideDTO.getSchool(),null);

            return "更新班级考试发布状态成功";
        }
        m_ExaminationMapper.updateExamHideByStudent(mUpdateExamHideDTO);

        addExamEditRecord(null, mUpdateExamHideDTO.getTeacherName(),"修改","examination",mUpdateExamHideDTO.getGrade()+mUpdateExamHideDTO.getClassName(),"修改了<"+mUpdateExamHideDTO.getStudentName()+">"+mUpdateExamHideDTO.getExamName()+"考试的发布状态"+"变为"+mUpdateExamHideDTO.getHide(),mUpdateExamHideDTO.getSchool(),null);

        return "更新"+  mUpdateExamHideDTO.getStudentName()+"考试发布状态成功";
    }

    /**
     * 按每场考试中最新的createTime进行升序比较。
     */
    private int compareExamCreateTime(
            List<M_Examination> leftRecords,
            List<M_Examination> rightRecords
    ) {
        LocalDateTime leftTime = latestCreateTime(leftRecords);
        LocalDateTime rightTime = latestCreateTime(rightRecords);

        if (leftTime == null && rightTime == null) {
            return 0;
        }
        if (leftTime == null) {
            return -1;
        }
        if (rightTime == null) {
            return 1;
        }
        return leftTime.compareTo(rightTime);
    }

    private LocalDateTime latestCreateTime(
            List<M_Examination> records
    ) {
        return records.stream()
                .filter(Objects::nonNull)
                .map(M_Examination::getCreateTime)
                .filter(Objects::nonNull)
                .max(LocalDateTime::compareTo)
                .orElse(null);
    }

    /**
     * 将学生在某场考试中的记录转换为返回对象。
     * 没有数据库记录时按缺考返回。
     */
    private M_StudentExamScoreVO buildStudentExamScoreVO(
            String studentName,
            M_Examination studentRecord,
            List<M_Examination> examRecords
    ) {
        M_StudentExamScoreVO result =
                new M_StudentExamScoreVO();
        result.setStudentName(studentName);

        if (studentRecord == null) {
            result.setStatus("缺考");
            examRecords.stream()
                    .filter(Objects::nonNull)
                    .map(M_Examination::getHide)
                    .filter(Objects::nonNull)
                    .findFirst()
                    .ifPresent(result::setHide);
            return result;
        }

        if (studentRecord.getId() != null) {
            result.setId(studentRecord.getId().intValue());
        }
        result.setScore(studentRecord.getScore());
        result.setHide(studentRecord.getHide());
        result.setStatus(studentRecord.getStatus());
        result.setImageUrl(studentRecord.getImageUrl());
        return result;
    }


    /**
     * 判断学生是否为正常考试。
     */
    private boolean isNormal(
            M_Examination examination
    ) {

        return examination != null
                && StringUtils.hasText(
                examination.getStatus()
        )
                && "正常".equals(
                examination
                        .getStatus()
                        .trim()
        );
    }

    /**
     * 根据status统计人数。
     */
    private int countByStatus(
            List<M_Examination> records,
            String targetStatus
    ) {

        if (records == null
                || records.isEmpty()) {
            return 0;
        }

        return (int) records.stream()
                .filter(record ->
                        record != null
                                &&
                                StringUtils.hasText(
                                        record.getStatus()
                                )
                                &&
                                targetStatus.equals(
                                        record
                                                .getStatus()
                                                .trim()
                                )
                )
                .count();
    }

    /**
     * 计算平均分，保留两位小数。
     */
    private BigDecimal calculateAverage(
            List<M_Examination> records
    ) {

        if (records == null
                || records.isEmpty()) {
            return null;
        }

        List<BigDecimal> scores =
                records.stream()
                        .filter(record ->
                                record != null
                                        &&
                                        record.getScore()
                                                != null
                        )
                        .map(
                                M_Examination::getScore
                        )
                        .collect(Collectors.toList());

        if (scores.isEmpty()) {
            return null;
        }

        BigDecimal sum =
                scores.stream()
                        .reduce(
                                BigDecimal.ZERO,
                                BigDecimal::add
                        );

        return sum.divide(
                BigDecimal.valueOf(
                        scores.size()
                ),
                2,
                RoundingMode.HALF_UP
        );
    }

    /**
     * 计算班级名次。同分学生名次相同，后续名次按人数顺延，
     * 例如成绩排名为1、2、2、4。
     */
    private Map<String, Integer> calculateClassRankLevels(
            List<M_Examination> records
    ) {

        if (records == null || records.isEmpty()) {
            return new HashMap<>();
        }

        List<M_Examination> rankedStudents = records.stream()
                .filter(this::isNormal)
                .filter(record -> record.getScore() != null)
                .filter(record ->
                        StringUtils.hasText(record.getStudentName())
                )
                .sorted(Comparator.comparing(
                        M_Examination::getScore,
                        Comparator.reverseOrder()
                ))
                .collect(Collectors.toList());

        Map<String, Integer> rankByStudent = new HashMap<>();
        BigDecimal previousScore = null;
        int currentRank = 0;

        for (int index = 0; index < rankedStudents.size(); index++) {
            M_Examination student = rankedStudents.get(index);

            if (previousScore == null
                    || student.getScore().compareTo(previousScore) != 0) {
                currentRank = index + 1;
                previousScore = student.getScore();
            }

            rankByStudent.put(
                    student.getStudentName().trim(),
                    currentRank
            );
        }

        return rankByStudent;
    }

    /**
     * 计算最高分。
     */
    private BigDecimal calculateMax(
            List<M_Examination> records
    ) {

        if (records == null
                || records.isEmpty()) {
            return null;
        }

        return records.stream()
                .filter(record ->
                        record != null
                                &&
                                record.getScore() != null
                )
                .map(M_Examination::getScore)
                .max(BigDecimal::compareTo)
                .orElse(null);
    }

    /**
     * 计算最低分。
     */
    private BigDecimal calculateMin(
            List<M_Examination> records
    ) {

        if (records == null
                || records.isEmpty()) {
            return null;
        }

        return records.stream()
                .filter(record ->
                        record != null
                                &&
                                record.getScore() != null
                )
                .map(M_Examination::getScore)
                .min(BigDecimal::compareTo)
                .orElse(null);
    }

    /**
     * 计算箱体图数据。
     *
     * 四分位数使用线性插值法：位置 = (样本数 - 1) * 百分位。
     * 上下须为1.5倍IQR范围内实际存在的最小值和最大值，
     * 范围外的成绩作为异常值返回。
     */
    void populateBoxPlotStatistics(
            M_QueryClassExaminationVO result,
            List<M_Examination> records
    ) {

        List<BigDecimal> sortedScores = records == null
                ? new ArrayList<>()
                : records.stream()
                .filter(Objects::nonNull)
                .map(M_Examination::getScore)
                .filter(Objects::nonNull)
                .sorted(BigDecimal::compareTo)
                .collect(Collectors.toList());

        if (sortedScores.isEmpty()) {
            result.setOutlierScores(new ArrayList<>());
            return;
        }

        BigDecimal q1 = calculatePercentile(
                sortedScores,
                new BigDecimal("0.25")
        );
        BigDecimal median = calculatePercentile(
                sortedScores,
                new BigDecimal("0.50")
        );
        BigDecimal q3 = calculatePercentile(
                sortedScores,
                new BigDecimal("0.75")
        );

        BigDecimal iqr = q3.subtract(q1);
        BigDecimal lowerFence = q1.subtract(
                iqr.multiply(new BigDecimal("1.5"))
        );
        BigDecimal upperFence = q3.add(
                iqr.multiply(new BigDecimal("1.5"))
        );

        List<BigDecimal> outlierScores = sortedScores.stream()
                .filter(score ->
                        score.compareTo(lowerFence) < 0
                                || score.compareTo(upperFence) > 0
                )
                .collect(Collectors.toList());

        BigDecimal lowerWhisker = sortedScores.stream()
                .filter(score -> score.compareTo(lowerFence) >= 0)
                .findFirst()
                .orElse(q1);

        BigDecimal upperWhisker = sortedScores.stream()
                .filter(score -> score.compareTo(upperFence) <= 0)
                .reduce((first, second) -> second)
                .orElse(q3);

        result.setQ1Score(q1);
        result.setMedianScore(median);
        result.setQ3Score(q3);
        result.setLowerWhisker(lowerWhisker);
        result.setUpperWhisker(upperWhisker);
        result.setOutlierScores(outlierScores);
    }

    /**
     * 对已经升序排列的数据计算百分位数。
     */
    private BigDecimal calculatePercentile(
            List<BigDecimal> sortedScores,
            BigDecimal percentile
    ) {

        if (sortedScores.size() == 1) {
            return sortedScores.get(0);
        }

        BigDecimal position = BigDecimal
                .valueOf(sortedScores.size() - 1L)
                .multiply(percentile);
        int lowerIndex = position.intValue();
        int upperIndex = lowerIndex + 1;
        BigDecimal fraction = position.subtract(
                BigDecimal.valueOf(lowerIndex)
        );

        BigDecimal lowerValue = sortedScores.get(lowerIndex);
        if (fraction.compareTo(BigDecimal.ZERO) == 0) {
            return lowerValue;
        }

        BigDecimal upperValue = sortedScores.get(upperIndex);
        return lowerValue.add(
                upperValue.subtract(lowerValue)
                        .multiply(fraction)
        );
    }

    /**
     * 根据条件统计成绩人数。
     */
    private int countScore(
            List<M_Examination> records,
            Predicate<BigDecimal> predicate
    ) {

        if (records == null
                || records.isEmpty()) {
            return 0;
        }

        return (int) records.stream()
                .filter(record ->
                        record != null
                                &&
                                record.getScore() != null
                )
                .map(M_Examination::getScore)
                .filter(predicate)
                .count();
    }

    /**
     * 查询并转换等级配置。
     * <p>
     * 数据库：
     * 5、25、50、75、100
     * <p>
     * 转换后：
     * 0.05、0.25、0.50、0.75、1.00
     */
    private List<BigDecimal> loadRankBoundaries(
            String school,
            String teacherName
    ) {

        // 优先查询当前教师
        String infoName = "考试排名等级";
        List<Integer> intConfigs =
                m_DefaultConfigMapper
                        .selectRankIntConfigList(
                                school,
                                teacherName,
                                infoName
                        );

        // 当前教师没有配置，查询教学处
        if (intConfigs == null
                || intConfigs.isEmpty()) {

            intConfigs =
                    m_DefaultConfigMapper
                            .selectRankIntConfigList(
                                    school,
                                    "教学处",
                                    infoName
                            );
        }

        if (intConfigs == null
                || intConfigs.size() < 5) {

            throw new IllegalArgumentException(
                    "没有查询到完整的等级配置"
            );
        }

        /*
         * 排序并去重。
         *
         * 预期：
         * 5、25、50、75、100
         */
        List<Integer> sortedConfigs =
                intConfigs.stream()
                        .filter(value ->
                                value != null
                        )
                        .distinct()
                        .sorted()
                        .collect(Collectors.toList());

        if (sortedConfigs.size() != 5) {
            throw new IllegalArgumentException(
                    "等级配置必须包含5个不同的整数"
            );
        }

        // 检查范围和递增关系
        int previous = 0;

        for (Integer value : sortedConfigs) {

            if (value <= previous
                    || value > 100) {

                throw new IllegalArgumentException(
                        "等级配置必须递增且不能超过100"
                );
            }

            previous = value;
        }

        if (sortedConfigs.get(4) != 100) {
            throw new IllegalArgumentException(
                    "等级配置最后一个值必须是100"
            );
        }

        /*
         * movePointLeft(2)相当于除以100。
         *
         * 5   -> 0.05
         * 25  -> 0.25
         * 100 -> 1.00
         */
        return sortedConfigs.stream()
                .map(value ->
                        BigDecimal
                                .valueOf(value)
                                .movePointLeft(2)
                )
                .collect(Collectors.toList());
    }

    /**
     * 计算当前班级学生在全年级中的等级。
     * <p>
     * 区间规则：包含低位，不包含高位。
     */
    private int[] calculateRankCounts(
            List<M_Examination> classStudents,
            List<M_Examination> gradeStudents,
            List<BigDecimal> boundaries
    ) {

        int[] result = new int[5];

        if (classStudents == null
                || classStudents.isEmpty()
                || gradeStudents == null
                || gradeStudents.isEmpty()) {

            return result;
        }

        /*
         * 提取全年级正常考试的所有成绩，
         * 按照从高到低排列。
         */
        List<BigDecimal> gradeScores =
                gradeStudents.stream()
                        .filter(record ->
                                record != null
                                        &&
                                        record.getScore()
                                                != null
                        )
                        .map(M_Examination::getScore)
                        .sorted(
                                Comparator.reverseOrder()
                        )
                        .collect(Collectors.toList());

        if (gradeScores.isEmpty()) {
            return result;
        }

        BigDecimal gradeStudentCount =
                BigDecimal.valueOf(
                        gradeScores.size()
                );

        for (M_Examination classStudent
                : classStudents) {

            if (classStudent == null
                    || classStudent.getScore()
                    == null) {
                continue;
            }

            BigDecimal currentScore =
                    classStudent.getScore();

            /*
             * 计算全年级中严格高于该学生的人数。
             *
             * 成绩相同的学生具有相同的排名比例。
             */
            long higherScoreCount =
                    gradeScores.stream()
                            .filter(score ->
                                    score.compareTo(
                                            currentScore
                                    ) > 0
                            )
                            .count();

            /*
             * 排名比例：
             *
             * 严格高于当前学生的人数
             * ÷
             * 全年级实考人数
             *
             * 第一名为0%，所以属于[0%,5%)。
             */
            BigDecimal rankPercent =
                    BigDecimal.valueOf(
                                    higherScoreCount
                            )
                            .divide(
                                    gradeStudentCount,
                                    10,
                                    RoundingMode.HALF_UP
                            );

            /*
             * 包含低位，不包含高位：
             *
             * A+：[0%, 5%)
             * A ：[5%, 25%)
             * B ：[25%, 50%)
             * C ：[50%, 75%)
             * D ：[75%, 100%)
             */
            if (rankPercent.compareTo(
                    boundaries.get(0)
            ) < 0) {

                result[0]++;

            } else if (rankPercent.compareTo(
                    boundaries.get(1)
            ) < 0) {

                result[1]++;

            } else if (rankPercent.compareTo(
                    boundaries.get(2)
            ) < 0) {

                result[2]++;

            } else if (rankPercent.compareTo(
                    boundaries.get(3)
            ) < 0) {

                result[3]++;

            } else if (rankPercent.compareTo(
                    boundaries.get(4)
            ) < 0) {

                result[4]++;
            }
        }

        return result;
    }

    /**
     * 校验查询参数。
     */
    private void validateRequest(
            String school,
            String grade,
            Integer className,
            String subject,
            String testName,
            String teacherName
    ) {

        if (!StringUtils.hasText(school)) {
            throw new IllegalArgumentException(
                    "学校不能为空"
            );
        }

        if (!StringUtils.hasText(grade)) {
            throw new IllegalArgumentException(
                    "年级不能为空"
            );
        }


        if (!StringUtils.hasText(subject)) {
            throw new IllegalArgumentException(
                    "学科不能为空"
            );
        }

        if (!StringUtils.hasText(testName)) {
            throw new IllegalArgumentException(
                    "考试名称不能为空"
            );
        }

        if (!StringUtils.hasText(teacherName)) {
            throw new IllegalArgumentException(
                    "教师姓名不能为空"
            );
        }
    }

    private String addExamEditRecord(Integer databaseId,String updateName,String actionWay,String databaseName,String classOrName,String contentInfo,String school,String supplementary){
        M_ExamEditRecord mExamEditRecord = new M_ExamEditRecord();
        mExamEditRecord.setSchool(school);
        mExamEditRecord.setDatabaseName(databaseName);
        mExamEditRecord.setDatabaseId(databaseId);
        mExamEditRecord.setActionWay(actionWay);
        mExamEditRecord.setCreateTime(LocalDateTime.now());
        mExamEditRecord.setContentInfo(contentInfo);
        mExamEditRecord.setClassOrName(classOrName);
        mExamEditRecord.setUpdateName(updateName);
        mExamEditRecord.setSupplementary(supplementary);



        m_ExamEditRecordMapper.insertSingleRecord(mExamEditRecord);
        return "添加考试编辑记录成功";}




}
