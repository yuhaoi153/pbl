package flyfish.service.impl;

import flyfish.exception.StringException;
import flyfish.mapper.M_ClubMapper;
import flyfish.mapper.M_ClubResultMapper;
import flyfish.mapper.M_ClubStudentInfoMapper;
import flyfish.mapper.M_UserMapper;
import flyfish.pojo.DTO.M_ClubChooseDTO;
import flyfish.pojo.M_Club;
import flyfish.pojo.M_ClubStudentInfo;
import flyfish.pojo.M_User;
import flyfish.pojo.VO.*;
import flyfish.service.M_ClubService;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;


@Service
@Slf4j
public class M_ClubServiceImpl implements M_ClubService {
    @Autowired
    private M_ClubMapper m_clubMapper;
    @Autowired
    private M_ClubStudentInfoMapper m_clubStudentInfoMapper;
    @Autowired
    private M_ClubStudentInfoMapper m_ClubStudentInfoMapper;
    @Autowired
    private M_ClubResultMapper m_clubResultMapper;
    @Autowired
    private M_UserMapper m_userMapper;


    @Override
    public List<ClubVO> getClubInfo(String admissionSemester, String grade, Boolean isActive) {
        List<ClubVO> clubVOList = m_clubMapper.getClubInfo(admissionSemester, grade, isActive);
        if (clubVOList != null && !clubVOList.isEmpty()) {
            log.info("clubVOList :" + clubVOList);
            return clubVOList;
        } else {
            log.info("没有查询到符合条件的社团信息");
        }
        return null;
    }

    /**
     * 修改社团报名信息
     *
     * @param clubChooseDTO
     * @return
     */
    @Override
    public List<M_ClubApplyNumVO> updateClubChoose(M_ClubChooseDTO clubChooseDTO) {
        Integer userId = clubChooseDTO.getUserId();
        //先根据用户ID，拿到现有一二三志愿情况，以判断是否报名人数增加或减少
        M_ClubStudentInfo mClubStudentInfo = m_clubStudentInfoMapper.getAllByUserId(userId);
        if (mClubStudentInfo == null) {
            log.info("用户{}没有找到对应的社团报名信息", userId);
            return null;
        }
        List<Integer> oldClubIds = new ArrayList<>();
        if (mClubStudentInfo.getFirstChoiceId() != null) {
            oldClubIds.add(mClubStudentInfo.getFirstChoiceId());
        } else {
            oldClubIds.add(0);
        }
        if (mClubStudentInfo.getSecondChoiceId() != null) {
            oldClubIds.add(mClubStudentInfo.getSecondChoiceId());
        } else {
            oldClubIds.add(0);
        }
        if (mClubStudentInfo.getThirdChoiceId() != null) {
            oldClubIds.add(mClubStudentInfo.getThirdChoiceId());
        } else {
            oldClubIds.add(0);
        }

        //需要前端传过来的时候，把空的选择id标记为0
        List<Integer> newClubIds = clubChooseDTO.getClubIds();

        //如果相同怎不做任何改变，否则则修改报名信息，同时修改社团表的数量信息
        Integer i = 0;
        List<M_ClubApplyNumVO> mClubApplyNumVOList = new ArrayList<>();

        if (!oldClubIds.get(i).equals(newClubIds.get(i))) {
            //先修改社团表的报名人数,但是只修改第一志愿的人数，其他的志愿统一只修改内容，不修改数量
            Integer j = 1;
            if (i == 0) {
                //第一志愿
                if (oldClubIds.get(i).equals(0) && !newClubIds.get(i).equals(0)) {
                    //第一种情况，原来没有，现在有，该社团报名人数+1
                    m_clubMapper.updateClubNumberById(newClubIds.get(i), j);
                    M_ClubApplyNumVO mClubApplyNumVO = new M_ClubApplyNumVO();
                    mClubApplyNumVO.setClubId(newClubIds.get(i));
                    Integer updateCurrentStudent = m_clubMapper.getCurrentStudentsById(newClubIds.get(i));
                    mClubApplyNumVO.setCurrentStudents(updateCurrentStudent);
                    mClubApplyNumVOList.add(mClubApplyNumVO);

                } else if (!oldClubIds.get(i).equals(0) && newClubIds.get(i).equals(0)) {
                    j = -1;
                    //第三种情况，原来有，现在没有，该社团报名人数-1
                    m_clubMapper.updateClubNumberById(oldClubIds.get(i), j);
                    M_ClubApplyNumVO mClubApplyNumVO = new M_ClubApplyNumVO();
                    mClubApplyNumVO.setClubId(oldClubIds.get(i));
                    Integer updateCurrentStudent = m_clubMapper.getCurrentStudentsById(oldClubIds.get(i));
                    mClubApplyNumVO.setCurrentStudents(updateCurrentStudent);
                    mClubApplyNumVOList.add(mClubApplyNumVO);
                } else if (!oldClubIds.get(i).equals(0) && !newClubIds.get(i).equals(0)) {
                    //第四种情况，原来有，现在也有，原有社团减1，新社团加1
                    j = -1;
                    m_clubMapper.updateClubNumberById(oldClubIds.get(i), j);
                    M_ClubApplyNumVO mClubApplyNumVO1 = new M_ClubApplyNumVO();
                    mClubApplyNumVO1.setClubId(oldClubIds.get(i));
                    Integer updateCurrentStudent1 = m_clubMapper.getCurrentStudentsById(oldClubIds.get(i));
                    mClubApplyNumVO1.setCurrentStudents(updateCurrentStudent1);
                    mClubApplyNumVOList.add(mClubApplyNumVO1);
                    j = 1;
                    m_clubMapper.updateClubNumberById(newClubIds.get(i), j);
                    M_ClubApplyNumVO mClubApplyNumVO2 = new M_ClubApplyNumVO();
                    mClubApplyNumVO2.setClubId(newClubIds.get(i));
                    Integer updateCurrentStudent2 = m_clubMapper.getCurrentStudentsById(newClubIds.get(i));
                    mClubApplyNumVO2.setCurrentStudents(updateCurrentStudent2);
                    mClubApplyNumVOList.add(mClubApplyNumVO2);
                }
            }


        }


        //然后按照新的志愿信息，更新学生的志愿信息
        Integer firstChoiceId = newClubIds.get(0).equals(0) ? null : newClubIds.get(0);
        Integer secondChoiceId = newClubIds.get(1).equals(0) ? null : newClubIds.get(1);
        Integer thirdChoiceId = newClubIds.get(2).equals(0) ? null : newClubIds.get(2);


        m_ClubStudentInfoMapper.updateClubChooseByUserId(firstChoiceId, secondChoiceId, thirdChoiceId, userId);


        return mClubApplyNumVOList;

    }

    @Override
    public List<M_ThreeClubChooseVO> getThreeClubChoose(Integer userId, String admissionSemester) {
        M_ClubStudentInfo mClubStudentInfo = m_ClubStudentInfoMapper.getAllByUserId(userId);
        if (mClubStudentInfo == null) {
            log.info("用户{}没有找到对应的社团报名信息", userId);
            return null;
        }
        List<M_ThreeClubChooseVO> mThreeClubChooseVOList = new ArrayList<>();
        //根据社团ID，查询社团名称
        Integer firstChoiceId = mClubStudentInfo.getFirstChoiceId();
        M_ThreeClubChooseVO mThreeClubChooseVO1 = new M_ThreeClubChooseVO();
        //如果不为空，则查询社团名称
        if (firstChoiceId != null) {
            String firstChoiceName = m_clubMapper.getClubNameById(firstChoiceId);

            mThreeClubChooseVO1.setId(firstChoiceId);
            mThreeClubChooseVO1.setName(firstChoiceName);
            if (mClubStudentInfo.getFirstChooseStatus().equals(0)) {
                mThreeClubChooseVO1.setResultStatus("pending");
                mThreeClubChooseVO1.setResultText("等待录取");
            } else if (mClubStudentInfo.getFirstChooseStatus().equals(-1)) {
                mThreeClubChooseVO1.setResultStatus("notAdmitted");
                mThreeClubChooseVO1.setResultText("未被录取");
            } else {
                mThreeClubChooseVO1.setResultStatus("admitted");
                mThreeClubChooseVO1.setResultText("已录取");
            }
            mThreeClubChooseVOList.add(mThreeClubChooseVO1);

        } else {
            mThreeClubChooseVOList.add(null);
        }
        Integer secondChoiceId = mClubStudentInfo.getSecondChoiceId();
        M_ThreeClubChooseVO mThreeClubChooseVO2 = new M_ThreeClubChooseVO();
        if (secondChoiceId != null) {
            String secondChoiceName = m_clubMapper.getClubNameById(secondChoiceId);
            mThreeClubChooseVO2.setId(secondChoiceId);
            mThreeClubChooseVO2.setName(secondChoiceName);
            if (mClubStudentInfo.getSecondChooseStatus().equals(0)) {
                mThreeClubChooseVO2.setResultStatus("pending");
                mThreeClubChooseVO2.setResultText("等待录取");
            } else if (mClubStudentInfo.getSecondChooseStatus().equals(-1)) {
                mThreeClubChooseVO2.setResultStatus("notAdmitted");
                mThreeClubChooseVO2.setResultText("未被录取");
            } else {
                mThreeClubChooseVO2.setResultStatus("admitted");
                mThreeClubChooseVO2.setResultText("已录取");
            }
            mThreeClubChooseVOList.add(mThreeClubChooseVO2);
        } else {
            mThreeClubChooseVOList.add(null);
        }
        Integer thirdChoiceId = mClubStudentInfo.getThirdChoiceId();
        M_ThreeClubChooseVO mThreeClubChooseVO3 = new M_ThreeClubChooseVO();
        if (thirdChoiceId != null) {
            String thirdChoiceName = m_clubMapper.getClubNameById(thirdChoiceId);
            mThreeClubChooseVO3.setId(thirdChoiceId);
            mThreeClubChooseVO3.setName(thirdChoiceName);
            if (mClubStudentInfo.getThirdChooseStatus().equals(0)) {
                mThreeClubChooseVO3.setResultStatus("pending");
                mThreeClubChooseVO3.setResultText("等待录取");
            } else if (mClubStudentInfo.getThirdChooseStatus().equals(-1)) {
                mThreeClubChooseVO3.setResultStatus("notAdmitted");
                mThreeClubChooseVO3.setResultText("未被录取");
            } else {
                mThreeClubChooseVO3.setResultStatus("admitted");
                mThreeClubChooseVO3.setResultText("已录取");
            }
            mThreeClubChooseVOList.add(mThreeClubChooseVO3);
        } else {
            mThreeClubChooseVOList.add(null);
        }
        return mThreeClubChooseVOList;
    }


