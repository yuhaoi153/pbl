package flyfish.service.impl;

import flyfish.mapper.RecordTaskMapper;
import flyfish.mapper.StudentInfoMapper;
import flyfish.pojo.DTO.PassTaskDTO;
import flyfish.pojo.PassTask;
import flyfish.pojo.Record;
import flyfish.pojo.VO.PassTaskVO;
import flyfish.service.RecordTaskService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class RecordTaskServiceImpl implements RecordTaskService {


    @Autowired
    private StudentInfoMapper studentInfoMapper;
    @Autowired
    private RecordTaskMapper recordTaskMapper;

    /**
     * 上传过关任务的数据
     * @param passTaskDTO
     * @return
     */
    public String uploadpassTask(PassTaskDTO passTaskDTO) throws Exception {
        String supplementary = passTaskDTO.getImages().stream()
                .collect(Collectors.joining("、"));
        //处理数据（多思考各种异常情况）
        //判断是否初始化:当前日期、班级、作业内容是否已经存在
        String classNumber = passTaskDTO.getClassNumber();
        String content = passTaskDTO.getContent();
        LocalDate checkdate = passTaskDTO.getCheckdate();
        String subject = passTaskDTO.getSubject();
        String school = passTaskDTO.getSchool();


        //处理value
        String value = passTaskDTO.getValue();
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
            throw  new Exception("没有扫到二维码");
        }
        //再反过来查询一遍学号，避免学号和姓名对不上
        if(nameList != null  && nameList.size()>0){
            studentNumberList = studentInfoMapper.getnewStudentNumberList(nameList,classNumber,school);
        }else {
            throw new Exception("没有扫到二维码");
        }


        List<PassTask> recordList = recordTaskMapper.isexist(classNumber,content,subject,school);
        List<PassTask> originrecordList = new ArrayList<>();
        if(recordList != null && recordList.size()>0){

        }else {
            //给全班的学生、姓名、作业内容、日期初始化，同时完成状态相反化
            List<String> allNameList = studentInfoMapper.getallName(classNumber,school);
            List<String> allstudentNumberList = studentInfoMapper.getallStudentNumber(classNumber,school);
            for(int i = 0 ; i<allstudentNumberList.size();i++){
                PassTask passTask = new PassTask();
                BeanUtils.copyProperties(passTaskDTO,passTask);
                passTask.setName(allNameList.get(i));
                passTask.setSupplementary(supplementary);
                passTask.setCreateTime(LocalDateTime.now());
                //反向设置
                if(passTaskDTO.getCompleted() == 1){
                    passTask.setCompleted(0);
                }else {
                    passTask.setCompleted(1);
                }
                originrecordList.add(passTask);
            };

            //全部上传到record数据库中
            recordTaskMapper.batchupload(originrecordList);
        }


        //更新数据库
        //开始构造record对象

        Integer completed = passTaskDTO.getCompleted();
        LocalDateTime nowtime = LocalDateTime.now();
        recordTaskMapper.updatecompleted(completed,nowtime,content,classNumber,nameList,subject,school);


        Integer uncompleted = 0;
        List<String> uncompletednameList = recordTaskMapper.getuncompleted(uncompleted,content,classNumber,subject,school);

        //没有登记完成作业的同学有：

        if(uncompletednameList !=null && uncompletednameList.size()>0){
            subject = passTaskDTO.getSubject();
            String feedbacknotification = "";
            feedbacknotification +=  subject + "《" + content + "》" ;
            feedbacknotification += "<br>";


            feedbacknotification += "没有完成任务的同学有:"+"<br>";
            feedbacknotification += String.join("、",uncompletednameList);
            return feedbacknotification;
        } else {
            return "全部同学完成了任务";
        }







    }

    /**
     * 自动查询未完成作业的名单
     * @param subject
     * @param classNumber
     * @param content
     * @return
     */
    @Override
    public String querypasstaskUncompleted(String subject, String classNumber, String content,String school) {

        Integer uncompleted = 0;
        List<String> uncompletednameList = recordTaskMapper.getuncompleted(uncompleted,content,classNumber,subject,school);

        //没有登记完成作业的同学有：

        if(uncompletednameList !=null && uncompletednameList.size()>0){

            String feedbacknotification = "";
            feedbacknotification +=  subject + "《" + content + "》" ;
            feedbacknotification += "<br>";


            feedbacknotification += "没有完成任务的同学有:"+"<br>";
            feedbacknotification += String.join("、",uncompletednameList);
            return feedbacknotification;
        } else {
            return "全部同学完成了任务";
        }


    }
}

