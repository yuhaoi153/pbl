package flyfish.service.impl;

import flyfish.mapper.AccumulateScoreMapper;
import flyfish.mapper.GroupNameListMapper;
import flyfish.mapper.PerformMapper;
import flyfish.mapper.StudentInfoMapper;
import flyfish.pojo.DTO.GroupFeedbackDTO;
import flyfish.pojo.GroupNameList;
import flyfish.pojo.Perform;
import flyfish.pojo.VO.GroupFeedbackVO;
import flyfish.service.GroupFeedbackService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class GroupFeedbackImpl implements GroupFeedbackService {
    @Autowired
    private StudentInfoMapper studentInfoMapper;
    @Autowired
    private GroupNameListMapper groupNameListMapper;
    @Autowired
    private PerformMapper performMapper;
    @Autowired
    private AccumulateScoreMapper accumulateScoreMapper;

    @Override
    public List<GroupFeedbackVO> uploadGroup(GroupFeedbackDTO groupFeedbackDTO) {
        String classNumber = groupFeedbackDTO.getClassNumber();
        String groupNumber = groupFeedbackDTO.getGroup();
        String subject = groupFeedbackDTO.getSubject();
        String value = groupFeedbackDTO.getValue();
        String school = groupFeedbackDTO.getSchool();


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
            return null;
        }

        String nameListString = String.join("、", nameList);

        //把组别，班级和学生名单存入数据
        //先删除当前班级当前小组
        groupNameListMapper.deleteGroup(classNumber,groupNumber,subject,school);
        LocalDateTime createTime = LocalDateTime.now();
        //再新增当前小组
        groupNameListMapper.addGroup(classNumber,groupNumber,nameListString,subject, createTime,school);
        List<GroupFeedbackVO> groupFeedbackVOList = groupNameListMapper.queryAllGroup(classNumber,subject,school);

        return groupFeedbackVOList;

    }

    //查询分组信息
    @Override
    public List<GroupFeedbackVO> queryGroup(String subject, String classNumber, String school) {
        List<GroupFeedbackVO> groupFeedbackVOList = groupNameListMapper.queryAllGroup(classNumber,subject,school);
        return groupFeedbackVOList;
    }

    //添加分组分数
    @Override
    public List<GroupFeedbackVO> addGroupScore(GroupFeedbackDTO groupFeedbackDTO) {
        String classNumber = groupFeedbackDTO.getClassNumber();
        String groupNumber = groupFeedbackDTO.getGroup();
        String subject = groupFeedbackDTO.getSubject();
        Integer score = groupFeedbackDTO.getScore();
        String school = groupFeedbackDTO.getSchool();
        String nameListString = groupNameListMapper.getNameList(classNumber,groupNumber,subject,school);
        if(nameListString == null || nameListString.equals("")){
            GroupFeedbackVO groupFeedbackVO = new GroupFeedbackVO();
            groupFeedbackVO.setName("小组成员为0");
            return List.of(groupFeedbackVO);
        }
        String[] nameList = nameListString.split("、");

        for (String name : nameList) {
            Perform perform = new Perform();
            perform.setName(name);
            perform.setClassNumber(classNumber);
            perform.setSubject(subject);
            perform.setScore(score);
            perform.setCheckdate(LocalDate.now());
            perform.setSituation("表扬");
            perform.setReason("小组表现优秀");
            perform.setSchool(school);
            performMapper.addSinglePerform(perform);
            accumulateScoreMapper.updateScannerwellScore(name,classNumber,subject,score,school);


        }
        groupNameListMapper.addGroupScore(classNumber,groupNumber,subject,score,school);
        List<GroupFeedbackVO> groupFeedbackVOList = groupNameListMapper.queryAllGroup(classNumber,subject,school);
        return groupFeedbackVOList;

    }

    @Override
    public List<GroupFeedbackVO> punishGroupScore(GroupFeedbackDTO groupFeedbackDTO) {
        String classNumber = groupFeedbackDTO.getClassNumber();
        String groupNumber = groupFeedbackDTO.getGroup();
        String subject = groupFeedbackDTO.getSubject();
        Integer score = groupFeedbackDTO.getScore();
        String school = groupFeedbackDTO.getSchool();
        String nameListString = groupNameListMapper.getNameList(classNumber,groupNumber,subject,school);
        if(nameListString == null || nameListString.equals("")){
            GroupFeedbackVO groupFeedbackVO = new GroupFeedbackVO();
            groupFeedbackVO.setName("小组成员为0");
            return List.of(groupFeedbackVO);
        }
        String[] nameList = nameListString.split("、");

        for (String name : nameList) {
            Perform perform = new Perform();
            perform.setName(name);
            perform.setClassNumber(classNumber);
            perform.setSubject(subject);
            perform.setScore(-score);
            perform.setCheckdate(LocalDate.now());
            perform.setSituation("批评");
            perform.setReason("小组表现不佳");
            perform.setSchool(school);
            performMapper.addSinglePerform(perform);
            accumulateScoreMapper.updateScannerbadScore(name,classNumber,subject,score,school);
        }
        groupNameListMapper.punishGroupScore(classNumber,groupNumber,subject,score,school);
        List<GroupFeedbackVO> groupFeedbackVOList = groupNameListMapper.queryAllGroup(classNumber,subject,school);
        return groupFeedbackVOList;
    }
}