    //分配社团名单
    @Override
    public String assignClubList() {
        //首先拿到当前学期批次下所有已经激活的社团名单，假定全部要重新分配
        String admissionSemester = m_clubMapper.getCurrentAdmissionSemester();
        //这里一次性拿到了所有年级的社团
        List<Integer> activeClubIds = m_clubMapper.getActiveClubIds(admissionSemester);

        //然后根据clubStudentInfo表中，第一志愿报名了，而且还没有被录取的名单的学生名单，并且其他志愿也没有被录取的名单
        List<M_ClubStudentInfo> firstChoiceStudentList = m_ClubStudentInfoMapper.getFirstChoiceNotAdmittedStudents();
        //对上面的对象进行分组，按照firstChoiceId进行分组，以社团ID为key，学生名单列表为value

        Map<Integer, List<Integer>> groupByFirstChoiceId = new HashMap<>();
        for (M_ClubStudentInfo mClubStudentInfo : firstChoiceStudentList) {
            Integer firstChoiceId = mClubStudentInfo.getFirstChoiceId();
            if (activeClubIds.contains(firstChoiceId)) {
                if (groupByFirstChoiceId.containsKey(firstChoiceId)) {
                    groupByFirstChoiceId.get(firstChoiceId).add(mClubStudentInfo.getUserId());
                } else {
                    List<Integer> newList = new ArrayList<>();
                    newList.add(mClubStudentInfo.getUserId());
                    groupByFirstChoiceId.put(firstChoiceId, newList);
                }
            } else {
                log.info("社团{}没有激活，无法分配学生", firstChoiceId);
            }
        }


        Integer choiceLevel = 1; //表示第一志愿
        assignClubToStudents(activeClubIds, choiceLevel, admissionSemester, groupByFirstChoiceId);

        //第一志愿分配完成后，开始分配第二志愿
        String clubStatus = "未分配完";
        //拿到所有状态是未分配完的社团名单
        List<Integer> activeClubIdsForSecond = m_clubMapper.getActiveClubIdsAndStatus(admissionSemester, clubStatus);
        List<M_ClubStudentInfo> secondChoiceStudentList = m_ClubStudentInfoMapper.getSecondChoiceNotAdmittedStudents();
        Map<Integer, List<Integer>> groupBySecondChoiceId = new HashMap<>();
        for (M_ClubStudentInfo mClubStudentInfo : secondChoiceStudentList) {
            Integer secondChoiceId = mClubStudentInfo.getSecondChoiceId();
            if (activeClubIdsForSecond.contains(secondChoiceId)) {
                if (groupBySecondChoiceId.containsKey(secondChoiceId)) {
                    groupBySecondChoiceId.get(secondChoiceId).add(mClubStudentInfo.getUserId());
                } else {
                    List<Integer> newList = new ArrayList<>();
                    newList.add(mClubStudentInfo.getUserId());
                    groupBySecondChoiceId.put(secondChoiceId, newList);
                }
            } else {
                log.info("社团{}没有激活，无法分配学生", secondChoiceId);
            }
        }
        choiceLevel = 2; //表示第二志愿
        assignClubToStudents(activeClubIdsForSecond, choiceLevel, admissionSemester, groupBySecondChoiceId);

        //第二志愿分配完成后，开始分配第三志愿
        clubStatus = "未分配完";
        List<Integer> activeClubIdsForThird = m_clubMapper.getActiveClubIdsAndStatus(admissionSemester, clubStatus);
        //拿到所有没有被录取的学生名单
        List<M_ClubStudentInfo> thirdChoiceStudentList = m_ClubStudentInfoMapper.getThirdChoiceNotAdmittedStudents();
        Map<Integer, List<Integer>> groupByThirdChoiceId = new HashMap<>();
        for (M_ClubStudentInfo mClubStudentInfo : thirdChoiceStudentList) {
            Integer thirdChoiceId = mClubStudentInfo.getThirdChoiceId();
            if (activeClubIdsForThird.contains(thirdChoiceId)) {
                if (groupByThirdChoiceId.containsKey(thirdChoiceId)) {
                    groupByThirdChoiceId.get(thirdChoiceId).add(mClubStudentInfo.getUserId());
                } else {
                    List<Integer> newList = new ArrayList<>();
                    newList.add(mClubStudentInfo.getUserId());
                    groupByThirdChoiceId.put(thirdChoiceId, newList);
                }
            } else {
                log.info("社团{}没有激活，无法分配学生", thirdChoiceId);
            }
        }
        choiceLevel = 3; //表示第三志愿
        assignClubToStudents(activeClubIdsForThird, choiceLevel, admissionSemester, groupByThirdChoiceId);

        //第三志愿分配完成后，开始分配没有被录取的学生
        //拿到所有状态是未分配完的社团名单
        clubStatus = "未分配完";
        List<Integer> activeClubIdsRemain = m_clubMapper.getActiveClubIdsAndStatus(admissionSemester, clubStatus);
        //拿到所有没有被录取的学生名单

        //被动分配之后，还需要做一件事，就是更改现有的社团报名人数，增加对应的数量
        for (Integer remainId : activeClubIdsRemain) {
            List<M_ClubStudentInfo> notAdmittedStudents = m_ClubStudentInfoMapper.getNotAdmitedStudents();
            log.info("未分配完的社团有：{}", remainId);
            //查看还有多少名额
            Integer usedNum = m_clubResultMapper.getCountByClubId(remainId);
            Integer maxNum = m_clubMapper.getMaxStudentsById(remainId);
            Integer availableNum = maxNum - usedNum;
            if (availableNum > 0) {
                //判断remainId对应的社团是哪个年级的
                String remainClubGrade = m_clubMapper.getGradeById(remainId);
                //遍历没有录取学生名单，找到该年级的学生
                List<M_ClubStudentInfo> filteredNotAdmittedStudents = new ArrayList<>();
                for (M_ClubStudentInfo mClubStudentInfo : notAdmittedStudents) {
                    if (mClubStudentInfo.getStudentGrade().equals(remainClubGrade)) {
                        filteredNotAdmittedStudents.add(mClubStudentInfo);
                    }
                }

                if (filteredNotAdmittedStudents.size() >= availableNum) {
                    //随机抽取availableNum个学生
                    // 随机抽取学生 - 修复版本
                    List<Integer> selectedStudents = new ArrayList<>();
                    List<Integer> copyList = filteredNotAdmittedStudents.stream()
                            .map(M_ClubStudentInfo::getUserId)
                            .collect(Collectors.toList());

// 确保不超出范围
                    int actualNum = Math.min(availableNum, copyList.size());
                    for (int i = 0; i < actualNum; i++) {
                        int randomIndex = ThreadLocalRandom.current().nextInt(copyList.size());
                        selectedStudents.add(copyList.get(randomIndex));
                        copyList.remove(randomIndex);
                    }
                    //把已经选择的所有学生改为已经录取，吧社团改为满员，吧学生和社团名单添加到clubresult表
                    //先把社团改为满员
                    String clubStatusFull = "已满员";
                    m_clubResultMapper.updateClubFinishedStatus(remainId, clubStatusFull);

                    //因为改变了这些学生的第一志愿，所以remainId社团报名人数需要增加
                    m_clubMapper.updateClubNumberById(remainId, selectedStudents.size());
                    //把学生和社团名单添加到clubresult表
                    if (!selectedStudents.isEmpty()) {
                        m_clubResultMapper.deleteClubResultsByStudentIds(selectedStudents);
                    }
                    //首先要查询学生的姓名，还有学生的班级
                    for (Integer studentId : selectedStudents) {
                        M_ClubStudentInfo mClubStudentInfo = m_clubStudentInfoMapper.getAllByUserId(studentId);
                        String clubName = m_clubMapper.getClubNameById(remainId);
                        if (mClubStudentInfo != null) {
                            String studentName = mClubStudentInfo.getStudentName();
                            String studentClass = mClubStudentInfo.getStudentClass();
                            String studentGrade = mClubStudentInfo.getStudentGrade();
                            studentClass = studentGrade + studentClass;
                            m_clubResultMapper.insertClubResult(remainId, studentId, studentName, studentClass, admissionSemester, clubName);
                            //因为改变了这些学生的第一志愿，对应这些学生报名的社团报名人数需要减少
                            Integer notChooseFirstChoiceId = mClubStudentInfo.getFirstChoiceId();
                            if (notChooseFirstChoiceId != null) {
                                Integer m = -1;
                                m_clubMapper.updateClubNumberById(notChooseFirstChoiceId, m);
                            }
                        } else {
                            log.info("学生{}没有找到对应的社团报名信息", studentId);
                        }

                    }

                    //批量修改学生状态
                    m_ClubStudentInfoMapper.batchUpdateRemainClubStudentStatus(selectedStudents, remainId);
                } else {
                    //如果报名人数小于名额，则全部录取，吧社团改为部分录取，吧学生和社团名单添加到clubresult表
                    //把社团改为部分录取
                    String clubStatusPartial = "未分配完";
                    m_clubResultMapper.updateClubFinishedStatus(remainId, clubStatusPartial);
                    //批量修改学生状态
                    List<Integer> studentIdList = new ArrayList<>();
                    for (M_ClubStudentInfo mClubStudentInfo : filteredNotAdmittedStudents) {
                        studentIdList.add(mClubStudentInfo.getUserId());
                    }
                    m_ClubStudentInfoMapper.batchUpdateRemainClubStudentStatus(studentIdList, remainId);
                    //因为改变了这些学生的第一志愿，所以remainId社团报名人数需要增加
                    m_clubMapper.updateClubNumberById(remainId, filteredNotAdmittedStudents.size());
                    //因为改变了这些学生的第一志愿，对应这些学生报名的社
                    //把学生和社团名单添加到clubresult表

                    //首先要查询学生的姓名，还有学生的班级
                    for (M_ClubStudentInfo mClubStudentInfo : filteredNotAdmittedStudents) {
                        Integer studentId = mClubStudentInfo.getUserId();
                        String clubName = m_clubMapper.getClubNameById(remainId);
                        if (mClubStudentInfo != null) {
                            String studentName = mClubStudentInfo.getStudentName();
                            String studentClass = mClubStudentInfo.getStudentClass();
                            String studentGrade = mClubStudentInfo.getStudentGrade();
                            studentClass = studentGrade + studentClass;
                            //插入记录之前，先删除clubResult表中该学生的记录
                            m_clubResultMapper.deleteClubResultsByStudentIds(List.of(studentId));
                            m_clubResultMapper.insertClubResult(remainId, studentId, studentName, studentClass, admissionSemester, clubName);
                            //因为改变了这些学生的第一志愿，对应这些学生报名的社团报名人数需要减少
                            Integer notChooseFirstChoiceId = mClubStudentInfo.getFirstChoiceId();
                            if (notChooseFirstChoiceId != null) {
                                Integer m = -1;
                                m_clubMapper.updateClubNumberById(notChooseFirstChoiceId, m);
                            }

                        } else {
                            log.info("学生{}没有找到对应的社团报名信息", studentId);
                        }
                    }


                }
            }


        }
        //查询是不是还有没有录取的学生
        List<M_ClubStudentInfo> remainingNotAdmittedStudents = m_ClubStudentInfoMapper.getNotAdmitedStudents();
        String notAdmittedStudentName = "";
        if (remainingNotAdmittedStudents != null && !remainingNotAdmittedStudents.isEmpty()) {
            log.info("还有{}个学生没有被录取", remainingNotAdmittedStudents.size());
            for (M_ClubStudentInfo mClubStudentInfo : remainingNotAdmittedStudents) {
                notAdmittedStudentName = notAdmittedStudentName + mClubStudentInfo.getStudentName() + ",";
            }
            return notAdmittedStudentName;

        }
        return "success";

    }











