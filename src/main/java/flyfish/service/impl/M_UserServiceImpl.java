package flyfish.service.impl;

import flyfish.constant.FeedBackConstant;
import flyfish.mapper.*;
import flyfish.pojo.*;
import flyfish.pojo.DTO.M_DeleteClassDTO;
import flyfish.pojo.VO.M_HomeworkStundentInfoVO;
import flyfish.pojo.VO.M_StudentUserVO;
import flyfish.pojo.VO.M_TeacherRoleVO;
import flyfish.pojo.VO.M_TeacherUserVO;
import flyfish.service.AccumulateScoreService;
import flyfish.service.M_UserService;
import flyfish.utils.ChineseNameToPinyin;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.text.Collator;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class M_UserServiceImpl implements M_UserService {
    @Autowired
    private M_UserMapper m_userMapper;

    @Autowired
    private M_TeacherRoleMapper m_teacherRoleMapper;

    @Autowired
    private M_ClassTeacherRelationMapper m_classTeacherRelationMapper;

    @Autowired
    private M_TeacherListMapper mTeacherListMapper;

    @Autowired
    private M_StudentInfoMapper mStudentInfoMapper;

    @Autowired
    private M_GradeYearMapper mGradeYearMapper;

    @Autowired
    private M_GradeClassNumMapper mGradeClassNumMapper;

    @Autowired
    private AccumulateScoreService accumulateScoreService;

    @Autowired
    private FeedBackMapper feedBackMapper;

    //这个是homework的studentInfo
    @Autowired
    private StudentInfoMapper studentInfoMapper;



    @Override
    public String matchStudentName(MultipartFile file, String school, String grade,Integer className) {
        List<String> studentNameList = new ArrayList<>();
        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);

            // 确定学生姓名所在的列索引（默认为0，即第一列）
            int nameColumnIndex = 0;

            // 收集数据行的起始行（跳过标题行，从索引1开始）
            int startRow = 1;
            int totalRows = sheet.getLastRowNum();

            // 只检查部分行（例如前10行，如果总行数较少则检查全部）
            int maxCheckRows = Math.min(10, totalRows - startRow + 1);
            if (maxCheckRows > 0) {
                int numericCount = 0;
                int validCount = 0;

                for (int i = startRow; i < startRow + maxCheckRows; i++) {
                    Row row = sheet.getRow(i);
                    if (row == null) continue;

                    Cell cell = row.getCell(0);
                    if (cell == null) continue;

                    validCount++;
                    // 判断单元格类型是否为数字
                    if (cell.getCellType() == CellType.NUMERIC) {
                        numericCount++;
                    }
                }

                // 如果数字类型单元格比例 >= 50%，则认为第一列是序号列，应该跳过
                if (validCount > 0 && (double) numericCount / validCount >= 0.5) {
                    nameColumnIndex = 1; // 使用第二列
                }
            }

            // 从第二行开始读取数据（跳过标题行）
            for (int rowIndex = startRow; rowIndex <= totalRows; rowIndex++) {
                Row row = sheet.getRow(rowIndex);
                if (row == null) continue;

                // 获取指定列的学生姓名
                Cell cell = row.getCell(nameColumnIndex);
                if (cell == null) continue;

                String studentName = cell.getStringCellValue();
                if (studentName != null && !studentName.trim().isEmpty()) {
                    studentNameList.add(studentName);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        List<String> originStudentNames = mStudentInfoMapper.getStudentNamesBySchoolAndClassName(school, grade,className);

        String result= compareAndReport(studentNameList, originStudentNames);

       return result;
    }

    private String compareAndReport(List<String> studentNameList, List<String> originStudentNames) {
        // 转为Set提高查找效率（假设名单中姓名唯一）
        Set<String> studentSet = new HashSet<>(studentNameList);
        Set<String> originSet = new HashSet<>(originStudentNames);

        // 需要删除的学生：在原始名单中存在，但在当前名单中不存在
        List<String> toDelete = new ArrayList<>();
        for (String name : originStudentNames) {
            if (!studentSet.contains(name)) {
                toDelete.add(name);
            }
        }

        // 需要新增的学生：在当前名单中存在，但在原始名单中不存在
        List<String> toAdd = new ArrayList<>();
        for (String name : studentNameList) {
            if (!originSet.contains(name)) {
                toAdd.add(name);
            }
        }

        // 构建并打印结果文字
        StringBuilder report = new StringBuilder();
        report.append("学生名单比对结果：\n");

        if (!toDelete.isEmpty()) {
            report.append("需要删除的学生：\n");
            for (String name : toDelete) {
                report.append("  - ").append(name).append("\n");
            }
        } else {
            report.append("无需删除的学生。\n");
        }

        if (!toAdd.isEmpty()) {
            report.append("需要新增的学生：\n");
            for (String name : toAdd) {
                report.append("  - ").append(name).append("\n");
            }
        } else {
            report.append("无需新增的学生。\n");
        }

        return report.toString();

    }


    // 根据前端的文件，批量插入教师信息到数据库中
    //如果已经有了，那么就不再改变，如果没有，就新增
    //变化大的删掉再新增，变化小的就直接新增或者不变

    /**
     * 批量插入教师信息到数据库中
     *
     * @param file
     * @return
     */
    @Override
    public String batchInsertTeacher(MultipartFile file) {
        //先解析文件，得到一个教师信息的列表
        List<M_User> userList = new ArrayList<>();
        List<M_TeacherRole> teacherRoleList = new ArrayList<>();
        String resp = "";
        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            // 从第二行开始读取（跳过标题行）
            for (int rowIndex = 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                Row row = sheet.getRow(rowIndex);
                if (row == null) continue;

                M_User user = parseRowToUser(row);
                if (user != null && isValidUser(user)) {
                    userList.add(user);
                }
            }
            resp += "解析文件成功，教师信息列表大小为: " + userList.size() + "\n";

        } catch (IOException e) {
            throw new RuntimeException(e);
        }


        //新增用户表
        //先删除已有的教师信息，再新增新的教师信息

        List<Integer> idList = m_userMapper.getIdsByUserList(userList);
        if (idList != null && !idList.isEmpty()) {
            m_teacherRoleMapper.batchDeleteTeacherRoleByIdList(idList);
        }


        m_userMapper.deleteTeacherUser(userList);
        m_userMapper.batchInsertUser(userList);


        //新增教师角色表
        //如果原本是班主任，这次新增就不能删掉这个班主任的职称信息
        for (M_User user : userList) {
            for (String title : user.getTitleList()) {
                M_TeacherRole teacherRole = new M_TeacherRole();
                teacherRole.setUserId(user.getId());
                teacherRole.setTitle(title);
                teacherRole.setTeacherName(user.getName());
                teacherRole.setSchool(user.getSchool());
                teacherRoleList.add(teacherRole);
                //查询教师职称表，如果是班主任，那么就把班主任的职称添加到教师职称表中
                String headTeacher = "班主任";
                M_TeacherRole existingRole = m_teacherRoleMapper.getTeacherRoleByUserIdAndTitle(user.getId(), headTeacher);
                if (existingRole != null) {
                    teacherRole.setTitle("班主任");
                }
            }
        }
        //先根据userId删除教师职称表中已有的教师职称信息，再批量插入新的教师职称信息
        m_teacherRoleMapper.batchDeleteTeacherRoleByUserIds(userList);
        m_teacherRoleMapper.batchInsertTeacherRole(teacherRoleList);

        resp += "add teacherRole success";

        return resp;
    }


    @Override
    public String batchInsertStudent(MultipartFile file) {
        List<M_User> userList = new ArrayList<>();
        List<M_StudentInfo> studentInfoList = new ArrayList<>();

        String resp = "";
        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            // 从第二行开始读取（跳过标题行）
            for (int rowIndex = 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                Row row = sheet.getRow(rowIndex);
                if (row == null) continue;
                M_User user = parseRowToStudentUser(row);
                if (user != null && isValidStudentUser(user)) {
                    userList.add(user);
                }
            }
            resp += "解析文件成功，学生信息列表大小为: " + userList.size() + "\n";

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        //新增用户表
        //拿到所有的学生用户信息，根据学校、年级和班级进行删除，再批量插入新的学生用户信息
        List<Integer> idList = m_userMapper.getIdsByUserList(userList);
        if (idList != null && !idList.isEmpty()) {
            mStudentInfoMapper.deleteStudentInfoByUserIdList(idList);
        }


        m_userMapper.deleteStudentUser(userList);
        m_userMapper.batchInsertUser(userList);

        //新增学生信息表
        for (M_User user : userList) {
            M_StudentInfo studentInfo = new M_StudentInfo();
            studentInfo.setUserId(user.getId());
            studentInfo.setGrade(user.getGrade());
            studentInfo.setClassName(user.getClassName());
            studentInfo.setSchool(user.getSchool());
            studentInfo.setStudentName(user.getName());
            Integer year = mGradeYearMapper.getYearByGrade(user.getGrade());
            studentInfo.setYear(year);
            studentInfoList.add(studentInfo);
        }


        //先根据userId删除学生信息表中已有的学生信息，再批量插入新的学生信息
        mStudentInfoMapper.deleteStudentInfoByUserIds(userList);
        mStudentInfoMapper.batchInsertStudentInfo(studentInfoList);


        resp += "add studentInfo success";
        return resp;
    }

    @Override
    public List<M_TeacherRoleVO> getTeacherRoleList(String school) {

        //拿到用户表的数据
        List<M_TeacherUserVO> mTeacherUserVOList = m_userMapper.getTeacherUserListBySchool(school);
        //新建一个教师角色列表，用来存储最终的教师角色信息
        List<M_TeacherRoleVO> teacherRoleVOList = new ArrayList<>();
        if (mTeacherUserVOList != null && !mTeacherUserVOList.isEmpty()) {
            for (M_TeacherUserVO mTeacherUserVO : mTeacherUserVOList) {
                //拿到教师职称表的数据
                List<String> titleList = m_teacherRoleMapper.getTeacherRoleByUserId(mTeacherUserVO.getId());
                String headTeacher = "是";
                String headTeacherClassName = m_classTeacherRelationMapper.getClassNameBySchoolTeacherNameAndTitle(mTeacherUserVO.getSchool(), mTeacherUserVO.getTeacherName(), headTeacher);


                String grade = null;
                //如果是班主任，那么就把班主任的职称添加到教师职称表中
                if (headTeacherClassName != null) {
                    //查询教师职称表，如果不是班主任，那么就把班主任的职称添加到教师职称表中
                    if (!titleList.contains("班主任")) {
                        titleList.add("班主任");
                        String title = "班主任";
                        m_teacherRoleMapper.addHeadTeacherRoleByUserId(mTeacherUserVO.getTeacherName(), mTeacherUserVO.getSchool(), mTeacherUserVO.getId(), title);
                    }

                    grade = headTeacherClassName.substring(0, 1) + "年级";
                }

                for (String title : titleList) {
                    M_TeacherRoleVO teacherRoleVO = new M_TeacherRoleVO();
                    if (title.equals("班主任")) {
                        teacherRoleVO.setClassName(headTeacherClassName);
                        teacherRoleVO.setGrade(grade);
                    }
                    teacherRoleVO.setTeacherName(mTeacherUserVO.getTeacherName());
                    teacherRoleVO.setSchool(mTeacherUserVO.getSchool());
                    teacherRoleVO.setTitle(title);
                    teacherRoleVO.setId(mTeacherUserVO.getId());

                    teacherRoleVOList.add(teacherRoleVO);

                }


            }
        }

        return teacherRoleVOList;


    }

    @Override
    public List<String> getTeacherRoleTypeList(String school) {
        //拿到教师职称表的数据，根据学校分组，得到一个教师职称类型列表
        List<String> teacherRoleTypeList = m_teacherRoleMapper.getTeacherRoleTypeListBySchool(school);
        //把列表转化为一个HashSet，去掉重复的职称类型，再把HashSet转化为一个列表
        HashSet<String> teacherRoleTypeSet = new HashSet<>(teacherRoleTypeList);
        teacherRoleTypeList.clear();
        teacherRoleTypeList.addAll(teacherRoleTypeSet);
        return teacherRoleTypeList;
    }

    /**
     * 批量插入班主任信息到数据库中
     *
     * @param file
     * @return
     */
    @Override
    public String batchInsertHeadTeacher(MultipartFile file) {
        //先解析文件，得到一个教师班主任班级列表
        List<M_ClassTeacherRelation> classTeacherRelationList = new ArrayList<>();
        String resp = "";


        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            // 从第二行开始读取（跳过标题行）
            for (int rowIndex = 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                Row row = sheet.getRow(rowIndex);
                if (row == null) continue;


                M_ClassTeacherRelation classTeacherRelation = parseRowToHeadTeacherClass(row);
                if (classTeacherRelation != null && isValidClassTeacherRelation(classTeacherRelation)) {
                    classTeacherRelationList.add(classTeacherRelation);
                }

            }
            resp += "解析文件成功，班主任教师信息列表大小为: " + classTeacherRelationList.size() + "\n";

        } catch (IOException e) {
            throw new RuntimeException(e);
        }


        //教师角色表中，如果原本不是班主任，这次新增就把这个教师的职称添加到教师职称表中，如果原本是班主任，这次新增就不改变这个教师的职称信息
        HashMap<String, List<M_TeacherRole>> teacherRoleMap = findNotHeadTeacherToAddHeadTeacherRole(classTeacherRelationList);
        //批量插入教师角色表中新增的班主任职称信息
        if (teacherRoleMap.get("新增班主任") != null && !teacherRoleMap.get("新增班主任").isEmpty()) {
            m_teacherRoleMapper.batchInsertTeacherRole(teacherRoleMap.get("新增班主任"));
        }

        List<M_TeacherRole> noUserList = teacherRoleMap.get("没有用户的教师");
        if (noUserList != null && !noUserList.isEmpty()) {
            resp += "以下教师没有用户信息，无法添加班主任职称信息: \n";
            for (M_TeacherRole teacherRole : noUserList) {
                resp += teacherRole.getTeacherName() + "、";
            }
        }


        //修改教师班级关系表，先将所有的班主任内容设置为否，然后根据班级进行修改
        String resp2 = updateClassTeacherRelation(classTeacherRelationList);
        resp += resp2;

        return resp;
    }

    /**
     * 批量删除教师用户，根据前端传来的教师用户id列表，删除数据库中对应的教师用户信息
     *
     * @param idList
     * @return
     */
    @Override
    public String batchDeleteTeacherUser(List<Integer> idList) {
        //先根据教师用户id列表删除教师职称表中对应的教师职称信息，再根据教师用户id列表删除用户表中对应的教师用户信息
        //先批量删除教师职称表中对应的教师职称信息
        m_teacherRoleMapper.batchDeleteTeacherRoleByIdList(idList);
        m_userMapper.batchDeleteTeacherUserByIdList(idList);
        //再批量删除用户表中对应的教师用户信息


        return "delete teacher user success";
    }

    @Override
    public String batchDeleteStudentUser(List<Integer> idList) {
        //拿到idList对应的学校，班级，学号
        List<M_StudentInfo> mstudentInfoList = mStudentInfoMapper.getStudentInfoByUserIdList(idList);
        if (mstudentInfoList != null && !mstudentInfoList.isEmpty()) {
            for (M_StudentInfo mStudentInfo : mstudentInfoList) {
                //逐一删掉该用户的作业studentInfo表中的信息
                String school = mStudentInfo.getSchool();
                String grade = mStudentInfo.getGrade().substring(0, 1);
                Integer className = mStudentInfo.getClassName();
                String studentNumber = mStudentInfo.getStudentNumber();
                String classNumber = turnChineseToNumber(grade)+className;
                studentInfoMapper.deleteBySchoolClassAndStudentNumber(school,classNumber,studentNumber);
            }
        }




        //删除学生信息表中对应的学生信息
        mStudentInfoMapper.deleteStudentInfoByUserIdList(idList);
        //再批量删除用户表中对应的学生用户信息
        m_userMapper.deleteUsersByIds(idList);


        return "delete student user success";
    }

    @Override
    public String batchDeleteTeacherRoleByIdList(List<Integer> idList) {
        //根据教师用户id列表拿到教师姓名和学校
        List<M_TeacherRole> teacherRoleList = m_teacherRoleMapper.getTeacherSchoolByUserIds(idList);
        for (M_TeacherRole teacherRole : teacherRoleList) {
            //根据教师姓名和学校拿到班主任班级信息
            String headTeacher = "是";
            String className = m_classTeacherRelationMapper.getClassNameBySchoolTeacherNameAndTitle(teacherRole.getSchool(), teacherRole.getTeacherName(), headTeacher);
            if (className != null) {
                //把这个教师的班主任信息设置为否
                m_classTeacherRelationMapper.setHeadTeacherNoBySchoolAndTeacherNameAndClassName(teacherRole.getSchool(), teacherRole.getTeacherName(), className);
            }

        }
        m_teacherRoleMapper.batchDeleteTeacherRoleByIdList(idList);
        return "delete teacher role success";
    }

    /**
     * 修改教师用户信息，根据前端传来的教师用户信息，修改数据库中对应的教师用户信息
     *
     * @param mTeacherUserVO
     * @return
     */
    @Override
    public String editTeacherUser(M_TeacherUserVO mTeacherUserVO) {
        //修改用户表
        m_userMapper.editTeacherUserById(mTeacherUserVO);
        //修改teachList表的subject
        //先查看表中是否有这个教师的信息，如果没有，就新增，如果有，就修改
        String subject = mTeacherListMapper.getSubjectBySchoolAndTeacherName(mTeacherUserVO.getSchool(), mTeacherUserVO.getTeacherName());
        if (subject != null) {
            mTeacherListMapper.editSubjectBySchoolAndTeacherName(mTeacherUserVO.getSchool(), mTeacherUserVO.getTeacherName(), mTeacherUserVO.getSubject());
        } else {
            M_TeacherList mTeacherList = new M_TeacherList();
            mTeacherList.setTeacherName(mTeacherUserVO.getTeacherName());
            mTeacherList.setSchool(mTeacherUserVO.getSchool());
            mTeacherList.setSubject(mTeacherUserVO.getSubject());
            mTeacherListMapper.insetTeacherList(mTeacherList);
        }

        //修改教师班级关系表
        editClassTeacher(mTeacherUserVO);

        //修改教师职称表
        editTeacherRole(mTeacherUserVO);

        return "";
    }


    /**
     * 新增教师用户信息，根据前端传来的教师用户信息，新增数据库中对应的教师用户信息
     *
     * @param mTeacherUserVO
     * @return
     */
    @Override
    public String addTeacherUser(M_TeacherUserVO mTeacherUserVO) {
        //先判断这个教师用户信息是否已经存在，如果已经存在，就返回错误信息，如果不存在，就新增
        Integer id = m_userMapper.getIdBySchoolAndTeacherNameRole(mTeacherUserVO.getSchool(), mTeacherUserVO.getTeacherName());
        //新增教师用户，并且主键回填
        if (id != null) {
            return "已经有" + mTeacherUserVO.getTeacherName() + "的教师用户信息了，无法新增";
        }
        M_User user = new M_User();
        user.setName(mTeacherUserVO.getTeacherName());
        user.setPhone(mTeacherUserVO.getPhone());
        user.setPassword(mTeacherUserVO.getPassword());
        user.setSchool(mTeacherUserVO.getSchool());
        user.setRole("教师");
        m_userMapper.batchInsertUser(List.of(user));
        Integer newId = m_userMapper.getIdBySchoolAndTeacherNameRole(mTeacherUserVO.getSchool(), mTeacherUserVO.getTeacherName());
        mTeacherUserVO.setId(newId);


        //判断teacherList表中是否有这个教师的信息，如果没有，就新增，如果有，就更新
            String subject = mTeacherListMapper.getSubjectBySchoolAndTeacherName(mTeacherUserVO.getSchool(), mTeacherUserVO.getTeacherName());
            if (subject != null) {
                mTeacherListMapper.editSubjectBySchoolAndTeacherName(mTeacherUserVO.getSchool(), mTeacherUserVO.getTeacherName(), mTeacherUserVO.getSubject());
            } else {
                M_TeacherList mTeacherList = new M_TeacherList();
                mTeacherList.setTeacherName(mTeacherUserVO.getTeacherName());
                mTeacherList.setSchool(mTeacherUserVO.getSchool());
                mTeacherList.setSubject(mTeacherUserVO.getSubject());
                mTeacherListMapper.insetTeacherList(mTeacherList);
            }

        //判断教师职称表中是否有这个教师的职称信息，如果没有，就新增，如果有，就修改
       editTeacherRole(mTeacherUserVO);

        //判断教师班级关系表中是否有这个教师的班级信息，如果没有，就新增，如果有，就修改
        editClassTeacher(mTeacherUserVO);
        return "新增"+mTeacherUserVO.getTeacherName()+"的教师用户信息成功";
    }

    @Override
    public String addStudentUser(M_StudentUserVO mStudentUserVO) {
        //如果学号不是八位数字，或者不是以year开头，那么就返回错误信息
        String grade = mStudentUserVO.getClassName().substring(0, 1) + "年级";
        Integer year = mGradeYearMapper.getYearByGrade(grade);
        Integer className = Integer.valueOf(mStudentUserVO.getClassName().substring(2, 3));
        String classNameStr ="0"+ mStudentUserVO.getClassName().substring(2, 3);
        if(mStudentUserVO.getStudentNumber() !=null && !mStudentUserVO.getStudentNumber().equals("") ){

        if( mStudentUserVO.getStudentNumber() != null && (!mStudentUserVO.getStudentNumber().matches("\\d{8}") || !mStudentUserVO.getStudentNumber().startsWith(year+classNameStr))) {
            return "学号必须是八位数字，并且以年分班号开头";
        }}
        //判断该校是不是已经有这个学生的学号了，如果有了，就返回错误信息
        Integer studentNumberId = mStudentInfoMapper.getIdByStudentNumber(mStudentUserVO.getSchool(), mStudentUserVO.getStudentNumber());
        if(studentNumberId != null){
            return "学号已经存在了，无法新增";
        }

        //判断是不是重复姓名
        Integer  id = mStudentInfoMapper.getNameByShoolClassAndStudentName(mStudentUserVO.getSchool(), grade, className, mStudentUserVO.getStudentName());
        if(id != null){
            return "班级里已经有"+mStudentUserVO.getStudentName()+"了，无法新增";
        }
        //修改用户表
        //吧phoneList转化为;分隔的字符串
        String phone = mStudentUserVO.getPhoneList() != null ? String.join(";", mStudentUserVO.getPhoneList()) : null;
        mStudentUserVO.setPhone(phone);
        //判断这个学生用户信息是否已经存在，如果已经存在，就返回错误信息，如果不存在，就新增
        List<String> exitPhone = m_userMapper.getPhoneByStudentNameAndSchool(mStudentUserVO.getStudentName(), mStudentUserVO.getSchool());
        if (exitPhone != null && exitPhone.size() > 0) {
            for (int i = 0; i < exitPhone.size(); i++) {
                for(String phoneNum : mStudentUserVO.getPhoneList()){
                    if(exitPhone.get(i).contains(phoneNum)){
                        return "已经有" + mStudentUserVO.getStudentName() + "的学生用户信息了，无法新增";
                    }
                }
            }
        }
            m_userMapper.insertStudentUser(mStudentUserVO);

        //修改学生信息表
        M_StudentInfo mStudentInfo = new M_StudentInfo();
        mStudentInfo.setStudentName(mStudentUserVO.getStudentName());
        mStudentInfo.setUserId(mStudentUserVO.getId());
        mStudentInfo.setGrade(grade);
        mStudentInfo.setYear(year);
        mStudentInfo.setClassName(className);
        mStudentInfo.setStudentNumber(mStudentUserVO.getStudentNumber());
        mStudentInfo.setSchool(mStudentUserVO.getSchool());


        Integer studentInfoId = mStudentInfoMapper.getIdByUserId(mStudentUserVO.getId());
        if(studentInfoId != null){
            mStudentInfoMapper.updateStudentInfo(mStudentInfo);
        }else {
            mStudentInfoMapper.batchInsertStudentInfo(List.of(mStudentInfo));
        }

        assignStudentNumberForNoStudentNumber(mStudentUserVO.getSchool());



        String studentNumber = mStudentInfoMapper.getStudentNumberByUserId(mStudentUserVO.getId());


        String classNumber = turnChineseToNumber(grade.substring(0,1))+className;

        //新增作业管理的学生信息
        StudentInfo studentInfo = new StudentInfo();
        studentInfoMapper.deleteBySchoolClassAndStudentNumber(mStudentUserVO.getSchool(), classNumber, mStudentUserVO.getStudentNumber());
        studentInfo.setStudentNumber(studentNumber);
        studentInfo.setName(mStudentInfo.getStudentName());
        studentInfo.setSchool(mStudentInfo.getSchool());
        studentInfo.setClassNumber(classNumber);
        studentInfo.setYear(mStudentInfo.getYear());
        studentInfo.setPinyin(ChineseNameToPinyin.convertToPinyin(mStudentInfo.getStudentName()));
        studentInfoMapper.addStudentInfo(studentInfo);

        //新增积分表
        //同时更新积分表
        accumulateScoreService.getNameClass(studentInfo.getClassNumber(),List.of(studentInfo.getName()),"语文",studentInfo.getSchool());
        accumulateScoreService.getNameClass(studentInfo.getClassNumber(),List.of(studentInfo.getName()),"数学",studentInfo.getSchool());
        accumulateScoreService.getNameClass(studentInfo.getClassNumber(),List.of(studentInfo.getName()),"英语",studentInfo.getSchool());



        return "新增"+mStudentUserVO.getStudentName()+"的学生用户信息成功";

    }



    @Override
    public String editStudentUser(M_StudentUserVO mStudentUserVO) {
        //如果学号不是八位数字，或者不是以year开头，那么就返回错误信息
        String grade = mStudentUserVO.getClassName().substring(0, 1) + "年级";
        Integer year = mGradeYearMapper.getYearByGrade(grade);
        Integer className = Integer.valueOf(mStudentUserVO.getClassName().substring(2, 3));
        String classNameStr ="0"+ mStudentUserVO.getClassName().substring(2, 3);

        if(!mStudentUserVO.getStudentNumber().equals("")){

            if( mStudentUserVO.getStudentNumber() != null && (!mStudentUserVO.getStudentNumber().matches("\\d{8}") || !mStudentUserVO.getStudentNumber().startsWith(year+classNameStr))) {
                return "学号必须是八位数字，并且以年分班号开头";
            }}
        //判断该校是不是已经有这个学生的学号了，如果有了，就返回错误信息
        Integer studentNumberId = mStudentInfoMapper.getIdByStudentNumber(mStudentUserVO.getSchool(), mStudentUserVO.getStudentNumber());
        if(studentNumberId != null){
            String name = mStudentInfoMapper.getStudentNameById(mStudentUserVO.getId());
            if(!name.equals(mStudentUserVO.getStudentName())){
            return "学号已经存在了，无法新增";}
        }
        //修改用户表
        //吧phoneList转化为;分隔的字符串
        String phone = mStudentUserVO.getPhoneList() != null ? String.join(";", mStudentUserVO.getPhoneList()) : null;
        mStudentUserVO.setPhone(phone);
        m_userMapper.updateStudentUser(mStudentUserVO);

        //修改学生信息表
        M_StudentInfo mStudentInfo = new M_StudentInfo();
        mStudentInfo.setStudentName(mStudentUserVO.getStudentName());
        mStudentInfo.setUserId(mStudentUserVO.getId());




        mStudentInfo.setGrade(grade);
        mStudentInfo.setYear(year);

        mStudentInfo.setClassName(className);

        mStudentInfo.setStudentNumber(mStudentUserVO.getStudentNumber());

        mStudentInfo.setSchool(mStudentUserVO.getSchool());

        Integer studentInfoId = mStudentInfoMapper.getIdByUserId(mStudentUserVO.getId());
        if(studentInfoId != null){
            mStudentInfoMapper.updateStudentInfo(mStudentInfo);
        }else {
            mStudentInfoMapper.batchInsertStudentInfo(List.of(mStudentInfo));
        }



        return "修改学生用户成功";
    }

    /**
     * 新增班主任信息，根据前端传来的班主任信息，新增数据库中对应的班主任信息
     * @param headTeacherRoleVO
     * @return
     */
    @Override
    public String addHeadTeacher(M_TeacherRoleVO headTeacherRoleVO) {
        Integer id = m_userMapper.getIdBySchoolAndTeacherNameRole(headTeacherRoleVO.getSchool(), headTeacherRoleVO.getTeacherName());
        if (id == null) {
            return "没有这个教师的用户信息，无法新增班主任信息";
        }
        headTeacherRoleVO.setId(id);
        String resp = editHeadTeacher(headTeacherRoleVO);
        if(resp.equals("修改班主任信息成功")){
        return "新增"+headTeacherRoleVO.getTeacherName()+"的班主任信息成功";}
        else {
            return "新增"+headTeacherRoleVO.getTeacherName()+"的班主任信息失败，原因是: "+resp;
        }
    }

    @Override
    public List<M_StudentUserVO> getStudentUserListByHeadTeacher(String school, String headTeacherClassName) {
        String grade = headTeacherClassName.substring(0, 1) + "年级";
        Integer className = Integer.valueOf(headTeacherClassName.substring(2, 3));
        List<M_StudentInfo> studentInfoList = mStudentInfoMapper.getStudentInfoListBySchoolAndGradeClassName(school, grade,className);
        List<M_StudentUserVO> studentUserVOList = new ArrayList<>();
        if(studentInfoList !=null && studentInfoList.size() > 0){

            for (M_StudentInfo studentInfo : studentInfoList) {
                M_StudentUserVO studentUserVO = new M_StudentUserVO();
                studentUserVO.setId(studentInfo.getUserId());
                studentUserVO.setStudentName(studentInfo.getStudentName());
                studentUserVO.setSchool(studentInfo.getSchool());
                studentUserVO.setStudentNumber(studentInfo.getStudentNumber());
                String phone = m_userMapper.getPhoneById(studentInfo.getUserId());
                String password = m_userMapper.getPasswordById(studentInfo.getUserId());
                studentUserVO.setGrade(grade);
                studentUserVO.setPassword(password);
                studentUserVO.setClassName(headTeacherClassName);
                if (phone != null) {
                    studentUserVO.setPhone(phone);
                }
                studentUserVOList.add(studentUserVO);
            }

        }

        //对studentUserVOList进行排序，按照学生姓名的姓首字母顺序进行排序
        //按照姓名字母顺序排序
        Collator collator = Collator.getInstance(Locale.CHINESE);
// 按姓名拼音升序
        studentUserVOList.sort((s1, s2) -> collator.compare(s1.getStudentName(), s2.getStudentName()));



        return studentUserVOList;
    }

    @Override
    public List<M_HomeworkStundentInfoVO> queryHomeworkStudentInfo(String school) {
        List<StudentInfo> homeworkStudentInfoList = studentInfoMapper.getStudentInfoBySchool(school) ;
        List<M_StudentInfo> userStudentInfoList = mStudentInfoMapper.getStudentInfoListBySchool(school);



        HashMap<String,List<StudentInfo>> homeworkClassStudentInfoMap = new HashMap<>();
        HashMap<String,List<M_StudentInfo>> userClassStudentInfoMap = new HashMap<>();
        //按照班级分类，拿到班级对应的map
        if(homeworkStudentInfoList != null && homeworkStudentInfoList.size() > 0){
        for(StudentInfo homeWorkstudentInfo : homeworkStudentInfoList){

            String homeworkClassName = homeWorkstudentInfo.getClassNumber();
            if(homeworkClassStudentInfoMap.containsKey(homeworkClassName)){
                homeworkClassStudentInfoMap.get(homeworkClassName).add(homeWorkstudentInfo);
            }else {
                List<StudentInfo> studentInfoList = new ArrayList<>();
                studentInfoList.add(homeWorkstudentInfo);  // 添加当前学生
                homeworkClassStudentInfoMap.put(homeworkClassName, studentInfoList);
            }
        }}

        if(userStudentInfoList != null && userStudentInfoList.size() > 0){
            for (M_StudentInfo userStudentInfo : userStudentInfoList){

                //把汉字的一二三四五六转化为数字
                String grade = userStudentInfo.getGrade().substring(0,1);
                String gradeNum = turnChineseToNumber(grade);
                String userClassName = gradeNum+userStudentInfo.getClassName() ;
                if(userClassStudentInfoMap.containsKey(userClassName)){
                    userClassStudentInfoMap.get(userClassName).add(userStudentInfo);
                }else {
                    List<M_StudentInfo> studentInfoList = new ArrayList<>();
                    studentInfoList.add(userStudentInfo);
                    userClassStudentInfoMap.put(userClassName, studentInfoList);
                }
            }
        }

        List<M_HomeworkStundentInfoVO> homeworkStudentInfoVOList = new ArrayList<>();
        //遍历homeworkClassStudentInfoMap
        if (homeworkClassStudentInfoMap != null && !homeworkClassStudentInfoMap.isEmpty()) {
            for (Map.Entry<String, List<StudentInfo>> entry : homeworkClassStudentInfoMap.entrySet()) {
                String homeworkClassName = entry.getKey();
                List<StudentInfo> homeworkStudentInfos = entry.getValue();

                if (userClassStudentInfoMap.containsKey(homeworkClassName)) {
                    List<M_StudentInfo> userStudentInfos = userClassStudentInfoMap.get(homeworkClassName);

                    // 每个班级独立的姓名列表
                    List<String> sameStudentNameList = new ArrayList<>();
                    List<String> homeworkStudentNameNotInUserList = new ArrayList<>();
                    List<String> userStudentNameNotInHomeworkList = new ArrayList<>();

                    // 提取姓名集合
                    Set<String> homeworkNameSet = homeworkStudentInfos.stream()
                            .map(StudentInfo::getName)
                            .collect(Collectors.toSet());
                    Set<String> userNameSet = userStudentInfos.stream()
                            .map(M_StudentInfo::getStudentName)
                            .collect(Collectors.toSet());

                    // 交集
                    Set<String> sameNameSet = new HashSet<>(homeworkNameSet);
                    sameNameSet.retainAll(userNameSet);
                    sameStudentNameList.addAll(sameNameSet);

                    // homework 独有
                    Set<String> homeworkOnlySet = new HashSet<>(homeworkNameSet);
                    homeworkOnlySet.removeAll(userNameSet);
                    homeworkStudentNameNotInUserList.addAll(homeworkOnlySet);

                    // user 独有
                    Set<String> userOnlySet = new HashSet<>(userNameSet);
                    userOnlySet.removeAll(homeworkNameSet);
                    userStudentNameNotInHomeworkList.addAll(userOnlySet);

                    M_HomeworkStundentInfoVO vo = new M_HomeworkStundentInfoVO();
                    vo.setClassName(homeworkClassName);
                    vo.setStudentNameList(sameStudentNameList);
                    vo.setHomeworkNotInUserList(homeworkStudentNameNotInUserList);
                    vo.setUserNotInHomeworkList(userStudentNameNotInHomeworkList);
                    homeworkStudentInfoVOList.add(vo);

                } else {
                    // 用户中没有该班级：所有学生都属于 homework 独有列表
                    List<String> homeworkOnlyList = homeworkStudentInfos.stream()
                            .map(StudentInfo::getName)
                            .collect(Collectors.toList());

                    M_HomeworkStundentInfoVO vo = new M_HomeworkStundentInfoVO();
                    vo.setClassName(homeworkClassName);
                    vo.setHomeworkNotInUserList(homeworkOnlyList);
                    vo.setStudentNameList(new ArrayList<>());   // 空列表，避免 null
                    vo.setUserNotInHomeworkList(new ArrayList<>());
                    homeworkStudentInfoVOList.add(vo);
                }
            }
        }









        if(homeworkStudentInfoVOList != null && homeworkStudentInfoVOList.size() > 0){
             // 排序
            homeworkStudentInfoVOList.sort((vo1, vo2) -> {
                int num1 = Integer.parseInt(vo1.getClassName().replaceAll("[^0-9]", ""));
                int num2 = Integer.parseInt(vo2.getClassName().replaceAll("[^0-9]", ""));
                return Integer.compare(num1, num2);
            });

            Collator collator = Collator.getInstance(Locale.CHINA);
            for (M_HomeworkStundentInfoVO vo : homeworkStudentInfoVOList) {
                List<String> nameList = vo.getStudentNameList();
                if (nameList != null) {
                    nameList.sort(collator);
                }
                // 对其他列表（如 homeworkNotInUserList、userNotInHomeworkList）也可按需排序
            }


        }

return homeworkStudentInfoVOList;
    }


    /**
     * 管理员同步学生名单到作业管理
     * @param school
     * @return
     */
    @Override
    public String syncUserList(String school) {
        //在同步所有的数据之前，要给所有的学生用户分配学号，确保每个学生都是有学号的
        String assignNumber= assignStudentNumberForNoStudentNumber(school);




        List<M_StudentInfo> allMStudentInfoList = mStudentInfoMapper.getStudentInfoListBySchool(school);
        // 对所有的学生按照班级进行分类
        HashMap<String, List<M_StudentInfo>> studentInfoMap = new HashMap<>();
        for (M_StudentInfo studentInfo : allMStudentInfoList) {
            String grade = studentInfo.getGrade().substring(0,1);
            String gradeNum = turnChineseToNumber(grade);
            String userClassName = gradeNum+studentInfo.getClassName() ;
            if (!studentInfoMap.containsKey(userClassName)) {
                studentInfoMap.put(userClassName, new ArrayList<>());
            }
            studentInfoMap.get(userClassName).add(studentInfo);
        }

        //拿到所有hashMap中的key,组成List
        List<String> userClassNameList = new ArrayList<>();
        for (Map.Entry<String, List<M_StudentInfo>> entry : studentInfoMap.entrySet()) {
            String userClassNmae = entry.getKey();
            userClassNameList.add(userClassNmae);
        }

        //拿到所有的作业用户的班级
        List<String> homeworkClassNameList = studentInfoMapper.getAllHomeworkClassNameBySchool(school);


        //拿到交集的班级列表
// 1. 交集（两个列表中都存在的班级）
        List<String> intersectionClassNameList = userClassNameList.stream()
                .filter(homeworkClassNameList::contains)
                .collect(Collectors.toList());

// 2. 在用户中但不在作业中的班级（user - homework）
        List<String> userOnlyClassNameList = userClassNameList.stream()
                .filter(c -> !homeworkClassNameList.contains(c))
                .collect(Collectors.toList());



        for(String userClassName : userOnlyClassNameList) {
            List<M_StudentInfo> userStudentInfoList = studentInfoMap.get(userClassName);
            List<StudentInfo> studentInfoList = new ArrayList<>();
            List<String> nameList = new ArrayList<>();
            for (M_StudentInfo mstudentInfo : userStudentInfoList) {
                StudentInfo studentInfo = new StudentInfo();
                studentInfo.setName(mstudentInfo.getStudentName());
                studentInfo.setClassNumber(userClassName);
                studentInfo.setSchool(mstudentInfo.getSchool());
                studentInfo.setPinyin(ChineseNameToPinyin.convertToPinyin(mstudentInfo.getStudentName()));
                studentInfo.setYear(mstudentInfo.getYear());
                studentInfo.setStudentNumber(mstudentInfo.getStudentNumber());
                studentInfoList.add(studentInfo);

                nameList.add(mstudentInfo.getStudentName());
            }


            studentInfoMapper.batchInsertStudentInfo(studentInfoList);

            String username = userClassName;
            //同时更新积分表
            accumulateScoreService.getNameClass(username,nameList,"语文",school);
            accumulateScoreService.getNameClass(username,nameList,"数学",school);
            accumulateScoreService.getNameClass(username,nameList,"英语",school);

            //同时插入反馈常量表
            String subject = "语文";
            Integer collectedNumber = FeedBackConstant.collectedNumber;
            Integer praiseNumber = FeedBackConstant.praiseNumber;
            Integer uncompletedNumber = FeedBackConstant.uncompletedNumber;
            Integer warningNumber = FeedBackConstant.warningNumber;
//        Integer feedbackHour = FeedBackConstant.feedbackHour;
            feedBackMapper.deleteConstant(username,subject,school);
            feedBackMapper.addConsant(username,subject,collectedNumber,praiseNumber,uncompletedNumber,warningNumber,school);
            subject = "数学";
            feedBackMapper.deleteConstant(username,subject,school);
            feedBackMapper.addConsant(username,subject,collectedNumber,praiseNumber,uncompletedNumber,warningNumber,school);
            subject = "英语";
            feedBackMapper.deleteConstant(username,subject,school);
            feedBackMapper.addConsant(username,subject,collectedNumber,praiseNumber,uncompletedNumber,warningNumber,school);


        }

        String resp= ""+assignNumber+"<br><br>";
        //新增的班级有
        resp+="已经存在的班级有："+String.join("、",intersectionClassNameList )+" 共计"+intersectionClassNameList.size()+"个"+"<br><br>";
        resp += "新增的班级有："+String.join("、",userOnlyClassNameList )+"共计"+userOnlyClassNameList.size()+"个"+"<br>";









        return resp;
    }

    @Override
    public String deleteClass(M_DeleteClassDTO mDeleteClassDTO) {
        if(mDeleteClassDTO != null){
            List<String> classNameList = mDeleteClassDTO.getClassNameList();
            for(String userClassName : classNameList){
                //把这个班级的学生信息删除掉
                String school = mDeleteClassDTO.getSchool();
                studentInfoMapper.deleteByClassAndSchool(userClassName,school);
            }
}

        return "删除"+String.join("、",mDeleteClassDTO.getClassNameList())+"班级成功";
    }

    private String turnChineseToNumber(String grade) {
        //把汉字一二三四五六变为数字
        if(grade != null && grade.length() == 1){
            if(grade.equals("一")){
                return "1";
            }else if(grade.equals("二")){
                return "2";
            }else if(grade.equals("三")){
                return "3";
            }else if(grade.equals("四")){
                return "4";
            }else if(grade.equals("五")){
                return "5";
            }else if(grade.equals("六")){
                return "6";}

        }else {
            return null;
        }
        return null;
    }

    /**
     * 修改班主任信息，根据前端传来的班主任信息，修改数据库中对应的班主任信息
     * @param headTeacherRoleVO
     * @return
     */
    @Override
    public String editHeadTeacher(M_TeacherRoleVO headTeacherRoleVO) {
        String headTeacherClassName = headTeacherRoleVO.getClassName();
        if (headTeacherClassName != null) {

            List<String> titleList = m_teacherRoleMapper.getTeacherRoleByUserId(headTeacherRoleVO.getId());
            if (titleList != null && titleList.size() > 0) {
                if (!titleList.contains("班主任")) {
                    //如果原来不是班主任，那么就把这个教师的职称添加到教师职称表中
                    M_TeacherRole teacherRole = new M_TeacherRole();
                    teacherRole.setUserId(headTeacherRoleVO.getId());
                    teacherRole.setTitle("班主任");
                    teacherRole.setTeacherName(headTeacherRoleVO.getTeacherName());
                    teacherRole.setSchool(headTeacherRoleVO.getSchool());
                    m_teacherRoleMapper.batchInsertTeacherRole(List.of(teacherRole));
                }

            } else {
                //如果原来不是班主任，那么就把这个教师的职称添加到教师职称表中
                M_TeacherRole teacherRole = new M_TeacherRole();
                teacherRole.setUserId(headTeacherRoleVO.getId());
                teacherRole.setTitle("班主任");
                teacherRole.setTeacherName(headTeacherRoleVO.getTeacherName());
                teacherRole.setSchool(headTeacherRoleVO.getSchool());
                m_teacherRoleMapper.batchInsertTeacherRole(List.of(teacherRole));
            }
            Integer teacherId = mTeacherListMapper.getTeacherId(headTeacherRoleVO.getSchool(), headTeacherRoleVO.getTeacherName());
            Integer classId = mGradeClassNumMapper.getClassIdByGradeAndClassName(headTeacherRoleVO.getSchool(), headTeacherRoleVO.getClassName());
            M_ClassTeacherRelation classTeacherRelation = new M_ClassTeacherRelation();
            if (teacherId == null) {
                return "没有这个教师列表信息，无法修改班主任信息";
            }
            if (classId == null) {
                return "没有这个班级的信息，无法修改班主任信息";
            }
            classTeacherRelation.setTeacherId(teacherId);
            classTeacherRelation.setTeacherName(headTeacherRoleVO.getTeacherName());
            classTeacherRelation.setClassId(classId);
            classTeacherRelation.setClassName(headTeacherRoleVO.getClassName());
            classTeacherRelation.setSchool(headTeacherRoleVO.getSchool());
            classTeacherRelation.setHeadTeacher("是");


            String headTeacher = "是";
            Integer id = m_classTeacherRelationMapper.getIdBySchoolAndTeacherNameAndTitle(headTeacherRoleVO.getSchool(), headTeacherRoleVO.getTeacherName(), headTeacher);
            if (id != null) {

                //如果这个教师已经是班主任了，那么就把这个教师的班主任信息设置为否
                m_classTeacherRelationMapper.setHeadTeacherNoBySchoolAndTeacherName(headTeacherRoleVO.getSchool(), headTeacherRoleVO.getTeacherName());

            }


            Integer IdByClass = m_classTeacherRelationMapper.getIdByClassNameSchool(headTeacherRoleVO.getSchool(), headTeacherRoleVO.getClassName());
            if (IdByClass != null) {
                m_classTeacherRelationMapper.setHeadTeacherNoByClassNameSchool(headTeacherRoleVO.getSchool(), headTeacherRoleVO.getClassName());
                Integer existingId = m_classTeacherRelationMapper.getIdBySchoolAndTeacherNameAndTitle(headTeacherRoleVO.getSchool(), headTeacherRoleVO.getTeacherName(), headTeacherRoleVO.getClassName());
                if (existingId != null) {
                    classTeacherRelation.setId(existingId);
                    m_classTeacherRelationMapper.updateClassTeacherRelation(classTeacherRelation);
                } else {
                    m_classTeacherRelationMapper.deleteByClassSchoolTeacher(headTeacherRoleVO.getSchool(), headTeacherRoleVO.getClassName(), headTeacherRoleVO.getTeacherName());
                    m_classTeacherRelationMapper.insetClassTeacherRelation(classTeacherRelation);
                }
            } else {
                m_classTeacherRelationMapper.deleteByClassSchoolTeacher(headTeacherRoleVO.getSchool(), headTeacherRoleVO.getClassName(), headTeacherRoleVO.getTeacherName());
                m_classTeacherRelationMapper.insetClassTeacherRelation(classTeacherRelation);
            }
        } else {
            //如果没有班主任班级信息，那么就把这个教师的班主任职称信息删除掉，并把这个教师的班主任信息设置为否
            m_teacherRoleMapper.deleteTeacherRoleBySchool(headTeacherRoleVO.getSchool(), "班主任");
            m_classTeacherRelationMapper.setHeadTeacherNoBySchoolAndTeacherName(headTeacherRoleVO.getSchool(), headTeacherRoleVO.getTeacherName());
        }
        return "修改班主任信息成功";
    }

    /**
     * 重新分配学生学号，按照年级和班级进行分组，在每个分组内按照姓名字母顺序排序，重新分配学号，学号的格式为年级+班级+四位流水号，例如：20250102，其中2025代表年级，01代表班级，02代表流水号
     *
     * @param school
     * @return
     */
    @Override
    public String reAssignStudentNumber(String school) {
        //先拿到所有的sutdentIfo数据
        List<M_StudentInfo> studentInfoList = mStudentInfoMapper.getStudentInfoListBySchool(school);
        //按照year组合className分组
        HashMap<String, List<M_StudentInfo>> studentYearClassMap = sortByYearAndClass(studentInfoList);
        //在每个分组内给studentNumber赋值，按照按照姓名字母顺序排序，重新分配学号，学号的格式为年级+班级+四位流水号，例如：20250102，其中2025代表年级，01代表班级，02代表流水号
        List<M_StudentInfo> updatedStudentInfoList = reAssignStudentNumberByYearClass(studentYearClassMap);
        //批量更新学生信息表中的学号信息
        batchUpdateStudentInfo(updatedStudentInfoList);

        return "分配" + updatedStudentInfoList.size() + "个学生的学号成功";
    }

    @Override
    public String assignStudentNumberForNoStudentNumber(String school) {
        //先拿到所有的sutdentIfo数据
        List<M_StudentInfo> studentInfoList = mStudentInfoMapper.getStudentInfoListBySchool(school);
        //按照year组合className分组
        HashMap<String, List<M_StudentInfo>> studentYearClassMap = sortByYearAndClass(studentInfoList);
        //在每个分组内给studentNumber赋值，按照按照姓名字母顺序排序，重新分配学号，学号的格式为年级+班级+四位流水号，例如：20250102，其中2025代表年级，01代表班级，02代表流水号
        //只给没有学号的学生分配学号，而且要在原有学号的基础上分配学号，例如：如果已经有一个学生的学号是20250101，那么下一个没有学号的学生的学号就是20250102
        List<M_StudentInfo> updatedStudentInfoList = assignStudentNumberForNoStudentNumberByYearClass(studentYearClassMap);
        //批量更新学生信息表中的学号信息
        batchUpdateStudentInfo(updatedStudentInfoList);

        return "分配" + updatedStudentInfoList.size() + "个没有学号的学生的学号成功";
    }


    private void batchUpdateStudentInfo(List<M_StudentInfo> updatedStudentInfoList) {
        for (M_StudentInfo studentInfo : updatedStudentInfoList) {
            mStudentInfoMapper.updateStudentInfoNumber(studentInfo);
        }
    }

    private List<M_StudentInfo> assignStudentNumberForNoStudentNumberByYearClass(HashMap<String, List<M_StudentInfo>> studentYearClassMap) {
        List<M_StudentInfo> updatedStudentInfoList = new ArrayList<>();
        for (String key : studentYearClassMap.keySet()) {
            List<M_StudentInfo> studentInfoList = studentYearClassMap.get(key);
            //按照姓名字母顺序排序
            //按照姓名字母顺序排序
            Collator collator = Collator.getInstance(Locale.CHINESE);
// 按姓名拼音升序
            studentInfoList.sort((s1, s2) -> collator.compare(s1.getStudentName(), s2.getStudentName()));
            int serialNumber = 1;

            List<M_StudentInfo> studentInfoListNoNumber = new ArrayList<>();
            Integer maxNumber = 0;
            for (M_StudentInfo studentInfo : studentInfoList) {
                if (studentInfo.getStudentNumber() != null && !studentInfo.getStudentNumber().isEmpty()) {


                    String numberStr = studentInfo.getStudentNumber().substring(6);
                    Integer number = Integer.valueOf(numberStr);
                    if (number > maxNumber) {
                        maxNumber = number;
                    }
                } else {
                    studentInfoListNoNumber.add(studentInfo);
                }
            }
            serialNumber = maxNumber + 1;
            //对没有分配学号的按首字母排序，再分配学号
            studentInfoListNoNumber.sort((s1, s2) -> s1.getStudentName().compareTo(s2.getStudentName()));

            for (M_StudentInfo studentInfo : studentInfoListNoNumber) {
                String year = String.valueOf(studentInfo.getYear());
                String className = String.format("%02d", studentInfo.getClassName());
                String serialNumberStr = String.format("%02d", serialNumber);
                String studentNumber = year + className + serialNumberStr;
                studentInfo.setStudentNumber(studentNumber);
                updatedStudentInfoList.add(studentInfo);
                serialNumber++;
            }
        }
        return updatedStudentInfoList;
    }

    private List<M_StudentInfo> reAssignStudentNumberByYearClass(HashMap<String, List<M_StudentInfo>> studentYearClassMap) {
        List<M_StudentInfo> updatedStudentInfoList = new ArrayList<>();
        for (String key : studentYearClassMap.keySet()) {
            List<M_StudentInfo> studentInfoList = studentYearClassMap.get(key);
            //按照姓名字母顺序排序
            Collator collator = Collator.getInstance(Locale.CHINESE);
// 按姓名拼音升序
            studentInfoList.sort((s1, s2) -> collator.compare(s1.getStudentName(), s2.getStudentName()));

            for (int i = 0; i < studentInfoList.size(); i++) {
                M_StudentInfo studentInfo = studentInfoList.get(i);
                String year = String.valueOf(studentInfo.getYear());
                String className = String.format("%02d", studentInfo.getClassName());
                String serialNumber = String.format("%02d", i + 1);
                String studentNumber = year + className + serialNumber;
                studentInfo.setStudentNumber(studentNumber);
                updatedStudentInfoList.add(studentInfo);
            }
        }
        return updatedStudentInfoList;
    }

    private HashMap<String, List<M_StudentInfo>> sortByYearAndClass(List<M_StudentInfo> studentInfoList) {
        HashMap<String, List<M_StudentInfo>> studentYearClassMap = new HashMap<>();
        for (M_StudentInfo studentInfo : studentInfoList) {
            String key = studentInfo.getYear() + "0" + studentInfo.getClassName();
            if (!studentYearClassMap.containsKey(key)) {
                studentYearClassMap.put(key, new ArrayList<>());
            }
            studentYearClassMap.get(key).add(studentInfo);
        }
        return studentYearClassMap;

    }

    private void editTeacherRole(M_TeacherUserVO mTeacherUserVO) {
        List<String> titleList = m_teacherRoleMapper.getTeacherRoleByUserId(mTeacherUserVO.getId());
        List<String> newTitleList = mTeacherUserVO.getTitleList();
        if (newTitleList == null || newTitleList.isEmpty()) {
            //删掉所有的职称
            m_teacherRoleMapper.batchDeleteTeacherRoleByIdList(List.of(mTeacherUserVO.getId()));
            //如果原职称中包括班主任，那么就把这个教师的班主任信息设置为否
            if (titleList != null && titleList.contains("班主任")) {
                String headTeacher = "是";
                Integer id = m_classTeacherRelationMapper.getIdBySchoolAndTeacherNameAndTitle(mTeacherUserVO.getSchool(), mTeacherUserVO.getTeacherName(), headTeacher);
                if (id != null) {

                    m_classTeacherRelationMapper.setHeadTeacherNoBySchoolAndTeacherName(mTeacherUserVO.getSchool(), mTeacherUserVO.getTeacherName());
                }
            }
            return;
        }

        if (titleList == null || titleList.isEmpty()) {
            if (newTitleList == null || newTitleList.isEmpty()) {
                return;
            } else {
                List<M_TeacherRole> teacherRoleList = new ArrayList<>();
                for (String title : newTitleList) {
                    M_TeacherRole teacherRole = new M_TeacherRole();
                    teacherRole.setUserId(mTeacherUserVO.getId());
                    teacherRole.setTitle(title);
                    teacherRole.setTeacherName(mTeacherUserVO.getTeacherName());
                    teacherRole.setSchool(mTeacherUserVO.getSchool());
                    teacherRoleList.add(teacherRole);
                }
                m_teacherRoleMapper.batchInsertTeacherRole(teacherRoleList);
                return;
            }
        }

        //找出需要删除的职称
        List<String> deleteTitleList = new ArrayList<>();
        for (String title : titleList) {
            if (!newTitleList.contains(title)) {
                deleteTitleList.add(title);
            }
        }
        if (deleteTitleList != null && !deleteTitleList.isEmpty()) {
            for (String title : deleteTitleList) {
                m_teacherRoleMapper.deleteTeacherRoleBySchool(mTeacherUserVO.getSchool(), title);
            }
            //如果原职称中包括班主任，那么就把这个教师的班主任信息设置为否
            if (deleteTitleList.contains("班主任")) {
                m_classTeacherRelationMapper.setHeadTeacherNoBySchoolAndTeacherName(mTeacherUserVO.getSchool(), mTeacherUserVO.getTeacherName());
            }
        }

        //找出需要新增的职称
        List<String> addTitleList = new ArrayList<>();
        for (String title : newTitleList) {
            if (!titleList.contains(title)) {
                addTitleList.add(title);
            }
        }
        if (addTitleList == null || addTitleList.isEmpty()) {
            return;
        }
        List<M_TeacherRole> teacherRoleList = new ArrayList<>();
        for (String title : addTitleList) {
            M_TeacherRole teacherRole = new M_TeacherRole();
            teacherRole.setUserId(mTeacherUserVO.getId());
            teacherRole.setTitle(title);
            teacherRole.setTeacherName(mTeacherUserVO.getTeacherName());
            teacherRole.setSchool(mTeacherUserVO.getSchool());
            teacherRoleList.add(teacherRole);
        }
        m_teacherRoleMapper.batchInsertTeacherRole(teacherRoleList);


    }

    private void editClassTeacher(M_TeacherUserVO mTeacherUserVO) {
        List<String> classNameList = m_classTeacherRelationMapper.getClassNameBySchoolTeacherName(mTeacherUserVO.getSchool(), mTeacherUserVO.getTeacherName());
        List<String> newClassNameList = mTeacherUserVO.getClassNameList();
        if (newClassNameList == null || newClassNameList.isEmpty()) {
            //删掉所有的班级
            m_classTeacherRelationMapper.deleteClassNameBySchoolAndTeacherName(mTeacherUserVO.getSchool(), mTeacherUserVO.getTeacherName());
            return;
        }
        if (classNameList == null || classNameList.isEmpty()) {
            if (newClassNameList == null || newClassNameList.isEmpty()) {
                return;
            } else {

                List<M_ClassTeacherRelation> mClassTeacherRelationList = getClassTeacherRelationList(mTeacherUserVO);

                m_classTeacherRelationMapper.insetClassTeacherRelationList(mClassTeacherRelationList);
                return;
            }
        }

        //找出需要删除的班级
        List<String> deleteClassNameList = new ArrayList<>();
        for (String className : classNameList) {
            if (!newClassNameList.contains(className)) {
                deleteClassNameList.add(className);
            }
        }

        if (deleteClassNameList != null && !deleteClassNameList.isEmpty()) {
            m_classTeacherRelationMapper.deleteRelationBySchoolAndClassNameList(mTeacherUserVO.getSchool(), deleteClassNameList);
        }


        //找出需要新增的班级
        List<String> addClassNameList = new ArrayList<>();
        for (String className : newClassNameList) {
            if (!classNameList.contains(className)) {
                addClassNameList.add(className);
            }
        }

        if (addClassNameList == null || addClassNameList.isEmpty()) {
            return;
        }
        mTeacherUserVO.setClassNameList(addClassNameList);
        List<M_ClassTeacherRelation> mClassTeacherRelationList = getClassTeacherRelationList(mTeacherUserVO);
        m_classTeacherRelationMapper.insetClassTeacherRelationList(mClassTeacherRelationList);

    }

    private List<M_ClassTeacherRelation> getClassTeacherRelationList(M_TeacherUserVO mTeacherUserVO) {
        List<M_ClassTeacherRelation> mClassTeacherRelationList = new ArrayList<>();
        for (String className : mTeacherUserVO.getClassNameList()) {
            M_ClassTeacherRelation classTeacherRelation = new M_ClassTeacherRelation();
            Integer teacherId = mTeacherListMapper.getTeacherId(mTeacherUserVO.getSchool(), mTeacherUserVO.getTeacherName());
            Integer classId = mGradeClassNumMapper.getClassIdByGradeAndClassName(mTeacherUserVO.getSchool(), className);
            classTeacherRelation.setTeacherId(teacherId);
            classTeacherRelation.setClassId(classId);
            classTeacherRelation.setSchool(mTeacherUserVO.getSchool());
            classTeacherRelation.setTeacherName(mTeacherUserVO.getTeacherName());
            classTeacherRelation.setClassName(className);
            classTeacherRelation.setHeadTeacher("否");
            mClassTeacherRelationList.add(classTeacherRelation);
        }
        return mClassTeacherRelationList;
    }

    private String updateClassTeacherRelation(List<M_ClassTeacherRelation> classTeacherRelationList) {
        String resp = "";
        //先删除所有的班主任信息
        String isheadTeacher = "是";
        m_classTeacherRelationMapper.deleteAllHeadTeacher(classTeacherRelationList.get(0).getSchool(), isheadTeacher);

        //删除所有班主任职称信息
        String title = "班主任";
        m_teacherRoleMapper.deleteTeacherRoleBySchool(classTeacherRelationList.get(0).getSchool(), title);

        //先看看teacherList表中有没有这个教师，如果没有，则不添加，并提醒管理员这个教师没有用户信息，如果有，则添加班主任信息到classTeacherRelation表中
        for (M_ClassTeacherRelation classTeacherRelation : classTeacherRelationList) {
            String subject = mTeacherListMapper.getSubjectBySchoolAndTeacherName(classTeacherRelation.getSchool(), classTeacherRelation.getTeacherName());
            if (subject == null) {
                continue;
            }

            Integer teacherId = mTeacherListMapper.getTeacherId(classTeacherRelation.getSchool(), classTeacherRelation.getTeacherName());
            Integer classId = mGradeClassNumMapper.getClassIdByGradeAndClassName(classTeacherRelation.getSchool(), classTeacherRelation.getClassName());

            classTeacherRelation.setTeacherId(teacherId);
            classTeacherRelation.setClassId(classId);
            classTeacherRelation.setHeadTeacher("是");
            m_classTeacherRelationMapper.deleteRelationByTeacherIdAndClassId(teacherId, classId);
            m_classTeacherRelationMapper.insetClassTeacherRelation(classTeacherRelation);

            //新增班主任职称信息到教师职称表中
            M_TeacherRole teacherRole = new M_TeacherRole();
            M_User mUser = m_userMapper.getTeacherUserBySchoolAndTeacherName(classTeacherRelation.getSchool(), classTeacherRelation.getTeacherName());
            if (mUser == null) {
                continue;
            }
            teacherRole.setUserId(mUser.getId());
            teacherRole.setTitle("班主任");
            teacherRole.setTeacherName(classTeacherRelation.getTeacherName());
            teacherRole.setSchool(classTeacherRelation.getSchool());
            m_teacherRoleMapper.batchInsertTeacherRole(List.of(teacherRole));
        }


        return resp;
    }

    private HashMap<String, List<M_TeacherRole>> findNotHeadTeacherToAddHeadTeacherRole(List<M_ClassTeacherRelation> classTeacherRelationList) {

        HashMap<String, List<M_TeacherRole>> teacherRoleMap = new HashMap<>();
        List<M_TeacherRole> addHeadTeacherRoleList = new ArrayList<>();
        List<M_TeacherRole> noUserList = new ArrayList<>();

        for (M_ClassTeacherRelation classTeacherRelation : classTeacherRelationList) {
            String headTeacher = "班主任";
            M_User user = m_userMapper.getTeacherUserBySchoolAndTeacherName(classTeacherRelation.getSchool(), classTeacherRelation.getTeacherName());
            if (user != null) {
                M_TeacherRole existingRole = m_teacherRoleMapper.getTeacherRoleByUserIdAndTitle(user.getId(), headTeacher);
                if (existingRole == null) {
                    M_TeacherRole teacherRole = new M_TeacherRole();
                    teacherRole.setUserId(user.getId());
                    teacherRole.setTitle(headTeacher);
                    teacherRole.setTeacherName(classTeacherRelation.getTeacherName());
                    teacherRole.setSchool(classTeacherRelation.getSchool());
                    addHeadTeacherRoleList.add(teacherRole);
                }
            } else {
                M_TeacherRole teacherRole = new M_TeacherRole();
                teacherRole.setTeacherName(classTeacherRelation.getTeacherName());
                teacherRole.setSchool(classTeacherRelation.getSchool());
                noUserList.add(teacherRole);
            }

        }
        teacherRoleMap.put("新增班主任", addHeadTeacherRoleList);
        teacherRoleMap.put("没有用户的教师", noUserList);
        return teacherRoleMap;
    }

    private boolean isValidClassTeacherRelation(M_ClassTeacherRelation classTeacherRelation) {
        //教师姓名不能为空
        if (classTeacherRelation.getTeacherName() == null || classTeacherRelation.getTeacherName().trim().isEmpty()) {
            return false;
        }

        //班级名称不能为空
        if (classTeacherRelation.getClassName() == null || classTeacherRelation.getClassName().trim().isEmpty()) {
            return false;
        }

        //学校不能为空
        if (classTeacherRelation.getSchool() == null || classTeacherRelation.getSchool().trim().isEmpty()) {
            //可以设置默认学校
            classTeacherRelation.setSchool("附小");
        }

        return true;
    }

    private M_ClassTeacherRelation parseRowToHeadTeacherClass(Row row) {
        try {
            M_ClassTeacherRelation classTeacherRelation = new M_ClassTeacherRelation();

            //第一列为年级+第二列为班级数字，前端返回的班级是1,2,3等数字，需要转换成整数，再拼接成年级+班级的格式
            String grade = row.getCell(0).getStringCellValue().trim();
            //如果年级不是以年级结尾，则返回错误
            if (!grade.endsWith("年级")) {
                throw new IllegalArgumentException("年级格式错误，应该以年级结尾");
            }

            //第三列为教师姓名
            classTeacherRelation.setTeacherName(row.getCell(2).getStringCellValue().trim());

            String classNum = null;
            //第二列为班级名称,前端返回的班级是1,2,3等数字，需要转换成整数
            if (row.getCell(1).getCellType() == org.apache.poi.ss.usermodel.CellType.NUMERIC) {
                classNum = String.valueOf((int) row.getCell(1).getNumericCellValue());
            } else {
                classNum = row.getCell(1).getStringCellValue().trim();
            }

            classTeacherRelation.setClassName(grade.substring(0, 1) + "(" + classNum + ")班");
            //第四列为学校
            if (row.getCell(3) == null || row.getCell(3).getStringCellValue().trim().isEmpty()) {
                classTeacherRelation.setSchool("附小");
            } else {
                classTeacherRelation.setSchool(row.getCell(3).getStringCellValue().trim());
            }

            return classTeacherRelation;

        } catch (Exception e) {
            System.err.println("解析第 " + (row.getRowNum() + 1) + " 行数据时发生错误: " + e.getMessage());
            return null;
        }
    }

    /**
     * 验证学生用户数据是否有效，并且设置默认值（如果需要）
     *
     * @param user
     * @return
     */
    private boolean isValidStudentUser(M_User user) {
        // 姓名不能为空
        if (user.getName() == null || user.getName().trim().isEmpty()) {
            return false;
        }

        // 密码不能为空，如果没有提供密码，可以设置默认密码
        if (user.getPassword() == null || user.getPassword().trim().isEmpty()) {
            user.setPassword("123"); // 默认密码
        }

        // 手机号不能为空
        if (user.getPhone() == null || user.getPhone().trim().isEmpty()) {
            return false;
        }

        // 学校不能为空，可以设置默认学校
        if (user.getSchool() == null || user.getSchool().trim().isEmpty()) {
            user.setSchool("附小"); // 默认学校
        }

        //年级不能为空
        if (user.getGrade() == null || user.getGrade().trim().isEmpty()) {
            return false;
        }

        //班级不能为空
        if (user.getClassName() == null) {
            return false;
        }

        return true;
    }


    /**
     * 管理员根据学校获取教师列表
     *
     * @param school
     * @return
     */
    @Override
    public List<M_TeacherUserVO> getTeacherUserList(String school) {

        //拿到用户表的数据
        List<M_TeacherUserVO> mTeacherUserVOList = m_userMapper.getTeacherUserListBySchool(school);
        if (mTeacherUserVOList != null && !mTeacherUserVOList.isEmpty()) {
            for (M_TeacherUserVO mTeacherUserVO : mTeacherUserVOList) {
                //拿到教师职称表的数据
                List<String> titleList = m_teacherRoleMapper.getTeacherRoleByUserId(mTeacherUserVO.getId());
                mTeacherUserVO.setTitleList(titleList);
                //拿到classTeacher表的className
                List<String> classNameList = m_classTeacherRelationMapper.getClassNameBySchoolTeacherName(mTeacherUserVO.getSchool(), mTeacherUserVO.getTeacherName());
                //拿到年级列表
                List<String> gradeList = new ArrayList<>();
                HashSet<String> gradeSet = new HashSet<>();
                for (String className : classNameList) {
                    String grade = className.substring(0, 1) + "年级";
                    gradeSet.add(grade);
                }
                gradeList.addAll(gradeSet);
                mTeacherUserVO.setGradeList(gradeList);
                mTeacherUserVO.setClassNameList(classNameList);
                //拿到教师的科目
                String subject = mTeacherListMapper.getSubjectBySchoolAndTeacherName(mTeacherUserVO.getSchool(), mTeacherUserVO.getTeacherName());
                mTeacherUserVO.setSubject(subject);


            }
        }

        return mTeacherUserVOList;
    }

    /**
     * 管理员根据学校获取学生列表
     *
     * @param school
     * @return
     */
    @Override
    public List<M_StudentUserVO> getStudentUserList(String school) {
        //拿到用户表的数据,name,password,phone,school,id
        List<M_StudentUserVO> mStudentUserVOList = m_userMapper.getStudentUserListBySchool(school);
        for (M_StudentUserVO mStudentUserVO : mStudentUserVOList) {
            //拿到学生的班级信息，根据userId,拿到grade, className
            M_StudentInfo mStudentInfo = mStudentInfoMapper.getAllByUserId(mStudentUserVO.getId());

            if (mStudentInfo != null) {
                String grade = mStudentInfo.getGrade();
                Integer classNameInt = mStudentInfo.getClassName();
                String className = grade.substring(0, 1) + "(" + classNameInt + ")班";
                mStudentUserVO.setStudentNumber(mStudentInfo.getStudentNumber());
                mStudentUserVO.setGrade(grade);
                mStudentUserVO.setClassName(className);
            }
        }
        //按照姓名字母顺序排序
        Collator collator = Collator.getInstance(Locale.CHINESE);
// 按姓名拼音升序
        mStudentUserVOList.sort((s1, s2) -> collator.compare(s1.getStudentName(), s2.getStudentName()));

        return mStudentUserVOList;
    }


    /**
     * 解析学生信息的每一行数据，得到一个学生对象
     *
     * @param row
     * @return
     */
    private M_User parseRowToStudentUser(Row row) {

        try {
            M_User user = new M_User();

            //第一列为姓名
            user.setName(row.getCell(0).getStringCellValue().trim());

            //第二列为密码
            //如果用户设置密码为数字，那么就把它转换成字符串
            if (row.getCell(1) == null) {
                user.setPassword("123");
            } else {
                if (row.getCell(1).getCellType() == org.apache.poi.ss.usermodel.CellType.NUMERIC) {
                    user.setPassword(String.valueOf((int) row.getCell(1).getNumericCellValue()));
                } else {
                    user.setPassword(row.getCell(1).getStringCellValue().trim());
                }
            }

            //第三列为手机号
            //如果用户设置手机号为数字，那么就把它转换成字符串，字符串只能11个字符，如果不是的则设置为空字符串
            if (row.getCell(2).getCellType() == org.apache.poi.ss.usermodel.CellType.NUMERIC) {
                String phone = String.valueOf((long) row.getCell(2).getNumericCellValue());
                user.setPhone(phone);
            } else {
                if (row.getCell(2).getStringCellValue().endsWith(";")) {
                    user.setPhone(row.getCell(2).getStringCellValue().trim().substring(0, row.getCell(2).getStringCellValue().trim().length() - 1));
                    user.setPhone(row.getCell(2).getStringCellValue().trim());
                } else {
                    user.setPhone(row.getCell(2).getStringCellValue().trim());
                }
            }

            //第四列为年级
            user.setGrade(row.getCell(3).getStringCellValue().trim());
            //第五列为班级,前端返回的班级是1,2,3等数字，需要转换成整数
            if (row.getCell(4).getCellType() == org.apache.poi.ss.usermodel.CellType.NUMERIC) {
                user.setClassName((int) row.getCell(4).getNumericCellValue());
            } else {
                user.setClassName(Integer.parseInt(row.getCell(4).getStringCellValue().trim()));
            }

            //第六列为学校
            if (row.getCell(5) == null || row.getCell(5).getStringCellValue().trim().isEmpty()) {
                user.setSchool("附小");
            } else {
                user.setSchool(row.getCell(5).getStringCellValue().trim());
            }
            //第七列为角色
            user.setRole("学生");
            return user;
        } catch (Exception e) {
            System.err.println("解析第 " + (row.getRowNum() + 1) + " 行数据时发生错误: " + e.getMessage());
            return null;
        }

    }


    /**
     * 把教师表的每一行数据解析成一个教师对象
     *
     * @param row
     * @return
     */

    private M_User parseRowToUser(Row row) {
        try {
            M_User user = new M_User();

            //第一列为姓名
            user.setName(row.getCell(0).getStringCellValue().trim());
            if (row.getCell(0).getStringCellValue().endsWith("年级")) {
                //报错
                throw new IllegalArgumentException("教师姓名格式错误，教师姓名不能以年级结尾");
            }

            //第二列为密码
            //如果用户设置密码为数字，那么就把它转换成字符串
            //先判断单元格有没有值，如果没有值，就设置为空字符串
            if (row.getCell(1) == null) {
                user.setPassword("xtxx");
            } else {
                if (row.getCell(1).getCellType() == org.apache.poi.ss.usermodel.CellType.NUMERIC) {
                    user.setPassword(String.valueOf((int) row.getCell(1).getNumericCellValue()));
                } else {
                    user.setPassword(row.getCell(1).getStringCellValue().trim());
                }
            }

            //第三列为手机号
            //如果用户设置手机号为数字，那么就把它转换成字符串，字符串只能11个字符，如果不是的则设置为空字符串
            if (row.getCell(2).getCellType() == org.apache.poi.ss.usermodel.CellType.NUMERIC) {
                String phone = String.valueOf((long) row.getCell(2).getNumericCellValue());
                user.setPhone(phone.length() == 11 ? phone : "");
            } else {
                user.setPhone(row.getCell(2).getStringCellValue().trim());
            }

            //第四列为学校
            if (row.getCell(3) == null || row.getCell(3).getStringCellValue().trim().isEmpty()) {
                user.setSchool("附小");
            } else {
                user.setSchool(row.getCell(3).getStringCellValue().trim());
            }

            //第五列为角色
            user.setRole("教师");

            if (row.getCell(4) == null || row.getCell(4).getStringCellValue().trim().isEmpty()) {
                user.setTitleList(new ArrayList<>());
            } else {
                String titleStr = row.getCell(4).getStringCellValue().trim();
                List<String> titleList = parseTileStrToList(titleStr);
                user.setTitleList(titleList);
            }
            return user;

        } catch (Exception e) {
            System.err.println("解析第 " + (row.getRowNum() + 1) + " 行数据时发生错误: " + e.getMessage());
            return null;
        }


    }

    /**
     * 把教师的职称字符串解析成一个职称列表，职称之间用;分隔
     *
     * @param titleStr
     * @return
     */
    private List<String> parseTileStrToList(String titleStr) {
        //如果职称字符串为空或者只包含空格，那么就返回一个空列表
        if (titleStr == null || titleStr.trim().isEmpty()) {
            return new ArrayList<>();
        }
        //否则就把职称字符串按照;分隔成一个职称列表，并且去掉每个职称的前后空格
        //遇到以;结尾，则不添加
        //如果职称是教师，那么就不添加，因为教师是默认角色，不需要添加到职称列表中
        String[] titleArray = titleStr.split(";");
        List<String> titleList = new ArrayList<>();
        for (String title : titleArray) {
            if (!title.trim().isEmpty()) {
                if (title.equals("教师")) {
                    continue;
                }
                titleList.add(title.trim());
            }
        }
        return titleList;
    }

    /**
     * 验证用户数据是否有效，并且设置默认值（如果需要）
     *
     * @param user
     * @return
     */
    private boolean isValidUser(M_User user) {
        // 姓名不能为空
        if (user.getName() == null || user.getName().trim().isEmpty()) {
            return false;
        }

        // 密码不能为空，如果没有提供密码，可以设置默认密码
        if (user.getPassword() == null || user.getPassword().trim().isEmpty()) {
            user.setPassword("xtxx"); // 默认密码
        }

        // 手机号不能为空
        if (user.getPhone() == null || user.getPhone().trim().isEmpty()) {
            return false;
        }

        // 学校不能为空，可以设置默认学校
        if (user.getSchool() == null || user.getSchool().trim().isEmpty()) {
            user.setSchool("附小"); // 默认学校
        }


        return true;
    }

}
