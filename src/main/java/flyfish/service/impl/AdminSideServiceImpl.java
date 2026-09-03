//package flyfish.service.impl;
//
//import flyfish.mapper.*;
//import flyfish.pojo.*;
//import flyfish.pojo.DTO.QueryPassTaskDTO;
//import flyfish.pojo.DTO.RecordQueryDTO;
//import flyfish.pojo.Record;
//import flyfish.pojo.VO.RecordVO;
//import flyfish.service.AdminSideService;
//import flyfish.service.PassTaskService;
//import lombok.extern.slf4j.Slf4j;
//import org.apache.poi.ss.usermodel.*;
//import org.apache.poi.xssf.usermodel.XSSFWorkbook;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.HttpHeaders;
//import org.springframework.http.MediaType;
//import org.springframework.http.ResponseEntity;
//import org.springframework.stereotype.Service;
//import org.springframework.web.multipart.MultipartFile;
//
//import java.io.ByteArrayOutputStream;
//import java.io.FileNotFoundException;
//import java.io.IOException;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.stream.Collectors;
//
//@Slf4j
//@Service
//public class AdminSideServiceImpl implements AdminSideService {
//
//    @Autowired
//    private ParentPasswordMapper parentPasswordMapper;
//    @Autowired
//    private StudentInfoMapper studentInfoMapper;
//    @Autowired
//    private RecordMapper recordMapper;
//    @Autowired
//    private PassTaskMapper passTaskMapper;
//    @Autowired
//    private RecordTaskMapper recordTaskMapper;
//    /**
//     * 传新的密码文件
//     * @param file
//     * @return
//     */
//    @Override
//    public String addPassWordFile(MultipartFile file) throws IOException {
//        List<ParentPassword> parentPasswords = new ArrayList<>();
//        Workbook workbook = WorkbookFactory.create(file.getInputStream());
//        Sheet sheet = workbook.getSheetAt(0);
//        // 假设第一行是列名，从第二行开始处理数据
//        boolean isFirstRow = true;
//        for (Row row : sheet) {
//            if (isFirstRow) {
//                isFirstRow = false; // 跳过第一行
//                continue;
//            }
//            if(row.getCell(0) == null || row.getCell(1) == null || row.getCell(2) == null || row.getCell(0).getCellType() == CellType.BLANK || row.getCell(1).getCellType() == CellType.BLANK || row.getCell(2).getCellType() == CellType.BLANK){
//                continue;
//            }
//
//
//
//            ParentPassword parentPassword = new ParentPassword();
//            if(row.getCell(0).getCellType() == CellType.STRING){
//                parentPassword.setClassNumber(row.getCell(0).getStringCellValue());
//            } else {
//                int classNumber = (int) row.getCell(0).getNumericCellValue();
//                parentPassword.setClassNumber(String.valueOf(classNumber));
//            }
//
//            parentPassword.setName(row.getCell(1).getStringCellValue());
//
//            if(row.getCell(2).getCellType() == CellType.STRING){
//            parentPassword.setPassword(row.getCell(2).getStringCellValue());
//            } else {
//                int password = (int) row.getCell(2).getNumericCellValue();
//                parentPassword.setPassword(String.valueOf(password));
//            }
//
//            parentPassword.setQueryTime(0);
//            parentPasswords.add(parentPassword);
//        }
//        workbook.close();
//
//
//        if(parentPasswords!=null && parentPasswords.size()>0) {
//            List<String> names = studentInfoMapper.getallName(parentPasswords.get(0).getClassNumber());
//
//            // 使用流API来过滤和收集数据
//            List<ParentPassword> matchedParentPasswords = parentPasswords.stream()
//                    .filter(pp -> names.contains(pp.getName()))
//                    .collect(Collectors.toList());
//
//            List<ParentPassword> unmatchedParentPasswords = parentPasswords.stream()
//                    .filter(pp -> !names.contains(pp.getName()))
//                    .collect(Collectors.toList());
//
//
//
//            //先查询数据库是否有数据表，如果没有直接新增，如果有的话先删除再新增
//           String firstName = parentPasswordMapper.isexist(parentPasswords.get(0).getClassNumber(),parentPasswords.get(0).getName());
//                    if(firstName!=null && !firstName.equals("")){
//                        parentPasswordMapper.deleteByClass(parentPasswords.get(0).getClassNumber());
//                    }
//                    if(matchedParentPasswords!=null && matchedParentPasswords.size()>0){
//                        parentPasswordMapper.addNewFile(matchedParentPasswords);
//                    }else {
//                        return "没有匹配到学生";
//                    }
//
//                    //没有匹配的学生姓名名单
//                    if(unmatchedParentPasswords!=null && unmatchedParentPasswords.size()>0){
//                        String result = "";
//                        for (ParentPassword p:unmatchedParentPasswords
//                             ) {
//                            result += p.getName() +"、";
//                        }
//                        result = "没有匹配的学生为："+result;
//                        return result;
//
//                    }else{
//                        return "全部学生密码上传完毕";
//                    }
//
//        }else {
//            throw new FileNotFoundException("文件为空");
//        }
//
//
//
//    }
//
//    /**
//     * 根据班级和人名查询学生家长
//     * @param classNumber
//     * @param studentName
//     * @return
//     */
//    @Override
//    public List<EditStudentForm> getALLContent(String classNumber, String studentName) {
//        List<ParentPassword> parentPasswordList = parentPasswordMapper.getAllContentByCLassName(classNumber,studentName);
//        List<EditStudentForm> editStudentFormList = new ArrayList<>();
//        for (ParentPassword pp: parentPasswordList
//             ) {
//            EditStudentForm editStudentForm = new EditStudentForm();
//            editStudentForm.setId(pp.getId());
//            editStudentForm.setClassName(pp.getClassNumber());
//            editStudentForm.setStudentName(pp.getName());
//            editStudentForm.setPassword(pp.getPassword());
//            editStudentForm.setQueryCount(pp.getQueryTime());
//            editStudentFormList.add(editStudentForm);
//        }
//        return editStudentFormList;
//    }
//
//    /**
//     * 新增用户名单
//     * @param editStudentForm
//     * @return
//     */
//    @Override
//    public EditStudentForm addstudentPassword(EditStudentForm editStudentForm) {
//
//        ParentPassword parentPassword = new ParentPassword();
//        parentPassword.setClassNumber(editStudentForm.getClassName());
//        parentPassword.setPassword(editStudentForm.getPassword());
//        parentPassword.setName(editStudentForm.getStudentName());
//        parentPassword.setQueryTime(editStudentForm.getQueryCount());
//        Integer id = parentPasswordMapper.addStudentPassword(parentPassword);
//        editStudentForm.setId(id);
//        return editStudentForm;
//
//
//
//    }
//
//    /**
//     * 编辑学生用户
//     * @param editStudentForm
//     * @return
//     */
//    @Override
//    public String editstudentPassword(EditStudentForm editStudentForm) {
//        ParentPassword parentPassword = new ParentPassword();
//        parentPassword.setClassNumber(editStudentForm.getClassName());
//        parentPassword.setPassword(editStudentForm.getPassword());
//        parentPassword.setName(editStudentForm.getStudentName());
//        parentPassword.setQueryTime(editStudentForm.getQueryCount());
//        parentPassword.setId(editStudentForm.getId());
//        parentPasswordMapper.editstudentPassword(parentPassword);
//        String result = editStudentForm.getStudentName();
//        return result;
//
//
//    }
//
//    /**
//     * 删除学生用户
//     * @param id
//     */
//    @Override
//    public void deleteClassPassword(Integer id) {
//        parentPasswordMapper.deleteById(id);
//    }
//
//    /**
//     * 获取所有的学生信息
//     * @param classNumber
//     * @param name
//     * @return
//     */
//    @Override
//    public List<StudentInfo> getALLStudentInfoContent(String classNumber, String name) {
//        List<StudentInfo> studentInfoList = studentInfoMapper.getAllContentByCLassName(classNumber,name);
//        return studentInfoList;
//
//
//    }
//
//    /**
//     * 修改学生信息
//     * @param studentInfo
//     * @return
//     */
//    @Override
//    public String editstudentInfo(StudentInfo studentInfo) {
//
//        studentInfoMapper.editstudentInfo(studentInfo);
//        String result = studentInfo.getName();
//        return result;
//
//    }
//
//
//    /**
//     * 删除学生信息
//     * @param id
//     */
//    @Override
//    public void deleteClassInfo(Integer id) {
//        studentInfoMapper.deleteById(id);
//    }
//
//    /**
//     * 新增学生信息
//     * @param studentInfo
//     * @return
//     */
//    @Override
//    public StudentInfo addstudentInfo(StudentInfo studentInfo) {
//
//        Integer id = studentInfoMapper.addStudentInfo(studentInfo);
//        studentInfo.setId(id);
//        return studentInfo;
//
//
//    }
//
//    /**
//     * 查询所有作业表现情况
//     * @param recordQueryDTO
//     * @return
//     */
//    @Override
//    public List<RecordVO> getALLRecordContent(RecordQueryDTO recordQueryDTO) {
//        List<RecordVO> recordList = recordMapper.getAllRecord(recordQueryDTO);
//        return recordList;
//    }
//
//    /**
//     * 编辑作业表现
//     * @param recordVO
//     * @return
//     */
//    @Override
//    public String editRecordContent(RecordVO recordVO) {
//        recordMapper.editRecord(recordVO);
//        String result = recordVO.getName();
//        return result;
//    }
//
//
//    /**
//     * 新增作业表现
//     * @param recordVO
//     * @return
//     */
//    @Override
//    public RecordVO addRecordContent(RecordVO recordVO) {
//        Integer id = recordMapper.addRecord(recordVO);
//        recordVO.setId(id);
//        return recordVO;
//
//    }
//
//    /**
//     * 删除作业表现
//     * @param ids
//     */
//    @Override
//    public void deleteRecord(List<Integer> ids) {
//        recordMapper.deleteRecord(ids);
//    }
//
//    /**
//     * 根据id查询所有的记录
//     * @param ids
//     * @return
//     */
//    @Override
//    public List<Record> findAllById(List<Integer> ids) {
//        List<Record> recordList = recordMapper.findAllById(ids);
//        return recordList;
//    }
//
//    /**
//     * 导出记录
//     * @param records
//     * @return
//     */
//    @Override
//    public ResponseEntity<byte[]> exportRecords(List<Record> records) throws IOException {
//        // 创建一个新的工作簿
//        Workbook workbook = new XSSFWorkbook();
//        // 创建一个工作表
//        Sheet sheet = workbook.createSheet("Records");
//
//        // 创建表头行
//        Row headerRow = sheet.createRow(0);
//        headerRow.createCell(0).setCellValue("序号");
//        headerRow.createCell(1).setCellValue("班级");
//        headerRow.createCell(2).setCellValue("日期");
//        headerRow.createCell(3).setCellValue("学科");
//        headerRow.createCell(4).setCellValue("学生姓名");
//        headerRow.createCell(5).setCellValue("作业内容");
//        headerRow.createCell(6).setCellValue("完成情况");
//        headerRow.createCell(7).setCellValue("等级");
//        // 根据你的 Record 类，继续添加其他字段
//
//        // 填充数据行
//        int rowNum = 1;
//        for (Record record : records) {
//            Row row = sheet.createRow(rowNum++);
//            row.createCell(0).setCellValue(record.getId());
//            row.createCell(1).setCellValue(record.getClassNumber());
//            row.createCell(2).setCellValue(record.getCheckdate().toString());
//            row.createCell(3).setCellValue(record.getSubject());
//            row.createCell(4).setCellValue(record.getName());
//            row.createCell(5).setCellValue(record.getContent());
//            if(record.getCompleted() == null){
//                row.createCell(6).setCellValue("没有定义");
//            }else if(record.getCompleted() == 1){
//                row.createCell(6).setCellValue("完成");}
//            else if (record.getCompleted() == 0){
//                row.createCell(6).setCellValue("未完成");
//
//            }else{
//                row.createCell(6).setCellValue(record.getCompleted());
//            }
//
//            if (record.getLevel() == null){
//                row.createCell(7).setCellValue("没有定义");}
//            else if (record.getLevel() == 1){
//                row.createCell(7).setCellValue("优秀作业");}
//            else if (record.getLevel() == 0){
//                row.createCell(7).setCellValue("合格");}
//            else if (record.getLevel() == -1){
//                row.createCell(7).setCellValue("不达标作业");}
//          else{
//                row.createCell(7).setCellValue(record.getLevel());}
//
//            // 根据你的 Record 类，继续添加其他字段
//        }
//
//        // 将数据写入 ByteArrayOutputStream
//        ByteArrayOutputStream out = new ByteArrayOutputStream();
//        workbook.write(out);
//        workbook.close();
//
//        // 设置响应头
//        HttpHeaders headers = new HttpHeaders();
//        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
//        headers.setContentDispositionFormData("attachment", "records.xlsx");
//
//        // 返回响应实体
//        return ResponseEntity.ok()
//                .headers(headers)
//                .contentType(MediaType.APPLICATION_OCTET_STREAM)
//                .body(out.toByteArray());
//    }
//
//
//    /**
//     * 查询所有的班级
//     * @return
//     */
//    @Override
//    public List<String> autoqueryclassNumber() {
//
//        List<String> classNumberList = studentInfoMapper.autoqueryclassNumber();
//        return classNumberList;
//    }
//
//    /**
//     * 查询所有的学生姓名
//
//     * @return
//     */
//    @Override
//    public List<String> autoqueryname() {
//        List<String> nameList = studentInfoMapper.autoqueryname();
//        return nameList;
//    }
//
//    /**
//     * 自动查询所有过关内容
//     * @return
//     */
//    @Override
//    public List<String> autoqueryPasscontent() {
//        List<String> contentList = passTaskMapper.autoqueryPasscontent();
//        return contentList;
//    }
//
//    /**
//     * 查询所有的过关任务
//     * @param queryPassTaskDTO
//     * @return
//     */
//    @Override
//    public List<PassTask> getALLPassContent(QueryPassTaskDTO queryPassTaskDTO) {
//        List<PassTask> passTaskList = passTaskMapper.queryALlPassContent(queryPassTaskDTO);
//        return passTaskList;
//    }
//
//    /**
//     * 新增学生过关情况的数据
//     * @param passTask
//     * @return
//     */
//    @Override
//    public PassTask addRecordPass(PassTask passTask) {
//        Integer id = passTaskMapper.addRecordPass(passTask);
//        passTask.setId(id);
//        return passTask;
//
//    }
//
//    /**
//     * x修改学生过关情况的数据
//     * @param passTask
//     * @return
//     */
//    @Override
//    public String editRecordPass(PassTask passTask) {
//        passTaskMapper.editRecordPass(passTask);
//        String result = passTask.getName();
//        return result;
//    }
//
//    @Override
//    public void deleteRecordPass(List<Integer> ids) {
//        passTaskMapper.deleteByIDs(ids);
//    }
//
//    @Override
//    public List<PassTask> findAllPassById(List<Integer> ids) {
//        List<PassTask> passTaskList = recordTaskMapper.queryALlPassContent(ids);
//        return passTaskList;
//    }
//
//    @Override
//    public ResponseEntity<byte[]> exportRecordsPass(List<PassTask> records) {
//        Workbook workbook = new XSSFWorkbook();
//        Sheet sheet = workbook.createSheet("RecordPass");
//
//
//        Row headerRow = sheet.createRow(0);
//        headerRow.createCell(0).setCellValue("序号");
//        headerRow.createCell(1).setCellValue("班级");
//        headerRow.createCell(2).setCellValue("日期");
//        headerRow.createCell(3).setCellValue("学科");
//        headerRow.createCell(4).setCellValue("学生姓名");
//        headerRow.createCell(5).setCellValue("作业内容");
//        headerRow.createCell(6).setCellValue("完成情况");
//        headerRow.createCell(7).setCellValue("参考链接");
//
//        int rowNum = 1;
//        for (PassTask record : records) {
//            Row row = sheet.createRow(rowNum++);
//            row.createCell(0).setCellValue(record.getId());
//            row.createCell(1).setCellValue(record.getClassNumber());
//            row.createCell(2).setCellValue(record.getCheckdate().toString());
//            row.createCell(3).setCellValue(record.getSubject());
//            row.createCell(4).setCellValue(record.getName());
//            row.createCell(5).setCellValue(record.getContent());
//            if (record.getCompleted() == null) {
//                row.createCell(6).setCellValue("没有定义");
//            } else if (record.getCompleted() == 1) {
//                row.createCell(6).setCellValue("完成");
//            } else if (record.getCompleted() == 0) {
//                row.createCell(6).setCellValue("未完成");
//
//            } else {
//                row.createCell(6).setCellValue(record.getCompleted());
//            }
//            if (record.getSupplementary() == null) {
//                row.createCell(7).setCellValue("没有链接");
//            } else {
//                row.createCell(7).setCellValue(record.getSupplementary());
//            }
//        }
//
//        ByteArrayOutputStream out = new ByteArrayOutputStream();
//        try {
//            workbook.write(out);
//            workbook.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//        // 设置响应头
//        HttpHeaders headers = new HttpHeaders();
//        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
//        headers.setContentDispositionFormData("attachment", "recordPass.xlsx");
//
//        // 返回响应实体
//        return ResponseEntity.ok()
//                .headers(headers)
//                .contentType(MediaType.APPLICATION_OCTET_STREAM)
//                .body(out.toByteArray());
//    }
//
//}