    //按班级导出社团名单
    @Override
    public ResponseEntity<byte[]> downloadClubListByClass(String admissionSemester) {


        //导出的名单是excel表，按班级进行分类，一个班级一个sheet表
        //每个sheet表包含，序号，学生姓名，社团名称，上课教师，上课地点，并且按照社团名称进行排序
        List<M_ClubResultByClassVO> mClubResultByClassVOList = m_clubResultMapper.getResultsByClass(admissionSemester);
        if (mClubResultByClassVOList != null && !mClubResultByClassVOList.isEmpty()) {
            //使用map,按班级进行分组
            Map<String, List<M_ClubResultByClassVO>> groupByClass = new HashMap<>();

            for (M_ClubResultByClassVO mClubResultByClassVO : mClubResultByClassVOList) {
                //把上课地点和上课教师信息补充完整
                String teacher = m_clubMapper.getTeacherByClubId(mClubResultByClassVO.getClubId(), admissionSemester);
                String position = m_clubMapper.getPositionByClubId(mClubResultByClassVO.getClubId(), admissionSemester);

                //把上课地点和上课教师信息设置到对象中
                mClubResultByClassVO.setTeacher(teacher);
                mClubResultByClassVO.setPosition(position);


                String studentClass = mClubResultByClassVO.getStudentClass();
                if (groupByClass.containsKey(studentClass)) {
                    groupByClass.get(studentClass).add(mClubResultByClassVO);
                } else {
                    List<M_ClubResultByClassVO> newList = new ArrayList<>();
                    newList.add(mClubResultByClassVO);
                    groupByClass.put(studentClass, newList);
                }
            }


            try (Workbook workbook = new XSSFWorkbook()) {
                // 创建样式
                CellStyle headerStyle = createHeaderStyle(workbook);
                CellStyle dataStyle = createDataStyle(workbook);

                // 遍历每个班级
                for (Map.Entry<String, List<M_ClubResultByClassVO>> entry : groupByClass.entrySet()) {
                    String className = entry.getKey();
                    List<M_ClubResultByClassVO> studentClubs = entry.getValue();

                    // 创建sheet，名称格式：班级名称+社团名单
                    String sheetName = getValidSheetName(className + "社团名单");
                    Sheet sheet = workbook.createSheet(sheetName);

                    // 创建标题行
                    createHeaderRow(sheet, headerStyle, className + "社团名单");

                    // 填充数据
                    fillDataRows(sheet, dataStyle, studentClubs);

                    // 自动调整列宽
                    autoSizeColumns(sheet);
                }

                // 将workbook转换为字节数组
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                workbook.write(outputStream);

                // 设置响应头
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
                headers.setContentDispositionFormData("attachment", "clubByClass.xlsx");

                return new ResponseEntity<>(outputStream.toByteArray(), headers, HttpStatus.OK);

            } catch (IOException e) {
                throw new RuntimeException("生成Excel文件失败", e);
            }


        } else {
            log.info("无法导出社团名单，当前学期{}没有任何社团名单", admissionSemester);
        }

        return null;
    }

    @Override
    public ResponseEntity<byte[]> downloadClubListByClub(String admissionSemester) {
        List<M_ClubResultByClassVO> mClubResultByClubVOList = m_clubResultMapper.getResultsByClub(admissionSemester);
        if (mClubResultByClubVOList != null && !mClubResultByClubVOList.isEmpty()) {
            //使用map,按社团进行分组
            Map<String, List<M_ClubResultByClassVO>> groupByClub = new HashMap<>();

            for (M_ClubResultByClassVO mClubResultByClassVO : mClubResultByClubVOList) {
                //把上课地点和上课教师信息补充完整
                String teacher = m_clubMapper.getTeacherByClubId(mClubResultByClassVO.getClubId(), admissionSemester);
                String position = m_clubMapper.getPositionByClubId(mClubResultByClassVO.getClubId(), admissionSemester);

                //把上课地点和上课教师信息设置到对象中
                mClubResultByClassVO.setTeacher(teacher);
                mClubResultByClassVO.setPosition(position);
                String clubName = mClubResultByClassVO.getClubName();
                if (groupByClub.containsKey(clubName)) {
                    groupByClub.get(clubName).add(mClubResultByClassVO);
                } else {
                    List<M_ClubResultByClassVO> newList = new ArrayList<>();
                    newList.add(mClubResultByClassVO);
                    groupByClub.put(clubName, newList);
                }
            }
            //以社团名称为sheet名称
            try (Workbook workbook = new XSSFWorkbook()) {
                // 创建样式
                CellStyle headerStyle = createHeaderStyle(workbook);
                CellStyle dataStyle = createDataStyle(workbook);
                // 遍历每个社团
                for (Map.Entry<String, List<M_ClubResultByClassVO>> entry : groupByClub.entrySet()) {
                    String clubName = entry.getKey();
                    List<M_ClubResultByClassVO> studentClubs = entry.getValue();
                    // 创建sheet，名称格式：社团名称+社团名单
                    String sheetName = getValidSheetName(clubName + "社团名单");
                    Sheet sheet = workbook.createSheet(sheetName);
                    // 创建标题行
                    createHeaderRowByClub(sheet, headerStyle, clubName + "社团名单");
                    // 填充数据
                    fillByClubDataRows(sheet, dataStyle, studentClubs);
                    // 自动调整列宽
                    autoSizeColumns(sheet);
                }
                // 将workbook转换为字节数组
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                workbook.write(outputStream);
                // 设置响应头
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
                headers.setContentDispositionFormData("attachment", "clubByClub.xlsx");
                return new ResponseEntity<>(outputStream.toByteArray(), headers, HttpStatus.OK);
            } catch (IOException e) {
                throw new RuntimeException("生成Excel文件失败", e);
            }

        }
        return null;
    }

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public String uploadClubList(MultipartFile file) {

        /**
         * 解析Excel文件为M_Club列表
         */

        List<M_Club> clubList = new ArrayList<>();

        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0); // 获取第一个sheet

            // 从第二行开始读取（跳过标题行）
            for (int rowIndex = 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                Row row = sheet.getRow(rowIndex);
                if (row == null) continue;

                M_Club club = parseRowToClub(row);
                if (club != null) {
                    clubList.add(club);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


        // 批量插入到数据库
        if (!clubList.isEmpty()) {
            //在此之前，要先批量删除已经存在的同年级的社团信息，假定社团名称是唯一的
            m_clubMapper.deleteAllSameClubs(clubList);
            m_clubMapper.insertClubList(clubList);
            return "success";
        } else {
            return "failure";
        }


    }

//    @Override
//    public String uploadStudentList(MultipartFile file) {
//        List<M_User> userList = new ArrayList<>();
//
//        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
//            Sheet sheet = workbook.getSheetAt(0); // 获取第一个sheet
//
//            // 从第二行开始读取（跳过标题行）
//            for (int rowIndex = 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
//                Row row = sheet.getRow(rowIndex);
//                if (row == null) continue;
//
//                M_User user = parseRowToUser(row);
//                if (user != null && isValidUser(user)) {
//                    userList.add(user);
//                }
//            }
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
//
//
//        if (!userList.isEmpty()) {
//            //在此之前，要先批量删除已经存在的同名的学生信息，假定姓名是唯一的
//            //删除同班级相同姓名的学生，不仅要删除user数据库，更需要删除clubStudentInfo数据库
//
//            m_userMapper.deleteAllSameUsers(userList);
//            //删除同班级同姓名的学生在clubStudentInfo表中的信息
//            m_ClubStudentInfoMapper.deleteAllSameClubStudentInfos(userList);
//
//
//            //插入user数据库同时拿到id
//            for (M_User user : userList) {
//                user.setSchool("附小");
//                m_userMapper.insertUserAndGetId(user);
//
//
//                //插入clubStudentInfo数据库
//                m_ClubStudentInfoMapper.insertClubStudentInfoFromUser(user);
//            }
//            return "success";
//        } else {
//            return "failure";
//        }
//
//    }


    @Override
    @Transactional
    public String uploadStudentList(MultipartFile file) {
        List<M_User> userList = new ArrayList<>();

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
        } catch (IOException e) {
            log.error("读取Excel文件失败", e);
            throw new RuntimeException("文件读取失败: " + e.getMessage());
        }

        if (userList.isEmpty()) {
            return "failure: 没有有效的学生数据";
        }

        return processUserListInBatches(userList);
    }

    /**
     * 分批处理用户数据
     */
    private String processUserListInBatches(List<M_User> userList) {
        int batchSize = 50; // 每批处理50条记录，可根据实际情况调整
        int total = userList.size();
        int successCount = 0;
        int failCount = 0;
        List<String> errorMessages = new ArrayList<>();

        log.info("开始处理 {} 条学生记录，分批大小: {}", total, batchSize);

        for (int i = 0; i < total; i += batchSize) {
            int end = Math.min(i + batchSize, total);
            List<M_User> batch = userList.subList(i, end);

            try {
                processSingleBatch(batch);
                successCount += batch.size();
                log.info("成功处理第 {} - {} 条记录，当前进度: {}/{}",
                        i + 1, end, end, total);

                // 可选：添加短暂延迟，避免对数据库造成过大压力
                if (end < total) {
                    Thread.sleep(50);
                }
            } catch (Exception e) {
                failCount += batch.size();
                String errorMsg = String.format("处理第 %d-%d 条记录时失败: %s",
                        i + 1, end, e.getMessage());
                errorMessages.add(errorMsg);
                log.error(errorMsg, e);

                // 根据业务需求决定是否继续处理后续批次
                // 如果需要全部成功，可以在这里break并回滚事务
                // break;
            }
        }

        return buildResultMessage(successCount, failCount, errorMessages, total);
    }

    /**
     * 处理单个批次的数据
     */
    private void processSingleBatch(List<M_User> batch) {
        if (batch.isEmpty()) return;

        try {
            // 1. 删除已存在的同名学生（分批删除）
            deleteExistingUsersInBatch(batch);

            // 2. 分批插入用户数据
            insertUsersInBatch(batch);

            // 3. 分批插入社团学生信息
            insertClubStudentInfoInBatch(batch);

        } catch (Exception e) {
            log.error("处理批次数据失败，批次大小: {}", batch.size(), e);
            throw new RuntimeException("批次处理失败: " + e.getMessage(), e);
        }
    }

    /**
     * 分批删除已存在的用户
     */
    private void deleteExistingUsersInBatch(List<M_User> batch) {
        try {
            // 分批删除用户表数据
            m_userMapper.deleteAllSameUsers(batch);

            // 分批删除社团学生信息表数据
            m_ClubStudentInfoMapper.deleteAllSameClubStudentInfos(batch);

            log.debug("成功删除批次中的重复记录，批次大小: {}", batch.size());
        } catch (Exception e) {
            log.error("删除批次记录失败", e);
            throw new RuntimeException("删除操作失败: " + e.getMessage(), e);
        }
    }

    /**
     * 分批插入用户数据
     */
    private void insertUsersInBatch(List<M_User> batch) {
        for (M_User user : batch) {
            try {
                user.setSchool("附小");
                m_userMapper.insertUserAndGetId(user);
            } catch (Exception e) {
                log.error("插入用户失败: {}", user.getName(), e);
                throw new RuntimeException("用户插入失败: " + user.getName(), e);
            }
        }
    }

    /**
     * 分批插入社团学生信息
     */
    private void insertClubStudentInfoInBatch(List<M_User> batch) {
        for (M_User user : batch) {
            try {
                m_ClubStudentInfoMapper.insertClubStudentInfoFromUser(user);
            } catch (Exception e) {
                log.error("插入社团学生信息失败，用户ID: {}", user.getId(), e);
                throw new RuntimeException("社团信息插入失败: " + user.getName(), e);
            }
        }
    }

    /**
     * 构建结果消息
     */
    private String buildResultMessage(int successCount, int failCount,
                                      List<String> errorMessages, int total) {
        StringBuilder result = new StringBuilder();

        if (failCount == 0) {
            result.append("success: 成功导入 ").append(successCount).append(" 条记录");
        } else {
            result.append("partial: 成功导入 ").append(successCount)
                    .append(" 条，失败 ").append(failCount).append(" 条，总计 ").append(total);

            if (!errorMessages.isEmpty()) {
                result.append("\n错误详情:\n");
                for (int i = 0; i < Math.min(errorMessages.size(), 5); i++) {
                    result.append(errorMessages.get(i)).append("\n");
                }
                if (errorMessages.size() > 5) {
                    result.append("... 还有 ").append(errorMessages.size() - 5).append(" 个错误");
                }
            }
        }

        log.info("导入结果: {}", result.toString());
        return result.toString();
    }




    @Override
    public String resetAllClubChoose() {

        //删除所有的clubResult
        //首先拿到当前学期
        String admissionSemester = m_clubMapper.getCurrentAdmissionSemester();
        m_clubResultMapper.deleteAllClubResults(admissionSemester);
        //重置每个志愿的录取状态，但是不能重置所有家长的选择。
        m_ClubStudentInfoMapper.resetAllClubChooseStatus(admissionSemester);
        m_clubMapper.setAllFinishedStatusToNotAssigned(admissionSemester);
        return "success";
    }

    //获取所有社团信息，给管理员使用
    @Override
    public List<ClubVO> getAllAdminClub(String admissionSemester) {
        List<ClubVO> adminClubList = m_clubMapper.getAllAdminClub(admissionSemester);
        return adminClubList;
    }


    //获取所有学生信息，给管理员使用
    @Override
    public List<M_ClubStudentInfoVO> getAllAdminStudentInfo(String admissionSemester) {
        List<M_ClubStudentInfoVO> adminStudentInfoList = m_ClubStudentInfoMapper.getAllAdminStudentInfo(admissionSemester);
        for (M_ClubStudentInfoVO mClubStudentInfoVO : adminStudentInfoList) {
            Integer firstChoiceId = mClubStudentInfoVO.getFirstChoiceId();
            Integer secondChoiceId = mClubStudentInfoVO.getSecondChoiceId();
            Integer thirdChoiceId = mClubStudentInfoVO.getThirdChoiceId();
            if (firstChoiceId != null) {
                String firstChoiceName = m_clubMapper.getClubNameById(firstChoiceId);
                mClubStudentInfoVO.setFirstChoiceName(firstChoiceName);
            }
            if (secondChoiceId != null) {
                String secondChoiceName = m_clubMapper.getClubNameById(secondChoiceId);
                mClubStudentInfoVO.setSecondChoiceName(secondChoiceName);
            }
            if (thirdChoiceId != null) {
                String thirdChoiceName = m_clubMapper.getClubNameById(thirdChoiceId);
                mClubStudentInfoVO.setThirdChoiceName(thirdChoiceName);
            }
            String grade = mClubStudentInfoVO.getStudentGrade();
            String classNumber = mClubStudentInfoVO.getStudentClass();
            mClubStudentInfoVO.setClassName(grade + classNumber);

            String assignmentName = m_clubResultMapper.getClubNameByUserId(mClubStudentInfoVO.getUserId());
            if (assignmentName != null) {
                mClubStudentInfoVO.setAssignedClubName(assignmentName);
            }
        }
        return adminStudentInfoList;
    }

    @Override
    public String resetChooseStatusByClubId(Integer clubId) {
        //首先拿到当前学期批次
        String admissionSemester = m_clubMapper.getCurrentAdmissionSemester();
        //然后删除指定result当前批次，当前社团的所有录取名单
        m_clubResultMapper.deleteClubResultsByClubId(clubId, admissionSemester);
        //然后把clubStudentInfo表中，选择了该社团的学生的对应志愿状态重置为未录取
        //逐一查询三个志愿，如果有该社团ID，则把对应的志愿状态重置为未录取
        m_ClubStudentInfoMapper.resetFirstStatusByClubId(clubId);
        m_ClubStudentInfoMapper.resetSecondStatusByClubId(clubId);
        m_ClubStudentInfoMapper.resetThirdStatusByClubId(clubId);
        //最后删除该社团
        m_clubMapper.deleteClubById(clubId);
        return "success";
    }

    @Override
    public String deleteClubsByGrade(String grade) {
        //首先拿到当前学期批次
        String admissionSemester = m_clubMapper.getCurrentAdmissionSemester();
        //然后拿到该年级的所有社团ID
        if (grade.equals("全部")) {
            grade = null;

        }
        List<Integer> clubIds = m_clubMapper.getClubIdsByGrade(grade, admissionSemester);
        if (clubIds != null && !clubIds.isEmpty()) {
            //然后删除指定result当前批次，当前年级的所有录取名单
            m_clubResultMapper.deleteClubResultsByClubIds(clubIds, admissionSemester);
            //然后把clubStudentInfo表中，选择了该年级社团的学生的对应志愿状态重置为未录取
            for (Integer clubId : clubIds) {
                //逐一查询三个志愿，如果有该社团ID，则把对应的志愿状态重置为未录取
                m_ClubStudentInfoMapper.resetFirstStatusByClubId(clubId);
                m_ClubStudentInfoMapper.resetSecondStatusByClubId(clubId);
                m_ClubStudentInfoMapper.resetThirdStatusByClubId(clubId);
            }
            //最后删除该年级的所有社团
            m_clubMapper.deleteClubsByGrade(grade, admissionSemester);
        }

        return "success";
    }


    //删除学生信息
    @Override
    public String deleteUserById(Integer studentId) {
        //首先删除clubResult表中该学生的所有录取信息
        //在删掉clubResult表中该学生的记录之前，先查询该学生被录取的社团，如果有的话，把对应的社团的录取状态改为未分配完
        String admissionSemester = m_clubMapper.getCurrentAdmissionSemester();
        Integer admittedClubId = m_clubResultMapper.getClubIdByUserId(studentId, admissionSemester);
        if (admittedClubId != null) {
            String clubStatus = "未分配完";
            m_clubResultMapper.updateClubFinishedStatus(admittedClubId, clubStatus);
        }

        m_clubResultMapper.deleteClubResultsByStudentIds(List.of(studentId));
        //首先看看该学生有没有选择第一志愿社团
        M_ClubStudentInfo mClubStudentInfo = m_ClubStudentInfoMapper.getAllByUserId(studentId);
        if (mClubStudentInfo != null) {
            Integer firstChoiceId = mClubStudentInfo.getFirstChoiceId();
            if (firstChoiceId != null) {
                //如果有选择第一志愿社团，则把该社团的报名人数减1
                Integer m = -1;
                m_clubMapper.updateClubNumberById(firstChoiceId, m);
            }
        }
        //然后删除clubStudentInfo表中该学生的所有选择信息
        m_ClubStudentInfoMapper.deleteClubStudentInfoByUserId(studentId);
        //最后删除user表中该学生的信息
        m_userMapper.deleteUserById(studentId);


        return "success";
    }

    @Override
    public String deleteUserByGrade(String grade) {
        //首先拿到当前学期批次
        String admissionSemester = m_clubMapper.getCurrentAdmissionSemester();
        //然后拿到该年级的所有学生ID
        if (grade.equals("全部")) {
            grade = null;

        }
        List<Integer> studentIds = m_ClubStudentInfoMapper.getUserIdsByGrade(grade, admissionSemester);
        if (studentIds != null && !studentIds.isEmpty()) {
            //在删掉clubResult表中该年级学生的记录之前，先查询这些学生被录取的社团，如果有的话，把对应的社团的录取状态改为未分配完
            for (Integer studentId : studentIds) {
                Integer admittedClubId = m_clubResultMapper.getClubIdByUserId(studentId, admissionSemester);
                if (admittedClubId != null) {
                    String clubStatus = "未分配完";
                    m_clubResultMapper.updateClubFinishedStatus(admittedClubId, clubStatus);
                }
            }
            //然后删除指定result当前批次，当前年级的所有录取名单
            m_clubResultMapper.deleteClubResultsByStudentIds(studentIds);
            //然后把clubStudentInfo表中，选择了该年级社团的学生的对应志愿状态重置为未录取
            for (Integer studentId : studentIds) {
                M_ClubStudentInfo mClubStudentInfo = m_ClubStudentInfoMapper.getAllByUserId(studentId);
                if (mClubStudentInfo != null) {
                    Integer firstChoiceId = mClubStudentInfo.getFirstChoiceId();
                    if (firstChoiceId != null) {
                        //如果有选择第一志愿社团，则把该社团的报名人数减1
                        Integer m = -1;
                        m_clubMapper.updateClubNumberById(firstChoiceId, m);
                    }
                }
            }
            //然后删除clubStudentInfo表中该年级的所有选择信息

            m_ClubStudentInfoMapper.deleteClubStudentInfosByUserIds(studentIds, admissionSemester);
            //最后删除user表中该年级的所有学生信息
            m_userMapper.deleteUsersByIds(studentIds);
        }
        return "success";
    }

    //添加新的社团
    @Override
    public String addNewClub(M_Club newClub) {
        //处理教师列表，教师列表是逗号分隔的字符串
        List<String> teacherList = newClub.getTeacherList();
        String admissionSemester = m_clubMapper.getCurrentAdmissionSemester();
        newClub.setAdmissionSemester(admissionSemester);
        //首先看看该年级该社团名称的社团是否已经存在
        String grade = newClub.getGrade();
        String clubName = newClub.getClubName();
        Integer existingClubId = m_clubMapper.getClubIdByNameAndGrade(clubName, grade, admissionSemester);
        if (existingClubId != null) {
            return "该年级已经存在同名社团，请修改社团名称或年级";
        }
        if (teacherList != null && !teacherList.isEmpty()) {
            String teachers = String.join(";", teacherList);
            newClub.setTeacher(teachers);
        } else {
            newClub.setTeacher("");
        }
        m_clubMapper.insertClub(newClub);
        return "success";

    }

    @Override
    public String editClubInfo(M_Club editClub) {
        //处理教师列表，教师列表是逗号分隔的字符串
        List<String> teacherList = editClub.getTeacherList();
        String admissionSemester = m_clubMapper.getCurrentAdmissionSemester();
        editClub.setAdmissionSemester(admissionSemester);
        //首先看看该年级该社团名称的社团是否已经存在
        String grade = editClub.getGrade();
        String clubName = editClub.getClubName();
        //拿到同年级同名社团的ID，如果存在，并且ID不是当前编辑的社团ID，则说明有重复
        Integer existingClubId = m_clubMapper.getClubIdByNameAndGrade(clubName, grade, admissionSemester);
        if (existingClubId != null && !existingClubId.equals(editClub.getId())) {
            return "该年级已经存在同名社团，请修改社团名称或年级";
        }
        if (teacherList != null && !teacherList.isEmpty()) {
            String teachers = String.join(";", teacherList);
            editClub.setTeacher(teachers);
        } else {
            editClub.setTeacher("");
        }

        //更新社团信息时，如果把社团的最大人数改小了，那么要判断当前社团的报名人数是否已经超过了新的最大人数，如果改大了，要把社团的状态改为未分配完
        //从clubResult表中查询当前社团的报名人数
        Integer usedNum = m_clubResultMapper.getCountByClubId(editClub.getId());
        if (usedNum != null) {
            Integer maxNum = editClub.getMaxStudents();
            if (maxNum != null) {
                if (usedNum >= maxNum) {

                    String clubStatus = "已满员";
                    editClub.setFinished(clubStatus);

                } else {
                    String clubStatus = "未分配完";
                    editClub.setFinished(clubStatus);
                }
            }
        }


        m_clubMapper.updateClub(editClub);
        return "success";
    }

    @Override
    public String editStudentInfo(M_ClubStudentInfoVO editStudent) {
        //先处理班级，如果用户输入的班级信息不是以班字结尾，我们要帮用户加上班字
        String studentNum = editStudent.getStudentClass();
        if (studentNum != null && !studentNum.isEmpty()) {
            if (!studentNum.endsWith("班")) {
                studentNum = studentNum + "班";
                editStudent.setStudentClass(studentNum);
            }
        }
        //首先处理phoneList
        List<String> phoneList = editStudent.getPhoneList();
        if (phoneList != null && !phoneList.isEmpty()) {
            String phones = String.join(";", phoneList);
            editStudent.setPhone(phones);
        } else {
            editStudent.setPhone("0755");
        }
        String admissionSemester = m_clubMapper.getCurrentAdmissionSemester();
        editStudent.setAdmissionSemester(admissionSemester);
        //然后插入user表
        M_User user = new M_User();
        user.setName(editStudent.getStudentName());

        user.setPhone(editStudent.getPhone());

        user.setId(editStudent.getId());
        //拿到同电话和姓名的用户，如果存在，并且ID不是当前编辑的学生ID，则说明有重复
        M_User existingUser = m_userMapper.getUserByNameAndPhone(user.getName(), user.getPhone());
        if (existingUser != null && !existingUser.getId().equals(editStudent.getId())) {
            //如果存在，则不允许添加，返回错误信息
            return "该学生姓名和联系电话已经存在，请检查后重新添加";
        }

        m_userMapper.updateUser(user);


        //根据年级和第一志愿社团名称，查询第一志愿社团ID
        String grade = editStudent.getStudentGrade();
        String firstChoiceName = editStudent.getFirstChoiceName();
        String resp = "";
        if (firstChoiceName != null && !firstChoiceName.isEmpty()) {
            Integer firstChoiceId = m_clubMapper.getClubIdByNameAndGrade(firstChoiceName, grade, admissionSemester);

            if (firstChoiceId != null) {
                //如果用户修改了第一志愿社团，则要把之前选择的第一志愿社团的报名人数减1
                Integer previousFirstChoiceId = m_ClubStudentInfoMapper.getFirstChoiceId(editStudent.getId());

                if (previousFirstChoiceId != null && !previousFirstChoiceId.equals(firstChoiceId)) {
                    Integer m = -1;
                    m_clubMapper.updateClubNumberById(previousFirstChoiceId, m);

                    //如果有选择第一志愿社团，则把该社团的报名人数加1
                    m = 1;
                    m_clubMapper.updateClubNumberById(firstChoiceId, m);
                    //如果之前选择第一志愿已经被录取，则修改状态为未录取
                    Integer firstChoiceStatus = m_ClubStudentInfoMapper.getFirstChooseStatus(editStudent.getId());
                    if (firstChoiceStatus != null && firstChoiceStatus.equals(1)) {
                        //直接修改为未录取
                        m_ClubStudentInfoMapper.setFirstStatusZeroByClubId(editStudent.getId());
                    }
                } else if (previousFirstChoiceId == null) {
                    //如果之前没有选择第一志愿社团，现在选择了，则把该社团的报名人数加1
                    Integer m = 1;
                    m_clubMapper.updateClubNumberById(firstChoiceId, m);
                    m_ClubStudentInfoMapper.setFirstStatusZeroByClubId(editStudent.getId());
                }
                editStudent.setFirstChoiceId(firstChoiceId);

            } else {
                resp = "该年级没有找到与第一志愿社团名称匹配的社团，请检查社团名称是否正确";

            }
        } else {
            //拿到之前选择的第一志愿社团名称，如果之前选择第一志愿是空的，则不需要提示错误，如果之前不为空，现在为空，则需要对应报名人数减1
            Integer previousFirstChoiceId = m_ClubStudentInfoMapper.getFirstChoiceId(editStudent.getId());
            if (previousFirstChoiceId != null) {
                Integer m = -1;
                m_clubMapper.updateClubNumberById(previousFirstChoiceId, m);
                m_ClubStudentInfoMapper.setFirstStatusZeroByClubId(editStudent.getId());
            }
            editStudent.setFirstChoiceId(null);
        }
        //根据年级和第二志愿社团名称，查询第二志愿社团ID
        String secondChoiceName = editStudent.getSecondChoiceName();
        if (secondChoiceName != null && !secondChoiceName.isEmpty()) {
            Integer secondChoiceId = m_clubMapper.getClubIdByNameAndGrade(secondChoiceName, grade, admissionSemester);
            if (secondChoiceId != null) {
                editStudent.setSecondChoiceId(secondChoiceId);
                //如果之前第二志愿被录取，且新的第二志愿ID和之前不同，则把第二志愿状态改为未录取
                Integer previousSecondChoiceId = m_ClubStudentInfoMapper.getSecondChoiceId(editStudent.getId());
                if (previousSecondChoiceId != null && !previousSecondChoiceId.equals(secondChoiceId)) {
                    Integer secondChoiceStatus = m_ClubStudentInfoMapper.getSecondChooseStatus(editStudent.getId());
                    if (secondChoiceStatus != null && secondChoiceStatus.equals(1)) {
                        m_ClubStudentInfoMapper.setSecondStatusZeroByClubId(editStudent.getId());
                    }
                }
            } else {
                resp = resp + "；该年级没有找到与第二志愿社团名称匹配的社团，请检查社团名称是否正确";
            }


        } else {

            Integer secondChoiceStatus = m_ClubStudentInfoMapper.getSecondChooseStatus(editStudent.getId());
            if (secondChoiceStatus != null && secondChoiceStatus.equals(1)) {
                m_ClubStudentInfoMapper.setSecondStatusZeroByClubId(editStudent.getId());
            }

            editStudent.setSecondChoiceId(null);
        }
        //根据年级和第三志愿社团名称，查询第三志愿社团ID
        String thirdChoiceName = editStudent.getThirdChoiceName();
        if (thirdChoiceName != null && !thirdChoiceName.isEmpty()) {
            Integer thirdChoiceId = m_clubMapper.getClubIdByNameAndGrade(thirdChoiceName, grade, admissionSemester);
            if (thirdChoiceId != null) {
                editStudent.setThirdChoiceId(thirdChoiceId);
                //如果之前第三志愿被录取，且新的第三志愿ID和之前不同，则把第三志愿状态改为未录取
                Integer previousThirdChoiceId = m_ClubStudentInfoMapper.getThirdChoiceId(editStudent.getId());
                if (previousThirdChoiceId != null && !previousThirdChoiceId.equals(thirdChoiceId)) {
                    Integer thirdChoiceStatus = m_ClubStudentInfoMapper.getThirdChooseStatus(editStudent.getId());
                    if (thirdChoiceStatus != null && thirdChoiceStatus.equals(1)) {
                        m_ClubStudentInfoMapper.setThirdStatusZeroByClubId(editStudent.getId());
                    }
                }
            } else {
                resp = resp + "；该年级没有找到与第三志愿社团名称匹配的社团，请检查社团名称是否正确";
            }
        } else {
            Integer thirdChoiceStatus = m_ClubStudentInfoMapper.getThirdChooseStatus(editStudent.getId());
            if (thirdChoiceStatus != null && thirdChoiceStatus.equals(1)) {
                m_ClubStudentInfoMapper.setThirdStatusZeroByClubId(editStudent.getId());
            }
            editStudent.setThirdChoiceId(null);

        }


        //根据年级和录取社团名称，查询录取社团ID
        String assignedClubName = editStudent.getAssignedClubName();
        if (assignedClubName != null && !assignedClubName.isEmpty()) {
            Integer assignedClubId = m_clubMapper.getClubIdByNameAndGrade(assignedClubName, grade, admissionSemester);
            editStudent.setAssignedClubId(assignedClubId);
            if (assignedClubId != null) {

                //如果录取的社团属于第一、第二、第三志愿之一，则把对应的志愿状态改为已录取
                if (assignedClubId.equals(editStudent.getFirstChoiceId())) {
                    m_ClubStudentInfoMapper.setFirstChooseStatusOne(editStudent.getId());
                } else if (assignedClubId.equals(editStudent.getSecondChoiceId())) {
                    m_ClubStudentInfoMapper.setSecondChooseStatusOne(editStudent.getId());
                } else if (assignedClubId.equals(editStudent.getThirdChoiceId())) {
                    m_ClubStudentInfoMapper.setThirdChooseStatusOne(editStudent.getId());
                } else {
                    //如果录取的社团不属于第一、第二、第三志愿之一，则把第一志愿状态改为已录取，其他两个志愿状态改为未录取
                    m_ClubStudentInfoMapper.setFirstChooseStatusOne(editStudent.getId());
                    m_ClubStudentInfoMapper.setSecondStatusZeroByClubId(editStudent.getId());
                    m_ClubStudentInfoMapper.setThirdStatusZeroByClubId(editStudent.getId());
                    //还要把第一志愿的社团改为录取社团，且把该社团的报名人数加1，且之前选择的第一志愿社团报名人数减1
                    Integer previousFirstChoiceId = editStudent.getFirstChoiceId();
                    if (previousFirstChoiceId != null) {
                        Integer m = -1;
                        m_clubMapper.updateClubNumberById(previousFirstChoiceId, m);
                    }
                    editStudent.setFirstChoiceId(assignedClubId);
                    Integer m = 1;
                    m_clubMapper.updateClubNumberById(assignedClubId, m);
                }

                //把该学生的录取信息插入clubResult表
                //插入记录之前，先删除clubResult表中该学生的记录
                //首先判断修改录取的社团和之前录取的社团是否相同，如果相同，则不需要删除再插入
                Integer previousAssignedClubId = m_clubResultMapper.getClubIdByUserId(editStudent.getId(), admissionSemester);
                if (previousAssignedClubId != null) {
                    if (!previousAssignedClubId.equals(assignedClubId)) {
                        //如果之前录取的社团为已满员，且刚好录取人数和最大人数相等，则把该社团的状态改为未分配完
                        String previousClubStatus = m_clubMapper.getFinishedById(previousAssignedClubId);
                        if (previousClubStatus != null && previousClubStatus.equals("已满员")) {
                            //先拿到reslut中该社团的录取人数
                            Integer admittedNumber = m_clubResultMapper.getCountByClubId(previousAssignedClubId);
                            //再拿到club中该社团的最大招生人数
                            Integer maxStudents = m_clubMapper.getMaxStudentsById(previousAssignedClubId);
                            if (admittedNumber != null && maxStudents != null) {
                                if (admittedNumber.equals(maxStudents)) {
                                    String clubStatusNotFull = "未分配完";
                                    m_clubResultMapper.updateClubFinishedStatus(previousAssignedClubId, clubStatusNotFull);
                                }
                            }
                        }

                    }

                }
                //删除之前的录取记录
                m_clubResultMapper.deleteClubResultsByStudentIds(List.of(editStudent.getId()));
                String studentClass = editStudent.getStudentGrade() + editStudent.getStudentClass();
                m_clubResultMapper.insertClubResult(assignedClubId, editStudent.getId(), editStudent.getStudentName(), studentClass, admissionSemester, assignedClubName);
                //如果录取社团的剩余人数刚好剩余1，则把该社团的状态改为已满员
                //先拿到reslut中该社团的录取人数
                Integer admittedNumber = m_clubResultMapper.getCountByClubId(assignedClubId);
                //再拿到club中该社团的最大招生人数
                Integer maxStudents = m_clubMapper.getMaxStudentsById(assignedClubId);
                if (admittedNumber != null && maxStudents != null) {
                    Integer availableNum = maxStudents - admittedNumber;
                    if (availableNum != null && availableNum <= 1) {
                        String clubStatusFull = "已满员";
                        m_clubResultMapper.updateClubFinishedStatus(assignedClubId, clubStatusFull);
                    }
                }
            }


        }else {
            if(editStudent.getAssignedClubName()!= null && !editStudent.getAssignedClubName().isEmpty()){
                resp = resp + "；该年级没有找到与录取社团名称匹配的社团，请检查社团名称是否正确";

            }
            //现在是修改后的录取社团为空，这时候要判断，之前录取社团是否为空，如果之前的不为空，则要删除之前的result表的记录，同时把所有的志愿状态改为未录取
            Integer previousAssignedClubId = m_clubResultMapper.getClubIdByUserId(editStudent.getId(), admissionSemester);
            if (previousAssignedClubId != null) {

                //把所有志愿状态改为未录取
                m_ClubStudentInfoMapper.setFirstStatusZeroByClubId(editStudent.getId());
                m_ClubStudentInfoMapper.setSecondStatusZeroByClubId(editStudent.getId());
                m_ClubStudentInfoMapper.setThirdStatusZeroByClubId(editStudent.getId());
                //如果之前录取的社团为已满员，且刚好录取人数和最大人数相等，则把该社团的状态改为未分配完
                String previousClubStatus = m_clubMapper.getFinishedById(previousAssignedClubId);
                if (previousClubStatus != null && previousClubStatus.equals("已满员")) {
                    //先拿到reslut中该社团的录取人数
                    Integer admittedNumber = m_clubResultMapper.getCountByClubId(previousAssignedClubId);
                    //再拿到club中该社团的最大招生人数
                    Integer maxStudents = m_clubMapper.getMaxStudentsById(previousAssignedClubId);
                    if (admittedNumber != null && maxStudents != null) {
                        if (admittedNumber.equals(maxStudents)) {
                            String clubStatusNotFull = "未分配完";
                            m_clubResultMapper.updateClubFinishedStatus(previousAssignedClubId, clubStatusNotFull);
                        }
                    }
                }
                //删除之前的录取记录
                m_clubResultMapper.deleteClubResultsByStudentIds(List.of(editStudent.getId()));
            }
        }
        //然后插入clubStudentInfo表
        m_ClubStudentInfoMapper.updateClubStudentInfoFromVO(editStudent);

        return resp;
    }


    //添加新的学生
    @Override
    public String addNewStudent(M_ClubStudentInfoVO newStudent) {
        //先处理班级，如果用户输入的班级信息不是以班字结尾，我们要帮用户加上班字
        String studentNum = newStudent.getStudentClass();
        if (studentNum != null && !studentNum.isEmpty()) {
            if (!studentNum.endsWith("班")) {
                studentNum = studentNum + "班";
                newStudent.setStudentClass(studentNum);
            }
        }
        //首先处理phoneList
        List<String> phoneList = newStudent.getPhoneList();
        if (phoneList != null && !phoneList.isEmpty()) {
            String phones = String.join(";", phoneList);
            newStudent.setPhone(phones);
        } else {
            newStudent.setPhone("0755");
        }
        String admissionSemester = m_clubMapper.getCurrentAdmissionSemester();
        newStudent.setAdmissionSemester(admissionSemester);
        //然后插入user表
        M_User user = new M_User();
        user.setName(newStudent.getStudentName());
        user.setPassword("123");
        user.setRole("学生");
        user.setPhone(newStudent.getPhone());
        user.setSchool("附小");
        //首先要判断是不是同名和同电话的学生已经存在
        M_User existingUser = m_userMapper.getUserByNameAndPhone(user.getName(), user.getPhone());
        if (existingUser != null) {
            //如果存在，则不允许添加，返回错误信息
            return "该学生姓名和联系电话已经存在，请检查后重新添加";
        }
        //插入user表并获取id
        m_userMapper.insertUserAndGetId(user);
        Integer userId = user.getId();
        newStudent.setUserId(userId);

        //根据年级和第一志愿社团名称，查询第一志愿社团ID
        String grade = newStudent.getStudentGrade();
        String firstChoiceName = newStudent.getFirstChoiceName();
        String resp = "";
        if (firstChoiceName != null && !firstChoiceName.isEmpty()) {
            Integer firstChoiceId = m_clubMapper.getClubIdByNameAndGrade(firstChoiceName, grade, admissionSemester);

            if (firstChoiceId != null) {
                newStudent.setFirstChoiceId(firstChoiceId);
                //如果有选择第一志愿社团，则把该社团的报名人数加1
                Integer m = 1;
                m_clubMapper.updateClubNumberById(firstChoiceId, m);
            } else {
                resp = "该年级没有找到与第一志愿社团名称匹配的社团，请检查社团名称是否正确";
            }
        } else {
            newStudent.setFirstChoiceId(null);
        }
        //根据年级和第二志愿社团名称，查询第二志愿社团ID
        String secondChoiceName = newStudent.getSecondChoiceName();
        if (secondChoiceName != null && !secondChoiceName.isEmpty()) {
            Integer secondChoiceId = m_clubMapper.getClubIdByNameAndGrade(secondChoiceName, grade, admissionSemester);
            if (secondChoiceId != null) {
                newStudent.setSecondChoiceId(secondChoiceId);
            } else {
                resp = resp + "；该年级没有找到与第二志愿社团名称匹配的社团，请检查社团名称是否正确";
            }


        } else {
            newStudent.setSecondChoiceId(null);
        }
        //根据年级和第三志愿社团名称，查询第三志愿社团ID
        String thirdChoiceName = newStudent.getThirdChoiceName();
        if (thirdChoiceName != null && !thirdChoiceName.isEmpty()) {
            Integer thirdChoiceId = m_clubMapper.getClubIdByNameAndGrade(thirdChoiceName, grade, admissionSemester);
            if (thirdChoiceId != null) {
                newStudent.setThirdChoiceId(thirdChoiceId);
            } else {
                resp = resp + "；该年级没有找到与第三志愿社团名称匹配的社团，请检查社团名称是否正确";
            }
        } else {
            newStudent.setThirdChoiceId(null);

        }
        //把所有志愿状态都设置为未录取
        newStudent.setFirstChooseStatus(0);
        newStudent.setSecondChooseStatus(0);
        newStudent.setThirdChooseStatus(0);

        //根据年级和录取社团名称，查询录取社团ID
        String assignedClubName = newStudent.getAssignedClubName();
        if (assignedClubName != null && !assignedClubName.isEmpty()) {
            Integer assignedClubId = m_clubMapper.getClubIdByNameAndGrade(assignedClubName, grade, admissionSemester);
            newStudent.setAssignedClubId(assignedClubId);
            if (assignedClubId != null) {

                //如果录取的社团属于第一、第二、第三志愿之一，则把对应的志愿状态改为已录取
                if (assignedClubId.equals(newStudent.getFirstChoiceId())) {
                    newStudent.setFirstChooseStatus(1);
                } else if (assignedClubId.equals(newStudent.getSecondChoiceId())) {
                    newStudent.setSecondChooseStatus(1);
                } else if (assignedClubId.equals(newStudent.getThirdChoiceId())) {
                    newStudent.setThirdChooseStatus(1);
                } else {
                    //如果录取的社团不属于第一、第二、第三志愿之一，则把第一志愿状态改为已录取，其他两个志愿状态改为未录取
                    newStudent.setFirstChooseStatus(1);
                    newStudent.setSecondChooseStatus(0);
                    newStudent.setThirdChooseStatus(0);
                    //还要把第一志愿的社团改为录取社团，且把该社团的报名人数加1，且之前选择的第一志愿社团报名人数减1
                    Integer previousFirstChoiceId = newStudent.getFirstChoiceId();
                    if (previousFirstChoiceId != null) {
                        Integer m = -1;
                        m_clubMapper.updateClubNumberById(previousFirstChoiceId, m);
                    }
                    newStudent.setFirstChoiceId(assignedClubId);
                    Integer m = 1;
                    m_clubMapper.updateClubNumberById(assignedClubId, m);
                }

                //把该学生的录取信息插入clubResult表
                //插入记录之前，先删除clubResult表中该学生的记录
                m_clubResultMapper.deleteClubResultsByStudentIds(List.of(userId));
                String studentClass = newStudent.getStudentGrade() + newStudent.getStudentClass();
                m_clubResultMapper.insertClubResult(assignedClubId, userId, newStudent.getStudentName(), studentClass, admissionSemester, assignedClubName);
                //如果录取社团的剩余人数刚好剩余1，则把该社团的状态改为已满员
                //先拿到reslut中该社团的录取人数
                Integer admittedNumber = m_clubResultMapper.getCountByClubId(assignedClubId);
                //再拿到club中该社团的最大招生人数
                Integer maxStudents = m_clubMapper.getMaxStudentsById(assignedClubId);
                if (admittedNumber != null && maxStudents != null) {
                    Integer availableNum = maxStudents - admittedNumber;
                    if (availableNum != null && availableNum <= 1) {
                        String clubStatusFull = "已满员";
                        m_clubResultMapper.updateClubFinishedStatus(assignedClubId, clubStatusFull);
                    }
                }
            }


        }
        //然后插入clubStudentInfo表
        m_ClubStudentInfoMapper.insertClubStudentInfoFromVO(newStudent);

        return resp;
    }


    /**
     * 将一行数据解析为M_User对象
     */
    private M_User parseRowToUser(Row row) {
        try {
            M_User user = new M_User();

            // 第一列：姓名
            user.setName(getStudentCellStringValue(row.getCell(0)));

            // 第二列：密码
            user.setPassword(getStudentCellStringValue(row.getCell(1)));

            // 第三列：角色
            user.setRole(getStudentCellStringValue(row.getCell(2)));

            // 第四列：手机号
            user.setPhone(getStudentCellStringValue(row.getCell(3)));

            // 第五列：年级
            user.setGrade(getStudentCellStringValue(row.getCell(4)));
            // 注意这里的年级和班级是分开的，年级是第几学年，班级是具体的班级名称
            //第六列：班级
            user.setClassNumber(getStudentCellStringValue(row.getCell(5)));

            // 第六列：招生批次
            user.setAdmissionSemester(getStudentCellStringValue(row.getCell(6)));

            return user;

        } catch (Exception e) {
            System.err.println("解析第 " + (row.getRowNum() + 1) + " 行数据时发生错误: " + e.getMessage());
            return null;
        }
    }

    /**
     * 验证用户数据是否有效
     */
    private boolean isValidUser(M_User user) {
        // 姓名不能为空
        if (user.getName() == null || user.getName().trim().isEmpty()) {
            return false;
        }

        // 密码不能为空，如果没有提供密码，可以设置默认密码
        if (user.getPassword() == null || user.getPassword().trim().isEmpty()) {
            user.setPassword("123"); // 默认密码
        }

        // 角色不能为空，可以设置默认角色
        if (user.getRole() == null || user.getRole().trim().isEmpty()) {
            user.setRole("学生"); // 默认角色
        }


        return true;
    }


    /**
     * 获取单元格的字符串值
     */
    private String getStudentCellStringValue(Cell cell) {
        if (cell == null) return null;

        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getLocalDateTimeCellValue().toString();
                } else {
                    // 防止科学计数法
                    double numericValue = cell.getNumericCellValue();
                    if (numericValue == Math.floor(numericValue)) {
                        return String.valueOf((long) numericValue);
                    } else {
                        return String.valueOf(numericValue);
                    }
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                try {
                    return cell.getStringCellValue().trim();
                } catch (Exception e) {
                    try {
                        return String.valueOf(cell.getNumericCellValue());
                    } catch (Exception ex) {
                        return cell.getCellFormula();
                    }
                }
            default:
                return null;
        }
    }


    /**
     * 将一行数据解析为M_Club对象
     */
    private M_Club parseRowToClub(Row row) {
        try {
            M_Club club = new M_Club();

            // 第一列：社团名称
            club.setClubName(getCellStringValue(row.getCell(0)));

            // 第二列：教师
            club.setTeacher(getCellStringValue(row.getCell(1)));

            // 第三列：社团简介
            club.setDescription(getCellStringValue(row.getCell(2)));

            // 第四列：招生人数
            club.setMaxStudents(getCellIntegerValue(row.getCell(3)));

            // 第五列：招生年级
            club.setGrade(getCellStringValue(row.getCell(4)));

            // 第六列：截止时间
            club.setDeadline(getCellDateTimeValue(row.getCell(5)));

            // 第七列：上课地点
            club.setPosition(getCellStringValue(row.getCell(6)));

            // 第八列：社团类别
            club.setCategory(getCellStringValue(row.getCell(7)));

            // 第九列：招生批次
            club.setAdmissionSemester(getCellStringValue(row.getCell(8)));

            // 验证必要字段
            if (club.getClubName() == null || club.getClubName().trim().isEmpty()) {
                return null; // 跳过社团名称为空的行
            }

            return club;

        } catch (Exception e) {
            System.err.println("解析第 " + (row.getRowNum() + 1) + " 行数据时发生错误: " + e.getMessage());
            return null;
        }
    }

    /**
     * 获取单元格的字符串值
     */
    private String getCellStringValue(Cell cell) {
        if (cell == null) return null;

        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getLocalDateTimeCellValue().format(DATE_FORMATTER);
                } else {
                    // 防止科学计数法
                    double numericValue = cell.getNumericCellValue();
                    if (numericValue == Math.floor(numericValue)) {
                        return String.valueOf((long) numericValue);
                    } else {
                        return String.valueOf(numericValue);
                    }
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                try {
                    return cell.getStringCellValue().trim();
                } catch (Exception e) {
                    try {
                        return String.valueOf(cell.getNumericCellValue());
                    } catch (Exception ex) {
                        return cell.getCellFormula();
                    }
                }
            default:
                return null;
        }
    }

    /**
     * 获取单元格的整数值
     */
    private Integer getCellIntegerValue(Cell cell) {
        if (cell == null) return null;

        try {
            switch (cell.getCellType()) {
                case NUMERIC:
                    return (int) cell.getNumericCellValue();
                case STRING:
                    String value = cell.getStringCellValue().trim();
                    return value.isEmpty() ? null : Integer.parseInt(value);
                case FORMULA:
                    try {
                        return (int) cell.getNumericCellValue();
                    } catch (Exception e) {
                        return null;
                    }
                default:
                    return null;
            }
        } catch (Exception e) {
            System.err.println("解析整数失败: " + e.getMessage());
            return null;
        }
    }

    // 在类中定义格式器
    private static final DateTimeFormatter COMMA_SEPARATED_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy,MM,dd,HH,mm,ss");

    private LocalDateTime getCellDateTimeValue(Cell cell) {
        if (cell == null) return null;

        try {
            switch (cell.getCellType()) {
                case NUMERIC:
                    if (DateUtil.isCellDateFormatted(cell)) {
                        return cell.getLocalDateTimeCellValue();
                    }
                    return null;
                case STRING:
                    String dateString = cell.getStringCellValue().trim();
                    if (dateString.isEmpty()) return null;

                    // 按优先级尝试不同的日期格式
                    try {
                        // 1. 首先尝试逗号分隔格式
                        return LocalDateTime.parse(dateString, COMMA_SEPARATED_FORMATTER);
                    } catch (Exception e1) {
                        try {
                            // 2. 尝试原有格式（如果存在）
                            if (DATE_FORMATTER != null) {
                                return LocalDateTime.parse(dateString, DATE_FORMATTER);
                            }
                        } catch (Exception e2) {
                            // 可以继续添加其他格式...
                        }
                    }
                    return null;
                default:
                    return null;
            }
        } catch (Exception e) {
            System.err.println("解析日期失败: " + e.getMessage());
            return null;
        }
    }


    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 12);
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setFillForegroundColor(IndexedColors.WHITE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    private CellStyle createDataStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setAlignment(HorizontalAlignment.LEFT);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    private void createHeaderRowByClub(Sheet sheet, CellStyle headerStyle, String title) {
        // 创建标题行（合并单元格）
        Row titleRow = sheet.createRow(0);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue(title);
        titleCell.setCellStyle(headerStyle);

        // 合并标题单元格（跨4列）
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 3));

        // 创建列标题行
        Row headerRow = sheet.createRow(1);
        String[] headers = {"学生姓名", "学生班级", "上课教师", "上课地点"};

        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }
    }

    private void createHeaderRow(Sheet sheet, CellStyle headerStyle, String title) {
        // 创建标题行（合并单元格）
        Row titleRow = sheet.createRow(0);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue(title);
        titleCell.setCellStyle(headerStyle);
        // 合并标题单元格（跨4列）
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 3));
        // 创建列标题行
        Row headerRow = sheet.createRow(1);
        String[] headers = {"学生姓名", "社团名称", "上课教师", "上课地点"};

        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }
    }

    private void fillByClubDataRows(Sheet sheet, CellStyle dataStyle, List<M_ClubResultByClassVO> studentClubs) {
        int rowNum = 2; // 从第3行开始（0-based索引）

        for (M_ClubResultByClassVO club : studentClubs) {
            Row row = sheet.createRow(rowNum++);

            Cell cell0 = row.createCell(0);
            cell0.setCellValue(club.getStudentName());
            cell0.setCellStyle(dataStyle);

            Cell cell1 = row.createCell(1);
            cell1.setCellValue(club.getStudentClass());
            cell1.setCellStyle(dataStyle);

            Cell cell2 = row.createCell(2);
            cell2.setCellValue(club.getTeacher());
            cell2.setCellStyle(dataStyle);

            Cell cell3 = row.createCell(3);
            cell3.setCellValue(club.getPosition());
            cell3.setCellStyle(dataStyle);
        }
    }

    private void fillDataRows(Sheet sheet, CellStyle dataStyle, List<M_ClubResultByClassVO> studentClubs) {
        int rowNum = 2; // 从第3行开始（0-based索引）

        for (M_ClubResultByClassVO club : studentClubs) {
            Row row = sheet.createRow(rowNum++);

            Cell cell0 = row.createCell(0);
            cell0.setCellValue(club.getStudentName());
            cell0.setCellStyle(dataStyle);

            Cell cell1 = row.createCell(1);
            cell1.setCellValue(club.getClubName());
            cell1.setCellStyle(dataStyle);

            Cell cell2 = row.createCell(2);
            cell2.setCellValue(club.getTeacher());
            cell2.setCellStyle(dataStyle);

            Cell cell3 = row.createCell(3);
            cell3.setCellValue(club.getPosition());
            cell3.setCellStyle(dataStyle);
        }
    }

    private void autoSizeColumns(Sheet sheet) {
        for (int i = 0; i < 4; i++) {
            //设置固定宽度
            sheet.setColumnWidth(i, 20 * 256);
        }
    }

    private String getValidSheetName(String originalName) {
        // Excel sheet名称不能超过31字符，不能包含特殊字符
        String validName = originalName.replaceAll("[\\\\/?*\\[\\]]", "");
        if (validName.length() > 31) {
            validName = validName.substring(0, 31);
        }
        return validName;
    }


    private void assignClubToStudents(List<Integer> activeClubIds, Integer choiceLevel, String admissionSemester, Map<Integer, List<Integer>> groupByChoiceId) {
        for (Integer activeClubId : activeClubIds) {
            String finished = m_clubMapper.getFinishedById(activeClubId);
            if (finished.equals("未分配完")) {
                //查询clubsresult表，看看社团还有多少名额可以分配
                Integer usedNum = m_clubResultMapper.getCountByClubId(activeClubId);
                Integer maxNum = m_clubMapper.getMaxStudentsById(activeClubId);
                Integer availableNum = maxNum - usedNum;
                if (availableNum > 0) {
                    //拿到报名该社团的学生名单
                    List<Integer> studentIdList = groupByChoiceId.get(activeClubId);


                    if (studentIdList != null && !studentIdList.isEmpty()) {
                        //如果报名人数大于名额，则随机抽取
                        if (studentIdList.size() >= availableNum) {
                            //随机抽取availableNum个学生
                            List<Integer> selectedStudents = new ArrayList<>();
                            List<Integer> copyList = new ArrayList<>(studentIdList);
                            for (int i = 0; i < availableNum; i++) {
                                int randomIndex = (int) (Math.random() * copyList.size());
                                selectedStudents.add(copyList.get(randomIndex));
                                copyList.remove(randomIndex);
                            }
                            //把已经选择的所有学生改为已经录取，吧社团改为满员，吧学生和社团名单添加到clubresult表
                            //先把社团改为满员
                            String clubStatus = "已满员";
                            m_clubResultMapper.updateClubFinishedStatus(activeClubId, clubStatus);
                            //批量修改学生状态
                            if (choiceLevel == 1) {//批量修改第一志愿录取状态
                                m_ClubStudentInfoMapper.batchUpdateFirstStudentStatus(selectedStudents, activeClubId);
                            }
                            if (choiceLevel == 2) {//批量修改第二志愿录取状态
                                m_ClubStudentInfoMapper.batchUpdateSecondStudentStatus(selectedStudents, activeClubId);
                            }
                            if (choiceLevel == 3) {//批量修改第三志愿录取状态
                                m_ClubStudentInfoMapper.batchUpdateThirdStudentStatus(selectedStudents, activeClubId);
                            }
                            //把学生和社团名单添加到clubresult表
                            //首先要查询学生的姓名，还有学生的班级
                            //如果被选择了的学生不为空，那么删除clubResult表中这些学生的记录
                            if (!selectedStudents.isEmpty()) {
                                m_clubResultMapper.deleteClubResultsByStudentIds(selectedStudents);
                            }

                            for (Integer studentId : selectedStudents) {

                                M_ClubStudentInfo mClubStudentInfo = m_clubStudentInfoMapper.getAllByUserId(studentId);
                                String clubName = m_clubMapper.getClubNameById(activeClubId);
                                if (mClubStudentInfo != null) {
                                    String studentName = mClubStudentInfo.getStudentName();
                                    String studentClass = mClubStudentInfo.getStudentClass();
                                    String studentGrade = mClubStudentInfo.getStudentGrade();
                                    studentClass = studentGrade + studentClass;
                                    m_clubResultMapper.insertClubResult(activeClubId, studentId, studentName, studentClass, admissionSemester, clubName);
                                } else {
                                    log.info("学生{}没有找到对应的社团报名信息", studentId);
                                }
                            }

                        } else {
                            //如果报名人数小于名额，则全部录取，吧社团改为部分录取，吧学生和社团名单添加到clubresult表
                            //把社团改为部分录取
                            String clubStatus = "未分配完";
                            m_clubResultMapper.updateClubFinishedStatus(activeClubId, clubStatus);
                            //批量修改学生状态
                            if (choiceLevel == 1) {//批量修改第一志愿录取状态
                                m_ClubStudentInfoMapper.batchUpdateFirstStudentStatus(studentIdList, activeClubId);
                            }
                            if (choiceLevel == 2) {//批量修改第二志愿录取状态
                                m_ClubStudentInfoMapper.batchUpdateSecondStudentStatus(studentIdList, activeClubId);
                            }
                            if (choiceLevel == 3) {//批量修改第三志愿录取状态
                                m_ClubStudentInfoMapper.batchUpdateThirdStudentStatus(studentIdList, activeClubId);
                            }
                            //把学生和社团名单添加到clubresult表
                            if (!studentIdList.isEmpty()) {
                                m_clubResultMapper.deleteClubResultsByStudentIds(studentIdList);
                            }
                            //首先要查询学生的姓名，还有学生的班级
                            for (Integer studentId : studentIdList) {
                                M_ClubStudentInfo mClubStudentInfo = m_clubStudentInfoMapper.getAllByUserId(studentId);
                                String clubName = m_clubMapper.getClubNameById(activeClubId);
                                if (mClubStudentInfo != null) {
                                    String studentName = mClubStudentInfo.getStudentName();
                                    String studentClass = mClubStudentInfo.getStudentClass();
                                    String studentGrade = mClubStudentInfo.getStudentGrade();
                                    studentClass = studentGrade + studentClass;
                                    m_clubResultMapper.insertClubResult(activeClubId, studentId, studentName, studentClass, admissionSemester, clubName);
                                } else {
                                    log.info("学生{}没有找到对应的社团报名信息", studentId);
                                }
                            }

                        }
                    }
                } else {
                    //如果没有名额了，则把社团改为已满员
                    String clubStatus = "已满员";
                    m_clubResultMapper.updateClubFinishedStatus(activeClubId, clubStatus);
                }

            }
        }

    }
}